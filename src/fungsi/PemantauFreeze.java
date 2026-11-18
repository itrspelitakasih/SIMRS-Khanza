package fungsi;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;
import javax.swing.SwingUtilities;

/**
 * Pencatat freeze: bila thread tampilan (EDT) tidak merespons lebih dari BATAS_MS, kode yang sedang
 * dijalankan EDT (stack trace) dicetak ke console dan ditambahkan ke file log/freeze.log.
 * Satu kali freeze dicatat sekali (di awal) lalu lamanya dicatat saat aplikasi kembali normal.
 */
public final class PemantauFreeze {
    private static final long BATAS_MS = 2000, INTERVAL_MS = 500;
    private static final AtomicLong denyutTerakhir = new AtomicLong(System.currentTimeMillis());
    private static volatile Thread edt;
    private static boolean jalan = false;

    private PemantauFreeze() {}

    public static synchronized void mulai() {
        if (jalan) {
            return;
        }
        jalan = true;
        Thread t = new Thread(PemantauFreeze::pantau, "Pemantau-Freeze");
        t.setDaemon(true);
        t.start();
    }

    private static void pantau() {
        boolean sedangFreeze = false;
        long mulaiFreeze = 0;
        while (true) {
            try {
                Thread.sleep(INTERVAL_MS);
            } catch (InterruptedException e) {
                return;
            }
            SwingUtilities.invokeLater(() -> {
                edt = Thread.currentThread();
                denyutTerakhir.set(System.currentTimeMillis());
            });
            long diam = System.currentTimeMillis() - denyutTerakhir.get();
            if (!sedangFreeze && diam > BATAS_MS && edt != null) {
                sedangFreeze = true;
                mulaiFreeze = denyutTerakhir.get();
                StringBuilder sb = new StringBuilder();
                sb.append("==== FREEZE ").append(waktu()).append(" : tampilan tidak merespons > ")
                        .append(BATAS_MS / 1000).append(" detik. Kode yang sedang berjalan:\n");
                for (StackTraceElement el : edt.getStackTrace()) {
                    sb.append("    at ").append(el).append('\n');
                }
                tulis(sb.toString());
            } else if (sedangFreeze && diam < BATAS_MS) {
                sedangFreeze = false;
                tulis("==== FREEZE selesai " + waktu() + ", lama sekitar "
                        + ((denyutTerakhir.get() - mulaiFreeze) / 1000) + " detik\n");
            }
        }
    }

    private static String waktu() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    private static void tulis(String teks) {
        System.out.print(teks);
        try {
            new java.io.File("log").mkdirs();
            try (PrintWriter pw = new PrintWriter(new FileWriter("log/freeze.log", true))) {
                pw.print(teks);
            }
        } catch (Exception e) {
            System.out.println("Pemantau freeze gagal menulis log : " + e);
        }
    }
}
