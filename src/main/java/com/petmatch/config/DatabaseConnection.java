package com.petmatch.config;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DatabaseConnection {
    private static final Properties PROPERTIES = loadProperties();

    private DatabaseConnection() {
    }

    private static Properties loadProperties() {
        Properties p = new Properties();
        try (InputStream in = DatabaseConnection.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (in != null)
                p.load(in);
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot load application.properties", ex);
        }
        return p;
    }

    public static Connection getConnection() throws SQLException {
        String url = value("PETMATCH_DB_URL", "db.url",
                "jdbc:postgresql://localhost:5432/pet_matcher_db");
        String user = value("PETMATCH_DB_USER", "db.username", "postgres");
        String password = value("PETMATCH_DB_PASSWORD", "db.password", "");
        return DriverManager.getConnection(url, user, password);
    }

    private static String value(String env, String key, String fallback) {
        String fromEnv = System.getenv(env);
        if (fromEnv != null && !fromEnv.isBlank())
            return fromEnv;
        return PROPERTIES.getProperty(key, fallback);
    }
}
