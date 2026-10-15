-- Jadwal perawatan berikutnya + pengingat WhatsApp H-1 di form Pemeliharaan Inventaris
-- (src/inventaris/InventarisPemeliharaan.java).
-- Dibuat otomatis oleh aplikasi saat form dibuka bila belum ada (butuh hak ALTER).
-- Jalankan terhadap DATABASE UTAMA SIMRS (bukan database WA gateway).

-- tgl_berikutnya : tanggal jadwal perawatan berikutnya (NULL = tidak ada jadwal)
-- no_wa          : nomor WA penerima pengingat (default no_telp petugas penanggung jawab)
-- wa_nomor       : wa_outbox.nomor pengingat yang diantrikan, untuk dibatalkan saat data diganti/dihapus
ALTER TABLE `pemeliharaan_inventaris` ADD COLUMN IF NOT EXISTS `tgl_berikutnya` date DEFAULT NULL;
ALTER TABLE `pemeliharaan_inventaris` ADD COLUMN IF NOT EXISTS `no_wa` varchar(20) DEFAULT NULL;
ALTER TABLE `pemeliharaan_inventaris` ADD COLUMN IF NOT EXISTS `wa_nomor` bigint(20) DEFAULT NULL;

-- Pengiriman memakai antrian wa_outbox (database WA, lihat update_gowa_wa_outbox_terjadwal.sql)
-- dengan SOURCE='PRWINV', dikirim worker GoWA bila NOTIFWAKONTROL=yes di setting/database.xml.
