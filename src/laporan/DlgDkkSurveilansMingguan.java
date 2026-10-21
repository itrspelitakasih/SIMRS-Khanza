/*
 * DlgDkkSurveilansMingguan.java
 *
 * Laporan Mingguan Surveilans Rawat Jalan & Rawat Inap per kelompok umur
 * (Balita <5, Anak 5-9, Remaja 10-18, Dewasa 19-59, Lansia >=60)
 * Filter Rawat : Semua / Ralan / Ranap (berdasarkan reg_periksa.status_lanjut)
 */

package laporan;

import fungsi.WarnaTable;
import fungsi.batasInput;
import fungsi.koneksiDB;
import fungsi.validasi;
import fungsi.akses;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

/**
 *
 * @author perpustakaan
 */
public final class DlgDkkSurveilansMingguan extends javax.swing.JDialog {
    private final DefaultTableModel tabMode,tabModePasien;
    private Connection koneksi=koneksiDB.condb();
    private validasi Valid=new validasi();
    private PreparedStatement ps;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean ceksukses = false;
    private ResultSet rs;
    private int i=0,balita=0,anak=0,remaja=0,dewasa=0,lansia=0,laki=0,per=0,jml=0;
    private String umur="",kelompok="",rawat="";

    /** Creates new form DlgDkkSurveilansMingguan
     * @param parent
     * @param modal */
    public DlgDkkSurveilansMingguan(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        this.setLocation(8,1);
        setSize(885,674);

        tabMode=new DefaultTableModel(null,new Object[]{
                "No.","Kode ICD 10","Jenis Penyakit","Balita (<5 Th)","Anak (5-9 Th)","Remaja (10-18 Th)",
                "Dewasa (19-59 Th)","Lansia (>=60 Th)","Laki","Perp","Jumlah"
            }){
              @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        tbBangsal.setModel(tabMode);
        tbBangsal.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbBangsal.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 11; i++) {
            TableColumn column = tbBangsal.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(30);
            }else if(i==1){
                column.setPreferredWidth(80);
            }else if(i==2){
                column.setPreferredWidth(250);
            }else if((i>=3)&&(i<=7)){
                column.setPreferredWidth(105);
            }else{
                column.setPreferredWidth(50);
            }
        }
        tbBangsal.setDefaultRenderer(Object.class, new WarnaTable());

