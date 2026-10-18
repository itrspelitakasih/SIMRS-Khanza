-- Fitur Asisten AI : menu "Asisten AI" di menubar frmUtama (paket komunikasi: FrmAsistenAI, DlgSetAsistenAI, AsistenAI, ChatAI).
-- Provider: OpenRouter (model gratis :free, default) atau Ollama (server sendiri), dipilih di tombol Setting (Admin Utama).
-- Tabel juga dibuat/diubah otomatis oleh aplikasi saat popup pertama kali dibuka (butuh hak CREATE/ALTER).

-- API key & alamat server TIDAK disimpan di database, tetapi di setting/database-extra.xml:
--   <entry key="OPENROUTERAPIKEY">hasil-enkripsi-sk-or-...</entry>   (dari openrouter.ai/keys, enkripsi dengan KhanzaPengenkripsiTeks)
--   <entry key="OLLAMAURL">http://192.168.1.10:11434</entry>          (alamat server Ollama, tidak dienkripsi)

-- Setting tunggal (id=1).
CREATE TABLE IF NOT EXISTS `set_asisten_ai` (
  `id` tinyint(4) NOT NULL DEFAULT 1,
  `provider` varchar(20) NOT NULL DEFAULT 'openrouter',
  `model` varchar(100) NOT NULL DEFAULT 'google/gemma-4-31b-it:free',
  `system_prompt` text,
  `aktif` enum('Yes','No') NOT NULL DEFAULT 'Yes',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Catatan pemakaian token per user.
CREATE TABLE IF NOT EXISTS `asisten_ai_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `waktu` datetime NOT NULL,
  `kode_user` varchar(50) NOT NULL,
  `model` varchar(100) NOT NULL,
  `input_tokens` int(11) NOT NULL DEFAULT 0,
  `output_tokens` int(11) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `waktu` (`waktu`),
  KEY `kode_user` (`kode_user`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Bila tabel versi awal (Claude) sudah terlanjur dibuat (kolom effort/max_tokens lama boleh dibiarkan atau dihapus):
-- ALTER TABLE `set_asisten_ai` ADD COLUMN `provider` varchar(20) NOT NULL DEFAULT 'openrouter' AFTER `id`,
--   MODIFY `model` varchar(100) NOT NULL DEFAULT 'google/gemma-4-31b-it:free';
-- ALTER TABLE `asisten_ai_log` MODIFY `model` varchar(100) NOT NULL;
-- UPDATE `set_asisten_ai` SET `provider`='openrouter', `model`='google/gemma-4-31b-it:free' WHERE `model` LIKE 'claude-%' OR `model`='openrouter/free';
-- ALTER TABLE `set_asisten_ai` DROP COLUMN `effort`, DROP COLUMN `max_tokens`;

-- Contoh rekap pemakaian per user per bulan:
-- SELECT kode_user, model, SUM(input_tokens) input, SUM(output_tokens) output
-- FROM asisten_ai_log WHERE waktu >= DATE_FORMAT(CURDATE(),'%Y-%m-01') GROUP BY kode_user, model;
