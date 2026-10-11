package komunikasi;

import fungsi.koneksiDBWa;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import java.net.HttpURLConnection;
import java.net.URL;

import java.nio.charset.StandardCharsets;

/**
 * Memanggil endpoint POST /api/antrian/panggil di PWA e-pasien saat kasir
 * memilih "Masuk Poli" (lihat DlgKasirRalan.ppMasukPoliBtnPrintActionPerformed),
 * supaya notifikasi WA "Anda dipanggil" pada aplikasi pasien terpicu tanpa
 * petugas harus membuka panel admin terpisah.
 *
 * Best-effort: dijalankan di thread terpisah, gagal/timeout hanya dicatat
 * ke console (tidak mengganggu alur kasir yang sedang berjalan).
 */
public class AntrianApiService {

    private static final int CONNECT_TIMEOUT = 5000;
    private static final int READ_TIMEOUT = 10000;

    public static void panggilAsync(String noRawat) {
        if (noRawat == null || noRawat.trim().isEmpty()) {
            return;
        }

        String noRawatFinal = noRawat.trim();

        Thread thread = new Thread(() -> panggil(noRawatFinal), "Antrian-Call-" + noRawatFinal);
        thread.setDaemon(true);
        thread.start();
    }

    private static boolean panggil(String noRawat) {
        String baseUrl = koneksiDBWa.ANTRIAN_API_BASE_URL();
        String token = koneksiDBWa.ANTRIAN_API_TOKEN();

        if (baseUrl.isEmpty() || token.isEmpty()) {
            System.out.println("ANTRIAN API: base URL/token belum diisi di setting, panggilan dilewati.");
            return false;
        }

        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }

        HttpURLConnection conn = null;

        try {
            URL url = new URL(baseUrl + "/api/antrian/panggil");
            String payload = "{\"no_rawat\":\"" + escapeJson(noRawat) + "\"}";

            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(CONNECT_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setRequestProperty("Authorization", "Bearer " + token);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("Accept", "application/json");

            try (OutputStream output = conn.getOutputStream()) {
                output.write(payload.getBytes(StandardCharsets.UTF_8));
            }

            int code = conn.getResponseCode();
            String response = readResponse(conn, code);

            System.out.println("ANTRIAN API RESPONSE CODE (" + noRawat + ") : " + code);
            System.out.println("ANTRIAN API RESPONSE BODY (" + noRawat + ") : " + response);

            return code >= 200 && code < 300;
        } catch (Exception e) {
            System.out.println("ANTRIAN API ERROR (" + noRawat + "): " + e.getMessage());
            return false;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private static String readResponse(HttpURLConnection conn, int code) {
        StringBuilder sb = new StringBuilder();

        try {
            InputStream stream = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();

            if (stream == null) {
                return "";
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                String line;

                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            }
        } catch (Exception e) {
            sb.append("Gagal membaca response: ").append(e.getMessage());
        }

        return sb.toString();
    }

    private static String escapeJson(String text) {
        if (text == null) {
            return "";
        }

        StringBuilder escaped = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            switch (c) {
                case '"':
                    escaped.append("\\\"");
                    break;
                case '\\':
                    escaped.append("\\\\");
                    break;
                default:
                    if (c < 0x20) {
                        escaped.append(String.format("\\u%04x", (int) c));
                    } else {
                        escaped.append(c);
                    }
            }
        }

        return escaped.toString();
    }
}
