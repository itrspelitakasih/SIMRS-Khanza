package setting;

import fungsi.WarnaTable;
import fungsi.batasInput;
import fungsi.koneksiDB;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ButtonGroup;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import komunikasi.GoWAService;

/**
 * Reset password beberapa user sekaligus (dibuka dari Setup User / DlgUser).
 * Centang user, pilih password acak per user atau satu password yang sama, lalu Reset Password.
 * Bila Kirim WA dicentang, username & password baru dikirim ke WA masing-masing user
 * (template AKUN_USER_ADMIN, nomor dari user_kontak / no_telp dokter / petugas).
 * UI dibuat di kode (tanpa .form) dengan komponen widget.
 */
public class DlgResetPasswordUser extends javax.swing.JDialog {
    private static final int PILIH=0,ID=1,NAMA=2,JABATAN=3,NOWA=4,PASSWORD=5,STATUS=6;
    private final DefaultTableModel tabMode;
    private final Connection koneksi=koneksiDB.condb();
    private boolean proses=false;

    private final widget.InternalFrame internalFrame1=new widget.InternalFrame();
    private final widget.ScrollPane Scroll=new widget.ScrollPane();
    private final widget.Table tbUser=new widget.Table();
    private final widget.panelisi panelAtas=new widget.panelisi();
    private final widget.Label LCari=new widget.Label();
    private final widget.TextBox TCari=new widget.TextBox();
    private final widget.Button BtnCari=new widget.Button();
    private final widget.CekBox ChkPilihSemua=new widget.CekBox();
    private final widget.Label LDipilih=new widget.Label();
    private final widget.panelisi panelBawah=new widget.panelisi();
    private final widget.RadioButton RbAcak=new widget.RadioButton();
    private final widget.RadioButton RbSama=new widget.RadioButton();
    private final widget.TextBox TPassSama=new widget.TextBox();
    private final widget.CekBox ChkKirimWA=new widget.CekBox();
    private final widget.Button BtnReset=new widget.Button();
    private final widget.Button BtnKeluar=new widget.Button();

    public DlgResetPasswordUser(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        tabMode=new DefaultTableModel(null,new Object[]{"P","ID User","Nama User","Jabatan","No.WA","Password Baru","Status"}){
            @Override
            public boolean isCellEditable(int rowIndex, int colIndex){
                return colIndex==PILIH&&!proses;
            }
            @Override
            public Class getColumnClass(int columnIndex) {
                return columnIndex==PILIH?Boolean.class:Object.class;
            }
        };
        initComponents();
    }

    private void initComponents() {
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new Color(240, 245, 235)), "::[ Reset Password User ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 0, 12), new Color(50, 50, 50)));
        internalFrame1.setLayout(new BorderLayout(1, 1));

