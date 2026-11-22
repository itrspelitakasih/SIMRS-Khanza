package fungsi;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * RSPK: kodefikasi dan salin tarif untuk jenis bayar baru: tindakan rawat inap, laboratorium dan radiologi.
 * Tindakan ralan sengaja tidak ikut: di RSPK satu tarif untuk semua jenis bayar (set_tarif cara_bayar_ralan = No).
 * Awalan kode per jenis+kelas mengikuti pola RS dan bisa diubah, nomor 4 digit, mis. BPJSRJ-0001.
 *  - hanya baris yang dicentang diproses dan diberi nomor;
 *  - hanya menambah baris; yang sudah ada di jenis bayar tujuan (nama+kelas/poli) dilewati, jadi aman diulang;
 *  - awalan milik jenis bayar lain ditolak, nomor melanjutkan yang terbesar;
 *  - template lab TIDAK disalin (diisi petugas sendiri); mapping SatuSehat radiologi ikut disalin (opsional);
 *  - harga bisa dari jenis bayar lain: cocok nama+kelas, nama kembar dibedakan pola Dr/Pr lalu urutan nomor;
 *    yang meragukan atau Rp 0 tidak ditebak. Tarif yang sudah ada hanya diubah harganya bila diminta;
 *  - query borongan, bukan per tarif (latensi server), semua dalam satu transaksi.
 */
public final class KodefikasiTarif {
    public enum Jenis {
        RANAP("Tindakan Ranap", "jns_perawatan_inap", "cara_bayar_ranap",
                "material", "bhp", "tarif_tindakandr", "tarif_tindakanpr", "kso", "menejemen", "total_byrdr", "total_byrpr", "total_byrdrpr"),
        LAB("Laboratorium", "jns_perawatan_lab", "cara_bayar_lab",
                "bagian_rs", "bhp", "tarif_perujuk", "tarif_tindakan_dokter", "tarif_tindakan_petugas", "kso", "menejemen", "total_byr"),
        RADIOLOGI("Radiologi", "jns_perawatan_radiologi", "cara_bayar_radiologi",
                "bagian_rs", "bhp", "tarif_perujuk", "tarif_tindakan_dokter", "tarif_tindakan_petugas", "kso", "menejemen", "total_byr");

        public final String nama, tabel;
        final String setting;
        final String[] kolomHarga;

        Jenis(String nama, String tabel, String setting, String... kolomHarga) {
            this.nama = nama; this.tabel = tabel; this.setting = setting; this.kolomHarga = kolomHarga;
        }
    }

    /** Pilihan filter kelas; indeks 0 = semua. */
    public static final String[] FILTER_KELAS = {"Semua Kelas", "Rawat Jalan / Tanpa Kelas", "Kelas 1", "Kelas 2", "Kelas 3", "Kelas Utama", "Kelas VIP", "Kelas VVIP"};

    public static boolean masukFilter(String filter, String kelas) {
        if (filter.equals(FILTER_KELAS[0])) return true;
        if (filter.equals(FILTER_KELAS[1])) return kelas.equals("-") || kelas.equals("Rawat Jalan");
        return filter.equals(kelas);
    }

    /** Satu baris aturan kodefikasi (jenis + kelas). */
    public static final class Aturan {
        public final Jenis jenis;
        public final String kelas;
        public String awalan;
        public boolean pilih;
        public int baru, sudahAda;
        /** Tarif lama yang harganya beda dari sumber harga, dan yang tidak punya padanan harga. */
        public int ubahHarga, tanpaHarga;

        Aturan(Jenis jenis, String kelas) { this.jenis = jenis; this.kelas = kelas; }
    }

    /** Satu tarif yang akan dibuat. */
    public static final class Baris {
        public final Jenis jenis;
        public final String kode, sumber, nama, kelas;
        /** Tarif untuk pratinjau; ranap bisa di Dokter, Paramedis dan/atau Dokter+Paramedis. */
        public final String tarif;
        public final boolean tarifNol;
        /** true = tarif lama yang diubah harganya, false = tarif baru. */
        public final boolean ubah;
        /** Dicentang user; yang tidak, kodenya "" (bila baru) dan tidak disimpan. */
        public final boolean pilih;
        /** Penanda baris yang tetap walau nomor kodenya berubah. */
        public final String kunci;
        /** Kode sumber harga ("" = harga sumber tarif) atau alasan tidak dipakai. */
        public final String kodeHarga, ketHarga;
        final Object[] nilai;
        final String sidik;

