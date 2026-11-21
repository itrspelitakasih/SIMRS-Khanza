package fungsi;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

/**
 * RSPK: Kalkulator Oksigen.
 * Menghitung jumlah liter oksigen dari aliran (L/menit) x lama pemakaian (jam mulai - jam selesai,
 * otomatis lewat tengah malam), lalu mengisikannya ke kolom Jumlah baris oksigen di tabel obat.
 * Kolom tabel obat: 0 = pilih (K), 1 = Jumlah, 3 = Nama Barang, 4 = Satuan, 6 = Harga.
 */
public final class KalkulatorOksigen {
    private KalkulatorOksigen() {}

    /** Baris oksigen di tabel: baris terpilih bila namanya oksigen, selain itu oksigen bersatuan liter pertama. */
    private static int barisOksigen(JTable tabel) {
        int pilih = tabel.getSelectedRow();
        if (pilih >= 0 && namaOksigen(tabel, pilih) && !masker(tabel, pilih)) return pilih;
        for (int r = 0; r < tabel.getRowCount(); r++) {
            if (namaOksigen(tabel, r) && !masker(tabel, r)) {
                String sat = String.valueOf(tabel.getValueAt(r, 4)).trim().toUpperCase();
                if (sat.startsWith("L")) return r;
            }
        }
        return -1;
    }

    private static boolean namaOksigen(JTable t, int r) {
        String n = String.valueOf(t.getValueAt(r, 3)).toUpperCase();
        return n.contains("OKSIGEN") || n.contains("OXYGEN") || n.matches(".*\\bO2\\b.*");
    }

    private static boolean masker(JTable t, int r) {
        String n = String.valueOf(t.getValueAt(r, 3)).toUpperCase();
        return n.contains("MASKER") || n.contains("SELANG") || n.contains("REGULATOR") || n.contains("TROLLY") || n.contains("NASAL") || n.contains("TABUNG");
    }

    private static int menit(String jam) {
        String[] p = jam.trim().replace('.', ':').split(":");
        int h = Integer.parseInt(p[0].trim());
        int m = p.length > 1 ? Integer.parseInt(p[1].trim()) : 0;
        if (h < 0 || h > 24 || m < 0 || m > 59) throw new IllegalArgumentException();
        return h * 60 + m;
    }

    public static void tampil(java.awt.Component induk, JTable tabel) {
        Window w = induk == null ? null : SwingUtilities.getWindowAncestor(induk);
        JDialog d = new JDialog(w, "Kalkulator Oksigen", java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        widget.InternalFrame f = new widget.InternalFrame();
        f.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)),
                "::[ Kalkulator Oksigen ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new java.awt.Font("Tahoma", 0, 12), new java.awt.Color(50, 50, 50)));
        f.setLayout(new BorderLayout(1, 1));
        widget.panelisi p = new widget.panelisi();
        p.setLayout(null);
        p.setPreferredSize(new Dimension(470, 170));

        widget.TextBox aliran = new widget.TextBox(), mulai = new widget.TextBox(), selesai = new widget.TextBox();
        widget.Label hasil = new widget.Label();
        String[][] lbl = {{"Aliran (L/menit) :", "10"}, {"Jam Mulai (jj:mm) :", "40"}, {"Jam Selesai (jj:mm) :", "70"}};
        widget.TextBox[] isian = {aliran, mulai, selesai};
        for (int i = 0; i < 3; i++) {
            widget.Label l = new widget.Label();
            l.setText(lbl[i][0]);
            l.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
            p.add(l); l.setBounds(0, Integer.parseInt(lbl[i][1]), 150, 23);
            isian[i].setName("oksigen" + i);
            p.add(isian[i]); isian[i].setBounds(154, Integer.parseInt(lbl[i][1]), 90, 23);
        }
        aliran.setText("2");
        widget.Label ket = new widget.Label();
        ket.setText("(lewat tengah malam otomatis)");
        p.add(ket); ket.setBounds(250, 70, 210, 23);
        hasil.setFont(new java.awt.Font("Tahoma", java.awt.Font.BOLD, 13));
        hasil.setText(" ");
        p.add(hasil); hasil.setBounds(12, 104, 400, 23);
        widget.Label ket2 = new widget.Label();
        ket2.setText("Pakai = isi Jumlah di baris Oksigen (satuan liter) pada tabel obat.");
        p.add(ket2); ket2.setBounds(12, 132, 400, 23);
        f.add(p, BorderLayout.CENTER);

