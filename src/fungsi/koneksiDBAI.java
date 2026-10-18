package fungsi;

import AESsecurity.EnkripsiAES;
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

/**
 * Setting Asisten AI dari setting/database-extra.xml.
 * API key dienkripsi dengan KhanzaPengenkripsiTeks seperti GOWA_PASSWORD.
 */
public class koneksiDBAI {
    private koneksiDBAI(){}

    /** Dibaca ulang setiap dipanggil supaya perubahan file langsung berlaku tanpa restart aplikasi. */
    private static Properties loadSettings() throws Exception {
        Properties setting = new Properties();
        try (FileInputStream fis = new FileInputStream("setting/database.xml")) {
            setting.loadFromXML(fis);
        }
        File extra = new File("setting/database-extra.xml");
        if (extra.exists()) {
            try (FileInputStream fis = new FileInputStream(extra)) {
                setting.loadFromXML(fis);
            }
        }
        return setting;
    }

    /** API key OpenRouter (model :free gratis); nilai polos berawalan sk-or- juga diterima untuk uji coba. */
    public static String OPENROUTERAPIKEY() {
        try {
            String nilai = loadSettings().getProperty("OPENROUTERAPIKEY", "").trim();
            if (nilai.isEmpty() || nilai.startsWith("sk-or-")) {
                return nilai;
            }
            return EnkripsiAES.decrypt(nilai).trim();
        } catch (Exception e) {
            System.out.println("Notifikasi Asisten AI : gagal membaca OPENROUTERAPIKEY "+e);
            return "";
        }
    }

    /** Alamat server Ollama, mis. http://192.168.1.10:11434 (tidak dienkripsi). */
    public static String OLLAMAURL() {
        try {
            return loadSettings().getProperty("OLLAMAURL", "").trim();
        } catch (Exception e) {
            return "";
        }
    }
}
