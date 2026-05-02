package bridging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fungsi.akses;
import fungsi.koneksiDB;
import fungsi.sekuel;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.imageio.ImageIO;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.swing.JOptionPane;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author windiartonugroho
 */
public class ApiOrthanc {

    private HttpHeaders headers;
    private JsonNode root;
    private HttpEntity requestEntity;
    private ObjectMapper mapper = new ObjectMapper();
    private sekuel Sequel = new sekuel();
    private SSLContext sslContext;
    private SSLSocketFactory sslFactory;
    private Scheme scheme;
    private HttpComponentsClientHttpRequestFactory factory;
    private String auth, authEncrypt, requestJson;
    private byte[] encodedBytes;
    private int i = 1;

    public ApiOrthanc() {
        try {
            auth = koneksiDB.USERORTHANC() + ":" + koneksiDB.PASSORTHANC();
            encodedBytes = Base64.encodeBase64(auth.getBytes());
            authEncrypt = new String(encodedBytes);
        } catch (Exception ex) {
            System.out.println("Notifikasi : " + ex);
        }
    }

    public String Auth() {
        return authEncrypt;
    }

    //awal
    public JsonNode AmbilSeries(String Norm, String Tanggal1, String Tanggal2) {
        System.out.println("Percobaan Mengambil Photo Pasien : " + Norm);
        try {
            headers = new HttpHeaders();
            System.out.println("Auth : " + authEncrypt);
            headers.add("Authorization", "Basic " + authEncrypt);
            requestJson = "{"
                    + "\"Level\": \"Study\","
                    + "\"Expand\": true,"
                    + "\"Query\": {"
                    + "\"StudyDate\": \"" + Tanggal1 + "-" + Tanggal2 + "\","
                    + "\"PatientID\": \"" + Norm + "\""
                    + "}"
                    + "}";
            System.out.println("Request JSON : " + requestJson);
            requestEntity = new HttpEntity(requestJson, headers);
            System.out.println("URL : " + koneksiDB.URLORTHANC() + ":" + koneksiDB.PORTORTHANC() + "/tools/find");
            requestJson = getRest().exchange(koneksiDB.URLORTHANC() + ":" + koneksiDB.PORTORTHANC() + "/tools/find",
                    HttpMethod.POST, requestEntity, String.class).getBody();
            System.out.println("Result JSON : " + requestJson);
            root = mapper.readTree(requestJson);
        } catch (Exception e) {
            System.out.println("Notifikasi : " + e);
            JOptionPane.showMessageDialog(null,
                    "Gagal mengambil data dari Orthanc, silahkan hubungi administrator ..!!");
        }
        return root;
    }

    public JsonNode AmbilPng(String NoRawat, String Series) {
        System.out.println("Percobaan Mengambil Gambar PNG : " + NoRawat + ", Series : " + Series);
        try {
            headers = new HttpHeaders();
            System.out.println("Auth : " + authEncrypt);
            headers.add("Authorization", "Basic " + authEncrypt);
            requestEntity = new HttpEntity(headers);
            System.out.println("URL : " + koneksiDB.URLORTHANC() + ":" + koneksiDB.PORTORTHANC() + "/series/" + Series);
            requestJson = getRest()
                    .exchange(koneksiDB.URLORTHANC() + ":" + koneksiDB.PORTORTHANC() + "/series/" + Series,
                            HttpMethod.GET, requestEntity, String.class)
                    .getBody();
            System.out.println("Result JSON : " + requestJson);
            root = mapper.readTree(requestJson);
            i = 1;
            for (JsonNode list : root.path("Instances")) {
                System.out.println("Mengambil Gambar PNG " + koneksiDB.URLORTHANC() + ":" + koneksiDB.PORTORTHANC()
                        + "/instances/" + list.asText() + "/preview");
                headers = new HttpHeaders();
                headers.add("Authorization", "Basic " + authEncrypt);
                headers.add("Accept", "image/png");
                headers.setAccept(Collections.singletonList(MediaType.APPLICATION_OCTET_STREAM));
                headers.setAccept(Collections.singletonList(MediaType.IMAGE_JPEG));
                HttpEntity<String> entity = new HttpEntity<>(headers);
                ResponseEntity<byte[]> response = getRest().exchange(koneksiDB.URLORTHANC() + ":"
                        + koneksiDB.PORTORTHANC() + "/instances/" + list.asText() + "/preview", HttpMethod.GET, entity,
                        byte[].class);
                Files.write(Paths.get("./gambarradiologi/" + NoRawat + i + ".png"), response.getBody());
                i++;
            }
            JOptionPane.showMessageDialog(null,
                    "Pengambilan Gambar PNG dari Orthanc berhasil, silahkan lihat di dalam folder Aplikasi..!!");
        } catch (Exception e) {
            System.out.println("Notifikasi : " + e);
            JOptionPane.showMessageDialog(null,
                    "Gagal mengambil Gambar PNG dari Orthanc, silahkan hubungi administrator ..!!");
        }
        return root;
    }
    //AMBIL GAMBAR

