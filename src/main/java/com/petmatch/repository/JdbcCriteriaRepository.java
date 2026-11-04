package com.petmatch.repository;

import com.petmatch.config.DatabaseConnection;
import com.petmatch.matching.*;
import java.sql.*;
import java.util.*;

public final class JdbcCriteriaRepository {
    public static List<MatchCriterion> load(Connection c) throws SQLException {
        List<MatchCriterion> rules = new ArrayList<>();
        try (var q = c.prepareStatement("SELECT code FROM match_criteria ORDER BY sort_order,code");
                var rs = q.executeQuery()) {
            while (rs.next())
                rules.add(
                        switch (rs.getString(1)) {
                            case "HOUSING" -> new HousingCriterion();
                            case "EXPERIENCE" -> new ExperienceCriterion();
                            case "HOUSEHOLD" -> new HouseholdCriterion();
                            default ->
                                throw new SQLException("Unsupported persisted criterion: " + rs.getString(1));
                        });
        }
        if (rules.size() < 3)
            throw new SQLException("At least three care criteria must be configured.");
        return rules;
    }

    public static List<MatchCriterion> load() {
        try (var c = DatabaseConnection.getConnection()) {
            return load(c);
        } catch (SQLException ex) {
            throw new IllegalStateException(
                    "Cannot load matching criteria. Run database/05_auth_and_rules.sql", ex);
        }
    }
}
