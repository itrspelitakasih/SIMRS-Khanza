package rekammedis;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.HashMap;
import java.util.Map;
import javax.swing.text.JTextComponent;

/**
 * Pengambil data otomatis untuk tombol "Cari" pada resume pasien ralan dan ranap:
 * skrining, pengkajian awal medis/keperawatan, SOAP, radiologi dan laboratorium.
 */
final class ResumeOtomatis {
    static final String[] AWAL_MEDIS_RALAN = {
        "penilaian_medis_ralan","penilaian_medis_ralan_penyakit_dalam","penilaian_medis_ralan_anak",
        "penilaian_medis_ralan_kandungan","penilaian_medis_ralan_mata","penilaian_medis_ralan_tht",
        "penilaian_medis_ralan_bedah","penilaian_medis_ralan_bedah_mulut","penilaian_medis_ralan_jantung",
        "penilaian_medis_ralan_paru","penilaian_medis_ralan_neurologi","penilaian_medis_ralan_orthopedi",
        "penilaian_medis_ralan_kulitdankelamin","penilaian_medis_ralan_psikiatrik","penilaian_medis_ralan_rehab_medik",
        "penilaian_medis_ralan_urologi","penilaian_medis_ralan_geriatri","penilaian_medis_ralan_gawat_darurat_psikiatri",
        "penilaian_medis_igd"
    };

    static final String[] AWAL_MEDIS_RANAP = {
        "penilaian_medis_ranap","penilaian_medis_ranap_kandungan","penilaian_medis_ranap_jantung",
        "penilaian_medis_ranap_neonatus","penilaian_medis_ranap_psikiatrik"
    };

    static final String[] AWAL_KEPERAWATAN_RALAN = {
        "penilaian_awal_keperawatan_ralan","penilaian_awal_keperawatan_ralan_bayi","penilaian_awal_keperawatan_ralan_psikiatri",
        "penilaian_awal_keperawatan_ralan_geriatri","penilaian_awal_keperawatan_gigi","penilaian_awal_keperawatan_kebidanan",
        "penilaian_awal_keperawatan_mata","penilaian_awal_keperawatan_igd"
    };

    static final String[] AWAL_KEPERAWATAN_RANAP = {
        "penilaian_awal_keperawatan_ranap","penilaian_awal_keperawatan_kebidanan_ranap",
        "penilaian_awal_keperawatan_ranap_bayi","penilaian_awal_keperawatan_ranap_neonatus"
    };

    // Nama kolom pengkajian keperawatan ranap/kebidanan yang berbeda dengan pengkajian ralan
    private static final String[][] ALIAS = {
        {"td","pemeriksaan_td"},{"nadi","pemeriksaan_nadi"},{"rr","pemeriksaan_rr"},{"suhu","pemeriksaan_suhu"},
        {"spo","pemeriksaan_spo2"},{"gcs","pemeriksaan_gcs"},{"bb","pemeriksaan_bb"},{"tb","pemeriksaan_tb"},
        {"keadaan","pemeriksaan_keadaan_umum"},{"alergi","riwayat_alergi"},{"keluhan_utama","keluhan"}
    };

    static class Soap {
        String teks="",keluhan="",objektif="",penilaian="",rtl="",instruksi="";
    }

    private ResumeOtomatis() {}

    /** Baris pertama dari tabel pengkajian pertama yang berisi data untuk no_rawat tersebut. */
    static Map<String,String> penilaian(Connection koneksi,String norawat,String[]... daftarTabel) {
        for(String[] tabel:daftarTabel){
            for(String nama:tabel){
                Map<String,String> hasil=ambilBaris(koneksi,"select * from "+nama+" where no_rawat=? limit 1",norawat);
                if(!hasil.isEmpty()){
                    for(String[] alias:ALIAS){
                        if(nilai(hasil,alias[0]).equals("")&&!nilai(hasil,alias[1]).equals("")){
                            hasil.put(alias[0],hasil.get(alias[1]));
                        }
                    }
                    return hasil;
                }
            }
        }
        return new HashMap<>();
    }

