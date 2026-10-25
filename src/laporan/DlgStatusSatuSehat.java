/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DlgStatusSatuSehat.java
 *
 * Duplikasi dari DlgStatusDataRM untuk memantau status kiriman data ke Satu Sehat
 * per kunjungan. Tiap kolom resource membandingkan jumlah data sumber di SIMRS
 * (yang memenuhi syarat dikirim, termasuk sudah dimapping) dengan jumlah ID yang
 * sudah tersimpan di tabel satu_sehat_*.
 */

package laporan;

import fungsi.WarnaTable;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import fungsi.akses;
import fungsi.batasInput;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import simrskhanza.DlgCariCaraBayar;
import simrskhanza.DlgCariPoli;

/**
 *
 * @author perpustakaan
 */
public final class DlgStatusSatuSehat extends javax.swing.JDialog {
    private final DefaultTableModel tabMode;
    private Connection koneksi=koneksiDB.condb();
    private sekuel Sequel=new sekuel();
    private validasi Valid=new validasi();
    private PreparedStatement ps;
    private ResultSet rs;
    private int i=0,j=0,sumber=0,terkirim=0;
    private String pilihan="",status="";
    private boolean lengkap=true;
    private StringBuilder htmlContent;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean ceksukses = false;
    private static final int KOLOMAWAL=7;
    private static final String SUDAH="Terkirim",BELUM="Belum",SEBAGIAN="Sebagian";
    private static final String KIRIMTTV=
        "select count(*) from ("+
            "select status,tgl_perawatan,jam_rawat from satu_sehat_observationttvsuhu where no_rawat=? and id_observation<>'' union "+
            "select status,tgl_perawatan,jam_rawat from satu_sehat_observationttvtensi where no_rawat=? and id_observation<>'' union "+
            "select status,tgl_perawatan,jam_rawat from satu_sehat_observationttvnadi where no_rawat=? and id_observation<>'' union "+
            "select status,tgl_perawatan,jam_rawat from satu_sehat_observationttvrespirasi where no_rawat=? and id_observation<>'' union "+
            "select status,tgl_perawatan,jam_rawat from satu_sehat_observationttvspo2 where no_rawat=? and id_observation<>'' union "+
            "select status,tgl_perawatan,jam_rawat from satu_sehat_observationttvbb where no_rawat=? and id_observation<>'' union "+
            "select status,tgl_perawatan,jam_rawat from satu_sehat_observationttvtb where no_rawat=? and id_observation<>'' union "+
            "select status,tgl_perawatan,jam_rawat from satu_sehat_observationttvlp where no_rawat=? and id_observation<>'' union "+
            "select status,tgl_perawatan,jam_rawat from satu_sehat_observationttvgcs where no_rawat=? and id_observation<>'' union "+
            "select status,tgl_perawatan,jam_rawat from satu_sehat_observationttvkesadaran where no_rawat=? and id_observation<>''"+
        ") ttv";

    // Item order yang sudah dimapping (sama dengan syarat join di form Kirim masing-masing).
    private static final String ORDERLAB=
        "select count(*) from permintaan_lab inner join permintaan_detail_permintaan_lab on permintaan_detail_permintaan_lab.noorder=permintaan_lab.noorder "+
        "inner join satu_sehat_mapping_lab on satu_sehat_mapping_lab.id_template=permintaan_detail_permintaan_lab.id_template where permintaan_lab.no_rawat=?";
    private static final String HASILLAB=ORDERLAB+
        " and exists(select 1 from periksa_lab where periksa_lab.no_rawat=permintaan_lab.no_rawat and periksa_lab.tgl_periksa=permintaan_lab.tgl_hasil)";
    private static final String ORDERLABMB=
        "select count(*) from permintaan_labmb inner join permintaan_detail_permintaan_labmb on permintaan_detail_permintaan_labmb.noorder=permintaan_labmb.noorder "+
        "inner join satu_sehat_mapping_lab on satu_sehat_mapping_lab.id_template=permintaan_detail_permintaan_labmb.id_template where permintaan_labmb.no_rawat=?";
    private static final String HASILLABMB=ORDERLABMB+
        " and exists(select 1 from periksa_lab where periksa_lab.no_rawat=permintaan_labmb.no_rawat and periksa_lab.tgl_periksa=permintaan_labmb.tgl_hasil)";
    private static final String ORDERRAD=
        "select count(*) from permintaan_radiologi inner join permintaan_pemeriksaan_radiologi on permintaan_pemeriksaan_radiologi.noorder=permintaan_radiologi.noorder "+
        "inner join satu_sehat_mapping_radiologi on satu_sehat_mapping_radiologi.kd_jenis_prw=permintaan_pemeriksaan_radiologi.kd_jenis_prw where permintaan_radiologi.no_rawat=?";
    private static final String HASILRAD=ORDERRAD+
        " and exists(select 1 from periksa_radiologi where periksa_radiologi.no_rawat=permintaan_radiologi.no_rawat and periksa_radiologi.tgl_periksa=permintaan_radiologi.tgl_hasil)";

