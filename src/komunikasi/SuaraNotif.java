package komunikasi;

import java.awt.Toolkit;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.prefs.Preferences;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

/**
 * Suara notifikasi lonceng. Nada dibangkitkan langsung (tanpa file) supaya selalu terdengar,
 * tidak bergantung skema suara Windows seperti Toolkit.beep(). Bila ada file suara/notif-pesan.wav,
 * suara/notif-chat.wav, suara/notif-hasil.wav atau suara/notif-tugas.wav di folder aplikasi, file itu yang diputar.
 * Diputar satu per satu di thread sendiri. Aktif/nonaktif disimpan per user Windows.
 */
public final class SuaraNotif {
    public enum Jenis {
        // nada {frekuensi Hz, durasi detik}
        PESAN("notif-pesan.wav",new double[][]{{988,0.14},{784,0.30}}),
        CHAT_SEMUA("notif-chat.wav",new double[][]{{880,0.25}}),
        HASIL("notif-hasil.wav",new double[][]{{659,0.13},{784,0.13},{1047,0.35}}),
        TUGAS("notif-tugas.wav",new double[][]{{1175,0.12},{880,0.12},{1175,0.30}});

        private final String file;
        private final double[][] nada;

        Jenis(String file,double[][] nada){
            this.file=file;
            this.nada=nada;
        }
    }

    private static final float RATE=44100f;
    private static final Preferences pref=Preferences.userNodeForPackage(SuaraNotif.class);
    private static final ExecutorService antrian=Executors.newSingleThreadExecutor(r -> {
        Thread t=new Thread(r,"SuaraNotif");
        t.setDaemon(true);
        return t;
    });

    private SuaraNotif(){}

    public static boolean isAktif(){
        return pref.getBoolean("aktif",true);
    }

    public static void setAktif(boolean aktif){
        pref.putBoolean("aktif",aktif);
    }

    public static void bunyi(Jenis jenis){
        if(isAktif()){
            antrian.execute(() -> putar(jenis));
        }
    }

    private static void putar(Jenis jenis){
        try {
            File f=new File("suara",jenis.file);
            if(f.isFile()){
                putarFile(f);
            }else{
                putarNada(jenis.nada);
            }
        } catch (Exception e) {
            System.out.println("Suara Notifikasi : "+e);
            Toolkit.getDefaultToolkit().beep();
        }
    }

    private static void putarFile(File f) throws Exception {
        try (AudioInputStream ais=AudioSystem.getAudioInputStream(new BufferedInputStream(new FileInputStream(f)))){
            AudioFormat format=ais.getFormat();
            try (SourceDataLine line=(SourceDataLine)AudioSystem.getLine(new DataLine.Info(SourceDataLine.class,format))){
                line.open(format);
                line.start();
                byte[] buf=new byte[4096];
                int n;
                while((n=ais.read(buf,0,buf.length))>0){
                    line.write(buf,0,n);
                }
                line.drain();
            }
        }
    }

    /** Nada mirip lonceng: sinus + harmonik dengan peluruhan eksponensial, 16-bit mono. */
    private static void putarNada(double[][] nada) throws Exception {
        int total=0;
        for(double[] n:nada){
            total+=(int)(n[1]*RATE);
        }
        byte[] buf=new byte[total*2];
        int pos=0;
        for(double[] n:nada){
            int jml=(int)(n[1]*RATE);
            for(int i=0;i<jml;i++){
                double t=i/RATE;
                double serang=Math.min(1.0,i/(RATE*0.005));
                double env=serang*Math.exp(-t*4.5/n[1]);
                double v=(Math.sin(2*Math.PI*n[0]*t)+0.35*Math.sin(4*Math.PI*n[0]*t))/1.35*env*0.6;
                short s=(short)(v*Short.MAX_VALUE);
                buf[pos++]=(byte)(s&0xff);
                buf[pos++]=(byte)((s>>8)&0xff);
            }
        }
        AudioFormat format=new AudioFormat(RATE,16,1,true,false);
        try (SourceDataLine line=AudioSystem.getSourceDataLine(format)){
            line.open(format);
            line.start();
            line.write(buf,0,buf.length);
            line.drain();
        }
    }
}
