package komunikasi;

import fungsi.koneksiDB;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

/**
 * Avatar bulat user chat dari foto pegawai (pegawai.photo lewat web server Khanza), atau lingkaran
 * berwarna berisi inisial nama bila foto tidak ada / server tidak terjangkau. Foto diunduh sekali
 * di background lalu disimpan di memori; pendengar diberi tahu saat foto siap agar tampilan digambar ulang.
 */
public class AvatarChat {
    private static final int UKURAN_SUMBER=96;
    private static final Color[] WARNA={new Color(46,125,50),new Color(21,101,192),new Color(198,40,40),new Color(106,27,154),
        new Color(239,108,0),new Color(0,131,143),new Color(93,64,55),new Color(84,110,122)};
    private static final Map<String,BufferedImage> foto=new HashMap<>();
    private static final Set<String> sudahDicoba=new HashSet<>();
    private static final Map<String,ImageIcon> cacheIkon=new HashMap<>();
    private static final Set<String> fileDitulis=new HashSet<>();
    private static final List<Runnable> pendengar=new ArrayList<>();
    private static final ExecutorService pengunduh=Executors.newFixedThreadPool(2,r -> {
        Thread t=new Thread(r,"AvatarChat");
        t.setDaemon(true);
        return t;
    });
    private static File folderTemp=null;

    /** Dipanggil di EDT setiap ada foto baru selesai diunduh. */
    public static synchronized void tambahPendengar(Runnable r){
        pendengar.add(r);
    }

    public static synchronized void hapusPendengar(Runnable r){
        pendengar.remove(r);
    }

    /** Avatar bulat ukuran px, dengan titik hijau di pojok kanan bawah bila online. */
    public static synchronized ImageIcon ikon(String kode,String nama,int ukuran,boolean online){
        muat(kode);
        boolean adaFoto=foto.containsKey(kode);
        String kunci=kode+"|"+ukuran+"|"+online+"|"+adaFoto;
        ImageIcon ikon=cacheIkon.get(kunci);
        if(ikon==null){
            ikon=new ImageIcon(gambar(kode,nama,ukuran,online));
            cacheIkon.put(kunci,ikon);
        }
        return ikon;
    }

    /**
     * Avatar sebagai file PNG sementara untuk dipakai di HTML (gelembung chat). Nama file beda antara
     * inisial & foto agar cache gambar Swing tidak menampilkan versi lama.
     */
    public static synchronized String urlFile(String kode,String nama,int ukuran){
        muat(kode);
        boolean adaFoto=foto.containsKey(kode);
        try {
            if(folderTemp==null){
                folderTemp=new File(System.getProperty("java.io.tmpdir"),"khanza-chat-avatar");
                folderTemp.mkdirs();
            }
            File f=new File(folderTemp,kode.replaceAll("[^A-Za-z0-9]","_")+"_"+Integer.toHexString(kode.hashCode())+
                    "_"+ukuran+(adaFoto?"_foto":"_inisial")+".png");
            // ditulis ulang sekali per sesi agar foto yang diganti di data pegawai ikut terbarui
            if(fileDitulis.add(f.getName())){
                ImageIO.write(gambar(kode,nama,ukuran,false),"png",f);
            }
            return f.toURI().toURL().toString();
        } catch (Exception e) {
            System.out.println("Notifikasi Avatar Chat : "+e);
            return "";
        }
    }

    private static void muat(String kode){
        if(kode.equals("")||!sudahDicoba.add(kode)){
            return;
        }
        pengunduh.execute(() -> {
            BufferedImage img=unduh(kode);
            if(img!=null){
                List<Runnable> salinan;
                synchronized(AvatarChat.class){
                    foto.put(kode,img);
                    salinan=new ArrayList<>(pendengar);
                }
                SwingUtilities.invokeLater(() -> salinan.forEach(Runnable::run));
            }
        });
    }