    // {judul kolom, sql jumlah data sumber, sql jumlah yang sudah punya ID Satu Sehat, tabel/kolom penyimpan ID}.
    // Semua parameter "?" = no_rawat. Tabel penyimpan dicek dulu karena sebagian dibuat otomatis oleh form Kirim-nya.
    private static final String[][] RESOURCE={
        {"Encounter",
            "select count(*) from reg_periksa where reg_periksa.no_rawat=?",
            "select count(*) from satu_sehat_encounter where satu_sehat_encounter.no_rawat=? and satu_sehat_encounter.id_encounter<>''",
            "satu_sehat_encounter"},
        {"Condition",
            "select count(*) from diagnosa_pasien where diagnosa_pasien.no_rawat=?",
            "select count(*) from satu_sehat_condition where satu_sehat_condition.no_rawat=? and satu_sehat_condition.id_condition<>''",
            "satu_sehat_condition"},
        {"EpisodeOfCare",
            "select count(*) from diagnosa_pasien where diagnosa_pasien.no_rawat=?",
            "select count(*) from satu_sehat_episode_of_care where satu_sehat_episode_of_care.no_rawat=? and satu_sehat_episode_of_care.id_episode_of_care<>''",
            "satu_sehat_episode_of_care"},
        {"Procedure",
            "select count(*) from prosedur_pasien where prosedur_pasien.no_rawat=?",
            "select count(*) from satu_sehat_procedure where satu_sehat_procedure.no_rawat=? and satu_sehat_procedure.id_procedure<>''",
            "satu_sehat_procedure"},
        {"Observation TTV",
            "select (select count(*) from pemeriksaan_ralan where pemeriksaan_ralan.no_rawat=? and (pemeriksaan_ralan.suhu_tubuh<>'' or pemeriksaan_ralan.tensi<>'' or "+
            "pemeriksaan_ralan.nadi<>'' or pemeriksaan_ralan.respirasi<>'' or pemeriksaan_ralan.spo2<>'' or pemeriksaan_ralan.berat<>'' or pemeriksaan_ralan.tinggi<>'' or "+
            "pemeriksaan_ralan.lingkar_perut<>''))+(select count(*) from pemeriksaan_ranap where pemeriksaan_ranap.no_rawat=? and (pemeriksaan_ranap.suhu_tubuh<>'' or "+
            "pemeriksaan_ranap.tensi<>'' or pemeriksaan_ranap.nadi<>'' or pemeriksaan_ranap.respirasi<>'' or pemeriksaan_ranap.spo2<>'' or pemeriksaan_ranap.berat<>'' or "+
            "pemeriksaan_ranap.tinggi<>''))",
            KIRIMTTV,
            "satu_sehat_observationttvsuhu"},
        {"Clinical Impression",
            "select (select count(*) from pemeriksaan_ralan where pemeriksaan_ralan.no_rawat=? and pemeriksaan_ralan.penilaian<>'')+"+
            "(select count(*) from pemeriksaan_ranap where pemeriksaan_ranap.no_rawat=? and pemeriksaan_ranap.penilaian<>'')",
            "select count(*) from satu_sehat_clinicalimpression where satu_sehat_clinicalimpression.no_rawat=? and satu_sehat_clinicalimpression.id_clinicalimpression<>''",
            "satu_sehat_clinicalimpression"},
        {"CarePlan",
            "select (select count(*) from pemeriksaan_ralan where pemeriksaan_ralan.no_rawat=? and pemeriksaan_ralan.rtl<>'')+"+
            "(select count(*) from pemeriksaan_ranap where pemeriksaan_ranap.no_rawat=? and pemeriksaan_ranap.rtl<>'')",
            "select count(*) from satu_sehat_careplan where satu_sehat_careplan.no_rawat=? and satu_sehat_careplan.id_careplan<>''",
            "satu_sehat_careplan"},
        {"Alergi",
            "select (select count(*) from pemeriksaan_ralan where pemeriksaan_ralan.no_rawat=? and pemeriksaan_ralan.alergi<>'')+"+
            "(select count(*) from pemeriksaan_ranap where pemeriksaan_ranap.no_rawat=? and pemeriksaan_ranap.alergi<>'')",
            "select count(*) from satu_sehat_allergy_intolerance where satu_sehat_allergy_intolerance.no_rawat=? and satu_sehat_allergy_intolerance.id_allergy_intolerance<>''",
            "satu_sehat_allergy_intolerance"},
        {"Riwayat Keluarga",
            "select count(*) from penilaian_awal_keperawatan_ralan where penilaian_awal_keperawatan_ralan.no_rawat=? and penilaian_awal_keperawatan_ralan.rpk<>'' and penilaian_awal_keperawatan_ralan.rpk<>'-'",
            "select count(*) from satu_sehat_familymemberhistory where satu_sehat_familymemberhistory.no_rawat=? and satu_sehat_familymemberhistory.id_familymemberhistory<>''",
            "satu_sehat_familymemberhistory"},
        {"Goal",
            "select count(*) from penilaian_awal_keperawatan_ralan where penilaian_awal_keperawatan_ralan.no_rawat=? and penilaian_awal_keperawatan_ralan.rencana<>''",
            "select count(*) from satu_sehat_goal where satu_sehat_goal.no_rawat=? and satu_sehat_goal.id_goal<>''",
            "satu_sehat_goal"},
        {"Risk Assessment",
            "select count(*) from penilaian_awal_keperawatan_ralan where penilaian_awal_keperawatan_ralan.no_rawat=?",
            "select count(*) from satu_sehat_riskassessment where satu_sehat_riskassessment.no_rawat=? and satu_sehat_riskassessment.id_riskassessment<>''",
            "satu_sehat_riskassessment"},
        {"ANC",
            "select if(exists(select 1 from penilaian_awal_keperawatan_kebidanan where penilaian_awal_keperawatan_kebidanan.no_rawat=?),1,0)",
            "select if(exists(select 1 from satu_sehat_anc_observation where satu_sehat_anc_observation.no_rawat=? and satu_sehat_anc_observation.id_observation<>''),1,0)",
            "satu_sehat_anc_observation"},
        {"Diet",
            "select count(*) from catatan_adime_gizi where catatan_adime_gizi.no_rawat=? and catatan_adime_gizi.instruksi<>''",
            "select count(*) from satu_sehat_diet where satu_sehat_diet.no_rawat=? and satu_sehat_diet.id_diet<>''",
            "satu_sehat_diet"},
        {"Imunisasi",
            "select count(*) from detail_pemberian_obat inner join satu_sehat_mapping_vaksin on satu_sehat_mapping_vaksin.kode_brng=detail_pemberian_obat.kode_brng "+
            "where detail_pemberian_obat.no_rawat=?",
            "select count(*) from satu_sehat_immunization where satu_sehat_immunization.no_rawat=? and satu_sehat_immunization.id_immunization<>''",
            "satu_sehat_immunization"},
        {"Permintaan Lab",ORDERLAB,kirimOrder("satu_sehat_servicerequest_lab","id_servicerequest","permintaan_lab"),"satu_sehat_servicerequest_lab"},
        {"Specimen Lab",ORDERLAB,kirimOrder("satu_sehat_specimen_lab","id_specimen","permintaan_lab"),"satu_sehat_specimen_lab"},
        {"Observation Lab",HASILLAB,kirimOrder("satu_sehat_observation_lab","id_observation","permintaan_lab"),"satu_sehat_observation_lab"},
        {"Hasil Lab",HASILLAB,kirimOrder("satu_sehat_diagnosticreport_lab","id_diagnosticreport","permintaan_lab"),"satu_sehat_diagnosticreport_lab"},
        {"Permintaan Lab MB",ORDERLABMB,kirimOrder("satu_sehat_servicerequest_lab_mb","id_servicerequest","permintaan_labmb"),"satu_sehat_servicerequest_lab_mb"},
        {"Specimen Lab MB",ORDERLABMB,kirimOrder("satu_sehat_specimen_lab_mb","id_specimen","permintaan_labmb"),"satu_sehat_specimen_lab_mb"},
        {"Observation Lab MB",HASILLABMB,kirimOrder("satu_sehat_observation_lab_mb","id_observation","permintaan_labmb"),"satu_sehat_observation_lab_mb"},
        {"Hasil Lab MB",HASILLABMB,kirimOrder("satu_sehat_diagnosticreport_lab_mb","id_diagnosticreport","permintaan_labmb"),"satu_sehat_diagnosticreport_lab_mb"},
        {"Permintaan Rad",ORDERRAD,kirimOrder("satu_sehat_servicerequest_radiologi","id_servicerequest","permintaan_radiologi"),"satu_sehat_servicerequest_radiologi"},
        {"Specimen Rad",ORDERRAD,kirimOrder("satu_sehat_specimen_radiologi","id_specimen","permintaan_radiologi"),"satu_sehat_specimen_radiologi"},
        {"Observation Rad",HASILRAD,kirimOrder("satu_sehat_observation_radiologi","id_observation","permintaan_radiologi"),"satu_sehat_observation_radiologi"},
        {"Hasil Rad",HASILRAD,kirimOrder("satu_sehat_diagnosticreport_radiologi","id_diagnosticreport","permintaan_radiologi"),"satu_sehat_diagnosticreport_radiologi"},
        {"Resep Obat",
            "select count(*) from resep_obat inner join resep_dokter on resep_dokter.no_resep=resep_obat.no_resep "+
            "inner join satu_sehat_mapping_obat on satu_sehat_mapping_obat.kode_brng=resep_dokter.kode_brng where resep_obat.no_rawat=?",
            "select count(*) from satu_sehat_medicationrequest inner join resep_obat on resep_obat.no_resep=satu_sehat_medicationrequest.no_resep "+
            "where resep_obat.no_rawat=? and satu_sehat_medicationrequest.id_medicationrequest<>''",
            "satu_sehat_medicationrequest"},
        {"Pemberian Obat",
            "select count(*) from detail_pemberian_obat inner join satu_sehat_mapping_obat on satu_sehat_mapping_obat.kode_brng=detail_pemberian_obat.kode_brng "+
            "where detail_pemberian_obat.no_rawat=?",
            "select count(*) from satu_sehat_medicationdispense where satu_sehat_medicationdispense.no_rawat=? and satu_sehat_medicationdispense.id_medicationdispanse<>''",
            "satu_sehat_medicationdispense"},
        {"Medication Statement",
            "select count(*) from resep_obat inner join resep_dokter on resep_dokter.no_resep=resep_obat.no_resep "+
            "inner join satu_sehat_mapping_obat on satu_sehat_mapping_obat.kode_brng=resep_dokter.kode_brng where resep_obat.no_rawat=? and resep_obat.tgl_penyerahan<>'0000-00-00'",
            "select count(*) from satu_sehat_medicationstatement inner join resep_obat on resep_obat.no_resep=satu_sehat_medicationstatement.no_resep "+
            "where resep_obat.no_rawat=? and satu_sehat_medicationstatement.id_medicationstatement<>''",
            "satu_sehat_medicationstatement"},
        {"Telaah Farmasi",
            "select count(*) from resep_obat inner join telaah_farmasi on telaah_farmasi.no_resep=resep_obat.no_resep where resep_obat.no_rawat=?",
            "select count(*) from satu_sehat_questionresponse_telaah_farmasi inner join resep_obat on resep_obat.no_resep=satu_sehat_questionresponse_telaah_farmasi.no_resep "+
            "where resep_obat.no_rawat=? and satu_sehat_questionresponse_telaah_farmasi.id_questionresponse<>''",
            "satu_sehat_questionresponse_telaah_farmasi"},
        {"Questionnaire Response",
            "select if(exists(select 1 from resep_obat inner join telaah_farmasi on telaah_farmasi.no_resep=resep_obat.no_resep where resep_obat.no_rawat=?),1,0)",
            "select if(exists(select 1 from satu_sehat_questionnaireresponse where satu_sehat_questionnaireresponse.no_rawat=? and satu_sehat_questionnaireresponse.id_questionnaireresponse<>''),1,0)",
            "satu_sehat_questionnaireresponse"},
        {"Composition Triase IGD",
            "select count(*) from data_triase_igd where data_triase_igd.no_rawat=?",
            "select count(*) from data_triase_igd where data_triase_igd.no_rawat=? and data_triase_igd.id_composition<>''",
            "data_triase_igd.id_composition"},
        {"RME Rawat Jalan",
            "select count(*) from resume_pasien where resume_pasien.no_rawat=?",
            "select count(*) from satu_sehat_rme_rawat_jalan where satu_sehat_rme_rawat_jalan.no_rawat=? and satu_sehat_rme_rawat_jalan.id_composition<>''",
            "satu_sehat_rme_rawat_jalan"},
        {"RME Rawat Inap",
            "select count(*) from resume_pasien_ranap where resume_pasien_ranap.no_rawat=?",
            "select count(*) from satu_sehat_rme_rawat_inap where satu_sehat_rme_rawat_inap.no_rawat=? and satu_sehat_rme_rawat_inap.id_composition<>''",
            "satu_sehat_rme_rawat_inap"}
    };

