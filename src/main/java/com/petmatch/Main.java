package com.petmatch;

import com.petmatch.config.DatabaseConnection;
import com.petmatch.ui.MainFrame;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        try {
            DatabaseConnection.getConnection().close();
        } catch (Exception ex) {
            String message = com.petmatch.config.DatabaseDiagnostics.explain(ex);
            System.err.println(message);
            if (!java.awt.GraphicsEnvironment.isHeadless())
                JOptionPane.showMessageDialog(null, message, "Database error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
            return;
        }
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            try {
                new MainFrame().setVisible(true);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null,
                        "Cannot connect to MySQL. Check .env / application.properties / environment variables "
                                + "(DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD).\n\n"
                                + com.petmatch.config.DatabaseDiagnostics.explain(ex),
                        "Database connection error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}