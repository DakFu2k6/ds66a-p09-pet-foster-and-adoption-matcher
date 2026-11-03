package com.petmatch.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.petmatch.repository.ApplicationRepository;
import com.petmatch.repository.FosterFamilyRepository;
import com.petmatch.repository.PetRepository;

public class DashboardPanel extends JPanel {
    private final PetRepository pets;
    private final FosterFamilyRepository families;
    private final ApplicationRepository apps;
    private final JLabel summary = new JLabel();

    public DashboardPanel(PetRepository p, FosterFamilyRepository f, ApplicationRepository a) {
        this.pets = p;
        this.families = f;
        this.apps = a;
        
        setLayout(new BorderLayout(16, 16));
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        
        add(UiUtil.pageTitle("TỔNG QUAN HỆ THỐNG", "Theo dõi nhanh số liệu và tiến trình nhận nuôi"), BorderLayout.NORTH);
        
        summary.setFont(new Font("SansSerif", Font.PLAIN, 18));
        summary.setVerticalAlignment(SwingConstants.TOP);
        add(summary, BorderLayout.CENTER);
        
        JButton refresh = new JButton("Làm mới dữ liệu");
        UiUtil.styleButton(refresh);
        refresh.addActionListener(e -> load());
        
        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT));
        south.add(refresh);
        add(south, BorderLayout.SOUTH);
        
        load();
    }

    private void load() {
        try {
            long available = pets.findAll().stream().filter(p -> p.getStatus().name().equals("AVAILABLE")).count();
            int familyCount = families.findAll().size();
            int appCount = apps.findAll().size();
            
            summary.setText("<html><div style='font-size:16px;color:#62736a'>TÓM TẮT DỮ LIỆU TRỰC TIẾP (LIVE SUMMARY)</div><br>"
                    + "<b>Thú cưng đang chờ nhận nuôi:</b> " + available + "<br><br>"
                    + "<b>Gia đình Foster đã đăng ký:</b> " + familyCount + "<br><br>"
                    + "<b>Tổng số đơn đăng ký nhận nuôi:</b> " + appCount + "<br><br>"
                    + "<span style='color:#2a705e'>Bước tiếp theo:</span> Tạo đơn đăng ký mới, đánh giá độ phù hợp và xác nhận bàn giao.</html>");
        } catch (Exception ex) {
            summary.setText("Không thể tải dữ liệu tổng quan: " + ex.getMessage());
        }
    }
}