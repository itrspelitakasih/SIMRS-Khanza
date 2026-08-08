-- Skema tabel wa_outbox untuk antrian pengiriman WhatsApp via GoWA
-- (menggantikan service Delphi/ServiceWADelphi yang sebelumnya menulis ke
-- tabel yang sama, database `wagw`). Dipakai oleh src/wa/GoWAService.java:
--   - kirim langsung   (kirimText(nomor, pesan) / kirimTextNow)
--   - kirim terjadwal  (kirimText(nomor, pesan, tanggalJam, source), mis.
--     reminder H-1 Surat Kontrol di src/surat/SuratKontrol.java)
-- Worker GoWAService (jalan tiap 15 detik, lihat processDueMessages())
-- membaca baris status='ANTRIAN' AND tanggal_jam<=NOW() lalu mengirimkannya.
--
-- Skrip ini idempotent dan HANYA menambah (tidak pernah menghapus/mengubah
-- data atau kolom lama), jadi data hasil export lama (mis. dump `wagw`
-- yang berisi riwayat status 'Terproses'/'BATAL' peninggalan Delphi) tetap
-- utuh dan tidak diproses ulang -- worker GoWA hanya mengambil baris
-- status='ANTRIAN'.
--
-- Jalankan skrip ini terhadap DATABASE WA GATEWAY (nilai entry DATABASEWA
-- pada setting/database.xml, contoh: `wagw`), BUKAN database utama
-- sik2023_server. Jalankan sekali setelah backup database.

SET @db = DATABASE();

-- Tabel dasar: dibuat hanya jika instalasi ini belum pernah punya wa_outbox
-- sama sekali (mis. instalasi baru yang langsung pakai GoWA tanpa bridge
-- Delphi lama). Definisi berikut disamakan dengan skema production yang
-- sudah berjalan (lihat struktur tabel `wagw`.`wa_outbox`).
CREATE TABLE IF NOT EXISTS `wa_outbox` (
    `nomor` bigint(20) NOT NULL AUTO_INCREMENT,
    `nowa` varchar(50) NOT NULL DEFAULT '',
    `pesan` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
    `tanggal_jam` datetime DEFAULT NULL,
    `status` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT 'ANTRIAN',
    `source` varchar(50) DEFAULT NULL,
    `sender` varchar(10) NOT NULL DEFAULT 'DELPHI' COMMENT 'ANY, DELPHI, GOWA',
    `success` varchar(1) DEFAULT NULL,
    `response` longtext DEFAULT NULL,
    `sender_name` varchar(50) DEFAULT NULL,
    `request` text DEFAULT NULL,
    `type` varchar(10) NOT NULL DEFAULT 'TEXT' COMMENT 'TEXT, IMAGE, VIDEO',
    `file` varchar(100) DEFAULT NULL,
    `sent_datetime` datetime DEFAULT NULL,
    PRIMARY KEY (`nomor`),
    KEY `nowa` (`nowa`),
    KEY `status` (`status`),
    KEY `sender` (`sender`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- Kolom device_id: rekam GOWA_DEVICE_ID (setting/database.xml) yang dipakai
-- saat pesan diantrikan (src/wa/GoWAService.java, enqueueText()), supaya
-- bisa ditelusuri device mana yang bertanggung jawab jika suatu saat ada
-- lebih dari satu device GoWA. Kolom ini baru, belum ada di tabel lama.
SET @has_device_id = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = @db
      AND table_name = 'wa_outbox'
      AND column_name = 'device_id'
);
SET @sql = IF(
    @has_device_id = 0,
    'ALTER TABLE `wa_outbox` ADD COLUMN `device_id` varchar(50) DEFAULT NULL AFTER `type`',
    'SELECT ''Kolom device_id sudah ada'' AS info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Index tambahan untuk query polling worker GoWA (processDueMessages), yang
-- jalan tiap 15 detik dengan WHERE status=... AND type=... AND source=...
-- AND tanggal_jam<=NOW() ORDER BY tanggal_jam. Index tunggal `status` yang
-- sudah ada belum cukup efisien untuk kombinasi filter ini; index komposit
-- berikut melengkapi (bukan menggantikan) index lama.
SET @has_idx_queue = (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = @db
      AND table_name = 'wa_outbox'
      AND index_name = 'idx_wa_outbox_queue'
);
SET @sql = IF(
    @has_idx_queue = 0,
    'ALTER TABLE `wa_outbox` ADD INDEX `idx_wa_outbox_queue` (`status`,`type`,`source`,`tanggal_jam`)',
    'SELECT ''Index idx_wa_outbox_queue sudah ada'' AS info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT
    COUNT(*) AS jumlah_kolom,
    SUM(column_name = 'device_id') AS ada_device_id,
    SUM(column_name = 'sent_datetime') AS ada_sent_datetime
FROM information_schema.columns
WHERE table_schema = @db
  AND table_name = 'wa_outbox';

-- =====================================================================
-- OPSIONAL: backfill device_id untuk baris LAMA yang kosong.
--
-- device_id TIDAK dipakai saat mengirim (GoWAService selalu membaca
-- GOWA_DEVICE_ID langsung dari database.xml saat itu juga), jadi baris
-- lama yang kosong TIDAK mempengaruhi pengiriman sama sekali -- blok ini
-- murni untuk kerapian riwayat/audit, boleh dilewati.
--
-- Hanya mengisi baris sender='GOWA' (yang memang lewat GoWA), baris lama
-- sender='DELPHI' sengaja TIDAK disentuh karena bukan riwayat GoWA.
--
-- Cara pakai: ganti nilai di bawah dengan isi GOWA_DEVICE_ID pada
-- setting/database.xml Anda, baru jalankan blok ini. Kalau dibiarkan
-- placeholder, blok ini tidak melakukan apa-apa (aman dijalankan apa adanya).
SET @current_device_id = 'ISI_DENGAN_GOWA_DEVICE_ID';

SET @sql = IF(
    @current_device_id <> 'ISI_DENGAN_GOWA_DEVICE_ID',
    CONCAT(
        'UPDATE `wa_outbox` SET `device_id`=''', @current_device_id, ''' ',
        'WHERE `device_id` IS NULL AND `sender`=''GOWA'''
    ),
    'SELECT ''Lewati backfill: @current_device_id belum diisi'' AS info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
