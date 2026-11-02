package main.java.com.petmatch;

import main.java.com.petmatch.config.DatabaseConnection;
import main.java.com.petmatch.ui.MainFrame;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            try {
                // Fail fast when the database is not configured or reachable.
                DatabaseConnection.getConnection().close();
                new MainFrame().setVisible(true);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null,
                        "Cannot connect to PostgreSQL. Check application.properties / environment variables.\n\n"
                                + ex.getMessage(),
                        "Database connection error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
