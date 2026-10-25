package komunikasi;

import fungsi.koneksiDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;

/**
 * Semua teks pesan WhatsApp di satu tempat. Teks bawaan ada di daftar TEMPLATE
 * di bawah; teks yang diubah lewat menu Template Pesan WA (DlgSetPesanWA)
 * disimpan di tabel set_pesan_wa dan dipakai menggantikan teks bawaan.
 * Penanda {nama}, {poli}, dst. diganti otomatis dengan data sebenarnya.
 * *teks* = tebal, _teks_ = miring (format WhatsApp).
 */
public final class pesanWA {

    private pesanWA() {}

    /**
     * Satu template pesan: kode (kunci di tabel set_pesan_wa), judul, penanda yang bisa dipakai dan teks bawaan.
     * Template turunan (induk != null, mis. Kirim surat per jenis surat) yang belum diubah memakai teks induknya.
     */
    public static final class Template {
        public final String kode, judul, penanda, bawaan;
        public final Template induk;

        Template(String kode, String judul, String penanda, String bawaan) {
            this(kode, judul, penanda, bawaan, null);
        }

        Template(String kode, String judul, String penanda, String bawaan, Template induk) {
            this.kode = kode;
            this.judul = judul;
            this.penanda = penanda;
            this.bawaan = bawaan;
            this.induk = induk;
        }

        @Override
        public String toString() {
            return judul;
        }
    }

    // =====================================================
    // KE ADMIN PENDAFTARAN (nomor NOWABOOKINGPERIKSA)
    // =====================================================

    public static final Template BOOKING_PERIKSA_BARU = new Template("BOOKING_PERIKSA_BARU",
            "Booking periksa baru (ke admin pendaftaran)",
            "{nama} {poli} {tanggal} {notelp}",
            "Ada booking periksa baru dari website.\n\n"
            + "Nama : {nama}\n"
            + "Poli : {poli}\n"
            + "Tgl. Periksa : {tanggal}\n"
            + "No.Telp : {notelp}");

    // =====================================================
    // KE PASIEN, SAAT BOOKING SUDAH DIREGISTRASI
    // =====================================================

    public static final Template REGISTRASI_PASIEN = new Template("REGISTRASI_PASIEN",
            "Registrasi pasien (isi utama)",
            "{nama} {pembuka} {norm} {poli} {dokter} {tanggal} {noantrean} {penutup} {portal}\n"
            + "{pembuka} dan {penutup} diisi dari template Registrasi pembuka/penutup sesuai kondisi pasien.",
            "Halo {nama},\n\n"
            + "{pembuka}\n\n"
            + "No. RM   : {norm}\n"
            + "Poli     : {poli}\n"
            + "Dokter   : {dokter}\n"
            + "Tgl. Periksa : {tanggal}\n"
            + "No. Antrean  : {noantrean}\n\n"
            + "Mohon sudah berada di ruang tunggu sebelum nomor Anda dipanggil. "
            + "Jika Anda tidak ada saat dipanggil, giliran Anda akan dipindahkan ke setelah 4 antrean berikutnya.\n\n"
            + "{penutup}");

    public static final Template REGISTRASI_PEMBUKA_PASIEN_LAMA = new Template("REGISTRASI_PEMBUKA_PASIEN_LAMA",
            "Registrasi - pembuka pasien lama",
            "{norm}",
            "Pendaftaran Anda sudah terdaftar untuk periksa.");

    public static final Template REGISTRASI_PEMBUKA_PASIEN_BARU = new Template("REGISTRASI_PEMBUKA_PASIEN_BARU",
            "Registrasi - pembuka pasien baru",
            "{norm}",
            "Pendaftaran Anda sebagai pasien baru sudah kami terima dan Anda sudah terdaftar untuk periksa. No. Rekam Medis Anda: {norm}.");

    public static final Template REGISTRASI_PENUTUP_ADA_NIK = new Template("REGISTRASI_PENUTUP_ADA_NIK",
            "Registrasi - penutup bila NIK sudah ada",
            "{portal}",
            "Cek status antrean Anda dengan login ke {portal} menggunakan NIK dan nomor WhatsApp Anda.");

