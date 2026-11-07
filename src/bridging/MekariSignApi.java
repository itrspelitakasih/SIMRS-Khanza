package bridging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fungsi.koneksiDB;
import fungsi.koneksiDBMekariSign;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

/**
 * Bridging TTE MekariSign (https://api-docs.esign.mekari.com).
 *
 * Alur: PDF berisi keyword (#1A, sama dengan Sertisign) → posisi keyword dicari dengan PDFBox lalu
 * ditutup kotak putih → dikirim ke request_psre_sign_only / request_global_sign_only dengan
 * annotation di posisi itu → id dokumen dicatat di tabel mekarisign_dokumen → status dicek dan
 * PDF bertanda tangan diunduh dari modul MekariSignDokumen.
 *
 * Autentikasi: HMAC (CLIENTIDMEKARISIGN + CLIENTSECRETMEKARISIGN) bila diisi, selain itu Bearer
 * APIKEYMEKARISIGN. Lihat {@link koneksiDBMekariSign}.
 */
public class MekariSignApi {
    public static final String KEYWORD = "#1A";
    /** Ukuran kotak tanda tangan dalam point PDF. */
    private static final float LEBAR_TTD = 60f, TINGGI_TTD = 60f;

    private static final ObjectMapper mapper = new ObjectMapper();
    private static boolean tabelSiap = false;

    /** Gagal dari API / validasi, pesannya siap ditampilkan ke user. */
    public static class MekariSignException extends Exception {
        public final int status;
        public MekariSignException(int status, String pesan) {
            super(pesan);
            this.status = status;
        }
    }

    /** Posisi satu keyword di PDF, koordinat dari pojok kiri atas halaman (point). */
    public static class Posisi {
        public int halaman;
        public float x, y, lebar, tinggi, lebarHalaman, tinggiHalaman;
    }

    /** Hasil kirim / baca dokumen. */
    public static class Dokumen {
        public String id = "", namaFile = "", kategori = "", status = "", signingUrl = "";
    }

    // ------------------------------------------------------------------ tabel

    public static synchronized void siapkanTabel(Connection koneksi) {
        if (tabelSiap) {
            return;
        }
        try (Statement st = koneksi.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS `mekarisign_dokumen` ("+
                "`id_dokumen` varchar(40) NOT NULL,"+
                "`no_rawat` varchar(17) NOT NULL,"+
                "`jenis_dokumen` varchar(60) NOT NULL,"+
                "`nama_file` varchar(150) NOT NULL,"+
                "`kd_dokter` varchar(20) NOT NULL,"+
                "`email` varchar(100) NOT NULL,"+
                "`kategori` varchar(10) NOT NULL,"+
                "`tgl_kirim` datetime NOT NULL,"+
                "`status` varchar(20) NOT NULL,"+
                "`tgl_update` datetime DEFAULT NULL,"+
                "PRIMARY KEY (`id_dokumen`),"+
                "KEY `no_rawat` (`no_rawat`),"+
                "KEY `tgl_kirim` (`tgl_kirim`)"+
                ") ENGINE=InnoDB DEFAULT CHARSET=latin1");
            tabelSiap = true;
        } catch (Exception e) {
            System.out.println("Notifikasi MekariSign : gagal membuat tabel mekarisign_dokumen "+e);
        }
    }

    private static void catat(String noRawat, String jenis, String kdDokter, String email, Dokumen d) {
        Connection koneksi = koneksiDB.condb();
        siapkanTabel(koneksi);
        try (PreparedStatement ps = koneksi.prepareStatement(
                "insert into mekarisign_dokumen values(?,?,?,?,?,?,?,now(),?,now())")) {
            ps.setString(1, d.id);
            ps.setString(2, noRawat);
            ps.setString(3, jenis);
            ps.setString(4, d.namaFile);
            ps.setString(5, kdDokter);
            ps.setString(6, email);
            ps.setString(7, d.kategori);
            ps.setString(8, d.status);
            ps.executeUpdate();
        } catch (Exception e) {
            System.out.println("Notifikasi MekariSign : gagal mencatat dokumen "+d.id+" "+e);
        }
    }

    public static void updateStatus(String id, String status) {
        Connection koneksi = koneksiDB.condb();
        siapkanTabel(koneksi);
        try (PreparedStatement ps = koneksi.prepareStatement(
                "update mekarisign_dokumen set status=?,tgl_update=now() where id_dokumen=?")) {
            ps.setString(1, status);
            ps.setString(2, id);
            ps.executeUpdate();
        } catch (Exception e) {
            System.out.println("Notifikasi MekariSign : gagal update status "+id+" "+e);
        }
    }

