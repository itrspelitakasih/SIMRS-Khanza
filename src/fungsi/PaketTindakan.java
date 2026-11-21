package fungsi;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * RSPK: Paket Tindakan.
 * Master paket (harga tetap) berisi tindakan, pemeriksaan lab (dengan item detail) dan radiologi.
 * terapkan() pada pasien (MCU/ralan):
 *  - tindakan  -> langsung dicatat sebagai tindakan dokter ralan (rawat_jl_dr) + jurnal, sama dengan input manual;
 *  - lab       -> satu permintaan lab (permintaan_lab + pemeriksaan + detail item), diproses petugas lab seperti biasa;
 *  - radiologi -> satu permintaan radiologi;
 *  - selisih jumlah tarif item dengan harga paket -> Potongan (lebih murah) atau Tambahan Biaya (lebih mahal),
 *    sehingga total tagihan = harga paket setelah semua hasil lab/radiologi disimpan.
 * Setiap item dicocokkan berdasarkan nama ke tarif jenis bayar pasien (kd_pj pasien, lalu '-'); lab juga item
 * detailnya (nama pemeriksaan). Bila tidak ketemu dipakai tarif yang dipilih di paket.
 * Satu paket hanya bisa diinput sekali per kunjungan (paket_tindakan_pasien).
 */
public final class PaketTindakan {
    private static volatile Boolean aktif = null;
    private static final String CS = " ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci";

    public static final String[] DDL = {
        "CREATE TABLE IF NOT EXISTS paket_tindakan (kode_paket varchar(10) NOT NULL, nama_paket varchar(80) NOT NULL, harga double NOT NULL DEFAULT 0, "
            + "PRIMARY KEY (kode_paket))" + CS,
        "CREATE TABLE IF NOT EXISTS paket_tindakan_tindakan (kode_paket varchar(10) NOT NULL, kd_jenis_prw varchar(15) NOT NULL, "
            + "PRIMARY KEY (kode_paket,kd_jenis_prw), KEY kd_jenis_prw (kd_jenis_prw), "
            + "CONSTRAINT paket_tindakan_tindakan_ibfk_1 FOREIGN KEY (kode_paket) REFERENCES paket_tindakan (kode_paket) ON DELETE CASCADE ON UPDATE CASCADE, "
            + "CONSTRAINT paket_tindakan_tindakan_ibfk_2 FOREIGN KEY (kd_jenis_prw) REFERENCES jns_perawatan (kd_jenis_prw) ON DELETE CASCADE ON UPDATE CASCADE)" + CS,
        "CREATE TABLE IF NOT EXISTS paket_tindakan_lab (kode_paket varchar(10) NOT NULL, kd_jenis_prw varchar(15) NOT NULL, id_template int(11) NOT NULL DEFAULT 0, "
            + "PRIMARY KEY (kode_paket,kd_jenis_prw,id_template), KEY kd_jenis_prw (kd_jenis_prw), "
            + "CONSTRAINT paket_tindakan_lab_ibfk_1 FOREIGN KEY (kode_paket) REFERENCES paket_tindakan (kode_paket) ON DELETE CASCADE ON UPDATE CASCADE, "
            + "CONSTRAINT paket_tindakan_lab_ibfk_2 FOREIGN KEY (kd_jenis_prw) REFERENCES jns_perawatan_lab (kd_jenis_prw) ON DELETE CASCADE ON UPDATE CASCADE)" + CS,
        "CREATE TABLE IF NOT EXISTS paket_tindakan_radiologi (kode_paket varchar(10) NOT NULL, kd_jenis_prw varchar(15) NOT NULL, "
            + "PRIMARY KEY (kode_paket,kd_jenis_prw), KEY kd_jenis_prw (kd_jenis_prw), "
            + "CONSTRAINT paket_tindakan_radiologi_ibfk_1 FOREIGN KEY (kode_paket) REFERENCES paket_tindakan (kode_paket) ON DELETE CASCADE ON UPDATE CASCADE, "
            + "CONSTRAINT paket_tindakan_radiologi_ibfk_2 FOREIGN KEY (kd_jenis_prw) REFERENCES jns_perawatan_radiologi (kd_jenis_prw) ON DELETE CASCADE ON UPDATE CASCADE)" + CS,
        "CREATE TABLE IF NOT EXISTS paket_tindakan_pasien (no_rawat varchar(17) NOT NULL, kode_paket varchar(10) NOT NULL, waktu datetime DEFAULT NULL, "
            + "noorder_lab varchar(15) DEFAULT NULL, noorder_radiologi varchar(15) DEFAULT NULL, jumlah_tarif double DEFAULT 0, harga_paket double DEFAULT 0, "
            + "PRIMARY KEY (no_rawat,kode_paket), KEY kode_paket (kode_paket), "
            + "CONSTRAINT paket_tindakan_pasien_ibfk_1 FOREIGN KEY (no_rawat) REFERENCES reg_periksa (no_rawat) ON DELETE CASCADE ON UPDATE CASCADE, "
            + "CONSTRAINT paket_tindakan_pasien_ibfk_2 FOREIGN KEY (kode_paket) REFERENCES paket_tindakan (kode_paket) ON UPDATE CASCADE)" + CS
    };

