package widget;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

/**
 * Gambar status lokalis yang bisa ditandai: klik kiri menambah tanda bernomor sesuai jenis aktif,
 * klik kanan menghapus tanda terdekat. Posisi disimpan relatif (0..1) terhadap gambar sehingga tetap
 * pas walau ukuran panel/cetakan berbeda.
 *
 * Format data (getData/setData): "kode,x,y;kode,x,y;..." misal "L,0.2510,0.4033;M,0.6120,0.7500".
 */
public class PanelLokalis extends javax.swing.JPanel {
    private static final long serialVersionUID = 1L;

    public static final String[] KODE={"L","M","F","B","N","LB","X"};
    public static final String[] NAMA={"Luka","Memar","Fraktur","Bengkak","Nyeri","Luka Bakar","Lainnya"};
    private static final Color[] WARNA={
        new Color(210,0,0),new Color(128,0,160),new Color(0,60,200),new Color(220,110,0),
        new Color(0,130,0),new Color(150,40,0),new Color(70,70,70)
    };

    private static final class Tanda {
        final String kode;
        final double x,y;
        Tanda(String kode,double x,double y){this.kode=kode;this.x=x;this.y=y;}
    }

    private final List<Tanda> tanda=new ArrayList<>();
    private String sumberGambar="/picture/semua.png";
    private Image gambar;
    private String jenisAktif=KODE[0];
    private boolean bisaDitandai=true;

