package setting;

import fungsi.HargaBarangTetap;
import fungsi.koneksiDB;
import fungsi.validasi;
import inventory.DlgBarang;
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
 * RSPK: tab "Harga Barang Tetap" di Setup Harga Obat (DlgSetHarga).
 * Barang di sini (mis. Oksigen per liter) memakai harga tetap per kelas dan tidak ikut
 * kenaikan persen per penjamin. Lihat fungsi.HargaBarangTetap.
 */
public class PanelHargaBarangTetap extends widget.InternalFrame {
    private final Connection koneksi = koneksiDB.condb();
    private final validasi Valid = new validasi();
    private final DefaultTableModel tabMode;
    private final widget.TextBox kdbarang = new widget.TextBox(), nmbarang = new widget.TextBox();
    private final widget.TextBox ralan = new widget.TextBox(), kelas1 = new widget.TextBox(), kelas2 = new widget.TextBox(),
            kelas3 = new widget.TextBox(), utama = new widget.TextBox(), vip = new widget.TextBox(), vvip = new widget.TextBox();
    private final widget.ComboBox ikutDiskon = new widget.ComboBox();
    private final widget.Table tabel = new widget.Table();
    private final widget.Label lcount = new widget.Label();

    public PanelHargaBarangTetap() {
        setBorder(null);
        setName("PanelHargaBarangTetap");
        setLayout(new BorderLayout(1, 1));

        widget.panelisi isi = new widget.panelisi();
        isi.setName("panelHargaBarangTetap");
        isi.setPreferredSize(new Dimension(100, 166));
        isi.setLayout(null);

        tambahLabel(isi, "Barang :", 0, 10, 100);
        kdbarang.setEditable(false);
        kdbarang.setName("kdbarangtetap");
        isi.add(kdbarang); kdbarang.setBounds(104, 10, 110, 23);
        nmbarang.setEditable(false);
        nmbarang.setName("nmbarangtetap");
        isi.add(nmbarang); nmbarang.setBounds(217, 10, 330, 23);
        widget.Button btnBarang = new widget.Button();
        btnBarang.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png")));
        btnBarang.setToolTipText("Pilih barang");
        btnBarang.setName("btnBarangTetap");
        btnBarang.addActionListener(e -> pilihBarang());
        isi.add(btnBarang); btnBarang.setBounds(550, 10, 28, 23);

        int x1 = 104, x2 = 300, x3 = 496, w = 80;
        tambahLabel(isi, "Ralan (Rp) :", 0, 40, 100); tambahIsian(isi, ralan, "ralantetap", x1, 40, w);
        tambahLabel(isi, "Kelas 1 (Rp) :", 190, 40, 106); tambahIsian(isi, kelas1, "kelas1tetap", x2, 40, w);
        tambahLabel(isi, "Kelas 2 (Rp) :", 386, 40, 106); tambahIsian(isi, kelas2, "kelas2tetap", x3, 40, w);
        tambahLabel(isi, "Kelas 3 (Rp) :", 0, 70, 100); tambahIsian(isi, kelas3, "kelas3tetap", x1, 70, w);
        tambahLabel(isi, "Utama (Rp) :", 190, 70, 106); tambahIsian(isi, utama, "utamatetap", x2, 70, w);
        tambahLabel(isi, "VIP (Rp) :", 386, 70, 106); tambahIsian(isi, vip, "viptetap", x3, 70, w);
        tambahLabel(isi, "VVIP (Rp) :", 0, 100, 100); tambahIsian(isi, vvip, "vviptetap", x1, 100, w);
        widget.Button samakan = new widget.Button();
        samakan.setText("Samakan semua kelas");
        samakan.setToolTipText("Isi semua kelas sama dengan harga Ralan");
        samakan.setName("btnSamakanTetap");
        samakan.addActionListener(e -> { String v = ralan.getText().trim(); for (widget.TextBox t : new widget.TextBox[]{kelas1, kelas2, kelas3, utama, vip, vvip}) t.setText(v); });
        isi.add(samakan); samakan.setBounds(x2 - 106, 100, 186, 23);
        tambahLabel(isi, "Ikut Diskon Jenis Bayar :", 300, 100, 192);
        ikutDiskon.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"Tidak", "Ya"}));
        ikutDiskon.setName("ikutdiskontetap");
        isi.add(ikutDiskon); ikutDiskon.setBounds(x3, 100, w, 23);

        widget.Label ket = new widget.Label();
        ket.setText("Barang di daftar ini memakai harga tetap per kelas, tidak ikut persen kenaikan per jenis bayar. Contoh: Oksigen per liter.");
        ket.setName("kettetap");
        isi.add(ket); ket.setBounds(10, 132, 980, 23);

        add(isi, BorderLayout.PAGE_START);

        tabMode = new DefaultTableModel(null, new Object[]{"Kode Barang", "Nama Barang", "Satuan", "Ralan", "Kelas 1", "Kelas 2", "Kelas 3", "Utama", "VIP", "VVIP", "Ikut Diskon"}) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabel.setModel(tabMode);
        tabel.setName("tbHargaBarangTetap");
        tabel.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        int[] lebar = {95, 230, 60, 75, 75, 75, 75, 75, 75, 75, 80};
        for (int i = 0; i < lebar.length; i++) tabel.getColumnModel().getColumn(i).setPreferredWidth(lebar[i]);
        tabel.setDefaultRenderer(Object.class, new fungsi.WarnaTable());
        tabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) { ambilBaris(); }
        });
        widget.ScrollPane scroll = new widget.ScrollPane();
        scroll.setName("scrollHargaBarangTetap");
        scroll.setViewportView(tabel);
        add(scroll, BorderLayout.CENTER);

        lcount.setText("0");
        lcount.setName("lcounttetap");
        widget.panelisi bawah = new widget.panelisi();
        bawah.setName("panelHargaBarangTetapBawah");
        bawah.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));
        widget.Label lr = new widget.Label(); lr.setText("Record :"); bawah.add(lr); bawah.add(lcount);
        add(bawah, BorderLayout.PAGE_END);

        if (HargaBarangTetap.aktif()) tampil();
    }

    private void tambahLabel(widget.panelisi p, String teks, int x, int y, int w) {
        widget.Label l = new widget.Label();
        l.setText(teks);
        l.setName("lbl" + teks.replaceAll("[^A-Za-z0-9]", ""));
        l.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        p.add(l); l.setBounds(x, y, w, 23);
    }

    private void tambahIsian(widget.panelisi p, widget.TextBox t, String nama, int x, int y, int w) {
        t.setName(nama);
        t.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        p.add(t); t.setBounds(x, y, w, 23);
    }

    private widget.Button tombol(String teks, String ikon, java.awt.event.ActionListener aksi) {
        widget.Button b = new widget.Button();
        b.setText(teks);
        try { b.setIcon(new javax.swing.ImageIcon(getClass().getResource(ikon))); } catch (Exception e) { }
        b.setName("btn" + teks + "Tetap");
        b.addActionListener(aksi);
        return b;
    }

    private void pilihBarang() {
        DlgBarang barang = new DlgBarang(null, false);
        barang.addWindowListener(new WindowAdapter() {
            @Override public void windowClosed(WindowEvent e) {
                if (barang.getTable().getSelectedRow() != -1) {
                    kdbarang.setText(barang.getTable().getValueAt(barang.getTable().getSelectedRow(), 1).toString());
                    nmbarang.setText(barang.getTable().getValueAt(barang.getTable().getSelectedRow(), 2).toString());
                    isiDariTabel(kdbarang.getText());
                }
            }
        });
        barang.emptTeks();
        barang.isCek();
        barang.setSize(getWidth() > 200 ? getWidth() - 20 : 900, getHeight() > 200 ? getHeight() - 20 : 600);
        barang.setLocationRelativeTo(this);
        barang.setVisible(true);
    }

    private double angka(widget.TextBox t) {
        try { return Double.parseDouble(t.getText().trim().replace(",", "")); } catch (Exception e) { return -1; }
    }

    public void simpan() {
        if (!HargaBarangTetap.aktif()) {
            JOptionPane.showMessageDialog(null, "Tabel set_harga_barang_tetap belum tersedia di database.");
            return;
        }
        if (kdbarang.getText().trim().equals("")) {
            JOptionPane.showMessageDialog(null, "Silahkan pilih barang terlebih dahulu.");
            return;
        }
        widget.TextBox[] isian = {ralan, kelas1, kelas2, kelas3, utama, vip, vvip};
        double[] nilai = new double[isian.length];
        for (int i = 0; i < isian.length; i++) {
            nilai[i] = angka(isian[i]);
            if (nilai[i] < 0) {
                JOptionPane.showMessageDialog(null, "Harga harus berupa angka (boleh 0).");
                isian[i].requestFocus();
                return;
            }
        }
        try (PreparedStatement ps = koneksi.prepareStatement(
                "insert into set_harga_barang_tetap (kode_brng,ralan,kelas1,kelas2,kelas3,utama,vip,vvip,ikut_diskon) values (?,?,?,?,?,?,?,?,?) "
                + "on duplicate key update ralan=values(ralan),kelas1=values(kelas1),kelas2=values(kelas2),kelas3=values(kelas3),"
                + "utama=values(utama),vip=values(vip),vvip=values(vvip),ikut_diskon=values(ikut_diskon)")) {
            ps.setString(1, kdbarang.getText().trim());
            for (int i = 0; i < nilai.length; i++) ps.setDouble(i + 2, nilai[i]);
            ps.setString(9, ikutDiskon.getSelectedItem().toString());
            ps.executeUpdate();
            tampil();
            kosongkan();
        } catch (Exception e) {
            System.out.println("Notif Harga Barang Tetap : " + e);
            JOptionPane.showMessageDialog(null, "Gagal menyimpan: " + e.getMessage());
        }
    }

    public void hapus() {
        if (kdbarang.getText().trim().equals("")) {
            JOptionPane.showMessageDialog(null, "Silahkan pilih data pada tabel terlebih dahulu.");
            return;
        }
        if (JOptionPane.showConfirmDialog(null, "Hapus harga tetap " + nmbarang.getText() + "?\nBarang ini akan kembali memakai harga normal (persen per jenis bayar).",
                "Konfirmasi", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try (PreparedStatement ps = koneksi.prepareStatement("delete from set_harga_barang_tetap where kode_brng=?")) {
            ps.setString(1, kdbarang.getText().trim());
            ps.executeUpdate();
            tampil();
            kosongkan();
        } catch (Exception e) {
            System.out.println("Notif Harga Barang Tetap : " + e);
        }
    }

    public void kosongkan() {
        for (widget.TextBox t : new widget.TextBox[]{kdbarang, nmbarang, ralan, kelas1, kelas2, kelas3, utama, vip, vvip}) t.setText("");
        ikutDiskon.setSelectedIndex(0);
        tabel.clearSelection();
    }

    public final void tampil() {
        Valid.tabelKosong(tabMode);
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select t.kode_brng,d.nama_brng,d.kode_sat,t.ralan,t.kelas1,t.kelas2,t.kelas3,t.utama,t.vip,t.vvip,t.ikut_diskon "
                + "from set_harga_barang_tetap t inner join databarang d on d.kode_brng=t.kode_brng order by d.nama_brng");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                tabMode.addRow(new Object[]{rs.getString(1), rs.getString(2), rs.getString(3), rs.getDouble(4), rs.getDouble(5), rs.getDouble(6),
                    rs.getDouble(7), rs.getDouble(8), rs.getDouble(9), rs.getDouble(10), rs.getString(11)});
            }
        } catch (Exception e) {
            System.out.println("Notif Harga Barang Tetap : " + e);
        }
        lcount.setText("" + tabMode.getRowCount());
    }

    private void ambilBaris() {
        int r = tabel.getSelectedRow();
        if (r < 0) return;
        kdbarang.setText(tabMode.getValueAt(r, 0).toString());
        nmbarang.setText(tabMode.getValueAt(r, 1).toString());
        widget.TextBox[] isian = {ralan, kelas1, kelas2, kelas3, utama, vip, vvip};
        for (int i = 0; i < isian.length; i++) { double v = Double.parseDouble(tabMode.getValueAt(r, 3 + i).toString()); isian[i].setText(v == Math.rint(v) ? String.valueOf((long) v) : String.valueOf(v)); }
        ikutDiskon.setSelectedItem(tabMode.getValueAt(r, 10).toString());
    }

    private void isiDariTabel(String kode) {
        for (int r = 0; r < tabMode.getRowCount(); r++) {
            if (tabMode.getValueAt(r, 0).toString().equals(kode)) {
                tabel.setRowSelectionInterval(r, r);
                ambilBaris();
                return;
            }
        }
    }
}
