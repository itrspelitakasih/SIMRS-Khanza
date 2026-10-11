package keuangan;

import fungsi.akses;
import fungsi.akuntindakanlaborat;
import fungsi.akuntindakanradiologi;
import fungsi.koneksiDB;
import fungsi.sekuel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Menyesuaikan pemeriksaan lab & radiologi pasien ke tarif kelas kamar inap, atau
 * mengembalikannya ke tarif tanpa kelas (Rawat Jalan / '-') bila pasien batal ranap.
 * Master jns_perawatan_lab / jns_perawatan_radiologi tidak diubah.
 *
 * Per pemeriksaan:
 * - dicari kode tarif dengan nm_perawatan yang sama untuk kelas tujuan,
 * - kd_jenis_prw diganti ke kode tujuan (beserta id_template hasil lab, data PA,
 *   dan permintaan/order), kecuali tidak aman (lihat alasanKodeTetap),
 * - nilai tarif diambil dari kode tujuan, status diubah ke Ranap / Ralan,
 * - dibuat jurnal pembalik sesuai status asal (nilai lama) dan jurnal status tujuan (nilai baru).
 * Pemeriksaan yang jasa medisnya sudah dibayar dilewati seluruhnya.
 */
public class UpdateTarifKelasLabRad {
    private final Connection koneksi=koneksiDB.condb();
    private final sekuel Sequel=new sekuel();
    private final Jurnal jur=new Jurnal();
    private final List<String> tidakDitemukan=new ArrayList<>(),kodeTetap=new ArrayList<>(),dilewati=new ArrayList<>(),
            templateDisalin=new ArrayList<>();
    private final Map<String,Boolean> adaTabel=new HashMap<>();
    private String kelas="",kdpj="",caraBayarLab="Yes",caraBayarRad="Yes";
    private int jmlLab=0,jmlRad=0,jmlKode=0;
    private boolean keRalan=false;
    private Pantau pantau;

    // id_template pada template kode tujuan yang namanya sama dengan item t1
    private static final String MAP_TEMPLATE=
        "(select min(x.id_template) from template_laboratorium x where x.kd_jenis_prw=? and x.Pemeriksaan=t1.Pemeriksaan)";
    // ke kelas: pemeriksaan Ralan, atau Ranap yang kodenya bukan kelas kamar terakhir (tanpa kelas,
    // atau kelas kamar sebelumnya saat pasien pindah kelas). Parameter: kelas kamar terakhir.
    private static final String FILTER_KE_KELAS="(p.status='Ralan' or j.kelas<>?)";
    // kembali tanpa kelas: semua pemeriksaan Ranap
    private static final String FILTER_KE_RALAN="p.status='Ranap'";
    private static final String KELAS_TANPA_KELAS="Rawat Jalan";

    public String getKelas(){return kelas;}
    public int getJmlLab(){return jmlLab;}
    public int getJmlRad(){return jmlRad;}
    public int getJmlKode(){return jmlKode;}
    public List<String> getTidakDitemukan(){return tidakDitemukan;}
    public List<String> getKodeTetap(){return kodeTetap;}
    public List<String> getDilewati(){return dilewati;}
    public List<String> getTemplateDisalin(){return templateDisalin;}

    /** Kelas kamar inap terakhir pasien, kosong jika belum masuk kamar. */
    public String cariKelas(String norawat){
        return Sequel.cariIsi(
            "select kamar.kelas from kamar inner join kamar_inap on kamar.kd_kamar=kamar_inap.kd_kamar where kamar_inap.no_rawat=? "+
            "order by STR_TO_DATE(concat(kamar_inap.tgl_masuk,' ',kamar_inap.jam_masuk),'%Y-%m-%d %H:%i:%s') desc limit 1",norawat);
    }

    /** Jumlah pemeriksaan yang akan diproses ke kelas kamar. */
    public int jumlahProses(String norawat){
        String kelaskamar=cariKelas(norawat);
        if(kelaskamar.equals("")){
            return 0;
        }
        return Sequel.cariInteger("select count(*) from periksa_lab p inner join jns_perawatan_lab j on j.kd_jenis_prw=p.kd_jenis_prw "+
                    "where p.no_rawat=? and "+FILTER_KE_KELAS,norawat,kelaskamar)+
               Sequel.cariInteger("select count(*) from periksa_radiologi p inner join jns_perawatan_radiologi j on j.kd_jenis_prw=p.kd_jenis_prw "+
                    "where p.no_rawat=? and "+FILTER_KE_KELAS,norawat,kelaskamar);
    }

    /** Jumlah pemeriksaan Ranap yang akan dikembalikan ke tanpa kelas. */
    public int jumlahRanap(String norawat){
        return Sequel.cariInteger("select count(*) from periksa_lab p where p.no_rawat=? and "+FILTER_KE_RALAN,norawat)+
               Sequel.cariInteger("select count(*) from periksa_radiologi p where p.no_rawat=? and "+FILTER_KE_RALAN,norawat);
    }

    /** Satu baris pemeriksaan yang akan diproses. jenis: PK, PA, MB atau RAD. */
    private static class Periksa {
        String jenis,kd,tgl,jam,nama,status;
        Periksa(String jenis,String kd,String tgl,String jam,String nama,String status){
            this.jenis=jenis;this.kd=kd;this.tgl=tgl;this.jam=jam;this.nama=nama;this.status=status;
        }
        boolean isRalan(){return status.equals("Ralan");}
        boolean isLab(){return !jenis.equals("RAD");}
        String label(){return (isLab()?"Lab ":"Radiologi ")+": "+nama+" ("+kd+", "+tgl+" "+jam+")";}
    }

    /** Sesuaikan ke tarif kelas kamar inap terakhir. Satu transaksi; true jika berhasil di-commit. */
    public boolean proses(String norawat){
        kelas=cariKelas(norawat);
        if(kelas.equals("")){
            return false;
        }
        keRalan=false;
        return jalankan(norawat);
    }

    /** Kembalikan pemeriksaan Ranap ke tarif tanpa kelas dan status Ralan (pasien batal ranap). */
    public boolean kembalikanTanpaKelas(String norawat){
        kelas=KELAS_TANPA_KELAS;
        keRalan=true;
        return jalankan(norawat);
    }

    /** Satu baris perbandingan tarif sekarang dengan tarif tujuan (untuk ditampilkan sebelum diproses). */
    public static class Banding {
        public String jenis="",waktu="",nama="",status="",kdSekarang="",kdTujuan="",keterangan="";
        public double tarifSekarang,tarifTujuan;
    }