    public PanelLokalis(){
        super();
        setLayout(null);
        setBackground(Color.WHITE);
        setBorder(javax.swing.BorderFactory.createLineBorder(new Color(110,110,110)));
        setFont(new Font("Tahoma",Font.BOLD,11));
        setToolTipText("Klik kiri: tambah tanda sesuai jenis terpilih, klik kanan: hapus tanda");
        setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        muatGambar();
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if(!isEnabled()||!bisaDitandai||getWidth()<=0||getHeight()<=0){
                    return;
                }
                String lama=getData();
                if(SwingUtilities.isRightMouseButton(e)){
                    hapusTerdekat(e.getX(),e.getY());
                }else if(SwingUtilities.isLeftMouseButton(e)){
                    tanda.add(new Tanda(jenisAktif,batas(e.getX()/(double)getWidth()),batas(e.getY()/(double)getHeight())));
                }
                repaint();
                firePropertyChange("data",lama,getData());
            }
        });
    }

    private void muatGambar(){
        gambar=null;
        try {
            java.net.URL url=PanelLokalis.class.getResource(sumberGambar);
            if(url!=null){
                gambar=new ImageIcon(url).getImage();
            }
        } catch (Exception e) {
            System.out.println("Notifikasi PanelLokalis : "+e);
        }
        repaint();
    }

    private void hapusTerdekat(int px,int py){
        int idx=-1;
        double jarak=Double.MAX_VALUE;
        for(int i=0;i<tanda.size();i++){
            double dx=tanda.get(i).x*getWidth()-px,dy=tanda.get(i).y*getHeight()-py,d=dx*dx+dy*dy;
            if(d<jarak){
                jarak=d;
                idx=i;
            }
        }
        if(idx>-1&&jarak<=20*20){
            tanda.remove(idx);
        }
    }

    private static double batas(double v){
        return v<0?0:(v>1?1:v);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2=(Graphics2D)g.create();
        try {
            if(gambar!=null){
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2.drawImage(gambar,0,0,getWidth(),getHeight(),this);
            }
            gambarTanda(g2,tanda,getWidth(),getHeight(),6,getFont());
        } finally {
            g2.dispose();
        }
    }

    private static void gambarTanda(Graphics2D g2,List<Tanda> daftar,int w,int h,int r,Font font){
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setFont(font);
        FontMetrics fm=g2.getFontMetrics();
        for(int i=0;i<daftar.size();i++){
            Tanda t=daftar.get(i);
            Color warna=warna(t.kode);
            int cx=(int)Math.round(t.x*w),cy=(int)Math.round(t.y*h);
            g2.setColor(Color.WHITE);
            g2.fillOval(cx-r-1,cy-r-1,2*r+2,2*r+2);
            g2.setColor(warna);
            g2.fillOval(cx-r,cy-r,2*r,2*r);

            String label=(i+1)+t.kode;
            int lw=fm.stringWidth(label)+6,lh=fm.getHeight();
            int lx=cx+r+2,ly=cy-r-lh+2;
            if(lx+lw>w){
                lx=cx-r-2-lw;
            }
            if(ly<0){
                ly=cy+r-2;
            }
            g2.setColor(new Color(255,255,255,220));
            g2.fillRoundRect(lx,ly,lw,lh,6,6);
            g2.setColor(warna);
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(lx,ly,lw,lh,6,6);
            g2.drawString(label,lx+3,ly+fm.getAscent());
        }
    }

    private static Color warna(String kode){
        for(int i=0;i<KODE.length;i++){
            if(KODE[i].equals(kode)){
                return WARNA[i];
            }
        }
        return WARNA[WARNA.length-1];
    }

    private static List<Tanda> urai(String data){
        List<Tanda> hasil=new ArrayList<>();
        if(data==null){
            return hasil;
        }
        for(String bagian:data.split(";")){
            String[] isi=bagian.trim().split(",");
            if(isi.length==3){
                try {
                    hasil.add(new Tanda(isi[0].trim(),batas(Double.parseDouble(isi[1].trim())),batas(Double.parseDouble(isi[2].trim()))));
                } catch (NumberFormatException e) {
                    // abaikan bagian yang rusak
                }
            }
        }
        return hasil;
    }

    /** Data tanda untuk disimpan ke database. */
    public String getData(){
        StringBuilder sb=new StringBuilder();
        for(Tanda t:tanda){
            if(sb.length()>0){
                sb.append(";");
            }
            sb.append(t.kode).append(",").append(String.format(Locale.US,"%.4f,%.4f",t.x,t.y));
        }
        return sb.toString();
    }

    public void setData(String data){
        String lama=getData();
        tanda.clear();
        tanda.addAll(urai(data));
        repaint();
        firePropertyChange("data",lama,getData());
    }

    public void hapusSemua(){
        setData("");
    }

    public int getJumlahTanda(){
        return tanda.size();
    }

    /** Kode jenis aktif (lihat KODE) untuk klik berikutnya. */
    public void setJenisAktif(String kode){
        jenisAktif=(kode==null||kode.trim().equals(""))?KODE[0]:kode.trim();
    }

    public String getJenisAktif(){
        return jenisAktif;
    }

    /** Pilih jenis aktif berdasarkan indeks NAMA/KODE, cocok untuk ComboBox berisi daftarJenis(). */
    public void setJenisAktif(int indeks){
        if(indeks>=0&&indeks<KODE.length){
            jenisAktif=KODE[indeks];
        }
    }

    /** Isi ComboBox jenis tanda, misal "L - Luka". */
    public static String[] daftarJenis(){
        String[] hasil=new String[KODE.length];
        for(int i=0;i<KODE.length;i++){
            hasil[i]=KODE[i]+" - "+NAMA[i];
        }
        return hasil;
    }

    /** Keterangan singkat kode, misal "L=Luka, M=Memar, ...". */
    public static String legenda(){
        StringBuilder sb=new StringBuilder();
        for(int i=0;i<KODE.length;i++){
            if(i>0){
                sb.append(", ");
            }
            sb.append(KODE[i]).append("=").append(NAMA[i]);
        }
        return sb.toString();
    }

    public boolean isBisaDitandai(){
        return bisaDitandai;
    }

    public void setBisaDitandai(boolean bisa){
        bisaDitandai=bisa;
        setCursor(Cursor.getPredefinedCursor(bisa?Cursor.CROSSHAIR_CURSOR:Cursor.DEFAULT_CURSOR));
    }

    public String getSumberGambar(){
        return sumberGambar;
    }

    /** Path resource gambar dasar, default "/picture/semua.png". */
    public void setSumberGambar(String sumber){
        sumberGambar=sumber;
        muatGambar();
    }

    /**
     * Gambar untuk laporan Jasper (parameter InputStream): gambar dasar diregangkan ke lebar x tinggi,
     * lalu tanda digambar tanpa ikut terdistorsi. Samakan rasio lebar:tinggi dengan elemen image di jrxml.
     */
    public static InputStream gambarCetak(String sumber,String data,int lebar,int tinggi){
        try {
            ByteArrayOutputStream out=new ByteArrayOutputStream();
            ImageIO.write(render(sumber,data,lebar,tinggi,Math.max(1f,tinggi/120f)),"png",out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            System.out.println("Notifikasi PanelLokalis : "+e);
            return PanelLokalis.class.getResourceAsStream(sumber);
        }
    }

    /**
     * URL gambar untuk HTML (riwayat perawatan, PDF html2pdf): bila ada tanda, gambar ditulis ke file PNG
     * sementara (nama dari isi data, jadi data sama memakai file yang sama); bila tidak ada tanda atau gagal,
     * kembali ke URL resource gambar dasar. skala = ukuran tanda/teks (1 = seperti di form).
     */
    public static String urlGambar(String sumber,String data,int lebar,int tinggi,float skala){
        String asli=String.valueOf(PanelLokalis.class.getResource(sumber));
        if(urai(data).isEmpty()){
            return asli;
        }
        try {
            java.io.File file=new java.io.File(System.getProperty("java.io.tmpdir"),
                    "khanza_lokalis_"+Integer.toHexString((sumber+"|"+data+"|"+lebar+"x"+tinggi+"|"+skala).hashCode())+".png");
            if(!file.exists()){
                ImageIO.write(render(sumber,data,lebar,tinggi,skala),"png",file);
                file.deleteOnExit();
            }
            return file.toURI().toURL().toString();
        } catch (Exception e) {
            System.out.println("Notifikasi PanelLokalis : "+e);
            return asli;
        }
    }

    private static BufferedImage render(String sumber,String data,int lebar,int tinggi,float skala) throws java.io.IOException {
        BufferedImage hasil=new BufferedImage(lebar,tinggi,BufferedImage.TYPE_INT_RGB);
        Graphics2D g2=hasil.createGraphics();
        try {
            g2.setColor(Color.WHITE);
            g2.fillRect(0,0,lebar,tinggi);
            java.net.URL url=PanelLokalis.class.getResource(sumber);
            if(url!=null){
                BufferedImage dasar=ImageIO.read(url);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g2.drawImage(dasar,0,0,lebar,tinggi,null);
            }
            gambarTanda(g2,urai(data),lebar,tinggi,Math.round(4*skala),new Font("Tahoma",Font.BOLD,Math.round(9*skala)));
        } finally {
            g2.dispose();
        }
        return hasil;
    }
}
