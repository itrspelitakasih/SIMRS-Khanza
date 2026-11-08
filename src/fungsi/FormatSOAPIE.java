package fungsi;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Merapikan catatan SOAPIE untuk laporan (dipakai dari report resume pasien).
 *
 * Baris "S : isian" menjadi "S\tisian" dan baris lanjutannya diberi tab di depan, sehingga
 * dengan tab stop di laporan huruf S/O/A/P/I/E berada di kolom kiri dan isian sejajar di kolom
 * kanan. JasperReports tidak mendukung hanging indent (firstLineIndent negatif diabaikan), jadi
 * isian dipotong sendiri per lebar kolom dan setiap potongan diawali tab.
 * Blok SOAPIE berakhir di baris kosong atau baris tanggal (awal catatan berikutnya).
 */
public final class FormatSOAPIE {
    private static final Pattern SOAPIE = Pattern.compile("^\\s*([SOAPIE])\\s*:\\s?(.*)$");
    private static final FontRenderContext FRC = new FontRenderContext(null, true, true);

    private FormatSOAPIE() {}

    /**
     * @param teks      isi catatan
     * @param lebarIsi  lebar kolom isian dalam point (lebar teks dikurangi posisi tab stop)
     * @param namaFont  font laporan, mis. "Tahoma"
     * @param ukuran    ukuran font laporan
     */
    public static String rapikan(String teks, float lebarIsi, String namaFont, int ukuran) {
        if (teks == null || teks.isEmpty()) {
            return "";
        }
        // layar memakai font laporan, PDF bisa jatuh ke Helvetica (metrik ~Arial): ukur dengan yang terlebar
        Font[] font = {new Font(namaFont, Font.PLAIN, ukuran), new Font("Arial", Font.PLAIN, ukuran)};
        // cadangan supaya beda metrik font PDF/layar tidak membuat Jasper memotong ulang baris
        float batas = lebarIsi * 0.93f;
        StringBuilder hasil = new StringBuilder();
        boolean dalamBlok = false;
        String[] baris = teks.replace("\r", "").split("\n", -1);
        for (int i = 0; i < baris.length; i++) {
            String b = baris[i];
            Matcher m = SOAPIE.matcher(b);
            if (m.matches()) {
                hasil.append(m.group(1)).append('\t').append(potong(m.group(2).trim(), font, batas));
                dalamBlok = true;
            } else if (b.trim().isEmpty() || b.matches("^\\s*\\d{4}-\\d{2}-\\d{2}.*")) {
                hasil.append(b);
                dalamBlok = false;
            } else if (dalamBlok) {
                hasil.append('\t').append(potong(b.trim(), font, batas));
            } else {
                hasil.append(b);
            }
            if (i < baris.length - 1) {
                hasil.append('\n');
            }
        }
        return hasil.toString();
    }

    public static String rapikan(String teks) {
        return rapikan(teks, 230f, "Tahoma", 9);
    }

    /** Potong teks per kata agar muat di lebar kolom; sambungan diawali "\n\t". */
    private static String potong(String teks, Font[] font, float batas) {
        StringBuilder out = new StringBuilder();
        StringBuilder baris = new StringBuilder();
        for (String kata : teks.split(" +")) {
            String coba = baris.length() == 0 ? kata : baris + " " + kata;
            if (baris.length() > 0 && lebar(coba, font) > batas) {
                out.append(baris).append("\n\t");
                baris.setLength(0);
                coba = kata;
            }
            // kata lebih panjang dari kolom: potong paksa per huruf
            while (lebar(coba, font) > batas && coba.length() > 1) {
                int n = coba.length() - 1;
                while (n > 1 && lebar(coba.substring(0, n), font) > batas) {
                    n--;
                }
                out.append(coba, 0, n).append("\n\t");
                coba = coba.substring(n);
            }
            baris.setLength(0);
            baris.append(coba);
        }
        return out.append(baris).toString();
    }

    private static float lebar(String s, Font[] font) {
        float w = 0;
        for (Font f : font) {
            w = Math.max(w, (float) f.getStringBounds(s, FRC).getWidth());
        }
        return w;
    }
}