    public JsonNode AmbilJpg(
            String NoRawat,
            String Series,
            String norawatslash,
            String tanggalPeriksa,
            String jamPeriksa
    ) {
        System.out.println("Percobaan Mengambil Gambar JPG : " + NoRawat + ", Series : " + Series);

        try {
            File folderTemp = new File("./gambarradiologi");

            if (!folderTemp.exists()) {
                boolean buatFolder = folderTemp.mkdirs();

                if (!buatFolder) {
                    JOptionPane.showMessageDialog(null,
                            "Gagal membuat folder temporary: " + folderTemp.getAbsolutePath());
                    return root;
                }
            }

            // Ambil No RM
            String noRm = Sequel.cariIsi(
                    "SELECT IFNULL(no_rkm_medis,'') "
                    + "FROM reg_periksa "
                    + "WHERE no_rawat=?",
                    norawatslash
            );

            // Ambil nama pasien
            String namaPasien = Sequel.cariIsi(
                    "SELECT IFNULL(nm_pasien,'') "
                    + "FROM pasien "
                    + "WHERE no_rkm_medis=?",
                    noRm
            );

            // Ambil jenis kelamin
            String jkPasien = Sequel.cariIsi(
                    "SELECT IFNULL(pasien.jk,'') "
                    + "FROM reg_periksa "
                    + "INNER JOIN pasien ON reg_periksa.no_rkm_medis = pasien.no_rkm_medis "
                    + "WHERE reg_periksa.no_rawat=?",
                    norawatslash
            );

            // Ambil tanggal lahir
            String tglLahirPasien = Sequel.cariIsi(
                    "SELECT IFNULL(pasien.tgl_lahir,'') "
                    + "FROM reg_periksa "
                    + "INNER JOIN pasien ON reg_periksa.no_rkm_medis = pasien.no_rkm_medis "
                    + "WHERE reg_periksa.no_rawat=?",
                    norawatslash
            );

            // Ambil nama penjab / perusahaan
            String namaPenjab = Sequel.cariIsi(
                    "SELECT IFNULL(penjab.png_jawab,'') "
                    + "FROM reg_periksa "
                    + "INNER JOIN penjab ON reg_periksa.kd_pj = penjab.kd_pj "
                    + "WHERE reg_periksa.no_rawat=?",
                    norawatslash
            );

            if (namaPenjab == null || namaPenjab.trim().equals("")) {
                namaPenjab = "UMUM";
            }

            if (namaPasien == null || namaPasien.trim().equals("")) {
                namaPasien = "TANPA_NAMA";
            }

            String prefixPenjab = bersihkanNamaFile(namaPenjab);
            String prefixNoRawat = bersihkanNamaFile(norawatslash);
            String prefixPasien = bersihkanNamaFile(namaPasien);

            String prefixFile = prefixPenjab + "_" + prefixNoRawat + "_" + prefixPasien;

            System.out.println("No RM       : " + noRm);
            System.out.println("Nama Pasien : " + namaPasien);
            System.out.println("JK          : " + jkPasien);
            System.out.println("Tgl Lahir   : " + tglLahirPasien);
            System.out.println("Penjab      : " + namaPenjab);
            System.out.println("Prefix File : " + prefixFile);

            // Ambil data Series dari Orthanc
            headers = new HttpHeaders();
            headers.add("Authorization", "Basic " + authEncrypt);
            requestEntity = new HttpEntity(headers);

            String urlSeries = koneksiDB.URLORTHANC() + ":"
                    + koneksiDB.PORTORTHANC()
                    + "/series/"
                    + Series;

            System.out.println("URL Series : " + urlSeries);

            requestJson = getRest().exchange(
                    urlSeries,
                    HttpMethod.GET,
                    requestEntity,
                    String.class
            ).getBody();

            System.out.println("Result JSON : " + requestJson);

            root = mapper.readTree(requestJson);

            int urut = 0;
            int sukses = 0;
            int gagal = 0;

            for (JsonNode list : root.path("Instances")) {
                String instanceId = list.asText();

                String urlPreview = koneksiDB.URLORTHANC() + ":"
                        + koneksiDB.PORTORTHANC()
                        + "/instances/"
                        + instanceId
                        + "/preview";

                System.out.println("Mengambil Gambar JPG : " + urlPreview);

                headers = new HttpHeaders();
                headers.add("Authorization", "Basic " + authEncrypt);
                headers.setAccept(Collections.singletonList(MediaType.IMAGE_JPEG));

                HttpEntity<String> entity = new HttpEntity<>(headers);

                ResponseEntity<byte[]> response = getRest().exchange(
                        urlPreview,
                        HttpMethod.GET,
                        entity,
                        byte[].class
                );

                if (response.getBody() == null || response.getBody().length == 0) {
                    System.out.println("Gambar kosong dari Orthanc. Instance: " + instanceId);
                    gagal++;
                    urut++;
                    continue;
                }

                String tanggalFile = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

                String random4 = UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 4);

                String namaFileUnik = prefixFile
                        + "_"
                        + tanggalFile
                        + "_"
                        + random4
                        + "_"
                        + urut
                        + ".jpg";

                File fileJpg = new File(folderTemp, namaFileUnik);

                // Simpan JPG preview dari Orthanc ke temporary lokal
                Files.write(fileJpg.toPath(), response.getBody());

                if (!fileJpg.exists() || fileJpg.length() == 0) {
                    System.out.println("File lokal gagal dibuat / kosong: " + fileJpg.getAbsolutePath());
                    gagal++;
                    urut++;
                    continue;
                }

                System.out.println("File lokal berhasil dibuat:");
                System.out.println("  Path : " + fileJpg.getAbsolutePath());
                System.out.println("  Size : " + fileJpg.length());
                System.out.println("  Nama : " + namaFileUnik);

                /*
             * Ambil metadata DICOM untuk watermark.
                 */
                JsonNode tags = ambilSimplifiedTagsInstance(instanceId);

                String wmNamaPasien = namaPasien;
                String wmJk = convertJKUntukWatermark(jkPasien);
                String wmUmur = hitungUmurTahun(tglLahirPasien);
                String wmNoRm = noRm;
                String wmPemeriksaan = "";
                String wmNamaRs = akses.getnamars();
                String wmTanggal = tanggalPeriksa;
                String wmJam = jamPeriksa;
                String wmTambahan = "";

                if (tags != null) {
                    String tagPatientName = ambilTag(tags, "PatientName");
                    String tagPatientSex = ambilTag(tags, "PatientSex");
                    String tagPatientBirthDate = ambilTag(tags, "PatientBirthDate");
                    String tagPatientID = ambilTag(tags, "PatientID");
                    String tagInstitutionName = ambilTag(tags, "InstitutionName");

                    if (!tagPatientName.trim().equals("")) {
                        wmNamaPasien = tagPatientName.replace("^", " ");
                    }

                    if (!tagPatientSex.trim().equals("")) {
                        wmJk = tagPatientSex;
                    }

                    if (!tagPatientBirthDate.trim().equals("")) {
                        wmUmur = hitungUmurTahunDariDicom(tagPatientBirthDate);
                    }

                    if (!tagPatientID.trim().equals("")) {
                        wmNoRm = tagPatientID;
                    }

                    if (!tagInstitutionName.trim().equals("")) {
                        wmNamaRs = tagInstitutionName;
                    }

                    String bodyPart = ambilTag(tags, "BodyPartExamined");
                    String protocol = ambilTag(tags, "ProtocolName");
                    String studyDesc = ambilTag(tags, "StudyDescription");
                    String seriesDesc = ambilTag(tags, "SeriesDescription");

                    if (!seriesDesc.trim().equals("")) {
                        wmPemeriksaan = seriesDesc;
                    } else if (!studyDesc.trim().equals("")) {
                        wmPemeriksaan = studyDesc;
                    } else if (!bodyPart.trim().equals("") || !protocol.trim().equals("")) {
                        wmPemeriksaan = bodyPart;

                        if (!protocol.trim().equals("")) {
                            if (!wmPemeriksaan.trim().equals("")) {
                                wmPemeriksaan = wmPemeriksaan + ":" + protocol;
                            } else {
                                wmPemeriksaan = protocol;
                            }
                        }
                    }

                    String studyDate = ambilTag(tags, "StudyDate");
                    String studyTime = ambilTag(tags, "StudyTime");
                    String exposureIndex = ambilTag(tags, "ExposureIndex");

                    if (!studyDate.trim().equals("") && studyDate.length() == 8) {
                        wmTanggal = studyDate.substring(0, 4)
                                + "-"
                                + studyDate.substring(4, 6)
                                + "-"
                                + studyDate.substring(6, 8);
                    }

                    if (!studyTime.trim().equals("") && studyTime.length() >= 6) {
                        wmJam = studyTime.substring(0, 2)
                                + ":"
                                + studyTime.substring(2, 4)
                                + ":"
                                + studyTime.substring(4, 6);
                    }

                    if (!exposureIndex.trim().equals("")) {
                        wmTambahan = "EI:" + exposureIndex;
                    }
                }

                /*
             * Tambahkan watermark ke file JPG.
             * Dilakukan sebelum upload ke PHP.
                 */
                boolean watermarkOk = tambahkanWatermarkRadiologi(
                        fileJpg,
                        wmNamaPasien,
                        wmJk,
                        wmUmur,
                        wmNoRm,
                        wmPemeriksaan,
                        wmNamaRs,
                        wmTanggal,
                        wmJam,
                        wmTambahan
                );

                if (!watermarkOk) {
                    System.out.println("Watermark gagal. Lanjut upload file asli tanpa watermark.");
                }

                /*
             * Upload ke PHP upload_from_java.php
                 */
                boolean uploadSukses = uploadImageRadiologiFix(namaFileUnik, "pages/upload");

                if (!uploadSukses) {
                    System.out.println("Upload ke web gagal: " + namaFileUnik);
                    gagal++;
                    urut++;
                    continue;
                }

                String lokasiGambar = "pages/upload/" + namaFileUnik;

                System.out.println("Menyimpan ke DB:");
                System.out.println("  no_rawat      : " + norawatslash);
                System.out.println("  tgl_periksa   : " + tanggalPeriksa);
                System.out.println("  jam           : " + jamPeriksa);
                System.out.println("  lokasi_gambar : " + lokasiGambar);

                Sequel.menyimpantf(
                        "gambar_radiologi",
                        "?,?,?,?",
                        "No.Rawat",
                        4,
                        new String[]{
                            norawatslash,
                            tanggalPeriksa,
                            jamPeriksa,
                            lokasiGambar
                        }
                );

                sukses++;

                try {
                    if (fileJpg.exists()) {
                        fileJpg.delete();
                    }
                } catch (Exception ex) {
                    System.out.println("Gagal hapus temporary: " + ex);
                }

                urut++;
            }

            JOptionPane.showMessageDialog(null,
                    "Penyimpanan Gambar JPG dari Orthanc selesai.\n"
                    + "Berhasil : " + sukses + "\n"
                    + "Gagal    : " + gagal);

        } catch (Exception e) {
            System.out.println("Notifikasi AmbilJpg : " + e);
            System.out.println("Error detail: " + e.getMessage());
            e.printStackTrace();

            JOptionPane.showMessageDialog(null,
                    "Gagal mengambil Gambar JPG dari Orthanc.\n"
                    + e.getMessage());
        }

