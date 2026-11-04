package com.petmatch.ui;

import com.petmatch.repository.ApplicationRepository;
import com.petmatch.repository.FosterFamilyRepository;
import com.petmatch.repository.JdbcApplicationRepository;
import com.petmatch.repository.JdbcFosterFamilyRepository;
import com.petmatch.repository.JdbcPetRepository;
import com.petmatch.repository.JdbcPlacementRepository;
import com.petmatch.repository.PetRepository;
import com.petmatch.repository.PlacementRepository;
import com.petmatch.service.ApplicationService;
import com.petmatch.service.FosterFamilyService;
import com.petmatch.service.MatchingService;
import com.petmatch.service.PetService;
import com.petmatch.service.PlacementService;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;

public class MainFrame extends JFrame {

    public MainFrame() {
        this("Staff");
    }

    public MainFrame(String staffName) {
        setTitle("PetMatch | Hệ Thống Quản Lý Nhận Nuôi Thú Cưng (Project G9)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1180, 760);
        setMinimumSize(new Dimension(1150, 700));
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(245, 248, 246));

        PetRepository pets = new JdbcPetRepository();
        FosterFamilyRepository families = new JdbcFosterFamilyRepository();
        ApplicationRepository apps = new JdbcApplicationRepository();
        PlacementRepository placements = new JdbcPlacementRepository();

        PetService petService = new PetService(pets);
        FosterFamilyService familyService = new FosterFamilyService(families);
        ApplicationService applicationService = new ApplicationService(apps);
        MatchingService matchingService = new MatchingService(com.petmatch.repository.JdbcCriteriaRepository.load());
        PlacementService placementService = new PlacementService(placements);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Tổng quan", new DashboardPanel(pets, families, apps));
        tabs.addTab("Thú cưng", new PetsPanel(petService));
        tabs.addTab("Gia đình nhận nuôi", new FamiliesPanel(familyService));
        tabs.addTab("Đơn đăng ký", new ApplicationsPanel(applicationService, pets, families));
        tabs.addTab(
                "Ghép nối & Bàn giao",
                new MatchingResultPanel(apps, pets, families, matchingService, placementService));

        tabs.setTabPlacement(JTabbedPane.LEFT);
        tabs.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 14));
        tabs.addChangeListener(e -> refreshPanel(tabs.getSelectedComponent()));
        add(tabs, BorderLayout.CENTER);
        javax.swing.JPanel header = new javax.swing.JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));
        header.setBackground(new Color(22, 67, 59));
        JLabel brand = new JLabel("PetMatch  /  Staff workspace");
        brand.setForeground(Color.WHITE);
        brand.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 22));
        header.add(brand);
        javax.swing.JButton logout = new javax.swing.JButton("Sign out · " + staffName);
        logout.addActionListener(
                e -> {
                    dispose();
                    new LoginFrame().setVisible(true);
                });
        header.add(logout, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);
        UiUtil.polish(tabs);

        JLabel footer = new JLabel("  PETMATCH  •  Java 17 / Swing / MySQL 8.4");
        footer.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        footer.setForeground(new Color(100, 115, 106));
        add(footer, BorderLayout.SOUTH);
    }

    private void refreshPanel(java.awt.Component panel) {
        if (panel instanceof DashboardPanel p)
            p.load();
        if (panel instanceof PetsPanel p)
            p.load();
        if (panel instanceof FamiliesPanel p)
            p.load();
        if (panel instanceof ApplicationsPanel p)
            p.loadApplicationsData();
        if (panel instanceof MatchingResultPanel p)
            p.load();
    }
}
