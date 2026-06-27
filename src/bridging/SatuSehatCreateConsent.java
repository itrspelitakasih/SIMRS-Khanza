package bridging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fungsi.koneksiDB;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpStatusCodeException;

/**
 * POST FHIR Consent ke {@code URLFHIRSATUSEHAT()/Consent} agar faskes
 * boleh akses data PII pasien (nama unmasked, alamat, dst.) di SatuSehat.
 * Pasien (atau wali) sudah tanda tangan form persetujuan tertulis offline;
 * helper ini hanya catat attestation ke SatuSehat.
 *
 * <p>NOTE 2026-05-08: SatuSehat Consent endpoint sedang tidak berfungsi
 * (terminology server mereka reject SEMUA nilai untuk Consent.status).
 * Set {@link #useLocalOnly} = true untuk skip HTTP POST dan record audit
 * trail di tabel lokal saja. Flip kembali ke false saat SatuSehat fix.
 */
public class SatuSehatCreateConsent {

    /** Bypass HTTP POST ke SatuSehat — record di DB lokal saja sebagai audit trail. */
    public static boolean useLocalOnly = true;

    private final ApiSatuSehat api = new ApiSatuSehat();
    private final ObjectMapper mapper = new ObjectMapper();
    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter LOCAL_TS = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public String consentId   = "";
    public String periodStart = "";
    public String periodEnd   = "";
    public String errorMessage = "";
    public String rawResponse  = "";

    public boolean kirim(String ihs, String namaPasien, int durasiTahun) {
        consentId = "";
        errorMessage = "";
        rawResponse = "";

        LocalDate today = LocalDate.now();
        LocalDate end   = today.plusYears(durasiTahun <= 0 ? 5 : durasiTahun);
        periodStart = today.format(ISO_DATE);
        periodEnd   = end.format(ISO_DATE);

        if (useLocalOnly) {
            consentId = "LOCAL-" + java.time.LocalDateTime.now().format(LOCAL_TS) + "-"
                      + (ihs == null ? "" : ihs.replaceAll("[^A-Za-z0-9]", ""));
            rawResponse = "{\"local\":true,\"note\":\"SatuSehat Consent endpoint tidak tersedia, audit trail dicatat lokal saja.\"}";
            return true;
        }

        try {
            String link = koneksiDB.URLFHIRSATUSEHAT();
            String orgId = koneksiDB.IDSATUSEHAT();
            if (link == null || link.trim().isEmpty()) {
                errorMessage = "URLFHIRSATUSEHAT belum dikonfigurasi.";
                return false;
            }
            if (orgId == null || orgId.trim().isEmpty()) {
                errorMessage = "IDSATUSEHAT (Organization ID) belum dikonfigurasi.";
                return false;
            }
            if (ihs == null || ihs.trim().isEmpty()) {
                errorMessage = "IHS Patient ID kosong.";
                return false;
            }

            String dateTimeNow = periodStart + "T00:00:00+07:00";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", "Bearer " + api.TokenSatuSehat());

            String safeNama = esc(namaPasien == null ? "" : namaPasien);
            String json = "{"
                    + "\"resourceType\":\"Consent\","
                    + "\"status\":\"active\","
                    + "\"scope\":{\"coding\":[{"
                    +   "\"system\":\"http://terminology.hl7.org/CodeSystem/consentscope\","
                    +   "\"code\":\"patient-privacy\","
                    +   "\"display\":\"Privacy Consent\""
                    + "}]},"
                    + "\"category\":[{\"coding\":[{"
                    +   "\"system\":\"http://loinc.org\","
                    +   "\"code\":\"59284-0\","
                    +   "\"display\":\"Patient Consent\""
                    + "}]}],"
                    + "\"patient\":{"
                    +   "\"reference\":\"Patient/" + esc(ihs) + "\","
                    +   "\"display\":\"" + safeNama + "\""
                    + "},"
                    + "\"dateTime\":\"" + dateTimeNow + "\","
                    + "\"performer\":[{\"reference\":\"Organization/" + esc(orgId) + "\"}],"
                    + "\"organization\":[{\"reference\":\"Organization/" + esc(orgId) + "\"}],"
                    + "\"policyRule\":{\"coding\":[{"
                    +   "\"system\":\"http://terminology.hl7.org/CodeSystem/consentpolicycodes\","
                    +   "\"code\":\"cric\""
                    + "}]},"
                    + "\"provision\":{"
                    +   "\"type\":\"permit\","
                    +   "\"period\":{"
                    +     "\"start\":\"" + periodStart + "T00:00:00+07:00\","
                    +     "\"end\":\""   + periodEnd   + "T23:59:59+07:00\""
                    +   "}"
                    + "}"
                    + "}";

            HttpEntity<String> requestEntity = new HttpEntity<>(json, headers);
            String url = link + "/Consent";
            System.out.println("URL : " + url);
            System.out.println("Request JSON : " + json);
            try {
                rawResponse = api.getRest().exchange(url, HttpMethod.POST, requestEntity, String.class).getBody();
            } catch (HttpStatusCodeException he) {
                rawResponse = he.getResponseBodyAsString();
                System.out.println("HTTP " + he.getStatusCode() + " body : " + rawResponse);
                String diagErr = "";
                try {
                    JsonNode er = mapper.readTree(rawResponse);
                    diagErr = er.path("issue").path(0).path("diagnostics").asText();
                    if (diagErr == null || diagErr.isEmpty()) diagErr = er.path("issue").path(0).path("details").path("text").asText();
                } catch (Exception ignored) {}
                errorMessage = "HTTP " + he.getStatusCode().value() + ": " +
                        (diagErr == null || diagErr.isEmpty() ? rawResponse : diagErr);
                return false;
            }
            System.out.println("Result JSON : " + rawResponse);

            JsonNode root = mapper.readTree(rawResponse);
            String id = root.path("id").asText();
            if (id != null && !id.isEmpty()) {
                consentId = id;
                return true;
            }
            String diag = root.path("issue").path(0).path("diagnostics").asText();
            errorMessage = (diag == null || diag.isEmpty()) ? rawResponse : diag;
            return false;
        } catch (Exception e) {
            errorMessage = e.getMessage() == null ? e.toString() : e.getMessage();
            System.out.println("Notifikasi : " + e);
            return false;
        }
    }

    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
