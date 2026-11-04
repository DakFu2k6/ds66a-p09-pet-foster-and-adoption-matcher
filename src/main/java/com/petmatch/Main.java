package com.petmatch;

import com.petmatch.config.DatabaseConnection;
import com.petmatch.config.DatabaseDiagnostics;
import com.petmatch.ui.LoginFrame;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        try {
            DatabaseConnection.getConnection().close();
        } catch (SQLException ex) {
            String message = DatabaseDiagnostics.explain(ex);
            System.err.println(message);

            if (!java.awt.GraphicsEnvironment.isHeadless()) {
                JOptionPane.showMessageDialog(
                        null, message, "Lỗi kết nối Cơ sở dữ liệu", JOptionPane.ERROR_MESSAGE);
            }
            System.exit(1);
            return;
        }

        SwingUtilities.invokeLater(
                () -> {
                    try {
                        UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
                        com.petmatch.ui.UiUtil.installTheme();
                    } catch (ClassNotFoundException
                            | InstantiationException
                            | IllegalAccessException
                            | javax.swing.UnsupportedLookAndFeelException ignored) {
                    }

                    try {
                        new LoginFrame().setVisible(true);
                    } catch (Exception ex) {
                        String errorMsg = """
                                Không thể kết nối đến cơ sở dữ liệu MySQL. Vui lòng kiểm tra lại cấu hình .env / application.properties / biến môi trường
                                (DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD).

                                Chi tiết lỗi:
                                """
                                + DatabaseDiagnostics.explain(ex);

                        JOptionPane.showMessageDialog(
                                null, errorMsg, "Lỗi khởi chạy hệ thống", JOptionPane.ERROR_MESSAGE);
                    }
                });
    }
}
