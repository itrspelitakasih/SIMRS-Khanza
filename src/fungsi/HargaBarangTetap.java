package fungsi;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * RSPK: Harga Barang Tetap.
 * Barang yang terdaftar di tabel set_harga_barang_tetap (mis. Oksigen per liter) memakai
 * harga tetap per kelas, tidak ikut kenaikan persen per penjamin (set_harga_obat_ralan/ranap)
 * dan tidak ikut harga per kelas di databarang (yang ditimpa saat penerimaan barang).
 *
 * Dipakai di query harga form input obat: potongan SQL dari kelas ini menggantikan
 * "(tabel.h_beli+(tabel.h_beli*?))" dan "tabel.kelas1" dst. Bila tabel belum ada dan tidak
 * bisa dibuat, SQL asli dikembalikan apa adanya sehingga form tetap berjalan seperti semula.
 */
public final class HargaBarangTetap {
    private static volatile Boolean aktif = null;

    private HargaBarangTetap() {}

    public static final String DDL =
        "CREATE TABLE IF NOT EXISTS set_harga_barang_tetap ("+
        "kode_brng varchar(15) NOT NULL,"+
        "ralan double NOT NULL DEFAULT 0, kelas1 double NOT NULL DEFAULT 0, kelas2 double NOT NULL DEFAULT 0,"+
        "kelas3 double NOT NULL DEFAULT 0, utama double NOT NULL DEFAULT 0, vip double NOT NULL DEFAULT 0,"+
        "vvip double NOT NULL DEFAULT 0, ikut_diskon enum('Ya','Tidak') NOT NULL DEFAULT 'Tidak',"+
        "PRIMARY KEY (kode_brng),"+
        "CONSTRAINT set_harga_barang_tetap_ibfk_1 FOREIGN KEY (kode_brng) REFERENCES databarang (kode_brng) ON DELETE CASCADE ON UPDATE CASCADE"+
        ") ENGINE=InnoDB";

    /** true bila tabel set_harga_barang_tetap tersedia (dibuat otomatis sekali per sesi). */
    public static boolean aktif() {
        if (aktif == null) {
            synchronized (HargaBarangTetap.class) {
                if (aktif == null) {
                    boolean ok = false;
                    try {
                        Connection c = koneksiDB.condb();
                        try (Statement st = c.createStatement()) {
                            st.execute(DDL);
                            ok = true;
                        } catch (Exception e) {
                            try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery("select kode_brng from set_harga_barang_tetap limit 1")) {
                                ok = true;
                            } catch (Exception e2) {
                                System.out.println("Notif Harga Barang Tetap : tabel tidak tersedia - " + e2.getMessage());
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("Notif Harga Barang Tetap : " + e);
                    }
                    aktif = ok;
                }
            }
        }
        return aktif;
    }

    /** Nama kolom harga tetap untuk kelas kamar ("Kelas 1" -> kelas1, "Kelas VIP" -> vip, dst). */
    public static String kolomKelas(String kelas) {
        if (kelas == null) return "ralan";
        switch (kelas.trim()) {
            case "Kelas 1": return "kelas1";
            case "Kelas 2": return "kelas2";
            case "Kelas 3": return "kelas3";
            case "Kelas Utama": return "utama";
            case "Kelas VIP": return "vip";
            case "Kelas VVIP": return "vvip";
            default: return "ralan";
        }
    }

    private static boolean kolomSah(String k) {
        return k.equals("ralan") || k.equals("kelas1") || k.equals("kelas2") || k.equals("kelas3")
            || k.equals("utama") || k.equals("vip") || k.equals("vvip");
    }

    /** Pengganti "(tabel.h_beli+(tabel.h_beli*?))": harga tetap bila ada, selain itu harga beli + kenaikan penjamin. */
    public static String markup(String tabel, String kolom) {
        String asli = "(" + tabel + ".h_beli+(" + tabel + ".h_beli*?))";
        if (!aktif() || !kolomSah(kolom)) return asli;
        return "ifnull((select hbt." + kolom + " from set_harga_barang_tetap hbt where hbt.kode_brng=" + tabel + ".kode_brng)," + asli + ")";
    }

    /** Pengganti "tabel.kelas1" di daftar kolom select (alias tetap nama kolom). */
    public static String kolom(String tabel, String kolom) {
        String asli = tabel + "." + kolom;
        if (!aktif() || !kolomSah(kolom)) return asli;
        return "ifnull((select hbt." + kolom + " from set_harga_barang_tetap hbt where hbt.kode_brng=" + tabel + ".kode_brng)," + asli + ") as " + kolom;
    }

    /** Sama dengan kolom() tetapi tanpa alias, untuk posisi "tabel.ralan as harga". */
    public static String nilai(String tabel, String kolom) {
        String asli = tabel + "." + kolom;
        if (!aktif() || !kolomSah(kolom)) return asli;
        return "ifnull((select hbt." + kolom + " from set_harga_barang_tetap hbt where hbt.kode_brng=" + tabel + ".kode_brng)," + asli + ")";
    }

    /** Subquery harga tetap (bisa NULL) untuk dibaca di Java, mis. "... as hargatetap". */
    public static String tetap(String tabel, String kolom) {
        if (!aktif() || !kolomSah(kolom)) return "null";
        return "(select hbt." + kolom + " from set_harga_barang_tetap hbt where hbt.kode_brng=" + tabel + ".kode_brng)";
    }

    /** true bila barang memakai harga tetap dan diset ikut diskon penjamin. */
    public static boolean ikutDiskon(String kodeBrng) {
        if (!aktif()) return true;
        try (java.sql.PreparedStatement ps = koneksiDB.condb().prepareStatement("select ikut_diskon from set_harga_barang_tetap where kode_brng=?")) {
            ps.setString(1, kodeBrng);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return "Ya".equals(rs.getString(1));
            }
        } catch (Exception e) {
            System.out.println("Notif Harga Barang Tetap : " + e);
        }
        return true;
    }
}
