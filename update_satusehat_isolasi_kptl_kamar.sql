-- Migrasi untuk modul baru yang diambil dari upstream mas-elkhanza/SIMRS-Khanza
-- (commit range 0c13b082..f53225af, Juli-Agustus 2026):
--   - Bridging SATUSEHAT: Tanda Tangan Elektronik (TTE) & Kirim Composition RME
--     (src/bridging/SatuSehatBridgingTTE.java, SatuSehatKirimCompositionRME.java)
--   - Mapping Tarif Kamar KPTL Satu Sehat (src/bridging/SatuSehatMapingTarifKamarKPTL.java)
--   - Checklist Kriteria Keluar Isolasi (src/rekammedis/RMChecklistKriteriaKeluarIsolasi.java)
--
-- Idempotent: aman dijalankan ulang. Tabel dibuat hanya jika belum ada; kolom
-- hak akses ditambahkan satu per satu dan dilewati bila sudah ada (mis.
-- satu_sehat_mapping_kptl_tarif_kamar kemungkinan sudah ada di server ini).
--
-- Jalankan sekali setelah backup database.

USE `sik2023_server`;

SET @db = DATABASE();

-- =====================================================================
-- 1. Tabel checklist_kriteria_keluar_isolasi
-- =====================================================================
CREATE TABLE IF NOT EXISTS `checklist_kriteria_keluar_isolasi` (
  `no_rawat` varchar(17) NOT NULL,
  `tanggal` datetime NOT NULL,
  `gejala_membaik` enum('Ya','Tidak','Tidak Berlaku') NOT NULL,
  `tidak_ada_indikasi_transmisi` enum('Ya','Tidak','Tidak Berlaku') NOT NULL,
  `hasil_penunjang_memenuhi` enum('Ya','Tidak','Tidak Berlaku') NOT NULL,
  `kriteria_pedoman_terpenuhi` enum('Ya','Tidak','Tidak Berlaku') NOT NULL,
  `persetujuan_dpjp` enum('Ya','Tidak') NOT NULL,
  `keputusan` enum('Keluar Isolasi','Lanjut Isolasi') NOT NULL,
  `alasan` varchar(500) DEFAULT NULL,
  `nik` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`no_rawat`,`tanggal`),
  KEY `nik` (`nik`),
  CONSTRAINT `checklist_kriteria_keluar_isolasi_ibfk_1` FOREIGN KEY (`no_rawat`) REFERENCES `reg_periksa` (`no_rawat`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `checklist_kriteria_keluar_isolasi_ibfk_2` FOREIGN KEY (`nik`) REFERENCES `pegawai` (`nik`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- =====================================================================
-- 2. Tabel satu_sehat_mapping_tarifkamar_kptl
-- =====================================================================
CREATE TABLE IF NOT EXISTS `satu_sehat_mapping_tarifkamar_kptl` (
  `kd_kamar` varchar(15) NOT NULL,
  `code` varchar(30) DEFAULT NULL,
  `system` varchar(100) NOT NULL,
  `display` varchar(80) DEFAULT NULL,
  PRIMARY KEY (`kd_kamar`),
  CONSTRAINT `satu_sehat_mapping_tarifkamar_kptl_ibfk_1` FOREIGN KEY (`kd_kamar`) REFERENCES `kamar` (`kd_kamar`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- =====================================================================
-- 3. Kolom hak akses baru pada tabel `user`
-- =====================================================================

SET @kolom_ada = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=@db AND table_name='user' AND column_name='satu_sehat_mapping_kptl_tarif_kamar');
SET @sql = IF(@kolom_ada=0, 'ALTER TABLE `user` ADD COLUMN `satu_sehat_mapping_kptl_tarif_kamar` enum(''true'',''false'') DEFAULT NULL', 'SELECT ''Kolom satu_sehat_mapping_kptl_tarif_kamar sudah ada, dilewati'' AS info');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @kolom_ada = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=@db AND table_name='user' AND column_name='checklist_kriteria_keluar_isolasi');
SET @sql = IF(@kolom_ada=0, 'ALTER TABLE `user` ADD COLUMN `checklist_kriteria_keluar_isolasi` enum(''true'',''false'') DEFAULT NULL', 'SELECT ''Kolom checklist_kriteria_keluar_isolasi sudah ada, dilewati'' AS info');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @kolom_ada = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=@db AND table_name='user' AND column_name='satu_sehat_tanda_tangan_elektronik');
SET @sql = IF(@kolom_ada=0, 'ALTER TABLE `user` ADD COLUMN `satu_sehat_tanda_tangan_elektronik` enum(''true'',''false'') DEFAULT NULL', 'SELECT ''Kolom satu_sehat_tanda_tangan_elektronik sudah ada, dilewati'' AS info');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @kolom_ada = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=@db AND table_name='user' AND column_name='satu_sehat_kirim_composition');
SET @sql = IF(@kolom_ada=0, 'ALTER TABLE `user` ADD COLUMN `satu_sehat_kirim_composition` enum(''true'',''false'') DEFAULT NULL', 'SELECT ''Kolom satu_sehat_kirim_composition sudah ada, dilewati'' AS info');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- =====================================================================
-- Ringkasan
-- =====================================================================
SELECT
    (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=@db AND table_name='checklist_kriteria_keluar_isolasi') AS tabel_checklist_keluar_isolasi_ada,
    (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=@db AND table_name='satu_sehat_mapping_tarifkamar_kptl') AS tabel_mapping_tarifkamar_kptl_ada,
    (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=@db AND table_name='user' AND column_name IN (
        'satu_sehat_mapping_kptl_tarif_kamar','checklist_kriteria_keluar_isolasi',
        'satu_sehat_tanda_tangan_elektronik','satu_sehat_kirim_composition'
    )) AS jumlah_kolom_akses_user_ditemukan;
