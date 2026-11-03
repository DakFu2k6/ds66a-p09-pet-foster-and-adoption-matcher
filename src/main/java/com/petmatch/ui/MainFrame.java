package com.petmatch.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;

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

public class MainFrame extends JFrame {
    
    public MainFrame() {
        setTitle("PetMatch | Foster & Adoption Manager");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1180, 760);
        setMinimumSize(new Dimension(950, 650));
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(245, 248, 246));

        PetRepository pets = new JdbcPetRepository();
        FosterFamilyRepository families = new JdbcFosterFamilyRepository();
        ApplicationRepository apps = new JdbcApplicationRepository();
        PlacementRepository placements = new JdbcPlacementRepository();

        PetService petService = new PetService(pets);
        FosterFamilyService familyService = new FosterFamilyService(families);
        ApplicationService applicationService = new ApplicationService(apps);
        MatchingService matchingService = new MatchingService();
        PlacementService placementService = new PlacementService(placements);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Overview", new DashboardPanel(pets, families, apps));
        tabs.addTab("Pets", new PetsPanel(petService));
        tabs.addTab("Foster Families", new FamiliesPanel(familyService));
        tabs.addTab("Applications", new ApplicationsPanel(applicationService));
        tabs.addTab("Matching & Placement", new MatchingResultPanel(apps, pets, families, matchingService, placementService));

        add(tabs, BorderLayout.CENTER);

        JLabel footer = new JLabel("  PETMATCH  •  Java 17 / Swing / MySQL");
        footer.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        footer.setForeground(new Color(100, 115, 106));
        add(footer, BorderLayout.SOUTH);
    }
}