package widget;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.TexturePaint;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.SwingUtilities;

/**
 * Odontogram standar PDGI (penomoran FDI, gigi tetap + gigi susu) yang bisa ditandai.
 * Geometri permukaan gigi diadaptasi dari pustaka https://github.com/pusilkom/odontogram (MIT License, (c) 2017 Pusilkom UI).
 *
 * Klik kiri  : pasang/lepas kondisi terpilih (kondisi permukaan pada permukaan yang diklik, kondisi gigi pada gigi yang diklik).
 * Klik kanan : hapus kondisi pada permukaan yang diklik, atau semua kondisi gigi bila diklik di luar permukaan.
 *
 * Format data (getData/setData): "gigi:permukaan:kode;..." misal "16:O:amf;11::rct;21:M:car".
 * Permukaan: M=mesial, D=distal, B=bukal/labial, L=lingual/palatal, O=oklusal; kosong = kondisi seluruh gigi.
 */
public class PanelOdontogram extends javax.swing.JPanel {
    private static final long serialVersionUID = 1L;

    // ===== Daftar kondisi PDGI =====
    public static final String JENIS_PERMUKAAN="P",JENIS_GIGI="G";

    /**
     * {kode, nama, jenis, grup, keterangan simbol/warna}. Kode dalam satu grup saling menggantikan pada gigi/permukaan yang sama.
     * Keterangan simbol ditampilkan di palet, cetakan dan riwayat perawatan sebagai legenda.
     */
    private static final String[][] KONDISI={
        {"car","Karies","P","car","Garis tepi hitam tebal pada permukaan"},
        {"amf","Tumpatan amalgam","P","isi","Permukaan diblok hitam"},
        {"cof","Tumpatan komposit","P","isi","Permukaan diblok hijau"},
        {"gif","Tumpatan glass ionomer","P","isi","Permukaan diarsir hijau"},
        {"fis","Pit & fissure sealant","P","isi","Permukaan diarsir merah muda"},
        {"inl","Inlay","P","isi","Permukaan diarsir hitam miring"},
        {"onl","Onlay","P","isi","Permukaan diarsir hitam silang"},
        {"non","Gigi tidak ada / tidak diketahui","G","label","Tulisan NON biru di sisi akar"},
        {"une","Un-erupted","G","label","Tulisan UNE biru di sisi akar"},
        {"pre","Partial erupt","G","label","Tulisan PRE biru di sisi akar"},
        {"ano","Anomali","G","label","Tulisan ANO biru di sisi akar"},
        {"ipx","Implant","G","label","Tulisan IPX biru di sisi akar"},
        {"prd","Partial denture","G","label","Tulisan PRD biru di sisi akar"},
        {"fld","Full denture","G","label","Tulisan FLD biru di sisi akar"},
        {"mis","Gigi hilang (missing)","G","xv","Tanda X hitam pada gigi"},
        {"rrx","Sisa akar","G","xv","Tanda V hitam pada gigi"},
        {"cfr","Fraktur","G","cfr","Tanda # hitam pada gigi"},
        {"nvt","Gigi non-vital","G","tri","Segitiga kosong di akar"},
        {"rct","Perawatan saluran akar","G","tri","Segitiga hitam di akar"},
        {"fmc","Full metal crown","G","crown","Kotak tepi hitam tebal"},
        {"poc","Porcelain crown","G","crown","Kotak tepi hitam tebal, isi hijau muda"},
        {"meb","Metal bridge","G","crown","Kotak tepi hitam tebal + garis penghubung di akar"},
        {"pob","Porcelain bridge","G","crown","Kotak isi hijau muda + garis penghubung di akar"},
        {"mgka","Migrasi ke kanan","G","arah","Panah merah ke kanan di akar"},
        {"mgki","Migrasi ke kiri","G","arah","Panah merah ke kiri di akar"},
        {"rtka","Rotasi searah jarum jam","G","arah","Panah melingkar merah searah jarum jam"},
        {"rtki","Rotasi berlawanan jarum jam","G","arah","Panah melingkar merah berlawanan jarum jam"}
    };

    public static final String[] PERMUKAAN={"M","D","B","L","O"};
    public static final String[] NAMA_PERMUKAAN={"Mesial","Distal","Bukal/Labial","Lingual/Palatal","Oklusal/Insisal"};

    // ===== Ukuran dasar (digambar pada skala 1, lalu diskalakan ke ukuran panel/cetak) =====
    private static final int W=40,NUM=16,LAB=16,ROOT=20,MX=12,MY=8,GAP=16,ROWGAP=8,MIDGAP=20;
    private static final int ROWH=NUM+LAB+ROOT+W;
    public static final int LEBAR_DASAR=MX*2+16*W+GAP;
    public static final int TINGGI_DASAR=MY*2+ROWH*4+ROWGAP*2+MIDGAP;