    /**
     * Perkiraan hasil proses tanpa menyimpan apa pun: kode & tarif sekarang dibandingkan dengan
     * kode & tarif tujuan. keRalan=false ke kelas kamar inap, true kembali ke tarif tanpa kelas.
     */
    public List<Banding> bandingkan(String norawat,boolean keRalan){
        List<Banding> hasil=new ArrayList<>();
        this.keRalan=keRalan;
        kelas=keRalan?KELAS_TANPA_KELAS:cariKelas(norawat);
        if(kelas.equals("")){
            return hasil;
        }
        siapkanTarif(norawat);
        String statusBaru=keRalan?"Ralan":"Ranap";
        try {
            for(Periksa p:bacaDaftar(norawat)){
                Banding b=new Banding();
                b.jenis=p.isLab()?"Lab "+p.jenis:"Radiologi";
                b.waktu=p.tgl+" "+p.jam;
                b.nama=p.nama;
                b.status=p.status+" -> "+statusBaru;
                b.kdSekarang=p.kd;
                b.tarifSekarang=jumlahTarif(norawat,p)[0];
                b.kdTujuan=p.kd;
                b.tarifTujuan=b.tarifSekarang;
                if(sudahDibayar(norawat,p)){
                    b.status=p.status;
                    b.keterangan="Dilewati, jasa medis sudah dibayar";
                    hasil.add(b);
                    continue;
                }
                String kdbaru=p.isLab()?
                        cariTarif("jns_perawatan_lab",p.nama,caraBayarLab,"and jns_perawatan_lab.kategori='"+p.jenis+"' "):
                        cariTarif("jns_perawatan_radiologi",p.nama,caraBayarRad,"");
                if(kdbaru.equals("")){
                    if(p.status.equals(statusBaru)){
                        continue; // tidak ada yang berubah
                    }
                    b.keterangan="Tarif "+kelas+" tidak ditemukan, harga & kode tetap";
                }else if(kdbaru.equals(p.kd)){
                    b.tarifTujuan=perkiraanTarif(norawat,p,kdbaru,false);
                    b.keterangan="Kode sudah sesuai";
                }else{
                    boolean salin=perluSalinTemplate(p,kdbaru);
                    String alasan=alasanKodeTetap(norawat,p,kdbaru,salin);
                    if(alasan.equals("")){
                        b.kdTujuan=kdbaru;
                        b.tarifTujuan=perkiraanTarif(norawat,p,kdbaru,salin);
                        b.keterangan="Kode & tarif diganti"+(salin?", item template hasil lab disalin":"");
                    }else{
                        b.tarifTujuan=perkiraanTarif(norawat,p,kdbaru,false);
                        b.keterangan="Kode tetap ("+alasan+"), tarif disesuaikan ke "+kdbaru;
                    }
                }
                if(p.status.equals(statusBaru)&&b.kdTujuan.equals(b.kdSekarang)&&b.tarifTujuan==b.tarifSekarang){
                    continue; // tidak ada yang berubah
                }
                hasil.add(b);
            }
        } catch (Exception e) {
            System.out.println("Notifikasi Banding Tarif Kelas : "+e);
        }
        return hasil;
    }

    /** Total tarif setelah proses: tarif master kode tujuan + tarif item hasil lab yang terpetakan. */
    private double perkiraanTarif(String norawat,Periksa p,String kdbaru,boolean salin) throws Exception{
        String master=p.isLab()?"jns_perawatan_lab":"jns_perawatan_radiologi";
        double total;
        try (PreparedStatement ps=koneksi.prepareStatement("select ifnull(total_byr,0) from "+master+" where kd_jenis_prw=?")){
            ps.setString(1,kdbaru);
            try (ResultSet rs=ps.executeQuery()){
                total=rs.next()?rs.getDouble(1):0;
            }
        }
        if(p.isLab()){
            // item tanpa pasangan di template tujuan: tetap (atau ikut tarif item asal bila template akan disalin)
            try (PreparedStatement ps=koneksi.prepareStatement(
                    "select ifnull(sum(ifnull(t2.biaya_item,"+(salin?"t1.biaya_item":"d.biaya_item")+")),0) from detail_periksa_lab d "+
                    "inner join template_laboratorium t1 on t1.id_template=d.id_template "+
                    "left join template_laboratorium t2 on t2.id_template="+MAP_TEMPLATE+" "+
                    "where d.no_rawat=? and d.kd_jenis_prw=? and d.tgl_periksa=? and d.jam=?")){
                ps.setString(1,kdbaru);
                ps.setString(2,norawat);
                ps.setString(3,p.kd);
                ps.setString(4,p.tgl);
                ps.setString(5,p.jam);
                try (ResultSet rs=ps.executeQuery()){
                    if(rs.next()){
                        total+=rs.getDouble(1);
                    }
                }
            }
        }
        return total;
    }

    private void siapkanTarif(String norawat){
        kdpj=Sequel.cariIsi("select reg_periksa.kd_pj from reg_periksa where reg_periksa.no_rawat=?",norawat);
        caraBayarLab=Sequel.cariIsi("select set_tarif.cara_bayar_lab from set_tarif");
        caraBayarRad=Sequel.cariIsi("select set_tarif.cara_bayar_radiologi from set_tarif");
        if(caraBayarLab.equals("")){
            caraBayarLab="Yes";
        }
        if(caraBayarRad.equals("")){
            caraBayarRad="Yes";
        }
    }

    private List<Periksa> bacaDaftar(String norawat) throws Exception{
        String filter=keRalan?FILTER_KE_RALAN:FILTER_KE_KELAS;
        List<Periksa> daftar=new ArrayList<>();
        try (PreparedStatement ps=koneksi.prepareStatement(
                "select p.kategori,p.kd_jenis_prw,p.tgl_periksa,p.jam,j.nm_perawatan,p.status from periksa_lab p "+
                "inner join jns_perawatan_lab j on j.kd_jenis_prw=p.kd_jenis_prw where p.no_rawat=? and "+filter+
                " order by p.tgl_periksa,p.jam")){
            ps.setString(1,norawat);
            if(!keRalan){
                ps.setString(2,kelas);
            }
            try (ResultSet rs=ps.executeQuery()){
                while(rs.next()){
                    daftar.add(new Periksa(rs.getString(1),rs.getString(2),rs.getString(3),rs.getString(4),rs.getString(5),rs.getString(6)));
                }
            }
        }
        try (PreparedStatement ps=koneksi.prepareStatement(
                "select p.kd_jenis_prw,p.tgl_periksa,p.jam,j.nm_perawatan,p.status from periksa_radiologi p "+
                "inner join jns_perawatan_radiologi j on j.kd_jenis_prw=p.kd_jenis_prw where p.no_rawat=? and "+filter+
                " order by p.tgl_periksa,p.jam")){
            ps.setString(1,norawat);
            if(!keRalan){
                ps.setString(2,kelas);
            }
            try (ResultSet rs=ps.executeQuery()){
                while(rs.next()){
                    daftar.add(new Periksa("RAD",rs.getString(1),rs.getString(2),rs.getString(3),rs.getString(4),rs.getString(5)));
                }
            }
        }
        return daftar;
    }