        return root;
    }

    private String bersihkanNamaFile(String teks) {
        if (teks == null) {
            return "";
        }

        teks = teks.trim();
        teks = teks.replaceAll("[^A-Za-z0-9]", "_");
        teks = teks.replaceAll("_+", "_");
        teks = teks.replaceAll("^_+", "");
        teks = teks.replaceAll("_+$", "");

        return teks;
    }

    private String ambilTag(JsonNode node, String namaTag) {
        try {
            if (node == null) {
                return "";
            }

            JsonNode hasil = node.path(namaTag);

            if (hasil == null || hasil.isMissingNode() || hasil.isNull()) {
                return "";
            }

            return hasil.asText();
        } catch (Exception e) {
            return "";
        }
    }

//    public JsonNode AmbilJpg(String NoRawat, String Series, String norawatslash, String tanggalPeriksa, String jamPeriksa) {
//        System.out.println("Percobaan Mengambil Gambar JPG : " + NoRawat + ", Series : " + Series);
//        try {
//            headers = new HttpHeaders();
//            System.out.println("Auth : " + authEncrypt);
//            headers.add("Authorization", "Basic " + authEncrypt);
//            requestEntity = new HttpEntity(headers);
//            System.out.println("URL : " + koneksiDB.URLORTHANC() + ":" + koneksiDB.PORTORTHANC() + "/series/" + Series);
//            requestJson = getRest()
//                    .exchange(koneksiDB.URLORTHANC() + ":" + koneksiDB.PORTORTHANC() + "/series/" + Series,
//                            HttpMethod.GET, requestEntity, String.class)
//                    .getBody();
//            System.out.println("Result JSON : " + requestJson);
//            root = mapper.readTree(requestJson);
//            i = 1;
//            for (JsonNode list : root.path("Instances")) {
//                System.out.println("Mengambil Gambar JPG " + koneksiDB.URLORTHANC() + ":" + koneksiDB.PORTORTHANC()
//                        + "/instances/" + list.asText() + "/preview");
//                headers = new HttpHeaders();
//                headers.add("Authorization", "Basic " + authEncrypt);
//                headers.add("Accept", "image/jpeg");
//                headers.setAccept(Collections.singletonList(MediaType.APPLICATION_OCTET_STREAM));
//                headers.setAccept(Collections.singletonList(MediaType.IMAGE_JPEG));
//                HttpEntity<String> entity = new HttpEntity<>(headers);
//                ResponseEntity<byte[]> response = getRest().exchange(koneksiDB.URLORTHANC() + ":"
//                        + koneksiDB.PORTORTHANC() + "/instances/" + list.asText() + "/preview", HttpMethod.GET, entity,
//                        byte[].class);
//                String uniqueName = NoRawat + "_" + System.currentTimeMillis() + "_" + i + ".jpg";
//                Files.write(Paths.get("./gambarradiologi/" + uniqueName), response.getBody());
//                uploadImageRadiologi(uniqueName, "pages/upload");
//
//                System.out.println("Menyimpan ke DB:");
//                System.out.println("  no_rawat     : " + norawatslash);
//                System.out.println("  tgl_periksa  : " + tanggalPeriksa);
//                System.out.println("  jam          : " + jamPeriksa);
//                System.out.println("  lokasi_gambar: " + "pages/upload/" + uniqueName);
//
//                Sequel.menyimpantf("gambar_radiologi", "?,?,?,?", "No.Rawat", 4,
//                        new String[]{
//                            norawatslash,
//                            tanggalPeriksa,
//                            jamPeriksa,
//                            "pages/upload/" + uniqueName
//                        });
//                i++;
//            }
//            JOptionPane.showMessageDialog(null, "Penyimpanan Gambar JPG dari Orthanc berhasil");
//        } catch (Exception e) {
//            System.out.println("Notifikasi : " + e);
//            System.out.println("Error detail: " + e.getMessage());
//            JOptionPane.showMessageDialog(null,
//                    "Gagal mengambil Gambar JPG dari Orthanc, silahkan hubungi administrator ..!!");
//        }
//        return root;
//    }
    private boolean uploadImageRadiologiFix(String namaFile, String folderTujuan) {
        String boundary = "----SIMRSKhanzaBoundary" + System.currentTimeMillis();
        String LINE_FEED = "\r\n";

        HttpURLConnection conn = null;
        OutputStream outputStream = null;
        PrintWriter writer = null;

        try {
            File fileSumber = new File("./gambarradiologi/" + namaFile);

            if (!fileSumber.exists()) {
                System.out.println("File sumber tidak ditemukan: " + fileSumber.getAbsolutePath());
                return false;
            }

            if (fileSumber.length() == 0) {
                System.out.println("File sumber kosong: " + fileSumber.getAbsolutePath());
                return false;
            }

            String urlUpload = "http://"
                    + koneksiDB.HOSTHYBRIDWEB()
                    + ":"
                    + koneksiDB.PORTWEB()
                    + "/"
                    + koneksiDB.HYBRIDWEB()
                    + "/radiologi/upload_from_java.php";

            System.out.println("Upload gambar ke PHP:");
            System.out.println("  URL  : " + urlUpload);
            System.out.println("  File : " + fileSumber.getAbsolutePath());
            System.out.println("  Size : " + fileSumber.length());

            URL url = new URL(urlUpload);
            conn = (HttpURLConnection) url.openConnection();

            conn.setUseCaches(false);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(60000);
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            conn.setRequestProperty("User-Agent", "SIMRS-Khanza");

            outputStream = conn.getOutputStream();
            writer = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"), true);

            /*
         * Field nama_file
             */
            writer.append("--").append(boundary).append(LINE_FEED);
            writer.append("Content-Disposition: form-data; name=\"nama_file\"").append(LINE_FEED);
            writer.append("Content-Type: text/plain; charset=UTF-8").append(LINE_FEED);
            writer.append(LINE_FEED);
            writer.append(namaFile).append(LINE_FEED);
            writer.flush();

            /*
         * Field folder_tujuan, optional.
         * PHP boleh mengabaikan ini karena foldernya sudah pages/upload.
             */
            writer.append("--").append(boundary).append(LINE_FEED);
            writer.append("Content-Disposition: form-data; name=\"folder_tujuan\"").append(LINE_FEED);
            writer.append("Content-Type: text/plain; charset=UTF-8").append(LINE_FEED);
            writer.append(LINE_FEED);
            writer.append(folderTujuan).append(LINE_FEED);
            writer.flush();

            /*
         * Field file gambar
             */
            writer.append("--").append(boundary).append(LINE_FEED);
            writer.append("Content-Disposition: form-data; name=\"gambar\"; filename=\"")
                    .append(namaFile)
                    .append("\"")
                    .append(LINE_FEED);
            writer.append("Content-Type: image/jpeg").append(LINE_FEED);
            writer.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
            writer.append(LINE_FEED);
            writer.flush();

            FileInputStream inputStream = new FileInputStream(fileSumber);
            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.flush();
            inputStream.close();

            writer.append(LINE_FEED);
            writer.flush();

            /*
         * Akhir multipart
             */
            writer.append("--").append(boundary).append("--").append(LINE_FEED);
            writer.close();

            int responseCode = conn.getResponseCode();

            InputStream responseStream;
            if (responseCode >= 200 && responseCode < 300) {
                responseStream = conn.getInputStream();
            } else {
                responseStream = conn.getErrorStream();
            }

            StringBuilder responseBuilder = new StringBuilder();

            if (responseStream != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(responseStream, "UTF-8"));
                String line;

                while ((line = reader.readLine()) != null) {
                    responseBuilder.append(line);
                }

                reader.close();
            }

            String hasil = responseBuilder.toString();

            System.out.println("HTTP Response Code : " + responseCode);
            System.out.println("Response upload PHP: " + hasil);

            if (responseCode < 200 || responseCode >= 300) {
                System.out.println("Upload gagal. HTTP code: " + responseCode);
                return false;
            }

            if (hasil == null || hasil.trim().equals("")) {
                System.out.println("Response upload kosong.");
                return false;
            }

            JsonNode json = mapper.readTree(hasil);

            if (json.path("status").asBoolean(false)) {
                System.out.println("Upload berhasil ke web: " + json.path("file").asText());
                return true;
            } else {
                System.out.println("Upload gagal dari PHP: " + json.path("message").asText());
                return false;
            }

        } catch (Exception e) {
            System.out.println("Gagal uploadImageRadiologiFix: " + e);
            e.printStackTrace();
            return false;

        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (Exception e) {
                System.out.println("Gagal close writer: " + e);
            }

            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (Exception e) {
                System.out.println("Gagal close outputStream: " + e);
            }

            try {
                if (conn != null) {
                    conn.disconnect();
                }
            } catch (Exception e) {
                System.out.println("Gagal disconnect koneksi: " + e);
            }
        }
    }

    public JsonNode AmbilBmp(String NoRawat, String Series) {
        System.out.println("Percobaan Mengambil Gambar BMP : " + NoRawat + ", Series : " + Series);
        try {
            headers = new HttpHeaders();
            System.out.println("Auth : " + authEncrypt);
            headers.add("Authorization", "Basic " + authEncrypt);
            requestEntity = new HttpEntity(headers);
            System.out.println("URL : " + koneksiDB.URLORTHANC() + ":" + koneksiDB.PORTORTHANC() + "/series/" + Series);
            requestJson = getRest()
                    .exchange(koneksiDB.URLORTHANC() + ":" + koneksiDB.PORTORTHANC() + "/series/" + Series,
                            HttpMethod.GET, requestEntity, String.class)
                    .getBody();
            System.out.println("Result JSON : " + requestJson);
            root = mapper.readTree(requestJson);
            i = 1;
            for (JsonNode list : root.path("Instances")) {
                System.out.println("Mengambil Gambar BMP " + koneksiDB.URLORTHANC() + ":" + koneksiDB.PORTORTHANC()
                        + "/instances/" + list.asText() + "/preview");
                headers = new HttpHeaders();
                headers.add("Authorization", "Basic " + authEncrypt);
                headers.add("Accept", "image/bmp");
                headers.setAccept(Collections.singletonList(MediaType.APPLICATION_OCTET_STREAM));
                headers.setAccept(Collections.singletonList(MediaType.IMAGE_JPEG));
                HttpEntity<String> entity = new HttpEntity<>(headers);
                ResponseEntity<byte[]> response = getRest().exchange(koneksiDB.URLORTHANC() + ":"
                        + koneksiDB.PORTORTHANC() + "/instances/" + list.asText() + "/preview", HttpMethod.GET, entity,
                        byte[].class);
                Files.write(Paths.get("./gambarradiologi/" + NoRawat + i + ".bmp"), response.getBody());
                i++;
            }
            JOptionPane.showMessageDialog(null,
                    "Pengambilan Gambar BMP dari Orthanc berhasil, silahkan lihat di dalam folder Aplikasi..!!");
        } catch (Exception e) {
            System.out.println("Notifikasi : " + e);
            JOptionPane.showMessageDialog(null,
                    "Gagal mengambil Gambar BMP dari Orthanc, silahkan hubungi administrator ..!!");
        }
        return root;
    }

    public JsonNode AmbilDcm(String NoRawat, String Series) {
        System.out.println("Percobaan Mengambil Gambar DCM : " + NoRawat + ", Series : " + Series);
        try {
            headers = new HttpHeaders();
            System.out.println("Auth : " + authEncrypt);
            headers.add("Authorization", "Basic " + authEncrypt);
            requestEntity = new HttpEntity(headers);
            System.out.println("URL : " + koneksiDB.URLORTHANC() + ":" + koneksiDB.PORTORTHANC() + "/series/" + Series);
            requestJson = getRest()
                    .exchange(koneksiDB.URLORTHANC() + ":" + koneksiDB.PORTORTHANC() + "/series/" + Series,
                            HttpMethod.GET, requestEntity, String.class)
                    .getBody();
            System.out.println("Result JSON : " + requestJson);
            root = mapper.readTree(requestJson);
            i = 1;
            for (JsonNode list : root.path("Instances")) {
                System.out.println("Mengambil Gambar DCM " + koneksiDB.URLORTHANC() + ":" + koneksiDB.PORTORTHANC()
                        + "/instances/" + list.asText() + "/file");
                headers = new HttpHeaders();
                headers.add("Authorization", "Basic " + authEncrypt);
                headers.setAccept(Collections.singletonList(MediaType.APPLICATION_OCTET_STREAM));
                headers.setAccept(Collections.singletonList(MediaType.IMAGE_JPEG));
                HttpEntity<String> entity = new HttpEntity<>(headers);
                ResponseEntity<byte[]> response = getRest().exchange(koneksiDB.URLORTHANC() + ":"
                        + koneksiDB.PORTORTHANC() + "/instances/" + list.asText() + "/file", HttpMethod.GET, entity,
                        byte[].class);
                Files.write(Paths.get("./gambarradiologi/" + NoRawat + i + ".dcm"), response.getBody());
                i++;
            }
            JOptionPane.showMessageDialog(null,
                    "Pengambilan Gambar DCM dari Orthanc berhasil, silahkan lihat di dalam folder Aplikasi..!!");
        } catch (Exception e) {
            System.out.println("Notifikasi : " + e);
            JOptionPane.showMessageDialog(null,
                    "Gagal mengambil Gambar DCM dari Orthanc, silahkan hubungi administrator ..!!");
        }
        return root;
    }

    public RestTemplate getRest() throws NoSuchAlgorithmException, KeyManagementException {
        sslContext = SSLContext.getInstance("SSL");
        TrustManager[] trustManagers = {
            new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                }

                public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                }
            }
        };
        sslContext.init(null, trustManagers, new SecureRandom());
        sslFactory = new SSLSocketFactory(sslContext, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        scheme = new Scheme("https", 443, sslFactory);
        factory = new HttpComponentsClientHttpRequestFactory();
        factory.getHttpClient().getConnectionManager().getSchemeRegistry().register(scheme);
        return new RestTemplate(factory);
    }

    public JsonNode AmbilJpgMultiSeries(
            String NoRawat,
            JsonNode studyNode,
            String norawatslash,
            String tanggalPeriksa,
            String jamPeriksa
    ) {
        System.out.println("Percobaan Multi Download JPG Orthanc");
        System.out.println("NoRawat       : " + NoRawat);
        System.out.println("NoRawat Slash : " + norawatslash);
        System.out.println("Tanggal       : " + tanggalPeriksa);
        System.out.println("Jam           : " + jamPeriksa);

        try {
            if (studyNode == null || studyNode.path("Series").isMissingNode()) {
                JOptionPane.showMessageDialog(null, "Data Series Orthanc tidak ditemukan.");
                return studyNode;
            }

            int totalSeries = 0;
            int totalSukses = 0;
            int totalGagal = 0;

            for (JsonNode seriesNode : studyNode.path("Series")) {
                String seriesId = seriesNode.asText();

                if (seriesId == null || seriesId.trim().equals("")) {
                    continue;
                }

                totalSeries++;

                System.out.println("======================================");
                System.out.println("Download Series ke-" + totalSeries);
                System.out.println("Series ID : " + seriesId);
                System.out.println("======================================");

                int[] hasil = AmbilJpgPerSeries(
                        NoRawat,
                        seriesId,
                        norawatslash,
                        tanggalPeriksa,
                        jamPeriksa,
                        totalSukses
                );

                totalSukses += hasil[0];
                totalGagal += hasil[1];
            }

            JOptionPane.showMessageDialog(null,
                    "Multi Download Gambar JPG dari Orthanc selesai.\n"
                    + "Total Series : " + totalSeries + "\n"
                    + "Berhasil     : " + totalSukses + "\n"
                    + "Gagal        : " + totalGagal);

        } catch (Exception e) {
            System.out.println("Notifikasi Multi Download : " + e);
            e.printStackTrace();

            JOptionPane.showMessageDialog(null,
                    "Gagal multi download gambar dari Orthanc.\n"
                    + e.getMessage());
        }

        return studyNode;
    }

    private int[] AmbilJpgPerSeries(
            String NoRawat,
            String Series,
            String norawatslash,
            String tanggalPeriksa,
            String jamPeriksa,
            int urutAwal
    ) {
        int sukses = 0;
        int gagal = 0;

        System.out.println("Percobaan Mengambil Gambar JPG : " + NoRawat + ", Series : " + Series);

        try {
            File folderTemp = new File("./gambarradiologi");

            if (!folderTemp.exists()) {
                boolean buatFolder = folderTemp.mkdirs();

                if (!buatFolder) {
                    System.out.println("Gagal membuat folder temporary: " + folderTemp.getAbsolutePath());
                    return new int[]{sukses, gagal + 1};
                }
            }

            String noRm = Sequel.cariIsi(
                    "SELECT IFNULL(no_rkm_medis,'') "
                    + "FROM reg_periksa "
                    + "WHERE no_rawat=?",
                    norawatslash
            );

            String namaPasien = Sequel.cariIsi(
                    "SELECT IFNULL(nm_pasien,'') "
                    + "FROM pasien "
                    + "WHERE no_rkm_medis=?",
                    noRm
            );

            String namaPenjab = Sequel.cariIsi(
                    "SELECT IFNULL(penjab.png_jawab,'') "
                    + "FROM reg_periksa "
                    + "INNER JOIN penjab ON reg_periksa.kd_pj = penjab.kd_pj "
                    + "WHERE reg_periksa.no_rawat=?",
                    norawatslash
            );

            if (namaPenjab == null || namaPenjab.trim().equals("")) {
                namaPenjab = "UMUM";
            }

            if (namaPasien == null || namaPasien.trim().equals("")) {
                namaPasien = "TANPA_NAMA";
            }

            String prefixPenjab = bersihkanNamaFile(namaPenjab);
            String prefixNoRawat = bersihkanNamaFile(norawatslash);
            String prefixPasien = bersihkanNamaFile(namaPasien);

            String prefixFile = prefixPenjab + "_" + prefixNoRawat + "_" + prefixPasien;

            System.out.println("No RM       : " + noRm);
            System.out.println("Nama Pasien : " + namaPasien);
            System.out.println("Penjab      : " + namaPenjab);
            System.out.println("Prefix File : " + prefixFile);

            headers = new HttpHeaders();
            headers.add("Authorization", "Basic " + authEncrypt);
            requestEntity = new HttpEntity(headers);

            String urlSeries = koneksiDB.URLORTHANC() + ":"
                    + koneksiDB.PORTORTHANC()
                    + "/series/"
                    + Series;

            System.out.println("URL Series : " + urlSeries);

            String jsonSeries = getRest().exchange(
                    urlSeries,
                    HttpMethod.GET,
                    requestEntity,
                    String.class
            ).getBody();

            System.out.println("Result JSON Series : " + jsonSeries);

            JsonNode rootSeries = mapper.readTree(jsonSeries);

            int urut = urutAwal;

            for (JsonNode list : rootSeries.path("Instances")) {
                String instanceId = list.asText();

                String urlPreview = koneksiDB.URLORTHANC() + ":"
                        + koneksiDB.PORTORTHANC()
                        + "/instances/"
                        + instanceId
                        + "/preview";

                System.out.println("Mengambil Gambar JPG : " + urlPreview);

                headers = new HttpHeaders();
                headers.add("Authorization", "Basic " + authEncrypt);
                headers.setAccept(Collections.singletonList(MediaType.IMAGE_JPEG));

                HttpEntity<String> entity = new HttpEntity<>(headers);

                ResponseEntity<byte[]> response = getRest().exchange(
                        urlPreview,
                        HttpMethod.GET,
                        entity,
                        byte[].class
                );

                if (response.getBody() == null || response.getBody().length == 0) {
                    System.out.println("Gambar kosong dari Orthanc. Instance: " + instanceId);
                    gagal++;
                    urut++;
                    continue;
                }

                String tanggalFile = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

                String random4 = UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 4);

                String namaFileUnik = prefixFile
                        + "_"
                        + tanggalFile
                        + "_"
                        + random4
                        + "_"
                        + urut
                        + ".jpg";

                File fileJpg = new File(folderTemp, namaFileUnik);

                Files.write(fileJpg.toPath(), response.getBody());

                if (!fileJpg.exists() || fileJpg.length() == 0) {
                    System.out.println("File lokal gagal dibuat / kosong: " + fileJpg.getAbsolutePath());
                    gagal++;
                    urut++;
                    continue;
                }

                System.out.println("File lokal berhasil dibuat:");
                System.out.println("  Path : " + fileJpg.getAbsolutePath());
                System.out.println("  Size : " + fileJpg.length());
                System.out.println("  Nama : " + namaFileUnik);

                boolean uploadSukses = uploadImageRadiologiFix(namaFileUnik, "pages/upload");

                if (!uploadSukses) {
                    System.out.println("Upload ke web gagal: " + namaFileUnik);
                    gagal++;
                    urut++;
                    continue;
                }

                String lokasiGambar = "pages/upload/" + namaFileUnik;

                System.out.println("Menyimpan ke DB:");
                System.out.println("  no_rawat      : " + norawatslash);
                System.out.println("  tgl_periksa   : " + tanggalPeriksa);
                System.out.println("  jam           : " + jamPeriksa);
                System.out.println("  lokasi_gambar : " + lokasiGambar);

                Sequel.menyimpantf(
                        "gambar_radiologi",
                        "?,?,?,?",
                        "No.Rawat",
                        4,
                        new String[]{
                            norawatslash,
                            tanggalPeriksa,
                            jamPeriksa,
                            lokasiGambar
                        }
                );

                sukses++;

                try {
                    if (fileJpg.exists()) {
                        fileJpg.delete();
                    }
                } catch (Exception ex) {
                    System.out.println("Gagal hapus temporary: " + ex);
                }

                urut++;
            }

        } catch (Exception e) {
            System.out.println("Notifikasi AmbilJpgPerSeries : " + e);
            e.printStackTrace();
            gagal++;
        }

        return new int[]{sukses, gagal};
    }

    public JsonNode AmbilPngUsg(String NoRawat, String Series, String norawatslash) {
        System.out.println("Percobaan Mengambil Gambar PNG : " + NoRawat + ", Series : " + Series);
        try {
            headers = new HttpHeaders();
            System.out.println("Auth : " + authEncrypt);
            headers.add("Authorization", "Basic " + authEncrypt);
            requestEntity = new HttpEntity(headers);
            System.out.println("URL : " + koneksiDB.URLORTHANC() + ":" + koneksiDB.PORTORTHANC() + "/series/" + Series);
            requestJson = getRest()
                    .exchange(koneksiDB.URLORTHANC() + ":" + koneksiDB.PORTORTHANC() + "/series/" + Series,
                            HttpMethod.GET, requestEntity, String.class)
                    .getBody();
            System.out.println("Result JSON : " + requestJson);
            root = mapper.readTree(requestJson);
            i = 1;
            for (JsonNode list : root.path("Instances")) {
                System.out.println("Mengambil Gambar PNG " + koneksiDB.URLORTHANC() + ":" + koneksiDB.PORTORTHANC()
                        + "/instances/" + list.asText() + "/preview");
                headers = new HttpHeaders();
                headers.add("Authorization", "Basic " + authEncrypt);
                headers.add("Accept", "image/png");
                headers.setAccept(Collections.singletonList(MediaType.APPLICATION_OCTET_STREAM));
                headers.setAccept(Collections.singletonList(MediaType.IMAGE_JPEG));
                HttpEntity<String> entity = new HttpEntity<>(headers);
                ResponseEntity<byte[]> response = getRest().exchange(koneksiDB.URLORTHANC() + ":"
                        + koneksiDB.PORTORTHANC() + "/instances/" + list.asText() + "/preview", HttpMethod.GET, entity,
                        byte[].class);
                Files.write(Paths.get("./gambarradiologi/" + NoRawat + i + ".png"), response.getBody());
                // Menambahkan fitur simpan gambar radiologi dari orthanc
                uploadImageUsg(NoRawat + i + ".png", "pages/upload");
                Sequel.menyimpantf("hasil_pemeriksaan_usg_gambar", "?,?", "No.Rawat", 2, new String[]{
                    norawatslash, "pages/upload/" + NoRawat + i + ".png"
                });
                i++;
            }
            JOptionPane.showMessageDialog(null, "Penyimpanan Gambar PNG dari Orthanc ke Webapps berhasil");
        } catch (Exception e) {
            System.out.println("Notifikasi : " + e);
            JOptionPane.showMessageDialog(null,
                    "Gagal mengambil Gambar PNG dari Orthanc, silahkan hubungi administrator ..!!");
        }
        return root;
    }

    public JsonNode AmbilJpgUsg(String NoRawat, String Series, String norawatslash) {

        System.out.println("Percobaan Mengambil Gambar JPG : " + NoRawat + ", Series : " + Series);
        try {
            headers = new HttpHeaders();
            System.out.println("Auth : " + authEncrypt);
            headers.add("Authorization", "Basic " + authEncrypt);
            requestEntity = new HttpEntity(headers);
            System.out.println("URL : " + koneksiDB.URLORTHANC() + ":" + koneksiDB.PORTORTHANC() + "/series/" + Series);
            requestJson = getRest()
                    .exchange(koneksiDB.URLORTHANC() + ":" + koneksiDB.PORTORTHANC() + "/series/" + Series,
                            HttpMethod.GET, requestEntity, String.class)
                    .getBody();
            System.out.println("Result JSON : " + requestJson);
            root = mapper.readTree(requestJson);
            i = 1;
            for (JsonNode list : root.path("Instances")) {
                System.out.println("Mengambil Gambar JPG " + koneksiDB.URLORTHANC() + ":" + koneksiDB.PORTORTHANC()
                        + "/instances/" + list.asText() + "/preview");
                headers = new HttpHeaders();
                headers.add("Authorization", "Basic " + authEncrypt);
                headers.add("Accept", "image/jpeg");
                headers.setAccept(Collections.singletonList(MediaType.APPLICATION_OCTET_STREAM));
                headers.setAccept(Collections.singletonList(MediaType.IMAGE_JPEG));
                HttpEntity<String> entity = new HttpEntity<>(headers);
                ResponseEntity<byte[]> response = getRest().exchange(koneksiDB.URLORTHANC() + ":"
                        + koneksiDB.PORTORTHANC() + "/instances/" + list.asText() + "/preview", HttpMethod.GET, entity,
                        byte[].class);
                String uniqueName = NoRawat + "_" + System.currentTimeMillis() + "_" + i + ".jpg";
                Files.write(Paths.get("./gambarradiologi/" + uniqueName), response.getBody());

                uploadImageUsg(uniqueName, "pages/upload");
                Sequel.menyimpantf("hasil_pemeriksaan_usg_gambar", "?,?", "No.Rawat", 2, new String[]{
                    norawatslash, "pages/upload/" + uniqueName
                });
                i++;
            }
            JOptionPane.showMessageDialog(null, "Penyimpanan Gambar JPG dari Orthanc berhasil");
        } catch (Exception e) {
            System.out.println("Notifikasi : " + e);
            JOptionPane.showMessageDialog(null,
                    "Gagal mengambil Gambar JPG dari Orthanc, silahkan hubungi administrator ..!!");
        }
        return root;
    }

    void uploadImageUsg(String FileName, String docpath) {
        try {
            File file = new File("gambarradiologi/" + FileName);
            byte[] data = new byte[(int) file.length()];
            data = FileUtils.readFileToByteArray(file);
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost postRequest = new HttpPost("http://" + koneksiDB.HOSTHYBRIDWEB() + ":" + koneksiDB.PORTWEB() + "/"
                    + koneksiDB.HYBRIDWEB() + "/hasilpemeriksaanusg/upload.php?doc=" + docpath);
            ByteArrayBody fileData = new ByteArrayBody(data, FileName);
            MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            reqEntity.addPart("file", fileData);
            postRequest.setEntity(reqEntity);
            httpClient.execute(postRequest);
            deleteFile();
        } catch (Exception e) {
            System.out.println("Upload error" + e);
        }
    }

    public JsonNode AmbilJpgUsg2(String NoRawat, String Series, String norawatslash) {

        System.out.println("Percobaan Mengambil Gambar JPG : " + NoRawat + ", Series : " + Series);
        try {
            headers = new HttpHeaders();
            System.out.println("Auth : " + authEncrypt);
            headers.add("Authorization", "Basic " + authEncrypt);
            requestEntity = new HttpEntity(headers);
            System.out.println("URL : " + koneksiDB.URLORTHANC() + ":" + koneksiDB.PORTORTHANC() + "/series/" + Series);
            requestJson = getRest()
                    .exchange(koneksiDB.URLORTHANC() + ":" + koneksiDB.PORTORTHANC() + "/series/" + Series,
                            HttpMethod.GET, requestEntity, String.class)
                    .getBody();
            System.out.println("Result JSON : " + requestJson);
            root = mapper.readTree(requestJson);
            i = 1;
            for (JsonNode list : root.path("Instances")) {
                System.out.println("Mengambil Gambar JPG " + koneksiDB.URLORTHANC() + ":" + koneksiDB.PORTORTHANC()
                        + "/instances/" + list.asText() + "/preview");
                headers = new HttpHeaders();
                headers.add("Authorization", "Basic " + authEncrypt);
                headers.add("Accept", "image/jpeg");
                headers.setAccept(Collections.singletonList(MediaType.APPLICATION_OCTET_STREAM));
                headers.setAccept(Collections.singletonList(MediaType.IMAGE_JPEG));
                HttpEntity<String> entity = new HttpEntity<>(headers);
                ResponseEntity<byte[]> response = getRest().exchange(koneksiDB.URLORTHANC() + ":"
                        + koneksiDB.PORTORTHANC() + "/instances/" + list.asText() + "/preview", HttpMethod.GET, entity,
                        byte[].class);
                Files.write(Paths.get("./gambarradiologi/" + NoRawat + i + ".jpg"), response.getBody());

                uploadImageUsg2(NoRawat + i + ".jpg", "pages/upload");
                Sequel.menyimpantf("hasil_pemeriksaan_usg_gynecologi_gambar", "?,?", "No.Rawat", 2, new String[]{
                    norawatslash, "pages/upload/" + NoRawat + i + ".jpg"
                });
                i++;
            }
            JOptionPane.showMessageDialog(null, "Penyimpanan Gambar JPG dari Orthanc berhasil");
        } catch (Exception e) {
            System.out.println("Notifikasi : " + e);
            JOptionPane.showMessageDialog(null,
                    "Gagal mengambil Gambar JPG dari Orthanc, silahkan hubungi administrator ..!!");
        }
        return root;
    }

    void uploadImageUsg2(String FileName, String docpath) {
        try {
            File file = new File("gambarradiologi/" + FileName);
            byte[] data = new byte[(int) file.length()];
            data = FileUtils.readFileToByteArray(file);
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost postRequest = new HttpPost("http://" + koneksiDB.HOSTHYBRIDWEB() + ":" + koneksiDB.PORTWEB() + "/"
                    + koneksiDB.HYBRIDWEB() + "/hasilpemeriksaanusggynecologi/upload.php?doc=" + docpath);
            ByteArrayBody fileData = new ByteArrayBody(data, FileName);
            MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            reqEntity.addPart("file", fileData);
            postRequest.setEntity(reqEntity);
            httpClient.execute(postRequest);
            deleteFile();
        } catch (Exception e) {
            System.out.println("Upload error" + e);
        }
    }

    void uploadImageRadiologi(String FileName, String docpath) {
        try {
            File file = new File("gambarradiologi/" + FileName);
            byte[] data = FileUtils.readFileToByteArray(file);
            HttpClient httpClient = new DefaultHttpClient();

            // Samakan URL dengan uploadImageUsg, hanya beda folder (radiologi bukan hasilpemeriksaanusg)
            HttpPost postRequest = new HttpPost("http://" + koneksiDB.HOSTHYBRIDWEB() + ":" + koneksiDB.PORTWEB() + "/"
                    + koneksiDB.HYBRIDWEB() + "/radiologi/upload.php?doc=" + docpath + "/");
            //                                                                                            ↑ tambah slash di sini
            ByteArrayBody fileData = new ByteArrayBody(data, FileName);
            MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            reqEntity.addPart("file", fileData);
            postRequest.setEntity(reqEntity);
            httpClient.execute(postRequest);
            deleteFile();
        } catch (Exception e) {
            System.out.println("Upload Radiologi error: " + e);
        }
    }

    public String UbahAccession(String studyId, String accessionBaru) {
        System.out.println("Inject AccessionNumber: " + accessionBaru + " ke Study: " + studyId);
        try {
            headers = new HttpHeaders();
            headers.add("Authorization", "Basic " + authEncrypt);
            headers.setContentType(MediaType.APPLICATION_JSON);
            requestJson = "{"
                    + "\"Replace\": {"
                    + "\"AccessionNumber\": \"" + accessionBaru + "\""
                    + "},"
                    + "\"KeepSource\": false"
                    + "}";
            System.out.println("Request JSON : " + requestJson);
            requestEntity = new HttpEntity(requestJson, headers);
            System.out.println("URL : " + koneksiDB.URLORTHANC() + ":" + koneksiDB.PORTORTHANC() + "/studies/" + studyId
                    + "/modify");
            String response = getRest().exchange(
                    koneksiDB.URLORTHANC() + ":" + koneksiDB.PORTORTHANC() + "/studies/" + studyId + "/modify",
                    HttpMethod.POST, requestEntity, String.class).getBody();
            System.out.println("Response : " + response);
            // /modify returns a new Study ID (KeepSource: false deletes the old one)
            JsonNode resp = mapper.readTree(response);
            String newStudyId = resp.path("ID").asText();
            System.out.println("New Study ID setelah inject: " + newStudyId);
            return newStudyId;
        } catch (Exception e) {
            System.out.println("Notifikasi UbahAccession : " + e);
            return "";
        }
    }

    public void KirimDicom(String Series, String ModalityName) {
        try {
            headers = new HttpHeaders();
            headers.add("Authorization", "Basic " + authEncrypt);
            requestJson = "{\"Resources\": [\"" + Series + "\"]}";
            requestEntity = new HttpEntity(requestJson, headers);
            getRest().exchange(
                    koneksiDB.URLORTHANC() + ":" + koneksiDB.PORTORTHANC() + "/modalities/" + ModalityName + "/store",
                    HttpMethod.POST, requestEntity, String.class);
        } catch (Exception e) {
            System.out.println("Notifikasi : " + e);
        }
    }

    public boolean kirimKeModality(String studyId) {
        System.out.println("Kirim Study ke Modality : " + studyId);
        try {
            headers = new HttpHeaders();
            headers.add("Authorization", "Basic " + authEncrypt);
            headers.setContentType(MediaType.APPLICATION_JSON);
            requestJson = "[\"" + studyId + "\"]";
            requestEntity = new HttpEntity(requestJson, headers);
            System.out.println("URL : " + koneksiDB.URLORTHANC() + ":" + koneksiDB.PORTORTHANC() + "/modalities/DCMROUTER/store");
            System.out.println("Request JSON : " + requestJson);
            String response = getRest().exchange(koneksiDB.URLORTHANC() + ":" + koneksiDB.PORTORTHANC() + "/modalities/DCMROUTER/store", HttpMethod.POST, requestEntity, String.class).getBody();
            System.out.println("Response : " + response);
            JOptionPane.showMessageDialog(null, "Proses kirim ke Modality selesai..!!");
            return true;
        } catch (Exception e) {
            System.out.println("Notifikasi : " + e);
            JOptionPane.showMessageDialog(null, "Gagal kirim ke Modality..!!");
            return false;
        }
    }

    public String getModality(String nmPemeriksaan) {
        String modality = "OT"; // Default Other
        String name = nmPemeriksaan.toUpperCase();
        if (name.contains("USG") || name.contains("ULTRASONO")) {
            modality = "US";
        } else if (name.contains("CR") || name.contains("DX") || name.contains("RONTGEN") || name.contains("ROENTGEN")
                || name.contains("THORAX") || name.contains("TORAKS") || name.contains("FOTO") || name.contains("PHOTO")
                || name.contains("PANORAMIC") || name.contains("RADIOGRAFI") || name.contains("RADIOGRAPHY")) {
            modality = "XR";
        } else if (name.contains("CT") || name.contains("MSCT") || name.contains("SCAN")) {
            modality = "CT";
        } else if (name.contains("MRI") || name.contains("MRA") || name.contains("MRCP")) {
            modality = "MR";
        } else if (name.contains("EKG") || name.contains("ECG") || name.contains("ELEKTROKAR")) {
            modality = "ECG";
        }
        return modality;
    }

    public String KirimKeOrthanc(
            String noRawat,
            String nmPasien,
            String noRm,
            String tglLahir,
            String jk,
            String accession,
            String fileURL,
            String tglPasien,
            String studyDesc,
            String seriesDesc,
            String modality,
            String instanceNum
    ) {
        System.out.println("Kirim Gambar ke Orthanc: " + fileURL);

        File localFile = null;

        try {
            // 1. Pastikan folder temporary tersedia
            File folderTemp = new File("./gambarradiologi");

            if (!folderTemp.exists()) {
                boolean berhasilBuatFolder = folderTemp.mkdirs();

                if (!berhasilBuatFolder) {
                    System.out.println("Gagal membuat folder temporary: " + folderTemp.getAbsolutePath());
                    return "";
                }
            }

            // 2. Buat nama file lokal yang aman
            String noRawatBersih = noRawat.replaceAll("[^A-Za-z0-9]", "");

            if (noRawatBersih.equals("")) {
                noRawatBersih = "NORAWAT";
            }

            String fileName = noRawatBersih + "_" + System.currentTimeMillis() + "_" + instanceNum + ".jpg";
            localFile = new File(folderTemp, fileName);

            // 3. Download file JPG dari web server
            headers = new HttpHeaders();
            requestEntity = new HttpEntity(headers);

            ResponseEntity<byte[]> responseFile = getRest().exchange(
                    fileURL,
                    HttpMethod.GET,
                    requestEntity,
                    byte[].class
            );

            if (responseFile.getBody() == null || responseFile.getBody().length == 0) {
                System.out.println("File gambar kosong atau gagal didownload dari URL: " + fileURL);
                return "";
            }

            Files.write(localFile.toPath(), responseFile.getBody());

            if (!localFile.exists()) {
                System.out.println("File lokal tidak ditemukan setelah download: " + localFile.getAbsolutePath());
                return "";
            }

            if (localFile.length() == 0) {
                System.out.println("File lokal kosong: " + localFile.getAbsolutePath());
                return "";
            }

            System.out.println("File berhasil didownload ke: " + localFile.getAbsolutePath());

            // 4. Baca file dan konversi ke Base64
            byte[] fileContent = FileUtils.readFileToByteArray(localFile);
            String encodedString = Base64.encodeBase64String(fileContent);

            // 5. Bersihkan accession untuk UID
            String cleanAccession = accession == null ? "" : accession.replaceAll("[^0-9]", "");

            if (cleanAccession.isEmpty()) {
                cleanAccession = noRawatBersih;
            }

            if (cleanAccession.isEmpty()) {
                cleanAccession = String.valueOf(System.currentTimeMillis());
            }

            String instanceNumBersih = instanceNum == null ? "1" : instanceNum.replaceAll("[^0-9]", "");

            if (instanceNumBersih.equals("")) {
                instanceNumBersih = "1";
            }

            String studyUID = "1.2.826.0.1.3680043.2." + cleanAccession;
            String seriesUID = studyUID + ".1";
            String sopUID = seriesUID + "." + instanceNumBersih;

            // 6. Header Orthanc
            headers = new HttpHeaders();
            headers.add("Authorization", "Basic " + authEncrypt);
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 7. Tags DICOM
            Map<String, Object> dicomData = new HashMap<>();
            Map<String, Object> tags = new HashMap<>();

            tags.put("PatientID", noRm);
            tags.put("PatientName", nmPasien);
            tags.put("PatientBirthDate", tglLahir == null ? "" : tglLahir.replaceAll("-", ""));
            tags.put("PatientSex", "L".equalsIgnoreCase(jk) ? "M" : "F");
            tags.put("AccessionNumber", accession);
            tags.put("StudyInstanceUID", studyUID);
            tags.put("SeriesInstanceUID", seriesUID);

            // SOPInstanceUID bisa diisi agar tiap gambar unik.
            // Kalau mau Orthanc otomatis, baris ini boleh dikomentari.
            tags.put("SOPInstanceUID", sopUID);

            tags.put("StudyDate", tglPasien == null ? "" : tglPasien.replaceAll("-", ""));
            tags.put("StudyDescription", studyDesc);
            tags.put("Modality", modality);
            tags.put("SeriesDescription", seriesDesc);
            tags.put("SeriesNumber", "1");
            tags.put("InstanceNumber", instanceNumBersih);
            tags.put("Manufacturer", "SIMRS Khanza");
            tags.put("InstitutionName", akses.getnamars());

            dicomData.put("Tags", tags);
            dicomData.put("Content", "data:image/jpeg;base64," + encodedString);
            dicomData.put("Force", true);

            requestJson = mapper.writeValueAsString(dicomData);

            // Jangan print JSON penuh karena base64 sangat panjang
            System.out.println("JSON Request Orthanc siap dikirim. File size: " + localFile.length() + " bytes");

            requestEntity = new HttpEntity(requestJson, headers);

            String response = "";

            try {
                response = getRest().exchange(
                        koneksiDB.URLORTHANC() + ":" + koneksiDB.PORTORTHANC() + "/tools/create-dicom",
                        HttpMethod.POST,
                        requestEntity,
                        String.class
                ).getBody();
            } catch (org.springframework.web.client.HttpClientErrorException e) {
                System.out.println("Detail Error Orthanc: " + e.getResponseBodyAsString());
                throw e;
            }

            System.out.println("Response Upload Orthanc: " + response);

            if (response == null || response.equals("")) {
                System.out.println("Response Orthanc kosong.");
                return "";
            }

            JsonNode resp = mapper.readTree(response);
            return resp.path("ID").asText();

        } catch (Exception e) {
            System.out.println("Notifikasi KirimKeOrthanc: " + e);
            e.printStackTrace();
            return "";

        } finally {
            // 8. Hapus file temporary
            try {
                if (localFile != null && localFile.exists()) {
                    localFile.delete();
                }
            } catch (Exception e) {
                System.out.println("Gagal hapus file temporary: " + e);
            }
        }
    }

    void deleteFile() {
        File file = new File("gambarradiologi");
        String[] myFiles;
        if (file.isDirectory()) {
            myFiles = file.list();
            for (int i = 0; i < myFiles.length; i++) {
                File myFile = new File(file, myFiles[i]);
                myFile.delete();
            }
        }
    }

    //TAMBAH WATERMARK
    private boolean tambahkanWatermarkRadiologi(
            File fileJpg,
            String namaPasien,
            String jk,
            String umur,
            String noRm,
            String pemeriksaan,
            String namaRs,
            String tanggalPeriksa,
            String jamPeriksa,
            String nilaiTambahan
    ) {
        try {
            BufferedImage image = ImageIO.read(fileJpg);

            if (image == null) {
                System.out.println("Gagal membaca file JPG untuk watermark: " + fileJpg.getAbsolutePath());
                return false;
            }

            Graphics2D g2d = image.createGraphics();

            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            int width = image.getWidth();

            int fontSize = Math.max(16, width / 45);
            int lineHeight = fontSize + 8;
            int margin = 15;

            Font font = new Font("Tahoma", Font.BOLD, fontSize);
            g2d.setFont(font);

            Color textColor = Color.WHITE;
            Color shadowColor = new Color(0, 0, 0, 190);

            java.util.List<String> kiri = new java.util.ArrayList<>();
            kiri.add(nilaiAtauStrip(namaPasien).toUpperCase());
            kiri.add(nilaiAtauStrip(jk).toUpperCase());
            kiri.add(nilaiAtauStrip(umur));
            kiri.add(nilaiAtauStrip(noRm));
            kiri.add(nilaiAtauStrip(pemeriksaan));

            java.util.List<String> kanan = new java.util.ArrayList<>();
            kanan.add(nilaiAtauStrip(namaRs).toUpperCase());
            kanan.add(nilaiAtauStrip(tanggalPeriksa));
            kanan.add(nilaiAtauStrip(jamPeriksa));

            if (nilaiTambahan != null && !nilaiTambahan.trim().equals("")) {
                kanan.add(nilaiTambahan);
            }

            int y = margin + fontSize;

            for (String teks : kiri) {
                gambarTeksDenganBayangan(g2d, teks, margin, y, textColor, shadowColor);
                y += lineHeight;
            }

            y = margin + fontSize;

            FontMetrics fm = g2d.getFontMetrics();

            for (String teks : kanan) {
                int textWidth = fm.stringWidth(teks);
                int x = width - textWidth - margin;

                gambarTeksDenganBayangan(g2d, teks, x, y, textColor, shadowColor);
                y += lineHeight;
            }

            g2d.dispose();

            ImageIO.write(image, "jpg", fileJpg);

            System.out.println("Watermark berhasil ditambahkan: " + fileJpg.getAbsolutePath());
            return true;

        } catch (Exception e) {
            System.out.println("Gagal tambahkan watermark: " + e);
            e.printStackTrace();
            return false;
        }
    }

    private void gambarTeksDenganBayangan(
            Graphics2D g2d,
            String teks,
            int x,
            int y,
            Color warnaTeks,
            Color warnaBayangan
    ) {
        g2d.setColor(warnaBayangan);
        g2d.drawString(teks, x + 2, y + 2);

        g2d.setColor(warnaTeks);
        g2d.drawString(teks, x, y);
    }

    private String hitungUmurTahun(String tglLahir) {
        try {
            if (tglLahir == null || tglLahir.trim().equals("")) {
                return "-";
            }

            LocalDate lahir = LocalDate.parse(tglLahir);
            LocalDate sekarang = LocalDate.now();

            int umur = Period.between(lahir, sekarang).getYears();

            return umur + "Y";

        } catch (Exception e) {
            System.out.println("Gagal hitung umur: " + e);
            return "-";
        }
    }

    private JsonNode ambilSimplifiedTagsInstance(String instanceId) {
        try {
            headers = new HttpHeaders();
            headers.add("Authorization", "Basic " + authEncrypt);
            requestEntity = new HttpEntity(headers);

            String url = koneksiDB.URLORTHANC() + ":"
                    + koneksiDB.PORTORTHANC()
                    + "/instances/"
                    + instanceId
                    + "/simplified-tags";

            System.out.println("Ambil simplified-tags : " + url);

            String hasil = getRest().exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    String.class
            ).getBody();

            System.out.println("Result simplified-tags : " + hasil);

            return mapper.readTree(hasil);

        } catch (Exception e) {
            System.out.println("Gagal ambil simplified-tags: " + e);
            return null;
        }
    }

    private String jkDariDatabase(String noRawat) {
        try {
            String jk = Sequel.cariIsi(
                    "SELECT IFNULL(pasien.jk,'') "
                    + "FROM reg_periksa "
                    + "INNER JOIN pasien ON reg_periksa.no_rkm_medis = pasien.no_rkm_medis "
                    + "WHERE reg_periksa.no_rawat=?",
                    noRawat
            );

            if ("L".equalsIgnoreCase(jk)) {
                return "M";
            } else if ("P".equalsIgnoreCase(jk)) {
                return "F";
            }

            return "-";
        } catch (Exception e) {
            System.out.println("Gagal ambil jk dari database: " + e);
            return "-";
        }
    }

    private String convertJKUntukWatermark(String jk) {
        if (jk == null) {
            return "-";
        }

        if ("L".equalsIgnoreCase(jk)) {
            return "M";
        }

        if ("P".equalsIgnoreCase(jk)) {
            return "F";
        }

        if ("M".equalsIgnoreCase(jk)) {
            return "M";
        }

        if ("F".equalsIgnoreCase(jk)) {
            return "F";
        }

        return "-";
    }

    private String hitungUmurTahunDariDicom(String tglLahirDicom) {
        try {
            if (tglLahirDicom == null || tglLahirDicom.trim().equals("")) {
                return "-";
            }

            LocalDate lahir = LocalDate.parse(
                    tglLahirDicom,
                    DateTimeFormatter.ofPattern("yyyyMMdd")
            );

            LocalDate sekarang = LocalDate.now();

            int umur = Period.between(lahir, sekarang).getYears();

            return umur + "Y";

        } catch (Exception e) {
            System.out.println("Gagal hitung umur DICOM: " + e);
            return "-";
        }
    }

    private String nilaiAtauStrip(String teks) {
        if (teks == null || teks.trim().equals("")) {
            return "-";
        }

        return teks.trim();
    }
}
