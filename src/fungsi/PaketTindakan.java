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

    /** Jurnal tindakan paket: t = {pendapatan, JM dokter, KSO, sarana, BHP, menejemen}; batal = jurnal balik. */
    private static void jurnalTindakan(Connection c, sekuel Sequel, String noRawat, String kodePaket, boolean batal, double[] t) throws Exception {
        if (akuntindakanralan.getSuspen_Piutang_Tindakan_Ralan().equals("")) akuntindakanralan.SetAkunTindakanRalan();
        String[][] akun = { // {debet saat input, kredit saat input}
            {akuntindakanralan.getSuspen_Piutang_Tindakan_Ralan(), akuntindakanralan.getTindakan_Ralan()},
            {akuntindakanralan.getBeban_Jasa_Medik_Dokter_Tindakan_Ralan(), akuntindakanralan.getUtang_Jasa_Medik_Dokter_Tindakan_Ralan()},
            {akuntindakanralan.getBeban_KSO_Tindakan_Ralan(), akuntindakanralan.getUtang_KSO_Tindakan_Ralan()},
            {akuntindakanralan.getBeban_Jasa_Sarana_Tindakan_Ralan(), akuntindakanralan.getUtang_Jasa_Sarana_Tindakan_Ralan()},
            {akuntindakanralan.getHPP_BHP_Tindakan_Ralan(), akuntindakanralan.getPersediaan_BHP_Tindakan_Ralan()},
            {akuntindakanralan.getBeban_Jasa_Menejemen_Tindakan_Ralan(), akuntindakanralan.getUtang_Jasa_Menejemen_Tindakan_Ralan()}
        };
        Map<String, double[]> j = new LinkedHashMap<>();
        for (int i = 0; i < akun.length; i++) {
            jurnal(c, j, akun[i][batal ? 1 : 0], t[i], 0);
            jurnal(c, j, akun[i][batal ? 0 : 1], 0, t[i]);
        }
        try (Statement st = c.createStatement()) { st.executeUpdate("delete from tampjurnal"); }
        for (Map.Entry<String, double[]> e : j.entrySet()) {
            String nm = Sequel.cariIsi("select nm_rek from rekening where kd_rek=?", e.getKey());
            ins(c, "insert into tampjurnal values (?,?,?,?)", e.getKey(), nm, e.getValue()[0], e.getValue()[1]);
        }
        String rm = Sequel.cariIsi("select concat(r.no_rkm_medis,' ',p.nm_pasien) from reg_periksa r inner join pasien p on p.no_rkm_medis=r.no_rkm_medis where r.no_rawat=?", noRawat);
        String ket = batal
                ? "PEMBATALAN TINDAKAN RAWAT JALAN PASIEN " + rm + " (BATAL PAKET TINDAKAN " + kodePaket + "), DIPOSTING OLEH " + akses.getkode()
                : "TINDAKAN RAWAT JALAN PASIEN " + rm + " (PAKET TINDAKAN " + kodePaket + "), DIPOSTING OLEH " + akses.getkode();
        if (!new keuangan.Jurnal().simpanJurnal(noRawat, "U", ket))
            throw new Exception("Gagal menyimpan jurnal tindakan.");
    }

    // ---------------- pencocokan tarif ke jenis bayar pasien (berdasarkan nama) ----------------
    // p/l/r = {kd_jenis_prw paket, nama, kd_poli/kelas}. Mengembalikan kode tarif yang dipakai, atau null bila tidak ada.
    // SQL yang sama dipakai per item (input ke pasien, nilai = "?") dan sekaligus (master paket, nilai = kolom tabel o)
    private static String sqlTindakan(String nama, String pj, String asli, String poliPaket, String poliPasien) {
        return "select j.kd_jenis_prw from jns_perawatan j where j.status='1' and j.nm_perawatan=" + nama + " and (j.kd_pj=" + pj + " or j.kd_pj='-') "
                + "order by (j.kd_pj=" + pj + ") desc,(j.kd_jenis_prw=" + asli + ") desc,(j.kd_poli=" + poliPaket + ") desc,(j.kd_poli=" + poliPasien + ") desc,j.kd_jenis_prw limit 1";
    }

    // Kelas harus sama ('-' dan 'Rawat Jalan' dianggap sama): tanpa syarat ini rawat jalan bisa kena tarif Kelas 1/VVIP.
    private static String sqlPenunjang(String tabel, String syarat, String nama, String pj, String kelas, String asli) {
        return "select j.kd_jenis_prw from " + tabel + " j where j.status='1'" + syarat + " and j.nm_perawatan=" + nama + " and (j.kd_pj=" + pj + " or j.kd_pj='-') "
                + "and (j.kelas=" + kelas + " or (" + kelas + " in ('-','Rawat Jalan') and j.kelas in ('-','Rawat Jalan'))) "
                + "order by (j.kd_pj=" + pj + ") desc,(j.kelas=" + kelas + ") desc,(j.kd_jenis_prw=" + asli + ") desc,j.kd_jenis_prw limit 1";
    }

    private static String cocokTindakan(Connection c, String[] p, String kdPj, String kdPoli) throws Exception {
        return satu(c, sqlTindakan("?", "?", "?", "?", "?"), p[1], kdPj, kdPj, p[0], p[2], kdPoli);
    }

    private static String cocokLab(Connection c, String[] l, String kdPj) throws Exception {
        return satu(c, sqlPenunjang("jns_perawatan_lab", " and j.kategori='PK'", "?", "?", "?", "?"), l[1], kdPj, l[2], l[2], kdPj, l[2], l[0]);
    }

    private static String cocokRad(Connection c, String[] r, String kdPj) throws Exception {
        return satu(c, sqlPenunjang("jns_perawatan_radiologi", "", "?", "?", "?", "?"), r[1], kdPj, r[2], r[2], kdPj, r[2], r[0]);
    }

    private static String tanda(int n) {
        return String.join(",", java.util.Collections.nCopies(n, "?"));
    }

    /**
     * Padanan banyak kode tarif di satu jenis bayar, satu query (master paket). jenis: T, L, R.
     * Hasil: kode asal -&gt; {kode padanan atau null, nama}.
     */
    public static Map<String, String[]> sesuaikan(String jenis, java.util.Collection<String> kode, String kdPj) throws Exception {
        Map<String, String[]> h = new LinkedHashMap<>();
        if (kode.isEmpty()) return h;
        String tabel, sub;
        switch (jenis) {
            case "T": tabel = "jns_perawatan"; sub = sqlTindakan("o.nm_perawatan", "?", "o.kd_jenis_prw", "o.kd_poli", "o.kd_poli"); break;
            case "L": tabel = "jns_perawatan_lab"; sub = sqlPenunjang(tabel, " and j.kategori='PK'", "o.nm_perawatan", "?", "o.kelas", "o.kd_jenis_prw"); break;
            default: tabel = "jns_perawatan_radiologi"; sub = sqlPenunjang(tabel, "", "o.nm_perawatan", "?", "o.kelas", "o.kd_jenis_prw");
        }
        List<Object> v = new ArrayList<>();
        v.add(kdPj); v.add(kdPj); v.addAll(kode);
        for (String[] r : baris(koneksiDB.condb(), "select o.kd_jenis_prw,(" + sub + "),o.nm_perawatan from " + tabel + " o where o.kd_jenis_prw in (" + tanda(kode.size()) + ")", v.toArray()))
            h.put(r[0], new String[]{r[1], r[2]});
        return h;
    }

    /**
     * Salin template hasil lab ke kode yang belum punya template, dari jenis bayar dariPj (nama &amp; kategori sama,
     * item terbanyak lalu rawat jalan). Kode yang sudah punya template tidak disentuh.
     * Mengembalikan nama pemeriksaan yang tidak punya template di jenis bayar sumber.
     */
    public static List<String> salinTemplate(java.util.Collection<String> tujuan, String dariPj) throws Exception {
        List<String> tanpaSumber = new ArrayList<>();
        if (tujuan.isEmpty()) return tanpaSumber;
        Connection c = koneksiDB.condb();
        boolean autoAwal = c.getAutoCommit();
        c.setAutoCommit(false);
        try {
            List<Object> v = new ArrayList<>();
            v.add(dariPj); v.addAll(tujuan);
            List<String[]> pasang = baris(c, "select o.kd_jenis_prw,(select j.kd_jenis_prw from jns_perawatan_lab j inner join template_laboratorium t on t.kd_jenis_prw=j.kd_jenis_prw "
                    + "where j.status='1' and j.kd_pj=? and j.nm_perawatan=o.nm_perawatan and j.kategori=o.kategori "
                    + "group by j.kd_jenis_prw,j.kelas order by count(*) desc,(j.kelas='-') desc,j.kd_jenis_prw limit 1),o.nm_perawatan "
                    + "from jns_perawatan_lab o where o.kd_jenis_prw in (" + tanda(tujuan.size()) + ") "
                    + "and not exists(select 1 from template_laboratorium x where x.kd_jenis_prw=o.kd_jenis_prw)", v.toArray());
            List<Object> asal = new ArrayList<>(), ke = new ArrayList<>(), pasangan = new ArrayList<>();
            for (String[] r : pasang) {
                if (r[1] == null) { tanpaSumber.add(r[2]); continue; }
                asal.add(r[1]); ke.add(r[0]); pasangan.add(r[1]); pasangan.add(r[0]);
            }
            if (!ke.isEmpty()) {
                String sql = "insert into template_laboratorium (kd_jenis_prw,Pemeriksaan,satuan,nilai_rujukan_ld,nilai_rujukan_la,nilai_rujukan_pd,nilai_rujukan_pa,"
                        + "method,bagian_rs,bhp,bagian_perujuk,bagian_dokter,bagian_laborat,kso,menejemen,biaya_item,urut) "
                        + "select j.kd_jenis_prw,s.Pemeriksaan,s.satuan,s.nilai_rujukan_ld,s.nilai_rujukan_la,s.nilai_rujukan_pd,s.nilai_rujukan_pa,"
                        + "ifnull(s.method,''),s.bagian_rs,s.bhp,s.bagian_perujuk,s.bagian_dokter,s.bagian_laborat,s.kso,s.menejemen,s.biaya_item,s.urut "
                        + "from template_laboratorium s inner join jns_perawatan_lab j where s.kd_jenis_prw in (" + tanda(asal.size()) + ") "
                        + "and j.kd_jenis_prw in (" + tanda(ke.size()) + ") and ("
                        + String.join(" or ", java.util.Collections.nCopies(ke.size(), "(s.kd_jenis_prw=? and j.kd_jenis_prw=?)")) + ") "
                        + "order by j.kd_jenis_prw,s.urut,s.id_template";
                List<Object> semua = new ArrayList<>(asal);
                semua.addAll(ke); semua.addAll(pasangan);
                ins(c, sql, semua.toArray());
                new sekuel().SimpanTrack("salin template_laboratorium dari " + asal + " ke " + ke);
            }
            c.commit();
        } catch (Exception e) {
            try { c.rollback(); } catch (Exception ex) { }
            throw e;
        } finally {
            c.setAutoCommit(autoAwal);
        }
        return tanpaSumber;
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
            if (kd == null || (!kd.equals(l[0]) && itemBaru(c, kd, itemLab(c, kodePaket, l[0])) == null)) {
                h.add("Laboratorium: " + l[1]);
                kd = l[0]; // yang dipakai = kode paket, template-nya tetap harus ada
            }
            if (angka(c, "select count(*) from template_laboratorium where kd_jenis_prw=?", kd) == 0)
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
            if (ttlTindakan > 0)
                jurnalTindakan(c, Sequel, noRawat, kodePaket, false, new double[]{ttlTindakan, ttlJm, ttlKso, ttlSarana, ttlBhp, ttlMen});

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
            // waktu = jam PC yang sama persis dengan tindakan & permintaan (bukan now() server), dipakai mengenali isi paket
            ins(c, "insert into paket_tindakan_pasien values (?,?,?,?,?,?,?)", noRawat, kodePaket, tgl + " " + jam, orderLab, orderRad, jumlah, harga);
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
                "select date_format(p.waktu,'%Y-%m-%d %H:%i:%s'),p.no_rawat,p.kode_paket,ifnull(t.nama_paket,'-'),ifnull(p.noorder_lab,'')," + status("p.noorder_lab", "l") + ","
                + "ifnull(p.noorder_radiologi,'')," + status("p.noorder_radiologi", "x") + " "
                + "from paket_tindakan_pasien p inner join reg_periksa r on r.no_rawat=p.no_rawat left join paket_tindakan t on t.kode_paket=p.kode_paket "
                + "left join permintaan_lab l on " + milikPaket("l", "noorder_lab") + " left join permintaan_radiologi x on " + milikPaket("x", "noorder_radiologi") + " "
                + "where r.no_rkm_medis=? order by p.waktu desc")) {
            ps.setString(1, noRkmMedis);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) tm.addRow(new Object[]{rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4),
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
    public static void isiRiwayat(javax.swing.table.DefaultTableModel td, List<String> lengkap, String noRawat, String kodePaket) {
        td.setRowCount(0);
        lengkap.clear();
        Connection c = koneksiDB.condb();
        try {
            PaketPasien pk = paketPasien(c, noRawat, kodePaket);
            if (pk == null) return;
            String orderLab = pk.orderLab, orderRad = pk.orderRad;
            // tindakan: jam sama persis dengan permintaan paket (sama dengan yang dihapus bila paket dihapus)
            for (String[] t : tindakanPaket(c, pk))
                barisRiwayat(td, lengkap, "Tindakan", t[4], "Tercatat", "", "", "", "Dokter: " + t[5], t[2] + " " + t[3], null);
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

    /**
     * Permintaan alias a benar milik paket p. Nomor permintaan bisa terpakai ulang (max+1) setelah permintaan dihapus,
     * jadi nomor saja tidak cukup: kunjungan harus sama, dibuat oleh paket, dan bukan milik paket yang lebih baru.
     */
    private static String milikPaket(String a, String kolom) {
        return a + ".noorder=p." + kolom + " and " + a + ".no_rawat=p.no_rawat and (" + a + ".informasi_tambahan like 'Paket Tindakan%' or timestamp("
                + a + ".tgl_permintaan," + a + ".jam_permintaan)=p.waktu) and not exists(select 1 from paket_tindakan_pasien q "
                + "where q.no_rawat=p.no_rawat and q." + kolom + "=p." + kolom + " and q.waktu>p.waktu)";
    }

    private static final class PaketPasien {
        String noRawat, kode, nama, tgl, jam, orderLab, orderRad, infoLab, infoRad, nomorLab, nomorRad;
        boolean sampelLab, sampelRad;
        double jumlah, harga;
    }

    private static PaketPasien paketPasien(Connection c, String noRawat, String kodePaket) throws Exception {
        List<String[]> h = baris(c, "select date_format(p.waktu,'%Y-%m-%d %H:%i:%s'),p.noorder_lab,p.noorder_radiologi,p.jumlah_tarif,p.harga_paket,ifnull(t.nama_paket,p.kode_paket),"
                + "l.noorder,l.informasi_tambahan,concat(l.tgl_permintaan,' ',l.jam_permintaan),if(l.tgl_sampel<>'0000-00-00','1','0'),"
                + "x.noorder,x.informasi_tambahan,concat(x.tgl_permintaan,' ',x.jam_permintaan),if(x.tgl_sampel<>'0000-00-00','1','0') "
                + "from paket_tindakan_pasien p left join paket_tindakan t on t.kode_paket=p.kode_paket "
                + "left join permintaan_lab l on " + milikPaket("l", "noorder_lab") + " "
                + "left join permintaan_radiologi x on " + milikPaket("x", "noorder_radiologi") + " "
                + "where p.no_rawat=? and p.kode_paket=?", noRawat, kodePaket);
        if (h.isEmpty()) return null;
        String[] r = h.get(0);
        PaketPasien p = new PaketPasien();
        p.noRawat = noRawat; p.kode = kodePaket;
        p.nomorLab = n(r[1]); p.nomorRad = n(r[2]);
        p.jumlah = Double.parseDouble(r[3]); p.harga = Double.parseDouble(r[4]); p.nama = r[5];
        p.orderLab = n(r[6]); p.infoLab = n(r[7]); p.sampelLab = "1".equals(r[9]);
        p.orderRad = n(r[10]); p.infoRad = n(r[11]); p.sampelRad = "1".equals(r[13]);
        // jam paket: data 202611.29-30 menyimpan waktu = jam server, jadi utamakan jam permintaan (jam PC, sama dengan tindakan)
        String w = r[8] != null ? r[8] : (r[12] != null ? r[12] : n(r[0]));
        if (w.length() < 19) throw new Exception("Waktu input paket tidak terbaca.");
        p.tgl = w.substring(0, 10); p.jam = w.substring(11, 19);
        return p;
    }

    /** {kd_jenis_prw, kd_dokter, tgl, jam, nama, dokter, material, bhp, jm dokter, kso, menejemen, biaya, stts_bayar} */
    private static List<String[]> tindakanPaket(Connection c, PaketPasien p) throws Exception {
        return baris(c, "select t.kd_jenis_prw,t.kd_dokter,date_format(t.tgl_perawatan,'%Y-%m-%d'),time_format(t.jam_rawat,'%H:%i:%s'),j.nm_perawatan,ifnull(d.nm_dokter,t.kd_dokter),"
                + "t.material,t.bhp,t.tarif_tindakandr,t.kso,t.menejemen,t.biaya_rawat,t.stts_bayar from rawat_jl_dr t "
                + "inner join jns_perawatan j on j.kd_jenis_prw=t.kd_jenis_prw left join dokter d on d.kd_dokter=t.kd_dokter "
                + "where t.no_rawat=? and t.tgl_perawatan=? and t.jam_rawat=? order by j.nm_perawatan", p.noRawat, p.tgl, p.jam);
    }

    // ---------------- hapus paket yang sudah diberikan ----------------
    private static final class RencanaHapus {
        PaketPasien p;
        List<String[]> tindakan;
        String tabelSesuai, kolomSesuai, nilaiSesuai, namaSesuai;
        double hapusSesuai, sisaSesuai;
        final List<String> isi = new ArrayList<>(), catatan = new ArrayList<>();
    }

    private static void cekPermintaan(Connection c, RencanaHapus r, boolean lab, boolean admin) throws Exception {
        PaketPasien p = r.p;
        String judul = lab ? "Lab" : "Radiologi", order = lab ? p.orderLab : p.orderRad, nomor = lab ? p.nomorLab : p.nomorRad;
        String akhiran = lab ? "lab" : "radiologi";
        if (nomor.equals("")) return;
        if (order.equals("")) {
            r.catatan.add("Permintaan " + judul + " " + nomor + " sudah dihapus atau bukan lagi milik paket ini, jadi tidak disentuh.");
            return;
        }
        double bayar = angka(c, "select count(*) from permintaan_pemeriksaan_" + akhiran + " where noorder=? and stts_bayar='Sudah'", order);
        if (lab) bayar += angka(c, "select count(*) from permintaan_detail_permintaan_lab where noorder=? and stts_bayar='Sudah'", order);
        if (bayar > 0) throw new Exception("Permintaan " + judul + " " + order + " sudah dibayar. Hubungi kasir.");
        // tgl_hasil tidak dikosongkan Khanza saat hasil dihapus, jadi yang diperiksa baris hasilnya
        double hasil = angka(c, "select count(*) from periksa_" + akhiran + " h inner join permintaan_" + akhiran + " o on o.no_rawat=h.no_rawat "
                + "where o.noorder=? and ((h.tgl_periksa=o.tgl_hasil and h.jam=o.jam_hasil) or (h.tgl_periksa>=o.tgl_permintaan "
                + "and exists(select 1 from permintaan_pemeriksaan_" + akhiran + " i where i.noorder=o.noorder and i.kd_jenis_prw=h.kd_jenis_prw) "
                + "and not exists(select 1 from permintaan_" + akhiran + " z where z.no_rawat=o.no_rawat and z.noorder<>o.noorder and z.tgl_hasil=h.tgl_periksa and z.jam_hasil=h.jam)))", order);
        if (hasil > 0) throw new Exception("Hasil " + judul + " untuk permintaan " + order + " sudah diinput.\nMinta petugas " + judul + " menghapus hasilnya dulu, lalu ulangi hapus paket.");
        if (lab ? p.sampelLab : p.sampelRad) {
            if (!admin) throw new Exception("Sampel " + judul + " permintaan " + order + " sudah diambil. Hanya Admin Utama yang boleh menghapus.");
            r.catatan.add("Sampel " + judul + " sudah diambil, beri tahu petugas " + judul + ".");
        }
        if (angka(c, "select count(*) from satu_sehat_servicerequest_" + akhiran + " where noorder=?", order) > 0)
            r.catatan.add("Permintaan " + judul + " sudah terkirim ke SatuSehat dan tidak ikut terhapus di sana.");
        r.isi.add("Permintaan " + judul + " " + order + ": " + (int) angka(c, "select count(*) from permintaan_pemeriksaan_" + akhiran + " where noorder=?", order) + " pemeriksaan");
    }

    /** Periksa semua syarat dan kumpulkan yang akan dihapus; melempar Exception bila tidak boleh dihapus. */
    private static RencanaHapus rencanaHapus(Connection c, sekuel Sequel, String noRawat, String kodePaket, boolean admin) throws Exception {
        RencanaHapus r = new RencanaHapus();
        PaketPasien p = paketPasien(c, noRawat, kodePaket);
        if (p == null) throw new Exception("Paket ini sudah tidak ada pada pasien (mungkin sudah dihapus user lain).");
        r.p = p;
        if (Sequel.cariRegistrasi(noRawat) > 0)
            throw new Exception("Billing sudah disimpan atau registrasi berstatus Batal, paket tidak boleh dihapus.\nHubungi bagian kasir/keuangan.");
        if (!admin && System.currentTimeMillis() - new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(p.tgl + " " + p.jam).getTime() > 172800000L)
            throw new Exception("Paket sudah lebih dari 2 x 24 jam. Hanya Admin Utama yang boleh menghapus.");

        r.tindakan = tindakanPaket(c, p);
        StringBuilder nama = new StringBuilder();
        for (int i = 0; i < r.tindakan.size(); i++) {
            String[] t = r.tindakan.get(i);
            if ("Sudah".equals(t[12])) throw new Exception("Tindakan " + t[4] + " sudah dibayar. Hubungi kasir.");
            nama.append(i == 0 ? "" : (i % 4 == 0 ? ",\n     " : ", ")).append(t[4]);
        }
        int diPaket = (int) angka(c, "select count(*) from paket_tindakan_tindakan where kode_paket=?", kodePaket);
        if (r.tindakan.isEmpty() && diPaket > 0 && angka(c, "select count(*) from rawat_jl_dr t inner join jns_perawatan j on j.kd_jenis_prw=t.kd_jenis_prw "
                + "where t.no_rawat=? and j.nm_perawatan in (select k.nm_perawatan from paket_tindakan_tindakan i inner join jns_perawatan k on k.kd_jenis_prw=i.kd_jenis_prw where i.kode_paket=?)", noRawat, kodePaket) > 0)
            throw new Exception("Tindakan paket tidak bisa dikenali (jamnya sudah diubah).\nHapus dulu tindakannya di Tindakan Rawat Jalan, lalu ulangi hapus paket.");
        if (!r.tindakan.isEmpty()) r.isi.add(r.tindakan.size() + " tindakan (jurnal dibalik): " + nama);
        if (r.tindakan.size() < diPaket)
            r.catatan.add("Hanya " + r.tindakan.size() + " dari " + diPaket + " tindakan paket ditemukan, cek sisanya di Tindakan Rawat Jalan.");

        cekPermintaan(c, r, true, admin);
        cekPermintaan(c, r, false, admin);

        double selisih = Math.round(p.harga - p.jumlah);
        if (selisih != 0) {
            boolean potongan = selisih < 0;
            double nilai = Math.abs(selisih);
            String tabel = potongan ? "pengurangan_biaya" : "tambahan_biaya", kolom = potongan ? "nama_pengurangan" : "nama_biaya";
            String besar = potongan ? "besar_pengurangan" : "besar_biaya", label = potongan ? "Potongan" : "Tambahan";
            List<String[]> ada = baris(c, "select " + kolom + "," + besar + " from " + tabel + " where no_rawat=? and " + kolom + " like 'Paket Tindakan%'", noRawat);
            String sekarang = "Paket Tindakan " + p.nama;
            if (sekarang.length() > 60) sekarang = sekarang.substring(0, 60);
            String[] kena = null;
            for (String calon : new String[]{p.infoLab, p.infoRad, sekarang})
                for (String[] a : ada) if (kena == null && a[0].trim().equalsIgnoreCase(calon.trim())) kena = a;
            // nama paket sudah diganti: hanya dipakai bila tidak mungkin milik paket lain
            if (kena == null && ada.size() == 1 && angka(c, "select count(*) from paket_tindakan_pasien where no_rawat=?", noRawat) == 1) kena = ada.get(0);
            if (kena == null) {
                r.catatan.add(label + " Rp " + rp(nilai) + " tidak ditemukan (mungkin sudah dihapus), cek billing.");
            } else {
                double sisa = Double.parseDouble(kena[1]) - nilai;
                if (sisa < -0.5) throw new Exception(label + " \"" + kena[0] + "\" sudah diubah manual dari Rp " + rp(nilai) + " menjadi Rp " + rp(Double.parseDouble(kena[1]))
                        + ".\nHapus dulu baris itu di " + label + " Biaya, lalu ulangi hapus paket.");
                r.tabelSesuai = tabel; r.kolomSesuai = kolom; r.nilaiSesuai = besar; r.namaSesuai = kena[0];
                r.hapusSesuai = nilai; r.sisaSesuai = sisa;
                r.isi.add(label + " biaya Rp " + rp(nilai) + (sisa > 0.5 ? " (sisa Rp " + rp(sisa) + " tetap ada)" : ""));
            }
        }
        return r;
    }

    /** Ringkasan yang akan dihapus, untuk konfirmasi. Tidak mengubah data. */
    public static String ringkasanHapus(String noRawat, String kodePaket, boolean admin) throws Exception {
        RencanaHapus r = rencanaHapus(koneksiDB.condb(), new sekuel(), noRawat, kodePaket, admin);
        StringBuilder sb = new StringBuilder("Hapus paket \"" + r.p.nama + "\" dari kunjungan " + noRawat + "?\n\nYang dihapus/dibatalkan:");
        if (r.isi.isEmpty()) sb.append("\n- catatan pemberian paket saja");
        for (String s : r.isi) sb.append("\n- ").append(s);
        if (!r.catatan.isEmpty()) sb.append("\n\nPerhatian:");
        for (String s : r.catatan) sb.append("\n- ").append(s);
        return sb.toString();
    }

    private static int hapusBaris(Connection c, sekuel Sequel, String sql, Object... v) throws Exception {
        int n;
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < v.length; i++) ps.setObject(i + 1, v[i]);
            n = ps.executeUpdate();
        }
        Sequel.SimpanTrack(sql + " " + java.util.Arrays.toString(v));
        return n;
    }

    /**
     * Hapus paket dari pasien: tindakan + jurnal balik, permintaan lab/radiologi, potongan/tambahan, catatan paket.
     * Semua dalam satu transaksi dan syarat diperiksa ulang. admin = Admin Utama (bebas batas 2x24 jam & sampel).
     */
    public static void hapus(String noRawat, String kodePaket, boolean admin) throws Exception {
        Connection c = koneksiDB.condb();
        sekuel Sequel = new sekuel();
        boolean autoAwal = c.getAutoCommit();
        c.setAutoCommit(false);
        try {
            baris(c, "select kode_paket from paket_tindakan_pasien where no_rawat=? and kode_paket=? for update", noRawat, kodePaket);
            RencanaHapus r = rencanaHapus(c, Sequel, noRawat, kodePaket, admin);
            double[] ttl = new double[6];
            for (String[] t : r.tindakan) {
                if (hapusBaris(c, Sequel, "delete from rawat_jl_dr where no_rawat=? and kd_jenis_prw=? and kd_dokter=? and tgl_perawatan=? and jam_rawat=?",
                        noRawat, t[0], t[1], t[2], t[3]) != 1)
                    throw new Exception("Tindakan " + t[4] + " berubah saat proses hapus, silakan ulangi.");
                int[] kolom = {11, 8, 9, 6, 7, 10}; // pendapatan, JM dokter, KSO, sarana, BHP, menejemen
                for (int i = 0; i < kolom.length; i++) if (t[kolom[i]] != null) ttl[i] += Double.parseDouble(t[kolom[i]]);
            }
            if (!r.tindakan.isEmpty() && (ttl[0] > 0 || angka(c, "select count(*) from jurnal where no_bukti=? and keterangan like ?",
                    noRawat, "%(PAKET TINDAKAN " + kodePaket + ")%") > 0))
                jurnalTindakan(c, Sequel, noRawat, kodePaket, true, ttl);
            if (!r.p.orderLab.equals("")) hapusBaris(c, Sequel, "delete from permintaan_lab where noorder=? and no_rawat=?", r.p.orderLab, noRawat);
            if (!r.p.orderRad.equals("")) hapusBaris(c, Sequel, "delete from permintaan_radiologi where noorder=? and no_rawat=?", r.p.orderRad, noRawat);
            if (r.tabelSesuai != null) {
                String syarat = " where no_rawat=? and " + r.kolomSesuai + "=?";
                if (r.sisaSesuai > 0.5) hapusBaris(c, Sequel, "update " + r.tabelSesuai + " set " + r.nilaiSesuai + "=" + r.nilaiSesuai + "-?" + syarat, r.hapusSesuai, noRawat, r.namaSesuai);
                else hapusBaris(c, Sequel, "delete from " + r.tabelSesuai + syarat, noRawat, r.namaSesuai);
            }
            if (hapusBaris(c, Sequel, "delete from paket_tindakan_pasien where no_rawat=? and kode_paket=?", noRawat, kodePaket) != 1)
                throw new Exception("Catatan paket berubah saat proses hapus, silakan ulangi.");
            c.commit();
        } catch (Exception e) {
            try { c.rollback(); } catch (Exception ex) { }
            throw new Exception(e.getMessage() == null ? e.toString() : e.getMessage());
        } finally {
            try { c.setAutoCommit(autoAwal); } catch (Exception ex) { }
        }
    }

    private static String statusLab(int ada, int total) {
        return total == 0 ? "Belum ada template" : (ada == 0 ? "Belum" : (ada < total ? "Sebagian" : "Sudah"));
    }
}