    /** Skrining rawat jalan tidak menyimpan no_rawat, dicocokkan lewat no_rkm_medis dan tanggal registrasi. */
    static Map<String,String> skrining(Connection koneksi,String norawat) {
        return ambilBaris(koneksi,
            "select skrining_rawat_jalan.* from skrining_rawat_jalan inner join reg_periksa "+
            "on skrining_rawat_jalan.no_rkm_medis=reg_periksa.no_rkm_medis and skrining_rawat_jalan.tanggal=reg_periksa.tgl_registrasi "+
            "where reg_periksa.no_rawat=? order by skrining_rawat_jalan.jam desc limit 1",norawat);
    }

    static String triase(Map<String,String> skrining) {
        StringBuilder sb=new StringBuilder();
        tambah(sb,", ","Kesadaran : ",nilai(skrining,"kesadaran"));
        tambah(sb,", ","Pernapasan : ",nilai(skrining,"pernapasan"));
        tambah(sb,", ","Nyeri Dada : ",nilai(skrining,"nyeri_dada"));
        tambah(sb,", ","Skala Nyeri : ",nilai(skrining,"skala_nyeri"));
        tambah(sb,", ","Batuk : ",nilai(skrining,"batuk"));
        tambah(sb,", ","Risiko Jatuh : ",nilai(skrining,"risiko_jatuh"));
        tambah(sb,", ","Keputusan : ",nilai(skrining,"keputusan"));
        return sb.toString();
    }

    static String tandaVital(Map<String,String> data) {
        StringBuilder sb=new StringBuilder();
        tambah(sb,", ","Keadaan Umum : ",nilai(data,"keadaan"));
        tambah(sb,", ","Kesadaran : ",nilai(data,"kesadaran"));
        tambah(sb,", ","GCS : ",nilai(data,"gcs"));
        tambah(sb,", ","TD : ",nilai(data,"td"));
        tambah(sb,", ","Nadi : ",nilai(data,"nadi"));
        tambah(sb,", ","RR : ",nilai(data,"rr"));
        tambah(sb,", ","Suhu : ",nilai(data,"suhu"));
        tambah(sb,", ","SpO2 : ",nilai(data,"spo"));
        tambah(sb,", ","BB : ",nilai(data,"bb"));
        tambah(sb,", ","TB : ",nilai(data,"tb"));
        return sb.toString();
    }

    /** Keluhan utama beserta riwayat penyakit, medis didahulukan lalu keperawatan. */
    static String keluhan(Map<String,String> medis,Map<String,String> perawat,String keluhanSoap) {
        StringBuilder sb=new StringBuilder();
        String keluhanUtama=pilih(medis,perawat,"keluhan_utama");
        tambah(sb,"\n","",keluhanUtama.equals("")?keluhanSoap:keluhanUtama);
        tambah(sb,"\n","Riwayat Penyakit Sekarang : ",pilih(medis,perawat,"rps"));
        tambah(sb,"\n","Riwayat Penyakit Dahulu : ",pilih(medis,perawat,"rpd"));
        tambah(sb,"\n","Riwayat Penyakit Keluarga : ",pilih(medis,perawat,"rpk"));
        tambah(sb,"\n","Riwayat Pengobatan : ",pilih(medis,perawat,"rpo"));
        return sb.toString();
    }

