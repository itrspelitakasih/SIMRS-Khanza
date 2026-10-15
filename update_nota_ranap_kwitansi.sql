-- Nomor nota/kwitansi Billing Ranap format baru (DlgBilingRanap.noNotaRanap):
--   Penjamin (selain Umum/Cash) : 001/RSPK/INV/X/2026
--   Umum/Cash                   : 001/RSPK/X/2026
-- Urutan bersama per bulan, romawi = bulan nota. Panjang maksimal 22 karakter
-- (999/RSPK/INV/VIII/2026), sedangkan kolom lama varchar(17). Jalankan sebelum memakai versi aplikasi baru.

ALTER TABLE `nota_inap` MODIFY `no_nota` varchar(30) DEFAULT NULL;
