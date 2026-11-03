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

import com.petmatch.model.Cat;
import com.petmatch.model.Dog;
import com.petmatch.model.Pet;
import com.petmatch.model.PetStatus;
import com.petmatch.model.PetType;
import com.petmatch.service.PetService;

public class PetsPanel extends JPanel {
    private final PetService service;
    
    private final DefaultTableModel model = new DefaultTableModel(new Object[] { 
            "ID", "Tên", "Loài", "Giống", "Tuổi (tháng)", "Sức khỏe", "Chăm sóc đặc biệt", "Môi trường sống", "Trạng thái" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(model);

    public PetsPanel(PetService service) {
        this.service = service;
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        add(UiUtil.pageTitle("QUẢN LÝ THÚ CƯNG", "Đăng ký và cập nhật hồ sơ chó mèo"), BorderLayout.NORTH);
        
        table.setRowHeight(28);
        add(new JScrollPane(table), BorderLayout.CENTER);
        
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addBtn = new JButton("Thêm thú cưng");
        JButton delBtn = new JButton("Xóa đã chọn");
        JButton refreshBtn = new JButton("Làm mới danh sách");
        
        for (JButton b : new JButton[] { addBtn, delBtn, refreshBtn }) {
            UiUtil.styleButton(b);
            actions.add(b);
        }
        add(actions, BorderLayout.SOUTH);
        
        addBtn.addActionListener(e -> addPet());
        delBtn.addActionListener(e -> deletePet());
        refreshBtn.addActionListener(e -> load());
        
        load();
    }

    private void load() {
        try {
            model.setRowCount(0);
            for (Pet p : service.findAll()) {
                model.addRow(new Object[] { 
                    p.getId(), p.getName(), p.getType(), p.getBreed(), p.getAgeMonths(),
                    p.getHealthStatus(), p.isSpecialCareRequired() ? "Có" : "Không", p.getHousingRequirement(), p.getStatus() 
                });
            }
        } catch (RuntimeException ex) {
            UiUtil.error(this, ex);
        }
    }

    private void addPet() {
        JTextField name = new JTextField();
        JTextField breed = new JTextField();
        JTextField age = new JTextField("12");
        
        JComboBox<PetType> type = new JComboBox<>(PetType.values());
        JComboBox<String> health = new JComboBox<>(new String[] { "GOOD", "RECOVERING", "SPECIAL_NEEDS" });
        JComboBox<String> temperament = new JComboBox<>(new String[] { "FRIENDLY", "CALM", "SHY", "ACTIVE" });
        JComboBox<String> housing = new JComboBox<>(new String[] { "ANY", "HOUSE", "APARTMENT", "YARD" });
        JCheckBox special = new JCheckBox("Yêu cầu chăm sóc đặc biệt");
        
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.add(new JLabel("Tên thú cưng:"));
        form.add(name);
        form.add(new JLabel("Loài:"));
        form.add(type);
        form.add(new JLabel("Giống:"));
        form.add(breed);
        form.add(new JLabel("Tuổi (tháng):"));
        form.add(age);
        form.add(new JLabel("Tình trạng sức khỏe:"));
        form.add(health);
        form.add(new JLabel("Tính cách:"));
        form.add(temperament);
        form.add(new JLabel("Yêu cầu chỗ ở:"));
        form.add(housing);
        form.add(new JLabel("Chăm sóc đặc biệt:"));
        form.add(special);
        
        if (JOptionPane.showConfirmDialog(this, form, "Đăng ký thú cưng mới",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION)
            return;
            
        try {
            int months = Integer.parseInt(age.getText().trim());
            Pet p = type.getSelectedItem() == PetType.DOG
                    ? new Dog(0, name.getText().trim(), breed.getText().trim(), months,
                            (String) health.getSelectedItem(), (String) temperament.getSelectedItem(),
                            special.isSelected(), (String) housing.getSelectedItem(), PetStatus.AVAILABLE, null)
                    : new Cat(0, name.getText().trim(), breed.getText().trim(), months,
                            (String) health.getSelectedItem(), (String) temperament.getSelectedItem(),
                            special.isSelected(), (String) housing.getSelectedItem(), PetStatus.AVAILABLE, null);
                            
            service.save(p);
            JOptionPane.showMessageDialog(this, "Thêm thú cưng mới thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            load();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Tuổi của thú cưng phải là một số nguyên dương!", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException | ClassCastException ex) {
            UiUtil.error(this, ex);
        } catch (RuntimeException ex) {
            UiUtil.error(this, ex);
        }
    }

    private void deletePet() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một thú cưng để xóa!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa thú cưng này?", "Xác nhận",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
            return;
            
        try {
            service.delete(((Number) model.getValueAt(row, 0)).longValue());
            JOptionPane.showMessageDialog(this, "Xóa thú cưng thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            load();
        } catch (RuntimeException ex) {
            UiUtil.error(this, ex);
        }
    }
}