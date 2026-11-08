package setting;

import fungsi.WarnaTable;
import fungsi.akses;
import fungsi.batasInput;
import fungsi.koneksiDB;
import fungsi.validasi;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 * Role hak akses user (khusus Admin Utama). Role = kumpulan kolom hak akses tabel user (template).
 * Memasang role menulis hak akses ke kolom user: "Tambahkan" (hak lama tetap) atau "Ganti" (hak user = gabungan role).
 * Login (akses.java) tidak berubah. Tabel: role_user, role_user_akses, user_role (lihat update_role_user.sql).
 */
public final class DlgRoleUser extends javax.swing.JDialog {
    private final Connection koneksi=koneksiDB.condb();
    private final validasi Valid=new validasi();
    private final DefaultTableModel modeRole,modeAkses,modeUser,modePilih;
    /** kolom hak akses tabel user (enum true/false), urut sesuai tabel */
    private final List<String> kolomAkses=new ArrayList<>();
    /** hak akses yang dicentang untuk role yang sedang diedit */
    private final Set<String> terpilih=new LinkedHashSet<>();
    /** user yang dicentang di tab Role Per User (tetap tersimpan walau daftar difilter) */
    private final Set<String> userDicentang=new LinkedHashSet<>();
    private String kdRoleLama="";
    private boolean memuat=false;

    /** Role bawaan, isinya diatur Admin (centang / salin dari user / script SQL). */
    private static final String[][] ROLE_BAWAAN={
        {"KASIR","Kasir"},{"APOTIK","Apotik / Farmasi"},{"UGD","UGD / IGD"},{"MCU","MCU"},{"LAB","Laboratorium"},
        {"RADIOLOGI","Radiologi"},{"DOKTER_UMUM","Dokter Umum"},{"KESLING","Kesehatan Lingkungan"},{"RM","Rekam Medis"},
        {"ADMIN","Admin"},{"AKUNTANSI","Akuntansi"},{"GIZI","Gizi"},{"RANAP","Rawat Inap"}
    };

    /** Creates new form DlgRoleUser
     * @param parent
     * @param modal */
    public DlgRoleUser(java.awt.Frame parent,boolean modal) {
        super(parent,modal);
        initComponents();
        this.setLocation(8,1);
        setSize(1000,650);

        modeRole=new DefaultTableModel(null,new Object[]{"Kode","Nama Role","Keterangan","Hak","User"}){
            @Override public boolean isCellEditable(int r,int c){return false;}
        };
        tbRole.setModel(modeRole);
        aturKolom(tbRole,new int[]{95,140,165,35,35});

        modeAkses=new DefaultTableModel(null,new Object[]{"P","Hak Akses","Kolom"}){
            @Override public boolean isCellEditable(int r,int c){return c==0;}
            @Override public Class<?> getColumnClass(int c){return c==0?Boolean.class:Object.class;}
        };
        tbAkses.setModel(modeAkses);
        aturKolom(tbAkses,new int[]{25,450,280});
        modeAkses.addTableModelListener(e->{
            if(!memuat&&e.getColumn()==0&&e.getFirstRow()>=0){
                for(int r=e.getFirstRow();r<=e.getLastRow()&&r<modeAkses.getRowCount();r++){
                    String kolom=modeAkses.getValueAt(r,2).toString();
                    if(Boolean.TRUE.equals(modeAkses.getValueAt(r,0))){
                        terpilih.add(kolom);
                    }else{
                        terpilih.remove(kolom);
                    }
                }
                hitungTerpilih();
            }
        });

        modeUser=new DefaultTableModel(null,new Object[]{"P","ID User","Nama","Role"}){
            @Override public boolean isCellEditable(int r,int c){return c==0;}
            @Override public Class<?> getColumnClass(int c){return c==0?Boolean.class:Object.class;}
        };
        tbUser.setModel(modeUser);
        aturKolom(tbUser,new int[]{25,100,170,150});
        modeUser.addTableModelListener(e->{
            if(!memuat&&e.getColumn()==0&&e.getFirstRow()>=0){
                for(int r=e.getFirstRow();r<=e.getLastRow()&&r<modeUser.getRowCount();r++){
                    String id=modeUser.getValueAt(r,1).toString();
                    if(Boolean.TRUE.equals(modeUser.getValueAt(r,0))){
                        userDicentang.add(id);
                    }else{
                        userDicentang.remove(id);
                    }
                }
                hitungUser();
            }
        });

        modePilih=new DefaultTableModel(null,new Object[]{"P","Kode","Nama Role","Hak"}){
            @Override public boolean isCellEditable(int r,int c){return c==0;}
            @Override public Class<?> getColumnClass(int c){return c==0?Boolean.class:Object.class;}
        };
        tbRolePilih.setModel(modePilih);
        aturKolom(tbRolePilih,new int[]{25,110,220,50});

        KdRole.setDocument(new batasInput((byte)20).getKata(KdRole));
        NmRole.setDocument(new batasInput((int)100).getKata(NmRole));
        KetRole.setDocument(new batasInput((int)200).getKata(KetRole));
        TCariAkses.setDocument(new batasInput((int)100).getKata(TCariAkses));
        TCariUser.setDocument(new batasInput((int)100).getKata(TCariUser));

        String[] kategori=new String[DaftarHakAkses.KATEGORI.length+1];
        kategori[0]="Semua Kategori";
        System.arraycopy(DaftarHakAkses.KATEGORI,0,kategori,1,DaftarHakAkses.KATEGORI.length);
        CmbKategori.setModel(new javax.swing.DefaultComboBoxModel<>(kategori));

        buatTabel();
        muatKolomAkses();
        tampilRole();
        tampilUser();
        tampilAkses();
    }