        Baris(Asal a, String kode, boolean pilih) {
            this.pilih = pilih; kunci = kunciBaris(a);
            ubah = a.ubah; kodeHarga = a.kodeHarga; ketHarga = a.ketHarga; sidik = a.sidik;
            jenis = a.jenis; this.kode = kode; sumber = a.kode; nama = a.nama; kelas = a.kelasTampil; tarif = a.tarif; tarifNol = a.tarifNol;
            nilai = a.nilai;
        }
    }

    private static final class Asal {
        Jenis jenis;
        String kode, nama, grup, kunci, kelasTampil;
        String tarif, kodeHarga = "", ketHarga = "", sidik = "";
        boolean ubah, tarifNol;
        Object[] nilai;
    }

    /** Data sumber yang belum ada di jenis bayar tujuan. */
    public static final class Data {
        public final String tujuan;
        private final Map<Jenis, List<String>> kolom = new LinkedHashMap<>();
        private final Map<Jenis, Map<String, List<Asal>>> grup = new LinkedHashMap<>();
        private final Map<Jenis, Map<String, Integer>> sudahAda = new LinkedHashMap<>();
        private final Map<Jenis, Map<String, List<Asal>>> ubah = new LinkedHashMap<>();
        private final Map<Jenis, Map<String, Integer>> tanpaHarga = new LinkedHashMap<>();
        private final Map<Jenis, Boolean> perJenisBayar = new HashMap<>();
        /** Nomor terbesar per jenis|awalan, supaya mencentang tidak query lagi (simpan membaca ulang). */
        private final Map<String, Integer> nomorTerakhir = new HashMap<>();

        Data(String tujuan) { this.tujuan = tujuan; }

        /** set_tarif: pencarian tarif jenis ini disaring per jenis bayar pasien? */
        public boolean perJenisBayar(Jenis j) { return perJenisBayar.getOrDefault(j, Boolean.TRUE); }
    }

    private static final int POTONG = 150;

    private static String kunciBaris(Asal a) {
        return a.jenis.name() + "|" + (a.ubah ? "U" : "B") + "|" + a.kode;
    }

    private KodefikasiTarif() {}

    // ---------------- pola bawaan ----------------
    /** Awalan bawaan sesuai pola RS: ranap KODE+K1, lab K1+KODE, radiologi K1+KODE, rawat jalan RJ, rawat inap RI. */
    public static String awalanBawaan(Jenis jenis, String kelas, String kode) {
        boolean rj = kelas.equals("-") || kelas.equals("Rawat Jalan");
        String k;
        switch (kelas) {
            case "Kelas 1": k = "K1"; break;
            case "Kelas 2": k = "K2"; break;
            case "Kelas 3": k = "K3"; break;
            case "Kelas Utama": k = "KU"; break;
            case "Kelas VIP": k = jenis == Jenis.RADIOLOGI ? "KV" : "V"; break;
            case "Kelas VVIP": k = jenis == Jenis.RADIOLOGI ? "KVV" : "VV"; break;
            default: k = "";
        }
        switch (jenis) {
            case RANAP: return kode + (rj ? "RI" : k);
            case LAB: return rj ? kode + "RJ" : k + kode;
            default: return rj ? "RJ" + kode : k + kode;
        }
    }

    // ---------------- muat ----------------
    private static String tanda(int n) {
        return String.join(",", Collections.nCopies(n, "?"));
    }