    private static final Color HIJAU=new Color(0,160,0),MERAH_MUDA=new Color(230,60,120),GARIS=new Color(40,40,40);
    private static final Font FONT_NO=new Font("Tahoma",Font.BOLD,10),FONT_LABEL=new Font("Tahoma",Font.BOLD,9);

    /** Posisi satu gigi pada diagram (koordinat dasar). */
    private static final class Gigi {
        final String no;
        final int baris,slot;
        final boolean atas,molar,kiriLayar;
        final Rectangle gigi,akar,label,nomor;
        Gigi(String no,int baris,int slot,int y){
            this.no=no;
            this.baris=baris;
            this.slot=slot;
            int kuadran=no.charAt(0)-'0';
            int urut=no.charAt(1)-'0';
            atas=(kuadran==1||kuadran==2||kuadran==5||kuadran==6);
            kiriLayar=(kuadran==1||kuadran==4||kuadran==5||kuadran==8);
            molar=urut>=4; // premolar & molar (gigi tetap 4-8, gigi susu 4-5): 5 permukaan
            int x=MX+slot*W+(slot>=8?GAP:0);
            if(atas){
                nomor=new Rectangle(x,y,W,NUM);
                label=new Rectangle(x,y+NUM,W,LAB);
                akar=new Rectangle(x,y+NUM+LAB,W,ROOT);
                gigi=new Rectangle(x,y+NUM+LAB+ROOT,W,W);
            }else{
                gigi=new Rectangle(x,y,W,W);
                akar=new Rectangle(x,y+W,W,ROOT);
                label=new Rectangle(x,y+W+ROOT,W,LAB);
                nomor=new Rectangle(x,y+W+ROOT+LAB,W,NUM);
            }
        }
        Rectangle area(){
            Rectangle r=new Rectangle(gigi);
            r.add(akar);r.add(label);r.add(nomor);
            return r;
        }
    }

    private static final List<Gigi> DIAGRAM=new ArrayList<>();
    private static final Map<String,Gigi> PETA=new LinkedHashMap<>();
    static {
        int y=MY;
        tambahBaris(new int[]{18,17,16,15,14,13,12,11,21,22,23,24,25,26,27,28},0,0,y);
        y+=ROWH+ROWGAP;
        tambahBaris(new int[]{55,54,53,52,51,61,62,63,64,65},1,3,y);
        y+=ROWH+MIDGAP;
        tambahBaris(new int[]{85,84,83,82,81,71,72,73,74,75},2,3,y);
        y+=ROWH+ROWGAP;
        tambahBaris(new int[]{48,47,46,45,44,43,42,41,31,32,33,34,35,36,37,38},3,0,y);
    }

    private static void tambahBaris(int[] nomor,int baris,int slotAwal,int y){
        for(int i=0;i<nomor.length;i++){
            Gigi g=new Gigi(String.valueOf(nomor[i]),baris,slotAwal+i,y);
            DIAGRAM.add(g);
            PETA.put(g.no,g);
        }
    }

    // ===== Data =====
    private static final class Entri {
        final String gigi,permukaan,kode;
        Entri(String gigi,String permukaan,String kode){this.gigi=gigi;this.permukaan=permukaan;this.kode=kode;}
    }

    private final List<Entri> entri=new ArrayList<>();
    private String kodeAktif="car";
    private boolean bisaDitandai=true;
    private String hoverGigi=null,hoverPermukaan=null;

