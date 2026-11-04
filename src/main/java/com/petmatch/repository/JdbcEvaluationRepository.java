package com.petmatch.repository;

import com.petmatch.config.DatabaseConnection;
import com.petmatch.matching.*;
import com.petmatch.model.*;
import com.petmatch.service.MatchingService;
import java.sql.*;

public final class JdbcEvaluationRepository {
    public static void save(Connection c, long id, MatchOperator op, Pet pet, FosterFamily family)
            throws SQLException {
        var result = new MatchingService(JdbcCriteriaRepository.load(c)).evaluateMatch(pet, family, op);
        long key;
        try (var q = c.prepareStatement(
                "INSERT INTO matching_results(application_id,logic_operator,overall_pass,summary)"
                        + " VALUES(?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS)) {
            q.setLong(1, id);
            q.setString(2, op.name());
            q.setBoolean(3, result.isSatisfied());
            q.setString(4, result.getReason());
            q.executeUpdate();
            try (var rs = q.getGeneratedKeys()) {
                if (!rs.next())
                    throw new SQLException("Missing evaluation ID");
                key = rs.getLong(1);
            }
        }
        for (MatchCriterion rule : JdbcCriteriaRepository.load(c)) {
            var detail = rule.evaluate(pet, family);
            try (var q = c.prepareStatement(
                    "INSERT INTO"
                            + " matching_criteria_results(matching_result_id,criterion_name,passed,explanation)"
                            + " VALUES(?,?,?,?)")) {
                q.setLong(1, key);
                q.setString(2, rule.getClass().getSimpleName());
                q.setBoolean(3, detail.isSatisfied());
                q.setString(4, detail.getReason());
                q.executeUpdate();
            }
        }
    }

    public static String latest(long id) {
        try (var c = DatabaseConnection.getConnection();
                var q = c.prepareStatement(
                        "SELECT logic_operator,summary,evaluated_at FROM matching_results WHERE"
                                + " application_id=? ORDER BY matching_result_id DESC LIMIT 1")) {
            q.setLong(1, id);
            try (var rs = q.executeQuery()) {
                return rs.next()
                        ? "Saved evaluation · "
                                + rs.getTimestamp(3)
                                + " · "
                                + rs.getString(1)
                                + "\n"
                                + rs.getString(2)
                        : "No saved evaluation. Select AND/OR and evaluate.";
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Cannot reload evaluation", ex);
        }
    }

    public static void record(long id, MatchOperator op, Pet p, FosterFamily f) {
        try (var c = DatabaseConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                save(c, id, op, p, f);
                c.commit();
            } catch (SQLException | RuntimeException ex) {
                c.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Cannot save evaluation", ex);
        }
    }
}
