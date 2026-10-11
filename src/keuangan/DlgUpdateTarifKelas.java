package keuangan;

import fungsi.WarnaTable;
import java.awt.Cursor;
import java.awt.Dimension;
import java.util.List;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Popup proses penyesuaian tarif Lab & Radiologi pasien: ke tarif kelas kamar inap
 * (dari Billing Ranap) atau kembali ke tarif tanpa kelas (saat Hapus Data Salah kamar inap).
 * Proses berjalan di thread terpisah, log & progres tampil langsung di popup.
 */
public class DlgUpdateTarifKelas extends javax.swing.JDialog {
    private final UpdateTarifKelasLabRad update=new UpdateTarifKelasLabRad();
    private String norawat="";
    private boolean keRalan=false,berjalan=false;
    private Runnable setelahSelesai;
    private final DefaultTableModel tabMode;

    /** Creates new form DlgUpdateTarifKelas
     * @param parent
     * @param modal */
    public DlgUpdateTarifKelas(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        this.setSize(980,560);

        Object[] judul={"Jenis","Tgl.Periksa","Pemeriksaan","Status","Kode Sekarang","Tarif Sekarang",
                        "Kode Tujuan","Tarif Tujuan","Selisih","Keterangan"};
        tabMode=new DefaultTableModel(null,judul){
            @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        tbBanding.setModel(tabMode);
        tbBanding.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbBanding.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] lebar={70,120,200,100,100,100,100,100,90,330};
        for(int i=0;i<lebar.length;i++){
            tbBanding.getColumnModel().getColumn(i).setPreferredWidth(lebar[i]);
        }
        tbBanding.setDefaultRenderer(Object.class,new WarnaTable());
        DefaultTableCellRenderer kanan=new DefaultTableCellRenderer();
        kanan.setHorizontalAlignment(SwingConstants.RIGHT);
        for(int i:new int[]{5,7,8}){
            tbBanding.getColumnModel().getColumn(i).setCellRenderer(kanan);
        }

        update.setPantau(new UpdateTarifKelasLabRad.Pantau() {
            @Override
            public void tulis(String pesan) {
                SwingUtilities.invokeLater(() -> {
                    TLog.append(pesan+"\n");
                    TLog.setCaretPosition(TLog.getDocument().getLength());
                });
            }

            @Override
            public void progres(int ke,int total) {
                // kemajuan per pemeriksaan ditampilkan di LblStatus
                SwingUtilities.invokeLater(() -> LblStatus.setText("Memproses "+ke+" dari "+total));
            }
        });
    }

    /**
     * @param norawat no. rawat pasien
     * @param pasien  teks nama pasien untuk ditampilkan
     * @param keRalan false = ke tarif kelas kamar inap, true = kembali ke tarif tanpa kelas (Ralan)
     */
    public void setData(String norawat,String pasien,boolean keRalan){
        this.norawat=norawat;
        this.keRalan=keRalan;
        LblNoRawat.setText(norawat);
        LblPasien.setText(pasien);
        int jumlah;
        if(keRalan){
            LblTujuan.setText("Tanpa Kelas (Ralan)");
            jumlah=update.jumlahRanap(norawat);
            LblJumlah.setText(jumlah+" pemeriksaan Lab/Radiologi berstatus Ranap");
        }else{
            LblTujuan.setText(update.cariKelas(norawat));
            jumlah=update.jumlahProses(norawat);
            LblJumlah.setText(jumlah+" pemeriksaan Lab/Radiologi belum bertarif "+LblTujuan.getText());
        }
        TLog.setText("");
        TLog.append((keRalan?
                "Pemeriksaan Ranap akan dikembalikan ke tarif & kode tanpa kelas, status diubah menjadi Ralan.\n":
                "Pemeriksaan akan disesuaikan ke tarif & kode "+LblTujuan.getText()+" berdasarkan nama perawatan, status diubah menjadi Ranap.\n")+
                "Jurnal koreksi dibuat otomatis. Master tarif jns_perawatan_lab / jns_perawatan_radiologi tidak diubah.\n"+
                "Pemeriksaan yang jasa medisnya sudah dibayar akan dilewati.\n\n"+
                (jumlah>0?"Klik Proses untuk memulai.\n":"Tidak ada pemeriksaan yang perlu diproses.\n"));
        LblStatus.setText(jumlah>0?"Siap diproses":"Tidak ada data");
        BtnProses.setEnabled(jumlah>0);
        BtnKeluar.setEnabled(true);
        tampilBanding();
        TabProses.setSelectedIndex(0);
    }