    private static String rapi(Object o) {
        return o == null ? "" : o.toString().trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    private static int urutKelas(String kelas) {
        String[] u = {"-", "Rawat Jalan", "Kelas 1", "Kelas 2", "Kelas 3", "Kelas Utama", "Kelas VIP", "Kelas VVIP"};
        for (int i = 0; i < u.length; i++) if (u[i].equals(kelas)) return i;
        return u.length;
    }

    private static List<Object[]> ambil(Connection c, String sql, List<String> kolom, Collection<?> param) throws Exception {
        List<Object[]> h = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            int i = 1;
            for (Object o : param) ps.setObject(i++, o);
            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData m = rs.getMetaData();
                int n = m.getColumnCount();
                if (kolom != null && kolom.isEmpty()) for (int k = 1; k <= n; k++) kolom.add(m.getColumnName(k).toLowerCase(Locale.ROOT));
                while (rs.next()) {
                    Object[] r = new Object[n];
                    for (int k = 0; k < n; k++) r[k] = rs.getObject(k + 1);
                    h.add(r);
                }
            }
        }
        return h;
    }

    private static <T> List<List<T>> potong(List<T> l) {
        List<List<T>> h = new ArrayList<>();
        for (int i = 0; i < l.size(); i += POTONG) h.add(l.subList(i, Math.min(l.size(), i + POTONG)));
        return h;
    }

    private static String nilai(List<String> kolom, Object[] r, String nama) {
        int i = kolom.indexOf(nama);
        return i < 0 || r[i] == null ? "" : r[i].toString();
    }

    private static String tarifTampil(Jenis j, List<String> kolom, Object[] r) {
        boolean ranap = j == Jenis.RANAP;
        String[] nama = ranap ? new String[]{"total_byrdr", "total_byrpr", "total_byrdrpr"} : new String[]{"total_byr"};
        String[] label = ranap ? new String[]{"Dr ", "Pr ", "Dr+Pr "} : new String[]{""};
        List<String> h = new ArrayList<>();
        for (int i = 0; i < nama.length; i++) {
            String t = nilai(kolom, r, nama[i]);
            double v = t.equals("") ? 0 : Double.parseDouble(t);
            if (v != 0) h.add(label[i] + String.format("%,.0f", v).replace(',', '.'));
        }
        return h.isEmpty() ? "0" : String.join(" / ", h);
    }

    private static String kunci(Jenis j, List<String> kolom, Object[] r) {
        String kls = nilai(kolom, r, "kelas");
        if ((j == Jenis.LAB || j == Jenis.RADIOLOGI) && kls.equals("Rawat Jalan")) kls = "-"; // sama-sama rawat jalan
        String k = rapi(nilai(kolom, r, "nm_perawatan")) + "|" + rapi(kls);
        if (j == Jenis.RANAP) k += "|" + rapi(nilai(kolom, r, "kd_bangsal"));
        if (j == Jenis.LAB) k += "|" + rapi(nilai(kolom, r, "kategori"));
        return k;
    }

    // ---------------- padanan harga ----------------
    private static String kunciHarga(Jenis j, List<String> kolom, Object[] r) {
        String kls = nilai(kolom, r, "kelas");
        if (j != Jenis.RANAP && kls.equals("Rawat Jalan")) kls = "-";
        return rapi(nilai(kolom, r, "nm_perawatan")) + "|" + rapi(kls) + (j == Jenis.LAB ? "|" + rapi(nilai(kolom, r, "kategori")) : "");
    }

    private static int nomorAkhir(String kode) {
        String n = kode.substring(kode.lastIndexOf('-') + 1);
        return n.matches("\\d{1,9}") ? Integer.parseInt(n) : Integer.MAX_VALUE;
    }

    /** Kelompok nama+kelas, di dalamnya urut nomor kode (urutan varian sama antar jenis bayar). */
    private static Map<String, List<Object[]>> kelompok(Jenis j, List<String> kolom, List<Object[]> baris) {
        Map<String, List<Object[]>> h = new HashMap<>();
        for (Object[] r : baris) h.computeIfAbsent(kunciHarga(j, kolom, r), k -> new ArrayList<>()).add(r);
        for (List<Object[]> l : h.values())
            l.sort((a, b) -> {
                String ka = nilai(kolom, a, "kd_jenis_prw"), kb = nilai(kolom, b, "kd_jenis_prw");
                int n = Integer.compare(nomorAkhir(ka), nomorAkhir(kb));
                return n != 0 ? n : ka.compareTo(kb);
            });
        return h;
    }

    private static double angka(List<String> kolom, Object[] r, String nama) {
        String s = nilai(kolom, r, nama);
        return s.equals("") ? 0 : Double.parseDouble(s);
    }

    private static String sidikHarga(Jenis j, List<String> kolom, Object[] r) {
        List<String> h = new ArrayList<>();
        for (String k : j.kolomHarga) {
            double v = angka(kolom, r, k);
            h.add(v == Math.rint(v) ? String.valueOf((long) v) : String.valueOf(v));
        }
        return String.join("/", h);
    }

    /** Pola ranap: terisi di Dokter / Paramedis / Dokter+Paramedis. */
    private static String polaTarif(List<String> kolom, Object[] r) {
        return (angka(kolom, r, "total_byrdr") != 0) + "," + (angka(kolom, r, "total_byrpr") != 0) + "," + (angka(kolom, r, "total_byrdrpr") != 0);
    }

    private static boolean satuHarga(Jenis j, List<String> kolom, List<Object[]> l) {
        for (Object[] r : l) if (!sidikHarga(j, kolom, r).equals(sidikHarga(j, kolom, l.get(0)))) return false;
        return true;
    }

    /** Padanan harga untuk r. null = tidak dipakai, alasannya di ket[0]. */
    private static Object[] cariHarga(Jenis j, List<String> kolom, Object[] r, Map<String, List<Object[]>> kelompokR,
            Map<String, List<Object[]>> kelompokHarga, String[] ket) {
        String k = kunciHarga(j, kolom, r);
        List<Object[]> calon = kelompokHarga.getOrDefault(k, Collections.emptyList());
        if (calon.isEmpty()) { ket[0] = "tidak ada di sumber harga"; return null; }
        Object[] pilih = null;
        if (satuHarga(j, kolom, calon)) pilih = calon.get(0);
        if (pilih == null && j == Jenis.RANAP) {
            List<Object[]> sama = new ArrayList<>();
            for (Object[] c : calon) if (polaTarif(kolom, c).equals(polaTarif(kolom, r))) sama.add(c);
            if (!sama.isEmpty() && satuHarga(j, kolom, sama)) pilih = sama.get(0);
        }
        if (pilih == null) {
            List<Object[]> sendiri = kelompokR.getOrDefault(k, Collections.emptyList());
            int i = sendiri.indexOf(r); // array = identitas baris
            if (sendiri.size() == calon.size() && i >= 0) pilih = calon.get(i);
        }
        if (pilih == null) { ket[0] = "harga ganda, tidak ditebak"; return null; }
        boolean nol = true;
        for (String h : j.kolomHarga) if (h.startsWith("total_") && angka(kolom, pilih, h) != 0) nol = false;
        if (nol) { ket[0] = "harga Rp 0 di sumber harga"; return null; }
        return pilih;
    }

    /**
     * Tarif sumber yang sudah ada di tujuan. Nama kembar dipasangkan menurut harga, lalu pola Dr/Pr, baru urutan,
     * supaya setelah salin sebagian yang tersisa adalah varian yang memang belum disalin.
     */
    private static java.util.Set<Object[]> pasangkan(Jenis j, List<String> kolom, List<Object[]> asal, List<Object[]> ada,
            Map<String, List<Object[]>> kelAsal, Map<String, List<Object[]>> kelHarga) {
        java.util.Set<Object[]> tutup = Collections.newSetFromMap(new java.util.IdentityHashMap<>());
        Map<String, List<Object[]>> perKunci = new HashMap<>();
        for (Object[] r : asal) perKunci.computeIfAbsent(kunci(j, kolom, r), k -> new ArrayList<>()).add(r);
        List<Object[]> sisa = new ArrayList<>(ada);
        for (int tahap = 0; tahap < 3; tahap++) {
            List<Object[]> belum = new ArrayList<>();
            for (Object[] t : sisa) {
                Object[] cocok = null;
                for (Object[] r : perKunci.getOrDefault(kunci(j, kolom, t), Collections.emptyList())) {
                    if (tutup.contains(r)) continue;
                    if (tahap == 1 && j == Jenis.RANAP && !polaTarif(kolom, r).equals(polaTarif(kolom, t))) continue;
                    if (tahap == 0) {
                        String harga = sidikHarga(j, kolom, t);
                        boolean sama = harga.equals(sidikHarga(j, kolom, r));
                        if (!sama && kelAsal != null) {
                            Object[] h = cariHarga(j, kolom, r, kelAsal, kelHarga, new String[1]);
                            sama = h != null && harga.equals(sidikHarga(j, kolom, h));
                        }
                        if (!sama) continue;
                    }
                    cocok = r;
                    break;
                }
                if (cocok == null) belum.add(t); else tutup.add(cocok);
            }
            sisa = belum;
        }
        return tutup;
    }

    /** Baca tarif sumber, buang yang sudah ada di tujuan. sumberHarga "" = harga sama dengan sumber tarif. */
    public static Data muat(String tujuan, String sumber, String sumberHarga) throws Exception {
        if (tujuan.equals(sumberHarga)) throw new Exception("Sumber harga tidak boleh sama dengan jenis bayar baru.");
        if (tujuan.equals("") || tujuan.equals("-")) throw new Exception("Pilih jenis bayar baru terlebih dahulu.");
        if (tujuan.equals(sumber)) throw new Exception("Sumber tarif tidak boleh sama dengan jenis bayar baru.");
        Connection c = koneksiDB.condb();
        Data d = new Data(tujuan);
        List<String> kolomSet = new ArrayList<>();
        List<Object[]> set = ambil(c, "select * from set_tarif limit 1", kolomSet, Collections.emptyList());
        for (Jenis j : Jenis.values())
            // tanpa setting Khanza memakai Yes
            d.perJenisBayar.put(j, set.isEmpty() ? Boolean.TRUE : (Boolean) "Yes".equals(nilai(kolomSet, set.get(0), j.setting)));

        for (Jenis j : Jenis.values()) {
            List<String> kolom = new ArrayList<>();
            List<Object[]> asal = ambil(c, "select * from " + j.tabel + " where status='1' and kd_pj=? order by kd_jenis_prw", kolom,
                    Collections.singletonList(sumber));
            List<Object[]> ada = ambil(c, "select * from " + j.tabel + " where status='1' and kd_pj=? order by kd_jenis_prw", null, Collections.singletonList(tujuan));
            boolean adaHarga = !sumberHarga.equals("");
            Map<String, List<Object[]>> kelHarga = new HashMap<>(), kelAsal = new HashMap<>(), kelAda = new HashMap<>();
            if (adaHarga) {
                List<Object[]> harga = sumberHarga.equals(sumber) ? asal
                        : ambil(c, "select * from " + j.tabel + " where status='1' and kd_pj=? order by kd_jenis_prw", null, Collections.singletonList(sumberHarga));
                kelHarga = kelompok(j, kolom, harga); kelAsal = kelompok(j, kolom, asal); kelAda = kelompok(j, kolom, ada);
            }
            Map<String, Integer> lewat = new HashMap<>();
            java.util.Set<Object[]> tertutup = pasangkan(j, kolom, asal, ada, adaHarga && !sumberHarga.equals(sumber) ? kelAsal : null, kelHarga);

            List<String> urutan = new ArrayList<>();
            Map<String, List<Asal>> grup = new HashMap<>();
            for (Object[] r : asal) {
                String g = nilai(kolom, r, "kelas"), k = kunci(j, kolom, r);
                if (!grup.containsKey(g)) { grup.put(g, new ArrayList<>()); urutan.add(g); }
                if (tertutup.contains(r)) {
                    lewat.merge(g, 1, Integer::sum);
                    continue;
                }
                Asal a = new Asal();
                a.jenis = j; a.kode = nilai(kolom, r, "kd_jenis_prw"); a.nama = nilai(kolom, r, "nm_perawatan");
                a.grup = g; a.kunci = k; a.kelasTampil = g;
                Object[] pakai = r;
                if (adaHarga && !sumberHarga.equals(sumber)) {
                    String[] ket = {""};
                    Object[] h = cariHarga(j, kolom, r, kelAsal, kelHarga, ket);
                    if (h == null) a.ketHarga = ket[0];
                    else {
                        pakai = r.clone();
                        for (String kh : j.kolomHarga) pakai[kolom.indexOf(kh)] = h[kolom.indexOf(kh)];
                        a.kodeHarga = nilai(kolom, h, "kd_jenis_prw");
                    }
                }
                a.tarif = tarifTampil(j, kolom, pakai);
                a.tarifNol = a.tarif.equals("0");
                a.sidik = sidikHarga(j, kolom, pakai);
                a.nilai = pakai;
                grup.get(g).add(a);
            }
            urutan.sort((a, b) -> urutKelas(a) - urutKelas(b));
            Map<String, List<Asal>> urut = new LinkedHashMap<>();
            for (String g : urutan) urut.put(g, grup.get(g));
            d.kolom.put(j, kolom);
            d.grup.put(j, urut);
            d.sudahAda.put(j, lewat);

            // tarif lama di tujuan yang harganya beda dari sumber harga
            Map<String, List<Asal>> berubah = new HashMap<>();
            Map<String, Integer> tanpa = new HashMap<>();
            if (adaHarga) {
                for (Object[] r : ada) {
                    String g = nilai(kolom, r, "kelas");
                    String[] ket = {""};
                    Object[] h = cariHarga(j, kolom, r, kelAda, kelHarga, ket);
                    if (h == null) { tanpa.merge(g, 1, Integer::sum); continue; }
                    String lama = sidikHarga(j, kolom, r), baru = sidikHarga(j, kolom, h);
                    if (lama.equals(baru)) continue;
                    Asal a = new Asal();
                    a.jenis = j; a.ubah = true; a.kode = nilai(kolom, r, "kd_jenis_prw"); a.nama = nilai(kolom, r, "nm_perawatan");
                    a.grup = g; a.kelasTampil = g; a.kodeHarga = nilai(kolom, h, "kd_jenis_prw");
                    String tl = tarifTampil(j, kolom, r), tb = tarifTampil(j, kolom, h);
                    a.tarif = tl.equals(tb) ? tb + " (rincian berubah)" : tl + " -> " + tb;
                    a.sidik = lama + ">" + baru;
                    berubah.computeIfAbsent(g, k -> new ArrayList<>()).add(a);
                }
            }
            d.ubah.put(j, berubah);
            d.tanpaHarga.put(j, tanpa);
        }
        return d;
    }

    /** Aturan awal. izin = jenis yang boleh dibuat user; jenis yang pencariannya tidak per jenis bayar tidak dicentang. */
    public static List<Aturan> aturanAwal(Data d, String kodeAwalan, Collection<Jenis> izin, boolean perbaruiHarga) {
        List<Aturan> h = new ArrayList<>();
        for (Jenis j : Jenis.values()) {
            java.util.Set<String> semua = new java.util.LinkedHashSet<>(d.grup.get(j).keySet());
            semua.addAll(d.ubah.get(j).keySet()); // kelas yang hanya ada di tujuan tetap bisa diperbarui harganya
            List<String> kelas = new ArrayList<>(semua);
            kelas.sort((x, y) -> urutKelas(x) - urutKelas(y));
            for (String k : kelas) {
                Aturan a = new Aturan(j, k);
                a.awalan = awalanBawaan(j, k, kodeAwalan);
                a.baru = d.grup.get(j).getOrDefault(k, Collections.emptyList()).size();
                a.sudahAda = d.sudahAda.get(j).getOrDefault(k, 0);
                a.ubahHarga = d.ubah.get(j).getOrDefault(k, Collections.emptyList()).size();
                a.tanpaHarga = d.tanpaHarga.get(j).getOrDefault(k, 0);
                a.pilih = izin.contains(j) && ((a.baru > 0 && d.perJenisBayar(j)) || (perbaruiHarga && a.ubahHarga > 0));
                h.add(a);
            }
        }
        return h;
    }

    // ---------------- susun kode ----------------
    /** Kode baru lalu perubahan harga (bila perbaruiHarga) untuk aturan yang dicentang. lewati = Baris.kunci yang tidak dicentang. Tidak mengubah data. */
    public static List<Baris> susun(Data d, List<Aturan> aturan, boolean perbaruiHarga, Collection<String> lewati) throws Exception {
        Connection c = koneksiDB.condb();
        List<Baris> h = new ArrayList<>();
        for (Jenis j : Jenis.values()) {
            List<Aturan> dipakai = new ArrayList<>();
            List<String> awalan = new ArrayList<>();
            for (Aturan a : aturan) {
                if (a.jenis != j || !a.pilih || d.grup.get(j).getOrDefault(a.kelas, Collections.emptyList()).isEmpty()) continue;
                a.awalan = a.awalan.trim().toUpperCase(Locale.ROOT);
                if (!a.awalan.matches("[A-Z0-9]{1,10}"))
                    throw new Exception("Awalan " + j.nama + " " + a.kelas + " harus 1-10 huruf/angka tanpa spasi atau tanda baca.");
                dipakai.add(a);
                if (!d.nomorTerakhir.containsKey(j.name() + "|" + a.awalan) && !awalan.contains(a.awalan)) awalan.add(a.awalan);
            }
            if (dipakai.isEmpty()) continue;
            if (!awalan.isEmpty()) {
            List<String> pola = new ArrayList<>();
            for (String a : awalan) pola.add(a + "-%");
            List<Object[]> ada = ambil(c, "select kd_jenis_prw,kd_pj from " + j.tabel + " where "
                    + String.join(" or ", Collections.nCopies(pola.size(), "kd_jenis_prw like ?")), null, pola);
            Map<String, Integer> baca = new HashMap<>();
            for (String a : awalan) baca.put(a, 0);
            for (Object[] r : ada) {
                String kode = r[0].toString().trim().toUpperCase(Locale.ROOT);
                int s = kode.indexOf('-');
                String aw = kode.substring(0, s), no = kode.substring(s + 1);
                if (!d.tujuan.equalsIgnoreCase(String.valueOf(r[1]).trim()))
                    throw new Exception("Awalan " + aw + " (" + j.nama + ") sudah dipakai jenis bayar " + r[1] + ", contoh " + r[0] + ".\nGanti Awalan Kode.");
                if (no.matches("\\d{1,9}")) baca.merge(aw, Integer.parseInt(no), Math::max);
            }
            for (Map.Entry<String, Integer> e : baca.entrySet()) d.nomorTerakhir.put(j.name() + "|" + e.getKey(), e.getValue());
            }
            Map<String, Integer> nomor = new HashMap<>();
            for (Aturan a : dipakai) nomor.put(a.awalan, d.nomorTerakhir.get(j.name() + "|" + a.awalan));
            for (Aturan a : dipakai) {
                for (Asal s : d.grup.get(j).get(a.kelas)) {
                    if (lewati.contains(kunciBaris(s))) { h.add(new Baris(s, "", false)); continue; }
                    int n = nomor.merge(a.awalan, 1, Integer::sum);
                    if (n > 9999) throw new Exception("Nomor untuk awalan " + a.awalan + " melebihi 9999, gunakan awalan lain.");
                    h.add(new Baris(s, a.awalan + String.format("-%04d", n), true));
                }
            }
        }
        if (perbaruiHarga)
            for (Jenis j : Jenis.values())
                for (Aturan a : aturan)
                    if (a.jenis == j && a.pilih)
                        for (Asal s : d.ubah.get(j).getOrDefault(a.kelas, Collections.emptyList())) h.add(new Baris(s, s.kode, !lewati.contains(kunciBaris(s))));
        return h;
    }

    // ---------------- simpan ----------------
    private static void sisip(Connection c, String tabel, List<String> kolom, List<Object[]> baris) throws Exception {
        if (baris.isEmpty()) return;
        List<String> nama = new ArrayList<>();
        for (String k : kolom) nama.add("`" + k + "`");
        String satu = "(" + tanda(kolom.size()) + ")";
        for (List<Object[]> p : potong(baris)) {
            try (PreparedStatement ps = c.prepareStatement("insert into " + tabel + " (" + String.join(",", nama) + ") values "
                    + String.join(",", Collections.nCopies(p.size(), satu)))) {
                int i = 1;
                for (Object[] r : p) for (Object o : r) ps.setObject(i++, o);
                ps.executeUpdate();
            }
        }
    }

    /**
     * Proses baris pratinjau yang dicentang. Data dibaca ulang di dalam transaksi; bila tidak sama lagi dengan pratinjau, batal semua.
     * Hasil {tarif baru, mapping radiologi, tarif lama yang harganya diubah}.
     */
    public static int[] simpan(String tujuan, String sumber, String sumberHarga, List<Aturan> aturan, List<Baris> pratinjau,
            boolean salinMapping, boolean perbaruiHarga) throws Exception {
        Connection c = koneksiDB.condb();
        boolean autoAwal = c.getAutoCommit();
        c.setAutoCommit(false);
        try {
            Data d = muat(tujuan, sumber, sumberHarga);
            java.util.Set<String> lewati = new java.util.HashSet<>();
            for (Baris b : pratinjau) if (!b.pilih) lewati.add(b.kunci);
            List<Baris> semua = susun(d, aturan, perbaruiHarga, lewati);
            if (semua.isEmpty()) throw new Exception("Tidak ada tarif yang dipilih.");
            boolean sama = semua.size() == pratinjau.size();
            for (int i = 0; sama && i < semua.size(); i++) {
                Baris x = semua.get(i), y = pratinjau.get(i);
                sama = x.kunci.equals(y.kunci) && x.pilih == y.pilih && x.kode.equals(y.kode) && x.sumber.equals(y.sumber) && x.kodeHarga.equals(y.kodeHarga) && x.sidik.equals(y.sidik);
            }
            if (!sama) throw new Exception("Data tarif berubah sejak pratinjau. Klik Tampilkan lagi.");
            semua.removeIf(b -> !b.pilih);
            if (semua.isEmpty()) throw new Exception("Tidak ada tarif yang dicentang.");

            List<Baris> diubah = new ArrayList<>();
            for (Baris b : semua) if (b.ubah) diubah.add(b);
            semua.removeAll(diubah);
            int[] jumlah = {semua.size(), 0, diubah.size()};
            sekuel lacak = new sekuel();
            ubahHarga(c, lacak, tujuan, diubah);
            for (Jenis j : Jenis.values()) {
                List<String> kolom = d.kolom.get(j);
                int iKode = kolom.indexOf("kd_jenis_prw"), iPj = kolom.indexOf("kd_pj");
                List<Object[]> baru = new ArrayList<>();
                for (Baris b : semua) {
                    if (b.jenis != j) continue;
                    Object[] r = b.nilai.clone();
                    r[iKode] = b.kode;
                    r[iPj] = tujuan;
                    baru.add(r);
                }
                sisip(c, j.tabel, kolom, baru);
            }

            if (salinMapping) {
                Map<String, List<String>> rad = new LinkedHashMap<>();
                for (Baris b : semua) if (b.jenis == Jenis.RADIOLOGI) rad.computeIfAbsent(b.sumber, k -> new ArrayList<>()).add(b.kode);
                jumlah[1] = salinMappingTabel(c, "satu_sehat_mapping_radiologi", "kd_jenis_prw", rad);
            }

            if (!semua.isEmpty())
                lacak.SimpanTrack("kodefikasi tarif " + tujuan + " dari " + sumber + (sumberHarga.equals("") ? "" : " harga dari " + sumberHarga) + ": " + jumlah[0] + " tarif "
                        + semua.get(0).kode + " s.d. " + semua.get(semua.size() - 1).kode + ", " + jumlah[1] + " mapping");
            c.commit();
            return jumlah;
        } catch (Exception e) {
            try { c.rollback(); } catch (Exception ex) { }
            throw new Exception(e.getMessage() == null ? e.toString() : e.getMessage());
        } finally {
            try { c.setAutoCommit(autoAwal); } catch (Exception ex) { }
        }
    }

    /** Ubah harga tarif lama lewat join ke kode sumber harga, per 100 kode. Harga lama dicatat di tracker untuk pemulihan. */
    private static void ubahHarga(Connection c, sekuel lacak, String tujuan, List<Baris> diubah) throws Exception {
        for (Jenis j : Jenis.values()) {
            List<Baris> l = new ArrayList<>();
            for (Baris b : diubah) if (b.jenis == j) l.add(b);
            for (int m = 0; m < l.size(); m += 100) {
                List<Baris> p = l.subList(m, Math.min(l.size(), m + 100));
                List<String> set = new ArrayList<>(), param = new ArrayList<>(), pasangan = new ArrayList<>(), catat = new ArrayList<>();
                for (String k : j.kolomHarga) set.add("t." + k + "=s." + k);
                java.util.Set<String> harga = new java.util.LinkedHashSet<>();
                for (Baris b : p) {
                    harga.add(b.kodeHarga);
                    pasangan.add(b.kodeHarga); pasangan.add(b.kode);
                    catat.add(b.kode + " " + b.sidik);
                }
                param.add(tujuan);
                param.addAll(harga);
                for (Baris b : p) param.add(b.kode);
                param.addAll(pasangan);
                String sql = "update " + j.tabel + " t inner join " + j.tabel + " s set " + String.join(",", set)
                        + " where t.kd_pj=? and s.kd_jenis_prw in (" + tanda(harga.size()) + ") and t.kd_jenis_prw in (" + tanda(p.size()) + ") and ("
                        + String.join(" or ", Collections.nCopies(p.size(), "(s.kd_jenis_prw=? and t.kd_jenis_prw=?)")) + ")";
                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    for (int i = 0; i < param.size(); i++) ps.setString(i + 1, param.get(i));
                    ps.executeUpdate();
                }
                lacak.SimpanTrack("ubah harga " + j.tabel + " " + tujuan + " (kode lama>baru) " + String.join("; ", catat));
            }
        }
    }

    private static int salinMappingTabel(Connection c, String tabel, String kolomKunci, Map<String, List<String>> tujuan) throws Exception {
        if (tujuan.isEmpty()) return 0;
        List<String> kolom = new ArrayList<>();
        List<Object[]> baru = new ArrayList<>();
        for (List<String> p : potong(new ArrayList<>(tujuan.keySet()))) {
            for (Object[] r : ambil(c, "select * from " + tabel + " where " + kolomKunci + " in (" + tanda(p.size()) + ")", kolom, p)) {
                int i = kolom.indexOf(kolomKunci);
                for (String t : tujuan.getOrDefault(String.valueOf(r[i]), Collections.emptyList())) {
                    Object[] n = r.clone();
                    n[i] = t;
                    baru.add(n);
                }
            }
        }
        sisip(c, tabel, kolom, baru);
        return baru.size();
    }
}