    private void aturKolom(JTable t,int[] lebar){
        t.setPreferredScrollableViewportSize(new Dimension(500,500));
        // tabel hak akses & pilih role melebar penuh; tabel lain bisa digeser horizontal
        t.setAutoResizeMode((t==tbAkses||t==tbRolePilih)?JTable.AUTO_RESIZE_LAST_COLUMN:JTable.AUTO_RESIZE_OFF);
        for(int i=0;i<lebar.length;i++){
            t.getColumnModel().getColumn(i).setPreferredWidth(lebar[i]);
        }
        if(t.getColumnClass(0)==Boolean.class){
            // kolom centang tidak ikut melebar
            t.getColumnModel().getColumn(0).setMinWidth(lebar[0]);
            t.getColumnModel().getColumn(0).setMaxWidth(lebar[0]);
        }
        t.setDefaultRenderer(Object.class,new WarnaTable());
        // area viewport di luar baris/kolom tabel digambar putih (tanpa ini tersisa bekas gambar/tooltip)
        t.setFillsViewportHeight(true);
        if(t.getParent() instanceof javax.swing.JViewport){
            t.getParent().setBackground(java.awt.Color.WHITE);
            ((javax.swing.JViewport)t.getParent()).setOpaque(true);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        internalFrame1 = new widget.InternalFrame();
        panelGlass8 = new widget.panelisi();
        LInfo = new widget.Label();
        BtnKeluar = new widget.Button();
        TabRawat = new widget.TabPane();
        internalFrame2 = new widget.InternalFrame();
        PanelRole = new widget.PanelBiasa();
        ScrollRole = new widget.ScrollPane();
        tbRole = new widget.Table();
        panelRoleTombol = new widget.panelisi();
        BtnRoleBaru = new widget.Button();
        BtnRoleGanti = new widget.Button();
        BtnRoleHapus = new widget.Button();
        PanelAkses = new widget.PanelBiasa();
        FormRole = new widget.PanelBiasa();
        jLabel1 = new widget.Label();
        KdRole = new widget.TextBox();
        jLabel2 = new widget.Label();
        NmRole = new widget.TextBox();
        jLabel3 = new widget.Label();
        KetRole = new widget.TextBox();
        jLabel4 = new widget.Label();
        CmbKategori = new widget.ComboBox();
        jLabel5 = new widget.Label();
        CmbTampil = new widget.ComboBox();
        jLabel6 = new widget.Label();
        TCariAkses = new widget.TextBox();
        BtnCariAkses = new widget.Button();
        jLabel7 = new widget.Label();
        CmbSalinUser = new widget.ComboBox();
        BtnSalinUser = new widget.Button();
        ScrollAkses = new widget.ScrollPane();
        tbAkses = new widget.Table();
        panelAksesTombol = new widget.panelisi();
        BtnSimpanRole = new widget.Button();
        BtnCentang = new widget.Button();
        BtnKosongkan = new widget.Button();
        BtnTerapkanUlang = new widget.Button();
        LJumlahAkses = new widget.Label();
        internalFrame3 = new widget.InternalFrame();
        PanelUser = new widget.PanelBiasa();
        panelCariUser = new widget.panelisi();
        jLabel8 = new widget.Label();
        TCariUser = new widget.TextBox();
        BtnCariUser = new widget.Button();
        BtnAllUser = new widget.Button();
        BtnCentangUser = new widget.Button();
        BtnBatalCentangUser = new widget.Button();
        ScrollUser = new widget.ScrollPane();
        tbUser = new widget.Table();
        PanelPasang = new widget.PanelBiasa();
        FormUser = new widget.PanelBiasa();
        jLabel9 = new widget.Label();
        TUser = new widget.TextBox();
        TNamaUser = new widget.TextBox();
        LJumlahUser = new widget.Label();
        ScrollPilih = new widget.ScrollPane();
        tbRolePilih = new widget.Table();
        panelPasangTombol = new widget.panelisi();
        jLabel10 = new widget.Label();
        CmbMode = new widget.ComboBox();
        BtnPasang = new widget.Button();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Role Hak Akses User ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 12), new java.awt.Color(50, 50, 50))); // NOI18N
        internalFrame1.setFont(new java.awt.Font("Tahoma", 2, 12)); // NOI18N
        internalFrame1.setName("internalFrame1"); // NOI18N
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

        panelGlass8.setName("panelGlass8"); // NOI18N
        panelGlass8.setPreferredSize(new java.awt.Dimension(44, 54));
        panelGlass8.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        LInfo.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LInfo.setText("Role = kumpulan hak akses. Pasang satu atau beberapa role (gabungan) ke user di tab Role Per User.");
        LInfo.setName("LInfo"); // NOI18N
        LInfo.setPreferredSize(new java.awt.Dimension(760, 23));
        panelGlass8.add(LInfo);

        BtnKeluar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/exit.png"))); // NOI18N
        BtnKeluar.setMnemonic(75);
        BtnKeluar.setText("Keluar");
        BtnKeluar.setToolTipText("Alt+K");
        BtnKeluar.setName("BtnKeluar"); // NOI18N
        BtnKeluar.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnKeluar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnKeluarActionPerformed(evt);
            }
        });
        BtnKeluar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnKeluarKeyPressed(evt);
            }
        });
        panelGlass8.add(BtnKeluar);

        internalFrame1.add(panelGlass8, java.awt.BorderLayout.PAGE_END);

        TabRawat.setBackground(new java.awt.Color(254, 255, 254));
        TabRawat.setForeground(new java.awt.Color(50, 50, 50));
        TabRawat.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        TabRawat.setName("TabRawat"); // NOI18N
        TabRawat.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                TabRawatMouseClicked(evt);
            }
        });

        internalFrame2.setBorder(null);
        internalFrame2.setName("internalFrame2"); // NOI18N
        internalFrame2.setLayout(new java.awt.BorderLayout(1, 1));

        PanelRole.setBorder(null);
        PanelRole.setName("PanelRole"); // NOI18N
        PanelRole.setPreferredSize(new java.awt.Dimension(480, 100));
        PanelRole.setLayout(new java.awt.BorderLayout(1, 1));

        ScrollRole.setName("ScrollRole"); // NOI18N
        ScrollRole.setOpaque(true);

        tbRole.setToolTipText("Klik role untuk melihat / mengubah hak aksesnya");
        tbRole.setName("tbRole"); // NOI18N
        tbRole.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbRoleMouseClicked(evt);
            }
        });
        tbRole.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                tbRoleKeyReleased(evt);
            }
        });
        ScrollRole.setViewportView(tbRole);

        PanelRole.add(ScrollRole, java.awt.BorderLayout.CENTER);

        panelRoleTombol.setName("panelRoleTombol"); // NOI18N
        panelRoleTombol.setPreferredSize(new java.awt.Dimension(44, 44));
        panelRoleTombol.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        BtnRoleBaru.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Cancel-2-16x16.png"))); // NOI18N
        BtnRoleBaru.setText("Baru");
        BtnRoleBaru.setToolTipText("Kosongkan isian untuk membuat role baru");
        BtnRoleBaru.setName("BtnRoleBaru"); // NOI18N
        BtnRoleBaru.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnRoleBaru.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnRoleBaruActionPerformed(evt);
            }
        });
        panelRoleTombol.add(BtnRoleBaru);

        BtnRoleGanti.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/inventaris.png"))); // NOI18N
        BtnRoleGanti.setText("Ganti");
        BtnRoleGanti.setToolTipText("Ganti kode, nama dan keterangan role terpilih (hak akses & pemegang ikut kode baru)");
        BtnRoleGanti.setName("BtnRoleGanti"); // NOI18N
        BtnRoleGanti.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnRoleGanti.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnRoleGantiActionPerformed(evt);
            }
        });
        panelRoleTombol.add(BtnRoleGanti);

        BtnRoleHapus.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/stop_f2.png"))); // NOI18N
        BtnRoleHapus.setText("Hapus");
        BtnRoleHapus.setToolTipText("Hapus role terpilih (hak akses user yang sudah terpasang tidak dicabut)");
        BtnRoleHapus.setName("BtnRoleHapus"); // NOI18N
        BtnRoleHapus.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnRoleHapus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnRoleHapusActionPerformed(evt);
            }
        });
        panelRoleTombol.add(BtnRoleHapus);

        PanelRole.add(panelRoleTombol, java.awt.BorderLayout.PAGE_END);

        internalFrame2.add(PanelRole, java.awt.BorderLayout.WEST);

        PanelAkses.setBorder(null);
        PanelAkses.setName("PanelAkses"); // NOI18N
        PanelAkses.setLayout(new java.awt.BorderLayout(1, 1));

        FormRole.setBackground(new java.awt.Color(255, 255, 255));
        FormRole.setBorder(null);
        FormRole.setName("FormRole"); // NOI18N
        FormRole.setPreferredSize(new java.awt.Dimension(600, 100));
        FormRole.setLayout(null);

        jLabel1.setText("Kode Role :");
        jLabel1.setName("jLabel1"); // NOI18N
        FormRole.add(jLabel1);
        jLabel1.setBounds(0, 8, 80, 23);

        KdRole.setName("KdRole"); // NOI18N
        KdRole.setToolTipText("Kode unik role, misal KASIR");
        FormRole.add(KdRole);
        KdRole.setBounds(84, 8, 100, 23);

        jLabel2.setText("Nama Role :");
        jLabel2.setName("jLabel2"); // NOI18N
        FormRole.add(jLabel2);
        jLabel2.setBounds(186, 8, 78, 23);

        NmRole.setName("NmRole"); // NOI18N
        FormRole.add(NmRole);
        NmRole.setBounds(267, 8, 180, 23);

        jLabel3.setText("Ket. :");
        jLabel3.setName("jLabel3"); // NOI18N
        FormRole.add(jLabel3);
        jLabel3.setBounds(450, 8, 40, 23);

        KetRole.setName("KetRole"); // NOI18N
        FormRole.add(KetRole);
        KetRole.setBounds(494, 8, 180, 23);

        jLabel4.setText("Kategori :");
        jLabel4.setName("jLabel4"); // NOI18N
        FormRole.add(jLabel4);
        jLabel4.setBounds(0, 38, 80, 23);

        CmbKategori.setName("CmbKategori"); // NOI18N
        CmbKategori.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                CmbKategoriItemStateChanged(evt);
            }
        });
        FormRole.add(CmbKategori);
        CmbKategori.setBounds(84, 38, 360, 23);

        jLabel5.setText("Tampil :");
        jLabel5.setName("jLabel5"); // NOI18N
        FormRole.add(jLabel5);
        jLabel5.setBounds(448, 38, 45, 23);

        CmbTampil.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Semua", "Yang dicentang", "Yang belum dicentang" }));
        CmbTampil.setName("CmbTampil"); // NOI18N
        CmbTampil.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                CmbTampilItemStateChanged(evt);
            }
        });
        FormRole.add(CmbTampil);
        CmbTampil.setBounds(497, 38, 177, 23);

        jLabel6.setText("Cari :");
        jLabel6.setName("jLabel6"); // NOI18N
        FormRole.add(jLabel6);
        jLabel6.setBounds(0, 68, 80, 23);

        TCariAkses.setToolTipText("Cari nama hak akses atau nama kolom, tekan Enter");
        TCariAkses.setName("TCariAkses"); // NOI18N
        TCariAkses.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TCariAksesKeyPressed(evt);
            }
        });
        FormRole.add(TCariAkses);
        TCariAkses.setBounds(84, 68, 180, 23);

        BtnCariAkses.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png"))); // NOI18N
        BtnCariAkses.setToolTipText("Cari");
        BtnCariAkses.setName("BtnCariAkses"); // NOI18N
        BtnCariAkses.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnCariAksesActionPerformed(evt);
            }
        });
        FormRole.add(BtnCariAkses);
        BtnCariAkses.setBounds(266, 68, 28, 23);

        jLabel7.setText("Salin dari :");
        jLabel7.setName("jLabel7"); // NOI18N
        FormRole.add(jLabel7);
        jLabel7.setBounds(296, 68, 70, 23);

        CmbSalinUser.setToolTipText("User yang hak aksesnya disalin ke role ini");
        CmbSalinUser.setName("CmbSalinUser"); // NOI18N
        FormRole.add(CmbSalinUser);
        CmbSalinUser.setBounds(370, 68, 220, 23);

        BtnSalinUser.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/editcopy.png"))); // NOI18N
        BtnSalinUser.setText("Salin");
        BtnSalinUser.setToolTipText("Centang hak akses role sama dengan hak akses user terpilih (belum disimpan)");
        BtnSalinUser.setName("BtnSalinUser"); // NOI18N
        BtnSalinUser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnSalinUserActionPerformed(evt);
            }
        });
        FormRole.add(BtnSalinUser);
        BtnSalinUser.setBounds(594, 68, 80, 23);

        PanelAkses.add(FormRole, java.awt.BorderLayout.NORTH);

        ScrollAkses.setName("ScrollAkses"); // NOI18N
        ScrollAkses.setOpaque(true);

        tbAkses.setToolTipText("Centang hak akses yang dimiliki role, lalu Simpan Role");
        tbAkses.setName("tbAkses"); // NOI18N
        ScrollAkses.setViewportView(tbAkses);

        PanelAkses.add(ScrollAkses, java.awt.BorderLayout.CENTER);

        panelAksesTombol.setName("panelAksesTombol"); // NOI18N
        panelAksesTombol.setPreferredSize(new java.awt.Dimension(44, 44));
        panelAksesTombol.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        BtnSimpanRole.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/save-16x16.png"))); // NOI18N
        BtnSimpanRole.setText("Simpan Role");
        BtnSimpanRole.setToolTipText("Simpan kode, nama dan hak akses role");
        BtnSimpanRole.setName("BtnSimpanRole"); // NOI18N
        BtnSimpanRole.setPreferredSize(new java.awt.Dimension(115, 30));
        BtnSimpanRole.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnSimpanRoleActionPerformed(evt);
            }
        });
        panelAksesTombol.add(BtnSimpanRole);

        BtnCentang.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/checked.png"))); // NOI18N
        BtnCentang.setText("Centang");
        BtnCentang.setToolTipText("Centang semua hak akses yang sedang tampil");
        BtnCentang.setName("BtnCentang"); // NOI18N
        BtnCentang.setPreferredSize(new java.awt.Dimension(95, 30));
        BtnCentang.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnCentangActionPerformed(evt);
            }
        });
        panelAksesTombol.add(BtnCentang);

        BtnKosongkan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Cancel-2-16x16.png"))); // NOI18N
        BtnKosongkan.setText("Kosongkan");
        BtnKosongkan.setToolTipText("Hilangkan centang semua hak akses yang sedang tampil");
        BtnKosongkan.setName("BtnKosongkan"); // NOI18N
        BtnKosongkan.setPreferredSize(new java.awt.Dimension(105, 30));
        BtnKosongkan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnKosongkanActionPerformed(evt);
            }
        });
        panelAksesTombol.add(BtnKosongkan);

        BtnTerapkanUlang.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/refresh.png"))); // NOI18N
        BtnTerapkanUlang.setText("Terapkan Ulang");
        BtnTerapkanUlang.setToolTipText("Tulis ulang hak akses semua user yang memegang role ini");
        BtnTerapkanUlang.setName("BtnTerapkanUlang"); // NOI18N
        BtnTerapkanUlang.setPreferredSize(new java.awt.Dimension(145, 30));
        BtnTerapkanUlang.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnTerapkanUlangActionPerformed(evt);
            }
        });
        panelAksesTombol.add(BtnTerapkanUlang);

        LJumlahAkses.setText("0 dicentang");
        LJumlahAkses.setName("LJumlahAkses"); // NOI18N
        LJumlahAkses.setPreferredSize(new java.awt.Dimension(230, 23));
        panelAksesTombol.add(LJumlahAkses);

        PanelAkses.add(panelAksesTombol, java.awt.BorderLayout.PAGE_END);

        internalFrame2.add(PanelAkses, java.awt.BorderLayout.CENTER);

        TabRawat.addTab("Data Role", internalFrame2);

        internalFrame3.setBorder(null);
        internalFrame3.setName("internalFrame3"); // NOI18N
        internalFrame3.setLayout(new java.awt.BorderLayout(1, 1));

        PanelUser.setBorder(null);
        PanelUser.setName("PanelUser"); // NOI18N
        PanelUser.setPreferredSize(new java.awt.Dimension(470, 100));
        PanelUser.setLayout(new java.awt.BorderLayout(1, 1));

        panelCariUser.setName("panelCariUser"); // NOI18N
        panelCariUser.setPreferredSize(new java.awt.Dimension(44, 44));
        panelCariUser.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        jLabel8.setText("Cari User :");
        jLabel8.setName("jLabel8"); // NOI18N
        jLabel8.setPreferredSize(new java.awt.Dimension(70, 23));
        panelCariUser.add(jLabel8);

        TCariUser.setToolTipText("Cari ID user, nama atau kode role, tekan Enter");
        TCariUser.setName("TCariUser"); // NOI18N
        TCariUser.setPreferredSize(new java.awt.Dimension(200, 23));
        TCariUser.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TCariUserKeyPressed(evt);
            }
        });
        panelCariUser.add(TCariUser);

        BtnCariUser.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png"))); // NOI18N
        BtnCariUser.setToolTipText("Cari");
        BtnCariUser.setName("BtnCariUser"); // NOI18N
        BtnCariUser.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnCariUser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnCariUserActionPerformed(evt);
            }
        });
        panelCariUser.add(BtnCariUser);

        BtnAllUser.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Search-16x16.png"))); // NOI18N
        BtnAllUser.setToolTipText("Tampilkan semua");
        BtnAllUser.setName("BtnAllUser"); // NOI18N
        BtnAllUser.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnAllUser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnAllUserActionPerformed(evt);
            }
        });
        panelCariUser.add(BtnAllUser);

        BtnCentangUser.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/checked.png"))); // NOI18N
        BtnCentangUser.setToolTipText("Centang semua user yang tampil");
        BtnCentangUser.setName("BtnCentangUser"); // NOI18N
        BtnCentangUser.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnCentangUser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnCentangUserActionPerformed(evt);
            }
        });
        panelCariUser.add(BtnCentangUser);

        BtnBatalCentangUser.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Cancel-2-16x16.png"))); // NOI18N
        BtnBatalCentangUser.setToolTipText("Hilangkan centang semua user");
        BtnBatalCentangUser.setName("BtnBatalCentangUser"); // NOI18N
        BtnBatalCentangUser.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnBatalCentangUser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnBatalCentangUserActionPerformed(evt);
            }
        });
        panelCariUser.add(BtnBatalCentangUser);

        PanelUser.add(panelCariUser, java.awt.BorderLayout.NORTH);

        ScrollUser.setName("ScrollUser"); // NOI18N
        ScrollUser.setOpaque(true);

        tbUser.setToolTipText("Centang beberapa user untuk dipasang role sekaligus; klik nama untuk melihat role-nya");
        tbUser.setName("tbUser"); // NOI18N
        tbUser.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbUserMouseClicked(evt);
            }
        });
        tbUser.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                tbUserKeyReleased(evt);
            }
        });
        ScrollUser.setViewportView(tbUser);

        PanelUser.add(ScrollUser, java.awt.BorderLayout.CENTER);

        internalFrame3.add(PanelUser, java.awt.BorderLayout.WEST);

        PanelPasang.setBorder(null);
        PanelPasang.setName("PanelPasang"); // NOI18N
        PanelPasang.setLayout(new java.awt.BorderLayout(1, 1));

        FormUser.setBackground(new java.awt.Color(255, 255, 255));
        FormUser.setBorder(null);
        FormUser.setName("FormUser"); // NOI18N
        FormUser.setPreferredSize(new java.awt.Dimension(500, 40));
        FormUser.setLayout(null);

        jLabel9.setText("User :");
        jLabel9.setName("jLabel9"); // NOI18N
        FormUser.add(jLabel9);
        jLabel9.setBounds(0, 8, 60, 23);

        TUser.setEditable(false);
        TUser.setHighlighter(null);
        TUser.setName("TUser"); // NOI18N
        FormUser.add(TUser);
        TUser.setBounds(64, 8, 110, 23);

        TNamaUser.setEditable(false);
        TNamaUser.setHighlighter(null);
        TNamaUser.setName("TNamaUser"); // NOI18N
        FormUser.add(TNamaUser);
        TNamaUser.setBounds(176, 8, 220, 23);

        LJumlahUser.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LJumlahUser.setForeground(new java.awt.Color(0, 90, 160));
        LJumlahUser.setText("0 user dicentang");
        LJumlahUser.setName("LJumlahUser"); // NOI18N
        FormUser.add(LJumlahUser);
        LJumlahUser.setBounds(400, 8, 150, 23);

        PanelPasang.add(FormUser, java.awt.BorderLayout.NORTH);

        ScrollPilih.setName("ScrollPilih"); // NOI18N
        ScrollPilih.setOpaque(true);

        tbRolePilih.setToolTipText("Centang satu atau beberapa role untuk dipasang ke user");
        tbRolePilih.setName("tbRolePilih"); // NOI18N
        ScrollPilih.setViewportView(tbRolePilih);

        PanelPasang.add(ScrollPilih, java.awt.BorderLayout.CENTER);

        panelPasangTombol.setName("panelPasangTombol"); // NOI18N
        panelPasangTombol.setPreferredSize(new java.awt.Dimension(44, 44));
        panelPasangTombol.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        jLabel10.setText("Cara :");
        jLabel10.setName("jLabel10"); // NOI18N
        jLabel10.setPreferredSize(new java.awt.Dimension(40, 23));
        panelPasangTombol.add(jLabel10);

        CmbMode.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Tambahkan ke hak akses yang ada", "Ganti seluruh hak akses dengan gabungan role" }));
        CmbMode.setName("CmbMode"); // NOI18N
        CmbMode.setPreferredSize(new java.awt.Dimension(290, 23));
        panelPasangTombol.add(CmbMode);

        BtnPasang.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/inventaris.png"))); // NOI18N
        BtnPasang.setText("Pasang Role");
        BtnPasang.setToolTipText("Tulis hak akses gabungan role terpilih ke user yang dicentang (atau user yang diklik)");
        BtnPasang.setName("BtnPasang"); // NOI18N
        BtnPasang.setPreferredSize(new java.awt.Dimension(130, 30));
        BtnPasang.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnPasangActionPerformed(evt);
            }
        });
        panelPasangTombol.add(BtnPasang);

        PanelPasang.add(panelPasangTombol, java.awt.BorderLayout.PAGE_END);

        internalFrame3.add(PanelPasang, java.awt.BorderLayout.CENTER);

        TabRawat.addTab("Role Per User", internalFrame3);

        internalFrame1.add(TabRawat, java.awt.BorderLayout.CENTER);

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);

        pack();
    }
    // </editor-fold>//GEN-END:initComponents

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        if(tbRole.getRowCount()>0&&kdRoleLama.equals("")){
            tbRole.setRowSelectionInterval(0,0);
            pilihRole();
        }
    }//GEN-LAST:event_formWindowOpened

    private void BtnKeluarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnKeluarActionPerformed
        dispose();
    }//GEN-LAST:event_BtnKeluarActionPerformed

    private void BtnKeluarKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnKeluarKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            dispose();
        }
    }//GEN-LAST:event_BtnKeluarKeyPressed

    private void TabRawatMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_TabRawatMouseClicked
        if(TabRawat.getSelectedIndex()==1){
            tampilPilihRole();
        }
    }//GEN-LAST:event_TabRawatMouseClicked

    private void tbRoleMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbRoleMouseClicked
        pilihRole();
    }//GEN-LAST:event_tbRoleMouseClicked

    private void tbRoleKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbRoleKeyReleased
        if(evt.getKeyCode()==KeyEvent.VK_UP||evt.getKeyCode()==KeyEvent.VK_DOWN){
            pilihRole();
        }
    }//GEN-LAST:event_tbRoleKeyReleased

    private void BtnRoleBaruActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnRoleBaruActionPerformed
        kdRoleLama="";
        KdRole.setText("");
        NmRole.setText("");
        KetRole.setText("");
        terpilih.clear();
        tbRole.clearSelection();
        tampilAkses();
        KdRole.requestFocus();
    }//GEN-LAST:event_BtnRoleBaruActionPerformed

    private void BtnRoleGantiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnRoleGantiActionPerformed
        gantiRole();
    }//GEN-LAST:event_BtnRoleGantiActionPerformed

    private void BtnRoleHapusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnRoleHapusActionPerformed
        if(kdRoleLama.equals("")){
            JOptionPane.showMessageDialog(null,"Silahkan pilih role yang akan dihapus..!!");
            return;
        }
        if(JOptionPane.showConfirmDialog(this,"Hapus role "+kdRoleLama+"?\nHak akses user yang sudah terpasang tidak dicabut, hanya data role-nya yang dihapus.",
                "Konfirmasi",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION){
            try (PreparedStatement ps=koneksi.prepareStatement("delete from role_user where kd_role=?")){
                ps.setString(1,kdRoleLama);
                ps.executeUpdate();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,"Gagal menghapus role : "+e.getMessage());
                return;
            }
            BtnRoleBaruActionPerformed(null);
            tampilRole();
            tampilUser();
        }
    }//GEN-LAST:event_BtnRoleHapusActionPerformed

    private void CmbKategoriItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_CmbKategoriItemStateChanged
        if(evt.getStateChange()==java.awt.event.ItemEvent.SELECTED){
            tampilAkses();
        }
    }//GEN-LAST:event_CmbKategoriItemStateChanged

    private void CmbTampilItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_CmbTampilItemStateChanged
        if(evt.getStateChange()==java.awt.event.ItemEvent.SELECTED){
            tampilAkses();
        }
    }//GEN-LAST:event_CmbTampilItemStateChanged

    private void TCariAksesKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TCariAksesKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            tampilAkses();
        }
    }//GEN-LAST:event_TCariAksesKeyPressed

    private void BtnCariAksesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCariAksesActionPerformed
        tampilAkses();
    }//GEN-LAST:event_BtnCariAksesActionPerformed

    private void BtnSalinUserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSalinUserActionPerformed
        if(CmbSalinUser.getSelectedItem()==null){
            return;
        }
        String id=CmbSalinUser.getSelectedItem().toString().split(" \\| ")[0];
        if(!terpilih.isEmpty()&&JOptionPane.showConfirmDialog(this,"Ganti centang role ini dengan hak akses user "+id+"?","Konfirmasi",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION){
            return;
        }
        Set<String> hak=hakAksesUser(id);
        terpilih.clear();
        terpilih.addAll(hak);
        tampilAkses();
        JOptionPane.showMessageDialog(null,hak.size()+" hak akses user "+id+" disalin ke role. Tekan Simpan Role untuk menyimpan.");
    }//GEN-LAST:event_BtnSalinUserActionPerformed

    private void BtnSimpanRoleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSimpanRoleActionPerformed
        simpanRole();
    }//GEN-LAST:event_BtnSimpanRoleActionPerformed

    private void BtnCentangActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCentangActionPerformed
        setCentangTampil(true);
    }//GEN-LAST:event_BtnCentangActionPerformed

    private void BtnKosongkanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnKosongkanActionPerformed
        setCentangTampil(false);
    }//GEN-LAST:event_BtnKosongkanActionPerformed

    private void BtnTerapkanUlangActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnTerapkanUlangActionPerformed
        terapkanUlang();
    }//GEN-LAST:event_BtnTerapkanUlangActionPerformed

    private void TCariUserKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TCariUserKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            tampilUser();
        }
    }//GEN-LAST:event_TCariUserKeyPressed

    private void BtnCariUserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCariUserActionPerformed
        tampilUser();
    }//GEN-LAST:event_BtnCariUserActionPerformed

    private void BtnAllUserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnAllUserActionPerformed
        TCariUser.setText("");
        tampilUser();
    }//GEN-LAST:event_BtnAllUserActionPerformed

    private void tbUserMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbUserMouseClicked
        pilihUser();
    }//GEN-LAST:event_tbUserMouseClicked

    private void tbUserKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbUserKeyReleased
        if(evt.getKeyCode()==KeyEvent.VK_UP||evt.getKeyCode()==KeyEvent.VK_DOWN){
            pilihUser();
        }
    }//GEN-LAST:event_tbUserKeyReleased

    private void BtnPasangActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnPasangActionPerformed
        pasangRole();
    }//GEN-LAST:event_BtnPasangActionPerformed

    private void BtnCentangUserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCentangUserActionPerformed
        setCentangUser(true);
    }//GEN-LAST:event_BtnCentangUserActionPerformed

    private void BtnBatalCentangUserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnBatalCentangUserActionPerformed
        userDicentang.clear();
        setCentangUser(false);
    }//GEN-LAST:event_BtnBatalCentangUserActionPerformed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            DlgRoleUser dialog=new DlgRoleUser(new javax.swing.JFrame(),true);
            dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    System.exit(0);
                }
            });
            dialog.setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private widget.Button BtnAllUser;
    private widget.Button BtnBatalCentangUser;
    private widget.Button BtnCariAkses;
    private widget.Button BtnCariUser;
    private widget.Button BtnCentang;
    private widget.Button BtnCentangUser;
    private widget.Button BtnKeluar;
    private widget.Button BtnKosongkan;
    private widget.Button BtnPasang;
    private widget.Button BtnRoleBaru;
    private widget.Button BtnRoleGanti;
    private widget.Button BtnRoleHapus;
    private widget.Button BtnSalinUser;
    private widget.Button BtnSimpanRole;
    private widget.Button BtnTerapkanUlang;
    private widget.ComboBox CmbKategori;
    private widget.ComboBox CmbMode;
    private widget.ComboBox CmbSalinUser;
    private widget.ComboBox CmbTampil;
    private widget.PanelBiasa FormRole;
    private widget.PanelBiasa FormUser;
    private widget.TextBox KdRole;
    private widget.TextBox KetRole;
    private widget.Label LInfo;
    private widget.Label LJumlahAkses;
    private widget.Label LJumlahUser;
    private widget.TextBox NmRole;
    private widget.PanelBiasa PanelAkses;
    private widget.PanelBiasa PanelPasang;
    private widget.PanelBiasa PanelRole;
    private widget.PanelBiasa PanelUser;
    private widget.ScrollPane ScrollAkses;
    private widget.ScrollPane ScrollPilih;
    private widget.ScrollPane ScrollRole;
    private widget.ScrollPane ScrollUser;
    private widget.TextBox TCariAkses;
    private widget.TextBox TCariUser;
    private widget.TextBox TNamaUser;
    private widget.TextBox TUser;
    private widget.TabPane TabRawat;
    private widget.InternalFrame internalFrame1;
    private widget.InternalFrame internalFrame2;
    private widget.InternalFrame internalFrame3;
    private widget.Label jLabel1;
    private widget.Label jLabel10;
    private widget.Label jLabel2;
    private widget.Label jLabel3;
    private widget.Label jLabel4;
    private widget.Label jLabel5;
    private widget.Label jLabel6;
    private widget.Label jLabel7;
    private widget.Label jLabel8;
    private widget.Label jLabel9;
    private widget.panelisi panelAksesTombol;
    private widget.panelisi panelCariUser;
    private widget.panelisi panelGlass8;
    private widget.panelisi panelPasangTombol;
    private widget.panelisi panelRoleTombol;
    private widget.Table tbAkses;
    private widget.Table tbRole;
    private widget.Table tbRolePilih;
    private widget.Table tbUser;
    // End of variables declaration//GEN-END:variables

    // ======================= Tabel & data =======================
    private void buatTabel(){
        // tabel tambahan RS (lihat update_role_user.sql), dibuat otomatis bila belum ada
        try (Statement st=koneksi.createStatement()){
            st.executeUpdate("CREATE TABLE IF NOT EXISTS `role_user` ("+
                    "`kd_role` varchar(20) NOT NULL,"+
                    "`nm_role` varchar(100) NOT NULL,"+
                    "`keterangan` varchar(200) DEFAULT NULL,"+
                    "PRIMARY KEY (`kd_role`)"+
                    ") ENGINE=InnoDB DEFAULT CHARSET=latin1");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS `role_user_akses` ("+
                    "`kd_role` varchar(20) NOT NULL,"+
                    "`kolom` varchar(100) NOT NULL,"+
                    "PRIMARY KEY (`kd_role`,`kolom`),"+
                    "CONSTRAINT `role_user_akses_ibfk_1` FOREIGN KEY (`kd_role`) REFERENCES `role_user` (`kd_role`) ON DELETE CASCADE ON UPDATE CASCADE"+
                    ") ENGINE=InnoDB DEFAULT CHARSET=latin1");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS `user_role` ("+
                    "`id_user` varchar(100) NOT NULL,"+
                    "`kd_role` varchar(20) NOT NULL,"+
                    "PRIMARY KEY (`id_user`,`kd_role`),"+
                    "KEY `kd_role` (`kd_role`),"+
                    "CONSTRAINT `user_role_ibfk_1` FOREIGN KEY (`kd_role`) REFERENCES `role_user` (`kd_role`) ON DELETE CASCADE ON UPDATE CASCADE"+
                    ") ENGINE=InnoDB DEFAULT CHARSET=latin1");
        } catch (Exception e) {
            System.out.println("Notifikasi buat tabel role user : "+e);
        }
        try (PreparedStatement ps=koneksi.prepareStatement("insert ignore into role_user(kd_role,nm_role,keterangan) values(?,?,'')")){
            for(String[] r:ROLE_BAWAAN){
                ps.setString(1,r[0]);
                ps.setString(2,r[1]);
                ps.executeUpdate();
            }
        } catch (Exception e) {
            System.out.println("Notifikasi role bawaan : "+e);
        }
    }

    private void muatKolomAkses(){
        kolomAkses.clear();
        try (Statement st=koneksi.createStatement();
             ResultSet rs=st.executeQuery("select column_name from information_schema.columns where table_schema=database() and table_name='user' "+
                     "and column_type='enum(''true'',''false'')' order by ordinal_position")){
            while(rs.next()){
                kolomAkses.add(rs.getString(1));
            }
        } catch (Exception e) {
            System.out.println("Notifikasi kolom hak akses : "+e);
        }
        // urut per kategori lalu nama hak akses (urutan kolom di database bercampur antar kategori)
        kolomAkses.sort((x,y)->DaftarHakAkses.label(x).compareToIgnoreCase(DaftarHakAkses.label(y)));
    }

    private void tampilRole(){
        String pilih=kdRoleLama;
        Valid.tabelKosong(modeRole);
        try (Statement st=koneksi.createStatement();
             ResultSet rs=st.executeQuery("select role_user.kd_role,role_user.nm_role,role_user.keterangan,"+
                     "(select count(*) from role_user_akses where role_user_akses.kd_role=role_user.kd_role),"+
                     "(select count(*) from user_role where user_role.kd_role=role_user.kd_role) from role_user order by role_user.nm_role")){
            while(rs.next()){
                modeRole.addRow(new Object[]{rs.getString(1),rs.getString(2),rs.getString(3),rs.getInt(4),rs.getInt(5)});
            }
        } catch (Exception e) {
            System.out.println("Notifikasi : "+e);
        }
        for(int i=0;i<modeRole.getRowCount();i++){
            if(modeRole.getValueAt(i,0).toString().equals(pilih)){
                tbRole.setRowSelectionInterval(i,i);
            }
        }
        tampilPilihRole();
    }

    private void pilihRole(){
        int r=tbRole.getSelectedRow();
        if(r<0){
            return;
        }
        kdRoleLama=modeRole.getValueAt(r,0).toString();
        KdRole.setText(kdRoleLama);
        NmRole.setText(modeRole.getValueAt(r,1).toString());
        KetRole.setText(modeRole.getValueAt(r,2)==null?"":modeRole.getValueAt(r,2).toString());
        terpilih.clear();
        try (PreparedStatement ps=koneksi.prepareStatement("select kolom from role_user_akses where kd_role=?")){
            ps.setString(1,kdRoleLama);
            try (ResultSet rs=ps.executeQuery()){
                while(rs.next()){
                    terpilih.add(rs.getString(1));
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi : "+e);
        }
        tampilAkses();
    }

    /** Isi tabel hak akses sesuai kategori, kata cari dan pilihan tampil. */
    private void tampilAkses(){
        memuat=true;
        try {
            modeAkses.setRowCount(0);
            String awalan=CmbKategori.getSelectedIndex()>0?CmbKategori.getSelectedItem().toString().substring(0,3):"";
            String cari=TCariAkses.getText().trim().toLowerCase();
            int tampil=CmbTampil.getSelectedIndex();
            for(String kolom:kolomAkses){
                String label=DaftarHakAkses.label(kolom);
                boolean dipilih=terpilih.contains(kolom);
                if(!awalan.equals("")&&!label.startsWith(awalan)){
                    continue;
                }
                if(!cari.equals("")&&!label.toLowerCase().contains(cari)&&!kolom.toLowerCase().contains(cari)){
                    continue;
                }
                if((tampil==1&&!dipilih)||(tampil==2&&dipilih)){
                    continue;
                }
                modeAkses.addRow(new Object[]{dipilih,label,kolom});
            }
        } finally {
            memuat=false;
        }
        hitungTerpilih();
    }

    private void hitungTerpilih(){
        LJumlahAkses.setText(terpilih.size()+" dicentang / "+modeAkses.getRowCount()+" tampil");
    }

    private void setCentangTampil(boolean nilai){
        memuat=true;
        try {
            for(int i=0;i<modeAkses.getRowCount();i++){
                modeAkses.setValueAt(nilai,i,0);
                if(nilai){
                    terpilih.add(modeAkses.getValueAt(i,2).toString());
                }else{
                    terpilih.remove(modeAkses.getValueAt(i,2).toString());
                }
            }
        } finally {
            memuat=false;
        }
        hitungTerpilih();
    }

    /** Kolom hak akses yang bernilai true pada user. */
    private Set<String> hakAksesUser(String idUser){
        Set<String> hasil=new LinkedHashSet<>();
        try (PreparedStatement ps=koneksi.prepareStatement("select * from user where id_user=AES_ENCRYPT(?,'nur')")){
            ps.setString(1,idUser);
            try (ResultSet rs=ps.executeQuery()){
                if(rs.next()){
                    for(String kolom:kolomAkses){
                        if("true".equals(rs.getString(kolom))){
                            hasil.add(kolom);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi : "+e);
        }
        return hasil;
    }

    private void simpanRole(){
        String kd=KdRole.getText().trim().toUpperCase().replace(' ','_');
        String nm=NmRole.getText().trim();
        if(kd.equals("")){
            Valid.textKosong(KdRole,"Kode Role");
            return;
        }
        if(nm.equals("")){
            Valid.textKosong(NmRole,"Nama Role");
            return;
        }
        boolean sukses=false;
        try {
            koneksi.setAutoCommit(false);
            if(!kdRoleLama.equals("")&&!kdRoleLama.equals(kd)){
                try (PreparedStatement ps=koneksi.prepareStatement("update role_user set kd_role=? where kd_role=?")){
                    ps.setString(1,kd);
                    ps.setString(2,kdRoleLama);
                    ps.executeUpdate();
                }
            }
            try (PreparedStatement ps=koneksi.prepareStatement("insert into role_user(kd_role,nm_role,keterangan) values(?,?,?) on duplicate key update nm_role=values(nm_role),keterangan=values(keterangan)")){
                ps.setString(1,kd);
                ps.setString(2,nm);
                ps.setString(3,KetRole.getText().trim());
                ps.executeUpdate();
            }
            try (PreparedStatement ps=koneksi.prepareStatement("delete from role_user_akses where kd_role=?")){
                ps.setString(1,kd);
                ps.executeUpdate();
            }
            try (PreparedStatement ps=koneksi.prepareStatement("insert into role_user_akses(kd_role,kolom) values(?,?)")){
                for(String kolom:terpilih){
                    ps.setString(1,kd);
                    ps.setString(2,kolom);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            koneksi.commit();
            sukses=true;
        } catch (Exception e) {
            try { koneksi.rollback(); } catch (Exception ex) { }
            JOptionPane.showMessageDialog(null,"Gagal menyimpan role : "+e.getMessage());
        } finally {
            try { koneksi.setAutoCommit(true); } catch (Exception ex) { }
        }
        if(sukses){
            kdRoleLama=kd;
            KdRole.setText(kd);
            tampilRole();
            tampilUser();
            JOptionPane.showMessageDialog(null,"Role "+kd+" tersimpan dengan "+terpilih.size()+" hak akses.\n"+
                    "Untuk user yang sudah memegang role ini, gunakan tombol Terapkan Ulang.");
        }
    }

    /** Ganti kode / nama / keterangan role terpilih saja (centang hak akses tidak diubah). */
    private void gantiRole(){
        if(kdRoleLama.equals("")){
            JOptionPane.showMessageDialog(null,"Silahkan pilih role yang akan diganti di tabel sebelah kiri..!!");
            return;
        }
        String kd=KdRole.getText().trim().toUpperCase().replace(' ','_');
        String nm=NmRole.getText().trim();
        if(kd.equals("")){
            Valid.textKosong(KdRole,"Kode Role");
            return;
        }
        if(nm.equals("")){
            Valid.textKosong(NmRole,"Nama Role");
            return;
        }
        if(!kd.equals(kdRoleLama)){
            for(int i=0;i<modeRole.getRowCount();i++){
                if(modeRole.getValueAt(i,0).toString().equals(kd)){
                    JOptionPane.showMessageDialog(null,"Kode role "+kd+" sudah dipakai role lain..!!");
                    KdRole.requestFocus();
                    return;
                }
            }
        }
        if(JOptionPane.showConfirmDialog(this,"Ganti role "+kdRoleLama+" menjadi :\nKode : "+kd+"\nNama : "+nm+"\nKeterangan : "+KetRole.getText().trim()+
                (kd.equals(kdRoleLama)?"":"\n\nHak akses role dan daftar user pemegangnya ikut pindah ke kode baru."),
                "Konfirmasi",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION){
            return;
        }
        try (PreparedStatement ps=koneksi.prepareStatement("update role_user set kd_role=?,nm_role=?,keterangan=? where kd_role=?")){
            ps.setString(1,kd);
            ps.setString(2,nm);
            ps.setString(3,KetRole.getText().trim());
            ps.setString(4,kdRoleLama);
            ps.executeUpdate();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,"Gagal mengganti role : "+e.getMessage());
            return;
        }
        kdRoleLama=kd;
        KdRole.setText(kd);
        tampilRole();
        tampilUser();
        JOptionPane.showMessageDialog(null,"Role berhasil diganti.");
    }

    private List<String> roleUser(String idUser){
        List<String> hasil=new ArrayList<>();
        try (PreparedStatement ps=koneksi.prepareStatement("select kd_role from user_role where id_user=? order by kd_role")){
            ps.setString(1,idUser);
            try (ResultSet rs=ps.executeQuery()){
                while(rs.next()){
                    hasil.add(rs.getString(1));
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi : "+e);
        }
        return hasil;
    }

    /**
     * Tulis hak akses gabungan role ke user. ganti=true: hak akses user disamakan persis dengan gabungan role,
     * ganti=false: hanya menambah. catatRole: simpan daftar role user (ganti = role user diganti, tambah = ditambah).
     */
    private boolean terapkan(String idUser,List<String> roles,boolean ganti,boolean catatRole){
        Set<String> hak=new LinkedHashSet<>();
        if(!roles.isEmpty()){
            StringBuilder in=new StringBuilder();
            for(int i=0;i<roles.size();i++){
                in.append(i==0?"?":",?");
            }
            try (PreparedStatement ps=koneksi.prepareStatement("select distinct kolom from role_user_akses where kd_role in ("+in+")")){
                for(int i=0;i<roles.size();i++){
                    ps.setString(i+1,roles.get(i));
                }
                try (ResultSet rs=ps.executeQuery()){
                    while(rs.next()){
                        hak.add(rs.getString(1));
                    }
                }
            } catch (Exception e) {
                System.out.println("Notifikasi : "+e);
                return false;
            }
        }
        hak.retainAll(kolomAkses);
        StringBuilder set=new StringBuilder();
        for(String kolom:kolomAkses){
            boolean ada=hak.contains(kolom);
            if(ganti||ada){
                if(set.length()>0){
                    set.append(",");
                }
                set.append("`").append(kolom).append("`='").append(ada?"true":"false").append("'");
            }
        }
        try {
            koneksi.setAutoCommit(false);
            if(set.length()>0){
                try (PreparedStatement ps=koneksi.prepareStatement("update user set "+set+" where id_user=AES_ENCRYPT(?,'nur')")){
                    ps.setString(1,idUser);
                    ps.executeUpdate();
                }
            }
            if(catatRole){
                if(ganti){
                    try (PreparedStatement ps=koneksi.prepareStatement("delete from user_role where id_user=?")){
                        ps.setString(1,idUser);
                        ps.executeUpdate();
                    }
                }
                try (PreparedStatement ps=koneksi.prepareStatement("insert ignore into user_role(id_user,kd_role) values(?,?)")){
                    for(String r:roles){
                        ps.setString(1,idUser);
                        ps.setString(2,r);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
            }
            koneksi.commit();
            return true;
        } catch (Exception e) {
            try { koneksi.rollback(); } catch (Exception ex) { }
            System.out.println("Notifikasi terapkan role : "+e);
            return false;
        } finally {
            try { koneksi.setAutoCommit(true); } catch (Exception ex) { }
        }
    }

    /** Pilihan cara: 0 = Tambahkan, 1 = Ganti, -1 = batal. */
    private int tanyaCara(String pesan){
        Object[] pilihan={"Tambahkan","Ganti","Batal"};
        int p=JOptionPane.showOptionDialog(this,pesan+"\n\nTambahkan = hak akses lama user tetap, ditambah hak akses role.\n"+
                "Ganti = hak akses user disamakan persis dengan gabungan role yang dipegang.",
                "Cara Menerapkan Role",JOptionPane.DEFAULT_OPTION,JOptionPane.QUESTION_MESSAGE,null,pilihan,pilihan[0]);
        return (p==0||p==1)?p:-1;
    }

    private void terapkanUlang(){
        if(kdRoleLama.equals("")){
            JOptionPane.showMessageDialog(null,"Silahkan pilih / simpan role terlebih dahulu..!!");
            return;
        }
        List<String> users=new ArrayList<>();
        try (PreparedStatement ps=koneksi.prepareStatement("select id_user from user_role where kd_role=?")){
            ps.setString(1,kdRoleLama);
            try (ResultSet rs=ps.executeQuery()){
                while(rs.next()){
                    users.add(rs.getString(1));
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi : "+e);
        }
        if(users.isEmpty()){
            JOptionPane.showMessageDialog(null,"Belum ada user yang memegang role "+kdRoleLama+".");
            return;
        }
        int cara=tanyaCara("Terapkan ulang hak akses ke "+users.size()+" user pemegang role "+kdRoleLama+"?\n"+
                "(Setiap user diterapkan gabungan semua role yang dipegangnya.)");
        if(cara<0){
            return;
        }
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        int berhasil=0;
        for(String u:users){
            if(terapkan(u,roleUser(u),cara==1,false)){
                berhasil++;
            }
        }
        setCursor(Cursor.getDefaultCursor());
        JOptionPane.showMessageDialog(null,"Hak akses diterapkan ke "+berhasil+" dari "+users.size()+" user.\nPerubahan berlaku saat user login ulang.");
    }

    // ======================= Role per user =======================
    private void tampilUser(){
        String pilih=TUser.getText();
        Valid.tabelKosong(modeUser);
        java.util.List<String> salin=new ArrayList<>();
        String sql="select u.id,ifnull((select pegawai.nama from pegawai where pegawai.nik=u.id),ifnull((select dokter.nm_dokter from dokter where dokter.kd_dokter=u.id),"+
                "ifnull((select petugas.nama from petugas where petugas.nip=u.id),''))) as nama,"+
                "ifnull((select group_concat(user_role.kd_role order by user_role.kd_role separator ', ') from user_role where user_role.id_user=u.id),'') as role "+
                "from (select convert(AES_DECRYPT(user.id_user,'nur') using latin1) as id from user) u order by u.id";
        String cari=TCariUser.getText().trim().toLowerCase();
        try (Statement st=koneksi.createStatement();ResultSet rs=st.executeQuery(sql)){
            memuat=true;
            while(rs.next()){
                String id=rs.getString(1)==null?"":rs.getString(1),nama=rs.getString(2),role=rs.getString(3);
                salin.add(id+" | "+nama);
                if(cari.equals("")||id.toLowerCase().contains(cari)||nama.toLowerCase().contains(cari)||role.toLowerCase().contains(cari)){
                    modeUser.addRow(new Object[]{userDicentang.contains(id),id,nama,role});
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi : "+e);
        } finally {
            memuat=false;
        }
        hitungUser();
        Object lama=CmbSalinUser.getSelectedItem();
        CmbSalinUser.setModel(new javax.swing.DefaultComboBoxModel<>(salin.toArray(new String[0])));
        if(lama!=null){
            CmbSalinUser.setSelectedItem(lama);
        }
        for(int i=0;i<modeUser.getRowCount();i++){
            if(modeUser.getValueAt(i,1).toString().equals(pilih)){
                tbUser.setRowSelectionInterval(i,i);
            }
        }
    }

    private void hitungUser(){
        LJumlahUser.setText(userDicentang.size()+" user dicentang");
    }

    private void setCentangUser(boolean nilai){
        memuat=true;
        try {
            for(int i=0;i<modeUser.getRowCount();i++){
                modeUser.setValueAt(nilai,i,0);
                if(nilai){
                    userDicentang.add(modeUser.getValueAt(i,1).toString());
                }
            }
        } finally {
            memuat=false;
        }
        hitungUser();
    }

    private void pilihUser(){
        int r=tbUser.getSelectedRow();
        if(r<0){
            return;
        }
        TUser.setText(modeUser.getValueAt(r,1).toString());
        TNamaUser.setText(modeUser.getValueAt(r,2).toString());
        tampilPilihRole();
    }

    /** Daftar role untuk dipasang, dicentang sesuai role user terpilih. */
    private void tampilPilihRole(){
        List<String> punya=TUser.getText().equals("")?new ArrayList<String>():roleUser(TUser.getText());
        modePilih.setRowCount(0);
        for(int i=0;i<modeRole.getRowCount();i++){
            String kd=modeRole.getValueAt(i,0).toString();
            modePilih.addRow(new Object[]{punya.contains(kd),kd,modeRole.getValueAt(i,1),modeRole.getValueAt(i,3)});
        }
    }

    private void pasangRole(){
        // sasaran: semua user yang dicentang; bila tidak ada yang dicentang, user yang sedang diklik
        List<String> sasaran=new ArrayList<>(userDicentang);
        if(sasaran.isEmpty()&&!TUser.getText().equals("")){
            sasaran.add(TUser.getText());
        }
        if(sasaran.isEmpty()){
            JOptionPane.showMessageDialog(null,"Silahkan centang user (boleh lebih dari 1) atau klik satu user terlebih dahulu..!!");
            return;
        }
        List<String> roles=new ArrayList<>();
        int jmlHak=0;
        for(int i=0;i<modePilih.getRowCount();i++){
            if(Boolean.TRUE.equals(modePilih.getValueAt(i,0))){
                roles.add(modePilih.getValueAt(i,1).toString());
                jmlHak+=Integer.parseInt(modePilih.getValueAt(i,3).toString());
            }
        }
        if(roles.isEmpty()){
            JOptionPane.showMessageDialog(null,"Centang minimal satu role..!!");
            return;
        }
        boolean ganti=CmbMode.getSelectedIndex()==1;
        if(jmlHak==0&&ganti){
            JOptionPane.showMessageDialog(null,"Role yang dipilih belum berisi hak akses. Cara Ganti akan mengosongkan semua hak akses user, dibatalkan.");
            return;
        }
        String daftar=sasaran.size()<=10?String.join(", ",sasaran):String.join(", ",sasaran.subList(0,10))+", ... ("+sasaran.size()+" user)";
        if(JOptionPane.showConfirmDialog(this,"Pasang role "+String.join(", ",roles)+" ke "+sasaran.size()+" user?\n"+daftar+"\nCara : "+
                CmbMode.getSelectedItem(),"Konfirmasi",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION){
            return;
        }
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        List<String> gagal=new ArrayList<>();
        for(String u:sasaran){
            if(!terapkan(u,roles,ganti,true)){
                gagal.add(u);
            }
        }
        setCursor(Cursor.getDefaultCursor());
        tampilUser();
        tampilRole();
        tampilPilihRole();
        if(gagal.isEmpty()){
            JOptionPane.showMessageDialog(null,"Role terpasang ke "+sasaran.size()+" user.\nPerubahan berlaku saat user login ulang.");
        }else{
            JOptionPane.showMessageDialog(null,"Role terpasang ke "+(sasaran.size()-gagal.size())+" user, gagal untuk : "+String.join(", ",gagal));
        }
    }

    public void isCek(){
        boolean admin="Admin Utama".equals(akses.getkode());
        BtnRoleBaru.setEnabled(admin);
        BtnRoleHapus.setEnabled(admin);
        BtnRoleGanti.setEnabled(admin);
        BtnSimpanRole.setEnabled(admin);
        BtnSalinUser.setEnabled(admin);
        BtnCentang.setEnabled(admin);
        BtnKosongkan.setEnabled(admin);
        BtnTerapkanUlang.setEnabled(admin);
        BtnPasang.setEnabled(admin);
    }
}
