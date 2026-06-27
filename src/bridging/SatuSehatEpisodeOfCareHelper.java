package bridging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fungsi.koneksiDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpStatusCodeException;

public class SatuSehatEpisodeOfCareHelper {

    private static final ApiSatuSehat api = new ApiSatuSehat();
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Get-or-create EpisodeOfCare untuk pasien + type.
     * Kalau ada EOC active matching, return id_eoc existing.
     * Kalau tidak ada, POST /EpisodeOfCare ke SatuSehat lalu simpan local.
     */
    public static String getOrCreate(
        String noRkmMedis,
        String idPasienIhs,
        String namaPasien,
        String typeCode,
        String typeDisplay,
        String conditionKdPenyakit,
        String periodStart,
        String careManagerNik
    ) {
        if (noRkmMedis == null || noRkmMedis.trim().isEmpty()) return "";
        if (typeCode == null || typeCode.trim().isEmpty()) return "";

        // 1. Check if active EOC exists in local DB
        String activeEoc = findActiveEoc(noRkmMedis, typeCode);
        if (activeEoc != null && !activeEoc.isEmpty()) {
            return activeEoc;
        }

        // 2. Fetch conditionId if conditionKdPenyakit is specified
        String conditionId = null;
        Connection con = koneksiDB.condb();
        if (conditionKdPenyakit != null && !conditionKdPenyakit.trim().isEmpty()) {
            String sql = "SELECT cond.id_condition FROM satu_sehat_condition cond "
                       + "INNER JOIN reg_periksa reg ON reg.no_rawat = cond.no_rawat "
                       + "WHERE reg.no_rkm_medis = ? AND cond.kd_penyakit = ? AND IFNULL(cond.id_condition, '') != '' "
                       + "ORDER BY cond.no_rawat DESC LIMIT 1";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, noRkmMedis);
                ps.setString(2, conditionKdPenyakit);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        conditionId = rs.getString("id_condition");
                    }
                }
            } catch (Exception e) {
                System.out.println("Error lookup id_condition: " + e);
            }
        }

        // 3. Resolve Care Manager (Practitioner IHS ID and employee NIP)
        String careManagerNikDb = null;
        String careManagerIhs = null;
        SatuSehatCekNIK cekViaSatuSehat = new SatuSehatCekNIK();
        if (careManagerNik != null && !careManagerNik.trim().isEmpty()) {
            if (careManagerNik.length() == 16 && careManagerNik.matches("\\d+")) {
                // Input is NIK KTP
                careManagerIhs = cekViaSatuSehat.tampilIDParktisi(careManagerNik);
                try (PreparedStatement ps = con.prepareStatement("SELECT nik FROM pegawai WHERE no_ktp = ? LIMIT 1")) {
                    ps.setString(1, careManagerNik);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            careManagerNikDb = rs.getString("nik");
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Error lookup NIP by NIK: " + e);
                }
            } else {
                // Input is employee NIP
                careManagerNikDb = careManagerNik;
                try (PreparedStatement ps = con.prepareStatement("SELECT no_ktp FROM pegawai WHERE nik = ? LIMIT 1")) {
                    ps.setString(1, careManagerNik);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            String ktp = rs.getString("no_ktp");
                            if (ktp != null && !ktp.isEmpty()) {
                                careManagerIhs = cekViaSatuSehat.tampilIDParktisi(ktp);
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Error lookup NIK by NIP: " + e);
                }
            }
        }

        // Default start date to today if empty
        if (periodStart == null || periodStart.trim().isEmpty()) {
            periodStart = LocalDate.now().toString();
        }

        // 4. Construct JSON payload for POST
        String json = "{"
            + "\"resourceType\": \"EpisodeOfCare\","
            + "\"identifier\": [{"
                + "\"system\": \"http://sys-ids.kemkes.go.id/episodeofcare/" + koneksiDB.IDSATUSEHAT() + "/\","
                + "\"use\": \"official\","
                + "\"value\": \"" + esc(noRkmMedis + "-" + typeCode) + "\""
            + "}],"
            + "\"status\": \"active\","
            + "\"type\": [{"
                + "\"coding\": [{"
                    + "\"system\": \"https://terminology.kemkes.go.id/CodeSystem/episodeofcare-type\","
                    + "\"code\": \"" + typeCode + "\","
                    + "\"display\": \"" + esc(typeDisplay) + "\""
                + "}]"
            + "}],"
            + (conditionId != null && !conditionId.isEmpty() ? "\"diagnosis\": [{"
                + "\"condition\": {\"reference\": \"Condition/" + conditionId + "\"},"
                + "\"role\": {"
                    + "\"coding\": [{"
                        + "\"system\": \"http://terminology.hl7.org/CodeSystem/diagnosis-role\","
                        + "\"code\": \"AD\","
                        + "\"display\": \"Admission diagnosis\""
                    + "}]"
                + "},"
                + "\"rank\": 1"
            + "}],": "")
            + "\"patient\": {"
                + "\"reference\": \"Patient/" + idPasienIhs + "\","
                + "\"display\": \"" + esc(namaPasien) + "\""
            + "},"
            + "\"period\": { \"start\": \"" + periodStart + "\" }"
            + (careManagerIhs != null && !careManagerIhs.isEmpty() ? ",\"careManager\": { \"reference\": \"Practitioner/" + careManagerIhs + "\" }" : "")
            + ",\"managingOrganization\": { \"reference\": \"Organization/" + koneksiDB.IDSATUSEHAT() + "\" }"
            + "}";

        // 5. Send POST to SatuSehat
        String idEoc = "";
        try {
            String link = koneksiDB.URLFHIRSATUSEHAT();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", "Bearer " + api.TokenSatuSehat());
            HttpEntity<String> requestEntity = new HttpEntity<>(json, headers);
            String url = link + "/EpisodeOfCare";
            System.out.println("POST EOC Request : " + json);
            String rawResponse = api.getRest().exchange(url, HttpMethod.POST, requestEntity, String.class).getBody();
            System.out.println("POST EOC Response: " + rawResponse);

            JsonNode root = mapper.readTree(rawResponse);
            idEoc = root.path("id").asText();

            if (idEoc != null && !idEoc.isEmpty()) {
                // 6. Save in local database
                String insertSql = "INSERT INTO satu_sehat_episodeofcare "
                    + "(id_eoc, no_rkm_medis, type_code, type_display, status, period_start, period_end, condition_kd_penyakit, care_manager_nik) "
                    + "VALUES (?, ?, ?, ?, 'active', ?, NULL, ?, ?)";
                try (PreparedStatement ps = con.prepareStatement(insertSql)) {
                    ps.setString(1, idEoc);
                    ps.setString(2, noRkmMedis);
                    ps.setString(3, typeCode);
                    ps.setString(4, typeDisplay);
                    ps.setString(5, periodStart);
                    ps.setString(6, conditionKdPenyakit);
                    ps.setString(7, careManagerNikDb);
                    ps.executeUpdate();
                } catch (Exception e) {
                    System.out.println("Error saving EOC locally: " + e);
                }
            }
        } catch (HttpStatusCodeException he) {
            System.out.println("EOC POST HTTP " + he.getStatusCode() + ": " + he.getResponseBodyAsString());
        } catch (Exception e) {
            System.out.println("EOC POST Exception: " + e);
        }

        return idEoc;
    }

    /**
     * Map nama poli ke type code Kemkes.
     * Returns [type_code, type_display] atau null.
     */
    public static String[] getTypeByPoli(String nmPoli) {
        if (nmPoli == null) return null;
        String poli = nmPoli.toLowerCase();
        if (poli.contains("anc")) {
            return new String[]{"ANC", "Antenatal Care"};
        }
        if (poli.contains("pnc")) {
            return new String[]{"PNC", "Postnatal Care"};
        }
        if (poli.contains("tb") || poli.contains("tuberkulosis") || poli.contains("tbc") || poli.contains("paru")) {
            return new String[]{"TB-DOTS", "TB-DOTS Program"};
        }
        if (poli.contains("hiv") || poli.contains("aids") || poli.contains("vct") || poli.contains("cst")) {
            return new String[]{"HIV-AIDS", "HIV-AIDS Program"};
        }
        if (poli.contains("hemodial") || poli.contains("dialisis")) {
            return new String[]{"HD", "Hemodialysis Program"};
        }
        if (poli.contains("onkologi") || poli.contains("kemoterapi") || poli.contains("kanker")) {
            return new String[]{"ONCO", "Oncology Program"};
        }
        if (poli.contains("rehab") || poli.contains("fisioterapi")) {
            return new String[]{"REHAB", "Rehabilitation Program"};
        }
        if (poli.contains("jiwa") || poli.contains("psikiatri") || poli.contains("mental")) {
            return new String[]{"MENTAL", "Mental Health Program"};
        }
        if (poli.contains("prolanis") || poli.contains("diabetes") || poli.contains("kronis")) {
            return new String[]{"CHRONIC", "Chronic Disease Management"};
        }
        return null;
    }

    /**
     * Fallback lookup EOC type by primary diagnosis from ICD-10.
     */
    public static String[] findTypeByDiagnosaRawat(String noRawat) {
        if (noRawat == null || noRawat.trim().isEmpty()) return null;
        Connection con = koneksiDB.condb();
        String sql = "SELECT kd_penyakit FROM diagnosa_pasien WHERE no_rawat = ? ORDER BY prioritas LIMIT 1";
        String kdPenyakit = null;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, noRawat);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    kdPenyakit = rs.getString("kd_penyakit");
                }
            }
        } catch (Exception e) {
            System.out.println("Error lookup primary diagnosis: " + e);
        }

        if (kdPenyakit == null || kdPenyakit.isEmpty()) return null;
        kdPenyakit = kdPenyakit.toUpperCase();

        if (kdPenyakit.startsWith("A15") || kdPenyakit.startsWith("A16") || kdPenyakit.startsWith("A17") || kdPenyakit.startsWith("A18") || kdPenyakit.startsWith("A19")) {
            return new String[]{"TB-DOTS", "TB-DOTS Program"};
        }
        if (kdPenyakit.startsWith("B20") || kdPenyakit.startsWith("B21") || kdPenyakit.startsWith("B22") || kdPenyakit.startsWith("B23") || kdPenyakit.startsWith("B24")) {
            return new String[]{"HIV-AIDS", "HIV-AIDS Program"};
        }
        return null;
    }

    /**
     * Link Encounter ke EOC via tabel satu_sehat_encounter_episodeofcare.
     */
    public static void linkEncounter(String noRawat, String idEoc) {
        if (noRawat == null || noRawat.isEmpty() || idEoc == null || idEoc.isEmpty()) return;
        Connection con = koneksiDB.condb();
        String sql = "INSERT INTO satu_sehat_encounter_episodeofcare (no_rawat, id_eoc) VALUES (?, ?) "
                   + "ON DUPLICATE KEY UPDATE id_eoc = VALUES(id_eoc)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, noRawat);
            ps.setString(2, idEoc);
            ps.executeUpdate();
        } catch (Exception e) {
            System.out.println("Error saving encounter relation link: " + e);
        }
    }

    /**
     * Update status EOC (untuk dialog "Selesai" button).
     * Internal: PUT /EpisodeOfCare/{id} ke SatuSehat + UPDATE local.
     */
    public static boolean updateStatus(
        String idEoc,
        String newStatus,
        String periodEnd
    ) {
        if (idEoc == null || idEoc.trim().isEmpty()) return false;
        Connection con = koneksiDB.condb();

        // 1. Fetch values for payload reconstruction
        String noRkmMedis = null;
        String typeCode = null;
        String typeDisplay = null;
        String periodStart = null;
        String conditionKdPenyakit = null;
        String careManagerNik = null;

        String selectSql = "SELECT no_rkm_medis, type_code, type_display, period_start, condition_kd_penyakit, care_manager_nik "
                         + "FROM satu_sehat_episodeofcare WHERE id_eoc = ? LIMIT 1";
        try (PreparedStatement ps = con.prepareStatement(selectSql)) {
            ps.setString(1, idEoc);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    noRkmMedis = rs.getString("no_rkm_medis");
                    typeCode = rs.getString("type_code");
                    typeDisplay = rs.getString("type_display");
                    periodStart = rs.getString("period_start");
                    conditionKdPenyakit = rs.getString("condition_kd_penyakit");
                    careManagerNik = rs.getString("care_manager_nik");
                }
            }
        } catch (Exception e) {
            System.out.println("Error EOC lookup for updateStatus: " + e);
            return false;
        }

        if (noRkmMedis == null) return false;

        // Resolve Patient IHS ID
        String idPasienIhs = new SatuSehatCekNIK().tampilIDPasien(
            new fungsi.sekuel().cariIsi("SELECT no_ktp FROM pasien WHERE no_rkm_medis = ?", noRkmMedis)
        );
        String namaPasien = new fungsi.sekuel().cariIsi("SELECT nm_pasien FROM pasien WHERE no_rkm_medis = ?", noRkmMedis);

        // Fetch conditionId if conditionKdPenyakit is specified
        String conditionId = null;
        if (conditionKdPenyakit != null && !conditionKdPenyakit.trim().isEmpty()) {
            String sql = "SELECT cond.id_condition FROM satu_sehat_condition cond "
                       + "INNER JOIN reg_periksa reg ON reg.no_rawat = cond.no_rawat "
                       + "WHERE reg.no_rkm_medis = ? AND cond.kd_penyakit = ? AND IFNULL(cond.id_condition, '') != '' "
                       + "ORDER BY cond.no_rawat DESC LIMIT 1";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, noRkmMedis);
                ps.setString(2, conditionKdPenyakit);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        conditionId = rs.getString("id_condition");
                    }
                }
            } catch (Exception ignored) {}
        }

        // Resolve Care Manager Practitioner IHS ID
        String careManagerIhs = null;
        if (careManagerNik != null && !careManagerNik.trim().isEmpty()) {
            String ktp = new fungsi.sekuel().cariIsi("SELECT no_ktp FROM pegawai WHERE nik = ?", careManagerNik);
            if (ktp != null && !ktp.isEmpty()) {
                careManagerIhs = new SatuSehatCekNIK().tampilIDParktisi(ktp);
            }
        }

        // Default end date to today if empty/null and status is finished/cancelled
        if (("finished".equals(newStatus) || "cancelled".equals(newStatus)) && (periodEnd == null || periodEnd.trim().isEmpty())) {
            periodEnd = LocalDate.now().toString();
        }

        // 2. Construct PUT JSON payload
        String json = "{"
            + "\"resourceType\": \"EpisodeOfCare\","
            + "\"id\": \"" + idEoc + "\","
            + "\"identifier\": [{"
                + "\"system\": \"http://sys-ids.kemkes.go.id/episodeofcare/" + koneksiDB.IDSATUSEHAT() + "/\","
                + "\"use\": \"official\","
                + "\"value\": \"" + esc(noRkmMedis + "-" + typeCode) + "\""
            + "}],"
            + "\"status\": \"" + newStatus + "\","
            + "\"type\": [{"
                + "\"coding\": [{"
                    + "\"system\": \"https://terminology.kemkes.go.id/CodeSystem/episodeofcare-type\","
                    + "\"code\": \"" + typeCode + "\","
                    + "\"display\": \"" + esc(typeDisplay) + "\""
                + "}]"
            + "}],"
            + (conditionId != null && !conditionId.isEmpty() ? "\"diagnosis\": [{"
                + "\"condition\": {\"reference\": \"Condition/" + conditionId + "\"},"
                + "\"role\": {"
                    + "\"coding\": [{"
                        + "\"system\": \"http://terminology.hl7.org/CodeSystem/diagnosis-role\","
                        + "\"code\": \"AD\","
                        + "\"display\": \"Admission diagnosis\""
                    + "}]"
                + "},"
                + "\"rank\": 1"
            + "}],": "")
            + "\"patient\": {"
                + "\"reference\": \"Patient/" + idPasienIhs + "\","
                + "\"display\": \"" + esc(namaPasien) + "\""
            + "},"
            + "\"period\": { \"start\": \"" + periodStart + "\"" + (periodEnd != null && !periodEnd.trim().isEmpty() ? ",\"end\": \"" + periodEnd + "\"" : "") + " }"
            + (careManagerIhs != null && !careManagerIhs.isEmpty() ? ",\"careManager\": { \"reference\": \"Practitioner/" + careManagerIhs + "\" }" : "")
            + ",\"managingOrganization\": { \"reference\": \"Organization/" + koneksiDB.IDSATUSEHAT() + "\" }"
            + "}";

        // 3. Send PUT request
        try {
            String link = koneksiDB.URLFHIRSATUSEHAT();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", "Bearer " + api.TokenSatuSehat());
            HttpEntity<String> requestEntity = new HttpEntity<>(json, headers);
            String url = link + "/EpisodeOfCare/" + idEoc;
            System.out.println("PUT EOC Request : " + json);
            String rawResponse = api.getRest().exchange(url, HttpMethod.PUT, requestEntity, String.class).getBody();
            System.out.println("PUT EOC Response: " + rawResponse);

            // 4. Update local DB status and period_end
            String updateSql = "UPDATE satu_sehat_episodeofcare SET status = ?, period_end = ? WHERE id_eoc = ?";
            try (PreparedStatement ps = con.prepareStatement(updateSql)) {
                ps.setString(1, newStatus);
                if (periodEnd != null && !periodEnd.isEmpty()) {
                    ps.setString(2, periodEnd);
                } else {
                    ps.setNull(2, java.sql.Types.DATE);
                }
                ps.setString(3, idEoc);
                ps.executeUpdate();
            }
            return true;
        } catch (HttpStatusCodeException he) {
            System.out.println("EOC PUT HTTP " + he.getStatusCode() + ": " + he.getResponseBodyAsString());
        } catch (Exception e) {
            System.out.println("EOC PUT Exception: " + e);
        }
        return false;
    }

    /**
     * Lookup EOC active untuk pasien + type.
     */
    public static String findActiveEoc(String noRkmMedis, String typeCode) {
        if (noRkmMedis == null || typeCode == null) return "";
        Connection con = koneksiDB.condb();
        String sql = "SELECT id_eoc FROM satu_sehat_episodeofcare WHERE no_rkm_medis = ? AND type_code = ? AND status = 'active' LIMIT 1";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, noRkmMedis);
            ps.setString(2, typeCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("id_eoc");
                }
            }
        } catch (Exception e) {
            System.out.println("Error lookup active EOC: " + e);
        }
        return "";
    }

    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