        tbUser.setModel(tabMode);
        tbUser.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tbUser.setDefaultRenderer(Object.class, new WarnaTable());
        int[] lebar={25,130,200,150,110,110,150};
        for(int i=0;i<lebar.length;i++){
            tbUser.getColumnModel().getColumn(i).setPreferredWidth(lebar[i]);
        }
        tabMode.addTableModelListener(e -> hitungDipilih());
        tbUser.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent evt) {
                // spasi mencentang / melepas user di baris terpilih
                if(evt.getKeyCode()==KeyEvent.VK_SPACE&&tbUser.getSelectedRow()!=-1&&!proses){
                    int[] baris=tbUser.getSelectedRows();
                    boolean nilai=!Boolean.TRUE.equals(tbUser.getValueAt(baris[0],PILIH));
                    for(int b:baris){
                        tbUser.setValueAt(nilai,b,PILIH);
                    }
                    evt.consume();
                }
            }
        });
        Scroll.setOpaque(true);
        Scroll.setViewportView(tbUser);
        internalFrame1.add(Scroll, BorderLayout.CENTER);

        panelAtas.setPreferredSize(new Dimension(44, 44));
        panelAtas.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 9));

        LCari.setText("Key Word :");
        LCari.setPreferredSize(new Dimension(65, 23));
        panelAtas.add(LCari);

        TCari.setDocument(new batasInput((byte)100).getKata(TCari));
        TCari.setPreferredSize(new Dimension(300, 23));
        TCari.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent evt) {
                if(evt.getKeyCode()==KeyEvent.VK_ENTER){
                    tampil();
                }
            }
        });
        panelAtas.add(TCari);

        BtnCari.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png")));
        BtnCari.setToolTipText("Cari");
        BtnCari.setPreferredSize(new Dimension(28, 23));
        BtnCari.addActionListener(evt -> tampil());
        panelAtas.add(BtnCari);

        ChkPilihSemua.setText("Pilih Semua");
        ChkPilihSemua.setOpaque(false);
        ChkPilihSemua.setPreferredSize(new Dimension(100, 23));
        ChkPilihSemua.addActionListener(evt -> {
            for(int i=0;i<tabMode.getRowCount();i++){
                tabMode.setValueAt(ChkPilihSemua.isSelected(),i,PILIH);
            }
        });
        panelAtas.add(ChkPilihSemua);

        LDipilih.setText("Dipilih : 0");
        LDipilih.setPreferredSize(new Dimension(120, 23));
        panelAtas.add(LDipilih);

        internalFrame1.add(panelAtas, BorderLayout.PAGE_START);

        panelBawah.setPreferredSize(new Dimension(55, 55));
        panelBawah.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 9));

        ButtonGroup grupPassword=new ButtonGroup();
        grupPassword.add(RbAcak);
        grupPassword.add(RbSama);

        RbAcak.setText("Password acak per user");
        RbAcak.setSelected(true);
        RbAcak.setOpaque(false);
        RbAcak.setPreferredSize(new Dimension(160, 23));
        RbAcak.addActionListener(evt -> TPassSama.setEnabled(false));
        panelBawah.add(RbAcak);

        RbSama.setText("Password sama :");
        RbSama.setOpaque(false);
        RbSama.setPreferredSize(new Dimension(115, 23));
        RbSama.addActionListener(evt -> {
            TPassSama.setEnabled(true);
            TPassSama.requestFocus();
        });
        panelBawah.add(RbSama);

        TPassSama.setDocument(new batasInput((byte)50).getKata(TPassSama));
        TPassSama.setEnabled(false);
        TPassSama.setPreferredSize(new Dimension(150, 23));
        panelBawah.add(TPassSama);

        ChkKirimWA.setText("Kirim WA");
        ChkKirimWA.setSelected(true);
        ChkKirimWA.setOpaque(false);
        ChkKirimWA.setToolTipText("Kirim username & password baru ke WA masing-masing user");
        ChkKirimWA.setPreferredSize(new Dimension(85, 23));
        panelBawah.add(ChkKirimWA);

        BtnReset.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/refresh.png")));
        BtnReset.setMnemonic('R');
        BtnReset.setText("Reset Password");
        BtnReset.setToolTipText("Alt+R");
        BtnReset.setPreferredSize(new Dimension(140, 30));
        BtnReset.addActionListener(evt -> resetPassword());
        panelBawah.add(BtnReset);

        BtnKeluar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/exit.png")));
        BtnKeluar.setMnemonic('K');
        BtnKeluar.setText("Keluar");
        BtnKeluar.setToolTipText("Alt+K");
        BtnKeluar.setPreferredSize(new Dimension(100, 30));
        BtnKeluar.addActionListener(evt -> {
            if(proses){
                JOptionPane.showMessageDialog(this,"Tunggu, reset password masih berjalan...");
            }else{
                dispose();
            }
        });
        panelBawah.add(BtnKeluar);

        internalFrame1.add(panelBawah, BorderLayout.PAGE_END);

        getContentPane().add(internalFrame1, BorderLayout.CENTER);
        pack();
    }

    /** Muat semua user beserta nama, jabatan & nomor WA dalam satu query. */
    public void tampil(){
        tabMode.setRowCount(0);
        ChkPilihSemua.setSelected(false);
        UserAkun.siapkanTabel(koneksi);
        String cari="%"+TCari.getText().trim()+"%";
        try (PreparedStatement ps=koneksi.prepareStatement(
                "select t.id,t.nama,t.jabatan,t.nowa from (select u.id,coalesce(dokter.nm_dokter,petugas.nama,user_kontak.nama,'') as nama,"+
                "case when dokter.kd_dokter is not null then ifnull(spesialis.nm_sps,'') when petugas.nip is not null then ifnull(jabatan.nm_jbtn,'') else 'User Manual' end as jabatan,"+
                "coalesce(nullif(user_kontak.no_wa,''),dokter.no_telp,petugas.no_telp,'') as nowa "+
                "from (select convert(AES_DECRYPT(user.id_user,'nur') using latin1) as id,user.id_user from user) u "+
                "left join dokter on dokter.kd_dokter=u.id left join spesialis on spesialis.kd_sps=dokter.kd_sps "+
                "left join petugas on petugas.nip=u.id left join jabatan on jabatan.kd_jbtn=petugas.kd_jbtn "+
                "left join user_kontak on user_kontak.id_user=u.id_user) t "+
                "where t.id like ? or t.nama like ? or t.jabatan like ? order by t.id")) {
            ps.setString(1,cari);
            ps.setString(2,cari);
            ps.setString(3,cari);
            try (ResultSet rs=ps.executeQuery()) {
                while(rs.next()){
                    tabMode.addRow(new Object[]{false,rs.getString(1),rs.getString(2),rs.getString(3),rs.getString(4),"",""});
                }
            }
        } catch (Exception e) {
            System.out.println("Notif Reset Password : "+e);
        }
        hitungDipilih();
    }

    private void hitungDipilih(){
        int jumlah=0;
        for(int i=0;i<tabMode.getRowCount();i++){
            if(Boolean.TRUE.equals(tabMode.getValueAt(i,PILIH))){
                jumlah++;
            }
        }
        LDipilih.setText("Dipilih : "+jumlah+" / "+tabMode.getRowCount());
    }

    private void resetPassword(){
        List<Integer> dipilih=new ArrayList<>();
        for(int i=0;i<tabMode.getRowCount();i++){
            if(Boolean.TRUE.equals(tabMode.getValueAt(i,PILIH))){
                dipilih.add(i);
            }
        }
        if(dipilih.isEmpty()){
            JOptionPane.showMessageDialog(this,"Silahkan centang dulu user yang akan direset password-nya...!!!");
            tbUser.requestFocus();
            return;
        }
        if(RbSama.isSelected()&&TPassSama.getText().trim().equals("")){
            JOptionPane.showMessageDialog(this,"Password sama masih kosong...!!!");
            TPassSama.requestFocus();
            return;
        }
        if(JOptionPane.showConfirmDialog(this,"Reset password "+dipilih.size()+" user"+(ChkKirimWA.isSelected()?" dan kirim ke WA masing-masing":"")+"?",
                "Konfirmasi",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION){
            return;
        }

        final boolean acak=RbAcak.isSelected();
        final boolean kirimWA=ChkKirimWA.isSelected();
        final String passSama=TPassSama.getText().trim();
        aturProses(true);
        new SwingWorker<int[],Object[]>(){
            @Override
            protected int[] doInBackground() {
                int sukses=0,gagal=0,gagalWA=0;
                for(int baris:dipilih){
                    String id=tabMode.getValueAt(baris,ID).toString();
                    String nama=tabMode.getValueAt(baris,NAMA).toString();
                    String noWA=tabMode.getValueAt(baris,NOWA).toString().trim();
                    String password=acak?UserAkun.generatePassword(8):passSama;
                    String status;
                    if(!simpanPassword(id,password)){
                        gagal++;
                        publish(new Object[]{baris,"","Gagal disimpan"});
                        continue;
                    }
                    sukses++;
                    if(!kirimWA){
                        status="Tersimpan";
                    }else if(noWA.isEmpty()){
                        status="Tersimpan, No.WA kosong";
                    }else if(GoWAService.kirimText(noWA,UserAkun.pesanAkunAdmin(id,nama.isEmpty()?id:nama,password))){
                        status="Tersimpan, WA terkirim";
                    }else{
                        gagalWA++;
                        status="Tersimpan, WA gagal";
                    }
                    publish(new Object[]{baris,password,status});
                }
                return new int[]{sukses,gagal,gagalWA};
            }

            @Override
            protected void process(List<Object[]> data) {
                for(Object[] d:data){
                    int baris=(Integer)d[0];
                    tabMode.setValueAt(d[1],baris,PASSWORD);
                    tabMode.setValueAt(d[2],baris,STATUS);
                }
            }

            @Override
            protected void done() {
                aturProses(false);
                try {
                    int[] hasil=get();
                    JOptionPane.showMessageDialog(DlgResetPasswordUser.this,
                        "Password berhasil direset : "+hasil[0]+" user"+
                        (hasil[1]>0?"\nGagal disimpan : "+hasil[1]+" user":"")+
                        (hasil[2]>0?"\nWA gagal terkirim : "+hasil[2]+" user, periksa setting GoWA / nomor WA":"")+
                        "\n\nPassword baru tampil di kolom Password Baru.");
                } catch (Exception e) {
                    System.out.println("Notif Reset Password : "+e);
                }
            }
        }.execute();
    }

    private boolean simpanPassword(String idUser,String password){
        try (PreparedStatement ps=koneksi.prepareStatement(
                "update user set password=AES_ENCRYPT(?,'windi') where id_user=AES_ENCRYPT(?,'nur')")) {
            ps.setString(1,password);
            ps.setString(2,idUser);
            return ps.executeUpdate()>0;
        } catch (Exception e) {
            System.out.println("Notif Reset Password : "+e);
            return false;
        }
    }

    private void aturProses(boolean jalan){
        proses=jalan;
        BtnReset.setEnabled(!jalan);
        BtnCari.setEnabled(!jalan);
        ChkPilihSemua.setEnabled(!jalan);
        BtnReset.setText(jalan?"Memproses...":"Reset Password");
        setCursor(java.awt.Cursor.getPredefinedCursor(jalan?java.awt.Cursor.WAIT_CURSOR:java.awt.Cursor.DEFAULT_CURSOR));
    }
}
