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
    private final DefaultTableModel model = new DefaultTableModel(new Object[] { "ID", "Name", "Species", "Breed",
            "Age (months)", "Health", "Special care", "Housing", "Status" }, 0) {
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
        
        add(UiUtil.pageTitle("Pet management", "Register and maintain dog and cat profiles"), BorderLayout.NORTH);
        
        table.setRowHeight(28);
        add(new JScrollPane(table), BorderLayout.CENTER);
        
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addBtn = new JButton("Add pet");
        JButton delBtn = new JButton("Delete selected");
        JButton refreshBtn = new JButton("Refresh");
        
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
                    p.getHealthStatus(), p.isSpecialCareRequired(), p.getHousingRequirement(), p.getStatus() 
                });
            }
        } catch (NumberFormatException ex) {
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
        JCheckBox special = new JCheckBox("Requires special care");
        
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.add(new JLabel("Name"));
        form.add(name);
        form.add(new JLabel("Species"));
        form.add(type);
        form.add(new JLabel("Breed"));
        form.add(breed);
        form.add(new JLabel("Age in months"));
        form.add(age);
        form.add(new JLabel("Health"));
        form.add(health);
        form.add(new JLabel("Temperament"));
        form.add(temperament);
        form.add(new JLabel("Housing requirement"));
        form.add(housing);
        form.add(new JLabel("Special care"));
        form.add(special);
        
        if (JOptionPane.showConfirmDialog(this, form, "Register pet",
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
            load();
        } catch (IllegalArgumentException | ClassCastException ex) {
            UiUtil.error(this, ex);
        }
    }

    private void deletePet() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a pet first.");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Delete selected pet?", "Confirm",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
            return;
            
        try {
            service.delete(((Number) model.getValueAt(row, 0)).longValue());
            load();
        } catch (Exception ex) {
            UiUtil.error(this, ex);
        }
    }
}