    // ------------------------------------------------------------------ PDF

    /** Cari semua kemunculan keyword di PDF. */
    public static List<Posisi> cariKeyword(PDDocument pdf, String keyword) throws IOException {
        List<Posisi> hasil = new ArrayList<>();
        PDFTextStripper stripper = new PDFTextStripper() {
            @Override
            protected void writeString(String text, List<TextPosition> posisi) throws IOException {
                int mulai = text.indexOf(keyword);
                while (mulai >= 0 && text.length() == posisi.size()) {
                    TextPosition awal = posisi.get(mulai);
                    TextPosition akhir = posisi.get(mulai+keyword.length()-1);
                    PDRectangle kotak = getCurrentPage().getCropBox();
                    Posisi p = new Posisi();
                    p.halaman = getCurrentPageNo();
                    p.x = awal.getXDirAdj();
                    p.lebar = akhir.getXDirAdj()+akhir.getWidthDirAdj()-p.x;
                    p.tinggi = Math.max(awal.getHeightDir(), 6f);
                    p.y = awal.getYDirAdj()-p.tinggi;
                    p.lebarHalaman = kotak.getWidth();
                    p.tinggiHalaman = kotak.getHeight();
                    hasil.add(p);
                    mulai = text.indexOf(keyword, mulai+keyword.length());
                }
                super.writeString(text, posisi);
            }
        };
        stripper.setSortByPosition(true);
        stripper.getText(pdf);
        return hasil;
    }

    /** Tutup tulisan keyword dengan kotak putih supaya tidak tercetak di bawah tanda tangan. */
    private static void tutupKeyword(PDDocument pdf, List<Posisi> daftar) throws IOException {
        for (Posisi p : daftar) {
            PDPage page = pdf.getPage(p.halaman-1);
            try (PDPageContentStream cs = new PDPageContentStream(pdf, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
                cs.setNonStrokingColor(Color.WHITE);
                cs.addRect(p.x-1, p.tinggiHalaman-(p.y+p.tinggi)-2, p.lebar+2, p.tinggi+4);
                cs.fill();
            }
        }
    }

    private static ObjectNode annotation(Posisi p) {
        float w = Math.min(LEBAR_TTD, p.lebarHalaman), h = Math.min(TINGGI_TTD, p.tinggiHalaman);
        float x = p.x+p.lebar/2-w/2, y = p.y+p.tinggi/2-h/2;
        x = Math.max(0, Math.min(x, p.lebarHalaman-w));
        y = Math.max(0, Math.min(y, p.tinggiHalaman-h));
        ObjectNode a = mapper.createObjectNode();
        a.put("type_of", "signature");
        // default QR code (seperti Sertisign); "bebas" = penanda tangan / auto sign memilih sendiri
        String tipe = koneksiDBMekariSign.TIPETTDMEKARISIGN();
        if (!tipe.equals("bebas")) {
            a.put("signature_type", tipe);
        }
        a.put("page", p.halaman);
        a.put("position_x", Math.floor(x));
        a.put("position_y", Math.floor(y));
        a.put("element_width", Math.floor(w));
        a.put("element_height", Math.floor(h));
        a.put("canvas_width", Math.floor(p.lebarHalaman));
        a.put("canvas_height", Math.floor(p.tinggiHalaman));
        return a;
    }

    // ------------------------------------------------------------------ API

    /**
     * Kirim PDF untuk ditandatangani satu dokter, tanda tangan diletakkan di setiap keyword #1A.
     * Dokumen langsung dicatat di tabel mekarisign_dokumen.
     */
    public static Dokumen kirim(File filePdf, String namaFile, String noRawat, String jenisDokumen,
                                String kdDokter, String namaDokter, String email) throws Exception {
        if (email == null || email.trim().isEmpty()) {
            throw new MekariSignException(0, "Email dokter "+namaDokter+" belum diisi di master dokter.\nEmail harus sama dengan akun MekariSign dokter.");
        }
        byte[] isi;
        try (PDDocument pdf = PDDocument.load(filePdf)) {
            List<Posisi> daftar = cariKeyword(pdf, KEYWORD);
            if (daftar.isEmpty()) {
                throw new MekariSignException(0, "Tempat tanda tangan ("+KEYWORD+") tidak ditemukan di dokumen.");
            }
            ArrayNode annotations = mapper.createArrayNode();
            for (Posisi p : daftar) {
                annotations.add(annotation(p));
            }
            tutupKeyword(pdf, daftar);
            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
            pdf.save(out);
            isi = out.toByteArray();

            String jenis = koneksiDBMekariSign.JENISMEKARISIGN();
            ObjectNode body = mapper.createObjectNode();
            body.put("doc", Base64.getEncoder().encodeToString(isi));
            body.put("filename", namaFile);
            ObjectNode signer = body.putArray("signers").addObject();
            signer.put("name", namaDokter);
            signer.put("email", email.trim());
            signer.set("annotations", annotations);
            String callback = koneksiDBMekariSign.URLDOKUMENMEKARISIGN();
            if (callback.toLowerCase().startsWith("https://")) {
                body.put("callback_url", callback);
            }
            if (jenis.equals("psre")) {
                body.put("signing_url", true);
            }

            JsonNode hasil = kirimJson(new HttpPost(url(jenis.equals("psre") ?
                    "/v1/documents/request_psre_sign_only" : "/v1/documents/request_global_sign_only")),
                    mapper.writeValueAsString(body));
            Dokumen d = bacaDokumen(hasil, email);
            if (d.id.isEmpty()) {
                throw new MekariSignException(0, "Respon MekariSign tidak berisi id dokumen.");
            }
            catat(noRawat, jenisDokumen, kdDokter, email.trim(), d);
            return d;
        }
    }

    /** Ambil dokumen terbaru dari MekariSign dan perbarui status di tabel. */
    public static Dokumen cek(String id, String email) throws Exception {
        Dokumen d = bacaDokumen(kirimJson(new HttpGet(url("/v1/documents/"+id)), null), email);
        if (!d.status.isEmpty()) {
            updateStatus(id, d.status);
        }
        return d;
    }

    /** Unduh PDF: bertanda tangan bila sudah selesai, selain itu file asli. */
    public static void download(String id, File tujuan) throws Exception {
        HttpGet get = new HttpGet(url("/v1/documents/"+id+"/download"));
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            pasangAuth(get);
            try (CloseableHttpResponse res = client.execute(get)) {
                byte[] isi = res.getEntity() == null ? new byte[0] : EntityUtils.toByteArray(res.getEntity());
                if (res.getCode() == 200) {
                    Files.write(tujuan.toPath(), isi);
                } else if (res.getCode() == 424) {
                    throw new MekariSignException(424, "File bertanda tangan masih diproses MekariSign, silahkan ulangi beberapa saat lagi..!!");
                } else {
                    throw galat(res.getCode(), new String(isi, StandardCharsets.UTF_8));
                }
            }
        }
    }

