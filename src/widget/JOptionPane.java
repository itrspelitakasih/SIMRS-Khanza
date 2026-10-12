package widget;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Pengganti JOptionPane dengan tampilan bergaya NotifPopup (kartu putih, garis warna, ikon bulat),
 * tetap modal dan muncul di tengah jendela pemanggil.
 *
 * Kelas ini turunan javax.swing.JOptionPane, jadi konstanta (YES_OPTION, ERROR_MESSAGE, ...) dan
 * method lain (showInputDialog, showOptionDialog) tetap ada. Yang diganti tampilannya hanya
 * showMessageDialog dan showConfirmDialog. Untuk memakainya di form cukup ganti
 *     import javax.swing.JOptionPane;
 * menjadi
 *     import widget.JOptionPane;
 *
 * Jenis pesan tanpa messageType (atau INFORMATION/PLAIN) ditebak dari isi teks:
 * "berhasil" = sukses (hijau), "gagal"/"error" = gagal (merah), "maaf"/"tidak"/"belum" = peringatan (oranye),
 * selain itu info (biru).
 *
 * Contoh tambahan: JOptionPane.sukses(this,"Tersimpan","Data pasien berhasil disimpan");
 */
public class JOptionPane extends javax.swing.JOptionPane {

    private static final long serialVersionUID = 1L;

    public static final int SUKSES = 0, INFO = 1, PERINGATAN = 2, GAGAL = 3, TANYA = 4;

    private static final int TEKS_MIN = 260, TEKS_MAX = 440, TINGGI_MAX = 320, SUDUT = 16;
    private static final Font F_JUDUL = new Font("Arial", Font.BOLD, 14);
    private static final Font F_PESAN = new Font("Arial", Font.PLAIN, 12);
    private static final Font F_TOMBOL = new Font("Arial", Font.BOLD, 11);

    // ===== pengganti JOptionPane.showMessageDialog =====

    public static void showMessageDialog(Component parent, Object message) {
        tampilkan(parent, message, null, -99, null, -1);
    }

    public static void showMessageDialog(Component parent, Object message, String title, int messageType) {
        tampilkan(parent, message, title, messageType, null, -1);
    }

    public static void showMessageDialog(Component parent, Object message, String title, int messageType, Icon icon) {
        tampilkan(parent, message, title, messageType, icon, -1);
    }

    // ===== pengganti JOptionPane.showConfirmDialog =====

    public static int showConfirmDialog(Component parent, Object message) {
        return tampilkan(parent, message, null, QUESTION_MESSAGE, null, YES_NO_CANCEL_OPTION);
    }

    public static int showConfirmDialog(Component parent, Object message, String title, int optionType) {
        return tampilkan(parent, message, title, QUESTION_MESSAGE, null, optionType);
    }

    public static int showConfirmDialog(Component parent, Object message, String title, int optionType, int messageType) {
        return tampilkan(parent, message, title, messageType, null, optionType);
    }

    public static int showConfirmDialog(Component parent, Object message, String title, int optionType, int messageType, Icon icon) {
        return tampilkan(parent, message, title, messageType, icon, optionType);
    }

    // ===== jalan pintas =====

    public static void sukses(Component parent, String judul, String pesan) {
        tampil(parent, SUKSES, judul, pesan);
    }

    public static void info(Component parent, String judul, String pesan) {
        tampil(parent, INFO, judul, pesan);
    }

    public static void peringatan(Component parent, String judul, String pesan) {
        tampil(parent, PERINGATAN, judul, pesan);
    }

    public static void gagal(Component parent, String judul, String pesan) {
        tampil(parent, GAGAL, judul, pesan);
    }

    /** Pertanyaan Ya/Tidak, true bila user memilih Ya. */
    public static boolean tanya(Component parent, String judul, String pesan) {
        return tampil(parent, TANYA, judul, pesan, null, YES_NO_OPTION) == YES_OPTION;
    }

    public static void tampil(Component parent, int jenis, String judul, String pesan) {
        tampil(parent, jenis, judul, pesan, null, -1);
    }

