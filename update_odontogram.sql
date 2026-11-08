-- Modul Odontogram standar PDGI (src/rekammedis/RMOdontogram.java, komponen src/widget/PanelOdontogram.java).
-- Tabel odontogram & odontogram_detail dibuat otomatis saat form dibuka, kolom user.odontogram dibuat otomatis
-- saat hak akses "[M]Odontogram" disimpan lewat Update User (butuh hak CREATE/ALTER). File ini untuk dijalankan manual.
-- Jalankan terhadap DATABASE UTAMA SIMRS.

-- hak akses
ALTER TABLE `user` ADD COLUMN `odontogram` enum('true','false') DEFAULT NULL;

-- satu pemeriksaan odontogram per no_rawat
CREATE TABLE IF NOT EXISTS `odontogram` (
  `no_rawat` varchar(17) NOT NULL,
  `tanggal` datetime NOT NULL,
  `kd_dokter` varchar(20) NOT NULL,
  `occlusi` varchar(20) DEFAULT NULL,
  `torus_palatinus` varchar(20) DEFAULT NULL,
  `torus_mandibularis` varchar(20) DEFAULT NULL,
  `palatum` varchar(10) DEFAULT NULL,
  `diastema` varchar(110) DEFAULT NULL,
  `gigi_anomali` varchar(110) DEFAULT NULL,
  `lain_lain` varchar(200) DEFAULT NULL,
  `d` tinyint(4) DEFAULT NULL,
  `m` tinyint(4) DEFAULT NULL,
  `f` tinyint(4) DEFAULT NULL,
  `foto` varchar(50) DEFAULT NULL,
  `rontgen` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`no_rawat`),
  KEY `kd_dokter` (`kd_dokter`),
  KEY `tanggal` (`tanggal`),
  CONSTRAINT `odontogram_ibfk_1` FOREIGN KEY (`no_rawat`) REFERENCES `reg_periksa` (`no_rawat`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `odontogram_ibfk_2` FOREIGN KEY (`kd_dokter`) REFERENCES `dokter` (`kd_dokter`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- kondisi per gigi (FDI 11-48, 51-85); permukaan M/D/B/L/O atau '' untuk kondisi seluruh gigi;
-- kode PDGI: car amf cof gif fis inl onl non une pre ano ipx prd fld mis rrx cfr nvt rct fmc poc meb pob mgka mgki rtka rtki
CREATE TABLE IF NOT EXISTS `odontogram_detail` (
  `no_rawat` varchar(17) NOT NULL,
  `gigi` char(2) NOT NULL,
  `permukaan` char(1) NOT NULL DEFAULT '',
  `kode` varchar(5) NOT NULL,
  PRIMARY KEY (`no_rawat`,`gigi`,`permukaan`,`kode`),
  CONSTRAINT `odontogram_detail_ibfk_1` FOREIGN KEY (`no_rawat`) REFERENCES `odontogram` (`no_rawat`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
