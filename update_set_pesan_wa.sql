-- Template Pesan WA (menu Program > Template Pesan WA, src/komunikasi/DlgSetPesanWA.java).
-- Hanya menyimpan template yang DIUBAH; template yang tidak ada barisnya memakai teks bawaan
-- di src/komunikasi/pesanWA.java. Kolom isi menyimpan emoji/karakter non-ASCII sebagai \uXXXX.
-- Dibuat otomatis oleh aplikasi saat template pertama kali dibaca bila belum ada (butuh hak CREATE).
-- Jalankan terhadap DATABASE UTAMA SIMRS.
CREATE TABLE IF NOT EXISTS `set_pesan_wa` (
  `kode` varchar(50) NOT NULL,
  `isi` text NOT NULL,
  PRIMARY KEY (`kode`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
