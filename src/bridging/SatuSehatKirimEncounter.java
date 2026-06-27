/*
  by Mas Elkhanza
 */

package bridging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import fungsi.WarnaTable;
import fungsi.batasInput;
import fungsi.koneksiDB;
import java.awt.Dimension;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import fungsi.sekuel;
import fungsi.validasi;
import fungsi.akses;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.event.KeyEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

/**
 *
 * @author dosen
 */
public final class SatuSehatKirimEncounter extends javax.swing.JDialog {
    private final DefaultTableModel tabMode;
    private sekuel Sequel=new sekuel();
    private validasi Valid=new validasi();
    private Connection koneksi=koneksiDB.condb();
    private PreparedStatement ps;
    private ResultSet rs;   
    private int i=0;
    private String link="",json="",iddokter="",idpasien="",idepisode="";
    private ApiSatuSehat api=new ApiSatuSehat();
    private HttpHeaders headers ;
    private HttpEntity requestEntity;
    private ObjectMapper mapper = new ObjectMapper();
    private JsonNode root;
    private JsonNode response;
    private SatuSehatCekNIK cekViaSatuSehat=new SatuSehatCekNIK();  
    private StringBuilder htmlContent;   
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean ceksukses = false; 
    
    /** Creates new form DlgKamar
     * @param parent
     * @param modal */
    public SatuSehatKirimEncounter(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();

        this.setLocation(10,2);
        setSize(628,674);

        tabMode=new DefaultTableModel(null,new String[]{
                "P","Tanggal Registrasi","No.Rawat","No.RM","Nama Pasien","No.KTP Pasien","Kode Dokter","Nama Dokter",
                "No.KTP Dokter","Kode Poli","Nama Poli/Unit","ID Lokasi Unit","Stts Rawat","Stts Lanjut",
                "Tanggal Pulang","ID Encounter"
            }){
              @Override public boolean isCellEditable(int rowIndex, int colIndex){
                boolean a = false;
                if (colIndex==0) {
                    a=true;
                }
                return a;
             }
             Class[] types = new Class[] {
                 java.lang.Boolean.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, 
                 java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, 
                 java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, 
                 java.lang.String.class
             };
             @Override
             public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
             }
        };
        tbObat.setModel(tabMode);

