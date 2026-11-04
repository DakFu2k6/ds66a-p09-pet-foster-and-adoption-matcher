package com.petmatch.ui;

import com.petmatch.model.AdoptionApplication;
import com.petmatch.model.ApplicationStatus;
import com.petmatch.service.ApplicationService;
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

public class ApplicationsPanel extends JPanel {

    private final ApplicationService applicationService;
    private final JTable table;
    private final DefaultTableModel tableModel;

    public ApplicationsPanel(ApplicationService applicationService) {
        this(
                applicationService,
                new com.petmatch.repository.JdbcPetRepository(),
                new com.petmatch.repository.JdbcFosterFamilyRepository());
    }

    public ApplicationsPanel(
            ApplicationService applicationService,
            com.petmatch.repository.PetRepository pets,
            com.petmatch.repository.FosterFamilyRepository families) {
        this.applicationService = applicationService;
        setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("QUẢN LÝ ĐƠN ĐĂNG KÝ NHẬN NUÔI", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(titleLabel, BorderLayout.NORTH);

        String[] columnNames = { "Mã Đơn", "Trạng thái", "Ngày nộp", "Thú cưng", "Gia đình" };
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
        approveButton.addActionListener(
                e -> updateSelectedApplicationStatus(ApplicationStatus.APPROVED));
        JButton rejectButton = new JButton("Từ chối đơn (REJECTED)");
        rejectButton.addActionListener(
                e -> updateSelectedApplicationStatus(ApplicationStatus.REJECTED));

        JButton create = new JButton("Tạo đơn đăng ký");
        create.addActionListener(
                e -> {
                    try {
                        var ps = pets.findAll().stream()
                                .filter(p -> p.getStatus() == com.petmatch.model.PetStatus.AVAILABLE)
                                .toList();
                        var fs = families.findAll().stream()
                                .filter(f -> f.hasCapacity() && f.getActivePetCount() == 0)
                                .toList();
                        javax.swing.JComboBox<String> pb = new javax.swing.JComboBox<>(
                                ps.stream().map(p -> p.getId() + " · " + p.getName()).toArray(String[]::new));
                        javax.swing.JComboBox<String> fb = new javax.swing.JComboBox<>(
                                fs.stream()
                                        .map(f -> f.getId() + " · " + f.getFamilyName())
                                        .toArray(String[]::new));
                        if (ps.isEmpty() || fs.isEmpty())
                            throw new IllegalArgumentException("Cần có thú cưng và gia đình khả dụng.");
                        JPanel form = new JPanel(new java.awt.GridLayout(0, 1, 8, 8));
                        form.add(new JLabel("Thú cưng"));
                        form.add(pb);
                        form.add(new JLabel("Gia đình"));
                        form.add(fb);
                        if (JOptionPane.showConfirmDialog(this, form, "Tạo đơn",
                                JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                            applicationService.createApplication(
                                    ps.get(pb.getSelectedIndex()), fs.get(fb.getSelectedIndex()));
                            loadApplicationsData();
                        }
                    } catch (RuntimeException ex) {
                        UiUtil.error(this, ex);
                    }
                });
        JButton cancel = new JButton("Hủy đơn");
        cancel.addActionListener(e -> updateSelectedApplicationStatus(ApplicationStatus.CANCELLED));
        bottomPanel.add(create);
        bottomPanel.add(cancel);
        bottomPanel.add(refreshButton);
        bottomPanel.add(approveButton);
        bottomPanel.add(rejectButton);

        add(bottomPanel, BorderLayout.SOUTH);

        loadApplicationsDataInternal();
    }

    public void loadApplicationsData() {
        loadApplicationsDataInternal();
    }

    private void loadApplicationsDataInternal() {
        tableModel.setRowCount(0);
        try {
            List<AdoptionApplication> list = applicationService.getAllApplications();
            for (AdoptionApplication app : list) {
                Object[] row = {
                        app.getId(),
                        app.getStatus(),
                        app.getApplicationDate(),
                        app.getPet().getName(),
                        app.getFamily().getFamilyName()
                };
                tableModel.addRow(row);
            }
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(
                    this, "Lỗi tải dữ liệu đơn: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateSelectedApplicationStatus(ApplicationStatus newStatus) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng chọn một đơn trong bảng để cập nhật trạng thái!",
                    "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            long appId = Long.parseLong(tableModel.getValueAt(selectedRow, 0).toString());
            applicationService.updateApplicationStatus(appId, newStatus);

            JOptionPane.showMessageDialog(
                    this,
                    "Cập nhật trạng thái đơn thành công!",
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);
            loadApplicationsData();
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(
                    this, "Lỗi cập nhật trạng thái: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
