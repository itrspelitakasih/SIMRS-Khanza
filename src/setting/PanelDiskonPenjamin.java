package setting;

import fungsi.DiskonPenjamin;
import fungsi.koneksiDB;
import fungsi.validasi;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 * RSPK: tab "Diskon Jenis Bayar" di Setup Harga Obat (DlgSetHarga).
 * Persen diskon per jenis bayar (penjab) untuk kategori Obat, Alkes, Tindakan, Operasi,
 * Laboratorium, Radiologi dan Kamar. Diterapkan otomatis sebagai Potongan di billing ralan/ranap.
 */
public class PanelDiskonPenjamin extends widget.InternalFrame {
    private final Connection koneksi = koneksiDB.condb();
    private final validasi Valid = new validasi();
    private final DefaultTableModel tabMode;
    private final widget.TextBox kdpj = new widget.TextBox(), nmpj = new widget.TextBox();
    private final widget.TextBox[] persen = new widget.TextBox[DiskonPenjamin.KATEGORI.length];
    private final widget.Table tabel = new widget.Table();
    private final widget.Label lcount = new widget.Label();

    public PanelDiskonPenjamin() {
        setBorder(null);
        setName("PanelDiskonPenjamin");
        setLayout(new BorderLayout(1, 1));

        widget.panelisi isi = new widget.panelisi();
        isi.setName("panelDiskonPenjamin");
        isi.setPreferredSize(new Dimension(100, 166));
        isi.setLayout(null);

        label(isi, "Jenis Bayar :", 0, 10, 110);
        kdpj.setEditable(false); kdpj.setName("kdpjdiskon");
        isi.add(kdpj); kdpj.setBounds(114, 10, 70, 23);
        nmpj.setEditable(false); nmpj.setName("nmpjdiskon");
        isi.add(nmpj); nmpj.setBounds(187, 10, 330, 23);
        widget.Button btnPj = new widget.Button();
        btnPj.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png")));
        btnPj.setToolTipText("Pilih jenis bayar");
        btnPj.setName("btnPjDiskon");
        btnPj.addActionListener(e -> pilihPenjamin());
        isi.add(btnPj); btnPj.setBounds(520, 10, 28, 23);

        int[][] pos = {{0, 40}, {200, 40}, {400, 40}, {0, 70}, {200, 70}, {400, 70}, {0, 100}};
        for (int i = 0; i < persen.length; i++) {
            label(isi, "Diskon " + DiskonPenjamin.KATEGORI[i] + " (%) :", pos[i][0], pos[i][1], 150);
            persen[i] = new widget.TextBox();
            persen[i].setName("persen" + DiskonPenjamin.KATEGORI[i]);
            persen[i].setHorizontalAlignment(javax.swing.JTextField.RIGHT);
            isi.add(persen[i]); persen[i].setBounds(pos[i][0] + 154, pos[i][1], 45, 23);
        }
        widget.Button salin = new widget.Button();
        salin.setText("Salin ke jenis bayar lain...");
        salin.setToolTipText("Salin persen diskon jenis bayar ini ke jenis bayar lain");
        salin.setName("btnSalinDiskon");
        salin.addActionListener(e -> salin());
        isi.add(salin); salin.setBounds(400, 100, 200, 23);

        widget.Label ket = new widget.Label();
        ket.setText("Dihitung otomatis saat billing dibuka (belum disimpan), tercetak di nota sebagai Potongan. Kosong = tanpa diskon. Alkes = kategori Alkes & BHP (sama dengan nota PAMA).");
        ket.setName("ketdiskon");
        isi.add(ket); ket.setBounds(10, 132, 1000, 23);
        add(isi, BorderLayout.PAGE_START);

        Object[] kolom = new Object[2 + DiskonPenjamin.KATEGORI.length];
        kolom[0] = "Kd PJ"; kolom[1] = "Jenis Bayar";
        for (int i = 0; i < DiskonPenjamin.KATEGORI.length; i++) kolom[2 + i] = DiskonPenjamin.KATEGORI[i] + " (%)";
        tabMode = new DefaultTableModel(null, kolom) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabel.setModel(tabMode);
        tabel.setName("tbDiskonPenjamin");
        tabel.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        tabel.getColumnModel().getColumn(0).setPreferredWidth(55);
        tabel.getColumnModel().getColumn(1).setPreferredWidth(250);
        for (int i = 2; i < kolom.length; i++) tabel.getColumnModel().getColumn(i).setPreferredWidth(95);
        tabel.setDefaultRenderer(Object.class, new fungsi.WarnaTable());
        tabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) { ambilBaris(); }
        });
        widget.ScrollPane scroll = new widget.ScrollPane();
        scroll.setName("scrollDiskonPenjamin");
        scroll.setViewportView(tabel);
        add(scroll, BorderLayout.CENTER);

        widget.panelisi bawah = new widget.panelisi();
        bawah.setName("panelDiskonPenjaminBawah");
        bawah.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));
        widget.Label lr = new widget.Label(); lr.setText("Record :"); bawah.add(lr);
        lcount.setText("0"); bawah.add(lcount);
        add(bawah, BorderLayout.PAGE_END);

        if (DiskonPenjamin.aktif()) tampil();
    }

    private void label(widget.panelisi p, String teks, int x, int y, int w) {
        widget.Label l = new widget.Label();
        l.setText(teks);
        l.setName("lbl" + teks.replaceAll("[^A-Za-z0-9]", ""));
        l.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        p.add(l); l.setBounds(x, y, w, 23);
    }

    private void pilihPenjamin() {
        simrskhanza.DlgCariCaraBayar penjab = new simrskhanza.DlgCariCaraBayar(null, false);
        penjab.addWindowListener(new WindowAdapter() {
            @Override public void windowClosed(WindowEvent e) {
                if (penjab.getTable().getSelectedRow() != -1) {
                    kdpj.setText(penjab.getTable().getValueAt(penjab.getTable().getSelectedRow(), 1).toString());
                    nmpj.setText(penjab.getTable().getValueAt(penjab.getTable().getSelectedRow(), 2).toString());
                    isiDariTabel(kdpj.getText());
                }
            }
        });
        penjab.isCek();
        penjab.setSize(getWidth() > 200 ? getWidth() - 20 : 900, getHeight() > 200 ? getHeight() - 20 : 600);
        penjab.setLocationRelativeTo(this);
        penjab.setVisible(true);
    }

    private double angka(widget.TextBox t) {
        String s = t.getText().trim().replace(',', '.');
        if (s.equals("")) return 0;
        try { return Double.parseDouble(s); } catch (Exception e) { return -1; }
    }

    private boolean simpanKe(String kd, double[] nilai) throws Exception {
        try (PreparedStatement del = koneksi.prepareStatement("delete from set_diskon_penjab where kd_pj=?")) {
            del.setString(1, kd);
            del.executeUpdate();
        }
        try (PreparedStatement ins = koneksi.prepareStatement("insert into set_diskon_penjab (kd_pj,kategori,persen) values (?,?,?)")) {
            for (int i = 0; i < nilai.length; i++) {
                if (nilai[i] <= 0) continue;
                ins.setString(1, kd);
                ins.setString(2, DiskonPenjamin.KATEGORI[i]);
                ins.setDouble(3, nilai[i]);
                ins.executeUpdate();
            }
        }
        return true;
    }

    private double[] bacaIsian() {
        double[] nilai = new double[persen.length];
        for (int i = 0; i < persen.length; i++) {
            nilai[i] = angka(persen[i]);
            if (nilai[i] < 0 || nilai[i] > 100) {
                JOptionPane.showMessageDialog(null, "Persen diskon harus angka 0 sampai 100.");
                persen[i].requestFocus();
                return null;
            }
        }
        return nilai;
    }

    public void simpan() {
        if (!DiskonPenjamin.aktif()) {
            JOptionPane.showMessageDialog(null, "Tabel set_diskon_penjab belum tersedia di database.");
            return;
        }
        if (kdpj.getText().trim().equals("")) {
            JOptionPane.showMessageDialog(null, "Silahkan pilih jenis bayar terlebih dahulu.");
            return;
        }
        double[] nilai = bacaIsian();
        if (nilai == null) return;
        try {
            simpanKe(kdpj.getText().trim(), nilai);
            tampil();
            kosongkan();
        } catch (Exception e) {
            System.out.println("Notif Diskon Penjamin : " + e);
            JOptionPane.showMessageDialog(null, "Gagal menyimpan: " + e.getMessage());
        }
    }

    private void salin() {
        double[] nilai = bacaIsian();
        if (nilai == null) return;
        if (kdpj.getText().trim().equals("")) {
            JOptionPane.showMessageDialog(null, "Pilih dulu jenis bayar sumber dan isi persennya.");
            return;
        }
        simrskhanza.DlgCariCaraBayar penjab = new simrskhanza.DlgCariCaraBayar(null, false);
        penjab.addWindowListener(new WindowAdapter() {
            @Override public void windowClosed(WindowEvent e) {
                if (penjab.getTable().getSelectedRow() == -1) return;
                String kd = penjab.getTable().getValueAt(penjab.getTable().getSelectedRow(), 1).toString();
                String nm = penjab.getTable().getValueAt(penjab.getTable().getSelectedRow(), 2).toString();
                if (JOptionPane.showConfirmDialog(null, "Salin diskon " + nmpj.getText() + " ke " + nm + "?", "Konfirmasi", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
                try {
                    simpanKe(kd, nilai);
                    tampil();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Gagal menyalin: " + ex.getMessage());
                }
            }
        });
        penjab.isCek();
        penjab.setSize(getWidth() > 200 ? getWidth() - 20 : 900, getHeight() > 200 ? getHeight() - 20 : 600);
        penjab.setLocationRelativeTo(this);
        penjab.setVisible(true);
    }

    public void hapus() {
        if (kdpj.getText().trim().equals("")) {
            JOptionPane.showMessageDialog(null, "Silahkan pilih data pada tabel terlebih dahulu.");
            return;
        }
        if (JOptionPane.showConfirmDialog(null, "Hapus semua diskon untuk " + nmpj.getText() + "?", "Konfirmasi", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try (PreparedStatement ps = koneksi.prepareStatement("delete from set_diskon_penjab where kd_pj=?")) {
            ps.setString(1, kdpj.getText().trim());
            ps.executeUpdate();
            tampil();
            kosongkan();
        } catch (Exception e) {
            System.out.println("Notif Diskon Penjamin : " + e);
        }
    }

    public void kosongkan() {
        kdpj.setText(""); nmpj.setText("");
        for (widget.TextBox t : persen) t.setText("");
        tabel.clearSelection();
    }

    public final void tampil() {
        Valid.tabelKosong(tabMode);
        StringBuilder sql = new StringBuilder("select p.kd_pj,p.png_jawab");
        for (String k : DiskonPenjamin.KATEGORI) sql.append(",max(if(d.kategori='").append(k).append("',d.persen,0))");
        sql.append(" from set_diskon_penjab d inner join penjab p on p.kd_pj=d.kd_pj group by p.kd_pj,p.png_jawab order by p.png_jawab");
        try (PreparedStatement ps = koneksi.prepareStatement(sql.toString()); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Object[] baris = new Object[2 + DiskonPenjamin.KATEGORI.length];
                baris[0] = rs.getString(1); baris[1] = rs.getString(2);
                for (int i = 0; i < DiskonPenjamin.KATEGORI.length; i++) {
                    double v = rs.getDouble(3 + i);
                    baris[2 + i] = v > 0 ? DiskonPenjamin.teksPersen(v) : "-";
                }
                tabMode.addRow(baris);
            }
        } catch (Exception e) {
            System.out.println("Notif Diskon Penjamin : " + e);
        }
        lcount.setText("" + tabMode.getRowCount());
    }

    private void ambilBaris() {
        int r = tabel.getSelectedRow();
        if (r < 0) return;
        kdpj.setText(tabMode.getValueAt(r, 0).toString());
        nmpj.setText(tabMode.getValueAt(r, 1).toString());
        for (int i = 0; i < persen.length; i++) {
            String v = tabMode.getValueAt(r, 2 + i).toString();
            persen[i].setText(v.equals("-") ? "" : v);
        }
    }

    private void isiDariTabel(String kd) {
        for (int r = 0; r < tabMode.getRowCount(); r++) {
            if (tabMode.getValueAt(r, 0).toString().equals(kd)) {
                tabel.setRowSelectionInterval(r, r);
                ambilBaris();
                return;
            }
        }
        for (widget.TextBox t : persen) t.setText("");
    }
}
