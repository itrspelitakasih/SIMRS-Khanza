package setting;

import fungsi.akses;
import fungsi.koneksiDB;
import java.awt.Component;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import komunikasi.GoWAService;
import komunikasi.pesanWA;

/**
 * Data tambahan akun user SIMRS untuk Setup User (admin): nama & nomor WA (tabel user_kontak),
 * generate password dan notifikasi WA saat akun dibuat / password direset admin.
 *
 * User "manual" = id_user yang bukan kode dokter / NIP petugas. Namanya disimpan di user_kontak,
 * sedangkan nomor WA dokter/petugas memakai no_telp masing-masing bila user_kontak.no_wa kosong.
 */
public final class UserAkun {

    private UserAkun() {}

    private static final String HURUF = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
    private static final SecureRandom ACAK = new SecureRandom();
    private static volatile boolean tabelSiap = false;

    /** Buat tabel bila belum ada (butuh hak CREATE), lihat update_user_kontak.sql. */
    public static synchronized void siapkanTabel(Connection koneksi) {
        if (tabelSiap) {
            return;
        }
        try (Statement st = koneksi.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS `user_kontak` ("
                    + "`id_user` varchar(700) NOT NULL,"
                    + "`nama` varchar(100) DEFAULT NULL,"
                    + "`no_wa` varchar(20) DEFAULT NULL,"
                    + "PRIMARY KEY (`id_user`)) ENGINE=InnoDB DEFAULT CHARSET=latin1");
            tabelSiap = true;
        } catch (Exception e) {
            System.out.println("Notif user_kontak : " + e);
        }
    }

    private static Connection koneksi() {
        Connection koneksi = koneksiDB.condb();
        siapkanTabel(koneksi);
        return koneksi;
    }

    private static String cari(String sql, String id) {
        try (PreparedStatement ps = koneksi().prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1) == null ? "" : rs.getString(1).trim();
                }
            }
        } catch (Exception e) {
            System.out.println("Notif user_kontak : " + e);
        }
        return "";
    }

    private static boolean ada(String sql, String id) {
        try (PreparedStatement ps = koneksi().prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            System.out.println("Notif user_kontak : " + e);
        }
        return false;
    }

    /** true bila id_user adalah kode dokter atau NIP petugas (bukan user manual). */
    public static boolean terhubungPegawai(String idUser) {
        return ada("select kd_dokter from dokter where kd_dokter=?", idUser)
                || ada("select nip from petugas where nip=?", idUser);
    }

    /** Nama user manual yang tersimpan di user_kontak. */
    public static String namaKontak(String idUser) {
        return cari("select nama from user_kontak where id_user=AES_ENCRYPT(?,'nur')", idUser);
    }

    /** Nomor WA yang tersimpan di user_kontak saja. */
    public static String noWATersimpan(String idUser) {
        return cari("select no_wa from user_kontak where id_user=AES_ENCRYPT(?,'nur')", idUser);
    }

    /** Nomor WA user: user_kontak.no_wa, bila kosong no_telp dokter lalu no_telp petugas. */
    public static String noWA(String idUser) {
        String nomor = noWATersimpan(idUser);
        if (nomor.isEmpty()) {
            nomor = cari("select no_telp from dokter where kd_dokter=?", idUser);
        }
        if (nomor.isEmpty()) {
            nomor = cari("select no_telp from petugas where nip=?", idUser);
        }
        return nomor;
    }

    /** Simpan nama (khusus user manual) & nomor WA; baris dihapus bila keduanya kosong. */
    public static void simpanKontak(String idUser, String nama, String noWA) {
        nama = nama == null ? "" : nama.trim();
        noWA = noWA == null ? "" : noWA.trim();
        if (nama.isEmpty() && noWA.isEmpty()) {
            hapusKontak(idUser);
            return;
        }
        try (PreparedStatement ps = koneksi().prepareStatement(
                "insert into user_kontak (id_user,nama,no_wa) values (AES_ENCRYPT(?,'nur'),?,?) "
                + "on duplicate key update nama=values(nama),no_wa=values(no_wa)")) {
            ps.setString(1, idUser);
            ps.setString(2, nama);
            ps.setString(3, noWA);
            ps.executeUpdate();
        } catch (Exception e) {
            System.out.println("Notif user_kontak : " + e);
        }
    }

    public static void gantiId(String idLama, String idBaru) {
        try (PreparedStatement ps = koneksi().prepareStatement(
                "update user_kontak set id_user=AES_ENCRYPT(?,'nur') where id_user=AES_ENCRYPT(?,'nur')")) {
            ps.setString(1, idBaru);
            ps.setString(2, idLama);
            ps.executeUpdate();
        } catch (Exception e) {
            System.out.println("Notif user_kontak : " + e);
        }
    }

    public static void hapusKontak(String idUser) {
        try (PreparedStatement ps = koneksi().prepareStatement(
                "delete from user_kontak where id_user=AES_ENCRYPT(?,'nur')")) {
            ps.setString(1, idUser);
            ps.executeUpdate();
        } catch (Exception e) {
            System.out.println("Notif user_kontak : " + e);
        }
    }

    /** Password acak tanpa karakter yang mirip (0/O, 1/l/I). */
    public static String generatePassword(int panjang) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < panjang; i++) {
            sb.append(HURUF.charAt(ACAK.nextInt(HURUF.length())));
        }
        return sb.toString();
    }

    private static String sekarang() {
        return new SimpleDateFormat("dd-MM-yyyy HH:mm").format(new Date());
    }

    /** WA ke user: akun dibuat / password direset oleh admin, berisi username & password baru. */
    public static void kirimAkunAdmin(Component parent, String noWA, String idUser, String nama, String password) {
        kirim(parent, noWA, pesanAkunAdmin(idUser, nama, password));
    }

    /** WA pemberitahuan keamanan saat user mengubah password-nya sendiri (tanpa password). */
    public static void kirimPasswordDiubah(String idUser) {
        String nama = cari("select nm_dokter from dokter where kd_dokter=?", idUser);
        if (nama.isEmpty()) {
            nama = cari("select nama from petugas where nip=?", idUser);
        }
        if (nama.isEmpty()) {
            nama = namaKontak(idUser);
        }
        Map<String, String> data = new HashMap<>();
        data.put("nama", nama.isEmpty() ? idUser : nama);
        data.put("username", idUser);
        data.put("tanggal", sekarang());
        data.put("namars", akses.getnamars());
        // parent null: dialog & sesi sudah ditutup (Log Out) saat pengiriman selesai
        kirim(null, noWA(idUser), pesanWA.isi(pesanWA.AKUN_USER_UBAH, data));
    }

    /** Isi pesan WA akun dibuat / password direset admin (template AKUN_USER_ADMIN). */
    public static String pesanAkunAdmin(String idUser, String nama, String password) {
        Map<String, String> data = new HashMap<>();
        data.put("nama", nama);
        data.put("username", idUser);
        data.put("password", password);
        data.put("tanggal", sekarang());
        data.put("namars", akses.getnamars());
        return pesanWA.isi(pesanWA.AKUN_USER_ADMIN, data);
    }

    private static void kirim(Component parent, String noWA, String pesan) {
        if (noWA == null || noWA.trim().isEmpty()) {
            return;
        }
        new Thread(() -> {
            if (!GoWAService.kirimText(noWA, pesan)) {
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(parent,
                        "Notifikasi WA ke " + noWA + " gagal dikirim.\nPeriksa setting GoWA / nomor WA user."));
            }
        }).start();
    }
}