    public static final Template REGISTRASI_PENUTUP_TANPA_NIK = new Template("REGISTRASI_PENUTUP_TANPA_NIK",
            "Registrasi - penutup bila NIK belum ada",
            "{portal}",
            "Cek status antrean Anda dengan login ke {portal}. Login menggunakan NIK, jadi mohon lengkapi data NIK Anda di loket pendaftaran.");

    // =====================================================
    // KE PASIEN, PENGIRIMAN SURAT (PDF)
    // =====================================================

    public static final Template SURAT = new Template("SURAT",
            "Kirim surat (PDF) ke pasien",
            "{nama} {jenis_surat} {namars} {password} {portal} {verifikasi} {catatan}\n"
            + "{password} diisi template Surat - info password bila PDF diberi password, {catatan} diisi catatan khusus surat (mis. Surat Kontrol).",
            "Yth. Bapak/Ibu *{nama}*,\n\n"
            + "Terlampir *{jenis_surat}* Anda dari *{namars}*.\n\n"
            + "{password}\n\n"
            + "✅ Keaslian surat dapat diverifikasi melalui:\n"
            + "• {portal}\n"
            + "• {verifikasi}\n\n"
            + "{catatan}\n\n"
            + "⚠️ Pesan ini dikirim otomatis oleh sistem dan tidak dapat dibalas.\n\n"
            + "Terima kasih,\n"
            + "{namars}");

    /** Jenis surat yang dikirim lewat WA, sesuai jenisSurat yang dikirim tiap form surat ke GoWAService.pesanSurat. */
    public static final String[] JENIS_SURAT = {
        "Surat Keterangan Sakit", "Surat Keterangan Sehat", "Surat Keterangan Bebas Narkoba",
        "Surat Keterangan Buta Warna", "Surat Keterangan Rawat Inap", "Surat Kontrol"
    };

    /** Nama jenis surat di pilihan Template Pesan WA, disamakan dengan nama menunya bila berbeda dari judul surat. */
    public static String labelJenis(String jenisSurat) {
        // form SuratKeteranganRawatInap dibuka dari menu "Surat Keterangan Rawat Inap 2" (DlgKamarInap/DlgRawatInap)
        return "Surat Keterangan Rawat Inap".equals(jenisSurat) ? "Surat Keterangan Rawat Inap 2" : jenisSurat;
    }

    /** Template Kirim surat khusus satu jenis surat; selama belum diubah memakai template SURAT (umum). */
    public static Template suratJenis(String jenisSurat) {
        if (jenisSurat == null || jenisSurat.trim().isEmpty()) {
            return SURAT;
        }
        return new Template("SURAT_" + jenisSurat.trim().toUpperCase().replaceAll("[^A-Z0-9]+", "_"),
                "Kirim surat - " + labelJenis(jenisSurat.trim()), SURAT.penanda, SURAT.bawaan, SURAT);
    }

    public static final Template SURAT_PASSWORD = new Template("SURAT_PASSWORD",
            "Surat - info password PDF",
            "(tanpa penanda) dipakai sebagai {password} di template Kirim surat",
            "🔐 File PDF dilindungi kata sandi berupa tanggal lahir pasien dengan format ddMMyyyy (contoh: 17081990).");

    public static final Template SURAT_KONTROL_CATATAN = new Template("SURAT_KONTROL_CATATAN",
            "Surat Kontrol - catatan tambahan",
            "(tanpa penanda) dipakai sebagai {catatan} saat mengirim Surat Kontrol",
            "📞 Mohon konfirmasi kehadiran Anda melalui WA *+62 813-4641-6008*.");

    // =====================================================
    // PENGINGAT
    // =====================================================

    public static final Template PENGINGAT_KONTROL = new Template("PENGINGAT_KONTROL",
            "Pengingat jadwal kontrol pasien",
            "{nama} {hari_tanggal} {poli} {dokter} {jam} {namars}",
            "Selamat pagi *{nama}*.\n\n"
            + "Mengingatkan kembali jadwal kontrol Anda di *RS Pelita Kasih* pada:\n\n"
            + "*Hari/Tanggal* : {hari_tanggal}\n"
            + "*Poliklinik* : {poli}\n"
            + "*Dokter* : {dokter}\n"
            + "*Jam Praktek* : {jam}\n\n"
            + "Mohon konfirmasi kehadiran Anda ke WA: *+62 813-4641-6008*\n\n"
            + "Ketik *1* jika *HADIR*\n"
            + "Ketik *2* jika *BATAL / RESCHEDULE* _(Atur Jadwal Ulang)_\n\n"
            + "Harap datang *30 menit sebelum jam praktek*.\n"
            + "Terima kasih.");

