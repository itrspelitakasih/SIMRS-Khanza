package komunikasi;

import fungsi.akses;
import fungsi.koneksiDB;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.swing.JOptionPane;
import javax.swing.JScrollBar;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import widget.TextArea;
import widget.editorpane;

/**
 * Chat antar user Khanza lewat database. Tab "Chat Semua" = chat global (penerima kosong),
 * tab "Pesan Pribadi" = inbox percakapan + daftar user untuk memulai chat pribadi.
 * Pesan baru diambil tiap 3 detik di background (SwingWorker).
 * Riwayat dihapus manual: chat pribadi oleh salah satu pihak, Chat Semua hanya oleh Admin Utama.
 */
public class DlgChat extends javax.swing.JDialog {
    private static final int MAKS_RIWAYAT=200;
    private final Connection koneksi=koneksiDB.condb();
    private final DefaultTableModel tabUser,tabInbox;
    private final TableRowSorter<DefaultTableModel> sorterUser;
    private final Map<String,String> namaUser=new HashMap<>();
    private static final int UKURAN_AVATAR=30,UKURAN_AVATAR_JUDUL=36,UKURAN_AVATAR_PESAN=26;
    /** Pesan percakapan aktif: {waktu, pengirim, pesan}; HTML dibangun ulang dari sini saat foto avatar siap. */
    private final List<Object[]> pesanTampil=new ArrayList<>();
    private final Runnable pendengarAvatar=() -> {
        this.tbUser.repaint();
        this.tbInbox.repaint();
        tampilkanJudul();
        if(!this.pesanTampil.isEmpty()){
            tampilkan();
        }
    };
    private final SimpleDateFormat fmtWaktu=new SimpleDateFormat("dd-MM-yyyy HH:mm"),
            fmtJam=new SimpleDateFormat("HH:mm"),fmtTgl=new SimpleDateFormat("dd-MM"),fmtHari=new SimpleDateFormat("yyyyMMdd");
    private final String kodeSaya=akses.getkode();
    private final Timer timer;
    private Set<String> online=new HashSet<>();
    /** "" = Chat Semua, null = belum ada percakapan pribadi dipilih, selain itu kode lawan chat pribadi. */
    private String lawan="",lawanPribadi=null,sigInbox="";
    private long idTerakhir=0;
    private int versi=0;
    private boolean sibuk=false,ulangi=false;

