package com.petmatch;

import com.petmatch.config.DatabaseConnection;
import com.petmatch.ui.MainFrame;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            try {
                DatabaseConnection.getConnection().close();
                new MainFrame().setVisible(true);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null,
                        "Cannot connect to MySQL. Check .env / application.properties / environment variables "
                                + "(DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD).\n\n"
                                + ex.getMessage(),
                        "Database connection error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}