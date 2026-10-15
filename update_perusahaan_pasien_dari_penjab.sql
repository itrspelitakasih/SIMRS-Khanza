-- =====================================================================
-- Isi pasien.perusahaan_pasien berdasarkan penjab (reg_periksa.kd_pj)
-- registrasi terakhir yang ternyata adalah perusahaan.
--
-- Latar belakang: saat registrasi, perusahaan pasien diisi di Jenis Bayar
-- (reg_periksa.kd_pj), padahal seharusnya di pasien.perusahaan_pasien.
-- Kode penjab (penjab.kd_pj) BERBEDA dengan kode perusahaan
-- (perusahaan_pasien.kode_perusahaan), jadi perlu tabel pemetaan.
--
-- Jalankan per langkah (bukan sekaligus) dan periksa hasil tiap langkah.
-- =====================================================================


-- ---------------------------------------------------------------------
-- LANGKAH 1. Backup nilai perusahaan_pasien saat ini
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS backup_pasien_perusahaan_20261015;
CREATE TABLE backup_pasien_perusahaan_20261015 AS
SELECT no_rkm_medis, perusahaan_pasien FROM pasien;

-- Cara mengembalikan (rollback) bila diperlukan:
-- UPDATE pasien p INNER JOIN backup_pasien_perusahaan_20261015 b ON b.no_rkm_medis=p.no_rkm_medis
--    SET p.perusahaan_pasien=b.perusahaan_pasien;


-- ---------------------------------------------------------------------
-- LANGKAH 1b. Kosongkan (reset) perusahaan semua pasien menjadi '-'
--   Jalankan SETELAH langkah 1 (backup). Langkah selanjutnya akan mengisi
--   ulang perusahaan_pasien dari penjab registrasi terakhir.
-- ---------------------------------------------------------------------
INSERT IGNORE INTO perusahaan_pasien (kode_perusahaan, nama_perusahaan, alamat, kota, no_telp)
VALUES ('-','-','-','-','0');

-- Cek jumlah pasien per perusahaan sebelum direset
SELECT p.perusahaan_pasien, pp.nama_perusahaan, COUNT(*) AS jml_pasien
FROM pasien p
LEFT JOIN perusahaan_pasien pp ON pp.kode_perusahaan=p.perusahaan_pasien
GROUP BY p.perusahaan_pasien, pp.nama_perusahaan
ORDER BY jml_pasien DESC;

UPDATE pasien SET perusahaan_pasien='-' WHERE perusahaan_pasien<>'-';


