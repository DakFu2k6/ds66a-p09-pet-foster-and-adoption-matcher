package com.petmatch.ui;

import com.petmatch.model.*;
import com.petmatch.repository.*;
import com.petmatch.service.ApplicationService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class ApplicationsPanel extends JPanel {
    private final ApplicationService service;
    private final PetRepository pets;
    private final FosterFamilyRepository families;
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[] { "ID", "Pet ID", "Family ID", "Date", "Status", "Notes" }, 0);

    public ApplicationsPanel(ApplicationService s, PetRepository p, FosterFamilyRepository f) {
        service = s;
        pets = p;
        families = f;
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        add(UiUtil.pageTitle("Adoption applications", "Create and track requests from foster families"),
                BorderLayout.NORTH);
        JTable table = new JTable(model);
        table.setRowHeight(28);
        add(new JScrollPane(table), BorderLayout.CENTER);
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton add = new JButton("New application"), refresh = new JButton("Refresh");
        UiUtil.styleButton(add);
        UiUtil.styleButton(refresh);
        bar.add(add);
        bar.add(refresh);
        add(bar, BorderLayout.SOUTH);
        add.addActionListener(e -> create());
        refresh.addActionListener(e -> load());
        load();
    }

    private void load() {
        try {
            model.setRowCount(0);
            for (AdoptionApplication a : service.findAll())
                model.addRow(new Object[] { a.getId(), a.getPetId(), a.getFamilyId(), a.getApplicationDate(),
                        a.getStatus(), a.getNotes() });
        } catch (Exception e) {
            UiUtil.error(this, e);
        }
    }

    private void create() {
        var petList = pets.findAll();
        var familyList = families.findAll();
        if (petList.isEmpty() || familyList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Create at least one pet and one family first.");
            return;
        }
        JComboBox<String> pet = new JComboBox<>(petList.stream()
                .map(x -> x.getId() + " - " + x.getName() + " (" + x.getStatus() + ")").toArray(String[]::new));
        JComboBox<String> family = new JComboBox<>(
                familyList.stream().map(x -> x.getId() + " - " + x.getFamilyName()).toArray(String[]::new));
        JTextField notes = new JTextField();
        JPanel p = new JPanel(new GridLayout(0, 2, 8, 8));
        p.add(new JLabel("Pet"));
        p.add(pet);
        p.add(new JLabel("Foster family"));
        p.add(family);
        p.add(new JLabel("Notes"));
        p.add(notes);
        if (JOptionPane.showConfirmDialog(this, p, "New adoption application",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION)
            return;
        try {
            long petId = Long.parseLong(pet.getSelectedItem().toString().split(" - ")[0]);
            long familyId = Long.parseLong(family.getSelectedItem().toString().split(" - ")[0]);
            service.create(petId, familyId, notes.getText());
            load();
        } catch (Exception e) {
            UiUtil.error(this, e);
        }
    }
}