    public static final Template PENGINGAT_INVENTARIS = new Template("PENGINGAT_INVENTARIS",
            "Pengingat perawatan inventaris (ke penanggung jawab)",
            "{kapan} {tanggal} {noinventaris} {barang} {ruang} {jenis} {pelaksana} {pj} {tgl_terakhir} {kegiatan} {namars}\n"
            + "{kapan} berisi \"hari ini\" atau \"besok\".",
            "*Pengingat Perawatan Aset/Inventaris*\n\n"
            + "Jadwal perawatan berikutnya jatuh pada *{kapan}, {tanggal}* untuk :\n\n"
            + "*No.Inventaris* : {noinventaris}\n"
            + "*Nama Barang* : {barang}\n"
            + "*Ruang* : {ruang}\n"
            + "*Jenis Pemeliharaan* : {jenis}\n"
            + "*Pelaksana* : {pelaksana}\n"
            + "*Penanggung Jawab* : {pj}\n\n"
            + "*Pemeliharaan terakhir* ({tgl_terakhir}) :\n"
            + "{kegiatan}\n\n"
            + "Mohon dipersiapkan. Terima kasih.\n_{namars}_");

    // =====================================================
    // AKUN USER SIMRS (ke nomor WA user, dari Setup User / setting.UserAkun)
    // =====================================================

    public static final Template AKUN_USER_ADMIN = new Template("AKUN_USER_ADMIN",
            "Akun SIMRS - dibuat/direset admin (ke user)",
            "{nama} {username} {password} {tanggal} {namars}",
            "*Akun SIMRS {namars}*\n\n"
            + "Halo *{nama}*, akun SIMRS Anda telah dibuat/diperbarui oleh admin.\n\n"
            + "*Username* : {username}\n"
            + "*Password* : {password}\n\n"
            + "Demi keamanan, segera ganti password melalui menu *Program > Ubah Password* "
            + "setelah login dan jangan bagikan kepada siapa pun.\n"
            + "_{tanggal}_");

    public static final Template AKUN_USER_UBAH = new Template("AKUN_USER_UBAH",
            "Akun SIMRS - password diubah sendiri oleh user (pemberitahuan keamanan)",
            "{nama} {username} {tanggal} {namars}\n"
            + "Sengaja tanpa {password}, password hanya diketahui user.",
            "*Pemberitahuan Keamanan Akun SIMRS*\n\n"
            + "Halo *{nama}*, password akun SIMRS Anda (username *{username}*) "
            + "baru saja diubah pada {tanggal}.\n\n"
            + "Jika bukan Anda yang melakukan perubahan ini, segera hubungi Unit IT.\n"
            + "_{namars}_");

    /** Urutan yang tampil di menu Template Pesan WA. */
    public static final Template[] DAFTAR = {
        SURAT, SURAT_PASSWORD, SURAT_KONTROL_CATATAN, PENGINGAT_KONTROL, PENGINGAT_INVENTARIS,
        BOOKING_PERIKSA_BARU, REGISTRASI_PASIEN, REGISTRASI_PEMBUKA_PASIEN_LAMA, REGISTRASI_PEMBUKA_PASIEN_BARU,
        REGISTRASI_PENUTUP_ADA_NIK, REGISTRASI_PENUTUP_TANPA_NIK, AKUN_USER_ADMIN, AKUN_USER_UBAH
    };

    // =====================================================
    // PENYIMPANAN (tabel set_pesan_wa)
    // =====================================================

    private static volatile boolean tabelSiap = false;