    /** Isi tab Perbandingan Tarif: kode & tarif sekarang vs yang akan dipakai (belum disimpan). */
    private void tampilBanding(){
        tabMode.setRowCount(0);
        double sekarang=0,tujuan=0;
        for(UpdateTarifKelasLabRad.Banding b:update.bandingkan(norawat,keRalan)){
            tabMode.addRow(new Object[]{
                b.jenis,b.waktu,b.nama,b.status,b.kdSekarang,rupiah(b.tarifSekarang),
                b.kdTujuan,rupiah(b.tarifTujuan),rupiah(b.tarifTujuan-b.tarifSekarang),b.keterangan
            });
            sekarang+=b.tarifSekarang;
            tujuan+=b.tarifTujuan;
        }
        LblTotal.setText(tabMode.getRowCount()==0?"-":
                "Sekarang "+rupiah(sekarang)+"   ->   "+(keRalan?"Tanpa Kelas ":LblTujuan.getText()+" ")+rupiah(tujuan)+
                "   (selisih "+rupiah(tujuan-sekarang)+")");
    }

    private String rupiah(double nilai){
        return String.format("%,.0f",nilai);
    }

    /** Dipanggil di thread layar setelah proses selesai (berhasil maupun gagal). */
    public void setSetelahSelesai(Runnable setelahSelesai){
        this.setelahSelesai=setelahSelesai;
    }

    /** Mulai proses tanpa menunggu tombol Proses ditekan. */
    public void mulai(){
        if(berjalan||norawat.equals("")){
            return;
        }
        berjalan=true;
        BtnProses.setEnabled(false);
        BtnKeluar.setEnabled(false);
        TLog.append("\n");
        TabProses.setSelectedIndex(1);
        LblStatus.setText("Memproses...");
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new Thread(() -> {
            boolean ok;
            try {
                ok=keRalan?update.kembalikanTanpaKelas(norawat):update.proses(norawat);
            } catch (Exception e) {
                System.out.println("Notifikasi Update Tarif Kelas : "+e);
                ok=false;
            }
            final boolean hasil=ok;
            SwingUtilities.invokeLater(() -> selesai(hasil));
        },"UpdateTarifKelas").start();
    }

    private void selesai(boolean ok){
        berjalan=false;
        this.setCursor(Cursor.getDefaultCursor());
        BtnKeluar.setEnabled(true);
        if(ok){
            LblStatus.setText("Selesai");
            TLog.append("\nRINGKASAN\n"+
                    "Lab : "+update.getJmlLab()+" pemeriksaan, Radiologi : "+update.getJmlRad()+" pemeriksaan, "+
                    update.getJmlKode()+" kode tindakan diganti.\n");
            daftar("Tarif tujuan tidak ditemukan (harga & kode tetap) :",update.getTidakDitemukan());
            daftar("Tarif disesuaikan tetapi kode tidak diganti :",update.getKodeTetap());
            daftar("Dilewati karena jasa medis sudah dibayar :",update.getDilewati());
            daftar("Item template hasil lab disalin :",update.getTemplateDisalin());
        }else{
            LblStatus.setText("Gagal, dibatalkan");
            BtnProses.setEnabled(true);
        }
        TLog.setCaretPosition(TLog.getDocument().getLength());
        if(setelahSelesai!=null){
            setelahSelesai.run();
        }
    }

