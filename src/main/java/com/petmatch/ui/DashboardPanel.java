package com.petmatch.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import com.petmatch.config.DatabaseConnection;
import com.petmatch.repository.ApplicationRepository;
import com.petmatch.repository.FosterFamilyRepository;
import com.petmatch.repository.PetRepository;

public class DashboardPanel extends JPanel {
    private final JLabel[] values = new JLabel[4];
    private final JLabel status = new JLabel(" ");
    private final DefaultTableModel rows = new DefaultTableModel(
            new Object[] { "Placement", "Pet", "Family", "Application", "Date", "Status" }, 0) {
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    };

    public DashboardPanel(PetRepository p, FosterFamilyRepository f, ApplicationRepository a) {
        setLayout(new BorderLayout(16, 16));
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        JPanel top = new JPanel(new BorderLayout(0, 16));
        top.setOpaque(false);
        top.add(
                UiUtil.pageTitle(
                        "Placement dashboard", "Pets → Families → Applications → Matching → Safe handover"),
                BorderLayout.NORTH);
        JPanel cards = new JPanel(new GridLayout(1, 4, 16, 0));
        cards.setOpaque(false);
        String[] labels = {
                "Available pets", "Active families", "Pending applications", "Active placements"
        };
        for (int i = 0; i < 4; i++) {
            JPanel card = new JPanel(new BorderLayout(0, 12));
            card.setBackground(Color.WHITE);
            card.setBorder(BorderFactory.createEmptyBorder(20, 18, 20, 18));
            card.add(new JLabel(labels[i]), BorderLayout.NORTH);
            values[i] = new JLabel("—");
            values[i].setFont(new Font("SansSerif", Font.BOLD, 32));
            values[i].setForeground(new Color(33, 98, 83));
            card.add(values[i]);
            cards.add(card);
        }
        top.add(cards);
        add(top, BorderLayout.NORTH);
        JTable table = new JTable(rows);
        UiUtil.polish(table);
        add(new JScrollPane(table));
        JPanel bottom = new JPanel(new BorderLayout());
        JButton reload = new JButton("Refresh dashboard");
        UiUtil.styleButton(reload);
        reload.addActionListener(e -> load());
        bottom.add(reload, BorderLayout.WEST);
        bottom.add(status);
        add(bottom, BorderLayout.SOUTH);
        loadDashboard();
    }

    public void load() {
        loadDashboard();
    }

    private void loadDashboard() {
        try (var c = DatabaseConnection.getConnection()) {
            try (var q = c.prepareStatement("SELECT * FROM v_dashboard_summary");
                    var rs = q.executeQuery()) {
                if (rs.next()) {
                    String[] columns = {
                            "available_pets", "active_families", "pending_applications", "active_placements"
                    };
                    for (int i = 0; i < 4; i++)
                        values[i].setText(rs.getString(columns[i]));
                }
            }
            rows.setRowCount(0);
            try (var q = c.prepareStatement(
                    "SELECT"
                            + " x.placement_id,p.name,f.family_name,x.application_id,x.placement_date,x.status"
                            + " FROM placements x JOIN pets p ON p.pet_id=x.pet_id JOIN foster_families f"
                            + " ON f.family_id=x.family_id ORDER BY x.placement_id DESC");
                    var rs = q.executeQuery()) {
                while (rs.next())
                    rows.addRow(
                            new Object[] {
                                    rs.getLong(1),
                                    rs.getString(2),
                                    rs.getString(3),
                                    rs.getLong(4),
                                    rs.getTimestamp(5),
                                    rs.getString(6)
                            });
            }
            status.setText("   Live MySQL data · " + rows.getRowCount() + " placements");
        } catch (Exception ex) {
            status.setText("   Cannot load dashboard: " + ex.getMessage());
        }
    }
}
