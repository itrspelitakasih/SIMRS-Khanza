package rekammedis;

import fungsi.WarnaTable;
import fungsi.akses;
import fungsi.batasInput;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import kepegawaian.DlgCariDokter;
import widget.PanelOdontogram;

/**
 * Odontogram standar PDGI (gigi tetap + gigi susu). Satu pemeriksaan per no_rawat; kondisi gigi pemeriksaan
 * terakhir pasien otomatis dibawa ke kunjungan berikutnya saat belum ada data di kunjungan tersebut.
 * Tabel: odontogram (header) dan odontogram_detail (gigi, permukaan, kode), lihat update_odontogram.sql.
 * Hak akses: user.odontogram (akses.getodontogram()).
 */
public final class RMOdontogram extends javax.swing.JDialog {
    private final DefaultTableModel tabMode;
    private final Connection koneksi=koneksiDB.condb();
    private final sekuel Sequel=new sekuel();
    private final validasi Valid=new validasi();
    private DlgCariDokter dokter;
    private final ExecutorService executor=Executors.newSingleThreadExecutor();
    private volatile boolean ceksukses=false;
    private String TANGGALMUNDUR="yes";
    private boolean memuat=false;

    private static final String[] KOLOM={
        "No.Rawat","No.RM","Nama Pasien","Tgl.Lahir","J.K.","Kode Dokter","Nama Dokter","Tanggal",
        "Occlusi","Torus Palatinus","Torus Mandibularis","Palatum","Diastema","Gigi Anomali","Lain-lain",
        "D","M","F","Foto","Rontgen"
    };
    private static final int[] LEBAR_KOLOM={105,70,150,65,35,90,150,115,80,95,110,60,150,150,180,30,30,30,120,120};

    /** Creates new form RMOdontogram
     * @param parent
     * @param modal */
    public RMOdontogram(java.awt.Frame parent,boolean modal) {
        super(parent,modal);
        initComponents();
        this.setLocation(8,1);
        setSize(1000,700);

        tabMode=new DefaultTableModel(null,KOLOM){
            @Override public boolean isCellEditable(int rowIndex,int colIndex){return false;}
        };
        tbObat.setModel(tabMode);
        tbObat.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbObat.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for(int i=0;i<KOLOM.length;i++){
            tbObat.getColumnModel().getColumn(i).setPreferredWidth(LEBAR_KOLOM[i]);
        }
        tbObat.setDefaultRenderer(Object.class,new WarnaTable());

        TNoRw.setDocument(new batasInput((byte)17).getKata(TNoRw));
        KdDokter.setDocument(new batasInput((byte)20).getKata(KdDokter));
        KetDiastema.setDocument(new batasInput((int)100).getKata(KetDiastema));
        KetAnomali.setDocument(new batasInput((int)100).getKata(KetAnomali));
        LainLain.setDocument(new batasInput((int)200).getKata(LainLain));
        Foto.setDocument(new batasInput((int)50).getKata(Foto));
        Rontgen.setDocument(new batasInput((int)50).getKata(Rontgen));
        D.setDocument(new batasInput((byte)2).getOnlyAngka(D));
        M.setDocument(new batasInput((byte)2).getOnlyAngka(M));
        F.setDocument(new batasInput((byte)2).getOnlyAngka(F));
        TCari.setDocument(new batasInput((int)100).getKata(TCari));
        KetDiastema.setEditable(false);
        KetAnomali.setEditable(false);

        siapkanPalet();
        Gambar.addPropertyChangeListener("data",e->{
            if(!memuat){
                hitungDMF();
            }
        });

        try {
            TANGGALMUNDUR=koneksiDB.TANGGALMUNDUR();
        } catch (Exception e) {
            TANGGALMUNDUR="yes";
        }
        buatTabel();
    }

    /** Palet kondisi PDGI: widget.Table 1 kolom berisi ikon simbol, kode, nama dan keterangan warna/simbol. */
    private void siapkanPalet(){
        DefaultTableModel modePalet=new DefaultTableModel(null,new Object[]{"Kondisi"}){
            @Override public boolean isCellEditable(int rowIndex,int colIndex){return false;}
        };
        for(String kode:PanelOdontogram.daftarKode()){
            modePalet.addRow(new Object[]{kode});
        }
        Palet.setModel(modePalet);
        Palet.setTableHeader(null);
        scrollPalet.setColumnHeaderView(null);
        Palet.setRowHeight(38);
        Palet.setShowGrid(false);
        Palet.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        Palet.setDefaultRenderer(Object.class,new DefaultTableCellRenderer(){
            private static final long serialVersionUID = 1L;
            @Override
            public Component getTableCellRendererComponent(JTable table,Object value,boolean isSelected,boolean hasFocus,int row,int column) {
                super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);
                String kode=String.valueOf(value);
                setFont(new Font("Tahoma",0,11));
                setText("<html><b>"+kode+"</b> "+PanelOdontogram.namaKondisi(kode)+
                        "<br><font color='#777777'>"+PanelOdontogram.keteranganSimbol(kode)+"</font></html>");
                setToolTipText(PanelOdontogram.isKondisiPermukaan(kode)?"Kondisi permukaan: klik permukaan gigi (M/D/B/L/O)":"Kondisi gigi: klik di mana saja pada gigi");
                setIcon(PanelOdontogram.ikon(kode,28));
                setIconTextGap(6);
                setBackground(isSelected?new Color(255,225,150):Color.WHITE);
                setForeground(new Color(50,50,50));
                return this;
            }
        });
        Palet.getSelectionModel().addListSelectionListener(e->{
            if(Palet.getSelectedRow()>-1){
                Gambar.setKodeAktif(Palet.getValueAt(Palet.getSelectedRow(),0).toString());
            }
        });
        Palet.setRowSelectionInterval(0,0);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        TanggalRegistrasi = new widget.TextBox();
        internalFrame1 = new widget.InternalFrame();
        panelGlass8 = new widget.panelisi();
        BtnSimpan = new widget.Button();
        BtnBatal = new widget.Button();
        BtnHapus = new widget.Button();
        BtnEdit = new widget.Button();
        BtnPrint = new widget.Button();
        jLabel7 = new widget.Label();
        LCount = new widget.Label();
        BtnKeluar = new widget.Button();
        TabRawat = new widget.TabPane();
        internalFrame2 = new widget.InternalFrame();
        scrollInput = new widget.ScrollPane();
        FormInput = new widget.PanelBiasa();
        jLabel4 = new widget.Label();
        TNoRw = new widget.TextBox();
        TNoRM = new widget.TextBox();
        TPasien = new widget.TextBox();
        jLabel8 = new widget.Label();
        TglLahir = new widget.TextBox();
        Jk = new widget.TextBox();
        jLabel16 = new widget.Label();
        KdDokter = new widget.TextBox();
        NmDokter = new widget.TextBox();
        BtnDokter = new widget.Button();
        jLabel18 = new widget.Label();
        Tanggal = new widget.Tanggal();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel9 = new widget.Label();
        scrollPalet = new widget.ScrollPane();
        Palet = new widget.Table();
        BtnAmbilTerakhir = new widget.Button();
        BtnHapusGambar = new widget.Button();
        Gambar = new widget.PanelOdontogram();
        LInfo = new widget.Label();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel10 = new widget.Label();
        Occlusi = new widget.ComboBox();
        jLabel11 = new widget.Label();
        TorusPalatinus = new widget.ComboBox();
        jLabel12 = new widget.Label();
        TorusMandibularis = new widget.ComboBox();
        jLabel13 = new widget.Label();
        Palatum = new widget.ComboBox();
        jLabel14 = new widget.Label();
        Diastema = new widget.ComboBox();
        KetDiastema = new widget.TextBox();
        jLabel15 = new widget.Label();
        Anomali = new widget.ComboBox();
        KetAnomali = new widget.TextBox();
        jLabel17 = new widget.Label();
        LainLain = new widget.TextBox();
        jLabel20 = new widget.Label();
        D = new widget.TextBox();
        jLabel22 = new widget.Label();
        M = new widget.TextBox();
        jLabel23 = new widget.Label();
        F = new widget.TextBox();
        jLabel24 = new widget.Label();
        Foto = new widget.TextBox();
        jLabel25 = new widget.Label();
        Rontgen = new widget.TextBox();
        internalFrame3 = new widget.InternalFrame();
        Scroll = new widget.ScrollPane();
        tbObat = new widget.Table();
        panelGlass9 = new widget.panelisi();
        jLabel19 = new widget.Label();
        DTPCari1 = new widget.Tanggal();
        jLabel21 = new widget.Label();
        DTPCari2 = new widget.Tanggal();
        jLabel6 = new widget.Label();
        TCari = new widget.TextBox();
        BtnCari = new widget.Button();
        BtnAll = new widget.Button();

