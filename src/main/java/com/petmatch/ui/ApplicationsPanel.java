package com.petmatch.ui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import com.petmatch.model.AdoptionApplication;
import com.petmatch.service.ApplicationService;

public class ApplicationsPanel extends JPanel {

    private final ApplicationService applicationService;
    private JTable table;
    private DefaultTableModel tableModel;

    public ApplicationsPanel(ApplicationService applicationService) {
        this.applicationService = applicationService;
        setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("QUẢN LÝ ĐƠN ĐĂNG KÝ NHẬN NUÔI", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(titleLabel, BorderLayout.NORTH);

        String[] columnNames = {"Mã Đơn", "Trạng thái", "Ngày nộp"};
        tableModel = new DefaultTableModel(columnNames, 0);
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton refreshButton = new JButton("Tải lại danh sách đơn");
        refreshButton.addActionListener(e -> loadApplicationsData());
        add(refreshButton, BorderLayout.SOUTH);

        loadApplicationsData();
    }

    public void loadApplicationsData() {
        tableModel.setRowCount(0);
        try {
            List<AdoptionApplication> list = applicationService.getAllApplications();
            for (AdoptionApplication app : list) {
                Object[] row = {
                    app.getId(),
                    app.getStatus(),
                    app.getApplicationDate()
                };
                tableModel.addRow(row);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu đơn: " + e.getMessage());
        }
    }
}