    /** Creates new form DlgChat
     * @param parent
     * @param modal */
    public DlgChat(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        this.setSize(1000,640);

        tabUser=new DefaultTableModel(null,new Object[]{"Kode","Nama User","Baru"}){
            @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        siapkanTabel(tbUser,tabUser,new int[]{0,255,40});
        tbUser.setDefaultRenderer(Object.class,new RendererChat(tabUser,2));
        sorterUser=new TableRowSorter<>(tabUser);
        for(int i=0;i<tabUser.getColumnCount();i++){
            sorterUser.setSortable(i,false);
        }
        tbUser.setRowSorter(sorterUser);

        tabInbox=new DefaultTableModel(null,new Object[]{"Kode","Nama","Pesan Terakhir","Jam","Baru"}){
            @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        siapkanTabel(tbInbox,tabInbox,new int[]{0,140,95,45,35});
        tbInbox.setDefaultRenderer(Object.class,new RendererChat(tabInbox,4));
        tbUser.setRowHeight(UKURAN_AVATAR+6);
        tbInbox.setRowHeight(UKURAN_AVATAR+6);
        // foto selesai diunduh -> gambar ulang avatar di daftar, judul & gelembung chat
        AvatarChat.tambahPendengar(pendengarAvatar);

        tbUser.getSelectionModel().addListSelectionListener(e -> {
            if(!e.getValueIsAdjusting()&&tbUser.getSelectedRow()>-1){
                bukaJikaBeda(tabUser.getValueAt(tbUser.convertRowIndexToModel(tbUser.getSelectedRow()),0).toString());
            }
        });
        tbInbox.getSelectionModel().addListSelectionListener(e -> {
            if(!e.getValueIsAdjusting()&&tbInbox.getSelectedRow()>-1){
                bukaJikaBeda(tabInbox.getValueAt(tbInbox.getSelectedRow(),0).toString());
            }
        });
        TCariUser.getDocument().addDocumentListener(new DocumentListener(){
            @Override public void insertUpdate(DocumentEvent e){saringUser();}
            @Override public void removeUpdate(DocumentEvent e){saringUser();}
            @Override public void changedUpdate(DocumentEvent e){saringUser();}
        });

        ChatNotifier.siapkanTabel(koneksi);
        isiUser();
        bukaPercakapan("");
        timer=new Timer(3000,e -> perbarui());
        timer.start();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        internalFrame1 = new widget.InternalFrame();
        TabChat = new widget.TabPane();
        panelGlobal = new widget.panelisi();
        ScrollGlobal = new widget.ScrollPane();
        TampilGlobal = new widget.editorpane();
        panelKirimGlobal = new widget.panelisi();
        ScrollPesanGlobal = new widget.ScrollPane();
        TPesanGlobal = new widget.TextArea();
        BtnKirimGlobal = new widget.Button();
        panelPribadi = new widget.panelisi();
        panelKiri = new widget.panelisi();
        TabDaftar = new widget.TabPane();
        panelInbox = new widget.panelisi();
        panelInboxAtas = new widget.panelisi();
        BtnPesanBaru = new widget.Button();
        ScrollInbox = new widget.ScrollPane();
        tbInbox = new widget.Table();
        panelDaftarUser = new widget.panelisi();
        panelCariUser = new widget.panelisi();
        label1 = new widget.Label();
        TCariUser = new widget.TextBox();
        ScrollUser = new widget.ScrollPane();
        tbUser = new widget.Table();
        panelKanan = new widget.panelisi();
        LblLawan = new widget.Label();
        ScrollPribadi = new widget.ScrollPane();
        TampilPribadi = new widget.editorpane();
        panelKirimPribadi = new widget.panelisi();
        ScrollPesanPribadi = new widget.ScrollPane();
        TPesanPribadi = new widget.TextArea();
        BtnKirimPribadi = new widget.Button();
        panelBawah = new widget.panelisi();
        BtnMuatUlang = new widget.Button();
        BtnHapus = new widget.Button();
        BtnKeluar = new widget.Button();
        LblStatus = new widget.Label();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
            public void windowActivated(java.awt.event.WindowEvent evt) {
                formWindowActivated(evt);
            }
        });

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Chat Antar User ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 0, 12), new java.awt.Color(50, 50, 50))); // NOI18N
        internalFrame1.setName("internalFrame1"); // NOI18N
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

        TabChat.setBackground(new java.awt.Color(255, 255, 253));
        TabChat.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(241, 246, 236)));
        TabChat.setForeground(new java.awt.Color(50, 50, 50));
        TabChat.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        TabChat.setName("TabChat"); // NOI18N
        TabChat.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                TabChatStateChanged(evt);
            }
        });

        panelGlobal.setName("panelGlobal"); // NOI18N
        panelGlobal.setLayout(new java.awt.BorderLayout(1, 1));

        ScrollGlobal.setName("ScrollGlobal"); // NOI18N
        ScrollGlobal.setOpaque(true);

        TampilGlobal.setEditable(false);
        TampilGlobal.setContentType("text/html"); // NOI18N
        TampilGlobal.setName("TampilGlobal"); // NOI18N
        ScrollGlobal.setViewportView(TampilGlobal);

        panelGlobal.add(ScrollGlobal, java.awt.BorderLayout.CENTER);

        panelKirimGlobal.setName("panelKirimGlobal"); // NOI18N
        panelKirimGlobal.setPreferredSize(new java.awt.Dimension(100, 70));
        panelKirimGlobal.setLayout(new java.awt.BorderLayout(5, 1));

        ScrollPesanGlobal.setName("ScrollPesanGlobal"); // NOI18N
        ScrollPesanGlobal.setOpaque(true);

        TPesanGlobal.setToolTipText("Enter = kirim, Shift+Enter = baris baru");
        TPesanGlobal.setName("TPesanGlobal"); // NOI18N
        TPesanGlobal.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TPesanGlobalKeyPressed(evt);
            }
        });
        ScrollPesanGlobal.setViewportView(TPesanGlobal);

        panelKirimGlobal.add(ScrollPesanGlobal, java.awt.BorderLayout.CENTER);

        BtnKirimGlobal.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/email.png"))); // NOI18N
        BtnKirimGlobal.setMnemonic('I');
        BtnKirimGlobal.setText("Kirim");
        BtnKirimGlobal.setToolTipText("Alt+I, Enter");
        BtnKirimGlobal.setGlassColor(new java.awt.Color(255, 255, 255));
        BtnKirimGlobal.setName("BtnKirimGlobal"); // NOI18N
        BtnKirimGlobal.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnKirimGlobal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnKirimGlobalActionPerformed(evt);
            }
        });
        panelKirimGlobal.add(BtnKirimGlobal, java.awt.BorderLayout.EAST);

        panelGlobal.add(panelKirimGlobal, java.awt.BorderLayout.PAGE_END);

        TabChat.addTab("Chat Semua", panelGlobal);

        panelPribadi.setName("panelPribadi"); // NOI18N
        panelPribadi.setLayout(new java.awt.BorderLayout(1, 1));

        panelKiri.setName("panelKiri"); // NOI18N
        panelKiri.setPreferredSize(new java.awt.Dimension(330, 100));
        panelKiri.setLayout(new java.awt.BorderLayout(1, 1));

        TabDaftar.setBackground(new java.awt.Color(255, 255, 253));
        TabDaftar.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(241, 246, 236)));
        TabDaftar.setForeground(new java.awt.Color(50, 50, 50));
        TabDaftar.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        TabDaftar.setName("TabDaftar"); // NOI18N

        panelInbox.setName("panelInbox"); // NOI18N
        panelInbox.setLayout(new java.awt.BorderLayout(1, 1));

        panelInboxAtas.setName("panelInboxAtas"); // NOI18N
        panelInboxAtas.setPreferredSize(new java.awt.Dimension(100, 44));
        panelInboxAtas.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 7));

        BtnPesanBaru.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/file-edit-16x16.png"))); // NOI18N
        BtnPesanBaru.setMnemonic('P');
        BtnPesanBaru.setText("Pesan Baru");
        BtnPesanBaru.setToolTipText("Alt+P");
        BtnPesanBaru.setName("BtnPesanBaru"); // NOI18N
        BtnPesanBaru.setPreferredSize(new java.awt.Dimension(120, 30));
        BtnPesanBaru.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnPesanBaruActionPerformed(evt);
            }
        });
        panelInboxAtas.add(BtnPesanBaru);

        panelInbox.add(panelInboxAtas, java.awt.BorderLayout.PAGE_START);

        ScrollInbox.setName("ScrollInbox"); // NOI18N
        ScrollInbox.setOpaque(true);

        tbInbox.setToolTipText("Klik percakapan untuk membuka");
        tbInbox.setName("tbInbox"); // NOI18N
        ScrollInbox.setViewportView(tbInbox);

        panelInbox.add(ScrollInbox, java.awt.BorderLayout.CENTER);

        TabDaftar.addTab("Percakapan", panelInbox);

        panelDaftarUser.setName("panelDaftarUser"); // NOI18N
        panelDaftarUser.setLayout(new java.awt.BorderLayout(1, 1));

        panelCariUser.setName("panelCariUser"); // NOI18N
        panelCariUser.setPreferredSize(new java.awt.Dimension(100, 40));
        panelCariUser.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 8));

        label1.setText("Cari :");
        label1.setName("label1"); // NOI18N
        label1.setPreferredSize(new java.awt.Dimension(40, 23));
        panelCariUser.add(label1);

        TCariUser.setToolTipText("Ketik nama/kode user untuk menyaring daftar");
        TCariUser.setName("TCariUser"); // NOI18N
        TCariUser.setPreferredSize(new java.awt.Dimension(250, 23));
        panelCariUser.add(TCariUser);

        panelDaftarUser.add(panelCariUser, java.awt.BorderLayout.PAGE_START);

        ScrollUser.setName("ScrollUser"); // NOI18N
        ScrollUser.setOpaque(true);

        tbUser.setToolTipText("Klik nama user untuk memulai chat pribadi");
        tbUser.setName("tbUser"); // NOI18N
        ScrollUser.setViewportView(tbUser);

        panelDaftarUser.add(ScrollUser, java.awt.BorderLayout.CENTER);

        TabDaftar.addTab("Daftar User", panelDaftarUser);

        panelKiri.add(TabDaftar, java.awt.BorderLayout.CENTER);

        panelPribadi.add(panelKiri, java.awt.BorderLayout.WEST);

        panelKanan.setName("panelKanan"); // NOI18N
        panelKanan.setLayout(new java.awt.BorderLayout(1, 1));

        LblLawan.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 10, 0, 0));
        LblLawan.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LblLawan.setText("Pesan Pribadi");
        LblLawan.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        LblLawan.setIconTextGap(8);
        LblLawan.setName("LblLawan"); // NOI18N
        LblLawan.setPreferredSize(new java.awt.Dimension(100, 44));
        panelKanan.add(LblLawan, java.awt.BorderLayout.PAGE_START);

        ScrollPribadi.setName("ScrollPribadi"); // NOI18N
        ScrollPribadi.setOpaque(true);

        TampilPribadi.setEditable(false);
        TampilPribadi.setContentType("text/html"); // NOI18N
        TampilPribadi.setName("TampilPribadi"); // NOI18N
        ScrollPribadi.setViewportView(TampilPribadi);

        panelKanan.add(ScrollPribadi, java.awt.BorderLayout.CENTER);

        panelKirimPribadi.setName("panelKirimPribadi"); // NOI18N
        panelKirimPribadi.setPreferredSize(new java.awt.Dimension(100, 70));
        panelKirimPribadi.setLayout(new java.awt.BorderLayout(5, 1));

        ScrollPesanPribadi.setName("ScrollPesanPribadi"); // NOI18N
        ScrollPesanPribadi.setOpaque(true);

        TPesanPribadi.setToolTipText("Enter = kirim, Shift+Enter = baris baru");
        TPesanPribadi.setName("TPesanPribadi"); // NOI18N
        TPesanPribadi.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TPesanPribadiKeyPressed(evt);
            }
        });
        ScrollPesanPribadi.setViewportView(TPesanPribadi);

        panelKirimPribadi.add(ScrollPesanPribadi, java.awt.BorderLayout.CENTER);

        BtnKirimPribadi.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/email.png"))); // NOI18N
        BtnKirimPribadi.setMnemonic('I');
        BtnKirimPribadi.setText("Kirim");
        BtnKirimPribadi.setToolTipText("Alt+I, Enter");
        BtnKirimPribadi.setName("BtnKirimPribadi"); // NOI18N
        BtnKirimPribadi.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnKirimPribadi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnKirimPribadiActionPerformed(evt);
            }
        });
        panelKirimPribadi.add(BtnKirimPribadi, java.awt.BorderLayout.EAST);

        panelKanan.add(panelKirimPribadi, java.awt.BorderLayout.PAGE_END);

        panelPribadi.add(panelKanan, java.awt.BorderLayout.CENTER);

        TabChat.addTab("Pesan Pribadi", panelPribadi);

        internalFrame1.add(TabChat, java.awt.BorderLayout.CENTER);

        panelBawah.setName("panelBawah"); // NOI18N
        panelBawah.setPreferredSize(new java.awt.Dimension(100, 56));
        panelBawah.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        BtnMuatUlang.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Search-16x16.png"))); // NOI18N
        BtnMuatUlang.setMnemonic('M');
        BtnMuatUlang.setText("Muat Ulang");
        BtnMuatUlang.setToolTipText("Alt+M");
        BtnMuatUlang.setName("BtnMuatUlang"); // NOI18N
        BtnMuatUlang.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnMuatUlang.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnMuatUlangActionPerformed(evt);
            }
        });
        panelBawah.add(BtnMuatUlang);

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
        panelBawah.add(BtnHapus);

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
        panelBawah.add(BtnKeluar);

        LblStatus.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LblStatus.setText("-");
        LblStatus.setName("LblStatus"); // NOI18N
        LblStatus.setPreferredSize(new java.awt.Dimension(420, 23));
        panelBawah.add(LblStatus);

        internalFrame1.add(panelBawah, java.awt.BorderLayout.PAGE_END);

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void TabChatStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_TabChatStateChanged
        String target=TabChat.getSelectedIndex()==0?"":lawanPribadi;
        if(!Objects.equals(target,lawan)){
            bukaPercakapan(target);
        }
    }//GEN-LAST:event_TabChatStateChanged

    private void BtnKirimGlobalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnKirimGlobalActionPerformed
        kirim(TPesanGlobal,"");
    }//GEN-LAST:event_BtnKirimGlobalActionPerformed

    private void TPesanGlobalKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TPesanGlobalKeyPressed
        tombolPesan(evt,TPesanGlobal,"");
    }//GEN-LAST:event_TPesanGlobalKeyPressed

    private void BtnKirimPribadiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnKirimPribadiActionPerformed
        kirim(TPesanPribadi,lawan);
    }//GEN-LAST:event_BtnKirimPribadiActionPerformed

    private void TPesanPribadiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TPesanPribadiKeyPressed
        tombolPesan(evt,TPesanPribadi,lawan);
    }//GEN-LAST:event_TPesanPribadiKeyPressed

    private void BtnPesanBaruActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnPesanBaruActionPerformed
        TabDaftar.setSelectedIndex(1);
        TCariUser.requestFocus();
    }//GEN-LAST:event_BtnPesanBaruActionPerformed

    private void BtnMuatUlangActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnMuatUlangActionPerformed
        isiUser();
        sigInbox="";
        bukaPercakapan(lawan);
    }//GEN-LAST:event_BtnMuatUlangActionPerformed

    private void BtnHapusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnHapusActionPerformed
        hapusRiwayat();
    }//GEN-LAST:event_BtnHapusActionPerformed

    private void BtnKeluarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnKeluarActionPerformed
        dispose();
    }//GEN-LAST:event_BtnKeluarActionPerformed

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        timer.stop();
        AvatarChat.hapusPendengar(pendengarAvatar);
    }//GEN-LAST:event_formWindowClosed

    private void formWindowActivated(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowActivated
        // pesan baru yang masuk saat jendela tidak aktif baru ditandai dibaca di sini
        perbarui();
        fokusPesan();
    }//GEN-LAST:event_formWindowActivated

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private widget.Button BtnHapus;
    private widget.Button BtnKeluar;
    private widget.Button BtnKirimGlobal;
    private widget.Button BtnKirimPribadi;
    private widget.Button BtnMuatUlang;
    private widget.Button BtnPesanBaru;
    private widget.Label LblLawan;
    private widget.Label LblStatus;
    private widget.ScrollPane ScrollGlobal;
    private widget.ScrollPane ScrollInbox;
    private widget.ScrollPane ScrollPesanGlobal;
    private widget.ScrollPane ScrollPesanPribadi;
    private widget.ScrollPane ScrollPribadi;
    private widget.ScrollPane ScrollUser;
    private widget.TextBox TCariUser;
    private widget.TextArea TPesanGlobal;
    private widget.TextArea TPesanPribadi;
    private widget.TabPane TabChat;
    private widget.TabPane TabDaftar;
    private widget.editorpane TampilGlobal;
    private widget.editorpane TampilPribadi;
    private widget.InternalFrame internalFrame1;
    private widget.Label label1;
    private widget.panelisi panelBawah;
    private widget.panelisi panelCariUser;
    private widget.panelisi panelDaftarUser;
    private widget.panelisi panelGlobal;
    private widget.panelisi panelInbox;
    private widget.panelisi panelInboxAtas;
    private widget.panelisi panelKanan;
    private widget.panelisi panelKiri;
    private widget.panelisi panelKirimGlobal;
    private widget.panelisi panelKirimPribadi;
    private widget.panelisi panelPribadi;
    private widget.Table tbInbox;
    private widget.Table tbUser;
    // End of variables declaration//GEN-END:variables

    /** Buka tab Pesan Pribadi langsung di percakapan dengan user tsb (dipakai notifikasi di frmUtama). */
    public void bukaPribadi(String kode){
        TabDaftar.setSelectedIndex(0);
        bukaPercakapan(kode);
    }

    public void bukaGlobal(){
        bukaPercakapan("");
    }

    private void siapkanTabel(JTable tabel,DefaultTableModel model,int[] lebar){
        tabel.setModel(model);
        tabel.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for(int i=0;i<lebar.length;i++){
            TableColumn kolom=tabel.getColumnModel().getColumn(i);
            kolom.setPreferredWidth(lebar[i]);
            if(lebar[i]==0){
                kolom.setMinWidth(0);
                kolom.setMaxWidth(0);
            }
        }
    }

    private String nama(String kode){
        return namaUser.getOrDefault(kode,kode);
    }

    private void bacaNama(String sql){
        try (PreparedStatement ps=koneksi.prepareStatement(sql);ResultSet rs=ps.executeQuery()){
            while(rs.next()){
                if(rs.getString(1)!=null&&rs.getString(2)!=null){
                    namaUser.put(rs.getString(1),rs.getString(2));
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi Chat : "+e);
        }
    }

    /** Daftar semua user Khanza (kecuali diri sendiri) urut nama, untuk memulai chat pribadi. */
    private void isiUser(){
        namaUser.clear();
        bacaNama("select nik,nama from pegawai");
        bacaNama("select nip,nama from petugas");
        bacaNama("select kd_dokter,nm_dokter from dokter");
        namaUser.put("Admin Utama","Admin Utama");

        List<String[]> daftar=new ArrayList<>();
        if(!kodeSaya.equals("Admin Utama")){
            daftar.add(new String[]{"Admin Utama","Admin Utama"});
        }
        try (PreparedStatement ps=koneksi.prepareStatement("select AES_DECRYPT(user.id_user,'nur') from user");ResultSet rs=ps.executeQuery()){
            while(rs.next()){
                String kode=rs.getString(1);
                if(kode!=null&&!kode.equals(kodeSaya)){
                    daftar.add(new String[]{kode,nama(kode)});
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi Chat : "+e);
        }
        daftar.sort((a,b) -> a[1].compareToIgnoreCase(b[1]));

        tabUser.setRowCount(0);
        for(String[] a:daftar){
            tabUser.addRow(new Object[]{a[0],a[1],""});
        }
        pilihBarisLawan();
    }

    private void saringUser(){
        final String cari=TCariUser.getText().trim().toLowerCase();
        sorterUser.setRowFilter(cari.equals("")?null:new RowFilter<DefaultTableModel,Integer>(){
            @Override public boolean include(Entry<? extends DefaultTableModel,? extends Integer> e){
                return e.getStringValue(0).toLowerCase().contains(cari)||e.getStringValue(1).toLowerCase().contains(cari);
            }
        });
        pilihBarisLawan();
    }

    private void pilihBarisLawan(){
        pilihBaris(tbUser,tabUser);
        pilihBaris(tbInbox,tabInbox);
    }

    private void pilihBaris(JTable tabel,DefaultTableModel model){
        if(lawan!=null&&!lawan.equals("")){
            for(int i=0;i<model.getRowCount();i++){
                if(model.getValueAt(i,0).toString().equals(lawan)){
                    int view=tabel.convertRowIndexToView(i);
                    if(view>-1){
                        tabel.setRowSelectionInterval(view,view);
                        tabel.scrollRectToVisible(tabel.getCellRect(view,0,true));
                        return;
                    }
                }
            }
        }
        tabel.clearSelection();
    }

    private void bukaJikaBeda(String kode){
        if(!kode.equals(lawan)){
            bukaPercakapan(kode);
        }
    }

    private boolean modeGlobal(){
        return "".equals(lawan);
    }

    private editorpane tampilanAktif(){
        return modeGlobal()?TampilGlobal:TampilPribadi;
    }

    private void fokusPesan(){
        if(modeGlobal()){
            TPesanGlobal.requestFocus();
        }else if(lawan!=null){
            TPesanPribadi.requestFocus();
        }
    }

    private void bukaPercakapan(String kode){
        lawan=kode;
        if(kode!=null&&!kode.equals("")){
            lawanPribadi=kode;
        }
        versi++;
        idTerakhir=0;
        pesanTampil.clear();
        int tab=modeGlobal()?0:1;
        if(TabChat.getSelectedIndex()!=tab){
            TabChat.setSelectedIndex(tab);
        }
        boolean adaLawan=lawan!=null&&!modeGlobal();
        tampilkanJudul();
        TPesanPribadi.setEnabled(adaLawan);
        BtnKirimPribadi.setEnabled(adaLawan);
        BtnHapus.setEnabled(adaLawan||(modeGlobal()&&kodeSaya.equals("Admin Utama")));
        tampilkan();
        pilihBarisLawan();
        perbarui();
        fokusPesan();
    }

    private void tombolPesan(KeyEvent evt,TextArea area,String tujuan){
        if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            evt.consume();
            if(evt.isShiftDown()){
                area.replaceSelection("\n");
            }else{
                kirim(area,tujuan);
            }
        }
    }

    private void kirim(TextArea area,String tujuan){
        String pesan=area.getText().trim();
        if(tujuan==null){
            JOptionPane.showMessageDialog(null,"Pilih percakapan atau user tujuan terlebih dahulu.");
            return;
        }
        if(pesan.equals("")){
            area.requestFocus();
            return;
        }
        if(pesan.length()>2000){
            JOptionPane.showMessageDialog(null,"Pesan terlalu panjang, maksimal 2000 karakter.");
            return;
        }
        try (PreparedStatement ps=koneksi.prepareStatement(
                "insert into chat_pesan(waktu,pengirim,penerima,pesan,dibaca) values(now(),?,?,?,'0')")){
            ps.setString(1,kodeSaya);
            ps.setString(2,tujuan);
            ps.setString(3,pesan);
            ps.executeUpdate();
            area.setText("");
            perbarui();
        } catch (Exception e) {
            System.out.println("Notifikasi Chat : "+e);
            JOptionPane.showMessageDialog(null,"Gagal mengirim pesan : "+e.getMessage());
        }
        area.requestFocus();
    }

    private void hapusRiwayat(){
        if(lawan==null){
            return;
        }
        String sql;
        String[] param;
        if(modeGlobal()){
            if(!kodeSaya.equals("Admin Utama")){
                JOptionPane.showMessageDialog(null,"Riwayat Chat Semua hanya bisa dihapus oleh Admin Utama.");
                return;
            }
            if(JOptionPane.showConfirmDialog(null,"Hapus seluruh riwayat Chat Semua ?\nRiwayat akan hilang untuk semua user.",
                    "Konfirmasi",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION){
                return;
            }
            sql="delete from chat_pesan where penerima=''";
            param=new String[]{};
        }else{
            if(JOptionPane.showConfirmDialog(null,"Hapus seluruh riwayat chat dengan "+nama(lawan)+" ?\nRiwayat akan hilang untuk kedua pihak.",
                    "Konfirmasi",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION){
                return;
            }
            sql="delete from chat_pesan where (pengirim=? and penerima=?) or (pengirim=? and penerima=?)";
            param=new String[]{kodeSaya,lawan,lawan,kodeSaya};
        }
        try (PreparedStatement ps=koneksi.prepareStatement(sql)){
            for(int i=0;i<param.length;i++){
                ps.setString(i+1,param[i]);
            }
            int jml=ps.executeUpdate();
            LblStatus.setText(jml+" pesan dihapus");
            ChatNotifier.cekSekarang();
        } catch (Exception e) {
            System.out.println("Notifikasi Chat : "+e);
            JOptionPane.showMessageDialog(null,"Gagal menghapus riwayat : "+e.getMessage());
        }
        sigInbox="";
        bukaPercakapan(lawan);
    }

    private static class Hasil {
        final List<Object[]> pesan=new ArrayList<>();
        final List<Object[]> inbox=new ArrayList<>();
        final Map<String,Integer> belumDibaca=new HashMap<>();
        final Set<String> online=new HashSet<>();
        int globalBelumDibaca=0;
        boolean adaYangDibaca=false;
    }

    /** Ambil pesan baru percakapan aktif, inbox, jumlah belum dibaca & status online di background. */
    private void perbarui(){
        if(sibuk){
            ulangi=true;
            return;
        }
        sibuk=true;
        ulangi=false;
        final int versiKerja=versi;
        final String lawanKerja=lawan;
        final long dariId=idTerakhir;
        final boolean tandaiDibaca=isActive();
        new SwingWorker<Hasil,Void>(){
            @Override protected Hasil doInBackground() throws Exception {
                Hasil h=new Hasil();
                if(lawanKerja!=null){
                    bacaPesan(h);
                    if(tandaiDibaca){
                        tandaiDibaca(h);
                    }
                }
                try (PreparedStatement ps=koneksi.prepareStatement(
                        "select pengirim,count(*) from chat_pesan where penerima=? and dibaca='0' group by pengirim")){
                    ps.setString(1,kodeSaya);
                    try (ResultSet rs=ps.executeQuery()){
                        while(rs.next()){
                            h.belumDibaca.put(rs.getString(1),rs.getInt(2));
                        }
                    }
                }
                try (PreparedStatement ps=koneksi.prepareStatement(
                        "select count(*) from chat_pesan where penerima='' and pengirim<>? and "+
                        "id>ifnull((select id_global_dibaca from chat_online where kode_user=?),0)")){
                    ps.setString(1,kodeSaya);
                    ps.setString(2,kodeSaya);
                    try (ResultSet rs=ps.executeQuery()){
                        if(rs.next()){
                            h.globalBelumDibaca=rs.getInt(1);
                        }
                    }
                }
                // pesan terakhir per lawan chat pribadi
                try (PreparedStatement ps=koneksi.prepareStatement(
                        "select c.id,if(c.pengirim=?,c.penerima,c.pengirim),c.waktu,c.pengirim,c.pesan from chat_pesan c inner join "+
                        "(select if(pengirim=?,penerima,pengirim) lawan,max(id) idmax from chat_pesan "+
                        "where penerima<>'' and (pengirim=? or penerima=?) group by 1) m on m.idmax=c.id order by c.id desc")){
                    ps.setString(1,kodeSaya);
                    ps.setString(2,kodeSaya);
                    ps.setString(3,kodeSaya);
                    ps.setString(4,kodeSaya);
                    try (ResultSet rs=ps.executeQuery()){
                        while(rs.next()){
                            h.inbox.add(new Object[]{rs.getLong(1),rs.getString(2),rs.getTimestamp(3),rs.getString(4),rs.getString(5)});
                        }
                    }
                }
                try (PreparedStatement ps=koneksi.prepareStatement(
                        "select kode_user from chat_online where terakhir_aktif>=now()-interval "+ChatNotifier.DETIK_ONLINE+" second");
                     ResultSet rs=ps.executeQuery()){
                    while(rs.next()){
                        h.online.add(rs.getString(1));
                    }
                }
                return h;
            }

            private void bacaPesan(Hasil h) throws Exception {
                PreparedStatement ps;
                if(lawanKerja.equals("")){
                    ps=koneksi.prepareStatement(
                        "select id,waktu,pengirim,pesan from chat_pesan where penerima='' and id>? order by id desc limit "+MAKS_RIWAYAT);
                    ps.setLong(1,dariId);
                }else{
                    ps=koneksi.prepareStatement(
                        "select id,waktu,pengirim,pesan from chat_pesan where ((pengirim=? and penerima=?) or (pengirim=? and penerima=?)) "+
                        "and id>? order by id desc limit "+MAKS_RIWAYAT);
                    ps.setString(1,kodeSaya);
                    ps.setString(2,lawanKerja);
                    ps.setString(3,lawanKerja);
                    ps.setString(4,kodeSaya);
                    ps.setLong(5,dariId);
                }
                try (ResultSet rs=ps.executeQuery()){
                    while(rs.next()){
                        h.pesan.add(new Object[]{rs.getLong(1),rs.getTimestamp(2),rs.getString(3),rs.getString(4)});
                    }
                } finally {
                    ps.close();
                }
                Collections.reverse(h.pesan);
            }

            private void tandaiDibaca(Hasil h) throws Exception {
                if(lawanKerja.equals("")){
                    try (PreparedStatement ps=koneksi.prepareStatement(
                            "update chat_online set id_global_dibaca=(select ifnull(max(id),0) from chat_pesan where penerima='') where kode_user=?")){
                        ps.setString(1,kodeSaya);
                        h.adaYangDibaca=ps.executeUpdate()>0;
                    }
                }else{
                    try (PreparedStatement ps=koneksi.prepareStatement(
                            "update chat_pesan set dibaca='1' where pengirim=? and penerima=? and dibaca='0'")){
                        ps.setString(1,lawanKerja);
                        ps.setString(2,kodeSaya);
                        h.adaYangDibaca=ps.executeUpdate()>0;
                    }
                }
            }

            @Override protected void done(){
                sibuk=false;
                try {
                    Hasil h=get();
                    if(h.adaYangDibaca){
                        // yang baru saja dibaca tidak dihitung lagi
                        if(lawanKerja.equals("")){
                            h.globalBelumDibaca=0;
                        }else{
                            h.belumDibaca.remove(lawanKerja);
                        }
                    }
                    if(versiKerja==versi){
                        for(Object[] p:h.pesan){
                            tambahPesan((Timestamp)p[1],(String)p[2],(String)p[3]);
                            idTerakhir=Math.max(idTerakhir,(Long)p[0]);
                        }
                        if(!h.pesan.isEmpty()){
                            tampilkan();
                        }
                    }
                    online=h.online;
                    tampilkanStatus(h);
                    if(h.adaYangDibaca){
                        ChatNotifier.cekSekarang();
                    }
                } catch (Exception e) {
                    System.out.println("Notifikasi Chat : "+e);
                    LblStatus.setText("Gagal memuat chat, mencoba lagi...");
                }
                if(ulangi||versiKerja!=versi){
                    perbarui();
                }
            }
        }.execute();
    }

    private void tampilkanStatus(Hasil h){
        int jmlOnline=0,totalPribadi=0;
        for(int i=0;i<tabUser.getRowCount();i++){
            String kode=tabUser.getValueAt(i,0).toString();
            Integer baru=h.belumDibaca.get(kode);
            boolean on=online.contains(kode);
            if(on){
                jmlOnline++;
            }
            tabUser.setValueAt(baru==null?"":baru.toString(),i,2);
        }
        tbUser.repaint();
        tampilkanJudul();
        for(Integer n:h.belumDibaca.values()){
            totalPribadi+=n;
        }

        StringBuilder sig=new StringBuilder();
        for(Object[] r:h.inbox){
            sig.append(r[0]).append(':').append(h.belumDibaca.getOrDefault((String)r[1],0)).append(';');
        }
        if(!sig.toString().equals(sigInbox)){
            sigInbox=sig.toString();
            String hariIni=fmtHari.format(new Date());
            tabInbox.setRowCount(0);
            for(Object[] r:h.inbox){
                String kode=(String)r[1];
                Timestamp waktu=(Timestamp)r[2];
                String cuplikan=((String)r[4]).replaceAll("\\s+"," ").trim();
                if(kodeSaya.equals(r[3])){
                    cuplikan="Saya: "+cuplikan;
                }
                Integer baru=h.belumDibaca.get(kode);
                tabInbox.addRow(new Object[]{kode,nama(kode),cuplikan,
                    fmtHari.format(waktu).equals(hariIni)?fmtJam.format(waktu):fmtTgl.format(waktu),
                    baru==null?"":baru.toString()});
            }
            pilihBaris(tbInbox,tabInbox);
        }else{
            tbInbox.repaint();
        }

        TabChat.setTitleAt(0,h.globalBelumDibaca>0?"Chat Semua ("+h.globalBelumDibaca+")":"Chat Semua");
        TabChat.setTitleAt(1,totalPribadi>0?"Pesan Pribadi ("+totalPribadi+")":"Pesan Pribadi");
        TabChat.setForegroundAt(1,totalPribadi>0?new Color(198,40,40):new Color(50,50,50));
        TabDaftar.setTitleAt(0,totalPribadi>0?"Percakapan ("+totalPribadi+")":"Percakapan");
        LblStatus.setText(jmlOnline+" user lain sedang online");
    }

    private static String esc(String s){
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;");
    }

    private void tambahPesan(Timestamp waktu,String pengirim,String pesan){
        pesanTampil.add(new Object[]{waktu,pengirim,pesan});
    }

    /** Gelembung pesan; pesan orang lain diberi avatar pengirim di kiri. */
    private void htmlPesan(StringBuilder sb,Timestamp waktu,String pengirim,String pesan){
        boolean saya=pengirim.equals(kodeSaya);
        sb.append("<table width='100%' cellspacing='0' cellpadding='3'><tr><td align='").append(saya?"right":"left").append("'>");
        if(!saya){
            sb.append("<table cellspacing='0' cellpadding='0'><tr><td valign='top' width='").append(UKURAN_AVATAR_PESAN+6).append("'>")
              .append("<img src='").append(AvatarChat.urlFile(pengirim,nama(pengirim),UKURAN_AVATAR_PESAN))
              .append("' width='").append(UKURAN_AVATAR_PESAN).append("' height='").append(UKURAN_AVATAR_PESAN).append("'></td><td>");
        }
        sb.append("<table cellspacing='0' cellpadding='6' bgcolor='").append(saya?"#DCF8C6":"#EEF2F7").append("'><tr><td>")
          .append("<span class='nama'>").append(esc(saya?"Saya":nama(pengirim))).append("</span> ")
          .append("<span class='jam'>").append(fmtWaktu.format(waktu)).append("</span><br>")
          .append(esc(pesan).replace("\n","<br>"))
          .append("</td></tr></table>");
        if(!saya){
            sb.append("</td></tr></table>");
        }
        sb.append("</td></tr></table>");
    }

    /** Judul percakapan pribadi: avatar lawan chat (titik hijau bila online) + nama. */
    private void tampilkanJudul(){
        if(modeGlobal()){
            return;
        }
        if(lawan==null){
            LblLawan.setIcon(null);
            LblLawan.setText("Pesan Pribadi");
        }else{
            LblLawan.setIcon(AvatarChat.ikon(lawan,nama(lawan),UKURAN_AVATAR_JUDUL,online.contains(lawan)));
            LblLawan.setText(nama(lawan)+(online.contains(lawan)?"  -  Online":""));
        }
    }

    private void tampilkan(){
        String isi;
        if(lawan==null){
            isi="<p style='color:#888888'>Pilih percakapan di sebelah kiri, atau klik 'Pesan Baru' untuk memulai chat pribadi.</p>";
        }else if(pesanTampil.isEmpty()){
            isi="<p style='color:#888888'>Belum ada pesan.</p>";
        }else{
            StringBuilder sb=new StringBuilder();
            for(Object[] p:pesanTampil){
                htmlPesan(sb,(Timestamp)p[0],(String)p[1],(String)p[2]);
            }
            isi=sb.toString();
        }
        final editorpane tampil=tampilanAktif();
        tampil.setText("<html><head><style>body{font-family:Arial;font-size:11pt;color:#323232;margin:4px}"+
                ".nama{color:#2E7D32;font-size:10pt}.jam{color:#888888;font-size:9pt}</style></head><body>"+
                isi+"</body></html>");
        final JScrollBar bar=(tampil==TampilGlobal?ScrollGlobal:ScrollPribadi).getVerticalScrollBar();
        SwingUtilities.invokeLater(() -> bar.setValue(bar.getMaximum()));
    }

    /** Kolom nama diberi avatar (titik hijau bila online), baris berpesan baru berlatar merah muda, kolom "Baru" merah. */
    private class RendererChat extends DefaultTableCellRenderer {
        private final DefaultTableModel model;
        private final int kolomBaru;

        RendererChat(DefaultTableModel model,int kolomBaru){
            this.model=model;
            this.kolomBaru=kolomBaru;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table,Object value,boolean isSelected,boolean hasFocus,int row,int column){
            super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);
            int baris=table.convertRowIndexToModel(row);
            String kode=model.getValueAt(baris,0).toString();
            boolean adaBaru=!model.getValueAt(baris,kolomBaru).toString().equals("");
            setFont(getFont().deriveFont(isSelected?java.awt.Font.BOLD:java.awt.Font.PLAIN));
            setBackground(isSelected?table.getSelectionBackground():(adaBaru?new Color(255,235,238):Color.WHITE));
            setForeground(column==kolomBaru?new Color(198,40,40):new Color(50,50,50));
            setIcon(column==1?AvatarChat.ikon(kode,model.getValueAt(baris,1).toString(),UKURAN_AVATAR,online.contains(kode)):null);
            setIconTextGap(6);
            return this;
        }
    }
}
