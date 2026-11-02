package main.java.com.petmatch.ui;

import main.java.com.petmatch.repository.*;
import main.java.com.petmatch.service.*;
import javax.swing.*;
import java.awt.*;

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
        MatchingService matching = new MatchingService();
        PlacementService placement = new PlacementService(new JdbcPlacementRepository(), apps, pets, families,
                matching);
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Overview", new DashboardPanel(pets, families, apps));
        tabs.addTab("Pets", new PetsPanel(new PetService(pets)));
        tabs.addTab("Foster Families", new FamiliesPanel(new FosterFamilyService(families)));
        tabs.addTab("Applications",
                new ApplicationsPanel(new ApplicationService(apps, pets, families), pets, families));
        tabs.addTab("Matching & Placement", new MatchingResultPanel(apps, pets, families, matching, placement));
        add(tabs, BorderLayout.CENTER);
        JLabel footer = new JLabel("  PETMATCH  •  Java 17 / Swing / PostgreSQL");
        footer.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        footer.setForeground(new Color(100, 115, 106));
        add(footer, BorderLayout.SOUTH);
    }
}