    // ------------------------------------------------------------------ auto sign

    /** Satu pasangan auto sign: dokumen dari docMakerEmail otomatis ditandatangani signerEmail. */
    public static class AutoSign {
        public String id = "", docMakerEmail = "", signerEmail = "", dibuat = "";
    }

    /** Profil akun API beserta sisa kuota. */
    public static class Profil {
        public String email = "";
        public long kuotaGlobal, kuotaPsre, kuotaEkyc;
    }

    public static Profil profil() throws Exception {
        JsonNode attr = kirimJson(new HttpGet(url("/v1/profile")), null).path("data").path("attributes");
        Profil p = new Profil();
        p.email = teks(attr.path("email"));
        p.kuotaGlobal = attr.path("balance").path("global_sign_document").asLong(0);
        p.kuotaPsre = attr.path("balance").path("psre_signing").asLong(0);
        p.kuotaEkyc = attr.path("balance").path("ekyc_quota").asLong(0);
        if (p.email.isEmpty()) {
            throw new MekariSignException(0, "Email akun MekariSign (pengirim) tidak terbaca dari profil.");
        }
        return p;
    }

    /** Email akun API (pengirim semua dokumen dari SIMRS). */
    public static String emailPengirim() throws Exception {
        return profil().email;
    }

    // ------------------------------------------------------------------ eKYC

    /** Permintaan eKYC (verifikasi identitas untuk sertifikat PSrE). */
    public static class Ekyc {
        public String id = "", email = "", status = "", dibuat = "", diupdate = "";
    }