        tabModePasien=new DefaultTableModel(null,new Object[]{
                "No.","No.Rawat","No.RM","Nama Pasien","J.K.","Tgl.Lahir","Umur","Kelompok Umur","Alamat Lengkap",
                "Kecamatan","No.HP Pasien/Keluarga","Kode ICD 10","Diagnosa","Tgl.Masuk RS","Keadaan","NIK","Keluhan Pasien","Rawat"
            }){
              @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        tbPasien.setModel(tabModePasien);
        tbPasien.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbPasien.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 18; i++) {
            TableColumn column = tbPasien.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(30);
            }else if(i==1){
                column.setPreferredWidth(110);
            }else if(i==2){
                column.setPreferredWidth(70);
            }else if(i==3){
                column.setPreferredWidth(180);
            }else if(i==4){
                column.setPreferredWidth(30);
            }else if(i==5){
                column.setPreferredWidth(70);
            }else if(i==6){
                column.setPreferredWidth(50);
            }else if(i==7){
                column.setPreferredWidth(90);
            }else if(i==8){
                column.setPreferredWidth(250);
            }else if(i==9){
                column.setPreferredWidth(120);
            }else if(i==10){
                column.setPreferredWidth(110);
            }else if(i==11){
                column.setPreferredWidth(70);
            }else if(i==12){
                column.setPreferredWidth(200);
            }else if(i==13){
                column.setPreferredWidth(75);
            }else if(i==14){
                column.setPreferredWidth(70);
            }else if(i==15){
                column.setPreferredWidth(120);
            }else if(i==16){
                column.setPreferredWidth(300);
            }else if(i==17){
                column.setPreferredWidth(50);
            }
        }
        tbPasien.setDefaultRenderer(Object.class, new WarnaTable());

        TCari.setDocument(new batasInput((byte)100).getKata(TCari));
        MingguKe.setDocument(new batasInput((byte)5).getKata(MingguKe));
        Tgl1.addItemListener(evt -> hitungMingguKe());
        hitungMingguKe();
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
        TabRawat = new widget.TabPane();
        Scroll = new widget.ScrollPane();
        tbBangsal = new widget.Table();
        Scroll2 = new widget.ScrollPane();
        tbPasien = new widget.Table();
        panelGlass5 = new widget.panelisi();
        label11 = new widget.Label();
        Tgl1 = new widget.Tanggal();
        label18 = new widget.Label();
        Tgl2 = new widget.Tanggal();
        label20 = new widget.Label();
        CmbRawat = new widget.ComboBox();
        label19 = new widget.Label();
        MingguKe = new widget.TextBox();
        label10 = new widget.Label();
        TCari = new widget.TextBox();
        BtnCari1 = new widget.Button();
        BtnPrint = new widget.Button();
        BtnKeluar = new widget.Button();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Laporan Mingguan Surveilans ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 0, 12), new java.awt.Color(50, 50, 50))); // NOI18N
        internalFrame1.setName("internalFrame1"); // NOI18N
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

        TabRawat.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        TabRawat.setName("TabRawat"); // NOI18N

        Scroll.setName("Scroll"); // NOI18N
        Scroll.setOpaque(true);

        tbBangsal.setName("tbBangsal"); // NOI18N
        Scroll.setViewportView(tbBangsal);

        TabRawat.addTab("Rekap Penyakit Per Kelompok Umur", Scroll);

        Scroll2.setName("Scroll2"); // NOI18N
        Scroll2.setOpaque(true);

        tbPasien.setName("tbPasien"); // NOI18N
        Scroll2.setViewportView(tbPasien);

        TabRawat.addTab("Daftar Pasien", Scroll2);

        internalFrame1.add(TabRawat, java.awt.BorderLayout.CENTER);

        panelGlass5.setName("panelGlass5"); // NOI18N
        panelGlass5.setPreferredSize(new java.awt.Dimension(55, 55));
        panelGlass5.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        label11.setText("Tanggal :");
        label11.setName("label11"); // NOI18N
        label11.setPreferredSize(new java.awt.Dimension(55, 23));
        panelGlass5.add(label11);

        Tgl1.setDisplayFormat("dd-MM-yyyy");
        Tgl1.setName("Tgl1"); // NOI18N
        Tgl1.setPreferredSize(new java.awt.Dimension(100, 23));
        panelGlass5.add(Tgl1);

        label18.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        label18.setText("s.d.");
        label18.setName("label18"); // NOI18N
        label18.setPreferredSize(new java.awt.Dimension(30, 23));
        panelGlass5.add(label18);

        Tgl2.setDisplayFormat("dd-MM-yyyy");
        Tgl2.setName("Tgl2"); // NOI18N
        Tgl2.setPreferredSize(new java.awt.Dimension(100, 23));
        panelGlass5.add(Tgl2);

        label20.setText("Rawat :");
        label20.setName("label20"); // NOI18N
        label20.setPreferredSize(new java.awt.Dimension(45, 23));
        panelGlass5.add(label20);

        CmbRawat.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Semua", "Ralan", "Ranap" }));
        CmbRawat.setName("CmbRawat"); // NOI18N
        CmbRawat.setPreferredSize(new java.awt.Dimension(80, 23));
        panelGlass5.add(CmbRawat);

        label19.setText("Minggu Ke :");
        label19.setName("label19"); // NOI18N
        label19.setPreferredSize(new java.awt.Dimension(70, 23));
        panelGlass5.add(label19);

        MingguKe.setName("MingguKe"); // NOI18N
        MingguKe.setPreferredSize(new java.awt.Dimension(40, 23));
        panelGlass5.add(MingguKe);

        label10.setText("Key Word :");
        label10.setName("label10"); // NOI18N
        label10.setPreferredSize(new java.awt.Dimension(65, 23));
        panelGlass5.add(label10);

        TCari.setToolTipText("Cari kode ICD 10 atau nama penyakit");
        TCari.setName("TCari"); // NOI18N
        TCari.setPreferredSize(new java.awt.Dimension(150, 23));
        TCari.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TCariKeyPressed(evt);
            }
        });
        panelGlass5.add(TCari);

        BtnCari1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png"))); // NOI18N
        BtnCari1.setMnemonic('2');
        BtnCari1.setToolTipText("Alt+2");
        BtnCari1.setName("BtnCari1"); // NOI18N
        BtnCari1.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnCari1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnCari1ActionPerformed(evt);
            }
        });
        BtnCari1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnCari1KeyPressed(evt);
            }
        });
        panelGlass5.add(BtnCari1);

        BtnPrint.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/b_print.png"))); // NOI18N
        BtnPrint.setMnemonic('T');
        BtnPrint.setText("Excel");
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
        panelGlass5.add(BtnPrint);

        BtnKeluar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/exit.png"))); // NOI18N
        BtnKeluar.setMnemonic('K');
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
        panelGlass5.add(BtnKeluar);

        internalFrame1.add(panelGlass5, java.awt.BorderLayout.PAGE_END);

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void BtnPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnPrintActionPerformed
        if(ceksukses){
            JOptionPane.showMessageDialog(null,"Masih proses menampilkan data, harap tunggu terlebih dahulu...!");
        }else if((tabMode.getRowCount()==0)&&(tabModePasien.getRowCount()==0)){
            JOptionPane.showMessageDialog(null,"Maaf, data sudah habis. Tidak ada data yang bisa anda print...!!!!");
        }else{
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            try {
                File f=new File("LaporanMingguanSurveilans.xls");
                try (BufferedWriter bw=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f),"UTF-8"))) {
                    bw.write(buatExcel());
                }
                Desktop.getDesktop().open(f);
            } catch (Exception e) {
                System.out.println("Notifikasi : "+e);
                JOptionPane.showMessageDialog(null,"Gagal membuat file Excel : "+e.getMessage());
            }
            this.setCursor(Cursor.getDefaultCursor());
        }
}//GEN-LAST:event_BtnPrintActionPerformed

    private void BtnPrintKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnPrintKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnPrintActionPerformed(null);
        }else{
            Valid.pindah(evt, BtnCari1, BtnKeluar);
        }
}//GEN-LAST:event_BtnPrintKeyPressed

    private void BtnKeluarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnKeluarActionPerformed
        dispose();
}//GEN-LAST:event_BtnKeluarActionPerformed

    private void BtnKeluarKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnKeluarKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            dispose();
        }else{Valid.pindah(evt,BtnPrint,Tgl1);}
}//GEN-LAST:event_BtnKeluarKeyPressed

