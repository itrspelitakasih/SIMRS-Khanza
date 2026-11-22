package keuangan;

import fungsi.WarnaTable;
import fungsi.koneksiDB;
import fungsi.sekuel;
import java.awt.Dimension;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Salin item template hasil lab ke kode lain dengan nama pemeriksaan & kategori sama yang belum punya
 * template (mis. kode tarif kelas). Sumber awal: kode terpilih bila punya template, selain itu jenis bayar
 * Cash, selain itu kode bernama sama dengan item template terbanyak. Sumber bisa diganti lewat pilihan
 * Jenis Bayar + Cari Template. Salin hanya untuk kode yang belum punya template. Update (tab Update Template)
 * menyamakan kode yang sudah punya template dengan sumber: baris diubah di tempat / ditambah, TIDAK pernah
 * dihapus (id_template dipakai hasil lab, permintaan dan paket) dan biaya tidak diubah.
 */
public class DlgSalinTemplateLab extends javax.swing.JDialog {
    private final Connection koneksi=koneksiDB.condb();
    private final sekuel Sequel=new sekuel();
    private final DefaultTableModel tabMode,tabUpdate;
    // isi template semua kode bernama sama: kode -> item {id,nama,satuan,ld,la,pd,pa,method,urut}
    private final Map<String,List<String[]>> isiTemplate=new LinkedHashMap<>();
    private final Map<String,String[]> infoKode=new HashMap<>(); // kode -> {kelas,jenis bayar}
    private String kodeKlik="",kategoriKlik="";
    // satu calon sumber per jenis bayar {kode periksa, kd_pj, nama jenis bayar, jumlah item}; urutan = isi CmbJenisBayar
    private final List<String[]> calon=new ArrayList<>();
    private String sumber="",pemeriksaan="",jenisBayarSumber="";
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