    /** Buat tabel bila belum ada (butuh hak CREATE), lihat update_set_pesan_wa.sql. */
    public static synchronized void siapkanTabel(Connection koneksi) {
        if (tabelSiap) {
            return;
        }
        try (Statement st = koneksi.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS `set_pesan_wa` ("
                    + "`kode` varchar(50) NOT NULL,"
                    + "`isi` text NOT NULL,"
                    + "PRIMARY KEY (`kode`)) ENGINE=InnoDB DEFAULT CHARSET=latin1");
            tabelSiap = true;
        } catch (Exception e) {
            System.out.println("Notif set_pesan_wa : " + e);
        }
    }

    /** Teks template: hasil ubahan di set_pesan_wa bila ada, selain itu teks induknya (template turunan) atau teks bawaan. */
    public static String ambil(Template t) {
        String tersimpan = ambilTersimpan(t);
        if (tersimpan != null) {
            return tersimpan;
        }
        return t.induk == null ? t.bawaan : ambil(t.induk);
    }

    /** Teks ubahan di set_pesan_wa, null bila belum pernah diubah (memakai teks bawaan). */
    public static String ambilTersimpan(Template t) {
        try {
            Connection koneksi = koneksiDB.condb();
            siapkanTabel(koneksi);
            try (PreparedStatement ps = koneksi.prepareStatement("select isi from set_pesan_wa where kode=?")) {
                ps.setString(1, t.kode);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return bukaEscape(rs.getString(1));
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Notif set_pesan_wa : " + e);
        }
        return null;
    }

    public static void simpan(Template t, String isi) throws Exception {
        Connection koneksi = koneksiDB.condb();
        siapkanTabel(koneksi);
        try (PreparedStatement ps = koneksi.prepareStatement(
                "insert into set_pesan_wa (kode,isi) values (?,?) on duplicate key update isi=values(isi)")) {
            ps.setString(1, t.kode);
            ps.setString(2, escape(isi));
            ps.executeUpdate();
        }
    }

    /** Hapus ubahan, template kembali memakai teks bawaan (template turunan kembali mengikuti induknya). */
    public static void kembalikanBawaan(Template t) throws Exception {
        Connection koneksi = koneksiDB.condb();
        siapkanTabel(koneksi);
        try (PreparedStatement ps = koneksi.prepareStatement("delete from set_pesan_wa where kode=?")) {
            ps.setString(1, t.kode);
            ps.executeUpdate();
        }
    }

    // Emoji & karakter non-ASCII disimpan sebagai \\uXXXX supaya aman di kolom/koneksi latin1 maupun utf8 3-byte.
    private static String escape(String teks) {
        StringBuilder sb = new StringBuilder();
        for (char c : teks.toCharArray()) {
            if (c == '\\') {
                sb.append("\\\\");
            } else if (c > 0x7E || (c < 0x20 && c != '\n' && c != '\t')) {
                sb.append(String.format("\\u%04X", (int) c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static String bukaEscape(String teks) {
        if (teks == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < teks.length(); i++) {
            char c = teks.charAt(i);
            if (c == '\\' && i + 1 < teks.length()) {
                char n = teks.charAt(i + 1);
                if (n == '\\') {
                    sb.append('\\');
                    i++;
                    continue;
                }
                if (n == 'u' && i + 5 < teks.length()) {
                    try {
                        sb.append((char) Integer.parseInt(teks.substring(i + 2, i + 6), 16));
                        i += 5;
                        continue;
                    } catch (NumberFormatException e) {
                        // bukan escape, tulis apa adanya
                    }
                }
            }
            sb.append(c);
        }
        return sb.toString();
    }

    // =====================================================
    // PENGISI PENANDA
    // =====================================================

    /**
     * Ganti tiap {kunci} di template dengan nilainya (null jadi kosong), lalu rapikan baris kosong
     * berlebih yang muncul bila penanda opsional (mis. {password}, {catatan}) diisi kosong.
     */
    public static String isi(String template, Map<String, String> data) {
        String hasil = template;
        for (Map.Entry<String, String> e : data.entrySet()) {
            hasil = hasil.replace("{" + e.getKey() + "}", e.getValue() == null ? "" : e.getValue());
        }
        return hasil.replaceAll("\n[ \t]*\n([ \t]*\n)+", "\n\n").trim();
    }

    /** Ambil template (ubahan atau bawaan) lalu isi penandanya. */
    public static String isi(Template t, Map<String, String> data) {
        return isi(ambil(t), data);
    }
}