    public PanelOdontogram(){
        super();
        setLayout(null);
        setBackground(Color.WHITE);
        setBorder(javax.swing.BorderFactory.createLineBorder(new Color(110,110,110)));
        setPreferredSize(new java.awt.Dimension(LEBAR_DASAR,TINGGI_DASAR));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setToolTipText("");
        MouseAdapter ma=new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if(!isEnabled()||!bisaDitandai){
                    return;
                }
                Point2D p=keDasar(e.getX(),e.getY());
                Gigi g=cariGigi(p);
                if(g==null){
                    return;
                }
                String perm=cariPermukaan(g,p);
                String lama=getData();
                if(SwingUtilities.isRightMouseButton(e)){
                    hapus(g.no,perm);
                }else if(SwingUtilities.isLeftMouseButton(e)){
                    pasang(g.no,perm);
                }
                repaint();
                firePropertyChange("data",lama,getData());
            }
            @Override
            public void mouseMoved(MouseEvent e) {
                Point2D p=keDasar(e.getX(),e.getY());
                Gigi g=cariGigi(p);
                String hg=g==null?null:g.no,hp=g==null?null:cariPermukaan(g,p);
                if(!sama(hg,hoverGigi)||!sama(hp,hoverPermukaan)){
                    hoverGigi=hg;
                    hoverPermukaan=hp;
                    repaint();
                }
            }
            @Override
            public void mouseExited(MouseEvent e) {
                hoverGigi=null;
                hoverPermukaan=null;
                repaint();
            }
        };
        addMouseListener(ma);
        addMouseMotionListener(ma);
    }

    private static boolean sama(String a,String b){
        return a==null?b==null:a.equals(b);
    }

    // ===== Info kondisi =====
    private static String[] kondisi(String kode){
        for(String[] k:KONDISI){
            if(k[0].equals(kode)){
                return k;
            }
        }
        return null;
    }

    public static boolean isKondisiPermukaan(String kode){
        String[] k=kondisi(kode);
        return k!=null&&k[2].equals(JENIS_PERMUKAAN);
    }

    public static String namaKondisi(String kode){
        String[] k=kondisi(kode);
        return k==null?kode:k[1];
    }

    /** Keterangan simbol/warna kondisi, misal "Permukaan diblok hitam". */
    public static String keteranganSimbol(String kode){
        String[] k=kondisi(kode);
        return k==null?"":k[4];
    }

    /** Kode yang dipakai pada data, urut seperti palet (untuk legenda cetakan). */
    public static List<String> kodeTerpakai(String data){
        List<Entri> daftar=urai(data);
        List<String> hasil=new ArrayList<>();
        for(String[] k:KONDISI){
            for(Entri e:daftar){
                if(e.kode.equals(k[0])){
                    hasil.add(k[0]);
                    break;
                }
            }
        }
        return hasil;
    }

    public static String namaPermukaan(String perm){
        for(int i=0;i<PERMUKAAN.length;i++){
            if(PERMUKAAN[i].equals(perm)){
                return NAMA_PERMUKAAN[i];
            }
        }
        return perm;
    }

    /** Kode semua kondisi, urut seperti palet. */
    public static String[] daftarKode(){
        String[] hasil=new String[KONDISI.length];
        for(int i=0;i<KONDISI.length;i++){
            hasil[i]=KONDISI[i][0];
        }
        return hasil;
    }

    /** Teks palet, misal "car - Karies". */
    public static String[] daftarKondisi(){
        String[] hasil=new String[KONDISI.length];
        for(int i=0;i<KONDISI.length;i++){
            hasil[i]=KONDISI[i][0]+" - "+KONDISI[i][1]+(KONDISI[i][2].equals(JENIS_PERMUKAAN)?" (permukaan)":"");
        }
        return hasil;
    }

    public static boolean isGigiValid(String no){
        return PETA.containsKey(no);
    }

    // ===== Ubah data =====
    private void pasang(String gigi,String perm){
        String[] k=kondisi(kodeAktif);
        if(k==null){
            return;
        }
        if(k[2].equals(JENIS_PERMUKAAN)){
            if(perm==null){
                return;
            }
        }else{
            perm="";
        }
        for(int i=0;i<entri.size();i++){
            Entri e=entri.get(i);
            if(e.gigi.equals(gigi)&&e.permukaan.equals(perm)&&e.kode.equals(kodeAktif)){
                entri.remove(i);
                return;
            }
        }
        for(int i=entri.size()-1;i>=0;i--){
            Entri e=entri.get(i);
            String[] ke=kondisi(e.kode);
            if(e.gigi.equals(gigi)&&e.permukaan.equals(perm)&&ke!=null&&ke[3].equals(k[3])){
                entri.remove(i);
            }
        }
        entri.add(new Entri(gigi,perm,kodeAktif));
    }

    private void hapus(String gigi,String perm){
        for(int i=entri.size()-1;i>=0;i--){
            Entri e=entri.get(i);
            if(e.gigi.equals(gigi)&&(perm==null||e.permukaan.equals(perm))){
                entri.remove(i);
            }
        }
    }

    private static List<Entri> urai(String data){
        List<Entri> hasil=new ArrayList<>();
        if(data==null){
            return hasil;
        }
        for(String bagian:data.split(";")){
            String[] isi=bagian.trim().split(":",-1);
            if(isi.length==3&&PETA.containsKey(isi[0].trim())&&kondisi(isi[2].trim())!=null){
                hasil.add(new Entri(isi[0].trim(),isi[1].trim(),isi[2].trim()));
            }
        }
        return hasil;
    }

    /** Data untuk disimpan, format "gigi:permukaan:kode;...". */
    public String getData(){
        return gabung(entri);
    }

    private static String gabung(List<Entri> daftar){
        StringBuilder sb=new StringBuilder();
        for(Entri e:daftar){
            if(sb.length()>0){
                sb.append(";");
            }
            sb.append(e.gigi).append(":").append(e.permukaan).append(":").append(e.kode);
        }
        return sb.toString();
    }

    public void setData(String data){
        String lama=getData();
        entri.clear();
        entri.addAll(urai(data));
        repaint();
        firePropertyChange("data",lama,getData());
    }

    /** Tambah satu kondisi (dipakai saat memuat dari tabel). */
    public void tambah(String gigi,String permukaan,String kode){
        if(PETA.containsKey(gigi)&&kondisi(kode)!=null){
            entri.add(new Entri(gigi,permukaan==null?"":permukaan,kode));
            repaint();
        }
    }

    public void hapusSemua(){
        setData("");
    }

    public int getJumlahEntri(){
        return entri.size();
    }

    /** Baris {gigi, permukaan, kode} untuk disimpan per baris ke tabel odontogram_detail. */
    public List<String[]> getEntri(){
        List<String[]> hasil=new ArrayList<>();
        for(Entri e:entri){
            hasil.add(new String[]{e.gigi,e.permukaan,e.kode});
        }
        return hasil;
    }

    public void setKodeAktif(String kode){
        if(kondisi(kode)!=null){
            kodeAktif=kode;
        }
    }

    public String getKodeAktif(){
        return kodeAktif;
    }

    public boolean isBisaDitandai(){
        return bisaDitandai;
    }

    public void setBisaDitandai(boolean bisa){
        bisaDitandai=bisa;
        setCursor(Cursor.getPredefinedCursor(bisa?Cursor.HAND_CURSOR:Cursor.DEFAULT_CURSOR));
    }

    /** Indeks DMF-T gigi tetap: {D, M, F}. */
    public int[] getDMF(){
        return hitungDMF(getData());
    }

    public static int[] hitungDMF(String data){
        Map<String,List<String>> perGigi=kelompok(urai(data));
        int d=0,m=0,f=0;
        for(Map.Entry<String,List<String>> en:perGigi.entrySet()){
            if(en.getKey().charAt(0)>'4'){
                continue;
            }
            List<String> k=en.getValue();
            if(k.contains("mis")){
                m++;
            }else if(k.contains("car")||k.contains("rrx")){
                d++;
            }else if(k.contains("amf")||k.contains("cof")||k.contains("gif")||k.contains("inl")||k.contains("onl")||
                    k.contains("fmc")||k.contains("poc")||k.contains("meb")||k.contains("pob")){
                f++;
            }
        }
        return new int[]{d,m,f};
    }

    private static Map<String,List<String>> kelompok(List<Entri> daftar){
        Map<String,List<String>> hasil=new LinkedHashMap<>();
        for(Gigi g:DIAGRAM){
            for(Entri e:daftar){
                if(e.gigi.equals(g.no)){
                    if(!hasil.containsKey(g.no)){
                        hasil.put(g.no,new ArrayList<String>());
                    }
                    hasil.get(g.no).add(e.kode);
                }
            }
        }
        return hasil;
    }

    /** Ringkasan per gigi, misal "16 : amf (O,M), rct". Satu baris per gigi, urut sesuai diagram. */
    public static List<String> ringkasan(String data){
        List<Entri> daftar=urai(data);
        List<String> hasil=new ArrayList<>();
        for(Gigi g:DIAGRAM){
            Map<String,StringBuilder> perKode=new LinkedHashMap<>();
            for(Entri e:daftar){
                if(e.gigi.equals(g.no)){
                    if(!perKode.containsKey(e.kode)){
                        perKode.put(e.kode,new StringBuilder());
                    }
                    if(!e.permukaan.equals("")){
                        StringBuilder sb=perKode.get(e.kode);
                        if(sb.length()>0){
                            sb.append(",");
                        }
                        sb.append(e.permukaan);
                    }
                }
            }
            if(!perKode.isEmpty()){
                StringBuilder baris=new StringBuilder(g.no).append(" : ");
                boolean awal=true;
                for(Map.Entry<String,StringBuilder> en:perKode.entrySet()){
                    if(!awal){
                        baris.append(", ");
                    }
                    awal=false;
                    baris.append(en.getKey()).append(" ").append(namaKondisi(en.getKey()));
                    if(en.getValue().length()>0){
                        baris.append(" (").append(en.getValue()).append(")");
                    }
                }
                hasil.add(baris.toString());
            }
        }
        return hasil;
    }

    // ===== Geometri =====
    private double skala(){
        return Math.min(getWidth()/(double)LEBAR_DASAR,getHeight()/(double)TINGGI_DASAR);
    }

    private AffineTransform transform(){
        double s=skala();
        AffineTransform at=new AffineTransform();
        at.translate((getWidth()-LEBAR_DASAR*s)/2,(getHeight()-TINGGI_DASAR*s)/2);
        at.scale(s,s);
        return at;
    }

    private Point2D keDasar(int x,int y){
        try {
            return transform().inverseTransform(new Point2D.Double(x,y),null);
        } catch (Exception e) {
            return new Point2D.Double(-1,-1);
        }
    }

    private static Gigi cariGigi(Point2D p){
        for(Gigi g:DIAGRAM){
            if(g.area().contains(p)){
                return g;
            }
        }
        return null;
    }

    /** Permukaan di titik p, null bila di luar kotak gigi. */
    private static String cariPermukaan(Gigi g,Point2D p){
        if(!g.gigi.contains(p)){
            return null;
        }
        for(String perm:PERMUKAAN){
            Shape s=bentuk(g,perm);
            if(s!=null&&s.contains(p)){
                return perm;
            }
        }
        return null;
    }

    /** Bagian geometris (ATAS, BAWAH, KIRI, KANAN, TENGAH) untuk permukaan anatomis. */
    private static String bagian(Gigi g,String perm){
        if(perm.equals("O")){
            return "TENGAH";
        }else if(perm.equals("B")){
            return g.atas?"ATAS":"BAWAH";
        }else if(perm.equals("L")){
            return g.atas?"BAWAH":"ATAS";
        }else if(perm.equals("M")){
            return g.kiriLayar?"KANAN":"KIRI";
        }else if(perm.equals("D")){
            return g.kiriLayar?"KIRI":"KANAN";
        }
        return "";
    }

    private static Shape bentuk(Gigi g,String perm){
        Rectangle r=g.gigi;
        double x=r.x,y=r.y,w=r.width,h=r.height,xw=x+w,yh=y+h,w4=w/4.0,h4=h/4.0,h2=h/2.0;
        String b=bagian(g,perm);
        Path2D.Double s=new Path2D.Double();
        if(g.molar){
            if(b.equals("TENGAH")){
                s.moveTo(x+w4,y+h4);s.lineTo(xw-w4,y+h4);s.lineTo(xw-w4,yh-h4);s.lineTo(x+w4,yh-h4);
            }else if(b.equals("ATAS")){
                s.moveTo(x,y);s.lineTo(x+w4,y+h4);s.lineTo(xw-w4,y+h4);s.lineTo(xw,y);
            }else if(b.equals("BAWAH")){
                s.moveTo(x,yh);s.lineTo(x+w4,yh-h4);s.lineTo(xw-w4,yh-h4);s.lineTo(xw,yh);
            }else if(b.equals("KIRI")){
                s.moveTo(x,y);s.lineTo(x+w4,y+h4);s.lineTo(x+w4,yh-h4);s.lineTo(x,yh);
            }else if(b.equals("KANAN")){
                s.moveTo(xw,y);s.lineTo(xw-w4,y+h4);s.lineTo(xw-w4,yh-h4);s.lineTo(xw,yh);
            }else{
                return null;
            }
        }else{
            // gigi anterior: 4 permukaan, tepi insisal berupa garis tengah (permukaan O tidak digambar terpisah)
            if(b.equals("ATAS")){
                s.moveTo(x,y);s.lineTo(x+w4,y+h2);s.lineTo(xw-w4,y+h2);s.lineTo(xw,y);
            }else if(b.equals("BAWAH")){
                s.moveTo(x,yh);s.lineTo(x+w4,y+h2);s.lineTo(xw-w4,y+h2);s.lineTo(xw,yh);
            }else if(b.equals("KIRI")){
                s.moveTo(x,y);s.lineTo(x+w4,y+h2);s.lineTo(x,yh);
            }else if(b.equals("KANAN")){
                s.moveTo(xw,y);s.lineTo(xw-w4,y+h2);s.lineTo(xw,yh);
            }else{
                return null;
            }
        }
        s.closePath();
        return s;
    }

    // ===== Gambar =====
    private static TexturePaint arsir(Color garis,Color latar,boolean silang){
        int n=6;
        BufferedImage img=new BufferedImage(n,n,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g=img.createGraphics();
        g.setColor(latar);
        g.fillRect(0,0,n,n);
        g.setColor(garis);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(new Line2D.Double(0,n,n,0));
        g.draw(new Line2D.Double(-1,1,1,-1));
        g.draw(new Line2D.Double(n-1,n+1,n+1,n-1));
        if(silang){
            g.draw(new Line2D.Double(0,0,n,n));
        }
        g.dispose();
        return new TexturePaint(img,new Rectangle2D.Double(0,0,n,n));
    }

    private static Paint isiPermukaan(String kode){
        if(kode.equals("amf")){
            return Color.BLACK;
        }else if(kode.equals("cof")){
            return HIJAU;
        }else if(kode.equals("gif")){
            return arsir(HIJAU,Color.WHITE,false);
        }else if(kode.equals("fis")){
            return arsir(MERAH_MUDA,Color.WHITE,false);
        }else if(kode.equals("inl")){
            return arsir(Color.BLACK,Color.WHITE,false);
        }else if(kode.equals("onl")){
            return arsir(Color.BLACK,Color.WHITE,true);
        }
        return null;
    }

    private static void gambarDiagram(Graphics2D g2,List<Entri> daftar,String hoverGigi,String hoverPerm){
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,RenderingHints.VALUE_STROKE_PURE);

        // garis bantu kuadran
        g2.setColor(new Color(200,200,200));
        g2.setStroke(new BasicStroke(1f,BasicStroke.CAP_BUTT,BasicStroke.JOIN_MITER,4f,new float[]{4f,3f},0f));
        double tengahX=MX+8*W+GAP/2.0,tengahY=MY+ROWH*2+ROWGAP+MIDGAP/2.0;
        g2.draw(new Line2D.Double(tengahX,MY,tengahX,TINGGI_DASAR-MY));
        g2.draw(new Line2D.Double(MX,tengahY,LEBAR_DASAR-MX,tengahY));

        Map<String,List<Entri>> perGigi=new LinkedHashMap<>();
        for(Entri e:daftar){
            if(!perGigi.containsKey(e.gigi)){
                perGigi.put(e.gigi,new ArrayList<Entri>());
            }
            perGigi.get(e.gigi).add(e);
        }

        for(Gigi g:DIAGRAM){
            List<Entri> isi=perGigi.containsKey(g.no)?perGigi.get(g.no):new ArrayList<Entri>();
            gambarGigi(g2,g,isi,perGigi,g.no.equals(hoverGigi)?hoverPerm:null,g.no.equals(hoverGigi));
        }
    }

    private static String kodeGrup(List<Entri> isi,String grup){
        for(Entri e:isi){
            String[] k=kondisi(e.kode);
            if(e.permukaan.equals("")&&k!=null&&k[3].equals(grup)){
                return e.kode;
            }
        }
        return null;
    }

    private static boolean adaJembatan(Map<String,List<Entri>> perGigi,Gigi g){
        if(g==null||!perGigi.containsKey(g.no)){
            return false;
        }
        String c=kodeGrup(perGigi.get(g.no),"crown");
        return "meb".equals(c)||"pob".equals(c);
    }

    private static Gigi tetangga(Gigi g,int arah){
        for(Gigi t:DIAGRAM){
            if(t.baris==g.baris&&t.slot==g.slot+arah){
                return t;
            }
        }
        return null;
    }

    private static void gambarGigi(Graphics2D g2,Gigi g,List<Entri> isi,Map<String,List<Entri>> perGigi,String hoverPerm,boolean hover){
        Rectangle r=g.gigi;
        BasicStroke tipis=new BasicStroke(1.2f),tebal=new BasicStroke(2.6f),sedang=new BasicStroke(1.8f);

        if(hover){
            g2.setColor(new Color(255,248,200));
            g2.fill(g.area());
        }

        // nomor gigi
        g2.setFont(FONT_NO);
        g2.setColor(g.no.charAt(0)>'4'?new Color(90,90,90):Color.BLACK);
        tulisTengah(g2,g.no,g.nomor);

        String crown=kodeGrup(isi,"crown");
        if("poc".equals(crown)||"pob".equals(crown)){
            g2.setColor(new Color(150,215,150));
            g2.fill(r);
        }

        // permukaan yang diisi
        for(Entri e:isi){
            if(e.permukaan.equals("")){
                continue;
            }
            Paint p=isiPermukaan(e.kode);
            Shape s=bentuk(g,e.permukaan);
            if(p!=null&&s!=null){
                g2.setPaint(p);
                g2.fill(s);
            }
        }
        if(hoverPerm!=null){
            Shape s=bentuk(g,hoverPerm);
            if(s!=null){
                g2.setColor(new Color(255,190,0,110));
                g2.fill(s);
            }
        }

        // garis dasar gigi
        g2.setColor(GARIS);
        g2.setStroke(tipis);
        for(String perm:PERMUKAAN){
            Shape s=bentuk(g,perm);
            if(s!=null){
                g2.draw(s);
            }
        }

        // karies: garis tebal mengelilingi permukaan
        for(Entri e:isi){
            if(e.kode.equals("car")){
                Shape s=bentuk(g,e.permukaan);
                if(s!=null){
                    g2.setColor(Color.BLACK);
                    g2.setStroke(new BasicStroke(3f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
                    g2.draw(s);
                }
            }
        }

        // mahkota / jembatan: kotak tebal
        if(crown!=null){
            g2.setColor(Color.BLACK);
            g2.setStroke(tebal);
            g2.draw(new Rectangle2D.Double(r.x+1.3,r.y+1.3,r.width-2.6,r.height-2.6));
        }

        // X / V / #
        String xv=kodeGrup(isi,"xv");
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(3f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
        if("mis".equals(xv)){
            g2.draw(new Line2D.Double(r.x+3,r.y+3,r.x+r.width-3,r.y+r.height-3));
            g2.draw(new Line2D.Double(r.x+r.width-3,r.y+3,r.x+3,r.y+r.height-3));
        }else if("rrx".equals(xv)){
            Path2D.Double v=new Path2D.Double();
            v.moveTo(r.x+4,r.y+4);
            v.lineTo(r.x+r.width/2.0,r.y+r.height-4);
            v.lineTo(r.x+r.width-4,r.y+4);
            g2.draw(v);
        }
        if(kodeGrup(isi,"cfr")!=null){
            double w5=r.width/5.0,h3=r.height/3.0;
            g2.setStroke(sedang);
            g2.draw(new Line2D.Double(r.x+2*w5,r.y+3,r.x+w5,r.y+r.height-3));
            g2.draw(new Line2D.Double(r.x+4*w5,r.y+3,r.x+3*w5,r.y+r.height-3));
            g2.draw(new Line2D.Double(r.x+3,r.y+h3,r.x+r.width-3,r.y+h3));
            g2.draw(new Line2D.Double(r.x+3,r.y+2*h3,r.x+r.width-3,r.y+2*h3));
        }

        // kotak akar: segitiga nvt/rct, garis jembatan, panah migrasi/rotasi
        Rectangle a=g.akar;
        String tri=kodeGrup(isi,"tri");
        if(tri!=null){
            Path2D.Double t=new Path2D.Double();
            double ujung=g.atas?a.y+2:a.y+a.height-2,dasar=g.atas?a.y+a.height-2:a.y+2;
            t.moveTo(a.x+a.width/3.0,dasar);
            t.lineTo(a.x+a.width*2/3.0,dasar);
            t.lineTo(a.x+a.width/2.0,ujung);
            t.closePath();
            g2.setColor(Color.BLACK);
            if(tri.equals("rct")){
                g2.fill(t);
            }
            g2.setStroke(sedang);
            g2.draw(t);
        }
        if(adaJembatan(perGigi,g)){
            double ty=a.y+a.height/2.0,cx=a.x+a.width/2.0;
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(3f));
            g2.draw(new Line2D.Double(cx,g.atas?a.y+a.height:a.y,cx,ty));
            Gigi kiri=tetangga(g,-1),kanan=tetangga(g,1);
            if(adaJembatan(perGigi,kiri)){
                g2.draw(new Line2D.Double(cx,ty,kiri.akar.x+kiri.akar.width/2.0,ty));
            }
            if(adaJembatan(perGigi,kanan)){
                g2.draw(new Line2D.Double(cx,ty,kanan.akar.x+kanan.akar.width/2.0,ty));
            }
        }
        String arah=kodeGrup(isi,"arah");
        if(arah!=null){
            gambarPanah(g2,arah,a);
        }

        // label kondisi gigi
        String label=kodeGrup(isi,"label");
        if(label!=null){
            g2.setFont(FONT_LABEL);
            g2.setColor(new Color(0,0,160));
            tulisTengah(g2,label.toUpperCase(),g.label);
        }
    }

    private static void gambarPanah(Graphics2D g2,String arah,Rectangle a){
        g2.setColor(new Color(170,0,0));
        g2.setStroke(new BasicStroke(1.8f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
        double cy=a.y+a.height/2.0,x1=a.x+5,x2=a.x+a.width-5,k=4;
        if(arah.equals("mgka")||arah.equals("mgki")){
            g2.draw(new Line2D.Double(x1,cy,x2,cy));
            double ux=arah.equals("mgka")?x2:x1,d=arah.equals("mgka")?-k:k;
            g2.draw(new Line2D.Double(ux,cy,ux+d,cy-k));
            g2.draw(new Line2D.Double(ux,cy,ux+d,cy+k));
        }else{
            double rw=a.width*0.45,rh=a.height*0.7,ax=a.x+(a.width-rw)/2.0,ay=a.y+(a.height-rh)/2.0;
            boolean searah=arah.equals("rtka");
            Arc2D.Double busur=new Arc2D.Double(ax,ay,rw,rh,searah?160:20,searah?-280:280,Arc2D.OPEN);
            g2.draw(busur);
            Point2D ujung=busur.getEndPoint();
            double ex=ujung.getX(),ey=ujung.getY(),d=searah?-k:k;
            g2.draw(new Line2D.Double(ex,ey,ex+d,ey-k*0.6));
            g2.draw(new Line2D.Double(ex,ey,ex+d*0.2,ey-k*1.2));
        }
    }

    private static void tulisTengah(Graphics2D g2,String teks,Rectangle r){
        FontMetrics fm=g2.getFontMetrics();
        g2.drawString(teks,(float)(r.x+(r.width-fm.stringWidth(teks))/2.0),(float)(r.y+(r.height-fm.getHeight())/2.0+fm.getAscent()));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2=(Graphics2D)g.create();
        try {
            g2.transform(transform());
            gambarDiagram(g2,entri,bisaDitandai&&isEnabled()?hoverGigi:null,bisaDitandai&&isEnabled()?hoverPermukaan:null);
        } finally {
            g2.dispose();
        }
    }

    @Override
    public String getToolTipText(MouseEvent e) {
        Point2D p=keDasar(e.getX(),e.getY());
        Gigi g=cariGigi(p);
        if(g==null){
            return null;
        }
        String perm=cariPermukaan(g,p);
        StringBuilder sb=new StringBuilder("<html><b>Gigi ").append(g.no).append("</b>");
        if(perm!=null){
            sb.append(" - ").append(namaPermukaan(perm));
        }
        for(Entri en:entri){
            if(en.gigi.equals(g.no)){
                sb.append("<br>").append(en.kode).append(" ").append(namaKondisi(en.kode));
                if(!en.permukaan.equals("")){
                    sb.append(" (").append(en.permukaan).append(")");
                }
            }
        }
        sb.append("<br><i>Klik kiri: pasang/lepas ").append(kodeAktif).append(", klik kanan: hapus</i></html>");
        return sb.toString();
    }

    // ===== Untuk cetak / HTML =====
    /** Gambar odontogram dengan lebar tertentu (tinggi mengikuti rasio diagram). */
    public static BufferedImage render(String data,int lebar){
        double s=lebar/(double)LEBAR_DASAR;
        int tinggi=(int)Math.round(TINGGI_DASAR*s);
        BufferedImage hasil=new BufferedImage(lebar,tinggi,BufferedImage.TYPE_INT_RGB);
        Graphics2D g2=hasil.createGraphics();
        try {
            g2.setColor(Color.WHITE);
            g2.fillRect(0,0,lebar,tinggi);
            g2.scale(s,s);
            gambarDiagram(g2,urai(data),null,null);
        } finally {
            g2.dispose();
        }
        return hasil;
    }

    /** InputStream PNG untuk parameter gambar laporan Jasper. */
    public static InputStream gambarCetak(String data,int lebar){
        try {
            ByteArrayOutputStream out=new ByteArrayOutputStream();
            ImageIO.write(render(data,lebar),"png",out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            System.out.println("Notifikasi PanelOdontogram : "+e);
            return null;
        }
    }

    /** URL file PNG sementara untuk HTML (riwayat perawatan, PDF). Nama file dari isi data, jadi data sama memakai file yang sama. */
    public static String urlGambar(String data,int lebar){
        try {
            java.io.File file=new java.io.File(System.getProperty("java.io.tmpdir"),
                    "khanza_odontogram_"+Integer.toHexString((data+"|"+lebar).hashCode())+".png");
            if(!file.exists()){
                ImageIO.write(render(data,lebar),"png",file);
                file.deleteOnExit();
            }
            return file.toURI().toURL().toString();
        } catch (Exception e) {
            System.out.println("Notifikasi PanelOdontogram : "+e);
            return "";
        }
    }

    /** Ikon contoh simbol kondisi untuk palet/legenda. */
    public static Icon ikon(final String kode,final int ukuran){
        return new Icon() {
            @Override
            public void paintIcon(Component c,Graphics g,int x,int y) {
                Graphics2D g2=(Graphics2D)g.create();
                try {
                    // gambar gigi molar atas 16 di area contoh, lalu skalakan ke ukuran ikon
                    Gigi contoh=PETA.get("16");
                    Rectangle area=new Rectangle(contoh.gigi);
                    String[] k=kondisi(kode);
                    if(k!=null&&(k[3].equals("tri")||k[3].equals("arah")||kode.equals("meb")||kode.equals("pob"))){
                        area.add(contoh.akar);
                    }else if(k!=null&&k[3].equals("label")){
                        area.add(contoh.akar);
                        area.add(contoh.label);
                    }
                    double s=ukuran/(double)Math.max(area.width,area.height);
                    g2.translate(x+(ukuran-area.width*s)/2.0,y+(ukuran-area.height*s)/2.0);
                    g2.scale(s,s);
                    g2.translate(-area.x,-area.y);
                    g2.clip(area);
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                    List<Entri> isi=new ArrayList<>();
                    isi.add(new Entri("16",isKondisiPermukaan(kode)?"O":"",kode));
                    Map<String,List<Entri>> peta=new LinkedHashMap<>();
                    peta.put("16",isi);
                    gambarGigi(g2,contoh,isi,peta,null,false);
                    if(kode.equals("meb")||kode.equals("pob")){
                        g2.setColor(Color.BLACK);
                        g2.setStroke(new BasicStroke(3f));
                        Rectangle a=contoh.akar;
                        g2.draw(new Line2D.Double(a.x,a.y+a.height/2.0,a.x+a.width,a.y+a.height/2.0));
                    }
                } finally {
                    g2.dispose();
                }
            }
            @Override
            public int getIconWidth() {
                return ukuran;
            }
            @Override
            public int getIconHeight() {
                return ukuran;
            }
        };
    }
}
