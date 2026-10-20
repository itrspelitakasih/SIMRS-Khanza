-- Surat Keterangan Menemani Berobat (src/surat/SuratKeteranganMenemaniBerobat.java),
-- isi surat diambil dari data penanggung jawab pasien (pasien.namakeluarga, alamatpj + kelurahanpj/kecamatanpj/kabupatenpj, pekerjaanpj).
-- Dibuat otomatis oleh aplikasi saat form dibuka bila belum ada (butuh hak CREATE).
-- Jalankan terhadap DATABASE UTAMA SIMRS.
CREATE TABLE IF NOT EXISTS `surat_keterangan_menemani_berobat` (
  `no_surat` varchar(30) NOT NULL,
  `no_rawat` varchar(17) DEFAULT NULL,
  `tanggal_surat` date DEFAULT NULL,
  PRIMARY KEY (`no_surat`),
  KEY `no_rawat` (`no_rawat`),
  CONSTRAINT `surat_keterangan_menemani_berobat_ibfk_1` FOREIGN KEY (`no_rawat`) REFERENCES `reg_periksa` (`no_rawat`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Nomor surat format RSPK (NNN/KODE/RJ-RSPK/BULAN_ROMAWI/TAHUN, src/surat/NomorSuratRSPK.java) lebih panjang
-- dari kolom lama; juga dilebarkan otomatis oleh aplikasi saat form dibuka (butuh hak ALTER).
ALTER TABLE `surat_keterangan_menemani_berobat` MODIFY `no_surat` varchar(30) NOT NULL;
ALTER TABLE `surat_keterangan_berobat` MODIFY `no_surat` varchar(30) NOT NULL;
-- Surat Keterangan Rawat Inap 2 (001/SKet/RI-RSPK/III/2026), Bebas Narkoba (0001/SKBN/RJ-RSPK/VIII/2025)
-- dan Cuti Hamil (001/SKH/RJ-RSPK/VIII/2025) memakai format yang sama; hanya dilebarkan bila masih lebih pendek.
ALTER TABLE `surat_keterangan_rawat_inap` MODIFY `no_surat` varchar(30) NOT NULL;
ALTER TABLE `surat_skbn` MODIFY `no_surat` varchar(30) NOT NULL;
ALTER TABLE `surat_cuti_hamil` MODIFY `no_surat` varchar(30) NOT NULL;