    // {nama resource, sql keterangan & ID Satu Sehat yang tersimpan, tabel/kolom penyimpan ID}. Semua parameter "?" = no_rawat.
    private static final String[][] DETAILID={
        {"Encounter","select '',satu_sehat_encounter.id_encounter from satu_sehat_encounter where satu_sehat_encounter.no_rawat=?","satu_sehat_encounter"},
        {"Condition","select concat(kd_penyakit,' (',status,')'),id_condition from satu_sehat_condition where no_rawat=?","satu_sehat_condition"},
        {"EpisodeOfCare","select concat(kd_penyakit,' (',status,')'),id_episode_of_care from satu_sehat_episode_of_care where no_rawat=?","satu_sehat_episode_of_care"},
        {"Procedure","select concat(kode,' (',status,')'),id_procedure from satu_sehat_procedure where no_rawat=?","satu_sehat_procedure"},
        {"Observation Suhu","select concat(tgl_perawatan,' ',jam_rawat,' ',status),id_observation from satu_sehat_observationttvsuhu where no_rawat=?","satu_sehat_observationttvsuhu"},
        {"Observation Tensi","select concat(tgl_perawatan,' ',jam_rawat,' ',status),id_observation from satu_sehat_observationttvtensi where no_rawat=?","satu_sehat_observationttvtensi"},
        {"Observation Nadi","select concat(tgl_perawatan,' ',jam_rawat,' ',status),id_observation from satu_sehat_observationttvnadi where no_rawat=?","satu_sehat_observationttvnadi"},
        {"Observation Respirasi","select concat(tgl_perawatan,' ',jam_rawat,' ',status),id_observation from satu_sehat_observationttvrespirasi where no_rawat=?","satu_sehat_observationttvrespirasi"},
        {"Observation SpO2","select concat(tgl_perawatan,' ',jam_rawat,' ',status),id_observation from satu_sehat_observationttvspo2 where no_rawat=?","satu_sehat_observationttvspo2"},
        {"Observation BB","select concat(tgl_perawatan,' ',jam_rawat,' ',status),id_observation from satu_sehat_observationttvbb where no_rawat=?","satu_sehat_observationttvbb"},
        {"Observation TB","select concat(tgl_perawatan,' ',jam_rawat,' ',status),id_observation from satu_sehat_observationttvtb where no_rawat=?","satu_sehat_observationttvtb"},
        {"Observation LP","select concat(tgl_perawatan,' ',jam_rawat,' ',status),id_observation from satu_sehat_observationttvlp where no_rawat=?","satu_sehat_observationttvlp"},
        {"Observation GCS","select concat(tgl_perawatan,' ',jam_rawat,' ',status),id_observation from satu_sehat_observationttvgcs where no_rawat=?","satu_sehat_observationttvgcs"},
        {"Observation Kesadaran","select concat(tgl_perawatan,' ',jam_rawat,' ',status),id_observation from satu_sehat_observationttvkesadaran where no_rawat=?","satu_sehat_observationttvkesadaran"},
        {"Clinical Impression","select concat(tgl_perawatan,' ',jam_rawat,' ',status),id_clinicalimpression from satu_sehat_clinicalimpression where no_rawat=?","satu_sehat_clinicalimpression"},
        {"CarePlan","select concat(tgl_perawatan,' ',jam_rawat,' ',status),id_careplan from satu_sehat_careplan where no_rawat=?","satu_sehat_careplan"},
        {"Alergi","select concat(tgl_perawatan,' ',jam_rawat,' ',status),id_allergy_intolerance from satu_sehat_allergy_intolerance where no_rawat=?","satu_sehat_allergy_intolerance"},
        {"Riwayat Keluarga","select '',id_familymemberhistory from satu_sehat_familymemberhistory where no_rawat=?","satu_sehat_familymemberhistory"},
        {"Goal","select '',id_goal from satu_sehat_goal where no_rawat=?","satu_sehat_goal"},
        {"Risk Assessment","select '',id_riskassessment from satu_sehat_riskassessment where no_rawat=?","satu_sehat_riskassessment"},
        {"ANC","select tipe,id_observation from satu_sehat_anc_observation where no_rawat=?","satu_sehat_anc_observation"},
        {"Diet","select tanggal,id_diet from satu_sehat_diet where no_rawat=?","satu_sehat_diet"},
        {"Imunisasi","select concat(tgl_perawatan,' ',jam,' ',kode_brng),id_immunization from satu_sehat_immunization where no_rawat=?","satu_sehat_immunization"},
        {"Permintaan Lab",detailOrder("satu_sehat_servicerequest_lab","id_servicerequest","permintaan_lab"),"satu_sehat_servicerequest_lab"},
        {"Specimen Lab",detailOrder("satu_sehat_specimen_lab","id_specimen","permintaan_lab"),"satu_sehat_specimen_lab"},
        {"Observation Lab",detailOrder("satu_sehat_observation_lab","id_observation","permintaan_lab"),"satu_sehat_observation_lab"},
        {"Hasil Lab",detailOrder("satu_sehat_diagnosticreport_lab","id_diagnosticreport","permintaan_lab"),"satu_sehat_diagnosticreport_lab"},
        {"Permintaan Lab MB",detailOrder("satu_sehat_servicerequest_lab_mb","id_servicerequest","permintaan_labmb"),"satu_sehat_servicerequest_lab_mb"},
        {"Specimen Lab MB",detailOrder("satu_sehat_specimen_lab_mb","id_specimen","permintaan_labmb"),"satu_sehat_specimen_lab_mb"},
        {"Observation Lab MB",detailOrder("satu_sehat_observation_lab_mb","id_observation","permintaan_labmb"),"satu_sehat_observation_lab_mb"},
        {"Hasil Lab MB",detailOrder("satu_sehat_diagnosticreport_lab_mb","id_diagnosticreport","permintaan_labmb"),"satu_sehat_diagnosticreport_lab_mb"},
        {"Permintaan Rad",detailOrder("satu_sehat_servicerequest_radiologi","id_servicerequest","permintaan_radiologi"),"satu_sehat_servicerequest_radiologi"},
        {"Specimen Rad",detailOrder("satu_sehat_specimen_radiologi","id_specimen","permintaan_radiologi"),"satu_sehat_specimen_radiologi"},
        {"Observation Rad",detailOrder("satu_sehat_observation_radiologi","id_observation","permintaan_radiologi"),"satu_sehat_observation_radiologi"},
        {"Hasil Rad",detailOrder("satu_sehat_diagnosticreport_radiologi","id_diagnosticreport","permintaan_radiologi"),"satu_sehat_diagnosticreport_radiologi"},
        {"Resep Obat","select concat(satu_sehat_medicationrequest.no_resep,' ',satu_sehat_medicationrequest.kode_brng),satu_sehat_medicationrequest.id_medicationrequest from satu_sehat_medicationrequest "+
            "inner join resep_obat on resep_obat.no_resep=satu_sehat_medicationrequest.no_resep where resep_obat.no_rawat=?","satu_sehat_medicationrequest"},
        {"Pemberian Obat","select concat(tgl_perawatan,' ',jam,' ',kode_brng),id_medicationdispanse from satu_sehat_medicationdispense where no_rawat=?","satu_sehat_medicationdispense"},
        {"Medication Statement","select concat(satu_sehat_medicationstatement.no_resep,' ',satu_sehat_medicationstatement.kode_brng),satu_sehat_medicationstatement.id_medicationstatement from satu_sehat_medicationstatement "+
            "inner join resep_obat on resep_obat.no_resep=satu_sehat_medicationstatement.no_resep where resep_obat.no_rawat=?","satu_sehat_medicationstatement"},
        {"Telaah Farmasi","select satu_sehat_questionresponse_telaah_farmasi.no_resep,satu_sehat_questionresponse_telaah_farmasi.id_questionresponse from satu_sehat_questionresponse_telaah_farmasi "+
            "inner join resep_obat on resep_obat.no_resep=satu_sehat_questionresponse_telaah_farmasi.no_resep where resep_obat.no_rawat=?","satu_sehat_questionresponse_telaah_farmasi"},
        {"Questionnaire Response","select concat(tgl_perawatan,' ',jam_rawat,' ',status),id_questionnaireresponse from satu_sehat_questionnaireresponse where no_rawat=?","satu_sehat_questionnaireresponse"},
        {"Composition Triase IGD","select '',id_composition from data_triase_igd where no_rawat=?","data_triase_igd.id_composition"},
        {"RME Rawat Jalan","select '',id_composition from satu_sehat_rme_rawat_jalan where no_rawat=?","satu_sehat_rme_rawat_jalan"},
        {"RME Rawat Inap","select '',id_composition from satu_sehat_rme_rawat_inap where no_rawat=?","satu_sehat_rme_rawat_inap"}
    };
    private int[] jmlterkirim=new int[RESOURCE.length],jmlbelum=new int[RESOURCE.length];
    private final Set<String> tabelada=new HashSet<>();

    private static String kirimOrder(String tabel,String kolomid,String permintaan){
        return "select count(*) from "+tabel+" inner join "+permintaan+" on "+permintaan+".noorder="+tabel+".noorder "+
               "where "+permintaan+".no_rawat=? and "+tabel+"."+kolomid+"<>''";
    }

    private static String detailOrder(String tabel,String kolomid,String permintaan){
        return "select concat("+tabel+".noorder,' ',"+tabel+".kd_jenis_prw),"+tabel+"."+kolomid+" from "+tabel+" "+
               "inner join "+permintaan+" on "+permintaan+".noorder="+tabel+".noorder where "+permintaan+".no_rawat=?";
    }

