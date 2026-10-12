package komunikasi;

import fungsi.akses;
import fungsi.koneksiDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.SwingUtilities;

/**
 * Pemantau notifikasi di belakang layar selama user login: kirim heartbeat status online ke chat_online
 * dan hitung pesan personal & Chat Semua yang belum dibaca. Query jalan di thread sendiri,
 * hasilnya dikirim ke listener di EDT.
 * - User dokter: hasil lab & radiologi pasien yang dokter tsb menjadi Perujuk, Dokter PJ penunjang, atau DPJP
 *   (dokter poli untuk ralan, dpjp_ranap untuk ranap).
 * - User petugas (bukan dokter): tugas baru sesuai hak akses menu, yaitu permintaan lab/radiologi yang belum
 *   ada hasil, resep belum terlayani, booking periksa belum dibalas, dan booking registrasi berstatus Belum.
 * Berbunyi (SuaraNotif) bila ada pesan, hasil, atau tugas baru.
 */
public class ChatNotifier {
    public interface Listener {
        void jumlahBelumDibaca(Info info);
    }

    /** Ringkasan pesan belum dibaca. dariKode/dariNama/cuplikan = pesan pribadi belum dibaca terbaru. */
    public static class Info {
        public int personal=0,global=0,jmlPengirim=0;
        public String dariKode="",dariNama="",cuplikan="";
        /** Pesan pribadi belum dibaca per pengirim (maks. 10), pengirim terbaru di depan. */
        public List<Pesan> pesan=new ArrayList<>();
        /** Pengirim & cuplikan Chat Semua terbaru yang belum dibaca. */
        public String globalDari="",globalCuplikan="";
        /** Hasil lab/radiologi yang belum dilihat dokter, terbaru di depan. */
        public List<Hasil> hasil=new ArrayList<>();
        /** Permintaan/resep/booking baru yang belum dilihat petugas, terbaru di depan. */
        public List<Tugas> tugas=new ArrayList<>();
    }

    /** Pesan pribadi belum dibaca dari satu pengirim. cuplikan = pesan terbarunya. */
    public static class Pesan {
        public String kode="",nama="",cuplikan="";
        public int jumlah=0;
    }

    /** Satu hasil penunjang. jenis = Lab/Radiologi, kategori = PK/PA/MB (lab), peran = Perujuk/Dokter PJ/DPJP. */
    public static class Hasil {
        public String jenis="",kategori="",noRawat="",tgl="",jam="",pasien="",peran="";
    }

    /**
     * Satu tugas petugas. jenis = PermLab/PermLabPA/PermLabMB/PermRad/Resep/BookPrk/BookReg.
     * kunci/tgl/jam = penanda di notif_dilihat (noorder, no_resep, no_booking, atau no_rkm_medis + tanggal periksa),
     * waktu = waktu permintaan/booking, rawat = ralan/ranap, nama = pasien, keterangan = dokter/poli.
     */
    public static class Tugas {
        public String jenis="",kunci="",tgl="",jam="",waktu="",noRawat="",rawat="",nama="",keterangan="";
    }

    public static final int DETIK_ONLINE=20;
    private static final int INTERVAL=5;
    // hasil penunjang & tugas dicek tiap CEK_NOTIF x INTERVAL detik
    private static final int CEK_NOTIF=3;
    private static ScheduledExecutorService jadwal=null;
    private static String kodeUser="";
    private static Listener listener=null;
    private static int personalLama=0,globalLama=0,hasilLama=0,tugasLama=0,putaran=0;
    private static Boolean dokter=null;
    private static String sqlTugas="";
    private static int jmlSumberTugas=0;
    private static List<Hasil> hasilTerakhir=new ArrayList<>();
    private static List<Tugas> tugasTerakhir=new ArrayList<>();
    private static boolean tabelSiap=false;

