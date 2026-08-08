-- Isi device_id untuk baris LAMA di wa_outbox yang kosong (NULL), khusus
-- baris yang memang sudah diproses lewat GoWA (sender='GOWA'). Baris lama
-- peninggalan bot Delphi (sender='DELPHI') sengaja TIDAK disentuh karena
-- bukan riwayat pengiriman GoWA.
--
-- Nilai device_id diambil dari entry GOWA_DEVICE_ID pada
-- setting/database.xml. device_id tidak dipakai untuk proses pengiriman
-- (GoWAService selalu membaca GOWA_DEVICE_ID langsung dari database.xml
-- saat mengirim), jadi script ini murni merapikan riwayat/audit -- aman
-- dijalankan kapan saja, dan aman dijalankan berkali-kali (baris yang
-- sudah terisi tidak akan diubah lagi).
--
-- Jalankan terhadap DATABASE WA GATEWAY (nilai entry DATABASEWA pada
-- setting/database.xml, mis. `wagw`), BUKAN sik2023_server.

SET @current_device_id = '6c003adb-9957-4fb4-aa18-4e9a2aef22de';

-- Pratinjau: jumlah baris yang akan ikut terisi.
SELECT COUNT(*) AS baris_akan_diisi
FROM `wa_outbox`
WHERE `device_id` IS NULL
  AND `sender` = 'GOWA';

UPDATE `wa_outbox`
SET `device_id` = @current_device_id
WHERE `device_id` IS NULL
  AND `sender` = 'GOWA';

-- Verifikasi hasil.
SELECT
    `sender`,
    `device_id`,
    COUNT(*) AS jumlah
FROM `wa_outbox`
GROUP BY `sender`, `device_id`
ORDER BY `sender`, `device_id`;
