-- Notifikasi lonceng frmUtama (paket komunikasi: ChatNotifier)
--   Dokter  : hasil lab & radiologi pasien (Perujuk / Dokter PJ / DPJP)
--   Petugas : permintaan lab PK/PA/MB & radiologi, resep, booking periksa, booking registrasi (sesuai hak akses menu)
-- Dibuat otomatis oleh aplikasi saat login bila belum ada (butuh hak CREATE/ALTER). Jalankan setelah update_chat_antar_user.sql.

-- Notifikasi yang sudah dilihat/ditandai user dari daftar lonceng. Data > 7 hari dihapus otomatis saat user login.
-- kunci: no_rawat (hasil), noorder (permintaan), no_resep, no_booking, atau no_rkm_medis (booking registrasi).
CREATE TABLE IF NOT EXISTS `notif_dilihat` (
  `kode_user` varchar(50) NOT NULL,
  `jenis` varchar(10) NOT NULL,
  `kunci` varchar(20) NOT NULL,
  `tgl` date NOT NULL,
  `jam` time NOT NULL,
  `waktu` datetime NOT NULL,
  PRIMARY KEY (`kode_user`,`jenis`,`kunci`,`tgl`,`jam`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Tanggal user pertama kali memakai notifikasi; data sebelum tanggal ini tidak dihitung baru.
ALTER TABLE `chat_online` ADD COLUMN IF NOT EXISTS `notif_mulai` date DEFAULT NULL;

-- Sisa versi uji sebelumnya (boleh dihapus bila ada):
-- DROP TABLE IF EXISTS `notif_hasil_dilihat`;
-- ALTER TABLE `chat_online` DROP COLUMN IF EXISTS `hasil_mulai`;
