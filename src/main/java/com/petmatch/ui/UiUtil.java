package com.petmatch.ui;

import javax.swing.*;
import java.awt.*;

public final class UiUtil {
    private UiUtil() {
    }

    public static void styleButton(JButton b) {
        b.setFocusPainted(false);
        b.setBackground(new Color(42, 112, 94));
        b.setForeground(Color.WHITE);
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
    }

    public static JPanel pageTitle(String title, String subtitle) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel h = new JLabel(title);
        h.setFont(new Font("SansSerif", Font.BOLD, 24));
        h.setForeground(new Color(35, 55, 48));
        JLabel s = new JLabel(subtitle);
        s.setForeground(new Color(105, 120, 112));
        JPanel x = new JPanel(new GridLayout(2, 1, 0, 4));
        x.setOpaque(false);
        x.add(h);
        x.add(s);
        p.add(x, BorderLayout.CENTER);
        p.setBorder(BorderFactory.createEmptyBorder(4, 4, 16, 4));
        return p;
    }

    public static void error(Component parent, Exception ex) {
        JOptionPane.showMessageDialog(parent, ex.getMessage(), "Operation failed", JOptionPane.ERROR_MESSAGE);
    }
}
