package com.petmatch.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import com.petmatch.model.ExperienceLevel;
import com.petmatch.model.FosterFamily;
import com.petmatch.model.HousingType;
import com.petmatch.service.FosterFamilyService;

public class FamiliesPanel extends JPanel {
    private final FosterFamilyService service;
    private final DefaultTableModel model = new DefaultTableModel(new Object[] { 
            "ID", "Tên gia đình", "Người liên hệ", "Điện thoại", 
            "Loại nhà", "Kinh nghiệm", "Sức chứa", "Đang nuôi", "Có sân", "Trạng thái" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    public FamiliesPanel(FosterFamilyService s) {
        this.service = s;
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        add(UiUtil.pageTitle("QUẢN LÝ GIA ĐÌNH NHẬN NUÔI", "Cập nhật thông tin và theo dõi sức chứa của các gia đình"),
                BorderLayout.NORTH);
        
        JTable table = new JTable(model);
        table.setRowHeight(28);
        add(new JScrollPane(table), BorderLayout.CENTER);
        
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addBtn = new JButton("Thêm gia đình mới");
        JButton refreshBtn = new JButton("Làm mới danh sách");
        
        UiUtil.styleButton(addBtn);
        UiUtil.styleButton(refreshBtn);
        
        bar.add(addBtn);
        bar.add(refreshBtn);
        add(bar, BorderLayout.SOUTH);
        
        addBtn.addActionListener(e -> addFamily());
        refreshBtn.addActionListener(e -> load());
        
        load();
    }

    private void load() {
        try {
            model.setRowCount(0);
            for (FosterFamily f : service.findAll()) {
                model.addRow(new Object[] { 
                    f.getId(), f.getFamilyName(), f.getContactName(), f.getPhone(),
                    f.getHousingType(), f.getExperienceLevel(), f.getCapacity(), f.getActivePetCount(),
                    f.isHasYard() ? "Có" : "Không", 
                    f.isActive() ? "Đang mở" : "Tạm ngưng" 
                });
            }
        } catch (RuntimeException e) {
            UiUtil.error(this, e);
        }
    }

    private void addFamily() {
        JTextField family = new JTextField();
        JTextField contact = new JTextField();
        JTextField phone = new JTextField();
        JTextField capacity = new JTextField("2");
        
        JComboBox<HousingType> housing = new JComboBox<>(HousingType.values());
        JComboBox<ExperienceLevel> experience = new JComboBox<>(ExperienceLevel.values());
        JCheckBox yard = new JCheckBox("Có sân vườn");
        JCheckBox children = new JCheckBox("Có trẻ nhỏ");
        
        JPanel p = new JPanel(new GridLayout(0, 2, 8, 8));
        p.add(new JLabel("Tên gia đình/Tổ chức:"));
        p.add(family);
        p.add(new JLabel("Người đại diện:"));
        p.add(contact);
        p.add(new JLabel("Số điện thoại:"));
        p.add(phone);
        p.add(new JLabel("Loại nhà ở:"));
        p.add(housing);
        p.add(new JLabel("Kinh nghiệm chăm sóc:"));
        p.add(experience);
        p.add(new JLabel("Sức chứa tối đa:"));
        p.add(capacity);
        p.add(yard);
        p.add(children);
        
        if (JOptionPane.showConfirmDialog(this, p, "Đăng ký gia đình nhận nuôi mới",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION)
            return;
            
        try {
            int parsedCapacity = Integer.parseInt(capacity.getText().trim());
            
            service.save(new FosterFamily(
                0, 
                family.getText().trim(), 
                contact.getText().trim(), 
                phone.getText().trim(),
                (HousingType) housing.getSelectedItem(), 
                (ExperienceLevel) experience.getSelectedItem(),
                parsedCapacity, 
                0, 
                yard.isSelected(), 
                children.isSelected(), 
                true
            ));
            
            JOptionPane.showMessageDialog(this, "Thêm gia đình mới thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            load();
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Sức chứa tối đa phải là một số nguyên (ví dụ: 1, 2, 3)!", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException e) {
            UiUtil.error(this, e);
        } catch (RuntimeException e) {
            UiUtil.error(this, e);
        }
    }
}