package com.petmatch.config;

import java.sql.*;

public final class DatabaseDiagnostics {
    public static String explain(Throwable error) {
        for (Throwable e = error; e != null; e = e.getCause()) {
            if (e instanceof SQLException s) {
                String hint = switch (s.getErrorCode()) {
                    case 1045 ->
                        "Access denied: check DB_USER / DB_PASSWORD and MySQL account host grants.";
                    case 1049 ->
                        "Database missing: run database/01_create_database.sql and 02_schema.sql.";
                    case 1146 -> "Table missing: run scripts/init_db.py for all tables.";
                    default ->
                        s.getSQLState() != null && s.getSQLState().startsWith("08")
                                ? "Cannot reach MySQL: check MySQL84 service, DB_PORT and 127.0.0.1."
                                : "Check .env in project root and MySQL schema. Environment variables"
                                        + " override .env.";
                };
                return hint + "\nSQLState=" + s.getSQLState() + "; code=" + s.getErrorCode();
            }
        }
        return error.getMessage();
    }

    public static void main(String[] args) {
        try (Connection c = DatabaseConnection.getConnection();
                Statement s = c.createStatement();
                ResultSet rs = s.executeQuery("SELECT VERSION(), DATABASE()")) {
            rs.next();
            System.out.println("Connected: MySQL " + rs.getString(1) + ", database=" + rs.getString(2));
            for (String table : new String[] {
                    "pets",
                    "care_profiles",
                    "foster_families",
                    "applications",
                    "placements",
                    "matching_results",
                    "matching_criteria_results",
                    "staff_users",
                    "match_criteria"
            }) {
                try (Statement check = c.createStatement()) {
                    check.executeQuery("SELECT 1 FROM " + table + " LIMIT 1").close();
                }
            }
            System.out.println("All 9 required tables are accessible.");
        } catch (Exception e) {
            System.err.println(explain(e));
            System.exit(1);
        }
    }
}
