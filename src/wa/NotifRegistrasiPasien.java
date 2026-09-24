package wa;

import fungsi.koneksiDB;
import fungsi.koneksiDBWa;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * WA ke pasien saat booking-nya sudah diregistrasi (reg_periksa dibuat):
 * poli, dokter, tanggal, no. antrean, dan ajakan login ke portal pasien
 * untuk cek status antrean. Teks pesannya diatur di wa/pesanWA.java.
 *
 * Aktif bila NOTIFWAREGISTRASIPASIEN=yes (dibaca lewat koneksiDBWa dari setting/database-extra.xml).
 * Dikirim di thread terpisah agar layar pendaftaran tidak menunggu GoWA.
 */
public final class NotifRegistrasiPasien {

    private NotifRegistrasiPasien() {}

    public static void kirim(String noRawat) {
        if (noRawat == null || noRawat.trim().equals("")
                || !koneksiDBWa.NOTIFWAREGISTRASIPASIEN().equalsIgnoreCase("yes")) {
            return;
        }

        new Thread(() -> {
            try {
                kirimSekarang(noRawat);
            } catch (Exception e) {
                System.out.println("Notif WA registrasi pasien gagal : " + e);
            }
        }).start();
    }

    private static void kirimSekarang(String noRawat) throws Exception {
        Connection koneksi = koneksiDB.condb();
        String noRm, nama, noKtp, noTlp, poli, dokter, noReg, tanggal;

        try (PreparedStatement ps = koneksi.prepareStatement(
                "select reg_periksa.no_rkm_medis,reg_periksa.no_reg,reg_periksa.tgl_registrasi,pasien.nm_pasien,pasien.no_ktp,pasien.no_tlp,"
                + "poliklinik.nm_poli,dokter.nm_dokter from reg_periksa "
                + "left join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                + "left join poliklinik on reg_periksa.kd_poli=poliklinik.kd_poli "
                + "left join dokter on reg_periksa.kd_dokter=dokter.kd_dokter "
                + "where reg_periksa.no_rawat=?")) {
            ps.setString(1, noRawat);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return;
                }
                noRm = nz(rs.getString("no_rkm_medis"));
                noReg = nz(rs.getString("no_reg"));
                tanggal = new SimpleDateFormat("dd-MM-yyyy").format(rs.getDate("tgl_registrasi"));
                nama = nz(rs.getString("nm_pasien"));
                noKtp = nz(rs.getString("no_ktp"));
                noTlp = nz(rs.getString("no_tlp"));
                poli = nz(rs.getString("nm_poli"));
                dokter = nz(rs.getString("nm_dokter"));
            }
        }

        if (noTlp.replaceAll("[^0-9]", "").length() < 9) {
            return;
        }

        boolean pasienBaru;
        try (PreparedStatement ps = koneksi.prepareStatement("select count(*) from reg_periksa where no_rkm_medis=?")) {
            ps.setString(1, noRm);
            try (ResultSet rs = ps.executeQuery()) {
                pasienBaru = rs.next() && rs.getInt(1) == 1;
            }
        }

        String portal = koneksiDBWa.URLPORTALPASIEN();
        boolean adaNik = !noKtp.trim().equals("") && !noKtp.trim().equals("-");

        // Teks pesan diatur di wa/pesanWA.java.
        String pembuka = pesanWA.isi(pasienBaru ? pesanWA.REGISTRASI_PEMBUKA_PASIEN_BARU : pesanWA.REGISTRASI_PEMBUKA_PASIEN_LAMA,
                Map.of("norm", noRm));
        String penutup = pesanWA.isi(adaNik ? pesanWA.REGISTRASI_PENUTUP_ADA_NIK : pesanWA.REGISTRASI_PENUTUP_TANPA_NIK,
                Map.of("portal", portal));

        Map<String, String> data = new HashMap<>();
        data.put("nama", nama);
        data.put("pembuka", pembuka);
        data.put("norm", noRm);
        data.put("poli", poli);
        data.put("dokter", dokter);
        data.put("tanggal", tanggal);
        data.put("noantrean", noReg);
        data.put("penutup", penutup);
        data.put("portal", portal);
        String pesan = pesanWA.isi(pesanWA.REGISTRASI_PASIEN, data);

        GoWAService.kirimText(noTlp, pesan);
    }

    private static String nz(String nilai) {
        return nilai == null ? "" : nilai;
    }
}
