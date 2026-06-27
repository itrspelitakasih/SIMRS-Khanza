package bridging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fungsi.koneksiDB;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpStatusCodeException;

/**
 * Helper untuk membuat (POST) FHIR Patient resource ke endpoint SatuSehat
 * di {@code koneksiDB.URLFHIRSATUSEHAT() + "/Patient"}. Hanya field yang
 * tervalidasi pada DlgPasien yang dikirim; address extension dilewati ketika
 * kode wilayah belum lengkap.
 */
public class SatuSehatCreatePasien {

    private final ApiSatuSehat api = new ApiSatuSehat();
    private final ObjectMapper mapper = new ObjectMapper();

    public String idpasien = "";
    public String errorMessage = "";
    public String rawResponse = "";

    public boolean kirim(String nik, String nama, String jk, String tglLahir,
                         String alamat, String rt, String rw, String kodePos,
                         String kdProp, String kdKab, String kdKec, String kdKel,
                         String phone, String email) {
        idpasien = "";
        errorMessage = "";
        rawResponse = "";
        try {
            String link = koneksiDB.URLFHIRSATUSEHAT();
            if (link == null || link.trim().isEmpty()) {
                errorMessage = "URLFHIRSATUSEHAT belum dikonfigurasi.";
                return false;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", "Bearer " + api.TokenSatuSehat());

            String gender = jk == null ? "" : jk.toUpperCase();
            String genderFhir = gender.startsWith("L") ? "male" : "female";

            StringBuilder addressExt = new StringBuilder();
            boolean hasAddressExt = (kdProp != null && !kdProp.isEmpty())
                                 || (kdKab  != null && !kdKab.isEmpty())
                                 || (kdKec  != null && !kdKec.isEmpty())
                                 || (kdKel  != null && !kdKel.isEmpty())
                                 || (rt     != null && !rt.isEmpty())
                                 || (rw     != null && !rw.isEmpty());
            if (hasAddressExt) {
                addressExt.append(",\"extension\":[{")
                          .append("\"url\":\"https://fhir.kemkes.go.id/StructureDefinition/administrativeCode\",")
                          .append("\"extension\":[");
                boolean first = true;
                if (kdProp != null && !kdProp.isEmpty()) { first = appendExt(addressExt, first, "province", kdProp); }
                if (kdKab  != null && !kdKab.isEmpty())  { first = appendExt(addressExt, first, "city",     kdKab);  }
                if (kdKec  != null && !kdKec.isEmpty())  { first = appendExt(addressExt, first, "district", kdKec);  }
                if (kdKel  != null && !kdKel.isEmpty())  { first = appendExt(addressExt, first, "village",  kdKel);  }
                if (rt     != null && !rt.isEmpty())     { first = appendExt(addressExt, first, "rt",       rt);     }
                if (rw     != null && !rw.isEmpty())     { first = appendExt(addressExt, first, "rw",       rw);     }
                addressExt.append("]}]");
            }

            StringBuilder telecom = new StringBuilder();
            boolean hasTelecom = (phone != null && !phone.isEmpty()) || (email != null && !email.isEmpty());
            if (hasTelecom) {
                telecom.append(",\"telecom\":[");
                boolean first = true;
                if (phone != null && !phone.isEmpty()) {
                    telecom.append("{\"system\":\"phone\",\"value\":\"").append(esc(phone)).append("\",\"use\":\"home\"}");
                    first = false;
                }
                if (email != null && !email.isEmpty()) {
                    if (!first) telecom.append(",");
                    telecom.append("{\"system\":\"email\",\"value\":\"").append(esc(email)).append("\",\"use\":\"home\"}");
                }
                telecom.append("]");
            }

            String json = "{"
                    + "\"resourceType\":\"Patient\","
                    + "\"identifier\":[{"
                    +   "\"use\":\"official\","
                    +   "\"system\":\"https://fhir.kemkes.go.id/id/nik\","
                    +   "\"value\":\"" + esc(nik) + "\""
                    + "}],"
                    + "\"active\":true,"
                    + "\"name\":[{\"use\":\"official\",\"text\":\"" + esc(nama) + "\"}],"
                    + "\"gender\":\"" + genderFhir + "\","
                    + "\"birthDate\":\"" + esc(tglLahir) + "\""
                    + telecom.toString()
                    + ",\"address\":[{"
                    +   "\"use\":\"home\",\"line\":[\"" + esc(alamat) + "\"],"
                    +   "\"postalCode\":\"" + esc(kodePos == null ? "" : kodePos) + "\","
                    +   "\"country\":\"ID\""
                    +   addressExt.toString()
                    + "}]"
                    + "}";

            HttpEntity<String> requestEntity = new HttpEntity<>(json, headers);
            String url = link + "/Patient";
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
                idpasien = id;
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

    private static boolean appendExt(StringBuilder sb, boolean first, String url, String code) {
        if (!first) sb.append(",");
        sb.append("{\"url\":\"").append(url).append("\",\"valueCode\":\"").append(esc(code)).append("\"}");
        return false;
    }

    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