    /** Creates new form DlgStatusSatuSehat
     * @param parent
     * @param modal */
    public DlgStatusSatuSehat(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        this.setLocation(8,1);
        setSize(885,674);

        Object[] judul=new Object[KOLOMAWAL+RESOURCE.length];
        judul[0]="No.Rawat";judul[1]="Tanggal";judul[2]="Dokter Dituju";judul[3]="Nomer RM";judul[4]="Pasien";judul[5]="Poliklinik";judul[6]="Status";
        for (i = 0; i < RESOURCE.length; i++) {
            judul[KOLOMAWAL+i]=RESOURCE[i][0];
            Resource.addItem(RESOURCE[i][0]);
        }
        tabMode=new DefaultTableModel(null,judul){
             @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        tbBangsal.setModel(tabMode);

        tbBangsal.setPreferredScrollableViewportSize(new Dimension(800,800));
        tbBangsal.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < judul.length; i++) {
            TableColumn column = tbBangsal.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(105);
            }else if(i==1){
                column.setPreferredWidth(65);
            }else if(i==2){
                column.setPreferredWidth(150);
            }else if(i==3){
                column.setPreferredWidth(65);
            }else if(i==4){
                column.setPreferredWidth(150);
            }else if(i==5){
                column.setPreferredWidth(130);
            }else if(i==6){
                column.setPreferredWidth(43);
            }else{
                column.setPreferredWidth(95);
            }
        }
        tbBangsal.setDefaultRenderer(Object.class, new WarnaTable(){
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column){
                Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String isi=(value==null?"":value.toString());
                if((column>=KOLOMAWAL)&&isi.equals(SUDAH)){
                    component.setForeground(new Color(0,128,0));
                }else if((column>=KOLOMAWAL)&&isi.equals(BELUM)){
                    component.setForeground(new Color(200,0,0));
                }else if((column>=KOLOMAWAL)&&isi.startsWith(SEBAGIAN)){
                    component.setForeground(new Color(210,110,0));
                }else{
                    component.setForeground(new Color(50,50,50));
                }
                return component;
            }
        });

        TCari.setDocument(new batasInput((int)90).getKata(TCari));
        ChkInput.setSelected(false);
        isForm();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        TKd = new widget.TextBox();
        kdpoli = new widget.TextBox();
        kdpenjab = new widget.TextBox();
        jPopupMenu1 = new javax.swing.JPopupMenu();
        MnDetailID = new javax.swing.JMenuItem();
        internalFrame1 = new widget.InternalFrame();
        Scroll = new widget.ScrollPane();
        tbBangsal = new widget.Table();
        panelGlass5 = new widget.panelisi();
        label11 = new widget.Label();
        Tgl1 = new widget.Tanggal();
        label18 = new widget.Label();
        Tgl2 = new widget.Tanggal();
        jLabel6 = new widget.Label();
        TCari = new widget.TextBox();
        BtnCari = new widget.Button();
        BtnAll = new widget.Button();
        jLabel7 = new widget.Label();
        BtnPrint = new widget.Button();
        BtnKeluar = new widget.Button();
        PanelInput = new javax.swing.JPanel();
        ChkInput = new widget.CekBox();
        FormInput = new widget.panelisi();
        label17 = new widget.Label();
        nmpoli = new widget.TextBox();
        BtnSeek2 = new widget.Button();
        label19 = new widget.Label();
        nmpenjab = new widget.TextBox();
        BtnSeek3 = new widget.Button();
        jLabel18 = new widget.Label();
        Status = new widget.ComboBox();
        jLabel21 = new widget.Label();
        Resource = new widget.ComboBox();
        jLabel20 = new widget.Label();
        StatusKirim = new widget.ComboBox();
        BtnKirim = new widget.Button();
        BtnKirimSemua = new widget.Button();

        TKd.setForeground(new java.awt.Color(255, 255, 255));
        TKd.setName("TKd"); // NOI18N

        kdpoli.setEditable(false);
        kdpoli.setName("kdpoli"); // NOI18N
        kdpoli.setPreferredSize(new java.awt.Dimension(75, 23));
        kdpoli.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                kdpoliKeyPressed(evt);
            }
        });

        kdpenjab.setEditable(false);
        kdpenjab.setName("kdpenjab"); // NOI18N
        kdpenjab.setPreferredSize(new java.awt.Dimension(75, 23));
        kdpenjab.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                kdpenjabKeyPressed(evt);
            }
        });

        jPopupMenu1.setName("jPopupMenu1"); // NOI18N

        MnDetailID.setBackground(new java.awt.Color(255, 255, 254));
        MnDetailID.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        MnDetailID.setForeground(new java.awt.Color(50, 50, 50));
        MnDetailID.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        MnDetailID.setText("Detail ID Satu Sehat");
        MnDetailID.setName("MnDetailID"); // NOI18N
        MnDetailID.setPreferredSize(new java.awt.Dimension(190, 26));
        MnDetailID.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MnDetailIDActionPerformed(evt);
            }
        });
        jPopupMenu1.add(MnDetailID);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Status Kiriman Data Satu Sehat ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 0, 12), new java.awt.Color(50, 50, 50))); // NOI18N
        internalFrame1.setName("internalFrame1"); // NOI18N
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

        Scroll.setName("Scroll"); // NOI18N
        Scroll.setOpaque(true);

        tbBangsal.setComponentPopupMenu(jPopupMenu1);
        tbBangsal.setName("tbBangsal"); // NOI18N
        tbBangsal.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbBangsalMouseClicked(evt);
            }
        });
        tbBangsal.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tbBangsalKeyPressed(evt);
            }
        });
        Scroll.setViewportView(tbBangsal);

        internalFrame1.add(Scroll, java.awt.BorderLayout.CENTER);

        panelGlass5.setName("panelGlass5"); // NOI18N
        panelGlass5.setPreferredSize(new java.awt.Dimension(55, 55));
        panelGlass5.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        label11.setText("Tanggal :");
        label11.setName("label11"); // NOI18N
        label11.setPreferredSize(new java.awt.Dimension(50, 23));
        panelGlass5.add(label11);

        Tgl1.setDisplayFormat("dd-MM-yyyy");
        Tgl1.setName("Tgl1"); // NOI18N
        Tgl1.setPreferredSize(new java.awt.Dimension(90, 23));
        panelGlass5.add(Tgl1);

        label18.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        label18.setText("s.d.");
        label18.setName("label18"); // NOI18N
        label18.setPreferredSize(new java.awt.Dimension(25, 23));
        panelGlass5.add(label18);

        Tgl2.setDisplayFormat("dd-MM-yyyy");
        Tgl2.setName("Tgl2"); // NOI18N
        Tgl2.setPreferredSize(new java.awt.Dimension(90, 23));
        panelGlass5.add(Tgl2);

        jLabel6.setText("Key Word :");
        jLabel6.setName("jLabel6"); // NOI18N
        jLabel6.setPreferredSize(new java.awt.Dimension(60, 23));
        panelGlass5.add(jLabel6);

        TCari.setName("TCari"); // NOI18N
        TCari.setPreferredSize(new java.awt.Dimension(155, 23));
        TCari.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TCariKeyPressed(evt);
            }
        });
        panelGlass5.add(TCari);

        BtnCari.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png"))); // NOI18N
        BtnCari.setMnemonic('2');
        BtnCari.setToolTipText("Alt+2");
        BtnCari.setName("BtnCari"); // NOI18N
        BtnCari.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnCari.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnCariActionPerformed(evt);
            }
        });
        BtnCari.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnCariKeyPressed(evt);
            }
        });
        panelGlass5.add(BtnCari);

        BtnAll.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Search-16x16.png"))); // NOI18N
        BtnAll.setMnemonic('M');
        BtnAll.setToolTipText("Alt+M");
        BtnAll.setName("BtnAll"); // NOI18N
        BtnAll.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnAllActionPerformed(evt);
            }
        });
        BtnAll.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnAllKeyPressed(evt);
            }
        });
        panelGlass5.add(BtnAll);

        jLabel7.setName("jLabel7"); // NOI18N
        jLabel7.setPreferredSize(new java.awt.Dimension(30, 23));
        panelGlass5.add(jLabel7);

        BtnKirim.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/34.png"))); // NOI18N
        BtnKirim.setMnemonic('I');
        BtnKirim.setText("Kirim");
        BtnKirim.setToolTipText("Alt+I");
        BtnKirim.setName("BtnKirim"); // NOI18N
        BtnKirim.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnKirim.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnKirimActionPerformed(evt);
            }
        });
        panelGlass5.add(BtnKirim);

        BtnKirimSemua.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/34.png"))); // NOI18N
        BtnKirimSemua.setMnemonic('S');
        BtnKirimSemua.setText("Kirim Semua");
        BtnKirimSemua.setToolTipText("Alt+S");
        BtnKirimSemua.setName("BtnKirimSemua"); // NOI18N
        BtnKirimSemua.setPreferredSize(new java.awt.Dimension(120, 30));
        BtnKirimSemua.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnKirimSemuaActionPerformed(evt);
            }
        });
        panelGlass5.add(BtnKirimSemua);

        BtnPrint.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/b_print.png"))); // NOI18N
        BtnPrint.setMnemonic('T');
        BtnPrint.setText("Cetak");
        BtnPrint.setToolTipText("Alt+T");
        BtnPrint.setName("BtnPrint"); // NOI18N
        BtnPrint.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnPrint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnPrintActionPerformed(evt);
            }
        });
        BtnPrint.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnPrintKeyPressed(evt);
            }
        });
        panelGlass5.add(BtnPrint);

        BtnKeluar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/exit.png"))); // NOI18N
        BtnKeluar.setMnemonic('K');
        BtnKeluar.setText("Keluar");
        BtnKeluar.setToolTipText("Alt+K");
        BtnKeluar.setName("BtnKeluar"); // NOI18N
        BtnKeluar.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnKeluar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnKeluarActionPerformed(evt);
            }
        });
        BtnKeluar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnKeluarKeyPressed(evt);
            }
        });
        panelGlass5.add(BtnKeluar);

        internalFrame1.add(panelGlass5, java.awt.BorderLayout.PAGE_END);

        PanelInput.setBackground(new java.awt.Color(255, 255, 255));
        PanelInput.setName("PanelInput"); // NOI18N
        PanelInput.setOpaque(false);
        PanelInput.setPreferredSize(new java.awt.Dimension(192, 65));
        PanelInput.setLayout(new java.awt.BorderLayout(1, 1));

        ChkInput.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/143.png"))); // NOI18N
        ChkInput.setMnemonic('M');
        ChkInput.setText(".: Filter Data");
        ChkInput.setBorderPainted(true);
        ChkInput.setBorderPaintedFlat(true);
        ChkInput.setFocusable(false);
        ChkInput.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        ChkInput.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        ChkInput.setName("ChkInput"); // NOI18N
        ChkInput.setPreferredSize(new java.awt.Dimension(192, 20));
        ChkInput.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/143.png"))); // NOI18N
        ChkInput.setRolloverSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/145.png"))); // NOI18N
        ChkInput.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/145.png"))); // NOI18N
        ChkInput.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ChkInputActionPerformed(evt);
            }
        });
        PanelInput.add(ChkInput, java.awt.BorderLayout.PAGE_END);

        FormInput.setName("FormInput"); // NOI18N
        FormInput.setPreferredSize(new java.awt.Dimension(100, 104));
        FormInput.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        label17.setText("Asal Poli :");
        label17.setName("label17"); // NOI18N
        label17.setPreferredSize(new java.awt.Dimension(60, 23));
        FormInput.add(label17);

        nmpoli.setEditable(false);
        nmpoli.setName("nmpoli"); // NOI18N
        nmpoli.setPreferredSize(new java.awt.Dimension(190, 23));
        FormInput.add(nmpoli);

        BtnSeek2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        BtnSeek2.setMnemonic('3');
        BtnSeek2.setToolTipText("Alt+3");
        BtnSeek2.setName("BtnSeek2"); // NOI18N
        BtnSeek2.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnSeek2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnSeek2ActionPerformed(evt);
            }
        });
        BtnSeek2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnSeek2KeyPressed(evt);
            }
        });
        FormInput.add(BtnSeek2);

        label19.setText("Cara Bayar :");
        label19.setName("label19"); // NOI18N
        label19.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label19);

        nmpenjab.setEditable(false);
        nmpenjab.setName("nmpenjab"); // NOI18N
        nmpenjab.setPreferredSize(new java.awt.Dimension(190, 23));
        FormInput.add(nmpenjab);

        BtnSeek3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        BtnSeek3.setMnemonic('3');
        BtnSeek3.setToolTipText("Alt+3");
        BtnSeek3.setName("BtnSeek3"); // NOI18N
        BtnSeek3.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnSeek3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnSeek3ActionPerformed(evt);
            }
        });
        BtnSeek3.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnSeek3KeyPressed(evt);
            }
        });
        FormInput.add(BtnSeek3);

        jLabel18.setText("Status :");
        jLabel18.setName("jLabel18"); // NOI18N
        jLabel18.setPreferredSize(new java.awt.Dimension(45, 23));
        FormInput.add(jLabel18);

        Status.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Semua", "Ralan", "Ranap" }));
        Status.setLightWeightPopupEnabled(false);
        Status.setName("Status"); // NOI18N
        Status.setPreferredSize(new java.awt.Dimension(92, 23));
        FormInput.add(Status);

        jLabel21.setText("Resource :");
        jLabel21.setName("jLabel21"); // NOI18N
        jLabel21.setPreferredSize(new java.awt.Dimension(60, 23));
        FormInput.add(jLabel21);

        Resource.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Semua" }));
        Resource.setLightWeightPopupEnabled(false);
        Resource.setName("Resource"); // NOI18N
        Resource.setPreferredSize(new java.awt.Dimension(160, 23));
        FormInput.add(Resource);

        jLabel20.setText("Kiriman :");
        jLabel20.setName("jLabel20"); // NOI18N
        jLabel20.setPreferredSize(new java.awt.Dimension(55, 23));
        FormInput.add(jLabel20);

        StatusKirim.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Semua", "Terkirim", "Belum Terkirim" }));
        StatusKirim.setLightWeightPopupEnabled(false);
        StatusKirim.setName("StatusKirim"); // NOI18N
        StatusKirim.setPreferredSize(new java.awt.Dimension(120, 23));
        FormInput.add(StatusKirim);

        PanelInput.add(FormInput, java.awt.BorderLayout.CENTER);

        internalFrame1.add(PanelInput, java.awt.BorderLayout.PAGE_START);

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void BtnPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnPrintActionPerformed
        if(ceksukses==false){
            if(tabMode.getRowCount()==0){
                JOptionPane.showMessageDialog(null,"Maaf, data sudah habis. Tidak ada data yang bisa anda print...!!!!");
                BtnPrint.requestFocus();
            }else if(tabMode.getRowCount()!=0){
                this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                try {
                    File g = new File("file2.css");
                    BufferedWriter bg = new BufferedWriter(new FileWriter(g));
                    bg.write(
                            ".isi td{border-right: 1px solid #e2e7dd;font: 11px Arial;height:12px;border-bottom: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"+
                            ".isi2 td{font: 11px Arial;height:12px;background: #ffffff;color:#323232;}"+
                            ".isi3 td{border-right: 1px solid #e2e7dd;font: 11px Arial;height:12px;border-top: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"+
                            ".isi4 td{font: 11px Arial;height:12px;border-top: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"
                    );
                    bg.close();

                    File f;
                    BufferedWriter bw;

                    pilihan = (String)JOptionPane.showInputDialog(null,"Silahkan pilih laporan..!","Pilihan Cetak",JOptionPane.QUESTION_MESSAGE,null,new Object[]{"Laporan 1 (HTML)","Laporan 2 (WPS)","Laporan 3 (CSV)"},"Laporan 1 (HTML)");
                    switch (pilihan) {
                        case "Laporan 1 (HTML)":
                        case "Laporan 2 (WPS)":
                                htmlContent = new StringBuilder();
                                htmlContent.append("<tr class='isi'>");
                                for(j=0;j<tabMode.getColumnCount();j++){
                                    htmlContent.append("<td valign='middle' bgcolor='#FFFAFA' align='center'>").append(tabMode.getColumnName(j)).append("</td>");
                                }
                                htmlContent.append("</tr>");
                                for(i=0;i<tabMode.getRowCount();i++){
                                    htmlContent.append("<tr class='isi'>");
                                    for(j=0;j<tabMode.getColumnCount();j++){
                                        htmlContent.append("<td valign='top'").append(j>=KOLOMAWAL?" align='center'":"").append(">").append(tabMode.getValueAt(i,j)).append("</td>");
                                    }
                                    htmlContent.append("</tr>");
                                }

                                f = new File(pilihan.equals("Laporan 1 (HTML)")?"StatusSatuSehat.html":"StatusSatuSehat.wps");
                                bw = new BufferedWriter(new FileWriter(f));
                                bw.write("<html>"+
                                            "<head><link href=\"file2.css\" rel=\"stylesheet\" type=\"text/css\" /></head>"+
                                            "<body>"+
                                                "<table width='1800px' border='0' align='center' cellpadding='3px' cellspacing='0' class='tbl_form'>"+
                                                    "<tr class='isi2'>"+
                                                        "<td valign='top' align='center'>"+
                                                            "<font size='4' face='Arial'>"+akses.getnamars()+"</font><br>"+
                                                            akses.getalamatrs()+", "+akses.getkabupatenrs()+", "+akses.getpropinsirs()+"<br>"+
                                                            akses.getkontakrs()+", E-mail : "+akses.getemailrs()+"<br><br>"+
                                                            "<font size='2' face='Arial'>REKAP STATUS KIRIMAN DATA SATU SEHAT PERIODE "+Tgl1.getSelectedItem()+" s.d. "+Tgl2.getSelectedItem()+"<br><br></font>"+
                                                        "</td>"+
                                                   "</tr>"+
                                                "</table>"+
                                                "<table width='1800px' border='0' align='center' cellpadding='3px' cellspacing='0' class='tbl_form'>"+
                                                    htmlContent.toString()+
                                                "</table>"+
                                            "</body>"+
                                         "</html>"
                                );

                                bw.close();
                                Desktop.getDesktop().browse(f.toURI());
                            break;
                        case "Laporan 3 (CSV)":
                                htmlContent = new StringBuilder();
                                for(j=0;j<tabMode.getColumnCount();j++){
                                    htmlContent.append(j==0?"":";").append("\"").append(tabMode.getColumnName(j)).append("\"");
                                }
                                htmlContent.append("\n");
                                for(i=0;i<tabMode.getRowCount();i++){
                                    for(j=0;j<tabMode.getColumnCount();j++){
                                        htmlContent.append(j==0?"":";").append("\"").append(tabMode.getValueAt(i,j)).append("\"");
                                    }
                                    htmlContent.append("\n");
                                }

                                f = new File("StatusSatuSehat.csv");
                                bw = new BufferedWriter(new FileWriter(f));
                                bw.write(htmlContent.toString());

                                bw.close();
                                Desktop.getDesktop().browse(f.toURI());
                            break;
                    }
                } catch (Exception e) {
                }
                this.setCursor(Cursor.getDefaultCursor());
            }
        }else{
            JOptionPane.showMessageDialog(null,"Masih proses menampilkan data, harap tunggu terlebih dahulu...!");
        }
}//GEN-LAST:event_BtnPrintActionPerformed

    private void BtnPrintKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnPrintKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnPrintActionPerformed(null);
        }else{
            //Valid.pindah(evt, BtnHapus, BtnAll);
        }
}//GEN-LAST:event_BtnPrintKeyPressed

    private void BtnKeluarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnKeluarActionPerformed
        dispose();
}//GEN-LAST:event_BtnKeluarActionPerformed

    private void BtnKeluarKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnKeluarKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            dispose();
        }else{Valid.pindah(evt,BtnKeluar,TKd);}
}//GEN-LAST:event_BtnKeluarKeyPressed

    private void tbBangsalMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbBangsalMouseClicked
        if(tabMode.getRowCount()!=0){
            try {
                getData();
            } catch (java.lang.NullPointerException e) {
            }
            if(evt.getClickCount()==2){
                MnDetailIDActionPerformed(null);
            }
        }
}//GEN-LAST:event_tbBangsalMouseClicked

    private void tbBangsalKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbBangsalKeyPressed
        if(tabMode.getRowCount()!=0){
            if((evt.getKeyCode()==KeyEvent.VK_ENTER)||(evt.getKeyCode()==KeyEvent.VK_UP)||(evt.getKeyCode()==KeyEvent.VK_DOWN)){
                try {
                    getData();
                } catch (java.lang.NullPointerException e) {
                }
            }
        }
}//GEN-LAST:event_tbBangsalKeyPressed

