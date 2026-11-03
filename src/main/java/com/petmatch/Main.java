package com.petmatch;

import java.sql.SQLException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.petmatch.config.DatabaseConnection;
import com.petmatch.config.DatabaseDiagnostics;
import com.petmatch.ui.MainFrame;

public class Main {
    public static void main(String[] args) {
        try {
            DatabaseConnection.getConnection().close();
        } catch (SQLException ex) {
            String message = DatabaseDiagnostics.explain(ex);
            System.err.println(message);
            if (!java.awt.GraphicsEnvironment.isHeadless())
                JOptionPane.showMessageDialog(null, message, "Database error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
            return;
        }
        
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                     javax.swing.UnsupportedLookAndFeelException ignored) {
            }
            try {
                new MainFrame().setVisible(true);
            } catch (Exception ex) {
                String errorMsg = """
                    Cannot connect to MySQL. Check .env / application.properties / environment variables
                    (DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD).

                    """ + DatabaseDiagnostics.explain(ex);

                JOptionPane.showMessageDialog(null, errorMsg, "Database connection error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}