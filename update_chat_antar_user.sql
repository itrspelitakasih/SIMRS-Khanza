-- Fitur Chat Antar User (paket komunikasi: DlgChat & ChatNotifier)
-- Tabel juga dibuat otomatis oleh aplikasi saat login bila belum ada (butuh hak CREATE).

-- Isi chat. penerima kosong ('') = Chat Semua (global), selain itu = chat personal ke kode user tsb.
CREATE TABLE IF NOT EXISTS `chat_pesan` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `waktu` datetime NOT NULL,
  `pengirim` varchar(50) NOT NULL,
  `penerima` varchar(50) NOT NULL DEFAULT '',
  `pesan` text NOT NULL,
  `dibaca` enum('0','1') NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `penerima` (`penerima`,`dibaca`),
  KEY `pengirim` (`pengirim`,`penerima`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Status online (heartbeat tiap 5 detik selama user login) & posisi terakhir Chat Semua yang sudah dibaca.
CREATE TABLE IF NOT EXISTS `chat_online` (
  `kode_user` varchar(50) NOT NULL,
  `terakhir_aktif` datetime NOT NULL,
  `id_global_dibaca` bigint(20) NOT NULL DEFAULT 0,
  PRIMARY KEY (`kode_user`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Bila chat_online sudah terlanjur dibuat tanpa kolom id_global_dibaca (aplikasi juga menambahkannya otomatis):
-- ALTER TABLE `chat_online` ADD COLUMN `id_global_dibaca` bigint(20) NOT NULL DEFAULT 0;