        final double[] liter = {-1};
        Runnable hitung = () -> {
            try {
                double a = Double.parseDouble(aliran.getText().trim().replace(',', '.'));
                int m1 = menit(mulai.getText()), m2 = menit(selesai.getText());
                int lama = m2 - m1;
                if (lama <= 0) lama += 24 * 60;
                liter[0] = Math.round(a * lama);
                int baris = tabel == null ? -1 : barisOksigen(tabel);
                String biaya = "";
                if (baris >= 0) {
                    try {
                        double h = Double.parseDouble(String.valueOf(tabel.getValueAt(baris, 6)).replace(",", ""));
                        biaya = "  ≈ Rp " + String.format("%,.0f", liter[0] * h).replace(',', '.');
                    } catch (Exception e) { }
                }
                hasil.setText("Lama " + (lama / 60) + " jam " + (lama % 60) + " menit  →  " + String.format("%,.0f", liter[0]).replace(',', '.') + " liter" + biaya);
            } catch (Exception e) {
                liter[0] = -1;
                hasil.setText("Isi aliran dan jam dengan benar (contoh 16:30).");
            }
        };
        javax.swing.event.DocumentListener dl = new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { hitung.run(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { hitung.run(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { hitung.run(); }
        };
        for (widget.TextBox t : isian) t.getDocument().addDocumentListener(dl);

        widget.panelisi tombol = new widget.panelisi();
        tombol.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 9));
        widget.Button pakai = new widget.Button(), tutup = new widget.Button();
        pakai.setText("Pakai");
        pakai.setIcon(new javax.swing.ImageIcon(KalkulatorOksigen.class.getResource("/picture/save-16x16.png")));
        pakai.setPreferredSize(new Dimension(100, 30));
        tutup.setText("Tutup");
        tutup.setIcon(new javax.swing.ImageIcon(KalkulatorOksigen.class.getResource("/picture/exit.png")));
        tutup.setPreferredSize(new Dimension(100, 30));
        tombol.add(pakai); tombol.add(tutup);
        f.add(tombol, BorderLayout.PAGE_END);
        tutup.addActionListener(e -> d.dispose());
        pakai.addActionListener(e -> {
            hitung.run();
            if (liter[0] <= 0) {
                JOptionPane.showMessageDialog(d, "Jumlah liter belum bisa dihitung. Periksa aliran dan jam.");
                return;
            }
            int baris = tabel == null ? -1 : barisOksigen(tabel);
            String jml = String.valueOf((long) liter[0]);
            if (baris < 0) {
                try {
                    java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new java.awt.datatransfer.StringSelection(jml), null);
                } catch (Exception ex) { }
                JOptionPane.showMessageDialog(d, "Baris Oksigen (satuan liter) belum ada di tabel.\n"
                        + "Cari barang Oksigen dulu, lalu klik Pakai lagi.\nJumlah " + jml + " liter sudah disalin (Ctrl+V).");
                return;
            }
            if (tabel.isEditing()) tabel.getCellEditor().stopCellEditing();
            tabel.setValueAt(jml, baris, 1);
            try { tabel.setValueAt(true, baris, 0); } catch (Exception ex) { }
            tabel.setRowSelectionInterval(baris, baris);
            tabel.scrollRectToVisible(tabel.getCellRect(baris, 1, true));
            d.dispose();
        });

        d.getContentPane().add(f);
        d.pack();
        d.setLocationRelativeTo(w); // di tengah jendela induk (tengah layar bila tidak ada)
        d.setVisible(true);
    }
}