    private static int tampil(final Component parent, final int jenis, final String judul, final Object pesan, final Icon icon, final int optionType) {
        final int[] hasil = {CLOSED_OPTION};
        Runnable r = new Runnable() {
            @Override
            public void run() {
                Dlg d = new Dlg(cariWindow(parent), jenis, judulAtauDefault(judul, jenis), pesan, icon, optionType);
                d.mulai();
                hasil[0] = d.hasil;
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            try {
                SwingUtilities.invokeAndWait(r);
            } catch (Exception e) {
                System.out.println("Notif : " + e);
            }
        }
        return hasil[0];
    }

    private static int tampilkan(Component parent, Object message, String title, int messageType, Icon icon, int optionType) {
        int jenis;
        switch (messageType) {
            case ERROR_MESSAGE:    jenis = GAGAL; break;
            case WARNING_MESSAGE:  jenis = PERINGATAN; break;
            case QUESTION_MESSAGE: jenis = TANYA; break;
            default:               jenis = optionType >= 0 ? TANYA : tebak(teksDari(message)); break;
        }
        return tampil(parent, jenis, title, message, icon, optionType);
    }

    private static Window cariWindow(Component parent) {
        if (parent instanceof Window) {
            return (Window) parent;
        }
        if (parent != null) {
            Window w = SwingUtilities.getWindowAncestor(parent);
            if (w != null) {
                return w;
            }
        }
        return KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
    }

    private static String judulAtauDefault(String judul, int jenis) {
        if (judul != null && !judul.trim().equals("") && !judul.equals("Message") && !judul.equals("Select an Option")) {
            return judul;
        }
        switch (jenis) {
            case SUKSES:     return "Berhasil";
            case PERINGATAN: return "Peringatan";
            case GAGAL:      return "Gagal";
            case TANYA:      return "Konfirmasi";
            default:         return "Informasi";
        }
    }

    private static String teksDari(Object message) {
        if (message == null) {
            return "";
        }
        if (message instanceof Object[]) {
            StringBuilder sb = new StringBuilder();
            for (Object o : (Object[]) message) {
                sb.append(teksDari(o)).append(' ');
            }
            return sb.toString();
        }
        if (message instanceof Component) {
            return "";
        }
        return String.valueOf(message);
    }

    /** Tebak jenis pesan dari isi teksnya. */
    static int tebak(String teks) {
        String s = teks == null ? "" : teks.toLowerCase();
        if (s.contains("gagal") || s.contains("error") || s.contains("exception") || s.contains("kesalahan")) {
            return GAGAL;
        }
        if (s.contains("tidak berhasil") || s.contains("belum berhasil")) {
            return PERINGATAN;
        }
        if (s.contains("berhasil") || s.contains("sukses") || s.contains("tersimpan")) {
            return SUKSES;
        }
        if (s.contains("maaf") || s.contains("silahkan") || s.contains("silakan") || s.contains("harus")
                || s.contains("wajib") || s.contains("belum") || s.contains("tidak") || s.contains("kosong")) {
            return PERINGATAN;
        }
        return INFO;
    }

    static Color warna(int jenis) {
        switch (jenis) {
            case INFO:       return new Color(41, 128, 185);
            case PERINGATAN: return new Color(230, 145, 20);
            case GAGAL:      return new Color(205, 55, 55);
            case TANYA:      return new Color(52, 101, 164);
            default:         return new Color(39, 160, 90);
        }
    }

    // ===== dialog =====

    private static class Dlg extends JDialog {

        private static final long serialVersionUID = 1L;

        int hasil = CLOSED_OPTION;
        private final Color warna;
        private final List<Tombol> tombol = new ArrayList<Tombol>();
        private Tombol utama;
        private Timer timer;
        private float opacity = 0f;
        private Point posAkhir;

        Dlg(Window owner, int jenis, String judul, Object pesan, Icon icon, int optionType) {
            super(owner, judul, ModalityType.APPLICATION_MODAL);
            this.warna = warna(jenis);
            setUndecorated(true);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);

            Kartu kartu = new Kartu();
            kartu.setLayout(new BorderLayout(16, 0));
            kartu.setBorder(BorderFactory.createEmptyBorder(20, 26, 16, 18));

            // ikon
            JComponent ik;
            if (icon != null) {
                ik = new JLabel(icon);
            } else {
                ik = new Ikon(jenis, warna);
            }
            JPanel kiri = new JPanel(new BorderLayout());
            kiri.setOpaque(false);
            kiri.add(ik, BorderLayout.NORTH);
            kartu.add(kiri, BorderLayout.WEST);

            // judul + tombol tutup
            JPanel tengah = new JPanel();
            tengah.setOpaque(false);
            tengah.setLayout(new BoxLayout(tengah, BoxLayout.Y_AXIS));

            JPanel baris = new JPanel(new BorderLayout(10, 0));
            baris.setOpaque(false);
            JLabel lJudul = new JLabel(judul);
            lJudul.setFont(F_JUDUL);
            lJudul.setForeground(new Color(40, 40, 40));
            baris.add(lJudul, BorderLayout.CENTER);
            baris.add(new Tutup(), BorderLayout.EAST);
            baris.setAlignmentX(Component.LEFT_ALIGNMENT);
            tengah.add(baris);
            tengah.add(Box.createVerticalStrut(8));

            // isi pesan
            Component isi = buatIsi(pesan);
            if (isi instanceof JComponent) {
                ((JComponent) isi).setAlignmentX(Component.LEFT_ALIGNMENT);
            }
            tengah.add(isi);
            kartu.add(tengah, BorderLayout.CENTER);

            // tombol
            JPanel bawah = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            bawah.setOpaque(false);
            bawah.setBorder(BorderFactory.createEmptyBorder(18, 0, 0, 0));
            switch (optionType) {
                case YES_NO_OPTION:
                    utama = tambahTombol(bawah, "Ya", YES_OPTION, true);
                    tambahTombol(bawah, "Tidak", NO_OPTION, false);
                    break;
                case YES_NO_CANCEL_OPTION:
                    utama = tambahTombol(bawah, "Ya", YES_OPTION, true);
                    tambahTombol(bawah, "Tidak", NO_OPTION, false);
                    tambahTombol(bawah, "Batal", CANCEL_OPTION, false);
                    break;
                case OK_CANCEL_OPTION:
                    utama = tambahTombol(bawah, "OK", OK_OPTION, true);
                    tambahTombol(bawah, "Batal", CANCEL_OPTION, false);
                    break;
                default:
                    utama = tambahTombol(bawah, "OK", OK_OPTION, true);
                    break;
            }
            kartu.add(bawah, BorderLayout.SOUTH);

            setContentPane(kartu);
            pasangKeyboard(kartu);

            // geser jendela dengan menyeret kartu
            MouseAdapter seret = new MouseAdapter() {
                private Point awal;

                @Override
                public void mousePressed(MouseEvent e) {
                    awal = e.getPoint();
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    if (awal != null) {
                        Point p = getLocation();
                        setLocation(p.x + e.getX() - awal.x, p.y + e.getY() - awal.y);
                    }
                }
            };
            kartu.addMouseListener(seret);
            kartu.addMouseMotionListener(seret);

            pack();
            try {
                setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), SUDUT, SUDUT));
            } catch (Exception e) {
                // shaped window tidak didukung, pakai kotak biasa
            }
            setLocationRelativeTo(owner);

            addWindowListener(new WindowAdapter() {
                @Override
                public void windowOpened(WindowEvent e) {
                    if (utama != null) {
                        utama.requestFocusInWindow();
                    }
                }
            });
        }

        private Component buatIsi(Object pesan) {
            if (pesan instanceof Component) {
                return (Component) pesan;
            }
            if (pesan instanceof Object[]) {
                JPanel p = new JPanel();
                p.setOpaque(false);
                p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
                for (Object o : (Object[]) pesan) {
                    Component c = buatIsi(o);
                    if (c instanceof JComponent) {
                        ((JComponent) c).setAlignmentX(Component.LEFT_ALIGNMENT);
                    }
                    p.add(c);
                    p.add(Box.createVerticalStrut(4));
                }
                return p;
            }
            String s = pesan == null ? "" : String.valueOf(pesan);
            if (s.regionMatches(true, 0, "<html>", 0, 6)) {
                JLabel l = new JLabel(s);
                l.setFont(F_PESAN);
                l.setForeground(new Color(85, 85, 85));
                return l;
            }

            JTextArea ta = new JTextArea(s);
            ta.setFont(F_PESAN);
            ta.setForeground(new Color(85, 85, 85));
            ta.setEditable(false);
            ta.setFocusable(false);
            ta.setOpaque(false);
            ta.setLineWrap(true);
            ta.setWrapStyleWord(true);
            ta.setBorder(null);

            FontMetrics fm = ta.getFontMetrics(F_PESAN);
            int lebar = 0;
            for (String b : s.split("\n")) {
                lebar = Math.max(lebar, fm.stringWidth(b));
            }
            lebar = Math.max(TEKS_MIN, Math.min(TEKS_MAX, lebar + 4));
            ta.setSize(lebar, Short.MAX_VALUE);
            int tinggi = ta.getPreferredSize().height;
            if (tinggi <= TINGGI_MAX) {
                ta.setPreferredSize(new Dimension(lebar, tinggi));
                return ta;
            }
            // pesan panjang (mis. detail error): pakai scroll
            JScrollPane sp = new JScrollPane(ta);
            sp.setOpaque(false);
            sp.getViewport().setOpaque(false);
            sp.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, new Color(230, 230, 230)));
            sp.setPreferredSize(new Dimension(lebar + 18, TINGGI_MAX));
            sp.getVerticalScrollBar().setUnitIncrement(14);
            return sp;
        }

        private Tombol tambahTombol(JPanel panel, String teks, final int nilai, boolean primer) {
            final Tombol t = new Tombol(teks, primer ? warna : null);
            t.addActionListener(e -> {
                hasil = nilai;
                tutup();
            });
            // Enter menekan tombol yang sedang fokus, bukan selalu tombol pertama
            t.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "tekan");
            t.getActionMap().put("tekan", new AbstractAction() {
                private static final long serialVersionUID = 1L;

                @Override
                public void actionPerformed(ActionEvent e) {
                    t.doClick();
                }
            });
            tombol.add(t);
            panel.add(t);
            return t;
        }

        private void pasangKeyboard(JComponent root) {
            root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "batal");
            root.getActionMap().put("batal", new AbstractAction() {
                private static final long serialVersionUID = 1L;

                @Override
                public void actionPerformed(ActionEvent e) {
                    hasil = CLOSED_OPTION;
                    tutup();
                }
            });
            root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "kanan");
            root.getActionMap().put("kanan", new AbstractAction() {
                private static final long serialVersionUID = 1L;

                @Override
                public void actionPerformed(ActionEvent e) {
                    pindahFokus(1);
                }
            });
            root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "kiri");
            root.getActionMap().put("kiri", new AbstractAction() {
                private static final long serialVersionUID = 1L;

                @Override
                public void actionPerformed(ActionEvent e) {
                    pindahFokus(-1);
                }
            });
        }

        private void pindahFokus(int arah) {
            if (tombol.isEmpty()) {
                return;
            }
            int i = 0;
            for (int k = 0; k < tombol.size(); k++) {
                if (tombol.get(k).isFocusOwner()) {
                    i = k;
                }
            }
            i = (i + arah + tombol.size()) % tombol.size();
            tombol.get(i).requestFocusInWindow();
        }

        void mulai() {
            posAkhir = getLocation();
            setLocation(posAkhir.x, posAkhir.y + 12);
            setOpacitySafe(0f);
            timer = new Timer(15, e -> {
                opacity = Math.min(1f, opacity + 0.12f);
                setOpacitySafe(opacity);
                setLocation(posAkhir.x, posAkhir.y + Math.round(12 * (1f - opacity)));
                if (opacity >= 1f) {
                    timer.stop();
                }
            });
            timer.start();
            setVisible(true); // modal: berhenti di sini sampai ditutup
        }

        private void tutup() {
            if (timer != null) {
                timer.stop();
            }
            setVisible(false);
            dispose();
        }

        private void setOpacitySafe(float o) {
            try {
                setOpacity(Math.max(0f, Math.min(1f, o)));
            } catch (Exception e) {
                // translucency tidak didukung
            }
        }

        /** Latar kartu putih dengan garis warna di kiri. */
        private class Kartu extends JPanel {

            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, w, h);
                g.setColor(warna);
                g.fillRect(0, 0, 6, h);
                g.setColor(new Color(205, 205, 205));
                g.drawRoundRect(0, 0, w - 1, h - 1, SUDUT, SUDUT);
                g.dispose();
            }
        }

        /** Tanda silang kecil di kanan atas. */
        private class Tutup extends JComponent {

            private static final long serialVersionUID = 1L;
            private boolean hover = false;

            Tutup() {
                setPreferredSize(new Dimension(20, 20));
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setToolTipText("Tutup (Esc)");
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        hasil = CLOSED_OPTION;
                        tutup();
                    }

                    @Override
                    public void mouseEntered(MouseEvent e) {
                        hover = true;
                        repaint();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        hover = false;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (hover) {
                    g.setColor(new Color(238, 238, 238));
                    g.fillOval(0, 0, 20, 20);
                }
                g.setColor(hover ? new Color(80, 80, 80) : new Color(150, 150, 150));
                g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.drawLine(6, 6, 14, 14);
                g.drawLine(14, 6, 6, 14);
                g.dispose();
            }
        }
    }

    /** Ikon lingkaran berwarna dengan simbol sesuai jenis. */
    private static class Ikon extends JComponent {

        private static final long serialVersionUID = 1L;
        private static final int D = 40, HALO = 6;
        private final int jenis;
        private final Color warna;

        Ikon(int jenis, Color warna) {
            this.jenis = jenis;
            this.warna = warna;
            setPreferredSize(new Dimension(D + HALO * 2, D + HALO * 2));
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setColor(new Color(warna.getRed(), warna.getGreen(), warna.getBlue(), 40));
            g.fillOval(0, 0, D + HALO * 2, D + HALO * 2);
            int x = HALO, y = HALO;
            g.setColor(warna);
            g.fillOval(x, y, D, D);
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(3.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            switch (jenis) {
                case SUKSES:
                    g.drawPolyline(new int[]{x + 11, x + 18, x + 29}, new int[]{y + 21, y + 28, y + 14}, 3);
                    break;
                case GAGAL:
                    g.drawLine(x + 14, y + 14, x + 26, y + 26);
                    g.drawLine(x + 26, y + 14, x + 14, y + 26);
                    break;
                case PERINGATAN:
                    g.drawLine(x + 20, y + 10, x + 20, y + 23);
                    g.fillOval(x + 18, y + 27, 5, 5);
                    break;
                case TANYA:
                    g.setFont(new Font("Arial", Font.BOLD, 24));
                    FontMetrics fm = g.getFontMetrics();
                    g.drawString("?", x + (D - fm.stringWidth("?")) / 2, y + (D - fm.getHeight()) / 2 + fm.getAscent());
                    break;
                default:
                    g.fillOval(x + 18, y + 8, 5, 5);
                    g.drawLine(x + 20, y + 18, x + 20, y + 30);
                    break;
            }
            g.dispose();
        }
    }

    /** Tombol bulat: primer berwarna penuh, sekunder putih bergaris. */
    private static class Tombol extends JButton {

        private static final long serialVersionUID = 1L;
        private final Color warna;
        private boolean hover = false;

        Tombol(String teks, Color warna) {
            super(teks);
            this.warna = warna;
            setFont(F_TOMBOL);
            setForeground(warna == null ? new Color(60, 60, 60) : Color.WHITE);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hover = false;
                    repaint();
                }
            });
            addFocusListener(new java.awt.event.FocusAdapter() {
                @Override
                public void focusGained(java.awt.event.FocusEvent e) {
                    repaint();
                }

                @Override
                public void focusLost(java.awt.event.FocusEvent e) {
                    repaint();
                }
            });
        }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(getFont());
            return new Dimension(Math.max(86, fm.stringWidth(getText()) + 36), 32);
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            boolean tekan = getModel().isArmed() && getModel().isPressed();
            Color fokus = warna == null ? new Color(120, 120, 120) : warna;
            if (isFocusOwner()) {
                g.setColor(new Color(fokus.getRed(), fokus.getGreen(), fokus.getBlue(), 60));
                g.fillRoundRect(0, 0, w, h, 12, 12);
            }
            if (warna != null) {
                Color c = warna;
                if (tekan) {
                    c = c.darker().darker();
                } else if (hover) {
                    c = c.darker();
                }
                g.setColor(c);
                g.fillRoundRect(2, 2, w - 4, h - 4, 10, 10);
            } else {
                g.setColor(tekan ? new Color(225, 225, 225) : (hover ? new Color(242, 242, 242) : Color.WHITE));
                g.fillRoundRect(2, 2, w - 4, h - 4, 10, 10);
                g.setColor(new Color(200, 200, 200));
                g.drawRoundRect(2, 2, w - 5, h - 5, 10, 10);
            }
            g.setFont(getFont());
            g.setColor(getForeground());
            FontMetrics fm = g.getFontMetrics();
            g.drawString(getText(), (w - fm.stringWidth(getText())) / 2, (h - fm.getHeight()) / 2 + fm.getAscent());
            g.dispose();
        }
    }
}