private void BtnCariActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCariActionPerformed
       runBackground(() ->tampil());
}//GEN-LAST:event_BtnCariActionPerformed

private void BtnCariKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnCariKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            runBackground(() ->tampil());
            this.setCursor(Cursor.getDefaultCursor());
        }else{
            Valid.pindah(evt, TKd, BtnPrint);
        }
}//GEN-LAST:event_BtnCariKeyPressed

    private void TCariKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TCariKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            BtnCariActionPerformed(null);
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_DOWN){
            BtnCari.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_UP){
            BtnKeluar.requestFocus();
        }
    }//GEN-LAST:event_TCariKeyPressed

    private void BtnAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnAllActionPerformed
        TCari.setText("");
        kdpoli.setText("");
        nmpoli.setText("");
        kdpenjab.setText("");
        nmpenjab.setText("");
        Status.setSelectedIndex(0);
        Resource.setSelectedIndex(0);
        StatusKirim.setSelectedIndex(0);
        runBackground(() ->tampil());
    }//GEN-LAST:event_BtnAllActionPerformed

    private void BtnAllKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnAllKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnAllActionPerformed(null);
        }else{

        }
    }//GEN-LAST:event_BtnAllKeyPressed

    private void ChkInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ChkInputActionPerformed
        isForm();
    }//GEN-LAST:event_ChkInputActionPerformed

    private void kdpoliKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_kdpoliKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_PAGE_DOWN){
            Sequel.cariIsi("select poliklinik.nm_poli from poliklinik where poliklinik.kd_poli=?", nmpoli,kdpoli.getText());
        }else if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            BtnAll.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_UP){
            Tgl2.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_UP){
            BtnSeek2ActionPerformed(null);
        }
    }//GEN-LAST:event_kdpoliKeyPressed

    private void BtnSeek2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSeek2ActionPerformed
        DlgCariPoli poli=new DlgCariPoli(null,false);
        poli.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}
            @Override
            public void windowClosing(WindowEvent e) {}
            @Override
            public void windowClosed(WindowEvent e) {
                if(poli.getTable().getSelectedRow()!= -1){
                    kdpoli.setText(poli.getTable().getValueAt(poli.getTable().getSelectedRow(),0).toString());
                    nmpoli.setText(poli.getTable().getValueAt(poli.getTable().getSelectedRow(),1).toString());
                }
                kdpoli.requestFocus();
            }
            @Override
            public void windowIconified(WindowEvent e) {}
            @Override
            public void windowDeiconified(WindowEvent e) {}
            @Override
            public void windowActivated(WindowEvent e) {poli.emptTeks();}
            @Override
            public void windowDeactivated(WindowEvent e) {}
        });
        poli.isCek();
        poli.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        poli.setLocationRelativeTo(internalFrame1);
        poli.setAlwaysOnTop(false);
        poli.setVisible(true);
    }//GEN-LAST:event_BtnSeek2ActionPerformed

    private void BtnSeek2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnSeek2KeyPressed
        //Valid.pindah(evt,DTPCari2,TCari);
    }//GEN-LAST:event_BtnSeek2KeyPressed

    private void kdpenjabKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_kdpenjabKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_PAGE_DOWN){
            Sequel.cariIsi("select penjab.png_jawab from penjab where penjab.kd_pj=?", nmpenjab,kdpenjab.getText());
        }else if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            Sequel.cariIsi("select penjab.png_jawab from penjab where penjab.kd_pj=?", nmpenjab,kdpenjab.getText());
            BtnAll.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_UP){
            Sequel.cariIsi("select penjab.png_jawab from penjab where penjab.kd_pj=?", nmpenjab,kdpenjab.getText());
            Tgl2.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_UP){
            BtnSeek2ActionPerformed(null);
        }
    }//GEN-LAST:event_kdpenjabKeyPressed

    private void BtnSeek3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSeek3ActionPerformed
        DlgCariCaraBayar penjab=new DlgCariCaraBayar(null,false);
        penjab.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}
            @Override
            public void windowClosing(WindowEvent e) {}
            @Override
            public void windowClosed(WindowEvent e) {
                if(penjab.getTable().getSelectedRow()!= -1){
                    kdpenjab.setText(penjab.getTable().getValueAt(penjab.getTable().getSelectedRow(),1).toString());
                    nmpenjab.setText(penjab.getTable().getValueAt(penjab.getTable().getSelectedRow(),2).toString());
                }
                kdpenjab.requestFocus();
            }
            @Override
            public void windowIconified(WindowEvent e) {}
            @Override
            public void windowDeiconified(WindowEvent e) {}
            @Override
            public void windowActivated(WindowEvent e) {penjab.emptTeks();}
            @Override
            public void windowDeactivated(WindowEvent e) {}
        });

        penjab.getTable().addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode()==KeyEvent.VK_SPACE){
                    penjab.dispose();
                }
            }
            @Override
            public void keyReleased(KeyEvent e) {}
        });
        penjab.isCek();
        penjab.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        penjab.setLocationRelativeTo(internalFrame1);
        penjab.setAlwaysOnTop(false);
        penjab.setVisible(true);
    }//GEN-LAST:event_BtnSeek3ActionPerformed

    private void BtnSeek3KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnSeek3KeyPressed
        //Valid.pindah(evt,DTPCari2,TCari);
    }//GEN-LAST:event_BtnSeek3KeyPressed

    private void MnDetailIDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnDetailIDActionPerformed
        if(tbBangsal.getSelectedRow()!= -1){
            String norawat=tbBangsal.getValueAt(tbBangsal.getSelectedRow(),0).toString();
            if(norawat.equals("")){
                return;
            }
            StringBuilder detail=new StringBuilder();
            detail.append("No.Rawat : ").append(norawat).append("\n")
                  .append("Pasien   : ").append(tbBangsal.getValueAt(tbBangsal.getSelectedRow(),3)).append(" ")
                  .append(tbBangsal.getValueAt(tbBangsal.getSelectedRow(),4)).append("\n\n");
            if(ceksukses){
                JOptionPane.showMessageDialog(null,"Masih proses menampilkan data, harap tunggu terlebih dahulu...!");
                return;
            }
            if(tabelada.isEmpty()){
                muatTabelAda();
            }
            for (String[] resource : DETAILID) {
                if(!adaTabel(resource[2])){
                    continue;
                }
                try (PreparedStatement psdetail=koneksi.prepareStatement(resource[1])) {
                    psdetail.setString(1,norawat);
                    try (ResultSet rsdetail=psdetail.executeQuery()) {
                        while(rsdetail.next()){
                            String keterangan=(rsdetail.getString(1)==null?"":rsdetail.getString(1));
                            String id=(rsdetail.getString(2)==null?"":rsdetail.getString(2));
                            detail.append(resource[0]).append(keterangan.equals("")?"":" ["+keterangan+"]")
                                  .append(" : ").append(id.equals("")?"(ID kosong)":id).append("\n");
                        }
                    }
                } catch (Exception e) {
                    detail.append(resource[0]).append(" : gagal dibaca (").append(e.getMessage()).append(")\n");
                }
            }
            widget.TextArea area=new widget.TextArea();
            area.setText(detail.toString());
            area.setEditable(false);
            area.setCaretPosition(0);
            widget.ScrollPane scroll=new widget.ScrollPane();
            scroll.setViewportView(area);
            scroll.setPreferredSize(new Dimension(620,420));
            JOptionPane.showMessageDialog(null,scroll,"Detail ID Satu Sehat",JOptionPane.INFORMATION_MESSAGE);
        }else{
            JOptionPane.showMessageDialog(null,"Silahkan pilih data kunjungan terlebih dahulu..!!");
        }
    }//GEN-LAST:event_MnDetailIDActionPerformed

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        if(koneksiDB.CARICEPAT().equals("aktif")){
            TCari.getDocument().addDocumentListener(new javax.swing.event.DocumentListener(){
                @Override
                public void insertUpdate(DocumentEvent e) {
                    if(TCari.getText().length()>2){
                        runBackground(() ->tampil());
                    }
                }
                @Override
                public void removeUpdate(DocumentEvent e) {
                    if(TCari.getText().length()>2){
                        runBackground(() ->tampil());
                    }
                }
                @Override
                public void changedUpdate(DocumentEvent e) {
                    if(TCari.getText().length()>2){
                        runBackground(() ->tampil());
                    }
                }
            });
        }
    }//GEN-LAST:event_formWindowOpened

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            DlgStatusSatuSehat dialog = new DlgStatusSatuSehat(new javax.swing.JFrame(), true);
            dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    System.exit(0);
                }
            });
            dialog.setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private widget.Button BtnAll;
    private widget.Button BtnCari;
    private widget.Button BtnKeluar;
    private widget.Button BtnKirim;
    private widget.Button BtnKirimSemua;
    private widget.Button BtnPrint;
    private widget.Button BtnSeek2;
    private widget.Button BtnSeek3;
    private widget.CekBox ChkInput;
    private widget.panelisi FormInput;
    private javax.swing.JMenuItem MnDetailID;
    private javax.swing.JPanel PanelInput;
    private widget.ComboBox Resource;
    private widget.ScrollPane Scroll;
    private widget.ComboBox Status;
    private widget.ComboBox StatusKirim;
    private widget.TextBox TCari;
    private widget.TextBox TKd;
    private widget.Tanggal Tgl1;
    private widget.Tanggal Tgl2;
    private widget.InternalFrame internalFrame1;
    private widget.Label jLabel18;
    private widget.Label jLabel20;
    private widget.Label jLabel21;
    private widget.Label jLabel6;
    private widget.Label jLabel7;
    private javax.swing.JPopupMenu jPopupMenu1;
    private widget.TextBox kdpenjab;
    private widget.TextBox kdpoli;
    private widget.Label label11;
    private widget.Label label17;
    private widget.Label label18;
    private widget.Label label19;
    private widget.TextBox nmpenjab;
    private widget.TextBox nmpoli;
    private widget.panelisi panelGlass5;
    private widget.Table tbBangsal;
    // End of variables declaration//GEN-END:variables

    public void tampil(){
        try{
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            Valid.tabelKosong(tabMode);
            ps=koneksi.prepareStatement(
                "select reg_periksa.no_rawat,reg_periksa.tgl_registrasi,dokter.nm_dokter,reg_periksa.no_rkm_medis,pasien.nm_pasien,poliklinik.nm_poli,reg_periksa.status_lanjut "+
                "from reg_periksa inner join dokter on reg_periksa.kd_dokter=dokter.kd_dokter inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                "inner join poliklinik on reg_periksa.kd_poli=poliklinik.kd_poli inner join penjab on reg_periksa.kd_pj=penjab.kd_pj where reg_periksa.stts<>'Batal' and "+
                "concat(reg_periksa.kd_poli,poliklinik.nm_poli) like ? and concat(reg_periksa.kd_pj,penjab.png_jawab) like ? "+
                "and reg_periksa.tgl_registrasi between ? and ? and reg_periksa.status_lanjut like ? "+
                (TCari.getText().equals("")?"":"and (reg_periksa.no_rawat like ? or dokter.nm_dokter like ? or reg_periksa.no_rkm_medis like ? or "+
                "pasien.nm_pasien like ? or poliklinik.nm_poli like ? or penjab.png_jawab like ?) ")+"order by reg_periksa.tgl_registrasi");
            try {
                ps.setString(1,"%"+kdpoli.getText()+nmpoli.getText()+"%");
                ps.setString(2,"%"+kdpenjab.getText()+nmpenjab.getText()+"%");
                ps.setString(3,Valid.SetTgl(Tgl1.getSelectedItem()+""));
                ps.setString(4,Valid.SetTgl(Tgl2.getSelectedItem()+""));
                ps.setString(5,"%"+Status.getSelectedItem().toString().replaceAll("Semua","")+"%");
                if(!TCari.getText().trim().equals("")){
                    ps.setString(6,"%"+TCari.getText().trim()+"%");
                    ps.setString(7,"%"+TCari.getText().trim()+"%");
                    ps.setString(8,"%"+TCari.getText().trim()+"%");
                    ps.setString(9,"%"+TCari.getText().trim()+"%");
                    ps.setString(10,"%"+TCari.getText().trim()+"%");
                    ps.setString(11,"%"+TCari.getText().trim()+"%");
                }

                rs=ps.executeQuery();
                jmlterkirim=new int[RESOURCE.length];
                jmlbelum=new int[RESOURCE.length];
                muatTabelAda();
                // Resource yang dipilih di filter dihitung lebih dulu supaya baris yang tidak cocok bisa dilewati tanpa menghitung resource lain.
                int dipilih=Resource.getSelectedIndex()-1;
                String filter=StatusKirim.getSelectedItem().toString();
                while(rs.next()){
                    Object[] baris=new Object[KOLOMAWAL+RESOURCE.length];
                    baris[0]=rs.getString("no_rawat");baris[1]=rs.getString("tgl_registrasi");baris[2]=rs.getString("nm_dokter");
                    baris[3]=rs.getString("no_rkm_medis");baris[4]=rs.getString("nm_pasien");baris[5]=rs.getString("nm_poli");baris[6]=rs.getString("status_lanjut");
                    if(dipilih>=0){
                        baris[KOLOMAWAL+dipilih]=statusResource(dipilih,rs.getString("no_rawat"));
                        if(!cocokFilter(baris[KOLOMAWAL+dipilih].toString(),filter)){
                            continue;
                        }
                    }
                    lengkap=true;
                    for (i = 0; i < RESOURCE.length; i++) {
                        if(i!=dipilih){
                            baris[KOLOMAWAL+i]=statusResource(i,rs.getString("no_rawat"));
                        }
                        status=baris[KOLOMAWAL+i].toString();
                        if(status.equals(BELUM)||status.startsWith(SEBAGIAN)){
                            lengkap=false;
                        }
                    }
                    if(dipilih<0&&!cocokFilter(lengkap?SUDAH:BELUM,filter)){
                        continue;
                    }
                    for (i = 0; i < RESOURCE.length; i++) {
                        status=baris[KOLOMAWAL+i].toString();
                        if(status.equals(SUDAH)){
                            jmlterkirim[i]++;
                        }else if(status.equals(BELUM)||status.startsWith(SEBAGIAN)){
                            jmlbelum[i]++;
                        }
                    }
                    tabMode.addRow(baris);
                }
                if(tabMode.getRowCount()>0){
                    Object[] baris=new Object[KOLOMAWAL+RESOURCE.length];
                    Object[] baris2=new Object[KOLOMAWAL+RESOURCE.length];
                    for (i = 0; i < KOLOMAWAL; i++) {
                        baris[i]="";
                        baris2[i]="";
                    }
                    baris[5]="Jumlah Terkirim";baris[6]=":";
                    baris2[5]="Jumlah Belum/Sebagian";baris2[6]=":";
                    for (i = 0; i < RESOURCE.length; i++) {
                        baris[KOLOMAWAL+i]=jmlterkirim[i];
                        baris2[KOLOMAWAL+i]=jmlbelum[i];
                    }
                    tabMode.addRow(baris);
                    tabMode.addRow(baris2);
                }
            } catch (Exception e) {
                System.out.println("Notifikasi : "+e);
            } finally{
                if(rs!=null){
                    rs.close();
                }
                if(ps!=null){
                    ps.close();
                }
            }
            this.setCursor(Cursor.getDefaultCursor());
        }catch(Exception e){
            System.out.println("Notifikasi : "+e);
        }
    }

    private String statusResource(int indeks,String norawat){
        int jmlsumber=hitung(RESOURCE[indeks][1],norawat);
        // Tabel penyimpan ID belum ada berarti form Kirim-nya belum pernah dipakai, jadi pasti belum ada yang terkirim.
        int jmlkirim=adaTabel(RESOURCE[indeks][3])?hitung(RESOURCE[indeks][2],norawat):0;
        if(jmlsumber==0&&jmlkirim==0){
            return "-";
        }else if(jmlkirim==0){
            return BELUM;
        }else if(jmlkirim<jmlsumber){
            return SEBAGIAN+" "+jmlkirim+"/"+jmlsumber;
        }
        return SUDAH;
    }

    private boolean cocokFilter(String isi,String filter){
        switch (filter) {
            case "Terkirim":
                return isi.equals(SUDAH);
            case "Belum Terkirim":
                return isi.equals(BELUM)||isi.startsWith(SEBAGIAN);
            default:
                return true;
        }
    }

    // Daftar "tabel" dan "tabel.kolom" yang ada di database, untuk tabel Satu Sehat yang dibuat otomatis oleh form Kirim.
    private void muatTabelAda(){
        tabelada.clear();
        try (PreparedStatement pstabel=koneksi.prepareStatement(
                "select lower(table_name),lower(column_name) from information_schema.columns where table_schema=database() "+
                "and (table_name like 'satu\\_sehat\\_%' or table_name='data_triase_igd')");
             ResultSet rstabel=pstabel.executeQuery()) {
            while(rstabel.next()){
                tabelada.add(rstabel.getString(1));
                tabelada.add(rstabel.getString(1)+"."+rstabel.getString(2));
            }
        } catch (Exception e) {
            System.out.println("Notifikasi : "+e);
        }
    }

    private boolean adaTabel(String nama){
        return tabelada.contains(nama.toLowerCase());
    }

    private void BtnKirimActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnKirimActionPerformed
        if(ceksukses){
            JOptionPane.showMessageDialog(null,"Masih proses menampilkan data, harap tunggu terlebih dahulu...!");
            return;
        }
        int baris=tbBangsal.getSelectedRow();
        if(baris==-1||tbBangsal.getValueAt(baris,0).toString().equals("")){
            JOptionPane.showMessageDialog(null,"Silahkan pilih data kunjungan terlebih dahulu..!!");
            return;
        }
        // Resource diambil dari filter Resource; kalau "Semua", dari kolom yang sedang diklik.
        int indeks=Resource.getSelectedIndex()-1;
        if(indeks<0&&tbBangsal.getSelectedColumn()>=KOLOMAWAL){
            indeks=tbBangsal.getSelectedColumn()-KOLOMAWAL;
        }
        if(indeks<0){
            Object pilih=JOptionPane.showInputDialog(null,"Silahkan pilih resource yang akan dikirim..!","Kirim Satu Sehat",
                    JOptionPane.QUESTION_MESSAGE,null,java.util.Arrays.stream(RESOURCE).map(r -> r[0]).toArray(),RESOURCE[0][0]);
            if(pilih==null){
                return;
            }
            for (int k = 0; k < RESOURCE.length; k++) {
                if(RESOURCE[k][0].equals(pilih.toString())){
                    indeks=k;
                }
            }
        }
        JDialog form=formKirim(RESOURCE[indeks][0]);
        if(form==null){
            return;
        }
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        isiFilterKirim(form,tbBangsal.getValueAt(baris,0).toString(),tbBangsal.getValueAt(baris,1).toString());
        form.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                runBackground(() ->tampil());
            }
        });
        form.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        form.setLocationRelativeTo(internalFrame1);
        form.setVisible(true);
        this.setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_BtnKirimActionPerformed

    private JDialog formKirim(String resource){
        JDialog form;
        switch (resource) {
            case "Encounter": form=new bridging.SatuSehatKirimEncounter(null,false); ((bridging.SatuSehatKirimEncounter)form).isCek(); break;
            case "Condition": form=new bridging.SatuSehatKirimCondition(null,false); ((bridging.SatuSehatKirimCondition)form).isCek(); break;
            case "EpisodeOfCare": form=new bridging.SatuSehatKirimEpisodeOfCare(null,false); ((bridging.SatuSehatKirimEpisodeOfCare)form).isCek(); break;
            case "Procedure": form=new bridging.SatuSehatKirimProcedure(null,false); ((bridging.SatuSehatKirimProcedure)form).isCek(); break;
            case "Observation TTV": form=new bridging.SatuSehatKirimObservationTTV(null,false); ((bridging.SatuSehatKirimObservationTTV)form).isCek(); break;
            case "Clinical Impression": form=new bridging.SatuSehatKirimClinicalImpression(null,false); ((bridging.SatuSehatKirimClinicalImpression)form).isCek(); break;
            case "CarePlan": form=new bridging.SatuSehatKirimCarePlan(null,false); ((bridging.SatuSehatKirimCarePlan)form).isCek(); break;
            case "Alergi": form=new bridging.SatuSehatKirimAllergyIntolerance(null,false); ((bridging.SatuSehatKirimAllergyIntolerance)form).isCek(); break;
            case "Riwayat Keluarga": form=new bridging.SatuSehatKirimFamilyMemberHistory(null,false); ((bridging.SatuSehatKirimFamilyMemberHistory)form).isCek(); break;
            case "Goal": form=new bridging.SatuSehatKirimGoal(null,false); ((bridging.SatuSehatKirimGoal)form).isCek(); break;
            case "Risk Assessment": form=new bridging.SatuSehatKirimRiskAssessment(null,false); ((bridging.SatuSehatKirimRiskAssessment)form).isCek(); break;
            case "ANC": form=new bridging.SatuSehatKirimANC(null,false); ((bridging.SatuSehatKirimANC)form).isCek(); break;
            case "Diet": form=new bridging.SatuSehatKirimDiet(null,false); ((bridging.SatuSehatKirimDiet)form).isCek(); break;
            case "Imunisasi": form=new bridging.SatuSehatKirimVaksin(null,false); ((bridging.SatuSehatKirimVaksin)form).isCek(); break;
            case "Permintaan Lab": form=new bridging.SatuSehatKirimServiceRequestLabPK(null,false); ((bridging.SatuSehatKirimServiceRequestLabPK)form).isCek(); break;
            case "Specimen Lab": form=new bridging.SatuSehatKirimSpecimenLabPK(null,false); ((bridging.SatuSehatKirimSpecimenLabPK)form).isCek(); break;
            case "Observation Lab": form=new bridging.SatuSehatKirimObservationLabPK(null,false); ((bridging.SatuSehatKirimObservationLabPK)form).isCek(); break;
            case "Hasil Lab": form=new bridging.SatuSehatKirimDiagnosticReportLabPK(null,false); ((bridging.SatuSehatKirimDiagnosticReportLabPK)form).isCek(); break;
            case "Permintaan Lab MB": form=new bridging.SatuSehatKirimServiceRequestLabMB(null,false); ((bridging.SatuSehatKirimServiceRequestLabMB)form).isCek(); break;
            case "Specimen Lab MB": form=new bridging.SatuSehatKirimSpecimenLabMB(null,false); ((bridging.SatuSehatKirimSpecimenLabMB)form).isCek(); break;
            case "Observation Lab MB": form=new bridging.SatuSehatKirimObservationLabMB(null,false); ((bridging.SatuSehatKirimObservationLabMB)form).isCek(); break;
            case "Hasil Lab MB": form=new bridging.SatuSehatKirimDiagnosticReportLabMB(null,false); ((bridging.SatuSehatKirimDiagnosticReportLabMB)form).isCek(); break;
            case "Permintaan Rad": form=new bridging.SatuSehatKirimServiceRequestRadiologi(null,false); ((bridging.SatuSehatKirimServiceRequestRadiologi)form).isCek(); break;
            case "Specimen Rad": form=new bridging.SatuSehatKirimSpecimenRadiologi(null,false); ((bridging.SatuSehatKirimSpecimenRadiologi)form).isCek(); break;
            case "Observation Rad": form=new bridging.SatuSehatKirimObservationRadiologi(null,false); ((bridging.SatuSehatKirimObservationRadiologi)form).isCek(); break;
            case "Hasil Rad": form=new bridging.SatuSehatKirimDiagnosticReportRadiologi(null,false); ((bridging.SatuSehatKirimDiagnosticReportRadiologi)form).isCek(); break;
            case "Resep Obat": form=new bridging.SatuSehatKirimMedicationRequest(null,false); ((bridging.SatuSehatKirimMedicationRequest)form).isCek(); break;
            case "Pemberian Obat": form=new bridging.SatuSehatKirimMedicationDispense(null,false); ((bridging.SatuSehatKirimMedicationDispense)form).isCek(); break;
            case "Medication Statement": form=new bridging.SatuSehatKirimMedicationStatement(null,false); ((bridging.SatuSehatKirimMedicationStatement)form).isCek(); break;
            case "Telaah Farmasi": form=new bridging.SatuSehatKirimQRTelaahFarmasi(null,false); ((bridging.SatuSehatKirimQRTelaahFarmasi)form).isCek(); break;
            case "Questionnaire Response": form=new bridging.SatuSehatKirimQuestionnaireResponse(null,false); ((bridging.SatuSehatKirimQuestionnaireResponse)form).isCek(); break;
            case "Composition Triase IGD": form=new bridging.SatuSehatKirimCompositionRME(null,false); ((bridging.SatuSehatKirimCompositionRME)form).isCek(); break;
            case "RME Rawat Jalan": form=new bridging.SatuSehatKirimRMERawatJalan(null,false); ((bridging.SatuSehatKirimRMERawatJalan)form).isCek(); break;
            case "RME Rawat Inap": form=new bridging.SatuSehatKirimRMERawatInap(null,false); ((bridging.SatuSehatKirimRMERawatInap)form).isCek(); break;
            default:
                JOptionPane.showMessageDialog(null,"Form kirim untuk "+resource+" belum tersedia..!!");
                return null;
        }
        return form;
    }

    // Form Kirim tidak punya setter publik, jadi filter diisi lewat nama komponen NetBeans (DTPCari1, DTPCari2, TCari, BtnCari) yang seragam di semua form Kirim.
    private void isiFilterKirim(JDialog form,String norawat,String tanggal){
        try {
            java.util.Date tglawal=java.sql.Date.valueOf(tanggal);
            Component c=cariKomponen(form.getContentPane(),"DTPCari1");
            if(c instanceof widget.Tanggal){
                ((widget.Tanggal)c).setDate(tglawal);
            }
            c=cariKomponen(form.getContentPane(),"DTPCari2");
            if(c instanceof widget.Tanggal){
                ((widget.Tanggal)c).setDate(Tgl2.getDate().before(tglawal)?tglawal:Tgl2.getDate());
            }
            c=cariKomponen(form.getContentPane(),"TCari");
            if(c instanceof widget.TextBox){
                ((widget.TextBox)c).setText(norawat);
            }
            c=cariKomponen(form.getContentPane(),"BtnCari");
            if(c instanceof widget.Button){
                ((widget.Button)c).doClick();
            }
        } catch (Exception e) {
            System.out.println("Notifikasi : "+e);
        }
    }

    private Component cariKomponen(java.awt.Container induk,String nama){
        for (Component c : induk.getComponents()) {
            if(nama.equals(c.getName())){
                return c;
            }
            if(c instanceof java.awt.Container){
                Component hasil=cariKomponen((java.awt.Container)c,nama);
                if(hasil!=null){
                    return hasil;
                }
            }
            // Menu "Pilih Semua" ada di popup menu tabel, bukan di pohon komponen biasa.
            if((c instanceof javax.swing.JComponent)&&(((javax.swing.JComponent)c).getComponentPopupMenu()!=null)){
                Component hasil=cariKomponen(((javax.swing.JComponent)c).getComponentPopupMenu(),nama);
                if(hasil!=null){
                    return hasil;
                }
            }
        }
        return null;
    }

    private void BtnKirimSemuaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnKirimSemuaActionPerformed
        if(ceksukses){
            JOptionPane.showMessageDialog(null,"Masih ada proses yang berjalan, harap tunggu terlebih dahulu...!");
            return;
        }
        int baris=tbBangsal.getSelectedRow();
        String norawat="",tglawal=Valid.SetTgl(Tgl1.getSelectedItem()+"");
        if(baris!=-1&&!tbBangsal.getValueAt(baris,0).toString().equals("")){
            int pilih=JOptionPane.showOptionDialog(null,"Kirim untuk kunjungan terpilih saja atau semua kunjungan periode "+Tgl1.getSelectedItem()+" s.d. "+Tgl2.getSelectedItem()+"?",
                    "Kirim Semua",JOptionPane.YES_NO_CANCEL_OPTION,JOptionPane.QUESTION_MESSAGE,null,
                    new Object[]{"Kunjungan "+tbBangsal.getValueAt(baris,0),"Semua Kunjungan Periode","Batal"},"Batal");
            if(pilih==0){
                norawat=tbBangsal.getValueAt(baris,0).toString();
                tglawal=tbBangsal.getValueAt(baris,1).toString();
            }else if(pilih!=1){
                return;
            }
        }
        String[] daftar=Resource.getSelectedIndex()>0?new String[]{Resource.getSelectedItem().toString()}:URUTANKIRIM;
        if(JOptionPane.showConfirmDialog(null,
                "Akan mengirim "+(daftar.length==1?daftar[0]:daftar.length+" modul")+" ke Satu Sehat untuk "+
                (norawat.equals("")?"semua kunjungan periode "+Tgl1.getSelectedItem()+" s.d. "+Tgl2.getSelectedItem():"kunjungan "+norawat)+".\n"+
                "Data yang terkirim masuk ke server "+koneksiDB.URLFHIRSATUSEHAT()+". Lanjutkan?",
                "Konfirmasi Kirim Semua",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION){
            return;
        }
        final String cari=norawat;
        final java.util.Date tgl1=java.sql.Date.valueOf(tglawal),tgl2=Tgl2.getDate().before(tgl1)?tgl1:Tgl2.getDate();
        runBackground(() -> {
            kirimBatch(daftar,cari,tgl1,tgl2);
            tampil();
        });
    }//GEN-LAST:event_BtnKirimSemuaActionPerformed

    // Urutan kirim mengikuti ketergantungan resource: Encounter dulu, lalu yang merujuk Encounter,
    // ServiceRequest -> Specimen -> Observation -> DiagnosticReport, MedicationRequest -> Dispense, dan Composition terakhir.
    private static final String[] URUTANKIRIM={
        "Encounter","Condition","EpisodeOfCare","Observation TTV","Procedure","Clinical Impression","CarePlan","Alergi",
        "Riwayat Keluarga","Goal","Risk Assessment","ANC","Diet","Imunisasi",
        "Permintaan Lab","Specimen Lab","Observation Lab","Hasil Lab",
        "Permintaan Lab MB","Specimen Lab MB","Observation Lab MB","Hasil Lab MB",
        "Permintaan Rad","Specimen Rad","Observation Rad","Hasil Rad",
        "Resep Obat","Pemberian Obat","Medication Statement","Telaah Farmasi","Questionnaire Response",
        "Composition Triase IGD","RME Rawat Jalan","RME Rawat Inap"
    };

    // Menjalankan form Kirim tanpa ditampilkan: isi filter, Cari, Pilih Semua, lalu Kirim, untuk setiap tab (TTV & Composition punya beberapa tab).
    // Logika kirim tetap milik form Kirim masing-masing, termasuk hak aksesnya (tombol Kirim nonaktif = dilewati).
    private void kirimBatch(String[] daftar,String norawat,java.util.Date tgl1,java.util.Date tgl2){
        StringBuilder ringkasan=new StringBuilder();
        for (int k = 0; k < daftar.length; k++) {
            String nama=daftar[k];
            setProses("Mengirim "+nama+" ("+(k+1)+"/"+daftar.length+")");
            System.out.println("Kirim Semua Satu Sehat : "+nama+" ("+(k+1)+"/"+daftar.length+")");
            JDialog[] wadah=new JDialog[1];
            try {
                SwingUtilities.invokeAndWait(() -> {
                    wadah[0]=formKirim(nama);
                    if(wadah[0]!=null){
                        // pack() membuat form "displayable" tanpa menampilkannya; runBackground di form Kirim menolak jalan kalau belum.
                        wadah[0].pack();
                        isiFilter(wadah[0],norawat,tgl1,tgl2);
                    }
                });
            } catch (Exception e) {
                System.out.println("Notifikasi : "+e);
            }
            JDialog form=wadah[0];
            if(form==null){
                continue;
            }
            try {
                Component kirim=cariKomponen(form.getContentPane(),"BtnKirim");
                if(!(kirim instanceof javax.swing.AbstractButton)||!kirim.isEnabled()){
                    ringkasan.append("- ").append(nama).append(" : dilewati, tidak ada hak akses kirim\n");
                    continue;
                }
                Component tab=cariKomponen(form.getContentPane(),"TabRawat");
                int jmltab=(tab instanceof javax.swing.JTabbedPane)?((javax.swing.JTabbedPane)tab).getTabCount():1;
                for (int t = 0; t < jmltab; t++) {
                    if(tab instanceof javax.swing.JTabbedPane){
                        final int indekstab=t;
                        SwingUtilities.invokeAndWait(() -> ((javax.swing.JTabbedPane)tab).setSelectedIndex(indekstab));
                    }
                    jalankan(cariKomponen(form.getContentPane(),"BtnCari"));
                    tungguForm(form);
                    jalankan(cariKomponen(form.getContentPane(),"ppPilihSemua"));
                    jalankan(kirim);
                }
            } catch (Exception e) {
                ringkasan.append("- ").append(nama).append(" : gagal, ").append(e).append("\n");
                System.out.println("Notifikasi : "+e);
            } finally {
                SwingUtilities.invokeLater(form::dispose);
            }
        }
        setProses("");
        String pesan="Kirim semua selesai. Cek status pada tabel; detail error ada di log/console (Response JSON)."+
                     (ringkasan.length()>0?"\n\n"+ringkasan:"");
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null,pesan));
    }

    private void isiFilter(JDialog form,String norawat,java.util.Date tgl1,java.util.Date tgl2){
        Component c=cariKomponen(form.getContentPane(),"DTPCari1");
        if(c instanceof widget.Tanggal){
            ((widget.Tanggal)c).setDate(tgl1);
        }
        c=cariKomponen(form.getContentPane(),"DTPCari2");
        if(c instanceof widget.Tanggal){
            ((widget.Tanggal)c).setDate(tgl2);
        }
        c=cariKomponen(form.getContentPane(),"TCari");
        if(c instanceof widget.TextBox){
            ((widget.TextBox)c).setText(norawat);
        }
    }

    // Memanggil action tombol/menu langsung di thread ini (bukan doClick) agar proses kirim tidak membekukan layar.
    private void jalankan(Component c){
        if(c instanceof javax.swing.AbstractButton){
            javax.swing.AbstractButton tombol=(javax.swing.AbstractButton)c;
            for (java.awt.event.ActionListener l : tombol.getActionListeners()) {
                l.actionPerformed(new java.awt.event.ActionEvent(tombol,java.awt.event.ActionEvent.ACTION_PERFORMED,tombol.getActionCommand()));
            }
        }
    }

    // Form Kirim memuat data di thread latar dan menandainya dengan field private "ceksukses"; ditunggu sampai selesai (maks. 10 menit).
    // Form tanpa field itu memuat datanya secara langsung, jadi tidak perlu ditunggu.
    private void tungguForm(JDialog form) throws InterruptedException {
        java.lang.reflect.Field proses;
        try {
            proses=form.getClass().getDeclaredField("ceksukses");
            proses.setAccessible(true);
        } catch (Exception e) {
            return;
        }
        long batas=System.currentTimeMillis()+600000;
        try {
            while(proses.getBoolean(form)&&System.currentTimeMillis()<batas){
                Thread.sleep(200);
            }
        } catch (IllegalAccessException e) {
            System.out.println("Notifikasi : "+e);
        }
    }

    private void setProses(String teks){
        SwingUtilities.invokeLater(() -> {
            ((javax.swing.border.TitledBorder)internalFrame1.getBorder()).setTitle("::[ Status Kiriman Data Satu Sehat ]::"+(teks.equals("")?"":" "+teks+"..."));
            internalFrame1.repaint();
        });
    }

    // Menjalankan sql count(*) dengan semua parameter "?" diisi no_rawat.
    private int hitung(String sql,String norawat){
        int hasil=0;
        try (PreparedStatement pshitung=koneksi.prepareStatement(sql)) {
            long jmlparameter=sql.chars().filter(c -> c=='?').count();
            for (int k = 1; k <= jmlparameter; k++) {
                pshitung.setString(k,norawat);
            }
            try (ResultSet rshitung=pshitung.executeQuery()) {
                if(rshitung.next()){
                    hasil=rshitung.getInt(1);
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi : "+e);
        }
        return hasil;
    }

    private void getData() {
        int row=tbBangsal.getSelectedRow();
        if(row!= -1){
            TKd.setText(tabMode.getValueAt(row,0).toString());
        }
    }

    private void isForm(){
        if(ChkInput.isSelected()==true){
            ChkInput.setVisible(false);
            PanelInput.setPreferredSize(new Dimension(WIDTH,65));
            FormInput.setVisible(true);
            ChkInput.setVisible(true);
        }else if(ChkInput.isSelected()==false){
            ChkInput.setVisible(false);
            PanelInput.setPreferredSize(new Dimension(WIDTH,20));
            FormInput.setVisible(false);
            ChkInput.setVisible(true);
        }
    }

    private void runBackground(Runnable task) {
        if (ceksukses) return;
        if (executor.isShutdown() || executor.isTerminated()) return;
        if (!isDisplayable()) return;

        ceksukses = true;
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        try {
            executor.submit(() -> {
                try {
                    task.run();
                } finally {
                    ceksukses = false;
                    SwingUtilities.invokeLater(() -> {
                        if (isDisplayable()) {
                            setCursor(Cursor.getDefaultCursor());
                        }
                    });
                }
            });
        } catch (RejectedExecutionException ex) {
            ceksukses = false;
        }
    }

    @Override
    public void dispose() {
        executor.shutdownNow();
        super.dispose();
    }
}
