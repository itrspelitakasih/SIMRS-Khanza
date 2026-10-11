package komunikasi;

import fungsi.koneksiDB;
import java.awt.Toolkit;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.SwingUtilities;

/**
 * Pemantau chat di belakang layar selama user login: kirim heartbeat status online ke chat_online
 * dan hitung pesan personal & Chat Semua yang belum dibaca. Query jalan di thread sendiri,
 * hasilnya dikirim ke listener di EDT. Bunyi beep bila ada pesan baru masuk.
 */
public class ChatNotifier {
    public interface Listener {
        void jumlahBelumDibaca(Info info);
    }

    /** Ringkasan pesan belum dibaca. dariKode/dariNama/cuplikan = pesan pribadi belum dibaca terbaru. */
    public static class Info {
        public int personal=0,global=0,jmlPengirim=0;
        public String dariKode="",dariNama="",cuplikan="";
    }

    public static final int DETIK_ONLINE=20;
    private static final int INTERVAL=5;
    private static ScheduledExecutorService jadwal=null;
    private static String kodeUser="";
    private static Listener listener=null;
    private static int personalLama=0,globalLama=0;
    private static boolean tabelSiap=false;

    public static synchronized void start(String kode,Listener l){
        stop();
        kodeUser=kode;
        listener=l;
        personalLama=0;
        globalLama=0;
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
            // tabel yang sudah terlanjur dibuat dengan struktur lama
            tambahKolom(st,"chat_online","id_global_dibaca","bigint(20) NOT NULL DEFAULT 0");
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
                        "select pengirim,pesan from chat_pesan where penerima=? and dibaca='0' order by id desc limit 1")){
                    ps.setString(1,kode);
                    try (ResultSet rs=ps.executeQuery()){
                        if(rs.next()){
                            info.dariKode=rs.getString(1);
                            info.cuplikan=rs.getString(2).replaceAll("\\s+"," ").trim();
                        }
                    }
                }
                info.dariNama=namaUser(koneksi,info.dariKode);
            }
            // pesan pribadi: beep 2x, Chat Semua: beep 1x
            if(info.personal>personalLama){
                Toolkit.getDefaultToolkit().beep();
                Thread.sleep(250);
                Toolkit.getDefaultToolkit().beep();
            }else if(info.global>globalLama){
                Toolkit.getDefaultToolkit().beep();
            }
            personalLama=info.personal;
            globalLama=info.global;
            if(l!=null){
                SwingUtilities.invokeLater(() -> l.jumlahBelumDibaca(info));
            }
        } catch (Exception e) {
            System.out.println("Notifikasi Chat : "+e);
        }
    }
}
