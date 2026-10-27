package komunikasi;

import fungsi.koneksiDB;
import fungsi.koneksiDBAI;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.function.Consumer;

/**
 * Asisten AI untuk chat umum, provider OpenRouter (model gratis :free) atau Ollama (server sendiri).
 * API key OpenRouter & alamat server Ollama dibaca dari setting/database-extra.xml (OPENROUTERAPIKEY / OLLAMAURL),
 * setting lain (provider, model, system prompt) di tabel set_asisten_ai, pemakaian token dicatat di asisten_ai_log.
 * Satu objek = satu percakapan; setting dibekukan saat percakapan dimulai.
 */
public class AsistenAI {
    public static final String OPENROUTER="openrouter",OLLAMA="ollama";
    /** Model chat gratis. Jangan pakai router openrouter/free: bisa memilih model non-chat (mis. penyaring content-safety). */
    public static final String[] MODEL_OPENROUTER={"google/gemma-4-31b-it:free","nvidia/nemotron-3-super-120b-a12b:free","nvidia/nemotron-3-ultra-550b-a55b:free"};
    /** Contoh model Ollama; daftar sebenarnya diambil dari server lewat Tes Koneksi. */
    public static final String[] MODEL_OLLAMA={"qwen2.5:7b","llama3.1:8b","gemma3:4b"};
    public static final String SYSTEM_PROMPT_BAWAAN=
            "Anda adalah asisten AI untuk staf rumah sakit yang memakai SIMRS Khanza. "+
            "Jawab dalam Bahasa Indonesia yang jelas dan ringkas, kecuali pengguna memakai bahasa lain. "+
            "Percakapan ini untuk pertanyaan umum; jangan meminta data identitas pasien. "+
            "Untuk pertanyaan medis, berikan informasi umum dan ingatkan bahwa keputusan klinis tetap pada tenaga kesehatan yang berwenang.";
    private static boolean tabelSiap=false;

    public static class Setting {
        public String provider=OPENROUTER,apiKey="",model=MODEL_OPENROUTER[0],systemPrompt=SYSTEM_PROMPT_BAWAAN;
        public boolean aktif=true;
    }

    public static class Hasil {
        public String teks="",pesanGagal=null,model="";
        public boolean terpotong=false;
        public long inputTokens=0,outputTokens=0;
    }

    private final Connection koneksi=koneksiDB.condb();
    private final Setting setting;
    private final ChatAI chat;

    /** Membaca setting saat ini; setting yang diubah sesudahnya berlaku di percakapan berikutnya. */
    public AsistenAI(){
        this(null);
    }

    /** Sama seperti AsistenAI(), tetapi system prompt diganti (mis. untuk draf hasil bacaan radiologi). */
    public AsistenAI(String systemPrompt){
        setting=baca(koneksi);
        if(systemPrompt!=null){
            setting.systemPrompt=systemPrompt;
        }
        chat=siapSetting(setting)?new ChatAI(setting):null;
    }

    private static boolean siapSetting(Setting s){
        return s.provider.equals(OLLAMA)?!koneksiDBAI.OLLAMAURL().equals(""):!s.apiKey.equals("");
    }

    /** Base URL API kompatibel OpenAI untuk provider tsb. */
    static String urlApi(String provider){
        if(provider.equals(OLLAMA)){
            String url=koneksiDBAI.OLLAMAURL().replaceAll("/+$","");
            return url.endsWith("/v1")?url:url+"/v1";
        }
        return "https://openrouter.ai/api/v1";
    }

    /** API key provider; Ollama tidak memakai API key. */
    public static String apiKey(String provider){
        return provider.equals(OLLAMA)?"":koneksiDBAI.OPENROUTERAPIKEY();
    }

    /** Pesan bila setting di database-extra.xml untuk provider tsb belum diisi. */
    public static String pesanBelumDiisi(String provider){
        return provider.equals(OLLAMA)?
                "Alamat server Ollama belum diisi di setting/database-extra.xml (OLLAMAURL, mis. http://192.168.1.10:11434).":
                "API key belum diisi di setting/database-extra.xml (OPENROUTERAPIKEY).";
    }

    static String tesKoneksi(String provider,List<String> modelTersedia){
        return ChatAI.tesKoneksi(provider,apiKey(provider),modelTersedia);
    }

    public Setting getSetting(){
        return setting;
    }

    public boolean siap(){
        return setting.aktif&&chat!=null;
    }

