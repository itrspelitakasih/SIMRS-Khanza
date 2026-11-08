-- Penandaan gambar status lokalis di Penilaian Awal Medis IGD (src/rekammedis/RMPenilaianAwalMedisIGD.java,
-- komponen src/widget/PanelLokalis.java). Satu baris per penilaian, data = "kode,x,y;kode,x,y;..."
-- (x,y relatif 0..1 terhadap gambar /picture/semua.png). Ikut terhapus/terganti bila penilaian_medis_igd dihapus/diubah.
-- Dibuat otomatis oleh aplikasi saat form dibuka bila belum ada (butuh hak CREATE).
-- Jalankan terhadap DATABASE UTAMA SIMRS.
CREATE TABLE IF NOT EXISTS `penilaian_medis_igd_lokalis` (
  `no_rawat` varchar(17) NOT NULL,
  `data` text NOT NULL,
  PRIMARY KEY (`no_rawat`),
  CONSTRAINT `penilaian_medis_igd_lokalis_ibfk_1` FOREIGN KEY (`no_rawat`) REFERENCES `penilaian_medis_igd` (`no_rawat`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
