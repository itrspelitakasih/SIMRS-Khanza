package surat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Nomor surat format RS Pelita Kasih: NNN/KODE/RJ-RSPK/BULAN_ROMAWI/TAHUN (mis. 001/SKB/RJ-RSPK/X/2026),
 * nomor urut mulai dari 001 lagi setiap bulan, sama seperti Surat Sakit / Surat Keterangan Sehat.
 */
public final class NomorSuratRSPK {
    private static final String[] ROMAWI={"I","II","III","IV","V","VI","VII","VIII","IX","X","XI","XII"};

    private NomorSuratRSPK() {}

    /**
     * @param tabel tabel surat yang punya kolom no_surat
     * @param kode  kode jenis surat, mis. SKB
     * @param tanggal tanggal surat format yyyy-MM-dd
     */
    public static String berikutnya(Connection koneksi,String tabel,String kode,String tanggal){
        String pola="/"+kode+"/RJ-RSPK/"+ROMAWI[Integer.parseInt(tanggal.substring(5,7))-1]+"/"+tanggal.substring(0,4);
        int terakhir=0;
        try (PreparedStatement ps=koneksi.prepareStatement(
                "select ifnull(max(cast(substring_index("+tabel+".no_surat,'/',1) as unsigned)),0) from "+tabel+" where "+tabel+".no_surat like ?")){
            ps.setString(1,"%"+pola);
            try (ResultSet rs=ps.executeQuery()){
                if(rs.next()){
                    terakhir=rs.getInt(1);
                }
            }
        } catch (Exception e) {
            System.out.println("Notif nomor surat : "+e);
        }
        return String.format("%03d",terakhir+1)+pola;
    }

    /** Lebarkan kolom no_surat bila masih lebih pendek dari panjang (butuh hak ALTER); format baru bisa sampai 26 karakter. */
    public static void lebarkanKolom(Connection koneksi,String tabel,int panjang){
        try (PreparedStatement ps=koneksi.prepareStatement(
                "select character_maximum_length from information_schema.columns where table_schema=database() and table_name=? and column_name='no_surat'")){
            ps.setString(1,tabel);
            try (ResultSet rs=ps.executeQuery()){
                if(rs.next()&&rs.getInt(1)<panjang){
                    try (Statement st=koneksi.createStatement()){
                        st.executeUpdate("ALTER TABLE `"+tabel+"` MODIFY `no_surat` varchar("+panjang+") NOT NULL");
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Notif lebar no_surat "+tabel+" : "+e);
        }
    }
}
