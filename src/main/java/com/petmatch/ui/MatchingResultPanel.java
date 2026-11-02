package main.java.com.petmatch.ui;

import main.java.com.petmatch.matching.CriterionResult;
import main.java.com.petmatch.model.*;
import main.java.com.petmatch.repository.*;
import main.java.com.petmatch.service.*;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MatchingResultPanel extends JPanel {
    private final ApplicationRepository apps;
    private final PetRepository pets;
    private final FosterFamilyRepository families;
    private final MatchingService matching;
    private final PlacementService placement;
    private final JComboBox<String> applicationBox = new JComboBox<>();
    private final JComboBox<MatchOperator> operator = new JComboBox<>(MatchOperator.values());
    private final JTextArea output = new JTextArea();

    public MatchingResultPanel(ApplicationRepository a, PetRepository p, FosterFamilyRepository f, MatchingService m,
            PlacementService placement) {
        apps = a;
        pets = p;
        families = f;
        matching = m;
        this.placement = placement;
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        add(UiUtil.pageTitle("Matching & placement", "Evaluate compatibility and safely confirm an adoption"),
                BorderLayout.NORTH);
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Application"));
        top.add(applicationBox);
        top.add(new JLabel("Logic"));
        top.add(operator);
        JButton evaluate = new JButton("Evaluate match"), confirm = new JButton("Confirm placement"),
                refresh = new JButton("Refresh applications");
        for (JButton b : List.of(evaluate, confirm, refresh)) {
            UiUtil.styleButton(b);
            top.add(b);
        }
        add(top, BorderLayout.CENTER);
        output.setEditable(false);
        output.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        output.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        add(new JScrollPane(output), BorderLayout.SOUTH);
        evaluate.addActionListener(e -> evaluate());
        confirm.addActionListener(e -> confirm());
        refresh.addActionListener(e -> load());
        load();
    }

    private void load() {
        try {
            applicationBox.removeAllItems();
            for (AdoptionApplication a : apps.findAll())
                applicationBox.addItem(
                        a.getId() + " | Pet " + a.getPetId() + " | Family " + a.getFamilyId() + " | " + a.getStatus());
        } catch (Exception e) {
            UiUtil.error(this, e);
        }
    }

    private Long selectedId() {
        if (applicationBox.getSelectedItem() == null)
            return null;
        return Long.parseLong(applicationBox.getSelectedItem().toString().split(" \\| ")[0]);
    }

    private void evaluate() {
        try {
            Long id = selectedId();
            if (id == null)
                return;
            AdoptionApplication a = apps.findById(id).orElseThrow();
            Pet pet = pets.findById(a.getPetId()).orElseThrow();
            FosterFamily family = families.findById(a.getFamilyId()).orElseThrow();
            List<CriterionResult> results = matching.evaluate(pet, family);
            MatchOperator op = (MatchOperator) operator.getSelectedItem();
            boolean pass = matching.isPassed(results, op);
            StringBuilder b = new StringBuilder("MATCHING EVALUATION\n===================\n");
            for (CriterionResult r : results)
                b.append(r.passed() ? "[PASS] " : "[FAIL] ").append(r.name()).append("\n       ")
                        .append(r.explanation()).append("\n\n");
            b.append("Operator: ").append(op).append("\nOverall: ").append(pass ? "SUITABLE" : "NOT SUITABLE");
            output.setText(b.toString());
        } catch (Exception e) {
            UiUtil.error(this, e);
        }
    }

    private void confirm() {
        try {
            Long id = selectedId();
            if (id == null)
                return;
            placement.confirm(id);
            output.setText("PLACEMENT CONFIRMED\nThe pet and family have been updated in one database transaction.");
            load();
        } catch (Exception e) {
            UiUtil.error(this, e);
        }
    }
}
