package com.petmatch.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public final class DatabaseConnection {
    private static final Properties PROPERTIES = loadProperties();
    private static final Map<String, String> DOT_ENV = loadDotEnv();

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

    private static Map<String, String> loadDotEnv() {
        Map<String, String> map = new HashMap<>();
        Path file = Path.of(".env");
        if (!Files.isRegularFile(file))
            return map;
        try {
            for (String line : Files.readAllLines(file)) {
                String s = line.trim();
                if (s.isEmpty() || s.startsWith("#") || !s.contains("="))
                    continue;
                int i = s.indexOf('=');
                map.put(s.substring(0, i).trim(), s.substring(i + 1).trim());
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Cannot read .env", ex);
        }
        return map;
    }

    public static Connection getConnection() throws SQLException {
        String host = value("DB_HOST", "db.host", "127.0.0.1");
        String port = value("DB_PORT", "db.port", "3306");
        String name = value("DB_NAME", "db.name", "pet_matcher_db");
        String user = value("DB_USER", "db.username", "root");
        String password = value("DB_PASSWORD", "db.password", "");
        String url = "jdbc:mysql://" + host + ":" + port + "/" + name
                + "?useSSL=false&allowPublicKeyRetrieval=true"
                + "&serverTimezone=Asia/Ho_Chi_Minh&characterEncoding=UTF-8";
        return DriverManager.getConnection(url, user, password);
    }

    private static String value(String envKey, String propKey, String fallback) {
        String fromEnv = System.getenv(envKey);
        if (fromEnv != null && !fromEnv.isBlank())
            return fromEnv;
        String fromFile = DOT_ENV.get(envKey);
        if (fromFile != null && !fromFile.isBlank())
            return fromFile;
        return PROPERTIES.getProperty(propKey, fallback);
    }
}