    private boolean jalankan(String norawat){
        jmlLab=0;jmlRad=0;jmlKode=0;
        tidakDitemukan.clear();kodeTetap.clear();dilewati.clear();templateDisalin.clear();
        String statusBaru=keRalan?"Ralan":"Ranap";
        siapkanTarif(norawat);
        if(akuntindakanlaborat.getSuspen_Piutang_Laborat_Ralan().equals("")||akuntindakanlaborat.getSuspen_Piutang_Laborat_Ranap().equals("")){
            akuntindakanlaborat.SetAkunTindakanLaborat();
        }
        if(akuntindakanradiologi.getSuspen_Piutang_Radiologi_Ralan().equals("")||akuntindakanradiologi.getSuspen_Piutang_Radiologi_Ranap().equals("")){
            akuntindakanradiologi.SetAkunTindakanRadiologi();
        }

        boolean sukses=true;
        double[] lamaLabRalan=new double[8],lamaLabRanap=new double[8],baruLab=new double[8],
                 lamaRadRalan=new double[8],lamaRadRanap=new double[8],baruRad=new double[8];
        Sequel.AutoComitFalse();
        try {
            List<Periksa> daftar=bacaDaftar(norawat);
            log("Ditemukan "+daftar.size()+" pemeriksaan untuk diproses ke "+(keRalan?"tarif tanpa kelas (Ralan)":"tarif "+kelas)+".");
            int ke=0;
            progres(0,daftar.size());
            for(Periksa p:daftar){
                if(!sukses){
                    break;
                }
                ke++;
                log("");
                log("["+ke+"/"+daftar.size()+"] "+p.label()+" - status "+p.status);
                if(sudahDibayar(norawat,p)){
                    dilewati.add(p.label());
                    log("    Dilewati, jasa medis/perujuk sudah dibayar.");
                    progres(ke,daftar.size());
                    continue;
                }
                double[] lama=jumlahTarif(norawat,p);
                String kdlama=p.kd;
                String kdbaru=p.isLab()?
                        cariTarif("jns_perawatan_lab",p.nama,caraBayarLab,"and jns_perawatan_lab.kategori='"+p.jenis+"' "):
                        cariTarif("jns_perawatan_radiologi",p.nama,caraBayarRad,"");
                if(kdbaru.equals("")){
                    if(p.status.equals(statusBaru)){
                        log("    Tidak ada tarif "+kelas+", tidak ada perubahan.");
                        progres(ke,daftar.size());
                        continue; // status sudah sesuai dan memang tidak ada tarif tujuannya
                    }
                    tidakDitemukan.add(p.label());
                    log("    Tarif "+kelas+" tidak ditemukan, harga & kode tetap.");
                }else if(!kdbaru.equals(p.kd)){
                    log("    Kode tujuan : "+kdbaru);
                    if(!salinTemplate(p,kdbaru)){
                        log("    GAGAL menyalin item template ke "+kdbaru+".");
                        sukses=false;
                        break;
                    }
                    String alasan=alasanKodeTetap(norawat,p,kdbaru);
                    if(alasan.equals("")){
                        sukses=gantiKode(norawat,p,kdbaru);
                        if(sukses){
                            jmlKode++;
                            p.kd=kdbaru;
                            log("    Kode diganti "+kdlama+" -> "+kdbaru+".");
                        }
                    }else{
                        kodeTetap.add(p.label()+" -> "+alasan);
                        log("    Kode tetap ("+alasan+"), tarif disesuaikan.");
                        sukses=updateTarif(norawat,p,kdbaru);
                    }
                }else{
                    log("    Kode sudah sesuai, tarif disegarkan.");
                    sukses=updateTarif(norawat,p,kdbaru);
                }
                if(sukses){
                    sukses=Sequel.queryu2tf("update "+(p.isLab()?"periksa_lab":"periksa_radiologi")+" set status=? "+
                            "where no_rawat=? and kd_jenis_prw=? and tgl_periksa=? and jam=?",5,new String[]{statusBaru,norawat,p.kd,p.tgl,p.jam});
                }
                if(sukses){
                    double[] baru=jumlahTarif(norawat,p);
                    if(p.status.equals(statusBaru)&&p.kd.equals(kdlama)&&java.util.Arrays.equals(lama,baru)){
                        // mis. kode harus tetap & tarif sudah sama: tidak perlu jurnal koreksi bernilai nol
                        log("    Tidak ada perubahan tarif.");
                        progres(ke,daftar.size());
                        continue;
                    }
                    log("    Tarif "+rupiah(lama[0])+" -> "+rupiah(baru[0])+", status "+p.status+" -> "+statusBaru+".");
                    if(p.isLab()){
                        tambahKe(p.isRalan()?lamaLabRalan:lamaLabRanap,lama);tambahKe(baruLab,baru);jmlLab++;
                    }else{
                        tambahKe(p.isRalan()?lamaRadRalan:lamaRadRanap,lama);tambahKe(baruRad,baru);jmlRad++;
                    }
                }else{
                    log("    GAGAL memperbarui data pemeriksaan.");
                }
                progres(ke,daftar.size());
            }

            // ===== Jurnal: balik nilai lama sesuai status asal, posting nilai baru sesuai status tujuan =====
            if(sukses){
                log("");
                log("Membuat jurnal koreksi...");
            }
            String oleh=" OLEH "+akses.getkode(),tujuan=keRalan?"RAWAT JALAN (BATAL RAWAT INAP)":"RAWAT INAP "+kelas.toUpperCase();
            if(sukses&&adaNilai(lamaLabRalan)){
                sukses=posting(akunLabRalan(),lamaLabRalan,true,norawat,"KOREKSI PEMERIKSAAN LABORAT RAWAT JALAN KE "+tujuan+oleh);
            }
            if(sukses&&adaNilai(lamaLabRanap)){
                sukses=posting(akunLabRanap(),lamaLabRanap,true,norawat,"KOREKSI PEMERIKSAAN LABORAT RAWAT INAP KE "+tujuan+oleh);
            }
            if(sukses&&adaNilai(baruLab)){
                sukses=posting(keRalan?akunLabRalan():akunLabRanap(),baruLab,false,norawat,"PEMERIKSAAN LABORAT "+tujuan+" (PENYESUAIAN TARIF)"+oleh);
            }
            if(sukses&&adaNilai(lamaRadRalan)){
                sukses=posting(akunRadRalan(),lamaRadRalan,true,norawat,"KOREKSI PEMERIKSAAN RADIOLOGI RAWAT JALAN KE "+tujuan+oleh);
            }
            if(sukses&&adaNilai(lamaRadRanap)){
                sukses=posting(akunRadRanap(),lamaRadRanap,true,norawat,"KOREKSI PEMERIKSAAN RADIOLOGI RAWAT INAP KE "+tujuan+oleh);
            }
            if(sukses&&adaNilai(baruRad)){
                sukses=posting(keRalan?akunRadRalan():akunRadRanap(),baruRad,false,norawat,"PEMERIKSAAN RADIOLOGI "+tujuan+" (PENYESUAIAN TARIF)"+oleh);
            }
        } catch (Exception e) {
            System.out.println("Notifikasi Update Tarif Kelas : "+e);
            log("ERROR : "+e.getMessage());
            sukses=false;
        }

        log("");
        if(sukses){
            Sequel.Commit();
            log("SELESAI. Semua perubahan disimpan.");
        }else{
            Sequel.RollBack();
            log("GAGAL. Semua perubahan dibatalkan (rollback), data kembali seperti semula.");
        }
        Sequel.AutoComitTrue();
        return sukses;
    }

