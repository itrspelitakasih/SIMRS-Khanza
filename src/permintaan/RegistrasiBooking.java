package permintaan;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Mengubah satu booking_registrasi berstatus 'Belum' menjadi registrasi
 * (reg_periksa), dengan isi kolom yang sama seperti tombol "Registrasi" di
 * DlgBookingRegistrasi (BtnEditActionPerformed), lalu menandai booking
 * 'Terdaftar'. Dipakai DlgBookingPeriksa untuk "Langsung Registrasi" saat
 * booking pasien baru diterima.
 *
 * Tidak mengatur transaksi sendiri: memakai koneksi pemanggil, sehingga ikut
 * commit/rollback transaksi pemanggil.
 */
public final class RegistrasiBooking {

    private RegistrasiBooking() {}

    /**
     * @return no_rawat yang dibuat, atau no_rawat yang sudah ada bila loket
     *         sudah meregistrasi kunjungan yang sama. String kosong bila
     *         booking tidak ditemukan / sudah tidak berstatus 'Belum'.
     */
    public static String registrasi(Connection koneksi, String noRkmMedis, String tanggalPeriksa) throws SQLException {
        String kdDokter, kdPoli, noReg, kdPj, status;
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select kd_dokter,kd_poli,no_reg,kd_pj,status from booking_registrasi where no_rkm_medis=? and tanggal_periksa=? for update")) {
            ps.setString(1, noRkmMedis);
            ps.setString(2, tanggalPeriksa);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return "";
                }
                kdDokter = rs.getString("kd_dokter");
                kdPoli = rs.getString("kd_poli");
                noReg = nz(rs.getString("no_reg"), "");
                kdPj = rs.getString("kd_pj");
                status = nz(rs.getString("status"), "");
            }
        }

        if (!status.equalsIgnoreCase("Belum")) {
            return "";
        }

        String sudahAda = "";
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select no_rawat from reg_periksa where no_rkm_medis=? and tgl_registrasi=? and kd_poli=? and kd_dokter=? limit 1")) {
            ps.setString(1, noRkmMedis);
            ps.setString(2, tanggalPeriksa);
            ps.setString(3, kdPoli);
            ps.setString(4, kdDokter);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    sudahAda = rs.getString(1);
                }
            }
        }

        if (!sudahAda.equals("")) {
            tandaiTerdaftar(koneksi, noRkmMedis, tanggalPeriksa);
            return sudahAda;
        }

        String pJawab = "", almtPj = "", hubunganPj = "";
        int tahun = 0, bulan = 0, hari = 0;
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select pasien.namakeluarga,pasien.keluarga,"
                + "concat(ifnull(pasien.alamat,''),', ',ifnull(kelurahan.nm_kel,''),', ',ifnull(kecamatan.nm_kec,''),', ',ifnull(kabupaten.nm_kab,'')) as alamat,"
                + "pasien.kelurahanpj,pasien.kecamatanpj,pasien.kabupatenpj,pasien.propinsipj,"
                + "TIMESTAMPDIFF(YEAR, pasien.tgl_lahir, CURDATE()) as tahun, "
                + "(TIMESTAMPDIFF(MONTH, pasien.tgl_lahir, CURDATE()) - ((TIMESTAMPDIFF(MONTH, pasien.tgl_lahir, CURDATE()) div 12) * 12)) as bulan, "
                + "TIMESTAMPDIFF(DAY, DATE_ADD(DATE_ADD(pasien.tgl_lahir,INTERVAL TIMESTAMPDIFF(YEAR, pasien.tgl_lahir, CURDATE()) YEAR), INTERVAL TIMESTAMPDIFF(MONTH, pasien.tgl_lahir, CURDATE()) - ((TIMESTAMPDIFF(MONTH, pasien.tgl_lahir, CURDATE()) div 12) * 12) MONTH), CURDATE()) as hari "
                + "from pasien left join kelurahan on pasien.kd_kel=kelurahan.kd_kel "
                + "left join kecamatan on pasien.kd_kec=kecamatan.kd_kec "
                + "left join kabupaten on pasien.kd_kab=kabupaten.kd_kab "
                + "where pasien.no_rkm_medis=?")) {
            ps.setString(1, noRkmMedis);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return "";
                }
                pJawab = nz(rs.getString("namakeluarga"), "");
                almtPj = nz(rs.getString("alamat"), "") + ", " + nz(rs.getString("kelurahanpj"), "")
                        + ", " + nz(rs.getString("kecamatanpj"), "") + ", " + nz(rs.getString("kabupatenpj"), "")
                        + ", " + nz(rs.getString("propinsipj"), "");
                hubunganPj = nz(rs.getString("keluarga"), "");
                tahun = rs.getInt("tahun");
                bulan = rs.getInt("bulan");
                hari = rs.getInt("hari");
            }
        }

        // Sama seperti DlgBookingRegistrasi: tahun bila > 0, lalu bulan, lalu hari.
        String umur, sttsUmur;
        if (tahun > 0) {
            umur = "" + tahun;
            sttsUmur = "Th";
        } else if (bulan > 0) {
            umur = "" + bulan;
            sttsUmur = "Bl";
        } else {
            umur = "" + hari;
            sttsUmur = "Hr";
        }

        String statusPoli = hitung(koneksi,
                "select count(no_rkm_medis) from reg_periksa where no_rkm_medis=? and kd_poli=?", noRkmMedis, kdPoli) > 0 ? "Lama" : "Baru";

        String biayaReg = "0";
        try (PreparedStatement ps = koneksi.prepareStatement("select registrasilama from poliklinik where kd_poli=?")) {
            ps.setString(1, kdPoli);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    biayaReg = "" + rs.getDouble(1);
                }
            }
        }

        String noRawat = nomorRawatBerikutnya(koneksi, tanggalPeriksa);

        try (PreparedStatement ps = koneksi.prepareStatement(
                "insert into reg_periksa (no_reg,no_rawat,tgl_registrasi,jam_reg,kd_dokter,no_rkm_medis,kd_poli,p_jawab,almt_pj,hubunganpj,"
                + "biaya_reg,stts,stts_daftar,status_lanjut,kd_pj,umurdaftar,sttsumur,status_bayar,status_poli) "
                + "values (?,?,?,?,?,?,?,?,?,?,?,'Belum','Lama','Ralan',?,?,?,'Belum Bayar',?)")) {
            ps.setString(1, noReg);
            ps.setString(2, noRawat);
            ps.setString(3, tanggalPeriksa);
            ps.setString(4, new SimpleDateFormat("HH:mm:ss").format(new Date()));
            ps.setString(5, kdDokter);
            ps.setString(6, noRkmMedis);
            ps.setString(7, kdPoli);
            ps.setString(8, pJawab);
            ps.setString(9, almtPj);
            ps.setString(10, hubunganPj);
            ps.setString(11, biayaReg);
            ps.setString(12, kdPj);
            ps.setString(13, umur);
            ps.setString(14, sttsUmur);
            ps.setString(15, statusPoli);
            ps.executeUpdate();
        }

        tandaiTerdaftar(koneksi, noRkmMedis, tanggalPeriksa);
        return noRawat;
    }

    /** Format no_rawat Khanza: yyyy/MM/dd/ + 6 digit urut per tanggal registrasi. */
    private static String nomorRawatBerikutnya(Connection koneksi, String tanggalPeriksa) throws SQLException {
        int terakhir = hitung(koneksi,
                "select ifnull(MAX(CONVERT(RIGHT(no_rawat,6),signed)),0) from reg_periksa where tgl_registrasi=?", tanggalPeriksa);
        return tanggalPeriksa.replace("-", "/") + "/" + String.format("%06d", terakhir + 1);
    }

    private static void tandaiTerdaftar(Connection koneksi, String noRkmMedis, String tanggalPeriksa) throws SQLException {
        try (PreparedStatement ps = koneksi.prepareStatement(
                "update booking_registrasi set status='Terdaftar' where no_rkm_medis=? and tanggal_periksa=?")) {
            ps.setString(1, noRkmMedis);
            ps.setString(2, tanggalPeriksa);
            ps.executeUpdate();
        }
    }

    private static int hitung(Connection koneksi, String sql, String... params) throws SQLException {
        try (PreparedStatement ps = koneksi.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setString(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private static String nz(String nilai, String pengganti) {
        return nilai == null ? pengganti : nilai;
    }
}