    private PaketTindakan() {}

    private static String rp(double v) {
        return String.format("%,.0f", v).replace(',', '.');
    }

    public static boolean aktif() {
        if (aktif == null) {
            synchronized (PaketTindakan.class) {
                if (aktif == null) {
                    boolean ok = true;
                    Connection c = koneksiDB.condb();
                    for (String d : DDL) {
                        try (Statement st = c.createStatement()) {
                            st.execute(d);
                        } catch (Exception e) {
                            System.out.println("Notif Paket Tindakan : gagal membuat tabel - " + e.getMessage());
                        }
                    }
                    try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery("select count(*) from paket_tindakan_pasien")) {
                        rs.next();
                    } catch (Exception e) {
                        ok = false;
                        System.out.println("Notif Paket Tindakan : tabel tidak tersedia - " + e.getMessage());
                    }
                    aktif = ok;
                }
            }
        }
        return aktif;
    }

    /** Jumlah tarif semua item paket (tindakan dokter + lab termasuk item detail + radiologi). */
    public static double jumlahTarif(String kodePaket) {
        double t = 0;
        Connection c = koneksiDB.condb();
        String[] sql = {
            "select ifnull(sum(j.total_byrdr),0) from paket_tindakan_tindakan p inner join jns_perawatan j on j.kd_jenis_prw=p.kd_jenis_prw where p.kode_paket=?",
            "select ifnull(sum(j.total_byr),0) from (select distinct kd_jenis_prw from paket_tindakan_lab where kode_paket=?) p inner join jns_perawatan_lab j on j.kd_jenis_prw=p.kd_jenis_prw",
            "select ifnull(sum(t.biaya_item),0) from paket_tindakan_lab p inner join template_laboratorium t on t.kd_jenis_prw=p.kd_jenis_prw and t.id_template=p.id_template where p.kode_paket=?",
            "select ifnull(sum(j.total_byr),0) from paket_tindakan_radiologi p inner join jns_perawatan_radiologi j on j.kd_jenis_prw=p.kd_jenis_prw where p.kode_paket=?"
        };
        for (String s : sql) {
            try (PreparedStatement ps = c.prepareStatement(s)) {
                ps.setString(1, kodePaket);
                try (ResultSet rs = ps.executeQuery()) { if (rs.next()) t += rs.getDouble(1); }
            } catch (Exception e) {
                System.out.println("Notif Paket Tindakan : " + e.getMessage());
            }
        }
        return t;
    }

    private static String nomorOrder(Connection c, String tabel, String awalan, int digit, String tgl) throws Exception {
        try (PreparedStatement ps = c.prepareStatement("select ifnull(max(convert(right(noorder," + digit + "),signed)),0) from " + tabel + " where tgl_permintaan=?")) {
            ps.setString(1, tgl);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return awalan + tgl.replace("-", "") + String.format("%0" + digit + "d", rs.getInt(1) + 1);
            }
        }
    }

    private static List<String[]> baris(Connection c, String sql, Object... param) throws Exception {
        List<String[]> h = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < param.length; i++) ps.setObject(i + 1, param[i]);
            try (ResultSet rs = ps.executeQuery()) {
                int n = rs.getMetaData().getColumnCount();
                while (rs.next()) {
                    String[] r = new String[n];
                    for (int i = 0; i < n; i++) r[i] = rs.getString(i + 1);
                    h.add(r);
                }
            }
        }
        return h;
    }

    /** Nilai kolom pertama baris pertama, atau null bila tidak ada. */
    private static String satu(Connection c, String sql, Object... param) throws Exception {
        List<String[]> h = baris(c, sql, param);
        return h.isEmpty() ? null : h.get(0)[0];
    }

    private static double angka(Connection c, String sql, Object... param) throws Exception {
        String s = satu(c, sql, param);
        return s == null ? 0 : Double.parseDouble(s);
    }

    private static void ins(Connection c, String sql, Object... v) throws Exception {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < v.length; i++) ps.setObject(i + 1, v[i]);
            ps.executeUpdate();
        }
    }

    private static void jurnal(Connection c, Map<String, double[]> j, String rek, double debet, double kredit) {
        if (rek == null || rek.equals("") || (debet == 0 && kredit == 0)) return;
        double[] v = j.computeIfAbsent(rek, k -> new double[2]);
        v[0] += debet; v[1] += kredit;
    }

    // ---------------- pencocokan tarif ke jenis bayar pasien (berdasarkan nama) ----------------
    // p/l/r = {kd_jenis_prw paket, nama, kd_poli/kelas}. Mengembalikan kode tarif yang dipakai, atau null bila tidak ada.
    private static String cocokTindakan(Connection c, String[] p, String kdPj, String kdPoli) throws Exception {
        return satu(c, "select kd_jenis_prw from jns_perawatan where status='1' and nm_perawatan=? and (kd_pj=? or kd_pj='-') "
                + "order by (kd_pj=?) desc,(kd_jenis_prw=?) desc,(kd_poli=?) desc,(kd_poli=?) desc,kd_jenis_prw limit 1", p[1], kdPj, kdPj, p[0], p[2], kdPoli);
    }

    private static String cocokLab(Connection c, String[] l, String kdPj) throws Exception {
        return satu(c, "select kd_jenis_prw from jns_perawatan_lab where status='1' and kategori='PK' and nm_perawatan=? and (kd_pj=? or kd_pj='-') "
                + "order by (kd_pj=?) desc,(kelas=?) desc,(kd_jenis_prw=?) desc,kd_jenis_prw limit 1", l[1], kdPj, kdPj, l[2], l[0]);
    }

    private static String cocokRad(Connection c, String[] r, String kdPj) throws Exception {
        return satu(c, "select kd_jenis_prw from jns_perawatan_radiologi where status='1' and nm_perawatan=? and (kd_pj=? or kd_pj='-') "
                + "order by (kd_pj=?) desc,(kelas=?) desc,(kd_jenis_prw=?) desc,kd_jenis_prw limit 1", r[1], kdPj, kdPj, r[2], r[0]);
    }

    /** Item lab yang dipilih eksplisit di paket: {id_template, nama item}. Kosong = semua item. */
    private static List<String[]> itemLab(Connection c, String kodePaket, String kdLab) throws Exception {
        return baris(c, "select t.id_template,t.Pemeriksaan from paket_tindakan_lab p inner join template_laboratorium t "
                + "on t.kd_jenis_prw=p.kd_jenis_prw and t.id_template=p.id_template where p.kode_paket=? and p.kd_jenis_prw=? and p.id_template>0", kodePaket, kdLab);
    }

    /** id_template padanan item (berdasarkan nama) di kode lab baru; null bila ada item yang tidak ketemu. */
    private static List<String> itemBaru(Connection c, String kd, List<String[]> item) throws Exception {
        List<String> h = new ArrayList<>();
        for (String[] it : item) {
            String id = satu(c, "select id_template from template_laboratorium where kd_jenis_prw=? and Pemeriksaan=? order by urut,id_template limit 1", kd, it[1]);
            if (id == null) return null;
            h.add(id);
        }
        return h;
    }

    /**
     * Item paket yang tidak punya tarif di jenis bayar pasien (akan memakai tarif paket bila tetap diinput),
     * dalam bentuk "Tindakan: nama" / "Laboratorium: nama" / "Radiologi: nama". Dipakai untuk peringatan sebelum input.
     */
    public static List<String> tanpaTarif(String noRawat, String kodePaket) throws Exception {
        Connection c = koneksiDB.condb();
        sekuel Sequel = new sekuel();
        String kdPj = Sequel.cariIsi("select kd_pj from reg_periksa where no_rawat=?", noRawat);
        String kdPoli = Sequel.cariIsi("select kd_poli from reg_periksa where no_rawat=?", noRawat);
        List<String> h = new ArrayList<>();
        List<String> tanpaTemplate = new ArrayList<>();
        for (String[] p : baris(c, "select p.kd_jenis_prw,j.nm_perawatan,j.kd_poli from paket_tindakan_tindakan p "
                + "inner join jns_perawatan j on j.kd_jenis_prw=p.kd_jenis_prw where p.kode_paket=? order by j.nm_perawatan", kodePaket))
            if (cocokTindakan(c, p, kdPj, kdPoli) == null) h.add("Tindakan: " + p[1]);
        for (String[] l : baris(c, "select distinct p.kd_jenis_prw,j.nm_perawatan,j.kelas from paket_tindakan_lab p "
                + "inner join jns_perawatan_lab j on j.kd_jenis_prw=p.kd_jenis_prw where p.kode_paket=? order by j.nm_perawatan", kodePaket)) {
            String kd = cocokLab(c, l, kdPj);
            if (kd == null || (!kd.equals(l[0]) && itemBaru(c, kd, itemLab(c, kodePaket, l[0])) == null)) h.add("Laboratorium: " + l[1]);
            else if (angka(c, "select count(*) from template_laboratorium where kd_jenis_prw=?", kd) == 0)
                tanpaTemplate.add(l[1]);
        }
        for (String[] r : baris(c, "select p.kd_jenis_prw,j.nm_perawatan,j.kelas from paket_tindakan_radiologi p "
                + "inner join jns_perawatan_radiologi j on j.kd_jenis_prw=p.kd_jenis_prw where p.kode_paket=? order by j.nm_perawatan", kodePaket))
            if (cocokRad(c, r, kdPj) == null) h.add("Radiologi: " + r[1] + " (" + r[2] + ")");
        if (!tanpaTemplate.isEmpty()) {
            StringBuilder sb = new StringBuilder("Lab tanpa template hasil (salin lewat Tarif Lab > Salin Template Lab):");
            for (int i = 0; i < tanpaTemplate.size(); i++) sb.append(i % 5 == 0 ? "\n     " : ", ").append(tanpaTemplate.get(i));
            h.add(sb.toString());
        }
        return h;
    }

    /**
     * Terapkan paket ke pasien. Mengembalikan ringkasan bila berhasil; melempar Exception berisi pesan bila gagal
     * (seluruh perubahan dibatalkan).
     */
    public static String terapkan(String noRawat, String kodePaket, String kdDokter) throws Exception {
        if (!aktif()) throw new Exception("Tabel Paket Tindakan belum tersedia di database.");
        Connection c = koneksiDB.condb();
        sekuel Sequel = new sekuel();
        try (PreparedStatement ps = c.prepareStatement("select count(*) from paket_tindakan_pasien where no_rawat=? and kode_paket=?")) {
            ps.setString(1, noRawat);
            ps.setString(2, kodePaket);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) throw new Exception("Paket ini sudah pernah diinput untuk kunjungan ini.");
            }
        }
        if (Sequel.cariRegistrasi(noRawat) > 0)
            throw new Exception("Billing pasien sudah disimpan/terverifikasi, paket tidak bisa ditambahkan.");
        String namaPaket = Sequel.cariIsi("select nama_paket from paket_tindakan where kode_paket=?", kodePaket);
        double harga = Sequel.cariIsiAngka("select harga from paket_tindakan where kode_paket=?", kodePaket);
        if (namaPaket.equals("")) throw new Exception("Paket tidak ditemukan.");
        if (kdDokter == null || kdDokter.trim().equals("")) throw new Exception("Dokter pada registrasi masih kosong.");

        Date sekarang = new Date();
        String tgl = new SimpleDateFormat("yyyy-MM-dd").format(sekarang), jam = new SimpleDateFormat("HH:mm:ss").format(sekarang);
        String info = ("Paket Tindakan " + namaPaket);
        if (info.length() > 60) info = info.substring(0, 60);
        String namaPenyesuaian = ("Paket Tindakan " + namaPaket);
        if (namaPenyesuaian.length() > 60) namaPenyesuaian = namaPenyesuaian.substring(0, 60);

        if (akuntindakanralan.getSuspen_Piutang_Tindakan_Ralan().equals("")) akuntindakanralan.SetAkunTindakanRalan();
        boolean autoAwal = c.getAutoCommit();
        c.setAutoCommit(false);
        int nTindakan = 0, nLab = 0, nItemLab = 0, nRad = 0, nSesuai = 0;
        String orderLab = null, orderRad = null;
        double ttlTindakan = 0, jumlah = 0;
        // Jenis bayar pasien: setiap item paket dicocokkan (berdasarkan nama) ke tarif jenis bayar pasien,
        // supaya riwayat tindakan/permintaan lab/radiologi memakai tarif sesuai jenis bayarnya.
        String kdPj = Sequel.cariIsi("select kd_pj from reg_periksa where no_rawat=?", noRawat);
        String kdPoli = Sequel.cariIsi("select kd_poli from reg_periksa where no_rawat=?", noRawat);
        String nmPj = Sequel.cariIsi("select png_jawab from penjab where kd_pj=?", kdPj);
        List<String> tidakCocok = new ArrayList<>();
        try {
            // ---- Tindakan dokter ralan + jurnal (sama dengan input Penanganan Dokter di rawat jalan)
            List<String[]> tindakan = new ArrayList<>();
            java.util.Set<String> sudah = new java.util.HashSet<>();
            for (String[] p : baris(c, "select p.kd_jenis_prw,j.nm_perawatan,j.kd_poli from paket_tindakan_tindakan p "
                    + "inner join jns_perawatan j on j.kd_jenis_prw=p.kd_jenis_prw where p.kode_paket=?", kodePaket)) {
                String kd = cocokTindakan(c, p, kdPj, kdPoli);
                if (kd == null) { kd = p[0]; tidakCocok.add(p[1]); } else if (!kd.equals(p[0])) nSesuai++;
                if (!sudah.add(kd)) continue;
                tindakan.addAll(baris(c, "select kd_jenis_prw,material,bhp,tarif_tindakandr,kso,menejemen,total_byrdr from jns_perawatan where kd_jenis_prw=?", kd));
            }
            double ttlJm = 0, ttlKso = 0, ttlSarana = 0, ttlBhp = 0, ttlMen = 0;
            for (String[] t : tindakan) {
                ins(c, "insert into rawat_jl_dr values (?,?,?,?,?,?,?,?,?,?,?,'Belum')", noRawat, t[0], kdDokter, tgl, jam,
                        Double.valueOf(t[1]), Double.valueOf(t[2]), Double.valueOf(t[3]), Double.valueOf(t[4]), Double.valueOf(t[5]), Double.valueOf(t[6]));
                ttlSarana += Double.parseDouble(t[1]); ttlBhp += Double.parseDouble(t[2]); ttlJm += Double.parseDouble(t[3]);
                ttlKso += Double.parseDouble(t[4]); ttlMen += Double.parseDouble(t[5]); ttlTindakan += Double.parseDouble(t[6]);
                nTindakan++;
            }
            if (ttlTindakan > 0) {
                Map<String, double[]> j = new LinkedHashMap<>();
                jurnal(c, j, akuntindakanralan.getSuspen_Piutang_Tindakan_Ralan(), ttlTindakan, 0);
                jurnal(c, j, akuntindakanralan.getTindakan_Ralan(), 0, ttlTindakan);
                jurnal(c, j, akuntindakanralan.getBeban_Jasa_Medik_Dokter_Tindakan_Ralan(), ttlJm, 0);
                jurnal(c, j, akuntindakanralan.getUtang_Jasa_Medik_Dokter_Tindakan_Ralan(), 0, ttlJm);
                jurnal(c, j, akuntindakanralan.getBeban_KSO_Tindakan_Ralan(), ttlKso, 0);
                jurnal(c, j, akuntindakanralan.getUtang_KSO_Tindakan_Ralan(), 0, ttlKso);
                jurnal(c, j, akuntindakanralan.getBeban_Jasa_Sarana_Tindakan_Ralan(), ttlSarana, 0);
                jurnal(c, j, akuntindakanralan.getUtang_Jasa_Sarana_Tindakan_Ralan(), 0, ttlSarana);
                jurnal(c, j, akuntindakanralan.getHPP_BHP_Tindakan_Ralan(), ttlBhp, 0);
                jurnal(c, j, akuntindakanralan.getPersediaan_BHP_Tindakan_Ralan(), 0, ttlBhp);
                jurnal(c, j, akuntindakanralan.getBeban_Jasa_Menejemen_Tindakan_Ralan(), ttlMen, 0);
                jurnal(c, j, akuntindakanralan.getUtang_Jasa_Menejemen_Tindakan_Ralan(), 0, ttlMen);
                try (Statement st = c.createStatement()) { st.executeUpdate("delete from tampjurnal"); }
                for (Map.Entry<String, double[]> e : j.entrySet()) {
                    String nm = Sequel.cariIsi("select nm_rek from rekening where kd_rek=?", e.getKey());
                    ins(c, "insert into tampjurnal values (?,?,?,?)", e.getKey(), nm, e.getValue()[0], e.getValue()[1]);
                }
                String rm = Sequel.cariIsi("select concat(r.no_rkm_medis,' ',p.nm_pasien) from reg_periksa r inner join pasien p on p.no_rkm_medis=r.no_rkm_medis where r.no_rawat=?", noRawat);
                if (!new keuangan.Jurnal().simpanJurnal(noRawat, "U", "TINDAKAN RAWAT JALAN PASIEN " + rm + " (PAKET TINDAKAN " + kodePaket + "), DIPOSTING OLEH " + akses.getkode()))
                    throw new Exception("Gagal menyimpan jurnal tindakan.");
            }

            jumlah += ttlTindakan;

            // ---- Permintaan laboratorium (PK)
            List<String[]> lab = baris(c, "select distinct p.kd_jenis_prw,j.nm_perawatan,j.kelas from paket_tindakan_lab p "
                    + "inner join jns_perawatan_lab j on j.kd_jenis_prw=p.kd_jenis_prw where p.kode_paket=?", kodePaket);
            if (!lab.isEmpty()) {
                orderLab = nomorOrder(c, "permintaan_lab", "PK", 4, tgl);
                ins(c, "insert into permintaan_lab values (?,?,?,?,'0000-00-00','00:00:00','0000-00-00','00:00:00',?,'ralan',?,?)", orderLab, noRawat, tgl, jam, kdDokter, info, "-");
                java.util.Set<String> sudahLab = new java.util.HashSet<>();
                for (String[] l : lab) {
                    List<String[]> item = itemLab(c, kodePaket, l[0]);
                    String kd = cocokLab(c, l, kdPj);
                    List<String> idBaru = new ArrayList<>();
                    if (kd != null && !kd.equals(l[0])) {
                        List<String> id = itemBaru(c, kd, item);
                        if (id == null) kd = null; else idBaru.addAll(id);
                    }
                    if (kd == null) { kd = l[0]; tidakCocok.add(l[1]); }
                    if (kd.equals(l[0])) { idBaru.clear(); for (String[] it : item) idBaru.add(it[0]); } else nSesuai++;
                    if (!sudahLab.add(kd)) continue;
                    // tanpa item yang dipilih = semua item pemeriksaan (form hasil lab hanya menampilkan item yang ada di detail permintaan)
                    if (idBaru.isEmpty()) for (String[] t : baris(c, "select id_template from template_laboratorium where kd_jenis_prw=? order by urut,id_template", kd)) idBaru.add(t[0]);
                    ins(c, "insert into permintaan_pemeriksaan_lab values (?,?,'Belum')", orderLab, kd);
                    jumlah += angka(c, "select total_byr from jns_perawatan_lab where kd_jenis_prw=?", kd);
                    nLab++;
                    for (String id : idBaru) {
                        ins(c, "insert into permintaan_detail_permintaan_lab values (?,?,?,'Belum')", orderLab, kd, Integer.valueOf(id));
                        jumlah += angka(c, "select biaya_item from template_laboratorium where kd_jenis_prw=? and id_template=?", kd, Integer.valueOf(id));
                        nItemLab++;
                    }
                }
            }

            // ---- Permintaan radiologi
            List<String[]> rad = baris(c, "select p.kd_jenis_prw,j.nm_perawatan,j.kelas from paket_tindakan_radiologi p "
                    + "inner join jns_perawatan_radiologi j on j.kd_jenis_prw=p.kd_jenis_prw where p.kode_paket=?", kodePaket);
            if (!rad.isEmpty()) {
                orderRad = nomorOrder(c, "permintaan_radiologi", "PR", 3, tgl);
                ins(c, "insert into permintaan_radiologi values (?,?,?,?,'0000-00-00','00:00:00','0000-00-00','00:00:00',?,'ralan',?,?)", orderRad, noRawat, tgl, jam, kdDokter, info, "-");
                java.util.Set<String> sudahRad = new java.util.HashSet<>();
                for (String[] r : rad) {
                    String kd = cocokRad(c, r, kdPj);
                    if (kd == null) { kd = r[0]; tidakCocok.add(r[1]); } else if (!kd.equals(r[0])) nSesuai++;
                    if (!sudahRad.add(kd)) continue;
                    ins(c, "insert into permintaan_pemeriksaan_radiologi values (?,?,'Belum')", orderRad, kd);
                    jumlah += angka(c, "select total_byr from jns_perawatan_radiologi where kd_jenis_prw=?", kd);
                    nRad++;
                }
            }

            // ---- Penyesuaian harga paket (dari tarif yang benar-benar dipakai pasien)
            double selisih = Math.round(harga - jumlah);
            if (selisih < 0) {
                ins(c, "insert into pengurangan_biaya values (?,?,?) on duplicate key update besar_pengurangan=besar_pengurangan+values(besar_pengurangan)", noRawat, namaPenyesuaian, -selisih);
            } else if (selisih > 0) {
                ins(c, "insert into tambahan_biaya values (?,?,?) on duplicate key update besar_biaya=besar_biaya+values(besar_biaya)", noRawat, namaPenyesuaian, selisih);
            }
            ins(c, "insert into paket_tindakan_pasien values (?,?,now(),?,?,?,?)", noRawat, kodePaket, orderLab, orderRad, jumlah, harga);
            c.commit();

            StringBuilder sb = new StringBuilder("Paket " + namaPaket + " berhasil diinput.\n");
            sb.append("- Tindakan: ").append(nTindakan).append(" item\n");
            if (orderLab != null) sb.append("- Permintaan Lab ").append(orderLab).append(": ").append(nLab).append(" pemeriksaan, ").append(nItemLab).append(" item\n");
            if (orderRad != null) sb.append("- Permintaan Radiologi ").append(orderRad).append(": ").append(nRad).append(" pemeriksaan\n");
            sb.append("- Tarif jenis bayar ").append(nmPj).append(": ").append(nSesuai).append(" item disesuaikan\n");
            if (!tidakCocok.isEmpty()) sb.append("- Tidak ada tarif ").append(nmPj).append(" (pakai tarif paket): ").append(String.join(", ", tidakCocok)).append("\n");
            sb.append("- Jumlah tarif item Rp ").append(rp(jumlah)).append(", harga paket Rp ").append(rp(harga));
            if (selisih != 0) sb.append("\n- ").append(selisih < 0 ? "Potongan" : "Tambahan").append(" Rp ").append(rp(Math.abs(selisih))).append(" (\"").append(namaPenyesuaian).append("\")");
            return sb.toString();
        } catch (Exception e) {
            try { c.rollback(); } catch (Exception ex) { }
            throw new Exception(e.getMessage() == null ? e.toString() : e.getMessage());
        } finally {
            try { c.setAutoCommit(autoAwal); } catch (Exception ex) { }
        }
    }

    // ---------------- riwayat paket yang sudah diberikan ----------------
    private static String status(String kolomNoorder, String alias) {
        return "if(" + kolomNoorder + " is null,'-',if(" + alias + ".noorder is null,'Dihapus',if(" + alias + ".tgl_hasil<>'0000-00-00','Sudah ada hasil',"
                + "if(" + alias + ".tgl_sampel<>'0000-00-00','Proses','Belum'))))";
    }

    /**
     * Paket yang sudah diberikan ke pasien (semua kunjungan), terbaru di atas. Kolom model:
     * Waktu Input, No.Rawat, Kode Paket, Nama Paket, No.Order Lab, Status Lab, No.Order Radiologi, Status Radiologi.
     */
    public static void isiDiberikan(javax.swing.table.DefaultTableModel tm, String noRkmMedis) {
        tm.setRowCount(0);
        try (PreparedStatement ps = koneksiDB.condb().prepareStatement(
                "select p.waktu,p.no_rawat,p.kode_paket,ifnull(t.nama_paket,'-'),ifnull(p.noorder_lab,'')," + status("p.noorder_lab", "l") + ","
                + "ifnull(p.noorder_radiologi,'')," + status("p.noorder_radiologi", "x") + " "
                + "from paket_tindakan_pasien p inner join reg_periksa r on r.no_rawat=p.no_rawat left join paket_tindakan t on t.kode_paket=p.kode_paket "
                + "left join permintaan_lab l on l.noorder=p.noorder_lab left join permintaan_radiologi x on x.noorder=p.noorder_radiologi "
                + "where r.no_rkm_medis=? order by p.waktu desc")) {
            ps.setString(1, noRkmMedis);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) tm.addRow(new Object[]{rs.getString(1).replaceAll("\\.0$", ""), rs.getString(2), rs.getString(3), rs.getString(4),
                    rs.getString(5), rs.getString(6), rs.getString(7), rs.getString(8)});
            }
        } catch (Exception e) {
            System.out.println("Notif Paket Tindakan : " + e.getMessage());
        }
    }

    private static String n(String s) { return s == null ? "" : s; }

    private static void barisRiwayat(javax.swing.table.DefaultTableModel td, List<String> lengkap, String jenis, String nama, String status,
            String hasil, String satuan, String rujukan, String ket, String waktu, String teks) {
        td.addRow(new Object[]{jenis, nama, status, hasil, satuan, rujukan, ket, waktu});
        lengkap.add(teks == null ? "" : teks);
    }

    /**
     * Riwayat & hasil pemeriksaan satu paket yang sudah diberikan: tindakan tercatat, item lab dengan nilai hasilnya,
     * radiologi dengan hasil bacaan. Kolom model: Jenis, Pemeriksaan / Item, Status, Hasil, Satuan, Nilai Rujukan, Keterangan, Waktu.
     * lengkap = teks hasil lengkap per baris (hasil radiologi), sejajar dengan baris model.
     */
    public static void isiRiwayat(javax.swing.table.DefaultTableModel td, List<String> lengkap, String waktu, String noRawat, String orderLab, String orderRad) {
        td.setRowCount(0);
        lengkap.clear();
        Connection c = koneksiDB.condb();
        try {
            // tindakan: dicatat bersamaan dengan input paket
            try (PreparedStatement ps = c.prepareStatement(
                    "select j.nm_perawatan,ifnull(d.nm_dokter,t.kd_dokter),concat(t.tgl_perawatan,' ',t.jam_rawat) from rawat_jl_dr t "
                    + "inner join jns_perawatan j on j.kd_jenis_prw=t.kd_jenis_prw left join dokter d on d.kd_dokter=t.kd_dokter "
                    + "where t.no_rawat=? and timestamp(t.tgl_perawatan,t.jam_rawat) between timestamp(?) - interval 15 second and timestamp(?) + interval 15 second "
                    + "order by j.nm_perawatan")) {
                ps.setString(1, noRawat); ps.setString(2, waktu); ps.setString(3, waktu);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) barisRiwayat(td, lengkap, "Tindakan", rs.getString(1), "Tercatat", "", "", "", "Dokter: " + rs.getString(2), rs.getString(3), null);
                }
            }
            // laboratorium: per pemeriksaan lalu item; nilai dari hasil lab terbaru sesudah permintaan
            if (orderLab != null && !orderLab.equals("")) {
                try (PreparedStatement ps = c.prepareStatement(
                        "select x.kd_jenis_prw,j.nm_perawatan,t.id_template,t.Pemeriksaan,t.satuan,h.nilai,h.nilai_rujukan,h.keterangan,concat(h.tgl_periksa,' ',h.jam) "
                        + "from permintaan_pemeriksaan_lab x inner join permintaan_lab p on p.noorder=x.noorder "
                        + "inner join jns_perawatan_lab j on j.kd_jenis_prw=x.kd_jenis_prw "
                        + "left join permintaan_detail_permintaan_lab dd on dd.noorder=x.noorder and dd.kd_jenis_prw=x.kd_jenis_prw "
                        + "left join template_laboratorium t on t.kd_jenis_prw=dd.kd_jenis_prw and t.id_template=dd.id_template "
                        + "left join detail_periksa_lab h on h.no_rawat=p.no_rawat and h.kd_jenis_prw=dd.kd_jenis_prw and h.id_template=dd.id_template and h.tgl_periksa>=p.tgl_permintaan "
                        + "where x.noorder=? order by j.nm_perawatan,x.kd_jenis_prw,t.urut,t.id_template,h.tgl_periksa desc,h.jam desc")) {
                    ps.setString(1, orderLab);
                    try (ResultSet rs = ps.executeQuery()) {
                        String akhir = "";
                        int judul = -1, ada = 0, total = 0;
                        java.util.Set<String> sudah = new java.util.HashSet<>();
                        while (rs.next()) {
                            if (!rs.getString(1).equals(akhir)) {
                                if (judul >= 0) td.setValueAt(statusLab(ada, total), judul, 2);
                                akhir = rs.getString(1); ada = 0; total = 0;
                                judul = td.getRowCount();
                                barisRiwayat(td, lengkap, "Laboratorium", rs.getString(2), "", "", "", "", "", "", null);
                            }
                            if (rs.getString(3) == null || !sudah.add(akhir + "#" + rs.getString(3))) continue;
                            total++;
                            boolean hasil = rs.getString(6) != null;
                            if (hasil) ada++;
                            barisRiwayat(td, lengkap, "", "      " + rs.getString(4), hasil ? "Sudah" : "Belum", n(rs.getString(6)), n(rs.getString(5)),
                                    n(rs.getString(7)), n(rs.getString(8)), n(rs.getString(9)), null);
                        }
                        if (judul >= 0) td.setValueAt(statusLab(ada, total), judul, 2);
                    }
                }
            }
            // radiologi: hasil bacaan dari hasil_radiologi sesudah permintaan
            if (orderRad != null && !orderRad.equals("")) {
                try (PreparedStatement ps = c.prepareStatement(
                        "select j.nm_perawatan,if(p.tgl_hasil='0000-00-00','',concat(p.tgl_hasil,' ',p.jam_hasil)),"
                        + "(select h.hasil from hasil_radiologi h where h.no_rawat=p.no_rawat and timestamp(h.tgl_periksa,h.jam)>=timestamp(p.tgl_permintaan,p.jam_permintaan) "
                        + "order by (h.tgl_periksa=p.tgl_hasil and h.jam=p.jam_hasil) desc,h.tgl_periksa,h.jam limit 1) "
                        + "from permintaan_pemeriksaan_radiologi x inner join permintaan_radiologi p on p.noorder=x.noorder "
                        + "inner join jns_perawatan_radiologi j on j.kd_jenis_prw=x.kd_jenis_prw where x.noorder=? order by j.nm_perawatan")) {
                    ps.setString(1, orderRad);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            String h = n(rs.getString(3)), ringkas = h.replaceAll("\\s+", " ").trim();
                            if (ringkas.length() > 120) ringkas = ringkas.substring(0, 120) + "...";
                            barisRiwayat(td, lengkap, "Radiologi", rs.getString(1), rs.getString(2).equals("") ? "Belum" : "Sudah", ringkas, "", "",
                                    h.equals("") ? "" : "Klik 2x untuk hasil lengkap", rs.getString(2), h);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Notif Paket Tindakan : " + e.getMessage());
        }
    }

    private static String statusLab(int ada, int total) {
        return total == 0 ? "Belum ada template" : (ada == 0 ? "Belum" : (ada < total ? "Sebagian" : "Sudah"));
    }
}