    /** Buat tabel bila belum ada (butuh hak CREATE), sama seperti tabel chat antar user. */
    public static synchronized void siapkanTabel(Connection koneksi){
        if(tabelSiap){
            return;
        }
        try (Statement st=koneksi.createStatement()){
            st.executeUpdate("CREATE TABLE IF NOT EXISTS `set_asisten_ai` ("+
                    "`id` tinyint(4) NOT NULL DEFAULT 1,"+
                    "`provider` varchar(20) NOT NULL DEFAULT 'openrouter',"+
                    "`model` varchar(100) NOT NULL DEFAULT 'google/gemma-4-31b-it:free',"+
                    "`system_prompt` text,"+
                    "`aktif` enum('Yes','No') NOT NULL DEFAULT 'Yes',"+
                    "PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS `asisten_ai_log` ("+
                    "`id` bigint(20) NOT NULL AUTO_INCREMENT,"+
                    "`waktu` datetime NOT NULL,"+
                    "`kode_user` varchar(50) NOT NULL,"+
                    "`model` varchar(100) NOT NULL,"+
                    "`input_tokens` int(11) NOT NULL DEFAULT 0,"+
                    "`output_tokens` int(11) NOT NULL DEFAULT 0,"+
                    "PRIMARY KEY (`id`),KEY `waktu` (`waktu`),KEY `kode_user` (`kode_user`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            // tabel versi awal (Claude) belum punya kolom provider dan kolom model masih 60 karakter
            try (ResultSet rs=st.executeQuery("SHOW COLUMNS FROM `set_asisten_ai` LIKE 'provider'")){
                if(!rs.next()){
                    st.executeUpdate("ALTER TABLE `set_asisten_ai` ADD COLUMN `provider` varchar(20) NOT NULL DEFAULT 'openrouter' AFTER `id`,"+
                            "MODIFY `model` varchar(100) NOT NULL DEFAULT 'google/gemma-4-31b-it:free'");
                    st.executeUpdate("ALTER TABLE `asisten_ai_log` MODIFY `model` varchar(100) NOT NULL");
                }
            }
            // Claude sudah dihapus dan router openrouter/free bisa memilih model non-chat: pindah ke model chat gratis
            st.executeUpdate("UPDATE `set_asisten_ai` SET `provider`='openrouter',`model`='google/gemma-4-31b-it:free' "+
                    "WHERE `provider` NOT IN ('openrouter','ollama') OR `model` LIKE 'claude-%' OR `model`='openrouter/free'");
            tabelSiap=true;
        } catch (Exception e) {
            System.out.println("Notifikasi Asisten AI : "+e);
        }
    }

    public static Setting baca(Connection koneksi){
        siapkanTabel(koneksi);
        Setting s=new Setting();
        try (PreparedStatement ps=koneksi.prepareStatement(
                "select provider,model,system_prompt,aktif from set_asisten_ai where id=1");
             ResultSet rs=ps.executeQuery()){
            if(rs.next()){
                s.provider=rs.getString(1).equals(OLLAMA)?OLLAMA:OPENROUTER;
                s.model=rs.getString(2);
                s.systemPrompt=rs.getString(3)==null?"":rs.getString(3);
                s.aktif=rs.getString(4).equals("Yes");
            }
        } catch (Exception e) {
            System.out.println("Notifikasi Asisten AI : "+e);
        }
        // API key tidak disimpan di database, tetapi di setting/database-extra.xml
        s.apiKey=apiKey(s.provider);
        return s;
    }

    public static void simpan(Connection koneksi,Setting s) throws Exception{
        siapkanTabel(koneksi);
        try (PreparedStatement ps=koneksi.prepareStatement(
                "insert into set_asisten_ai(id,provider,model,system_prompt,aktif) values(1,?,?,?,?) "+
                "on duplicate key update provider=values(provider),model=values(model),"+
                "system_prompt=values(system_prompt),aktif=values(aktif)")){
            ps.setString(1,s.provider);
            ps.setString(2,s.model);
            ps.setString(3,s.systemPrompt);
            ps.setString(4,s.aktif?"Yes":"No");
            ps.executeUpdate();
        }
    }

    /**
     * Kirim pesan dan stream jawaban ke potongan (dipanggil dari thread background).
     * Bila gagal, pesan user tidak masuk riwayat sehingga bisa dikirim ulang.
     */
    public Hasil kirim(String pesan,String kodeUser,Consumer<String> potongan){
        Hasil h=new Hasil();
        if(!siap()){
            h.pesanGagal=setting.aktif?pesanBelumDiisi(setting.provider):"Asisten AI sedang dinonaktifkan oleh admin.";
            return h;
        }
        chat.kirim(pesan,potongan,h);
        if(h.pesanGagal==null){
            catatPemakaian(kodeUser,h.model.equals("")?setting.model:h.model,h);
        }
        return h;
    }

    private void catatPemakaian(String kodeUser,String model,Hasil h){
        try (PreparedStatement ps=koneksi.prepareStatement(
                "insert into asisten_ai_log(waktu,kode_user,model,input_tokens,output_tokens) values(now(),?,?,?,?)")){
            ps.setString(1,kodeUser);
            ps.setString(2,model);
            ps.setLong(3,h.inputTokens);
            ps.setLong(4,h.outputTokens);
            ps.executeUpdate();
        } catch (Exception e) {
            System.out.println("Notifikasi Asisten AI : "+e);
        }
    }
}