        TanggalRegistrasi.setHighlighter(null);
        TanggalRegistrasi.setName("TanggalRegistrasi"); // NOI18N

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Odontogram ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 12), new java.awt.Color(50, 50, 50))); // NOI18N
        internalFrame1.setFont(new java.awt.Font("Tahoma", 2, 12)); // NOI18N
        internalFrame1.setName("internalFrame1"); // NOI18N
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

        panelGlass8.setName("panelGlass8"); // NOI18N
        panelGlass8.setPreferredSize(new java.awt.Dimension(44, 54));
        panelGlass8.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        BtnSimpan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/save-16x16.png"))); // NOI18N
        BtnSimpan.setMnemonic(83);
        BtnSimpan.setText("Simpan");
        BtnSimpan.setToolTipText("Alt+S");
        BtnSimpan.setName("BtnSimpan"); // NOI18N
        BtnSimpan.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnSimpan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnSimpanActionPerformed(evt);
            }
        });
        BtnSimpan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnSimpanKeyPressed(evt);
            }
        });
        panelGlass8.add(BtnSimpan);

        BtnBatal.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Cancel-2-16x16.png"))); // NOI18N
        BtnBatal.setMnemonic(66);
        BtnBatal.setText("Baru");
        BtnBatal.setToolTipText("Alt+B");
        BtnBatal.setName("BtnBatal"); // NOI18N
        BtnBatal.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnBatal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnBatalActionPerformed(evt);
            }
        });
        BtnBatal.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnBatalKeyPressed(evt);
            }
        });
        panelGlass8.add(BtnBatal);

        BtnHapus.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/stop_f2.png"))); // NOI18N
        BtnHapus.setMnemonic(72);
        BtnHapus.setText("Hapus");
        BtnHapus.setToolTipText("Alt+H");
        BtnHapus.setName("BtnHapus"); // NOI18N
        BtnHapus.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnHapus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnHapusActionPerformed(evt);
            }
        });
        BtnHapus.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnHapusKeyPressed(evt);
            }
        });
        panelGlass8.add(BtnHapus);

        BtnEdit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/inventaris.png"))); // NOI18N
        BtnEdit.setMnemonic(71);
        BtnEdit.setText("Ganti");
        BtnEdit.setToolTipText("Alt+G");
        BtnEdit.setName("BtnEdit"); // NOI18N
        BtnEdit.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnEditActionPerformed(evt);
            }
        });
        BtnEdit.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnEditKeyPressed(evt);
            }
        });
        panelGlass8.add(BtnEdit);

        BtnPrint.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/b_print.png"))); // NOI18N
        BtnPrint.setMnemonic(84);
        BtnPrint.setText("Cetak");
        BtnPrint.setToolTipText("Alt+T");
        BtnPrint.setName("BtnPrint"); // NOI18N
        BtnPrint.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnPrint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnPrintActionPerformed(evt);
            }
        });
        BtnPrint.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnPrintKeyPressed(evt);
            }
        });
        panelGlass8.add(BtnPrint);

        jLabel7.setText("Record :");
        jLabel7.setName("jLabel7"); // NOI18N
        jLabel7.setPreferredSize(new java.awt.Dimension(80, 23));
        panelGlass8.add(jLabel7);

        LCount.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LCount.setText("0");
        LCount.setName("LCount"); // NOI18N
        LCount.setPreferredSize(new java.awt.Dimension(70, 23));
        panelGlass8.add(LCount);

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

        scrollInput.setName("scrollInput"); // NOI18N

        FormInput.setBackground(new java.awt.Color(255, 255, 255));
        FormInput.setBorder(null);
        FormInput.setName("FormInput"); // NOI18N
        FormInput.setPreferredSize(new java.awt.Dimension(965, 662));
        FormInput.setLayout(null);

        jLabel4.setText("No.Rawat :");
        jLabel4.setName("jLabel4"); // NOI18N
        FormInput.add(jLabel4);
        jLabel4.setBounds(0, 10, 80, 23);

        TNoRw.setEditable(false);
        TNoRw.setHighlighter(null);
        TNoRw.setName("TNoRw"); // NOI18N
        FormInput.add(TNoRw);
        TNoRw.setBounds(84, 10, 136, 23);

        TNoRM.setEditable(false);
        TNoRM.setHighlighter(null);
        TNoRM.setName("TNoRM"); // NOI18N
        FormInput.add(TNoRM);
        TNoRM.setBounds(222, 10, 100, 23);

        TPasien.setEditable(false);
        TPasien.setHighlighter(null);
        TPasien.setName("TPasien"); // NOI18N
        FormInput.add(TPasien);
        TPasien.setBounds(324, 10, 270, 23);

        jLabel8.setText("Tgl.Lahir :");
        jLabel8.setName("jLabel8"); // NOI18N
        FormInput.add(jLabel8);
        jLabel8.setBounds(598, 10, 70, 23);

        TglLahir.setEditable(false);
        TglLahir.setHighlighter(null);
        TglLahir.setName("TglLahir"); // NOI18N
        FormInput.add(TglLahir);
        TglLahir.setBounds(672, 10, 100, 23);

        Jk.setEditable(false);
        Jk.setHighlighter(null);
        Jk.setName("Jk"); // NOI18N
        FormInput.add(Jk);
        Jk.setBounds(774, 10, 40, 23);

        jLabel16.setText("Dokter :");
        jLabel16.setName("jLabel16"); // NOI18N
        FormInput.add(jLabel16);
        jLabel16.setBounds(0, 40, 80, 23);

        KdDokter.setEditable(false);
        KdDokter.setHighlighter(null);
        KdDokter.setName("KdDokter"); // NOI18N
        FormInput.add(KdDokter);
        KdDokter.setBounds(84, 40, 110, 23);

        NmDokter.setEditable(false);
        NmDokter.setHighlighter(null);
        NmDokter.setName("NmDokter"); // NOI18N
        FormInput.add(NmDokter);
        NmDokter.setBounds(196, 40, 250, 23);

        BtnDokter.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        BtnDokter.setMnemonic(50);
        BtnDokter.setToolTipText("Alt+2");
        BtnDokter.setName("BtnDokter"); // NOI18N
        BtnDokter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnDokterActionPerformed(evt);
            }
        });
        FormInput.add(BtnDokter);
        BtnDokter.setBounds(448, 40, 28, 23);

        jLabel18.setText("Tanggal :");
        jLabel18.setName("jLabel18"); // NOI18N
        FormInput.add(jLabel18);
        jLabel18.setBounds(598, 40, 70, 23);

        Tanggal.setForeground(new java.awt.Color(50, 70, 50));
        Tanggal.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "09-11-2026 00:00:00" }));
        Tanggal.setDisplayFormat("dd-MM-yyyy HH:mm:ss");
        Tanggal.setName("Tanggal"); // NOI18N
        Tanggal.setOpaque(false);
        FormInput.add(Tanggal);
        Tanggal.setBounds(672, 40, 135, 23);

        jSeparator1.setBackground(new java.awt.Color(239, 244, 234));
        jSeparator1.setForeground(new java.awt.Color(239, 244, 234));
        jSeparator1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        jSeparator1.setName("jSeparator1"); // NOI18N
        FormInput.add(jSeparator1);
        jSeparator1.setBounds(0, 72, 960, 1);

        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel9.setText("Kondisi PDGI (pilih, lalu klik gigi) :");
        jLabel9.setName("jLabel9"); // NOI18N
        FormInput.add(jLabel9);
        jLabel9.setBounds(10, 78, 250, 23);

        scrollPalet.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPalet.setName("scrollPalet"); // NOI18N

        Palet.setToolTipText("Pilih kondisi, lalu klik gigi/permukaan pada odontogram");
        Palet.setName("Palet"); // NOI18N
        scrollPalet.setViewportView(Palet);

        FormInput.add(scrollPalet);
        scrollPalet.setBounds(10, 98, 250, 400);

        BtnAmbilTerakhir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/editcopy.png"))); // NOI18N
        BtnAmbilTerakhir.setText("Data Terakhir");
        BtnAmbilTerakhir.setToolTipText("Ambil data terakhir: isi gambar dengan kondisi gigi dari pemeriksaan odontogram sebelumnya");
        BtnAmbilTerakhir.setName("BtnAmbilTerakhir"); // NOI18N
        BtnAmbilTerakhir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnAmbilTerakhirActionPerformed(evt);
            }
        });
        FormInput.add(BtnAmbilTerakhir);
        BtnAmbilTerakhir.setBounds(10, 502, 124, 24);

        BtnHapusGambar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/delete-16x16.png"))); // NOI18N
        BtnHapusGambar.setText("Hapus Tanda");
        BtnHapusGambar.setToolTipText("Hapus semua tanda pada odontogram");
        BtnHapusGambar.setName("BtnHapusGambar"); // NOI18N
        BtnHapusGambar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnHapusGambarActionPerformed(evt);
            }
        });
        FormInput.add(BtnHapusGambar);
        BtnHapusGambar.setBounds(136, 502, 124, 24);

        Gambar.setName("Gambar"); // NOI18N
        Gambar.setLayout(null);

        FormInput.add(Gambar);
        Gambar.setBounds(270, 78, 680, 424);

        LInfo.setForeground(new java.awt.Color(0, 90, 160));
        LInfo.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LInfo.setName("LInfo"); // NOI18N
        FormInput.add(LInfo);
        LInfo.setBounds(270, 504, 680, 20);

        jSeparator2.setBackground(new java.awt.Color(239, 244, 234));
        jSeparator2.setForeground(new java.awt.Color(239, 244, 234));
        jSeparator2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        jSeparator2.setName("jSeparator2"); // NOI18N
        FormInput.add(jSeparator2);
        jSeparator2.setBounds(0, 530, 960, 1);

        jLabel10.setText("Occlusi :");
        jLabel10.setName("jLabel10"); // NOI18N
        FormInput.add(jLabel10);
        jLabel10.setBounds(0, 538, 80, 23);

        Occlusi.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Normal Bite", "Cross Bite", "Steep Bite" }));
        Occlusi.setName("Occlusi"); // NOI18N
        FormInput.add(Occlusi);
        Occlusi.setBounds(84, 538, 120, 23);

        jLabel11.setText("Torus Palatinus :");
        jLabel11.setName("jLabel11"); // NOI18N
        FormInput.add(jLabel11);
        jLabel11.setBounds(210, 538, 110, 23);

        TorusPalatinus.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Tidak Ada", "Kecil", "Sedang", "Besar", "Multiple" }));
        TorusPalatinus.setName("TorusPalatinus"); // NOI18N
        FormInput.add(TorusPalatinus);
        TorusPalatinus.setBounds(324, 538, 110, 23);

        jLabel12.setText("Torus Mandibularis :");
        jLabel12.setName("jLabel12"); // NOI18N
        FormInput.add(jLabel12);
        jLabel12.setBounds(440, 538, 125, 23);

        TorusMandibularis.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Tidak Ada", "Sisi Kiri", "Sisi Kanan", "Kedua Sisi" }));
        TorusMandibularis.setName("TorusMandibularis"); // NOI18N
        FormInput.add(TorusMandibularis);
        TorusMandibularis.setBounds(569, 538, 110, 23);

        jLabel13.setText("Palatum :");
        jLabel13.setName("jLabel13"); // NOI18N
        FormInput.add(jLabel13);
        jLabel13.setBounds(685, 538, 70, 23);

        Palatum.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Dalam", "Sedang", "Rendah" }));
        Palatum.setName("Palatum"); // NOI18N
        FormInput.add(Palatum);
        Palatum.setBounds(759, 538, 90, 23);

        jLabel14.setText("Diastema :");
        jLabel14.setName("jLabel14"); // NOI18N
        FormInput.add(jLabel14);
        jLabel14.setBounds(0, 568, 80, 23);

        Diastema.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Tidak Ada", "Ada" }));
        Diastema.setName("Diastema"); // NOI18N
        Diastema.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                DiastemaItemStateChanged(evt);
            }
        });
        FormInput.add(Diastema);
        Diastema.setBounds(84, 568, 90, 23);

        KetDiastema.setName("KetDiastema"); // NOI18N
        KetDiastema.setToolTipText("Jelaskan letak dan ukuran diastema");
        FormInput.add(KetDiastema);
        KetDiastema.setBounds(178, 568, 300, 23);

        jLabel15.setText("Gigi Anomali :");
        jLabel15.setName("jLabel15"); // NOI18N
        FormInput.add(jLabel15);
        jLabel15.setBounds(482, 568, 90, 23);

        Anomali.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Tidak Ada", "Ada" }));
        Anomali.setName("Anomali"); // NOI18N
        Anomali.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                AnomaliItemStateChanged(evt);
            }
        });
        FormInput.add(Anomali);
        Anomali.setBounds(576, 568, 90, 23);

        KetAnomali.setName("KetAnomali"); // NOI18N
        KetAnomali.setToolTipText("Jelaskan gigi mana dan bentuk anomalinya");
        FormInput.add(KetAnomali);
        KetAnomali.setBounds(670, 568, 280, 23);

        jLabel17.setText("Lain-lain :");
        jLabel17.setName("jLabel17"); // NOI18N
        FormInput.add(jLabel17);
        jLabel17.setBounds(0, 598, 80, 23);

        LainLain.setName("LainLain"); // NOI18N
        FormInput.add(LainLain);
        LainLain.setBounds(84, 598, 866, 23);

        jLabel20.setText("D :");
        jLabel20.setName("jLabel20"); // NOI18N
        FormInput.add(jLabel20);
        jLabel20.setBounds(0, 628, 80, 23);

        D.setName("D"); // NOI18N
        D.setToolTipText("Decay: jumlah gigi tetap karies/sisa akar (dihitung otomatis dari gambar, bisa diubah)");
        FormInput.add(D);
        D.setBounds(84, 628, 40, 23);

        jLabel22.setText("M :");
        jLabel22.setName("jLabel22"); // NOI18N
        FormInput.add(jLabel22);
        jLabel22.setBounds(124, 628, 30, 23);

        M.setName("M"); // NOI18N
        M.setToolTipText("Missing: jumlah gigi tetap hilang (dihitung otomatis dari gambar, bisa diubah)");
        FormInput.add(M);
        M.setBounds(158, 628, 40, 23);

        jLabel23.setText("F :");
        jLabel23.setName("jLabel23"); // NOI18N
        FormInput.add(jLabel23);
        jLabel23.setBounds(198, 628, 30, 23);

        F.setName("F"); // NOI18N
        F.setToolTipText("Filled: jumlah gigi tetap ditumpat/mahkota tanpa karies (dihitung otomatis dari gambar, bisa diubah)");
        FormInput.add(F);
        F.setBounds(232, 628, 40, 23);

        jLabel24.setText("Jumlah Foto Diambil :");
        jLabel24.setName("jLabel24"); // NOI18N
        FormInput.add(jLabel24);
        jLabel24.setBounds(282, 628, 130, 23);

        Foto.setName("Foto"); // NOI18N
        Foto.setToolTipText("Misal: 2 (digital)");
        FormInput.add(Foto);
        Foto.setBounds(416, 628, 180, 23);

        jLabel25.setText("Jumlah Rontgen Foto :");
        jLabel25.setName("jLabel25"); // NOI18N
        FormInput.add(jLabel25);
        jLabel25.setBounds(600, 628, 135, 23);

        Rontgen.setName("Rontgen"); // NOI18N
        Rontgen.setToolTipText("Misal: 1 (Dental/PA/OPG/Ceph)");
        FormInput.add(Rontgen);
        Rontgen.setBounds(739, 628, 211, 23);

        scrollInput.setViewportView(FormInput);

        internalFrame2.add(scrollInput, java.awt.BorderLayout.CENTER);

        TabRawat.addTab("Input Odontogram", internalFrame2);

        internalFrame3.setBorder(null);
        internalFrame3.setName("internalFrame3"); // NOI18N
        internalFrame3.setLayout(new java.awt.BorderLayout(1, 1));

        Scroll.setName("Scroll"); // NOI18N
        Scroll.setOpaque(true);
        Scroll.setPreferredSize(new java.awt.Dimension(452, 200));

        tbObat.setToolTipText("Silahkan klik untuk memilih data yang mau diedit ataupun dihapus");
        tbObat.setName("tbObat"); // NOI18N
        tbObat.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbObatMouseClicked(evt);
            }
        });
        tbObat.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tbObatKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                tbObatKeyReleased(evt);
            }
        });
        Scroll.setViewportView(tbObat);

        internalFrame3.add(Scroll, java.awt.BorderLayout.CENTER);

        panelGlass9.setName("panelGlass9"); // NOI18N
        panelGlass9.setPreferredSize(new java.awt.Dimension(44, 44));
        panelGlass9.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        jLabel19.setText("Tgl.Pemeriksaan :");
        jLabel19.setName("jLabel19"); // NOI18N
        jLabel19.setPreferredSize(new java.awt.Dimension(100, 23));
        panelGlass9.add(jLabel19);

        DTPCari1.setForeground(new java.awt.Color(50, 70, 50));
        DTPCari1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "09-11-2026" }));
        DTPCari1.setDisplayFormat("dd-MM-yyyy");
        DTPCari1.setName("DTPCari1"); // NOI18N
        DTPCari1.setOpaque(false);
        DTPCari1.setPreferredSize(new java.awt.Dimension(95, 23));
        panelGlass9.add(DTPCari1);

        jLabel21.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel21.setText("s.d.");
        jLabel21.setName("jLabel21"); // NOI18N
        jLabel21.setPreferredSize(new java.awt.Dimension(23, 23));
        panelGlass9.add(jLabel21);

        DTPCari2.setForeground(new java.awt.Color(50, 70, 50));
        DTPCari2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "09-11-2026" }));
        DTPCari2.setDisplayFormat("dd-MM-yyyy");
        DTPCari2.setName("DTPCari2"); // NOI18N
        DTPCari2.setOpaque(false);
        DTPCari2.setPreferredSize(new java.awt.Dimension(95, 23));
        panelGlass9.add(DTPCari2);

        jLabel6.setText("Key Word :");
        jLabel6.setName("jLabel6"); // NOI18N
        jLabel6.setPreferredSize(new java.awt.Dimension(90, 23));
        panelGlass9.add(jLabel6);

        TCari.setName("TCari"); // NOI18N
        TCari.setPreferredSize(new java.awt.Dimension(310, 23));
        TCari.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TCariKeyPressed(evt);
            }
        });
        panelGlass9.add(TCari);

        BtnCari.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png"))); // NOI18N
        BtnCari.setMnemonic(51);
        BtnCari.setToolTipText("Alt+3");
        BtnCari.setName("BtnCari"); // NOI18N
        BtnCari.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnCari.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnCariActionPerformed(evt);
            }
        });
        panelGlass9.add(BtnCari);

        BtnAll.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Search-16x16.png"))); // NOI18N
        BtnAll.setMnemonic(77);
        BtnAll.setToolTipText("Alt+M");
        BtnAll.setName("BtnAll"); // NOI18N
        BtnAll.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnAllActionPerformed(evt);
            }
        });
        panelGlass9.add(BtnAll);

        internalFrame3.add(panelGlass9, java.awt.BorderLayout.PAGE_END);

        TabRawat.addTab("Data Odontogram", internalFrame3);

        internalFrame1.add(TabRawat, java.awt.BorderLayout.CENTER);

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);

        pack();
    }
    // </editor-fold>//GEN-END:initComponents

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        if(koneksiDB.CARICEPAT().equals("aktif")){
            TCari.getDocument().addDocumentListener(new javax.swing.event.DocumentListener(){
                @Override
                public void insertUpdate(DocumentEvent e) {
                    if(TCari.getText().length()>2){
                        runBackground(()->tampil());
                    }
                }
                @Override
                public void removeUpdate(DocumentEvent e) {
                    if(TCari.getText().length()>2){
                        runBackground(()->tampil());
                    }
                }
                @Override
                public void changedUpdate(DocumentEvent e) {
                    if(TCari.getText().length()>2){
                        runBackground(()->tampil());
                    }
                }
            });
        }
    }//GEN-LAST:event_formWindowOpened

    private void BtnSimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSimpanActionPerformed
        aksiSimpan();
    }//GEN-LAST:event_BtnSimpanActionPerformed

    private void BtnSimpanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnSimpanKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            aksiSimpan();
        }else{
            Valid.pindah(evt,LainLain,BtnBatal);
        }
    }//GEN-LAST:event_BtnSimpanKeyPressed

    private void BtnBatalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnBatalActionPerformed
        emptTeks();
    }//GEN-LAST:event_BtnBatalActionPerformed

    private void BtnBatalKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnBatalKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            emptTeks();
        }else{
            Valid.pindah(evt,BtnSimpan,BtnHapus);
        }
    }//GEN-LAST:event_BtnBatalKeyPressed

    private void BtnHapusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnHapusActionPerformed
        aksiHapus();
    }//GEN-LAST:event_BtnHapusActionPerformed

    private void BtnHapusKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnHapusKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            aksiHapus();
        }else{
            Valid.pindah(evt,BtnBatal,BtnEdit);
        }
    }//GEN-LAST:event_BtnHapusKeyPressed

    private void BtnEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnEditActionPerformed
        aksiGanti();
    }//GEN-LAST:event_BtnEditActionPerformed

    private void BtnEditKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnEditKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            aksiGanti();
        }else{
            Valid.pindah(evt,BtnHapus,BtnPrint);
        }
    }//GEN-LAST:event_BtnEditKeyPressed

    private void BtnPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnPrintActionPerformed
        aksiCetak();
    }//GEN-LAST:event_BtnPrintActionPerformed

    private void BtnPrintKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnPrintKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            aksiCetak();
        }else{
            Valid.pindah(evt,BtnEdit,BtnKeluar);
        }
    }//GEN-LAST:event_BtnPrintKeyPressed

    private void BtnKeluarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnKeluarActionPerformed
        dispose();
    }//GEN-LAST:event_BtnKeluarActionPerformed

    private void BtnKeluarKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnKeluarKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            dispose();
        }else{
            Valid.pindah(evt,BtnPrint,TCari);
        }
    }//GEN-LAST:event_BtnKeluarKeyPressed

    private void TabRawatMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_TabRawatMouseClicked
        if(TabRawat.getSelectedIndex()==1){
            runBackground(()->tampil());
        }
    }//GEN-LAST:event_TabRawatMouseClicked

    private void BtnDokterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnDokterActionPerformed
        aksiCariDokter();
    }//GEN-LAST:event_BtnDokterActionPerformed

    private void BtnAmbilTerakhirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnAmbilTerakhirActionPerformed
        if(Gambar.getJumlahEntri()==0||JOptionPane.showConfirmDialog(this,"Ganti isi gambar dengan data odontogram terakhir pasien?","Konfirmasi",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION){
            ambilDataTerakhir();
        }
    }//GEN-LAST:event_BtnAmbilTerakhirActionPerformed

    private void BtnHapusGambarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnHapusGambarActionPerformed
        if(Gambar.getJumlahEntri()>0&&JOptionPane.showConfirmDialog(this,"Hapus semua tanda pada odontogram?","Konfirmasi",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION){
            Gambar.hapusSemua();
        }
    }//GEN-LAST:event_BtnHapusGambarActionPerformed

    private void DiastemaItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_DiastemaItemStateChanged
        KetDiastema.setEditable(Diastema.getSelectedIndex()==1);
    }//GEN-LAST:event_DiastemaItemStateChanged

    private void AnomaliItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_AnomaliItemStateChanged
        KetAnomali.setEditable(Anomali.getSelectedIndex()==1);
    }//GEN-LAST:event_AnomaliItemStateChanged

    private void tbObatMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbObatMouseClicked
        if(tabMode.getRowCount()!=0){
            getData();
            if(evt.getClickCount()==2){
                TabRawat.setSelectedIndex(0);
            }
        }
    }//GEN-LAST:event_tbObatMouseClicked

    private void tbObatKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbObatKeyPressed
        if(tabMode.getRowCount()!=0&&evt.getKeyCode()==KeyEvent.VK_SPACE){
            getData();
            TabRawat.setSelectedIndex(0);
        }
    }//GEN-LAST:event_tbObatKeyPressed

    private void tbObatKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbObatKeyReleased
        if(tabMode.getRowCount()!=0&&(evt.getKeyCode()==KeyEvent.VK_UP||evt.getKeyCode()==KeyEvent.VK_DOWN)){
            getData();
        }
    }//GEN-LAST:event_tbObatKeyReleased

    private void TCariKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TCariKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            runBackground(()->tampil());
        }
    }//GEN-LAST:event_TCariKeyPressed

    private void BtnCariActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCariActionPerformed
        runBackground(()->tampil());
    }//GEN-LAST:event_BtnCariActionPerformed

    private void BtnAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnAllActionPerformed
        TCari.setText("");
        runBackground(()->tampil());
    }//GEN-LAST:event_BtnAllActionPerformed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            RMOdontogram dialog=new RMOdontogram(new javax.swing.JFrame(),true);
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
    private widget.ComboBox Anomali;
    private widget.Button BtnAll;
    private widget.Button BtnAmbilTerakhir;
    private widget.Button BtnBatal;
    private widget.Button BtnCari;
    private widget.Button BtnDokter;
    private widget.Button BtnEdit;
    private widget.Button BtnHapus;
    private widget.Button BtnHapusGambar;
    private widget.Button BtnKeluar;
    private widget.Button BtnPrint;
    private widget.Button BtnSimpan;
    private widget.TextBox D;
    private widget.Tanggal DTPCari1;
    private widget.Tanggal DTPCari2;
    private widget.ComboBox Diastema;
    private widget.TextBox F;
    private widget.PanelBiasa FormInput;
    private widget.TextBox Foto;
    private widget.PanelOdontogram Gambar;
    private widget.TextBox Jk;
    private widget.TextBox KdDokter;
    private widget.TextBox KetAnomali;
    private widget.TextBox KetDiastema;
    private widget.Label LCount;
    private widget.Label LInfo;
    private widget.TextBox LainLain;
    private widget.TextBox M;
    private widget.TextBox NmDokter;
    private widget.ComboBox Occlusi;
    private widget.ComboBox Palatum;
    private widget.Table Palet;
    private widget.TextBox Rontgen;
    private widget.ScrollPane Scroll;
    private widget.TextBox TCari;
    private widget.TextBox TNoRM;
    private widget.TextBox TNoRw;
    private widget.TextBox TPasien;
    private widget.TabPane TabRawat;
    private widget.Tanggal Tanggal;
    private widget.TextBox TanggalRegistrasi;
    private widget.TextBox TglLahir;
    private widget.ComboBox TorusMandibularis;
    private widget.ComboBox TorusPalatinus;
    private widget.InternalFrame internalFrame1;
    private widget.InternalFrame internalFrame2;
    private widget.InternalFrame internalFrame3;
    private widget.Label jLabel10;
    private widget.Label jLabel11;
    private widget.Label jLabel12;
    private widget.Label jLabel13;
    private widget.Label jLabel14;
    private widget.Label jLabel15;
    private widget.Label jLabel16;
    private widget.Label jLabel17;
    private widget.Label jLabel18;
    private widget.Label jLabel19;
    private widget.Label jLabel20;
    private widget.Label jLabel21;
    private widget.Label jLabel22;
    private widget.Label jLabel23;
    private widget.Label jLabel24;
    private widget.Label jLabel25;
    private widget.Label jLabel4;
    private widget.Label jLabel6;
    private widget.Label jLabel7;
    private widget.Label jLabel8;
    private widget.Label jLabel9;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private widget.panelisi panelGlass8;
    private widget.panelisi panelGlass9;
    private widget.ScrollPane scrollInput;
    private widget.ScrollPane scrollPalet;
    private widget.Table tbObat;
    // End of variables declaration//GEN-END:variables

    // ======================= Aksi =======================
    private String tanggalSimpan(){
        return Valid.SetTgl(Tanggal.getSelectedItem()+"")+" "+Tanggal.getSelectedItem().toString().substring(11,19);
    }

    private boolean cekIsian(){
        if(TNoRw.getText().trim().equals("")||TNoRM.getText().trim().equals("")){
            Valid.textKosong(TNoRw,"Pasien");
            return false;
        }else if(KdDokter.getText().trim().equals("")||NmDokter.getText().trim().equals("")){
            Valid.textKosong(BtnDokter,"Dokter");
            return false;
        }
        return true;
    }

    private boolean cekTanggalReg(){
        if(akses.getkode().equals("Admin Utama")){
            return true;
        }
        if(TanggalRegistrasi.getText().equals("")){
            TanggalRegistrasi.setText(Sequel.cariIsi("select concat(reg_periksa.tgl_registrasi,' ',reg_periksa.jam_reg) from reg_periksa where reg_periksa.no_rawat=?",TNoRw.getText()));
        }
        return Sequel.cekTanggalRegistrasi(TanggalRegistrasi.getText(),tanggalSimpan());
    }

    private void aksiSimpan() {
        if(cekIsian()){
            if(Sequel.cariInteger("select count(odontogram.no_rawat) from odontogram where odontogram.no_rawat=?",TNoRw.getText())>0){
                JOptionPane.showMessageDialog(null,"Odontogram untuk No.Rawat ini sudah ada, silahkan pilih datanya lalu gunakan tombol Ganti..!!");
            }else if(cekTanggalReg()){
                simpan();
            }
        }
    }

    /** Pemilik data (dokter yang sama, maks 48 jam) atau Admin Utama. */
    private boolean bolehUbah(String aksi){
        if(akses.getkode().equals("Admin Utama")){
            return true;
        }
        int baris=tbObat.getSelectedRow();
        if(KdDokter.getText().equals(tbObat.getValueAt(baris,5).toString())){
            return Sequel.cekTanggal48jam(tbObat.getValueAt(baris,7).toString(),Sequel.ambiltanggalsekarang());
        }
        JOptionPane.showMessageDialog(null,"Hanya bisa "+aksi+" oleh dokter yang bersangkutan..!!");
        return false;
    }

    private void aksiHapus() {
        if(tbObat.getSelectedRow()>-1){
            if(bolehUbah("dihapus")&&JOptionPane.showConfirmDialog(this,"Hapus odontogram No.Rawat "+tbObat.getValueAt(tbObat.getSelectedRow(),0)+"?","Konfirmasi",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION){
                hapus();
            }
        }else{
            JOptionPane.showMessageDialog(rootPane,"Silahkan anda pilih data terlebih dahulu di tab Data Odontogram..!!");
        }
    }

    private void aksiGanti() {
        if(cekIsian()){
            if(tbObat.getSelectedRow()>-1){
                if(!tbObat.getValueAt(tbObat.getSelectedRow(),0).toString().equals(TNoRw.getText())){
                    JOptionPane.showMessageDialog(null,"Data yang dipilih di tabel berbeda dengan No.Rawat di form..!!");
                }else if(bolehUbah("diganti")&&cekTanggalReg()){
                    ganti();
                }
            }else{
                JOptionPane.showMessageDialog(rootPane,"Silahkan anda pilih data terlebih dahulu di tab Data Odontogram..!!");
            }
        }
    }

    private void aksiCariDokter() {
        if(dokter==null||!dokter.isDisplayable()){
            dokter=new DlgCariDokter(null,false);
            dokter.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            dokter.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    if(dokter.getTable().getSelectedRow()!=-1){
                        KdDokter.setText(dokter.getTable().getValueAt(dokter.getTable().getSelectedRow(),0).toString());
                        NmDokter.setText(dokter.getTable().getValueAt(dokter.getTable().getSelectedRow(),1).toString());
                    }
                    BtnDokter.requestFocus();
                    dokter=null;
                }
            });
            dokter.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
            dokter.setLocationRelativeTo(internalFrame1);
        }
        dokter.isCek();
        if(dokter.isVisible()){
            dokter.toFront();
            return;
        }
        dokter.setVisible(true);
    }

    private void aksiCetak() {
        if(TNoRw.getText().trim().equals("")){
            JOptionPane.showMessageDialog(null,"Silahkan pilih pasien terlebih dahulu..!!");
            return;
        }
        try {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            File f=new File("odontogram.html");
            try (BufferedWriter bw=new BufferedWriter(new FileWriter(f))){
                bw.write("<html><head><meta charset='utf-8'><title>Odontogram</title>"+
                        "<style>body{font-family:Tahoma,Arial;font-size:12px;color:#222;margin:20px}td{font-size:12px;padding:3px;vertical-align:top}"+
                        "table.b{border-collapse:collapse}table.b td{border:1px solid #999}</style></head><body>");
                bw.write("<table width='100%'><tr><td align='center'><b style='font-size:15px'>"+esc(akses.getnamars())+"</b><br>"+
                        esc(akses.getalamatrs())+", "+esc(akses.getkabupatenrs())+"<br>"+esc(akses.getkontakrs())+"</td></tr></table><hr>");
                bw.write("<p align='center'><b style='font-size:14px'>ODONTOGRAM</b></p>");
                bw.write(htmlIsi(TNoRw.getText(),TNoRM.getText(),TPasien.getText(),TglLahir.getText(),Jk.getText(),
                        tanggalSimpan(),KdDokter.getText(),NmDokter.getText(),Gambar.getData(),
                        Occlusi.getSelectedItem()+"",TorusPalatinus.getSelectedItem()+"",TorusMandibularis.getSelectedItem()+"",Palatum.getSelectedItem()+"",
                        teksDiastema(),teksAnomali(),LainLain.getText(),D.getText(),M.getText(),F.getText(),Foto.getText(),Rontgen.getText()));
                bw.write("<br><table width='100%'><tr><td width='60%'></td><td align='center'>"+esc(akses.getkabupatenrs())+", "+
                        esc(Valid.SetTgl3(tanggalSimpan().substring(0,10)))+"<br>Dokter Pemeriksa<br><br><br><br>( "+esc(NmDokter.getText())+" )</td></tr></table>");
                bw.write("</body></html>");
            }
            Desktop.getDesktop().browse(f.toURI());
        } catch (Exception e) {
            System.out.println("Notifikasi : "+e);
            JOptionPane.showMessageDialog(null,"Gagal membuat cetakan odontogram : "+e.getMessage());
        } finally {
            this.setCursor(Cursor.getDefaultCursor());
        }
    }

    private static String esc(String s){
        if(s==null){
            return "";
        }
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("'","&#39;");
    }

    /** Isi HTML odontogram (dipakai cetak form). */
    private static String htmlIsi(String norawat,String norm,String nama,String tgllahir,String jk,String tanggal,String kddokter,String nmdokter,
            String data,String occlusi,String torusp,String torusm,String palatum,String diastema,String anomali,String lain,
            String d,String m,String f,String foto,String rontgen){
        StringBuilder sb=new StringBuilder();
        sb.append("<table width='100%' class='b'>")
          .append("<tr><td width='18%'>No.Rawat</td><td width='32%'>").append(esc(norawat)).append("</td><td width='18%'>Tanggal Pemeriksaan</td><td width='32%'>").append(esc(tanggal)).append("</td></tr>")
          .append("<tr><td>No.RM / Nama</td><td>").append(esc(norm)).append(" / ").append(esc(nama)).append("</td><td>Tgl.Lahir / J.K.</td><td>").append(esc(tgllahir)).append(" / ").append(esc(jk)).append("</td></tr>")
          .append("<tr><td>Dokter</td><td colspan='3'>").append(esc(kddokter)).append(" ").append(esc(nmdokter)).append("</td></tr></table><br>");
        sb.append("<div align='center'><img src='").append(PanelOdontogram.urlGambar(data,1360)).append("' width='680' height='")
          .append(Math.round(680.0*PanelOdontogram.TINGGI_DASAR/PanelOdontogram.LEBAR_DASAR)).append("'></div><br>");
        sb.append(htmlKeterangan(data));
        sb.append("<br><table width='100%' class='b'>")
          .append("<tr><td width='18%'>Occlusi</td><td width='32%'>").append(esc(occlusi)).append("</td><td width='18%'>Torus Palatinus</td><td width='32%'>").append(esc(torusp)).append("</td></tr>")
          .append("<tr><td>Torus Mandibularis</td><td>").append(esc(torusm)).append("</td><td>Palatum</td><td>").append(esc(palatum)).append("</td></tr>")
          .append("<tr><td>Diastema</td><td>").append(esc(diastema)).append("</td><td>Gigi Anomali</td><td>").append(esc(anomali)).append("</td></tr>")
          .append("<tr><td>Lain-lain</td><td colspan='3'>").append(esc(lain)).append("</td></tr>")
          .append("<tr><td>D / M / F</td><td>D = ").append(esc(d)).append(", M = ").append(esc(m)).append(", F = ").append(esc(f)).append("</td><td>Jumlah Foto / Rontgen</td><td>")
          .append(esc(foto)).append(" / ").append(esc(rontgen)).append("</td></tr></table>");
        return sb.toString();
    }

    /** Tabel keterangan kondisi per gigi (juga dipakai riwayat perawatan). */
    public static String htmlKeterangan(String data){
        List<String> ringkas=PanelOdontogram.ringkasan(data);
        StringBuilder sb=new StringBuilder("<table width='100%' class='b' border='0' cellpadding='3px' cellspacing='0'>");
        if(ringkas.isEmpty()){
            sb.append("<tr><td>Semua gigi dalam keadaan normal / tidak ada tanda.</td></tr>");
        }else{
            for(String r:ringkas){
                int p=r.indexOf(" : ");
                sb.append("<tr><td width='8%' align='center'>").append(esc(r.substring(0,p))).append("</td><td width='92%'>").append(esc(r.substring(p+3))).append("</td></tr>");
            }
        }
        sb.append("</table>");
        // legenda: arti simbol/warna untuk kode yang dipakai
        List<String> kode=PanelOdontogram.kodeTerpakai(data);
        if(!kode.isEmpty()){
            sb.append("Keterangan simbol :<table width='100%' class='b' border='0' cellpadding='3px' cellspacing='0'>");
            for(String k:kode){
                sb.append("<tr><td width='8%' align='center'>").append(k).append("</td><td width='32%'>").append(esc(PanelOdontogram.namaKondisi(k)))
                  .append("</td><td width='60%'>").append(esc(PanelOdontogram.keteranganSimbol(k))).append("</td></tr>");
            }
            sb.append("</table>");
        }
        return sb.toString();
    }

    private String teksDiastema(){
        return Diastema.getSelectedIndex()==0?"Tidak Ada":("Ada"+(KetDiastema.getText().trim().equals("")?"":", "+KetDiastema.getText().trim()));
    }

    private String teksAnomali(){
        return Anomali.getSelectedIndex()==0?"Tidak Ada":("Ada"+(KetAnomali.getText().trim().equals("")?"":", "+KetAnomali.getText().trim()));
    }

    private void setTeksPilihan(String teks,widget.ComboBox combo,widget.TextBox ket){
        if(teks==null||teks.equals("")||teks.equals("Tidak Ada")){
            combo.setSelectedIndex(0);
            ket.setText("");
        }else{
            combo.setSelectedIndex(1);
            ket.setText(teks.startsWith("Ada, ")?teks.substring(5):(teks.equals("Ada")?"":teks));
        }
        ket.setEditable(combo.getSelectedIndex()==1);
    }

    private void hitungDMF(){
        int[] dmf=Gambar.getDMF();
        D.setText(""+dmf[0]);
        M.setText(""+dmf[1]);
        F.setText(""+dmf[2]);
    }

    // ======================= Data =======================
    private void buatTabel() {
        // tabel tambahan RS (lihat update_odontogram.sql), dibuat otomatis bila belum ada
        try (java.sql.Statement st=koneksi.createStatement()){
            st.executeUpdate("CREATE TABLE IF NOT EXISTS `odontogram` ("+
                    "`no_rawat` varchar(17) NOT NULL,"+
                    "`tanggal` datetime NOT NULL,"+
                    "`kd_dokter` varchar(20) NOT NULL,"+
                    "`occlusi` varchar(20) DEFAULT NULL,"+
                    "`torus_palatinus` varchar(20) DEFAULT NULL,"+
                    "`torus_mandibularis` varchar(20) DEFAULT NULL,"+
                    "`palatum` varchar(10) DEFAULT NULL,"+
                    "`diastema` varchar(110) DEFAULT NULL,"+
                    "`gigi_anomali` varchar(110) DEFAULT NULL,"+
                    "`lain_lain` varchar(200) DEFAULT NULL,"+
                    "`d` tinyint(4) DEFAULT NULL,"+
                    "`m` tinyint(4) DEFAULT NULL,"+
                    "`f` tinyint(4) DEFAULT NULL,"+
                    "`foto` varchar(50) DEFAULT NULL,"+
                    "`rontgen` varchar(50) DEFAULT NULL,"+
                    "PRIMARY KEY (`no_rawat`),"+
                    "KEY `kd_dokter` (`kd_dokter`),"+
                    "KEY `tanggal` (`tanggal`),"+
                    "CONSTRAINT `odontogram_ibfk_1` FOREIGN KEY (`no_rawat`) REFERENCES `reg_periksa` (`no_rawat`) ON DELETE CASCADE ON UPDATE CASCADE,"+
                    "CONSTRAINT `odontogram_ibfk_2` FOREIGN KEY (`kd_dokter`) REFERENCES `dokter` (`kd_dokter`) ON DELETE CASCADE ON UPDATE CASCADE"+
                    ") ENGINE=InnoDB DEFAULT CHARSET=latin1");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS `odontogram_detail` ("+
                    "`no_rawat` varchar(17) NOT NULL,"+
                    "`gigi` char(2) NOT NULL,"+
                    "`permukaan` char(1) NOT NULL DEFAULT '',"+
                    "`kode` varchar(5) NOT NULL,"+
                    "PRIMARY KEY (`no_rawat`,`gigi`,`permukaan`,`kode`),"+
                    "CONSTRAINT `odontogram_detail_ibfk_1` FOREIGN KEY (`no_rawat`) REFERENCES `odontogram` (`no_rawat`) ON DELETE CASCADE ON UPDATE CASCADE"+
                    ") ENGINE=InnoDB DEFAULT CHARSET=latin1");
        } catch (Exception e) {
            System.out.println("Notifikasi buat tabel odontogram : "+e);
        }
    }

    /** Data gambar odontogram tersimpan untuk satu no_rawat, format PanelOdontogram ("gigi:permukaan:kode;..."). */
    public static String dataGambar(Connection koneksi,String norawat){
        StringBuilder sb=new StringBuilder();
        try (PreparedStatement ps=koneksi.prepareStatement("select odontogram_detail.gigi,odontogram_detail.permukaan,odontogram_detail.kode from odontogram_detail where odontogram_detail.no_rawat=?")){
            ps.setString(1,norawat);
            try (ResultSet rs=ps.executeQuery()){
                while(rs.next()){
                    if(sb.length()>0){
                        sb.append(";");
                    }
                    sb.append(rs.getString(1)).append(":").append(rs.getString(2)).append(":").append(rs.getString(3));
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi odontogram_detail : "+e);
        }
        return sb.toString();
    }

    private void muatGambar(String data){
        memuat=true;
        try {
            Gambar.setData(data);
        } finally {
            memuat=false;
        }
    }

    private boolean simpanDetail(){
        if(!Sequel.queryu2tf("delete from odontogram_detail where no_rawat=?",1,new String[]{TNoRw.getText()})){
            return false;
        }
        for(String[] e:Gambar.getEntri()){
            if(!Sequel.queryu2tf("insert into odontogram_detail(no_rawat,gigi,permukaan,kode) values(?,?,?,?)",4,new String[]{
                TNoRw.getText(),e[0],e[1],e[2]
            })){
                return false;
            }
        }
        return true;
    }

    private String[] isiHeader(){
        return new String[]{
            Occlusi.getSelectedItem()+"",TorusPalatinus.getSelectedItem()+"",TorusMandibularis.getSelectedItem()+"",Palatum.getSelectedItem()+"",
            teksDiastema(),teksAnomali(),LainLain.getText(),angka(D.getText()),angka(M.getText()),angka(F.getText()),Foto.getText(),Rontgen.getText()
        };
    }

    private static String angka(String s){
        return s==null||s.trim().equals("")?"0":s.trim();
    }

    private void simpan() {
        String[] h=isiHeader();
        Sequel.AutoComitFalse();
        boolean sukses=Sequel.menyimpantf("odontogram","?,?,?,?,?,?,?,?,?,?,?,?,?,?,?","Odontogram",15,new String[]{
            TNoRw.getText(),tanggalSimpan(),KdDokter.getText(),h[0],h[1],h[2],h[3],h[4],h[5],h[6],h[7],h[8],h[9],h[10],h[11]
        })&&simpanDetail();
        if(sukses){
            Sequel.Commit();
        }else{
            Sequel.RollBack();
        }
        Sequel.AutoComitTrue();
        if(sukses){
            tabMode.addRow(barisTabel(h));
            LCount.setText(""+tabMode.getRowCount());
            JOptionPane.showMessageDialog(null,"Odontogram berhasil disimpan..!!");
            LInfo.setText("Tersimpan untuk No.Rawat "+TNoRw.getText());
        }else{
            JOptionPane.showMessageDialog(null,"Gagal menyimpan odontogram..!!");
        }
    }

    private void ganti() {
        String[] h=isiHeader();
        Sequel.AutoComitFalse();
        boolean sukses=Sequel.mengedittf("odontogram","no_rawat=?","tanggal=?,kd_dokter=?,occlusi=?,torus_palatinus=?,torus_mandibularis=?,palatum=?,diastema=?,gigi_anomali=?,lain_lain=?,d=?,m=?,f=?,foto=?,rontgen=?",15,new String[]{
            tanggalSimpan(),KdDokter.getText(),h[0],h[1],h[2],h[3],h[4],h[5],h[6],h[7],h[8],h[9],h[10],h[11],TNoRw.getText()
        })&&simpanDetail();
        if(sukses){
            Sequel.Commit();
        }else{
            Sequel.RollBack();
        }
        Sequel.AutoComitTrue();
        if(sukses){
            Object[] baris=barisTabel(h);
            for(int i=0;i<baris.length;i++){
                tbObat.setValueAt(baris[i],tbObat.getSelectedRow(),i);
            }
            JOptionPane.showMessageDialog(null,"Odontogram berhasil diganti..!!");
        }else{
            JOptionPane.showMessageDialog(null,"Gagal mengganti odontogram..!!");
        }
    }

    private Object[] barisTabel(String[] h){
        return new Object[]{
            TNoRw.getText(),TNoRM.getText(),TPasien.getText(),TglLahir.getText(),Jk.getText(),KdDokter.getText(),NmDokter.getText(),tanggalSimpan(),
            h[0],h[1],h[2],h[3],h[4],h[5],h[6],h[7],h[8],h[9],h[10],h[11]
        };
    }

    private void hapus() {
        if(Sequel.queryu2tf("delete from odontogram where no_rawat=?",1,new String[]{
            tbObat.getValueAt(tbObat.getSelectedRow(),0).toString()
        })){
            boolean formSama=tbObat.getValueAt(tbObat.getSelectedRow(),0).toString().equals(TNoRw.getText());
            tabMode.removeRow(tbObat.getSelectedRow());
            LCount.setText(""+tabMode.getRowCount());
            if(formSama){
                emptTeks();
            }
        }else{
            JOptionPane.showMessageDialog(null,"Gagal menghapus..!!");
        }
    }

    private void tampil() {
        Valid.tabelKosong(tabMode);
        String sql="select reg_periksa.no_rawat,pasien.no_rkm_medis,pasien.nm_pasien,pasien.tgl_lahir,pasien.jk,odontogram.kd_dokter,dokter.nm_dokter,odontogram.tanggal,"+
                "odontogram.occlusi,odontogram.torus_palatinus,odontogram.torus_mandibularis,odontogram.palatum,odontogram.diastema,odontogram.gigi_anomali,"+
                "odontogram.lain_lain,odontogram.d,odontogram.m,odontogram.f,odontogram.foto,odontogram.rontgen from odontogram "+
                "inner join reg_periksa on odontogram.no_rawat=reg_periksa.no_rawat "+
                "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                "inner join dokter on odontogram.kd_dokter=dokter.kd_dokter "+
                "where odontogram.tanggal between ? and ? "+
                (TCari.getText().trim().equals("")?"":"and (reg_periksa.no_rawat like ? or pasien.no_rkm_medis like ? or pasien.nm_pasien like ? or odontogram.kd_dokter like ? or dokter.nm_dokter like ?) ")+
                "order by odontogram.tanggal";
        try (PreparedStatement ps=koneksi.prepareStatement(sql)){
            ps.setString(1,Valid.SetTgl(DTPCari1.getSelectedItem()+"")+" 00:00:00");
            ps.setString(2,Valid.SetTgl(DTPCari2.getSelectedItem()+"")+" 23:59:59");
            if(!TCari.getText().trim().equals("")){
                for(int i=3;i<=7;i++){
                    ps.setString(i,"%"+TCari.getText()+"%");
                }
            }
            try (ResultSet rs=ps.executeQuery()){
                while(rs.next()){
                    tabMode.addRow(new Object[]{
                        rs.getString("no_rawat"),rs.getString("no_rkm_medis"),rs.getString("nm_pasien"),rs.getString("tgl_lahir"),rs.getString("jk"),
                        rs.getString("kd_dokter"),rs.getString("nm_dokter"),rs.getString("tanggal"),rs.getString("occlusi"),rs.getString("torus_palatinus"),
                        rs.getString("torus_mandibularis"),rs.getString("palatum"),rs.getString("diastema"),rs.getString("gigi_anomali"),rs.getString("lain_lain"),
                        rs.getString("d"),rs.getString("m"),rs.getString("f"),rs.getString("foto"),rs.getString("rontgen")
                    });
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi : "+e);
        }
        SwingUtilities.invokeLater(()->LCount.setText(""+tabMode.getRowCount()));
    }

    private static String nilai(Object o){
        return o==null?"":o.toString();
    }

    private void getData() {
        int r=tbObat.getSelectedRow();
        if(r!=-1){
            TNoRw.setText(nilai(tbObat.getValueAt(r,0)));
            TNoRM.setText(nilai(tbObat.getValueAt(r,1)));
            TPasien.setText(nilai(tbObat.getValueAt(r,2)));
            TglLahir.setText(nilai(tbObat.getValueAt(r,3)));
            Jk.setText(nilai(tbObat.getValueAt(r,4)));
            if(akses.getjml2()<1||KdDokter.isEditable()){
                KdDokter.setText(nilai(tbObat.getValueAt(r,5)));
                NmDokter.setText(nilai(tbObat.getValueAt(r,6)));
            }
            Valid.SetTgl2(Tanggal,nilai(tbObat.getValueAt(r,7)));
            Occlusi.setSelectedItem(nilai(tbObat.getValueAt(r,8)));
            TorusPalatinus.setSelectedItem(nilai(tbObat.getValueAt(r,9)));
            TorusMandibularis.setSelectedItem(nilai(tbObat.getValueAt(r,10)));
            Palatum.setSelectedItem(nilai(tbObat.getValueAt(r,11)));
            setTeksPilihan(nilai(tbObat.getValueAt(r,12)),Diastema,KetDiastema);
            setTeksPilihan(nilai(tbObat.getValueAt(r,13)),Anomali,KetAnomali);
            LainLain.setText(nilai(tbObat.getValueAt(r,14)));
            muatGambar(dataGambar(koneksi,TNoRw.getText()));
            D.setText(nilai(tbObat.getValueAt(r,15)));
            M.setText(nilai(tbObat.getValueAt(r,16)));
            F.setText(nilai(tbObat.getValueAt(r,17)));
            Foto.setText(nilai(tbObat.getValueAt(r,18)));
            Rontgen.setText(nilai(tbObat.getValueAt(r,19)));
            TanggalRegistrasi.setText("");
            LInfo.setText("Data tersimpan No.Rawat "+TNoRw.getText()+", pemeriksaan "+nilai(tbObat.getValueAt(r,7)));
        }
    }

    /** Isi form dari odontogram yang sudah tersimpan untuk no_rawat ini. true bila ada. */
    private boolean muatDataRawat(){
        boolean ada=false;
        try (PreparedStatement ps=koneksi.prepareStatement("select odontogram.*,dokter.nm_dokter from odontogram inner join dokter on odontogram.kd_dokter=dokter.kd_dokter where odontogram.no_rawat=?")){
            ps.setString(1,TNoRw.getText());
            try (ResultSet rs=ps.executeQuery()){
                if(rs.next()){
                    ada=true;
                    isiHeaderDari(rs);
                    Valid.SetTgl2(Tanggal,rs.getString("tanggal"));
                    D.setText(rs.getString("d"));
                    M.setText(rs.getString("m"));
                    F.setText(rs.getString("f"));
                    Foto.setText(rs.getString("foto"));
                    Rontgen.setText(rs.getString("rontgen"));
                    muatGambar(dataGambar(koneksi,TNoRw.getText()));
                    LInfo.setText("Odontogram kunjungan ini sudah tersimpan ("+rs.getString("tanggal")+", "+rs.getString("nm_dokter")+"). Ubah lalu tekan Ganti.");
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi : "+e);
        }
        return ada;
    }

    private void isiHeaderDari(ResultSet rs) throws java.sql.SQLException {
        Occlusi.setSelectedItem(rs.getString("occlusi"));
        TorusPalatinus.setSelectedItem(rs.getString("torus_palatinus"));
        TorusMandibularis.setSelectedItem(rs.getString("torus_mandibularis"));
        Palatum.setSelectedItem(rs.getString("palatum"));
        setTeksPilihan(rs.getString("diastema"),Diastema,KetDiastema);
        setTeksPilihan(rs.getString("gigi_anomali"),Anomali,KetAnomali);
        LainLain.setText(rs.getString("lain_lain"));
    }

    /** Bawa kondisi gigi dari odontogram terakhir pasien (kunjungan sebelumnya) ke form. */
    private void ambilDataTerakhir(){
        try (PreparedStatement ps=koneksi.prepareStatement(
                "select odontogram.*,dokter.nm_dokter from odontogram inner join reg_periksa on odontogram.no_rawat=reg_periksa.no_rawat "+
                "inner join dokter on odontogram.kd_dokter=dokter.kd_dokter "+
                "where reg_periksa.no_rkm_medis=? and odontogram.no_rawat<>? order by odontogram.tanggal desc limit 1")){
            ps.setString(1,TNoRM.getText());
            ps.setString(2,TNoRw.getText());
            try (ResultSet rs=ps.executeQuery()){
                if(rs.next()){
                    isiHeaderDari(rs);
                    muatGambar(dataGambar(koneksi,rs.getString("no_rawat")));
                    hitungDMF();
                    LInfo.setText("Kondisi gigi dibawa dari pemeriksaan "+rs.getString("tanggal")+" (No.Rawat "+rs.getString("no_rawat")+", "+rs.getString("nm_dokter")+"). Perbarui lalu Simpan.");
                }else{
                    LInfo.setText("Belum ada odontogram sebelumnya untuk pasien ini.");
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi : "+e);
        }
    }

    private void isRawat() {
        try (PreparedStatement ps=koneksi.prepareStatement(
                "select reg_periksa.no_rkm_medis,pasien.nm_pasien,pasien.jk,pasien.tgl_lahir,reg_periksa.tgl_registrasi,reg_periksa.jam_reg "+
                "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis where reg_periksa.no_rawat=?")){
            ps.setString(1,TNoRw.getText());
            try (ResultSet rs=ps.executeQuery()){
                if(rs.next()){
                    TNoRM.setText(rs.getString("no_rkm_medis"));
                    DTPCari1.setDate(rs.getDate("tgl_registrasi"));
                    TPasien.setText(rs.getString("nm_pasien"));
                    Jk.setText(rs.getString("jk"));
                    TglLahir.setText(rs.getString("tgl_lahir"));
                    TanggalRegistrasi.setText(rs.getString("tgl_registrasi")+" "+rs.getString("jam_reg"));
                }
            }
        } catch (Exception e) {
            System.out.println("Notif : "+e);
        }
    }

    public void emptTeks() {
        Occlusi.setSelectedIndex(0);
        TorusPalatinus.setSelectedIndex(0);
        TorusMandibularis.setSelectedIndex(0);
        Palatum.setSelectedIndex(1);
        Diastema.setSelectedIndex(0);
        KetDiastema.setText("");
        KetDiastema.setEditable(false);
        Anomali.setSelectedIndex(0);
        KetAnomali.setText("");
        KetAnomali.setEditable(false);
        LainLain.setText("");
        Foto.setText("");
        Rontgen.setText("");
        muatGambar("");
        hitungDMF();
        Tanggal.setDate(new Date());
        LInfo.setText("");
        TabRawat.setSelectedIndex(0);
    }

    /** Dipanggil dari Rawat Jalan: muat odontogram kunjungan ini, atau bawa kondisi dari kunjungan terakhir. */
    public void setNoRm(String norwt,Date tgl2) {
        TNoRw.setText(norwt);
        TCari.setText(norwt);
        DTPCari2.setDate(tgl2);
        isRawat();
        emptTeks();
        if(!muatDataRawat()){
            ambilDataTerakhir();
        }
        runBackground(()->tampil());
    }

    public void isCek(){
        BtnSimpan.setEnabled(akses.getodontogram());
        BtnHapus.setEnabled(akses.getodontogram());
        BtnEdit.setEnabled(akses.getodontogram());
        BtnPrint.setEnabled(akses.getodontogram());
        Gambar.setBisaDitandai(akses.getodontogram());
        if(akses.getjml2()>=1){
            KdDokter.setEditable(false);
            BtnDokter.setEnabled(false);
            KdDokter.setText(akses.getkode());
            NmDokter.setText(Sequel.CariDokter(KdDokter.getText()));
            if(NmDokter.getText().equals("")){
                KdDokter.setText("");
                JOptionPane.showMessageDialog(null,"User login bukan Dokter...!!");
            }
        }
        if(TANGGALMUNDUR.equals("no")){
            if(!akses.getkode().equals("Admin Utama")){
                Tanggal.setEditable(false);
                Tanggal.setEnabled(false);
            }
        }
    }

    private void runBackground(Runnable task) {
        if(ceksukses) return;
        if(executor.isShutdown()||executor.isTerminated()) return;
        if(!isDisplayable()) return;

        ceksukses=true;
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            executor.submit(()->{
                try {
                    task.run();
                } finally {
                    ceksukses=false;
                    SwingUtilities.invokeLater(()->{
                        if(isDisplayable()){
                            setCursor(Cursor.getDefaultCursor());
                        }
                    });
                }
            });
        } catch (RejectedExecutionException ex) {
            ceksukses=false;
        }
    }

    @Override
    public void dispose() {
        executor.shutdownNow();
        super.dispose();
    }
}
