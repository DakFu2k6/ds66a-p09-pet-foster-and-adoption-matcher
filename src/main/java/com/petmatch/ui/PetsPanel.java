package com.petmatch.ui;

import com.petmatch.model.Cat;
import com.petmatch.model.Dog;
import com.petmatch.model.Pet;
import com.petmatch.model.PetStatus;
import com.petmatch.model.PetType;
import com.petmatch.service.PetService;
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

public class PetsPanel extends JPanel {
    private final PetService service;

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[] {
                    "ID",
                    "Tên",
                    "Loài",
                    "Giống",
                    "Tuổi (tháng)",
                    "Sức khỏe",
                    "Chăm sóc đặc biệt",
                    "Môi trường sống",
                    "Trạng thái"
            },
            0) {
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

        add(
                UiUtil.pageTitle("QUẢN LÝ THÚ CƯNG", "Đăng ký và cập nhật hồ sơ chó mèo"),
                BorderLayout.NORTH);

        table.setRowHeight(28);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addBtn = new JButton("Thêm thú cưng");
        JButton editBtn = new JButton("Sửa hồ sơ");
        editBtn.addActionListener(e -> editPet());
        JButton delBtn = new JButton("Xóa đã chọn");
        JButton refreshBtn = new JButton("Làm mới danh sách");

        for (JButton b : new JButton[] { addBtn, editBtn, delBtn, refreshBtn }) {
            UiUtil.styleButton(b);
            actions.add(b);
        }
        add(actions, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> addPet());
        delBtn.addActionListener(e -> deletePet());
        refreshBtn.addActionListener(e -> load());

        load();
    }

    public final void load() {
        try {
            model.setRowCount(0);
            for (Pet p : service.findAll()) {
                model.addRow(
                        new Object[] {
                                p.getId(),
                                p.getName(),
                                p.getType(),
                                p.getBreed(),
                                p.getAgeMonths(),
                                p.getHealthStatus(),
                                p.isSpecialCareRequired() ? "Có" : "Không",
                                p.getHousingRequirement(),
                                p.getStatus()
                        });
            }
        } catch (RuntimeException ex) {
            UiUtil.error(this, ex);
        }
    }

    private void addPet() {
        petForm(null);
    }

    private void editPet() {
        int row = table.getSelectedRow();
        if (row < 0)
            return;
        try {
            petForm(service.findById(((Number) model.getValueAt(row, 0)).longValue()).orElseThrow());
        } catch (RuntimeException ex) {
            UiUtil.error(this, ex);
        }
    }

    private void petForm(Pet existing) {
        JTextField name = new JTextField();
        JTextField breed = new JTextField();
        JTextField age = new JTextField("12");

        JComboBox<PetType> type = new JComboBox<>(PetType.values());
        JComboBox<String> health = new JComboBox<>(new String[] { "GOOD", "RECOVERING", "SPECIAL_NEEDS" });
        JComboBox<String> temperament = new JComboBox<>(new String[] { "FRIENDLY", "CALM", "SHY", "ACTIVE" });
        JComboBox<String> housing = new JComboBox<>(new String[] { "ANY", "HOUSE", "APARTMENT", "YARD" });
        JComboBox<PetStatus> status = new JComboBox<>(
                new PetStatus[] { PetStatus.AVAILABLE, PetStatus.RESERVED, PetStatus.UNAVAILABLE });
        JCheckBox special = new JCheckBox("Yêu cầu chăm sóc đặc biệt");

        JTextField exercise = new JTextField("30"),
                feeding = new JTextField("Feed twice daily"),
                medical = new JTextField("Routine veterinary review");
        if (existing != null) {
            status.setSelectedItem(existing.getStatus());
            name.setText(existing.getName());
            breed.setText(existing.getBreed());
            age.setText("" + existing.getAgeMonths());
            type.setSelectedItem(existing.getType());
            type.setEnabled(false);
            health.setSelectedItem(existing.getHealthStatus());
            temperament.setSelectedItem(existing.getTemperament());
            housing.setSelectedItem(existing.getHousingRequirement());
            special.setSelected(existing.isSpecialCareRequired());
            var care = existing.getCareProfile();
            if (care != null) {
                exercise.setText("" + care.getDailyExerciseMinutes());
                feeding.setText(care.getFeedingNotes());
                medical.setText(care.getMedicalNotes());
            }
        }
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

        form.add(new JLabel("Trạng thái:"));
        form.add(status);
        form.add(new JLabel("Vận động (phút/ngày):"));
        form.add(exercise);
        form.add(new JLabel("Chế độ ăn:"));
        form.add(feeding);
        form.add(new JLabel("Ghi chú y tế:"));
        form.add(medical);
        if (JOptionPane.showConfirmDialog(
                this, form, "Đăng ký thú cưng mới", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION)
            return;

        try {
            int months = Integer.parseInt(age.getText().trim());
            Pet p = type.getSelectedItem() == PetType.DOG
                    ? new Dog(
                            0,
                            name.getText().trim(),
                            breed.getText().trim(),
                            months,
                            (String) health.getSelectedItem(),
                            (String) temperament.getSelectedItem(),
                            special.isSelected(),
                            (String) housing.getSelectedItem(),
                            PetStatus.AVAILABLE,
                            null)
                    : new Cat(
                            0,
                            name.getText().trim(),
                            breed.getText().trim(),
                            months,
                            (String) health.getSelectedItem(),
                            (String) temperament.getSelectedItem(),
                            special.isSelected(),
                            (String) housing.getSelectedItem(),
                            PetStatus.AVAILABLE,
                            null);

            p.setStatus((PetStatus) status.getSelectedItem());
            if (existing != null) {
                p.setId(existing.getId());
                if (existing.getStatus() == PetStatus.PLACED)
                    throw new IllegalArgumentException("Committed pets cannot be edited.");
            }
            p.setCareProfile(
                    new com.petmatch.model.CareProfile(
                            0,
                            Integer.parseInt(exercise.getText().trim()),
                            feeding.getText(),
                            medical.getText()));
            service.save(p);
            JOptionPane.showMessageDialog(
                    this, "Lưu hồ sơ thú cưng thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            load();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Tuổi của thú cưng phải là một số nguyên dương!",
                    "Lỗi nhập liệu",
                    JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException | ClassCastException ex) {
            UiUtil.error(this, ex);
        } catch (RuntimeException ex) {
            UiUtil.error(this, ex);
        }
    }

    private void deletePet() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(
                    this, "Vui lòng chọn một thú cưng để xóa!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (JOptionPane.showConfirmDialog(
                this, "Bạn có chắc chắn muốn xóa thú cưng này?", "Xác nhận",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
            return;

        try {
            service.delete(((Number) model.getValueAt(row, 0)).longValue());
            JOptionPane.showMessageDialog(
                    this, "Xóa thú cưng thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            load();
        } catch (RuntimeException ex) {
            UiUtil.error(this, ex);
        }
    }
}
