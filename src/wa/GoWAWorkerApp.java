/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wa;

import fungsi.koneksiDBWa;

import java.awt.BorderLayout;
import java.awt.Font;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

/**
 * UI standalone untuk worker antrian GoWA (wa_outbox), dipaketkan
 * sebagai jar terpisah (GoWAWorker.jar) supaya bisa dijalankan di luar
 * aplikasi desktop SIMRSKhanza, mengikuti pola frmUtama pada
 * KhanzaHMSServiceSatuSehat: satu jendela dengan area log dan tombol
 * Keluar.
 */
public class GoWAWorkerApp extends JFrame {

    private final JTextArea logArea = new JTextArea();
    private final JLabel statusLabel = new JLabel("Status : memulai...");
    private final JLabel jamLabel = new JLabel();
    private final SimpleDateFormat waktuFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public GoWAWorkerApp() {
        initComponents();
        redirectConsoleToLog();
        startWorker();
        startJamTicker();
    }

    private void initComponents() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("SIMKES Khanza Service GoWA");
        setSize(640, 420);
        setLocationRelativeTo(null);

        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(logArea);
        getContentPane().add(scrollPane, BorderLayout.CENTER);

        JPanel topPanel = new JPanel(new BorderLayout());
        statusLabel.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        jamLabel.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        topPanel.add(statusLabel, BorderLayout.WEST);
        topPanel.add(jamLabel, BorderLayout.EAST);
        getContentPane().add(topPanel, BorderLayout.NORTH);

        JButton prosesButton = new JButton("Proses Sekarang");
        prosesButton.addActionListener(e -> {
            System.out.println("Memicu proses antrian secara manual...");
            GoWAService.processQueueNow();
        });

        JButton bersihkanButton = new JButton("Bersihkan Log");
        bersihkanButton.addActionListener(e -> logArea.setText(""));

        JButton keluarButton = new JButton("Keluar");
        keluarButton.addActionListener(e -> System.exit(0));

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(prosesButton);
        bottomPanel.add(bersihkanButton);
        bottomPanel.add(keluarButton);
        getContentPane().add(bottomPanel, BorderLayout.SOUTH);
    }

    private void redirectConsoleToLog() {
        OutputStream logStream = new OutputStream() {
            private final StringBuilder buffer = new StringBuilder();

            @Override
            public void write(int b) {
                char c = (char) b;
                if (c == '\n') {
                    String line = "[" + waktuFormat.format(new Date()) + "] " + buffer;
                    buffer.setLength(0);
                    appendLog(line);
                } else if (c != '\r') {
                    buffer.append(c);
                }
            }
        };

        PrintStream printStream = new PrintStream(logStream, true);
        System.setOut(printStream);
        System.setErr(printStream);
    }

    private void appendLog(String line) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(line);
            logArea.append("\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    private void startWorker() {
        statusLabel.setText("Status : berjalan (device " + safeDeviceId() + ")");
        GoWAService.startQueueWorker();
    }

    private String safeDeviceId() {
        try {
            String id = koneksiDBWa.GOWA_DEVICE_ID();
            return (id == null || id.trim().isEmpty()) ? "default" : id.trim();
        } catch (Exception e) {
            return "default";
        }
    }

    private void startJamTicker() {
        Timer timer = new Timer(1000, e -> jamLabel.setText(waktuFormat.format(new Date())));
        timer.start();
    }

    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> new GoWAWorkerApp().setVisible(true));
    }
}
