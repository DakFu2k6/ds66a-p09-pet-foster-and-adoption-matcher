package com.petmatch.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
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
import com.petmatch.model.ApplicationStatus;
import com.petmatch.service.ApplicationService;

public class ApplicationsPanel extends JPanel {

    private final ApplicationService applicationService;
    private final JTable table;
    private final DefaultTableModel tableModel;

    public ApplicationsPanel(ApplicationService applicationService) {
        this.applicationService = applicationService;
        setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("QUẢN LÝ ĐƠN ĐĂNG KÝ NHẬN NUÔI", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(titleLabel, BorderLayout.NORTH);

        String[] columnNames = {"Mã Đơn", "Trạng thái", "Ngày nộp"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton refreshButton = new JButton("Tải lại danh sách");
        refreshButton.addActionListener(e -> loadApplicationsData());
        JButton approveButton = new JButton("Duyệt đơn (APPROVED)");
        approveButton.addActionListener(e -> updateSelectedApplicationStatus(ApplicationStatus.APPROVED));
        JButton rejectButton = new JButton("Từ chối đơn (REJECTED)");
        rejectButton.addActionListener(e -> updateSelectedApplicationStatus(ApplicationStatus.REJECTED));

        bottomPanel.add(refreshButton);
        bottomPanel.add(approveButton);
        bottomPanel.add(rejectButton);
        
        add(bottomPanel, BorderLayout.SOUTH);

        loadApplicationsData();
    }

    private void loadApplicationsData() {
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
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu đơn: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateSelectedApplicationStatus(ApplicationStatus newStatus) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một đơn trong bảng để cập nhật trạng thái!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            long appId = Long.parseLong(tableModel.getValueAt(selectedRow, 0).toString());
            applicationService.updateApplicationStatus(appId, newStatus);
            
            JOptionPane.showMessageDialog(this, "Cập nhật trạng thái đơn thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            loadApplicationsData();
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(this, "Lỗi cập nhật trạng thái: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}