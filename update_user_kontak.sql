-- Nama & nomor WA akun user SIMRS (src/setting/UserAkun.java), dipakai Setup User (DlgUser)
-- untuk user manual dan notifikasi WA saat admin membuat user / generate password.
-- id_user disimpan terenkripsi sama seperti tabel user: AES_ENCRYPT(id,'nur').
-- nama hanya diisi untuk user manual (bukan kode dokter / NIP petugas).
-- no_wa kosong = memakai no_telp dokter / petugas.
-- Dibuat otomatis oleh aplikasi bila belum ada (butuh hak CREATE).
-- Jalankan terhadap DATABASE UTAMA SIMRS.
CREATE TABLE IF NOT EXISTS `user_kontak` (
  `id_user` varchar(700) NOT NULL,
  `nama` varchar(100) DEFAULT NULL,
  `no_wa` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`id_user`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