private void BtnCari1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCari1ActionPerformed
        runBackground(() ->tampil());
}//GEN-LAST:event_BtnCari1ActionPerformed

private void BtnCari1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnCari1KeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            runBackground(() ->tampil());
        }else{
            Valid.pindah(evt, TCari, BtnPrint);
        }
}//GEN-LAST:event_BtnCari1KeyPressed

    private void TCariKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TCariKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            runBackground(() ->tampil());
        }else{
            Valid.pindah(evt, Tgl2, BtnCari1);
        }
    }//GEN-LAST:event_TCariKeyPressed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            DlgDkkSurveilansMingguan dialog = new DlgDkkSurveilansMingguan(new javax.swing.JFrame(), true);
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
    private widget.Button BtnCari1;
    private widget.Button BtnKeluar;
    private widget.Button BtnPrint;
    private widget.ComboBox CmbRawat;
    private widget.TextBox MingguKe;
    private widget.ScrollPane Scroll;
    private widget.ScrollPane Scroll2;
    private widget.TextBox TCari;
    private widget.TabPane TabRawat;
    private widget.Tanggal Tgl1;
    private widget.Tanggal Tgl2;
    private widget.InternalFrame internalFrame1;
    private widget.Label label10;
    private widget.Label label11;
    private widget.Label label18;
    private widget.Label label19;
    private widget.Label label20;
    private widget.panelisi panelGlass5;
    private widget.Table tbBangsal;
    private widget.Table tbPasien;
    // End of variables declaration//GEN-END:variables

    private String kelompokUmur(String sttsumur,int umurdaftar){
        if(!"Th".equals(sttsumur)||(umurdaftar<5)){
            return "Balita";
        }else if(umurdaftar<=9){
            return "Anak";
        }else if(umurdaftar<=18){
            return "Remaja";
        }else if(umurdaftar<=59){
            return "Dewasa";
        }else{
            return "Lansia";
        }
    }

    private void tampil(){
        try{
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            Valid.tabelKosong(tabMode);
            Valid.tabelKosong(tabModePasien);
            rawat=CmbRawat.getSelectedItem().toString();
            ps=koneksi.prepareStatement(
                    "select diagnosa_pasien.kd_penyakit,SUBSTRING(penyakit.nm_penyakit,1,80) as nm_penyakit,"+
                    "sum(if(reg_periksa.sttsumur<>'Th' or reg_periksa.umurdaftar<5,1,0)) as balita,"+
                    "sum(if(reg_periksa.sttsumur='Th' and reg_periksa.umurdaftar between 5 and 9,1,0)) as anak,"+
                    "sum(if(reg_periksa.sttsumur='Th' and reg_periksa.umurdaftar between 10 and 18,1,0)) as remaja,"+
                    "sum(if(reg_periksa.sttsumur='Th' and reg_periksa.umurdaftar between 19 and 59,1,0)) as dewasa,"+
                    "sum(if(reg_periksa.sttsumur='Th' and reg_periksa.umurdaftar>=60,1,0)) as lansia,"+
                    "sum(if(pasien.jk='L',1,0)) as laki,sum(if(pasien.jk='P',1,0)) as per "+
                    "from diagnosa_pasien inner join penyakit on diagnosa_pasien.kd_penyakit=penyakit.kd_penyakit "+
                    "inner join reg_periksa on reg_periksa.no_rawat=diagnosa_pasien.no_rawat "+
                    "inner join pasien on pasien.no_rkm_medis=reg_periksa.no_rkm_medis "+
                    "where diagnosa_pasien.status=reg_periksa.status_lanjut and reg_periksa.status_lanjut like ? and diagnosa_pasien.prioritas='1' and reg_periksa.tgl_registrasi between ? and ? "+
                    "and diagnosa_pasien.kd_penyakit<>'-' and (diagnosa_pasien.kd_penyakit like ? or penyakit.nm_penyakit like ?) "+
                    "group by diagnosa_pasien.kd_penyakit order by count(diagnosa_pasien.no_rawat) desc,diagnosa_pasien.kd_penyakit");
            try {
                ps.setString(1,"Semua".equals(rawat)?"%":rawat);
                ps.setString(2,Valid.SetTgl(Tgl1.getSelectedItem()+""));
                ps.setString(3,Valid.SetTgl(Tgl2.getSelectedItem()+""));
                ps.setString(4,"%"+TCari.getText().trim()+"%");
                ps.setString(5,"%"+TCari.getText().trim()+"%");
                rs=ps.executeQuery();
                i=1;
                while(rs.next()){
                    balita=rs.getInt("balita");anak=rs.getInt("anak");remaja=rs.getInt("remaja");dewasa=rs.getInt("dewasa");lansia=rs.getInt("lansia");
                    laki=rs.getInt("laki");per=rs.getInt("per");jml=laki+per;
                    tabMode.addRow(new Object[]{
                        i,rs.getString("kd_penyakit"),rs.getString("nm_penyakit"),balita,anak,remaja,dewasa,lansia,laki,per,jml
                    });
                    i++;
                }
            } finally {
                if(rs!=null){
                    rs.close();
                }
                if(ps!=null){
                    ps.close();
                }
            }

            ps=koneksi.prepareStatement(
                    "select reg_periksa.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,pasien.jk,pasien.tgl_lahir,reg_periksa.umurdaftar,reg_periksa.sttsumur,"+
                    "concat(pasien.alamat,', ',kelurahan.nm_kel) as alamat,kecamatan.nm_kec,pasien.no_tlp,diagnosa_pasien.kd_penyakit,penyakit.nm_penyakit,"+
                    "reg_periksa.tgl_registrasi,pasien.no_ktp,reg_periksa.status_lanjut,"+
                    "if(pasien_mati.no_rkm_medis is not null or exists(select 1 from kamar_inap where kamar_inap.no_rawat=reg_periksa.no_rawat and kamar_inap.stts_pulang in ('Meninggal','+')),'MENINGGAL',"+
                    "if(reg_periksa.status_lanjut='Ranap' and exists(select 1 from kamar_inap where kamar_inap.no_rawat=reg_periksa.no_rawat and kamar_inap.stts_pulang='-'),'SAKIT','HIDUP')) as keadaan,"+
                    "ifnull(if(reg_periksa.status_lanjut='Ranap',"+
                    "(select pemeriksaan_ranap.keluhan from pemeriksaan_ranap where pemeriksaan_ranap.no_rawat=reg_periksa.no_rawat order by pemeriksaan_ranap.tgl_perawatan,pemeriksaan_ranap.jam_rawat limit 1),"+
                    "(select pemeriksaan_ralan.keluhan from pemeriksaan_ralan where pemeriksaan_ralan.no_rawat=reg_periksa.no_rawat order by pemeriksaan_ralan.tgl_perawatan,pemeriksaan_ralan.jam_rawat limit 1)),'') as keluhan "+
                    "from diagnosa_pasien inner join penyakit on diagnosa_pasien.kd_penyakit=penyakit.kd_penyakit "+
                    "inner join reg_periksa on reg_periksa.no_rawat=diagnosa_pasien.no_rawat "+
                    "inner join pasien on pasien.no_rkm_medis=reg_periksa.no_rkm_medis "+
                    "inner join kelurahan on pasien.kd_kel=kelurahan.kd_kel inner join kecamatan on pasien.kd_kec=kecamatan.kd_kec "+
                    "left join pasien_mati on pasien_mati.no_rkm_medis=pasien.no_rkm_medis "+
                    "where diagnosa_pasien.status=reg_periksa.status_lanjut and reg_periksa.status_lanjut like ? and diagnosa_pasien.prioritas='1' and reg_periksa.tgl_registrasi between ? and ? "+
                    "and diagnosa_pasien.kd_penyakit<>'-' and (diagnosa_pasien.kd_penyakit like ? or penyakit.nm_penyakit like ?) "+
                    "order by reg_periksa.tgl_registrasi,reg_periksa.jam_reg");
            try {
                ps.setString(1,"Semua".equals(rawat)?"%":rawat);
                ps.setString(2,Valid.SetTgl(Tgl1.getSelectedItem()+""));
                ps.setString(3,Valid.SetTgl(Tgl2.getSelectedItem()+""));
                ps.setString(4,"%"+TCari.getText().trim()+"%");
                ps.setString(5,"%"+TCari.getText().trim()+"%");
                rs=ps.executeQuery();
                i=1;
                while(rs.next()){
                    umur=rs.getString("umurdaftar")+" "+rs.getString("sttsumur");
                    kelompok=kelompokUmur(rs.getString("sttsumur"),rs.getInt("umurdaftar"));
                    tabModePasien.addRow(new Object[]{
                        i,rs.getString("no_rawat"),rs.getString("no_rkm_medis"),rs.getString("nm_pasien"),rs.getString("jk"),
                        rs.getString("tgl_lahir"),umur,kelompok,rs.getString("alamat"),rs.getString("nm_kec"),rs.getString("no_tlp"),
                        rs.getString("kd_penyakit"),rs.getString("nm_penyakit"),rs.getString("tgl_registrasi"),rs.getString("keadaan"),
                        rs.getString("no_ktp"),rs.getString("keluhan").replaceAll("[\\r\\n]+"," "),rs.getString("status_lanjut")
                    });
                    i++;
                }
            } finally {
                if(rs!=null){
                    rs.close();
                }
                if(ps!=null){
                    ps.close();
                }
            }
            this.setCursor(Cursor.getDefaultCursor());
        }catch(Exception e){
            System.out.println("Notifikasi : "+e);
        }
    }

    // Minggu ke-1 dimulai dari hari Senin pertama di tahun berjalan
    private void hitungMingguKe(){
        try {
            LocalDate tgl=Tgl1.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate senin=tgl.withDayOfYear(1).with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));
            MingguKe.setText(tgl.isBefore(senin)?"1":String.valueOf((ChronoUnit.DAYS.between(senin,tgl)/7)+1));
        } catch (Exception e) {
            MingguKe.setText("");
        }
    }

    private String html(Object nilai){
        return nilai==null?"":nilai.toString().replaceAll("&","&amp;").replaceAll("<","&lt;").replaceAll(">","&gt;");
    }

    private String buatExcel(){
        StringBuilder sb=new StringBuilder();
        sb.append("<html xmlns:o='urn:schemas-microsoft-com:office:office' xmlns:x='urn:schemas-microsoft-com:office:excel'>")
          .append("<head><meta http-equiv='Content-Type' content='text/html; charset=UTF-8'>")
          .append("<style>td,th{font-family:Arial;font-size:10pt;vertical-align:top;} .b td,.b th{border:0.5pt solid #000;} th{background:#D9D9D9;} .txt{mso-number-format:'\\@';}</style>")
          .append("</head><body>");

        sb.append("<table>")
          .append("<tr><td colspan='15'><b>LAPORAN MINGGUAN SURVEILANS PENYAKIT RUMAH SAKIT - ").append("Semua".equals(rawat)?"RAWAT JALAN &amp; RAWAT INAP":("Ranap".equals(rawat)?"RAWAT INAP":"RAWAT JALAN")).append("</b></td></tr>")
          .append("<tr><td colspan='2'>RUMAH SAKIT</td><td colspan='13'>: ").append(html(akses.getnamars())).append("</td></tr>")
          .append("<tr><td colspan='2'>MINGGU KE</td><td colspan='11' class='txt'>: ").append(html(MingguKe.getText())).append("</td></tr>")
          .append("<tr><td colspan='2'>PERIODE</td><td colspan='13'>: ").append(Tgl1.getSelectedItem()).append(" s.d. ").append(Tgl2.getSelectedItem()).append("</td></tr>")
          .append("</table><br>");

        sb.append("<table class='b' cellspacing='0'><tr>")
          .append("<th>No.</th><th>Nama Pasien</th><th>Jenis Kelamin (L/P)</th><th>Tanggal Lahir</th><th>Umur</th><th>Kelompok Umur</th>")
          .append("<th>Alamat Lengkap</th><th>Kecamatan</th><th>No. HP Pasien/Keluarga Pasien</th><th>Diagnosa</th>")
          .append("<th>Tanggal Sakit/Tanggal Masuk RS</th><th>Rawat</th><th>Keadaan Saat Ini (Hidup/Sakit/Meninggal)</th><th>NIK</th><th>Keluhan Pasien</th></tr>");
        for(int r=0;r<tabModePasien.getRowCount();r++){
            sb.append("<tr><td>").append(html(tabModePasien.getValueAt(r,0))).append("</td>")
              .append("<td>").append(html(tabModePasien.getValueAt(r,3))).append("</td>")
              .append("<td>").append(html(tabModePasien.getValueAt(r,4))).append("</td>")
              .append("<td>").append(html(tabModePasien.getValueAt(r,5))).append("</td>")
              .append("<td>").append(html(tabModePasien.getValueAt(r,6))).append("</td>")
              .append("<td>").append(html(tabModePasien.getValueAt(r,7))).append("</td>")
              .append("<td>").append(html(tabModePasien.getValueAt(r,8))).append("</td>")
              .append("<td>").append(html(tabModePasien.getValueAt(r,9))).append("</td>")
              .append("<td class='txt'>").append(html(tabModePasien.getValueAt(r,10))).append("</td>")
              .append("<td>").append(html(tabModePasien.getValueAt(r,12))).append("</td>")
              .append("<td>").append(html(tabModePasien.getValueAt(r,13))).append("</td>")
              .append("<td>").append(html(tabModePasien.getValueAt(r,17))).append("</td>")
              .append("<td>").append(html(tabModePasien.getValueAt(r,14))).append("</td>")
              .append("<td class='txt'>").append(html(tabModePasien.getValueAt(r,15))).append("</td>")
              .append("<td>").append(html(tabModePasien.getValueAt(r,16))).append("</td></tr>");
        }
        sb.append("</table><br><br>");

        sb.append("<table>")
          .append("<tr><td colspan='2'>Nama Fasilitas Kesehatan</td><td colspan='9'>: ").append(html(akses.getnamars())).append("</td></tr>")
          .append("<tr><td colspan='2'>Kabupaten</td><td colspan='9'>: ").append(html(akses.getkabupatenrs())).append("</td></tr>")
          .append("<tr><td colspan='11'>Minggu Epidemiologi ke ").append(html(MingguKe.getText())).append(" : Tanggal ")
          .append(Tgl1.getSelectedItem()).append(" s.d. ").append(Tgl2.getSelectedItem()).append("</td></tr>")
          .append("</table><br>");

        balita=0;anak=0;remaja=0;dewasa=0;lansia=0;laki=0;per=0;jml=0;
        sb.append("<table class='b' cellspacing='0'><tr>")
          .append("<th>No.</th><th>Kode ICD 10</th><th>Jenis Penyakit</th><th>Balita (&lt;5 Th)</th><th>Anak (5-9 Th)</th>")
          .append("<th>Remaja (10-18 Th)</th><th>Dewasa (19-59 Th)</th><th>Lansia (&gt;=60 Th)</th><th>Laki-laki</th><th>Perempuan</th><th>Jumlah</th></tr>");
        for(int r=0;r<tabMode.getRowCount();r++){
            sb.append("<tr>");
            for(int c=0;c<11;c++){
                sb.append("<td>").append(html(tabMode.getValueAt(r,c))).append("</td>");
            }
            sb.append("</tr>");
            balita+=Integer.parseInt(tabMode.getValueAt(r,3).toString());
            anak+=Integer.parseInt(tabMode.getValueAt(r,4).toString());
            remaja+=Integer.parseInt(tabMode.getValueAt(r,5).toString());
            dewasa+=Integer.parseInt(tabMode.getValueAt(r,6).toString());
            lansia+=Integer.parseInt(tabMode.getValueAt(r,7).toString());
            laki+=Integer.parseInt(tabMode.getValueAt(r,8).toString());
            per+=Integer.parseInt(tabMode.getValueAt(r,9).toString());
            jml+=Integer.parseInt(tabMode.getValueAt(r,10).toString());
        }
        sb.append("<tr><td colspan='3'><b>TOTAL</b></td><td><b>").append(balita).append("</b></td><td><b>").append(anak)
          .append("</b></td><td><b>").append(remaja).append("</b></td><td><b>").append(dewasa).append("</b></td><td><b>").append(lansia)
          .append("</b></td><td><b>").append(laki).append("</b></td><td><b>").append(per).append("</b></td><td><b>").append(jml).append("</b></td></tr>")
          .append("</table></body></html>");
        return sb.toString();
    }

    private void runBackground(Runnable task) {
        if (ceksukses) return;
        if (executor.isShutdown() || executor.isTerminated()) return;
        if (!isDisplayable()) return;

        ceksukses = true;
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        try {
            executor.submit(() -> {
                try {
                    task.run();
                } finally {
                    ceksukses = false;
                    SwingUtilities.invokeLater(() -> {
                        if (isDisplayable()) {
                            setCursor(Cursor.getDefaultCursor());
                        }
                    });
                }
            });
        } catch (RejectedExecutionException ex) {
            ceksukses = false;
        }
    }

    @Override
    public void dispose() {
        executor.shutdownNow();
        super.dispose();
    }
}