    /** Penerima laporan jalannya proses, dipanggil dari thread proses. */
    public interface Pantau {
        void tulis(String pesan);
        void progres(int ke,int total);
    }

    public void setPantau(Pantau pantau){
        this.pantau=pantau;
    }

    private void log(String pesan){
        if(pantau!=null){
            pantau.tulis(pesan);
        }
    }

    private void progres(int ke,int total){
        if(pantau!=null){
            pantau.progres(ke,total);
        }
    }

    private String rupiah(double nilai){
        return String.format("Rp %,.0f",nilai);
    }

    /** Jasa medis/perujuk yang sudah dibayar tidak boleh diubah nilainya. */
    private boolean sudahDibayar(String norawat,Periksa p) throws Exception{
        String[] tabel=p.isLab()?
                new String[]{"bayar_periksa_lab","bayar_periksa_lab_perujuk","bayar_detail_periksa_lab","bayar_detail_periksa_lab_perujuk"}:
                new String[]{"bayar_periksa_radiologi","bayar_periksa_radiologi_perujuk"};
        for(String t:tabel){
            if(adaTabel(t)&&hitung("select count(*) from "+t+" where no_rawat=? and kd_jenis_prw=? and tgl_periksa=? and jam=?",
                    norawat,p.kd,p.tgl,p.jam)>0){
                return true;
            }
        }
        return false;
    }

    /**
     * Kode tarif kelas umumnya dibuat tanpa item template hasil lab. Jika kode tujuan belum punya
     * item sama sekali, item disalin dari kode asal (nama item, satuan, nilai rujukan, tarif item,
     * urutan) agar hasil lab bisa ikut dipindah. Kode yang sudah punya item tidak disentuh.
     */
    private boolean salinTemplate(Periksa p,String kdbaru) throws Exception{
        if(!perluSalinTemplate(p,kdbaru)){
            return true;
        }
        boolean ok=Sequel.queryu2tf(
            "insert into template_laboratorium (kd_jenis_prw,Pemeriksaan,satuan,nilai_rujukan_ld,nilai_rujukan_la,nilai_rujukan_pd,"+
            "nilai_rujukan_pa,bagian_rs,bhp,bagian_perujuk,bagian_dokter,bagian_laborat,kso,menejemen,biaya_item,urut) "+
            "select ?,Pemeriksaan,satuan,nilai_rujukan_ld,nilai_rujukan_la,nilai_rujukan_pd,nilai_rujukan_pa,bagian_rs,bhp,"+
            "bagian_perujuk,bagian_dokter,bagian_laborat,kso,menejemen,biaya_item,urut from template_laboratorium "+
            "where kd_jenis_prw=? order by id_template",2,new String[]{kdbaru,p.kd});
        if(ok){
            templateDisalin.add(p.kd+" -> "+kdbaru);
            log("    Kode "+kdbaru+" belum punya item hasil lab, item template disalin dari "+p.kd+".");
        }
        return ok;
    }

    /** Kode tujuan lab PK/MB belum punya item template sama sekali, sedangkan kode asal punya. */
    private boolean perluSalinTemplate(Periksa p,String kdbaru) throws Exception{
        return (p.jenis.equals("PK")||p.jenis.equals("MB"))&&
                hitung("select count(*) from template_laboratorium where kd_jenis_prw=?",kdbaru)==0&&
                hitung("select count(*) from template_laboratorium where kd_jenis_prw=?",p.kd)>0;
    }

    /** Kosong jika kode aman diganti, selain itu alasan kode dipertahankan. */
    private String alasanKodeTetap(String norawat,Periksa p,String kdbaru) throws Exception{
        return alasanKodeTetap(norawat,p,kdbaru,false);
    }

    /** templateAkanDisalin=true: cek kecocokan item dilewati karena item tujuan disalin dari kode asal. */
    private String alasanKodeTetap(String norawat,Periksa p,String kdbaru,boolean templateAkanDisalin) throws Exception{
        String tabelPeriksa=p.isLab()?"periksa_lab":"periksa_radiologi";
        if(hitung("select count(*) from "+tabelPeriksa+" where no_rawat=? and kd_jenis_prw=? and tgl_periksa=? and jam=?",
                norawat,kdbaru,p.tgl,p.jam)>0){
            return "kode "+kdbaru+" sudah ada di waktu periksa yang sama";
        }
        if(p.isLab()&&!templateAkanDisalin&&!templateCocok(
                "from detail_periksa_lab d inner join template_laboratorium t1 on t1.id_template=d.id_template "+
                "where d.no_rawat=? and d.kd_jenis_prw=? and d.tgl_periksa=? and d.jam=?",kdbaru,norawat,p.kd,p.tgl,p.jam)){
            return "item hasil lab tidak semua ada di template "+kdbaru;
        }
        if(p.jenis.equals("RAD")&&adaTabel("satu_sehat_imagingstudy")&&hitung(
                "select count(*) from satu_sehat_imagingstudy where no_rawat=? and kd_jenis_prw=? and tgl_periksa=? and jam=?",
                norawat,p.kd,p.tgl,p.jam)>0){
            return "sudah dikirim ke SatuSehat";
        }
        for(String noorder:cariOrder(norawat,p)){
            if(hitung("select count(*) from "+tabelPemeriksaanOrder(p)+" where noorder=? and kd_jenis_prw=?",noorder,kdbaru)>0){
                return "kode "+kdbaru+" sudah ada di permintaan "+noorder;
            }
            String detail=tabelDetailOrder(p);
            if(detail!=null&&!templateAkanDisalin&&!templateCocok(
                    "from "+detail+" d inner join template_laboratorium t1 on t1.id_template=d.id_template "+
                    "where d.noorder=? and d.kd_jenis_prw=?",kdbaru,noorder,p.kd)){
                return "item permintaan "+noorder+" tidak semua ada di template "+kdbaru;
            }
            for(String t:tabelSatuSehat(p)){
                if(adaTabel(t)&&hitung("select count(*) from "+t+" where noorder=? and kd_jenis_prw=?",noorder,p.kd)>0){
                    return "sudah dikirim ke SatuSehat";
                }
            }
        }
        return "";
    }