    /** SOAP rawat jalan/IGD, ditambah SOAP rawat inap bila ranap=true, urut waktu. */
    static Soap soap(Connection koneksi,String norawat,boolean ranap) {
        String kolom="tgl_perawatan,jam_rawat,suhu_tubuh,tensi,nadi,respirasi,tinggi,berat,spo2,gcs,kesadaran,keluhan,pemeriksaan,penilaian,rtl,instruksi,evaluasi,nip";
        String sql="select 'Ralan/IGD' as jenis,"+kolom+" from pemeriksaan_ralan where no_rawat=?";
        if(ranap){
            sql=sql+" union all select 'Ranap' as jenis,"+kolom+" from pemeriksaan_ranap where no_rawat=?";
        }
        sql="select soap.*,ifnull(pegawai.nama,soap.nip) as nama from ("+sql+") soap "+
            "left join pegawai on soap.nip=pegawai.nik order by soap.tgl_perawatan,soap.jam_rawat";
        Soap hasil=new Soap();
        StringBuilder teks=new StringBuilder(),objektif=new StringBuilder();
        try (PreparedStatement ps=koneksi.prepareStatement(sql)) {
            ps.setString(1,norawat);
            if(ranap) ps.setString(2,norawat);
            try (ResultSet rs=ps.executeQuery()) {
                while(rs.next()){
                    StringBuilder o=new StringBuilder();
                    tambah(o,", ","TD : ",rs.getString("tensi"));
                    tambah(o,", ","Nadi : ",rs.getString("nadi"));
                    tambah(o,", ","RR : ",rs.getString("respirasi"));
                    tambah(o,", ","Suhu : ",rs.getString("suhu_tubuh"));
                    tambah(o,", ","SpO2 : ",rs.getString("spo2"));
                    tambah(o,", ","GCS : ",rs.getString("gcs"));
                    tambah(o,", ","Kesadaran : ",rs.getString("kesadaran"));
                    tambah(o,", ","BB : ",rs.getString("berat"));
                    tambah(o,", ","TB : ",rs.getString("tinggi"));
                    tambah(o,"\n","",rs.getString("pemeriksaan"));

                    StringBuilder baris=new StringBuilder();
                    tambah(baris,"\n","S : ",rs.getString("keluhan"));
                    tambah(baris,"\n","O : ",o.toString());
                    tambah(baris,"\n","A : ",rs.getString("penilaian"));
                    tambah(baris,"\n","P : ",rs.getString("rtl"));
                    tambah(baris,"\n","I : ",rs.getString("instruksi"));
                    tambah(baris,"\n","E : ",rs.getString("evaluasi"));
                    String waktu=rs.getString("tgl_perawatan")+" "+rs.getString("jam_rawat");
                    if(baris.length()>0){
                        tambah(teks,"\n\n","",waktu+(ranap?" ["+rs.getString("jenis")+"]":"")+" ("+rs.getString("nama")+")\n"+baris);
                    }
                    tambah(objektif,"\n",waktu+" - ",rs.getString("pemeriksaan"));
                    if(hasil.keluhan.equals("")) hasil.keluhan=bersih(rs.getString("keluhan"));
                    if(!bersih(rs.getString("penilaian")).equals("")) hasil.penilaian=bersih(rs.getString("penilaian"));
                    if(!bersih(rs.getString("rtl")).equals("")) hasil.rtl=bersih(rs.getString("rtl"));
                    if(!bersih(rs.getString("instruksi")).equals("")) hasil.instruksi=bersih(rs.getString("instruksi"));
                }
            }
        } catch (Exception e) {
            System.out.println("Notif : "+e);
        }
        hasil.teks=teks.toString();
        hasil.objektif=objektif.toString();
        return hasil;
    }

    static String radiologi(Connection koneksi,String norawat) {
        return daftar(koneksi,
            "select concat(periksa_radiologi.tgl_periksa,' ',periksa_radiologi.jam,' - ',"+
            "group_concat(distinct jns_perawatan_radiologi.nm_perawatan separator ', '),"+
            "if(ifnull(hasil_radiologi.hasil,'')='','',concat(' :\\n',hasil_radiologi.hasil))) "+
            "from periksa_radiologi inner join jns_perawatan_radiologi on periksa_radiologi.kd_jenis_prw=jns_perawatan_radiologi.kd_jenis_prw "+
            "left join hasil_radiologi on hasil_radiologi.no_rawat=periksa_radiologi.no_rawat "+
            "and hasil_radiologi.tgl_periksa=periksa_radiologi.tgl_periksa and hasil_radiologi.jam=periksa_radiologi.jam "+
            "where periksa_radiologi.no_rawat=? "+
            "group by periksa_radiologi.tgl_periksa,periksa_radiologi.jam,hasil_radiologi.hasil "+
            "order by periksa_radiologi.tgl_periksa,periksa_radiologi.jam",norawat);
    }

