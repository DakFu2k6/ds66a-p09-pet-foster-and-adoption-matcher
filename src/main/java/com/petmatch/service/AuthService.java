package com.petmatch.service;

import com.petmatch.config.DatabaseConnection;
import java.sql.*;

public final class AuthService {
    public String login(String username, char[] password) {
        try (var c = DatabaseConnection.getConnection()) {
            c.setAutoCommit(false);
            try (var q = c.prepareStatement("SELECT * FROM staff_users WHERE username=? FOR UPDATE")) {
                q.setString(1, username.trim());
                try (var r = q.executeQuery()) {
                    if (!r.next()) {
                        c.rollback();
                        return null;
                    }
                    Timestamp lock = r.getTimestamp("locked_until");
                    if (!r.getBoolean("is_active")
                            || (lock != null && lock.toInstant().isAfter(java.time.Instant.now()))) {
                        c.rollback();
                        return null;
                    }
                    boolean ok = PasswordHasher.verify(password, r.getString("password_hash"));
                    try (var u = c.prepareStatement(
                            ok
                                    ? "UPDATE staff_users SET failed_attempts=0,locked_until=NULL WHERE user_id=?"
                                    : "UPDATE staff_users SET"
                                            + " locked_until=IF(failed_attempts>=4,DATE_ADD(NOW(),INTERVAL 5"
                                            + " MINUTE),NULL),failed_attempts=failed_attempts+1 WHERE user_id=?")) {
                        u.setLong(1, r.getLong("user_id"));
                        u.executeUpdate();
                    }
                    String display = ok ? r.getString("display_name") : null;
                    c.commit();
                    return display;
                }
            } catch (SQLException | RuntimeException ex) {
                c.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Cannot sign in. Check database setup.", ex);
        } finally {
            java.util.Arrays.fill(password, '\0');
        }
    }

    public static void main(String[] args) throws Exception {
        var console = System.console();
        if (console == null)
            throw new IllegalStateException(
                    "Run scripts/create_admin.py instead, or use a real terminal.");
        String user = console.readLine("Username: ");
        char[] pass = console.readPassword("Password (12+ characters): ");
        try {
            if (pass.length < 12 || !user.matches("[A-Za-z0-9_.-]{3,50}"))
                throw new IllegalArgumentException("Invalid username/password");
            try (var c = DatabaseConnection.getConnection();
                    var q = c.prepareStatement(
                            "INSERT INTO staff_users(username,display_name,password_hash) VALUES(?,?,?)")) {
                q.setString(1, user);
                q.setString(2, user);
                q.setString(3, PasswordHasher.hash(pass));
                q.executeUpdate();
            }
        } finally {
            java.util.Arrays.fill(pass, '\0');
        }
    }
}
