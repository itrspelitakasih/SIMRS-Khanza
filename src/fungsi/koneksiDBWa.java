/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fungsi;


import AESsecurity.EnkripsiAES;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.util.Properties;
import javax.swing.JOptionPane;


/**
 *
 * @author khanzasoft
 */
public class koneksiDBWa {
    private static Connection connection=null;
    private static final Properties prop = new Properties();  
    private static final MysqlDataSource dataSource=new MysqlDataSource();

    public koneksiDBWa(){}

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

    public static Connection newConnection() throws Exception {
        Properties setting = loadSettings();
        MysqlDataSource source = new MysqlDataSource();

        source.setURL("jdbc:mysql://"
                + setting(setting, "HOSTWA", "HOST") + ":"
                + setting(setting, "PORTWA", "PORT") + "/"
                + setting(setting, "DATABASEWA", "DATABASE")
                + "?zeroDateTimeBehavior=convertToNull&autoReconnect=true");
        source.setUser(setting(setting, "USERWA", "USER"));
        source.setPassword(setting(setting, "PASWA", "PAS"));

        return source.getConnection();
    }

    /** Ambil key WA terenkripsi; bila tidak diisi, pakai key database utama (tabel WA ada di DB SIMRS). */
    private static String setting(Properties setting, String keyWA, String keyUtama) throws Exception {
        String nilai = setting.getProperty(keyWA, "").trim();
        if (nilai.isEmpty()) {
            nilai = setting.getProperty(keyUtama, "").trim();
        }
        return EnkripsiAES.decrypt(nilai);
    }

    public static Connection condb(){ 
        if(connection == null){
            try{
                connection=newConnection();
                System.out.println("  Koneksi Berhasil. Menyambungkan ke database bridging Gateway WA...!!!");
            }catch(Exception e){
                JOptionPane.showMessageDialog(null,"Koneksi ke server bridging Gateway WA terputus : "+e);
            }
        }
        return connection;        
    }
    private static void load() {
        try {
            if(prop.isEmpty()){
                prop.putAll(loadSettings());
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                "Gagal load setting database WA : "+e);
        }
    }
    public static String FOLDERWA(){
        load();
        return prop.getProperty("FOLDERWA", "").trim();
    }

    public static String WAHA_BASE_URL() {
        load();
        return prop.getProperty("WAHA_BASE_URL", "").trim();
    }

    public static String SESSION() {
        load();
        return prop.getProperty("WAHA_SESSION", "").trim();
    }

    public static String getAPIKey() {
        load();
        return prop.getProperty("WAHA_API_KEY", "").trim();
    }
    public static String TOKEN() {
        load();
        return prop.getProperty("TOKEN", "").trim();
    }

    public static String FILE_BASE_URL() {
        load();
        return prop.getProperty("WAHA_FILE_BASE_URL", "").trim();
    }

    public static String WAHA_API_KEY() {
        load();
        return prop.getProperty("WAHA_API_KEY", "").trim();
    }
   
   //SETTING GOWA

    public static String GOWA_BASE_URL() {
        load();
        try {
            return EnkripsiAES.decrypt(prop.getProperty("GOWA_BASE_URL"));
        } catch (Exception e) {
            return "";
        }
    }

    public static String GOWA_USERNAME() {
        load();
        try {
            return EnkripsiAES.decrypt(prop.getProperty("GOWA_USERNAME"));
        } catch (Exception e) {
            return "";
        }
    }

    public static String GOWA_PASSWORD() {
        load();
        try {
            return EnkripsiAES.decrypt(prop.getProperty("GOWA_PASSWORD"));
        } catch (Exception e) {
            return "";
        }
    }

    public static String GOWA_DEVICE_ID() {
        load();
        return prop.getProperty("GOWA_DEVICE_ID", "");
    }

    // SETTING PANGGIL ANTREAN PWA E-PASIEN

    public static String ANTRIAN_API_BASE_URL() {
        load();
        return prop.getProperty("ANTRIAN_API_BASE_URL", "").trim();
    }

    public static String ANTRIAN_API_TOKEN() {
        load();
        return prop.getProperty("ANTRIAN_API_TOKEN", "").trim();
    }

    // SETTING NOTIF WA BOOKING & REGISTRASI (teks pesan di wa/pesanWA.java)

    /** yes/no: WA ke admin pendaftaran (NOWABOOKINGPERIKSA) saat ada booking periksa baru. Default yes. */
    public static String NOTIFWABOOKINGPERIKSA() {
        load();
        return prop.getProperty("NOTIFWABOOKINGPERIKSA", "yes").trim();
    }

    /** yes/no: WA ke pasien saat booking diregistrasi (reg_periksa dibuat). Default yes. */
    public static String NOTIFWAREGISTRASIPASIEN() {
        load();
        return prop.getProperty("NOTIFWAREGISTRASIPASIEN", "yes").trim();
    }

    /** Alamat portal pasien yang dicantumkan di WA registrasi, untuk cek status antrean. */
    public static String URLPORTALPASIEN() {
        load();
        return prop.getProperty("URLPORTALPASIEN", "http://me.rspelitakasih.id/").trim();
    }
}
