package fungsi;

import AESsecurity.EnkripsiAES;
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

/**
 * Setting TTE MekariSign dari setting/database.xml, ditimpa setting/database-extra.xml bila ada.
 *
 * Kunci yang dibaca:
 *   URLAPIMEKARISIGN      base URL API, mis. https://sandbox-api.mekari.com/v2/esign-hmac (HMAC)
 *                         atau https://sandbox-api.mekari.com/v2/esign (OAuth2). Ejaan URLAPIMEKARIISIGN juga diterima.
 *   CLIENTIDMEKARISIGN    client id HMAC dari Mekari Developer Center
 *   CLIENTSECRETMEKARISIGN client secret HMAC
 *   APIKEYMEKARISIGN      access token OAuth2 (Bearer), dipakai bila client id/secret HMAC kosong
 *   URLDOKUMENMEKARISIGN  callback_url webhook (opsional, harus HTTPS milik RS sendiri)
 *   JENISMEKARISIGN       psre (default, TTE tersertifikasi) atau global
 *   TIPETTDMEKARISIGN     qr_code (default), font, draw, image, atau bebas (tidak dibatasi)
 *
 * Nilai rahasia boleh dienkripsi dengan KhanzaPengenkripsiTeks (EnkripsiAES) atau ditulis polos.
 */
public class koneksiDBMekariSign {
    private koneksiDBMekariSign(){}

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

    private static String polos(String kunci) {
        try {
            return loadSettings().getProperty(kunci, "").trim();
        } catch (Exception e) {
            System.out.println("Notifikasi MekariSign : gagal membaca "+kunci+" "+e);
            return "";
        }
    }

    /** Nilai terenkripsi EnkripsiAES; bila gagal didekripsi dianggap nilai polos. */
    private static String rahasia(String kunci) {
        String nilai = polos(kunci);
        if (nilai.isEmpty()) {
            return "";
        }
        String hasil = EnkripsiAES.decrypt(nilai);
        return (hasil == null || hasil.trim().isEmpty()) ? nilai : hasil.trim();
    }

    public static String URLAPIMEKARISIGN() {
        String url = polos("URLAPIMEKARISIGN");
        if (url.isEmpty()) {
            url = polos("URLAPIMEKARIISIGN");
        }
        while (url.endsWith("/")) {
            url = url.substring(0, url.length()-1);
        }
        return url;
    }

    public static String CLIENTIDMEKARISIGN() {
        return rahasia("CLIENTIDMEKARISIGN");
    }

    public static String CLIENTSECRETMEKARISIGN() {
        return rahasia("CLIENTSECRETMEKARISIGN");
    }

    public static String APIKEYMEKARISIGN() {
        return rahasia("APIKEYMEKARISIGN");
    }

    public static String URLDOKUMENMEKARISIGN() {
        return polos("URLDOKUMENMEKARISIGN");
    }

    /**
     * Tipe tanda tangan yang diizinkan: qr_code (default, seperti Sertisign), font, draw, image,
     * atau "bebas" = tidak dibatasi (perlu untuk auto sign bila Mekari menolak qr_code dengan tanda tangan tersimpan).
     */
    public static String TIPETTDMEKARISIGN() {
        String tipe = polos("TIPETTDMEKARISIGN").toLowerCase();
        return tipe.isEmpty() ? "qr_code" : tipe;
    }

    /** psre atau global; selain itu dianggap psre. */
    public static String JENISMEKARISIGN() {
        return polos("JENISMEKARISIGN").equalsIgnoreCase("global") ? "global" : "psre";
    }
}