        //tbKamar.setDefaultRenderer(Object.class, new WarnaTable(panelJudul.getBackground(),tbKamar.getBackground()));
        tbObat.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbObat.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 16; i++) {
            TableColumn column = tbObat.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(20);
            }else if(i==1){
                column.setPreferredWidth(150);
            }else if(i==2){
                column.setPreferredWidth(105);
            }else if(i==3){
                column.setPreferredWidth(70);
            }else if(i==4){
                column.setPreferredWidth(150);
            }else if(i==5){
                column.setPreferredWidth(110);
            }else if(i==6){
                column.setPreferredWidth(80);
            }else if(i==7){
                column.setPreferredWidth(150);
            }else if(i==8){
                column.setPreferredWidth(110);
            }else if(i==9){
                column.setPreferredWidth(80);
            }else if(i==10){
                column.setPreferredWidth(140);
            }else if(i==11){
                column.setPreferredWidth(210);
            }else if(i==12){
                column.setPreferredWidth(63);
            }else if(i==13){
                column.setPreferredWidth(63);
            }else if(i==14){
                column.setPreferredWidth(150);
            }else if(i==15){
                column.setPreferredWidth(215);
            }
        }
        tbObat.setDefaultRenderer(Object.class, new WarnaTable());
        
        TCari.setDocument(new batasInput((byte)100).getKata(TCari));

        cmbStatus.addActionListener(e -> runBackground(() -> tampil()));
        
        try {
            link=koneksiDB.URLFHIRSATUSEHAT();
        } catch (Exception e) {
            System.out.println("Notif : "+e);
        }  
        
        HTMLEditorKit kit = new HTMLEditorKit();
        LoadHTML.setEditable(true);
        LoadHTML.setEditorKit(kit);
        StyleSheet styleSheet = kit.getStyleSheet();
        styleSheet.addRule(
                ".isi td{border-right: 1px solid #e2e7dd;font: 8.5px tahoma;height:12px;border-bottom: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"+
                ".isi2 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#323232;}"+
                ".isi3 td{border-right: 1px solid #e2e7dd;font: 8.5px tahoma;height:12px;border-top: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"+
                ".isi4 td{font: 11px tahoma;height:12px;border-top: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"+
                ".isi5 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#AA0000;}"+
                ".isi6 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#FF0000;}"+
                ".isi7 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#C8C800;}"+
                ".isi8 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#00AA00;}"+
                ".isi9 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#969696;}"
        );
        Document doc = kit.createDefaultDocument();
        LoadHTML.setDocument(doc);
    }
    
    

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPopupMenu1 = new javax.swing.JPopupMenu();
        ppPilihSemua = new javax.swing.JMenuItem();
        ppBersihkan = new javax.swing.JMenuItem();
        LoadHTML = new widget.editorpane();
        internalFrame1 = new widget.InternalFrame();
        Scroll = new widget.ScrollPane();
        tbObat = new widget.Table();
        jPanel3 = new javax.swing.JPanel();
        panelGlass8 = new widget.panelisi();
        jLabel7 = new widget.Label();
        LCount = new widget.Label();
        BtnAll = new widget.Button();
        BtnKirim = new widget.Button();
        BtnCreateIHS = new widget.Button();
        BtnConsent = new widget.Button();
        BtnUpdate = new widget.Button();
        BtnPrint = new widget.Button();
        BtnKeluar = new widget.Button();
        panelGlass9 = new widget.panelisi();
        jLabel15 = new widget.Label();
        DTPCari1 = new widget.Tanggal();
        jLabel17 = new widget.Label();
        DTPCari2 = new widget.Tanggal();
        jLabel16 = new widget.Label();
        TCari = new widget.TextBox();
        BtnCari = new widget.Button();
        jLabel18 = new widget.Label();
        cmbStatus = new widget.ComboBox();

        jPopupMenu1.setName("jPopupMenu1"); // NOI18N

        ppPilihSemua.setBackground(new java.awt.Color(255, 255, 254));
        ppPilihSemua.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        ppPilihSemua.setForeground(new java.awt.Color(50, 50, 50));
        ppPilihSemua.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        ppPilihSemua.setText("Pilih Semua");
        ppPilihSemua.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        ppPilihSemua.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        ppPilihSemua.setName("ppPilihSemua"); // NOI18N
        ppPilihSemua.setPreferredSize(new java.awt.Dimension(150, 26));
        ppPilihSemua.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ppPilihSemuaActionPerformed(evt);
            }
        });
        jPopupMenu1.add(ppPilihSemua);

        ppBersihkan.setBackground(new java.awt.Color(255, 255, 254));
        ppBersihkan.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        ppBersihkan.setForeground(new java.awt.Color(50, 50, 50));
        ppBersihkan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        ppBersihkan.setText("Hilangkan Pilihan");
        ppBersihkan.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        ppBersihkan.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        ppBersihkan.setName("ppBersihkan"); // NOI18N
        ppBersihkan.setPreferredSize(new java.awt.Dimension(150, 26));
        ppBersihkan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ppBersihkanActionPerformed(evt);
            }
        });
        jPopupMenu1.add(ppBersihkan);

        LoadHTML.setBorder(null);
        LoadHTML.setName("LoadHTML"); // NOI18N

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setIconImage(null);
        setIconImages(null);
        setUndecorated(true);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Pengiriman Data Encounter Satu Sehat ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50, 50, 50))); // NOI18N
        internalFrame1.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        internalFrame1.setName("internalFrame1"); // NOI18N
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

        Scroll.setComponentPopupMenu(jPopupMenu1);
        Scroll.setName("Scroll"); // NOI18N
        Scroll.setOpaque(true);

        tbObat.setComponentPopupMenu(jPopupMenu1);
        tbObat.setName("tbObat"); // NOI18N
        Scroll.setViewportView(tbObat);

        internalFrame1.add(Scroll, java.awt.BorderLayout.CENTER);

        jPanel3.setName("jPanel3"); // NOI18N
        jPanel3.setOpaque(false);
        jPanel3.setPreferredSize(new java.awt.Dimension(44, 100));
        jPanel3.setLayout(new java.awt.BorderLayout(1, 1));

        panelGlass8.setName("panelGlass8"); // NOI18N
        panelGlass8.setPreferredSize(new java.awt.Dimension(44, 44));
        panelGlass8.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        jLabel7.setText("Record :");
        jLabel7.setName("jLabel7"); // NOI18N
        jLabel7.setPreferredSize(new java.awt.Dimension(53, 23));
        panelGlass8.add(jLabel7);

        LCount.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LCount.setText("0");
        LCount.setName("LCount"); // NOI18N
        LCount.setPreferredSize(new java.awt.Dimension(60, 23));
        panelGlass8.add(LCount);

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

        BtnKirim.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/34.png"))); // NOI18N
        BtnKirim.setMnemonic('K');
        BtnKirim.setText("Kirim");
        BtnKirim.setToolTipText("Alt+K");
        BtnKirim.setName("BtnKirim"); // NOI18N
        BtnKirim.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnKirim.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnKirimActionPerformed(evt);
            }
        });
        panelGlass8.add(BtnKirim);

        BtnCreateIHS.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        BtnCreateIHS.setMnemonic('I');
        BtnCreateIHS.setText("IHS");
        BtnCreateIHS.setToolTipText("Create IHS untuk pasien terpilih (Alt+I)");
        BtnCreateIHS.setName("BtnCreateIHS"); // NOI18N
        BtnCreateIHS.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnCreateIHS.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnCreateIHSActionPerformed(evt);
            }
        });
        panelGlass8.add(BtnCreateIHS);

        BtnConsent.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        BtnConsent.setMnemonic('N');
        BtnConsent.setText("Consent");
        BtnConsent.setToolTipText("Submit Consent untuk pasien terpilih (Alt+N)");
        BtnConsent.setName("BtnConsent"); // NOI18N
        BtnConsent.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnConsent.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnConsentActionPerformed(evt);
            }
        });
        panelGlass8.add(BtnConsent);

        BtnUpdate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/edit_f2.png"))); // NOI18N
        BtnUpdate.setMnemonic('U');
        BtnUpdate.setText("Update");
        BtnUpdate.setToolTipText("Alt+U");
        BtnUpdate.setName("BtnUpdate"); // NOI18N
        BtnUpdate.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnUpdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnUpdateActionPerformed(evt);
            }
        });
        panelGlass8.add(BtnUpdate);

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
        panelGlass8.add(BtnPrint);

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

        jPanel3.add(panelGlass8, java.awt.BorderLayout.CENTER);

        panelGlass9.setName("panelGlass9"); // NOI18N
        panelGlass9.setPreferredSize(new java.awt.Dimension(44, 44));
        panelGlass9.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        jLabel15.setText("Tgl.Registrasi :");
        jLabel15.setName("jLabel15"); // NOI18N
        jLabel15.setPreferredSize(new java.awt.Dimension(85, 23));
        panelGlass9.add(jLabel15);

        DTPCari1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "05-04-2026" }));
        DTPCari1.setDisplayFormat("dd-MM-yyyy");
        DTPCari1.setName("DTPCari1"); // NOI18N
        DTPCari1.setOpaque(false);
        DTPCari1.setPreferredSize(new java.awt.Dimension(95, 23));
        panelGlass9.add(DTPCari1);

        jLabel17.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel17.setText("s.d.");
        jLabel17.setName("jLabel17"); // NOI18N
        jLabel17.setPreferredSize(new java.awt.Dimension(24, 23));
        panelGlass9.add(jLabel17);

        DTPCari2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "05-04-2026" }));
        DTPCari2.setDisplayFormat("dd-MM-yyyy");
        DTPCari2.setName("DTPCari2"); // NOI18N
        DTPCari2.setOpaque(false);
        DTPCari2.setPreferredSize(new java.awt.Dimension(95, 23));
        panelGlass9.add(DTPCari2);

        jLabel16.setText("Key Word :");
        jLabel16.setName("jLabel16"); // NOI18N
        jLabel16.setPreferredSize(new java.awt.Dimension(70, 23));
        panelGlass9.add(jLabel16);

        TCari.setName("TCari"); // NOI18N
        TCari.setPreferredSize(new java.awt.Dimension(210, 23));
        TCari.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TCariKeyPressed(evt);
            }
        });
        panelGlass9.add(TCari);

        BtnCari.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png"))); // NOI18N
        BtnCari.setMnemonic('6');
        BtnCari.setToolTipText("Alt+6");
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

        jLabel18.setText("Status Kirim:");
        jLabel18.setName("jLabel18"); // NOI18N
        jLabel18.setPreferredSize(new java.awt.Dimension(70, 23));
        panelGlass9.add(jLabel18);

        cmbStatus.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Semua", "Belum dikirim", "Sudah dikirim" }));
        cmbStatus.setName("cmbStatus"); // NOI18N
        panelGlass9.add(cmbStatus);

        jPanel3.add(panelGlass9, java.awt.BorderLayout.PAGE_START);

        internalFrame1.add(jPanel3, java.awt.BorderLayout.PAGE_END);

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void BtnKeluarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnKeluarActionPerformed
        dispose();
    }//GEN-LAST:event_BtnKeluarActionPerformed

    private void BtnKeluarKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnKeluarKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            dispose();
        }else{Valid.pindah(evt,BtnPrint,BtnKeluar);}
    }//GEN-LAST:event_BtnKeluarKeyPressed

    private void BtnPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnPrintActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try{
            htmlContent = new StringBuilder();
            htmlContent.append(                             
                "<tr class='isi'>"+
                    "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Tanggal Registrasi</b></td>"+
                    "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>No.Rawat</b></td>"+
                    "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>No.RM</b></td>"+
                    "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Nama Pasien</b></td>"+
                    "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>No.KTP Pasien</b></td>"+
                    "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Kode Dokter</b></td>"+
                    "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Nama Dokter</b></td>"+
                    "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>No.KTP Dokter</b></td>"+
                    "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Kode Poli</b></td>"+
                    "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Nama Poli/Unit</b></td>"+
                    "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>ID Lokasi Unit</b></td>"+
                    "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Stts Rawat</b></td>"+
                    "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Stts Lanjut</b></td>"+
                    "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Tanggal Pulang</b></td>"+
                    "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>ID Encounter</b></td>"+
                "</tr>"
            );
            for (i = 0; i < tabMode.getRowCount(); i++) {
                htmlContent.append(
                    "<tr class='isi'>"+
                        "<td valign='top'>"+tbObat.getValueAt(i,1).toString()+"</td>"+
                        "<td valign='top'>"+tbObat.getValueAt(i,2).toString()+"</td>"+
                        "<td valign='top'>"+tbObat.getValueAt(i,3).toString()+"</td>"+
                        "<td valign='top'>"+tbObat.getValueAt(i,4).toString()+"</td>"+
                        "<td valign='top'>"+tbObat.getValueAt(i,5).toString()+"</td>"+
                        "<td valign='top'>"+tbObat.getValueAt(i,6).toString()+"</td>"+
                        "<td valign='top'>"+tbObat.getValueAt(i,7).toString()+"</td>"+
                        "<td valign='top'>"+tbObat.getValueAt(i,8).toString()+"</td>"+
                        "<td valign='top'>"+tbObat.getValueAt(i,9).toString()+"</td>"+
                        "<td valign='top'>"+tbObat.getValueAt(i,10).toString()+"</td>"+
                        "<td valign='top'>"+tbObat.getValueAt(i,11).toString()+"</td>"+
                        "<td valign='top'>"+tbObat.getValueAt(i,12).toString()+"</td>"+
                        "<td valign='top'>"+tbObat.getValueAt(i,13).toString()+"</td>"+
                        "<td valign='top'>"+tbObat.getValueAt(i,14).toString()+"</td>"+
                        "<td valign='top'>"+tbObat.getValueAt(i,15).toString()+"</td>"+
                    "</tr>");
            }
            LoadHTML.setText(
                "<html>"+
                  "<table width='100%' border='0' align='center' cellpadding='1px' cellspacing='0' class='tbl_form'>"+
                   htmlContent.toString()+
                  "</table>"+
                "</html>"
            );
            htmlContent=null;

            File g = new File("file2.css");            
            BufferedWriter bg = new BufferedWriter(new FileWriter(g));
            bg.write(
                ".isi td{border-right: 1px solid #e2e7dd;font: 8.5px tahoma;height:12px;border-bottom: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"+
                ".isi2 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#323232;}"+
                ".isi3 td{border-right: 1px solid #e2e7dd;font: 8.5px tahoma;height:12px;border-top: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"+
                ".isi4 td{font: 11px tahoma;height:12px;border-top: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"+
                ".isi5 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#AA0000;}"+
                ".isi6 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#FF0000;}"+
                ".isi7 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#C8C800;}"+
                ".isi8 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#00AA00;}"+
                ".isi9 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#969696;}"
            );
            bg.close();

            File f = new File("DataSatuSehatEncounter.html");            
            BufferedWriter bw = new BufferedWriter(new FileWriter(f));            
            bw.write(LoadHTML.getText().replaceAll("<head>","<head>"+
                        "<link href=\"file2.css\" rel=\"stylesheet\" type=\"text/css\" />"+
                        "<table width='100%' border='0' align='center' cellpadding='3px' cellspacing='0' class='tbl_form'>"+
                            "<tr class='isi2'>"+
                                "<td valign='top' align='center'>"+
                                    "<font size='4' face='Tahoma'>"+akses.getnamars()+"</font><br>"+
                                    akses.getalamatrs()+", "+akses.getkabupatenrs()+", "+akses.getpropinsirs()+"<br>"+
                                    akses.getkontakrs()+", E-mail : "+akses.getemailrs()+"<br><br>"+
                                    "<font size='2' face='Tahoma'>DATA PENGIRIMAN SATU SEHAT ENCOUNTER<br><br></font>"+        
                                "</td>"+
                           "</tr>"+
                        "</table>")
            );
            bw.close();                         
            Desktop.getDesktop().browse(f.toURI());
        }catch(Exception e){
            System.out.println("Notifikasi : "+e);
        }
        this.setCursor(Cursor.getDefaultCursor());       
    }//GEN-LAST:event_BtnPrintActionPerformed

    private void TCariKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TCariKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            BtnCariActionPerformed(null);
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_DOWN){
            BtnCariActionPerformed(null);
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_UP){
            BtnKeluar.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_UP){
            tbObat.requestFocus();
        }
    }//GEN-LAST:event_TCariKeyPressed

    private void BtnCariActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCariActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        runBackground(() ->tampil());
        this.setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_BtnCariActionPerformed

    private void BtnCariKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnCariKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnCariActionPerformed(null);
        }else{
            Valid.pindah(evt,TCari,BtnPrint);
        }
    }//GEN-LAST:event_BtnCariKeyPressed

    private void BtnKirimActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnKirimActionPerformed
        for(i=0;i<tbObat.getRowCount();i++){
            if(tbObat.getValueAt(i,0).toString().equals("true")&&(!tbObat.getValueAt(i,5).toString().equals(""))&&(!tbObat.getValueAt(i,8).toString().equals(""))&&tbObat.getValueAt(i,15).toString().equals("")){
                try {
                    iddokter=cekViaSatuSehat.tampilIDParktisi(tbObat.getValueAt(i,8).toString());
                    idpasien=cekViaSatuSehat.tampilIDPasien(tbObat.getValueAt(i,5).toString());
                    String startUtc = toUtc(tbObat.getValueAt(i,1).toString());
                    String endUtc   = toUtc(tbObat.getValueAt(i,14).toString());
                    String episodeType = getEpisodeOfCareType(tbObat.getValueAt(i,10).toString());
                    if(episodeType != null){
                        String eocCode = episodeType.split("\\|")[0];
                        String eocDisplay = episodeType.split("\\|")[1];
                        idepisode="";
                        try{
                            headers = new HttpHeaders();
                            headers.setContentType(MediaType.APPLICATION_JSON);
                            headers.add("Authorization", "Bearer "+api.TokenSatuSehat());
                            json = "{" +
                                        "\"resourceType\": \"EpisodeOfCare\"," +
                                        "\"identifier\": [" +
                                            "{" +
                                                "\"system\": \"http://sys-ids.kemkes.go.id/episode-of-care/"+koneksiDB.IDSATUSEHAT()+"\"," +
                                                "\"value\": \""+tbObat.getValueAt(i,2).toString()+"\"" +
                                            "}" +
                                        "]," +
                                        "\"status\": \"active\"," +
                                        "\"statusHistory\": [" +
                                            "{" +
                                                "\"status\": \"active\"," +
                                                "\"period\": {" +
                                                    "\"start\": \""+startUtc+"\"" +
                                                "}" +
                                            "}" +
                                        "]," +
                                        "\"type\": [" +
                                            "{" +
                                                "\"coding\": [" +
                                                    "{" +
                                                        "\"system\": \"http://terminology.kemkes.go.id/CodeSystem/episodeofcare-type\"," +
                                                        "\"code\": \""+eocCode+"\"," +
                                                        "\"display\": \""+eocDisplay+"\"" +
                                                    "}" +
                                                "]" +
                                            "}" +
                                        "]," +
                                        "\"patient\": {" +
                                            "\"reference\": \"Patient/"+idpasien+"\"," +
                                            "\"display\": \""+tbObat.getValueAt(i,4).toString()+"\"" +
                                        "}," +
                                        "\"period\": {" +
                                            "\"start\": \""+startUtc+"\"" +
                                        "}," +
                                        "\"managingOrganization\": {" +
                                            "\"reference\": \"Organization/"+koneksiDB.IDSATUSEHAT()+"\"" +
                                        "}" +
                                    "}";
                            System.out.println("URL : "+link+"/EpisodeOfCare");
                            System.out.println("Request JSON : "+json);
                            requestEntity = new HttpEntity(json,headers);
                            json=api.getRest().exchange(link+"/EpisodeOfCare", HttpMethod.POST, requestEntity, String.class).getBody();
                            System.out.println("Result JSON : "+json);
                            root = mapper.readTree(json);
                            response = root.path("id");
                            if(!response.asText().equals("")){
                                idepisode=response.asText();
                                Sequel.menyimpan("satu_sehat_episodeofcare","?,?","No.Rawat",2,new String[]{
                                    tbObat.getValueAt(i,2).toString(),response.asText()
                                });
                            }
                        }catch(Exception e){
                            System.out.println("Notifikasi Bridging : "+e);
                        }

                        try{
                            headers = new HttpHeaders();
                            headers.setContentType(MediaType.APPLICATION_JSON);
                            headers.add("Authorization", "Bearer "+api.TokenSatuSehat());
                            json = "{" +
                                        "\"resourceType\": \"Encounter\"," +
                                        "\"status\": \"arrived\"," +
                                        "\"class\": {" +
                                            "\"system\": \"http://terminology.hl7.org/CodeSystem/v3-ActCode\"," +
                                            "\"code\": \""+(tbObat.getValueAt(i,13).toString().equals("Ralan")?"AMB":"IMP")+"\"," +
                                            "\"display\": \""+(tbObat.getValueAt(i,13).toString().equals("Ralan")?"ambulatory":"inpatient encounter")+"\"" +
                                        "}," +
                                        "\"subject\": {" +
                                            "\"reference\": \"Patient/"+idpasien+"\"," +
                                            "\"display\": \""+tbObat.getValueAt(i,4).toString()+"\"" +
                                        "}," +
                                        "\"participant\": [" +
                                            "{" +
                                                "\"type\": [" +
                                                    "{" +
                                                        "\"coding\": [" +
                                                            "{" +
                                                                "\"system\": \"http://terminology.hl7.org/CodeSystem/v3-ParticipationType\"," +
                                                                "\"code\": \"ATND\"," +
                                                                "\"display\": \"attender\"" +
                                                            "}" +
                                                        "]" +
                                                    "}" +
                                                "]," +
                                                "\"individual\": {" +
                                                    "\"reference\": \"Practitioner/"+iddokter+"\"," +
                                                    "\"display\": \""+tbObat.getValueAt(i,7).toString()+"\"" +
                                                "}" +
                                            "}" +
                                        "]," +
                                        "\"period\": {" +
                                            "\"start\": \""+startUtc+"\"" +
                                        "}," +
                                        "\"location\": [" +
                                            "{" +
                                                "\"location\": {" +
                                                    "\"reference\": \"Location/"+tbObat.getValueAt(i,11).toString()+"\"," +
                                                    "\"display\": \""+tbObat.getValueAt(i,10).toString()+"\"" +
                                                "}" +
                                            "}" +
                                        "]," +
                                        "\"statusHistory\": [" +
                                            "{" +
                                                "\"status\": \"arrived\"," +
                                                "\"period\": {" +
                                                    "\"start\": \""+startUtc+"\"," +
                                                    "\"end\": \""+endUtc+"\"" +
                                                "}" +
                                            "}" +
                                        "]," +
                                        "\"serviceProvider\": {" +
                                            "\"reference\": \"Organization/"+koneksiDB.IDSATUSEHAT()+"\"" +
                                        "}," +
                                        (idepisode.isEmpty()?"":
                                        "\"episodeOfCare\": [" +
                                            "{" +
                                                "\"reference\": \"EpisodeOfCare/"+idepisode+"\"" +
                                            "}" +
                                        "],") +
                                        "\"identifier\": [" +
                                            "{" +
                                                "\"system\": \"http://sys-ids.kemkes.go.id/encounter/"+koneksiDB.IDSATUSEHAT()+"\"," +
                                                "\"value\": \""+tbObat.getValueAt(i,2).toString()+"\"" +
                                            "}" +
                                        "]" +
                                    "}";
                            System.out.println("URL : "+link+"/Encounter");
                            System.out.println("Request JSON : "+json);
                            requestEntity = new HttpEntity(json,headers);
                            json=api.getRest().exchange(link+"/Encounter", HttpMethod.POST, requestEntity, String.class).getBody();
                            System.out.println("Result JSON : "+json);
                            root = mapper.readTree(json);
                            response = root.path("id");
                            if(!response.asText().equals("")){
                                if(Sequel.menyimpantf2("satu_sehat_encounter","?,?","No.Rawat",2,new String[]{
                                    tbObat.getValueAt(i,2).toString(),response.asText()
                                })==true){
                                    tbObat.setValueAt(response.asText(),i,15);
                                    tbObat.setValueAt(false,i,0);
                                }
                            }
                        }catch(Exception e){
                            System.out.println("Notifikasi Bridging : "+e);
                        }
                    }else{
                        try{
                            headers = new HttpHeaders();
                            headers.setContentType(MediaType.APPLICATION_JSON);
                            headers.add("Authorization", "Bearer "+api.TokenSatuSehat());
                            json = "{" +
                                        "\"resourceType\": \"Encounter\"," +
                                        "\"status\": \"arrived\"," +
                                        "\"class\": {" +
                                            "\"system\": \"http://terminology.hl7.org/CodeSystem/v3-ActCode\"," +
                                            "\"code\": \""+(tbObat.getValueAt(i,13).toString().equals("Ralan")?"AMB":"IMP")+"\"," +
                                            "\"display\": \""+(tbObat.getValueAt(i,13).toString().equals("Ralan")?"ambulatory":"inpatient encounter")+"\"" +
                                        "}," +
                                        "\"subject\": {" +
                                            "\"reference\": \"Patient/"+idpasien+"\"," +
                                            "\"display\": \""+tbObat.getValueAt(i,4).toString()+"\"" +
                                        "}," +
                                        "\"participant\": [" +
                                            "{" +
                                                "\"type\": [" +
                                                    "{" +
                                                        "\"coding\": [" +
                                                            "{" +
                                                                "\"system\": \"http://terminology.hl7.org/CodeSystem/v3-ParticipationType\"," +
                                                                "\"code\": \"ATND\"," +
                                                                "\"display\": \"attender\"" +
                                                            "}" +
                                                        "]" +
                                                    "}" +
                                                "]," +
                                                "\"individual\": {" +
                                                    "\"reference\": \"Practitioner/"+iddokter+"\"," +
                                                    "\"display\": \""+tbObat.getValueAt(i,7).toString()+"\"" +
                                                "}" +
                                            "}" +
                                        "]," +
                                        "\"period\": {" +
                                            "\"start\": \""+startUtc+"\"" +
                                        "}," +
                                        "\"location\": [" +
                                            "{" +
                                                "\"location\": {" +
                                                    "\"reference\": \"Location/"+tbObat.getValueAt(i,11).toString()+"\"," +
                                                    "\"display\": \""+tbObat.getValueAt(i,10).toString()+"\"" +
                                                "}" +
                                            "}" +
                                        "]," +
                                        "\"statusHistory\": [" +
                                            "{" +
                                                "\"status\": \"arrived\"," +
                                                "\"period\": {" +
                                                    "\"start\": \""+startUtc+"\"," +
                                                    "\"end\": \""+endUtc+"\"" +
                                                "}" +
                                            "}" +
                                        "]," +
                                        "\"serviceProvider\": {" +
                                            "\"reference\": \"Organization/"+koneksiDB.IDSATUSEHAT()+"\"" +
                                        "}," +
                                        "\"identifier\": [" +
                                            "{" +
                                                "\"system\": \"http://sys-ids.kemkes.go.id/encounter/"+koneksiDB.IDSATUSEHAT()+"\"," +
                                                "\"value\": \""+tbObat.getValueAt(i,2).toString()+"\"" +
                                            "}" +
                                        "]" +
                                    "}";
                            System.out.println("URL : "+link+"/Encounter");
                            System.out.println("Request JSON : "+json);
                            requestEntity = new HttpEntity(json,headers);
                            json=api.getRest().exchange(link+"/Encounter", HttpMethod.POST, requestEntity, String.class).getBody();
                            System.out.println("Result JSON : "+json);
                            root = mapper.readTree(json);
                            response = root.path("id");
                            if(!response.asText().equals("")){
                                if(Sequel.menyimpantf2("satu_sehat_encounter","?,?","No.Rawat",2,new String[]{
                                    tbObat.getValueAt(i,2).toString(),response.asText()
                                })==true){
                                    tbObat.setValueAt(response.asText(),i,15);
                                    tbObat.setValueAt(false,i,0);
                                }
                            }
                        }catch(Exception e){
                            System.out.println("Notifikasi Bridging : "+e);
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Notifikasi : "+e);
                }
            }
        }
    }//GEN-LAST:event_BtnKirimActionPerformed

    private void BtnCreateIHSActionPerformed(java.awt.event.ActionEvent evt) {
        int row = tbObat.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(null, "Pilih dulu row pasien di tabel.");
            return;
        }
        final String nik  = tbObat.getValueAt(row, 5) == null ? "" : tbObat.getValueAt(row, 5).toString().trim();
        final String nama = tbObat.getValueAt(row, 4) == null ? "" : tbObat.getValueAt(row, 4).toString().trim();
        if (nik.isEmpty() || nik.length() != 16 || !nik.matches("\\d+")) {
            JOptionPane.showMessageDialog(null, "NIK pasien di row ini tidak valid (harus 16 digit angka).");
            return;
        }

        String cachedEarly = Sequel.cariIsi(
                "SELECT ihs_number FROM satu_sehat_cache_ihs_pasien WHERE no_ktp=?", nik);
        if (cachedEarly != null && !cachedEarly.isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "Pasien sudah memiliki IHS (cache lokal).\nIHS: " + cachedEarly);
            return;
        }

        String[] cols = { "jk", "tmp_lahir", "tgl_lahir", "alamat", "no_tlp", "email",
                          "kd_prop", "kd_kab", "kd_kec", "kd_kel" };
        String[] vals = new String[cols.length];
        for (int k = 0; k < cols.length; k++) {
            vals[k] = Sequel.cariIsi("SELECT " + cols[k] + " FROM pasien WHERE no_ktp=?", nik);
            if (vals[k] == null) vals[k] = "";
        }
        final String jk = vals[0], tglLahir = vals[2], alamatRaw = vals[3].trim(),
                     phone = vals[4].trim(), email = vals[5].trim(),
                     dbKdProp = vals[6], dbKdKab = vals[7], dbKdKec = vals[8], dbKdKel = vals[9];

        if (nama.isEmpty()) { JOptionPane.showMessageDialog(null, "Nama pasien kosong."); return; }
        if (jk == null || jk.isEmpty()) { JOptionPane.showMessageDialog(null, "Jenis kelamin pasien kosong di tabel pasien."); return; }
        if (tglLahir == null || tglLahir.isEmpty() || tglLahir.equals("0000-00-00")) {
            JOptionPane.showMessageDialog(null, "Tanggal lahir pasien kosong."); return;
        }
        if (alamatRaw.isEmpty()) { JOptionPane.showMessageDialog(null, "Alamat pasien kosong."); return; }

        if (dbKdProp == null || !dbKdProp.matches("\\d{2}"))
        { JOptionPane.showMessageDialog(null, "Kode propinsi pasien (" + dbKdProp + ") belum format KEMENDAGRI 2-digit."); return; }
        if (dbKdKab == null || !dbKdKab.matches("\\d{4}"))
        { JOptionPane.showMessageDialog(null, "Kode kabupaten pasien (" + dbKdKab + ") belum format KEMENDAGRI 4-digit."); return; }
        if (dbKdKec == null || !dbKdKec.matches("\\d{6}"))
        { JOptionPane.showMessageDialog(null, "Kode kecamatan pasien (" + dbKdKec + ") belum format KEMENDAGRI 6-digit."); return; }
        if (dbKdKel == null || !dbKdKel.matches("\\d{10}"))
        { JOptionPane.showMessageDialog(null, "Kode kelurahan pasien (" + dbKdKel + ") belum format KEMENDAGRI 10-digit."); return; }

        java.util.regex.Matcher rtrwM = java.util.regex.Pattern
                .compile("(?i)\\bRT\\s*0*(\\d{1,3})\\s*[/\\\\\\-, ]+\\s*RW\\s*0*(\\d{1,3})\\b")
                .matcher(alamatRaw);
        final String rt, rw, line;
        if (rtrwM.find()) {
            rt = String.format("%03d", Integer.parseInt(rtrwM.group(1)));
            rw = String.format("%03d", Integer.parseInt(rtrwM.group(2)));
            String cleaned = (alamatRaw.substring(0, rtrwM.start()) + alamatRaw.substring(rtrwM.end()))
                    .replaceAll("\\s+", " ").replaceAll("^[,\\s]+|[,\\s]+$", "").trim();
            line = cleaned.isEmpty() ? alamatRaw : cleaned;
        } else { rt = ""; rw = ""; line = alamatRaw; }

        final String pkdProp = dbKdProp, pkdKab = dbKdKab, pkdKec = dbKdKec, pkdKel = dbKdKel;
        final String fjk = jk, ftgl = tglLahir;

        final javax.swing.JDialog progress = new javax.swing.JDialog(
                javax.swing.SwingUtilities.getWindowAncestor(this), "Mohon tunggu",
                java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        javax.swing.JLabel lbl = new javax.swing.JLabel("Sedang mengirim ke SatuSehat…", javax.swing.SwingConstants.CENTER);
        lbl.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 16, 8, 16));
        javax.swing.JProgressBar bar = new javax.swing.JProgressBar();
        bar.setIndeterminate(true);
        bar.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 16, 12, 16));
        progress.setLayout(new java.awt.BorderLayout());
        progress.add(lbl, java.awt.BorderLayout.NORTH);
        progress.add(bar, java.awt.BorderLayout.CENTER);
        progress.setSize(340, 110);
        progress.setLocationRelativeTo(this);
        progress.setDefaultCloseOperation(javax.swing.JDialog.DO_NOTHING_ON_CLOSE);

        BtnCreateIHS.setEnabled(false);

        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                try {
                    String existing = cekViaSatuSehat.tampilIDPasien(nik);
                    if (existing != null && !existing.isEmpty()) return "DUP|" + existing;
                    SatuSehatCreatePasien creator = new SatuSehatCreatePasien();
                    boolean ok = creator.kirim(nik, nama, fjk, ftgl, line, rt, rw, "",
                            pkdProp, pkdKab, pkdKec, pkdKel, phone, email);
                    return ok ? "OK|" + creator.idpasien
                              : "ERR|" + (creator.errorMessage == null ? "" : creator.errorMessage);
                } catch (Exception e) {
                    return "ERR|" + (e.getMessage() == null ? e.toString() : e.getMessage());
                }
            }
            @Override
            protected void done() {
                progress.dispose();
                BtnCreateIHS.setEnabled(true);
                String result;
                try { result = get(); }
                catch (Exception e) { JOptionPane.showMessageDialog(null, "Gagal: " + e.getMessage()); return; }
                if (result == null) { JOptionPane.showMessageDialog(null, "Tidak ada respon."); return; }
                int sep = result.indexOf('|');
                String tag = sep < 0 ? result : result.substring(0, sep);
                String body = sep < 0 ? "" : result.substring(sep + 1);
                if ("DUP".equals(tag) || "OK".equals(tag)) {
                    Sequel.queryu2tf(
                        "INSERT INTO satu_sehat_cache_ihs_pasien (no_ktp, ihs_number) VALUES (?,?) " +
                        "ON DUPLICATE KEY UPDATE ihs_number=VALUES(ihs_number)",
                        2, new String[]{ nik, body });
                    JOptionPane.showMessageDialog(null,
                            ("DUP".equals(tag) ? "Pasien sudah memiliki IHS di SatuSehat.\n" : "Berhasil membuat IHS Pasien.\n")
                            + "IHS: " + body);
                } else {
                    JOptionPane.showMessageDialog(null, "Gagal membuat IHS:\n" + body);
                }
            }
        };
        worker.execute();
        progress.setVisible(true);
    }

    private void BtnConsentActionPerformed(java.awt.event.ActionEvent evt) {
        int row = tbObat.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(null, "Pilih dulu row pasien di tabel.");
            return;
        }
        final String nik  = tbObat.getValueAt(row, 5) == null ? "" : tbObat.getValueAt(row, 5).toString().trim();
        final String nama = tbObat.getValueAt(row, 4) == null ? "" : tbObat.getValueAt(row, 4).toString().trim();
        if (nik.isEmpty() || nik.length() != 16 || !nik.matches("\\d+")) {
            JOptionPane.showMessageDialog(null, "NIK pasien di row ini tidak valid (harus 16 digit angka).");
            return;
        }

        final String ihs = Sequel.cariIsi(
                "SELECT ihs_number FROM satu_sehat_cache_ihs_pasien WHERE no_ktp=?", nik);
        if (ihs == null || ihs.isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "Pasien belum punya IHS di cache lokal.\nKlik tombol IHS dulu untuk create / sync IHS pasien.");
            return;
        }

        String existingConsent = Sequel.cariIsi(
                "SELECT consent_id FROM satu_sehat_consent_pasien WHERE no_ktp=? AND status='active' " +
                "AND (period_end IS NULL OR period_end >= CURDATE()) ORDER BY created_at DESC LIMIT 1",
                nik);
        if (existingConsent != null && !existingConsent.isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "Pasien sudah punya Consent aktif.\nConsent ID: " + existingConsent);
            return;
        }

        int konfirm = JOptionPane.showConfirmDialog(this,
                "Catat persetujuan pasien (audit lokal).\n\n" +
                "Pasien : " + nama + "\nNIK    : " + nik + "\nIHS    : " + ihs + "\n\n" +
                "Catatan: SatuSehat Consent endpoint sedang tidak tersedia.\n" +
                "Persetujuan dicatat di DB lokal sebagai audit trail (berlaku 5 tahun).\n" +
                "Klik YES kalau pasien sudah tanda tangan form persetujuan tertulis.",
                "Konfirmasi Consent (lokal)", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (konfirm != JOptionPane.YES_OPTION) return;

        final javax.swing.JDialog progress = new javax.swing.JDialog(
                javax.swing.SwingUtilities.getWindowAncestor(this), "Mohon tunggu",
                java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        javax.swing.JLabel lbl = new javax.swing.JLabel("Mencatat persetujuan…", javax.swing.SwingConstants.CENTER);
        lbl.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 16, 8, 16));
        javax.swing.JProgressBar bar = new javax.swing.JProgressBar();
        bar.setIndeterminate(true);
        bar.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 16, 12, 16));
        progress.setLayout(new java.awt.BorderLayout());
        progress.add(lbl, java.awt.BorderLayout.NORTH);
        progress.add(bar, java.awt.BorderLayout.CENTER);
        progress.setSize(360, 110);
        progress.setLocationRelativeTo(this);
        progress.setDefaultCloseOperation(javax.swing.JDialog.DO_NOTHING_ON_CLOSE);

        BtnConsent.setEnabled(false);

        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            SatuSehatCreateConsent creator = new SatuSehatCreateConsent();
            @Override
            protected String doInBackground() {
                try {
                    boolean ok = creator.kirim(ihs, nama, 5);
                    return ok ? "OK" : "ERR|" + (creator.errorMessage == null ? "" : creator.errorMessage);
                } catch (Exception e) {
                    return "ERR|" + (e.getMessage() == null ? e.toString() : e.getMessage());
                }
            }
            @Override
            protected void done() {
                progress.dispose();
                BtnConsent.setEnabled(true);
                String result;
                try { result = get(); }
                catch (Exception e) { JOptionPane.showMessageDialog(null, "Gagal: " + e.getMessage()); return; }
                if ("OK".equals(result)) {
                    Sequel.queryu2tf(
                        "INSERT INTO satu_sehat_consent_pasien " +
                        "(no_ktp, ihs_number, consent_id, status, period_start, period_end, raw_response) " +
                        "VALUES (?,?,?,?,?,?,?) " +
                        "ON DUPLICATE KEY UPDATE status=VALUES(status), period_start=VALUES(period_start), " +
                        "period_end=VALUES(period_end), raw_response=VALUES(raw_response)",
                        7, new String[]{
                            nik, ihs, creator.consentId, "active",
                            creator.periodStart, creator.periodEnd, creator.rawResponse
                        });
                    JOptionPane.showMessageDialog(null,
                            "Persetujuan tercatat (audit lokal).\n\n" +
                            "Consent ID : " + creator.consentId + "\n" +
                            "Berlaku    : " + creator.periodStart + " s/d " + creator.periodEnd + "\n\n" +
                            "Catatan: SatuSehat Consent endpoint belum tersedia.");
                } else {
                    String body = result == null ? "" : (result.indexOf('|') > 0 ? result.substring(result.indexOf('|') + 1) : result);
                    JOptionPane.showMessageDialog(null, "Gagal submit Consent:\n" + body);
                }
            }
        };
        worker.execute();
        progress.setVisible(true);
    }

    private void ppPilihSemuaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ppPilihSemuaActionPerformed
        for(i=0;i<tbObat.getRowCount();i++){
            tbObat.setValueAt(true,i,0);
        }
    }//GEN-LAST:event_ppPilihSemuaActionPerformed

    private void ppBersihkanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ppBersihkanActionPerformed
        for(i=0;i<tbObat.getRowCount();i++){
            tbObat.setValueAt(false,i,0);
        }
    }//GEN-LAST:event_ppBersihkanActionPerformed

    private void BtnUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnUpdateActionPerformed
        for(i=0;i<tbObat.getRowCount();i++){
            if(tbObat.getValueAt(i,0).toString().equals("true")&&(!tbObat.getValueAt(i,5).toString().equals(""))&&(!tbObat.getValueAt(i,8).toString().equals(""))&&(!tbObat.getValueAt(i,15).toString().equals(""))){
                try {
                    iddokter=cekViaSatuSehat.tampilIDParktisi(tbObat.getValueAt(i,8).toString());
                    idpasien=cekViaSatuSehat.tampilIDPasien(tbObat.getValueAt(i,5).toString());
                    try{
                        headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_JSON);
                        headers.add("Authorization", "Bearer "+api.TokenSatuSehat());
                        String diagnosis = getDiagnosisEncounter(tbObat.getValueAt(i,2).toString());
                        String startUtc = toUtc(tbObat.getValueAt(i,1).toString());
                        String endUtc   = toUtc(tbObat.getValueAt(i,14).toString());
                        boolean isRanap = "Ranap".equals(tbObat.getValueAt(i,13).toString());
                        json = "{" +
                                    "\"resourceType\": \"Encounter\"," +
                                    "\"id\": \""+tbObat.getValueAt(i,15).toString()+"\"," +
                                    "\"identifier\": [" +
                                        "{" +
                                            "\"system\": \"http://sys-ids.kemkes.go.id/encounter/"+koneksiDB.IDSATUSEHAT()+"\"," +
                                            "\"value\": \""+tbObat.getValueAt(i,2).toString()+"\"" +
                                        "}" +
                                    "]," +
                                    "\"status\": \"finished\"," +
                                    "\"class\": {" +
                                        "\"system\": \"http://terminology.hl7.org/CodeSystem/v3-ActCode\"," +
                                        "\"code\": \""+(isRanap?"IMP":"AMB")+"\"," +
                                        "\"display\": \""+(isRanap?"inpatient encounter":"ambulatory")+"\"" +
                                    "}," +
                                    "\"subject\": {" +
                                        "\"reference\": \"Patient/"+idpasien+"\"," +
                                        "\"display\": \""+tbObat.getValueAt(i,4).toString()+"\"" +
                                    "}," +
                                    "\"participant\": [" +
                                        "{" +
                                            "\"type\": [" +
                                                "{" +
                                                    "\"coding\": [" +
                                                        "{" +
                                                            "\"system\": \"http://terminology.hl7.org/CodeSystem/v3-ParticipationType\"," +
                                                            "\"code\": \"ATND\"," +
                                                            "\"display\": \"attender\"" +
                                                        "}" +
                                                    "]" +
                                                "}" +
                                            "]," +
                                            "\"individual\": {" +
                                                "\"reference\": \"Practitioner/"+iddokter+"\"," +
                                                "\"display\": \""+tbObat.getValueAt(i,7).toString()+"\"" +
                                            "}" +
                                        "}" +
                                    "]," +
                                    "\"period\": {" +
                                        "\"start\": \""+startUtc+"\"," +
                                        "\"end\": \""+endUtc+"\"" +
                                    "}," +
                                    "\"location\": [" +
                                        "{" +
                                            "\"location\": {" +
                                                "\"reference\": \"Location/"+tbObat.getValueAt(i,11).toString()+"\"," +
                                                "\"display\": \""+tbObat.getValueAt(i,10).toString()+"\"" +
                                            "}" +
                                        "}" +
                                    "]," +
                                    "\"statusHistory\": [" +
                                        "{" +
                                            "\"status\": \"arrived\"," +
                                            "\"period\": {" +
                                                "\"start\": \""+startUtc+"\"," +
                                                "\"end\": \""+startUtc+"\"" +
                                            "}" +
                                        "}," +
                                        "{" +
                                            "\"status\": \"in-progress\"," +
                                            "\"period\": {" +
                                                "\"start\": \""+startUtc+"\"," +
                                                "\"end\": \""+endUtc+"\"" +
                                            "}" +
                                        "}," +
                                        "{" +
                                            "\"status\": \"finished\"," +
                                            "\"period\": {" +
                                                "\"start\": \""+endUtc+"\"," +
                                                "\"end\": \""+endUtc+"\"" +
                                            "}" +
                                        "}" +
                                    "]," +
                                    (diagnosis.isEmpty()?"":"\"diagnosis\": ["+diagnosis+"],") +
                                    (isRanap?
                                        "\"hospitalization\": {" +
                                            "\"dischargeDisposition\": {" +
                                                "\"coding\": [" +
                                                    "{" +
                                                        "\"system\": \"http://terminology.hl7.org/CodeSystem/discharge-disposition\"," +
                                                        "\"code\": \"home\"," +
                                                        "\"display\": \"Home\"" +
                                                    "}" +
                                                "]" +
                                            "}" +
                                        "}," : "") +
                                    "\"serviceProvider\": {" +
                                        "\"reference\": \"Organization/"+koneksiDB.IDSATUSEHAT()+"\"" +
                                    "}" +
                                "}";
                        System.out.println("URL : "+link+"/Encounter/"+tbObat.getValueAt(i,15).toString());
                        System.out.println("Request JSON : "+json);
                        requestEntity = new HttpEntity(json,headers);
                        json=api.getRest().exchange(link+"/Encounter/"+tbObat.getValueAt(i,15).toString(), HttpMethod.PUT, requestEntity, String.class).getBody();
                        System.out.println("Result JSON : "+json);
                        tbObat.setValueAt(false,i,0);
                    }catch(Exception e){
                        if(e instanceof org.springframework.web.client.HttpStatusCodeException){
                            System.out.println("Notifikasi Bridging : "+e+" => "+((org.springframework.web.client.HttpStatusCodeException)e).getResponseBodyAsString());
                        }else{
                            System.out.println("Notifikasi Bridging : "+e);
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Notifikasi : "+e);
                }
            }
        }
    }//GEN-LAST:event_BtnUpdateActionPerformed

    /** Konversi ISO-8601 dengan offset WIB (+07:00) ke UTC (+00:00).
     *  SatuSehat docs minta UTC. Input format: "2026-05-24T14:30:00+07:00".
     *  Output: "2026-05-24T07:30:00+00:00". Kalau gagal parse, return apa adanya. */
    private static String toUtc(String wibIso) {
        if (wibIso == null || wibIso.isEmpty()) return wibIso;
        try {
            java.time.OffsetDateTime odt = java.time.OffsetDateTime.parse(wibIso);
            return odt.withOffsetSameInstant(java.time.ZoneOffset.UTC)
                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssxxx"));
        } catch (Exception e) {
            return wibIso;
        }
    }

    private String getDiagnosisEncounter(String noRawat) {
        StringBuilder diag = new StringBuilder();
        try (PreparedStatement ps2 = koneksi.prepareStatement(
                "select satu_sehat_condition.id_condition,diagnosa_pasien.prioritas "+
                "from satu_sehat_condition "+
                "inner join diagnosa_pasien on diagnosa_pasien.no_rawat=satu_sehat_condition.no_rawat "+
                "and diagnosa_pasien.kd_penyakit=satu_sehat_condition.kd_penyakit "+
                "and diagnosa_pasien.status=satu_sehat_condition.status "+
                "where satu_sehat_condition.no_rawat=? and ifnull(satu_sehat_condition.id_condition,'')!='' "+
                "order by diagnosa_pasien.prioritas")) {
            ps2.setString(1,noRawat);
            try (ResultSet rs2 = ps2.executeQuery()) {
                int rank = 0;
                while (rs2.next()) {
                    rank++;
                    if(diag.length()>0){
                        diag.append(",");
                    }
                    diag.append("{" +
                        "\"condition\": {\"reference\": \"Condition/"+rs2.getString("id_condition")+"\"}," +
                        "\"use\": {\"coding\": [{" +
                            "\"system\": \"http://terminology.hl7.org/CodeSystem/diagnosis-role\"," +
                            "\"code\": \"DD\"," +
                            "\"display\": \"Discharge diagnosis\"" +
                        "}]}," +
                        "\"rank\": "+rank +
                    "}");
                }
            }
        } catch (Exception e) {
            System.out.println("Notif Diagnosis : "+e);
        }
        return diag.toString();
    }

    private void BtnAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnAllActionPerformed
        TCari.setText("");
        runBackground(() ->tampil());
    }//GEN-LAST:event_BtnAllActionPerformed

    private void BtnAllKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnAllKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            TCari.setText("");
            runBackground(() ->tampil());
        }else{
            Valid.pindah(evt, BtnPrint, BtnKeluar);
        }
    }//GEN-LAST:event_BtnAllKeyPressed

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
            SatuSehatKirimEncounter dialog = new SatuSehatKirimEncounter(new javax.swing.JFrame(), true);
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
    private widget.Button BtnConsent;
    private widget.Button BtnCreateIHS;
    private widget.Button BtnKeluar;
    private widget.Button BtnKirim;
    private widget.Button BtnPrint;
    private widget.Button BtnUpdate;
    private widget.Tanggal DTPCari1;
    private widget.Tanggal DTPCari2;
    private widget.Label LCount;
    private widget.editorpane LoadHTML;
    private widget.ScrollPane Scroll;
    private widget.TextBox TCari;
    private widget.ComboBox cmbStatus;
    private widget.InternalFrame internalFrame1;
    private widget.Label jLabel15;
    private widget.Label jLabel16;
    private widget.Label jLabel17;
    private widget.Label jLabel18;
    private widget.Label jLabel7;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPopupMenu jPopupMenu1;
    private widget.panelisi panelGlass8;
    private widget.panelisi panelGlass9;
    private javax.swing.JMenuItem ppBersihkan;
    private javax.swing.JMenuItem ppPilihSemua;
    private widget.Table tbObat;
    // End of variables declaration//GEN-END:variables
    private void tampil() {
        Valid.tabelKosong(tabMode);
        try{
            ps=koneksi.prepareStatement(
                   "select reg_periksa.tgl_registrasi,reg_periksa.jam_reg,reg_periksa.no_rawat,pasien.nm_pasien,pasien.no_ktp,reg_periksa.no_rkm_medis,reg_periksa.kd_poli,reg_periksa.stts,"+
                   "pegawai.nama,pegawai.no_ktp as ktpdokter,poliklinik.nm_poli,satu_sehat_mapping_lokasi_ralan.id_lokasi_satusehat,reg_periksa.kd_dokter,"+
                   "reg_periksa.status_lanjut,concat(reg_periksa.tgl_registrasi,'T',reg_periksa.jam_reg,'+07:00') as pulang,ifnull(satu_sehat_encounter.id_encounter,'') as id_encounter "+
                   "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis inner join pegawai on pegawai.nik=reg_periksa.kd_dokter "+
                   "inner join poliklinik on reg_periksa.kd_poli=poliklinik.kd_poli inner join satu_sehat_mapping_lokasi_ralan on satu_sehat_mapping_lokasi_ralan.kd_poli=poliklinik.kd_poli "+
                   "left join satu_sehat_encounter on satu_sehat_encounter.no_rawat=reg_periksa.no_rawat "+
                   "where reg_periksa.status_bayar='Sudah Bayar' and reg_periksa.tgl_registrasi between ? and ? "+
                   (cmbStatus.getSelectedItem().toString().equals("Belum dikirim")?"and (satu_sehat_encounter.id_encounter is null or satu_sehat_encounter.id_encounter='') ":"")+
                   (cmbStatus.getSelectedItem().toString().equals("Sudah dikirim")?"and satu_sehat_encounter.id_encounter is not null and satu_sehat_encounter.id_encounter!='' ":"")+
                   (TCari.getText().equals("")?"":"and (reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or "+
                   "pasien.nm_pasien like ? or pasien.no_ktp like ? or pegawai.nama like ? or poliklinik.nm_poli like ? or "+
                   "reg_periksa.stts like ? or reg_periksa.status_lanjut like ?)"));
            try {
                ps.setString(1,Valid.SetTgl(DTPCari1.getSelectedItem()+""));
                ps.setString(2,Valid.SetTgl(DTPCari2.getSelectedItem()+""));
                if(!TCari.getText().equals("")){
                    ps.setString(3,"%"+TCari.getText()+"%");
                    ps.setString(4,"%"+TCari.getText()+"%");
                    ps.setString(5,"%"+TCari.getText()+"%");
                    ps.setString(6,"%"+TCari.getText()+"%");
                    ps.setString(7,"%"+TCari.getText()+"%");
                    ps.setString(8,"%"+TCari.getText()+"%");
                    ps.setString(9,"%"+TCari.getText()+"%");
                    ps.setString(10,"%"+TCari.getText()+"%");
                }
                rs=ps.executeQuery();
                while(rs.next()){
                    tabMode.addRow(new Object[]{
                        false,rs.getString("tgl_registrasi")+"T"+rs.getString("jam_reg")+"+07:00",rs.getString("no_rawat"),rs.getString("no_rkm_medis"),rs.getString("nm_pasien"),
                        rs.getString("no_ktp"),rs.getString("kd_dokter"),rs.getString("nama"),rs.getString("ktpdokter"),rs.getString("kd_poli"),rs.getString("nm_poli"),
                        rs.getString("id_lokasi_satusehat"),rs.getString("stts"),rs.getString("status_lanjut"),rs.getString("pulang"),rs.getString("id_encounter")
                    });
                }
            } catch (Exception e) {
                System.out.println("Notif : "+e);
            } finally{
                if(rs!=null){
                    rs.close();
                }
                if(ps!=null){
                    ps.close();
                }
            }
        }catch(Exception e){
            System.out.println("Notifikasi : "+e);
        }
        LCount.setText(""+tabMode.getRowCount());
    }

    private String getEpisodeOfCareType(String nmPoli) {
        String poli = nmPoli.toLowerCase();
        if(poli.contains("anc"))         return "ANC|Antenatal Care";
        if(poli.contains("pnc"))         return "PNC|Postnatal Care";
        if(poli.contains("tb") || poli.contains("tuberkulosis") || poli.contains("tbc") || poli.contains("paru"))
                                         return "TB-DOTS|TB-DOTS Program";
        if(poli.contains("hiv") || poli.contains("aids") || poli.contains("vct") || poli.contains("cst"))
                                         return "HIV-AIDS|HIV-AIDS Program";
        if(poli.contains("hemodial") || poli.contains("dialisis"))
                                         return "HD|Hemodialysis Program";
        if(poli.contains("onkologi") || poli.contains("kemoterapi") || poli.contains("kanker"))
                                         return "ONCO|Oncology Program";
        if(poli.contains("rehab") || poli.contains("fisioterapi"))
                                         return "REHAB|Rehabilitation Program";
        if(poli.contains("jiwa") || poli.contains("psikiatri") || poli.contains("mental"))
                                         return "MENTAL|Mental Health Program";
        if(poli.contains("prolanis") || poli.contains("diabetes") || poli.contains("kronis"))
                                         return "CHRONIC|Chronic Disease Management";
        return null;
    }

    public void isCek(){
        BtnKirim.setEnabled(akses.getsatu_sehat_kirim_encounter());
        BtnUpdate.setEnabled(akses.getsatu_sehat_kirim_encounter());
        BtnPrint.setEnabled(akses.getsatu_sehat_kirim_encounter());
    }
    
    public JTable getTable(){
        return tbObat;
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
