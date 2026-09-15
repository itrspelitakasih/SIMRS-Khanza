-- Migrasi DB untuk fitur baru dari vendor (SIMRS-Khanza upstream, commit 67ddae1c66..0345d7b27a):
--   - Ringkasan Hutang Vendor Aset/Inventaris
--   - Ringkasan Beban Hutang Lain
--   - Set Resep Per Cara Bayar
-- Dibuat: 2026-09-12. BACKUP DATABASE dulu sebelum menjalankan ini di server produksi.

-- 1. Kolom hak akses baru di tabel `user` (dipakai oleh fungsi/akses.java, setting/DlgUser.java, setting/DlgUpdateUser.java)
ALTER TABLE `user`
  ADD COLUMN `ringkasan_hutang_vendor_inventaris` enum('true','false') DEFAULT NULL AFTER `satu_sehat_kirim_composition`,
  ADD COLUMN `ringkasan_beban_hutang_lain` enum('true','false') DEFAULT NULL AFTER `ringkasan_hutang_vendor_inventaris`,
  ADD COLUMN `set_resep_per_cara_bayar` enum('true','false') DEFAULT NULL AFTER `ringkasan_beban_hutang_lain`;

-- 2. Tabel master & transaksi untuk "Ringkasan Beban Hutang Lain" (belum ada di Dev sebelumnya)
--    Cek dulu apakah kedua tabel ini sudah ada di DB Anda (mis. dari update vendor Agustus 2026) sebelum menjalankan CREATE TABLE.

CREATE TABLE IF NOT EXISTS `pemberi_hutang_lain` (
  `kode_pemberi_hutang` varchar(5) NOT NULL,
  `nama_pemberi_hutang` varchar(50) DEFAULT NULL,
  `alamat` varchar(150) DEFAULT NULL,
  `no_telp` varchar(13) DEFAULT NULL,
  `kd_rek` varchar(15) DEFAULT NULL,
  `status` enum('0','1') NOT NULL,
  PRIMARY KEY (`kode_pemberi_hutang`),
  KEY `kd_rek` (`kd_rek`),
  CONSTRAINT `pemberi_hutang_lain_ibfk_1` FOREIGN KEY (`kd_rek`) REFERENCES `rekening` (`kd_rek`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

CREATE TABLE IF NOT EXISTS `beban_hutang_lain` (
  `no_hutang` varchar(20) NOT NULL,
  `tgl_hutang` date DEFAULT NULL,
  `nip` varchar(20) DEFAULT NULL,
  `kode_pemberi_hutang` varchar(5) DEFAULT NULL,
  `kd_rek` varchar(15) NOT NULL,
  `keterangan` varchar(100) DEFAULT NULL,
  `tgltempo` date NOT NULL,
  `nominal` double NOT NULL,
  `sisahutang` double NOT NULL,
  `status` enum('Sudah Lunas','Belum Lunas') NOT NULL,
  PRIMARY KEY (`no_hutang`),
  KEY `nip` (`nip`),
  KEY `kode_pemberi_hutang` (`kode_pemberi_hutang`),
  KEY `kd_rek` (`kd_rek`),
  CONSTRAINT `beban_hutang_lain_ibfk_1` FOREIGN KEY (`nip`) REFERENCES `petugas` (`nip`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `beban_hutang_lain_ibfk_2` FOREIGN KEY (`kode_pemberi_hutang`) REFERENCES `pemberi_hutang_lain` (`kode_pemberi_hutang`) ON UPDATE CASCADE,
  CONSTRAINT `beban_hutang_lain_ibfk_3` FOREIGN KEY (`kd_rek`) REFERENCES `rekening` (`kd_rek`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- 3. Tabel mapping obat x cara bayar untuk "Set Resep Per Cara Bayar"
--    (baru ketahuan dibutuhkan setelah update vendor 2026-09-14 menambahkan fungsi/resepdokter.java,
--    yang dipakai oleh inventory/DlgPeresepanDokter.java untuk membatasi obat per cara bayar saat resep dokter)
CREATE TABLE IF NOT EXISTS `set_resep_per_cara_bayar` (
  `kd_pj` char(3) NOT NULL,
  `kode_brng` varchar(15) NOT NULL,
  KEY `kode_brng` (`kode_brng`),
  KEY `kd_pj` (`kd_pj`),
  CONSTRAINT `set_resep_per_cara_bayar_ibfk_1` FOREIGN KEY (`kode_brng`) REFERENCES `databarang` (`kode_brng`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `set_resep_per_cara_bayar_ibfk_2` FOREIGN KEY (`kd_pj`) REFERENCES `penjab` (`kd_pj`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci ROW_FORMAT=DYNAMIC;

-- Catatan:
-- - "Ringkasan Hutang Vendor Aset/Inventaris" (keuangan/KeuanganRingkasanHutangVendorAsetInventaris.java)
--   hanya membaca tabel yang sudah ada (bayar_pemesanan_inventaris, inventaris_pemesanan, setting), tidak perlu tabel baru.
-- - Kalau tabel `set_resep_per_cara_bayar` tidak dibuat, DlgSetResepPerCaraBayar & resep dokter tetap jalan
--   (dianggap tidak ada obat yang dibatasi per cara bayar), tapi menu Set Resep Per Cara Bayar akan gagal saat
--   simpan/hapus data karena tabelnya belum ada.
-- - Setelah migrasi, aktifkan hak akses 3 kolom baru di atas untuk user yang perlu lewat menu Setting > User.