    private static BufferedImage unduh(String kode){
        try {
            String path="";
            Connection koneksi=koneksiDB.condb();
            try (PreparedStatement ps=koneksi.prepareStatement("select photo from pegawai where nik=?")){
                ps.setString(1,kode);
                try (ResultSet rs=ps.executeQuery()){
                    if(rs.next()&&rs.getString(1)!=null){
                        path=rs.getString(1).trim();
                    }
                }
            }
            // pegawai tanpa foto hanya berisi folder "pages/pegawai/photo/"
            if(path.equals("")||path.endsWith("/")){
                return null;
            }
            URLConnection con=new URL("http://"+koneksiDB.HOSTHYBRIDWEB()+":"+koneksiDB.PORTWEB()+"/"+
                    koneksiDB.HYBRIDWEB()+"/penggajian/"+path.replace(" ","%20")).openConnection();
            con.setConnectTimeout(3000);
            con.setReadTimeout(5000);
            BufferedImage asli;
            try (InputStream in=con.getInputStream()){
                asli=ImageIO.read(in);
            }
            if(asli==null){
                return null;
            }
            // potong tengah jadi persegi lalu perkecil
            int sisi=Math.min(asli.getWidth(),asli.getHeight());
            BufferedImage kecil=new BufferedImage(UKURAN_SUMBER,UKURAN_SUMBER,BufferedImage.TYPE_INT_ARGB);
            Graphics2D g=kecil.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.drawImage(asli,0,0,UKURAN_SUMBER,UKURAN_SUMBER,(asli.getWidth()-sisi)/2,(asli.getHeight()-sisi)/2,
                    (asli.getWidth()+sisi)/2,(asli.getHeight()+sisi)/2,null);
            g.dispose();
            return kecil;
        } catch (Exception e) {
            System.out.println("Notifikasi Avatar Chat ("+kode+") : "+e);
            return null;
        }
    }

    private static BufferedImage gambar(String kode,String nama,int ukuran,boolean online){
        BufferedImage img=new BufferedImage(ukuran,ukuran,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g=img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        Ellipse2D.Double lingkaran=new Ellipse2D.Double(0,0,ukuran,ukuran);
        BufferedImage f=foto.get(kode);
        if(f!=null){
            g.setClip(lingkaran);
            g.drawImage(f,0,0,ukuran,ukuran,null);
            g.setClip(null);
        }else{
            g.setColor(WARNA[Math.abs(kode.hashCode()%WARNA.length)]);
            g.fill(lingkaran);
            String inisial=inisial(nama);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial",Font.PLAIN,Math.max(8,ukuran*2/5)));
            FontMetrics fm=g.getFontMetrics();
            g.drawString(inisial,(ukuran-fm.stringWidth(inisial))/2,(ukuran-fm.getHeight())/2+fm.getAscent());
        }
        if(online){
            int d=Math.max(7,ukuran/3);
            g.setColor(Color.WHITE);
            g.fillOval(ukuran-d,ukuran-d,d,d);
            g.setColor(new Color(67,160,71));
            g.fillOval(ukuran-d+2,ukuran-d+2,d-4,d-4);
            g.setStroke(new BasicStroke(1f));
        }
        g.dispose();
        return img;
    }

    /** Dua huruf awal nama, gelar seperti "dr." atau "S.Kom" dilewati. */
    static String inisial(String nama){
        StringBuilder sb=new StringBuilder();
        for(String kata:nama.trim().split("\\s+")){
            if(kata.equals("")||kata.contains(".")||kata.contains(",")||!Character.isLetter(kata.charAt(0))){
                continue;
            }
            sb.append(Character.toUpperCase(kata.charAt(0)));
            if(sb.length()==2){
                break;
            }
        }
        if(sb.length()==0&&!nama.trim().equals("")){
            sb.append(Character.toUpperCase(nama.trim().charAt(0)));
        }
        return sb.toString();
    }
}