    static String laborat(Connection koneksi,String norawat) {
        return daftar(koneksi,
            "select concat(detail_periksa_lab.tgl_periksa,' ',jns_perawatan_lab.nm_perawatan,' - ',template_laboratorium.Pemeriksaan,' : ',"+
            "detail_periksa_lab.nilai,' ',template_laboratorium.satuan,"+
            "if(detail_periksa_lab.keterangan='','',concat(' (',detail_periksa_lab.keterangan,')'))) "+
            "from detail_periksa_lab inner join template_laboratorium on detail_periksa_lab.id_template=template_laboratorium.id_template "+
            "inner join jns_perawatan_lab on detail_periksa_lab.kd_jenis_prw=jns_perawatan_lab.kd_jenis_prw "+
            "where detail_periksa_lab.no_rawat=? "+
            "order by detail_periksa_lab.tgl_periksa,detail_periksa_lab.jam,detail_periksa_lab.kd_jenis_prw,template_laboratorium.urut",norawat);
    }

    /** Baris pertama hasil query sebagai map nama_kolom -> nilai. Tabel yang tidak ada di database diabaikan. */
    static Map<String,String> ambilBaris(Connection koneksi,String sql,String norawat) {
        Map<String,String> hasil=new HashMap<>();
        try (PreparedStatement ps=koneksi.prepareStatement(sql)) {
            ps.setString(1,norawat);
            try (ResultSet rs=ps.executeQuery()) {
                if(rs.next()){
                    ResultSetMetaData meta=rs.getMetaData();
                    for(int i=1;i<=meta.getColumnCount();i++){
                        hasil.put(meta.getColumnLabel(i).toLowerCase(),bersih(rs.getString(i)));
                    }
                }
            }
        } catch (Exception e) {
            // tabel pengkajian tertentu bisa saja belum dibuat di database
        }
        return hasil;
    }

    /** Kolom pertama setiap baris digabung dengan baris baru (tanpa batas group_concat_max_len). */
    static String daftar(Connection koneksi,String sql,String... parameter) {
        StringBuilder hasil=new StringBuilder();
        try (PreparedStatement ps=koneksi.prepareStatement(sql)) {
            for(int i=0;i<parameter.length;i++){
                ps.setString(i+1,parameter[i]);
            }
            try (ResultSet rs=ps.executeQuery()) {
                while(rs.next()){
                    tambah(hasil,"\n","",rs.getString(1));
                }
            }
        } catch (Exception e) {
            System.out.println("Notif : "+e);
        }
        return hasil.toString();
    }

    static String bersih(String isi) {
        return isi==null?"":isi.trim();
    }

    static String nilai(Map<String,String> data,String kolom) {
        String isi=bersih(data.get(kolom));
        return isi.equals("-")?"":isi;
    }

    static String pilih(Map<String,String> utama,Map<String,String> cadangan,String kolom) {
        String isi=nilai(utama,kolom);
        return isi.equals("")?nilai(cadangan,kolom):isi;
    }

    static void tambah(StringBuilder sb,String pemisah,String label,String isi) {
        isi=bersih(isi);
        if(isi.equals("")||isi.equals("-")) return;
        if(sb.length()>0) sb.append(pemisah);
        sb.append(label).append(isi);
    }

    /** Isi komponen hanya bila ada data, supaya isian manual tidak terhapus. */
    static void setIsi(JTextComponent komponen,String isi) {
        if(!bersih(isi).equals("")) komponen.setText(isi);
    }

    /** Isi komponen hanya bila komponen masih kosong. */
    static void setJikaKosong(JTextComponent komponen,String isi) {
        if(komponen.getText().trim().equals("")) setIsi(komponen,isi);
    }
}