    private void daftar(String judul,List<String> isi){
        if(!isi.isEmpty()){
            TLog.append("\n"+judul+"\n- "+String.join("\n- ",isi)+"\n");
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
        LblNoRawat = new widget.Label();
        label2 = new widget.Label();
        LblPasien = new widget.Label();
        label3 = new widget.Label();
        LblTujuan = new widget.Label();
        label4 = new widget.Label();
        LblJumlah = new widget.Label();
        label5 = new widget.Label();
        LblTotal = new widget.Label();
        TabProses = new widget.TabPane();
        scrollPane2 = new widget.ScrollPane();
        tbBanding = new widget.Table();
        scrollPane1 = new widget.ScrollPane();
        TLog = new widget.TextArea();
        panelBawah = new widget.panelisi();
        LblStatus = new widget.Label();
        BtnProses = new widget.Button();
        BtnKeluar = new widget.Button();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Update Tarif Lab & Radiologi Sesuai Kelas ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 0, 12), new java.awt.Color(50, 50, 50))); // NOI18N
        internalFrame1.setName("internalFrame1"); // NOI18N
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

        panelInfo.setName("panelInfo"); // NOI18N
        panelInfo.setPreferredSize(new java.awt.Dimension(100, 94));
        panelInfo.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 5));

        label1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        label1.setText("No.Rawat :");
        label1.setName("label1"); // NOI18N
        label1.setPreferredSize(new java.awt.Dimension(65, 23));
        panelInfo.add(label1);

        LblNoRawat.setText("-");
        LblNoRawat.setName("LblNoRawat"); // NOI18N
        LblNoRawat.setPreferredSize(new java.awt.Dimension(140, 23));
        panelInfo.add(LblNoRawat);

        label2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        label2.setText("Pasien :");
        label2.setName("label2"); // NOI18N
        label2.setPreferredSize(new java.awt.Dimension(60, 23));
        panelInfo.add(label2);

        LblPasien.setText("-");
        LblPasien.setName("LblPasien"); // NOI18N
        LblPasien.setPreferredSize(new java.awt.Dimension(400, 23));
        panelInfo.add(LblPasien);

        label3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        label3.setText("Tujuan :");
        label3.setName("label3"); // NOI18N
        label3.setPreferredSize(new java.awt.Dimension(65, 23));
        panelInfo.add(label3);

        LblTujuan.setText("-");
        LblTujuan.setName("LblTujuan"); // NOI18N
        LblTujuan.setPreferredSize(new java.awt.Dimension(140, 23));
        panelInfo.add(LblTujuan);

        label4.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        label4.setText("Jumlah :");
        label4.setName("label4"); // NOI18N
        label4.setPreferredSize(new java.awt.Dimension(60, 23));
        panelInfo.add(label4);

        LblJumlah.setText("-");
        LblJumlah.setName("LblJumlah"); // NOI18N
        LblJumlah.setPreferredSize(new java.awt.Dimension(400, 23));
        panelInfo.add(LblJumlah);

        label5.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        label5.setText("Total :");
        label5.setName("label5"); // NOI18N
        label5.setPreferredSize(new java.awt.Dimension(65, 23));
        panelInfo.add(label5);

        LblTotal.setText("-");
        LblTotal.setName("LblTotal"); // NOI18N
        LblTotal.setPreferredSize(new java.awt.Dimension(605, 23));
        panelInfo.add(LblTotal);

        internalFrame1.add(panelInfo, java.awt.BorderLayout.PAGE_START);

        TabProses.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        TabProses.setName("TabProses"); // NOI18N

        scrollPane2.setName("scrollPane2"); // NOI18N
        scrollPane2.setOpaque(true);

        tbBanding.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        tbBanding.setToolTipText("Perbandingan kode & tarif sekarang dengan kode & tarif yang akan dipakai");
        tbBanding.setName("tbBanding"); // NOI18N
        scrollPane2.setViewportView(tbBanding);

        TabProses.addTab("Perbandingan Tarif", scrollPane2);

        scrollPane1.setName("scrollPane1"); // NOI18N
        scrollPane1.setOpaque(true);

        TLog.setEditable(false);
        TLog.setColumns(20);
        TLog.setRows(5);
        TLog.setName("TLog"); // NOI18N
        scrollPane1.setViewportView(TLog);

        TabProses.addTab("Log Proses", scrollPane1);

        internalFrame1.add(TabProses, java.awt.BorderLayout.CENTER);

        panelBawah.setName("panelBawah"); // NOI18N
        panelBawah.setPreferredSize(new java.awt.Dimension(100, 56));
        panelBawah.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));


        LblStatus.setText("Siap diproses");
        LblStatus.setName("LblStatus"); // NOI18N
        LblStatus.setPreferredSize(new java.awt.Dimension(180, 23));
        panelBawah.add(LblStatus);

        BtnProses.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/save-16x16.png"))); // NOI18N
        BtnProses.setMnemonic('P');
        BtnProses.setText("Proses");
        BtnProses.setToolTipText("Alt+P");
        BtnProses.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        BtnProses.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        BtnProses.setName("BtnProses"); // NOI18N
        BtnProses.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnProses.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnProsesActionPerformed(evt);
            }
        });
        panelBawah.add(BtnProses);

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

        internalFrame1.add(panelBawah, java.awt.BorderLayout.PAGE_END);

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void BtnProsesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnProsesActionPerformed
        mulai();
    }//GEN-LAST:event_BtnProsesActionPerformed

    private void BtnKeluarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnKeluarActionPerformed
        if(!berjalan){
            dispose();
        }
    }//GEN-LAST:event_BtnKeluarActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private widget.Button BtnKeluar;
    private widget.Button BtnProses;
    private widget.Label LblJumlah;
    private widget.Label LblNoRawat;
    private widget.Label LblPasien;
    private widget.Label LblStatus;
    private widget.Label LblTotal;
    private widget.Label LblTujuan;
    private widget.TextArea TLog;
    private widget.TabPane TabProses;
    private widget.InternalFrame internalFrame1;
    private widget.Label label1;
    private widget.Label label2;
    private widget.Label label3;
    private widget.Label label4;
    private widget.Label label5;
    private widget.panelisi panelBawah;
    private widget.panelisi panelInfo;
    private widget.ScrollPane scrollPane1;
    private widget.ScrollPane scrollPane2;
    private widget.Table tbBanding;
    // End of variables declaration//GEN-END:variables
}