    /** eKYC terbaru per email (huruf kecil). Daftar dari Mekari sudah urut terbaru dulu. */
    public static java.util.Map<String, Ekyc> daftarEkyc() throws Exception {
        java.util.Map<String, Ekyc> hasil = new java.util.HashMap<>();
        for (int page = 1; page <= 50; page++) {
            JsonNode root = kirimJson(new HttpGet(url("/v1/ekyc_request?page="+page+"&limit=100")), null);
            for (JsonNode n : root.path("data")) {
                JsonNode attr = n.path("attributes");
                Ekyc e = new Ekyc();
                e.id = teks(n.path("id"));
                e.email = teks(attr.path("email"));
                e.status = teks(attr.path("status"));
                e.dibuat = teks(attr.path("created_at"));
                e.diupdate = teks(attr.path("updated_at"));
                if (!e.email.isEmpty() && !hasil.containsKey(e.email.toLowerCase())) {
                    hasil.put(e.email.toLowerCase(), e);
                }
            }
            if (page >= root.path("pagination").path("total_pages").asInt(0)) {
                break;
            }
        }
        return hasil;
    }

    /** Undang dokter eKYC; undangan dikirim Mekari ke email dokter. Memakai 1 kuota eKYC. */
    public static void undangEkyc(String email) throws Exception {
        ObjectNode body = mapper.createObjectNode();
        body.put("email", email.trim());
        body.put("send_email", true);
        kirimJson(new HttpPost(url("/v1/ekyc_request")), mapper.writeValueAsString(body));
    }

    public static String keteranganEkyc(String status) {
        switch (status == null ? "" : status) {
            case "not_started": return "Belum mulai";
            case "verifying":   return "Sedang diverifikasi";
            case "success":     return "Terverifikasi";
            case "failed":      return "Gagal";
            case "expired":     return "Kedaluwarsa";
            case "revoked":     return "Dicabut";
            case "deactivated": return "Nonaktif";
            default:            return "Belum diundang";
        }
    }

    /** Semua auto sign per pengirim (bukan auto sign template). */
    public static List<AutoSign> daftarAutoSign() throws Exception {
        List<AutoSign> hasil = new ArrayList<>();
        for (int page = 1; page <= 50; page++) {
            JsonNode root = kirimJson(new HttpGet(url("/v1/auto_sign?page="+page+"&limit=100")), null);
            for (JsonNode n : root.path("data")) {
                JsonNode attr = n.path("attributes");
                AutoSign a = new AutoSign();
                a.id = teks(n.path("id"));
                a.docMakerEmail = teks(attr.path("doc_maker_email"));
                a.signerEmail = teks(attr.path("signer_email"));
                a.dibuat = teks(attr.path("created_at"));
                if (!a.docMakerEmail.isEmpty()) {
                    hasil.add(a);
                }
            }
            if (page >= root.path("pagination").path("total_pages").asInt(0)) {
                break;
            }
        }
        return hasil;
    }

    /** Aktifkan auto sign untuk dokumen dari akun API ke dokter ini. */
    public static void aktifkanAutoSign(String pengirim, String emailDokter) throws Exception {
        ObjectNode body = mapper.createObjectNode();
        body.putArray("doc_maker_emails").add(pengirim);
        body.putArray("signer_emails").add(emailDokter.trim());
        kirimJson(new HttpPost(url("/v1/auto_sign")), mapper.writeValueAsString(body));
    }

    public static void hapusAutoSign(String id) throws Exception {
        kirimJson(new HttpDelete(url("/v1/auto_sign/"+id)), null);
    }

    private static Dokumen bacaDokumen(JsonNode root, String email) {
        Dokumen d = new Dokumen();
        JsonNode data = root.path("data");
        JsonNode attr = data.path("attributes");
        d.id = teks(data.path("id"));
        d.namaFile = teks(attr.path("filename"));
        d.kategori = teks(attr.path("category"));
        d.status = teks(attr.path("signing_status"));
        for (JsonNode s : attr.path("signers")) {
            if (email == null || teks(s.path("email")).equalsIgnoreCase(email.trim())) {
                d.signingUrl = teks(s.path("signing_url"));
                break;
            }
        }
        return d;
    }

    /** Isi teks node; "" bila tidak ada / null. Pengganti asText("") yang belum ada di jackson-databind 2.2.3. */
    private static String teks(JsonNode n) {
        return (n == null || n.isMissingNode() || n.isNull()) ? "" : n.asText();
    }

    private static String url(String path) throws MekariSignException {
        String base = koneksiDBMekariSign.URLAPIMEKARISIGN();
        if (base.isEmpty()) {
            throw new MekariSignException(0, "URLAPIMEKARISIGN belum diisi di setting/database-extra.xml");
        }
        return base+path;
    }