-- ---------------------------------------------------------------------
-- LANGKAH 2. Tabel pemetaan penjab -> perusahaan_pasien
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS tmp_map_penjab_perusahaan;
CREATE TABLE tmp_map_penjab_perusahaan (
  kd_pj char(3) NOT NULL,
  kode_perusahaan varchar(8) NOT NULL,
  PRIMARY KEY (kd_pj)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- 2a. Isi otomatis bila nama penjab sama dengan nama perusahaan
--     (dibandingkan dengan png_jawab maupun penjab.nama_perusahaan).
INSERT IGNORE INTO tmp_map_penjab_perusahaan (kd_pj, kode_perusahaan)
SELECT pj.kd_pj, MIN(pp.kode_perusahaan)
FROM penjab pj
INNER JOIN perusahaan_pasien pp
   ON UPPER(TRIM(pp.nama_perusahaan)) IN (UPPER(TRIM(pj.png_jawab)), UPPER(TRIM(pj.nama_perusahaan)))
WHERE pp.kode_perusahaan<>'-'
GROUP BY pj.kd_pj;

-- 2b. Periksa hasil pemetaan otomatis. HAPUS baris yang salah, misalnya:
--     DELETE FROM tmp_map_penjab_perusahaan WHERE kd_pj='A09';
SELECT m.kd_pj, pj.png_jawab, m.kode_perusahaan, pp.nama_perusahaan
FROM tmp_map_penjab_perusahaan m
INNER JOIN penjab pj ON pj.kd_pj=m.kd_pj
INNER JOIN perusahaan_pasien pp ON pp.kode_perusahaan=m.kode_perusahaan
ORDER BY pj.png_jawab;

-- 2c. Penjab yang dipakai di registrasi tetapi BELUM terpetakan.
--     Tentukan mana yang sebenarnya perusahaan (abaikan UMUM, BPJS, asuransi, dll).
SELECT pj.kd_pj, pj.png_jawab, pj.nama_perusahaan, COUNT(*) AS jml_registrasi
FROM reg_periksa r
INNER JOIN penjab pj ON pj.kd_pj=r.kd_pj
LEFT JOIN tmp_map_penjab_perusahaan m ON m.kd_pj=r.kd_pj
WHERE m.kd_pj IS NULL
GROUP BY pj.kd_pj, pj.png_jawab, pj.nama_perusahaan
ORDER BY jml_registrasi DESC;

-- 2d. Tambahkan pemetaan manual untuk penjab perusahaan yang belum terpetakan.
--     Daftar kode perusahaan: SELECT * FROM perusahaan_pasien ORDER BY nama_perusahaan;
-- INSERT INTO tmp_map_penjab_perusahaan (kd_pj, kode_perusahaan) VALUES
--   ('A10','I0002'),
--   ('A11','I0005');

-- 2e. (Opsional) Bila perusahaannya belum ada di perusahaan_pasien, buat baru
--     dari data penjab dengan kode_perusahaan = kd_pj, lalu petakan.
--     Isi daftar kd_pj perusahaan di dalam IN (...). Pastikan kodenya belum
--     dipakai di perusahaan_pasien.
-- INSERT IGNORE INTO perusahaan_pasien (kode_perusahaan, nama_perusahaan, alamat, kota, no_telp)
-- SELECT kd_pj, LEFT(IF(TRIM(nama_perusahaan) IN ('','-'), png_jawab, nama_perusahaan),70),
--        LEFT(alamat_asuransi,100), '-', LEFT(no_telp,27)
-- FROM penjab WHERE kd_pj IN ('A10','A11');
-- INSERT IGNORE INTO tmp_map_penjab_perusahaan (kd_pj, kode_perusahaan)
-- SELECT kd_pj, kd_pj FROM penjab WHERE kd_pj IN ('A10','A11');


-- ---------------------------------------------------------------------
-- LANGKAH 3. Cari registrasi terakhir tiap pasien yang penjabnya perusahaan
--   "Terakhir" = registrasi terbaru di antara penjab yang terpetakan, jadi
--   kunjungan UMUM/BPJS sesudahnya tidak menghapus perusahaan pasien.
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS tmp_perusahaan_terakhir;
CREATE TABLE tmp_perusahaan_terakhir (
  no_rkm_medis varchar(15) NOT NULL,
  kode_perusahaan varchar(8) NOT NULL,
  kd_pj char(3) NOT NULL,
  waktu_reg datetime NOT NULL,
  PRIMARY KEY (no_rkm_medis)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

INSERT IGNORE INTO tmp_perusahaan_terakhir (no_rkm_medis, kode_perusahaan, kd_pj, waktu_reg)
SELECT r.no_rkm_medis, m.kode_perusahaan, r.kd_pj, x.waktu
FROM reg_periksa r
INNER JOIN tmp_map_penjab_perusahaan m ON m.kd_pj=r.kd_pj
INNER JOIN (
    SELECT r2.no_rkm_medis, MAX(CONCAT(r2.tgl_registrasi,' ',r2.jam_reg)) AS waktu
    FROM reg_periksa r2
    INNER JOIN tmp_map_penjab_perusahaan m2 ON m2.kd_pj=r2.kd_pj
    GROUP BY r2.no_rkm_medis
) x ON x.no_rkm_medis=r.no_rkm_medis AND CONCAT(r.tgl_registrasi,' ',r.jam_reg)=x.waktu;


-- ---------------------------------------------------------------------
-- LANGKAH 4. Pratinjau pasien yang akan berubah
-- ---------------------------------------------------------------------
SELECT p.no_rkm_medis, p.nm_pasien,
       p.perusahaan_pasien AS perusahaan_lama, pl.nama_perusahaan AS nama_lama,
       t.kode_perusahaan AS perusahaan_baru, pb.nama_perusahaan AS nama_baru,
       t.kd_pj, t.waktu_reg
FROM pasien p
INNER JOIN tmp_perusahaan_terakhir t ON t.no_rkm_medis=p.no_rkm_medis
LEFT JOIN perusahaan_pasien pl ON pl.kode_perusahaan=p.perusahaan_pasien
INNER JOIN perusahaan_pasien pb ON pb.kode_perusahaan=t.kode_perusahaan
WHERE p.perusahaan_pasien<>t.kode_perusahaan
ORDER BY pb.nama_perusahaan, p.nm_pasien;


-- ---------------------------------------------------------------------
-- LANGKAH 5. Update pasien.perusahaan_pasien
--   Tambahkan  AND p.perusahaan_pasien IN ('','-')  pada WHERE bila hanya
--   ingin mengisi pasien yang perusahaannya masih kosong.
-- ---------------------------------------------------------------------
UPDATE pasien p
INNER JOIN tmp_perusahaan_terakhir t ON t.no_rkm_medis=p.no_rkm_medis
SET p.perusahaan_pasien=t.kode_perusahaan
WHERE p.perusahaan_pasien<>t.kode_perusahaan;


-- ---------------------------------------------------------------------
-- LANGKAH 6. Bersihkan tabel bantu (backup disimpan dulu sampai yakin)
-- ---------------------------------------------------------------------
-- DROP TABLE IF EXISTS tmp_perusahaan_terakhir;
-- DROP TABLE IF EXISTS tmp_map_penjab_perusahaan;
-- DROP TABLE IF EXISTS backup_pasien_perusahaan_20261015;
