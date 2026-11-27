package fungsi;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * RSPK: Diskon per Jenis Bayar (penjab) per kategori (Obat, Alkes, Tindakan, Operasi, Laboratorium,
 * Radiologi, Kamar). Persen diatur di Setup Harga Obat -> tab "Diskon Jenis Bayar" (tabel set_diskon_penjab).
 *
 * Saat billing ralan/ranap dibuka dan belum disimpan, terapkan() menghitung subtotal tiap kategori
 * untuk no_rawat dan menulis baris Potongan di pengurangan_biaya, mis. "Diskon Obat 5% (Jenis Bayar)".
 * Baris diskon otomatis lama selalu dihapus dulu sehingga ikut berubah bila tagihan berubah.
 * Alkes = barang berkategori "Alkes &amp; BHP" (KATEGORI_ALKES). Barang di Harga Barang Tetap dengan ikut_diskon='Tidak'
 * tidak dihitung dalam diskon Obat/Alkes. Diskon Obat/Alkes dihitung per obat per tanggal (seperti nota PAMA),
 * 2 desimal, lalu dijumlah TANPA dibulatkan ke rupiah (potongan billing bisa berdesimal).
 * Nota PAMA setelah diskon yang membulatkan per obat (0,5 dianggap 1, lihat diskonBaris()).
 */
public final class DiskonPenjamin {
    private static volatile Boolean aktif = null;
    public static final String[] KATEGORI = {"Obat", "Alkes", "Tindakan", "Operasi", "Laboratorium", "Radiologi", "Kamar"};
    private static final String PENANDA = "(Jenis Bayar)";

    private DiskonPenjamin() {}

    public static final String DDL =
        "CREATE TABLE IF NOT EXISTS set_diskon_penjab ("+
        "kd_pj char(3) NOT NULL,"+
        "jenis_rawat enum('Ralan','Ranap') NOT NULL DEFAULT 'Ranap',"+
        "kategori enum('Obat','Alkes','Tindakan','Operasi','Laboratorium','Radiologi','Kamar') NOT NULL,"+
        "persen double NOT NULL DEFAULT 0,"+
        "PRIMARY KEY (kd_pj,jenis_rawat,kategori),"+
        "CONSTRAINT set_diskon_penjab_ibfk_1 FOREIGN KEY (kd_pj) REFERENCES penjab (kd_pj) ON DELETE CASCADE ON UPDATE CASCADE"+
        ") ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci";

    /** Catatan diskon otomatis yang pernah diterapkan per no_rawat & kategori. Bila barisnya lalu dihapus kasir, tidak dibuat ulang. */
    public static final String DDL_LOG =
        "CREATE TABLE IF NOT EXISTS diskon_jenis_bayar_log ("+
        "no_rawat varchar(17) NOT NULL,"+
        "kategori varchar(20) NOT NULL,"+
        "waktu datetime DEFAULT NULL,"+
        "PRIMARY KEY (no_rawat,kategori)"+
        ") ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci";

