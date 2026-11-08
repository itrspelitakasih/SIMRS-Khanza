package fungsi;

import java.awt.Component;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.JOptionPane;

/**
 * Peringatan urutan pengisian rekam medis pasien IGD (poli UGD/UGD-K):
 * Triase -> Penilaian Awal Keperawatan IGD -> Penilaian Awal Medis IGD -> Pemeriksaan (CPPT/SOAP).
 *
 * Hanya peringatan: bila ada tahap sebelumnya yang belum diisi, user diminta konfirmasi
 * dan tetap boleh menyimpan (pasien kritis bisa ditangani dulu, dokumentasi menyusul).
 * Untuk registrasi selain IGD selalu lolos tanpa pertanyaan.
 */
public final class UrutanAsesmenIGD {
    public static final int TRIASE=1, KEPERAWATAN=2, MEDIS=3, PEMERIKSAAN=4;

    private static final String[] NAMA={"","Triase IGD","Penilaian Awal Keperawatan IGD","Penilaian Awal Medis IGD","Pemeriksaan (CPPT/SOAP)"};

    private UrutanAsesmenIGD(){}

    /**
     * @return true bila boleh lanjut menyimpan (urutan sesuai, bukan pasien IGD,
     *         atau user memilih tetap menyimpan), false bila user membatalkan.
     */
    public static boolean bolehSimpan(Component parent,String norawat,int tahap){
        if(norawat==null||norawat.trim().equals("")||tahap<=TRIASE){
            return true;
        }
        StringBuilder kurang=new StringBuilder();
        try (PreparedStatement ps=koneksiDB.condb().prepareStatement(
                "select reg_periksa.kd_poli in ('UGD','UGD-K'),"+
                "exists(select 1 from data_triase_igd where data_triase_igd.no_rawat=reg_periksa.no_rawat),"+
                "exists(select 1 from penilaian_awal_keperawatan_igd where penilaian_awal_keperawatan_igd.no_rawat=reg_periksa.no_rawat),"+
                "(exists(select 1 from penilaian_medis_igd where penilaian_medis_igd.no_rawat=reg_periksa.no_rawat) or "+
                " exists(select 1 from penilaian_medis_ralan_gawat_darurat_psikiatri where penilaian_medis_ralan_gawat_darurat_psikiatri.no_rawat=reg_periksa.no_rawat)) "+
                "from reg_periksa where reg_periksa.no_rawat=?")){
            ps.setString(1,norawat);
            try (ResultSet rs=ps.executeQuery()){
                if(!rs.next()||!rs.getBoolean(1)){
                    return true;
                }
                for(int i=TRIASE;i<tahap;i++){
                    if(!rs.getBoolean(i+1)){
                        kurang.append("- ").append(NAMA[i]).append("\n");
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi UrutanAsesmenIGD : "+e);
            return true;
        }
        if(kurang.length()==0){
            return true;
        }
        return JOptionPane.showConfirmDialog(parent,
                "Urutan pengisian IGD belum sesuai.\nSebelum "+NAMA[tahap]+", tahap berikut belum diisi:\n"+kurang+
                "\nUrutan: Triase -> Keperawatan -> Medis -> Pemeriksaan (CPPT).\nTetap simpan?",
                "Peringatan Urutan Asesmen IGD",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE)==JOptionPane.YES_OPTION;
    }
}
