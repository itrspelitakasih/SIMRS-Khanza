package keuangan;

import fungsi.WarnaTable;
import fungsi.koneksiDB;
import fungsi.sekuel;
import java.awt.Dimension;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Salin item template hasil lab ke kode lain dengan nama pemeriksaan & kategori sama yang belum punya
 * template (mis. kode tarif kelas). Sumber: kode terpilih bila punya template, selain itu kode bernama
 * sama dengan item template terbanyak. Kode yang sudah punya template tidak ditampilkan & tidak disentuh.
 */
public class DlgSalinTemplateLab extends javax.swing.JDialog {
    private final Connection koneksi=koneksiDB.condb();
    private final sekuel Sequel=new sekuel();
    private final DefaultTableModel tabMode;
    private String sumber="";
    private int jmlItem=0;

    /** Creates new form DlgSalinTemplateLab
     * @param parent
     * @param modal */
    public DlgSalinTemplateLab(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        this.setSize(860,520);

        Object[] judul={"P","Kode Periksa","Nama Pemeriksaan","Kelas","Jenis Bayar","Total Tarif"};
        tabMode=new DefaultTableModel(null,judul){
            @Override public boolean isCellEditable(int rowIndex, int colIndex){return colIndex==0;}
            @Override public Class<?> getColumnClass(int columnIndex){return columnIndex==0?Boolean.class:Object.class;}
        };
        tbTujuan.setModel(tabMode);
        tbTujuan.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbTujuan.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] lebar={25,110,260,110,200,110};
        for(int i=0;i<lebar.length;i++){
            tbTujuan.getColumnModel().getColumn(i).setPreferredWidth(lebar[i]);
        }
        tbTujuan.setDefaultRenderer(Object.class,new WarnaTable());
        DefaultTableCellRenderer kanan=new DefaultTableCellRenderer();
        kanan.setHorizontalAlignment(SwingConstants.RIGHT);
        tbTujuan.getColumnModel().getColumn(5).setCellRenderer(kanan);
        tabMode.addTableModelListener(e -> hitungPilihan());
    }

    /**
     * Siapkan sumber & daftar kode tujuan. Mengembalikan true bila ada kode tujuan dan dialog perlu
     * ditampilkan; bila tidak, pesan sudah ditampilkan di sini.
     */
    public boolean setData(String kd,String nm,String kategori){
        sumber="";
        tabMode.setRowCount(0);
        try {
            if(Sequel.cariInteger("select count(*) from template_laboratorium where kd_jenis_prw=?",kd)>0){
                sumber=kd;
            }else{
                try (PreparedStatement ps=koneksi.prepareStatement(
                        "select t.kd_jenis_prw from template_laboratorium t inner join jns_perawatan_lab j on j.kd_jenis_prw=t.kd_jenis_prw "+
                        "where j.nm_perawatan=? and j.kategori=? and j.status='1' group by t.kd_jenis_prw order by count(*) desc,t.kd_jenis_prw limit 1")){
                    ps.setString(1,nm);
                    ps.setString(2,kategori);
                    try (ResultSet rs=ps.executeQuery()){
                        if(rs.next()){
                            sumber=rs.getString(1);
                        }
                    }
                }
            }
            if(sumber.equals("")){
                JOptionPane.showMessageDialog(null,"Belum ada kode "+nm+" yang memiliki template untuk disalin.");
                return false;
            }
            jmlItem=Sequel.cariInteger("select count(*) from template_laboratorium where kd_jenis_prw=?",sumber);
            try (PreparedStatement ps=koneksi.prepareStatement(
                    "select j.kd_jenis_prw,j.nm_perawatan,j.kelas,penjab.png_jawab,j.total_byr from jns_perawatan_lab j "+
                    "inner join penjab on penjab.kd_pj=j.kd_pj where j.nm_perawatan=? and j.kategori=? and j.status='1' "+
                    "and j.kd_jenis_prw<>? and not exists(select 1 from template_laboratorium t where t.kd_jenis_prw=j.kd_jenis_prw) "+
                    "order by j.kelas,j.kd_jenis_prw")){
                ps.setString(1,nm);
                ps.setString(2,kategori);
                ps.setString(3,sumber);
                try (ResultSet rs=ps.executeQuery()){
                    while(rs.next()){
                        tabMode.addRow(new Object[]{true,rs.getString(1),rs.getString(2),rs.getString(3),rs.getString(4),
                            String.format("%,.0f",rs.getDouble(5))});
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi Salin Template : "+e);
            JOptionPane.showMessageDialog(null,"Gagal membaca data template : "+e.getMessage());
            return false;
        }
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Semua kode "+nm+" sudah memiliki template.");
            return false;
        }
        LblSumber.setText(sumber+" - "+nm+" ("+jmlItem+" item template)");
        LblStatus.setText("Centang kode tujuan lalu klik Salin");
        hitungPilihan();
        return true;
    }

    private void hitungPilihan(){
        int dipilih=0;
        for(int i=0;i<tabMode.getRowCount();i++){
            if(Boolean.TRUE.equals(tabMode.getValueAt(i,0))){
                dipilih++;
            }
        }
        LblTujuan.setText(dipilih+" dari "+tabMode.getRowCount()+" kode belum punya template dipilih");
        BtnSalin.setEnabled(dipilih>0);
    }

    private void pilihSemua(boolean pilih){
        for(int i=0;i<tabMode.getRowCount();i++){
            tabMode.setValueAt(pilih,i,0);
        }
    }

    private void salin(){
        if(tbTujuan.isEditing()){
            tbTujuan.getCellEditor().stopCellEditing();
        }
        List<String> tujuan=new ArrayList<>();
        for(int i=0;i<tabMode.getRowCount();i++){
            if(Boolean.TRUE.equals(tabMode.getValueAt(i,0))){
                tujuan.add(tabMode.getValueAt(i,1).toString());
            }
        }
        if(tujuan.isEmpty()){
            JOptionPane.showMessageDialog(null,"Silahkan pilih kode tujuan terlebih dahulu..!!");
            return;
        }
        if(JOptionPane.showConfirmDialog(null,"Salin "+jmlItem+" item template dari "+sumber+" ke "+tujuan.size()+" kode terpilih?",
                "Salin Template Lab",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION){
            return;
        }
        boolean sukses=true;
        Sequel.AutoComitFalse();
        for(String kdtujuan:tujuan){
            if(!Sequel.queryu2tf(
                    "insert into template_laboratorium (kd_jenis_prw,Pemeriksaan,satuan,nilai_rujukan_ld,nilai_rujukan_la,nilai_rujukan_pd,"+
                    "nilai_rujukan_pa,bagian_rs,bhp,bagian_perujuk,bagian_dokter,bagian_laborat,kso,menejemen,biaya_item,urut) "+
                    "select ?,Pemeriksaan,satuan,nilai_rujukan_ld,nilai_rujukan_la,nilai_rujukan_pd,nilai_rujukan_pa,bagian_rs,bhp,"+
                    "bagian_perujuk,bagian_dokter,bagian_laborat,kso,menejemen,biaya_item,urut from template_laboratorium "+
                    "where kd_jenis_prw=? order by id_template",2,new String[]{kdtujuan,sumber})){
                sukses=false;
                break;
            }
        }
        if(sukses){
            Sequel.Commit();
        }else{
            Sequel.RollBack();
        }
        Sequel.AutoComitTrue();
        if(sukses){
            for(int i=tabMode.getRowCount()-1;i>=0;i--){
                if(Boolean.TRUE.equals(tabMode.getValueAt(i,0))){
                    tabMode.removeRow(i);
                }
            }
            LblStatus.setText("Template disalin ke "+tujuan.size()+" kode");
            JOptionPane.showMessageDialog(null,"Template berhasil disalin ke "+tujuan.size()+" kode.");
        }else{
            LblStatus.setText("Gagal, perubahan dibatalkan");
            JOptionPane.showMessageDialog(null,"Gagal menyalin template, perubahan dibatalkan.");
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
        panelInfo = new widget.panelisi();
        label1 = new widget.Label();
        LblSumber = new widget.Label();
        label2 = new widget.Label();
        LblTujuan = new widget.Label();
        scrollPane1 = new widget.ScrollPane();
        tbTujuan = new widget.Table();
        panelBawah = new widget.panelisi();
        BtnPilihSemua = new widget.Button();
        BtnBatalPilih = new widget.Button();
        BtnSalin = new widget.Button();
        BtnKeluar = new widget.Button();
        LblStatus = new widget.Label();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Salin Template Laboratorium ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 0, 12), new java.awt.Color(50, 50, 50))); // NOI18N
        internalFrame1.setName("internalFrame1"); // NOI18N
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

        panelInfo.setName("panelInfo"); // NOI18N
        panelInfo.setPreferredSize(new java.awt.Dimension(100, 66));
        panelInfo.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 5));

        label1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        label1.setText("Sumber :");
        label1.setName("label1"); // NOI18N
        label1.setPreferredSize(new java.awt.Dimension(70, 23));
        panelInfo.add(label1);

        LblSumber.setText("-");
        LblSumber.setName("LblSumber"); // NOI18N
        LblSumber.setPreferredSize(new java.awt.Dimension(700, 23));
        panelInfo.add(LblSumber);

        label2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        label2.setText("Tujuan :");
        label2.setName("label2"); // NOI18N
        label2.setPreferredSize(new java.awt.Dimension(70, 23));
        panelInfo.add(label2);

        LblTujuan.setText("-");
        LblTujuan.setName("LblTujuan"); // NOI18N
        LblTujuan.setPreferredSize(new java.awt.Dimension(700, 23));
        panelInfo.add(LblTujuan);

        internalFrame1.add(panelInfo, java.awt.BorderLayout.PAGE_START);

        scrollPane1.setName("scrollPane1"); // NOI18N
        scrollPane1.setOpaque(true);

        tbTujuan.setToolTipText("Centang kode yang akan diisi template");
        tbTujuan.setName("tbTujuan"); // NOI18N
        scrollPane1.setViewportView(tbTujuan);

        internalFrame1.add(scrollPane1, java.awt.BorderLayout.CENTER);

        panelBawah.setName("panelBawah"); // NOI18N
        panelBawah.setPreferredSize(new java.awt.Dimension(100, 56));
        panelBawah.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        BtnPilihSemua.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/checked.png"))); // NOI18N
        BtnPilihSemua.setMnemonic('A');
        BtnPilihSemua.setText("Pilih Semua");
        BtnPilihSemua.setToolTipText("Alt+A");
        BtnPilihSemua.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        BtnPilihSemua.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        BtnPilihSemua.setName("BtnPilihSemua"); // NOI18N
        BtnPilihSemua.setPreferredSize(new java.awt.Dimension(120, 30));
        BtnPilihSemua.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnPilihSemuaActionPerformed(evt);
            }
        });
        panelBawah.add(BtnPilihSemua);

        BtnBatalPilih.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Cancel-2-16x16.png"))); // NOI18N
        BtnBatalPilih.setMnemonic('B');
        BtnBatalPilih.setText("Batal Pilih");
        BtnBatalPilih.setToolTipText("Alt+B");
        BtnBatalPilih.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        BtnBatalPilih.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        BtnBatalPilih.setName("BtnBatalPilih"); // NOI18N
        BtnBatalPilih.setPreferredSize(new java.awt.Dimension(120, 30));
        BtnBatalPilih.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnBatalPilihActionPerformed(evt);
            }
        });
        panelBawah.add(BtnBatalPilih);

        BtnSalin.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/editcopy.png"))); // NOI18N
        BtnSalin.setMnemonic('S');
        BtnSalin.setText("Salin");
        BtnSalin.setToolTipText("Alt+S");
        BtnSalin.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        BtnSalin.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        BtnSalin.setName("BtnSalin"); // NOI18N
        BtnSalin.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnSalin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnSalinActionPerformed(evt);
            }
        });
        panelBawah.add(BtnSalin);

        BtnKeluar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/exit.png"))); // NOI18N
        BtnKeluar.setMnemonic('K');
        BtnKeluar.setText("Keluar");
        BtnKeluar.setToolTipText("Alt+K");
        BtnKeluar.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        BtnKeluar.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        BtnKeluar.setName("BtnKeluar"); // NOI18N
        BtnKeluar.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnKeluar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnKeluarActionPerformed(evt);
            }
        });
        panelBawah.add(BtnKeluar);

        LblStatus.setText("-");
        LblStatus.setName("LblStatus"); // NOI18N
        LblStatus.setPreferredSize(new java.awt.Dimension(300, 23));
        panelBawah.add(LblStatus);

        internalFrame1.add(panelBawah, java.awt.BorderLayout.PAGE_END);

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void BtnPilihSemuaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnPilihSemuaActionPerformed
        pilihSemua(true);
    }//GEN-LAST:event_BtnPilihSemuaActionPerformed

    private void BtnBatalPilihActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnBatalPilihActionPerformed
        pilihSemua(false);
    }//GEN-LAST:event_BtnBatalPilihActionPerformed

    private void BtnSalinActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSalinActionPerformed
        salin();
    }//GEN-LAST:event_BtnSalinActionPerformed

    private void BtnKeluarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnKeluarActionPerformed
        dispose();
    }//GEN-LAST:event_BtnKeluarActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private widget.Button BtnBatalPilih;
    private widget.Button BtnKeluar;
    private widget.Button BtnPilihSemua;
    private widget.Button BtnSalin;
    private widget.Label LblStatus;
    private widget.Label LblSumber;
    private widget.Label LblTujuan;
    private widget.InternalFrame internalFrame1;
    private widget.Label label1;
    private widget.Label label2;
    private widget.panelisi panelBawah;
    private widget.panelisi panelInfo;
    private widget.ScrollPane scrollPane1;
    private widget.Table tbTujuan;
    // End of variables declaration//GEN-END:variables
}