        tabUpdate=new DefaultTableModel(null,new Object[]{"P","Kode Periksa","Nama Pemeriksaan","Kelas","Jenis Bayar","Perubahan"}){
            @Override public boolean isCellEditable(int rowIndex, int colIndex){return colIndex==0;}
            @Override public Class<?> getColumnClass(int columnIndex){return columnIndex==0?Boolean.class:Object.class;}
        };
        tbUpdate.setModel(tabUpdate);
        tbUpdate.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] lebarUpdate={25,110,170,90,180,240};
        for(int i=0;i<lebarUpdate.length;i++){
            tbUpdate.getColumnModel().getColumn(i).setPreferredWidth(lebarUpdate[i]);
        }
        tbUpdate.setDefaultRenderer(Object.class,new WarnaTable());
        tabUpdate.addTableModelListener(e -> hitungPilihan());
    }

    /**
     * Siapkan sumber & daftar kode tujuan. Mengembalikan true bila ada kode tujuan dan dialog perlu
     * ditampilkan; bila tidak, pesan sudah ditampilkan di sini.
     */
    public boolean setData(String kd,String nm,String kategori){
        sumber="";
        pemeriksaan=nm;
        kodeKlik=kd;
        kategoriKlik=kategori;
        calon.clear();
        CmbJenisBayar.removeAllItems();
        tabMode.setRowCount(0);
        boolean adaCash=false;
        try {
            // urutan: kode yang diklik, jenis bayar Cash, item terbanyak, rawat jalan (kelas '-');
            // baris pertama tiap jenis bayar = sumber jenis bayar itu, baris pertama semuanya = sumber awal
            try (PreparedStatement ps=koneksi.prepareStatement(
                    "select j.kd_jenis_prw,j.kd_pj,ifnull(p.png_jawab,j.kd_pj),count(*) from template_laboratorium t "+
                    "inner join jns_perawatan_lab j on j.kd_jenis_prw=t.kd_jenis_prw left join penjab p on p.kd_pj=j.kd_pj "+
                    "where j.kd_jenis_prw=? or (j.nm_perawatan=? and j.kategori=? and j.status='1') "+
                    "group by j.kd_jenis_prw,j.kd_pj,p.png_jawab,j.kelas "+
                    "order by (j.kd_jenis_prw=?) desc,(ifnull(p.png_jawab,'')='Cash') desc,count(*) desc,(j.kelas='-') desc,j.kd_jenis_prw")){
                ps.setString(1,kd);
                ps.setString(2,nm);
                ps.setString(3,kategori);
                ps.setString(4,kd);
                try (ResultSet rs=ps.executeQuery()){
                    Set<String> sudah=new HashSet<>();
                    while(rs.next()){
                        if(sudah.add(rs.getString(2))){
                            calon.add(new String[]{rs.getString(1),rs.getString(2),rs.getString(3),rs.getString(4)});
                            adaCash|=rs.getString(3).trim().equalsIgnoreCase("Cash");
                        }
                    }
                }
            }
            if(calon.isEmpty()){
                JOptionPane.showMessageDialog(null,"Belum ada kode "+nm+" yang memiliki template untuk disalin, termasuk di jenis bayar Cash.\n"+
                        "Isi dulu templatenya di salah satu kode lewat menu Template Laboratorium.");
                return false;
            }
            // sumber awal tetap paling atas, sisanya urut nama supaya mudah dicari
            calon.subList(1,calon.size()).sort((a,b) -> a[2].compareToIgnoreCase(b[2]));
            for(String[] c:calon){
                CmbJenisBayar.addItem(c[2]+" ["+c[1]+"] - "+c[3]+" item");
            }
            CmbJenisBayar.setSelectedIndex(0);
            muatIsi();
            pakaiSumber(0);
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
        if(tabMode.getRowCount()==0&&isiTemplate.size()<2){
            JOptionPane.showMessageDialog(null,"Tidak ada kode "+nm+" lain untuk disalin atau diupdate.");
            return false;
        }
        TabTemplate.setSelectedIndex(tabMode.getRowCount()==0?1:0);
        LblStatus.setText(adaCash?"Centang kode tujuan lalu klik Salin":"Cash belum punya template, cek sumbernya");
        hitungPilihan();
        return true;
    }

    private void pakaiSumber(int i){
        String[] c=calon.get(i);
        sumber=c[0];
        jenisBayarSumber=c[2];
        jmlItem=Integer.parseInt(c[3]);
        LblSumber.setText(sumber+" - "+pemeriksaan+" - "+jenisBayarSumber+" ("+jmlItem+" item template)");
        tampilUpdate();
    }

    private void muatIsi() throws Exception {
        isiTemplate.clear();
        infoKode.clear();
        try (PreparedStatement ps=koneksi.prepareStatement(
                "select t.kd_jenis_prw,j.kelas,ifnull(p.png_jawab,j.kd_pj),t.id_template,t.Pemeriksaan,t.satuan,t.nilai_rujukan_ld,t.nilai_rujukan_la,"+
                "t.nilai_rujukan_pd,t.nilai_rujukan_pa,ifnull(t.method,''),ifnull(t.urut,0) from template_laboratorium t "+
                "inner join jns_perawatan_lab j on j.kd_jenis_prw=t.kd_jenis_prw left join penjab p on p.kd_pj=j.kd_pj "+
                "where j.kd_jenis_prw=? or (j.nm_perawatan=? and j.kategori=? and j.status='1') order by j.kelas,t.kd_jenis_prw,t.urut,t.id_template")){
            ps.setString(1,kodeKlik);
            ps.setString(2,pemeriksaan);
            ps.setString(3,kategoriKlik);
            try (ResultSet rs=ps.executeQuery()){
                while(rs.next()){
                    String kode=rs.getString(1);
                    infoKode.put(kode,new String[]{rs.getString(2),rs.getString(3)});
                    String[] item=new String[9];
                    for(int i=0;i<item.length;i++){
                        item[i]=rs.getString(i+4)==null?"":rs.getString(i+4);
                    }
                    isiTemplate.computeIfAbsent(kode,k -> new ArrayList<>()).add(item);
                }
            }
        }
    }

    private static final class Selisih{
        final List<String[]> ubah=new ArrayList<>(); // {id sumber, id tujuan}
        final List<String> tambah=new ArrayList<>(); // id sumber
        int lebih=0;
    }

    /**
     * Item dipasangkan berdasarkan nama. Template satu item di kedua sisi dianggap item yang sama walau namanya beda,
     * supaya ganti nama tidak menjadi item dobel. Item yang hanya ada di tujuan dibiarkan.
     */
    private Selisih selisih(String tujuan){
        Selisih h=new Selisih();
        List<String[]> asal=isiTemplate.get(sumber),sekarang=isiTemplate.get(tujuan);
        if(asal==null||sekarang==null){
            return h;
        }
        List<String[]> sisa=new ArrayList<>(sekarang);
        boolean satuItem=asal.size()==1&&sekarang.size()==1;
        for(String[] a:asal){
            String[] pasangan=null;
            for(String[] s:sisa){
                if(satuItem||namaItem(s[1]).equals(namaItem(a[1]))){
                    pasangan=s;
                    break;
                }
            }
            if(pasangan==null){
                h.tambah.add(a[0]);
                continue;
            }
            sisa.remove(pasangan);
            for(int i=1;i<a.length;i++){
                if(!a[i].equals(pasangan[i])){
                    h.ubah.add(new String[]{a[0],pasangan[0]});
                    break;
                }
            }
        }
        h.lebih=sisa.size();
        return h;
    }

    private static String namaItem(String s){
        return s.trim().replaceAll("\\s+"," ").toLowerCase();
    }

    /** Tab Update: kode bertemplate yang isinya beda dari sumber. Tidak dicentang dari awal karena menimpa data. */
    private void tampilUpdate(){
        tabUpdate.setRowCount(0);
        for(String kode:isiTemplate.keySet()){
            if(kode.equals(sumber)){
                continue;
            }
            Selisih h=selisih(kode);
            if(h.ubah.isEmpty()&&h.tambah.isEmpty()){
                continue;
            }
            List<String> ket=new ArrayList<>();
            if(!h.ubah.isEmpty()){
                ket.add(h.ubah.size()+" diubah");
            }
            if(!h.tambah.isEmpty()){
                ket.add(h.tambah.size()+" ditambah");
            }
            if(h.lebih>0){
                ket.add(h.lebih+" item lain dibiarkan");
            }
            tabUpdate.addRow(new Object[]{false,kode,pemeriksaan,infoKode.get(kode)[0],infoKode.get(kode)[1],String.join(", ",ket)});
        }
        TabTemplate.setTitleAt(1,"Update Template ("+tabUpdate.getRowCount()+")");
        hitungPilihan();
    }

    private void update(){
        if(tbUpdate.isEditing()){
            tbUpdate.getCellEditor().stopCellEditing();
        }
        List<String> tujuan=new ArrayList<>();
        for(int i=0;i<tabUpdate.getRowCount();i++){
            if(Boolean.TRUE.equals(tabUpdate.getValueAt(i,0))){
                tujuan.add(tabUpdate.getValueAt(i,1).toString());
            }
        }
        if(tujuan.isEmpty()||!pilihanSudahDicari()){
            JOptionPane.showMessageDialog(null,"Silahkan pilih kode yang akan diupdate terlebih dahulu..!!");
            return;
        }
        if(JOptionPane.showConfirmDialog(null,"Update "+tujuan.size()+" kode mengikuti template "+sumber+" ("+jenisBayarSumber+")?\n\n"+
                "- Nama item, satuan, nilai rujukan, metode dan urutan disamakan; item yang kurang ditambah.\n"+
                "- Biaya item tidak diubah dan tidak ada item yang dihapus.\n"+
                "- Cetak ulang hasil lama memakai nama, satuan dan metode yang baru; nilai rujukan hasil lama tetap.",
                "Update Template Lab",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE)!=JOptionPane.YES_OPTION){
            return;
        }
        try {
            int[] jumlah=terapkanUpdate(tujuan);
            LblStatus.setText("Update selesai");
            JOptionPane.showMessageDialog(null,"Template "+tujuan.size()+" kode berhasil diupdate.\n"+jumlah[0]+" item diubah, "+jumlah[1]+" item ditambah.");
        } catch (Exception e) {
            System.out.println("Notifikasi Update Template : "+e);
            LblStatus.setText("Gagal, perubahan dibatalkan");
            JOptionPane.showMessageDialog(null,"Gagal mengupdate template, perubahan dibatalkan.\n"+e.getMessage());
        }
    }

    /** Satu perintah SQL per item sumber (bukan per kode tujuan) karena latensi server. Hasil {diubah, ditambah}. */
    private int[] terapkanUpdate(List<String> tujuan) throws Exception {
        int[] jumlah={0,0};
        boolean autoAwal=koneksi.getAutoCommit();
        koneksi.setAutoCommit(false);
        try {
            muatIsi(); // bandingkan dengan isi terbaru
            Map<String,List<String>> ubah=new LinkedHashMap<>(),tambah=new LinkedHashMap<>();
            for(String kode:tujuan){
                if(kode.equals(sumber)){
                    continue;
                }
                Selisih h=selisih(kode);
                for(String[] u:h.ubah){
                    ubah.computeIfAbsent(u[0],k -> new ArrayList<>()).add(u[1]);
                }
                for(String t:h.tambah){
                    tambah.computeIfAbsent(t,k -> new ArrayList<>()).add(kode);
                }
            }
            for(Map.Entry<String,List<String>> e:ubah.entrySet()){
                jumlah[0]+=e.getValue().size();
                jalankan("update template_laboratorium t inner join template_laboratorium s on s.id_template=? set t.Pemeriksaan=s.Pemeriksaan,t.satuan=s.satuan,"+
                        "t.nilai_rujukan_ld=s.nilai_rujukan_ld,t.nilai_rujukan_la=s.nilai_rujukan_la,t.nilai_rujukan_pd=s.nilai_rujukan_pd,"+
                        "t.nilai_rujukan_pa=s.nilai_rujukan_pa,t.method=ifnull(s.method,''),t.urut=s.urut where t.id_template in ",e.getKey(),e.getValue(),"");
            }
            for(Map.Entry<String,List<String>> e:tambah.entrySet()){
                jumlah[1]+=e.getValue().size();
                jalankan("insert into template_laboratorium (kd_jenis_prw,Pemeriksaan,satuan,nilai_rujukan_ld,nilai_rujukan_la,nilai_rujukan_pd,nilai_rujukan_pa,"+
                        "method,bagian_rs,bhp,bagian_perujuk,bagian_dokter,bagian_laborat,kso,menejemen,biaya_item,urut) "+
                        "select j.kd_jenis_prw,s.Pemeriksaan,s.satuan,s.nilai_rujukan_ld,s.nilai_rujukan_la,s.nilai_rujukan_pd,s.nilai_rujukan_pa,"+
                        "ifnull(s.method,''),s.bagian_rs,s.bhp,s.bagian_perujuk,s.bagian_dokter,s.bagian_laborat,s.kso,s.menejemen,s.biaya_item,s.urut "+
                        "from template_laboratorium s inner join jns_perawatan_lab j where s.id_template=? and j.kd_jenis_prw in ",e.getKey(),e.getValue()," order by j.kd_jenis_prw");
            }
            koneksi.commit();
        } catch (Exception e) {
            koneksi.rollback();
            throw e;
        } finally {
            koneksi.setAutoCommit(autoAwal);
            muatIsi();
            tampilUpdate();
        }
        return jumlah;
    }

    private void jalankan(String sql,String idSumber,List<String> daftar,String akhiran) throws Exception {
        String lengkap=sql+"("+String.join(",",java.util.Collections.nCopies(daftar.size(),"?"))+")"+akhiran;
        try (PreparedStatement ps=koneksi.prepareStatement(lengkap)){
            ps.setString(1,idSumber);
            for(int i=0;i<daftar.size();i++){
                ps.setString(i+2,daftar.get(i));
            }
            ps.executeUpdate();
        }
        Sequel.SimpanTrack(lengkap+" "+idSumber+" "+daftar);
    }

    /** Salin hanya aktif bila jenis bayar di pilihan sama dengan sumber yang sedang dipakai. */
    private boolean pilihanSudahDicari(){
        int i=CmbJenisBayar.getSelectedIndex();
        return i>=0&&i<calon.size()&&calon.get(i)[0].equals(sumber);
    }

    private void hitungPilihan(){
        if(tabMode==null||tabUpdate==null){
            return; // masih initComponents
        }
        boolean diUpdate=TabTemplate.getSelectedIndex()==1;
        DefaultTableModel m=diUpdate?tabUpdate:tabMode;
        int dipilih=0;
        for(int i=0;i<m.getRowCount();i++){
            if(Boolean.TRUE.equals(m.getValueAt(i,0))){
                dipilih++;
            }
        }
        if(diUpdate){
            LblTujuan.setText(m.getRowCount()==0?"Semua kode bertemplate sudah sama dengan sumber"
                    :dipilih+" dari "+m.getRowCount()+" kode bertemplate yang beda dari sumber dipilih");
        }else{
            LblTujuan.setText(dipilih+" dari "+m.getRowCount()+" kode belum punya template dipilih");
        }
        boolean siap=pilihanSudahDicari();
        BtnSalin.setEnabled(!diUpdate&&dipilih>0&&siap);
        BtnUpdate.setEnabled(siap&&(!diUpdate||dipilih>0));
    }

    private void pilihSemua(boolean pilih){
        DefaultTableModel m=TabTemplate.getSelectedIndex()==1?tabUpdate:tabMode;
        for(int i=0;i<m.getRowCount();i++){
            m.setValueAt(pilih,i,0);
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
        if(JOptionPane.showConfirmDialog(null,"Salin "+jmlItem+" item template dari "+sumber+" ("+jenisBayarSumber+") ke "+tujuan.size()+" kode terpilih?",
                "Salin Template Lab",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION){
            return;
        }
        boolean sukses=true;
        Sequel.AutoComitFalse();
        for(String kdtujuan:tujuan){
            if(!Sequel.queryu2tf(
                    "insert into template_laboratorium (kd_jenis_prw,Pemeriksaan,satuan,nilai_rujukan_ld,nilai_rujukan_la,nilai_rujukan_pd,"+
                    "nilai_rujukan_pa,method,bagian_rs,bhp,bagian_perujuk,bagian_dokter,bagian_laborat,kso,menejemen,biaya_item,urut) "+
                    "select ?,Pemeriksaan,satuan,nilai_rujukan_ld,nilai_rujukan_la,nilai_rujukan_pd,nilai_rujukan_pa,ifnull(method,''),bagian_rs,bhp,"+
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
            try {
                muatIsi();
                tampilUpdate();
            } catch (Exception e) {
                System.out.println("Notifikasi Salin Template : "+e);
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
        label3 = new widget.Label();
        CmbJenisBayar = new widget.ComboBox();
        BtnCariTemplate = new widget.Button();
        label1 = new widget.Label();
        LblSumber = new widget.Label();
        label2 = new widget.Label();
        LblTujuan = new widget.Label();
        TabTemplate = new widget.TabPane();
        scrollPane1 = new widget.ScrollPane();
        tbTujuan = new widget.Table();
        scrollPane2 = new widget.ScrollPane();
        tbUpdate = new widget.Table();
        panelBawah = new widget.panelisi();
        BtnPilihSemua = new widget.Button();
        BtnBatalPilih = new widget.Button();
        BtnSalin = new widget.Button();
        BtnUpdate = new widget.Button();
        BtnKeluar = new widget.Button();
        LblStatus = new widget.Label();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Salin Template Laboratorium ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 12), new java.awt.Color(50, 50, 50))); // NOI18N
        internalFrame1.setName("internalFrame1"); // NOI18N
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

        panelInfo.setName("panelInfo"); // NOI18N
        panelInfo.setPreferredSize(new java.awt.Dimension(100, 94));
        panelInfo.setLayout(null);

        label3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        label3.setText("Jenis Bayar :");
        label3.setName("label3"); // NOI18N
        panelInfo.add(label3);
        label3.setBounds(0, 8, 90, 23);

        CmbJenisBayar.setToolTipText("Jenis bayar yang sudah punya template untuk pemeriksaan ini");
        CmbJenisBayar.setName("CmbJenisBayar"); // NOI18N
        CmbJenisBayar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CmbJenisBayarActionPerformed(evt);
            }
        });
        panelInfo.add(CmbJenisBayar);
        CmbJenisBayar.setBounds(94, 8, 420, 23);

        BtnCariTemplate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Search-16x16.png"))); // NOI18N
        BtnCariTemplate.setMnemonic(67);
        BtnCariTemplate.setText("Cari Template");
        BtnCariTemplate.setToolTipText("Alt+C, pakai template dari jenis bayar yang dipilih");
        BtnCariTemplate.setName("BtnCariTemplate"); // NOI18N
        BtnCariTemplate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnCariTemplateActionPerformed(evt);
            }
        });
        panelInfo.add(BtnCariTemplate);
        BtnCariTemplate.setBounds(520, 6, 140, 27);

        label1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        label1.setText("Sumber :");
        label1.setName("label1"); // NOI18N
        panelInfo.add(label1);
        label1.setBounds(0, 36, 90, 23);

        LblSumber.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LblSumber.setText("-");
        LblSumber.setName("LblSumber"); // NOI18N
        panelInfo.add(LblSumber);
        LblSumber.setBounds(94, 36, 740, 23);

        label2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        label2.setText("Tujuan :");
        label2.setName("label2"); // NOI18N
        panelInfo.add(label2);
        label2.setBounds(0, 64, 90, 23);

        LblTujuan.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LblTujuan.setText("-");
        LblTujuan.setName("LblTujuan"); // NOI18N
        panelInfo.add(LblTujuan);
        LblTujuan.setBounds(94, 64, 740, 23);

        internalFrame1.add(panelInfo, java.awt.BorderLayout.PAGE_START);

        TabTemplate.setBackground(new java.awt.Color(254, 255, 254));
        TabTemplate.setForeground(new java.awt.Color(50, 50, 50));
        TabTemplate.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        TabTemplate.setName("TabTemplate"); // NOI18N
        TabTemplate.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                TabTemplateStateChanged(evt);
            }
        });

        scrollPane1.setName("scrollPane1"); // NOI18N
        scrollPane1.setOpaque(true);

        tbTujuan.setToolTipText("Centang kode yang akan diisi template");
        tbTujuan.setName("tbTujuan"); // NOI18N
        scrollPane1.setViewportView(tbTujuan);

        TabTemplate.addTab("Belum Ada Template", scrollPane1);

        scrollPane2.setName("scrollPane2"); // NOI18N
        scrollPane2.setOpaque(true);

        tbUpdate.setToolTipText("Centang kode yang templatenya akan disamakan dengan sumber");
        tbUpdate.setName("tbUpdate"); // NOI18N
        scrollPane2.setViewportView(tbUpdate);

        TabTemplate.addTab("Update Template", scrollPane2);

        internalFrame1.add(TabTemplate, java.awt.BorderLayout.CENTER);

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

        BtnUpdate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/inventaris.png"))); // NOI18N
        BtnUpdate.setMnemonic(85);
        BtnUpdate.setText("Update");
        BtnUpdate.setToolTipText("Alt+U, samakan kode yang sudah punya template dengan sumber");
        BtnUpdate.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        BtnUpdate.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        BtnUpdate.setName("BtnUpdate"); // NOI18N
        BtnUpdate.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnUpdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnUpdateActionPerformed(evt);
            }
        });
        panelBawah.add(BtnUpdate);

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
        LblStatus.setPreferredSize(new java.awt.Dimension(260, 23));
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

    private void CmbJenisBayarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CmbJenisBayarActionPerformed
        if(!sumber.equals("")){
            LblStatus.setText(pilihanSudahDicari()?"Centang kode tujuan lalu klik Salin":"Klik Cari Template untuk memakai pilihan ini");
            hitungPilihan();
        }
    }//GEN-LAST:event_CmbJenisBayarActionPerformed

    private void BtnCariTemplateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCariTemplateActionPerformed
        int i=CmbJenisBayar.getSelectedIndex();
        if(i>=0&&i<calon.size()){
            pakaiSumber(i);
            LblStatus.setText("Centang kode tujuan lalu klik Salin");
            hitungPilihan();
        }
    }//GEN-LAST:event_BtnCariTemplateActionPerformed

    private void TabTemplateStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_TabTemplateStateChanged
        hitungPilihan();
    }//GEN-LAST:event_TabTemplateStateChanged

    private void BtnUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnUpdateActionPerformed
        if(TabTemplate.getSelectedIndex()!=1){
            TabTemplate.setSelectedIndex(1);
            LblStatus.setText("Centang kode lalu klik Update");
        }else{
            update();
        }
    }//GEN-LAST:event_BtnUpdateActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private widget.Button BtnBatalPilih;
    private widget.Button BtnCariTemplate;
    private widget.Button BtnKeluar;
    private widget.Button BtnPilihSemua;
    private widget.Button BtnSalin;
    private widget.Button BtnUpdate;
    private widget.ComboBox CmbJenisBayar;
    private widget.Label LblStatus;
    private widget.Label LblSumber;
    private widget.Label LblTujuan;
    private widget.TabPane TabTemplate;
    private widget.InternalFrame internalFrame1;
    private widget.Label label1;
    private widget.Label label2;
    private widget.Label label3;
    private widget.panelisi panelBawah;
    private widget.panelisi panelInfo;
    private widget.ScrollPane scrollPane1;
    private widget.ScrollPane scrollPane2;
    private widget.Table tbTujuan;
    private widget.Table tbUpdate;
    // End of variables declaration//GEN-END:variables
}
