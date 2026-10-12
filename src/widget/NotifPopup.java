package widget;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Popup notifikasi (toast) yang muncul di pojok kanan bawah jendela pemanggil
 * dan hilang otomatis. Tidak memblok input seperti JOptionPane.
 *
 * Contoh: NotifPopup.sukses(this,"Resume Tersimpan","Resume pasien berhasil disimpan");
 */
public class NotifPopup extends JWindow {

    private static final long serialVersionUID = 1L;

    public static final int SUKSES = 0, INFO = 1, PERINGATAN = 2, GAGAL = 3;

    private static final int LEBAR = 340, TINGGI = 78, JARAK = 10, DURASI = 3500, TICK = 30;
    private static final List<NotifPopup> aktif = new ArrayList<NotifPopup>();

    private final String judul, pesan;
    private final Color warna;
    private final int jenis;
    private final Window owner;
    private Timer timer;
    private int sisa = DURASI;
    private float opacity = 0f;
    private boolean menutup = false, hover = false;

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

    public static void tampil(final Component parent, final int jenis, final String judul, final String pesan) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                Window w = parent == null ? null : (parent instanceof Window ? (Window) parent : SwingUtilities.getWindowAncestor(parent));
                new NotifPopup(w, jenis, judul, pesan).mulai();
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            SwingUtilities.invokeLater(r);
        }
    }

    private NotifPopup(Window owner, int jenis, String judul, String pesan) {
        super(owner);
        this.owner = owner;
        this.jenis = jenis;
        this.judul = judul == null ? "" : judul;
        this.pesan = pesan == null ? "" : pesan;
        switch (jenis) {
            case INFO:       warna = new Color(41, 128, 185); break;
            case PERINGATAN: warna = new Color(230, 145, 20); break;
            case GAGAL:      warna = new Color(205, 55, 55); break;
            default:         warna = new Color(39, 160, 90); break;
        }
        setFocusableWindowState(false);
        setAlwaysOnTop(true);
        setSize(LEBAR, TINGGI);
        try {
            setShape(new RoundRectangle2D.Double(0, 0, LEBAR, TINGGI, 14, 14));
        } catch (Exception e) {
            // shaped window tidak didukung, pakai kotak biasa
        }
        Isi isi = new Isi();
        setContentPane(isi);
        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                tutup();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                hover = true;
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hover = false;
            }
        };
        isi.addMouseListener(ma);
        isi.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void mulai() {
        synchronized (aktif) {
            aktif.add(this);
        }
        aturPosisi();
        setOpacitySafe(0f);
        setVisible(true);
        timer = new Timer(TICK, e -> langkah());
        timer.start();
    }

    private void langkah() {
        if (menutup) {
            opacity -= 0.1f;
            if (opacity <= 0f) {
                timer.stop();
                setVisible(false);
                dispose();
                synchronized (aktif) {
                    aktif.remove(this);
                }
                for (NotifPopup p : salinAktif()) {
                    p.aturPosisi();
                }
                return;
            }
            setOpacitySafe(opacity);
            return;
        }
        if (opacity < 1f) {
            opacity = Math.min(1f, opacity + 0.12f);
            setOpacitySafe(opacity);
        }
        if (!hover) {
            sisa -= TICK;
        }
        repaint();
        if (sisa <= 0) {
            menutup = true;
        }
    }

    private void tutup() {
        menutup = true;
    }

    private void setOpacitySafe(float o) {
        try {
            setOpacity(Math.max(0f, Math.min(1f, o)));
        } catch (Exception e) {
            // translucency tidak didukung
        }
    }

    private static List<NotifPopup> salinAktif() {
        synchronized (aktif) {
            return new ArrayList<NotifPopup>(aktif);
        }
    }

    private void aturPosisi() {
        Rectangle area;
        if (owner != null && owner.isShowing()) {
            area = owner.getBounds();
        } else {
            GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
            Insets in = Toolkit.getDefaultToolkit().getScreenInsets(gc);
            Rectangle b = gc.getBounds();
            area = new Rectangle(b.x + in.left, b.y + in.top, b.width - in.left - in.right, b.height - in.top - in.bottom);
        }
        int urutan;
        synchronized (aktif) {
            urutan = aktif.indexOf(this);
        }
        if (urutan < 0) {
            urutan = 0;
        }
        int x = area.x + area.width - LEBAR - 20;
        int y = area.y + area.height - (TINGGI + JARAK) * (urutan + 1) - 20;
        setLocation(x, y);
    }

    private class Isi extends JComponent {

        private static final long serialVersionUID = 1L;
        private final Font fJudul = new Font("Tahoma", Font.BOLD, 13);
        private final Font fPesan = new Font("Tahoma", Font.PLAIN, 11);

        Isi() {
            setPreferredSize(new Dimension(LEBAR, TINGGI));
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();

            // latar & garis tepi
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, w, h);
            g.setColor(warna);
            g.fillRect(0, 0, 6, h);
            g.setColor(new Color(215, 215, 215));
            g.drawRoundRect(0, 0, w - 1, h - 1, 14, 14);

            // ikon lingkaran
            int d = 34, ix = 20, iy = (h - d) / 2 - 2;
            g.setColor(warna);
            g.fillOval(ix, iy, d, d);
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            switch (jenis) {
                case SUKSES:
                    g.drawPolyline(new int[]{ix + 9, ix + 15, ix + 25}, new int[]{iy + 18, iy + 24, iy + 11}, 3);
                    break;
                case GAGAL:
                    g.drawLine(ix + 11, iy + 11, ix + 23, iy + 23);
                    g.drawLine(ix + 23, iy + 11, ix + 11, iy + 23);
                    break;
                case PERINGATAN:
                    g.drawLine(ix + 17, iy + 9, ix + 17, iy + 19);
                    g.fillOval(ix + 15, iy + 23, 4, 4);
                    break;
                default:
                    g.fillOval(ix + 15, iy + 7, 4, 4);
                    g.drawLine(ix + 17, iy + 15, ix + 17, iy + 25);
                    break;
            }

            // teks
            int tx = ix + d + 14, maxW = w - tx - 28;
            g.setColor(new Color(40, 40, 40));
            g.setFont(fJudul);
            g.drawString(potong(g.getFontMetrics(), judul, maxW), tx, iy + 13);
            g.setColor(new Color(95, 95, 95));
            g.setFont(fPesan);
            FontMetrics fm = g.getFontMetrics();
            List<String> baris = bungkus(fm, pesan, maxW, 2);
            int by = iy + 30;
            for (String s : baris) {
                g.drawString(s, tx, by);
                by += fm.getHeight();
            }

            // tombol tutup
            g.setColor(new Color(150, 150, 150));
            g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawLine(w - 20, 12, w - 13, 19);
            g.drawLine(w - 13, 12, w - 20, 19);

            // progress sisa waktu
            int pw = (int) ((w - 6) * Math.max(0, sisa) / (double) DURASI);
            g.setColor(new Color(warna.getRed(), warna.getGreen(), warna.getBlue(), 70));
            g.fillRect(6, h - 4, w - 6, 4);
            g.setColor(warna);
            g.fillRect(6, h - 4, pw, 4);
            g.dispose();
        }

        private String potong(FontMetrics fm, String s, int maxW) {
            if (fm.stringWidth(s) <= maxW) {
                return s;
            }
            while (s.length() > 0 && fm.stringWidth(s + "...") > maxW) {
                s = s.substring(0, s.length() - 1);
            }
            return s + "...";
        }

        private List<String> bungkus(FontMetrics fm, String s, int maxW, int maxBaris) {
            List<String> hasil = new ArrayList<String>();
            StringBuilder cur = new StringBuilder();
            String[] kata = s.split("\\s+");
            for (int i = 0; i < kata.length; i++) {
                String coba = cur.length() == 0 ? kata[i] : cur + " " + kata[i];
                if (fm.stringWidth(coba) <= maxW || cur.length() == 0) {
                    cur = new StringBuilder(coba);
                } else {
                    hasil.add(cur.toString());
                    cur = new StringBuilder(kata[i]);
                    if (hasil.size() == maxBaris - 1) {
                        StringBuilder rest = new StringBuilder(cur);
                        for (int j = i + 1; j < kata.length; j++) {
                            rest.append(" ").append(kata[j]);
                        }
                        hasil.add(potong(fm, rest.toString(), maxW));
                        return hasil;
                    }
                }
            }
            if (cur.length() > 0) {
                hasil.add(potong(fm, cur.toString(), maxW));
            }
            return hasil;
        }
    }
}
