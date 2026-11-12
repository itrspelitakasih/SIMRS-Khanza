/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fungsi;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Pewarnaan daftar pasien Rawat Jalan (Kasir Ralan), aktif bila AKTIFKANWARNARALAN=yes.
 * RSPK: warna lembut agar tabel tidak menjadi satu blok biru pekat (hampir semua pasien sudah bayar),
 * baris terpilih memakai sorotan biru tua seperti tabel lain.
 * @author Owner
 */
public class WarnaTableKasirRalan extends DefaultTableCellRenderer {
    ////// tambahan untuk men set kolom mana yang mau di ubah warna bersadarkan status
    public int kolom = 15;
    public int statrawat = 10;

    private static final Color PUTIH        = Color.WHITE;
    private static final Color ZEBRA        = new Color(0xF2F8FD);
    private static final Color BAYAR        = new Color(0xD7EAFB);   // Sudah Bayar
    private static final Color BAYAR_ZEBRA  = new Color(0xC8E1F8);
    private static final Color BATAL        = new Color(0xFCE1CF);   // Batal
    private static final Color BATAL_ZEBRA  = new Color(0xF9D3BA);
    private static final Color TEKS         = new Color(50, 50, 50);
    private static final Color TEKS_BATAL   = new Color(0x9A3B00);
    private static final Color PILIH        = new Color(0x1E78D2);
    private static final Border JARAK       = BorderFactory.createEmptyBorder(0, 4, 0, 2);

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column){
        Component component = super.getTableCellRendererComponent(table, value, isSelected, false, row, column);
        boolean ganjil = (row % 2 == 1);
        boolean bayar = false, batal = false;
        try { bayar = table.getValueAt(row, kolom).toString().equals("Sudah Bayar"); } catch (Exception e) { }
        try { batal = table.getValueAt(row, statrawat).toString().equals("Batal"); } catch (Exception e) { }

        if (isSelected) {
            component.setBackground(PILIH);
            component.setForeground(Color.WHITE);
        } else if (batal) {
            component.setBackground(ganjil ? BATAL_ZEBRA : BATAL);
            component.setForeground(TEKS_BATAL);
        } else if (bayar) {
            component.setBackground(ganjil ? BAYAR_ZEBRA : BAYAR);
            component.setForeground(TEKS);
        } else {
            component.setBackground(ganjil ? ZEBRA : PUTIH);
            component.setForeground(TEKS);
        }
        // Membuat teks tebal saat baris dipilih
        component.setFont(component.getFont().deriveFont(isSelected ? Font.BOLD : Font.PLAIN));
        setBorder(JARAK);
        return component;
    }

}
