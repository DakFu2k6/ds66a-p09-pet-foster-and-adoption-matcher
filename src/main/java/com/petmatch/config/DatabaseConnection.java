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
                String v = s.substring(i + 1).trim();
                if (v.length() >= 2 && ((v.startsWith("\"") && v.endsWith("\""))
                        || (v.startsWith("'") && v.endsWith("'"))))
                    v = v.substring(1, v.length() - 1);
                map.put(s.substring(0, i).trim(), v);
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
        if (!java.util.Set.of("127.0.0.1", "localhost", "::1").contains(host))
            throw new SQLException("Local-only mode: DB_HOST must be localhost or 127.0.0.1.");
        if (!name.matches("[A-Za-z0-9_]+"))
            throw new SQLException("Invalid DB_NAME.");
        try {
            int n = Integer.parseInt(port);
            if (n < 1 || n > 65535)
                throw new NumberFormatException();
        } catch (NumberFormatException e) {
            throw new SQLException("DB_PORT must be 1..65535.");
        }
        String url = "jdbc:mysql://" + host + ":" + port + "/" + name
                + "?sslMode=DISABLED&allowPublicKeyRetrieval=true&connectTimeout=5000&socketTimeout=15000"
                + "&serverTimezone=Asia/Ho_Chi_Minh&characterEncoding=UTF-8";
        return DriverManager.getConnection(url, user, password);
    }

    private static String value(String envKey, String propKey, String fallback) {
        String fromEnv = System.getenv(envKey);
        if (fromEnv != null)
            return fromEnv;
        String fromFile = DOT_ENV.get(envKey);
        if (fromFile != null)
            return fromFile;
        return PROPERTIES.getProperty(propKey, fallback);
    }
}