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
    private final DefaultTableModel model = new DefaultTableModel(new Object[] { "ID", "Family", "Contact", "Phone",
            "Housing", "Experience", "Capacity", "Occupied", "Yard", "Active" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    public FamiliesPanel(FosterFamilyService s) {
        this.service = s;
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        add(UiUtil.pageTitle("Foster families", "Manage household conditions and available capacity"),
                BorderLayout.NORTH);
        
        JTable table = new JTable(model);
        table.setRowHeight(28);
        add(new JScrollPane(table), BorderLayout.CENTER);
        
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addBtn = new JButton("Add family");
        JButton refreshBtn = new JButton("Refresh");
        
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
                    f.isHasYard(), f.isActive() 
                });
            }
        } catch (NumberFormatException e) {
            UiUtil.error(this, e);
        } catch (IllegalArgumentException e) {
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
        JCheckBox yard = new JCheckBox("Has yard");
        JCheckBox children = new JCheckBox("Has children");
        
        JPanel p = new JPanel(new GridLayout(0, 2, 8, 8));
        p.add(new JLabel("Family name"));
        p.add(family);
        p.add(new JLabel("Contact name"));
        p.add(contact);
        p.add(new JLabel("Phone"));
        p.add(phone);
        p.add(new JLabel("Housing"));
        p.add(housing);
        p.add(new JLabel("Experience"));
        p.add(experience);
        p.add(new JLabel("Capacity"));
        p.add(capacity);
        p.add(yard);
        p.add(children);
        
        if (JOptionPane.showConfirmDialog(this, p, "Register foster family",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION)
            return;
            
        try {
            service.save(new FosterFamily(
                0, 
                family.getText().trim(), 
                contact.getText().trim(), 
                phone.getText().trim(),
                (HousingType) housing.getSelectedItem(), 
                (ExperienceLevel) experience.getSelectedItem(),
                Integer.parseInt(capacity.getText().trim()), 
                0, 
                yard.isSelected(), 
                children.isSelected(), 
                true
            ));
            load();
        } catch (IllegalArgumentException e) {
            UiUtil.error(this, e);
        }
    }
}