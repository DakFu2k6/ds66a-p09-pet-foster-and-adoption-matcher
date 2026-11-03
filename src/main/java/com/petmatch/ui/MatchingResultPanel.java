package com.petmatch.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.petmatch.matching.CriterionResult;
import com.petmatch.model.AdoptionApplication;
import com.petmatch.model.FosterFamily;
import com.petmatch.model.MatchOperator;
import com.petmatch.model.Pet;
import com.petmatch.repository.ApplicationRepository;
import com.petmatch.repository.FosterFamilyRepository;
import com.petmatch.repository.PetRepository;
import com.petmatch.service.MatchingService;
import com.petmatch.service.PlacementService;

public class MatchingResultPanel extends JPanel {
    private final ApplicationRepository apps;
    private final PetRepository pets;
    private final FosterFamilyRepository families;
    private final MatchingService matching;
    private final PlacementService placement;
    
    private final JComboBox<String> applicationBox = new JComboBox<>();
    private final JComboBox<MatchOperator> operator = new JComboBox<>(MatchOperator.values());
    private final JTextArea output = new JTextArea();

    public MatchingResultPanel(ApplicationRepository apps, PetRepository pets, FosterFamilyRepository families, MatchingService matching,
            PlacementService placement) {
        this.apps = apps;
        this.pets = pets;
        this.families = families;
        this.matching = matching;
        this.placement = placement;
        
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        add(UiUtil.pageTitle("QUẢN LÝ GHÉP NỐI & BÀN GIAO", "Đánh giá mức độ phù hợp và xác nhận bàn giao an toàn"),
                BorderLayout.NORTH);
                
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Chọn Đơn:"));
        top.add(applicationBox);
        top.add(new JLabel("Tiêu chí (Logic):"));
        top.add(operator);
        
        JButton evaluate = new JButton("Đánh giá độ phù hợp");
        JButton confirm = new JButton("Xác nhận bàn giao");
        JButton refresh = new JButton("Làm mới danh sách");
        
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
            for (AdoptionApplication a : apps.findAll()) {
                applicationBox.addItem(a.getId() + " - Trạng thái: " + a.getStatus());
            }
        } catch (RuntimeException e) {
            UiUtil.error(this, e);
        }
    }

    private Integer selectedId() {
        Object selected = applicationBox.getSelectedItem();
        if (selected == null)
            return null;
        try {
            String text = selected.toString().split(" - ")[0].trim();
            return Integer.valueOf(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void evaluate() {
        try {
            Integer id = selectedId();
            if (id == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một đơn đăng ký!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
                
            AdoptionApplication app = apps.findById(id.longValue())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn đăng ký"));
            
            Pet pet = app.getPet();
            if (pet == null) {
                pet = pets.findAll().stream().findFirst().orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin Thú cưng"));
            }
            
            FosterFamily family = app.getFamily();
            if (family == null) {
                family = families.findAll().stream().findFirst().orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin Gia đình nhận nuôi"));
            }
            
            MatchOperator op = (MatchOperator) operator.getSelectedItem();
            
            CriterionResult result = matching.evaluateMatch(pet, family, op);
            
            StringBuilder b = new StringBuilder("MATCHING EVALUATION (ĐÁNH GIÁ GHÉP NỐI)\n=========================================\n");
            b.append("Mã Đơn: ").append(app.getId())
             .append(" | Pet ID: ").append(pet.getId())
             .append(" | Family ID: ").append(family.getId()).append("\n");
            b.append("Quy tắc Logic: ").append(op).append("\n");
            b.append("Kết quả chung: ").append(result.isSatisfied() ? "[ĐẠT - PASS]" : "[TỪ CHỐI - FAIL]").append("\n\n");
            b.append("Giải thích chi tiết từ hệ thống:\n").append(result.getReason()).append("\n");
            
            output.setText(b.toString());
        } catch (RuntimeException e) {
            UiUtil.error(this, e);
        }
    }

    private void confirm() {
        try {
            Integer id = selectedId();
            if (id == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một đơn đăng ký!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
                
            AdoptionApplication app = apps.findById(id.longValue())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn đăng ký"));
            
            Pet pet = app.getPet();
            if (pet == null) {
                pet = pets.findAll().stream().findFirst().orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin Thú cưng"));
            }
            
            FosterFamily family = app.getFamily();
            if (family == null) {
                family = families.findAll().stream().findFirst().orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin Gia đình nhận nuôi"));
            }
            
            placement.createPlacement(id.longValue(), pet, family);
            
            output.setText(String.format("""
                PLACEMENT CONFIRMED (ĐÃ BÀN GIAO THÀNH CÔNG!)
                =========================================
                Thú cưng (ID: %d) và Gia đình (ID: %d) đã được bàn giao an toàn!
                """, pet.getId(), family.getId()));
            load();
        } catch (RuntimeException e) {
            UiUtil.error(this, e);
        }
    }
}