    /** Ganti kd_jenis_prw beserta tarif, id_template hasil, data PA dan permintaan. */
    private boolean gantiKode(String norawat,Periksa p,String kdbaru) throws Exception{
        boolean ok;
        String[] kunci={norawat,p.kd,p.tgl,p.jam};
        if(p.isLab()){
            ok=Sequel.queryu2tf(
                "update periksa_lab p inner join jns_perawatan_lab j on j.kd_jenis_prw=? set p.kd_jenis_prw=j.kd_jenis_prw,"+
                "p.bagian_rs=j.bagian_rs,p.bhp=j.bhp,p.tarif_perujuk=j.tarif_perujuk,p.tarif_tindakan_dokter=j.tarif_tindakan_dokter,"+
                "p.tarif_tindakan_petugas=j.tarif_tindakan_petugas,p.kso=j.kso,p.menejemen=j.menejemen,p.biaya=j.total_byr "+
                "where p.no_rawat=? and p.kd_jenis_prw=? and p.tgl_periksa=? and p.jam=?",5,gabung(kdbaru,kunci));
            ok=ok&&Sequel.queryu2tf(
                "update detail_periksa_lab d inner join template_laboratorium t1 on t1.id_template=d.id_template "+
                "inner join template_laboratorium t2 on t2.id_template="+MAP_TEMPLATE+" "+
                "set d.kd_jenis_prw=t2.kd_jenis_prw,d.id_template=t2.id_template,d.bagian_rs=t2.bagian_rs,d.bhp=t2.bhp,"+
                "d.bagian_perujuk=t2.bagian_perujuk,d.bagian_dokter=t2.bagian_dokter,d.bagian_laborat=t2.bagian_laborat,"+
                "d.kso=t2.kso,d.menejemen=t2.menejemen,d.biaya_item=t2.biaya_item "+
                "where d.no_rawat=? and d.kd_jenis_prw=? and d.tgl_periksa=? and d.jam=?",5,gabung(kdbaru,kunci));
            if(p.jenis.equals("PA")){
                ok=ok&&Sequel.queryu2tf("update detail_periksa_labpa set kd_jenis_prw=? where no_rawat=? and kd_jenis_prw=? and tgl_periksa=? and jam=?",
                        5,gabung(kdbaru,kunci));
                ok=ok&&Sequel.queryu2tf("update detail_periksa_labpa_gambar set kd_jenis_prw=? where no_rawat=? and kd_jenis_prw=? and tgl_periksa=? and jam=?",
                        5,gabung(kdbaru,kunci));
            }
        }else{
            ok=Sequel.queryu2tf(
                "update periksa_radiologi p inner join jns_perawatan_radiologi j on j.kd_jenis_prw=? set p.kd_jenis_prw=j.kd_jenis_prw,"+
                "p.bagian_rs=j.bagian_rs,p.bhp=j.bhp,p.tarif_perujuk=j.tarif_perujuk,p.tarif_tindakan_dokter=j.tarif_tindakan_dokter,"+
                "p.tarif_tindakan_petugas=j.tarif_tindakan_petugas,p.kso=j.kso,p.menejemen=j.menejemen,p.biaya=j.total_byr "+
                "where p.no_rawat=? and p.kd_jenis_prw=? and p.tgl_periksa=? and p.jam=?",5,gabung(kdbaru,kunci));
        }
        for(String noorder:cariOrder(norawat,p)){
            if(!ok){
                break;
            }
            String detail=tabelDetailOrder(p);
            if(detail!=null){
                ok=Sequel.queryu2tf(
                    "update "+detail+" d inner join template_laboratorium t1 on t1.id_template=d.id_template "+
                    "inner join template_laboratorium t2 on t2.id_template="+MAP_TEMPLATE+" "+
                    "set d.kd_jenis_prw=t2.kd_jenis_prw,d.id_template=t2.id_template where d.noorder=? and d.kd_jenis_prw=?",
                    3,new String[]{kdbaru,noorder,p.kd});
            }
            ok=ok&&Sequel.queryu2tf("update "+tabelPemeriksaanOrder(p)+" set kd_jenis_prw=? where noorder=? and kd_jenis_prw=?",
                    3,new String[]{kdbaru,noorder,p.kd});
        }
        return ok;
    }

    /** Hanya nilai tarif yang disesuaikan, kode tetap. */
    private boolean updateTarif(String norawat,Periksa p,String kdbaru){
        String[] param=gabung(kdbaru,new String[]{norawat,p.kd,p.tgl,p.jam});
        if(!p.isLab()){
            return Sequel.queryu2tf(
                "update periksa_radiologi p inner join jns_perawatan_radiologi j on j.kd_jenis_prw=? set p.bagian_rs=j.bagian_rs,p.bhp=j.bhp,"+
                "p.tarif_perujuk=j.tarif_perujuk,p.tarif_tindakan_dokter=j.tarif_tindakan_dokter,p.tarif_tindakan_petugas=j.tarif_tindakan_petugas,"+
                "p.kso=j.kso,p.menejemen=j.menejemen,p.biaya=j.total_byr where p.no_rawat=? and p.kd_jenis_prw=? and p.tgl_periksa=? and p.jam=?",5,param);
        }
        return Sequel.queryu2tf(
                "update periksa_lab p inner join jns_perawatan_lab j on j.kd_jenis_prw=? set p.bagian_rs=j.bagian_rs,p.bhp=j.bhp,"+
                "p.tarif_perujuk=j.tarif_perujuk,p.tarif_tindakan_dokter=j.tarif_tindakan_dokter,p.tarif_tindakan_petugas=j.tarif_tindakan_petugas,"+
                "p.kso=j.kso,p.menejemen=j.menejemen,p.biaya=j.total_byr where p.no_rawat=? and p.kd_jenis_prw=? and p.tgl_periksa=? and p.jam=?",5,param)&&
            Sequel.queryu2tf(
                "update detail_periksa_lab d inner join template_laboratorium t1 on t1.id_template=d.id_template "+
                "inner join template_laboratorium t2 on t2.id_template="+MAP_TEMPLATE+" "+
                "set d.bagian_rs=t2.bagian_rs,d.bhp=t2.bhp,d.bagian_perujuk=t2.bagian_perujuk,d.bagian_dokter=t2.bagian_dokter,"+
                "d.bagian_laborat=t2.bagian_laborat,d.kso=t2.kso,d.menejemen=t2.menejemen,d.biaya_item=t2.biaya_item "+
                "where d.no_rawat=? and d.kd_jenis_prw=? and d.tgl_periksa=? and d.jam=?",5,param);
    }