    private static JsonNode kirimJson(HttpUriRequestBase req, String body) throws Exception {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            pasangAuth(req);
            req.addHeader("Accept", "application/json");
            if (body != null) {
                req.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));
            }
            try (CloseableHttpResponse res = client.execute(req)) {
                String isi = res.getEntity() == null ? "" : EntityUtils.toString(res.getEntity(), StandardCharsets.UTF_8);
                if (res.getCode() >= 200 && res.getCode() < 300) {
                    JsonNode hasil = isi.trim().isEmpty() ? null : mapper.readTree(isi);
                    return hasil == null ? mapper.createObjectNode() : hasil;
                }
                Header reset = res.getFirstHeader("X-RateLimit-Reset");
                if (res.getCode() == 429 && reset != null) {
                    throw new MekariSignException(429, "Batas request MekariSign tercapai, ulangi "+reset.getValue()+" detik lagi.");
                }
                throw galat(res.getCode(), isi);
            }
        }
    }

    /** HMAC bila client id & secret diisi, selain itu Bearer token. */
    private static void pasangAuth(HttpUriRequestBase req) throws Exception {
        String clientId = koneksiDBMekariSign.CLIENTIDMEKARISIGN(), secret = koneksiDBMekariSign.CLIENTSECRETMEKARISIGN();
        if (!clientId.isEmpty() && !secret.isEmpty()) {
            SimpleDateFormat f = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US);
            f.setTimeZone(TimeZone.getTimeZone("GMT"));
            String tanggal = f.format(new Date());
            URI uri = req.getUri();
            String path = uri.getRawPath()+(uri.getRawQuery() == null ? "" : "?"+uri.getRawQuery());
            String teks = "date: "+tanggal+"\n"+req.getMethod()+" "+path+" HTTP/1.1";
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            String signature = Base64.getEncoder().encodeToString(mac.doFinal(teks.getBytes(StandardCharsets.UTF_8)));
            req.addHeader("Date", tanggal);
            req.addHeader("Authorization", "hmac username=\""+clientId+"\", algorithm=\"hmac-sha256\", headers=\"date request-line\", signature=\""+signature+"\"");
            return;
        }
        String token = koneksiDBMekariSign.APIKEYMEKARISIGN();
        if (token.isEmpty()) {
            throw new MekariSignException(0, "Kredensial MekariSign belum diisi di setting/database-extra.xml\n(CLIENTIDMEKARISIGN + CLIENTSECRETMEKARISIGN untuk HMAC, atau APIKEYMEKARISIGN untuk OAuth2).");
        }
        req.addHeader("Authorization", "Bearer "+token);
    }

    /** Ubah respon galat (bentuknya beragam) menjadi pesan yang bisa dibaca. */
    private static MekariSignException galat(int status, String isi) {
        System.out.println("Notifikasi MekariSign : HTTP "+status+" "+isi);
        String pesan = "";
        try {
            JsonNode r = mapper.readTree(isi);
            pesan = teks(r.path("message"));
            if (pesan.isEmpty()) {
                pesan = teks(r.path("data").path("message"));
            }
            JsonNode params = r.path("data").path("params");
            if (params.isObject()) {
                StringBuilder sb = new StringBuilder(pesan);
                params.fields().forEachRemaining(e -> {
                    sb.append("\n- ").append(e.getKey()).append(" : ");
                    if (e.getValue().isArray()) {
                        for (JsonNode n : e.getValue()) {
                            sb.append(n.asText()).append(" ");
                        }
                    } else {
                        sb.append(e.getValue().toString());
                    }
                });
                pesan = sb.toString();
            }
        } catch (Exception e) {
            pesan = isi.length() > 300 ? isi.substring(0, 300) : isi;
        }
        String awal;
        switch (status) {
            case 400: awal = "Kuota MekariSign tidak cukup / request salah"; break;
            case 401: awal = "Autentikasi MekariSign ditolak, periksa kredensial & URL"; break;
            case 403: awal = "Akses ditolak MekariSign (kredensial atau fitur belum diaktifkan untuk RS)"; break;
            case 404: awal = "Dokumen tidak ditemukan di MekariSign"; break;
            case 422: awal = "Data ditolak MekariSign"; break;
            case 424: awal = "Layanan PSrE/MekariSign sedang tidak tersedia, ulangi beberapa saat lagi"; break;
            default:  awal = "Gagal menghubungi MekariSign (HTTP "+status+")";
        }
        return new MekariSignException(status, awal+(pesan.isEmpty() ? "" : " :\n"+pesan));
    }
}
