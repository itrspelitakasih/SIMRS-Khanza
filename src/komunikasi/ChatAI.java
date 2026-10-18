package komunikasi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Percakapan lewat API chat completions kompatibel OpenAI: OpenRouter (model gratis :free)
 * atau server Ollama (http://host:11434/v1). Satu objek = satu percakapan; riwayat disimpan sebagai pesan role/content.
 * JSON memakai org.json (java-json.jar) karena urutan classpath memuat Jackson versi lama lebih dulu.
 */
class ChatAI {
    private final String urlApi,apiKey;
    private final AsistenAI.Setting setting;
    private final List<JSONObject> riwayat=new ArrayList<>();

    ChatAI(AsistenAI.Setting setting){
        this.setting=setting;
        this.urlApi=AsistenAI.urlApi(setting.provider);
        this.apiKey=setting.apiKey;
    }

    private static JSONObject pesan(String role,String isi) throws Exception{
        return new JSONObject().put("role",role).put("content",isi);
    }

    /** org.json lama mengembalikan "null" untuk nilai null JSON, jadi cek isNull dulu. */
    private static String teks(JSONObject o,String kunci){
        return o==null||!o.has(kunci)||o.isNull(kunci)?"":o.optString(kunci,"");
    }

    private static HttpURLConnection buka(String url,String apiKey) throws IOException{
        HttpURLConnection con=(HttpURLConnection)new URL(url).openConnection();
        con.setConnectTimeout(15000);
        // Ollama di server tanpa GPU bisa lama memuat model pada permintaan pertama
        con.setReadTimeout(300000);
        if(!apiKey.equals("")){
            con.setRequestProperty("Authorization","Bearer "+apiKey);
        }
        con.setRequestProperty("X-Title","SIMRS Khanza");
        return con;
    }

    /** Pesan error dari body JSON {"error":{"message":...}} (OpenRouter) atau {"error":"..."} (Ollama). */
    private static String pesanGagal(String provider,String model,int kode,InputStream body){
        String detail="";
        try (InputStream in=body){
            if(in!=null){
                JSONObject n=new JSONObject(new JSONTokener(new InputStreamReader(in,StandardCharsets.UTF_8)));
                JSONObject error=n.optJSONObject("error");
                detail=error!=null?teks(error,"message"):teks(n,"error");
            }
        } catch (Exception e) {
            detail="";
        }
        boolean ollama=provider.equals(AsistenAI.OLLAMA);
        if(ollama&&kode==404){
            return "Model "+model+" belum ada di server Ollama. Jalankan di server: ollama pull "+model;
        }
        switch (kode) {
            case 401:
                return "API key OpenRouter tidak valid. Periksa OPENROUTERAPIKEY di setting/database-extra.xml.";
            case 402:
                return "Kredit/batas akun OpenRouter habis. "+detail;
            case 429:
                return "Batas pemakaian gratis OpenRouter tercapai (per menit/harian). Coba lagi nanti. "+detail;
            default:
                return (ollama?"Ollama":"OpenRouter")+" error "+kode+" : "+detail;
        }
    }

    private static String gagalTerhubung(String provider,IOException e){
        return provider.equals(AsistenAI.OLLAMA)?
                "Tidak bisa terhubung ke server Ollama ("+AsistenAI.urlApi(provider)+"). Pastikan server menyala, "+
                "OLLAMAURL di setting/database-extra.xml benar, dan Ollama dijalankan dengan OLLAMA_HOST=0.0.0.0. ("+e.getMessage()+")":
                "Tidak bisa terhubung ke openrouter.ai. Periksa koneksi internet PC ini. ("+e.getMessage()+")";
    }

    /** Mengisi h (teks, token, model) dan menambah riwayat bila berhasil. */
    void kirim(String isi,Consumer<String> potongan,AsistenAI.Hasil h){
        StringBuilder jawaban=new StringBuilder();
        String alasanSelesai="";
        try {
            JSONArray messages=new JSONArray();
            if(!setting.systemPrompt.trim().equals("")){
                messages.put(pesan("system",setting.systemPrompt));
            }
            for(JSONObject p:riwayat){
                messages.put(p);
            }
            messages.put(pesan("user",isi));
            JSONObject body=new JSONObject().put("model",setting.model).put("stream",true).put("messages",messages);

            HttpURLConnection con=buka(urlApi+"/chat/completions",apiKey);
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.setRequestProperty("Content-Type","application/json");
            try (OutputStream out=con.getOutputStream()){
                out.write(body.toString().getBytes(StandardCharsets.UTF_8));
            }
            int kode=con.getResponseCode();
            if(kode!=200){
                h.pesanGagal=pesanGagal(setting.provider,setting.model,kode,con.getErrorStream());
                return;
            }
            // Server-Sent Events: baris "data: {json}", baris ":" = komentar keep-alive, diakhiri "data: [DONE]"
            try (BufferedReader rd=new BufferedReader(new InputStreamReader(con.getInputStream(),StandardCharsets.UTF_8))){
                String baris;
                while((baris=rd.readLine())!=null){
                    if(!baris.startsWith("data:")){
                        continue;
                    }
                    String data=baris.substring(5).trim();
                    if(data.equals("[DONE]")){
                        break;
                    }
                    JSONObject n=new JSONObject(data);
                    if(n.has("error")){
                        JSONObject error=n.optJSONObject("error");
                        h.pesanGagal="Error : "+(error!=null?teks(error,"message"):teks(n,"error"));
                        break;
                    }
                    if(!teks(n,"model").equals("")){
                        h.model=teks(n,"model");
                    }
                    JSONArray choices=n.optJSONArray("choices");
                    JSONObject pilihan=choices==null?null:choices.optJSONObject(0);
                    String potong=pilihan==null?"":teks(pilihan.optJSONObject("delta"),"content");
                    if(!potong.equals("")){
                        jawaban.append(potong);
                        potongan.accept(potong);
                    }
                    if(!teks(pilihan,"finish_reason").equals("")){
                        alasanSelesai=teks(pilihan,"finish_reason");
                    }
                    JSONObject usage=n.optJSONObject("usage");
                    if(usage!=null){
                        h.inputTokens=usage.optLong("prompt_tokens",0);
                        h.outputTokens=usage.optLong("completion_tokens",0);
                    }
                }
            }
        } catch (IOException e) {
            h.pesanGagal=gagalTerhubung(setting.provider,e);
        } catch (Exception e) {
            h.pesanGagal="Gagal : "+e.getMessage();
        }
        h.teks=jawaban.toString();
        if(h.pesanGagal!=null){
            return;
        }
        if(h.teks.trim().equals("")){
            h.pesanGagal="Model tidak memberi jawaban, silakan kirim ulang atau ganti model.";
            return;
        }
        h.terpotong=alasanSelesai.equals("length");
        try {
            riwayat.add(pesan("user",isi));
            riwayat.add(pesan("assistant",h.teks));
        } catch (Exception e) {
            System.out.println("Notifikasi Asisten AI : "+e);
        }
    }

    /**
     * Cek koneksi tanpa memakai kuota chat. OpenRouter: validasi API key lewat /key.
     * Ollama: ambil daftar model yang sudah di-pull lewat /models dan isi ke modelTersedia.
     */
    static String tesKoneksi(String provider,String apiKey,List<String> modelTersedia){
        String url=AsistenAI.urlApi(provider);
        try {
            boolean ollama=provider.equals(AsistenAI.OLLAMA);
            HttpURLConnection con=buka(url+(ollama?"/models":"/key"),apiKey);
            con.setReadTimeout(15000);
            int kode=con.getResponseCode();
            if(kode!=200){
                return pesanGagal(provider,"",kode,con.getErrorStream());
            }
            JSONObject n;
            try (InputStream in=con.getInputStream()){
                n=new JSONObject(new JSONTokener(new InputStreamReader(in,StandardCharsets.UTF_8)));
            }
            if(ollama){
                JSONArray data=n.optJSONArray("data");
                for(int i=0;data!=null&&i<data.length();i++){
                    String id=teks(data.optJSONObject(i),"id");
                    if(!id.equals("")){
                        modelTersedia.add(id);
                    }
                }
                return modelTersedia.isEmpty()?
                        "Server Ollama terhubung, tetapi belum ada model. Jalankan di server, misalnya: ollama pull qwen2.5:7b":
                        "Server Ollama terhubung. Model tersedia : "+String.join(", ",modelTersedia);
            }
            JSONObject data=n.optJSONObject("data");
            return "Koneksi berhasil. API key OpenRouter valid"+
                    (data!=null&&data.optBoolean("is_free_tier",false)?" (akun gratis, model :free dibatasi jumlah request per hari).":".");
        } catch (IOException e) {
            return gagalTerhubung(provider,e);
        } catch (Exception e) {
            return "Gagal : "+e.getMessage();
        }
    }
}