    /** Setiap item harus punya pasangan nama di template kode tujuan, dan pasangannya tidak boleh dobel. */
    private boolean templateCocok(String fromWhere,String kdbaru,String... param) throws Exception{
        try (PreparedStatement ps=koneksi.prepareStatement(
                "select count(*),count(m),count(distinct m) from (select "+MAP_TEMPLATE+" m "+fromWhere+") z")){
            ps.setString(1,kdbaru);
            for(int i=0;i<param.length;i++){
                ps.setString(i+2,param[i]);
            }
            try (ResultSet rs=ps.executeQuery()){
                rs.next();
                return rs.getInt(1)==rs.getInt(2)&&rs.getInt(2)==rs.getInt(3);
            }
        }
    }

    /** No. order permintaan yang hasilnya adalah pemeriksaan ini. */
    private List<String> cariOrder(String norawat,Periksa p) throws Exception{
        String tabel=p.jenis.equals("RAD")?"permintaan_radiologi":p.jenis.equals("PA")?"permintaan_labpa":p.jenis.equals("MB")?"permintaan_labmb":"permintaan_lab";
        List<String> hasil=new ArrayList<>();
        if(!adaTabel(tabel)){
            return hasil;
        }
        try (PreparedStatement ps=koneksi.prepareStatement(
                "select o.noorder from "+tabel+" o inner join "+tabelPemeriksaanOrder(p)+" d on d.noorder=o.noorder "+
                "where o.no_rawat=? and o.tgl_hasil=? and o.jam_hasil=? and d.kd_jenis_prw=?")){
            ps.setString(1,norawat);
            ps.setString(2,p.tgl);
            ps.setString(3,p.jam);
            ps.setString(4,p.kd);
            try (ResultSet rs=ps.executeQuery()){
                while(rs.next()){
                    hasil.add(rs.getString(1));
                }
            }
        }
        return hasil;
    }

    private String tabelPemeriksaanOrder(Periksa p){
        return p.jenis.equals("RAD")?"permintaan_pemeriksaan_radiologi":p.jenis.equals("PA")?"permintaan_pemeriksaan_labpa":
               p.jenis.equals("MB")?"permintaan_pemeriksaan_labmb":"permintaan_pemeriksaan_lab";
    }

    private String tabelDetailOrder(Periksa p){
        return p.jenis.equals("PK")?"permintaan_detail_permintaan_lab":p.jenis.equals("MB")?"permintaan_detail_permintaan_labmb":null;
    }

    private String[] tabelSatuSehat(Periksa p){
        if(p.jenis.equals("RAD")){
            return new String[]{"satu_sehat_servicerequest_radiologi","satu_sehat_specimen_radiologi","satu_sehat_observation_radiologi","satu_sehat_diagnosticreport_radiologi"};
        }else if(p.jenis.equals("PK")){
            return new String[]{"satu_sehat_servicerequest_lab","satu_sehat_specimen_lab","satu_sehat_observation_lab","satu_sehat_diagnosticreport_lab"};
        }else if(p.jenis.equals("MB")){
            return new String[]{"satu_sehat_servicerequest_lab_mb","satu_sehat_specimen_lab_mb","satu_sehat_observation_lab_mb","satu_sehat_diagnosticreport_lab_mb"};
        }
        return new String[]{};
    }

    /** Total tarif satu pemeriksaan (header + item detail lab), urutan sesuai akun jurnal. */
    private double[] jumlahTarif(String norawat,Periksa p) throws Exception{
        double[] hasil=jumlah(
            "select ifnull(sum(biaya),0),ifnull(sum(tarif_tindakan_dokter),0),ifnull(sum(tarif_tindakan_petugas),0),ifnull(sum(bhp),0),"+
            "ifnull(sum(kso),0),ifnull(sum(bagian_rs),0),ifnull(sum(tarif_perujuk),0),ifnull(sum(menejemen),0) from "+
            (p.isLab()?"periksa_lab":"periksa_radiologi")+" where no_rawat=? and kd_jenis_prw=? and tgl_periksa=? and jam=?",norawat,p);
        if(p.isLab()){
            tambahKe(hasil,jumlah(
                "select ifnull(sum(biaya_item),0),ifnull(sum(bagian_dokter),0),ifnull(sum(bagian_laborat),0),ifnull(sum(bhp),0),"+
                "ifnull(sum(kso),0),ifnull(sum(bagian_rs),0),ifnull(sum(bagian_perujuk),0),ifnull(sum(menejemen),0) from detail_periksa_lab "+
                "where no_rawat=? and kd_jenis_prw=? and tgl_periksa=? and jam=?",norawat,p));
        }
        return hasil;
    }

    private double[] jumlah(String sql,String norawat,Periksa p) throws Exception{
        double[] hasil=new double[8];
        try (PreparedStatement ps=koneksi.prepareStatement(sql)){
            ps.setString(1,norawat);
            ps.setString(2,p.kd);
            ps.setString(3,p.tgl);
            ps.setString(4,p.jam);
            try (ResultSet rs=ps.executeQuery()){
                if(rs.next()){
                    for(int i=0;i<8;i++){
                        hasil[i]=rs.getDouble(i+1);
                    }
                }
            }
        }
        return hasil;
    }

    private void tambahKe(double[] tujuan,double[] nilai){
        for(int i=0;i<8;i++){
            tujuan[i]+=nilai[i];
        }
    }

    private int hitung(String sql,String... param) throws Exception{
        try (PreparedStatement ps=koneksi.prepareStatement(sql)){
            for(int i=0;i<param.length;i++){
                ps.setString(i+1,param[i]);
            }
            try (ResultSet rs=ps.executeQuery()){
                return rs.next()?rs.getInt(1):0;
            }
        }
    }

    /** Beberapa tabel (SatuSehat, PA/MB, bayar perujuk) belum tentu ada di semua versi database. */
    private boolean adaTabel(String tabel) throws Exception{
        Boolean ada=adaTabel.get(tabel);
        if(ada==null){
            ada=hitung("select count(*) from information_schema.tables where table_schema=database() and table_name=?",tabel)>0;
            adaTabel.put(tabel,ada);
        }
        return ada;
    }

