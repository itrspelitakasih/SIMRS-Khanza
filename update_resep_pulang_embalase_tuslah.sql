-- Kolom embalase & tuslah pada tabel resep_pulang.
-- Dipakai oleh src/simrskhanza/DlgInputResepPulang.java, yang sekarang punya
-- kolom "Emb" dan "Tslh" pada tabel input resep pulang (sama seperti pada
-- src/inventory/DlgCariObat2.java, tbObat), diisi otomatis dari nilai
-- default fungsi/embalasetuslah.java (tabel set_embalase) tapi tetap bisa
-- diedit manual per baris.
--
-- Nilai total (embalase+tuslah+(harga*jml_barang)) dihitung & disimpan saat
-- BtnSimpanActionPerformed menyimpan ke resep_pulang, mengikuti pola yang
-- sudah ada pada detail_pemberian_obat.total (lihat CREATE TABLE
-- detail_pemberian_obat di sik.sql). Setelah kolom ini ada dan total ikut
-- menjumlahkan embalase+tuslah, tagihan rawat inap (src/keuangan/DlgBilingRanap.java,
-- prosesResepPulang -> sqlpsreseppulang, dan src/keuangan/DlgPerkiraanBiayaRanap.java
-- yang men-sum(resep_pulang.total)) otomatis ikut menghitung tambahan ini
-- tanpa perlu perubahan kode lain, karena keduanya membaca resep_pulang.total
-- langsung, bukan menghitung ulang dari harga*jml_barang.
--
-- Skrip ini idempotent (aman dijalankan berkali-kali) dan hanya menambah
-- kolom baru, tidak pernah menghapus/mengubah data lama. Jalankan terhadap
-- database utama (sik2023_server, atau nama database SIMRS Anda) sekali
-- setelah backup database.

SET @db = DATABASE();

SET @has_embalase = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = @db
      AND table_name = 'resep_pulang'
      AND column_name = 'embalase'
);
SET @sql = IF(
    @has_embalase = 0,
    'ALTER TABLE `resep_pulang` ADD COLUMN `embalase` double DEFAULT NULL AFTER `no_faktur`',
    'SELECT ''Kolom embalase sudah ada'' AS info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_tuslah = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = @db
      AND table_name = 'resep_pulang'
      AND column_name = 'tuslah'
);
SET @sql = IF(
    @has_tuslah = 0,
    'ALTER TABLE `resep_pulang` ADD COLUMN `tuslah` double DEFAULT NULL AFTER `embalase`',
    'SELECT ''Kolom tuslah sudah ada'' AS info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT
    COUNT(*) AS jumlah_kolom,
    SUM(column_name = 'embalase') AS ada_embalase,
    SUM(column_name = 'tuslah') AS ada_tuslah
FROM information_schema.columns
WHERE table_schema = @db
  AND table_name = 'resep_pulang';
