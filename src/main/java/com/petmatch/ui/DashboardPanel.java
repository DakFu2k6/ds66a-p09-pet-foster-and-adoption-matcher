package com.petmatch.ui;

import com.petmatch.repository.*;
import javax.swing.*;
import java.awt.*;

public class DashboardPanel extends JPanel {
    private final PetRepository pets;
    private final FosterFamilyRepository families;
    private final ApplicationRepository apps;
    private final JLabel summary = new JLabel();

    public DashboardPanel(PetRepository p, FosterFamilyRepository f, ApplicationRepository a) {
        pets = p;
        families = f;
        apps = a;
        setLayout(new BorderLayout(16, 16));
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        add(UiUtil.pageTitle("Shelter overview", "A quick view of the adoption workflow"), BorderLayout.NORTH);
        summary.setFont(new Font("SansSerif", Font.PLAIN, 18));
        summary.setVerticalAlignment(SwingConstants.TOP);
        add(summary, BorderLayout.CENTER);
        JButton refresh = new JButton("Refresh overview");
        UiUtil.styleButton(refresh);
        refresh.addActionListener(e -> load());
        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT));
        south.add(refresh);
        add(south, BorderLayout.SOUTH);
        load();
    }

    private void load() {
        try {
            long available = pets.findAll().stream().filter(p -> p.getStatus().name().equals("AVAILABLE")).count();
            int familyCount = families.findAll().size(), appCount = apps.findAll().size();
            summary.setText("<html><div style='font-size:16px;color:#62736a'>LIVE SYSTEM SUMMARY</div><br>"
                    + "<b>Available pets:</b> " + available + "<br><br><b>Registered foster families:</b> "
                    + familyCount
                    + "<br><br><b>Adoption applications:</b> " + appCount + "<br><br>"
                    + "<span style='color:#2a705e'>Next step:</span> create an application, evaluate matching and confirm a valid placement.</html>");
        } catch (Exception ex) {
            summary.setText("Could not load dashboard: " + ex.getMessage());
        }
    }
}