    public static boolean aktif() {
        if (aktif == null) {
            synchronized (DiskonPenjamin.class) {
                if (aktif == null) {
                    boolean ok = false;
                    Connection c = koneksiDB.condb();
                    try (Statement st = c.createStatement()) {
                        st.execute(DDL);
                        ok = true;
                        try { st.execute(DDL_LOG); } catch (Exception e3) { System.out.println("Notif Diskon Jenis Bayar : log - " + e3.getMessage()); }
                        // Migrasi tabel versi 202611.24 (tanpa jenis_rawat): setting lama dianggap untuk Ranap
                        try (ResultSet rk = st.executeQuery("show columns from set_diskon_penjab like 'jenis_rawat'")) {
                            if (!rk.next()) {
                                try (Statement st2 = c.createStatement()) {
                                    st2.execute("alter table set_diskon_penjab add column jenis_rawat enum('Ralan','Ranap') NOT NULL DEFAULT 'Ranap' after kd_pj, "
                                            + "drop primary key, add primary key (kd_pj,jenis_rawat,kategori)");
                                }
                            }
                        } catch (Exception e4) { System.out.println("Notif Diskon Jenis Bayar : migrasi - " + e4.getMessage()); }
                    } catch (Exception e) {
                        System.out.println("Notif Diskon Jenis Bayar : gagal membuat tabel - " + e.getMessage());
                        try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery("select kd_pj from set_diskon_penjab limit 1")) {
                            ok = true;
                        } catch (Exception e2) {
                            System.out.println("Notif Diskon Penjamin : tabel tidak tersedia - " + e2.getMessage());
                        }
                    }
                    aktif = ok;
                }
            }
        }
        return aktif;
    }

    /** Ringkasan setting diskon jenis bayar pasien, mis. "Obat 5%, Alkes 3%". Kosong bila tidak ada. */
    public static String ringkasan(String noRawat, String jenisRawat) {
        if (noRawat == null || noRawat.trim().equals("") || !aktif()) return "";
        StringBuilder sb = new StringBuilder();
        try (PreparedStatement ps = koneksiDB.condb().prepareStatement(
                "select d.kategori,d.persen from set_diskon_penjab d inner join reg_periksa r on r.kd_pj=d.kd_pj where r.no_rawat=? and d.jenis_rawat=? and d.persen>0 "
                + "order by field(d.kategori,'Obat','Alkes','Tindakan','Operasi','Laboratorium','Radiologi','Kamar')")) {
            ps.setString(1, noRawat);
            ps.setString(2, jenisRawat);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (sb.length() > 0) sb.append(", ");
                    sb.append(rs.getString(1)).append(" ").append(teksPersen(rs.getDouble(2))).append("%");
                }
            }
        } catch (Exception e) {
            System.out.println("Notif Diskon Jenis Bayar : " + e.getMessage());
        }
        return sb.toString();
    }

    /** Teks persen gaya Indonesia: 5 -> "5", 2.5 -> "2,5". */
    public static String teksPersen(double p) {
        if (p == Math.rint(p)) return String.valueOf((long) p);
        return String.valueOf(p).replace('.', ',');
    }

    private static double angka(Connection c, String sql, String noRawat, int jumlahParam) {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 1; i <= jumlahParam; i++) ps.setString(i, noRawat);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        } catch (Exception e) {
            System.out.println("Notif Diskon Penjamin : " + e.getMessage());
        }
        return 0;
    }

    /**
     * Kategori barang yang dihitung sebagai Alkes (sama dengan nota PAMA: kategori "Alkes &amp; BHP").
     * Selain kategori ini masuk Obat.
     */
    public static final String KATEGORI_ALKES = "Alkes & BHP";

    /** Diskon satu baris dengan 2 digit desimal: total x p%, setengah menjauhi nol (retur bernilai negatif). */
    public static java.math.BigDecimal diskon2(double total, double persen) {
        return java.math.BigDecimal.valueOf(total).multiply(java.math.BigDecimal.valueOf(persen))
                .divide(java.math.BigDecimal.valueOf(100)).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    /** Diskon satu baris (per obat) dalam rupiah: diskon 2 desimal dibulatkan, 0,5 dianggap 1. */
    public static double diskonBaris(double total, double persen) {
        return diskon2(total, persen).setScale(0, java.math.RoundingMode.HALF_UP).doubleValue();
    }

    /** Teks diskon untuk satu baris obat, mis. " [Diskon 5% : 4.387,40 -> 4.387]". Kosong bila persen 0. */
    public static String teksDiskonBaris(double total, double persen) {
        if (persen <= 0 || total == 0) return "";
        return teksDiskon(persen, diskon2(total, persen).doubleValue(), diskonBaris(total, persen));
    }

    /**
     * Persen diskon Obat/Alkes yang sedang dipakai billing: hanya kategori yang baris potongannya
     * "Diskon Obat/Alkes ... (Jenis Bayar)" ada di pengurangan_biaya (bila dihapus kasir, tidak ditampilkan per obat).
     * Elemen: {persen obat, persen alkes}.
     */
    public static double[] persenTerpasang(String noRawat, String jenisRawat) {
        double[] h = {0, 0};
        if (noRawat == null || noRawat.trim().equals("") || !aktif()) return h;
        try (PreparedStatement ps = koneksiDB.condb().prepareStatement(
                "select d.kategori,d.persen from set_diskon_penjab d inner join reg_periksa r on r.kd_pj=d.kd_pj "
                + "where r.no_rawat=? and d.jenis_rawat=? and d.persen>0 and d.kategori in ('Obat','Alkes') "
                + "and exists(select 1 from pengurangan_biaya p where p.no_rawat=r.no_rawat and p.nama_pengurangan like concat('Diskon ',d.kategori,' %',?))")) {
            ps.setString(1, noRawat);
            ps.setString(2, jenisRawat);
            ps.setString(3, PENANDA);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) h[rs.getString(1).equals("Alkes") ? 1 : 0] = rs.getDouble(2);
            }
        } catch (Exception e) {
            System.out.println("Notif Diskon Jenis Bayar : " + e.getMessage());
        }
        return h;
    }

    /** Kode barang Harga Barang Tetap yang tidak ikut diskon (ikut_diskon='Tidak'). */
    public static java.util.Set<String> barangTanpaDiskon() {
        java.util.Set<String> h = new java.util.HashSet<>();
        if (!HargaBarangTetap.aktif()) return h;
        try (Statement st = koneksiDB.condb().createStatement();
             ResultSet rs = st.executeQuery("select kode_brng from set_harga_barang_tetap where ikut_diskon='Tidak'")) {
            while (rs.next()) h.add(rs.getString(1));
        } catch (Exception e) {
            System.out.println("Notif Diskon Jenis Bayar : " + e.getMessage());
        }
        return h;
    }

    /**
     * Diskon per baris Billing Ranap = jumlah diskon per obat per tanggal (sama dengan barisObatAlkes dan nota PAMA),
     * sehingga jumlah diskon yang tampil di billing sama dengan baris Potongan. Kunci:
     * "O|depook|kode_brng|biaya_obat" (pemberian obat), "OP|kd_obat" (obat operasi), "RT|kode_brng" (retur, negatif).
     * Resep pulang dihitung per baris dengan teksDiskonBaris(). Nilai: {diskon 2 desimal, diskon dibulatkan per obat per tanggal}.
     */
    public static Map<String, double[]> diskonBarisBilling(String noRawat, double persenObat, double persenAlkes) {
        Map<String, double[]> h = new java.util.HashMap<>();
        if (persenObat <= 0 && persenAlkes <= 0) return h;
        Connection c = koneksiDB.condb();
        String kecuali = HargaBarangTetap.aktif()
                ? " and not exists(select 1 from set_harga_barang_tetap hbt where hbt.kode_brng=databarang.kode_brng and hbt.ikut_diskon='Tidak')" : "";
        String isAlkes = "if(ifnull((select nama from kategori_barang where kode=databarang.kode_kategori),'')=?,1,0)";
        String[] sql = {
            "select " + isAlkes + ",sum(detail_pemberian_obat.total),concat('O|',if(detail_pemberian_obat.kd_bangsal='OK01',1,0),'|',detail_pemberian_obat.kode_brng,'|',detail_pemberian_obat.biaya_obat) "
                + "from detail_pemberian_obat inner join databarang on databarang.kode_brng=detail_pemberian_obat.kode_brng where detail_pemberian_obat.no_rawat=?" + kecuali
                + " group by detail_pemberian_obat.tgl_perawatan,if(detail_pemberian_obat.kd_bangsal='OK01',1,0),detail_pemberian_obat.kode_brng,detail_pemberian_obat.biaya_obat",
            "select 0,sum(hargasatuan*jumlah),concat('OP|',kd_obat) from beri_obat_operasi where no_rawat=? group by date(tanggal),kd_obat",
            "select " + isAlkes + ",-1*sum(detreturjual.subtotal),concat('RT|',databarang.kode_brng) from detreturjual inner join returjual on returjual.no_retur_jual=detreturjual.no_retur_jual "
                + "inner join databarang on databarang.kode_brng=detreturjual.kode_brng where returjual.no_retur_jual like concat('%',?,'%')" + kecuali
                + " group by returjual.tgl_retur,if(returjual.kd_bangsal='OK01',1,0),databarang.kode_brng"
        };
        for (String s : sql) {
            try (PreparedStatement ps = c.prepareStatement(s)) {
                int i = 1;
                if (s.contains("kategori_barang")) ps.setString(i++, KATEGORI_ALKES);
                ps.setString(i, noRawat);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        double p = rs.getInt(1) == 1 ? persenAlkes : persenObat;
                        if (p <= 0) continue;
                        double[] v = h.computeIfAbsent(rs.getString(3), k -> new double[2]);
                        v[0] += diskon2(rs.getDouble(2), p).doubleValue();
                        v[1] += diskonBaris(rs.getDouble(2), p);
                    }
                }
            } catch (Exception e) {
                System.out.println("Notif Diskon Jenis Bayar : " + e.getMessage());
            }
        }
        return h;
    }

    /**
     * Teks diskon untuk billing, tampil tanpa desimal, mis. " [Diskon 5% : 4,387]" (pemisah sesuai locale komputer).
     * Nilai potongan yang tersimpan tetap 2 desimal (tanpa dibulatkan); diskonBulat tidak dipakai.
     */
    public static String teksDiskon(double persen, double diskon2, double diskonBulat) {
        if (persen <= 0 || (diskon2 == 0 && diskonBulat == 0)) return "";
        java.text.DecimalFormat d0 = new java.text.DecimalFormat("#,##0");
        d0.setRoundingMode(java.math.RoundingMode.HALF_UP);
        return " [Diskon " + teksPersen(persen) + "% : " + d0.format(java.math.BigDecimal.valueOf(diskon2).setScale(2, java.math.RoundingMode.HALF_UP)) + "]";
    }

    /**
     * Baris Obat/Alkes untuk no_rawat dengan pengelompokan yang sama seperti nota PAMA (diskon per obat per tanggal):
     * pemberian obat per tanggal+depo+barang, obat operasi per tanggal+obat, resep pulang per tanggal+barang,
     * retur per tanggal+depo+barang (negatif), dan tagihan obat langsung. Elemen: {1=alkes/0=obat, total}.
     */
    public static java.util.List<double[]> barisObatAlkes(String noRawat) {
        Connection c = koneksiDB.condb();
        java.util.List<double[]> baris = new java.util.ArrayList<>();
        String kecuali = HargaBarangTetap.aktif()
                ? " and not exists(select 1 from set_harga_barang_tetap hbt where hbt.kode_brng=databarang.kode_brng and hbt.ikut_diskon='Tidak')" : "";
        String isAlkes = "if(ifnull((select nama from kategori_barang where kode=databarang.kode_kategori),'')=?,1,0)";
        String[] sql = {
            "select " + isAlkes + ",sum(detail_pemberian_obat.total) from detail_pemberian_obat inner join databarang on databarang.kode_brng=detail_pemberian_obat.kode_brng "
                + "where detail_pemberian_obat.no_rawat=?" + kecuali + " group by detail_pemberian_obat.tgl_perawatan,if(detail_pemberian_obat.kd_bangsal='OK01',1,0),detail_pemberian_obat.kode_brng",
            "select 0,sum(hargasatuan*jumlah) from beri_obat_operasi where no_rawat=? group by date(tanggal),kd_obat",
            "select " + isAlkes + ",sum(resep_pulang.total) from resep_pulang inner join databarang on databarang.kode_brng=resep_pulang.kode_brng "
                + "where resep_pulang.no_rawat=?" + kecuali + " group by resep_pulang.tanggal,resep_pulang.kode_brng",
            "select " + isAlkes + ",-1*sum(detreturjual.subtotal) from detreturjual inner join returjual on returjual.no_retur_jual=detreturjual.no_retur_jual "
                + "inner join databarang on databarang.kode_brng=detreturjual.kode_brng where returjual.no_retur_jual like concat('%',?,'%')" + kecuali
                + " group by returjual.tgl_retur,if(returjual.kd_bangsal='OK01',1,0),databarang.kode_brng",
            "select 0,ifnull(sum(besar_tagihan),0) from tagihan_obat_langsung where no_rawat=? having sum(besar_tagihan)>0"
        };
        for (String s : sql) {
            try (PreparedStatement ps = c.prepareStatement(s)) {
                int i = 1;
                if (s.contains("kategori_barang")) ps.setString(i++, KATEGORI_ALKES);
                ps.setString(i, noRawat);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) baris.add(new double[]{rs.getDouble(1), rs.getDouble(2)});
                }
            } catch (Exception e) {
                System.out.println("Notif Diskon Jenis Bayar : " + e.getMessage());
            }
        }
        return baris;
    }

    /** Subtotal tiap kategori untuk no_rawat. */
    public static Map<String, Double> subtotal(String noRawat) {
        Connection c = koneksiDB.condb();
        Map<String, Double> h = new LinkedHashMap<>();
        double obat = 0, alk = 0;
        for (double[] b : barisObatAlkes(noRawat)) {
            if (b[0] == 1) alk += b[1]; else obat += b[1];
        }
        h.put("Obat", obat);
        h.put("Alkes", alk);
        h.put("Tindakan", angka(c,
                "select (select ifnull(sum(biaya_rawat),0) from rawat_jl_dr where no_rawat=?)+(select ifnull(sum(biaya_rawat),0) from rawat_jl_pr where no_rawat=?)"
                + "+(select ifnull(sum(biaya_rawat),0) from rawat_jl_drpr where no_rawat=?)+(select ifnull(sum(biaya_rawat),0) from rawat_inap_dr where no_rawat=?)"
                + "+(select ifnull(sum(biaya_rawat),0) from rawat_inap_pr where no_rawat=?)+(select ifnull(sum(biaya_rawat),0) from rawat_inap_drpr where no_rawat=?)", noRawat, 6));
        h.put("Operasi", angka(c,
                "select (select ifnull(sum(biayaoperator1+biayaoperator2+biayaoperator3+biayaasisten_operator1+biayaasisten_operator2+biayaasisten_operator3+"
                + "biayainstrumen+biayadokter_anak+biayaperawaat_resusitas+biayadokter_anestesi+biayaasisten_anestesi+biayaasisten_anestesi2+biayabidan+"
                + "biayabidan2+biayabidan3+biayaperawat_luar+biayaalat+biayasewaok+akomodasi+bagian_rs+biaya_omloop+biaya_omloop2+biaya_omloop3+"
                + "biaya_omloop4+biaya_omloop5+biayasarpras+biaya_dokter_pjanak+biaya_dokter_umum),0) from operasi where no_rawat=?)", noRawat, 1));
        h.put("Laboratorium", angka(c,
                "select (select ifnull(sum(biaya),0) from periksa_lab where no_rawat=?)+(select ifnull(sum(biaya_item),0) from detail_periksa_lab where no_rawat=?)", noRawat, 2));
        h.put("Radiologi", angka(c, "select ifnull(sum(biaya),0) from periksa_radiologi where no_rawat=?", noRawat, 1));
        h.put("Kamar", angka(c, "select ifnull(sum(ttl_biaya),0) from kamar_inap where no_rawat=?", noRawat, 1));
        return h;
    }

    /** Hapus diskon otomatis lama lalu tulis ulang sesuai setting penjamin pasien. Kembalikan jumlah baris diskon. */
    public static int terapkan(String noRawat, String jenisRawat) {
        if (noRawat == null || noRawat.trim().equals("") || !aktif()) return 0;
        Connection c = koneksiDB.condb();
        // Kategori yang barisnya masih ada sekarang, dan kategori yang pernah diterapkan (log).
        java.util.Set<String> ada = new java.util.HashSet<>(), pernah = new java.util.HashSet<>();
        try (PreparedStatement ps = c.prepareStatement("select nama_pengurangan from pengurangan_biaya where no_rawat=? and nama_pengurangan like ?")) {
            ps.setString(1, noRawat);
            ps.setString(2, "Diskon %" + PENANDA);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String nm = rs.getString(1);
                    for (String k : KATEGORI) if (nm.startsWith("Diskon " + k + " ")) ada.add(k);
                }
            }
        } catch (Exception e) {
            System.out.println("Notif Diskon Jenis Bayar : " + e.getMessage());
        }
        try (PreparedStatement ps = c.prepareStatement("select kategori from diskon_jenis_bayar_log where no_rawat=?")) {
            ps.setString(1, noRawat);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) pernah.add(rs.getString(1));
            }
        } catch (Exception e) {
            System.out.println("Notif Diskon Jenis Bayar : log - " + e.getMessage());
        }
        try (PreparedStatement del = c.prepareStatement("delete from pengurangan_biaya where no_rawat=? and nama_pengurangan like ?")) {
            del.setString(1, noRawat);
            del.setString(2, "Diskon %" + PENANDA);
            del.executeUpdate();
        } catch (Exception e) {
            System.out.println("Notif Diskon Penjamin : " + e.getMessage());
            return 0;
        }
        Map<String, Double> persen = new LinkedHashMap<>();
        try (PreparedStatement ps = c.prepareStatement(
                "select d.kategori,d.persen from set_diskon_penjab d inner join reg_periksa r on r.kd_pj=d.kd_pj where r.no_rawat=? and d.jenis_rawat=? and d.persen>0")) {
            ps.setString(1, noRawat);
            ps.setString(2, jenisRawat);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) persen.put(rs.getString(1), rs.getDouble(2));
            }
        } catch (Exception e) {
            System.out.println("Notif Diskon Penjamin : " + e.getMessage());
            return 0;
        }
        if (persen.isEmpty()) return 0;
        Map<String, Double> sub = subtotal(noRawat);
        java.util.List<double[]> baris = barisObatAlkes(noRawat);
        int n = 0;
        try (PreparedStatement ins = c.prepareStatement("insert into pengurangan_biaya (no_rawat,nama_pengurangan,besar_pengurangan) values (?,?,?)");
             PreparedStatement log = c.prepareStatement("insert ignore into diskon_jenis_bayar_log (no_rawat,kategori,waktu) values (?,?,now())")) {
            for (String k : KATEGORI) {
                Double p = persen.get(k);
                double s = sub.getOrDefault(k, 0d);
                if (p == null || p <= 0 || s <= 0) continue;
                // Pernah diterapkan tetapi barisnya sudah tidak ada = dihapus kasir: jangan dibuat ulang.
                if (pernah.contains(k) && !ada.contains(k)) continue;
                double nilai;
                if (k.equals("Obat") || k.equals("Alkes")) {
                    // Per obat per tanggal, 2 desimal TANPA dibulatkan ke rupiah (billing); pembulatan hanya di nota PAMA
                    java.math.BigDecimal jml = java.math.BigDecimal.ZERO;
                    double jenis = k.equals("Alkes") ? 1 : 0;
                    for (double[] b : baris) if (b[0] == jenis) jml = jml.add(diskon2(b[1], p));
                    nilai = jml.doubleValue();
                } else {
                    nilai = Math.round(s * p / 100d);
                }
                if (nilai <= 0) continue;
                ins.setString(1, noRawat);
                ins.setString(2, "Diskon " + k + " " + teksPersen(p) + "% " + PENANDA);
                ins.setDouble(3, nilai);
                ins.executeUpdate();
                try {
                    log.setString(1, noRawat);
                    log.setString(2, k);
                    log.executeUpdate();
                } catch (Exception e) {
                    System.out.println("Notif Diskon Jenis Bayar : log - " + e.getMessage());
                }
                n++;
            }
        } catch (Exception e) {
            System.out.println("Notif Diskon Penjamin : " + e.getMessage());
        }
        return n;
    }
}
