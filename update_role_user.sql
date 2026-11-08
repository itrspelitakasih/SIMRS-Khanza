-- Role Hak Akses User (src/setting/DlgRoleUser.java, menu "Role Hak Akses User", khusus Admin Utama).
-- Role = template kumpulan kolom hak akses tabel `user`. Memasang role menulis hak akses ke kolom user
-- (cara Tambahkan / Ganti); login (akses.java) tidak berubah, jadi Set User / Update User tetap berlaku.
-- Tabel dibuat otomatis saat form dibuka. File ini untuk dijalankan manual / mengisi role lewat script.
-- Jalankan terhadap DATABASE UTAMA SIMRS.

CREATE TABLE IF NOT EXISTS `role_user` (
  `kd_role` varchar(20) NOT NULL,
  `nm_role` varchar(100) NOT NULL,
  `keterangan` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`kd_role`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- kolom = nama kolom hak akses di tabel `user` (misal registrasi, billing_ralan, odontogram)
CREATE TABLE IF NOT EXISTS `role_user_akses` (
  `kd_role` varchar(20) NOT NULL,
  `kolom` varchar(100) NOT NULL,
  PRIMARY KEY (`kd_role`,`kolom`),
  CONSTRAINT `role_user_akses_ibfk_1` FOREIGN KEY (`kd_role`) REFERENCES `role_user` (`kd_role`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- role yang dipegang user; id_user = ID login biasa (bukan terenkripsi). Satu user boleh banyak role (gabungan).
CREATE TABLE IF NOT EXISTS `user_role` (
  `id_user` varchar(100) NOT NULL,
  `kd_role` varchar(20) NOT NULL,
  PRIMARY KEY (`id_user`,`kd_role`),
  KEY `kd_role` (`kd_role`),
  CONSTRAINT `user_role_ibfk_1` FOREIGN KEY (`kd_role`) REFERENCES `role_user` (`kd_role`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- role bawaan (isi hak aksesnya diatur lewat form atau script di bawah)
INSERT IGNORE INTO `role_user` (`kd_role`,`nm_role`,`keterangan`) VALUES
('KASIR','Kasir',''),('APOTIK','Apotik / Farmasi',''),('UGD','UGD / IGD',''),('MCU','MCU',''),
('LAB','Laboratorium',''),('RADIOLOGI','Radiologi',''),('DOKTER_UMUM','Dokter Umum',''),('KESLING','Kesehatan Lingkungan',''),
('RM','Rekam Medis',''),('ADMIN','Admin',''),('AKUNTANSI','Akuntansi',''),('GIZI','Gizi',''),('RANAP','Rawat Inap','');

-- ============ CONTOH mengisi role lewat script ============
-- 1) Tambah hak akses ke role:
--    INSERT IGNORE INTO role_user_akses (kd_role,kolom) VALUES ('KASIR','billing_ralan'),('KASIR','billing_ranap');
-- 2) Daftar nama kolom hak akses yang tersedia:
--    SELECT column_name FROM information_schema.columns
--     WHERE table_schema=DATABASE() AND table_name='user' AND column_type="enum('true','false')";
-- 3) Role baru:
--    INSERT INTO role_user (kd_role,nm_role,keterangan) VALUES ('PERAWAT_POLI','Perawat Poli','');
-- 4) Catat user pemegang role (hak akses baru tertulis ke user setelah "Terapkan Ulang" di form):
--    INSERT IGNORE INTO user_role (id_user,kd_role) VALUES ('kasir01','KASIR');