    private String[] gabung(String awal,String[] sisa){
        String[] hasil=new String[sisa.length+1];
        hasil[0]=awal;
        System.arraycopy(sisa,0,hasil,1,sisa.length);
        return hasil;
    }

    /**
     * Kode tarif dengan nama sama untuk kelas tujuan; utamakan cara bayar yang persis.
     * Ke kelas kamar: kelas harus persis sama. Kembali tanpa kelas: kelas 'Rawat Jalan' atau '-'
     * (sama seperti pilihan tarif saat input lab/radiologi Ralan), utamakan 'Rawat Jalan'.
     */
    private String cariTarif(String tabel,String nama,String caraBayar,String filterTambahan){
        String filterPj=caraBayar.equals("Yes")?"and ("+tabel+".kd_pj=? or "+tabel+".kd_pj='-') ":"";
        String filterKelas=keRalan?"and ("+tabel+".kelas=? or "+tabel+".kelas='-') ":"and "+tabel+".kelas=? ";
        String sql="select "+tabel+".kd_jenis_prw from "+tabel+" where "+tabel+".nm_perawatan=? and "+tabel+".status='1' "+
                   filterKelas+filterPj+filterTambahan+
                   "order by ("+tabel+".kd_pj=?) desc,("+tabel+".kelas='"+KELAS_TANPA_KELAS+"') desc limit 1";
        try (PreparedStatement ps=koneksi.prepareStatement(sql)){
            int i=1;
            ps.setString(i++,nama);
            ps.setString(i++,kelas);
            if(caraBayar.equals("Yes")){
                ps.setString(i++,kdpj);
            }
            ps.setString(i++,kdpj);
            try (ResultSet rs=ps.executeQuery()){
                if(rs.next()){
                    return rs.getString(1);
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi Cari Tarif Kelas : "+e);
        }
        return "";
    }

    private boolean adaNilai(double[] nilai){
        for(double n:nilai){
            if(n>0){
                return true;
            }
        }
        return false;
    }

    /**
     * akun[i] = {rek debet, nama debet, rek kredit, nama kredit} saat posting normal,
     * dengan urutan i sama seperti urutan nilai total. balik=true menukar posisi debet/kredit.
     */
    private boolean posting(String[][] akun,double[] nilai,boolean balik,String norawat,String keterangan){
        boolean ok=Sequel.queryutf("delete from tampjurnal");
        for(int i=0;i<akun.length&&ok;i++){
            if(nilai[i]>0){
                ok=simpanTamp(akun[i][0],akun[i][1],nilai[i],!balik)&&simpanTamp(akun[i][2],akun[i][3],nilai[i],balik);
            }
        }
        if(ok){
            ok=jur.simpanJurnal(norawat,"U",keterangan);
        }
        log("    "+(ok?"Jurnal : ":"GAGAL jurnal : ")+keterangan+" ("+rupiah(nilai[0])+")");
        return ok;
    }

    private boolean simpanTamp(String rek,String nama,double nilai,boolean debet){
        if(debet){
            return Sequel.menyimpantf("tampjurnal","'"+rek+"','"+nama+"','"+nilai+"','0'","debet=debet+'"+nilai+"'","kd_rek='"+rek+"'");
        }
        return Sequel.menyimpantf("tampjurnal","'"+rek+"','"+nama+"','0','"+nilai+"'","kredit=kredit+'"+nilai+"'","kd_rek='"+rek+"'");
    }

    private String[][] akunLabRanap(){
        return new String[][]{
            {akuntindakanlaborat.getSuspen_Piutang_Laborat_Ranap(),"Suspen Piutang Laborat Ranap",akuntindakanlaborat.getLaborat_Ranap(),"Pendapatan Laborat Rawat Inap"},
            {akuntindakanlaborat.getBeban_Jasa_Medik_Dokter_Laborat_Ranap(),"Beban Jasa Medik Dokter Laborat Ranap",akuntindakanlaborat.getUtang_Jasa_Medik_Dokter_Laborat_Ranap(),"Utang Jasa Medik Dokter Laborat Ranap"},
            {akuntindakanlaborat.getBeban_Jasa_Medik_Petugas_Laborat_Ranap(),"Beban Jasa Medik Petugas Laborat Ranap",akuntindakanlaborat.getUtang_Jasa_Medik_Petugas_Laborat_Ranap(),"Utang Jasa Medik Petugas Laborat Ranap"},
            {akuntindakanlaborat.getHPP_Persediaan_Laborat_Rawat_inap(),"HPP Persediaan Laborat Rawat Inap",akuntindakanlaborat.getPersediaan_BHP_Laborat_Rawat_Inap(),"Persediaan BHP Laborat Rawat Inap"},
            {akuntindakanlaborat.getBeban_Kso_Laborat_Ranap(),"Beban KSO Laborat Ranap",akuntindakanlaborat.getUtang_Kso_Laborat_Ranap(),"Utang KSO Laborat Ranap"},
            {akuntindakanlaborat.getBeban_Jasa_Sarana_Laborat_Ranap(),"Beban Jasa Sarana Laborat Ranap",akuntindakanlaborat.getUtang_Jasa_Sarana_Laborat_Ranap(),"Utang Jasa Sarana Laborat Ranap"},
            {akuntindakanlaborat.getBeban_Jasa_Perujuk_Laborat_Ranap(),"Beban Jasa Perujuk Laborat Ranap",akuntindakanlaborat.getUtang_Jasa_Perujuk_Laborat_Ranap(),"Utang Jasa Perujuk Laborat Ranap"},
            {akuntindakanlaborat.getBeban_Jasa_Menejemen_Laborat_Ranap(),"Beban Jasa Menejemen Laborat Ranap",akuntindakanlaborat.getUtang_Jasa_Menejemen_Laborat_Ranap(),"Utang Jasa Menejemen Laborat Ranap"}
        };
    }

    private String[][] akunLabRalan(){
        return new String[][]{
            {akuntindakanlaborat.getSuspen_Piutang_Laborat_Ralan(),"Suspen Piutang Laborat Ralan",akuntindakanlaborat.getLaborat_Ralan(),"Pendapatan Laborat Rawat Jalan"},
            {akuntindakanlaborat.getBeban_Jasa_Medik_Dokter_Laborat_Ralan(),"Beban Jasa Medik Dokter Laborat Ralan",akuntindakanlaborat.getUtang_Jasa_Medik_Dokter_Laborat_Ralan(),"Utang Jasa Medik Dokter Laborat Ralan"},
            {akuntindakanlaborat.getBeban_Jasa_Medik_Petugas_Laborat_Ralan(),"Beban Jasa Medik Petugas Laborat Ralan",akuntindakanlaborat.getUtang_Jasa_Medik_Petugas_Laborat_Ralan(),"Utang Jasa Medik Petugas Laborat Ralan"},
            {akuntindakanlaborat.getHPP_Persediaan_Laborat_Rawat_Jalan(),"HPP Persediaan Laborat Rawat Jalan",akuntindakanlaborat.getPersediaan_BHP_Laborat_Rawat_Jalan(),"Persediaan BHP Laborat Rawat Jalan"},
            {akuntindakanlaborat.getBeban_Kso_Laborat_Ralan(),"Beban KSO Laborat Ralan",akuntindakanlaborat.getUtang_Kso_Laborat_Ralan(),"Utang KSO Laborat Ralan"},
            {akuntindakanlaborat.getBeban_Jasa_Sarana_Laborat_Ralan(),"Beban Jasa Sarana Laborat Ralan",akuntindakanlaborat.getUtang_Jasa_Sarana_Laborat_Ralan(),"Utang Jasa Sarana Laborat Ralan"},
            {akuntindakanlaborat.getBeban_Jasa_Perujuk_Laborat_Ralan(),"Beban Jasa Perujuk Laborat Ralan",akuntindakanlaborat.getUtang_Jasa_Perujuk_Laborat_Ralan(),"Utang Jasa Perujuk Laborat Ralan"},
            {akuntindakanlaborat.getBeban_Jasa_Menejemen_Laborat_Ralan(),"Beban Jasa Menejemen Laborat Ralan",akuntindakanlaborat.getUtang_Jasa_Menejemen_Laborat_Ralan(),"Utang Jasa Menejemen Laborat Ralan"}
        };
    }

    private String[][] akunRadRanap(){
        return new String[][]{
            {akuntindakanradiologi.getSuspen_Piutang_Radiologi_Ranap(),"Suspen Piutang Radiologi Ranap",akuntindakanradiologi.getRadiologi_Ranap(),"Pendapatan Radiologi Rawat Inap"},
            {akuntindakanradiologi.getBeban_Jasa_Medik_Dokter_Radiologi_Ranap(),"Beban Jasa Medik Dokter Radiologi Ranap",akuntindakanradiologi.getUtang_Jasa_Medik_Dokter_Radiologi_Ranap(),"Utang Jasa Medik Dokter Radiologi Ranap"},
            {akuntindakanradiologi.getBeban_Jasa_Medik_Petugas_Radiologi_Ranap(),"Beban Jasa Medik Petugas Radiologi Ranap",akuntindakanradiologi.getUtang_Jasa_Medik_Petugas_Radiologi_Ranap(),"Utang Jasa Medik Petugas Radiologi Ranap"},
            {akuntindakanradiologi.getHPP_Persediaan_Radiologi_Rawat_Inap(),"HPP Persediaan Radiologi Rawat Inap",akuntindakanradiologi.getPersediaan_BHP_Radiologi_Rawat_Inap(),"Persediaan BHP Radiologi Rawat Inap"},
            {akuntindakanradiologi.getBeban_Kso_Radiologi_Ranap(),"Beban KSO Radiologi Ranap",akuntindakanradiologi.getUtang_Kso_Radiologi_Ranap(),"Utang KSO Radiologi Ranap"},
            {akuntindakanradiologi.getBeban_Jasa_Sarana_Radiologi_Ranap(),"Beban Jasa Sarana Radiologi Ranap",akuntindakanradiologi.getUtang_Jasa_Sarana_Radiologi_Ranap(),"Utang Jasa Sarana Radiologi Ranap"},
            {akuntindakanradiologi.getBeban_Jasa_Perujuk_Radiologi_Ranap(),"Beban Jasa Perujuk Radiologi Ranap",akuntindakanradiologi.getUtang_Jasa_Perujuk_Radiologi_Ranap(),"Utang Jasa Perujuk Radiologi Ranap"},
            {akuntindakanradiologi.getBeban_Jasa_Menejemen_Radiologi_Ranap(),"Beban Jasa Menejemen Radiologi Ranap",akuntindakanradiologi.getUtang_Jasa_Menejemen_Radiologi_Ranap(),"Utang Jasa Menejemen Radiologi Ranap"}
        };
    }

    private String[][] akunRadRalan(){
        return new String[][]{
            {akuntindakanradiologi.getSuspen_Piutang_Radiologi_Ralan(),"Suspen Piutang Radiologi Ralan",akuntindakanradiologi.getRadiologi_Ralan(),"Pendapatan Radiologi Rawat Jalan"},
            {akuntindakanradiologi.getBeban_Jasa_Medik_Dokter_Radiologi_Ralan(),"Beban Jasa Medik Dokter Radiologi Ralan",akuntindakanradiologi.getUtang_Jasa_Medik_Dokter_Radiologi_Ralan(),"Utang Jasa Medik Dokter Radiologi Ralan"},
            {akuntindakanradiologi.getBeban_Jasa_Medik_Petugas_Radiologi_Ralan(),"Beban Jasa Medik Petugas Radiologi Ralan",akuntindakanradiologi.getUtang_Jasa_Medik_Petugas_Radiologi_Ralan(),"Utang Jasa Medik Petugas Radiologi Ralan"},
            {akuntindakanradiologi.getHPP_Persediaan_Radiologi_Rawat_Jalan(),"HPP Persediaan Radiologi Rawat Jalan",akuntindakanradiologi.getPersediaan_BHP_Radiologi_Rawat_Jalan(),"Persediaan BHP Radiologi Rawat Jalan"},
            {akuntindakanradiologi.getBeban_Kso_Radiologi_Ralan(),"Beban KSO Radiologi Ralan",akuntindakanradiologi.getUtang_Kso_Radiologi_Ralan(),"Utang KSO Radiologi Ralan"},
            {akuntindakanradiologi.getBeban_Jasa_Sarana_Radiologi_Ralan(),"Beban Jasa Sarana Radiologi Ralan",akuntindakanradiologi.getUtang_Jasa_Sarana_Radiologi_Ralan(),"Utang Jasa Sarana Radiologi Ralan"},
            {akuntindakanradiologi.getBeban_Jasa_Perujuk_Radiologi_Ralan(),"Beban Jasa Perujuk Radiologi Ralan",akuntindakanradiologi.getUtang_Jasa_Perujuk_Radiologi_Ralan(),"Utang Jasa Perujuk Radiologi Ralan"},
            {akuntindakanradiologi.getBeban_Jasa_Menejemen_Radiologi_Ralan(),"Beban Jasa Menejemen Radiologi Ralan",akuntindakanradiologi.getUtang_Jasa_Menejemen_Radiologi_Ralan(),"Utang Jasa Menejemen Radiologi Ralan"}
        };
    }
}
