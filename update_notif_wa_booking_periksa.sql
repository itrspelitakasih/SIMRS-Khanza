-- Tabel penanda "sudah dinotifikasi WA" untuk booking periksa online.
-- Dipakai oleh permintaan/DlgBookingPeriksa.java supaya notifikasi WA ke
-- HP pendaftaran hanya terkirim SEKALI per booking, walau menu Booking
-- Periksa Online dibuka bersamaan di beberapa komputer pendaftaran
-- (PRIMARY KEY no_booking dipakai sebagai penanda klaim: insert pertama
-- yang berhasil dari salah satu komputer yang berhak mengirim WA,
-- komputer lain akan gagal insert karena duplikat dan tidak ikut mengirim).
--
-- Jalankan sekali setelah backup database.

USE `sik2023_server`;

CREATE TABLE IF NOT EXISTS `booking_periksa_notifwa` (
  `no_booking` varchar(17) NOT NULL,
  `tanggal_notif` datetime NOT NULL,
  PRIMARY KEY (`no_booking`),
  CONSTRAINT `booking_periksa_notifwa_ibfk_1` FOREIGN KEY (`no_booking`) REFERENCES `booking_periksa` (`no_booking`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
