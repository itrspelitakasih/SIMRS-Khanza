package fungsi;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Merapikan teks alamat di laporan jasper: angka romawi dan RT/RW jadi huruf besar.
 * Romawi hanya memakai huruf I, V, X agar kata seperti "Di", "Mi", "Li" tidak ikut berubah.
 */
public class FormatAlamat {
    private static final Pattern ROMAWI=Pattern.compile("(?i)(?<![\\p{L}\\p{N}'])(?=[ivx])x{0,3}(ix|iv|v?i{0,3})(?![\\p{L}\\p{N}'])");
    private static final Pattern RTRW=Pattern.compile("(?i)(?<![\\p{L}\\p{N}'])r[tw](?![\\p{L}'])");

    public static String rapikan(String teks){
        if(teks==null){
            return null;
        }
        return hurufBesar(RTRW,hurufBesar(ROMAWI,teks));
    }

    private static String hurufBesar(Pattern pola,String teks){
        Matcher cocok=pola.matcher(teks);
        StringBuffer hasil=new StringBuffer();
        while(cocok.find()){
            cocok.appendReplacement(hasil,cocok.group().toUpperCase());
        }
        cocok.appendTail(hasil);
        return hasil.toString();
    }
}
