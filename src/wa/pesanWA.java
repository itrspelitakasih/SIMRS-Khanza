package wa;

import java.util.Map;

/**
 * Semua teks pesan WhatsApp booking & registrasi, di satu tempat supaya mudah
 * diubah. Ubah teks di konstanta TEMPLATE di bawah; penanda {nama}, {poli},
 * dst. diganti otomatis dengan data sebenarnya. Penanda yang tersedia ditulis
 * di atas tiap template. *teks* = tebal, _teks_ = miring (format WhatsApp).
 */
public final class pesanWA {

    private pesanWA() {}

    // =====================================================
    // KE ADMIN PENDAFTARAN (nomor NOWABOOKINGPERIKSA)
    // =====================================================

    /** Booking periksa (pasien baru) masuk. Penanda: {nama} {poli} {tanggal} {notelp} */
    public static final String BOOKING_PERIKSA_BARU =
            "Ada booking periksa baru dari website.\n\n"
            + "Nama : {nama}\n"
            + "Poli : {poli}\n"
            + "Tgl. Periksa : {tanggal}\n"
            + "No.Telp : {notelp}";

    // =====================================================
    // KE PASIEN, SAAT BOOKING SUDAH DIREGISTRASI
    // =====================================================

    /**
     * Isi utama. Penanda: {nama} {pembuka} {norm} {poli} {dokter} {tanggal}
     * {noantrean} {penutup}. {pembuka} dan {penutup} diisi dari template
     * di bawahnya sesuai kondisi pasien.
     */
    public static final String REGISTRASI_PASIEN =
            "Halo {nama},\n\n"
            + "{pembuka}\n\n"
            + "No. RM : {norm}\n"
            + "Poli : {poli}\n"
            + "Dokter : {dokter}\n"
            + "Tgl. Periksa : {tanggal}\n"
            + "No. Antrean : {noantrean}\n\n"
            + "{penutup}";

    /** {pembuka} untuk pasien lama. */
    public static final String REGISTRASI_PEMBUKA_PASIEN_LAMA =
            "Pendaftaran Anda sudah terdaftar untuk periksa.";

    /** {pembuka} untuk pasien baru (kunjungan pertama). Penanda: {norm} */
    public static final String REGISTRASI_PEMBUKA_PASIEN_BARU =
            "Pendaftaran Anda sebagai pasien baru sudah kami terima dan Anda sudah terdaftar untuk periksa. No. Rekam Medis Anda: {norm}.";

    /** {penutup} bila NIK pasien sudah ada. Penanda: {portal} */
    public static final String REGISTRASI_PENUTUP_ADA_NIK =
            "Cek status antrean Anda dengan login ke {portal} menggunakan NIK dan nomor WhatsApp Anda.";

    /** {penutup} bila NIK pasien belum ada (login portal memakai NIK). Penanda: {portal} */
    public static final String REGISTRASI_PENUTUP_TANPA_NIK =
            "Cek status antrean Anda dengan login ke {portal}. Login memakai NIK, jadi mohon lengkapi NIK Anda di loket pendaftaran.";

    // =====================================================
    // PENGISI PENANDA
    // =====================================================

    /** Ganti tiap {kunci} di template dengan nilainya (null jadi kosong). */
    public static String isi(String template, Map<String, String> data) {
        String hasil = template;
        for (Map.Entry<String, String> e : data.entrySet()) {
            hasil = hasil.replace("{" + e.getKey() + "}", e.getValue() == null ? "" : e.getValue());
        }
        return hasil;
    }
}