    public static synchronized void start(String kode,Listener l){
        stop();
        kodeUser=kode;
        listener=l;
        personalLama=0;
        globalLama=0;
        hasilLama=0;
        tugasLama=0;
        putaran=0;
        dokter=null;
        hasilTerakhir=new ArrayList<>();
        tugasTerakhir=new ArrayList<>();
        siapkanSqlTugas();
        jadwal=Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t=new Thread(r,"ChatNotifier");
            t.setDaemon(true);
            return t;
        });
        jadwal.scheduleWithFixedDelay(ChatNotifier::cek,1,INTERVAL,TimeUnit.SECONDS);
    }

    public static synchronized void stop(){
        if(jadwal!=null){
            jadwal.shutdownNow();
            jadwal=null;
        }
        kodeUser="";
        listener=null;
    }

    /** Minta cek ulang segera, mis. setelah pesan dibaca di DlgChat. */
    public static synchronized void cekSekarang(){
        if(jadwal!=null){
            jadwal.execute(ChatNotifier::cek);
        }
    }

    /** Tandai hasil sudah dilihat user agar tidak dihitung lagi, lalu cek ulang. */
    public static void tandaiHasilDilihat(List<Hasil> daftar){
        List<String[]> kunci=new ArrayList<>();
        for(Hasil h:daftar){
            kunci.add(new String[]{h.jenis,h.noRawat,h.tgl,h.jam});
        }
        tandaiDilihat(kunci);
    }

    /** Tandai tugas sudah dilihat user agar tidak dihitung lagi, lalu cek ulang. */
    public static void tandaiTugasDilihat(List<Tugas> daftar){
        List<String[]> kunci=new ArrayList<>();
        for(Tugas t:daftar){
            kunci.add(new String[]{t.jenis,t.kunci,t.tgl,t.jam});
        }
        tandaiDilihat(kunci);
    }

    private static synchronized void tandaiDilihat(List<String[]> daftar){
        if(jadwal==null||daftar.isEmpty()){
            return;
        }
        String kode=kodeUser;
        jadwal.execute(() -> {
            try (PreparedStatement ps=koneksiDB.condb().prepareStatement(
                    "insert ignore into notif_dilihat(kode_user,jenis,kunci,tgl,jam,waktu) values(?,?,?,?,?,now())")){
                for(String[] k:daftar){
                    ps.setString(1,kode);
                    ps.setString(2,k[0]);
                    ps.setString(3,k[1]);
                    ps.setString(4,k[2]);
                    ps.setString(5,k[3]);
                    ps.addBatch();
                }
                ps.executeBatch();
            } catch (Exception e) {
                System.out.println("Notifikasi : "+e);
            }
            // jumlah turun karena ditandai, bukan karena ada yang baru: cek ulang segera tanpa bunyi
            putaran=0;
            cek();
        });
    }

    public static synchronized void siapkanTabel(Connection koneksi){
        if(tabelSiap){
            return;
        }
        try (Statement st=koneksi.createStatement()){
            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS chat_pesan (id bigint(20) NOT NULL AUTO_INCREMENT,waktu datetime NOT NULL,"+
                "pengirim varchar(50) NOT NULL,penerima varchar(50) NOT NULL DEFAULT '',pesan text NOT NULL,"+
                "dibaca enum('0','1') NOT NULL DEFAULT '0',PRIMARY KEY (id),KEY penerima (penerima,dibaca),"+
                "KEY pengirim (pengirim,penerima)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS chat_online (kode_user varchar(50) NOT NULL,terakhir_aktif datetime NOT NULL,"+
                "id_global_dibaca bigint(20) NOT NULL DEFAULT 0,PRIMARY KEY (kode_user)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS notif_dilihat (kode_user varchar(50) NOT NULL,jenis varchar(10) NOT NULL,"+
                "kunci varchar(20) NOT NULL,tgl date NOT NULL,jam time NOT NULL,waktu datetime NOT NULL,"+
                "PRIMARY KEY (kode_user,jenis,kunci,tgl,jam)) ENGINE=InnoDB DEFAULT CHARSET=latin1");
            // tabel yang sudah terlanjur dibuat dengan struktur lama
            tambahKolom(st,"chat_online","id_global_dibaca","bigint(20) NOT NULL DEFAULT 0");
            tambahKolom(st,"chat_online","notif_mulai","date DEFAULT NULL");
            tabelSiap=true;
        } catch (Exception e) {
            System.out.println("Notifikasi Chat : "+e);
        }
    }

    private static void tambahKolom(Statement st,String tabel,String kolom,String definisi) throws Exception {
        try (ResultSet rs=st.executeQuery(
                "select count(*) from information_schema.columns where table_schema=database() "+
                "and table_name='"+tabel+"' and column_name='"+kolom+"'")){
            if(rs.next()&&rs.getInt(1)>0){
                return;
            }
        }
        st.executeUpdate("ALTER TABLE "+tabel+" ADD COLUMN "+kolom+" "+definisi);
    }

    /** Nama user dari pegawai/dokter/petugas, kode user bila tidak ditemukan. */
    public static String namaUser(Connection koneksi,String kode){
        if(kode.equals("Admin Utama")){
            return kode;
        }
        try (PreparedStatement ps=koneksi.prepareStatement(
                "select ifnull((select nama from pegawai where nik=?),ifnull((select nm_dokter from dokter where kd_dokter=?),"+
                "(select nama from petugas where nip=?)))")){
            ps.setString(1,kode);
            ps.setString(2,kode);
            ps.setString(3,kode);
            try (ResultSet rs=ps.executeQuery()){
                if(rs.next()&&rs.getString(1)!=null){
                    return rs.getString(1);
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi Chat : "+e);
        }
        return kode;
    }

    private static final java.util.Map<String,String> namaCache=new java.util.concurrent.ConcurrentHashMap<>();

    /** namaUser dengan cache, karena dipanggil tiap putaran cek. */
    private static String namaTersimpan(Connection koneksi,String kode){
        return namaCache.computeIfAbsent(kode,k -> namaUser(koneksi,k));
    }

    private static void cek(){
        String kode;
        Listener l;
        synchronized(ChatNotifier.class){
            kode=kodeUser;
            l=listener;
        }
        if(kode.equals("")){
            return;
        }
        try {
            Connection koneksi=koneksiDB.condb();
            siapkanTabel(koneksi);
            // user baru: Chat Semua lama tidak dihitung belum dibaca
            try (PreparedStatement ps=koneksi.prepareStatement(
                    "insert into chat_online(kode_user,terakhir_aktif,id_global_dibaca) "+
                    "select ?,now(),ifnull(max(id),0) from chat_pesan where penerima='' "+
                    "on duplicate key update terakhir_aktif=now()")){
                ps.setString(1,kode);
                ps.executeUpdate();
            }
            Info info=new Info();
            try (PreparedStatement ps=koneksi.prepareStatement(
                    "select (select count(*) from chat_pesan where penerima=? and dibaca='0'),"+
                    "(select count(distinct pengirim) from chat_pesan where penerima=? and dibaca='0'),"+
                    "(select count(*) from chat_pesan where penerima='' and pengirim<>? and "+
                    "id>(select id_global_dibaca from chat_online where kode_user=?))")){
                ps.setString(1,kode);
                ps.setString(2,kode);
                ps.setString(3,kode);
                ps.setString(4,kode);
                try (ResultSet rs=ps.executeQuery()){
                    if(rs.next()){
                        info.personal=rs.getInt(1);
                        info.jmlPengirim=rs.getInt(2);
                        info.global=rs.getInt(3);
                    }
                }
            }
            if(info.personal>0){
                try (PreparedStatement ps=koneksi.prepareStatement(
                        "select c.pengirim,c.jml,p.pesan from (select pengirim,count(*) jml,max(id) id from chat_pesan "+
                        "where penerima=? and dibaca='0' group by pengirim) c inner join chat_pesan p on p.id=c.id "+
                        "order by c.id desc limit 10")){
                    ps.setString(1,kode);
                    try (ResultSet rs=ps.executeQuery()){
                        while(rs.next()){
                            Pesan p=new Pesan();
                            p.kode=rs.getString(1);
                            p.jumlah=rs.getInt(2);
                            p.cuplikan=rs.getString(3).replaceAll("\\s+"," ").trim();
                            info.pesan.add(p);
                        }
                    }
                }
                for(Pesan p:info.pesan){
                    p.nama=namaTersimpan(koneksi,p.kode);
                }
                if(!info.pesan.isEmpty()){
                    info.dariKode=info.pesan.get(0).kode;
                    info.dariNama=info.pesan.get(0).nama;
                    info.cuplikan=info.pesan.get(0).cuplikan;
                }
            }
            if(info.global>0){
                try (PreparedStatement ps=koneksi.prepareStatement(
                        "select pengirim,pesan from chat_pesan where penerima='' and pengirim<>? order by id desc limit 1")){
                    ps.setString(1,kode);
                    try (ResultSet rs=ps.executeQuery()){
                        if(rs.next()){
                            info.globalDari=namaTersimpan(koneksi,rs.getString(1));
                            info.globalCuplikan=rs.getString(2).replaceAll("\\s+"," ").trim();
                        }
                    }
                }
            }
            if(dokter==null){
                dokter=siapkanUser(koneksi,kode);
            }
            if(putaran%CEK_NOTIF==0){
                String batas=batasNotif(koneksi,kode);
                if(dokter){
                    hasilTerakhir=hasilBaru(koneksi,kode,batas);
                }else if(jmlSumberTugas>0){
                    tugasTerakhir=tugasBaru(koneksi,kode,batas);
                }
            }
            putaran++;
            info.hasil=hasilTerakhir;
            info.tugas=tugasTerakhir;
            if(info.personal>personalLama){
                SuaraNotif.bunyi(SuaraNotif.Jenis.PESAN);
            }else if(info.hasil.size()>hasilLama){
                SuaraNotif.bunyi(SuaraNotif.Jenis.HASIL);
            }else if(info.tugas.size()>tugasLama){
                SuaraNotif.bunyi(SuaraNotif.Jenis.TUGAS);
            }else if(info.global>globalLama){
                SuaraNotif.bunyi(SuaraNotif.Jenis.CHAT_SEMUA);
            }
            personalLama=info.personal;
            globalLama=info.global;
            hasilLama=info.hasil.size();
            tugasLama=info.tugas.size();
            if(l!=null){
                SwingUtilities.invokeLater(() -> l.jumlahBelumDibaca(info));
            }
        } catch (Exception e) {
            System.out.println("Notifikasi Chat : "+e);
        }
    }

    /**
     * Sekali per login: tentukan apakah user dokter (dapat notif hasil) atau petugas (notif tugas),
     * catat tanggal pertama memakai notifikasi, dan bersihkan tanda dilihat lebih dari 7 hari.
     */
    private static boolean siapkanUser(Connection koneksi,String kode){
        boolean adalahDokter=false;
        try (PreparedStatement ps=koneksi.prepareStatement("select count(*) from dokter where kd_dokter=?")){
            ps.setString(1,kode);
            try (ResultSet rs=ps.executeQuery()){
                adalahDokter=rs.next()&&rs.getInt(1)>0;
            }
        } catch (Exception e) {
            System.out.println("Notifikasi : "+e);
        }
        try (PreparedStatement ps=koneksi.prepareStatement(
                "delete from notif_dilihat where kode_user=? and tgl<date_sub(curdate(),interval 7 day)")){
            ps.setString(1,kode);
            ps.executeUpdate();
        } catch (Exception e) {
            System.out.println("Notifikasi : "+e);
        }
        // pertama kali dipakai: hasil/tugas sebelum hari ini tidak dihitung baru
        try (PreparedStatement ps=koneksi.prepareStatement(
                "update chat_online set notif_mulai=curdate() where kode_user=? and notif_mulai is null")){
            ps.setString(1,kode);
            ps.executeUpdate();
        } catch (Exception e) {
            System.out.println("Notifikasi : "+e);
        }
        return adalahDokter;
    }

    /** Batas tanggal notifikasi: 3 hari terakhir, tetapi tidak sebelum user pertama kali memakai notifikasi. */
    private static String batasNotif(Connection koneksi,String kode){
        try (PreparedStatement ps=koneksi.prepareStatement(
                "select greatest(date_sub(curdate(),interval 2 day),ifnull(notif_mulai,curdate())) from chat_online where kode_user=?")){
            ps.setString(1,kode);
            try (ResultSet rs=ps.executeQuery()){
                if(rs.next()&&rs.getString(1)!=null){
                    return rs.getString(1);
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi : "+e);
        }
        return "";
    }

    // periksa_lab/radiologi tidak punya indeks tanggal: ambil dulu no_rawat yang mungkin punya hasil baru
    // (registrasi sejak batas, lewat awalan no_rawat yyyy/mm/dd, dan pasien yang masih/baru keluar ranap),
    // lalu cocokkan lewat primary key. Parameter: batas (awalan no_rawat), batas (tgl_keluar).
    private static final String KANDIDAT=
        "select no_rawat from reg_periksa where no_rawat>=replace(?,'-','/') "+
        "union select no_rawat from kamar_inap where stts_pulang='-' or tgl_keluar>=?";

    // Parameter: 3 untuk lab, 3 untuk radiologi (KANDIDAT + batas tgl_periksa), lalu kode dokter.
    private static final String SQL_HASIL=
        "select x.jenis,x.no_rawat,x.tgl_periksa,x.jam,max(x.kategori),group_concat(distinct concat_ws(', ',"+
        "if(x.perujuk=u.kd,'Perujuk',null),if(x.pj=u.kd,'Dokter PJ',null),"+
        "if((r.status_lanjut='Ralan' and r.kd_dokter=u.kd) or d.kd_dokter is not null,'DPJP',null)) separator ', '),pasien.nm_pasien "+
        "from (select 'Lab' jenis,p.no_rawat,p.tgl_periksa,p.jam,p.kategori,p.dokter_perujuk perujuk,p.kd_dokter pj "+
        "from ("+KANDIDAT+") c inner join periksa_lab p on p.no_rawat=c.no_rawat where p.tgl_periksa>=? "+
        "union all select 'Radiologi',p.no_rawat,p.tgl_periksa,p.jam,'',p.dokter_perujuk,p.kd_dokter "+
        "from ("+KANDIDAT+") c inner join periksa_radiologi p on p.no_rawat=c.no_rawat "+
        "inner join hasil_radiologi h on h.no_rawat=p.no_rawat and h.tgl_periksa=p.tgl_periksa and h.jam=p.jam "+
        "where p.tgl_periksa>=? and trim(h.hasil)<>'') x "+
        "inner join (select ? kd) u "+
        "inner join reg_periksa r on r.no_rawat=x.no_rawat "+
        "inner join pasien on pasien.no_rkm_medis=r.no_rkm_medis "+
        "left join dpjp_ranap d on d.no_rawat=x.no_rawat and d.kd_dokter=u.kd "+
        "where (x.perujuk=u.kd or x.pj=u.kd or (r.status_lanjut='Ralan' and r.kd_dokter=u.kd) or d.kd_dokter is not null) "+
        "and not exists(select 1 from notif_dilihat n where n.kode_user=u.kd and n.jenis=x.jenis and "+
        "n.kunci=x.no_rawat and n.tgl=x.tgl_periksa and n.jam=x.jam) "+
        "group by x.jenis,x.no_rawat,x.tgl_periksa,x.jam,pasien.nm_pasien order by x.tgl_periksa desc,x.jam desc limit 50";

    /**
     * Hasil lab & radiologi sejak batas yang belum ditandai dilihat. Lab dianggap keluar saat periksa_lab
     * tersimpan, radiologi saat hasil_radiologi sudah diisi.
     */
    private static List<Hasil> hasilBaru(Connection koneksi,String kode,String batas){
        List<Hasil> daftar=new ArrayList<>();
        if(batas.equals("")){
            return daftar;
        }
        try (PreparedStatement ps=koneksi.prepareStatement(SQL_HASIL)){
            for(int i=1;i<=6;i++){
                ps.setString(i,batas);
            }
            ps.setString(7,kode);
            try (ResultSet rs=ps.executeQuery()){
                while(rs.next()){
                    Hasil h=new Hasil();
                    h.jenis=rs.getString(1);
                    h.noRawat=rs.getString(2);
                    h.tgl=rs.getString(3);
                    h.jam=rs.getString(4);
                    h.kategori=rs.getString(5);
                    h.peran=rs.getString(6);
                    h.pasien=rs.getString(7);
                    daftar.add(h);
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi Hasil : "+e);
        }
        return daftar;
    }

    // Sumber tugas; tiap sumber punya satu parameter batas tanggal. Kolom: jenis,kunci,tgl,jam (penanda dilihat),
    // waktu (tampil), no_rawat, rawat, nama, keterangan.
    private static String sumberPermintaan(String jenis,String tabel){
        return "select '"+jenis+"' jenis,p.noorder kunci,p.tgl_permintaan tgl,p.jam_permintaan jam,"+
            "concat(p.tgl_permintaan,' ',p.jam_permintaan) waktu,p.no_rawat,p.status rawat,ps.nm_pasien nama,"+
            "concat('Perujuk : ',ifnull(d.nm_dokter,p.dokter_perujuk)) keterangan from "+tabel+" p "+
            "inner join reg_periksa r on r.no_rawat=p.no_rawat inner join pasien ps on ps.no_rkm_medis=r.no_rkm_medis "+
            "left join dokter d on d.kd_dokter=p.dokter_perujuk where p.tgl_hasil='0000-00-00' and p.tgl_permintaan>=?";
    }

    private static final String SUMBER_RESEP=
        "select 'Resep',o.no_resep,o.tgl_peresepan,o.jam_peresepan,concat(o.tgl_peresepan,' ',o.jam_peresepan),"+
        "o.no_rawat,o.status,ps.nm_pasien,concat('Dokter : ',ifnull(d.nm_dokter,o.kd_dokter)) from resep_obat o "+
        "inner join reg_periksa r on r.no_rawat=o.no_rawat inner join pasien ps on ps.no_rkm_medis=r.no_rkm_medis "+
        "left join dokter d on d.kd_dokter=o.kd_dokter where o.tgl_perawatan='0000-00-00' and o.tgl_peresepan>=?";

    private static final String SUMBER_BOOKING_PERIKSA=
        "select 'BookPrk',b.no_booking,date(b.tanggal_booking),time(b.tanggal_booking),b.tanggal_booking,'','ralan',b.nama,"+
        "concat(ifnull(pl.nm_poli,''),', periksa ',date_format(b.tanggal,'%d-%m-%Y')) from booking_periksa b "+
        "left join poliklinik pl on pl.kd_poli=b.kd_poli where b.status='Belum Dibalas' and b.tanggal_booking>=?";

    private static final String SUMBER_BOOKING_REGISTRASI=
        "select 'BookReg',b.no_rkm_medis,b.tanggal_periksa,'00:00:00',concat(b.tanggal_booking,' ',b.jam_booking),'','ralan',"+
        "ps.nm_pasien,concat(ifnull(pl.nm_poli,''),', periksa ',date_format(b.tanggal_periksa,'%d-%m-%Y')) "+
        "from booking_registrasi b inner join pasien ps on ps.no_rkm_medis=b.no_rkm_medis "+
        "left join poliklinik pl on pl.kd_poli=b.kd_poli where b.status='Belum' and b.tanggal_booking>=?";

    /** Susun query tugas dari hak akses menu user yang login (sama dengan syarat tampilnya menu di frmUtama). */
    private static void siapkanSqlTugas(){
        List<String> sumber=new ArrayList<>();
        if(akses.getpermintaan_lab()||akses.getperiksa_lab()||akses.getpemeriksaan_lab_pa()||akses.getpemeriksaan_lab_mb()){
            sumber.add(sumberPermintaan("PermLab","permintaan_lab"));
            sumber.add(sumberPermintaan("PermLabPA","permintaan_labpa"));
            sumber.add(sumberPermintaan("PermLabMB","permintaan_labmb"));
        }
        if(akses.getpermintaan_radiologi()||akses.getperiksa_radiologi()){
            sumber.add(sumberPermintaan("PermRad","permintaan_radiologi"));
        }
        if(akses.getresep_dokter()){
            sumber.add(SUMBER_RESEP);
        }
        if(akses.getbooking_periksa()){
            sumber.add(SUMBER_BOOKING_PERIKSA);
        }
        if(akses.getbooking_registrasi()){
            sumber.add(SUMBER_BOOKING_REGISTRASI);
        }
        jmlSumberTugas=sumber.size();
        sqlTugas=sumber.isEmpty()?"":
            "select x.jenis,x.kunci,x.tgl,x.jam,x.waktu,x.no_rawat,x.rawat,x.nama,x.keterangan from ("+
            String.join(" union all ",sumber)+") x where not exists(select 1 from notif_dilihat n where n.kode_user=? "+
            "and n.jenis=x.jenis and n.kunci=x.kunci and n.tgl=x.tgl and n.jam=x.jam) order by x.waktu desc limit 50";
    }

    private static List<Tugas> tugasBaru(Connection koneksi,String kode,String batas){
        List<Tugas> daftar=new ArrayList<>();
        if(batas.equals("")||sqlTugas.equals("")){
            return daftar;
        }
        try (PreparedStatement ps=koneksi.prepareStatement(sqlTugas)){
            for(int i=1;i<=jmlSumberTugas;i++){
                ps.setString(i,batas);
            }
            ps.setString(jmlSumberTugas+1,kode);
            try (ResultSet rs=ps.executeQuery()){
                while(rs.next()){
                    Tugas t=new Tugas();
                    t.jenis=rs.getString(1);
                    t.kunci=rs.getString(2);
                    t.tgl=rs.getString(3);
                    t.jam=rs.getString(4);
                    t.waktu=rs.getString(5);
                    t.noRawat=rs.getString(6);
                    t.rawat=rs.getString(7);
                    t.nama=rs.getString(8);
                    t.keterangan=rs.getString(9);
                    daftar.add(t);
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi Tugas : "+e);
        }
        return daftar;
    }
}
