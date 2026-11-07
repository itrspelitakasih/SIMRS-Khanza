package rekammedis;

import fungsi.WarnaTable;
import fungsi.batasInput;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import fungsi.akses;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import kepegawaian.DlgCariDokter;
import kepegawaian.DlgCariPetugas;
import simrskhanza.DlgCariPerusahaan;

/**
 *
 * @author perpustakaan
 */
public final class RMMCURSPK extends javax.swing.JDialog {
    private static final String[] URUTAN_KOLOM_TAMPIL_MCU = {
            "No.Rawat", "Badge", "No.RM", "Nama Pasien", "Tanggal", "Unit/Poliklinik", "Dokter PJ", "Perusahaan",
            "Kesimpulan"
    };
    private final DefaultTableModel tabMode;
    private Connection koneksi = koneksiDB.condb();
    private sekuel Sequel = new sekuel();
    private validasi Valid = new validasi();
    private PreparedStatement ps;
    private ResultSet rs;
    private int i = 0;
    private DlgCariDokter dokter;
    private DlgCariPetugas petugas;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean ceksukses = false;
    private String TANGGALMUNDUR = "yes";
    private DlgCariPerusahaan perusahaan;
    private boolean modeCopyPengkajian = false;
    private int barisSumberCopyPengkajian = -1;

    /**
     * Creates new form DlgRujuk
     * 
     * @param parent
     * @param modal
     */
    public RMMCURSPK(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        aturInputKesimpulanMcu();
        aturTabTraversal(internalFrame1);
        aturKeypressRiwayatPenyakit();
        aturKeypressLaboratoriumRontgen();
        aturKeypressPemeriksaanKhusus();
        aturDass();
        aturNavigasiEnter();

        tabMode = new DefaultTableModel(null, new Object[] {
                "Tahun",
                "No.Rawat",
                "No.RM",
                "Nama Pasien",
                "Surname",
                "Perusahaan",
                "Badge",
                "Alamat",
                "Tanggal",
                "Kode Dokter",
                "Dokter PJ",
                "Kode Petugas",
                "Nama Petugas",
                "Kode Petugas Lab",
                "Nama Petugas Lab",
                "Saran",
                "MCU Grup",
                "Medical History",
                "Physical Examination",
                "Kesimpulan Laboratorium",
                "Conclusion Chest X Ray",
                "Conclusion EKG",
                "Conclusion Spirometry",
                "Conclusion Audiometry",
                "Kesimpulan",
                "Tmp.Lahir",
                "Tgl.Lahir",
                "J.K.",
                "No.Telp",
                "Suku/Bangsa",
                "Status Nikah",
                "Date/Year of Employment",
                "Year of Employment",
                "Job Title",
                "Activities",
                "Hobby",
                "Moonlight Working",
                "Position Applied For",
                "Departemen",
                "Supervisor",
                "Manager",
                "Driving/Mobile Equipment",
                "Working at Heights",
                "Clerical/Administrative",
                "Requires Colour Vision",
                "Potential Dust Exposure",
                "Catering/Food Handler",
                "Other Potential Dangerous",
                "Head Injury/Contussion",
                "Fainting/Blackouts/Epilepsy",
                "Visual Changes",
                "Hearing Loss",
                "Nose/Sinus/Throat >4 Weeks",
                "Gynaecological Problems",
                "Chronic Skin Problem",
                "Chronic Diarrhea",
                "Anorexia >4 Weeks",
                "Gastritis",
                "Jaundice/Hepatitis",
                "Chronic Cough >4 Weeks",
                "Haemorhoid",
                "Chronic Abdominal Pain",
                "Diabetes",
                "Asthma",
                "Allergies",
                "Tuberculosis/Bronchitis",
                "Psychiatric Disorder",
                "Sexual Transmitted Diseases",
                "Weight Change >5kg/Month",
                "Hypertension",
                "Chest Pain/Heart Disease",
                "Malaria/Tropical Disease",
                "Surgery/Operation",
                "Back Pain >4 Weeks",
                "Thypoid Fever",
                "Swollen/Painful Joints",
                "Kidney Problem/Urinary Stones",
                "Other Chronical Diseases",
                "Family History Father",
                "Family History Mother",
                "Family History Siblings",
                "Family History Others",
                "Cigarettes Perday",
                "Alcohol Gr/Week",
                "Prescribed Medication 1",
                "Prescribed Medication 2",
                "Any Allergies",
                "Hb",
                "WBC",
                "ESR",
                "Bl.Gr",
                "GT",
                "SGOT",
                "SGPT",
                "Urea",
                "Crea",
                "Gluco",
                "Random Glucose",
                "Total Cholestrol",
                "Protein",
                "Blood",
                "Bilirubin",
                "Malaria",
                "TPHA",
                "Mantoux Test",
                "Leukosit",
                "Lab Others",
                "OVA",
                "Culture",
                "Cysta",
                "Parasites",
                "Pneumoconiosis",
                "Pneumoconiosis Detail",
                "ILO Classification",
                "ILO Classification Detail",
                "Other Abnormalities",
                "Evidence of TB",
                "Evidence of TB Detail",
                "Comment",
                "T.D.",
                "Nadi",
                "R.R.",
                "T.B.",
                "B.B.",
                "B.M.I.",
                "Klasifikasi BMI",
                "Pemeriksaan Laboratorium",
                "Rontgen Thorax",
                "ECG Abnormal",
                "VC (1)",
                "VC (2)",
                "VC (3)",
                "VC (4)",
                "FVC (1)",
                "FVC (2)",
                "FVC (3)",
                "FVC (4)",
                "FEV (1)",
                "FEV (2)",
                "FEV (3)",
                "FEV (4)",
                "FEV 1/FVC (1)",
                "FEV 1/FVC (2)",
                "FEV 1/FVC (3)",
                "FEV 1/FVC (4)",
                "Tinitus Never",
                "Tinitus Previously",
                "Tinitus Rarely",
                "Tinitus Often",
                "Tinitus Always",
                "Ear Protection Never",
                "Ear Protection Previously",
                "Ear Protection Rarely",
                "Ear Protection Often",
                "Ear Protection Always",
                "Type of Hearing Protection",
                "Left Ear AB 500",
                "Left Ear AB 1000",
                "Left Ear AB 1500",
                "Left Ear AB 2000",
                "Left Ear AB 3000",
                "Left Ear AB 4000",
                "Left Ear AB 5000",
                "Left Ear AB 6000",
                "Left Ear AC 500",
                "Left Ear AC 1000",
                "Left Ear AC 1500",
                "Left Ear AC 2000",
                "Left Ear AC 3000",
                "Left Ear AC 4000",
                "Left Ear AC 5000",
                "Left Ear AC 6000",
                "Right Ear AB 500",
                "Right Ear AB 1000",
                "Right Ear AB 1500",
                "Right Ear AB 2000",
                "Right Ear AB 3000",
                "Right Ear AB 4000",
                "Right Ear AB 5000",
                "Right Ear AB 6000",
                "Right Ear AC 500",
                "Right Ear AC 1000",
                "Right Ear AC 1500",
                "Right Ear AC 2000",
                "Right Ear AC 3000",
                "Right Ear AC 4000",
                "Right Ear AC 5000",
                "Right Ear AC 6000",
                "Unaided distant R",
                "Unaided distant L",
                "Glasses distant R",
                "Glasses distant L",
                "Unaided near R",
                "Unaided near L",
                "Glasses near R",
                "Glasses near L",
                "Night Vision 1",
                "Night Vision 2",
                "Brake Test 1",
                "Brake Test 2",
                "Buta Warna",
                "Lapang Pandang L",
                "Lapang Pandang R",
                "Fundi",
                "BCG",
                "DPT",
                "Polio",
                "Morbili",
                "Thyphoid",
                "HEP A",
                "HEP B",
                "Tetanus",
                "Imunisasi Other",
                "Scoliosis",
                "Kyphosis",
                "Lordosis",
                "Forward Flexion 0-80",
                "Hyperextensi 0-25",
                "Lateral Flexion 0-20",
                "Heel Walking",
                "Toe Walking",
                "Squats x3",
                "ENT",
                "Cardio Vascular System",
                "Respiratory System",
                "Abdomen",
                "Genito Urinary System",
                "Central & Peripheral Nervous Sys",
                "Skin",
                "Lymph Nodes",
                "Dental",
                "Muskuloskeletal",
                "Requires Spectacles",
                "Colour Blindness",
                "Respiratory Problem",
                "Impaired Hearing",
                "Vertigo",
                "Gol. Darah",
                "Medically Fit",
                "Fit With Restrictions",
                "Specify",
                "Saran",
                "Trombosit",
                "Rhesus",
                "Trigliserida",
                "HDL Kolesterol",
                "LDL Kolesterol",
                "Asam Urat",
                "Urine Warna",
                "Urine Kejernihan",
                "Urine Reaksi Kimia",
                "Urine Keton",
                "Urine Glukosa",
                "Urine Nitrit",
                "Urine Sel Darah Putih",
                "Urine Sel Darah Merah",
                "Urine Bakteri",
                "Urine Kristal",
                "Urine Epitel",
                "HBsAg",
                "Anti HBs",
                "CEA",
                "AFP",
                "Narkoba Amphetamine",
                "Narkoba Methamphetamine",
                "Narkoba Morphine",
                "Narkoba Benzodiazepine",
                "Narkoba Cocain",
                "Narkoba Marijuana",
                "Unit/Poliklinik"
        }) {
            @Override
            public boolean isCellEditable(int rowIndex, int colIndex) {
                return false;
            }
        };
        tbObat.setModel(tabMode);

        tbObat.setPreferredScrollableViewportSize(new Dimension(500, 500));
        tbObat.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < tabMode.getColumnCount(); i++) {
            TableColumn column = tbObat.getColumnModel().getColumn(i);
            String header = tabMode.getColumnName(i);
            if (isKolomSembunyiUi(header)) {
                column.setMinWidth(0);
                column.setMaxWidth(0);
                column.setPreferredWidth(0);
            } else {
                column.setMinWidth(0);
                column.setMaxWidth(Integer.MAX_VALUE);
                column.setPreferredWidth(getLebarKolomMcu(header));
            }
        }
        for (i = 0; i < URUTAN_KOLOM_TAMPIL_MCU.length; i++) {
            int indexModel = tabMode.findColumn(URUTAN_KOLOM_TAMPIL_MCU[i]);
            int indexView = tbObat.convertColumnIndexToView(indexModel);
            tbObat.getColumnModel().moveColumn(indexView, i);
        }
        tbObat.setDefaultRenderer(Object.class, new WarnaTable());

    TNoRw.setDocument(new batasInput((byte)17).getKata(TNoRw));surname.setDocument(new batasInput((int)100).getKata(surname));kesimpulan.setDocument(new batasInput((int)2000).getKata(kesimpulan));

    // RiwayatAlergiMakanan.setDocument(new
    // batasInput((int)150).getKata(RiwayatAlergiMakanan));
    TD.setDocument(new batasInput((byte)8).getKata(TD));Nadi.setDocument(new batasInput((byte)5).getKata(Nadi));RR.setDocument(new batasInput((byte)5).getKata(RR));TB.setDocument(new batasInput((byte)5).getKata(TB));BB.setDocument(new batasInput((byte)5).getKata(BB));
    // Suhu.setDocument(new batasInput((byte)5).getKata(Suhu));
    IMT.setDocument(new batasInput((byte)6).getKata(IMT));
    // LP.setDocument(new batasInput((byte)6).getKata(LP));
    // Rambut.setDocument(new batasInput((int)100).getKata(Rambut));
    // Visus.setDocument(new batasInput((int)50).getKata(Visus));
    // KeteranganLuasLapangPandang.setDocument(new
    // batasInput((int)50).getKata(KeteranganLuasLapangPandang));
    FamilyHistoryFather.setDocument(new batasInput((int)100).getKata(FamilyHistoryFather));
    // PenyakitKulit.setDocument(new batasInput((int)100).getKata(PenyakitKulit));
    // KeteranganAreaGenitalia.setDocument(new
    // batasInput((int)100).getKata(KeteranganAreaGenitalia));
    // KeteranganAnus.setDocument(new batasInput((int)100).getKata(KeteranganAnus));
    // KetExtremitasAtas.setDocument(new
    // batasInput((byte)50).getKata(KetExtremitasAtas));
    // KetExtremitasBawah.setDocument(new
    // batasInput((byte)50).getKata(KetExtremitasBawah));
    PemeriksaanLaboratorium.setDocument(new batasInput((int)1000).getKata(PemeriksaanLaboratorium));RongsenThorax.setDocument(new batasInput((int)1000).getKata(RongsenThorax));ConcRadiologi.setDocument(new batasInput((int)1000).getKata(ConcRadiologi));ConcEcg.setDocument(new batasInput((int)1000).getKata(ConcEcg));ecg_abnormal.setDocument(new batasInput((int)255).getKata(ecg_abnormal));type_of_hearing.setDocument(new batasInput((int)255).getKata(type_of_hearing));eye_unaided_distant_l.setDocument(new batasInput((int)20).getKata(eye_unaided_distant_l));eye_unaided_distant_r.setDocument(new batasInput((int)20).getKata(eye_unaided_distant_r));ConcSpirometry.setDocument(new batasInput((int)1000).getKata(ConcSpirometry));ConcAudiometry.setDocument(new batasInput((int)1000).getKata(ConcAudiometry));

    sinkronConcEcg();bl_group.setDocument(new batasInput((byte)3).getKata(bl_group));

    sinkronBloodGroup();
//        Treadmill.setDocument(new batasInput((int)1000).getKata(Treadmill));
//        Romberg.setDocument(new batasInput((int)1000).getKata(Romberg));
//        Backstrength.setDocument(new batasInput((int)1000).getKata(Backstrength));
//        AbiTanganKanan.setDocument(new batasInput((int)1000).getKata(AbiTanganKanan));
//        AbiTanganKiri.setDocument(new batasInput((int)1000).getKata(AbiTanganKiri));
//        AbiKakiKanan.setDocument(new batasInput((int)1000).getKata(AbiKakiKanan));
//        AbiKakiKiri.setDocument(new batasInput((int)1000).getKata(AbiKakiKiri));
//        Lainlain.setDocument(new batasInput((int)1000).getKata(Lainlain));
//        Merokok.setDocument(new batasInput((int)100).getKata(Merokok));
//        Alkohol.setDocument(new batasInput((int)100).getKata(Alkohol));
        PhyExam.setDocument(new batasInput((int)1000).getKata(PhyExam));
        catatan.setDocument(new batasInput((int)1000).getKata(catatan));
        Dass21.setDocument(new batasInput((int)1000).getKata(Dass21));
        TCari.setDocument(new batasInput((int)100).getKata(TCari));
        
        BB.getDocument().addDocumentListener(new javax.swing.event.DocumentListener(){
            @Override
            public void insertUpdate(DocumentEvent e) {
                isBMI();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                isBMI();
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                isBMI();
            }
        });
        
        TB.getDocument().addDocumentListener(new javax.swing.event.DocumentListener(){
            @Override
            public void insertUpdate(DocumentEvent e) {
                isBMI();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                isBMI();
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                isBMI();
            }
        });
        
        IMT.getDocument().addDocumentListener(new javax.swing.event.DocumentListener(){
            @Override
            public void insertUpdate(DocumentEvent e) {
                isLP();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                isLP();
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                isLP();
            }
        });
        
//        LP.getDocument().addDocumentListener(new javax.swing.event.DocumentListener(){
//            @Override
//            public void insertUpdate(DocumentEvent e) {
//                isLP();
//            }
//            @Override
//            public void removeUpdate(DocumentEvent e) {
//                isLP();
//            }
//            @Override
//            public void changedUpdate(DocumentEvent e) {
//                isLP();
//            }
//        });
        
        HTMLEditorKit kit = new HTMLEditorKit();
        LoadHTML.setEditable(true);
        LoadHTML.setEditorKit(kit);
        StyleSheet styleSheet = kit.getStyleSheet();
        styleSheet.addRule(
                ".isi td{border-right: 1px solid #e2e7dd;font: 8.5px Arial;height:12px;border-bottom: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"+
                ".isi2 td{font: 8.5px Arial;border:none;height:12px;background: #ffffff;color:#323232;}"+
                ".isi3 td{border-right: 1px solid #e2e7dd;font: 8.5px Arial;height:12px;border-top: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"+
                ".isi4 td{font: 11px Arial;height:12px;border-top: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"+
                ".isi5 td{font: 8.5px Arial;border:none;height:12px;background: #ffffff;color:#AA0000;}"+
                ".isi6 td{font: 8.5px Arial;border:none;height:12px;background: #ffffff;color:#FF0000;}"+
                ".isi7 td{font: 8.5px Arial;border:none;height:12px;background: #ffffff;color:#C8C800;}"+
                ".isi8 td{font: 8.5px Arial;border:none;height:12px;background: #ffffff;color:#00AA00;}"+
                ".isi9 td{font: 8.5px Arial;border:none;height:12px;background: #ffffff;color:#969696;}"
        );
        Document doc = kit.createDefaultDocument();
        LoadHTML.setDocument(doc);
        
        try {
            TANGGALMUNDUR=koneksiDB.TANGGALMUNDUR();
        } catch (Exception e) {
            TANGGALMUNDUR="yes";
        }
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        LoadHTML = new widget.editorpane();
        jPopupMenu1 = new javax.swing.JPopupMenu();
        MnPenilaianMCU = new javax.swing.JMenuItem();
        MnCopyPengkajian = new javax.swing.JMenuItem();
        TanggalRegistrasi = new widget.TextBox();
        WindowGabungMCU = new javax.swing.JDialog();
        internalFrame8 = new widget.InternalFrame();
        BtnCloseIn6 = new widget.Button();
        BtnSimpan6 = new widget.Button();
        label40 = new widget.Label();
        NoRmTujuan = new widget.TextBox();
        NmPasienTujuan = new widget.TextBox();
        label41 = new widget.Label();
        BtnCari1 = new widget.Button();
        internalFrame1 = new widget.InternalFrame();
        panelGlass8 = new widget.panelisi();
        BtnSimpan = new widget.Button();
        BtnBatal = new widget.Button();
        BtnHapus = new widget.Button();
        BtnEdit = new widget.Button();
        BtnPrint = new widget.Button();
        BtnAll = new widget.Button();
        BtnKeluar = new widget.Button();
        BtnRiwayatMCU = new widget.Button();
        TabRawat = new javax.swing.JTabbedPane();
        internalFrame2 = new widget.InternalFrame();
        scrollInput = new widget.ScrollPane();
        FormInput = new widget.PanelBiasa();
        LblMcu1 = new widget.Label();
        LblMcu2 = new widget.Label();
        LblMcu3 = new widget.Label();
        LblMcu4 = new widget.Label();
        LblMcu5 = new widget.Label();
        LblMcu6 = new widget.Label();
        LblMcu7 = new widget.Label();
        SepMcu1 = new javax.swing.JSeparator();
        LblMcu8 = new widget.Label();
        LblMcu9 = new widget.Label();
        LblMcu10 = new widget.Label();
        LblMcu11 = new widget.Label();
        LblMcu12 = new widget.Label();
        LblMcu13 = new widget.Label();
        LblMcu14 = new widget.Label();
        LblMcu15 = new widget.Label();
        LblMcu16 = new widget.Label();
        LblMcu17 = new widget.Label();
        LblMcu18 = new widget.Label();
        LblMcu19 = new widget.Label();
        LblMcu20 = new widget.Label();
        LblMcu21 = new widget.Label();
        LblMcu22 = new widget.Label();
        LblMcu23 = new widget.Label();
        LblMcu24 = new widget.Label();
        SepMcu2 = new javax.swing.JSeparator();
        LblMcu25 = new widget.Label();
        LblMcu26 = new widget.Label();
        SepMcu3 = new javax.swing.JSeparator();
        LblMcu27 = new widget.Label();
        LblMcu28 = new widget.Label();
        LblMcu29 = new widget.Label();
        LblMcu30 = new widget.Label();
        LblMcu31 = new widget.Label();
        LblMcu32 = new widget.Label();
        LblMcu33 = new widget.Label();
        LblMcu34 = new widget.Label();
        LblMcu35 = new widget.Label();
        LblMcu36 = new widget.Label();
        SepMcu4 = new javax.swing.JSeparator();
        LblMcu37 = new widget.Label();
        LblMcu38 = new widget.Label();
        LblMcu39 = new widget.Label();
        LblMcu40 = new widget.Label();
        LblMcu41 = new widget.Label();
        LblMcu42 = new widget.Label();
        LblMcu43 = new widget.Label();
        LblMcu44 = new widget.Label();
        LblMcu45 = new widget.Label();
        LblMcu46 = new widget.Label();
        LblMcu47 = new widget.Label();
        LblMcu48 = new widget.Label();
        LblMcu49 = new widget.Label();
        LblMcu50 = new widget.Label();
        LblMcu51 = new widget.Label();
        LblMcu52 = new widget.Label();
        LblMcu53 = new widget.Label();
        LblMcu54 = new widget.Label();
        LblMcu55 = new widget.Label();
        LblMcu56 = new widget.Label();
        LblMcu57 = new widget.Label();
        LblMcu58 = new widget.Label();
        LblMcu59 = new widget.Label();
        LblMcu60 = new widget.Label();
        LblMcu61 = new widget.Label();
        SepMcu5 = new javax.swing.JSeparator();
        LblMcu62 = new widget.Label();
        LblMcu63 = new widget.Label();
        LblMcu64 = new widget.Label();
        LblMcu65 = new widget.Label();
        LblMcu66 = new widget.Label();
        LblMcu67 = new widget.Label();
        LblMcu68 = new widget.Label();
        SepMcu6 = new javax.swing.JSeparator();
        LblMcu69 = new widget.Label();
        LblMcu70 = new widget.Label();
        LblMcu71 = new widget.Label();
        LblMcu72 = new widget.Label();
        LblMcu73 = new widget.Label();
        LblMcu74 = new widget.Label();
        LblMcu75 = new widget.Label();
        LblMcu76 = new widget.Label();
        LblMcu77 = new widget.Label();
        LblMcu78 = new widget.Label();
        LblMcu79 = new widget.Label();
        LblMcu80 = new widget.Label();
        SepMcu7 = new javax.swing.JSeparator();
        LblMcu81 = new widget.Label();
        LblMcu82 = new widget.Label();
        LblMcu83 = new widget.Label();
        LblMcu84 = new widget.Label();
        LblMcu85 = new widget.Label();
        SepMcu8 = new javax.swing.JSeparator();
        LblMcu86 = new widget.Label();
        LblMcu87 = new widget.Label();
        LblMcu88 = new widget.Label();
        LblMcu89 = new widget.Label();
        LblMcu90 = new widget.Label();
        LblMcu91 = new widget.Label();
        LblMcu92 = new widget.Label();
        SepMcu9 = new javax.swing.JSeparator();
        LblMcu93 = new widget.Label();
        LblMcu94 = new widget.Label();
        SepMcu10 = new javax.swing.JSeparator();
        LblMcu95 = new widget.Label();
        LblMcu96 = new widget.Label();
        LblMcu97 = new widget.Label();
        LblMcu98 = new widget.Label();
        LblMcu99 = new widget.Label();
        LblMcu100 = new widget.Label();
        LblMcu101 = new widget.Label();
        SepMcu11 = new javax.swing.JSeparator();
        LblMcu102 = new widget.Label();
        LblMcu103 = new widget.Label();
        LblMcu104 = new widget.Label();
        SepMcu12 = new javax.swing.JSeparator();
        LblMcu105 = new widget.Label();
        LblMcu106 = new widget.Label();
        LblMcu107 = new widget.Label();
        LblMcu108 = new widget.Label();
        LblMcu109 = new widget.Label();
        LblMcu110 = new widget.Label();
        LblMcu111 = new widget.Label();
        LblMcu112 = new widget.Label();
        LblMcu113 = new widget.Label();
        SepMcu13 = new javax.swing.JSeparator();
        LblMcu114 = new widget.Label();
        LblMcu115 = new widget.Label();
        LblMcu116 = new widget.Label();
        LblMcu117 = new widget.Label();
        LblMcu118 = new widget.Label();
        LblMcu119 = new widget.Label();
        LblMcu120 = new widget.Label();
        LblMcu121 = new widget.Label();
        LblMcu122 = new widget.Label();
        LblMcu123 = new widget.Label();
        LblMcu124 = new widget.Label();
        LblMcu125 = new widget.Label();
        LblMcu126 = new widget.Label();
        LblMcu127 = new widget.Label();
        LblMcu128 = new widget.Label();
        LblMcu129 = new widget.Label();
        LblMcu130 = new widget.Label();
        SepMcu14 = new javax.swing.JSeparator();
        LblMcu131 = new widget.Label();
        LblMcu132 = new widget.Label();
        LblMcu133 = new widget.Label();
        LblMcu134 = new widget.Label();
        LblMcu135 = new widget.Label();
        LblMcu136 = new widget.Label();
        LblMcu137 = new widget.Label();
        LblMcu138 = new widget.Label();
        LblMcu139 = new widget.Label();
        LblMcu140 = new widget.Label();
        LblMcu141 = new widget.Label();
        LblMcu142 = new widget.Label();
        LblMcu143 = new widget.Label();
        LblMcu144 = new widget.Label();
        SepMcu15 = new javax.swing.JSeparator();
        LblMcu145 = new widget.Label();
        LblMcu146 = new widget.Label();
        LblMcu147 = new widget.Label();
        LblMcu148 = new widget.Label();
        LblMcu149 = new widget.Label();
        LblMcu150 = new widget.Label();
        LblMcu151 = new widget.Label();
        LblMcu152 = new widget.Label();
        LblMcu153 = new widget.Label();
        LblMcu154 = new widget.Label();
        LblMcu155 = new widget.Label();
        LblMcu156 = new widget.Label();
        LblMcu157 = new widget.Label();
        LblMcu158 = new widget.Label();
        SepMcu16 = new javax.swing.JSeparator();
        LblMcu159 = new widget.Label();
        LblMcu160 = new widget.Label();
        LblMcu161 = new widget.Label();
        LblMcu162 = new widget.Label();
        LblMcu163 = new widget.Label();
        LblMcu164 = new widget.Label();
        LblMcu165 = new widget.Label();
        LblMcu166 = new widget.Label();
        LblMcu167 = new widget.Label();
        LblMcu168 = new widget.Label();
        SepMcu17 = new javax.swing.JSeparator();
        LblMcu169 = new widget.Label();
        LblMcu170 = new widget.Label();
        LblMcu171 = new widget.Label();
        LblMcu172 = new widget.Label();
        LblMcu173 = new widget.Label();
        LblMcu174 = new widget.Label();
        LblMcu175 = new widget.Label();
        LblMcu176 = new widget.Label();
        LblMcu177 = new widget.Label();
        LblMcu178 = new widget.Label();
        SepMcu18 = new javax.swing.JSeparator();
        LblMcu179 = new widget.Label();
        LblMcu180 = new widget.Label();
        LblMcu181 = new widget.Label();
        LblMcu182 = new widget.Label();
        LblMcu183 = new widget.Label();
        LblMcu184 = new widget.Label();
        LblMcu185 = new widget.Label();
        LblMcu186 = new widget.Label();
        LblMcu187 = new widget.Label();
        LblMcu188 = new widget.Label();
        LblMcu189 = new widget.Label();
        LblMcu190 = new widget.Label();
        LblMcu191 = new widget.Label();
        SepMcu19 = new javax.swing.JSeparator();
        LblMcu192 = new widget.Label();
        LblMcu193 = new widget.Label();
        LblMcu194 = new widget.Label();
        LblMcu195 = new widget.Label();
        LblMcu196 = new widget.Label();
        SepMcu20 = new javax.swing.JSeparator();
        LblMcu197 = new widget.Label();
        LblMcu198 = new widget.Label();
        LblMcu199 = new widget.Label();
        LblMcu200 = new widget.Label();
        LblMcu201 = new widget.Label();
        LblMcu202 = new widget.Label();
        LblMcu203 = new widget.Label();
        LblMcu204 = new widget.Label();
        LblMcu205 = new widget.Label();
        SepMcu21 = new javax.swing.JSeparator();
        LblMcu206 = new widget.Label();
        LblMcu207 = new widget.Label();
        LblMcu208 = new widget.Label();
        LblMcu209 = new widget.Label();
        SepMcu22 = new javax.swing.JSeparator();
        LblMcu210 = new widget.Label();
        LblMcu211 = new widget.Label();
        LblMcu212 = new widget.Label();
        LblMcu213 = new widget.Label();
        LblMcu214 = new widget.Label();
        LblMcu215 = new widget.Label();
        LblMcu216 = new widget.Label();
        SepMcu23 = new javax.swing.JSeparator();
        LblMcu217 = new widget.Label();
        LblMcu218 = new widget.Label();
        LblMcu219 = new widget.Label();
        LblMcu220 = new widget.Label();
        TNoRw = new widget.TextBox();
        surname = new widget.TextBox();
        TNoRM = new widget.TextBox();
        NmDokter = new widget.TextBox();
        BtnDokter = new widget.Button();
        TglAsuhan = new widget.Tanggal();
        scrollPane10 = new widget.ScrollPane();
        PemeriksaanLaboratorium = new widget.TextArea();
        scrollPane11 = new widget.ScrollPane();
        RongsenThorax = new widget.TextArea();
        scrollPane15 = new widget.ScrollPane();
        specify = new widget.TextArea();
        BtnDokter3 = new widget.Button();
        BtnDokter2 = new widget.Button();
        FamilyHistoryFather = new widget.TextBox();
        SttsNikah = new widget.ComboBox();
        TmpLahir = new widget.TextBox();
        NoTlp = new widget.TextBox();
        Perusahaan = new widget.TextBox();
        Doe = new widget.TextBox();
        Yoe = new widget.TextBox();
        JobTitle = new widget.TextBox();
        Activities = new widget.TextBox();
        Hobby = new widget.TextBox();
        OtherJob = new widget.TextBox();
        JobInvolvesExposingToOtherPotentialDangerous = new widget.CekBox();
        JobInvolvesDrivingOrOperatingMobileEquipment = new widget.CekBox();
        JobInvolvesWorkingAtHeights = new widget.CekBox();
        JobInvolvesClericalOfficeBasedOrAdministrative = new widget.CekBox();
        JobInvolvesRequiresColourVision = new widget.CekBox();
        JobInvolvesPotentialDustExposure = new widget.CekBox();
        JobInvolvesCateringStaffIncludingFoodHandlers = new widget.CekBox();
        McuGroup = new widget.ComboBox();
        RWP1 = new widget.CekBox();
        RWP2 = new widget.CekBox();
        RWP3 = new widget.CekBox();
        RWP4 = new widget.CekBox();
        RWP5 = new widget.CekBox();
        RWP8 = new widget.CekBox();
        RWP7 = new widget.CekBox();
        RWP6 = new widget.CekBox();
        RWP10 = new widget.CekBox();
        RWP9 = new widget.CekBox();
        RWP11 = new widget.CekBox();
        RWP12 = new widget.CekBox();
        RWP13 = new widget.CekBox();
        RWP14 = new widget.CekBox();
        RWP15 = new widget.CekBox();
        RWP18 = new widget.CekBox();
        RWP17 = new widget.CekBox();
        RWP16 = new widget.CekBox();
        RWP20 = new widget.CekBox();
        RWP19 = new widget.CekBox();
        RWP24 = new widget.CekBox();
        RWP26 = new widget.CekBox();
        RWP28 = new widget.CekBox();
        RWP25 = new widget.CekBox();
        RWP22 = new widget.CekBox();
        RWP27 = new widget.CekBox();
        RWP23 = new widget.CekBox();
        RWP29 = new widget.CekBox();
        RWP21 = new widget.CekBox();
        RWP30 = new widget.CekBox();
        FamilyHistoryMother = new widget.TextBox();
        FamilyHistorySiblings = new widget.TextBox();
        FamilyHistoryOther = new widget.TextBox();
        CigarettesPerday = new widget.TextBox();
        AlcoholGrWeek = new widget.TextBox();
        PrescribedMedication = new widget.TextBox();
        PrescribedMedication2 = new widget.TextBox();
        spirometri_vc_1 = new widget.TextBox();
        PosisiKerja = new widget.ComboBox();
        AnyAllergies = new widget.TextBox();
        spirometri_vc_2 = new widget.TextBox();
        spirometri_vc_3 = new widget.TextBox();
        spirometri_vc_4 = new widget.TextBox();
        hb = new widget.TextBox();
        spirometri_fvc_2 = new widget.TextBox();
        spirometri_fvc_3 = new widget.TextBox();
        spirometri_fvc_4 = new widget.TextBox();
        spirometri_fev_1_3 = new widget.TextBox();
        spirometri_fev_1_2 = new widget.TextBox();
        spirometri_fev_1_4 = new widget.TextBox();
        spirometri_fev_1_1 = new widget.TextBox();
        spirometri_fev_1_fvc_2 = new widget.TextBox();
        spirometri_fev_1_fvc_4 = new widget.TextBox();
        spirometri_fev_1_fvc_3 = new widget.TextBox();
        spirometri_fev_1_fvc_1 = new widget.TextBox();
        audiometri_tinitus_always = new widget.CekBox();
        audiometri_tinitus_never = new widget.CekBox();
        audiometri_tinitus_previously = new widget.CekBox();
        audiometri_tinitus_rarely = new widget.CekBox();
        audiometri_tinitus_often = new widget.CekBox();
        audiometri_ear_protection_worn_always = new widget.CekBox();
        audiometri_ear_protection_worn_never = new widget.CekBox();
        audiometri_ear_protection_worn_previously = new widget.CekBox();
        audiometri_ear_protection_worn_rarely = new widget.CekBox();
        audiometri_ear_protection_worn_often = new widget.CekBox();
        BB = new widget.TextBox();
        Nadi = new widget.TextBox();
        TD = new widget.TextBox();
        RR = new widget.TextBox();
        TB = new widget.TextBox();
        IMT = new widget.TextBox();
        visual_fields_right = new widget.TextBox();
        audiometri_left_ear_1000 = new widget.TextBox();
        audiometri_left_ear_2000 = new widget.TextBox();
        audiometri_left_ear_1500 = new widget.TextBox();
        audiometri_left_ear_500 = new widget.TextBox();
        audiometri_left_ear_3000 = new widget.TextBox();
        audiometri_left_ear_4000 = new widget.TextBox();
        audiometri_left_ear_6000 = new widget.TextBox();
        audiometri_left_ear_5000 = new widget.TextBox();
        cbConcEcg = new widget.ComboBox();
        eye_color_blindless = new widget.ComboBox();
        KlasifikasiIMT1 = new widget.TextBox();
        fundi = new widget.ComboBox();
        imunisasi_bcg = new widget.ComboBox();
        imunisasi_dpt = new widget.ComboBox();
        imunisasi_polio = new widget.ComboBox();
        imunisasi_hep_b = new widget.ComboBox();
        imunisasi_morbili = new widget.ComboBox();
        imunisasi_tetanus = new widget.ComboBox();
        imunisasi_thyphoid = new widget.ComboBox();
        imunisasi_others = new widget.ComboBox();
        imunisasi_hep_a = new widget.ComboBox();
        vertebra_scoliosis = new widget.ComboBox();
        vertebra_lordosis = new widget.ComboBox();
        vertebra_hyperextensi_0_25 = new widget.ComboBox();
        vertebra_forward_flexion_0_80 = new widget.ComboBox();
        vertebra_heel_walking = new widget.ComboBox();
        vertebra_lateral_flexion_0_20 = new widget.ComboBox();
        vertebra_squats_x3 = new widget.ComboBox();
        vertebra_toe_walking = new widget.ComboBox();
        vertebra_kyphosis = new widget.ComboBox();
        exam_ent_comments = new widget.TextBox();
        exam_cardio_vascular_system_comments = new widget.TextBox();
        exam_respiratory_system_comments = new widget.TextBox();
        exam_abdomen_comments = new widget.TextBox();
        exam_genito_urinary_system_comments = new widget.TextBox();
        exam_central_peripheral_nervous_system_comments = new widget.TextBox();
        exam_skin_comments = new widget.TextBox();
        exam_lymph_nodes_comments = new widget.TextBox();
        SukuBangsa = new widget.TextBox();
        NIP = new widget.TextBox();
        KdDokter1 = new widget.TextBox();
        Year = new widget.TextBox();
        visual_fields_left = new widget.TextBox();
        conclusion_requires_spectacles = new widget.ComboBox();
        conclusion_respiratory_problem = new widget.ComboBox();
        fit = new widget.ComboBox();
        conclusion_vertigo = new widget.ComboBox();
        conclusion_impaired_hearing = new widget.ComboBox();
        blood_group = new widget.ComboBox();
        conclusion_colour_blindness = new widget.ComboBox();
        fit_with_restrictions = new widget.ComboBox();
        exam_dental_muskulo = new widget.TextBox();
        audiometri_left_ear_1001 = new widget.TextBox();
        audiometri_left_ear_2001 = new widget.TextBox();
        audiometri_left_ear_1501 = new widget.TextBox();
        audiometri_left_ear_501 = new widget.TextBox();
        audiometri_left_ear_3001 = new widget.TextBox();
        audiometri_left_ear_4001 = new widget.TextBox();
        audiometri_left_ear_6001 = new widget.TextBox();
        audiometri_left_ear_5001 = new widget.TextBox();
        audiometri_left_ear_1002 = new widget.TextBox();
        audiometri_left_ear_2002 = new widget.TextBox();
        audiometri_left_ear_1502 = new widget.TextBox();
        audiometri_left_ear_502 = new widget.TextBox();
        audiometri_left_ear_3002 = new widget.TextBox();
        audiometri_left_ear_4002 = new widget.TextBox();
        audiometri_left_ear_6002 = new widget.TextBox();
        audiometri_left_ear_5002 = new widget.TextBox();
        audiometri_left_ear_1003 = new widget.TextBox();
        audiometri_left_ear_2003 = new widget.TextBox();
        audiometri_left_ear_1503 = new widget.TextBox();
        audiometri_left_ear_503 = new widget.TextBox();
        audiometri_left_ear_3003 = new widget.TextBox();
        audiometri_left_ear_4003 = new widget.TextBox();
        audiometri_left_ear_6003 = new widget.TextBox();
        audiometri_left_ear_5003 = new widget.TextBox();
        KdPetugas = new widget.TextBox();
        NmPetugas = new widget.TextBox();
        BtnPetugas = new widget.Button();
        ConcRadiologi = new widget.TextBox();
        scrollPane12 = new widget.ScrollPane();
        PhyExam = new widget.TextArea();
        scrollPane13 = new widget.ScrollPane();
        catatan = new widget.TextArea();
        scrollPane5 = new widget.ScrollPane();
        kesimpulan = new widget.TextArea();
        scrollPane6 = new widget.ScrollPane();
        saran = new widget.TextArea();
        scrollPane7 = new widget.ScrollPane();
        Dass21 = new widget.TextArea();
        ConcEcg = new widget.TextBox();
        ConcSpirometry = new widget.TextBox();
        ConcAudiometry = new widget.TextBox();
        TPasien = new widget.TextBox();
        spirometri_fvc_1 = new widget.TextBox();
        wbc = new widget.TextBox();
        esr = new widget.TextBox();
        bl_group = new widget.TextBox();
        sgot = new widget.TextBox();
        sgpt = new widget.TextBox();
        urea = new widget.TextBox();
        creatinin = new widget.TextBox();
        gamaa_gt = new widget.TextBox();
        glucose = new widget.TextBox();
        random_glucose = new widget.TextBox();
        total_cholestrol = new widget.TextBox();
        protein = new widget.TextBox();
        blood = new widget.TextBox();
        bilirubin = new widget.TextBox();
        malaria = new widget.TextBox();
        tpha = new widget.TextBox();
        mantoux_test = new widget.TextBox();
        leukosit = new widget.TextBox();
        lab_others = new widget.TextBox();
        ova = new widget.TextBox();
        culture = new widget.TextBox();
        cysta = new widget.TextBox();
        pnemunosicosis2 = new widget.TextBox();
        parasites1 = new widget.TextBox();
        pnemunosicosis = new widget.TextBox();
        ILO_clasification = new widget.TextBox();
        ILO_clasification2 = new widget.TextBox();
        oth_abnormal = new widget.TextBox();
        tb2 = new widget.TextBox();
        tb1 = new widget.TextBox();
        page3_comment = new widget.TextBox();
        type_of_hearing = new widget.TextBox();
        eye_unaided_distant_l = new widget.TextBox();
        eye_unaided_distant_r = new widget.TextBox();
        BtnPT = new widget.Button();
        trombosit = new widget.TextBox();
        rhesuss = new widget.TextBox();
        triglyceride = new widget.TextBox();
        hdl_cholesterol = new widget.TextBox();
        ldl_cholesterol = new widget.TextBox();
        uric_acid = new widget.TextBox();
        urine_colour = new widget.TextBox();
        urine_turbidity = new widget.TextBox();
        urine_chemical_reaction = new widget.TextBox();
        urine_ketones = new widget.TextBox();
        urine_glucose = new widget.TextBox();
        urine_nitrites = new widget.TextBox();
        urine_wbc = new widget.TextBox();
        urine_rbc = new widget.TextBox();
        urine_bacteria = new widget.TextBox();
        urine_crystal = new widget.TextBox();
        urine_epithel = new widget.TextBox();
        hbsag = new widget.TextBox();
        anti_hbs = new widget.TextBox();
        cea = new widget.TextBox();
        afp = new widget.TextBox();
        drug_amphetamine = new widget.TextBox();
        drug_methamphetamine = new widget.TextBox();
        drug_morphine = new widget.TextBox();
        drug_benzodiazepine = new widget.TextBox();
        drug_cocain = new widget.TextBox();
        drug_marijuana = new widget.TextBox();
        KdPetugasLab = new widget.TextBox();
        NmPetugasLab = new widget.TextBox();
        BtnPetugasLab = new widget.Button();
        scrollPane14 = new widget.ScrollPane();
        ecg_abnormal = new widget.TextArea();
        exam_dental_comments = new widget.TextBox();
        dass1 = new widget.TextBox();
        dass2 = new widget.TextBox();
        dass3 = new widget.TextBox();
        eye_glasses_distant_l = new widget.TextBox();
        eye_unaided_near_l = new widget.TextBox();
        eye_glasses_distant_r = new widget.TextBox();
        eye_unaided_near_r = new widget.TextBox();
        eye_glasses_near_l = new widget.TextBox();
        eye_night_vision_1 = new widget.TextBox();
        eye_unaided_near_r1 = new widget.TextBox();
        eye_glasses_near_r = new widget.TextBox();
        eye_night_vision_2 = new widget.TextBox();
        eye_brake_test_2 = new widget.TextBox();
        manager = new widget.CekBox();
        departemen = new widget.CekBox();
        supervisor = new widget.CekBox();
        TglLahir = new widget.Tanggal();
        Jk = new widget.ComboBox();
        btn_AmbilData = new widget.Button();
        internalFrame3 = new widget.InternalFrame();
        Scroll = new widget.ScrollPane();
        tbObat = new widget.Table();
        panelGlass9 = new widget.panelisi();
        jLabel19 = new widget.Label();
        DTPCari1 = new widget.Tanggal();
        jLabel21 = new widget.Label();
        DTPCari2 = new widget.Tanggal();
        jLabel6 = new widget.Label();
        TCari = new widget.TextBox();
        BtnCari = new widget.Button();
        jLabel7 = new widget.Label();
        LCount = new widget.Label();

        LoadHTML.setBorder(null);
        LoadHTML.setName("LoadHTML"); // NOI18N

        jPopupMenu1.setName("jPopupMenu1"); // NOI18N

        MnPenilaianMCU.setBackground(new java.awt.Color(255, 255, 254));
        MnPenilaianMCU.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        MnPenilaianMCU.setForeground(new java.awt.Color(50, 50, 50));
        MnPenilaianMCU.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        MnPenilaianMCU.setText("Laporan Pengkajian MCU");
        MnPenilaianMCU.setName("MnPenilaianMCU"); // NOI18N
        MnPenilaianMCU.setPreferredSize(new java.awt.Dimension(180, 26));
        MnPenilaianMCU.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MnPenilaianMCUActionPerformed(evt);
            }
        });
        jPopupMenu1.add(MnPenilaianMCU);

        MnCopyPengkajian.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        MnCopyPengkajian.setForeground(new java.awt.Color(50, 50, 50));
        MnCopyPengkajian.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        MnCopyPengkajian.setText("Copy Data");
        MnCopyPengkajian.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        MnCopyPengkajian.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        MnCopyPengkajian.setName("MnCopyPengkajian"); // NOI18N
        MnCopyPengkajian.setPreferredSize(new java.awt.Dimension(150, 26));
        MnCopyPengkajian.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MnCopyPengkajianActionPerformed(evt);
            }
        });
        jPopupMenu1.add(MnCopyPengkajian);

        TanggalRegistrasi.setHighlighter(null);
        TanggalRegistrasi.setName("TanggalRegistrasi"); // NOI18N

        WindowGabungMCU.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        WindowGabungMCU.setModal(true);
        WindowGabungMCU.setName("WindowGabungMCU"); // NOI18N
        WindowGabungMCU.setUndecorated(true);
        WindowGabungMCU.setResizable(false);

        internalFrame8.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(230, 235, 225)), "::[ Copy Data Pengkajian MCU ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 12), new java.awt.Color(50, 50, 50))); // NOI18N
        internalFrame8.setName("internalFrame8"); // NOI18N
        internalFrame8.setLayout(null);

        BtnCloseIn6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/cross.png"))); // NOI18N
        BtnCloseIn6.setMnemonic('P');
        BtnCloseIn6.setText("Tutup");
        BtnCloseIn6.setToolTipText("Alt+P");
        BtnCloseIn6.setName("BtnCloseIn6"); // NOI18N
        BtnCloseIn6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnCloseIn6ActionPerformed(evt);
            }
        });
        internalFrame8.add(BtnCloseIn6);
        BtnCloseIn6.setBounds(130, 87, 100, 30);

        BtnSimpan6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/save-16x16.png"))); // NOI18N
        BtnSimpan6.setMnemonic('S');
        BtnSimpan6.setText("Simpan");
        BtnSimpan6.setToolTipText("Alt+S");
        BtnSimpan6.setName("BtnSimpan6"); // NOI18N
        BtnSimpan6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnSimpan6ActionPerformed(evt);
            }
        });
        internalFrame8.add(BtnSimpan6);
        BtnSimpan6.setBounds(20, 87, 100, 30);

        label40.setText("No. Rawat Tujuan :");
        label40.setName("label40"); // NOI18N
        label40.setPreferredSize(new java.awt.Dimension(35, 23));
        internalFrame8.add(label40);
        label40.setBounds(0, 22, 105, 23);

        NoRmTujuan.setHighlighter(null);
        NoRmTujuan.setName("NoRmTujuan"); // NOI18N
        NoRmTujuan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                NoRmTujuanKeyPressed(evt);
            }
        });
        internalFrame8.add(NoRmTujuan);
        NoRmTujuan.setBounds(108, 22, 100, 23);

        NmPasienTujuan.setEditable(false);
        NmPasienTujuan.setHighlighter(null);
        NmPasienTujuan.setName("NmPasienTujuan"); // NOI18N
        internalFrame8.add(NmPasienTujuan);
        NmPasienTujuan.setBounds(108, 52, 300, 23);

        label41.setText("Nama Pasien :");
        label41.setName("label41"); // NOI18N
        label41.setPreferredSize(new java.awt.Dimension(35, 23));
        internalFrame8.add(label41);
        label41.setBounds(0, 52, 105, 23);

        BtnCari1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png"))); // NOI18N
        BtnCari1.setMnemonic('2');
        BtnCari1.setToolTipText("Alt+2");
        BtnCari1.setName("BtnCari1"); // NOI18N
        BtnCari1.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnCari1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnCari1ActionPerformed(evt);
            }
        });
        internalFrame8.add(BtnCari1);
        BtnCari1.setBounds(210, 22, 28, 23);

        WindowGabungMCU.getContentPane().add(internalFrame8, java.awt.BorderLayout.CENTER);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Pengkajian Medical Check Up (MCU) ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 12), new java.awt.Color(50, 50, 50))); // NOI18N
        internalFrame1.setFont(new java.awt.Font("Tahoma", 2, 12)); // NOI18N
        internalFrame1.setName("internalFrame1"); // NOI18N
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

        panelGlass8.setName("panelGlass8"); // NOI18N
        panelGlass8.setPreferredSize(new java.awt.Dimension(44, 54));
        panelGlass8.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        BtnSimpan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/save-16x16.png"))); // NOI18N
        BtnSimpan.setMnemonic('S');
        BtnSimpan.setText("Simpan");
        BtnSimpan.setToolTipText("Alt+S");
        BtnSimpan.setName("BtnSimpan"); // NOI18N
        BtnSimpan.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnSimpan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnSimpanActionPerformed(evt);
            }
        });
        BtnSimpan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnSimpanKeyPressed(evt);
            }
        });
        panelGlass8.add(BtnSimpan);

        BtnBatal.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Cancel-2-16x16.png"))); // NOI18N
        BtnBatal.setMnemonic('B');
        BtnBatal.setText("Baru");
        BtnBatal.setToolTipText("Alt+B");
        BtnBatal.setName("BtnBatal"); // NOI18N
        BtnBatal.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnBatal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnBatalActionPerformed(evt);
            }
        });
        BtnBatal.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnBatalKeyPressed(evt);
            }
        });
        panelGlass8.add(BtnBatal);

        BtnHapus.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/stop_f2.png"))); // NOI18N
        BtnHapus.setMnemonic('H');
        BtnHapus.setText("Hapus");
        BtnHapus.setToolTipText("Alt+H");
        BtnHapus.setName("BtnHapus"); // NOI18N
        BtnHapus.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnHapus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnHapusActionPerformed(evt);
            }
        });
        BtnHapus.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnHapusKeyPressed(evt);
            }
        });
        panelGlass8.add(BtnHapus);

        BtnEdit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/inventaris.png"))); // NOI18N
        BtnEdit.setMnemonic('G');
        BtnEdit.setText("Ganti");
        BtnEdit.setToolTipText("Alt+G");
        BtnEdit.setName("BtnEdit"); // NOI18N
        BtnEdit.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnEditActionPerformed(evt);
            }
        });
        BtnEdit.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnEditKeyPressed(evt);
            }
        });
        panelGlass8.add(BtnEdit);

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
        panelGlass8.add(BtnPrint);

        BtnAll.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Search-16x16.png"))); // NOI18N
        BtnAll.setMnemonic('M');
        BtnAll.setText("Semua");
        BtnAll.setToolTipText("Alt+M");
        BtnAll.setName("BtnAll"); // NOI18N
        BtnAll.setPreferredSize(new java.awt.Dimension(100, 30));
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
        panelGlass8.add(BtnAll);

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
        panelGlass8.add(BtnKeluar);

        BtnRiwayatMCU.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/preview-16x16.png"))); // NOI18N
        BtnRiwayatMCU.setMnemonic('3');
        BtnRiwayatMCU.setText("Riwayat");
        BtnRiwayatMCU.setToolTipText("Alt+3");
        BtnRiwayatMCU.setName("BtnRiwayatMCU"); // NOI18N
        BtnRiwayatMCU.setPreferredSize(new java.awt.Dimension(100, 23));
        BtnRiwayatMCU.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnRiwayatMCUActionPerformed(evt);
            }
        });
        BtnRiwayatMCU.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnRiwayatMCUKeyPressed(evt);
            }
        });
        panelGlass8.add(BtnRiwayatMCU);

        internalFrame1.add(panelGlass8, java.awt.BorderLayout.PAGE_END);

        TabRawat.setBackground(new java.awt.Color(254, 255, 254));
        TabRawat.setForeground(new java.awt.Color(50, 50, 50));
        TabRawat.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        TabRawat.setName("TabRawat"); // NOI18N

        internalFrame2.setBorder(null);
        internalFrame2.setName("internalFrame2"); // NOI18N
        internalFrame2.setLayout(new java.awt.BorderLayout(1, 1));

        scrollInput.setName("scrollInput"); // NOI18N
        scrollInput.setPreferredSize(new java.awt.Dimension(102, 557));

        FormInput.setBackground(new java.awt.Color(255, 255, 255));
        FormInput.setBorder(null);
        FormInput.setName("FormInput"); // NOI18N
        FormInput.setPreferredSize(new java.awt.Dimension(910, 3480));
        FormInput.setLayout(null);

        LblMcu1.setText("No.Rawat :");
        LblMcu1.setName("LblMcu1"); // NOI18N
        FormInput.add(LblMcu1);
        LblMcu1.setBounds(0, 10, 70, 23);

        LblMcu2.setText("Surname :");
        LblMcu2.setName("LblMcu2"); // NOI18N
        FormInput.add(LblMcu2);
        LblMcu2.setBounds(575, 10, 60, 23);

        LblMcu3.setText("Dokter P.J. :");
        LblMcu3.setName("LblMcu3"); // NOI18N
        FormInput.add(LblMcu3);
        LblMcu3.setBounds(0, 40, 70, 23);

        LblMcu4.setText("Tanggal :");
        LblMcu4.setName("LblMcu4"); // NOI18N
        FormInput.add(LblMcu4);
        LblMcu4.setBounds(395, 40, 57, 23);

        LblMcu5.setText("MCU Grup :");
        LblMcu5.setName("LblMcu5"); // NOI18N
        FormInput.add(LblMcu5);
        LblMcu5.setBounds(595, 40, 70, 23);

        LblMcu6.setText("Year :");
        LblMcu6.setName("LblMcu6"); // NOI18N
        FormInput.add(LblMcu6);
        LblMcu6.setBounds(772, 40, 45, 23);

        LblMcu7.setText("Petugas :");
        LblMcu7.setName("LblMcu7"); // NOI18N
        FormInput.add(LblMcu7);
        LblMcu7.setBounds(0, 70, 70, 23);

        SepMcu1.setBackground(new java.awt.Color(239, 244, 234));
        SepMcu1.setForeground(new java.awt.Color(239, 244, 234));
        SepMcu1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        SepMcu1.setName("SepMcu1"); // NOI18N
        FormInput.add(SepMcu1);
        SepMcu1.setBounds(0, 100, 880, 1);

        LblMcu8.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LblMcu8.setText("A. DATA KARYAWAN & GENERAL INFORMATION (Hal. 1 & 3)");
        LblMcu8.setName("LblMcu8"); // NOI18N
        FormInput.add(LblMcu8);
        LblMcu8.setBounds(10, 100, 600, 23);

        LblMcu9.setText("Tempat Lahir :");
        LblMcu9.setName("LblMcu9"); // NOI18N
        FormInput.add(LblMcu9);
        LblMcu9.setBounds(0, 125, 120, 23);

        LblMcu10.setText("Tgl.Lahir :");
        LblMcu10.setName("LblMcu10"); // NOI18N
        FormInput.add(LblMcu10);
        LblMcu10.setBounds(300, 125, 115, 23);

        LblMcu11.setText("J.K. :");
        LblMcu11.setName("LblMcu11"); // NOI18N
        FormInput.add(LblMcu11);
        LblMcu11.setBounds(590, 125, 100, 23);

        LblMcu12.setText("Suku/Bangsa :");
        LblMcu12.setName("LblMcu12"); // NOI18N
        FormInput.add(LblMcu12);
        LblMcu12.setBounds(0, 155, 120, 23);

        LblMcu13.setText("Status Nikah :");
        LblMcu13.setName("LblMcu13"); // NOI18N
        FormInput.add(LblMcu13);
        LblMcu13.setBounds(300, 155, 115, 23);

        LblMcu14.setText("No.Telp/HP :");
        LblMcu14.setName("LblMcu14"); // NOI18N
        FormInput.add(LblMcu14);
        LblMcu14.setBounds(590, 155, 100, 23);

        LblMcu15.setText("Perusahaan :");
        LblMcu15.setName("LblMcu15"); // NOI18N
        FormInput.add(LblMcu15);
        LblMcu15.setBounds(0, 185, 120, 23);

        LblMcu16.setText("Badge/ID Karyawan :");
        LblMcu16.setName("LblMcu16"); // NOI18N
        FormInput.add(LblMcu16);
        LblMcu16.setBounds(300, 185, 115, 23);

        LblMcu17.setText("Job Title :");
        LblMcu17.setName("LblMcu17"); // NOI18N
        FormInput.add(LblMcu17);
        LblMcu17.setBounds(590, 185, 100, 23);

        LblMcu18.setText("Date of Employment :");
        LblMcu18.setName("LblMcu18"); // NOI18N
        FormInput.add(LblMcu18);
        LblMcu18.setBounds(0, 215, 120, 23);

        LblMcu19.setText("Year :");
        LblMcu19.setName("LblMcu19"); // NOI18N
        FormInput.add(LblMcu19);
        LblMcu19.setBounds(300, 215, 115, 23);

        LblMcu20.setText("Exercise Habits :");
        LblMcu20.setName("LblMcu20"); // NOI18N
        FormInput.add(LblMcu20);
        LblMcu20.setBounds(590, 215, 100, 23);

        LblMcu21.setText("Hobby :");
        LblMcu21.setName("LblMcu21"); // NOI18N
        FormInput.add(LblMcu21);
        LblMcu21.setBounds(0, 245, 120, 23);

        LblMcu22.setText("Moonlight Working :");
        LblMcu22.setName("LblMcu22"); // NOI18N
        FormInput.add(LblMcu22);
        LblMcu22.setBounds(300, 245, 115, 23);

        LblMcu23.setText("Position Applied For :");
        LblMcu23.setName("LblMcu23"); // NOI18N
        FormInput.add(LblMcu23);
        LblMcu23.setBounds(570, 245, 120, 23);

        LblMcu24.setText("Job Involves :");
        LblMcu24.setName("LblMcu24"); // NOI18N
        FormInput.add(LblMcu24);
        LblMcu24.setBounds(0, 275, 120, 23);

        SepMcu2.setBackground(new java.awt.Color(239, 244, 234));
        SepMcu2.setForeground(new java.awt.Color(239, 244, 234));
        SepMcu2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        SepMcu2.setName("SepMcu2"); // NOI18N
        FormInput.add(SepMcu2);
        SepMcu2.setBounds(0, 345, 880, 1);

        LblMcu25.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LblMcu25.setText("B. MEDICAL HISTORY (Hal. 3)");
        LblMcu25.setName("LblMcu25"); // NOI18N
        FormInput.add(LblMcu25);
        LblMcu25.setBounds(10, 345, 600, 23);

        LblMcu26.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LblMcu26.setText("Please indicate if you ever suffered from the following conditions (centang = Y, kosong = N)");
        LblMcu26.setName("LblMcu26"); // NOI18N
        FormInput.add(LblMcu26);
        LblMcu26.setBounds(44, 370, 826, 23);

        SepMcu3.setBackground(new java.awt.Color(239, 244, 234));
        SepMcu3.setForeground(new java.awt.Color(239, 244, 234));
        SepMcu3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        SepMcu3.setName("SepMcu3"); // NOI18N
        FormInput.add(SepMcu3);
        SepMcu3.setBounds(0, 605, 880, 1);

        LblMcu27.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LblMcu27.setText("C. FAMILY HEALTH HISTORY, CONSUMPTION & MEDICATION (Hal. 3)");
        LblMcu27.setName("LblMcu27"); // NOI18N
        FormInput.add(LblMcu27);
        LblMcu27.setBounds(10, 605, 600, 23);

        LblMcu28.setText("Father :");
        LblMcu28.setName("LblMcu28"); // NOI18N
        FormInput.add(LblMcu28);
        LblMcu28.setBounds(0, 630, 80, 23);

        LblMcu29.setText("Cigarettes (perday) :");
        LblMcu29.setName("LblMcu29"); // NOI18N
        FormInput.add(LblMcu29);
        LblMcu29.setBounds(280, 630, 120, 23);

        LblMcu30.setText("Prescribed Medication :");
        LblMcu30.setName("LblMcu30"); // NOI18N
        FormInput.add(LblMcu30);
        LblMcu30.setBounds(490, 630, 130, 23);

        LblMcu31.setText("Mother :");
        LblMcu31.setName("LblMcu31"); // NOI18N
        FormInput.add(LblMcu31);
        LblMcu31.setBounds(0, 660, 80, 23);

        LblMcu32.setText("Alcohol (gr/week) :");
        LblMcu32.setName("LblMcu32"); // NOI18N
        FormInput.add(LblMcu32);
        LblMcu32.setBounds(280, 660, 120, 23);

        LblMcu33.setText("Siblings :");
        LblMcu33.setName("LblMcu33"); // NOI18N
        FormInput.add(LblMcu33);
        LblMcu33.setBounds(0, 690, 80, 23);

        LblMcu34.setText("Any Allergies :");
        LblMcu34.setName("LblMcu34"); // NOI18N
        FormInput.add(LblMcu34);
        LblMcu34.setBounds(490, 690, 130, 23);

        LblMcu35.setText("Other :");
        LblMcu35.setName("LblMcu35"); // NOI18N
        FormInput.add(LblMcu35);
        LblMcu35.setBounds(0, 720, 80, 23);

        LblMcu36.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LblMcu36.setText("(10 gr alcohol = 1 can beer)");
        LblMcu36.setName("LblMcu36"); // NOI18N
        FormInput.add(LblMcu36);
        LblMcu36.setBounds(280, 690, 205, 23);

        SepMcu4.setBackground(new java.awt.Color(239, 244, 234));
        SepMcu4.setForeground(new java.awt.Color(239, 244, 234));
        SepMcu4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        SepMcu4.setName("SepMcu4"); // NOI18N
        FormInput.add(SepMcu4);
        SepMcu4.setBounds(0, 750, 880, 1);

        LblMcu37.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LblMcu37.setText("D. SUPPORTIVE INVESTIGATION - LABORATORY RESULT (Hal. 4)");
        LblMcu37.setName("LblMcu37"); // NOI18N
        FormInput.add(LblMcu37);
        LblMcu37.setBounds(10, 750, 600, 23);

        LblMcu38.setText("Hb :");
        LblMcu38.setName("LblMcu38"); // NOI18N
        FormInput.add(LblMcu38);
        LblMcu38.setBounds(0, 775, 104, 23);

        LblMcu39.setText("WBC :");
        LblMcu39.setName("LblMcu39"); // NOI18N
        FormInput.add(LblMcu39);
        LblMcu39.setBounds(176, 775, 104, 23);

        LblMcu40.setText("ESR :");
        LblMcu40.setName("LblMcu40"); // NOI18N
        FormInput.add(LblMcu40);
        LblMcu40.setBounds(352, 775, 104, 23);

        LblMcu41.setText("Blood Group :");
        LblMcu41.setName("LblMcu41"); // NOI18N
        FormInput.add(LblMcu41);
        LblMcu41.setBounds(528, 775, 104, 23);

        LblMcu42.setText("Gamma GT :");
        LblMcu42.setName("LblMcu42"); // NOI18N
        FormInput.add(LblMcu42);
        LblMcu42.setBounds(704, 775, 104, 23);

        LblMcu43.setText("SGOT :");
        LblMcu43.setName("LblMcu43"); // NOI18N
        FormInput.add(LblMcu43);
        LblMcu43.setBounds(0, 805, 104, 23);

        LblMcu44.setText("SGPT :");
        LblMcu44.setName("LblMcu44"); // NOI18N
        FormInput.add(LblMcu44);
        LblMcu44.setBounds(176, 805, 104, 23);

        LblMcu45.setText("Urea :");
        LblMcu45.setName("LblMcu45"); // NOI18N
        FormInput.add(LblMcu45);
        LblMcu45.setBounds(352, 805, 104, 23);

        LblMcu46.setText("Creatinin :");
        LblMcu46.setName("LblMcu46"); // NOI18N
        FormInput.add(LblMcu46);
        LblMcu46.setBounds(528, 805, 104, 23);

        LblMcu47.setText("Glucose :");
        LblMcu47.setName("LblMcu47"); // NOI18N
        FormInput.add(LblMcu47);
        LblMcu47.setBounds(704, 805, 104, 23);

        LblMcu48.setText("Random Glucose :");
        LblMcu48.setName("LblMcu48"); // NOI18N
        FormInput.add(LblMcu48);
        LblMcu48.setBounds(0, 835, 104, 23);

        LblMcu49.setText("Total Choles. :");
        LblMcu49.setName("LblMcu49"); // NOI18N
        FormInput.add(LblMcu49);
        LblMcu49.setBounds(176, 835, 104, 23);

        LblMcu50.setText("Protein :");
        LblMcu50.setName("LblMcu50"); // NOI18N
        FormInput.add(LblMcu50);
        LblMcu50.setBounds(352, 835, 104, 23);

        LblMcu51.setText("Blood :");
        LblMcu51.setName("LblMcu51"); // NOI18N
        FormInput.add(LblMcu51);
        LblMcu51.setBounds(528, 835, 104, 23);

        LblMcu52.setText("Leucocyte :");
        LblMcu52.setName("LblMcu52"); // NOI18N
        FormInput.add(LblMcu52);
        LblMcu52.setBounds(704, 835, 104, 23);

        LblMcu53.setText("Bilirubin :");
        LblMcu53.setName("LblMcu53"); // NOI18N
        FormInput.add(LblMcu53);
        LblMcu53.setBounds(0, 865, 104, 23);

        LblMcu54.setText("Malaria :");
        LblMcu54.setName("LblMcu54"); // NOI18N
        FormInput.add(LblMcu54);
        LblMcu54.setBounds(176, 865, 104, 23);

        LblMcu55.setText("TPHA :");
        LblMcu55.setName("LblMcu55"); // NOI18N
        FormInput.add(LblMcu55);
        LblMcu55.setBounds(352, 865, 104, 23);

        LblMcu56.setText("Mantoux Test :");
        LblMcu56.setName("LblMcu56"); // NOI18N
        FormInput.add(LblMcu56);
        LblMcu56.setBounds(528, 865, 104, 23);

        LblMcu57.setText("Others :");
        LblMcu57.setName("LblMcu57"); // NOI18N
        FormInput.add(LblMcu57);
        LblMcu57.setBounds(704, 865, 104, 23);

        LblMcu58.setText("Ova :");
        LblMcu58.setName("LblMcu58"); // NOI18N
        FormInput.add(LblMcu58);
        LblMcu58.setBounds(0, 895, 104, 23);

        LblMcu59.setText("Culture :");
        LblMcu59.setName("LblMcu59"); // NOI18N
        FormInput.add(LblMcu59);
        LblMcu59.setBounds(176, 895, 104, 23);

        LblMcu60.setText("Cysta :");
        LblMcu60.setName("LblMcu60"); // NOI18N
        FormInput.add(LblMcu60);
        LblMcu60.setBounds(352, 895, 104, 23);

        LblMcu61.setText("Parasites :");
        LblMcu61.setName("LblMcu61"); // NOI18N
        FormInput.add(LblMcu61);
        LblMcu61.setBounds(528, 895, 104, 23);

        SepMcu5.setBackground(new java.awt.Color(239, 244, 234));
        SepMcu5.setForeground(new java.awt.Color(239, 244, 234));
        SepMcu5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        SepMcu5.setName("SepMcu5"); // NOI18N
        FormInput.add(SepMcu5);
        SepMcu5.setBounds(44, 925, 836, 1);

        LblMcu62.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LblMcu62.setText("Pemeriksaan Lain");
        LblMcu62.setName("LblMcu62"); // NOI18N
        FormInput.add(LblMcu62);
        LblMcu62.setBounds(44, 925, 300, 23);

        LblMcu63.setText("Trombosit :");
        LblMcu63.setName("LblMcu63"); // NOI18N
        FormInput.add(LblMcu63);
        LblMcu63.setBounds(0, 950, 104, 23);

        LblMcu64.setText("Rhesus :");
        LblMcu64.setName("LblMcu64"); // NOI18N
        FormInput.add(LblMcu64);
        LblMcu64.setBounds(176, 950, 104, 23);

        LblMcu65.setText("Triglyceride :");
        LblMcu65.setName("LblMcu65"); // NOI18N
        FormInput.add(LblMcu65);
        LblMcu65.setBounds(352, 950, 104, 23);

        LblMcu66.setText("HDL Cholest. :");
        LblMcu66.setName("LblMcu66"); // NOI18N
        FormInput.add(LblMcu66);
        LblMcu66.setBounds(528, 950, 104, 23);

        LblMcu67.setText("LDL Cholest. :");
        LblMcu67.setName("LblMcu67"); // NOI18N
        FormInput.add(LblMcu67);
        LblMcu67.setBounds(704, 950, 104, 23);

        LblMcu68.setText("Asam Urat :");
        LblMcu68.setName("LblMcu68"); // NOI18N
        FormInput.add(LblMcu68);
        LblMcu68.setBounds(0, 980, 104, 23);

        SepMcu6.setBackground(new java.awt.Color(239, 244, 234));
        SepMcu6.setForeground(new java.awt.Color(239, 244, 234));
        SepMcu6.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        SepMcu6.setName("SepMcu6"); // NOI18N
        FormInput.add(SepMcu6);
        SepMcu6.setBounds(44, 1010, 836, 1);

        LblMcu69.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LblMcu69.setText("Urinalisis");
        LblMcu69.setName("LblMcu69"); // NOI18N
        FormInput.add(LblMcu69);
        LblMcu69.setBounds(44, 1010, 300, 23);

        LblMcu70.setText("Warna :");
        LblMcu70.setName("LblMcu70"); // NOI18N
        FormInput.add(LblMcu70);
        LblMcu70.setBounds(0, 1035, 104, 23);

        LblMcu71.setText("Kejernihan :");
        LblMcu71.setName("LblMcu71"); // NOI18N
        FormInput.add(LblMcu71);
        LblMcu71.setBounds(176, 1035, 104, 23);

        LblMcu72.setText("pH/Kimia :");
        LblMcu72.setName("LblMcu72"); // NOI18N
        FormInput.add(LblMcu72);
        LblMcu72.setBounds(352, 1035, 104, 23);

        LblMcu73.setText("Keton :");
        LblMcu73.setName("LblMcu73"); // NOI18N
        FormInput.add(LblMcu73);
        LblMcu73.setBounds(528, 1035, 104, 23);

        LblMcu74.setText("Glukosa :");
        LblMcu74.setName("LblMcu74"); // NOI18N
        FormInput.add(LblMcu74);
        LblMcu74.setBounds(704, 1035, 104, 23);

        LblMcu75.setText("Nitrit :");
        LblMcu75.setName("LblMcu75"); // NOI18N
        FormInput.add(LblMcu75);
        LblMcu75.setBounds(0, 1065, 104, 23);

        LblMcu76.setText("Leukosit :");
        LblMcu76.setName("LblMcu76"); // NOI18N
        FormInput.add(LblMcu76);
        LblMcu76.setBounds(176, 1065, 104, 23);

        LblMcu77.setText("Eritrosit :");
        LblMcu77.setName("LblMcu77"); // NOI18N
        FormInput.add(LblMcu77);
        LblMcu77.setBounds(352, 1065, 104, 23);

        LblMcu78.setText("Bakteri :");
        LblMcu78.setName("LblMcu78"); // NOI18N
        FormInput.add(LblMcu78);
        LblMcu78.setBounds(528, 1065, 104, 23);

        LblMcu79.setText("Kristal :");
        LblMcu79.setName("LblMcu79"); // NOI18N
        FormInput.add(LblMcu79);
        LblMcu79.setBounds(704, 1065, 104, 23);

        LblMcu80.setText("Epitel :");
        LblMcu80.setName("LblMcu80"); // NOI18N
        FormInput.add(LblMcu80);
        LblMcu80.setBounds(0, 1095, 104, 23);

        SepMcu7.setBackground(new java.awt.Color(239, 244, 234));
        SepMcu7.setForeground(new java.awt.Color(239, 244, 234));
        SepMcu7.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        SepMcu7.setName("SepMcu7"); // NOI18N
        FormInput.add(SepMcu7);
        SepMcu7.setBounds(44, 1125, 836, 1);

        LblMcu81.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LblMcu81.setText("Imunologi / Serologi");
        LblMcu81.setName("LblMcu81"); // NOI18N
        FormInput.add(LblMcu81);
        LblMcu81.setBounds(44, 1125, 300, 23);

        LblMcu82.setText("HBsAg :");
        LblMcu82.setName("LblMcu82"); // NOI18N
        FormInput.add(LblMcu82);
        LblMcu82.setBounds(0, 1150, 104, 23);

        LblMcu83.setText("Anti HBs :");
        LblMcu83.setName("LblMcu83"); // NOI18N
        FormInput.add(LblMcu83);
        LblMcu83.setBounds(176, 1150, 104, 23);

        LblMcu84.setText("CEA :");
        LblMcu84.setName("LblMcu84"); // NOI18N
        FormInput.add(LblMcu84);
        LblMcu84.setBounds(352, 1150, 104, 23);

        LblMcu85.setText("AFP :");
        LblMcu85.setName("LblMcu85"); // NOI18N
        FormInput.add(LblMcu85);
        LblMcu85.setBounds(528, 1150, 104, 23);

        SepMcu8.setBackground(new java.awt.Color(239, 244, 234));
        SepMcu8.setForeground(new java.awt.Color(239, 244, 234));
        SepMcu8.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        SepMcu8.setName("SepMcu8"); // NOI18N
        FormInput.add(SepMcu8);
        SepMcu8.setBounds(44, 1180, 836, 1);

        LblMcu86.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LblMcu86.setText("Uji NAPZA");
        LblMcu86.setName("LblMcu86"); // NOI18N
        FormInput.add(LblMcu86);
        LblMcu86.setBounds(44, 1180, 300, 23);

        LblMcu87.setText("Amfetamin :");
        LblMcu87.setName("LblMcu87"); // NOI18N
        FormInput.add(LblMcu87);
        LblMcu87.setBounds(0, 1205, 104, 23);

        LblMcu88.setText("Metamfetamin :");
        LblMcu88.setName("LblMcu88"); // NOI18N
        FormInput.add(LblMcu88);
        LblMcu88.setBounds(176, 1205, 104, 23);

        LblMcu89.setText("Morfin :");
        LblMcu89.setName("LblMcu89"); // NOI18N
        FormInput.add(LblMcu89);
        LblMcu89.setBounds(352, 1205, 104, 23);

        LblMcu90.setText("Benzodiazepin :");
        LblMcu90.setName("LblMcu90"); // NOI18N
        FormInput.add(LblMcu90);
        LblMcu90.setBounds(528, 1205, 104, 23);

        LblMcu91.setText("Kokain :");
        LblMcu91.setName("LblMcu91"); // NOI18N
        FormInput.add(LblMcu91);
        LblMcu91.setBounds(704, 1205, 104, 23);

        LblMcu92.setText("Marijuana :");
        LblMcu92.setName("LblMcu92"); // NOI18N
        FormInput.add(LblMcu92);
        LblMcu92.setBounds(0, 1235, 104, 23);

        SepMcu9.setBackground(new java.awt.Color(239, 244, 234));
        SepMcu9.setForeground(new java.awt.Color(239, 244, 234));
        SepMcu9.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        SepMcu9.setName("SepMcu9"); // NOI18N
        FormInput.add(SepMcu9);
        SepMcu9.setBounds(44, 1265, 836, 1);

        LblMcu93.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LblMcu93.setText("Kesimpulan Laboratorium");
        LblMcu93.setName("LblMcu93"); // NOI18N
        FormInput.add(LblMcu93);
        LblMcu93.setBounds(44, 1265, 300, 23);

        LblMcu94.setText("Petugas Laboratorium :");
        LblMcu94.setName("LblMcu94"); // NOI18N
        FormInput.add(LblMcu94);
        LblMcu94.setBounds(0, 1350, 150, 23);

        SepMcu10.setBackground(new java.awt.Color(239, 244, 234));
        SepMcu10.setForeground(new java.awt.Color(239, 244, 234));
        SepMcu10.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        SepMcu10.setName("SepMcu10"); // NOI18N
        FormInput.add(SepMcu10);
        SepMcu10.setBounds(0, 1380, 880, 1);

        LblMcu95.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LblMcu95.setText("E. CHEST X-RAY (Hal. 4)");
        LblMcu95.setName("LblMcu95"); // NOI18N
        FormInput.add(LblMcu95);
        LblMcu95.setBounds(10, 1380, 600, 23);

        LblMcu96.setText("Pneumoconiosis :");
        LblMcu96.setName("LblMcu96"); // NOI18N
        FormInput.add(LblMcu96);
        LblMcu96.setBounds(0, 1405, 150, 23);

        LblMcu97.setText("If yes-ILO Classification :");
        LblMcu97.setName("LblMcu97"); // NOI18N
        FormInput.add(LblMcu97);
        LblMcu97.setBounds(0, 1435, 150, 23);

        LblMcu98.setText("Evidence of TB :");
        LblMcu98.setName("LblMcu98"); // NOI18N
        FormInput.add(LblMcu98);
        LblMcu98.setBounds(0, 1465, 150, 23);

        LblMcu99.setText("Other Abnormalities :");
        LblMcu99.setName("LblMcu99"); // NOI18N
        FormInput.add(LblMcu99);
        LblMcu99.setBounds(0, 1495, 150, 23);

        LblMcu100.setText("Comment :");
        LblMcu100.setName("LblMcu100"); // NOI18N
        FormInput.add(LblMcu100);
        LblMcu100.setBounds(0, 1525, 150, 23);

        LblMcu101.setText("Hasil Rontgen :");
        LblMcu101.setName("LblMcu101"); // NOI18N
        FormInput.add(LblMcu101);
        LblMcu101.setBounds(0, 1555, 120, 23);

        SepMcu11.setBackground(new java.awt.Color(239, 244, 234));
        SepMcu11.setForeground(new java.awt.Color(239, 244, 234));
        SepMcu11.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        SepMcu11.setName("SepMcu11"); // NOI18N
        FormInput.add(SepMcu11);
        SepMcu11.setBounds(0, 1620, 880, 1);

        LblMcu102.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LblMcu102.setText("F. ECG (Hal. 4)");
        LblMcu102.setName("LblMcu102"); // NOI18N
        FormInput.add(LblMcu102);
        LblMcu102.setBounds(10, 1620, 600, 23);

        LblMcu103.setText("ECG :");
        LblMcu103.setName("LblMcu103"); // NOI18N
        FormInput.add(LblMcu103);
        LblMcu103.setBounds(0, 1645, 150, 23);

        LblMcu104.setText("Abnormal (specify) :");
        LblMcu104.setName("LblMcu104"); // NOI18N
        FormInput.add(LblMcu104);
        LblMcu104.setBounds(330, 1645, 120, 23);

        SepMcu12.setBackground(new java.awt.Color(239, 244, 234));
        SepMcu12.setForeground(new java.awt.Color(239, 244, 234));
        SepMcu12.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        SepMcu12.setName("SepMcu12"); // NOI18N
        FormInput.add(SepMcu12);
        SepMcu12.setBounds(0, 1710, 880, 1);

        LblMcu105.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LblMcu105.setText("G. SPIROMETRY (Hal. 4)");
        LblMcu105.setName("LblMcu105"); // NOI18N
        FormInput.add(LblMcu105);
        LblMcu105.setBounds(10, 1710, 600, 23);

        LblMcu106.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        LblMcu106.setText("(1)");
        LblMcu106.setName("LblMcu106"); // NOI18N
        FormInput.add(LblMcu106);
        LblMcu106.setBounds(154, 1735, 120, 23);

        LblMcu107.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        LblMcu107.setText("(2)");
        LblMcu107.setName("LblMcu107"); // NOI18N
        FormInput.add(LblMcu107);
        LblMcu107.setBounds(284, 1735, 120, 23);

        LblMcu108.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        LblMcu108.setText("(3)");
        LblMcu108.setName("LblMcu108"); // NOI18N
        FormInput.add(LblMcu108);
        LblMcu108.setBounds(414, 1735, 120, 23);

        LblMcu109.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        LblMcu109.setText("(4) %");
        LblMcu109.setName("LblMcu109"); // NOI18N
        FormInput.add(LblMcu109);
        LblMcu109.setBounds(544, 1735, 120, 23);

        LblMcu110.setText("VC :");
        LblMcu110.setName("LblMcu110"); // NOI18N
        FormInput.add(LblMcu110);
        LblMcu110.setBounds(0, 1760, 150, 23);

        LblMcu111.setText("FVC :");
        LblMcu111.setName("LblMcu111"); // NOI18N
        FormInput.add(LblMcu111);
        LblMcu111.setBounds(0, 1790, 150, 23);

        LblMcu112.setText("FEV 1 :");
        LblMcu112.setName("LblMcu112"); // NOI18N
        FormInput.add(LblMcu112);
        LblMcu112.setBounds(0, 1820, 150, 23);

        LblMcu113.setText("FEV 1/FVC :");
        LblMcu113.setName("LblMcu113"); // NOI18N
        FormInput.add(LblMcu113);
        LblMcu113.setBounds(0, 1850, 150, 23);

        SepMcu13.setBackground(new java.awt.Color(239, 244, 234));
        SepMcu13.setForeground(new java.awt.Color(239, 244, 234));
        SepMcu13.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        SepMcu13.setName("SepMcu13"); // NOI18N
        FormInput.add(SepMcu13);
        SepMcu13.setBounds(0, 1880, 880, 1);

        LblMcu114.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LblMcu114.setText("H. AUDIOMETRY (Hal. 4)");
        LblMcu114.setName("LblMcu114"); // NOI18N
        FormInput.add(LblMcu114);
        LblMcu114.setBounds(10, 1880, 600, 23);

        LblMcu115.setText("Tinitus :");
        LblMcu115.setName("LblMcu115"); // NOI18N
        FormInput.add(LblMcu115);
        LblMcu115.setBounds(0, 1905, 150, 23);

        LblMcu116.setText("Ear Protection Worn :");
        LblMcu116.setName("LblMcu116"); // NOI18N
        FormInput.add(LblMcu116);
        LblMcu116.setBounds(0, 1935, 150, 23);

        LblMcu117.setText("Type of Protection :");
        LblMcu117.setName("LblMcu117"); // NOI18N
        FormInput.add(LblMcu117);
        LblMcu117.setBounds(0, 1965, 150, 23);

        LblMcu118.setText("Frekuensi (Hz) :");
        LblMcu118.setName("LblMcu118"); // NOI18N
        FormInput.add(LblMcu118);
        LblMcu118.setBounds(0, 1995, 150, 23);

        LblMcu119.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        LblMcu119.setText("500");
        LblMcu119.setName("LblMcu119"); // NOI18N
        FormInput.add(LblMcu119);
        LblMcu119.setBounds(154, 1995, 80, 23);

        LblMcu120.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        LblMcu120.setText("1000");
        LblMcu120.setName("LblMcu120"); // NOI18N
        FormInput.add(LblMcu120);
        LblMcu120.setBounds(244, 1995, 80, 23);

        LblMcu121.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        LblMcu121.setText("1500");
        LblMcu121.setName("LblMcu121"); // NOI18N
        FormInput.add(LblMcu121);
        LblMcu121.setBounds(334, 1995, 80, 23);

        LblMcu122.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        LblMcu122.setText("2000");
        LblMcu122.setName("LblMcu122"); // NOI18N
        FormInput.add(LblMcu122);
        LblMcu122.setBounds(424, 1995, 80, 23);

        LblMcu123.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        LblMcu123.setText("3000");
        LblMcu123.setName("LblMcu123"); // NOI18N
        FormInput.add(LblMcu123);
        LblMcu123.setBounds(514, 1995, 80, 23);

        LblMcu124.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        LblMcu124.setText("4000");
        LblMcu124.setName("LblMcu124"); // NOI18N
        FormInput.add(LblMcu124);
        LblMcu124.setBounds(604, 1995, 80, 23);

        LblMcu125.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        LblMcu125.setText("5000");
        LblMcu125.setName("LblMcu125"); // NOI18N
        FormInput.add(LblMcu125);
        LblMcu125.setBounds(694, 1995, 80, 23);

        LblMcu126.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        LblMcu126.setText("6000");
        LblMcu126.setName("LblMcu126"); // NOI18N
        FormInput.add(LblMcu126);
        LblMcu126.setBounds(784, 1995, 80, 23);

        LblMcu127.setText("Left Ear AC :");
        LblMcu127.setName("LblMcu127"); // NOI18N
        FormInput.add(LblMcu127);
        LblMcu127.setBounds(0, 2020, 150, 23);

        LblMcu128.setText("Left Ear BC :");
        LblMcu128.setName("LblMcu128"); // NOI18N
        FormInput.add(LblMcu128);
        LblMcu128.setBounds(0, 2050, 150, 23);

        LblMcu129.setText("Right Ear AC :");
        LblMcu129.setName("LblMcu129"); // NOI18N
        FormInput.add(LblMcu129);
        LblMcu129.setBounds(0, 2080, 150, 23);

        LblMcu130.setText("Right Ear BC :");
        LblMcu130.setName("LblMcu130"); // NOI18N
        FormInput.add(LblMcu130);
        LblMcu130.setBounds(0, 2110, 150, 23);

        SepMcu14.setBackground(new java.awt.Color(239, 244, 234));
        SepMcu14.setForeground(new java.awt.Color(239, 244, 234));
        SepMcu14.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        SepMcu14.setName("SepMcu14"); // NOI18N
        FormInput.add(SepMcu14);
        SepMcu14.setBounds(0, 2140, 880, 1);

        LblMcu131.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LblMcu131.setText("I. PHYSICAL EXAMINATION (Hal. 5)");
        LblMcu131.setName("LblMcu131"); // NOI18N
        FormInput.add(LblMcu131);
        LblMcu131.setBounds(10, 2140, 600, 23);

        LblMcu132.setText("Blood Pressure :");
        LblMcu132.setName("LblMcu132"); // NOI18N
        FormInput.add(LblMcu132);
        LblMcu132.setBounds(0, 2165, 120, 23);

        LblMcu133.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LblMcu133.setText("mmHg");
        LblMcu133.setName("LblMcu133"); // NOI18N
        FormInput.add(LblMcu133);
        LblMcu133.setBounds(192, 2165, 40, 23);

        LblMcu134.setText("Pulse :");
        LblMcu134.setName("LblMcu134"); // NOI18N
        FormInput.add(LblMcu134);
        LblMcu134.setBounds(235, 2165, 60, 23);

        LblMcu135.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LblMcu135.setText("x/menit");
        LblMcu135.setName("LblMcu135"); // NOI18N
        FormInput.add(LblMcu135);
        LblMcu135.setBounds(352, 2165, 45, 23);

        LblMcu136.setText("RR :");
        LblMcu136.setName("LblMcu136"); // NOI18N
        FormInput.add(LblMcu136);
        LblMcu136.setBounds(400, 2165, 40, 23);

        LblMcu137.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LblMcu137.setText("x/menit");
        LblMcu137.setName("LblMcu137"); // NOI18N
        FormInput.add(LblMcu137);
        LblMcu137.setBounds(497, 2165, 45, 23);

        LblMcu138.setText("Height :");
        LblMcu138.setName("LblMcu138"); // NOI18N
        FormInput.add(LblMcu138);
        LblMcu138.setBounds(545, 2165, 60, 23);

        LblMcu139.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LblMcu139.setText("cm");
        LblMcu139.setName("LblMcu139"); // NOI18N
        FormInput.add(LblMcu139);
        LblMcu139.setBounds(662, 2165, 25, 23);

        LblMcu140.setText("Weight :");
        LblMcu140.setName("LblMcu140"); // NOI18N
        FormInput.add(LblMcu140);
        LblMcu140.setBounds(690, 2165, 60, 23);

        LblMcu141.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LblMcu141.setText("Kg");
        LblMcu141.setName("LblMcu141"); // NOI18N
        FormInput.add(LblMcu141);
        LblMcu141.setBounds(807, 2165, 25, 23);

        LblMcu142.setText("BMI (BB/TB2) :");
        LblMcu142.setName("LblMcu142"); // NOI18N
        FormInput.add(LblMcu142);
        LblMcu142.setBounds(0, 2195, 120, 23);

        LblMcu143.setText("Klasifikasi BMI :");
        LblMcu143.setName("LblMcu143"); // NOI18N
        FormInput.add(LblMcu143);
        LblMcu143.setBounds(195, 2195, 100, 23);

        LblMcu144.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LblMcu144.setText("BMI Ideal : 18.5 - 24.99");
        LblMcu144.setName("LblMcu144"); // NOI18N
        FormInput.add(LblMcu144);
        LblMcu144.setBounds(480, 2195, 200, 23);

        SepMcu15.setBackground(new java.awt.Color(239, 244, 234));
        SepMcu15.setForeground(new java.awt.Color(239, 244, 234));
        SepMcu15.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        SepMcu15.setName("SepMcu15"); // NOI18N
        FormInput.add(SepMcu15);
        SepMcu15.setBounds(0, 2225, 880, 1);

        LblMcu145.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LblMcu145.setText("J. EYE EXAMINATION (Hal. 5)");
        LblMcu145.setName("LblMcu145"); // NOI18N
        FormInput.add(LblMcu145);
        LblMcu145.setBounds(10, 2225, 600, 23);

        LblMcu146.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        LblMcu146.setText("Unaided L");
        LblMcu146.setName("LblMcu146"); // NOI18N
        FormInput.add(LblMcu146);
        LblMcu146.setBounds(154, 2250, 95, 23);

        LblMcu147.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        LblMcu147.setText("Unaided R");
        LblMcu147.setName("LblMcu147"); // NOI18N
        FormInput.add(LblMcu147);
        LblMcu147.setBounds(254, 2250, 95, 23);

        LblMcu148.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        LblMcu148.setText("With Glasses L");
        LblMcu148.setName("LblMcu148"); // NOI18N
        FormInput.add(LblMcu148);
        LblMcu148.setBounds(354, 2250, 95, 23);

        LblMcu149.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        LblMcu149.setText("With Glasses R");
        LblMcu149.setName("LblMcu149"); // NOI18N
        FormInput.add(LblMcu149);
        LblMcu149.setBounds(454, 2250, 95, 23);

        LblMcu150.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        LblMcu150.setText("1");
        LblMcu150.setName("LblMcu150"); // NOI18N
        FormInput.add(LblMcu150);
        LblMcu150.setBounds(664, 2250, 100, 23);

        LblMcu151.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        LblMcu151.setText("2");
        LblMcu151.setName("LblMcu151"); // NOI18N
        FormInput.add(LblMcu151);
        LblMcu151.setBounds(766, 2250, 100, 23);

        LblMcu152.setText("Distant :");
        LblMcu152.setName("LblMcu152"); // NOI18N
        FormInput.add(LblMcu152);
        LblMcu152.setBounds(0, 2275, 150, 23);

        LblMcu153.setText("Near :");
        LblMcu153.setName("LblMcu153"); // NOI18N
        FormInput.add(LblMcu153);
        LblMcu153.setBounds(0, 2305, 150, 23);

        LblMcu154.setText("Night Vision :");
        LblMcu154.setName("LblMcu154"); // NOI18N
        FormInput.add(LblMcu154);
        LblMcu154.setBounds(560, 2275, 100, 23);

        LblMcu155.setText("Brake Test :");
        LblMcu155.setName("LblMcu155"); // NOI18N
        FormInput.add(LblMcu155);
        LblMcu155.setBounds(560, 2305, 100, 23);

        LblMcu156.setText("Color Blindness :");
        LblMcu156.setName("LblMcu156"); // NOI18N
        FormInput.add(LblMcu156);
        LblMcu156.setBounds(0, 2335, 150, 23);

        LblMcu157.setText("Visual Fields L/R :");
        LblMcu157.setName("LblMcu157"); // NOI18N
        FormInput.add(LblMcu157);
        LblMcu157.setBounds(300, 2335, 120, 23);

        LblMcu158.setText("Fundi :");
        LblMcu158.setName("LblMcu158"); // NOI18N
        FormInput.add(LblMcu158);
        LblMcu158.setBounds(570, 2335, 90, 23);

        SepMcu16.setBackground(new java.awt.Color(239, 244, 234));
        SepMcu16.setForeground(new java.awt.Color(239, 244, 234));
        SepMcu16.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        SepMcu16.setName("SepMcu16"); // NOI18N
        FormInput.add(SepMcu16);
        SepMcu16.setBounds(0, 2365, 880, 1);

        LblMcu159.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LblMcu159.setText("K. VACCINATION HISTORY (Hal. 5)");
        LblMcu159.setName("LblMcu159"); // NOI18N
        FormInput.add(LblMcu159);
        LblMcu159.setBounds(10, 2365, 600, 23);

        LblMcu160.setText("BCG :");
        LblMcu160.setName("LblMcu160"); // NOI18N
        FormInput.add(LblMcu160);
        LblMcu160.setBounds(0, 2390, 104, 23);

        LblMcu161.setText("DPT :");
        LblMcu161.setName("LblMcu161"); // NOI18N
        FormInput.add(LblMcu161);
        LblMcu161.setBounds(176, 2390, 104, 23);

        LblMcu162.setText("Polio :");
        LblMcu162.setName("LblMcu162"); // NOI18N
        FormInput.add(LblMcu162);
        LblMcu162.setBounds(352, 2390, 104, 23);

        LblMcu163.setText("Morbili :");
        LblMcu163.setName("LblMcu163"); // NOI18N
        FormInput.add(LblMcu163);
        LblMcu163.setBounds(528, 2390, 104, 23);

        LblMcu164.setText("Typhoid :");
        LblMcu164.setName("LblMcu164"); // NOI18N
        FormInput.add(LblMcu164);
        LblMcu164.setBounds(704, 2390, 104, 23);

        LblMcu165.setText("Hep-A :");
        LblMcu165.setName("LblMcu165"); // NOI18N
        FormInput.add(LblMcu165);
        LblMcu165.setBounds(0, 2420, 104, 23);

        LblMcu166.setText("Hep-B :");
        LblMcu166.setName("LblMcu166"); // NOI18N
        FormInput.add(LblMcu166);
        LblMcu166.setBounds(176, 2420, 104, 23);

        LblMcu167.setText("Tetanus :");
        LblMcu167.setName("LblMcu167"); // NOI18N
        FormInput.add(LblMcu167);
        LblMcu167.setBounds(352, 2420, 104, 23);

        LblMcu168.setText("Others :");
        LblMcu168.setName("LblMcu168"); // NOI18N
        FormInput.add(LblMcu168);
        LblMcu168.setBounds(528, 2420, 104, 23);

        SepMcu17.setBackground(new java.awt.Color(239, 244, 234));
        SepMcu17.setForeground(new java.awt.Color(239, 244, 234));
        SepMcu17.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        SepMcu17.setName("SepMcu17"); // NOI18N
        FormInput.add(SepMcu17);
        SepMcu17.setBounds(0, 2450, 880, 1);

        LblMcu169.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LblMcu169.setText("L. VERTEBRA (Hal. 5)");
        LblMcu169.setName("LblMcu169"); // NOI18N
        FormInput.add(LblMcu169);
        LblMcu169.setBounds(10, 2450, 600, 23);

        LblMcu170.setText("Scoliosis :");
        LblMcu170.setName("LblMcu170"); // NOI18N
        FormInput.add(LblMcu170);
        LblMcu170.setBounds(0, 2475, 150, 23);

        LblMcu171.setText("Lateral Flexion 0-20 :");
        LblMcu171.setName("LblMcu171"); // NOI18N
        FormInput.add(LblMcu171);
        LblMcu171.setBounds(300, 2475, 150, 23);

        LblMcu172.setText("Kyphosis :");
        LblMcu172.setName("LblMcu172"); // NOI18N
        FormInput.add(LblMcu172);
        LblMcu172.setBounds(0, 2505, 150, 23);

        LblMcu173.setText("Heel Walking :");
        LblMcu173.setName("LblMcu173"); // NOI18N
        FormInput.add(LblMcu173);
        LblMcu173.setBounds(300, 2505, 150, 23);

        LblMcu174.setText("Lordosis :");
        LblMcu174.setName("LblMcu174"); // NOI18N
        FormInput.add(LblMcu174);
        LblMcu174.setBounds(0, 2535, 150, 23);

        LblMcu175.setText("Toe Walking :");
        LblMcu175.setName("LblMcu175"); // NOI18N
        FormInput.add(LblMcu175);
        LblMcu175.setBounds(300, 2535, 150, 23);

        LblMcu176.setText("Forward Flexion 0-80 :");
        LblMcu176.setName("LblMcu176"); // NOI18N
        FormInput.add(LblMcu176);
        LblMcu176.setBounds(0, 2565, 150, 23);

        LblMcu177.setText("Squats 3x :");
        LblMcu177.setName("LblMcu177"); // NOI18N
        FormInput.add(LblMcu177);
        LblMcu177.setBounds(300, 2565, 150, 23);

        LblMcu178.setText("Hyperextensi 0-25 :");
        LblMcu178.setName("LblMcu178"); // NOI18N
        FormInput.add(LblMcu178);
        LblMcu178.setBounds(0, 2595, 150, 23);

        SepMcu18.setBackground(new java.awt.Color(239, 244, 234));
        SepMcu18.setForeground(new java.awt.Color(239, 244, 234));
        SepMcu18.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        SepMcu18.setName("SepMcu18"); // NOI18N
        FormInput.add(SepMcu18);
        SepMcu18.setBounds(0, 2625, 880, 1);

        LblMcu179.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LblMcu179.setText("M. HASIL PEMERIKSAAN / EXAMINATION (Hal. 5)");
        LblMcu179.setName("LblMcu179"); // NOI18N
        FormInput.add(LblMcu179);
        LblMcu179.setBounds(10, 2625, 600, 23);

        LblMcu180.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LblMcu180.setText("Isi - bila Normal (N), isi keterangan bila Abnormal (A)");
        LblMcu180.setName("LblMcu180"); // NOI18N
        FormInput.add(LblMcu180);
        LblMcu180.setBounds(44, 2650, 826, 23);

        LblMcu181.setText("1. ENT :");
        LblMcu181.setName("LblMcu181"); // NOI18N
        FormInput.add(LblMcu181);
        LblMcu181.setBounds(0, 2675, 200, 23);

        LblMcu182.setText("6. Central & Peripheral Nervous :");
        LblMcu182.setName("LblMcu182"); // NOI18N
        FormInput.add(LblMcu182);
        LblMcu182.setBounds(440, 2675, 200, 23);

        LblMcu183.setText("2. Cardio Vascular System :");
        LblMcu183.setName("LblMcu183"); // NOI18N
        FormInput.add(LblMcu183);
        LblMcu183.setBounds(0, 2705, 200, 23);

        LblMcu184.setText("7. Skin :");
        LblMcu184.setName("LblMcu184"); // NOI18N
        FormInput.add(LblMcu184);
        LblMcu184.setBounds(440, 2705, 200, 23);

        LblMcu185.setText("3. Respiratory System :");
        LblMcu185.setName("LblMcu185"); // NOI18N
        FormInput.add(LblMcu185);
        LblMcu185.setBounds(0, 2735, 200, 23);

        LblMcu186.setText("8. Lymph Nodes :");
        LblMcu186.setName("LblMcu186"); // NOI18N
        FormInput.add(LblMcu186);
        LblMcu186.setBounds(440, 2735, 200, 23);

        LblMcu187.setText("4. Abdomen :");
        LblMcu187.setName("LblMcu187"); // NOI18N
        FormInput.add(LblMcu187);
        LblMcu187.setBounds(0, 2765, 200, 23);

        LblMcu188.setText("9. Dental :");
        LblMcu188.setName("LblMcu188"); // NOI18N
        FormInput.add(LblMcu188);
        LblMcu188.setBounds(440, 2765, 200, 23);

        LblMcu189.setText("5. Genito Urinary System :");
        LblMcu189.setName("LblMcu189"); // NOI18N
        FormInput.add(LblMcu189);
        LblMcu189.setBounds(0, 2795, 200, 23);

        LblMcu190.setText("10. Muskuloskeletal Sys :");
        LblMcu190.setName("LblMcu190"); // NOI18N
        FormInput.add(LblMcu190);
        LblMcu190.setBounds(440, 2795, 200, 23);

        LblMcu191.setText("Note :");
        LblMcu191.setName("LblMcu191"); // NOI18N
        FormInput.add(LblMcu191);
        LblMcu191.setBounds(0, 2825, 200, 23);

        SepMcu19.setBackground(new java.awt.Color(239, 244, 234));
        SepMcu19.setForeground(new java.awt.Color(239, 244, 234));
        SepMcu19.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        SepMcu19.setName("SepMcu19"); // NOI18N
        FormInput.add(SepMcu19);
        SepMcu19.setBounds(0, 2890, 880, 1);

        LblMcu192.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LblMcu192.setText("N. SCORE DASS 21");
        LblMcu192.setName("LblMcu192"); // NOI18N
        FormInput.add(LblMcu192);
        LblMcu192.setBounds(10, 2890, 600, 23);

        LblMcu193.setText("Depresi :");
        LblMcu193.setName("LblMcu193"); // NOI18N
        FormInput.add(LblMcu193);
        LblMcu193.setBounds(0, 2915, 120, 23);

        LblMcu194.setText("Kecemasan :");
        LblMcu194.setName("LblMcu194"); // NOI18N
        FormInput.add(LblMcu194);
        LblMcu194.setBounds(180, 2915, 80, 23);

        LblMcu195.setText("Stress :");
        LblMcu195.setName("LblMcu195"); // NOI18N
        FormInput.add(LblMcu195);
        LblMcu195.setBounds(320, 2915, 60, 23);

        LblMcu196.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LblMcu196.setText("(jumlah jawaban, skor = jawaban x 2)");
        LblMcu196.setName("LblMcu196"); // NOI18N
        FormInput.add(LblMcu196);
        LblMcu196.setBounds(440, 2915, 430, 23);

        SepMcu20.setBackground(new java.awt.Color(239, 244, 234));
        SepMcu20.setForeground(new java.awt.Color(239, 244, 234));
        SepMcu20.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        SepMcu20.setName("SepMcu20"); // NOI18N
        FormInput.add(SepMcu20);
        SepMcu20.setBounds(0, 2945, 880, 1);

        LblMcu197.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LblMcu197.setText("O. ASSESSMENT FINDINGS AND ACTION PLAN (Hal. 2)");
        LblMcu197.setName("LblMcu197"); // NOI18N
        FormInput.add(LblMcu197);
        LblMcu197.setBounds(10, 2945, 600, 23);

        LblMcu198.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LblMcu198.setText("Medical History and General Information");
        LblMcu198.setName("LblMcu198"); // NOI18N
        FormInput.add(LblMcu198);
        LblMcu198.setBounds(44, 2970, 410, 23);

        LblMcu199.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LblMcu199.setText("Physical Examination");
        LblMcu199.setName("LblMcu199"); // NOI18N
        FormInput.add(LblMcu199);
        LblMcu199.setBounds(464, 2970, 406, 23);

        LblMcu200.setText("Chest X Ray :");
        LblMcu200.setName("LblMcu200"); // NOI18N
        FormInput.add(LblMcu200);
        LblMcu200.setBounds(0, 3060, 120, 23);

        LblMcu201.setText("ECG :");
        LblMcu201.setName("LblMcu201"); // NOI18N
        FormInput.add(LblMcu201);
        LblMcu201.setBounds(440, 3060, 80, 23);

        LblMcu202.setText("Spirometry :");
        LblMcu202.setName("LblMcu202"); // NOI18N
        FormInput.add(LblMcu202);
        LblMcu202.setBounds(0, 3090, 120, 23);

        LblMcu203.setText("Audiometry :");
        LblMcu203.setName("LblMcu203"); // NOI18N
        FormInput.add(LblMcu203);
        LblMcu203.setBounds(440, 3090, 80, 23);

        LblMcu204.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LblMcu204.setText("Conclusion (Kesimpulan)");
        LblMcu204.setName("LblMcu204"); // NOI18N
        FormInput.add(LblMcu204);
        LblMcu204.setBounds(44, 3120, 410, 23);

        LblMcu205.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LblMcu205.setText("Recommendation (Saran)");
        LblMcu205.setName("LblMcu205"); // NOI18N
        FormInput.add(LblMcu205);
        LblMcu205.setBounds(464, 3120, 406, 23);

        SepMcu21.setBackground(new java.awt.Color(239, 244, 234));
        SepMcu21.setForeground(new java.awt.Color(239, 244, 234));
        SepMcu21.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        SepMcu21.setName("SepMcu21"); // NOI18N
        FormInput.add(SepMcu21);
        SepMcu21.setBounds(0, 3215, 880, 1);

        LblMcu206.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LblMcu206.setText("P. MEDICAL CHECKUP CERTIFICATE (Hal. 6)");
        LblMcu206.setName("LblMcu206"); // NOI18N
        FormInput.add(LblMcu206);
        LblMcu206.setBounds(10, 3215, 600, 23);

        LblMcu207.setText("Departemen :");
        LblMcu207.setName("LblMcu207"); // NOI18N
        FormInput.add(LblMcu207);
        LblMcu207.setBounds(0, 3240, 120, 23);

        LblMcu208.setText("Supervisor :");
        LblMcu208.setName("LblMcu208"); // NOI18N
        FormInput.add(LblMcu208);
        LblMcu208.setBounds(300, 3240, 115, 23);

        LblMcu209.setText("Manager :");
        LblMcu209.setName("LblMcu209"); // NOI18N
        FormInput.add(LblMcu209);
        LblMcu209.setBounds(570, 3240, 120, 23);

        SepMcu22.setBackground(new java.awt.Color(239, 244, 234));
        SepMcu22.setForeground(new java.awt.Color(239, 244, 234));
        SepMcu22.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        SepMcu22.setName("SepMcu22"); // NOI18N
        FormInput.add(SepMcu22);
        SepMcu22.setBounds(44, 3270, 836, 1);

        LblMcu210.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LblMcu210.setText("Summary");
        LblMcu210.setName("LblMcu210"); // NOI18N
        FormInput.add(LblMcu210);
        LblMcu210.setBounds(44, 3270, 300, 23);

        LblMcu211.setText("Requires Spectacles :");
        LblMcu211.setName("LblMcu211"); // NOI18N
        FormInput.add(LblMcu211);
        LblMcu211.setBounds(0, 3295, 120, 23);

        LblMcu212.setText("Colour Blindness :");
        LblMcu212.setName("LblMcu212"); // NOI18N
        FormInput.add(LblMcu212);
        LblMcu212.setBounds(270, 3295, 145, 23);

        LblMcu213.setText("Respiratory Problem :");
        LblMcu213.setName("LblMcu213"); // NOI18N
        FormInput.add(LblMcu213);
        LblMcu213.setBounds(550, 3295, 140, 23);

        LblMcu214.setText("Impaired Hearing :");
        LblMcu214.setName("LblMcu214"); // NOI18N
        FormInput.add(LblMcu214);
        LblMcu214.setBounds(0, 3325, 120, 23);

        LblMcu215.setText("Vertigo :");
        LblMcu215.setName("LblMcu215"); // NOI18N
        FormInput.add(LblMcu215);
        LblMcu215.setBounds(270, 3325, 145, 23);

        LblMcu216.setText("Blood Group :");
        LblMcu216.setName("LblMcu216"); // NOI18N
        FormInput.add(LblMcu216);
        LblMcu216.setBounds(550, 3325, 140, 23);

        SepMcu23.setBackground(new java.awt.Color(239, 244, 234));
        SepMcu23.setForeground(new java.awt.Color(239, 244, 234));
        SepMcu23.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        SepMcu23.setName("SepMcu23"); // NOI18N
        FormInput.add(SepMcu23);
        SepMcu23.setBounds(44, 3355, 836, 1);

        LblMcu217.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LblMcu217.setText("Fitness");
        LblMcu217.setName("LblMcu217"); // NOI18N
        FormInput.add(LblMcu217);
        LblMcu217.setBounds(44, 3355, 300, 23);

        LblMcu218.setText("FIT :");
        LblMcu218.setName("LblMcu218"); // NOI18N
        FormInput.add(LblMcu218);
        LblMcu218.setBounds(0, 3380, 120, 23);

        LblMcu219.setText("FIT w/ Restrictions :");
        LblMcu219.setName("LblMcu219"); // NOI18N
        FormInput.add(LblMcu219);
        LblMcu219.setBounds(0, 3410, 120, 23);

        LblMcu220.setText("Specify / Comment :");
        LblMcu220.setName("LblMcu220"); // NOI18N
        FormInput.add(LblMcu220);
        LblMcu220.setBounds(380, 3410, 140, 23);


        TNoRw.setHighlighter(null);
        TNoRw.setName("TNoRw"); // NOI18N
        TNoRw.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TNoRwKeyPressed(evt);
            }
        });
        FormInput.add(TNoRw);
        TNoRw.setBounds(74, 10, 131, 23);

        surname.setHighlighter(null);
        surname.setName("surname"); // NOI18N
        FormInput.add(surname);
        surname.setBounds(639, 10, 231, 23);

        TNoRM.setEditable(false);
        TNoRM.setHighlighter(null);
        TNoRM.setName("TNoRM"); // NOI18N
        FormInput.add(TNoRM);
        TNoRM.setBounds(207, 10, 100, 23);


        NmDokter.setEditable(false);
        NmDokter.setName("NmDokter"); // NOI18N
        NmDokter.setPreferredSize(new java.awt.Dimension(207, 23));
        FormInput.add(NmDokter);
        NmDokter.setBounds(176, 40, 180, 23);

        BtnDokter.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        BtnDokter.setMnemonic('2');
        BtnDokter.setToolTipText("Alt+2");
        BtnDokter.setName("BtnDokter"); // NOI18N
        BtnDokter.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnDokter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnDokterActionPerformed(evt);
            }
        });
        FormInput.add(BtnDokter);
        BtnDokter.setBounds(358, 40, 28, 23);




        TglAsuhan.setForeground(new java.awt.Color(50, 70, 50));
        TglAsuhan.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "31-07-2026" }));
        TglAsuhan.setDisplayFormat("dd-MM-yyyy HH:mm:ss");
        TglAsuhan.setName("TglAsuhan"); // NOI18N
        TglAsuhan.setOpaque(false);
        FormInput.add(TglAsuhan);
        TglAsuhan.setBounds(456, 40, 130, 23);



        scrollPane10.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        scrollPane10.setName("scrollPane10"); // NOI18N
        scrollPane10.setPreferredSize(new java.awt.Dimension(100, 74));

        PemeriksaanLaboratorium.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        PemeriksaanLaboratorium.setColumns(20);
        PemeriksaanLaboratorium.setRows(5);
        PemeriksaanLaboratorium.setName("PemeriksaanLaboratorium"); // NOI18N
        PemeriksaanLaboratorium.setPreferredSize(new java.awt.Dimension(100, 52));
        scrollPane10.setViewportView(PemeriksaanLaboratorium);

        FormInput.add(scrollPane10);
        scrollPane10.setBounds(44, 1290, 826, 53);



        scrollPane11.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        scrollPane11.setName("scrollPane11"); // NOI18N

        RongsenThorax.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        RongsenThorax.setColumns(20);
        RongsenThorax.setRows(5);
        RongsenThorax.setName("RongsenThorax"); // NOI18N
        RongsenThorax.setPreferredSize(new java.awt.Dimension(182, 52));
        scrollPane11.setViewportView(RongsenThorax);

        FormInput.add(scrollPane11);
        scrollPane11.setBounds(154, 1555, 716, 53);


        scrollPane15.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        scrollPane15.setName("scrollPane15"); // NOI18N

        specify.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        specify.setColumns(20);
        specify.setRows(5);
        specify.setName("specify"); // NOI18N
        specify.setPreferredSize(new java.awt.Dimension(102, 52));
        specify.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                specifyKeyPressed(evt);
            }
        });
        scrollPane15.setViewportView(specify);

        FormInput.add(scrollPane15);
        scrollPane15.setBounds(524, 3410, 346, 53);






        BtnDokter3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        BtnDokter3.setMnemonic('2');
        BtnDokter3.setToolTipText("Alt+2");
        BtnDokter3.setName("BtnDokter3"); // NOI18N
        BtnDokter3.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnDokter3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnDokter3ActionPerformed(evt);
            }
        });
        FormInput.add(BtnDokter3);
        BtnDokter3.setBounds(10, 1290, 28, 23);

        BtnDokter2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        BtnDokter2.setMnemonic('2');
        BtnDokter2.setToolTipText("Alt+2");
        BtnDokter2.setName("BtnDokter2"); // NOI18N
        BtnDokter2.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnDokter2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnDokter2ActionPerformed(evt);
            }
        });
        FormInput.add(BtnDokter2);
        BtnDokter2.setBounds(124, 1555, 28, 23);

        FamilyHistoryFather.setFocusTraversalPolicyProvider(true);
        FamilyHistoryFather.setName("FamilyHistoryFather"); // NOI18N
        FormInput.add(FamilyHistoryFather);
        FamilyHistoryFather.setBounds(84, 630, 190, 23);


        SttsNikah.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "BELUM MENIKAH", "MENIKAH", "JANDA", "DUDHA", "JOMBLO" }));
        SttsNikah.setName("SttsNikah"); // NOI18N
        FormInput.add(SttsNikah);
        SttsNikah.setBounds(419, 155, 120, 23);


        TmpLahir.setHighlighter(null);
        TmpLahir.setName("TmpLahir"); // NOI18N
        FormInput.add(TmpLahir);
        TmpLahir.setBounds(124, 125, 170, 23);



        NoTlp.setHighlighter(null);
        NoTlp.setName("NoTlp"); // NOI18N
        NoTlp.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                NoTlpKeyPressed(evt);
            }
        });
        FormInput.add(NoTlp);
        NoTlp.setBounds(694, 155, 176, 23);



        Perusahaan.setHighlighter(null);
        Perusahaan.setName("Perusahaan"); // NOI18N
        FormInput.add(Perusahaan);
        Perusahaan.setBounds(124, 185, 140, 23);


        Doe.setHighlighter(null);
        Doe.setName("Doe"); // NOI18N
        Doe.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                DoeKeyPressed(evt);
            }
        });
        FormInput.add(Doe);
        Doe.setBounds(124, 215, 170, 23);


        Yoe.setHighlighter(null);
        Yoe.setName("Yoe"); // NOI18N
        Yoe.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                YoeKeyPressed(evt);
            }
        });
        FormInput.add(Yoe);
        Yoe.setBounds(419, 215, 80, 23);


        JobTitle.setHighlighter(null);
        JobTitle.setName("JobTitle"); // NOI18N
        JobTitle.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                JobTitleKeyPressed(evt);
            }
        });
        FormInput.add(JobTitle);
        JobTitle.setBounds(694, 185, 176, 23);

        Activities.setHighlighter(null);
        Activities.setName("Activities"); // NOI18N
        Activities.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                ActivitiesKeyPressed(evt);
            }
        });
        FormInput.add(Activities);
        Activities.setBounds(694, 215, 176, 23);



        Hobby.setHighlighter(null);
        Hobby.setName("Hobby"); // NOI18N
        Hobby.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                HobbyKeyPressed(evt);
            }
        });
        FormInput.add(Hobby);
        Hobby.setBounds(124, 245, 170, 23);

        OtherJob.setHighlighter(null);
        OtherJob.setName("OtherJob"); // NOI18N
        OtherJob.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                OtherJobKeyPressed(evt);
            }
        });
        FormInput.add(OtherJob);
        OtherJob.setBounds(419, 245, 150, 23);



        JobInvolvesExposingToOtherPotentialDangerous.setText(" Exposing to other potential dangerous");
        JobInvolvesExposingToOtherPotentialDangerous.setName("JobInvolvesExposingToOtherPotentialDangerous"); // NOI18N
        FormInput.add(JobInvolvesExposingToOtherPotentialDangerous);
        JobInvolvesExposingToOtherPotentialDangerous.setBounds(644, 295, 236, 20);

        JobInvolvesDrivingOrOperatingMobileEquipment.setText(" Driving or operating mobile equipment ");
        JobInvolvesDrivingOrOperatingMobileEquipment.setName("JobInvolvesDrivingOrOperatingMobileEquipment"); // NOI18N
        FormInput.add(JobInvolvesDrivingOrOperatingMobileEquipment);
        JobInvolvesDrivingOrOperatingMobileEquipment.setBounds(124, 275, 265, 20);

        JobInvolvesWorkingAtHeights.setText(" Working at heights");
        JobInvolvesWorkingAtHeights.setName("JobInvolvesWorkingAtHeights"); // NOI18N
        FormInput.add(JobInvolvesWorkingAtHeights);
        JobInvolvesWorkingAtHeights.setBounds(394, 275, 245, 20);

        JobInvolvesClericalOfficeBasedOrAdministrative.setText(" Clerical, office based or administrative");
        JobInvolvesClericalOfficeBasedOrAdministrative.setName("JobInvolvesClericalOfficeBasedOrAdministrative"); // NOI18N
        FormInput.add(JobInvolvesClericalOfficeBasedOrAdministrative);
        JobInvolvesClericalOfficeBasedOrAdministrative.setBounds(644, 275, 236, 20);

        JobInvolvesRequiresColourVision.setText(" Requires colour vision (colour coded)");
        JobInvolvesRequiresColourVision.setName("JobInvolvesRequiresColourVision"); // NOI18N
        FormInput.add(JobInvolvesRequiresColourVision);
        JobInvolvesRequiresColourVision.setBounds(124, 315, 265, 20);

        JobInvolvesPotentialDustExposure.setText(" Potential dust exposure");
        JobInvolvesPotentialDustExposure.setName("JobInvolvesPotentialDustExposure"); // NOI18N
        FormInput.add(JobInvolvesPotentialDustExposure);
        JobInvolvesPotentialDustExposure.setBounds(124, 295, 265, 20);

        JobInvolvesCateringStaffIncludingFoodHandlers.setText(" Catering staff including all food handlers");
        JobInvolvesCateringStaffIncludingFoodHandlers.setName("JobInvolvesCateringStaffIncludingFoodHandlers"); // NOI18N
        FormInput.add(JobInvolvesCateringStaffIncludingFoodHandlers);
        JobInvolvesCateringStaffIncludingFoodHandlers.setBounds(394, 295, 245, 20);

        McuGroup.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Grup A", "Grup B", "Grup C" }));
        McuGroup.setName("McuGroup"); // NOI18N
        FormInput.add(McuGroup);
        McuGroup.setBounds(669, 40, 100, 23);


        RWP1.setText(" Head injury or contussion ");
        RWP1.setName("RWP1"); // NOI18N
        FormInput.add(RWP1);
        RWP1.setBounds(44, 395, 275, 20);

        RWP2.setText(" Fainting, blackouts, epilepsy ");
        RWP2.setName("RWP2"); // NOI18N
        FormInput.add(RWP2);
        RWP2.setBounds(44, 415, 275, 20);

        RWP3.setText(" Visual changes ");
        RWP3.setName("RWP3"); // NOI18N
        FormInput.add(RWP3);
        RWP3.setBounds(44, 435, 275, 20);

        RWP4.setText(" Hearing loss ");
        RWP4.setName("RWP4"); // NOI18N
        FormInput.add(RWP4);
        RWP4.setBounds(44, 455, 275, 20);

        RWP5.setText(" Nose, sinus, throut traouble more 4 weeks ");
        RWP5.setName("RWP5"); // NOI18N
        FormInput.add(RWP5);
        RWP5.setBounds(44, 475, 275, 20);

        RWP8.setText(" Chronic diarrhea ");
        RWP8.setName("RWP8"); // NOI18N
        FormInput.add(RWP8);
        RWP8.setBounds(44, 535, 275, 20);

        RWP7.setText(" Chronic skin problem ");
        RWP7.setName("RWP7"); // NOI18N
        FormInput.add(RWP7);
        RWP7.setBounds(44, 515, 275, 20);

        RWP6.setText(" Gynaecological problems ");
        RWP6.setName("RWP6"); // NOI18N
        FormInput.add(RWP6);
        RWP6.setBounds(44, 495, 275, 20);

        RWP10.setText(" Gastritis ");
        RWP10.setName("RWP10"); // NOI18N
        FormInput.add(RWP10);
        RWP10.setBounds(44, 575, 275, 20);

        RWP9.setText(" Anorexia more 4 weeks ");
        RWP9.setName("RWP9"); // NOI18N
        FormInput.add(RWP9);
        RWP9.setBounds(44, 555, 275, 20);

        RWP11.setText(" Jaundice / hepatitis ");
        RWP11.setName("RWP11"); // NOI18N
        FormInput.add(RWP11);
        RWP11.setBounds(324, 395, 275, 20);

        RWP12.setText(" Chronic cough >4weeks ");
        RWP12.setName("RWP12"); // NOI18N
        FormInput.add(RWP12);
        RWP12.setBounds(324, 415, 275, 20);

        RWP13.setText(" Haemorhoid ");
        RWP13.setName("RWP13"); // NOI18N
        FormInput.add(RWP13);
        RWP13.setBounds(324, 435, 275, 20);

        RWP14.setText(" Chronic abdomenal pain ");
        RWP14.setName("RWP14"); // NOI18N
        FormInput.add(RWP14);
        RWP14.setBounds(324, 455, 275, 20);

        RWP15.setText(" Diabetes ");
        RWP15.setName("RWP15"); // NOI18N
        FormInput.add(RWP15);
        RWP15.setBounds(324, 495, 275, 20);

        RWP18.setText(" Tuberculosis/Bronchitis ");
        RWP18.setName("RWP18"); // NOI18N
        FormInput.add(RWP18);
        RWP18.setBounds(324, 555, 275, 20);

        RWP17.setText(" Allergies ");
        RWP17.setName("RWP17"); // NOI18N
        FormInput.add(RWP17);
        RWP17.setBounds(324, 535, 275, 20);

        RWP16.setText(" Asthma ");
        RWP16.setName("RWP16"); // NOI18N
        FormInput.add(RWP16);
        RWP16.setBounds(324, 515, 275, 20);

        RWP20.setText(" Sexual transmitted diseases ");
        RWP20.setName("RWP20"); // NOI18N
        FormInput.add(RWP20);
        RWP20.setBounds(324, 475, 275, 20);

        RWP19.setText(" Psychiatric disorder ");
        RWP19.setName("RWP19"); // NOI18N
        FormInput.add(RWP19);
        RWP19.setBounds(324, 575, 275, 20);

        RWP24.setText(" Malaria / tropical disease ");
        RWP24.setName("RWP24"); // NOI18N
        FormInput.add(RWP24);
        RWP24.setBounds(604, 435, 266, 20);

        RWP26.setText(" Back pain> 4 weeks ");
        RWP26.setName("RWP26"); // NOI18N
        FormInput.add(RWP26);
        RWP26.setBounds(604, 495, 266, 20);

        RWP28.setText(" Swollen or painful joints ");
        RWP28.setName("RWP28"); // NOI18N
        RWP28.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RWP28ActionPerformed(evt);
            }
        });
        FormInput.add(RWP28);
        RWP28.setBounds(604, 535, 266, 20);

        RWP25.setText(" Surgery / operation ");
        RWP25.setName("RWP25"); // NOI18N
        RWP25.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RWP25ActionPerformed(evt);
            }
        });
        FormInput.add(RWP25);
        RWP25.setBounds(604, 455, 266, 20);

        RWP22.setText(" Hypertension ");
        RWP22.setName("RWP22"); // NOI18N
        FormInput.add(RWP22);
        RWP22.setBounds(604, 395, 266, 20);

        RWP27.setText(" Thypoid fever ");
        RWP27.setName("RWP27"); // NOI18N
        FormInput.add(RWP27);
        RWP27.setBounds(604, 515, 266, 20);

        RWP23.setText(" Chest pain/ heart disease ");
        RWP23.setName("RWP23"); // NOI18N
        FormInput.add(RWP23);
        RWP23.setBounds(604, 415, 266, 20);

        RWP29.setText(" Kidney problem/ Urinary stones ");
        RWP29.setName("RWP29"); // NOI18N
        RWP29.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RWP29ActionPerformed(evt);
            }
        });
        FormInput.add(RWP29);
        RWP29.setBounds(604, 555, 266, 20);

        RWP21.setText(" Unusual change of weight >5 kg/month ");
        RWP21.setName("RWP21"); // NOI18N
        FormInput.add(RWP21);
        RWP21.setBounds(604, 475, 266, 20);

        RWP30.setText(" Other chronical diseases ");
        RWP30.setName("RWP30"); // NOI18N
        FormInput.add(RWP30);
        RWP30.setBounds(604, 575, 266, 20);






        FamilyHistoryMother.setFocusTraversalPolicyProvider(true);
        FamilyHistoryMother.setName("FamilyHistoryMother"); // NOI18N
        FormInput.add(FamilyHistoryMother);
        FamilyHistoryMother.setBounds(84, 660, 190, 23);


        FamilyHistorySiblings.setFocusTraversalPolicyProvider(true);
        FamilyHistorySiblings.setName("FamilyHistorySiblings"); // NOI18N
        FormInput.add(FamilyHistorySiblings);
        FamilyHistorySiblings.setBounds(84, 690, 190, 23);


        FamilyHistoryOther.setFocusTraversalPolicyProvider(true);
        FamilyHistoryOther.setName("FamilyHistoryOther"); // NOI18N
        FormInput.add(FamilyHistoryOther);
        FamilyHistoryOther.setBounds(84, 720, 190, 23);



        CigarettesPerday.setFocusTraversalPolicyProvider(true);
        CigarettesPerday.setName("CigarettesPerday"); // NOI18N
        FormInput.add(CigarettesPerday);
        CigarettesPerday.setBounds(404, 630, 80, 23);


        AlcoholGrWeek.setFocusTraversalPolicyProvider(true);
        AlcoholGrWeek.setName("AlcoholGrWeek"); // NOI18N
        FormInput.add(AlcoholGrWeek);
        AlcoholGrWeek.setBounds(404, 660, 80, 23);


        PrescribedMedication.setFocusTraversalPolicyProvider(true);
        PrescribedMedication.setName("PrescribedMedication"); // NOI18N
        FormInput.add(PrescribedMedication);
        PrescribedMedication.setBounds(624, 630, 246, 23);


        PrescribedMedication2.setFocusTraversalPolicyProvider(true);
        PrescribedMedication2.setName("PrescribedMedication2"); // NOI18N
        FormInput.add(PrescribedMedication2);
        PrescribedMedication2.setBounds(624, 660, 246, 23);


        spirometri_vc_1.setFocusTraversalPolicyProvider(true);
        spirometri_vc_1.setName("spirometri_vc_1"); // NOI18N
        FormInput.add(spirometri_vc_1);
        spirometri_vc_1.setBounds(154, 1760, 120, 23);


        PosisiKerja.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Pre employment", "Preplacement", "Periodic" }));
        PosisiKerja.setName("PosisiKerja"); // NOI18N
        FormInput.add(PosisiKerja);
        PosisiKerja.setBounds(694, 245, 176, 23);

        AnyAllergies.setFocusTraversalPolicyProvider(true);
        AnyAllergies.setName("AnyAllergies"); // NOI18N
        FormInput.add(AnyAllergies);
        AnyAllergies.setBounds(624, 690, 246, 23);


        spirometri_vc_2.setFocusTraversalPolicyProvider(true);
        spirometri_vc_2.setName("spirometri_vc_2"); // NOI18N
        FormInput.add(spirometri_vc_2);
        spirometri_vc_2.setBounds(284, 1760, 120, 23);


        spirometri_vc_3.setFocusTraversalPolicyProvider(true);
        spirometri_vc_3.setName("spirometri_vc_3"); // NOI18N
        FormInput.add(spirometri_vc_3);
        spirometri_vc_3.setBounds(414, 1760, 120, 23);



        spirometri_vc_4.setFocusTraversalPolicyProvider(true);
        spirometri_vc_4.setName("spirometri_vc_4"); // NOI18N
        FormInput.add(spirometri_vc_4);
        spirometri_vc_4.setBounds(544, 1760, 120, 23);

        hb.setFocusTraversalPolicyProvider(true);
        hb.setName("hb"); // NOI18N
        FormInput.add(hb);
        hb.setBounds(106, 775, 68, 23);


        spirometri_fvc_2.setFocusTraversalPolicyProvider(true);
        spirometri_fvc_2.setName("spirometri_fvc_2"); // NOI18N
        FormInput.add(spirometri_fvc_2);
        spirometri_fvc_2.setBounds(284, 1790, 120, 23);


        spirometri_fvc_3.setFocusTraversalPolicyProvider(true);
        spirometri_fvc_3.setName("spirometri_fvc_3"); // NOI18N
        FormInput.add(spirometri_fvc_3);
        spirometri_fvc_3.setBounds(414, 1790, 120, 23);



        spirometri_fvc_4.setFocusTraversalPolicyProvider(true);
        spirometri_fvc_4.setName("spirometri_fvc_4"); // NOI18N
        FormInput.add(spirometri_fvc_4);
        spirometri_fvc_4.setBounds(544, 1790, 120, 23);

        spirometri_fev_1_3.setFocusTraversalPolicyProvider(true);
        spirometri_fev_1_3.setName("spirometri_fev_1_3"); // NOI18N
        FormInput.add(spirometri_fev_1_3);
        spirometri_fev_1_3.setBounds(414, 1820, 120, 23);

        spirometri_fev_1_2.setFocusTraversalPolicyProvider(true);
        spirometri_fev_1_2.setName("spirometri_fev_1_2"); // NOI18N
        FormInput.add(spirometri_fev_1_2);
        spirometri_fev_1_2.setBounds(284, 1820, 120, 23);



        spirometri_fev_1_4.setFocusTraversalPolicyProvider(true);
        spirometri_fev_1_4.setName("spirometri_fev_1_4"); // NOI18N
        FormInput.add(spirometri_fev_1_4);
        spirometri_fev_1_4.setBounds(544, 1820, 120, 23);



        spirometri_fev_1_1.setFocusTraversalPolicyProvider(true);
        spirometri_fev_1_1.setName("spirometri_fev_1_1"); // NOI18N
        FormInput.add(spirometri_fev_1_1);
        spirometri_fev_1_1.setBounds(154, 1820, 120, 23);

        spirometri_fev_1_fvc_2.setFocusTraversalPolicyProvider(true);
        spirometri_fev_1_fvc_2.setName("spirometri_fev_1_fvc_2"); // NOI18N
        FormInput.add(spirometri_fev_1_fvc_2);
        spirometri_fev_1_fvc_2.setBounds(284, 1850, 120, 23);

        spirometri_fev_1_fvc_4.setFocusTraversalPolicyProvider(true);
        spirometri_fev_1_fvc_4.setName("spirometri_fev_1_fvc_4"); // NOI18N
        FormInput.add(spirometri_fev_1_fvc_4);
        spirometri_fev_1_fvc_4.setBounds(544, 1850, 120, 23);



        spirometri_fev_1_fvc_3.setFocusTraversalPolicyProvider(true);
        spirometri_fev_1_fvc_3.setName("spirometri_fev_1_fvc_3"); // NOI18N
        FormInput.add(spirometri_fev_1_fvc_3);
        spirometri_fev_1_fvc_3.setBounds(414, 1850, 120, 23);



        spirometri_fev_1_fvc_1.setFocusTraversalPolicyProvider(true);
        spirometri_fev_1_fvc_1.setName("spirometri_fev_1_fvc_1"); // NOI18N
        FormInput.add(spirometri_fev_1_fvc_1);
        spirometri_fev_1_fvc_1.setBounds(154, 1850, 120, 23);




        audiometri_tinitus_always.setText("Always");
        audiometri_tinitus_always.setName("audiometri_tinitus_always"); // NOI18N
        FormInput.add(audiometri_tinitus_always);
        audiometri_tinitus_always.setBounds(534, 1905, 90, 23);

        audiometri_tinitus_never.setText("Never");
        audiometri_tinitus_never.setName("audiometri_tinitus_never"); // NOI18N
        FormInput.add(audiometri_tinitus_never);
        audiometri_tinitus_never.setBounds(154, 1905, 90, 23);

        audiometri_tinitus_previously.setText("Previously");
        audiometri_tinitus_previously.setName("audiometri_tinitus_previously"); // NOI18N
        FormInput.add(audiometri_tinitus_previously);
        audiometri_tinitus_previously.setBounds(249, 1905, 90, 23);

        audiometri_tinitus_rarely.setText("Rarely");
        audiometri_tinitus_rarely.setName("audiometri_tinitus_rarely"); // NOI18N
        FormInput.add(audiometri_tinitus_rarely);
        audiometri_tinitus_rarely.setBounds(344, 1905, 90, 23);

        audiometri_tinitus_often.setText("Often");
        audiometri_tinitus_often.setName("audiometri_tinitus_often"); // NOI18N
        FormInput.add(audiometri_tinitus_often);
        audiometri_tinitus_often.setBounds(439, 1905, 90, 23);


        audiometri_ear_protection_worn_always.setText("Always");
        audiometri_ear_protection_worn_always.setName("audiometri_ear_protection_worn_always"); // NOI18N
        FormInput.add(audiometri_ear_protection_worn_always);
        audiometri_ear_protection_worn_always.setBounds(534, 1935, 90, 23);

        audiometri_ear_protection_worn_never.setText("Never");
        audiometri_ear_protection_worn_never.setName("audiometri_ear_protection_worn_never"); // NOI18N
        FormInput.add(audiometri_ear_protection_worn_never);
        audiometri_ear_protection_worn_never.setBounds(154, 1935, 90, 23);

        audiometri_ear_protection_worn_previously.setText("Previously");
        audiometri_ear_protection_worn_previously.setName("audiometri_ear_protection_worn_previously"); // NOI18N
        FormInput.add(audiometri_ear_protection_worn_previously);
        audiometri_ear_protection_worn_previously.setBounds(249, 1935, 90, 23);

        audiometri_ear_protection_worn_rarely.setText("Rarely");
        audiometri_ear_protection_worn_rarely.setName("audiometri_ear_protection_worn_rarely"); // NOI18N
        FormInput.add(audiometri_ear_protection_worn_rarely);
        audiometri_ear_protection_worn_rarely.setBounds(344, 1935, 90, 23);

        audiometri_ear_protection_worn_often.setText("Often");
        audiometri_ear_protection_worn_often.setName("audiometri_ear_protection_worn_often"); // NOI18N
        FormInput.add(audiometri_ear_protection_worn_often);
        audiometri_ear_protection_worn_often.setBounds(439, 1935, 90, 23);


        BB.setFocusTraversalPolicyProvider(true);
        BB.setName("BB"); // NOI18N
        FormInput.add(BB);
        BB.setBounds(754, 2165, 50, 23);


        Nadi.setFocusTraversalPolicyProvider(true);
        Nadi.setName("Nadi"); // NOI18N
        Nadi.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                NadiKeyPressed(evt);
            }
        });
        FormInput.add(Nadi);
        Nadi.setBounds(299, 2165, 50, 23);



        TD.setFocusTraversalPolicyProvider(true);
        TD.setName("TD"); // NOI18N
        FormInput.add(TD);
        TD.setBounds(124, 2165, 65, 23);



        RR.setFocusTraversalPolicyProvider(true);
        RR.setName("RR"); // NOI18N
        RR.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                RRKeyPressed(evt);
            }
        });
        FormInput.add(RR);
        RR.setBounds(444, 2165, 50, 23);



        TB.setFocusTraversalPolicyProvider(true);
        TB.setName("TB"); // NOI18N
        TB.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TBKeyPressed(evt);
            }
        });
        FormInput.add(TB);
        TB.setBounds(609, 2165, 50, 23);







        IMT.setEditable(false);
        IMT.setFocusTraversalPolicyProvider(true);
        IMT.setName("IMT"); // NOI18N
        FormInput.add(IMT);
        IMT.setBounds(124, 2195, 65, 23);


        visual_fields_right.setFocusTraversalPolicyProvider(true);
        visual_fields_right.setName("visual_fields_right"); // NOI18N
        FormInput.add(visual_fields_right);
        visual_fields_right.setBounds(496, 2335, 70, 23);




        audiometri_left_ear_1000.setFocusTraversalPolicyProvider(true);
        audiometri_left_ear_1000.setName("audiometri_left_ear_1000"); // NOI18N
        FormInput.add(audiometri_left_ear_1000);
        audiometri_left_ear_1000.setBounds(244, 2020, 80, 23);

        audiometri_left_ear_2000.setFocusTraversalPolicyProvider(true);
        audiometri_left_ear_2000.setName("audiometri_left_ear_2000"); // NOI18N
        FormInput.add(audiometri_left_ear_2000);
        audiometri_left_ear_2000.setBounds(424, 2020, 80, 23);



        audiometri_left_ear_1500.setFocusTraversalPolicyProvider(true);
        audiometri_left_ear_1500.setName("audiometri_left_ear_1500"); // NOI18N
        FormInput.add(audiometri_left_ear_1500);
        audiometri_left_ear_1500.setBounds(334, 2020, 80, 23);



        audiometri_left_ear_500.setFocusTraversalPolicyProvider(true);
        audiometri_left_ear_500.setName("audiometri_left_ear_500"); // NOI18N
        FormInput.add(audiometri_left_ear_500);
        audiometri_left_ear_500.setBounds(154, 2020, 80, 23);

        audiometri_left_ear_3000.setFocusTraversalPolicyProvider(true);
        audiometri_left_ear_3000.setName("audiometri_left_ear_3000"); // NOI18N
        FormInput.add(audiometri_left_ear_3000);
        audiometri_left_ear_3000.setBounds(514, 2020, 80, 23);


        audiometri_left_ear_4000.setFocusTraversalPolicyProvider(true);
        audiometri_left_ear_4000.setName("audiometri_left_ear_4000"); // NOI18N
        FormInput.add(audiometri_left_ear_4000);
        audiometri_left_ear_4000.setBounds(604, 2020, 80, 23);


        audiometri_left_ear_6000.setFocusTraversalPolicyProvider(true);
        audiometri_left_ear_6000.setName("audiometri_left_ear_6000"); // NOI18N
        FormInput.add(audiometri_left_ear_6000);
        audiometri_left_ear_6000.setBounds(784, 2020, 80, 23);



        audiometri_left_ear_5000.setFocusTraversalPolicyProvider(true);
        audiometri_left_ear_5000.setName("audiometri_left_ear_5000"); // NOI18N
        FormInput.add(audiometri_left_ear_5000);
        audiometri_left_ear_5000.setBounds(694, 2020, 80, 23);










        cbConcEcg.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Normal Condition", "Abnormal Condition" }));
        cbConcEcg.setName("cbConcEcg"); // NOI18N
        FormInput.add(cbConcEcg);
        cbConcEcg.setBounds(154, 1645, 170, 23);

        eye_color_blindless.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Color Blind", "Normal", "Partial Color Blind" }));
        eye_color_blindless.setName("eye_color_blindless"); // NOI18N
        FormInput.add(eye_color_blindless);
        eye_color_blindless.setBounds(154, 2335, 140, 23);









        KlasifikasiIMT1.setEditable(false);
        KlasifikasiIMT1.setFocusTraversalPolicyProvider(true);
        KlasifikasiIMT1.setName("KlasifikasiIMT1"); // NOI18N
        FormInput.add(KlasifikasiIMT1);
        KlasifikasiIMT1.setBounds(299, 2195, 170, 23);

        fundi.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Normal", "Abnormal" }));
        fundi.setName("fundi"); // NOI18N
        FormInput.add(fundi);
        fundi.setBounds(664, 2335, 140, 23);




        imunisasi_bcg.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "-", "No", "Yes" }));
        imunisasi_bcg.setName("imunisasi_bcg"); // NOI18N
        FormInput.add(imunisasi_bcg);
        imunisasi_bcg.setBounds(106, 2390, 68, 23);




        imunisasi_dpt.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "-", "No", "Yes" }));
        imunisasi_dpt.setName("imunisasi_dpt"); // NOI18N
        FormInput.add(imunisasi_dpt);
        imunisasi_dpt.setBounds(282, 2390, 68, 23);

        imunisasi_polio.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "-", "No", "Yes" }));
        imunisasi_polio.setName("imunisasi_polio"); // NOI18N
        FormInput.add(imunisasi_polio);
        imunisasi_polio.setBounds(458, 2390, 68, 23);

        imunisasi_hep_b.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "-", "No", "Yes" }));
        imunisasi_hep_b.setName("imunisasi_hep_b"); // NOI18N
        FormInput.add(imunisasi_hep_b);
        imunisasi_hep_b.setBounds(282, 2420, 68, 23);







        imunisasi_morbili.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "-", "No", "Yes" }));
        imunisasi_morbili.setName("imunisasi_morbili"); // NOI18N
        FormInput.add(imunisasi_morbili);
        imunisasi_morbili.setBounds(634, 2390, 68, 23);

        imunisasi_tetanus.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "-", "No", "Yes" }));
        imunisasi_tetanus.setName("imunisasi_tetanus"); // NOI18N
        FormInput.add(imunisasi_tetanus);
        imunisasi_tetanus.setBounds(458, 2420, 68, 23);

        imunisasi_thyphoid.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "-", "No", "Yes" }));
        imunisasi_thyphoid.setName("imunisasi_thyphoid"); // NOI18N
        FormInput.add(imunisasi_thyphoid);
        imunisasi_thyphoid.setBounds(810, 2390, 68, 23);

        imunisasi_others.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "-", "No", "Yes" }));
        imunisasi_others.setName("imunisasi_others"); // NOI18N
        FormInput.add(imunisasi_others);
        imunisasi_others.setBounds(634, 2420, 68, 23);

        imunisasi_hep_a.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "-", "No", "Yes" }));
        imunisasi_hep_a.setName("imunisasi_hep_a"); // NOI18N
        FormInput.add(imunisasi_hep_a);
        imunisasi_hep_a.setBounds(106, 2420, 68, 23);


        vertebra_scoliosis.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Normal", "Yes", "No" }));
        vertebra_scoliosis.setName("vertebra_scoliosis"); // NOI18N
        FormInput.add(vertebra_scoliosis);
        vertebra_scoliosis.setBounds(154, 2475, 80, 23);




        vertebra_lordosis.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Normal", "Yes", "No" }));
        vertebra_lordosis.setName("vertebra_lordosis"); // NOI18N
        FormInput.add(vertebra_lordosis);
        vertebra_lordosis.setBounds(154, 2535, 80, 23);

        vertebra_hyperextensi_0_25.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Normal", "Yes", "No" }));
        vertebra_hyperextensi_0_25.setName("vertebra_hyperextensi_0_25"); // NOI18N
        FormInput.add(vertebra_hyperextensi_0_25);
        vertebra_hyperextensi_0_25.setBounds(154, 2595, 80, 23);

        vertebra_forward_flexion_0_80.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Normal", "Yes", "No" }));
        vertebra_forward_flexion_0_80.setName("vertebra_forward_flexion_0_80"); // NOI18N
        FormInput.add(vertebra_forward_flexion_0_80);
        vertebra_forward_flexion_0_80.setBounds(154, 2565, 80, 23);







        vertebra_heel_walking.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Normal", "Yes", "No" }));
        vertebra_heel_walking.setName("vertebra_heel_walking"); // NOI18N
        FormInput.add(vertebra_heel_walking);
        vertebra_heel_walking.setBounds(454, 2505, 80, 23);

        vertebra_lateral_flexion_0_20.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Normal", "Yes", "No" }));
        vertebra_lateral_flexion_0_20.setName("vertebra_lateral_flexion_0_20"); // NOI18N
        FormInput.add(vertebra_lateral_flexion_0_20);
        vertebra_lateral_flexion_0_20.setBounds(454, 2475, 80, 23);

        vertebra_squats_x3.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Normal", "Yes", "No" }));
        vertebra_squats_x3.setName("vertebra_squats_x3"); // NOI18N
        FormInput.add(vertebra_squats_x3);
        vertebra_squats_x3.setBounds(454, 2565, 80, 23);

        vertebra_toe_walking.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Normal", "Yes", "No" }));
        vertebra_toe_walking.setName("vertebra_toe_walking"); // NOI18N
        FormInput.add(vertebra_toe_walking);
        vertebra_toe_walking.setBounds(454, 2535, 80, 23);

        vertebra_kyphosis.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Normal", "Yes", "No" }));
        vertebra_kyphosis.setName("vertebra_kyphosis"); // NOI18N
        FormInput.add(vertebra_kyphosis);
        vertebra_kyphosis.setBounds(154, 2505, 80, 23);



        exam_ent_comments.setFocusTraversalPolicyProvider(true);
        exam_ent_comments.setName("exam_ent_comments"); // NOI18N
        FormInput.add(exam_ent_comments);
        exam_ent_comments.setBounds(204, 2675, 230, 23);

        exam_cardio_vascular_system_comments.setFocusTraversalPolicyProvider(true);
        exam_cardio_vascular_system_comments.setName("exam_cardio_vascular_system_comments"); // NOI18N
        FormInput.add(exam_cardio_vascular_system_comments);
        exam_cardio_vascular_system_comments.setBounds(204, 2705, 230, 23);


        exam_respiratory_system_comments.setFocusTraversalPolicyProvider(true);
        exam_respiratory_system_comments.setName("exam_respiratory_system_comments"); // NOI18N
        FormInput.add(exam_respiratory_system_comments);
        exam_respiratory_system_comments.setBounds(204, 2735, 230, 23);


        exam_abdomen_comments.setFocusTraversalPolicyProvider(true);
        exam_abdomen_comments.setName("exam_abdomen_comments"); // NOI18N
        FormInput.add(exam_abdomen_comments);
        exam_abdomen_comments.setBounds(204, 2765, 230, 23);


        exam_genito_urinary_system_comments.setFocusTraversalPolicyProvider(true);
        exam_genito_urinary_system_comments.setName("exam_genito_urinary_system_comments"); // NOI18N
        FormInput.add(exam_genito_urinary_system_comments);
        exam_genito_urinary_system_comments.setBounds(204, 2795, 230, 23);


        exam_central_peripheral_nervous_system_comments.setFocusTraversalPolicyProvider(true);
        exam_central_peripheral_nervous_system_comments.setName("exam_central_peripheral_nervous_system_comments"); // NOI18N
        FormInput.add(exam_central_peripheral_nervous_system_comments);
        exam_central_peripheral_nervous_system_comments.setBounds(644, 2675, 226, 23);


        exam_skin_comments.setFocusTraversalPolicyProvider(true);
        exam_skin_comments.setName("exam_skin_comments"); // NOI18N
        FormInput.add(exam_skin_comments);
        exam_skin_comments.setBounds(644, 2705, 226, 23);


        exam_lymph_nodes_comments.setFocusTraversalPolicyProvider(true);
        exam_lymph_nodes_comments.setName("exam_lymph_nodes_comments"); // NOI18N
        FormInput.add(exam_lymph_nodes_comments);
        exam_lymph_nodes_comments.setBounds(644, 2735, 226, 23);






        SukuBangsa.setHighlighter(null);
        SukuBangsa.setName("SukuBangsa"); // NOI18N
        FormInput.add(SukuBangsa);
        SukuBangsa.setBounds(124, 155, 170, 23);


        NIP.setHighlighter(null);
        NIP.setName("NIP"); // NOI18N
        FormInput.add(NIP);
        NIP.setBounds(419, 185, 150, 23);

        KdDokter1.setEditable(false);
        KdDokter1.setName("KdDokter1"); // NOI18N
        KdDokter1.setPreferredSize(new java.awt.Dimension(80, 23));
        FormInput.add(KdDokter1);
        KdDokter1.setBounds(74, 40, 100, 23);

        Year.setFocusTraversalPolicyProvider(true);
        Year.setName("Year"); // NOI18N
        Year.setPreferredSize(new java.awt.Dimension(80, 23));
        FormInput.add(Year);
        Year.setBounds(820, 40, 50, 23);


        visual_fields_left.setFocusTraversalPolicyProvider(true);
        visual_fields_left.setName("visual_fields_left"); // NOI18N
        FormInput.add(visual_fields_left);
        visual_fields_left.setBounds(424, 2335, 70, 23);




        conclusion_requires_spectacles.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "No", "Yes" }));
        conclusion_requires_spectacles.setName("conclusion_requires_spectacles"); // NOI18N
        FormInput.add(conclusion_requires_spectacles);
        conclusion_requires_spectacles.setBounds(124, 3295, 70, 23);





        conclusion_respiratory_problem.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "No", "Yes" }));
        conclusion_respiratory_problem.setName("conclusion_respiratory_problem"); // NOI18N
        FormInput.add(conclusion_respiratory_problem);
        conclusion_respiratory_problem.setBounds(694, 3295, 70, 23);

        fit.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Medically Fit", "Presently has minor medical problem", "Unfit" }));
        fit.setName("fit"); // NOI18N
        FormInput.add(fit);
        fit.setBounds(124, 3380, 250, 23);

        conclusion_vertigo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "No", "Yes" }));
        conclusion_vertigo.setName("conclusion_vertigo"); // NOI18N
        FormInput.add(conclusion_vertigo);
        conclusion_vertigo.setBounds(419, 3325, 70, 23);

        conclusion_impaired_hearing.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "No", "Yes" }));
        conclusion_impaired_hearing.setName("conclusion_impaired_hearing"); // NOI18N
        FormInput.add(conclusion_impaired_hearing);
        conclusion_impaired_hearing.setBounds(124, 3325, 70, 23);


        blood_group.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-" }));
        blood_group.setName("blood_group"); // NOI18N
        FormInput.add(blood_group);
        blood_group.setBounds(694, 3325, 70, 23);


        conclusion_colour_blindness.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "No", "Yes" }));
        conclusion_colour_blindness.setName("conclusion_colour_blindness"); // NOI18N
        FormInput.add(conclusion_colour_blindness);
        conclusion_colour_blindness.setBounds(419, 3295, 70, 23);


        fit_with_restrictions.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Medically fit but has following restrictions", "Work duties will be restricted", "Specify", "-" }));
        fit_with_restrictions.setName("fit_with_restrictions"); // NOI18N
        FormInput.add(fit_with_restrictions);
        fit_with_restrictions.setBounds(124, 3410, 250, 23);

        exam_dental_muskulo.setFocusTraversalPolicyProvider(true);
        exam_dental_muskulo.setName("exam_dental_muskulo"); // NOI18N
        FormInput.add(exam_dental_muskulo);
        exam_dental_muskulo.setBounds(644, 2795, 226, 23);

        audiometri_left_ear_1001.setFocusTraversalPolicyProvider(true);
        audiometri_left_ear_1001.setName("audiometri_left_ear_1001"); // NOI18N
        FormInput.add(audiometri_left_ear_1001);
        audiometri_left_ear_1001.setBounds(244, 2050, 80, 23);

        audiometri_left_ear_2001.setFocusTraversalPolicyProvider(true);
        audiometri_left_ear_2001.setName("audiometri_left_ear_2001"); // NOI18N
        FormInput.add(audiometri_left_ear_2001);
        audiometri_left_ear_2001.setBounds(424, 2050, 80, 23);



        audiometri_left_ear_1501.setFocusTraversalPolicyProvider(true);
        audiometri_left_ear_1501.setName("audiometri_left_ear_1501"); // NOI18N
        FormInput.add(audiometri_left_ear_1501);
        audiometri_left_ear_1501.setBounds(334, 2050, 80, 23);



        audiometri_left_ear_501.setFocusTraversalPolicyProvider(true);
        audiometri_left_ear_501.setName("audiometri_left_ear_501"); // NOI18N
        FormInput.add(audiometri_left_ear_501);
        audiometri_left_ear_501.setBounds(154, 2050, 80, 23);

        audiometri_left_ear_3001.setFocusTraversalPolicyProvider(true);
        audiometri_left_ear_3001.setName("audiometri_left_ear_3001"); // NOI18N
        FormInput.add(audiometri_left_ear_3001);
        audiometri_left_ear_3001.setBounds(514, 2050, 80, 23);


        audiometri_left_ear_4001.setFocusTraversalPolicyProvider(true);
        audiometri_left_ear_4001.setName("audiometri_left_ear_4001"); // NOI18N
        FormInput.add(audiometri_left_ear_4001);
        audiometri_left_ear_4001.setBounds(604, 2050, 80, 23);


        audiometri_left_ear_6001.setFocusTraversalPolicyProvider(true);
        audiometri_left_ear_6001.setName("audiometri_left_ear_6001"); // NOI18N
        FormInput.add(audiometri_left_ear_6001);
        audiometri_left_ear_6001.setBounds(784, 2050, 80, 23);



        audiometri_left_ear_5001.setFocusTraversalPolicyProvider(true);
        audiometri_left_ear_5001.setName("audiometri_left_ear_5001"); // NOI18N
        FormInput.add(audiometri_left_ear_5001);
        audiometri_left_ear_5001.setBounds(694, 2050, 80, 23);

        audiometri_left_ear_1002.setFocusTraversalPolicyProvider(true);
        audiometri_left_ear_1002.setName("audiometri_left_ear_1002"); // NOI18N
        FormInput.add(audiometri_left_ear_1002);
        audiometri_left_ear_1002.setBounds(244, 2080, 80, 23);

        audiometri_left_ear_2002.setFocusTraversalPolicyProvider(true);
        audiometri_left_ear_2002.setName("audiometri_left_ear_2002"); // NOI18N
        FormInput.add(audiometri_left_ear_2002);
        audiometri_left_ear_2002.setBounds(424, 2080, 80, 23);

        audiometri_left_ear_1502.setFocusTraversalPolicyProvider(true);
        audiometri_left_ear_1502.setName("audiometri_left_ear_1502"); // NOI18N
        FormInput.add(audiometri_left_ear_1502);
        audiometri_left_ear_1502.setBounds(334, 2080, 80, 23);

        audiometri_left_ear_502.setFocusTraversalPolicyProvider(true);
        audiometri_left_ear_502.setName("audiometri_left_ear_502"); // NOI18N
        FormInput.add(audiometri_left_ear_502);
        audiometri_left_ear_502.setBounds(154, 2080, 80, 23);

        audiometri_left_ear_3002.setFocusTraversalPolicyProvider(true);
        audiometri_left_ear_3002.setName("audiometri_left_ear_3002"); // NOI18N
        FormInput.add(audiometri_left_ear_3002);
        audiometri_left_ear_3002.setBounds(514, 2080, 80, 23);

        audiometri_left_ear_4002.setFocusTraversalPolicyProvider(true);
        audiometri_left_ear_4002.setName("audiometri_left_ear_4002"); // NOI18N
        FormInput.add(audiometri_left_ear_4002);
        audiometri_left_ear_4002.setBounds(604, 2080, 80, 23);

        audiometri_left_ear_6002.setFocusTraversalPolicyProvider(true);
        audiometri_left_ear_6002.setName("audiometri_left_ear_6002"); // NOI18N
        FormInput.add(audiometri_left_ear_6002);
        audiometri_left_ear_6002.setBounds(784, 2080, 80, 23);

        audiometri_left_ear_5002.setFocusTraversalPolicyProvider(true);
        audiometri_left_ear_5002.setName("audiometri_left_ear_5002"); // NOI18N
        FormInput.add(audiometri_left_ear_5002);
        audiometri_left_ear_5002.setBounds(694, 2080, 80, 23);

        audiometri_left_ear_1003.setFocusTraversalPolicyProvider(true);
        audiometri_left_ear_1003.setName("audiometri_left_ear_1003"); // NOI18N
        FormInput.add(audiometri_left_ear_1003);
        audiometri_left_ear_1003.setBounds(244, 2110, 80, 23);

        audiometri_left_ear_2003.setFocusTraversalPolicyProvider(true);
        audiometri_left_ear_2003.setName("audiometri_left_ear_2003"); // NOI18N
        FormInput.add(audiometri_left_ear_2003);
        audiometri_left_ear_2003.setBounds(424, 2110, 80, 23);

        audiometri_left_ear_1503.setFocusTraversalPolicyProvider(true);
        audiometri_left_ear_1503.setName("audiometri_left_ear_1503"); // NOI18N
        FormInput.add(audiometri_left_ear_1503);
        audiometri_left_ear_1503.setBounds(334, 2110, 80, 23);

        audiometri_left_ear_503.setFocusTraversalPolicyProvider(true);
        audiometri_left_ear_503.setName("audiometri_left_ear_503"); // NOI18N
        FormInput.add(audiometri_left_ear_503);
        audiometri_left_ear_503.setBounds(154, 2110, 80, 23);

        audiometri_left_ear_3003.setFocusTraversalPolicyProvider(true);
        audiometri_left_ear_3003.setName("audiometri_left_ear_3003"); // NOI18N
        FormInput.add(audiometri_left_ear_3003);
        audiometri_left_ear_3003.setBounds(514, 2110, 80, 23);

        audiometri_left_ear_4003.setFocusTraversalPolicyProvider(true);
        audiometri_left_ear_4003.setName("audiometri_left_ear_4003"); // NOI18N
        FormInput.add(audiometri_left_ear_4003);
        audiometri_left_ear_4003.setBounds(604, 2110, 80, 23);

        audiometri_left_ear_6003.setFocusTraversalPolicyProvider(true);
        audiometri_left_ear_6003.setName("audiometri_left_ear_6003"); // NOI18N
        FormInput.add(audiometri_left_ear_6003);
        audiometri_left_ear_6003.setBounds(784, 2110, 80, 23);

        audiometri_left_ear_5003.setFocusTraversalPolicyProvider(true);
        audiometri_left_ear_5003.setName("audiometri_left_ear_5003"); // NOI18N
        FormInput.add(audiometri_left_ear_5003);
        audiometri_left_ear_5003.setBounds(694, 2110, 80, 23);


        KdPetugas.setEditable(false);
        KdPetugas.setName("KdPetugas"); // NOI18N
        KdPetugas.setPreferredSize(new java.awt.Dimension(80, 23));
        FormInput.add(KdPetugas);
        KdPetugas.setBounds(74, 70, 100, 23);

        NmPetugas.setEditable(false);
        NmPetugas.setName("NmPetugas"); // NOI18N
        NmPetugas.setPreferredSize(new java.awt.Dimension(207, 23));
        FormInput.add(NmPetugas);
        NmPetugas.setBounds(176, 70, 180, 23);

        BtnPetugas.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        BtnPetugas.setMnemonic('2');
        BtnPetugas.setToolTipText("Alt+2");
        BtnPetugas.setName("BtnPetugas"); // NOI18N
        BtnPetugas.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnPetugas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnPetugasActionPerformed(evt);
            }
        });
        FormInput.add(BtnPetugas);
        BtnPetugas.setBounds(358, 70, 28, 23);

        ConcRadiologi.setName("ConcRadiologi"); // NOI18N
        ConcRadiologi.setPreferredSize(new java.awt.Dimension(80, 23));
        ConcRadiologi.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                ConcRadiologiKeyPressed(evt);
            }
        });
        FormInput.add(ConcRadiologi);
        ConcRadiologi.setBounds(124, 3060, 310, 23);




        scrollPane12.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        scrollPane12.setName("scrollPane12"); // NOI18N

        PhyExam.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        PhyExam.setColumns(20);
        PhyExam.setRows(5);
        PhyExam.setName("PhyExam"); // NOI18N
        PhyExam.setPreferredSize(new java.awt.Dimension(182, 52));
        scrollPane12.setViewportView(PhyExam);

        FormInput.add(scrollPane12);
        scrollPane12.setBounds(464, 2990, 406, 60);

        scrollPane13.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        scrollPane13.setName("scrollPane13"); // NOI18N

        catatan.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        catatan.setColumns(20);
        catatan.setRows(5);
        catatan.setName("catatan"); // NOI18N
        catatan.setPreferredSize(new java.awt.Dimension(182, 52));
        scrollPane13.setViewportView(catatan);

        FormInput.add(scrollPane13);
        scrollPane13.setBounds(204, 2825, 666, 53);



        scrollPane5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        scrollPane5.setName("scrollPane5"); // NOI18N

        kesimpulan.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        kesimpulan.setColumns(20);
        kesimpulan.setRows(5);
        kesimpulan.setName("kesimpulan"); // NOI18N
        scrollPane5.setViewportView(kesimpulan);

        FormInput.add(scrollPane5);
        scrollPane5.setBounds(44, 3140, 410, 60);

        scrollPane6.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        scrollPane6.setName("scrollPane6"); // NOI18N

        saran.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        saran.setColumns(20);
        saran.setRows(5);
        saran.setName("saran"); // NOI18N
        scrollPane6.setViewportView(saran);

        FormInput.add(scrollPane6);
        scrollPane6.setBounds(464, 3140, 406, 60);

        scrollPane7.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        scrollPane7.setName("scrollPane7"); // NOI18N

        Dass21.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        Dass21.setColumns(20);
        Dass21.setRows(5);
        Dass21.setName("Dass21"); // NOI18N
        scrollPane7.setViewportView(Dass21);

        FormInput.add(scrollPane7);
        scrollPane7.setBounds(44, 2990, 410, 60);


        ConcEcg.setName("ConcEcg"); // NOI18N
        ConcEcg.setPreferredSize(new java.awt.Dimension(80, 23));
        FormInput.add(ConcEcg);
        ConcEcg.setBounds(524, 3060, 346, 23);


        ConcSpirometry.setName("ConcSpirometry"); // NOI18N
        ConcSpirometry.setPreferredSize(new java.awt.Dimension(80, 23));
        FormInput.add(ConcSpirometry);
        ConcSpirometry.setBounds(124, 3090, 310, 23);


        ConcAudiometry.setName("ConcAudiometry"); // NOI18N
        ConcAudiometry.setPreferredSize(new java.awt.Dimension(80, 23));
        FormInput.add(ConcAudiometry);
        ConcAudiometry.setBounds(524, 3090, 346, 23);



        TPasien.setEditable(false);
        TPasien.setHighlighter(null);
        TPasien.setName("TPasien"); // NOI18N
        FormInput.add(TPasien);
        TPasien.setBounds(309, 10, 260, 23);

        spirometri_fvc_1.setFocusTraversalPolicyProvider(true);
        spirometri_fvc_1.setName("spirometri_fvc_1"); // NOI18N
        FormInput.add(spirometri_fvc_1);
        spirometri_fvc_1.setBounds(154, 1790, 120, 23);


        wbc.setFocusTraversalPolicyProvider(true);
        wbc.setName("wbc"); // NOI18N
        FormInput.add(wbc);
        wbc.setBounds(282, 775, 68, 23);



        esr.setFocusTraversalPolicyProvider(true);
        esr.setName("esr"); // NOI18N
        FormInput.add(esr);
        esr.setBounds(458, 775, 68, 23);


        bl_group.setFocusTraversalPolicyProvider(true);
        bl_group.setName("bl_group"); // NOI18N
        FormInput.add(bl_group);
        bl_group.setBounds(634, 775, 68, 23);

        sgot.setFocusTraversalPolicyProvider(true);
        sgot.setName("sgot"); // NOI18N
        FormInput.add(sgot);
        sgot.setBounds(106, 805, 68, 23);


        sgpt.setFocusTraversalPolicyProvider(true);
        sgpt.setName("sgpt"); // NOI18N
        FormInput.add(sgpt);
        sgpt.setBounds(282, 805, 68, 23);



        urea.setFocusTraversalPolicyProvider(true);
        urea.setName("urea"); // NOI18N
        FormInput.add(urea);
        urea.setBounds(458, 805, 68, 23);


        creatinin.setFocusTraversalPolicyProvider(true);
        creatinin.setName("creatinin"); // NOI18N
        FormInput.add(creatinin);
        creatinin.setBounds(634, 805, 68, 23);


        gamaa_gt.setFocusTraversalPolicyProvider(true);
        gamaa_gt.setName("gamaa_gt"); // NOI18N
        FormInput.add(gamaa_gt);
        gamaa_gt.setBounds(810, 775, 68, 23);


        glucose.setFocusTraversalPolicyProvider(true);
        glucose.setName("glucose"); // NOI18N
        FormInput.add(glucose);
        glucose.setBounds(810, 805, 68, 23);

        random_glucose.setFocusTraversalPolicyProvider(true);
        random_glucose.setName("random_glucose"); // NOI18N
        FormInput.add(random_glucose);
        random_glucose.setBounds(106, 835, 68, 23);


        total_cholestrol.setFocusTraversalPolicyProvider(true);
        total_cholestrol.setName("total_cholestrol"); // NOI18N
        FormInput.add(total_cholestrol);
        total_cholestrol.setBounds(282, 835, 68, 23);



        protein.setFocusTraversalPolicyProvider(true);
        protein.setName("protein"); // NOI18N
        FormInput.add(protein);
        protein.setBounds(458, 835, 68, 23);


        blood.setFocusTraversalPolicyProvider(true);
        blood.setName("blood"); // NOI18N
        FormInput.add(blood);
        blood.setBounds(634, 835, 68, 23);

        bilirubin.setFocusTraversalPolicyProvider(true);
        bilirubin.setName("bilirubin"); // NOI18N
        FormInput.add(bilirubin);
        bilirubin.setBounds(106, 865, 68, 23);


        malaria.setFocusTraversalPolicyProvider(true);
        malaria.setName("malaria"); // NOI18N
        FormInput.add(malaria);
        malaria.setBounds(282, 865, 68, 23);



        tpha.setFocusTraversalPolicyProvider(true);
        tpha.setName("tpha"); // NOI18N
        FormInput.add(tpha);
        tpha.setBounds(458, 865, 68, 23);


        mantoux_test.setFocusTraversalPolicyProvider(true);
        mantoux_test.setName("mantoux_test"); // NOI18N
        FormInput.add(mantoux_test);
        mantoux_test.setBounds(634, 865, 68, 23);


        leukosit.setFocusTraversalPolicyProvider(true);
        leukosit.setName("leukosit"); // NOI18N
        FormInput.add(leukosit);
        leukosit.setBounds(810, 835, 68, 23);


        lab_others.setFocusTraversalPolicyProvider(true);
        lab_others.setName("lab_others"); // NOI18N
        FormInput.add(lab_others);
        lab_others.setBounds(810, 865, 68, 23);

        ova.setFocusTraversalPolicyProvider(true);
        ova.setName("ova"); // NOI18N
        FormInput.add(ova);
        ova.setBounds(106, 895, 68, 23);


        culture.setFocusTraversalPolicyProvider(true);
        culture.setName("culture"); // NOI18N
        FormInput.add(culture);
        culture.setBounds(282, 895, 68, 23);



        cysta.setFocusTraversalPolicyProvider(true);
        cysta.setName("cysta"); // NOI18N
        FormInput.add(cysta);
        cysta.setBounds(458, 895, 68, 23);


        pnemunosicosis2.setFocusTraversalPolicyProvider(true);
        pnemunosicosis2.setName("pnemunosicosis2"); // NOI18N
        FormInput.add(pnemunosicosis2);
        pnemunosicosis2.setBounds(411, 1405, 255, 23);





        parasites1.setFocusTraversalPolicyProvider(true);
        parasites1.setName("parasites1"); // NOI18N
        FormInput.add(parasites1);
        parasites1.setBounds(634, 895, 68, 23);

        pnemunosicosis.setFocusTraversalPolicyProvider(true);
        pnemunosicosis.setName("pnemunosicosis"); // NOI18N
        FormInput.add(pnemunosicosis);
        pnemunosicosis.setBounds(154, 1405, 255, 23);

        ILO_clasification.setFocusTraversalPolicyProvider(true);
        ILO_clasification.setName("ILO_clasification"); // NOI18N
        FormInput.add(ILO_clasification);
        ILO_clasification.setBounds(154, 1435, 255, 23);

        ILO_clasification2.setFocusTraversalPolicyProvider(true);
        ILO_clasification2.setName("ILO_clasification2"); // NOI18N
        FormInput.add(ILO_clasification2);
        ILO_clasification2.setBounds(411, 1435, 255, 23);

        oth_abnormal.setFocusTraversalPolicyProvider(true);
        oth_abnormal.setName("oth_abnormal"); // NOI18N
        FormInput.add(oth_abnormal);
        oth_abnormal.setBounds(154, 1495, 716, 23);

        tb2.setFocusTraversalPolicyProvider(true);
        tb2.setName("tb2"); // NOI18N
        FormInput.add(tb2);
        tb2.setBounds(411, 1465, 255, 23);

        tb1.setFocusTraversalPolicyProvider(true);
        tb1.setName("tb1"); // NOI18N
        FormInput.add(tb1);
        tb1.setBounds(154, 1465, 255, 23);

        page3_comment.setFocusTraversalPolicyProvider(true);
        page3_comment.setName("page3_comment"); // NOI18N
        FormInput.add(page3_comment);
        page3_comment.setBounds(154, 1525, 716, 23);



















        type_of_hearing.setFocusTraversalPolicyProvider(true);
        type_of_hearing.setName("type_of_hearing"); // NOI18N
        FormInput.add(type_of_hearing);
        type_of_hearing.setBounds(154, 1965, 470, 23);

        eye_unaided_distant_l.setFocusTraversalPolicyProvider(true);
        eye_unaided_distant_l.setName("eye_unaided_distant_l"); // NOI18N
        eye_unaided_distant_l.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                eye_unaided_distant_lKeyPressed(evt);
            }
        });
        FormInput.add(eye_unaided_distant_l);
        eye_unaided_distant_l.setBounds(154, 2275, 95, 23);

        eye_unaided_distant_r.setFocusTraversalPolicyProvider(true);
        eye_unaided_distant_r.setName("eye_unaided_distant_r"); // NOI18N
        eye_unaided_distant_r.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                eye_unaided_distant_rKeyPressed(evt);
            }
        });
        FormInput.add(eye_unaided_distant_r);
        eye_unaided_distant_r.setBounds(254, 2275, 95, 23);

        BtnPT.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        BtnPT.setMnemonic('2');
        BtnPT.setToolTipText("Alt+2");
        BtnPT.setName("BtnPT"); // NOI18N
        BtnPT.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnPT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnPTActionPerformed(evt);
            }
        });
        FormInput.add(BtnPT);
        BtnPT.setBounds(266, 185, 28, 23);

        trombosit.setHighlighter(null);
        trombosit.setName("trombosit"); // NOI18N
        FormInput.add(trombosit);
        trombosit.setBounds(106, 950, 68, 23);

        rhesuss.setHighlighter(null);
        rhesuss.setName("rhesuss"); // NOI18N
        FormInput.add(rhesuss);
        rhesuss.setBounds(282, 950, 68, 23);

        triglyceride.setHighlighter(null);
        triglyceride.setName("triglyceride"); // NOI18N
        FormInput.add(triglyceride);
        triglyceride.setBounds(458, 950, 68, 23);

        hdl_cholesterol.setHighlighter(null);
        hdl_cholesterol.setName("hdl_cholesterol"); // NOI18N
        FormInput.add(hdl_cholesterol);
        hdl_cholesterol.setBounds(634, 950, 68, 23);

        ldl_cholesterol.setHighlighter(null);
        ldl_cholesterol.setName("ldl_cholesterol"); // NOI18N
        FormInput.add(ldl_cholesterol);
        ldl_cholesterol.setBounds(810, 950, 68, 23);

        uric_acid.setHighlighter(null);
        uric_acid.setName("uric_acid"); // NOI18N
        FormInput.add(uric_acid);
        uric_acid.setBounds(106, 980, 68, 23);

        urine_colour.setHighlighter(null);
        urine_colour.setName("urine_colour"); // NOI18N
        FormInput.add(urine_colour);
        urine_colour.setBounds(106, 1035, 68, 23);

        urine_turbidity.setHighlighter(null);
        urine_turbidity.setName("urine_turbidity"); // NOI18N
        FormInput.add(urine_turbidity);
        urine_turbidity.setBounds(282, 1035, 68, 23);

        urine_chemical_reaction.setHighlighter(null);
        urine_chemical_reaction.setName("urine_chemical_reaction"); // NOI18N
        FormInput.add(urine_chemical_reaction);
        urine_chemical_reaction.setBounds(458, 1035, 68, 23);

        urine_ketones.setHighlighter(null);
        urine_ketones.setName("urine_ketones"); // NOI18N
        FormInput.add(urine_ketones);
        urine_ketones.setBounds(634, 1035, 68, 23);

        urine_glucose.setHighlighter(null);
        urine_glucose.setName("urine_glucose"); // NOI18N
        FormInput.add(urine_glucose);
        urine_glucose.setBounds(810, 1035, 68, 23);

        urine_nitrites.setHighlighter(null);
        urine_nitrites.setName("urine_nitrites"); // NOI18N
        FormInput.add(urine_nitrites);
        urine_nitrites.setBounds(106, 1065, 68, 23);

        urine_wbc.setHighlighter(null);
        urine_wbc.setName("urine_wbc"); // NOI18N
        FormInput.add(urine_wbc);
        urine_wbc.setBounds(282, 1065, 68, 23);

        urine_rbc.setHighlighter(null);
        urine_rbc.setName("urine_rbc"); // NOI18N
        FormInput.add(urine_rbc);
        urine_rbc.setBounds(458, 1065, 68, 23);

        urine_bacteria.setHighlighter(null);
        urine_bacteria.setName("urine_bacteria"); // NOI18N
        FormInput.add(urine_bacteria);
        urine_bacteria.setBounds(634, 1065, 68, 23);

        urine_crystal.setHighlighter(null);
        urine_crystal.setName("urine_crystal"); // NOI18N
        FormInput.add(urine_crystal);
        urine_crystal.setBounds(810, 1065, 68, 23);

        urine_epithel.setHighlighter(null);
        urine_epithel.setName("urine_epithel"); // NOI18N
        FormInput.add(urine_epithel);
        urine_epithel.setBounds(106, 1095, 68, 23);

        hbsag.setHighlighter(null);
        hbsag.setName("hbsag"); // NOI18N
        FormInput.add(hbsag);
        hbsag.setBounds(106, 1150, 68, 23);

        anti_hbs.setHighlighter(null);
        anti_hbs.setName("anti_hbs"); // NOI18N
        FormInput.add(anti_hbs);
        anti_hbs.setBounds(282, 1150, 68, 23);

        cea.setHighlighter(null);
        cea.setName("cea"); // NOI18N
        FormInput.add(cea);
        cea.setBounds(458, 1150, 68, 23);

        afp.setHighlighter(null);
        afp.setName("afp"); // NOI18N
        FormInput.add(afp);
        afp.setBounds(634, 1150, 68, 23);

        drug_amphetamine.setHighlighter(null);
        drug_amphetamine.setName("drug_amphetamine"); // NOI18N
        FormInput.add(drug_amphetamine);
        drug_amphetamine.setBounds(106, 1205, 68, 23);

        drug_methamphetamine.setHighlighter(null);
        drug_methamphetamine.setName("drug_methamphetamine"); // NOI18N
        FormInput.add(drug_methamphetamine);
        drug_methamphetamine.setBounds(282, 1205, 68, 23);

        drug_morphine.setHighlighter(null);
        drug_morphine.setName("drug_morphine"); // NOI18N
        FormInput.add(drug_morphine);
        drug_morphine.setBounds(458, 1205, 68, 23);

        drug_benzodiazepine.setHighlighter(null);
        drug_benzodiazepine.setName("drug_benzodiazepine"); // NOI18N
        FormInput.add(drug_benzodiazepine);
        drug_benzodiazepine.setBounds(634, 1205, 68, 23);

        drug_cocain.setHighlighter(null);
        drug_cocain.setName("drug_cocain"); // NOI18N
        FormInput.add(drug_cocain);
        drug_cocain.setBounds(810, 1205, 68, 23);

        drug_marijuana.setHighlighter(null);
        drug_marijuana.setName("drug_marijuana"); // NOI18N
        FormInput.add(drug_marijuana);
        drug_marijuana.setBounds(106, 1235, 68, 23);
































        KdPetugasLab.setEditable(false);
        KdPetugasLab.setName("KdPetugasLab"); // NOI18N
        KdPetugasLab.setPreferredSize(new java.awt.Dimension(80, 23));
        FormInput.add(KdPetugasLab);
        KdPetugasLab.setBounds(154, 1350, 100, 23);

        NmPetugasLab.setEditable(false);
        NmPetugasLab.setName("NmPetugasLab"); // NOI18N
        NmPetugasLab.setPreferredSize(new java.awt.Dimension(207, 23));
        FormInput.add(NmPetugasLab);
        NmPetugasLab.setBounds(256, 1350, 200, 23);

        BtnPetugasLab.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        BtnPetugasLab.setMnemonic('2');
        BtnPetugasLab.setToolTipText("Alt+2");
        BtnPetugasLab.setName("BtnPetugasLab"); // NOI18N
        BtnPetugasLab.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnPetugasLab.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnPetugasLabActionPerformed(evt);
            }
        });
        FormInput.add(BtnPetugasLab);
        BtnPetugasLab.setBounds(458, 1350, 28, 23);

        scrollPane14.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        scrollPane14.setName("scrollPane14"); // NOI18N

        ecg_abnormal.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        ecg_abnormal.setColumns(20);
        ecg_abnormal.setRows(5);
        ecg_abnormal.setName("ecg_abnormal"); // NOI18N
        ecg_abnormal.setPreferredSize(new java.awt.Dimension(182, 52));
        scrollPane14.setViewportView(ecg_abnormal);

        FormInput.add(scrollPane14);
        scrollPane14.setBounds(454, 1645, 416, 53);


        exam_dental_comments.setFocusTraversalPolicyProvider(true);
        exam_dental_comments.setName("exam_dental_comments"); // NOI18N
        FormInput.add(exam_dental_comments);
        exam_dental_comments.setBounds(644, 2765, 226, 23);


        dass1.setFocusTraversalPolicyProvider(true);
        dass1.setName("dass1"); // NOI18N
        FormInput.add(dass1);
        dass1.setBounds(124, 2915, 50, 23);

        dass2.setFocusTraversalPolicyProvider(true);
        dass2.setName("dass2"); // NOI18N
        FormInput.add(dass2);
        dass2.setBounds(264, 2915, 50, 23);

        dass3.setFocusTraversalPolicyProvider(true);
        dass3.setName("dass3"); // NOI18N
        FormInput.add(dass3);
        dass3.setBounds(384, 2915, 50, 23);






        eye_glasses_distant_l.setFocusTraversalPolicyProvider(true);
        eye_glasses_distant_l.setName("eye_glasses_distant_l"); // NOI18N
        eye_glasses_distant_l.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                eye_glasses_distant_lKeyPressed(evt);
            }
        });
        FormInput.add(eye_glasses_distant_l);
        eye_glasses_distant_l.setBounds(354, 2275, 95, 23);

        eye_unaided_near_l.setFocusTraversalPolicyProvider(true);
        eye_unaided_near_l.setName("eye_unaided_near_l"); // NOI18N
        eye_unaided_near_l.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                eye_unaided_near_lKeyPressed(evt);
            }
        });
        FormInput.add(eye_unaided_near_l);
        eye_unaided_near_l.setBounds(154, 2305, 95, 23);

        eye_glasses_distant_r.setFocusTraversalPolicyProvider(true);
        eye_glasses_distant_r.setName("eye_glasses_distant_r"); // NOI18N
        eye_glasses_distant_r.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                eye_glasses_distant_rKeyPressed(evt);
            }
        });
        FormInput.add(eye_glasses_distant_r);
        eye_glasses_distant_r.setBounds(454, 2275, 95, 23);

        eye_unaided_near_r.setFocusTraversalPolicyProvider(true);
        eye_unaided_near_r.setName("eye_unaided_near_r"); // NOI18N
        eye_unaided_near_r.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                eye_unaided_near_rKeyPressed(evt);
            }
        });
        FormInput.add(eye_unaided_near_r);
        eye_unaided_near_r.setBounds(254, 2305, 95, 23);

        eye_glasses_near_l.setFocusTraversalPolicyProvider(true);
        eye_glasses_near_l.setName("eye_glasses_near_l"); // NOI18N
        eye_glasses_near_l.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                eye_glasses_near_lKeyPressed(evt);
            }
        });
        FormInput.add(eye_glasses_near_l);
        eye_glasses_near_l.setBounds(354, 2305, 95, 23);

        eye_night_vision_1.setFocusTraversalPolicyProvider(true);
        eye_night_vision_1.setName("eye_night_vision_1"); // NOI18N
        eye_night_vision_1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                eye_night_vision_1KeyPressed(evt);
            }
        });
        FormInput.add(eye_night_vision_1);
        eye_night_vision_1.setBounds(664, 2275, 100, 23);

        eye_unaided_near_r1.setFocusTraversalPolicyProvider(true);
        eye_unaided_near_r1.setName("eye_unaided_near_r1"); // NOI18N
        eye_unaided_near_r1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                eye_unaided_near_r1KeyPressed(evt);
            }
        });
        FormInput.add(eye_unaided_near_r1);
        eye_unaided_near_r1.setBounds(664, 2305, 100, 23);

        eye_glasses_near_r.setFocusTraversalPolicyProvider(true);
        eye_glasses_near_r.setName("eye_glasses_near_r"); // NOI18N
        eye_glasses_near_r.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                eye_glasses_near_rKeyPressed(evt);
            }
        });
        FormInput.add(eye_glasses_near_r);
        eye_glasses_near_r.setBounds(454, 2305, 95, 23);

        eye_night_vision_2.setFocusTraversalPolicyProvider(true);
        eye_night_vision_2.setName("eye_night_vision_2"); // NOI18N
        eye_night_vision_2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                eye_night_vision_2KeyPressed(evt);
            }
        });
        FormInput.add(eye_night_vision_2);
        eye_night_vision_2.setBounds(766, 2275, 100, 23);

        eye_brake_test_2.setFocusTraversalPolicyProvider(true);
        eye_brake_test_2.setName("eye_brake_test_2"); // NOI18N
        eye_brake_test_2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                eye_brake_test_2KeyPressed(evt);
            }
        });
        FormInput.add(eye_brake_test_2);
        eye_brake_test_2.setBounds(766, 2305, 100, 23);





        manager.setName("manager"); // NOI18N
        manager.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                managerKeyPressed(evt);
            }
        });
        manager.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                managerActionPerformed(evt);
            }
        });
        FormInput.add(manager);
        manager.setBounds(694, 3240, 25, 23);

        departemen.setName("departemen"); // NOI18N
        departemen.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                departemenKeyPressed(evt);
            }
        });
        departemen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                departemenActionPerformed(evt);
            }
        });
        FormInput.add(departemen);
        departemen.setBounds(124, 3240, 25, 23);

        supervisor.setName("supervisor"); // NOI18N
        supervisor.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                supervisorKeyPressed(evt);
            }
        });
        supervisor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                supervisorActionPerformed(evt);
            }
        });
        FormInput.add(supervisor);
        supervisor.setBounds(419, 3240, 25, 23);


        TglLahir.setForeground(new java.awt.Color(50, 70, 50));
        TglLahir.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "08-08-2026" }));
        TglLahir.setDisplayFormat("dd-MM-yyyy");
        TglLahir.setName("TglLahir"); // NOI18N
        TglLahir.setOpaque(false);
        FormInput.add(TglLahir);
        TglLahir.setBounds(419, 125, 100, 23);

        Jk.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Laki Laki", "Perempuan" }));
        Jk.setName("Jk"); // NOI18N
        FormInput.add(Jk);
        Jk.setBounds(694, 125, 100, 23);

        btn_AmbilData.setIcon(new javax.swing.ImageIcon(getClass().getResource("/48x48/preview-16x16.png"))); // NOI18N
        btn_AmbilData.setText("Cari Data");
        btn_AmbilData.setName("btn_AmbilData"); // NOI18N
        btn_AmbilData.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_AmbilDataActionPerformed(evt);
            }
        });
        FormInput.add(btn_AmbilData);
        btn_AmbilData.setBounds(456, 70, 130, 23);

        scrollInput.setViewportView(FormInput);

        internalFrame2.add(scrollInput, java.awt.BorderLayout.CENTER);

        TabRawat.addTab("Input Pengkajian", internalFrame2);

        internalFrame3.setBorder(null);
        internalFrame3.setName("internalFrame3"); // NOI18N
        internalFrame3.setLayout(new java.awt.BorderLayout(1, 1));

        Scroll.setName("Scroll"); // NOI18N
        Scroll.setOpaque(true);
        Scroll.setPreferredSize(new java.awt.Dimension(452, 200));

        tbObat.setAutoCreateRowSorter(true);
        tbObat.setToolTipText("Silahkan klik untuk memilih data yang mau diedit ataupun dihapus");
        tbObat.setComponentPopupMenu(jPopupMenu1);
        tbObat.setName("tbObat"); // NOI18N
        tbObat.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbObatMouseClicked(evt);
            }
        });
        tbObat.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tbObatKeyPressed(evt);
            }
        });
        Scroll.setViewportView(tbObat);

        internalFrame3.add(Scroll, java.awt.BorderLayout.CENTER);

        panelGlass9.setName("panelGlass9"); // NOI18N
        panelGlass9.setPreferredSize(new java.awt.Dimension(44, 44));
        panelGlass9.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        jLabel19.setText("Tgl.Asuhan :");
        jLabel19.setName("jLabel19"); // NOI18N
        jLabel19.setPreferredSize(new java.awt.Dimension(70, 23));
        panelGlass9.add(jLabel19);

        DTPCari1.setForeground(new java.awt.Color(50, 70, 50));
        DTPCari1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "08-08-2026" }));
        DTPCari1.setDisplayFormat("dd-MM-yyyy");
        DTPCari1.setName("DTPCari1"); // NOI18N
        DTPCari1.setOpaque(false);
        DTPCari1.setPreferredSize(new java.awt.Dimension(90, 23));
        panelGlass9.add(DTPCari1);

        jLabel21.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel21.setText("s.d.");
        jLabel21.setName("jLabel21"); // NOI18N
        jLabel21.setPreferredSize(new java.awt.Dimension(23, 23));
        panelGlass9.add(jLabel21);

        DTPCari2.setForeground(new java.awt.Color(50, 70, 50));
        DTPCari2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "08-08-2026" }));
        DTPCari2.setDisplayFormat("dd-MM-yyyy");
        DTPCari2.setName("DTPCari2"); // NOI18N
        DTPCari2.setOpaque(false);
        DTPCari2.setPreferredSize(new java.awt.Dimension(90, 23));
        panelGlass9.add(DTPCari2);

        jLabel6.setText("Key Word :");
        jLabel6.setName("jLabel6"); // NOI18N
        jLabel6.setPreferredSize(new java.awt.Dimension(80, 23));
        panelGlass9.add(jLabel6);

        TCari.setName("TCari"); // NOI18N
        TCari.setPreferredSize(new java.awt.Dimension(195, 23));
        TCari.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TCariKeyPressed(evt);
            }
        });
        panelGlass9.add(TCari);

        BtnCari.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png"))); // NOI18N
        BtnCari.setMnemonic('3');
        BtnCari.setToolTipText("Alt+3");
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
        panelGlass9.add(BtnCari);

        jLabel7.setText("Record :");
        jLabel7.setName("jLabel7"); // NOI18N
        jLabel7.setPreferredSize(new java.awt.Dimension(60, 23));
        panelGlass9.add(jLabel7);

        LCount.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LCount.setText("0");
        LCount.setName("LCount"); // NOI18N
        LCount.setPreferredSize(new java.awt.Dimension(70, 23));
        panelGlass9.add(LCount);

        internalFrame3.add(panelGlass9, java.awt.BorderLayout.PAGE_END);

        TabRawat.addTab("Data Pengkajian", internalFrame3);

        internalFrame1.add(TabRawat, java.awt.BorderLayout.CENTER);

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void BtnSimpanActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_BtnSimpanActionPerformed
        sinkronFieldMcu();
        if (TNoRM.getText().trim().equals("")) {
            Valid.textKosong(TNoRw, "Nama Pasien");
        } else if (comboTerpilih(cbConcEcg, "Abnormal Condition") && ecg_abnormal.getText().trim().equals("")) {
            Valid.textKosong(ecg_abnormal, "Keterangan ECG Abnormal");

        } else if (saran.getText().trim().equals("")) {
            Valid.textKosong(saran, "Saran");
        } else if (NmDokter.getText().trim().equals("")) {
            Valid.textKosong(BtnDokter, "Dokter");
        } else if (KdDokter1.getText().trim().equals("")) {
            Valid.textKosong(BtnDokter, "Dokter");
            // }else if(RisikoLP.getText().trim().equals("")){
            // Valid.textKosong(RisikoLP,"Risiko Berdasar LP");
        } else {
            if (akses.getkode().equals("Admin Utama")) {
                simpan();
            } else {
                if (TanggalRegistrasi.getText().equals("")) {
                    TanggalRegistrasi.setText(Sequel.cariIsi(
                            "select concat(reg_periksa.tgl_registrasi,' ',reg_periksa.jam_reg) from reg_periksa where reg_periksa.no_rawat=?",
                            TNoRw.getText()));
                }
                setTanggalAsuhanMinimalRegistrasi();
                if (Sequel.cekTanggalRegistrasi(TanggalRegistrasi.getText(), getTanggalJamAsuhan()) == true) {
                    simpan();
                }
            }
        }

    }// GEN-LAST:event_BtnSimpanActionPerformed

    private void BtnSimpanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnSimpanKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnSimpanActionPerformed(null);
        }else{
            Valid.pindah(evt,specify,BtnBatal);
        }
    }//GEN-LAST:event_BtnSimpanKeyPressed

    private void surnameKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,TPasien,TglAsuhan);
    }

    private void BtnBatalActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_BtnBatalActionPerformed
        emptTeks();
    }// GEN-LAST:event_BtnBatalActionPerformed

    private void BtnBatalKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnBatalKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            emptTeks();
        }else{
            Valid.pindah(evt,BtnSimpan,BtnHapus);
        }
    }//GEN-LAST:event_BtnBatalKeyPressed

    private void BtnHapusActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_BtnHapusActionPerformed
        if (tbObat.getSelectedRow() > -1) {
            if (akses.getkode().equals("Admin Utama")) {
                hapus();
            } else {
                String depUser = Sequel.cariIsi("select departemen from pegawai where nik=?", akses.getkode());
                boolean bolehHapus = false;
                if ("R01".equals(depUser) || "R023".equals(depUser) || "R07".equals(depUser)) {
                    bolehHapus = true;
                } else {
                    bolehHapus = Sequel.cekTanggal48jam(getTabelValue("tanggal"), Sequel.ambiltanggalsekarang());
                }

                if (bolehHapus) {
                    hapus();
                }
            }
        } else {
            JOptionPane.showMessageDialog(rootPane, "Silahkan anda pilih data terlebih dahulu..!!");
        }

    }// GEN-LAST:event_BtnHapusActionPerformed

    private void BtnHapusKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnHapusKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnHapusActionPerformed(null);
        }else{
            Valid.pindah(evt,BtnBatal,BtnEdit);
        }
    }//GEN-LAST:event_BtnHapusKeyPressed

    private void BtnEditActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_BtnEditActionPerformed
        sinkronFieldMcu();
        if (TNoRM.getText().trim().equals("")) {
            Valid.textKosong(TNoRw, "Nama Pasien");
        } else if (comboTerpilih(cbConcEcg, "Abnormal Condition") && ecg_abnormal.getText().trim().equals("")) {
            Valid.textKosong(ecg_abnormal, "Keterangan ECG Abnormal");
        } else if (kesimpulan.getText().trim().equals("")) {
            Valid.textKosong(kesimpulan, "Kesimpulan");
        } else if (saran.getText().trim().equals("")) {
            Valid.textKosong(saran, "Saran");
        } else if (NmDokter.getText().trim().equals("")) {
            Valid.textKosong(BtnDokter, "Dokter");
        } else if (KdDokter1.getText().trim().equals("")) {
            Valid.textKosong(BtnDokter, "Dokter");
            // }else if(RisikoLP.getText().trim().equals("")){
            // Valid.textKosong(RisikoLP,"Risiko Berdasar LP");
        } else {
            if (tbObat.getSelectedRow() > -1) {
                if (akses.getkode().equals("Admin Utama") ||
                        akses.getkode().equals("dr. Slamet Sugiyanto") ||
                        akses.getkode().equals("dr. Hikmawaty Tahir")) {

                    ganti();
                } else {
                    String depUser = Sequel.cariIsi("select departemen from pegawai where nik=?", akses.getkode());
                    boolean bolehGanti = false;
                    // Bebas Akses 48Jam
                    if ("R01".equals(depUser) || "R023".equals(depUser) || "R07".equals(depUser)) {
                        bolehGanti = true;
                    } else {
                        bolehGanti = Sequel.cekTanggal48jam(getTabelValue("tanggal"),
                                Sequel.ambiltanggalsekarang());
                    }

                    if (bolehGanti) {
                        if (TanggalRegistrasi.getText().equals("")) {
                            TanggalRegistrasi.setText(Sequel.cariIsi(
                                    "select concat(reg_periksa.tgl_registrasi,' ',reg_periksa.jam_reg) from reg_periksa where reg_periksa.no_rawat=?",
                                    TNoRw.getText()));
                        }
                        setTanggalAsuhanMinimalRegistrasi();
                        if (Sequel.cekTanggalRegistrasi(TanggalRegistrasi.getText(),
                                getTanggalJamAsuhan()) == true) {
                            ganti();
                        }
                    }
                }
            } else {
                JOptionPane.showMessageDialog(rootPane, "Silahkan anda pilih data terlebih dahulu..!!");
            }
        }
    }// GEN-LAST:event_BtnEditActionPerformed

    private void BtnEditKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnEditKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnEditActionPerformed(null);
        }else{
            Valid.pindah(evt,BtnHapus,BtnPrint);
        }
    }//GEN-LAST:event_BtnEditKeyPressed

    private void BtnKeluarActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_BtnKeluarActionPerformed
        dispose();
    }// GEN-LAST:event_BtnKeluarActionPerformed

    private void BtnKeluarKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnKeluarKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnKeluarActionPerformed(null);
        }else{
            Valid.pindah(evt,BtnEdit,BtnRiwayatMCU);
        }
    }//GEN-LAST:event_BtnKeluarKeyPressed

    private void BtnRiwayatMCUActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnRiwayatMCUActionPerformed
        if (TNoRw.getText().trim().equals("")) {
            JOptionPane.showMessageDialog(null, "Maaf, silahkan pilih/cari data pasien terlebih dahulu..!!");
        } else {
            tampilkanRiwayat(TNoRw.getText().trim());
        }
    }//GEN-LAST:event_BtnRiwayatMCUActionPerformed

    private void BtnRiwayatMCUKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnRiwayatMCUKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnRiwayatMCUActionPerformed(null);
        }else{
            Valid.pindah(evt,BtnKeluar,TCari);
        }
    }//GEN-LAST:event_BtnRiwayatMCUKeyPressed

    private void BtnPrintActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_BtnPrintActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if (tabMode.getRowCount() == 0) {
            JOptionPane.showMessageDialog(null, "Maaf, data sudah habis. Tidak ada data yang bisa anda print...!!!!");
            BtnBatal.requestFocus();
        } else if (tabMode.getRowCount() != 0) {
            try {
                File g = new File("file2.css");
                BufferedWriter bg = new BufferedWriter(new FileWriter(g));
                bg.write(
                        ".isi td{border-right: 1px solid #e2e7dd;font: 8.5px Arial;height:12px;border-bottom: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"
                                +
                                ".isi2 td{font: 8.5px Arial;border:none;height:12px;background: #ffffff;color:#323232;}"
                                +
                                ".isi3 td{border-right: 1px solid #e2e7dd;font: 8.5px Arial;height:12px;border-top: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"
                                +
                                ".isi4 td{font: 11px Arial;height:12px;border-top: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"
                                +
                                ".isi5 td{font: 8.5px Arial;border:none;height:12px;background: #ffffff;color:#AA0000;}"
                                +
                                ".isi6 td{font: 8.5px Arial;border:none;height:12px;background: #ffffff;color:#FF0000;}"
                                +
                                ".isi7 td{font: 8.5px Arial;border:none;height:12px;background: #ffffff;color:#C8C800;}"
                                +
                                ".isi8 td{font: 8.5px Arial;border:none;height:12px;background: #ffffff;color:#00AA00;}"
                                +
                                ".isi9 td{font: 8.5px Arial;border:none;height:12px;background: #ffffff;color:#969696;}");
                bg.close();

                File f;
                BufferedWriter bw;
                StringBuilder htmlContent;

                String pilihan = (String) JOptionPane.showInputDialog(null, "Silahkan pilih laporan..!",
                        "Pilihan Cetak", JOptionPane.QUESTION_MESSAGE, null,
                        new Object[] { "Laporan 1 (HTML)", "Laporan 2 (WPS)", "Laporan 3 (CSV)", "Laporan 4 (Jasper)", "Laporan 5 (Excel)" },
                        "Laporan 1 (HTML)");
                if (cetakPilihanLaporan(pilihan)) {
                    this.setCursor(Cursor.getDefaultCursor());
                    return;
                }
                switch (pilihan) {
                    case "Laporan 1 (HTML)":
                        if (System.currentTimeMillis() >= 0) {
                            htmlContent = new StringBuilder();
                            htmlContent.append(headerExportHtml()).append(barisExportHtml());
                            LoadHTML.setText(
                                    "<html>" +
                                            "<table width='10000px' border='0' align='center' cellpadding='1px' cellspacing='0' class='tbl_form'>"
                                            +
                                            htmlContent.toString() +
                                            "</table>" +
                                            "</html>");

                            f = new File("DataMCU.wps");
                            bw = new BufferedWriter(new FileWriter(f));
                            bw.write(LoadHTML.getText().replaceAll("<head>", "<head>" +
                                    "<link href=\"file2.css\" rel=\"stylesheet\" type=\"text/css\" />" +
                                    "<table width='10000px' border='0' align='center' cellpadding='3px' cellspacing='0' class='tbl_form'>"
                                    +
                                    "<tr class='isi2'>" +
                                    "<td valign='top' align='center'>" +
                                    "<font size='4' face='Arial'>" + akses.getnamars() + "</font><br>" +
                                    akses.getalamatrs() + ", " + akses.getkabupatenrs() + ", " + akses.getpropinsirs()
                                    + "<br>" +
                                    akses.getkontakrs() + ", E-mail : " + akses.getemailrs() + "<br><br>" +
                                    "<font size='2' face='Arial'>DATA PEMERIKSAAN MCU<br><br></font>" +
                                    "</td>" +
                                    "</tr>" +
                                    "</table>"));
                            bw.close();
                            Desktop.getDesktop().browse(f.toURI());
                            break;
                        }
                        htmlContent = new StringBuilder();
                        htmlContent.append(
                                "<tr class='isi'>").append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>No.Rawat</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>No.RM</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Nama Pasien</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>J.K.</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Tgl.Lahir</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Tanggal</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Informasi</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Riwayat Peyakit Sekarang</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Riwayat Penyakit Keluarga</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Riwayat Penyakit Dahulu</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Alergi Makan & Obat</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Keadaan Umum</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Kesadaran</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>T.D.</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Nadi</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>R.R.</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>T.B.</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>B.B.</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Suhu</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>B.M.I.</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Klasifikasi B.M.I.</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>L.P.</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Risiko Berdasar L.P.</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Submandibula</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Axilla</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Supraklavikula</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Leher</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Inguinal</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Oedema</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Frontalis</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Maxilaris</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Rambut</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Palpebra</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Sklera</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Cornea</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Buta Warna</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Konjungtiva</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Lensa</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Pupil</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Menggunakan Kacamata</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Visus</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Luas Lapang Pandang</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Keterangan Luas Lapang Pandang</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Lubang Telinga</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Daun Telinga</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Selaput Pendengaran</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Proc.Mastoideus</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Septum Nasi</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Lubang Hidung</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Sinus</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Bibir</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Gusi</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Gigi</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Caries</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Lidah</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Faring</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Tonsil</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Kelenjar Limfe</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Kelenjar Gondok</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Gerakan Dada</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Vocal Fremitus</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Perkusi Dada</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Bunyi Napas</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Bunyi Tambahan</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Ictus Cordis</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Bunyi Jantung</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Batas</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Mamae</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Keterangan Mamae</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Inspeksi</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Palpasi</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Hepar</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Perkusi Abdomen</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Auskultasi</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Limpa</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Costovertebral</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Scoliosis</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Kondisi Kulit</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Penyakit Kulit</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Extrimitas Atas</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Keterangan Extrimitas Atas</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Extrimitas Bawah</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Keterangan Extrimitas Bawah</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Area Genitalia</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Keterangan Area Genitalia</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Anus & Perianal</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Keterangan Anus & Perianal</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Pemeriksaan Laboratorium</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Rontgen Thorax</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>EKG</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Spirometri</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Audiometri</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Treadmill</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Romberg Test</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Back Strength</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>ABI Tangan Kanan</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>ABI Tangan Kiri</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>ABI Kaki Kanan</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>ABI Kaki Kiri</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Lain-lain</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Merokok</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Alkohol</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Kesimpulan</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Anjuran</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Kode Dokter</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Nama Dokter Penanggung Jawab</b></td>")
                                .append(
                                        "</tr>");
                        for (i = 0; i < tabMode.getRowCount(); i++) {
                            htmlContent.append(
                                    "<tr class='isi'>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 0).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 1).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 2).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 3).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 4).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 5).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 6).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 7).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 8).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 9).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 10).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 11).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 12).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 13).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 14).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 15).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 16).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 17).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 18).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 19).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 20).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 21).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 22).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 23).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 24).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 25).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 26).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 27).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 28).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 29).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 30).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 31).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 32).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 33).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 34).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 35).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 36).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 37).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 38).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 39).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 40).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 41).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 42).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 43).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 44).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 45).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 46).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 47).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 48).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 49).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 50).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 51).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 52).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 53).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 54).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 55).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 56).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 57).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 58).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 59).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 60).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 61).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 62).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 63).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 64).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 65).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 66).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 67).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 68).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 69).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 70).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 71).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 72).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 73).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 74).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 75).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 76).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 77).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 78).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 79).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 80).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 81).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 82).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 83).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 84).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 85).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 86).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 87).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 88).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 89).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 90).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 91).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 92).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 93).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 94).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 95).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 96).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 97).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 98).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 99).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 100).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 101).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 102).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 103).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 104).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 105).toString()).append("</td>").append(
                                            "</tr>");
                        }
                        LoadHTML.setText(
                                "<html>" +
                                        "<table width='10000px' border='0' align='center' cellpadding='1px' cellspacing='0' class='tbl_form'>"
                                        +
                                        htmlContent.toString() +
                                        "</table>" +
                                        "</html>");

                        f = new File("DataMCU.html");
                        bw = new BufferedWriter(new FileWriter(f));
                        bw.write(LoadHTML.getText().replaceAll("<head>", "<head>" +
                                "<link href=\"file2.css\" rel=\"stylesheet\" type=\"text/css\" />" +
                                "<table width='10000px' border='0' align='center' cellpadding='3px' cellspacing='0' class='tbl_form'>"
                                +
                                "<tr class='isi2'>" +
                                "<td valign='top' align='center'>" +
                                "<font size='4' face='Arial'>" + akses.getnamars() + "</font><br>" +
                                akses.getalamatrs() + ", " + akses.getkabupatenrs() + ", " + akses.getpropinsirs()
                                + "<br>" +
                                akses.getkontakrs() + ", E-mail : " + akses.getemailrs() + "<br><br>" +
                                "<font size='2' face='Arial'>DATA PEMERIKSAAN MCU<br><br></font>" +
                                "</td>" +
                                "</tr>" +
                                "</table>"));
                        bw.close();
                        Desktop.getDesktop().browse(f.toURI());
                        break;
                    case "Laporan 2 (WPS)":
                        htmlContent = new StringBuilder();
                        htmlContent.append(
                                "<tr class='isi'>").append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>No.Rawat</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>No.RM</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Nama Pasien</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>J.K.</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Tgl.Lahir</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Tanggal</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Informasi</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Riwayat Peyakit Sekarang</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Riwayat Penyakit Keluarga</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Riwayat Penyakit Dahulu</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Alergi Makan & Obat</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Keadaan Umum</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Kesadaran</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>T.D.</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Nadi</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>R.R.</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>T.B.</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>B.B.</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Suhu</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>B.M.I.</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Klasifikasi B.M.I.</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>L.P.</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Risiko Berdasar L.P.</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Submandibula</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Axilla</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Supraklavikula</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Leher</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Inguinal</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Oedema</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Frontalis</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Maxilaris</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Rambut</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Palpebra</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Sklera</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Cornea</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Buta Warna</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Konjungtiva</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Lensa</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Pupil</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Menggunakan Kacamata</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Visus</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Luas Lapang Pandang</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Keterangan Luas Lapang Pandang</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Lubang Telinga</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Daun Telinga</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Selaput Pendengaran</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Proc.Mastoideus</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Septum Nasi</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Lubang Hidung</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Sinus</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Bibir</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Gusi</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Gigi</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Caries</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Lidah</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Faring</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Tonsil</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Kelenjar Limfe</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Kelenjar Gondok</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Gerakan Dada</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Vocal Fremitus</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Perkusi Dada</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Bunyi Napas</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Bunyi Tambahan</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Ictus Cordis</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Bunyi Jantung</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Batas</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Mamae</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Keterangan Mamae</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Inspeksi</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Palpasi</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Hepar</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Perkusi Abdomen</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Auskultasi</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Limpa</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Costovertebral</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Scoliosis</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Kondisi Kulit</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Penyakit Kulit</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Extrimitas Atas</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Keterangan Extrimitas Atas</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Extrimitas Bawah</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Keterangan Extrimitas Bawah</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Area Genitalia</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Keterangan Area Genitalia</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Anus & Perianal</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Keterangan Anus & Perianal</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Pemeriksaan Laboratorium</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Rontgen Thorax</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>EKG</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Spirometri</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Audiometri</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Treadmill</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Romberg Test</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Back Strength</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>ABI Tangan Kanan</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>ABI Tangan Kiri</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>ABI Kaki Kanan</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>ABI Kaki Kiri</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Lain-lain</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Merokok</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Alkohol</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Kesimpulan</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Anjuran</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Kode Dokter</b></td>")
                                .append(
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Nama Dokter Penanggung Jawab</b></td>")
                                .append(
                                        "</tr>");
                        for (i = 0; i < tabMode.getRowCount(); i++) {
                            htmlContent.append(
                                    "<tr class='isi'>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 0).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 1).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 2).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 3).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 4).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 5).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 6).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 7).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 8).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 9).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 10).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 11).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 12).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 13).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 14).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 15).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 16).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 17).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 18).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 19).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 20).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 21).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 22).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 23).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 24).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 25).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 26).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 27).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 28).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 29).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 30).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 31).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 32).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 33).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 34).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 35).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 36).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 37).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 38).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 39).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 40).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 41).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 42).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 43).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 44).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 45).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 46).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 47).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 48).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 49).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 50).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 51).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 52).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 53).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 54).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 55).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 56).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 57).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 58).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 59).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 60).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 61).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 62).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 63).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 64).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 65).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 66).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 67).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 68).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 69).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 70).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 71).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 72).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 73).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 74).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 75).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 76).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 77).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 78).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 79).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 80).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 81).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 82).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 83).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 84).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 85).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 86).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 87).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 88).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 89).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 90).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 91).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 92).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 93).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 94).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 95).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 96).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 97).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 98).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 99).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 100).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 101).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 102).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 103).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 104).toString()).append("</td>").append(
                                            "<td valign='top'>")
                                    .append(tbObat.getValueAt(i, 105).toString()).append("</td>").append(
                                            "</tr>");
                        }
                        LoadHTML.setText(
                                "<html>" +
                                        "<table width='10000px' border='0' align='center' cellpadding='1px' cellspacing='0' class='tbl_form'>"
                                        +
                                        htmlContent.toString() +
                                        "</table>" +
                                        "</html>");

                        f = new File("DataMCU.wps");
                        bw = new BufferedWriter(new FileWriter(f));
                        bw.write(LoadHTML.getText().replaceAll("<head>", "<head>" +
                                "<link href=\"file2.css\" rel=\"stylesheet\" type=\"text/css\" />" +
                                "<table width='10000px' border='0' align='center' cellpadding='3px' cellspacing='0' class='tbl_form'>"
                                +
                                "<tr class='isi2'>" +
                                "<td valign='top' align='center'>" +
                                "<font size='4' face='Arial'>" + akses.getnamars() + "</font><br>" +
                                akses.getalamatrs() + ", " + akses.getkabupatenrs() + ", " + akses.getpropinsirs()
                                + "<br>" +
                                akses.getkontakrs() + ", E-mail : " + akses.getemailrs() + "<br><br>" +
                                "<font size='2' face='Arial'>DATA PEMERIKSAAN MCU<br><br></font>" +
                                "</td>" +
                                "</tr>" +
                                "</table>"));
                        bw.close();
                        Desktop.getDesktop().browse(f.toURI());
                        break;
                    case "Laporan 3 (CSV)":
                        if (System.currentTimeMillis() >= 0) {
                            f = new File("DataMCU.csv");
                            bw = new BufferedWriter(new FileWriter(f));
                            bw.write(exportCsv().toString());
                            bw.close();
                            Desktop.getDesktop().browse(f.toURI());
                            break;
                        }
                        htmlContent = new StringBuilder();
                        htmlContent.append(
                                "\"No.Rawat\";\"No.RM\";\"Nama Pasien\";\"J.K.\";\"Tgl.Lahir\";\"Tanggal\";\"Informasi\";\"Riwayat Peyakit Sekarang\";\"Riwayat Penyakit Keluargan\";\"Riwayat Penyakit Dahulu\";\"Alergi Makan & Obat\";\"Keadaan Umum\";\"Kesadaran\";\"T.D.n\";\"Nadin\";\"R.R.\";\"T.B.\";\"B.B.\";\"Suhu\";\"B.M.I.\";\"Klasifikasi B.M.I.\";\"L.P.\";\"Risiko Berdasar L.P.\";\"Submandibula\";\"Axilla\";\"Supraklavikula\";\"Leher\";\"Inguinal\";\"Oedema\";\"Frontalis\";\"Maxilaris\";\"Rambut\";\"Palpebra\";\"Sklera\";\"Cornea\";\"Buta Warna\";\"Konjungtiva\";\"Lensa\";\"Pupil\";\"Menggunakan Kacamata\";\"Visus\";\"Luas Lapang Pandang\";\"Keterangan Luas Lapang Pandang\";\"Lubang Telinga\";\"Daun Telinga\";\"Selaput Pendengaran\";\"Proc.Mastoideus\";\"Septum Nasi\";\"Lubang Hidung\";\"Sinus\";\"Bibir\";\"Gusi\";\"Gigi\";\"Caries\";\"Lidah\";\"Faring\";\"Tonsil\";\"Kelenjar Limfe\";\"Kelenjar Gondok\";\"Gerakan Dada\";\"Vocal Fremitus\";\"Perkusi Dada\";\"Bunyi Napas\";\"Bunyi Tambahan\";\"Ictus Cordis\";\"Bunyi Jantung\";\"Batas\";\"Mamae\";\"Keterangan Mamae\";\"Inspeksi\";\"Palpasi\";\"Hepar\";\"Perkusi Abdomen\";\"Auskultasi\";\"Limpa\";\"Costovertebral\";\"Scoliosis\";\"Kondisi Kulit\";\"Penyakit Kulit\";\"Extrimitas Atas\";\"Keterangan Extrimitas Atas\";\"Extrimitas Bawah\";\"Keterangan Extrimitas Bawah\";\"Area Genitalia\";\"Keterangan Area Genitalia\";\"Anus & Perianal\";\"Keterangan Anus & Perianal\";\"Pemeriksaan Laboratoriumn\";\"Rontgen Thorax\";\"EKG\";\"Spirometri\";\"Audiometri\";\"Treadmill\";\"Romberg Test\";\"Back Strength\";\"ABI Tangan Kanan\";\"ABI Tangan Kiri\";\"ABI Kaki Kanan\";\"ABI Kaki Kiri\";\"Lain-lainn\";\"Merokokn\";\"Alkohol\";\"Kesimpulann\";\"Anjuran\";\"Kode Dokter\";\"Nama Dokter Penanggung Jawab\"\n");
                        for (i = 0; i < tabMode.getRowCount(); i++) {
                            htmlContent.append("\"").append(tbObat.getValueAt(i, 0).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 1).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 2).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 3).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 4).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 5).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 6).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 7).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 8).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 9).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 10).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 11).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 12).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 13).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 14).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 15).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 16).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 17).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 18).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 19).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 20).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 21).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 22).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 23).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 24).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 25).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 26).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 27).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 28).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 29).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 30).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 31).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 32).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 33).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 34).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 35).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 36).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 37).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 38).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 39).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 40).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 41).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 42).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 43).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 44).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 45).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 46).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 47).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 48).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 49).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 50).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 51).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 52).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 53).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 54).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 55).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 56).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 57).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 58).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 59).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 60).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 61).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 62).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 63).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 64).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 65).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 66).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 67).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 68).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 69).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 70).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 71).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 72).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 73).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 74).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 75).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 76).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 77).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 78).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 79).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 80).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 81).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 82).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 83).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 84).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 85).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 86).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 87).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 88).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 89).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 90).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 91).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 92).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 93).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 94).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 95).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 96).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 97).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 98).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 99).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 100).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 101).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 102).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 103).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 104).toString()).append("\";\"")
                                    .append(tbObat.getValueAt(i, 104).toString()).append("\"\n");
                        }
                        f = new File("DataMCU.csv");
                        bw = new BufferedWriter(new FileWriter(f));
                        bw.write(htmlContent.toString());
                        bw.close();
                        Desktop.getDesktop().browse(f.toURI());
                        break;
                }
            } catch (Exception e) {
                System.out.println("Notifikasi : " + e);
            }
        }
        this.setCursor(Cursor.getDefaultCursor());
    }// GEN-LAST:event_BtnPrintActionPerformed

    private void BtnPrintKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnPrintKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnPrintActionPerformed(null);
        }else{
            Valid.pindah(evt,BtnEdit,BtnKeluar);
        }
    }//GEN-LAST:event_BtnPrintKeyPressed

    private void TCariKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TCariKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            BtnCariActionPerformed(null);
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_DOWN){
            BtnCari.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_UP){
            BtnKeluar.requestFocus();
        }
    }//GEN-LAST:event_TCariKeyPressed

    private void BtnCariActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_BtnCariActionPerformed
        runBackground(() -> tampil());
    }// GEN-LAST:event_BtnCariActionPerformed

    private void BtnCariKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnCariKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnCariActionPerformed(null);
        }else{
            Valid.pindah(evt,TCari,BtnAll);
        }
    }//GEN-LAST:event_BtnCariKeyPressed

    private void BtnAllActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_BtnAllActionPerformed
        TCari.setText("");
        runBackground(() -> tampil());
    }// GEN-LAST:event_BtnAllActionPerformed

    private void BtnAllKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnAllKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            TCari.setText("");
            runBackground(() -> tampil());
        }else{
            Valid.pindah(evt,BtnCari,surname);
        }
    }//GEN-LAST:event_BtnAllKeyPressed

    private void tbObatMouseClicked(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_tbObatMouseClicked
        if (tabMode.getRowCount() != 0) {
            try {
                getData();
            } catch (java.lang.NullPointerException e) {
            }
            if ((evt.getClickCount() == 2) && (tbObat.getSelectedColumn() == 0)) {
                TabRawat.setSelectedIndex(0);
            }
        }
    }// GEN-LAST:event_tbObatMouseClicked

    private void tbObatKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbObatKeyPressed
        if(tabMode.getRowCount()!=0){
            if((evt.getKeyCode()==KeyEvent.VK_ENTER)||(evt.getKeyCode()==KeyEvent.VK_UP)
                    ||(evt.getKeyCode()==KeyEvent.VK_DOWN)){
                try {
                    getData();
                } catch (java.lang.NullPointerException e) {
                }
            }else if(evt.getKeyCode()==KeyEvent.VK_SPACE){
                try {
                    getData();
                    TabRawat.setSelectedIndex(0);
                } catch (java.lang.NullPointerException e) {
                }
            }
        }
    }//GEN-LAST:event_tbObatKeyPressed

    private void formWindowOpened(java.awt.event.WindowEvent evt) {// GEN-FIRST:event_formWindowOpened
        if (koneksiDB.CARICEPAT().equals("aktif")) {
            TCari.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    if (TCari.getText().length() > 2) {
                        runBackground(() -> tampil());
                    }
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    if (TCari.getText().length() > 2) {
                        runBackground(() -> tampil());
                    }
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    if (TCari.getText().length() > 2) {
                        runBackground(() -> tampil());
                    }
                }
            });
        }
    }// GEN-LAST:event_formWindowOpened

    private void MnPenilaianMCUActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_MnPenilaianMCUActionPerformed
        cetakJasperPenilaianMcuTerpilih();
    }// GEN-LAST:event_MnPenilaianMCUActionPerformed

    private void ConcRadiologiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_ConcRadiologiKeyPressed
        Valid.pindah(evt,PhyExam,ConcEcg);
    }//GEN-LAST:event_ConcRadiologiKeyPressed

    private void BtnPetugasLabActionPerformed(java.awt.event.ActionEvent evt) {
        if (petugas == null || !petugas.isDisplayable()) {
            petugas = new DlgCariPetugas(null, false);
            petugas.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            petugas.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    if (petugas.getTable().getSelectedRow() != -1) {
                        KdPetugasLab.setText(
                                petugas.getTable().getValueAt(petugas.getTable().getSelectedRow(), 0).toString());
                        NmPetugasLab.setText(
                                petugas.getTable().getValueAt(petugas.getTable().getSelectedRow(), 1).toString());
                    }
                    BtnPetugasLab.requestFocus();
                    petugas = null;
                }
            });
            petugas.setSize(internalFrame1.getWidth() - 20, internalFrame1.getHeight() - 20);
            petugas.setLocationRelativeTo(internalFrame1);
        }
        if (petugas == null)
            return;
        if (!petugas.isVisible()) {
            petugas.isCek();
            petugas.emptTeks();
        }
        if (petugas.isVisible()) {
            petugas.toFront();
        }
        petugas.setVisible(true);
    }

    private void BtnPetugasActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_BtnPetugasActionPerformed
        if (petugas == null || !petugas.isDisplayable()) {
            petugas = new DlgCariPetugas(null, false);
            petugas.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            petugas.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    if (petugas.getTable().getSelectedRow() != -1) {
                        KdPetugas.setText(
                                petugas.getTable().getValueAt(petugas.getTable().getSelectedRow(), 0).toString());
                        NmPetugas.setText(
                                petugas.getTable().getValueAt(petugas.getTable().getSelectedRow(), 1).toString());
                    }
                    BtnPetugas.requestFocus();
                    petugas = null;
                }
            });

            petugas.setSize(internalFrame1.getWidth() - 20, internalFrame1.getHeight() - 20);
            petugas.setLocationRelativeTo(internalFrame1);
        }
        if (petugas == null)
            return;
        if (!petugas.isVisible()) {
            petugas.isCek();
            petugas.emptTeks();
        }
        if (petugas.isVisible()) {
            petugas.toFront();
            return;
        }
        petugas.setVisible(true);
    }// GEN-LAST:event_BtnPetugasActionPerformed

    private void TBKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TBKeyPressed
        Valid.pindah(evt,RR,BB);
    }//GEN-LAST:event_TBKeyPressed

    private void RRKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_RRKeyPressed
        Valid.pindah(evt,Nadi,TB);
    }//GEN-LAST:event_RRKeyPressed

    private void NadiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_NadiKeyPressed
        Valid.pindah(evt,TD,RR);
    }//GEN-LAST:event_NadiKeyPressed

    private void RWP29ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_RWP29ActionPerformed
        // TODO add your handling code here:
    }// GEN-LAST:event_RWP29ActionPerformed

    private void RWP25ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_RWP25ActionPerformed
        // TODO add your handling code here:
    }// GEN-LAST:event_RWP25ActionPerformed

    private void RWP28ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_RWP28ActionPerformed
        // TODO add your handling code here:
    }// GEN-LAST:event_RWP28ActionPerformed

    private void OtherJobKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_OtherJobKeyPressed
        Valid.pindah(evt,Hobby,PosisiKerja);
    }//GEN-LAST:event_OtherJobKeyPressed

    private void HobbyKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_HobbyKeyPressed
        Valid.pindah(evt,Activities,OtherJob);
    }//GEN-LAST:event_HobbyKeyPressed

    private void ActivitiesKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_ActivitiesKeyPressed
        Valid.pindah(evt,Yoe,Hobby);
    }//GEN-LAST:event_ActivitiesKeyPressed

    private void JobTitleKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_JobTitleKeyPressed
        Valid.pindah(evt,NIP,Doe);
    }//GEN-LAST:event_JobTitleKeyPressed

    private void YoeKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_YoeKeyPressed
        Valid.pindah(evt,Doe,Activities);
    }//GEN-LAST:event_YoeKeyPressed

    private void DoeKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_DoeKeyPressed
        Valid.pindah(evt,JobTitle,Yoe);
    }//GEN-LAST:event_DoeKeyPressed

    private void NoTlpKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_NoTlpKeyPressed
        Valid.pindah(evt,SttsNikah,Perusahaan);
    }//GEN-LAST:event_NoTlpKeyPressed

    private void BtnDokter2ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_BtnDokter2ActionPerformed
        if (TNoRw.getText().equals("") && TNoRM.getText().equals("")) {
            JOptionPane.showMessageDialog(null, "Pasien masih kosong...!!!");
        } else {
            RMCariHasilRadiologi cariradiologi = new RMCariHasilRadiologi(null, false);
            cariradiologi.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    if (cariradiologi.getTable().getSelectedRow() != -1) {
                        RongsenThorax.append(cariradiologi.getTable()
                                .getValueAt(cariradiologi.getTable().getSelectedRow(), 2).toString() + ", ");
                        RongsenThorax.requestFocus();
                    }
                }
            });
            cariradiologi.setNoRawat(TNoRw.getText());
            cariradiologi.tampil();
            cariradiologi.setSize(internalFrame1.getWidth() - 20, internalFrame1.getHeight() - 20);
            cariradiologi.setLocationRelativeTo(internalFrame1);
            cariradiologi.setVisible(true);
        }
    }// GEN-LAST:event_BtnDokter2ActionPerformed

    private void BtnDokter3ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_BtnDokter3ActionPerformed
        if (TNoRw.getText().equals("") && TNoRM.getText().equals("")) {
            JOptionPane.showMessageDialog(null, "Pasien masih kosong...!!!");
        } else {
            RMCariHasilLaborat carilaborat = new RMCariHasilLaborat(null, false);
            carilaborat.getTable().addKeyListener(new KeyListener() {
                @Override
                public void keyTyped(KeyEvent e) {
                }

                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                        if (carilaborat.getTable().getSelectedRow() != -1) {
                            PemeriksaanLaboratorium.append(carilaborat.getTable()
                                    .getValueAt(carilaborat.getTable().getSelectedRow(), 3).toString() + ", ");
                            PemeriksaanLaboratorium.requestFocus();
                        }
                    }
                }

                @Override
                public void keyReleased(KeyEvent e) {
                }
            });

            carilaborat.BtnKeluar.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    for (i = 0; i < carilaborat.getTable().getRowCount(); i++) {
                        if (carilaborat.getTable().getValueAt(i, 0).toString().equals("true")) {
                            PemeriksaanLaboratorium.append(carilaborat.getTable().getValueAt(i, 3).toString() + ", ");
                        }
                    }
                    PemeriksaanLaboratorium.requestFocus();
                }
            });
            carilaborat.setNoRawat(TNoRw.getText());
            carilaborat.tampil();
            carilaborat.setSize(internalFrame1.getWidth() - 20, internalFrame1.getHeight() - 20);
            carilaborat.setLocationRelativeTo(internalFrame1);
            carilaborat.setVisible(true);
        }
    }// GEN-LAST:event_BtnDokter3ActionPerformed

    private void specifyKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_specifyKeyPressed
        Valid.pindah2(evt,fit_with_restrictions,BtnSimpan);
    }//GEN-LAST:event_specifyKeyPressed

    private void BtnDokterActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_BtnDokterActionPerformed
        if (dokter == null || !dokter.isDisplayable()) {
            dokter = new DlgCariDokter(null, false);
            dokter.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            dokter.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    if (dokter.getTable().getSelectedRow() != -1) {
                        KdDokter1.setText(
                                dokter.getTable().getValueAt(dokter.getTable().getSelectedRow(), 0).toString());
                        NmDokter.setText(
                                dokter.getTable().getValueAt(dokter.getTable().getSelectedRow(), 1).toString());
                    }
                    BtnDokter.requestFocus();
                    dokter = null;
                }
            });
            dokter.setSize(internalFrame1.getWidth() - 20, internalFrame1.getHeight() - 20);
            dokter.setLocationRelativeTo(internalFrame1);
        }
        if (dokter == null)
            return;
        dokter.isCek();
        if (dokter.isVisible()) {
            dokter.toFront();
            return;
        }
        dokter.setVisible(true);
    }// GEN-LAST:event_BtnDokterActionPerformed

    private void TNoRwKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TNoRwKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_PAGE_DOWN){
            isRawat();
        }else{
            Valid.pindah(evt,TCari,TPasien);
        }
    }//GEN-LAST:event_TNoRwKeyPressed

    private void eye_unaided_distant_lKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_eye_unaided_distant_lKeyPressed
        Valid.pindah(evt,BB,eye_unaided_distant_r);
    }//GEN-LAST:event_eye_unaided_distant_lKeyPressed

    private void eye_unaided_distant_rKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_eye_unaided_distant_rKeyPressed
        Valid.pindah(evt,eye_unaided_distant_l,eye_glasses_distant_l);
    }//GEN-LAST:event_eye_unaided_distant_rKeyPressed

    private void BtnPTActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_BtnPTActionPerformed
        if (perusahaan == null || !perusahaan.isDisplayable()) {
            perusahaan = new DlgCariPerusahaan(null, false);
            perusahaan.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            perusahaan.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    if (perusahaan.getTable().getSelectedRow() != -1) {
                        Perusahaan.setText(
                                perusahaan.getTable().getValueAt(perusahaan.getTable().getSelectedRow(), 1).toString());
                    }
                    Perusahaan.requestFocus();
                    perusahaan = null;
                }
            });
            perusahaan.setSize(internalFrame1.getWidth() - 20, internalFrame1.getHeight() - 20);
            perusahaan.setLocationRelativeTo(internalFrame1);
        }

        if (perusahaan == null)
            return;
        if (!perusahaan.isVisible()) {
            perusahaan.emptTeks();
            perusahaan.isCek();
        }
        if (perusahaan.isVisible()) {
            perusahaan.toFront();
            return;
        }
        perusahaan.setVisible(true);
    }// GEN-LAST:event_BtnPTActionPerformed

    private void eye_glasses_distant_lKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_eye_glasses_distant_lKeyPressed
        Valid.pindah(evt,eye_unaided_distant_r,eye_glasses_distant_r);
    }//GEN-LAST:event_eye_glasses_distant_lKeyPressed

    private void eye_unaided_near_lKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_eye_unaided_near_lKeyPressed
        Valid.pindah(evt,eye_night_vision_2,eye_unaided_near_r);
    }//GEN-LAST:event_eye_unaided_near_lKeyPressed

    private void eye_glasses_distant_rKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_eye_glasses_distant_rKeyPressed
        Valid.pindah(evt,eye_glasses_distant_l,eye_night_vision_1);
    }//GEN-LAST:event_eye_glasses_distant_rKeyPressed

    private void eye_unaided_near_rKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_eye_unaided_near_rKeyPressed
        Valid.pindah(evt,eye_unaided_near_l,eye_glasses_near_l);
    }//GEN-LAST:event_eye_unaided_near_rKeyPressed

    private void eye_glasses_near_lKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_eye_glasses_near_lKeyPressed
        Valid.pindah(evt,eye_unaided_near_r,eye_glasses_near_r);
    }//GEN-LAST:event_eye_glasses_near_lKeyPressed

    private void eye_night_vision_1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_eye_night_vision_1KeyPressed
        Valid.pindah(evt,eye_glasses_distant_r,eye_night_vision_2);
    }//GEN-LAST:event_eye_night_vision_1KeyPressed

    private void eye_unaided_near_r1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_eye_unaided_near_r1KeyPressed
        Valid.pindah(evt,eye_glasses_near_r,eye_brake_test_2);
    }//GEN-LAST:event_eye_unaided_near_r1KeyPressed

    private void eye_glasses_near_rKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_eye_glasses_near_rKeyPressed
        Valid.pindah(evt,eye_glasses_near_l,eye_unaided_near_r1);
    }//GEN-LAST:event_eye_glasses_near_rKeyPressed

    private void eye_night_vision_2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_eye_night_vision_2KeyPressed
        Valid.pindah(evt,eye_night_vision_1,eye_unaided_near_l);
    }//GEN-LAST:event_eye_night_vision_2KeyPressed

    private void eye_brake_test_2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_eye_brake_test_2KeyPressed
        Valid.pindah(evt,eye_unaided_near_r1,eye_color_blindless);
    }//GEN-LAST:event_eye_brake_test_2KeyPressed

    private void managerKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_managerKeyPressed
        Valid.pindah(evt,supervisor,conclusion_requires_spectacles);
    }//GEN-LAST:event_managerKeyPressed

    private void departemenKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_departemenKeyPressed
        Valid.pindah(evt,saran,supervisor);
    }//GEN-LAST:event_departemenKeyPressed

    private void supervisorKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_supervisorKeyPressed
        Valid.pindah(evt,departemen,manager);
    }//GEN-LAST:event_supervisorKeyPressed

    private void managerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_managerActionPerformed
        if (manager.isSelected()) {
            departemen.setSelected(false);
            supervisor.setSelected(false);
        }
    }//GEN-LAST:event_managerActionPerformed

    private void departemenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_departemenActionPerformed
        if (departemen.isSelected()) {
            supervisor.setSelected(false);
            manager.setSelected(false);
        }
    }//GEN-LAST:event_departemenActionPerformed

    private void supervisorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_supervisorActionPerformed
        if (supervisor.isSelected()) {
            departemen.setSelected(false);
            manager.setSelected(false);
        }
    }//GEN-LAST:event_supervisorActionPerformed

    private void MnCopyPengkajianActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnCopyPengkajianActionPerformed
        if (tabMode.getRowCount() == 0) {
            JOptionPane.showMessageDialog(null, "Maaf, data pengkajian MCU kosong...!!!!");
        } else if (tbObat.getSelectedRow() == -1) {
            JOptionPane.showMessageDialog(null,
                    "Maaf, silahkan anda pilih dulu data pengkajian yang mau dicopy...!!!");
        } else {
            modeCopyPengkajian = true;
            barisSumberCopyPengkajian = tbObat.getSelectedRow();
            tampilkanPopupCopyPengkajian();
        }
    }//GEN-LAST:event_MnCopyPengkajianActionPerformed

    private void btn_AmbilDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_AmbilDataActionPerformed
        if (TNoRM.getText().trim().equals("")) {
            JOptionPane.showMessageDialog(null, "Maaf, silahkan pilih/cari data pasien terlebih dahulu..!!");
        } else {
            ambilDataPengkajianTerakhir();
        }
    }//GEN-LAST:event_btn_AmbilDataActionPerformed

    /**
     * Mencari data pengkajian MCU terakhir (berdasar tanggal) milik No. Rekam Medis pasien yang
     * sedang dibuka (dari No. Rawat lain, bukan No. Rawat yang sedang dibuka saat ini), lalu
     * mengisikannya ke form ini. Memakai mekanisme yang sama dengan "Copy Pengkajian" (tampil()
     * + isiDataPengkajian()); bedanya sumber dicari otomatis berdasar No. RM, tidak perlu
     * dipilih manual dari daftar.
     */
    private void ambilDataPengkajianTerakhir() {
        String noRM = TNoRM.getText().trim();
        String noRawatSaatIni = TNoRw.getText().trim();
        String noRawatSumber = "";
        try {
            java.sql.PreparedStatement psCari = koneksi.prepareStatement(
                    "select no_rawat from penilaian_mcu where no_rkm_medis=? and no_rawat<>? order by tanggal desc limit 1");
            try {
                psCari.setString(1, noRM);
                psCari.setString(2, noRawatSaatIni);
                java.sql.ResultSet rsCari = psCari.executeQuery();
                try {
                    if (rsCari.next()) {
                        noRawatSumber = rsCari.getString("no_rawat");
                    }
                } finally {
                    rsCari.close();
                }
            } finally {
                psCari.close();
            }
        } catch (Exception e) {
            System.out.println("Notif : " + e);
        }

        if (noRawatSumber.equals("")) {
            JOptionPane.showMessageDialog(null,
                    "Maaf, belum ada data pengkajian MCU sebelumnya untuk No. RM " + noRM + "..!!");
            return;
        }

        int reply = JOptionPane.showConfirmDialog(rootPane,
                "Ambil data pengkajian MCU terakhir milik No. RM " + noRM + " (No. Rawat sumber: "
                        + noRawatSumber + ") ke No. Rawat " + noRawatSaatIni + " ini..??",
                "Konfirmasi Ambil Data", JOptionPane.YES_NO_OPTION);
        if (reply != JOptionPane.YES_OPTION) {
            return;
        }

        String tCariAsal = TCari.getText();
        Date dtCari1Asal = DTPCari1.getDate();
        Date dtCari2Asal = DTPCari2.getDate();
        try {
            TCari.setText(noRawatSumber);
            DTPCari1.setDate(new Date(0));
            DTPCari2.setDate(new Date());
            tampil();

            int baris = -1;
            for (int i = 0; i < tbObat.getRowCount(); i++) {
                if (noRawatSumber.equals(getTabelValue("no_rawat", i))) {
                    baris = i;
                    break;
                }
            }
            if (baris < 0) {
                JOptionPane.showMessageDialog(null,
                        "Maaf, data sumber tidak ditemukan lagi (daftar berubah)..!!");
                return;
            }
            isiDataPengkajian(baris);
            setTanggalAsuhanSamaDenganRegistrasi();
            isiPetugasLogin();
            tbObat.clearSelection();
            JOptionPane.showMessageDialog(null,
                    "Data pengkajian MCU terakhir berhasil diambil. Silahkan periksa kembali datanya lalu klik Simpan..!!");
        } finally {
            TCari.setText(tCariAsal);
            DTPCari1.setDate(dtCari1Asal);
            DTPCari2.setDate(dtCari2Asal);
            tampil();
        }
    }

    private void BtnCloseIn6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCloseIn6ActionPerformed
        modeCopyPengkajian = false;
        barisSumberCopyPengkajian = -1;
        WindowGabungMCU.dispose();
    }//GEN-LAST:event_BtnCloseIn6ActionPerformed

    private void BtnSimpan6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSimpan6ActionPerformed
        String noRawatSumber = getTabelValue("no_rawat", barisSumberCopyPengkajian);
        if (NoRmTujuan.getText().trim().equals("")) {
            Valid.textKosong(NoRmTujuan, "No. Rawat Tujuan");
            return;
        }
        if (NmPasienTujuan.getText().trim().equals("")) {
            cariNamaPasienTujuanCopy();
        }
        if (NmPasienTujuan.getText().trim().equals("")) {
            return;
        }
        String noRawatTujuan = NoRmTujuan.getText().trim();
        if (noRawatTujuan.equals(noRawatSumber)) {
            JOptionPane.showMessageDialog(rootPane,
                    "Maaf, No. Rawat tujuan tidak boleh sama dengan No. Rawat sumber..!!");
            return;
        }
        WindowGabungMCU.dispose();
        TNoRw.setText(noRawatTujuan);
        isRawat();
    }//GEN-LAST:event_BtnSimpan6ActionPerformed

    private void NoRmTujuanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_NoRmTujuanKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            cariNamaPasienTujuanCopy();
            NoRmTujuan.requestFocus();
        }
    }//GEN-LAST:event_NoRmTujuanKeyPressed

    private void BtnCari1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCari1ActionPerformed
        cariNamaPasienTujuanCopy();
        NoRmTujuan.requestFocus();
    }//GEN-LAST:event_BtnCari1ActionPerformed

    /**
     * Mencari & menampilkan nama pasien pemilik No. Rawat tujuan yang diketik pada
     * {@code NoRmTujuan} (dialog {@code WindowGabungMCU}), gaya sama seperti validasi
     * No.R.M tujuan pada fitur Gabungkan Data RM di {@code DlgPasien}.
     */
    private void cariNamaPasienTujuanCopy() {
        if (NoRmTujuan.getText().trim().equals("")) {
            NmPasienTujuan.setText("");
            return;
        }
        Sequel.cariIsi(
                "select pasien.nm_pasien from reg_periksa inner join pasien on "
                        + "reg_periksa.no_rkm_medis=pasien.no_rkm_medis where reg_periksa.no_rawat=?",
                NmPasienTujuan, NoRmTujuan.getText().trim());
        if (NmPasienTujuan.getText().trim().equals("")) {
            JOptionPane.showMessageDialog(rootPane, "Maaf, No. Rawat tujuan tidak ditemukan..!!");
        }
    }

    /**
     * Menampilkan popup {@code WindowGabungMCU} untuk mengetikkan No. Rawat tujuan tempat
     * data pengkajian (baris yang dipilih lewat menu "Copy Data") akan ditempelkan.
     */
    private void tampilkanPopupCopyPengkajian() {
        String noRawatSumber = getTabelValue("no_rawat", barisSumberCopyPengkajian);
        String namaPasienSumber = getTabelValue("nama_pasien", barisSumberCopyPengkajian);

        internalFrame8.setBorder(javax.swing.BorderFactory.createTitledBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(230, 235, 225)),
                "::[ Copy Dari " + noRawatSumber + " - " + namaPasienSumber + " ]::",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 12),
                new java.awt.Color(50, 50, 50)));

        NoRmTujuan.setText("");
        NmPasienTujuan.setText("");
        WindowGabungMCU.setSize(430, 130);
        WindowGabungMCU.setLocationRelativeTo(internalFrame1);
        NoRmTujuan.requestFocus();
        WindowGabungMCU.setVisible(true);
    }

    /**
     * Menampilkan riwayat pengkajian MCU pasien pemilik No. Rawat yang diberikan, dipanggil
     * dari DlgMCU (tombol Riwayat MCU pada daftar pasien) sehingga tidak perlu lagi membuka
     * tab Input Pengkajian terlebih dahulu.
     */
    public void tampilkanRiwayat(String noRawat) {
        String noRM = Sequel.cariIsi("select no_rkm_medis from reg_periksa where no_rawat=?", noRawat).trim();
        if (noRM.equals("")) {
            JOptionPane.showMessageDialog(null, "Maaf, No.RM pasien tidak ditemukan...!!!");
            return;
        }
        TCari.setText(noRM);
        DTPCari1.setDate(new Date(0));
        DTPCari2.setDate(new Date());
        tampil();
        TabRawat.setSelectedIndex(1);
    }

    private void cetakJasperPenilaianMcuTerpilih() {
        if (tbObat.getSelectedRow() > -1) {
            Map<String, Object> param = new HashMap<>();
            param.put("namars", akses.getnamars());
            param.put("alamatrs", akses.getalamatrs());
            param.put("kotars", akses.getkabupatenrs());
            param.put("propinsirs", akses.getpropinsirs());
            param.put("kontakrs", akses.getkontakrs());
            param.put("emailrs", akses.getemailrs());
            java.io.ByteArrayInputStream logoStream = Sequel.cariGambar("select setting.logo from setting");
            byte[] logoBytes = null;
            if (logoStream != null) {
                try {
                    logoBytes = new byte[logoStream.available()];
                    logoStream.read(logoBytes);
                } catch (Exception e) {
                    // fall back to stream if reading fails
                }
            }
            param.put("logo", logoBytes != null ? logoBytes : logoStream);
            param.put("foto_pasien", getFotoPasien(getTabelValue("no_rawat")));
            String kdDokter = getTabelValue("kd_dokter");
            String nmDokter = getTabelValue("nm_dokter");
            String tanggal = getTabelValue("tanggal");
            String nmPetugas = getTabelValue("nm_petugas");
            if (nmPetugas.equals("")) {
                nmPetugas = Sequel.cariIsi("select ifnull(petugas.nama,'') from penilaian_mcu " +
                        "left join petugas on penilaian_mcu.kd_petugas=petugas.nip " +
                        "where penilaian_mcu.no_rawat=?", getTabelValue("no_rawat"));
            }
            String finger = Sequel.cariIsi(
                    "select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",
                    kdDokter);
            param.put("finger",
                    "Dikeluarkan di " + akses.getnamars() + ", Kabupaten/Kota " + akses.getkabupatenrs()
                            + "\nDitandatangani secara elektronik oleh " + nmDokter + "\nID "
                            + (finger.equals("") ? kdDokter : finger) + "\n" + Valid.SetTgl3(tanggal));
            param.put("petugas_penginput", nmPetugas);
            Valid.MyReportqry("RSPKrptCetakPenilaianAwalMCU.jasper", "report", "::[ Laporan Pengkajian Awal MCU ]::",
                    queryCetakPenilaianMcu(getTabelValue("no_rawat")), param);
        } else {
            JOptionPane.showMessageDialog(rootPane, "Silahkan anda pilih data terlebih dahulu..!!");
        }
    }

    private String queryCetakPenilaianMcu(String noRawat) {
        return selectPenilaianMcu() + "where penilaian_mcu.no_rawat='" + escapeSql(noRawat) + "'";
    }

    private String escapeSql(String nilai) {
        return nilai == null ? "" : nilai.replace("\\", "\\\\").replace("'", "\\'");
    }

    private java.awt.Image getFotoPasien(String noRawat) {
        String namaFoto = Sequel.cariIsi(
                "select ifnull(personal_pasien.gambar,'') from reg_periksa " +
                        "inner join personal_pasien on personal_pasien.no_rkm_medis=reg_periksa.no_rkm_medis " +
                        "where reg_periksa.no_rawat=?", noRawat);
        if (namaFoto.equals("") || namaFoto.equals("-")) {
            return null;
        }
        try {
            return javax.imageio.ImageIO.read(new java.net.URL("http://" + koneksiDB.HOSTHYBRIDWEB() + ":"
                    + koneksiDB.PORTWEB() + "/" + koneksiDB.HYBRIDWEB() + "/photopasien/" + namaFoto));
        } catch (Exception e) {
            System.out.println("Notif Foto Pasien MCU : " + e);
            return null;
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            RMMCURSPK dialog = new RMMCURSPK(new javax.swing.JFrame(), true);
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
    private widget.TextBox Activities;
    private widget.TextBox AlcoholGrWeek;
    private widget.TextBox AnyAllergies;
    private widget.TextBox BB;
    private widget.Button BtnAll;
    private widget.Button BtnBatal;
    private widget.Button BtnCari;
    private widget.Button BtnCari1;
    private widget.Button BtnCloseIn6;
    private widget.Button BtnDokter;
    private widget.Button BtnDokter2;
    private widget.Button BtnDokter3;
    private widget.Button BtnEdit;
    private widget.Button BtnHapus;
    private widget.Button BtnKeluar;
    private widget.Button BtnPT;
    private widget.Button BtnPetugas;
    private widget.Button BtnPetugasLab;
    private widget.Button BtnPrint;
    private widget.Button BtnRiwayatMCU;
    private widget.Button BtnSimpan;
    private widget.Button BtnSimpan6;
    private widget.TextBox CigarettesPerday;
    private widget.TextBox ConcAudiometry;
    private widget.TextBox ConcEcg;
    private widget.TextBox ConcRadiologi;
    private widget.TextBox ConcSpirometry;
    private widget.Tanggal DTPCari1;
    private widget.Tanggal DTPCari2;
    private widget.TextArea Dass21;
    private widget.TextBox Doe;
    private widget.TextBox FamilyHistoryFather;
    private widget.TextBox FamilyHistoryMother;
    private widget.TextBox FamilyHistoryOther;
    private widget.TextBox FamilyHistorySiblings;
    private widget.PanelBiasa FormInput;
    private widget.TextBox Hobby;
    private widget.TextBox ILO_clasification;
    private widget.TextBox ILO_clasification2;
    private widget.TextBox IMT;
    private widget.ComboBox Jk;
    private widget.CekBox JobInvolvesCateringStaffIncludingFoodHandlers;
    private widget.CekBox JobInvolvesClericalOfficeBasedOrAdministrative;
    private widget.CekBox JobInvolvesDrivingOrOperatingMobileEquipment;
    private widget.CekBox JobInvolvesExposingToOtherPotentialDangerous;
    private widget.CekBox JobInvolvesPotentialDustExposure;
    private widget.CekBox JobInvolvesRequiresColourVision;
    private widget.CekBox JobInvolvesWorkingAtHeights;
    private widget.TextBox JobTitle;
    private widget.TextBox KdDokter1;
    private widget.TextBox KdPetugas;
    private widget.TextBox KdPetugasLab;
    private widget.TextBox KlasifikasiIMT1;
    private widget.Label LCount;
    private widget.editorpane LoadHTML;
    private widget.ComboBox McuGroup;
    private javax.swing.JMenuItem MnCopyPengkajian;
    private javax.swing.JMenuItem MnPenilaianMCU;
    private widget.TextBox NIP;
    private widget.TextBox Nadi;
    private widget.TextBox NmDokter;
    private widget.TextBox NmPasienTujuan;
    private widget.TextBox NmPetugas;
    private widget.TextBox NmPetugasLab;
    private widget.TextBox NoRmTujuan;
    private widget.TextBox NoTlp;
    private widget.TextBox OtherJob;
    private widget.TextArea PemeriksaanLaboratorium;
    private widget.TextBox Perusahaan;
    private widget.TextArea PhyExam;
    private widget.ComboBox PosisiKerja;
    private widget.TextBox PrescribedMedication;
    private widget.TextBox PrescribedMedication2;
    private widget.TextBox RR;
    private widget.CekBox RWP1;
    private widget.CekBox RWP10;
    private widget.CekBox RWP11;
    private widget.CekBox RWP12;
    private widget.CekBox RWP13;
    private widget.CekBox RWP14;
    private widget.CekBox RWP15;
    private widget.CekBox RWP16;
    private widget.CekBox RWP17;
    private widget.CekBox RWP18;
    private widget.CekBox RWP19;
    private widget.CekBox RWP2;
    private widget.CekBox RWP20;
    private widget.CekBox RWP21;
    private widget.CekBox RWP22;
    private widget.CekBox RWP23;
    private widget.CekBox RWP24;
    private widget.CekBox RWP25;
    private widget.CekBox RWP26;
    private widget.CekBox RWP27;
    private widget.CekBox RWP28;
    private widget.CekBox RWP29;
    private widget.CekBox RWP3;
    private widget.CekBox RWP30;
    private widget.CekBox RWP4;
    private widget.CekBox RWP5;
    private widget.CekBox RWP6;
    private widget.CekBox RWP7;
    private widget.CekBox RWP8;
    private widget.CekBox RWP9;
    private widget.TextArea RongsenThorax;
    private widget.ScrollPane Scroll;
    private widget.ComboBox SttsNikah;
    private widget.TextBox SukuBangsa;
    private widget.TextBox TB;
    private widget.TextBox TCari;
    private widget.TextBox TD;
    private widget.TextBox TNoRM;
    private widget.TextBox TNoRw;
    private widget.TextBox TPasien;
    private javax.swing.JTabbedPane TabRawat;
    private widget.TextBox TanggalRegistrasi;
    private widget.Tanggal TglAsuhan;
    private widget.Tanggal TglLahir;
    private widget.TextBox TmpLahir;
    private javax.swing.JDialog WindowGabungMCU;
    private widget.TextBox Year;
    private widget.TextBox Yoe;
    private widget.TextBox afp;
    private widget.TextBox anti_hbs;
    private widget.CekBox audiometri_ear_protection_worn_always;
    private widget.CekBox audiometri_ear_protection_worn_never;
    private widget.CekBox audiometri_ear_protection_worn_often;
    private widget.CekBox audiometri_ear_protection_worn_previously;
    private widget.CekBox audiometri_ear_protection_worn_rarely;
    private widget.TextBox audiometri_left_ear_1000;
    private widget.TextBox audiometri_left_ear_1001;
    private widget.TextBox audiometri_left_ear_1002;
    private widget.TextBox audiometri_left_ear_1003;
    private widget.TextBox audiometri_left_ear_1500;
    private widget.TextBox audiometri_left_ear_1501;
    private widget.TextBox audiometri_left_ear_1502;
    private widget.TextBox audiometri_left_ear_1503;
    private widget.TextBox audiometri_left_ear_2000;
    private widget.TextBox audiometri_left_ear_2001;
    private widget.TextBox audiometri_left_ear_2002;
    private widget.TextBox audiometri_left_ear_2003;
    private widget.TextBox audiometri_left_ear_3000;
    private widget.TextBox audiometri_left_ear_3001;
    private widget.TextBox audiometri_left_ear_3002;
    private widget.TextBox audiometri_left_ear_3003;
    private widget.TextBox audiometri_left_ear_4000;
    private widget.TextBox audiometri_left_ear_4001;
    private widget.TextBox audiometri_left_ear_4002;
    private widget.TextBox audiometri_left_ear_4003;
    private widget.TextBox audiometri_left_ear_500;
    private widget.TextBox audiometri_left_ear_5000;
    private widget.TextBox audiometri_left_ear_5001;
    private widget.TextBox audiometri_left_ear_5002;
    private widget.TextBox audiometri_left_ear_5003;
    private widget.TextBox audiometri_left_ear_501;
    private widget.TextBox audiometri_left_ear_502;
    private widget.TextBox audiometri_left_ear_503;
    private widget.TextBox audiometri_left_ear_6000;
    private widget.TextBox audiometri_left_ear_6001;
    private widget.TextBox audiometri_left_ear_6002;
    private widget.TextBox audiometri_left_ear_6003;
    private widget.CekBox audiometri_tinitus_always;
    private widget.CekBox audiometri_tinitus_never;
    private widget.CekBox audiometri_tinitus_often;
    private widget.CekBox audiometri_tinitus_previously;
    private widget.CekBox audiometri_tinitus_rarely;
    private widget.TextBox bilirubin;
    private widget.TextBox bl_group;
    private widget.TextBox blood;
    private widget.ComboBox blood_group;
    private widget.Button btn_AmbilData;
    private widget.TextArea catatan;
    private widget.ComboBox cbConcEcg;
    private widget.TextBox cea;
    private widget.ComboBox conclusion_colour_blindness;
    private widget.ComboBox conclusion_impaired_hearing;
    private widget.ComboBox conclusion_requires_spectacles;
    private widget.ComboBox conclusion_respiratory_problem;
    private widget.ComboBox conclusion_vertigo;
    private widget.TextBox creatinin;
    private widget.TextBox culture;
    private widget.TextBox cysta;
    private widget.TextBox dass1;
    private widget.TextBox dass2;
    private widget.TextBox dass3;
    private widget.CekBox departemen;
    private widget.TextBox drug_amphetamine;
    private widget.TextBox drug_benzodiazepine;
    private widget.TextBox drug_cocain;
    private widget.TextBox drug_marijuana;
    private widget.TextBox drug_methamphetamine;
    private widget.TextBox drug_morphine;
    private widget.TextArea ecg_abnormal;
    private widget.TextBox esr;
    private widget.TextBox exam_abdomen_comments;
    private widget.TextBox exam_cardio_vascular_system_comments;
    private widget.TextBox exam_central_peripheral_nervous_system_comments;
    private widget.TextBox exam_dental_comments;
    private widget.TextBox exam_dental_muskulo;
    private widget.TextBox exam_ent_comments;
    private widget.TextBox exam_genito_urinary_system_comments;
    private widget.TextBox exam_lymph_nodes_comments;
    private widget.TextBox exam_respiratory_system_comments;
    private widget.TextBox exam_skin_comments;
    private widget.TextBox eye_brake_test_2;
    private widget.ComboBox eye_color_blindless;
    private widget.TextBox eye_glasses_distant_l;
    private widget.TextBox eye_glasses_distant_r;
    private widget.TextBox eye_glasses_near_l;
    private widget.TextBox eye_glasses_near_r;
    private widget.TextBox eye_night_vision_1;
    private widget.TextBox eye_night_vision_2;
    private widget.TextBox eye_unaided_distant_l;
    private widget.TextBox eye_unaided_distant_r;
    private widget.TextBox eye_unaided_near_l;
    private widget.TextBox eye_unaided_near_r;
    private widget.TextBox eye_unaided_near_r1;
    private widget.ComboBox fit;
    private widget.ComboBox fit_with_restrictions;
    private widget.ComboBox fundi;
    private widget.TextBox gamaa_gt;
    private widget.TextBox glucose;
    private widget.TextBox hb;
    private widget.TextBox hbsag;
    private widget.TextBox hdl_cholesterol;
    private widget.ComboBox imunisasi_bcg;
    private widget.ComboBox imunisasi_dpt;
    private widget.ComboBox imunisasi_hep_a;
    private widget.ComboBox imunisasi_hep_b;
    private widget.ComboBox imunisasi_morbili;
    private widget.ComboBox imunisasi_others;
    private widget.ComboBox imunisasi_polio;
    private widget.ComboBox imunisasi_tetanus;
    private widget.ComboBox imunisasi_thyphoid;
    private widget.InternalFrame internalFrame1;
    private widget.InternalFrame internalFrame2;
    private widget.InternalFrame internalFrame3;
    private widget.InternalFrame internalFrame8;
    private widget.Label jLabel19;
    private widget.Label jLabel21;
    private widget.Label jLabel6;
    private widget.Label jLabel7;
    private javax.swing.JPopupMenu jPopupMenu1;
    private widget.TextArea kesimpulan;
    private widget.TextBox lab_others;
    private widget.Label label40;
    private widget.Label label41;
    private widget.TextBox ldl_cholesterol;
    private widget.TextBox leukosit;
    private widget.TextBox malaria;
    private widget.CekBox manager;
    private widget.TextBox mantoux_test;
    private widget.TextBox oth_abnormal;
    private widget.TextBox ova;
    private widget.TextBox page3_comment;
    private widget.panelisi panelGlass8;
    private widget.panelisi panelGlass9;
    private widget.TextBox parasites1;
    private widget.TextBox pnemunosicosis;
    private widget.TextBox pnemunosicosis2;
    private widget.TextBox protein;
    private widget.TextBox random_glucose;
    private widget.TextBox rhesuss;
    private widget.TextArea saran;
    private widget.ScrollPane scrollInput;
    private widget.ScrollPane scrollPane10;
    private widget.ScrollPane scrollPane11;
    private widget.ScrollPane scrollPane12;
    private widget.ScrollPane scrollPane13;
    private widget.ScrollPane scrollPane14;
    private widget.ScrollPane scrollPane15;
    private widget.ScrollPane scrollPane5;
    private widget.ScrollPane scrollPane6;
    private widget.ScrollPane scrollPane7;
    private widget.TextBox sgot;
    private widget.TextBox sgpt;
    private widget.TextArea specify;
    private widget.TextBox spirometri_fev_1_1;
    private widget.TextBox spirometri_fev_1_2;
    private widget.TextBox spirometri_fev_1_3;
    private widget.TextBox spirometri_fev_1_4;
    private widget.TextBox spirometri_fev_1_fvc_1;
    private widget.TextBox spirometri_fev_1_fvc_2;
    private widget.TextBox spirometri_fev_1_fvc_3;
    private widget.TextBox spirometri_fev_1_fvc_4;
    private widget.TextBox spirometri_fvc_1;
    private widget.TextBox spirometri_fvc_2;
    private widget.TextBox spirometri_fvc_3;
    private widget.TextBox spirometri_fvc_4;
    private widget.TextBox spirometri_vc_1;
    private widget.TextBox spirometri_vc_2;
    private widget.TextBox spirometri_vc_3;
    private widget.TextBox spirometri_vc_4;
    private widget.CekBox supervisor;
    private widget.TextBox surname;
    private widget.TextBox tb1;
    private widget.TextBox tb2;
    private widget.Table tbObat;
    private widget.TextBox total_cholestrol;
    private widget.TextBox tpha;
    private widget.TextBox triglyceride;
    private widget.TextBox trombosit;
    private widget.TextBox type_of_hearing;
    private widget.TextBox urea;
    private widget.TextBox uric_acid;
    private widget.TextBox urine_bacteria;
    private widget.TextBox urine_chemical_reaction;
    private widget.TextBox urine_colour;
    private widget.TextBox urine_crystal;
    private widget.TextBox urine_epithel;
    private widget.TextBox urine_glucose;
    private widget.TextBox urine_ketones;
    private widget.TextBox urine_nitrites;
    private widget.TextBox urine_rbc;
    private widget.TextBox urine_turbidity;
    private widget.TextBox urine_wbc;
    private widget.ComboBox vertebra_forward_flexion_0_80;
    private widget.ComboBox vertebra_heel_walking;
    private widget.ComboBox vertebra_hyperextensi_0_25;
    private widget.ComboBox vertebra_kyphosis;
    private widget.ComboBox vertebra_lateral_flexion_0_20;
    private widget.ComboBox vertebra_lordosis;
    private widget.ComboBox vertebra_scoliosis;
    private widget.ComboBox vertebra_squats_x3;
    private widget.ComboBox vertebra_toe_walking;
    private widget.TextBox visual_fields_left;
    private widget.TextBox visual_fields_right;
    private widget.TextBox wbc;
    private widget.Label LblMcu1;
    private widget.Label LblMcu2;
    private widget.Label LblMcu3;
    private widget.Label LblMcu4;
    private widget.Label LblMcu5;
    private widget.Label LblMcu6;
    private widget.Label LblMcu7;
    private javax.swing.JSeparator SepMcu1;
    private widget.Label LblMcu8;
    private widget.Label LblMcu9;
    private widget.Label LblMcu10;
    private widget.Label LblMcu11;
    private widget.Label LblMcu12;
    private widget.Label LblMcu13;
    private widget.Label LblMcu14;
    private widget.Label LblMcu15;
    private widget.Label LblMcu16;
    private widget.Label LblMcu17;
    private widget.Label LblMcu18;
    private widget.Label LblMcu19;
    private widget.Label LblMcu20;
    private widget.Label LblMcu21;
    private widget.Label LblMcu22;
    private widget.Label LblMcu23;
    private widget.Label LblMcu24;
    private javax.swing.JSeparator SepMcu2;
    private widget.Label LblMcu25;
    private widget.Label LblMcu26;
    private javax.swing.JSeparator SepMcu3;
    private widget.Label LblMcu27;
    private widget.Label LblMcu28;
    private widget.Label LblMcu29;
    private widget.Label LblMcu30;
    private widget.Label LblMcu31;
    private widget.Label LblMcu32;
    private widget.Label LblMcu33;
    private widget.Label LblMcu34;
    private widget.Label LblMcu35;
    private widget.Label LblMcu36;
    private javax.swing.JSeparator SepMcu4;
    private widget.Label LblMcu37;
    private widget.Label LblMcu38;
    private widget.Label LblMcu39;
    private widget.Label LblMcu40;
    private widget.Label LblMcu41;
    private widget.Label LblMcu42;
    private widget.Label LblMcu43;
    private widget.Label LblMcu44;
    private widget.Label LblMcu45;
    private widget.Label LblMcu46;
    private widget.Label LblMcu47;
    private widget.Label LblMcu48;
    private widget.Label LblMcu49;
    private widget.Label LblMcu50;
    private widget.Label LblMcu51;
    private widget.Label LblMcu52;
    private widget.Label LblMcu53;
    private widget.Label LblMcu54;
    private widget.Label LblMcu55;
    private widget.Label LblMcu56;
    private widget.Label LblMcu57;
    private widget.Label LblMcu58;
    private widget.Label LblMcu59;
    private widget.Label LblMcu60;
    private widget.Label LblMcu61;
    private javax.swing.JSeparator SepMcu5;
    private widget.Label LblMcu62;
    private widget.Label LblMcu63;
    private widget.Label LblMcu64;
    private widget.Label LblMcu65;
    private widget.Label LblMcu66;
    private widget.Label LblMcu67;
    private widget.Label LblMcu68;
    private javax.swing.JSeparator SepMcu6;
    private widget.Label LblMcu69;
    private widget.Label LblMcu70;
    private widget.Label LblMcu71;
    private widget.Label LblMcu72;
    private widget.Label LblMcu73;
    private widget.Label LblMcu74;
    private widget.Label LblMcu75;
    private widget.Label LblMcu76;
    private widget.Label LblMcu77;
    private widget.Label LblMcu78;
    private widget.Label LblMcu79;
    private widget.Label LblMcu80;
    private javax.swing.JSeparator SepMcu7;
    private widget.Label LblMcu81;
    private widget.Label LblMcu82;
    private widget.Label LblMcu83;
    private widget.Label LblMcu84;
    private widget.Label LblMcu85;
    private javax.swing.JSeparator SepMcu8;
    private widget.Label LblMcu86;
    private widget.Label LblMcu87;
    private widget.Label LblMcu88;
    private widget.Label LblMcu89;
    private widget.Label LblMcu90;
    private widget.Label LblMcu91;
    private widget.Label LblMcu92;
    private javax.swing.JSeparator SepMcu9;
    private widget.Label LblMcu93;
    private widget.Label LblMcu94;
    private javax.swing.JSeparator SepMcu10;
    private widget.Label LblMcu95;
    private widget.Label LblMcu96;
    private widget.Label LblMcu97;
    private widget.Label LblMcu98;
    private widget.Label LblMcu99;
    private widget.Label LblMcu100;
    private widget.Label LblMcu101;
    private javax.swing.JSeparator SepMcu11;
    private widget.Label LblMcu102;
    private widget.Label LblMcu103;
    private widget.Label LblMcu104;
    private javax.swing.JSeparator SepMcu12;
    private widget.Label LblMcu105;
    private widget.Label LblMcu106;
    private widget.Label LblMcu107;
    private widget.Label LblMcu108;
    private widget.Label LblMcu109;
    private widget.Label LblMcu110;
    private widget.Label LblMcu111;
    private widget.Label LblMcu112;
    private widget.Label LblMcu113;
    private javax.swing.JSeparator SepMcu13;
    private widget.Label LblMcu114;
    private widget.Label LblMcu115;
    private widget.Label LblMcu116;
    private widget.Label LblMcu117;
    private widget.Label LblMcu118;
    private widget.Label LblMcu119;
    private widget.Label LblMcu120;
    private widget.Label LblMcu121;
    private widget.Label LblMcu122;
    private widget.Label LblMcu123;
    private widget.Label LblMcu124;
    private widget.Label LblMcu125;
    private widget.Label LblMcu126;
    private widget.Label LblMcu127;
    private widget.Label LblMcu128;
    private widget.Label LblMcu129;
    private widget.Label LblMcu130;
    private javax.swing.JSeparator SepMcu14;
    private widget.Label LblMcu131;
    private widget.Label LblMcu132;
    private widget.Label LblMcu133;
    private widget.Label LblMcu134;
    private widget.Label LblMcu135;
    private widget.Label LblMcu136;
    private widget.Label LblMcu137;
    private widget.Label LblMcu138;
    private widget.Label LblMcu139;
    private widget.Label LblMcu140;
    private widget.Label LblMcu141;
    private widget.Label LblMcu142;
    private widget.Label LblMcu143;
    private widget.Label LblMcu144;
    private javax.swing.JSeparator SepMcu15;
    private widget.Label LblMcu145;
    private widget.Label LblMcu146;
    private widget.Label LblMcu147;
    private widget.Label LblMcu148;
    private widget.Label LblMcu149;
    private widget.Label LblMcu150;
    private widget.Label LblMcu151;
    private widget.Label LblMcu152;
    private widget.Label LblMcu153;
    private widget.Label LblMcu154;
    private widget.Label LblMcu155;
    private widget.Label LblMcu156;
    private widget.Label LblMcu157;
    private widget.Label LblMcu158;
    private javax.swing.JSeparator SepMcu16;
    private widget.Label LblMcu159;
    private widget.Label LblMcu160;
    private widget.Label LblMcu161;
    private widget.Label LblMcu162;
    private widget.Label LblMcu163;
    private widget.Label LblMcu164;
    private widget.Label LblMcu165;
    private widget.Label LblMcu166;
    private widget.Label LblMcu167;
    private widget.Label LblMcu168;
    private javax.swing.JSeparator SepMcu17;
    private widget.Label LblMcu169;
    private widget.Label LblMcu170;
    private widget.Label LblMcu171;
    private widget.Label LblMcu172;
    private widget.Label LblMcu173;
    private widget.Label LblMcu174;
    private widget.Label LblMcu175;
    private widget.Label LblMcu176;
    private widget.Label LblMcu177;
    private widget.Label LblMcu178;
    private javax.swing.JSeparator SepMcu18;
    private widget.Label LblMcu179;
    private widget.Label LblMcu180;
    private widget.Label LblMcu181;
    private widget.Label LblMcu182;
    private widget.Label LblMcu183;
    private widget.Label LblMcu184;
    private widget.Label LblMcu185;
    private widget.Label LblMcu186;
    private widget.Label LblMcu187;
    private widget.Label LblMcu188;
    private widget.Label LblMcu189;
    private widget.Label LblMcu190;
    private widget.Label LblMcu191;
    private javax.swing.JSeparator SepMcu19;
    private widget.Label LblMcu192;
    private widget.Label LblMcu193;
    private widget.Label LblMcu194;
    private widget.Label LblMcu195;
    private widget.Label LblMcu196;
    private javax.swing.JSeparator SepMcu20;
    private widget.Label LblMcu197;
    private widget.Label LblMcu198;
    private widget.Label LblMcu199;
    private widget.Label LblMcu200;
    private widget.Label LblMcu201;
    private widget.Label LblMcu202;
    private widget.Label LblMcu203;
    private widget.Label LblMcu204;
    private widget.Label LblMcu205;
    private javax.swing.JSeparator SepMcu21;
    private widget.Label LblMcu206;
    private widget.Label LblMcu207;
    private widget.Label LblMcu208;
    private widget.Label LblMcu209;
    private javax.swing.JSeparator SepMcu22;
    private widget.Label LblMcu210;
    private widget.Label LblMcu211;
    private widget.Label LblMcu212;
    private widget.Label LblMcu213;
    private widget.Label LblMcu214;
    private widget.Label LblMcu215;
    private widget.Label LblMcu216;
    private javax.swing.JSeparator SepMcu23;
    private widget.Label LblMcu217;
    private widget.Label LblMcu218;
    private widget.Label LblMcu219;
    private widget.Label LblMcu220;
    // End of variables declaration//GEN-END:variables

    private void aturInputKesimpulanMcu() {
        ConcRadiologi.setEditable(true);
        ConcSpirometry.setEditable(true);
        ConcAudiometry.setEditable(true);
        ConcEcg.setEditable(false);
        cbConcEcg.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                sinkronConcEcg();
            }
        });
        sinkronConcEcg();
    }

    private void aturTabTraversal(Container container) {
        for (Component component : container.getComponents()) {
            if (component instanceof JComponent) {
                JComponent komponen = (JComponent) component;
                komponen.setFocusTraversalKeysEnabled(false);
                komponen.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0),
                        "fokusBerikutnya");
                komponen.getActionMap().put("fokusBerikutnya", new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        ((Component) evt.getSource()).transferFocus();
                    }
                });
                komponen.getInputMap(JComponent.WHEN_FOCUSED)
                        .put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, KeyEvent.SHIFT_DOWN_MASK), "fokusSebelumnya");
                komponen.getActionMap().put("fokusSebelumnya", new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        ((Component) evt.getSource()).transferFocusBackward();
                    }
                });
            }
            if (component instanceof Container) {
                aturTabTraversal((Container) component);
            }
        }
    }

    private void aturNavigasiEnter() {
        // Urutan Enter/PageUp mengikuti tata letak form (sama dengan urutan laporan MCU).
        final java.util.List<java.awt.Component> urutan = java.util.Arrays.asList(new java.awt.Component[] {
            TNoRw, TPasien, surname, TglAsuhan, McuGroup, Year,
            TmpLahir, TglLahir, Jk, SukuBangsa, SttsNikah, NoTlp,
            Perusahaan, NIP, JobTitle, Doe, Yoe, Activities,
            Hobby, OtherJob, PosisiKerja, JobInvolvesDrivingOrOperatingMobileEquipment, JobInvolvesWorkingAtHeights, JobInvolvesClericalOfficeBasedOrAdministrative,
            JobInvolvesPotentialDustExposure, JobInvolvesCateringStaffIncludingFoodHandlers, JobInvolvesExposingToOtherPotentialDangerous, JobInvolvesRequiresColourVision, RWP1, RWP2,
            RWP3, RWP4, RWP5, RWP6, RWP7, RWP8,
            RWP9, RWP10, RWP11, RWP12, RWP13, RWP14,
            RWP20, RWP15, RWP16, RWP17, RWP18, RWP19,
            RWP22, RWP23, RWP24, RWP25, RWP21, RWP26,
            RWP27, RWP28, RWP29, RWP30, FamilyHistoryFather, CigarettesPerday,
            PrescribedMedication, FamilyHistoryMother, AlcoholGrWeek, PrescribedMedication2, FamilyHistorySiblings, AnyAllergies,
            FamilyHistoryOther, hb, wbc, esr, bl_group, gamaa_gt,
            sgot, sgpt, urea, creatinin, glucose, random_glucose,
            total_cholestrol, protein, blood, leukosit, bilirubin, malaria,
            tpha, mantoux_test, lab_others, ova, culture, cysta,
            parasites1, trombosit, rhesuss, triglyceride, hdl_cholesterol, ldl_cholesterol,
            uric_acid, urine_colour, urine_turbidity, urine_chemical_reaction, urine_ketones, urine_glucose,
            urine_nitrites, urine_wbc, urine_rbc, urine_bacteria, urine_crystal, urine_epithel,
            hbsag, anti_hbs, cea, afp, drug_amphetamine, drug_methamphetamine,
            drug_morphine, drug_benzodiazepine, drug_cocain, drug_marijuana, PemeriksaanLaboratorium, pnemunosicosis,
            pnemunosicosis2, ILO_clasification, ILO_clasification2, tb1, tb2, oth_abnormal,
            page3_comment, RongsenThorax, cbConcEcg, ecg_abnormal, spirometri_vc_1, spirometri_vc_2,
            spirometri_vc_3, spirometri_vc_4, spirometri_fvc_1, spirometri_fvc_2, spirometri_fvc_3, spirometri_fvc_4,
            spirometri_fev_1_1, spirometri_fev_1_2, spirometri_fev_1_3, spirometri_fev_1_4, spirometri_fev_1_fvc_1, spirometri_fev_1_fvc_2,
            spirometri_fev_1_fvc_3, spirometri_fev_1_fvc_4, audiometri_tinitus_never, audiometri_tinitus_previously, audiometri_tinitus_rarely, audiometri_tinitus_often,
            audiometri_tinitus_always, audiometri_ear_protection_worn_never, audiometri_ear_protection_worn_previously, audiometri_ear_protection_worn_rarely, audiometri_ear_protection_worn_often, audiometri_ear_protection_worn_always,
            type_of_hearing, audiometri_left_ear_500, audiometri_left_ear_1000, audiometri_left_ear_1500, audiometri_left_ear_2000, audiometri_left_ear_3000,
            audiometri_left_ear_4000, audiometri_left_ear_5000, audiometri_left_ear_6000, audiometri_left_ear_501, audiometri_left_ear_1001, audiometri_left_ear_1501,
            audiometri_left_ear_2001, audiometri_left_ear_3001, audiometri_left_ear_4001, audiometri_left_ear_5001, audiometri_left_ear_6001, audiometri_left_ear_502,
            audiometri_left_ear_1002, audiometri_left_ear_1502, audiometri_left_ear_2002, audiometri_left_ear_3002, audiometri_left_ear_4002, audiometri_left_ear_5002,
            audiometri_left_ear_6002, audiometri_left_ear_503, audiometri_left_ear_1003, audiometri_left_ear_1503, audiometri_left_ear_2003, audiometri_left_ear_3003,
            audiometri_left_ear_4003, audiometri_left_ear_5003, audiometri_left_ear_6003, TD, Nadi, RR,
            TB, BB, eye_unaided_distant_l, eye_unaided_distant_r, eye_glasses_distant_l, eye_glasses_distant_r,
            eye_night_vision_1, eye_night_vision_2, eye_unaided_near_l, eye_unaided_near_r, eye_glasses_near_l, eye_glasses_near_r,
            eye_unaided_near_r1, eye_brake_test_2, eye_color_blindless, visual_fields_left, visual_fields_right, fundi,
            imunisasi_bcg, imunisasi_dpt, imunisasi_polio, imunisasi_morbili, imunisasi_thyphoid, imunisasi_hep_a,
            imunisasi_hep_b, imunisasi_tetanus, imunisasi_others, vertebra_scoliosis, vertebra_lateral_flexion_0_20, vertebra_kyphosis,
            vertebra_heel_walking, vertebra_lordosis, vertebra_toe_walking, vertebra_forward_flexion_0_80, vertebra_squats_x3, vertebra_hyperextensi_0_25,
            exam_ent_comments, exam_central_peripheral_nervous_system_comments, exam_cardio_vascular_system_comments, exam_skin_comments, exam_respiratory_system_comments, exam_lymph_nodes_comments,
            exam_abdomen_comments, exam_dental_comments, exam_genito_urinary_system_comments, exam_dental_muskulo, catatan, dass1,
            dass2, dass3, Dass21, PhyExam, ConcRadiologi, ConcEcg,
            ConcSpirometry, ConcAudiometry, kesimpulan, saran, departemen, supervisor,
            manager, conclusion_requires_spectacles, conclusion_colour_blindness, conclusion_respiratory_problem, conclusion_impaired_hearing, conclusion_vertigo,
            blood_group, fit, fit_with_restrictions, specify
        });
        java.awt.Component[] tanpaHandler = new java.awt.Component[] {
            TPasien, TglAsuhan, McuGroup, Year, TmpLahir, TglLahir,
            Jk, SukuBangsa, SttsNikah, Perusahaan, NIP, JobInvolvesDrivingOrOperatingMobileEquipment,
            JobInvolvesWorkingAtHeights, JobInvolvesClericalOfficeBasedOrAdministrative, JobInvolvesPotentialDustExposure, JobInvolvesCateringStaffIncludingFoodHandlers, JobInvolvesExposingToOtherPotentialDangerous, JobInvolvesRequiresColourVision,
            leukosit, PemeriksaanLaboratorium, RongsenThorax, cbConcEcg, audiometri_tinitus_never, audiometri_tinitus_previously,
            audiometri_tinitus_rarely, audiometri_tinitus_often, audiometri_tinitus_always, audiometri_ear_protection_worn_never, audiometri_ear_protection_worn_previously, audiometri_ear_protection_worn_rarely,
            audiometri_ear_protection_worn_often, audiometri_ear_protection_worn_always, catatan, dass1, dass2, dass3,
            Dass21, PhyExam, ConcEcg, ConcSpirometry, ConcAudiometry, kesimpulan,
            saran, conclusion_requires_spectacles, conclusion_colour_blindness, conclusion_respiratory_problem, conclusion_impaired_hearing, conclusion_vertigo,
            blood_group, fit, fit_with_restrictions
        };
        for (final java.awt.Component komponen : tanpaHandler) {
            komponen.addKeyListener(new java.awt.event.KeyAdapter() {
                @Override
                public void keyPressed(java.awt.event.KeyEvent evt) {
                    int posisi = urutan.indexOf(komponen);
                    if (evt.getKeyCode() == KeyEvent.VK_ENTER && !(komponen instanceof javax.swing.JTextArea)) {
                        if (posisi < urutan.size() - 1) {
                            urutan.get(posisi + 1).requestFocus();
                        } else {
                            BtnSimpan.requestFocus();
                        }
                    } else if (evt.getKeyCode() == KeyEvent.VK_PAGE_UP && posisi > 0) {
                        urutan.get(posisi - 1).requestFocus();
                    }
                }
            });
        }
    }

    private void aturDass() {
        java.awt.event.KeyAdapter keyAdapter = new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                hitungDass();
            }
        };
        dass1.addKeyListener(keyAdapter);
        dass2.addKeyListener(keyAdapter);
        dass3.addKeyListener(keyAdapter);
    }

    private void hitungDass() {
        int d1 = 0;
        int d2 = 0;
        int d3 = 0;
        try {
            d1 = Integer.parseInt(dass1.getText().trim());
        } catch (Exception e) {
        }
        try {
            d2 = Integer.parseInt(dass2.getText().trim());
        } catch (Exception e) {
        }
        try {
            d3 = Integer.parseInt(dass3.getText().trim());
        } catch (Exception e) {
        }

        int score1 = d1 * 2;
        int score2 = d2 * 2;
        int score3 = d3 * 2;

        String ket1 = klasifikasiDepresi(score1);
        String ket2 = klasifikasiKecemasan(score2);
        String ket3 = klasifikasiStress(score3);

        Dass21.setText("Dass21: Score Depression " + score1 + " (" + ket1 + "),\n" +
                "Score Anxiety " + score2 + " (" + ket2 + "), Score Stress " + score3 + " (" + ket3 + ")");
    }

    private String klasifikasiDepresi(int score) {
        if (score <= 9)
            return "Normal";
        if (score <= 13)
            return "Ringan";
        if (score <= 20)
            return "Sedang";
        if (score <= 27)
            return "Parah";
        return "Sangat Parah";
    }

    private String klasifikasiKecemasan(int score) {
        if (score <= 7)
            return "Normal";
        if (score <= 9)
            return "Ringan";
        if (score <= 14)
            return "Sedang";
        if (score <= 19)
            return "Parah";
        return "Sangat Parah";
    }

    private String klasifikasiStress(int score) {
        if (score <= 14)
            return "Normal";
        if (score <= 18)
            return "Ringan";
        if (score <= 25)
            return "Sedang";
        if (score <= 33)
            return "Parah";
        return "Sangat Parah";
    }

    private void aturKeypressRiwayatPenyakit() {
        PosisiKerja.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    JobInvolvesDrivingOrOperatingMobileEquipment.requestFocus();
                } else if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_PAGE_UP) {
                    OtherJob.requestFocus();
                }
            }
        });
        RWP1.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                RWP1KeyPressed(evt);
            }
        });
        RWP2.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                RWP2KeyPressed(evt);
            }
        });
        RWP3.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                RWP3KeyPressed(evt);
            }
        });
        RWP4.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                RWP4KeyPressed(evt);
            }
        });
        RWP5.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                RWP5KeyPressed(evt);
            }
        });
        RWP6.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                RWP6KeyPressed(evt);
            }
        });
        RWP7.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                RWP7KeyPressed(evt);
            }
        });
        RWP8.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                RWP8KeyPressed(evt);
            }
        });
        RWP9.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                RWP9KeyPressed(evt);
            }
        });
        RWP10.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                RWP10KeyPressed(evt);
            }
        });
        RWP11.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                RWP11KeyPressed(evt);
            }
        });
        RWP12.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                RWP12KeyPressed(evt);
            }
        });
        RWP13.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                RWP13KeyPressed(evt);
            }
        });
        RWP14.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                RWP14KeyPressed(evt);
            }
        });
        RWP15.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                RWP15KeyPressed(evt);
            }
        });
        RWP16.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                RWP16KeyPressed(evt);
            }
        });
        RWP17.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                RWP17KeyPressed(evt);
            }
        });
        RWP18.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                RWP18KeyPressed(evt);
            }
        });
        RWP19.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                RWP19KeyPressed(evt);
            }
        });
        RWP20.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                RWP20KeyPressed(evt);
            }
        });
        RWP21.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                RWP21KeyPressed(evt);
            }
        });
        RWP22.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                RWP22KeyPressed(evt);
            }
        });
        RWP23.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                RWP23KeyPressed(evt);
            }
        });
        RWP24.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                RWP24KeyPressed(evt);
            }
        });
        RWP25.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                RWP25KeyPressed(evt);
            }
        });
        RWP26.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                RWP26KeyPressed(evt);
            }
        });
        RWP27.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                RWP27KeyPressed(evt);
            }
        });
        RWP28.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                RWP28KeyPressed(evt);
            }
        });
        RWP29.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                RWP29KeyPressed(evt);
            }
        });
        RWP30.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                RWP30KeyPressed(evt);
            }
        });
        FamilyHistoryFather.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                FamilyHistoryFatherKeyPressed(evt);
            }
        });
        FamilyHistoryMother.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                FamilyHistoryMotherKeyPressed(evt);
            }
        });
        FamilyHistorySiblings.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                FamilyHistorySiblingsKeyPressed(evt);
            }
        });
        FamilyHistoryOther.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                FamilyHistoryOtherKeyPressed(evt);
            }
        });
        CigarettesPerday.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                CigarettesPerdayKeyPressed(evt);
            }
        });
        AlcoholGrWeek.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                AlcoholGrWeekKeyPressed(evt);
            }
        });
        PrescribedMedication.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                PrescribedMedicationKeyPressed(evt);
            }
        });
        PrescribedMedication2.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                PrescribedMedication2KeyPressed(evt);
            }
        });
        AnyAllergies.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                AnyAllergiesKeyPressed(evt);
            }
        });
    }

    private void aturKeypressLaboratoriumRontgen() {
        hb.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                hbKeyPressed(evt);
            }
        });
        wbc.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                wbcKeyPressed(evt);
            }
        });
        esr.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                esrKeyPressed(evt);
            }
        });
        bl_group.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                bl_groupKeyPressed(evt);
            }
        });
        gamaa_gt.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                gamaa_gtKeyPressed(evt);
            }
        });
        sgot.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                sgotKeyPressed(evt);
            }
        });
        sgpt.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                sgptKeyPressed(evt);
            }
        });
        urea.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                ureaKeyPressed(evt);
            }
        });
        creatinin.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                creatininKeyPressed(evt);
            }
        });
        glucose.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                glucoseKeyPressed(evt);
            }
        });
        random_glucose.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                random_glucoseKeyPressed(evt);
            }
        });
        total_cholestrol.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                total_cholestrolKeyPressed(evt);
            }
        });
        protein.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                proteinKeyPressed(evt);
            }
        });
        blood.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                bloodKeyPressed(evt);
            }
        });
        bilirubin.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                bilirubinKeyPressed(evt);
            }
        });
        malaria.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                malariaKeyPressed(evt);
            }
        });
        tpha.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tphaKeyPressed(evt);
            }
        });
        mantoux_test.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                mantoux_testKeyPressed(evt);
            }
        });
        lab_others.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                lab_othersKeyPressed(evt);
            }
        });
        ova.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                ovaKeyPressed(evt);
            }
        });
        culture.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                cultureKeyPressed(evt);
            }
        });
        cysta.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                cystaKeyPressed(evt);
            }
        });
        parasites1.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                parasites1KeyPressed(evt);
            }
        });
        trombosit.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                trombositKeyPressed(evt);
            }
        });
        rhesuss.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                rhesussKeyPressed(evt);
            }
        });
        triglyceride.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                triglycerideKeyPressed(evt);
            }
        });
        hdl_cholesterol.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                hdl_cholesterolKeyPressed(evt);
            }
        });
        ldl_cholesterol.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                ldl_cholesterolKeyPressed(evt);
            }
        });
        uric_acid.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                uric_acidKeyPressed(evt);
            }
        });
        urine_colour.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                urine_colourKeyPressed(evt);
            }
        });
        urine_turbidity.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                urine_turbidityKeyPressed(evt);
            }
        });
        urine_chemical_reaction.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                urine_chemical_reactionKeyPressed(evt);
            }
        });
        urine_ketones.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                urine_ketonesKeyPressed(evt);
            }
        });
        urine_glucose.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                urine_glucoseKeyPressed(evt);
            }
        });
        urine_nitrites.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                urine_nitritesKeyPressed(evt);
            }
        });
        urine_wbc.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                urine_wbcKeyPressed(evt);
            }
        });
        urine_rbc.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                urine_rbcKeyPressed(evt);
            }
        });
        urine_bacteria.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                urine_bacteriaKeyPressed(evt);
            }
        });
        urine_crystal.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                urine_crystalKeyPressed(evt);
            }
        });
        urine_epithel.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                urine_epithelKeyPressed(evt);
            }
        });
        hbsag.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                hbsagKeyPressed(evt);
            }
        });
        anti_hbs.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                anti_hbsKeyPressed(evt);
            }
        });
        cea.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                ceaKeyPressed(evt);
            }
        });
        afp.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                afpKeyPressed(evt);
            }
        });
        drug_amphetamine.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                drug_amphetamineKeyPressed(evt);
            }
        });
        drug_methamphetamine.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                drug_methamphetamineKeyPressed(evt);
            }
        });
        drug_morphine.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                drug_morphineKeyPressed(evt);
            }
        });
        drug_benzodiazepine.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                drug_benzodiazepineKeyPressed(evt);
            }
        });
        drug_cocain.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                drug_cocainKeyPressed(evt);
            }
        });
        drug_marijuana.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                drug_marijuanaKeyPressed(evt);
            }
        });
        BtnPetugasLab.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnPetugasLabKeyPressed(evt);
            }
        });
        pnemunosicosis.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                pnemunosicosisKeyPressed(evt);
            }
        });
        pnemunosicosis2.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                pnemunosicosis2KeyPressed(evt);
            }
        });
        ILO_clasification.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                ILO_clasificationKeyPressed(evt);
            }
        });
        ILO_clasification2.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                ILO_clasification2KeyPressed(evt);
            }
        });
        tb1.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tb1KeyPressed(evt);
            }
        });
        tb2.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tb2KeyPressed(evt);
            }
        });
        oth_abnormal.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                oth_abnormalKeyPressed(evt);
            }
        });
        page3_comment.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                page3_commentKeyPressed(evt);
            }
        });
    }

    private void aturKeypressPemeriksaanKhusus() {
        ecg_abnormal.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                ecg_abnormalKeyPressed(evt);
            }
        });
        spirometri_vc_1.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                spirometri_vc_1KeyPressed(evt);
            }
        });
        spirometri_vc_2.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                spirometri_vc_2KeyPressed(evt);
            }
        });
        spirometri_vc_3.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                spirometri_vc_3KeyPressed(evt);
            }
        });
        spirometri_vc_4.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                spirometri_vc_4KeyPressed(evt);
            }
        });
        spirometri_fvc_1.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                spirometri_fvc_1KeyPressed(evt);
            }
        });
        spirometri_fvc_2.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                spirometri_fvc_2KeyPressed(evt);
            }
        });
        spirometri_fvc_3.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                spirometri_fvc_3KeyPressed(evt);
            }
        });
        spirometri_fvc_4.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                spirometri_fvc_4KeyPressed(evt);
            }
        });
        spirometri_fev_1_1.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                spirometri_fev_1_1KeyPressed(evt);
            }
        });
        spirometri_fev_1_2.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                spirometri_fev_1_2KeyPressed(evt);
            }
        });
        spirometri_fev_1_3.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                spirometri_fev_1_3KeyPressed(evt);
            }
        });
        spirometri_fev_1_4.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                spirometri_fev_1_4KeyPressed(evt);
            }
        });
        spirometri_fev_1_fvc_1.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                spirometri_fev_1_fvc_1KeyPressed(evt);
            }
        });
        spirometri_fev_1_fvc_2.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                spirometri_fev_1_fvc_2KeyPressed(evt);
            }
        });
        spirometri_fev_1_fvc_3.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                spirometri_fev_1_fvc_3KeyPressed(evt);
            }
        });
        spirometri_fev_1_fvc_4.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                spirometri_fev_1_fvc_4KeyPressed(evt);
            }
        });
        type_of_hearing.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                type_of_hearingKeyPressed(evt);
            }
        });
        audiometri_left_ear_500.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                audiometri_left_ear_500KeyPressed(evt);
            }
        });
        audiometri_left_ear_1000.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                audiometri_left_ear_1000KeyPressed(evt);
            }
        });
        audiometri_left_ear_1500.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                audiometri_left_ear_1500KeyPressed(evt);
            }
        });
        audiometri_left_ear_2000.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                audiometri_left_ear_2000KeyPressed(evt);
            }
        });
        audiometri_left_ear_3000.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                audiometri_left_ear_3000KeyPressed(evt);
            }
        });
        audiometri_left_ear_4000.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                audiometri_left_ear_4000KeyPressed(evt);
            }
        });
        audiometri_left_ear_5000.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                audiometri_left_ear_5000KeyPressed(evt);
            }
        });
        audiometri_left_ear_6000.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                audiometri_left_ear_6000KeyPressed(evt);
            }
        });
        audiometri_left_ear_501.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                audiometri_left_ear_501KeyPressed(evt);
            }
        });
        audiometri_left_ear_1001.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                audiometri_left_ear_1001KeyPressed(evt);
            }
        });
        audiometri_left_ear_1501.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                audiometri_left_ear_1501KeyPressed(evt);
            }
        });
        audiometri_left_ear_2001.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                audiometri_left_ear_2001KeyPressed(evt);
            }
        });
        audiometri_left_ear_3001.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                audiometri_left_ear_3001KeyPressed(evt);
            }
        });
        audiometri_left_ear_4001.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                audiometri_left_ear_4001KeyPressed(evt);
            }
        });
        audiometri_left_ear_5001.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                audiometri_left_ear_5001KeyPressed(evt);
            }
        });
        audiometri_left_ear_6001.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                audiometri_left_ear_6001KeyPressed(evt);
            }
        });
        audiometri_left_ear_502.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                audiometri_left_ear_502KeyPressed(evt);
            }
        });
        audiometri_left_ear_1002.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                audiometri_left_ear_1002KeyPressed(evt);
            }
        });
        audiometri_left_ear_1502.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                audiometri_left_ear_1502KeyPressed(evt);
            }
        });
        audiometri_left_ear_2002.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                audiometri_left_ear_2002KeyPressed(evt);
            }
        });
        audiometri_left_ear_3002.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                audiometri_left_ear_3002KeyPressed(evt);
            }
        });
        audiometri_left_ear_4002.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                audiometri_left_ear_4002KeyPressed(evt);
            }
        });
        audiometri_left_ear_5002.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                audiometri_left_ear_5002KeyPressed(evt);
            }
        });
        audiometri_left_ear_6002.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                audiometri_left_ear_6002KeyPressed(evt);
            }
        });
        audiometri_left_ear_503.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                audiometri_left_ear_503KeyPressed(evt);
            }
        });
        audiometri_left_ear_1003.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                audiometri_left_ear_1003KeyPressed(evt);
            }
        });
        audiometri_left_ear_1503.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                audiometri_left_ear_1503KeyPressed(evt);
            }
        });
        audiometri_left_ear_2003.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                audiometri_left_ear_2003KeyPressed(evt);
            }
        });
        audiometri_left_ear_3003.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                audiometri_left_ear_3003KeyPressed(evt);
            }
        });
        audiometri_left_ear_4003.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                audiometri_left_ear_4003KeyPressed(evt);
            }
        });
        audiometri_left_ear_5003.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                audiometri_left_ear_5003KeyPressed(evt);
            }
        });
        audiometri_left_ear_6003.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                audiometri_left_ear_6003KeyPressed(evt);
            }
        });
        TD.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TDKeyPressed(evt);
            }
        });
        BB.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BBKeyPressed(evt);
            }
        });
        IMT.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                IMTKeyPressed(evt);
            }
        });
        KlasifikasiIMT1.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KlasifikasiIMT1KeyPressed(evt);
            }
        });
        eye_glasses_near_r.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                eye_glasses_near_rKeyPressed(evt);
            }
        });
        eye_glasses_near_l.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                eye_glasses_near_lKeyPressed(evt);
            }
        });
        eye_glasses_distant_r.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                eye_glasses_distant_rKeyPressed(evt);
            }
        });
        eye_glasses_distant_l.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                eye_glasses_distant_lKeyPressed(evt);
            }
        });
        eye_night_vision_1.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                eye_night_vision_1KeyPressed(evt);
            }
        });
        eye_night_vision_2.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                eye_night_vision_2KeyPressed(evt);
            }
        });
        eye_unaided_near_r.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                eye_unaided_near_rKeyPressed(evt);
            }
        });
        eye_unaided_near_l.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                eye_unaided_near_lKeyPressed(evt);
            }
        });
        eye_unaided_near_r1.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                eye_unaided_near_r1KeyPressed(evt);
            }
        });
        eye_brake_test_2.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                eye_brake_test_2KeyPressed(evt);
            }
        });
        eye_color_blindless.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                eye_color_blindlessKeyPressed(evt);
            }
        });
        visual_fields_left.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                visual_fields_leftKeyPressed(evt);
            }
        });
        visual_fields_right.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                visual_fields_rightKeyPressed(evt);
            }
        });
        fundi.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                fundiKeyPressed(evt);
            }
        });
        imunisasi_bcg.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                imunisasi_bcgKeyPressed(evt);
            }
        });
        imunisasi_dpt.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                imunisasi_dptKeyPressed(evt);
            }
        });
        imunisasi_polio.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                imunisasi_polioKeyPressed(evt);
            }
        });
        imunisasi_morbili.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                imunisasi_morbiliKeyPressed(evt);
            }
        });
        imunisasi_thyphoid.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                imunisasi_thyphoidKeyPressed(evt);
            }
        });
        imunisasi_hep_a.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                imunisasi_hep_aKeyPressed(evt);
            }
        });
        imunisasi_hep_b.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                imunisasi_hep_bKeyPressed(evt);
            }
        });
        imunisasi_tetanus.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                imunisasi_tetanusKeyPressed(evt);
            }
        });
        imunisasi_others.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                imunisasi_othersKeyPressed(evt);
            }
        });
        vertebra_scoliosis.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                vertebra_scoliosisKeyPressed(evt);
            }
        });
        vertebra_lordosis.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                vertebra_lordosisKeyPressed(evt);
            }
        });
        vertebra_hyperextensi_0_25.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                vertebra_hyperextensi_0_25KeyPressed(evt);
            }
        });
        vertebra_heel_walking.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                vertebra_heel_walkingKeyPressed(evt);
            }
        });
        vertebra_squats_x3.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                vertebra_squats_x3KeyPressed(evt);
            }
        });
        vertebra_kyphosis.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                vertebra_kyphosisKeyPressed(evt);
            }
        });
        vertebra_forward_flexion_0_80.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                vertebra_forward_flexion_0_80KeyPressed(evt);
            }
        });
        vertebra_lateral_flexion_0_20.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                vertebra_lateral_flexion_0_20KeyPressed(evt);
            }
        });
        vertebra_toe_walking.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                vertebra_toe_walkingKeyPressed(evt);
            }
        });
        exam_ent_comments.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                exam_ent_commentsKeyPressed(evt);
            }
        });
        exam_cardio_vascular_system_comments.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                exam_cardio_vascular_system_commentsKeyPressed(evt);
            }
        });
        exam_respiratory_system_comments.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                exam_respiratory_system_commentsKeyPressed(evt);
            }
        });
        exam_abdomen_comments.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                exam_abdomen_commentsKeyPressed(evt);
            }
        });
        exam_genito_urinary_system_comments.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                exam_genito_urinary_system_commentsKeyPressed(evt);
            }
        });
        exam_central_peripheral_nervous_system_comments.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                exam_central_peripheral_nervous_system_commentsKeyPressed(evt);
            }
        });
        exam_skin_comments.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                exam_skin_commentsKeyPressed(evt);
            }
        });
        exam_lymph_nodes_comments.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                exam_lymph_nodes_commentsKeyPressed(evt);
            }
        });
        exam_dental_comments.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                exam_dental_commentsKeyPressed(evt);
            }
        });
        exam_dental_muskulo.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                exam_dental_muskuloKeyPressed(evt);
            }
        });
    }

    private String selectPenilaianMcu() {
        return "select " +
                "penilaian_mcu.no_rawat,penilaian_mcu.tanggal,penilaian_mcu.`year` as year,penilaian_mcu.kd_dokter,penilaian_mcu.kd_petugas,"
                +
                "penilaian_mcu.catatan," +
                "ifnull(penilaian_mcu.nama_pasien,pasien.nm_pasien) as nama_pasien,ifnull(penilaian_mcu.surname,'') as surname,"
                +
                "penilaian_mcu.mcu_group,penilaian_mcu.dass_21,penilaian_mcu.phy_exam," +
                "ifnull(nullif(penilaian_mcu.laborat,''),penilaian_mcu.conc_lab) as conc_lab,penilaian_mcu.conc_radiologi,penilaian_mcu.conc_ecg,"
                +
                "penilaian_mcu.conc_spirometry,penilaian_mcu.conc_audiometry,penilaian_mcu.kesimpulan1," +
                "ifnull(penilaian_mcu.no_rkm_medis,pasien.no_rkm_medis) as no_rkm_medis," +
                "ifnull(penilaian_mcu.tmp_lahir,pasien.tmp_lahir) as tmp_lahir,ifnull(penilaian_mcu.tgl_lahir,pasien.tgl_lahir) as tgl_lahir,"
                +
                "ifnull(penilaian_mcu.jk,pasien.jk) as jk,ifnull(penilaian_mcu.no_tlp,pasien.no_tlp) as no_tlp," +
                "ifnull((select sb.nama_suku_bangsa from suku_bangsa sb where cast(sb.id as char)=penilaian_mcu.suku_bangsa),ifnull(nullif(penilaian_mcu.suku_bangsa,''),ifnull(suku_bangsa_pasien.nama_suku_bangsa,pasien.suku_bangsa))) as suku_bangsa,"
                +
                "ifnull(penilaian_mcu.stts_nikah,pasien.stts_nikah) as stts_nikah,penilaian_mcu.doe,penilaian_mcu.yoe,penilaian_mcu.job_title,"
                +
                "penilaian_mcu.activities,penilaian_mcu.hobby,penilaian_mcu.other_job,penilaian_mcu.posisi_kerja,penilaian_mcu.departemen,"
                +
                "penilaian_mcu.supervisor,penilaian_mcu.manager,penilaian_mcu.job_involves_driving_or_operating_mobile_equipment,"
                +
                "penilaian_mcu.job_involves_working_at_heights,penilaian_mcu.job_involves_clerical_office_based_or_administrative,"
                +
                "penilaian_mcu.job_involves_requires_colour_vision,penilaian_mcu.job_involves_potential_dust_exposure,"
                +
                "penilaian_mcu.job_involves_catering_staff_including_food_handlers,penilaian_mcu.job_involves_exposing_to_other_potential_dangerous,"
                +
                "penilaian_mcu.med_hist_head_injury_or_contussion,penilaian_mcu.med_hist_fainting_blackouts_epilepsy," +
                "penilaian_mcu.med_hist_visual_changes,penilaian_mcu.med_hist_hearing_loss," +
                "penilaian_mcu.med_hist_nose_sinus_throat_trouble_more_4_weeks,penilaian_mcu.med_hist_gynaecological_problems,"
                +
                "penilaian_mcu.med_hist_chronic_skin_problem,penilaian_mcu.med_hist_chronic_diarrhea,penilaian_mcu.med_hist_anorexia_more_4_weeks,"
                +
                "penilaian_mcu.med_hist_gastritis,penilaian_mcu.med_hist_jaundice_hepatitis,penilaian_mcu.med_hist_chronic_cough_more_4_weeks,"
                +
                "penilaian_mcu.med_hist_haemorhoid,penilaian_mcu.med_hist_chronic_abdominal_pain,penilaian_mcu.med_hist_diabetes,"
                +
                "penilaian_mcu.med_hist_asthma,penilaian_mcu.med_hist_allergies,penilaian_mcu.med_hist_tuberculosis_bronchitis,"
                +
                "penilaian_mcu.med_hist_psychiatric_disorder,penilaian_mcu.med_hist_sexual_transmitted_diseases," +
                "penilaian_mcu.med_hist_unusual_change_of_weight_more_5kg_per_month,penilaian_mcu.med_hist_hypertension,"
                +
                "penilaian_mcu.med_hist_chest_pain_heart_disease,penilaian_mcu.med_hist_malaria_tropical_disease," +
                "penilaian_mcu.med_hist_surgery_operation,penilaian_mcu.med_hist_back_pain_more_4_weeks,penilaian_mcu.med_hist_thypoid_fever,"
                +
                "penilaian_mcu.med_hist_swollen_or_painful_joints,penilaian_mcu.med_hist_kidney_problem_urinary_stones,"
                +
                "penilaian_mcu.med_hist_other_chronical_diseases,penilaian_mcu.family_history_father,penilaian_mcu.family_history_mother,"
                +
                "penilaian_mcu.family_history_siblings,penilaian_mcu.family_history_other,penilaian_mcu.cigarettes_perday,"
                +
                "penilaian_mcu.alcohol_gr_week,penilaian_mcu.prescribed_medication,penilaian_mcu.prescribed_medication_2,"
                +
                "penilaian_mcu.any_allergies,penilaian_mcu.hb,penilaian_mcu.wbc,penilaian_mcu.esr,penilaian_mcu.bl_group,penilaian_mcu.gamaa_gt,"
                +
                "penilaian_mcu.sgot,penilaian_mcu.sgpt,penilaian_mcu.urea,penilaian_mcu.creatinin,penilaian_mcu.glucose,"
                +
                "penilaian_mcu.random_glucose,penilaian_mcu.total_cholestrol,penilaian_mcu.protein,penilaian_mcu.blood,penilaian_mcu.bilirubin,"
                +
                "penilaian_mcu.malaria,penilaian_mcu.tpha,penilaian_mcu.mantoux_test,penilaian_mcu.leukosit,penilaian_mcu.lab_others,"
                +
                "penilaian_mcu.ova,penilaian_mcu.culture,penilaian_mcu.cysta,penilaian_mcu.parasites1,penilaian_mcu.pnemunosicosis,"
                +
                "penilaian_mcu.pnemunosicosis2,penilaian_mcu.ILO_clasification,penilaian_mcu.ILO_clasification2,penilaian_mcu.oth_abnormal,"
                +
                "penilaian_mcu.tb1,penilaian_mcu.tb2,penilaian_mcu.page3_comment,penilaian_mcu.td,penilaian_mcu.nadi,penilaian_mcu.rr,"
                +
                "penilaian_mcu.tb,penilaian_mcu.bb,penilaian_mcu.bmi,penilaian_mcu.kasifikasi_bmi,penilaian_mcu.laborat,penilaian_mcu.radiologi,"
                +
                "penilaian_mcu.ekg,penilaian_mcu.spirometri_vc_1,penilaian_mcu.spirometri_vc_2,penilaian_mcu.spirometri_vc_3,"
                +
                "penilaian_mcu.spirometri_vc_4,penilaian_mcu.spirometri_fvc_1,penilaian_mcu.spirometri_fvc_2,penilaian_mcu.spirometri_fvc_3,"
                +
                "penilaian_mcu.spirometri_fvc_4,penilaian_mcu.spirometri_fev_1_1,penilaian_mcu.spirometri_fev_1_2,penilaian_mcu.spirometri_fev_1_3,"
                +
                "penilaian_mcu.spirometri_fev_1_4,penilaian_mcu.spirometri_fev_1_fvc_1,penilaian_mcu.spirometri_fev_1_fvc_2,"
                +
                "penilaian_mcu.spirometri_fev_1_fvc_3,penilaian_mcu.spirometri_fev_1_fvc_4,penilaian_mcu.audiometri_tinitus_never,"
                +
                "penilaian_mcu.audiometri_tinitus_previously,penilaian_mcu.audiometri_tinitus_rarely,penilaian_mcu.audiometri_tinitus_often,"
                +
                "penilaian_mcu.audiometri_tinitus_always,penilaian_mcu.audiometri_ear_protection_worn_never," +
                "penilaian_mcu.audiometri_ear_protection_worn_previously,penilaian_mcu.audiometri_ear_protection_worn_rarely,"
                +
                "penilaian_mcu.audiometri_ear_protection_worn_often,penilaian_mcu.audiometri_ear_protection_worn_always,"
                +
                "penilaian_mcu.type_of_hearing,penilaian_mcu.audiometri_left_ear_500_AB,penilaian_mcu.audiometri_left_ear_1000_AB,"
                +
                "penilaian_mcu.audiometri_left_ear_1500_AB,penilaian_mcu.audiometri_left_ear_2000_AB,penilaian_mcu.audiometri_left_ear_3000_AB,"
                +
                "penilaian_mcu.audiometri_left_ear_4000_AB,penilaian_mcu.audiometri_left_ear_5000_AB,penilaian_mcu.audiometri_left_ear_6000_AB,"
                +
                "penilaian_mcu.audiometri_left_ear_500_AC,penilaian_mcu.audiometri_left_ear_1000_AC,penilaian_mcu.audiometri_left_ear_1500_AC,"
                +
                "penilaian_mcu.audiometri_left_ear_2000_AC,penilaian_mcu.audiometri_left_ear_3000_AC,penilaian_mcu.audiometri_left_ear_4000_AC,"
                +
                "penilaian_mcu.audiometri_left_ear_5000_AC,penilaian_mcu.audiometri_left_ear_6000_AC,penilaian_mcu.audiometri_right_ear_500_ab,"
                +
                "penilaian_mcu.audiometri_right_ear_1000_ab,penilaian_mcu.audiometri_right_ear_1500_ab,penilaian_mcu.audiometri_right_ear_2000_ab,"
                +
                "penilaian_mcu.audiometri_right_ear_3000_ab,penilaian_mcu.audiometri_right_ear_4000_ab,penilaian_mcu.audiometri_right_ear_5000_ab,"
                +
                "penilaian_mcu.audiometri_right_ear_6000_ab,penilaian_mcu.audiometri_right_ear_500_ac,penilaian_mcu.audiometri_right_ear_1000_ac,"
                +
                "penilaian_mcu.audiometri_right_ear_1500_ac,penilaian_mcu.audiometri_right_ear_2000_ac,penilaian_mcu.audiometri_right_ear_3000_ac,"
                +
                "penilaian_mcu.audiometri_right_ear_4000_ac,penilaian_mcu.audiometri_right_ear_5000_ac,penilaian_mcu.audiometri_right_ear_6000_ac,"
                +
                "penilaian_mcu.eye_unaided_distant_r,penilaian_mcu.eye_unaided_distant_l,penilaian_mcu.eye_glasses_distant_r,"
                +
                "penilaian_mcu.eye_glasses_distant_l,penilaian_mcu.eye_unaided_near_r,penilaian_mcu.eye_unaided_near_l,"
                +
                "penilaian_mcu.eye_glasses_near_r,penilaian_mcu.eye_glasses_near_l,penilaian_mcu.eye_night_vision_1," +
                "penilaian_mcu.eye_night_vision_2,penilaian_mcu.eye_brake_test_1,penilaian_mcu.eye_brake_test_2,penilaian_mcu.eye_color_blindless,"
                +
                "penilaian_mcu.visual_fields_left,penilaian_mcu.visual_fields_right,penilaian_mcu.fundi,penilaian_mcu.imunisasi_bcg,"
                +
                "penilaian_mcu.imunisasi_dpt,penilaian_mcu.imunisasi_polio,penilaian_mcu.imunisasi_morbili,penilaian_mcu.imunisasi_thyphoid,"
                +
                "penilaian_mcu.imunisasi_hep_a,penilaian_mcu.imunisasi_hep_b,penilaian_mcu.imunisasi_tetanus,penilaian_mcu.imunisasi_others,"
                +
                "penilaian_mcu.vertebra_scoliosis,penilaian_mcu.vertebra_kyphosis,penilaian_mcu.vertebra_lordosis," +
                "penilaian_mcu.vertebra_forward_flexion_0_80,penilaian_mcu.vertebra_hyperextensi_0_25,penilaian_mcu.vertebra_lateral_flexion_0_20,"
                +
                "penilaian_mcu.vertebra_heel_walking,penilaian_mcu.vertebra_toe_walking,penilaian_mcu.vertebra_squats_x3,"
                +
                "penilaian_mcu.exam_ent_comments,penilaian_mcu.exam_cardio_vascular_system_comments,penilaian_mcu.exam_respiratory_system_comments,"
                +
                "penilaian_mcu.exam_abdomen_comments,penilaian_mcu.exam_genito_urinary_system_comments," +
                "penilaian_mcu.exam_central_peripheral_nervous_system_comments,penilaian_mcu.exam_skin_comments," +
                "penilaian_mcu.exam_lymph_nodes_comments,penilaian_mcu.exam_dental_comments,penilaian_mcu.exam_dental_muskulo,"
                +
                "penilaian_mcu.conclusion_requires_spectacles,penilaian_mcu.conclusion_colour_blindness," +
                "penilaian_mcu.conclusion_respiratory_problem,penilaian_mcu.conclusion_impaired_hearing,penilaian_mcu.conclusion_vertigo,"
                +
                "penilaian_mcu.blood_group,penilaian_mcu.medically_fit,penilaian_mcu.fit_with_restrictions,penilaian_mcu.specify,"
                +
                "penilaian_mcu.unfit_comment_1,penilaian_mcu.trombosit,"
                +
                "penilaian_mcu.rhesuss,penilaian_mcu.triglyceride,penilaian_mcu.hdl_cholesterol,penilaian_mcu.ldl_cholesterol,"
                +
                "penilaian_mcu.uric_acid,penilaian_mcu.urine_colour,penilaian_mcu.urine_turbidity,penilaian_mcu.urine_chemical_reaction,"
                +
                "penilaian_mcu.urine_ketones,penilaian_mcu.urine_glucose,penilaian_mcu.urine_nitrites,penilaian_mcu.urine_wbc,"
                +
                "penilaian_mcu.urine_rbc,penilaian_mcu.urine_bacteria,penilaian_mcu.urine_crystal,penilaian_mcu.urine_epithel,penilaian_mcu.hbsag,"
                +
                "penilaian_mcu.anti_hbs,penilaian_mcu.cea,penilaian_mcu.afp,penilaian_mcu.drug_amphetamine,penilaian_mcu.drug_methamphetamine,"
                +
                "penilaian_mcu.drug_morphine,penilaian_mcu.drug_benzodiazepine,penilaian_mcu.drug_cocain,penilaian_mcu.drug_marijuana,"
                +
                "dokter.nm_dokter,petugas.nama as nm_petugas," + selectNamaPenanggungJawab()
                + " as perusahaan_pasien,pasien.nip,pasien.alamat as alamat_pasien," +
                "ifnull(penilaian_mcu.nama_pasien,pasien.nm_pasien) as nm_pasien,penilaian_mcu.kd_petugas_lab,petugas_lab.nama as nm_petugas_lab,"
                +
                "poliklinik.nm_poli "
                +
                "from penilaian_mcu " +
                "inner join reg_periksa on penilaian_mcu.no_rawat=reg_periksa.no_rawat " +
                "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis " +
                "left join suku_bangsa suku_bangsa_pasien on suku_bangsa_pasien.id=pasien.suku_bangsa " +
                "left join perusahaan_pasien on perusahaan_pasien.kode_perusahaan=pasien.perusahaan_pasien " +
                "left join dokter on penilaian_mcu.kd_dokter=dokter.kd_dokter " +
                "left join petugas on penilaian_mcu.kd_petugas=petugas.nip " +
                "left join petugas petugas_lab on penilaian_mcu.kd_petugas_lab=petugas_lab.nip " +
                "left join poliklinik on reg_periksa.kd_poli=poliklinik.kd_poli ";
    }

    private String getTanggalAsuhan() {
        return Valid.SetTgl(TglAsuhan.getSelectedItem() + "");
    }

    private String getTanggalJamAsuhan() {
        String nilai = TglAsuhan.getSelectedItem() == null ? "" : TglAsuhan.getSelectedItem().toString();
        String jamSekarang = new java.text.SimpleDateFormat("HH:mm:ss").format(new Date());
        return Valid.SetTgl(nilai) + " " + (nilai.length() >= 19 ? nilai.substring(11, 19) : jamSekarang);
    }

    private void setTanggalAsuhanMinimalRegistrasi() {
        String tanggalRegistrasi = TanggalRegistrasi.getText().trim();
        if (tanggalRegistrasi.equals("")) {
            return;
        }
        try {
            java.text.SimpleDateFormat formatTanggal = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date registrasi = formatTanggal.parse(tanggalRegistrasi);
            Date asuhan = formatTanggal.parse(getTanggalJamAsuhan());
            if (asuhan.before(registrasi)) {
                TglAsuhan.setDate(registrasi);
            }
        } catch (Exception e) {
            System.out.println("Notif : " + e);
        }
    }

    /**
     * Memaksa tanggal asuhan mengikuti tanggal registrasi No. Rawat tujuan. Dipakai setelah
     * copy pengkajian MCU agar data hasil paste memakai tanggal registrasi tujuan, bukan
     * tanggal asuhan bawaan dari data sumber yang disalin.
     */
    private void setTanggalAsuhanSamaDenganRegistrasi() {
        String tanggalRegistrasi = TanggalRegistrasi.getText().trim();
        if (tanggalRegistrasi.equals("")) {
            return;
        }
        try {
            java.text.SimpleDateFormat formatTanggal = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date registrasi = formatTanggal.parse(tanggalRegistrasi);
            TglAsuhan.setDate(registrasi);
        } catch (Exception e) {
            System.out.println("Notif : " + e);
        }
    }

    private String getTahunDariTanggal(String tanggal) {
        String nilai = tanggal == null ? "" : tanggal.trim();
        return nilai.length() >= 4 ? nilai.substring(0, 4) : "";
    }

    private String getTahunAsuhan() {
        return getTahunDariTanggal(getTanggalAsuhan());
    }

    private String getYearMcu() {
        String nilaiYear = Year.getText().trim();
        return nilaiYear.equals("") ? getTahunAsuhan() : nilaiYear;
    }

    private String nilai(javax.swing.text.JTextComponent teks) {
        return teks.getText();
    }

    private String nilaiCombo(javax.swing.JComboBox combo) {
        Object nilai = combo.getSelectedItem();
        return nilai == null ? "" : nilai.toString();
    }

    private String nilaiComboLower(javax.swing.JComboBox combo) {
        return nilaiCombo(combo).toLowerCase();
    }

    private boolean comboTerpilih(javax.swing.JComboBox combo, String nilai) {
        return nilaiCombo(combo).equalsIgnoreCase(nilai);
    }

    private String nilaiComboYesNo(javax.swing.JComboBox combo) {
        return comboTerpilih(combo, "Yes") ? "Yes" : "No";
    }

    private String nilaiBloodGroupMcu() {
        String nilai = normalisasiBloodGroup(bl_group.getText());
        return nilai.equals("") ? nilaiCombo(blood_group) : nilai;
    }

    private String normalisasiBloodGroup(String nilai) {
        String hasil = nilai == null ? "" : nilai.trim().toUpperCase();
        return hasil.equals("A+") || hasil.equals("A-") ||
                hasil.equals("B+") || hasil.equals("B-") ||
                hasil.equals("AB+") || hasil.equals("AB-") ||
                hasil.equals("O+") || hasil.equals("O-") ? hasil : "";
    }

    private String nilaiPertamaTerisiValue(String... daftarNilai) {
        for (String nilai : daftarNilai) {
            if (nilai != null && !nilai.trim().equals("")) {
                return nilai;
            }
        }
        return "";
    }

    private String nilaiLaboratMcu() {
        return nilai(PemeriksaanLaboratorium);
    }

    private String nilaiKesimpulanMcu() {
        return nilai(kesimpulan);
    }

    private String nilaiSaranMcu() {
        return nilai(saran);
    }

    private String nilaiCatatanMcu() {
        return nilai(catatan);
    }

    private void sinkronFieldMcu() {
        String nilaiLaborat = nilaiLaboratMcu();
        setText(PemeriksaanLaboratorium, nilaiLaborat);

        String nilaiKesimpulan = nilaiKesimpulanMcu();
        setText(kesimpulan, nilaiKesimpulan);

        String nilaiSaran = nilaiSaranMcu();
        setText(saran, nilaiSaran);
    }

    private void sinkronBloodGroup() {
        bl_group.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                setBloodGroupDariBlGroup();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                setBloodGroupDariBlGroup();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                setBloodGroupDariBlGroup();
            }
        });
        setBloodGroupDariBlGroup();
    }

    private void setBloodGroupDariBlGroup() {
        String nilai = normalisasiBloodGroup(bl_group.getText());
        if (!nilai.equals("")) {
            setCombo(blood_group, nilai);
        }
    }

    private String nilaiTabel(int baris, int kolom) {
        Object nilai = tbObat.getValueAt(baris, kolom);
        return nilai == null ? "" : nilai.toString();
    }

    private String escapeHtml(String nilai) {
        return nilai == null ? ""
                : nilai.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private String escapeCsv(String nilai) {
        return nilai == null ? "" : nilai.replace("\"", "\"\"");
    }

    private StringBuilder headerExportHtml() {
        StringBuilder html = new StringBuilder("<tr class='isi'>");
        for (int kolom = 0; kolom < tabMode.getColumnCount(); kolom++) {
            html.append("<td valign='middle' bgcolor='#FFFAFA' align='center'><b>")
                    .append(escapeHtml(tabMode.getColumnName(kolom))).append("</b></td>");
        }
        return html.append("</tr>");
    }

    private StringBuilder barisExportHtml() {
        StringBuilder html = new StringBuilder();
        for (int baris = 0; baris < tabMode.getRowCount(); baris++) {
            html.append("<tr class='isi'>");
            for (int kolom = 0; kolom < tabMode.getColumnCount(); kolom++) {
                html.append("<td valign='top'>").append(escapeHtml(nilaiTabel(baris, kolom))).append("</td>");
            }
            html.append("</tr>");
        }
        return html;
    }

    private String htmlLaporanMcu() {
        return "<html><head>" +
                "<link href=\"file2.css\" rel=\"stylesheet\" type=\"text/css\" />" +
                "</head><body>" +
                "<table width='10000px' border='0' align='center' cellpadding='3px' cellspacing='0' class='tbl_form'>" +
                "<tr class='isi2'>" +
                "<td valign='top' align='center'>" +
                "<font size='4' face='Arial'>" + escapeHtml(akses.getnamars()) + "</font><br>" +
                escapeHtml(akses.getalamatrs()) + ", " + escapeHtml(akses.getkabupatenrs()) + ", "
                + escapeHtml(akses.getpropinsirs()) + "<br>" +
                escapeHtml(akses.getkontakrs()) + ", E-mail : " + escapeHtml(akses.getemailrs()) + "<br><br>" +
                "<font size='2' face='Arial'>DATA PEMERIKSAAN MCU<br><br></font>" +
                "</td>" +
                "</tr>" +
                "</table>" +
                "<table width='10000px' border='0' align='center' cellpadding='1px' cellspacing='0' class='tbl_form'>" +
                headerExportHtml().toString() + barisExportHtml().toString() +
                "</table>" +
                "</body></html>";
    }

    private void tulisDanBukaFile(String namaFile, String isi) throws Exception {
        File file = new File(namaFile);
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(isi);
        writer.close();
        Desktop.getDesktop().browse(file.toURI());
    }

    private boolean cetakPilihanLaporan(String pilihan) throws Exception {
        if (pilihan == null) {
            return true;
        } else if ("Laporan 1 (HTML)".equals(pilihan)) {
            LoadHTML.setText(htmlLaporanMcu());
            tulisDanBukaFile("DataMCU.html", LoadHTML.getText());
            return true;
        } else if ("Laporan 2 (WPS)".equals(pilihan)) {
            LoadHTML.setText(htmlLaporanMcu());
            tulisDanBukaFile("DataMCU.wps", LoadHTML.getText());
            return true;
        } else if ("Laporan 3 (CSV)".equals(pilihan)) {
            tulisDanBukaFile("DataMCU.csv", exportCsv().toString());
            return true;
        } else if ("Laporan 4 (Jasper)".equals(pilihan)) {
            cetakJasperPenilaianMcuTerpilih();
            return true;
        } else if ("Laporan 5 (Excel)".equals(pilihan)) {
            exportExcel();
            return true;
        }
        return false;
    }

    private StringBuilder exportCsv() {
        StringBuilder csv = new StringBuilder();
        boolean firstCol = true;
        for (int kolom = 0; kolom < tabMode.getColumnCount(); kolom++) {
            String header = tabMode.getColumnName(kolom);
            if (isKolomExportCsv(header)) {
                if (!firstCol) {
                    csv.append(";");
                }
                csv.append("\"").append(escapeCsv(header)).append("\"");
                firstCol = false;
            }
        }
        csv.append("\n");
        for (int baris = 0; baris < tabMode.getRowCount(); baris++) {
            firstCol = true;
            for (int kolom = 0; kolom < tabMode.getColumnCount(); kolom++) {
                String header = tabMode.getColumnName(kolom);
                if (isKolomExportCsv(header)) {
                    if (!firstCol) {
                        csv.append(";");
                    }
                    csv.append("\"").append(escapeCsv(nilaiTabel(baris, kolom))).append("\"");
                    firstCol = false;
                }
            }
            csv.append("\n");
        }
        return csv;
    }

    private boolean isKolomExportCsv(String header) {
        if ("No.Rawat".equals(header)) return true;
        if ("Dokter PJ".equals(header)) return true;
        if ("No.RM".equals(header)) return true;
        if ("Nama Pasien".equals(header)) return true;
        if ("Perusahaan".equals(header)) return true;
        if ("Badge".equals(header)) return true;
        if ("Alamat".equals(header)) return true;
        if ("Medical History".equals(header)) return true; // dass21
        
        // med_hist
        if ("Head Injury/Contussion".equals(header)) return true;
        if ("Fainting/Blackouts/Epilepsy".equals(header)) return true;
        if ("Visual Changes".equals(header)) return true;
        if ("Hearing Loss".equals(header)) return true;
        if ("Nose/Sinus/Throat >4 Weeks".equals(header)) return true;
        if ("Gynaecological Problems".equals(header)) return true;
        if ("Chronic Skin Problem".equals(header)) return true;
        if ("Chronic Diarrhea".equals(header)) return true;
        if ("Anorexia >4 Weeks".equals(header)) return true;
        if ("Gastritis".equals(header)) return true;
        if ("Jaundice/Hepatitis".equals(header)) return true;
        if ("Chronic Cough >4 Weeks".equals(header)) return true;
        if ("Haemorhoid".equals(header)) return true;
        if ("Chronic Abdominal Pain".equals(header)) return true;
        if ("Diabetes".equals(header)) return true;
        if ("Asthma".equals(header)) return true;
        if ("Allergies".equals(header)) return true;
        if ("Tuberculosis/Bronchitis".equals(header)) return true;
        if ("Psychiatric Disorder".equals(header)) return true;
        if ("Sexual Transmitted Diseases".equals(header)) return true;
        if ("Weight Change >5kg/Month".equals(header)) return true;
        if ("Hypertension".equals(header)) return true;
        if ("Chest Pain/Heart Disease".equals(header)) return true;
        if ("Malaria/Tropical Disease".equals(header)) return true;
        if ("Surgery/Operation".equals(header)) return true;
        if ("Back Pain >4 Weeks".equals(header)) return true;
        if ("Thypoid Fever".equals(header)) return true;
        if ("Swollen/Painful Joints".equals(header)) return true;
        if ("Kidney Problem/Urinary Stones".equals(header)) return true;
        if ("Other Chronical Diseases".equals(header)) return true;
        
        // grup pemeriksaan - hasil Lab
        if ("Pemeriksaan Laboratorium".equals(header)) return true;
        if ("Kesimpulan Laboratorium".equals(header)) return true;
        
        // grup pemeriksaan - phy_exam
        if ("Physical Examination".equals(header)) return true;
        
        // grup pemeriksaan - ecg
        if ("Conclusion EKG".equals(header)) return true;
        if ("ECG Abnormal".equals(header)) return true;
        
        // grup pemeriksaan - eye
        if ("Unaided distant R".equals(header)) return true;
        if ("Unaided distant L".equals(header)) return true;
        if ("Glasses distant R".equals(header)) return true;
        if ("Glasses distant L".equals(header)) return true;
        if ("Unaided near R".equals(header)) return true;
        if ("Unaided near L".equals(header)) return true;
        if ("Glasses near R".equals(header)) return true;
        if ("Glasses near L".equals(header)) return true;
        if ("Night Vision 1".equals(header)) return true;
        if ("Night Vision 2".equals(header)) return true;
        if ("Brake Test 1".equals(header)) return true;
        if ("Brake Test 2".equals(header)) return true;
        if ("Buta Warna".equals(header)) return true;
        if ("Lapang Pandang L".equals(header)) return true;
        if ("Lapang Pandang R".equals(header)) return true;
        if ("Fundi".equals(header)) return true;
        
        return false;
    }

    private void exportExcel() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Simpan File Excel");
        fc.setSelectedFile(new File("DataMCU.xls"));
        if (fc.showSaveDialog(null) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = fc.getSelectedFile();
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            bw.write("<html xmlns:o=\"urn:schemas-microsoft-com:office:office\" xmlns:x=\"urn:schemas-microsoft-com:office:excel\" xmlns=\"http://www.w3.org/TR/REC-html40\">\n");
            bw.write("<head>\n");
            bw.write("<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\">\n");
            bw.write("<style>\n");
            bw.write("  .text { mso-number-format:\"\\@\"; }\n");
            bw.write("  th { font-weight: bold; background-color: #f2f2f2; }\n");
            bw.write("</style>\n");
            bw.write("</head>\n");
            bw.write("<body>\n");
            bw.write("<table border=\"1\">\n");

            // Write Header
            bw.write("  <tr>\n");
            for (int kolom = 0; kolom < tabMode.getColumnCount(); kolom++) {
                String header = tabMode.getColumnName(kolom);
                if (isKolomExportCsv(header)) {
                    bw.write("    <th>" + escapeHtml(header) + "</th>\n");
                }
            }
            bw.write("  </tr>\n");

            // Write Data
            for (int baris = 0; baris < tabMode.getRowCount(); baris++) {
                bw.write("  <tr>\n");
                for (int kolom = 0; kolom < tabMode.getColumnCount(); kolom++) {
                    String header = tabMode.getColumnName(kolom);
                    if (isKolomExportCsv(header)) {
                        String val = nilaiTabel(baris, kolom);
                        if ("No.RM".equals(header) || "No.Rawat".equals(header) || "Badge".equals(header)) {
                            bw.write("    <td class=\"text\">" + escapeHtml(val) + "</td>\n");
                        } else {
                            bw.write("    <td>" + escapeHtml(val) + "</td>\n");
                        }
                    }
                }
                bw.write("  </tr>\n");
            }

            bw.write("</table>\n");
            bw.write("</body>\n");
            bw.write("</html>\n");
            bw.close();
            JOptionPane.showMessageDialog(null, "Export Excel berhasil..!!");
            Desktop.getDesktop().browse(file.toURI());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Gagal export Excel : " + e.getMessage());
        }
    }

    private String nilaiFitWithRestrictions() {
        return nilaiCombo(fit_with_restrictions);
    }

    private String nilaiSpecify() {
        return comboTerpilih(fit_with_restrictions, "Specify") ? nilai(specify) : "";
    }

    private String nilaiNormalAbnormal(String nilai) {
        String nilaiBersih = nilai == null ? "" : nilai.trim();
        if (nilaiBersih.equalsIgnoreCase("normal")) {
            return "Normal";
        } else if (nilaiBersih.equalsIgnoreCase("abnormal")) {
            return "Abnormal";
        }
        return "";
    }

    private String nilaiEyeColorBlindless(String nilai) {
        String nilaiBersih = nilai == null ? "" : nilai.trim();
        if (nilaiBersih.equalsIgnoreCase("normal")) {
            return "Normal";
        } else if (nilaiBersih.equalsIgnoreCase("color blind")) {
            return "Color Blind";
        } else if (nilaiBersih.equalsIgnoreCase("partial color blind")
                || nilaiBersih.equalsIgnoreCase("partial colod blind")
                || nilaiBersih.equalsIgnoreCase("red green absen")) {
            return "Partial Color Blind";
        }
        return "Normal";
    }

    private String nilaiConcEcgTabel() {
        String nilai = nilaiNormalAbnormal(getTabelValue("conc_ecg"));
        return nilai.equals("") ? nilaiNormalAbnormal(getTabelValue("ekg")) : nilai;
    }

    private void setConcEcgDariTabel() {
        String nilai = nilaiConcEcgTabel();
        if (nilai.equals("")) {
            if (cbConcEcg.getItemCount() > 0) {
                cbConcEcg.setSelectedIndex(0);
            }
        } else {
            setCombo(cbConcEcg, nilai);
        }
        sinkronConcEcg();
    }

    private void sinkronConcEcg() {
        boolean abnormal = comboTerpilih(cbConcEcg, "Abnormal Condition");
        setText(ConcEcg, nilaiCombo(cbConcEcg));
        ecg_abnormal.setEditable(abnormal);
        ecg_abnormal.setEnabled(abnormal);
    }

    private String nilaiEkgMcu() {
        return comboTerpilih(cbConcEcg, "Abnormal Condition") ? nilai(ecg_abnormal) : nilaiCombo(cbConcEcg);
    }

    private String nilaiCek(javax.swing.AbstractButton cek) {
        return cek.isSelected() ? "Yes" : "No";
    }

    private String nilaiCekTunggal(javax.swing.AbstractButton cek) {
        return cek.isSelected() ? "1" : "";
    }

    private String tampilJenisKelamin(String nilai) {
        String nilaiBersih = nilai == null ? "" : nilai.trim();
        if (nilaiBersih.equalsIgnoreCase("L") || nilaiBersih.equalsIgnoreCase("Laki-laki")
                || nilaiBersih.equalsIgnoreCase("Laki-Laki") || nilaiBersih.equalsIgnoreCase("Laki laki")) {
            return "Laki laki";
        } else if (nilaiBersih.equalsIgnoreCase("P") || nilaiBersih.equalsIgnoreCase("Perempuan")) {
            return "Perempuan";
        }
        return nilaiBersih;
    }

    private String getKodeJenisKelamin() {
        Object nilai = Jk.getSelectedItem();
        return tampilJenisKelamin(nilai == null ? "" : nilai.toString());
    }

    private String getTglLahir() {
        return Valid.SetTgl(TglLahir.getSelectedItem() + "");
    }

    private void setText(javax.swing.text.JTextComponent teks, String nilai) {
        teks.setText(nilai == null ? "" : nilai);
    }

    private void setCombo(javax.swing.JComboBox combo, String nilai) {
        if (nilai == null || nilai.equals("")) {
            return;
        }
        for (int index = 0; index < combo.getItemCount(); index++) {
            Object item = combo.getItemAt(index);
            if (item != null && item.toString().equalsIgnoreCase(nilai)) {
                combo.setSelectedIndex(index);
                return;
            }
        }
        combo.setSelectedItem(nilai);
    }

    private void setFitWithRestrictions(String nilai, String keterangan) {
        String nilaiBersih = nilai == null ? "" : nilai.trim();
        setText(specify, "");
        if (nilaiBersih.equals("")) {
            if (fit_with_restrictions.getItemCount() > 0) {
                fit_with_restrictions.setSelectedIndex(0);
            }
            return;
        }
        for (int index = 0; index < fit_with_restrictions.getItemCount(); index++) {
            Object item = fit_with_restrictions.getItemAt(index);
            if (item != null && item.toString().equalsIgnoreCase(nilaiBersih)) {
                fit_with_restrictions.setSelectedIndex(index);
                if ("Specify".equalsIgnoreCase(nilaiBersih)) {
                    setText(specify, keterangan);
                }
                return;
            }
        }
        // Kompatibilitas data lama: teks bebas pernah disimpan langsung di
        // fit_with_restrictions.
        setCombo(fit_with_restrictions, "Specify");
        setText(specify, nilaiBersih);
    }

    private void setCek(javax.swing.AbstractButton cek, String nilai) {
        cek.setSelected("yes".equalsIgnoreCase(nilai) || "true".equalsIgnoreCase(nilai) || "1".equals(nilai));
    }

    private void kosongkanText(javax.swing.text.JTextComponent... daftarTeks) {
        for (javax.swing.text.JTextComponent teks : daftarTeks) {
            teks.setText("");
        }
    }

    private void resetCombo(javax.swing.JComboBox... daftarCombo) {
        for (javax.swing.JComboBox combo : daftarCombo) {
            if (combo.getItemCount() > 0) {
                combo.setSelectedIndex(0);
            }
        }
    }

    private void resetCek(javax.swing.AbstractButton... daftarCek) {
        for (javax.swing.AbstractButton cek : daftarCek) {
            cek.setSelected(false);
        }
    }

    private String getTabelValue(String kolom) {
        return getTabelValue(kolom, tbObat.getSelectedRow());
    }

    private String getTabelValue(String kolom, int baris) {
        if (baris < 0) {
            return "";
        }
        int index = indexKolomTabel(kolom);
        if (index < 0) {
            return "";
        }
        int viewColumn = tbObat.convertColumnIndexToView(index);
        if (viewColumn < 0) {
            return "";
        }
        Object nilai = tbObat.getValueAt(baris, viewColumn);
        return nilai == null ? "" : nilai.toString();
    }

    private int indexKolomTabel(String kolom) {
        if("year".equals(kolom)) return 0;
        if("no_rawat".equals(kolom)) return 1;
        if("no_rkm_medis".equals(kolom)) return 2;
        if("nama_pasien".equals(kolom)) return 3;
        if("surname".equals(kolom)) return 4;
        if("perusahaan_pasien".equals(kolom)) return 5;
        if("nip".equals(kolom)) return 6;
        if("alamat_pasien".equals(kolom)) return 7;
        if("tanggal".equals(kolom)) return 8;
        if("kd_dokter".equals(kolom)) return 9;
        if("nm_dokter".equals(kolom)) return 10;
        if("kd_petugas".equals(kolom)) return 11;
        if("nm_petugas".equals(kolom)) return 12;
        if("kd_petugas_lab".equals(kolom)) return 13;
        if("nm_petugas_lab".equals(kolom)) return 14;
        if("mcu_group".equals(kolom)) return 16;
        if("dass_21".equals(kolom)) return 17;
        if("phy_exam".equals(kolom)) return 18;
        if("conc_lab".equals(kolom)) return 19;
        if("conc_radiologi".equals(kolom)) return 20;
        if("conc_ecg".equals(kolom)) return 21;
        if("conc_spirometry".equals(kolom)) return 22;
        if("conc_audiometry".equals(kolom)) return 23;
        if("kesimpulan1".equals(kolom)) return 24;
        if("tmp_lahir".equals(kolom)) return 25;
        if("tgl_lahir".equals(kolom)) return 26;
        if("jk".equals(kolom)) return 27;
        if("no_tlp".equals(kolom)) return 28;
        if("suku_bangsa".equals(kolom)) return 29;
        if("stts_nikah".equals(kolom)) return 30;
        if("doe".equals(kolom)) return 31;
        if("yoe".equals(kolom)) return 32;
        if("job_title".equals(kolom)) return 33;
        if("activities".equals(kolom)) return 34;
        if("hobby".equals(kolom)) return 35;
        if("other_job".equals(kolom)) return 36;
        if("posisi_kerja".equals(kolom)) return 37;
        if("departemen".equals(kolom)) return 38;
        if("supervisor".equals(kolom)) return 39;
        if("manager".equals(kolom)) return 40;
        if("job_involves_driving_or_operating_mobile_equipment".equals(kolom)) return 41;
        if("job_involves_working_at_heights".equals(kolom)) return 42;
        if("job_involves_clerical_office_based_or_administrative".equals(kolom)) return 43;
        if("job_involves_requires_colour_vision".equals(kolom)) return 44;
        if("job_involves_potential_dust_exposure".equals(kolom)) return 45;
        if("job_involves_catering_staff_including_food_handlers".equals(kolom)) return 46;
        if("job_involves_exposing_to_other_potential_dangerous".equals(kolom)) return 47;
        if("med_hist_head_injury_or_contussion".equals(kolom)) return 48;
        if("med_hist_fainting_blackouts_epilepsy".equals(kolom)) return 49;
        if("med_hist_visual_changes".equals(kolom)) return 50;
        if("med_hist_hearing_loss".equals(kolom)) return 51;
        if("med_hist_nose_sinus_throat_trouble_more_4_weeks".equals(kolom)) return 52;
        if("med_hist_gynaecological_problems".equals(kolom)) return 53;
        if("med_hist_chronic_skin_problem".equals(kolom)) return 54;
        if("med_hist_chronic_diarrhea".equals(kolom)) return 55;
        if("med_hist_anorexia_more_4_weeks".equals(kolom)) return 56;
        if("med_hist_gastritis".equals(kolom)) return 57;
        if("med_hist_jaundice_hepatitis".equals(kolom)) return 58;
        if("med_hist_chronic_cough_more_4_weeks".equals(kolom)) return 59;
        if("med_hist_haemorhoid".equals(kolom)) return 60;
        if("med_hist_chronic_abdominal_pain".equals(kolom)) return 61;
        if("med_hist_diabetes".equals(kolom)) return 62;
        if("med_hist_asthma".equals(kolom)) return 63;
        if("med_hist_allergies".equals(kolom)) return 64;
        if("med_hist_tuberculosis_bronchitis".equals(kolom)) return 65;
        if("med_hist_psychiatric_disorder".equals(kolom)) return 66;
        if("med_hist_sexual_transmitted_diseases".equals(kolom)) return 67;
        if("med_hist_unusual_change_of_weight_more_5kg_per_month".equals(kolom)) return 68;
        if("med_hist_hypertension".equals(kolom)) return 69;
        if("med_hist_chest_pain_heart_disease".equals(kolom)) return 70;
        if("med_hist_malaria_tropical_disease".equals(kolom)) return 71;
        if("med_hist_surgery_operation".equals(kolom)) return 72;
        if("med_hist_back_pain_more_4_weeks".equals(kolom)) return 73;
        if("med_hist_thypoid_fever".equals(kolom)) return 74;
        if("med_hist_swollen_or_painful_joints".equals(kolom)) return 75;
        if("med_hist_kidney_problem_urinary_stones".equals(kolom)) return 76;
        if("med_hist_other_chronical_diseases".equals(kolom)) return 77;
        if("family_history_father".equals(kolom)) return 78;
        if("family_history_mother".equals(kolom)) return 79;
        if("family_history_siblings".equals(kolom)) return 80;
        if("family_history_other".equals(kolom)) return 81;
        if("cigarettes_perday".equals(kolom)) return 82;
        if("alcohol_gr_week".equals(kolom)) return 83;
        if("prescribed_medication".equals(kolom)) return 84;
        if("prescribed_medication_2".equals(kolom)) return 85;
        if("any_allergies".equals(kolom)) return 86;
        if("hb".equals(kolom)) return 87;
        if("wbc".equals(kolom)) return 88;
        if("esr".equals(kolom)) return 89;
        if("bl_group".equals(kolom)) return 90;
        if("gamaa_gt".equals(kolom)) return 91;
        if("sgot".equals(kolom)) return 92;
        if("sgpt".equals(kolom)) return 93;
        if("urea".equals(kolom)) return 94;
        if("creatinin".equals(kolom)) return 95;
        if("glucose".equals(kolom)) return 96;
        if("random_glucose".equals(kolom)) return 97;
        if("total_cholestrol".equals(kolom)) return 98;
        if("protein".equals(kolom)) return 99;
        if("blood".equals(kolom)) return 100;
        if("bilirubin".equals(kolom)) return 101;
        if("malaria".equals(kolom)) return 102;
        if("tpha".equals(kolom)) return 103;
        if("mantoux_test".equals(kolom)) return 104;
        if("leukosit".equals(kolom)) return 105;
        if("lab_others".equals(kolom)) return 106;
        if("ova".equals(kolom)) return 107;
        if("culture".equals(kolom)) return 108;
        if("cysta".equals(kolom)) return 109;
        if("parasites1".equals(kolom)) return 110;
        if("pnemunosicosis".equals(kolom)) return 111;
        if("pnemunosicosis2".equals(kolom)) return 112;
        if("ILO_clasification".equals(kolom)) return 113;
        if("ILO_clasification2".equals(kolom)) return 114;
        if("oth_abnormal".equals(kolom)) return 115;
        if("tb1".equals(kolom)) return 116;
        if("tb2".equals(kolom)) return 117;
        if("page3_comment".equals(kolom)) return 118;
        if("td".equals(kolom)) return 119;
        if("nadi".equals(kolom)) return 120;
        if("rr".equals(kolom)) return 121;
        if("tb".equals(kolom)) return 122;
        if("bb".equals(kolom)) return 123;
        if("bmi".equals(kolom)) return 124;
        if("kasifikasi_bmi".equals(kolom)) return 125;
        if("laborat".equals(kolom)) return 126;
        if("radiologi".equals(kolom)) return 127;
        if("ekg".equals(kolom)) return 128;
        if("spirometri_vc_1".equals(kolom)) return 129;
        if("spirometri_vc_2".equals(kolom)) return 130;
        if("spirometri_vc_3".equals(kolom)) return 131;
        if("spirometri_vc_4".equals(kolom)) return 132;
        if("spirometri_fvc_1".equals(kolom)) return 133;
        if("spirometri_fvc_2".equals(kolom)) return 134;
        if("spirometri_fvc_3".equals(kolom)) return 135;
        if("spirometri_fvc_4".equals(kolom)) return 136;
        if("spirometri_fev_1_1".equals(kolom)) return 137;
        if("spirometri_fev_1_2".equals(kolom)) return 138;
        if("spirometri_fev_1_3".equals(kolom)) return 139;
        if("spirometri_fev_1_4".equals(kolom)) return 140;
        if("spirometri_fev_1_fvc_1".equals(kolom)) return 141;
        if("spirometri_fev_1_fvc_2".equals(kolom)) return 142;
        if("spirometri_fev_1_fvc_3".equals(kolom)) return 143;
        if("spirometri_fev_1_fvc_4".equals(kolom)) return 144;
        if("audiometri_tinitus_never".equals(kolom)) return 145;
        if("audiometri_tinitus_previously".equals(kolom)) return 146;
        if("audiometri_tinitus_rarely".equals(kolom)) return 147;
        if("audiometri_tinitus_often".equals(kolom)) return 148;
        if("audiometri_tinitus_always".equals(kolom)) return 149;
        if("audiometri_ear_protection_worn_never".equals(kolom)) return 150;
        if("audiometri_ear_protection_worn_previously".equals(kolom)) return 151;
        if("audiometri_ear_protection_worn_rarely".equals(kolom)) return 152;
        if("audiometri_ear_protection_worn_often".equals(kolom)) return 153;
        if("audiometri_ear_protection_worn_always".equals(kolom)) return 154;
        if("type_of_hearing".equals(kolom)) return 155;
        if("audiometri_left_ear_500_AB".equals(kolom)) return 156;
        if("audiometri_left_ear_1000_AB".equals(kolom)) return 157;
        if("audiometri_left_ear_1500_AB".equals(kolom)) return 158;
        if("audiometri_left_ear_2000_AB".equals(kolom)) return 159;
        if("audiometri_left_ear_3000_AB".equals(kolom)) return 160;
        if("audiometri_left_ear_4000_AB".equals(kolom)) return 161;
        if("audiometri_left_ear_5000_AB".equals(kolom)) return 162;
        if("audiometri_left_ear_6000_AB".equals(kolom)) return 163;
        if("audiometri_left_ear_500_AC".equals(kolom)) return 164;
        if("audiometri_left_ear_1000_AC".equals(kolom)) return 165;
        if("audiometri_left_ear_1500_AC".equals(kolom)) return 166;
        if("audiometri_left_ear_2000_AC".equals(kolom)) return 167;
        if("audiometri_left_ear_3000_AC".equals(kolom)) return 168;
        if("audiometri_left_ear_4000_AC".equals(kolom)) return 169;
        if("audiometri_left_ear_5000_AC".equals(kolom)) return 170;
        if("audiometri_left_ear_6000_AC".equals(kolom)) return 171;
        if("audiometri_right_ear_500_ab".equals(kolom)) return 172;
        if("audiometri_right_ear_1000_ab".equals(kolom)) return 173;
        if("audiometri_right_ear_1500_ab".equals(kolom)) return 174;
        if("audiometri_right_ear_2000_ab".equals(kolom)) return 175;
        if("audiometri_right_ear_3000_ab".equals(kolom)) return 176;
        if("audiometri_right_ear_4000_ab".equals(kolom)) return 177;
        if("audiometri_right_ear_5000_ab".equals(kolom)) return 178;
        if("audiometri_right_ear_6000_ab".equals(kolom)) return 179;
        if("audiometri_right_ear_500_ac".equals(kolom)) return 180;
        if("audiometri_right_ear_1000_ac".equals(kolom)) return 181;
        if("audiometri_right_ear_1500_ac".equals(kolom)) return 182;
        if("audiometri_right_ear_2000_ac".equals(kolom)) return 183;
        if("audiometri_right_ear_3000_ac".equals(kolom)) return 184;
        if("audiometri_right_ear_4000_ac".equals(kolom)) return 185;
        if("audiometri_right_ear_5000_ac".equals(kolom)) return 186;
        if("audiometri_right_ear_6000_ac".equals(kolom)) return 187;
        if("eye_unaided_distant_r".equals(kolom)) return 188;
        if("eye_unaided_distant_l".equals(kolom)) return 189;
        if("eye_glasses_distant_r".equals(kolom)) return 190;
        if("eye_glasses_distant_l".equals(kolom)) return 191;
        if("eye_unaided_near_r".equals(kolom)) return 192;
        if("eye_unaided_near_l".equals(kolom)) return 193;
        if("eye_glasses_near_r".equals(kolom)) return 194;
        if("eye_glasses_near_l".equals(kolom)) return 195;
        if("eye_night_vision_1".equals(kolom)) return 196;
        if("eye_night_vision_2".equals(kolom)) return 197;
        if("eye_brake_test_1".equals(kolom)) return 198;
        if("eye_brake_test_2".equals(kolom)) return 199;
        if("eye_color_blindless".equals(kolom)) return 200;
        if("visual_fields_left".equals(kolom)) return 201;
        if("visual_fields_right".equals(kolom)) return 202;
        if("fundi".equals(kolom)) return 203;
        if("imunisasi_bcg".equals(kolom)) return 204;
        if("imunisasi_dpt".equals(kolom)) return 205;
        if("imunisasi_polio".equals(kolom)) return 206;
        if("imunisasi_morbili".equals(kolom)) return 207;
        if("imunisasi_thyphoid".equals(kolom)) return 208;
        if("imunisasi_hep_a".equals(kolom)) return 209;
        if("imunisasi_hep_b".equals(kolom)) return 210;
        if("imunisasi_tetanus".equals(kolom)) return 211;
        if("imunisasi_others".equals(kolom)) return 212;
        if("vertebra_scoliosis".equals(kolom)) return 213;
        if("vertebra_kyphosis".equals(kolom)) return 214;
        if("vertebra_lordosis".equals(kolom)) return 215;
        if("vertebra_forward_flexion_0_80".equals(kolom)) return 216;
        if("vertebra_hyperextensi_0_25".equals(kolom)) return 217;
        if("vertebra_lateral_flexion_0_20".equals(kolom)) return 218;
        if("vertebra_heel_walking".equals(kolom)) return 219;
        if("vertebra_toe_walking".equals(kolom)) return 220;
        if("vertebra_squats_x3".equals(kolom)) return 221;
        if("exam_ent_comments".equals(kolom)) return 222;
        if("exam_cardio_vascular_system_comments".equals(kolom)) return 223;
        if("exam_respiratory_system_comments".equals(kolom)) return 224;
        if("exam_abdomen_comments".equals(kolom)) return 225;
        if("exam_genito_urinary_system_comments".equals(kolom)) return 226;
        if("exam_central_peripheral_nervous_system_comments".equals(kolom)) return 227;
        if("exam_skin_comments".equals(kolom)) return 228;
        if("exam_lymph_nodes_comments".equals(kolom)) return 229;
        if("exam_dental_comments".equals(kolom)) return 230;
        if("exam_dental_muskulo".equals(kolom)) return 231;
        if("conclusion_requires_spectacles".equals(kolom)) return 232;
        if("conclusion_colour_blindness".equals(kolom)) return 233;
        if("conclusion_respiratory_problem".equals(kolom)) return 234;
        if("conclusion_impaired_hearing".equals(kolom)) return 235;
        if("conclusion_vertigo".equals(kolom)) return 236;
        if("blood_group".equals(kolom)) return 237;
        if("medically_fit".equals(kolom)) return 238;
        if("fit_with_restrictions".equals(kolom)) return 239;
        if("specify".equals(kolom)) return 240;
        if("unfit_comment_1".equals(kolom)) return 241;
        if("trombosit".equals(kolom)) return 242;
        if("rhesuss".equals(kolom)) return 243;
        if("triglyceride".equals(kolom)) return 244;
        if("hdl_cholesterol".equals(kolom)) return 245;
        if("ldl_cholesterol".equals(kolom)) return 246;
        if("uric_acid".equals(kolom)) return 247;
        if("urine_colour".equals(kolom)) return 248;
        if("urine_turbidity".equals(kolom)) return 249;
        if("urine_chemical_reaction".equals(kolom)) return 250;
        if("urine_ketones".equals(kolom)) return 251;
        if("urine_glucose".equals(kolom)) return 252;
        if("urine_nitrites".equals(kolom)) return 253;
        if("urine_wbc".equals(kolom)) return 254;
        if("urine_rbc".equals(kolom)) return 255;
        if("urine_bacteria".equals(kolom)) return 256;
        if("urine_crystal".equals(kolom)) return 257;
        if("urine_epithel".equals(kolom)) return 258;
        if("hbsag".equals(kolom)) return 259;
        if("anti_hbs".equals(kolom)) return 260;
        if("cea".equals(kolom)) return 261;
        if("afp".equals(kolom)) return 262;
        if("drug_amphetamine".equals(kolom)) return 263;
        if("drug_methamphetamine".equals(kolom)) return 264;
        if("drug_morphine".equals(kolom)) return 265;
        if("drug_benzodiazepine".equals(kolom)) return 266;
        if("drug_cocain".equals(kolom)) return 267;
        if("drug_marijuana".equals(kolom)) return 268;
        if("nm_poli".equals(kolom)) return 269;
        return -1;
    }

    private boolean isKolomSembunyiUi(String header) {
        for (String kolomTampil : URUTAN_KOLOM_TAMPIL_MCU) {
            if (kolomTampil.equals(header)) return false;
        }
        return true;
    }

    private int getLebarKolomMcu(String header) {
        if ("Tahun".equals(header)) return 40;
        if ("No.Rawat".equals(header)) return 105;
        if ("No.RM".equals(header)) return 65;
        if ("Nama Pasien".equals(header)) return 200;
        if ("Surname".equals(header)) return 100;
        if ("Perusahaan".equals(header)) return 200;
        if ("Badge".equals(header)) return 55;
        if ("Tanggal".equals(header)) return 120;
        if ("Unit/Poliklinik".equals(header)) return 150;
        if ("Dokter PJ".equals(header)) return 200;
        if ("Saran".equals(header)) return 200;
        if ("MCU Grup".equals(header)) return 80;
        if ("Medical History".equals(header)) return 85;
        if ("Physical Examination".equals(header)) return 170;
        if ("Kesimpulan Laboratorium".equals(header)) return 180;
        if ("Conclusion Chest X Ray".equals(header)) return 180;
        if ("Conclusion EKG".equals(header)) return 130;
        if ("Conclusion Spirometry".equals(header)) return 180;
        if ("Conclusion Audiometry".equals(header)) return 180;
        if ("Kesimpulan".equals(header)) return 260;
        if ("Pemeriksaan Laboratorium".equals(header)) return 180;
        if ("ECG Abnormal".equals(header)) return 180;
        return 90;
    }

    private String selectNamaPenanggungJawab() {
        return "ifnull(nullif(perusahaan_pasien.nama_perusahaan,''),pasien.perusahaan_pasien)";
    }

    private String[] getNilaiPenilaianMcu() {
        return new String[] {
                TNoRw.getText(), getTanggalAsuhan(), getYearMcu(), KdDokter1.getText(), KdPetugas.getText(),
                nilaiCatatanMcu(), TPasien.getText(), surname.getText(), nilaiCombo(McuGroup), nilai(Dass21),
                nilai(PhyExam), nilaiLaboratMcu(), nilai(ConcRadiologi),
                nilaiCombo(cbConcEcg), nilai(ConcSpirometry), nilai(ConcAudiometry), nilaiKesimpulanMcu(),
                TNoRM.getText(), TmpLahir.getText(), getTglLahir(), getKodeJenisKelamin(),
                NoTlp.getText(), SukuBangsa.getText(), nilaiCombo(SttsNikah), Doe.getText(), Yoe.getText(),
                JobTitle.getText(), Activities.getText(), Hobby.getText(), OtherJob.getText(), nilaiCombo(PosisiKerja),
                nilaiCekTunggal(departemen), nilaiCekTunggal(supervisor), nilaiCekTunggal(manager),
                nilaiCek(JobInvolvesDrivingOrOperatingMobileEquipment), nilaiCek(JobInvolvesWorkingAtHeights),
                nilaiCek(JobInvolvesClericalOfficeBasedOrAdministrative),
                nilaiCek(JobInvolvesRequiresColourVision), nilaiCek(JobInvolvesPotentialDustExposure),
                nilaiCek(JobInvolvesCateringStaffIncludingFoodHandlers),
                nilaiCek(JobInvolvesExposingToOtherPotentialDangerous), nilaiCek(RWP1), nilaiCek(RWP2), nilaiCek(RWP3),
                nilaiCek(RWP4), nilaiCek(RWP5), nilaiCek(RWP6),
                nilaiCek(RWP7), nilaiCek(RWP8), nilaiCek(RWP9), nilaiCek(RWP10), nilaiCek(RWP11), nilaiCek(RWP12),
                nilaiCek(RWP13), nilaiCek(RWP14),
                nilaiCek(RWP15), nilaiCek(RWP16), nilaiCek(RWP17), nilaiCek(RWP18), nilaiCek(RWP19), nilaiCek(RWP20),
                nilaiCek(RWP21), nilaiCek(RWP22),
                nilaiCek(RWP23), nilaiCek(RWP24), nilaiCek(RWP25), nilaiCek(RWP26), nilaiCek(RWP27), nilaiCek(RWP28),
                nilaiCek(RWP29), nilaiCek(RWP30),
                FamilyHistoryFather.getText(), FamilyHistoryMother.getText(), FamilyHistorySiblings.getText(),
                FamilyHistoryOther.getText(), CigarettesPerday.getText(), AlcoholGrWeek.getText(),
                PrescribedMedication.getText(), PrescribedMedication2.getText(), AnyAllergies.getText(), hb.getText(),
                wbc.getText(), esr.getText(), bl_group.getText(), gamaa_gt.getText(),
                sgot.getText(), sgpt.getText(), urea.getText(), creatinin.getText(), glucose.getText(),
                random_glucose.getText(), total_cholestrol.getText(), protein.getText(), blood.getText(),
                bilirubin.getText(), malaria.getText(), tpha.getText(), mantoux_test.getText(), leukosit.getText(),
                lab_others.getText(), ova.getText(), culture.getText(), cysta.getText(),
                parasites1.getText(), pnemunosicosis.getText(), pnemunosicosis2.getText(), ILO_clasification.getText(),
                ILO_clasification2.getText(), oth_abnormal.getText(), tb1.getText(), tb2.getText(),
                page3_comment.getText(),
                TD.getText(), Nadi.getText(), RR.getText(), TB.getText(), BB.getText(), IMT.getText(),
                KlasifikasiIMT1.getText(),
                nilaiLaboratMcu(), nilai(RongsenThorax), nilaiEkgMcu(), spirometri_vc_1.getText(),
                spirometri_vc_2.getText(), spirometri_vc_3.getText(),
                spirometri_vc_4.getText(), spirometri_fvc_1.getText(), spirometri_fvc_2.getText(),
                spirometri_fvc_3.getText(), spirometri_fvc_4.getText(), spirometri_fev_1_1.getText(),
                spirometri_fev_1_2.getText(), spirometri_fev_1_3.getText(), spirometri_fev_1_4.getText(),
                spirometri_fev_1_fvc_1.getText(), spirometri_fev_1_fvc_2.getText(), spirometri_fev_1_fvc_3.getText(),
                spirometri_fev_1_fvc_4.getText(), nilaiCek(audiometri_tinitus_never),
                nilaiCek(audiometri_tinitus_previously), nilaiCek(audiometri_tinitus_rarely),
                nilaiCek(audiometri_tinitus_often), nilaiCek(audiometri_tinitus_always),
                nilaiCek(audiometri_ear_protection_worn_never),
                nilaiCek(audiometri_ear_protection_worn_previously), nilaiCek(audiometri_ear_protection_worn_rarely),
                nilaiCek(audiometri_ear_protection_worn_often), nilaiCek(audiometri_ear_protection_worn_always),
                type_of_hearing.getText(), audiometri_left_ear_500.getText(), audiometri_left_ear_1000.getText(),
                audiometri_left_ear_1500.getText(), audiometri_left_ear_2000.getText(),
                audiometri_left_ear_3000.getText(), audiometri_left_ear_4000.getText(),
                audiometri_left_ear_5000.getText(),
                audiometri_left_ear_6000.getText(), audiometri_left_ear_501.getText(),
                audiometri_left_ear_1001.getText(), audiometri_left_ear_1501.getText(),
                audiometri_left_ear_2001.getText(),
                audiometri_left_ear_3001.getText(), audiometri_left_ear_4001.getText(),
                audiometri_left_ear_5001.getText(), audiometri_left_ear_6001.getText(),
                audiometri_left_ear_502.getText(), audiometri_left_ear_1002.getText(),
                audiometri_left_ear_1502.getText(), audiometri_left_ear_2002.getText(),
                audiometri_left_ear_3002.getText(), audiometri_left_ear_4002.getText(),
                audiometri_left_ear_5002.getText(), audiometri_left_ear_6002.getText(),
                audiometri_left_ear_503.getText(), audiometri_left_ear_1003.getText(),
                audiometri_left_ear_1503.getText(), audiometri_left_ear_2003.getText(),
                audiometri_left_ear_3003.getText(), audiometri_left_ear_4003.getText(),
                audiometri_left_ear_5003.getText(), audiometri_left_ear_6003.getText(),
                eye_unaided_distant_r.getText(), eye_unaided_distant_l.getText(), eye_glasses_distant_r.getText(),
                eye_glasses_distant_l.getText(), eye_unaided_near_r.getText(),
                eye_unaided_near_l.getText(), eye_glasses_near_r.getText(), eye_glasses_near_l.getText(),
                eye_night_vision_1.getText(), eye_night_vision_2.getText(),
                eye_unaided_near_r1.getText(), eye_brake_test_2.getText(),
                nilaiEyeColorBlindless(nilaiCombo(eye_color_blindless)), visual_fields_left.getText(),
                visual_fields_right.getText(), nilaiCombo(fundi), nilaiCombo(imunisasi_bcg),
                nilaiCombo(imunisasi_dpt), nilaiCombo(imunisasi_polio), nilaiCombo(imunisasi_morbili),
                nilaiCombo(imunisasi_thyphoid), nilaiCombo(imunisasi_hep_a),
                nilaiCombo(imunisasi_hep_b), nilaiCombo(imunisasi_tetanus), nilaiCombo(imunisasi_others),
                nilaiCombo(vertebra_scoliosis), nilaiCombo(vertebra_kyphosis),
                nilaiCombo(vertebra_lordosis), nilaiCombo(vertebra_forward_flexion_0_80),
                nilaiCombo(vertebra_hyperextensi_0_25), nilaiCombo(vertebra_lateral_flexion_0_20),
                nilaiCombo(vertebra_heel_walking),
                nilaiCombo(vertebra_toe_walking), nilaiCombo(vertebra_squats_x3), exam_ent_comments.getText(),
                exam_cardio_vascular_system_comments.getText(), exam_respiratory_system_comments.getText(),
                exam_abdomen_comments.getText(), exam_genito_urinary_system_comments.getText(),
                exam_central_peripheral_nervous_system_comments.getText(), exam_skin_comments.getText(),
                exam_lymph_nodes_comments.getText(), exam_dental_comments.getText(), exam_dental_muskulo.getText(),
                nilaiComboYesNo(conclusion_requires_spectacles), nilaiComboYesNo(conclusion_colour_blindness),
                nilaiComboYesNo(conclusion_respiratory_problem),
                nilaiComboYesNo(conclusion_impaired_hearing), nilaiComboYesNo(conclusion_vertigo), nilaiBloodGroupMcu(),
                nilaiCombo(fit), nilaiFitWithRestrictions(), nilaiSpecify(), nilaiSaranMcu(),
                nilai(trombosit), nilai(rhesuss), nilai(triglyceride), nilai(hdl_cholesterol), nilai(ldl_cholesterol),
                nilai(uric_acid),
                nilai(urine_colour), nilai(urine_turbidity), nilai(urine_chemical_reaction), nilai(urine_ketones),
                nilai(urine_glucose),
                nilai(urine_nitrites), nilai(urine_wbc), nilai(urine_rbc), nilai(urine_bacteria), nilai(urine_crystal),
                nilai(urine_epithel),
                nilai(hbsag), nilai(anti_hbs), nilai(cea), nilai(afp), nilai(drug_amphetamine),
                nilai(drug_methamphetamine),
                nilai(drug_morphine), nilai(drug_benzodiazepine), nilai(drug_cocain), nilai(drug_marijuana)
        };
    }

    private String[] getNilaiUbahPenilaianMcu() {
        String[] nilai = getNilaiPenilaianMcu();
        String[] ubah = new String[nilai.length];
        System.arraycopy(nilai, 1, ubah, 0, nilai.length - 1);
        ubah[ubah.length - 1] = getTabelValue("no_rawat").equals("") ? TNoRw.getText() : getTabelValue("no_rawat");
        return ubah;
    }

    private void tampil() {
        Valid.tabelKosong(tabMode);
        try {
            String sql = selectPenilaianMcu() + "where penilaian_mcu.tanggal between ? and ? ";
            if (!TCari.getText().equals("")) {
                sql = sql
                        + "and (penilaian_mcu.no_rawat like ? or pasien.no_rkm_medis like ? or pasien.nm_pasien like ? or penilaian_mcu.surname like ? or pasien.perusahaan_pasien like ? or perusahaan_pasien.nama_perusahaan like ? or pasien.nip like ? or penilaian_mcu.kd_dokter like ? or dokter.nm_dokter like ? or penilaian_mcu.kd_petugas like ? or petugas.nama like ?) ";
            }
            sql = sql + "order by penilaian_mcu.tanggal";
            ps = koneksi.prepareStatement(sql);
            try {
                ps.setString(1, Valid.SetTgl(DTPCari1.getSelectedItem() + ""));
                ps.setString(2, Valid.SetTgl(DTPCari2.getSelectedItem() + ""));
                if (!TCari.getText().equals("")) {
                    for (int index = 3; index <= 13; index++) {
                        ps.setString(index, "%" + TCari.getText() + "%");
                    }
                }
                rs = ps.executeQuery();
                while (rs.next()) {
                    tabMode.addRow(new Object[] {
                            rs.getString("year"), rs.getString("no_rawat"), rs.getString("no_rkm_medis"),
                            rs.getString("nama_pasien"), rs.getString("surname"),
                            rs.getString("perusahaan_pasien"), rs.getString("nip"), rs.getString("alamat_pasien"),
                            rs.getString("tanggal"), rs.getString("kd_dokter"), rs.getString("nm_dokter"),
                            rs.getString("kd_petugas"), rs.getString("nm_petugas"), rs.getString("kd_petugas_lab"),
                            rs.getString("nm_petugas_lab"),
                            rs.getString("unfit_comment_1"), rs.getString("mcu_group"), rs.getString("dass_21"),
                            rs.getString("phy_exam"),
                            rs.getString("conc_lab"), rs.getString("conc_radiologi"), rs.getString("conc_ecg"),
                            rs.getString("conc_spirometry"), rs.getString("conc_audiometry"),
                            rs.getString("kesimpulan1"), rs.getString("tmp_lahir"), rs.getString("tgl_lahir"),
                            rs.getString("jk"), rs.getString("no_tlp"), rs.getString("suku_bangsa"),
                            rs.getString("stts_nikah"), rs.getString("doe"), rs.getString("yoe"),
                            rs.getString("job_title"), rs.getString("activities"), rs.getString("hobby"),
                            rs.getString("other_job"), rs.getString("posisi_kerja"), rs.getString("departemen"),
                            rs.getString("supervisor"), rs.getString("manager"),
                            rs.getString("job_involves_driving_or_operating_mobile_equipment"),
                            rs.getString("job_involves_working_at_heights"),
                            rs.getString("job_involves_clerical_office_based_or_administrative"),
                            rs.getString("job_involves_requires_colour_vision"),
                            rs.getString("job_involves_potential_dust_exposure"),
                            rs.getString("job_involves_catering_staff_including_food_handlers"),
                            rs.getString("job_involves_exposing_to_other_potential_dangerous"),
                            rs.getString("med_hist_head_injury_or_contussion"),
                            rs.getString("med_hist_fainting_blackouts_epilepsy"),
                            rs.getString("med_hist_visual_changes"), rs.getString("med_hist_hearing_loss"),
                            rs.getString("med_hist_nose_sinus_throat_trouble_more_4_weeks"),
                            rs.getString("med_hist_gynaecological_problems"),
                            rs.getString("med_hist_chronic_skin_problem"), rs.getString("med_hist_chronic_diarrhea"),
                            rs.getString("med_hist_anorexia_more_4_weeks"),
                            rs.getString("med_hist_gastritis"), rs.getString("med_hist_jaundice_hepatitis"),
                            rs.getString("med_hist_chronic_cough_more_4_weeks"),
                            rs.getString("med_hist_haemorhoid"), rs.getString("med_hist_chronic_abdominal_pain"),
                            rs.getString("med_hist_diabetes"), rs.getString("med_hist_asthma"),
                            rs.getString("med_hist_allergies"), rs.getString("med_hist_tuberculosis_bronchitis"),
                            rs.getString("med_hist_psychiatric_disorder"),
                            rs.getString("med_hist_sexual_transmitted_diseases"),
                            rs.getString("med_hist_unusual_change_of_weight_more_5kg_per_month"),
                            rs.getString("med_hist_hypertension"), rs.getString("med_hist_chest_pain_heart_disease"),
                            rs.getString("med_hist_malaria_tropical_disease"),
                            rs.getString("med_hist_surgery_operation"), rs.getString("med_hist_back_pain_more_4_weeks"),
                            rs.getString("med_hist_thypoid_fever"),
                            rs.getString("med_hist_swollen_or_painful_joints"),
                            rs.getString("med_hist_kidney_problem_urinary_stones"),
                            rs.getString("med_hist_other_chronical_diseases"), rs.getString("family_history_father"),
                            rs.getString("family_history_mother"),
                            rs.getString("family_history_siblings"), rs.getString("family_history_other"),
                            rs.getString("cigarettes_perday"), rs.getString("alcohol_gr_week"),
                            rs.getString("prescribed_medication"), rs.getString("prescribed_medication_2"),
                            rs.getString("any_allergies"), rs.getString("hb"), rs.getString("wbc"),
                            rs.getString("esr"), rs.getString("bl_group"), rs.getString("gamaa_gt"),
                            rs.getString("sgot"), rs.getString("sgpt"), rs.getString("urea"),
                            rs.getString("creatinin"), rs.getString("glucose"), rs.getString("random_glucose"),
                            rs.getString("total_cholestrol"), rs.getString("protein"),
                            rs.getString("blood"), rs.getString("bilirubin"), rs.getString("malaria"),
                            rs.getString("tpha"), rs.getString("mantoux_test"), rs.getString("leukosit"),
                            rs.getString("lab_others"), rs.getString("ova"), rs.getString("culture"),
                            rs.getString("cysta"), rs.getString("parasites1"), rs.getString("pnemunosicosis"),
                            rs.getString("pnemunosicosis2"), rs.getString("ILO_clasification"),
                            rs.getString("ILO_clasification2"), rs.getString("oth_abnormal"), rs.getString("tb1"),
                            rs.getString("tb2"), rs.getString("page3_comment"), rs.getString("td"),
                            rs.getString("nadi"), rs.getString("rr"), rs.getString("tb"), rs.getString("bb"),
                            rs.getString("bmi"), rs.getString("kasifikasi_bmi"), rs.getString("laborat"),
                            rs.getString("radiologi"), rs.getString("ekg"),
                            rs.getString("spirometri_vc_1"), rs.getString("spirometri_vc_2"),
                            rs.getString("spirometri_vc_3"), rs.getString("spirometri_vc_4"),
                            rs.getString("spirometri_fvc_1"), rs.getString("spirometri_fvc_2"),
                            rs.getString("spirometri_fvc_3"), rs.getString("spirometri_fvc_4"),
                            rs.getString("spirometri_fev_1_1"), rs.getString("spirometri_fev_1_2"),
                            rs.getString("spirometri_fev_1_3"), rs.getString("spirometri_fev_1_4"),
                            rs.getString("spirometri_fev_1_fvc_1"), rs.getString("spirometri_fev_1_fvc_2"),
                            rs.getString("spirometri_fev_1_fvc_3"),
                            rs.getString("spirometri_fev_1_fvc_4"), rs.getString("audiometri_tinitus_never"),
                            rs.getString("audiometri_tinitus_previously"),
                            rs.getString("audiometri_tinitus_rarely"), rs.getString("audiometri_tinitus_often"),
                            rs.getString("audiometri_tinitus_always"),
                            rs.getString("audiometri_ear_protection_worn_never"),
                            rs.getString("audiometri_ear_protection_worn_previously"),
                            rs.getString("audiometri_ear_protection_worn_rarely"),
                            rs.getString("audiometri_ear_protection_worn_often"),
                            rs.getString("audiometri_ear_protection_worn_always"), rs.getString("type_of_hearing"),
                            rs.getString("audiometri_left_ear_500_AB"),
                            rs.getString("audiometri_left_ear_1000_AB"), rs.getString("audiometri_left_ear_1500_AB"),
                            rs.getString("audiometri_left_ear_2000_AB"),
                            rs.getString("audiometri_left_ear_3000_AB"), rs.getString("audiometri_left_ear_4000_AB"),
                            rs.getString("audiometri_left_ear_5000_AB"),
                            rs.getString("audiometri_left_ear_6000_AB"), rs.getString("audiometri_left_ear_500_AC"),
                            rs.getString("audiometri_left_ear_1000_AC"),
                            rs.getString("audiometri_left_ear_1500_AC"), rs.getString("audiometri_left_ear_2000_AC"),
                            rs.getString("audiometri_left_ear_3000_AC"),
                            rs.getString("audiometri_left_ear_4000_AC"), rs.getString("audiometri_left_ear_5000_AC"),
                            rs.getString("audiometri_left_ear_6000_AC"),
                            rs.getString("audiometri_right_ear_500_ab"), rs.getString("audiometri_right_ear_1000_ab"),
                            rs.getString("audiometri_right_ear_1500_ab"),
                            rs.getString("audiometri_right_ear_2000_ab"), rs.getString("audiometri_right_ear_3000_ab"),
                            rs.getString("audiometri_right_ear_4000_ab"),
                            rs.getString("audiometri_right_ear_5000_ab"), rs.getString("audiometri_right_ear_6000_ab"),
                            rs.getString("audiometri_right_ear_500_ac"),
                            rs.getString("audiometri_right_ear_1000_ac"), rs.getString("audiometri_right_ear_1500_ac"),
                            rs.getString("audiometri_right_ear_2000_ac"),
                            rs.getString("audiometri_right_ear_3000_ac"), rs.getString("audiometri_right_ear_4000_ac"),
                            rs.getString("audiometri_right_ear_5000_ac"),
                            rs.getString("audiometri_right_ear_6000_ac"), rs.getString("eye_unaided_distant_r"),
                            rs.getString("eye_unaided_distant_l"),
                            rs.getString("eye_glasses_distant_r"), rs.getString("eye_glasses_distant_l"),
                            rs.getString("eye_unaided_near_r"), rs.getString("eye_unaided_near_l"),
                            rs.getString("eye_glasses_near_r"), rs.getString("eye_glasses_near_l"),
                            rs.getString("eye_night_vision_1"), rs.getString("eye_night_vision_2"),
                            rs.getString("eye_brake_test_1"), rs.getString("eye_brake_test_2"),
                            rs.getString("eye_color_blindless"), rs.getString("visual_fields_left"),
                            rs.getString("visual_fields_right"), rs.getString("fundi"), rs.getString("imunisasi_bcg"),
                            rs.getString("imunisasi_dpt"), rs.getString("imunisasi_polio"),
                            rs.getString("imunisasi_morbili"), rs.getString("imunisasi_thyphoid"),
                            rs.getString("imunisasi_hep_a"), rs.getString("imunisasi_hep_b"),
                            rs.getString("imunisasi_tetanus"), rs.getString("imunisasi_others"),
                            rs.getString("vertebra_scoliosis"), rs.getString("vertebra_kyphosis"),
                            rs.getString("vertebra_lordosis"), rs.getString("vertebra_forward_flexion_0_80"),
                            rs.getString("vertebra_hyperextensi_0_25"),
                            rs.getString("vertebra_lateral_flexion_0_20"), rs.getString("vertebra_heel_walking"),
                            rs.getString("vertebra_toe_walking"),
                            rs.getString("vertebra_squats_x3"), rs.getString("exam_ent_comments"),
                            rs.getString("exam_cardio_vascular_system_comments"),
                            rs.getString("exam_respiratory_system_comments"), rs.getString("exam_abdomen_comments"),
                            rs.getString("exam_genito_urinary_system_comments"),
                            rs.getString("exam_central_peripheral_nervous_system_comments"),
                            rs.getString("exam_skin_comments"), rs.getString("exam_lymph_nodes_comments"),
                            rs.getString("exam_dental_comments"), rs.getString("exam_dental_muskulo"),
                            rs.getString("conclusion_requires_spectacles"),
                            rs.getString("conclusion_colour_blindness"), rs.getString("conclusion_respiratory_problem"),
                            rs.getString("conclusion_impaired_hearing"),
                            rs.getString("conclusion_vertigo"), rs.getString("blood_group"),
                            rs.getString("medically_fit"), rs.getString("fit_with_restrictions"),
                            rs.getString("specify"), rs.getString("unfit_comment_1"), rs.getString("trombosit"),
                            rs.getString("rhesuss"), rs.getString("triglyceride"),
                            rs.getString("hdl_cholesterol"), rs.getString("ldl_cholesterol"), rs.getString("uric_acid"),
                            rs.getString("urine_colour"), rs.getString("urine_turbidity"),
                            rs.getString("urine_chemical_reaction"), rs.getString("urine_ketones"),
                            rs.getString("urine_glucose"), rs.getString("urine_nitrites"),
                            rs.getString("urine_wbc"), rs.getString("urine_rbc"), rs.getString("urine_bacteria"),
                            rs.getString("urine_crystal"), rs.getString("urine_epithel"),
                            rs.getString("hbsag"), rs.getString("anti_hbs"), rs.getString("cea"), rs.getString("afp"),
                            rs.getString("drug_amphetamine"),
                            rs.getString("drug_methamphetamine"), rs.getString("drug_morphine"),
                            rs.getString("drug_benzodiazepine"), rs.getString("drug_cocain"),
                            rs.getString("drug_marijuana"), rs.getString("nm_poli")
                    });
                }
            } catch (Exception e) {
                System.out.println("Notif : " + e);
            } finally {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi : " + e);
        }
        LCount.setText("" + tabMode.getRowCount());
    }

    public void emptTeks() {
        TglAsuhan.setDate(new Date());
        setText(Year, getTahunAsuhan());
        kosongkanText(kesimpulan, Dass21, PhyExam, catatan, ConcRadiologi, ConcEcg, ConcSpirometry, ConcAudiometry,
                Doe, Yoe, JobTitle, Activities, Hobby, OtherJob, FamilyHistoryFather, FamilyHistoryMother,
                FamilyHistorySiblings,
                FamilyHistoryOther, CigarettesPerday, AlcoholGrWeek, PrescribedMedication, PrescribedMedication2,
                AnyAllergies, TD, Nadi, RR, TB, BB,
                IMT, KlasifikasiIMT1, visual_fields_left, visual_fields_right, PemeriksaanLaboratorium, RongsenThorax,
                ecg_abnormal, type_of_hearing, eye_unaided_distant_l, eye_unaided_distant_r, spirometri_vc_1,
                spirometri_vc_2, spirometri_vc_3,
                spirometri_vc_4, spirometri_fvc_1, hb, wbc, esr, bl_group, gamaa_gt, sgot, sgpt, urea, creatinin,
                glucose, random_glucose, total_cholestrol, protein, blood, bilirubin,
                malaria, tpha, mantoux_test, leukosit, lab_others, ova, culture, cysta, parasites1, pnemunosicosis,
                pnemunosicosis2, ILO_clasification, ILO_clasification2, oth_abnormal, tb1, tb2, page3_comment,
                spirometri_fvc_2, spirometri_fvc_3, spirometri_fvc_4, spirometri_fev_1_3, spirometri_fev_1_2,
                spirometri_fev_1_4, spirometri_fev_1_1, spirometri_fev_1_fvc_2, spirometri_fev_1_fvc_4,
                spirometri_fev_1_fvc_3, spirometri_fev_1_fvc_1, audiometri_left_ear_1000,
                audiometri_left_ear_2000, audiometri_left_ear_1500, audiometri_left_ear_500, audiometri_left_ear_3000,
                audiometri_left_ear_4000, audiometri_left_ear_6000,
                audiometri_left_ear_5000, audiometri_left_ear_1001, audiometri_left_ear_2001, audiometri_left_ear_1501,
                audiometri_left_ear_501, audiometri_left_ear_3001,
                audiometri_left_ear_4001, audiometri_left_ear_6001, audiometri_left_ear_5001, audiometri_left_ear_1002,
                audiometri_left_ear_2002, audiometri_left_ear_1502,
                audiometri_left_ear_502, audiometri_left_ear_3002, audiometri_left_ear_4002, audiometri_left_ear_6002,
                audiometri_left_ear_5002, audiometri_left_ear_1003,
                audiometri_left_ear_2003, audiometri_left_ear_1503, audiometri_left_ear_503, audiometri_left_ear_3003,
                audiometri_left_ear_4003, audiometri_left_ear_6003, audiometri_left_ear_5003,
                exam_ent_comments, exam_cardio_vascular_system_comments, exam_respiratory_system_comments,
                exam_abdomen_comments, exam_genito_urinary_system_comments,
                exam_central_peripheral_nervous_system_comments, exam_skin_comments,
                exam_lymph_nodes_comments, exam_dental_comments, exam_dental_muskulo, specify, saran, surname,
                KdPetugas, NmPetugas,
                trombosit, rhesuss, triglyceride, hdl_cholesterol, ldl_cholesterol, uric_acid,
                urine_colour, urine_turbidity, urine_chemical_reaction, urine_ketones, urine_glucose,
                urine_nitrites, urine_wbc, urine_rbc, urine_bacteria, urine_crystal, urine_epithel,
                hbsag, anti_hbs, cea, afp, drug_amphetamine, drug_methamphetamine,
                drug_morphine, drug_benzodiazepine, drug_cocain, drug_marijuana, dass1, dass2, dass3,
                eye_glasses_distant_l, eye_glasses_distant_r,
                eye_unaided_near_l, eye_unaided_near_r, eye_glasses_near_l, eye_glasses_near_r,
                eye_night_vision_1, eye_night_vision_2, eye_unaided_near_r1, eye_brake_test_2);
        resetCombo(McuGroup, PosisiKerja, cbConcEcg, eye_color_blindless, fundi,
                imunisasi_bcg, imunisasi_dpt, imunisasi_polio, imunisasi_hep_b, imunisasi_morbili, imunisasi_tetanus,
                imunisasi_thyphoid, imunisasi_others,
                imunisasi_hep_a, vertebra_scoliosis, vertebra_lordosis, vertebra_hyperextensi_0_25,
                vertebra_forward_flexion_0_80, vertebra_heel_walking, vertebra_lateral_flexion_0_20, vertebra_squats_x3,
                vertebra_toe_walking, vertebra_kyphosis, conclusion_requires_spectacles, conclusion_colour_blindness,
                conclusion_respiratory_problem,
                conclusion_impaired_hearing, conclusion_vertigo, blood_group, fit, fit_with_restrictions);
        sinkronConcEcg();
        resetCek(departemen, supervisor, manager,
                JobInvolvesDrivingOrOperatingMobileEquipment, JobInvolvesWorkingAtHeights,
                JobInvolvesClericalOfficeBasedOrAdministrative,
                JobInvolvesRequiresColourVision, JobInvolvesPotentialDustExposure,
                JobInvolvesCateringStaffIncludingFoodHandlers,
                JobInvolvesExposingToOtherPotentialDangerous, RWP1, RWP2, RWP3, RWP4, RWP5, RWP6, RWP7, RWP8, RWP9,
                RWP10, RWP11, RWP12, RWP13,
                RWP14, RWP15, RWP16, RWP17, RWP18, RWP19, RWP20, RWP21, RWP22, RWP23, RWP24, RWP25, RWP26, RWP27, RWP28,
                RWP29, RWP30,
                audiometri_tinitus_always, audiometri_tinitus_never, audiometri_tinitus_previously,
                audiometri_tinitus_rarely, audiometri_tinitus_often, audiometri_ear_protection_worn_always,
                audiometri_ear_protection_worn_never, audiometri_ear_protection_worn_previously,
                audiometri_ear_protection_worn_rarely, audiometri_ear_protection_worn_often);
        isiPetugasLogin();
        TabRawat.setSelectedIndex(0);
    }

    private void getData() {
        int baris = tbObat.getSelectedRow();
        if (baris != -1) {
            isiIdentitasKunjungan(baris);
            isiDataPengkajian(baris);
        }
    }

    private void isiIdentitasKunjungan(int baris) {
        if (baris < 0) {
            return;
        }
        setText(TNoRw, getTabelValue("no_rawat", baris));
        setText(Year, getTabelValue("year", baris));
        if (Year.getText().trim().equals("")) {
            setText(Year, getTahunDariTanggal(getTabelValue("tanggal", baris)));
        }
        setText(TNoRM, getTabelValue("no_rkm_medis", baris));
        setText(TPasien, getTabelValue("nama_pasien", baris));
        setText(TmpLahir, getTabelValue("tmp_lahir", baris));
        Valid.SetTgl(TglLahir, getTabelValue("tgl_lahir", baris));
        setCombo(Jk, tampilJenisKelamin(getTabelValue("jk", baris)));
        setText(NoTlp, getTabelValue("no_tlp", baris));
        setText(SukuBangsa, getTabelValue("suku_bangsa", baris));
        setCombo(SttsNikah, getTabelValue("stts_nikah", baris));
        setText(Perusahaan, getTabelValue("perusahaan_pasien", baris));
        setText(NIP, getTabelValue("nip", baris));
        setText(KdDokter1, getTabelValue("kd_dokter", baris));
        setText(NmDokter, getTabelValue("nm_dokter", baris));
    }

    /**
     * Mengisi seluruh data inputan hasil pengkajian MCU (di luar identitas kunjungan/pasien
     * pada {@link #isiIdentitasKunjungan(int)}) dari baris tabel sumber. Dipakai baik untuk
     * menampilkan data existing (getData) maupun untuk fitur copy pengkajian ke no.rawat baru,
     * sehingga daftar kolom cukup dirawat di satu tempat.
     */
    private void isiDataPengkajian(int baris) {
        if (baris < 0) {
            return;
        }
        if (tbObat.getSelectedRow() != baris) {
            tbObat.setRowSelectionInterval(baris, baris);
        }
        {
            setText(surname, getTabelValue("surname"));
            setText(KdPetugas, getTabelValue("kd_petugas"));
            setText(NmPetugas, getTabelValue("nm_petugas"));
            setCombo(McuGroup, getTabelValue("mcu_group"));
            setText(saran, getTabelValue("unfit_comment_1"));
            String dass21Val = getTabelValue("dass_21");
            setText(Dass21, dass21Val);
            try {
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                        "Score Depression\\s+(\\d+).*Score Anxiety\\s+(\\d+).*Score Stress\\s+(\\d+)",
                        java.util.regex.Pattern.DOTALL);
                java.util.regex.Matcher matcher = pattern.matcher(dass21Val);
                if (matcher.find()) {
                    dass1.setText(String.valueOf(Integer.parseInt(matcher.group(1)) / 2));
                    dass2.setText(String.valueOf(Integer.parseInt(matcher.group(2)) / 2));
                    dass3.setText(String.valueOf(Integer.parseInt(matcher.group(3)) / 2));
                } else {
                    dass1.setText("");
                    dass2.setText("");
                    dass3.setText("");
                }
            } catch (Exception e) {
                dass1.setText("");
                dass2.setText("");
                dass3.setText("");
            }
            setText(PhyExam, getTabelValue("phy_exam"));
            setText(catatan, Sequel.cariIsi("select ifnull(catatan,'') from penilaian_mcu where no_rawat=?",
                    getTabelValue("no_rawat")));
            setText(ConcRadiologi, getTabelValue("conc_radiologi"));
            setText(ConcEcg, nilaiConcEcgTabel());
            setText(ConcSpirometry, getTabelValue("conc_spirometry"));
            setText(ConcAudiometry, getTabelValue("conc_audiometry"));
            setText(kesimpulan, getTabelValue("kesimpulan1"));
            setText(Doe, getTabelValue("doe"));
            setText(Yoe, getTabelValue("yoe"));
            setText(JobTitle, getTabelValue("job_title"));
            setText(Activities, getTabelValue("activities"));
            setText(Hobby, getTabelValue("hobby"));
            setText(OtherJob, getTabelValue("other_job"));
            setCombo(PosisiKerja, getTabelValue("posisi_kerja"));
            setCek(departemen, getTabelValue("departemen"));
            setCek(supervisor, getTabelValue("supervisor"));
            setCek(manager, getTabelValue("manager"));
            setCek(JobInvolvesDrivingOrOperatingMobileEquipment,
                    getTabelValue("job_involves_driving_or_operating_mobile_equipment"));
            setCek(JobInvolvesWorkingAtHeights, getTabelValue("job_involves_working_at_heights"));
            setCek(JobInvolvesClericalOfficeBasedOrAdministrative,
                    getTabelValue("job_involves_clerical_office_based_or_administrative"));
            setCek(JobInvolvesRequiresColourVision, getTabelValue("job_involves_requires_colour_vision"));
            setCek(JobInvolvesPotentialDustExposure, getTabelValue("job_involves_potential_dust_exposure"));
            setCek(JobInvolvesCateringStaffIncludingFoodHandlers,
                    getTabelValue("job_involves_catering_staff_including_food_handlers"));
            setCek(JobInvolvesExposingToOtherPotentialDangerous,
                    getTabelValue("job_involves_exposing_to_other_potential_dangerous"));
            setCek(RWP1, getTabelValue("med_hist_head_injury_or_contussion"));
            setCek(RWP2, getTabelValue("med_hist_fainting_blackouts_epilepsy"));
            setCek(RWP3, getTabelValue("med_hist_visual_changes"));
            setCek(RWP4, getTabelValue("med_hist_hearing_loss"));
            setCek(RWP5, getTabelValue("med_hist_nose_sinus_throat_trouble_more_4_weeks"));
            setCek(RWP6, getTabelValue("med_hist_gynaecological_problems"));
            setCek(RWP7, getTabelValue("med_hist_chronic_skin_problem"));
            setCek(RWP8, getTabelValue("med_hist_chronic_diarrhea"));
            setCek(RWP9, getTabelValue("med_hist_anorexia_more_4_weeks"));
            setCek(RWP10, getTabelValue("med_hist_gastritis"));
            setCek(RWP11, getTabelValue("med_hist_jaundice_hepatitis"));
            setCek(RWP12, getTabelValue("med_hist_chronic_cough_more_4_weeks"));
            setCek(RWP13, getTabelValue("med_hist_haemorhoid"));
            setCek(RWP14, getTabelValue("med_hist_chronic_abdominal_pain"));
            setCek(RWP15, getTabelValue("med_hist_diabetes"));
            setCek(RWP16, getTabelValue("med_hist_asthma"));
            setCek(RWP17, getTabelValue("med_hist_allergies"));
            setCek(RWP18, getTabelValue("med_hist_tuberculosis_bronchitis"));
            setCek(RWP19, getTabelValue("med_hist_psychiatric_disorder"));
            setCek(RWP20, getTabelValue("med_hist_sexual_transmitted_diseases"));
            setCek(RWP21, getTabelValue("med_hist_unusual_change_of_weight_more_5kg_per_month"));
            setCek(RWP22, getTabelValue("med_hist_hypertension"));
            setCek(RWP23, getTabelValue("med_hist_chest_pain_heart_disease"));
            setCek(RWP24, getTabelValue("med_hist_malaria_tropical_disease"));
            setCek(RWP25, getTabelValue("med_hist_surgery_operation"));
            setCek(RWP26, getTabelValue("med_hist_back_pain_more_4_weeks"));
            setCek(RWP27, getTabelValue("med_hist_thypoid_fever"));
            setCek(RWP28, getTabelValue("med_hist_swollen_or_painful_joints"));
            setCek(RWP29, getTabelValue("med_hist_kidney_problem_urinary_stones"));
            setCek(RWP30, getTabelValue("med_hist_other_chronical_diseases"));
            setText(FamilyHistoryFather, getTabelValue("family_history_father"));
            setText(FamilyHistoryMother, getTabelValue("family_history_mother"));
            setText(FamilyHistorySiblings, getTabelValue("family_history_siblings"));
            setText(FamilyHistoryOther, getTabelValue("family_history_other"));
            setText(CigarettesPerday, getTabelValue("cigarettes_perday"));
            setText(AlcoholGrWeek, getTabelValue("alcohol_gr_week"));
            setText(PrescribedMedication, getTabelValue("prescribed_medication"));
            setText(PrescribedMedication2, getTabelValue("prescribed_medication_2"));
            setText(AnyAllergies, getTabelValue("any_allergies"));
            setText(hb, getTabelValue("hb"));
            setText(wbc, getTabelValue("wbc"));
            setText(esr, getTabelValue("esr"));
            setText(bl_group, getTabelValue("bl_group"));
            setText(gamaa_gt, getTabelValue("gamaa_gt"));
            setText(sgot, getTabelValue("sgot"));
            setText(sgpt, getTabelValue("sgpt"));
            setText(urea, getTabelValue("urea"));
            setText(creatinin, getTabelValue("creatinin"));
            setText(glucose, getTabelValue("glucose"));
            setText(random_glucose, getTabelValue("random_glucose"));
            setText(total_cholestrol, getTabelValue("total_cholestrol"));
            setText(protein, getTabelValue("protein"));
            setText(blood, getTabelValue("blood"));
            setText(bilirubin, getTabelValue("bilirubin"));
            setText(malaria, getTabelValue("malaria"));
            setText(tpha, getTabelValue("tpha"));
            setText(mantoux_test, getTabelValue("mantoux_test"));
            setText(leukosit, getTabelValue("leukosit"));
            setText(lab_others, getTabelValue("lab_others"));
            setText(ova, getTabelValue("ova"));
            setText(culture, getTabelValue("culture"));
            setText(cysta, getTabelValue("cysta"));
            setText(parasites1, getTabelValue("parasites1"));
            setText(pnemunosicosis, getTabelValue("pnemunosicosis"));
            setText(pnemunosicosis2, getTabelValue("pnemunosicosis2"));
            setText(ILO_clasification, getTabelValue("ILO_clasification"));
            setText(ILO_clasification2, getTabelValue("ILO_clasification2"));
            setText(oth_abnormal, getTabelValue("oth_abnormal"));
            setText(tb1, getTabelValue("tb1"));
            setText(tb2, getTabelValue("tb2"));
            setText(page3_comment, getTabelValue("page3_comment"));
            setText(TD, getTabelValue("td"));
            setText(Nadi, getTabelValue("nadi"));
            setText(RR, getTabelValue("rr"));
            setText(TB, getTabelValue("tb"));
            setText(BB, getTabelValue("bb"));
            setText(IMT, getTabelValue("bmi"));
            setText(KlasifikasiIMT1, getTabelValue("kasifikasi_bmi"));
            setText(PemeriksaanLaboratorium,
                    nilaiPertamaTerisiValue(getTabelValue("laborat"), getTabelValue("conc_lab")));
            setText(RongsenThorax, getTabelValue("radiologi"));
            setConcEcgDariTabel();
            setText(ecg_abnormal, comboTerpilih(cbConcEcg, "Abnormal Condition") ? getTabelValue("ekg") : "");
            setText(spirometri_vc_1, getTabelValue("spirometri_vc_1"));
            setText(spirometri_vc_2, getTabelValue("spirometri_vc_2"));
            setText(spirometri_vc_3, getTabelValue("spirometri_vc_3"));
            setText(spirometri_vc_4, getTabelValue("spirometri_vc_4"));
            setText(spirometri_fvc_1, getTabelValue("spirometri_fvc_1"));
            setText(spirometri_fvc_2, getTabelValue("spirometri_fvc_2"));
            setText(spirometri_fvc_3, getTabelValue("spirometri_fvc_3"));
            setText(spirometri_fvc_4, getTabelValue("spirometri_fvc_4"));
            setText(spirometri_fev_1_1, getTabelValue("spirometri_fev_1_1"));
            setText(spirometri_fev_1_2, getTabelValue("spirometri_fev_1_2"));
            setText(spirometri_fev_1_3, getTabelValue("spirometri_fev_1_3"));
            setText(spirometri_fev_1_4, getTabelValue("spirometri_fev_1_4"));
            setText(spirometri_fev_1_fvc_1, getTabelValue("spirometri_fev_1_fvc_1"));
            setText(spirometri_fev_1_fvc_2, getTabelValue("spirometri_fev_1_fvc_2"));
            setText(spirometri_fev_1_fvc_3, getTabelValue("spirometri_fev_1_fvc_3"));
            setText(spirometri_fev_1_fvc_4, getTabelValue("spirometri_fev_1_fvc_4"));
            setCek(audiometri_tinitus_never, getTabelValue("audiometri_tinitus_never"));
            setCek(audiometri_tinitus_previously, getTabelValue("audiometri_tinitus_previously"));
            setCek(audiometri_tinitus_rarely, getTabelValue("audiometri_tinitus_rarely"));
            setCek(audiometri_tinitus_often, getTabelValue("audiometri_tinitus_often"));
            setCek(audiometri_tinitus_always, getTabelValue("audiometri_tinitus_always"));
            setCek(audiometri_ear_protection_worn_never, getTabelValue("audiometri_ear_protection_worn_never"));
            setCek(audiometri_ear_protection_worn_previously,
                    getTabelValue("audiometri_ear_protection_worn_previously"));
            setCek(audiometri_ear_protection_worn_rarely, getTabelValue("audiometri_ear_protection_worn_rarely"));
            setCek(audiometri_ear_protection_worn_often, getTabelValue("audiometri_ear_protection_worn_often"));
            setCek(audiometri_ear_protection_worn_always, getTabelValue("audiometri_ear_protection_worn_always"));
            setText(type_of_hearing, getTabelValue("type_of_hearing"));
            setText(audiometri_left_ear_500, getTabelValue("audiometri_left_ear_500_AB"));
            setText(audiometri_left_ear_1000, getTabelValue("audiometri_left_ear_1000_AB"));
            setText(audiometri_left_ear_1500, getTabelValue("audiometri_left_ear_1500_AB"));
            setText(audiometri_left_ear_2000, getTabelValue("audiometri_left_ear_2000_AB"));
            setText(audiometri_left_ear_3000, getTabelValue("audiometri_left_ear_3000_AB"));
            setText(audiometri_left_ear_4000, getTabelValue("audiometri_left_ear_4000_AB"));
            setText(audiometri_left_ear_5000, getTabelValue("audiometri_left_ear_5000_AB"));
            setText(audiometri_left_ear_6000, getTabelValue("audiometri_left_ear_6000_AB"));
            setText(audiometri_left_ear_501, getTabelValue("audiometri_left_ear_500_AC"));
            setText(audiometri_left_ear_1001, getTabelValue("audiometri_left_ear_1000_AC"));
            setText(audiometri_left_ear_1501, getTabelValue("audiometri_left_ear_1500_AC"));
            setText(audiometri_left_ear_2001, getTabelValue("audiometri_left_ear_2000_AC"));
            setText(audiometri_left_ear_3001, getTabelValue("audiometri_left_ear_3000_AC"));
            setText(audiometri_left_ear_4001, getTabelValue("audiometri_left_ear_4000_AC"));
            setText(audiometri_left_ear_5001, getTabelValue("audiometri_left_ear_5000_AC"));
            setText(audiometri_left_ear_6001, getTabelValue("audiometri_left_ear_6000_AC"));
            setText(audiometri_left_ear_502, getTabelValue("audiometri_right_ear_500_ab"));
            setText(audiometri_left_ear_1002, getTabelValue("audiometri_right_ear_1000_ab"));
            setText(audiometri_left_ear_1502, getTabelValue("audiometri_right_ear_1500_ab"));
            setText(audiometri_left_ear_2002, getTabelValue("audiometri_right_ear_2000_ab"));
            setText(audiometri_left_ear_3002, getTabelValue("audiometri_right_ear_3000_ab"));
            setText(audiometri_left_ear_4002, getTabelValue("audiometri_right_ear_4000_ab"));
            setText(audiometri_left_ear_5002, getTabelValue("audiometri_right_ear_5000_ab"));
            setText(audiometri_left_ear_6002, getTabelValue("audiometri_right_ear_6000_ab"));
            setText(audiometri_left_ear_503, getTabelValue("audiometri_right_ear_500_ac"));
            setText(audiometri_left_ear_1003, getTabelValue("audiometri_right_ear_1000_ac"));
            setText(audiometri_left_ear_1503, getTabelValue("audiometri_right_ear_1500_ac"));
            setText(audiometri_left_ear_2003, getTabelValue("audiometri_right_ear_2000_ac"));
            setText(audiometri_left_ear_3003, getTabelValue("audiometri_right_ear_3000_ac"));
            setText(audiometri_left_ear_4003, getTabelValue("audiometri_right_ear_4000_ac"));
            setText(audiometri_left_ear_5003, getTabelValue("audiometri_right_ear_5000_ac"));
            setText(audiometri_left_ear_6003, getTabelValue("audiometri_right_ear_6000_ac"));
            setText(eye_unaided_distant_r, getTabelValue("eye_unaided_distant_r"));
            setText(eye_unaided_distant_l, getTabelValue("eye_unaided_distant_l"));
            setText(eye_glasses_distant_r, getTabelValue("eye_glasses_distant_r"));
            setText(eye_glasses_distant_l, getTabelValue("eye_glasses_distant_l"));
            setText(eye_unaided_near_r, getTabelValue("eye_unaided_near_r"));
            setText(eye_unaided_near_l, getTabelValue("eye_unaided_near_l"));
            setText(eye_glasses_near_r, getTabelValue("eye_glasses_near_r"));
            setText(eye_glasses_near_l, getTabelValue("eye_glasses_near_l"));
            setText(eye_night_vision_1, getTabelValue("eye_night_vision_1"));
            setText(eye_night_vision_2, getTabelValue("eye_night_vision_2"));
            setText(eye_unaided_near_r1, getTabelValue("eye_brake_test_1"));
            setText(eye_brake_test_2, getTabelValue("eye_brake_test_2"));
            setCombo(eye_color_blindless, nilaiEyeColorBlindless(getTabelValue("eye_color_blindless")));
            setText(visual_fields_left, getTabelValue("visual_fields_left"));
            setText(visual_fields_right, getTabelValue("visual_fields_right"));
            setCombo(fundi, getTabelValue("fundi"));
            setCombo(imunisasi_bcg, getTabelValue("imunisasi_bcg"));
            setCombo(imunisasi_dpt, getTabelValue("imunisasi_dpt"));
            setCombo(imunisasi_polio, getTabelValue("imunisasi_polio"));
            setCombo(imunisasi_morbili, getTabelValue("imunisasi_morbili"));
            setCombo(imunisasi_thyphoid, getTabelValue("imunisasi_thyphoid"));
            setCombo(imunisasi_hep_a, getTabelValue("imunisasi_hep_a"));
            setCombo(imunisasi_hep_b, getTabelValue("imunisasi_hep_b"));
            setCombo(imunisasi_tetanus, getTabelValue("imunisasi_tetanus"));
            setCombo(imunisasi_others, getTabelValue("imunisasi_others"));
            setCombo(vertebra_scoliosis, getTabelValue("vertebra_scoliosis"));
            setCombo(vertebra_kyphosis, getTabelValue("vertebra_kyphosis"));
            setCombo(vertebra_lordosis, getTabelValue("vertebra_lordosis"));
            setCombo(vertebra_forward_flexion_0_80, getTabelValue("vertebra_forward_flexion_0_80"));
            setCombo(vertebra_hyperextensi_0_25, getTabelValue("vertebra_hyperextensi_0_25"));
            setCombo(vertebra_lateral_flexion_0_20, getTabelValue("vertebra_lateral_flexion_0_20"));
            setCombo(vertebra_heel_walking, getTabelValue("vertebra_heel_walking"));
            setCombo(vertebra_toe_walking, getTabelValue("vertebra_toe_walking"));
            setCombo(vertebra_squats_x3, getTabelValue("vertebra_squats_x3"));
            setText(exam_ent_comments, getTabelValue("exam_ent_comments"));
            setText(exam_cardio_vascular_system_comments, getTabelValue("exam_cardio_vascular_system_comments"));
            setText(exam_respiratory_system_comments, getTabelValue("exam_respiratory_system_comments"));
            setText(exam_abdomen_comments, getTabelValue("exam_abdomen_comments"));
            setText(exam_genito_urinary_system_comments, getTabelValue("exam_genito_urinary_system_comments"));
            setText(exam_central_peripheral_nervous_system_comments,
                    getTabelValue("exam_central_peripheral_nervous_system_comments"));
            setText(exam_skin_comments, getTabelValue("exam_skin_comments"));
            setText(exam_lymph_nodes_comments, getTabelValue("exam_lymph_nodes_comments"));
            setText(exam_dental_comments, getTabelValue("exam_dental_comments"));
            setText(exam_dental_muskulo, getTabelValue("exam_dental_muskulo"));
            setCombo(conclusion_requires_spectacles, getTabelValue("conclusion_requires_spectacles"));
            setCombo(conclusion_colour_blindness, getTabelValue("conclusion_colour_blindness"));
            setCombo(conclusion_respiratory_problem, getTabelValue("conclusion_respiratory_problem"));
            setCombo(conclusion_impaired_hearing, getTabelValue("conclusion_impaired_hearing"));
            setCombo(conclusion_vertigo, getTabelValue("conclusion_vertigo"));
            setCombo(blood_group, getTabelValue("blood_group"));
            setCombo(fit, getTabelValue("medically_fit"));
            setFitWithRestrictions(getTabelValue("fit_with_restrictions"), getTabelValue("specify"));
            setText(trombosit, getTabelValue("trombosit"));
            setText(rhesuss, getTabelValue("rhesuss"));
            setText(triglyceride, getTabelValue("triglyceride"));
            setText(hdl_cholesterol, getTabelValue("hdl_cholesterol"));
            setText(ldl_cholesterol, getTabelValue("ldl_cholesterol"));
            setText(uric_acid, getTabelValue("uric_acid"));
            setText(urine_colour, getTabelValue("urine_colour"));
            setText(urine_turbidity, getTabelValue("urine_turbidity"));
            setText(urine_chemical_reaction, getTabelValue("urine_chemical_reaction"));
            setText(urine_ketones, getTabelValue("urine_ketones"));
            setText(urine_glucose, getTabelValue("urine_glucose"));
            setText(urine_nitrites, getTabelValue("urine_nitrites"));
            setText(urine_wbc, getTabelValue("urine_wbc"));
            setText(urine_rbc, getTabelValue("urine_rbc"));
            setText(urine_bacteria, getTabelValue("urine_bacteria"));
            setText(urine_crystal, getTabelValue("urine_crystal"));
            setText(urine_epithel, getTabelValue("urine_epithel"));
            setText(hbsag, getTabelValue("hbsag"));
            setText(anti_hbs, getTabelValue("anti_hbs"));
            setText(cea, getTabelValue("cea"));
            setText(afp, getTabelValue("afp"));
            setText(drug_amphetamine, getTabelValue("drug_amphetamine"));
            setText(drug_methamphetamine, getTabelValue("drug_methamphetamine"));
            setText(drug_morphine, getTabelValue("drug_morphine"));
            setText(drug_benzodiazepine, getTabelValue("drug_benzodiazepine"));
            setText(drug_cocain, getTabelValue("drug_cocain"));
            setText(drug_marijuana, getTabelValue("drug_marijuana"));
            if (!getTabelValue("tanggal").equals("")) {
                Valid.SetTgl2(TglAsuhan, getTabelValue("tanggal"));
            }
        }
    }

    private void isRawat() {
        boolean identitasDitemukan = false;
        try {
            ps = koneksi.prepareStatement(
                    "select reg_periksa.no_rkm_medis,pasien.nm_pasien,pasien.jk,pasien.tmp_lahir,pasien.tgl_lahir,pasien.no_tlp,ifnull(suku_bangsa.nama_suku_bangsa,pasien.suku_bangsa) as suku_bangsa,pasien.stts_nikah,"
                            +
                            selectNamaPenanggungJawab()
                            + " as perusahaan_pasien,pasien.nip,reg_periksa.tgl_registrasi,reg_periksa.jam_reg,reg_periksa.kd_dokter,dokter.nm_dokter "
                            +
                            "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis " +
                            "left join suku_bangsa on suku_bangsa.id=pasien.suku_bangsa " +
                            "left join perusahaan_pasien on perusahaan_pasien.kode_perusahaan=pasien.perusahaan_pasien "
                            +
                            "left join dokter on dokter.kd_dokter=reg_periksa.kd_dokter where reg_periksa.no_rawat=?");
            try {
                ps.setString(1, TNoRw.getText());
                rs = ps.executeQuery();
                if (rs.next()) {
                    identitasDitemukan = true;
                    setText(TNoRM, rs.getString("no_rkm_medis"));
                    setText(TPasien, rs.getString("nm_pasien"));
                    setText(surname, "");
                    setText(TmpLahir, rs.getString("tmp_lahir"));
                    setCombo(Jk, tampilJenisKelamin(rs.getString("jk")));
                    Valid.SetTgl(TglLahir, rs.getString("tgl_lahir"));
                    setText(NoTlp, rs.getString("no_tlp"));
                    setText(SukuBangsa, rs.getString("suku_bangsa"));
                    setCombo(SttsNikah, rs.getString("stts_nikah"));
                    setText(Perusahaan, rs.getString("perusahaan_pasien"));
                    setText(NIP, rs.getString("nip"));
                    setText(KdDokter1, rs.getString("kd_dokter"));
                    setText(NmDokter, rs.getString("nm_dokter"));
                    DTPCari1.setDate(rs.getDate("tgl_registrasi"));
                    setText(Year, getTahunDariTanggal(rs.getString("tgl_registrasi")));
                    TanggalRegistrasi.setText(rs.getString("tgl_registrasi") + " " + rs.getString("jam_reg"));
                    setTanggalAsuhanMinimalRegistrasi();
                }
            } catch (Exception e) {
                System.out.println("Notif : " + e);
            } finally {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            }
        } catch (Exception e) {
            System.out.println("Notif : " + e);
        }
        if (identitasDitemukan && modeCopyPengkajian) {
            salinDataPengkajianTujuanBaru();
        }
    }

    /**
     * Memastikan tab "Data Pengkajian" langsung menampilkan data No. Rawat yang baru dibuka
     * lewat {@link #setNoRm}. Rentang tanggal pencarian (DTPCari1/DTPCari2) yang dibawa
     * pemanggil bisa jadi tidak mencakup tanggal registrasi No. Rawat ini (mis. masih memakai
     * tanggal kunjungan sumber saat mencari data untuk di-copy), sehingga tanpa penyesuaian ini
     * data yang baru saja disalin/disimpan tidak akan pernah muncul di daftar.
     */
    private void tampilkanDataPengkajianTujuan() {
        if (TanggalRegistrasi.getText().trim().equals("")) {
            return;
        }
        Date registrasi = DTPCari1.getDate();
        Date akhir = DTPCari2.getDate();
        if (registrasi != null && (akhir == null || !akhir.after(registrasi))) {
            java.util.Calendar kalender = java.util.Calendar.getInstance();
            kalender.setTime(registrasi);
            kalender.add(java.util.Calendar.DATE, 1);
            DTPCari2.setDate(kalender.getTime());
        }
        tampil();
    }

    /**
     * Menyalin seluruh data inputan pengkajian MCU dari baris sumber (dipilih lewat menu
     * "Copy Pengkajian") ke No. Rawat tujuan yang baru saja dimuat oleh {@link #isRawat()}.
     * Identitas kunjungan/pasien tujuan (No.Rawat, No.RM, nama, tanggal lahir, dokter PJ, dll)
     * tetap memakai data registrasi tujuan yang sudah dimuat, tidak ikut ditimpa.
     */
    private void salinDataPengkajianTujuanBaru() {
        int baris = barisSumberCopyPengkajian;
        modeCopyPengkajian = false;
        barisSumberCopyPengkajian = -1;
        if (baris < 0 || baris >= tbObat.getRowCount()) {
            JOptionPane.showMessageDialog(null,
                    "Maaf, data sumber copy pengkajian sudah tidak tersedia (daftar berubah)..!!");
            return;
        }
        String noRawatSumber = getTabelValue("no_rawat", baris);
        if (noRawatSumber.equals("")) {
            return;
        }
        if (noRawatSumber.equals(TNoRw.getText())) {
            JOptionPane.showMessageDialog(null,
                    "Copy pengkajian dibatalkan karena No. Rawat tujuan sama dengan No. Rawat sumber..!!");
            return;
        }
        int reply = JOptionPane.showConfirmDialog(rootPane,
                "Salin semua data pengkajian dari No. Rawat " + noRawatSumber + " ke No. Rawat "
                        + TNoRw.getText() + " ini..??",
                "Konfirmasi Copy Pengkajian", JOptionPane.YES_NO_OPTION);
        if (reply != JOptionPane.YES_OPTION) {
            return;
        }
        isiDataPengkajian(baris);
        setTanggalAsuhanSamaDenganRegistrasi();
        // isiDataPengkajian ikut menyalin KdPetugas/NmPetugas dari data sumber. Petugas
        // penginput data hasil copy ini harus tetap user yang sedang login (yang benar-benar
        // melakukan copy & simpan), bukan petugas dari data sumber, supaya kolom kd_petugas
        // dan log riwayat perubahan tetap mencatat siapa yang benar-benar mengisi data ini.
        isiPetugasLogin();
        tbObat.clearSelection();
        JOptionPane.showMessageDialog(null,
                "Data pengkajian berhasil disalin. Silahkan periksa kembali datanya lalu klik Simpan..!!");
    }

    public void setNoRm(String norwt, Date tgl2) {
        TNoRw.setText(norwt);
        TCari.setText(norwt);
        DTPCari2.setDate(tgl2);
        isRawat();
        tampilkanDataPengkajianTujuan();
    }

    public void isCek() {
        boolean bolehAkses = akses.getpenilaian_mcu();
        BtnSimpan.setEnabled(bolehAkses);
        BtnHapus.setEnabled(bolehAkses);
        BtnEdit.setEnabled(bolehAkses);
        isiPetugasLogin();

        if (akses.getjml2() >= 1) {
            KdDokter1.setEditable(false);
            KdDokter1.setText(akses.getkode());
            NmDokter.setText(Sequel.CariDokter(KdDokter1.getText()));
            if (NmDokter.getText().equals("")) {
                // User non-dokter yang diberi akses penilaian_mcu tetap boleh membuka form dan
                // memilih dokter penanggung jawab.
                KdDokter1.setText("");
                BtnDokter.setEnabled(bolehAkses);
            } else {
                // Jika user login adalah dokter, dokter penanggung jawab dikunci sesuai akun
                // login.
                BtnDokter.setEnabled(false);
            }
        } else {
            BtnDokter.setEnabled(bolehAkses);
        }

        if (TANGGALMUNDUR.equals("no")) {
            if (!akses.getkode().equals("Admin Utama")) {
                TglAsuhan.setEditable(false);
                TglAsuhan.setEnabled(false);
            }
        }
    }

    private void isiPetugasLogin() {
        if (akses.getkode().equals("Admin Utama")) {
            BtnPetugas.setEnabled(akses.getpenilaian_mcu());
            return;
        }

        KdPetugas.setEditable(false);
        KdPetugas.setText(akses.getkode());
        NmPetugas.setText(Sequel.CariPetugas(KdPetugas.getText()));
        if (NmPetugas.getText().equals("")) {
            // Bila akun login belum terdaftar di tabel petugas, petugas bisa dipilih manual
            // selama punya akses penilaian_mcu.
            KdPetugas.setText("");
            BtnPetugas.setEnabled(akses.getpenilaian_mcu());
        } else {
            // Petugas penginput diambil dari akun login agar data MCU tetap mencatat siapa
            // yang mengisi.
            BtnPetugas.setEnabled(false);
        }
    }

    public void setTampil() {
        TabRawat.setSelectedIndex(1);
    }

    private void hapus() {
        if (Sequel.queryu2tf("delete from penilaian_mcu where no_rawat=?", 1, new String[] {
                getTabelValue("no_rawat")
        }) == true) {
            tampil();
            emptTeks();
        } else {
            JOptionPane.showMessageDialog(null, "Gagal menghapus..!!");
        }
    }

    private void ganti() {
        sinkronFieldMcu();
        String[] nilai = getNilaiUbahPenilaianMcu();
        if (Sequel.mengedittf("penilaian_mcu", "no_rawat=?",
                "tanggal=?,`year`=?,kd_dokter=?,kd_petugas=?,catatan=?,nama_pasien=?,surname=?,mcu_group=?,dass_21=?,phy_exam=?,conc_lab=?,conc_radiologi=?,"
                        +
                        "conc_ecg=?,conc_spirometry=?,conc_audiometry=?,kesimpulan1=?,no_rkm_medis=?,tmp_lahir=?,tgl_lahir=?,jk=?,no_tlp=?,suku_bangsa=?,stts_nikah=?,"
                        +
                        "doe=?,yoe=?,job_title=?,activities=?,hobby=?,other_job=?,posisi_kerja=?,departemen=?,supervisor=?,manager=?,"
                        +
                        "job_involves_driving_or_operating_mobile_equipment=?,job_involves_working_at_heights=?," +
                        "job_involves_clerical_office_based_or_administrative=?,job_involves_requires_colour_vision=?,job_involves_potential_dust_exposure=?,"
                        +
                        "job_involves_catering_staff_including_food_handlers=?,job_involves_exposing_to_other_potential_dangerous=?,"
                        +
                        "med_hist_head_injury_or_contussion=?,med_hist_fainting_blackouts_epilepsy=?,med_hist_visual_changes=?,med_hist_hearing_loss=?,"
                        +
                        "med_hist_nose_sinus_throat_trouble_more_4_weeks=?,med_hist_gynaecological_problems=?,med_hist_chronic_skin_problem=?,"
                        +
                        "med_hist_chronic_diarrhea=?,med_hist_anorexia_more_4_weeks=?,med_hist_gastritis=?,med_hist_jaundice_hepatitis=?,"
                        +
                        "med_hist_chronic_cough_more_4_weeks=?,med_hist_haemorhoid=?,med_hist_chronic_abdominal_pain=?,med_hist_diabetes=?,med_hist_asthma=?,"
                        +
                        "med_hist_allergies=?,med_hist_tuberculosis_bronchitis=?,med_hist_psychiatric_disorder=?,med_hist_sexual_transmitted_diseases=?,"
                        +
                        "med_hist_unusual_change_of_weight_more_5kg_per_month=?,med_hist_hypertension=?,med_hist_chest_pain_heart_disease=?,"
                        +
                        "med_hist_malaria_tropical_disease=?,med_hist_surgery_operation=?,med_hist_back_pain_more_4_weeks=?,med_hist_thypoid_fever=?,"
                        +
                        "med_hist_swollen_or_painful_joints=?,med_hist_kidney_problem_urinary_stones=?,med_hist_other_chronical_diseases=?,family_history_father=?,"
                        +
                        "family_history_mother=?,family_history_siblings=?,family_history_other=?,cigarettes_perday=?,alcohol_gr_week=?,prescribed_medication=?,"
                        +
                        "prescribed_medication_2=?,any_allergies=?,hb=?,wbc=?,esr=?,bl_group=?,gamaa_gt=?,sgot=?,sgpt=?,urea=?,creatinin=?,glucose=?,random_glucose=?,"
                        +
                        "total_cholestrol=?,protein=?,blood=?,bilirubin=?,malaria=?,tpha=?,mantoux_test=?,leukosit=?,lab_others=?,ova=?,culture=?,cysta=?,"
                        +
                        "parasites1=?,pnemunosicosis=?,pnemunosicosis2=?,ILO_clasification=?,ILO_clasification2=?,oth_abnormal=?,tb1=?,tb2=?,page3_comment=?,td=?,"
                        +
                        "nadi=?,rr=?,tb=?,bb=?,bmi=?,kasifikasi_bmi=?,laborat=?,radiologi=?,ekg=?,spirometri_vc_1=?,spirometri_vc_2=?,spirometri_vc_3=?,"
                        +
                        "spirometri_vc_4=?,spirometri_fvc_1=?,spirometri_fvc_2=?,spirometri_fvc_3=?,spirometri_fvc_4=?,spirometri_fev_1_1=?,spirometri_fev_1_2=?,"
                        +
                        "spirometri_fev_1_3=?,spirometri_fev_1_4=?,spirometri_fev_1_fvc_1=?,spirometri_fev_1_fvc_2=?,spirometri_fev_1_fvc_3=?,"
                        +
                        "spirometri_fev_1_fvc_4=?,audiometri_tinitus_never=?,audiometri_tinitus_previously=?,audiometri_tinitus_rarely=?,audiometri_tinitus_often=?,"
                        +
                        "audiometri_tinitus_always=?,audiometri_ear_protection_worn_never=?,audiometri_ear_protection_worn_previously=?,"
                        +
                        "audiometri_ear_protection_worn_rarely=?,audiometri_ear_protection_worn_often=?,audiometri_ear_protection_worn_always=?,type_of_hearing=?,"
                        +
                        "audiometri_left_ear_500_AB=?,audiometri_left_ear_1000_AB=?,audiometri_left_ear_1500_AB=?,audiometri_left_ear_2000_AB=?,"
                        +
                        "audiometri_left_ear_3000_AB=?,audiometri_left_ear_4000_AB=?,audiometri_left_ear_5000_AB=?,audiometri_left_ear_6000_AB=?,"
                        +
                        "audiometri_left_ear_500_AC=?,audiometri_left_ear_1000_AC=?,audiometri_left_ear_1500_AC=?,audiometri_left_ear_2000_AC=?,"
                        +
                        "audiometri_left_ear_3000_AC=?,audiometri_left_ear_4000_AC=?,audiometri_left_ear_5000_AC=?,audiometri_left_ear_6000_AC=?,"
                        +
                        "audiometri_right_ear_500_ab=?,audiometri_right_ear_1000_ab=?,audiometri_right_ear_1500_ab=?,audiometri_right_ear_2000_ab=?,"
                        +
                        "audiometri_right_ear_3000_ab=?,audiometri_right_ear_4000_ab=?,audiometri_right_ear_5000_ab=?,audiometri_right_ear_6000_ab=?,"
                        +
                        "audiometri_right_ear_500_ac=?,audiometri_right_ear_1000_ac=?,audiometri_right_ear_1500_ac=?,audiometri_right_ear_2000_ac=?,"
                        +
                        "audiometri_right_ear_3000_ac=?,audiometri_right_ear_4000_ac=?,audiometri_right_ear_5000_ac=?,audiometri_right_ear_6000_ac=?,"
                        +
                        "eye_unaided_distant_r=?,eye_unaided_distant_l=?,eye_glasses_distant_r=?,eye_glasses_distant_l=?,eye_unaided_near_r=?,eye_unaided_near_l=?,"
                        +
                        "eye_glasses_near_r=?,eye_glasses_near_l=?,eye_night_vision_1=?,eye_night_vision_2=?,eye_brake_test_1=?,eye_brake_test_2=?,"
                        +
                        "eye_color_blindless=?,visual_fields_left=?,visual_fields_right=?,fundi=?,imunisasi_bcg=?,imunisasi_dpt=?,imunisasi_polio=?,"
                        +
                        "imunisasi_morbili=?,imunisasi_thyphoid=?,imunisasi_hep_a=?,imunisasi_hep_b=?,imunisasi_tetanus=?,imunisasi_others=?,vertebra_scoliosis=?,"
                        +
                        "vertebra_kyphosis=?,vertebra_lordosis=?,vertebra_forward_flexion_0_80=?,vertebra_hyperextensi_0_25=?,vertebra_lateral_flexion_0_20=?,"
                        +
                        "vertebra_heel_walking=?,vertebra_toe_walking=?,vertebra_squats_x3=?,exam_ent_comments=?,exam_cardio_vascular_system_comments=?,"
                        +
                        "exam_respiratory_system_comments=?,exam_abdomen_comments=?,exam_genito_urinary_system_comments=?,"
                        +
                        "exam_central_peripheral_nervous_system_comments=?,exam_skin_comments=?,exam_lymph_nodes_comments=?,exam_dental_comments=?,"
                        +
                        "exam_dental_muskulo=?,conclusion_requires_spectacles=?,conclusion_colour_blindness=?,conclusion_respiratory_problem=?,"
                        +
                        "conclusion_impaired_hearing=?,conclusion_vertigo=?,blood_group=?,medically_fit=?,fit_with_restrictions=?,specify=?,unfit_comment_1=?,"
                        +
                        "trombosit=?,rhesuss=?,triglyceride=?,hdl_cholesterol=?,ldl_cholesterol=?,uric_acid=?,urine_colour=?,urine_turbidity=?,"
                        +
                        "urine_chemical_reaction=?,urine_ketones=?,urine_glucose=?,urine_nitrites=?,urine_wbc=?,urine_rbc=?,urine_bacteria=?,urine_crystal=?,"
                        +
                        "urine_epithel=?,hbsag=?,anti_hbs=?,cea=?,afp=?,drug_amphetamine=?,drug_methamphetamine=?,drug_morphine=?,drug_benzodiazepine=?,drug_cocain=?,"
                        +
                        "drug_marijuana=?",
                nilai.length, nilai) == true) {
            updateIdentitasPasienMcu();
            tampil();
            emptTeks();
            TabRawat.setSelectedIndex(1);
        }
    }

    private void simpan() {
        sinkronFieldMcu();
        if (Sequel.menyimpantf("penilaian_mcu(" +
                "no_rawat,tanggal,`year`,kd_dokter,kd_petugas,catatan,nama_pasien,surname,mcu_group,dass_21,phy_exam,conc_lab,conc_radiologi,conc_ecg,"
                +
                "conc_spirometry,conc_audiometry,kesimpulan1,no_rkm_medis,tmp_lahir,tgl_lahir,jk,no_tlp,suku_bangsa,stts_nikah,doe,yoe,job_title,activities,"
                +
                "hobby,other_job,posisi_kerja,departemen,supervisor,manager,job_involves_driving_or_operating_mobile_equipment,"
                +
                "job_involves_working_at_heights,job_involves_clerical_office_based_or_administrative,job_involves_requires_colour_vision,"
                +
                "job_involves_potential_dust_exposure,job_involves_catering_staff_including_food_handlers,job_involves_exposing_to_other_potential_dangerous,"
                +
                "med_hist_head_injury_or_contussion,med_hist_fainting_blackouts_epilepsy,med_hist_visual_changes,med_hist_hearing_loss,"
                +
                "med_hist_nose_sinus_throat_trouble_more_4_weeks,med_hist_gynaecological_problems,med_hist_chronic_skin_problem,med_hist_chronic_diarrhea,"
                +
                "med_hist_anorexia_more_4_weeks,med_hist_gastritis,med_hist_jaundice_hepatitis,med_hist_chronic_cough_more_4_weeks,med_hist_haemorhoid,"
                +
                "med_hist_chronic_abdominal_pain,med_hist_diabetes,med_hist_asthma,med_hist_allergies,med_hist_tuberculosis_bronchitis,"
                +
                "med_hist_psychiatric_disorder,med_hist_sexual_transmitted_diseases,med_hist_unusual_change_of_weight_more_5kg_per_month,"
                +
                "med_hist_hypertension,med_hist_chest_pain_heart_disease,med_hist_malaria_tropical_disease,med_hist_surgery_operation,"
                +
                "med_hist_back_pain_more_4_weeks,med_hist_thypoid_fever,med_hist_swollen_or_painful_joints,med_hist_kidney_problem_urinary_stones,"
                +
                "med_hist_other_chronical_diseases,family_history_father,family_history_mother,family_history_siblings,family_history_other,cigarettes_perday,"
                +
                "alcohol_gr_week,prescribed_medication,prescribed_medication_2,any_allergies,hb,wbc,esr,bl_group,gamaa_gt,sgot,sgpt,urea,creatinin,glucose,"
                +
                "random_glucose,total_cholestrol,protein,blood,bilirubin,malaria,tpha,mantoux_test,leukosit,lab_others,ova,culture,cysta,parasites1,"
                +
                "pnemunosicosis,pnemunosicosis2,ILO_clasification,ILO_clasification2,oth_abnormal,tb1,tb2,page3_comment,td,nadi,rr,tb,bb,bmi,kasifikasi_bmi,"
                +
                "laborat,radiologi,ekg,spirometri_vc_1,spirometri_vc_2,spirometri_vc_3,spirometri_vc_4,spirometri_fvc_1,spirometri_fvc_2,spirometri_fvc_3,"
                +
                "spirometri_fvc_4,spirometri_fev_1_1,spirometri_fev_1_2,spirometri_fev_1_3,spirometri_fev_1_4,spirometri_fev_1_fvc_1,spirometri_fev_1_fvc_2,"
                +
                "spirometri_fev_1_fvc_3,spirometri_fev_1_fvc_4,audiometri_tinitus_never,audiometri_tinitus_previously,audiometri_tinitus_rarely,"
                +
                "audiometri_tinitus_often,audiometri_tinitus_always,audiometri_ear_protection_worn_never,audiometri_ear_protection_worn_previously,"
                +
                "audiometri_ear_protection_worn_rarely,audiometri_ear_protection_worn_often,audiometri_ear_protection_worn_always,type_of_hearing,"
                +
                "audiometri_left_ear_500_AB,audiometri_left_ear_1000_AB,audiometri_left_ear_1500_AB,audiometri_left_ear_2000_AB,audiometri_left_ear_3000_AB,"
                +
                "audiometri_left_ear_4000_AB,audiometri_left_ear_5000_AB,audiometri_left_ear_6000_AB,audiometri_left_ear_500_AC,audiometri_left_ear_1000_AC,"
                +
                "audiometri_left_ear_1500_AC,audiometri_left_ear_2000_AC,audiometri_left_ear_3000_AC,audiometri_left_ear_4000_AC,audiometri_left_ear_5000_AC,"
                +
                "audiometri_left_ear_6000_AC,audiometri_right_ear_500_ab,audiometri_right_ear_1000_ab,audiometri_right_ear_1500_ab,"
                +
                "audiometri_right_ear_2000_ab,audiometri_right_ear_3000_ab,audiometri_right_ear_4000_ab,audiometri_right_ear_5000_ab,"
                +
                "audiometri_right_ear_6000_ab,audiometri_right_ear_500_ac,audiometri_right_ear_1000_ac,audiometri_right_ear_1500_ac,"
                +
                "audiometri_right_ear_2000_ac,audiometri_right_ear_3000_ac,audiometri_right_ear_4000_ac,audiometri_right_ear_5000_ac,"
                +
                "audiometri_right_ear_6000_ac,eye_unaided_distant_r,eye_unaided_distant_l,eye_glasses_distant_r,eye_glasses_distant_l,eye_unaided_near_r,"
                +
                "eye_unaided_near_l,eye_glasses_near_r,eye_glasses_near_l,eye_night_vision_1,eye_night_vision_2,eye_brake_test_1,eye_brake_test_2,"
                +
                "eye_color_blindless,visual_fields_left,visual_fields_right,fundi,imunisasi_bcg,imunisasi_dpt,imunisasi_polio,imunisasi_morbili,"
                +
                "imunisasi_thyphoid,imunisasi_hep_a,imunisasi_hep_b,imunisasi_tetanus,imunisasi_others,vertebra_scoliosis,vertebra_kyphosis,vertebra_lordosis,"
                +
                "vertebra_forward_flexion_0_80,vertebra_hyperextensi_0_25,vertebra_lateral_flexion_0_20,vertebra_heel_walking,vertebra_toe_walking,"
                +
                "vertebra_squats_x3,exam_ent_comments,exam_cardio_vascular_system_comments,exam_respiratory_system_comments,exam_abdomen_comments,"
                +
                "exam_genito_urinary_system_comments,exam_central_peripheral_nervous_system_comments,exam_skin_comments,exam_lymph_nodes_comments,"
                +
                "exam_dental_comments,exam_dental_muskulo,conclusion_requires_spectacles,conclusion_colour_blindness,conclusion_respiratory_problem,"
                +
                "conclusion_impaired_hearing,conclusion_vertigo,blood_group,medically_fit,fit_with_restrictions,specify,unfit_comment_1,trombosit,rhesuss,"
                +
                "triglyceride,hdl_cholesterol,ldl_cholesterol,uric_acid,urine_colour,urine_turbidity,urine_chemical_reaction,urine_ketones,urine_glucose,"
                +
                "urine_nitrites,urine_wbc,urine_rbc,urine_bacteria,urine_crystal,urine_epithel,hbsag,anti_hbs,cea,afp,drug_amphetamine,drug_methamphetamine,"
                +
                "drug_morphine,drug_benzodiazepine,drug_cocain,drug_marijuana)",
                "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?",
                "No.Rawat", 262, getNilaiPenilaianMcu()) == true) {
            updateIdentitasPasienMcu();
            tampil();
            emptTeks();
        }
    }

    private void updateIdentitasPasienMcu() {
        if (TNoRM.getText().trim().equals("")) {
            return;
        }

        String kodePerusahaan = getKodePerusahaanPasienMcu();
        if (kodePerusahaan.equals("")) {
            Sequel.queryu2tf("update pasien set nip=? where no_rkm_medis=?", 2, new String[] {
                    NIP.getText().trim(), TNoRM.getText().trim()
            });
            JOptionPane.showMessageDialog(null,
                    "Perusahaan tidak ditemukan di master perusahaan. NIP pasien tetap diperbarui, tetapi perusahaan pasien belum diubah..!!");
            return;
        }

        if (Sequel.queryu2tf("update pasien set perusahaan_pasien=?,nip=? where no_rkm_medis=?", 3, new String[] {
                kodePerusahaan, NIP.getText().trim(), TNoRM.getText().trim()
        }) == false) {
            JOptionPane.showMessageDialog(null, "Data Perusahaan/NIP pasien gagal diperbarui..!!");
        }
    }

    private String getKodePerusahaanPasienMcu() {
        String perusahaan = Perusahaan.getText().trim();
        if (perusahaan.equals("")) {
            return "-";
        }

        String kodePerusahaan = Sequel.cariIsi("select kode_perusahaan from perusahaan_pasien where kode_perusahaan=?",
                perusahaan);
        if (kodePerusahaan.equals("")) {
            kodePerusahaan = Sequel.cariIsi(
                    "select kode_perusahaan from perusahaan_pasien where nama_perusahaan=? order by kode_perusahaan limit 1",
                    perusahaan);
        }
        if (kodePerusahaan.equals("")) {
            buatMasterPerusahaanPasienMcu(perusahaan);
            kodePerusahaan = Sequel.cariIsi(
                    "select kode_perusahaan from perusahaan_pasien where nama_perusahaan=? order by kode_perusahaan desc limit 1",
                    perusahaan);
        }
        return kodePerusahaan;
    }

    private void buatMasterPerusahaanPasienMcu(String namaPerusahaan) {
        Sequel.queryu2tf(
                "insert into perusahaan_pasien(kode_perusahaan,nama_perusahaan,alamat,kota,no_telp) " +
                        "select concat('I',lpad(ifnull(max(cast(substr(kode_perusahaan,2) as unsigned)),0)+1,4,'0')),?,'-','-','0' "
                        +
                        "from perusahaan_pasien where kode_perusahaan regexp '^I[0-9]+$'",
                1, new String[] { namaPerusahaan });
    }

    private void isBMI() {
        try {
            if ((!TB.getText().equals("")) && (!BB.getText().equals(""))) {
                try {
                    IMT.setText(Valid
                            .SetAngka8(Valid.SetAngka(BB.getText())
                                    / ((Valid.SetAngka(TB.getText()) / 100) * (Valid.SetAngka(TB.getText()) / 100)), 1)
                            + "");
                } catch (Exception e) {
                    IMT.setText("");
                }
                if (Valid.SetAngka(IMT.getText()) < 18.5) {
                    KlasifikasiIMT1.setText("Under Weight");
                } else if ((Valid.SetAngka(IMT.getText()) >= 18.5) && (Valid.SetAngka(IMT.getText()) <= 24.9)) {
                    KlasifikasiIMT1.setText("Normal");
                } else if ((Valid.SetAngka(IMT.getText()) >= 24.9) && (Valid.SetAngka(IMT.getText()) <= 29.9)) {
                    KlasifikasiIMT1.setText("Overweight");
                } else if (Valid.SetAngka(IMT.getText()) >= 30) {
                    KlasifikasiIMT1.setText("Obesitas II");
                } else {
                    KlasifikasiIMT1.setText("");
                }
            } else {
                IMT.setText("");
                KlasifikasiIMT1.setText("");
            }
        } catch (Exception e) {
            IMT.setText("");
            KlasifikasiIMT1.setText("");
        }
    }

    private void isLP() {
        // LP dan RisikoLP sudah tidak dipakai di form RMMCURSPK baru.
    }

    private void runBackground(Runnable task) {
        if (ceksukses)
            return;
        if (executor.isShutdown() || executor.isTerminated())
            return;
        if (!isDisplayable())
            return;

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

    private void RWP1KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,JobInvolvesRequiresColourVision,RWP2);
    }

    private void RWP2KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,RWP1,RWP3);
    }

    private void RWP3KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,RWP2,RWP4);
    }

    private void RWP4KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,RWP3,RWP5);
    }

    private void RWP5KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,RWP4,RWP6);
    }

    private void RWP6KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,RWP5,RWP7);
    }

    private void RWP7KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,RWP6,RWP8);
    }

    private void RWP8KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,RWP7,RWP9);
    }

    private void RWP9KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,RWP8,RWP10);
    }

    private void RWP10KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,RWP9,RWP11);
    }

    private void RWP11KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,RWP10,RWP12);
    }

    private void RWP12KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,RWP11,RWP13);
    }

    private void RWP13KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,RWP12,RWP14);
    }

    private void RWP14KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,RWP13,RWP20);
    }

    private void RWP15KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,RWP20,RWP16);
    }

    private void RWP16KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,RWP15,RWP17);
    }

    private void RWP17KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,RWP16,RWP18);
    }

    private void RWP18KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,RWP17,RWP19);
    }

    private void RWP19KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,RWP18,RWP22);
    }

    private void RWP20KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,RWP14,RWP15);
    }

    private void RWP21KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,RWP25,RWP26);
    }

    private void RWP22KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,RWP19,RWP23);
    }

    private void RWP23KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,RWP22,RWP24);
    }

    private void RWP24KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,RWP23,RWP25);
    }

    private void RWP25KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,RWP24,RWP21);
    }

    private void RWP26KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,RWP21,RWP27);
    }

    private void RWP27KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,RWP26,RWP28);
    }

    private void RWP28KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,RWP27,RWP29);
    }

    private void RWP29KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,RWP28,RWP30);
    }

    private void RWP30KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,RWP29,FamilyHistoryFather);
    }

    private void FamilyHistoryFatherKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,RWP30,CigarettesPerday);
    }

    private void FamilyHistoryMotherKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,PrescribedMedication,AlcoholGrWeek);
    }

    private void FamilyHistorySiblingsKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,PrescribedMedication2,AnyAllergies);
    }

    private void FamilyHistoryOtherKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,AnyAllergies,hb);
    }

    private void CigarettesPerdayKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,FamilyHistoryFather,PrescribedMedication);
    }

    private void AlcoholGrWeekKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,FamilyHistoryMother,PrescribedMedication2);
    }

    private void PrescribedMedicationKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,CigarettesPerday,FamilyHistoryMother);
    }

    private void PrescribedMedication2KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,AlcoholGrWeek,FamilyHistorySiblings);
    }

    private void AnyAllergiesKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,FamilyHistorySiblings,FamilyHistoryOther);
    }

    private void hbKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,FamilyHistoryOther,wbc);
    }

    private void wbcKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,hb,esr);
    }

    private void esrKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,wbc,bl_group);
    }

    private void bl_groupKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,esr,gamaa_gt);
    }

    private void gamaa_gtKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,bl_group,sgot);
    }

    private void sgotKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,gamaa_gt,sgpt);
    }

    private void sgptKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,sgot,urea);
    }

    private void ureaKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,sgpt,creatinin);
    }

    private void creatininKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,urea,glucose);
    }

    private void glucoseKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,creatinin,random_glucose);
    }

    private void random_glucoseKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,glucose,total_cholestrol);
    }

    private void total_cholestrolKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,random_glucose,protein);
    }

    private void proteinKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,total_cholestrol,blood);
    }

    private void bloodKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,protein,leukosit);
    }

    private void bilirubinKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,leukosit,malaria);
    }

    private void malariaKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,bilirubin,tpha);
    }

    private void tphaKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,malaria,mantoux_test);
    }

    private void mantoux_testKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,tpha,lab_others);
    }

    private void lab_othersKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,mantoux_test,ova);
    }

    private void ovaKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,lab_others,culture);
    }

    private void cultureKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,ova,cysta);
    }

    private void cystaKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,culture,parasites1);
    }

    private void parasites1KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,cysta,trombosit);
    }

    private void trombositKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,parasites1,rhesuss);
    }

    private void rhesussKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,trombosit,triglyceride);
    }

    private void triglycerideKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,rhesuss,hdl_cholesterol);
    }

    private void hdl_cholesterolKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,triglyceride,ldl_cholesterol);
    }

    private void ldl_cholesterolKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,hdl_cholesterol,uric_acid);
    }

    private void uric_acidKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,ldl_cholesterol,urine_colour);
    }

    private void urine_colourKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,uric_acid,urine_turbidity);
    }

    private void urine_turbidityKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,urine_colour,urine_chemical_reaction);
    }

    private void urine_chemical_reactionKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,urine_turbidity,urine_ketones);
    }

    private void urine_ketonesKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,urine_chemical_reaction,urine_glucose);
    }

    private void urine_glucoseKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,urine_ketones,urine_nitrites);
    }

    private void urine_nitritesKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,urine_glucose,urine_wbc);
    }

    private void urine_wbcKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,urine_nitrites,urine_rbc);
    }

    private void urine_rbcKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,urine_wbc,urine_bacteria);
    }

    private void urine_bacteriaKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,urine_rbc,urine_crystal);
    }

    private void urine_crystalKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,urine_bacteria,urine_epithel);
    }

    private void urine_epithelKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,urine_crystal,hbsag);
    }

    private void hbsagKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,urine_epithel,anti_hbs);
    }

    private void anti_hbsKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,hbsag,cea);
    }

    private void ceaKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,anti_hbs,afp);
    }

    private void afpKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,cea,drug_amphetamine);
    }

    private void drug_amphetamineKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,afp,drug_methamphetamine);
    }

    private void drug_methamphetamineKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,drug_amphetamine,drug_morphine);
    }

    private void drug_morphineKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,drug_methamphetamine,drug_benzodiazepine);
    }

    private void drug_benzodiazepineKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,drug_morphine,drug_cocain);
    }

    private void drug_cocainKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,drug_benzodiazepine,drug_marijuana);
    }

    private void drug_marijuanaKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,drug_cocain,PemeriksaanLaboratorium);
    }

    private void BtnPetugasLabKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,drug_marijuana,pnemunosicosis);
    }

    private void pnemunosicosisKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,PemeriksaanLaboratorium,pnemunosicosis2);
    }

    private void pnemunosicosis2KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,pnemunosicosis,ILO_clasification);
    }

    private void ILO_clasificationKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,pnemunosicosis2,ILO_clasification2);
    }

    private void ILO_clasification2KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,ILO_clasification,tb1);
    }

    private void tb1KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,ILO_clasification2,tb2);
    }

    private void tb2KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,tb1,oth_abnormal);
    }

    private void oth_abnormalKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,tb2,page3_comment);
    }

    private void page3_commentKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,oth_abnormal,RongsenThorax);
    }

    private void ecg_abnormalKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,cbConcEcg,spirometri_vc_1);
    }

    private void spirometri_vc_1KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,ecg_abnormal,spirometri_vc_2);
    }

    private void spirometri_vc_2KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,spirometri_vc_1,spirometri_vc_3);
    }

    private void spirometri_vc_3KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,spirometri_vc_2,spirometri_vc_4);
    }

    private void spirometri_vc_4KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,spirometri_vc_3,spirometri_fvc_1);
    }

    private void spirometri_fvc_1KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,spirometri_vc_4,spirometri_fvc_2);
    }

    private void spirometri_fvc_2KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,spirometri_fvc_1,spirometri_fvc_3);
    }

    private void spirometri_fvc_3KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,spirometri_fvc_2,spirometri_fvc_4);
    }

    private void spirometri_fvc_4KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,spirometri_fvc_3,spirometri_fev_1_1);
    }

    private void spirometri_fev_1_1KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,spirometri_fvc_4,spirometri_fev_1_2);
    }

    private void spirometri_fev_1_2KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,spirometri_fev_1_1,spirometri_fev_1_3);
    }

    private void spirometri_fev_1_3KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,spirometri_fev_1_2,spirometri_fev_1_4);
    }

    private void spirometri_fev_1_4KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,spirometri_fev_1_3,spirometri_fev_1_fvc_1);
    }

    private void spirometri_fev_1_fvc_1KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,spirometri_fev_1_4,spirometri_fev_1_fvc_2);
    }

    private void spirometri_fev_1_fvc_2KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,spirometri_fev_1_fvc_1,spirometri_fev_1_fvc_3);
    }

    private void spirometri_fev_1_fvc_3KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,spirometri_fev_1_fvc_2,spirometri_fev_1_fvc_4);
    }

    private void spirometri_fev_1_fvc_4KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,spirometri_fev_1_fvc_3,audiometri_tinitus_never);
    }

    private void type_of_hearingKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,audiometri_ear_protection_worn_always,audiometri_left_ear_500);
    }

    private void audiometri_left_ear_500KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,type_of_hearing,audiometri_left_ear_1000);
    }

    private void audiometri_left_ear_1000KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,audiometri_left_ear_500,audiometri_left_ear_1500);
    }

    private void audiometri_left_ear_1500KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,audiometri_left_ear_1000,audiometri_left_ear_2000);
    }

    private void audiometri_left_ear_2000KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,audiometri_left_ear_1500,audiometri_left_ear_3000);
    }

    private void audiometri_left_ear_3000KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,audiometri_left_ear_2000,audiometri_left_ear_4000);
    }

    private void audiometri_left_ear_4000KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,audiometri_left_ear_3000,audiometri_left_ear_5000);
    }

    private void audiometri_left_ear_5000KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,audiometri_left_ear_4000,audiometri_left_ear_6000);
    }

    private void audiometri_left_ear_6000KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,audiometri_left_ear_5000,audiometri_left_ear_501);
    }

    private void audiometri_left_ear_501KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,audiometri_left_ear_6000,audiometri_left_ear_1001);
    }

    private void audiometri_left_ear_1001KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,audiometri_left_ear_501,audiometri_left_ear_1501);
    }

    private void audiometri_left_ear_1501KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,audiometri_left_ear_1001,audiometri_left_ear_2001);
    }

    private void audiometri_left_ear_2001KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,audiometri_left_ear_1501,audiometri_left_ear_3001);
    }

    private void audiometri_left_ear_3001KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,audiometri_left_ear_2001,audiometri_left_ear_4001);
    }

    private void audiometri_left_ear_4001KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,audiometri_left_ear_3001,audiometri_left_ear_5001);
    }

    private void audiometri_left_ear_5001KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,audiometri_left_ear_4001,audiometri_left_ear_6001);
    }

    private void audiometri_left_ear_6001KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,audiometri_left_ear_5001,audiometri_left_ear_502);
    }

    private void audiometri_left_ear_502KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,audiometri_left_ear_6001,audiometri_left_ear_1002);
    }

    private void audiometri_left_ear_1002KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,audiometri_left_ear_502,audiometri_left_ear_1502);
    }

    private void audiometri_left_ear_1502KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,audiometri_left_ear_1002,audiometri_left_ear_2002);
    }

    private void audiometri_left_ear_2002KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,audiometri_left_ear_1502,audiometri_left_ear_3002);
    }

    private void audiometri_left_ear_3002KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,audiometri_left_ear_2002,audiometri_left_ear_4002);
    }

    private void audiometri_left_ear_4002KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,audiometri_left_ear_3002,audiometri_left_ear_5002);
    }

    private void audiometri_left_ear_5002KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,audiometri_left_ear_4002,audiometri_left_ear_6002);
    }

    private void audiometri_left_ear_6002KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,audiometri_left_ear_5002,audiometri_left_ear_503);
    }

    private void audiometri_left_ear_503KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,audiometri_left_ear_6002,audiometri_left_ear_1003);
    }

    private void audiometri_left_ear_1003KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,audiometri_left_ear_503,audiometri_left_ear_1503);
    }

    private void audiometri_left_ear_1503KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,audiometri_left_ear_1003,audiometri_left_ear_2003);
    }

    private void audiometri_left_ear_2003KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,audiometri_left_ear_1503,audiometri_left_ear_3003);
    }

    private void audiometri_left_ear_3003KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,audiometri_left_ear_2003,audiometri_left_ear_4003);
    }

    private void audiometri_left_ear_4003KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,audiometri_left_ear_3003,audiometri_left_ear_5003);
    }

    private void audiometri_left_ear_5003KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,audiometri_left_ear_4003,audiometri_left_ear_6003);
    }

    private void audiometri_left_ear_6003KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,audiometri_left_ear_5003,TD);
    }

    private void TDKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,audiometri_left_ear_6003,Nadi);
    }

    private void BBKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,TB,eye_unaided_distant_l);
    }

    private void IMTKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,BB,KlasifikasiIMT1);
    }

    private void KlasifikasiIMT1KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,IMT,eye_unaided_distant_r);
    }

    private void eye_color_blindlessKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,eye_brake_test_2,visual_fields_left);
    }

    private void visual_fields_leftKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,eye_color_blindless,visual_fields_right);
    }

    private void visual_fields_rightKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,visual_fields_left,fundi);
    }

    private void fundiKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,visual_fields_right,imunisasi_bcg);
    }

    private void imunisasi_bcgKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,fundi,imunisasi_dpt);
    }

    private void imunisasi_dptKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,imunisasi_bcg,imunisasi_polio);
    }

    private void imunisasi_polioKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,imunisasi_dpt,imunisasi_morbili);
    }

    private void imunisasi_morbiliKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,imunisasi_polio,imunisasi_thyphoid);
    }

    private void imunisasi_thyphoidKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,imunisasi_morbili,imunisasi_hep_a);
    }

    private void imunisasi_hep_aKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,imunisasi_thyphoid,imunisasi_hep_b);
    }

    private void imunisasi_hep_bKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,imunisasi_hep_a,imunisasi_tetanus);
    }

    private void imunisasi_tetanusKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,imunisasi_hep_b,imunisasi_others);
    }

    private void imunisasi_othersKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,imunisasi_tetanus,vertebra_scoliosis);
    }

    private void vertebra_scoliosisKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,imunisasi_others,vertebra_lateral_flexion_0_20);
    }

    private void vertebra_lordosisKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,vertebra_heel_walking,vertebra_toe_walking);
    }

    private void vertebra_hyperextensi_0_25KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,vertebra_squats_x3,exam_ent_comments);
    }

    private void vertebra_heel_walkingKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,vertebra_kyphosis,vertebra_lordosis);
    }

    private void vertebra_squats_x3KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,vertebra_forward_flexion_0_80,vertebra_hyperextensi_0_25);
    }

    private void vertebra_kyphosisKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,vertebra_lateral_flexion_0_20,vertebra_heel_walking);
    }

    private void vertebra_forward_flexion_0_80KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,vertebra_toe_walking,vertebra_squats_x3);
    }

    private void vertebra_lateral_flexion_0_20KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,vertebra_scoliosis,vertebra_kyphosis);
    }

    private void vertebra_toe_walkingKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,vertebra_lordosis,vertebra_forward_flexion_0_80);
    }

    private void exam_ent_commentsKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,vertebra_hyperextensi_0_25,exam_central_peripheral_nervous_system_comments);
    }

    private void exam_cardio_vascular_system_commentsKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,exam_central_peripheral_nervous_system_comments,exam_skin_comments);
    }

    private void exam_respiratory_system_commentsKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,exam_skin_comments,exam_lymph_nodes_comments);
    }

    private void exam_abdomen_commentsKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,exam_lymph_nodes_comments,exam_dental_comments);
    }

    private void exam_genito_urinary_system_commentsKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,exam_dental_comments,exam_dental_muskulo);
    }

    private void exam_central_peripheral_nervous_system_commentsKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,exam_ent_comments,exam_cardio_vascular_system_comments);
    }

    private void exam_skin_commentsKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,exam_cardio_vascular_system_comments,exam_respiratory_system_comments);
    }

    private void exam_lymph_nodes_commentsKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,exam_respiratory_system_comments,exam_abdomen_comments);
    }

    private void exam_dental_commentsKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,exam_abdomen_comments,exam_genito_urinary_system_comments);
    }

    private void exam_dental_muskuloKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,exam_genito_urinary_system_comments,catatan);
    }
}
