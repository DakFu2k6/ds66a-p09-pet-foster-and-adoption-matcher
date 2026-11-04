package com.petmatch.ui;

import com.petmatch.service.AuthService;
import java.awt.*;
import javax.swing.*;

public final class LoginFrame extends JFrame {
    private final JTextField username = new JTextField(22);
    private final JPasswordField password = new JPasswordField(22);

    public LoginFrame() {
        super("PetMatch | Staff sign in");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        JPanel brand = new JPanel(new BorderLayout());
        brand.setBackground(new Color(22, 67, 59));
        brand.setBorder(BorderFactory.createEmptyBorder(42, 32, 42, 32));
        JLabel title = new JLabel(
                "<html><font color='white' size='7'>PetMatch</font><br><br><font color='#b7e4cc'>Every"
                        + " placement.<br>A better beginning.</font></html>");
        brand.add(title);
        JPanel form = new JPanel(new GridLayout(0, 1, 8, 14));
        form.setBorder(BorderFactory.createEmptyBorder(40, 36, 40, 36));
        form.add(new JLabel("STAFF WORKSPACE"));
        form.add(new JLabel("Username"));
        form.add(username);
        form.add(new JLabel("Password"));
        form.add(password);
        JButton login = new JButton("Sign in");
        UiUtil.styleButton(login);
        form.add(login);
        JLabel status = new JLabel("Create your staff account with scripts/create_admin.py");
        form.add(status);
        login.addActionListener(
                e -> {
                    String user = username.getText();
                    char[] secret = password.getPassword();
                    password.setText("");
                    login.setEnabled(false);
                    new SwingWorker<String, Void>() {
                        protected String doInBackground() {
                            return new AuthService().login(user, secret);
                        }

                        protected void done() {
                            try {
                                String name = get();
                                if (name == null)
                                    status.setText("Invalid credentials, inactive account or temporarily locked.");
                                else {
                                    new MainFrame(name).setVisible(true);
                                    dispose();
                                }
                            } catch (Exception ex) {
                                UiUtil.error(LoginFrame.this, ex);
                            } finally {
                                login.setEnabled(true);
                            }
                        }
                    }.execute();
                });
        add(brand, BorderLayout.WEST);
        add(form);
        getRootPane().setDefaultButton(login);
        pack();
        setMinimumSize(new Dimension(820, 480));
        setLocationRelativeTo(null);
    }
}
