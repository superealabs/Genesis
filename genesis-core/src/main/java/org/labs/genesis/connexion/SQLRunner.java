package org.labs.genesis.connexion;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

public class SQLRunner {
    private static final List<String> CONTROL_FLOW_STARTERS = Arrays.asList("IF", "BEGIN", "CASE");

    /**
     * Executes a SQL script across any JDBC-compatible database.
     *
     * @param connex The JDBC connection object (assumes autoCommit=false).
     * @param query  The SQL script to execute.
     * @throws Exception If execution or transaction fails.
     */
    public static void execute(Connection connex, String query) throws Exception {
        if (connex == null) {
            throw new Exception("\nConnection is null\n");
        }

        String[] queries = query.split(";"); // Split script into individual statements

        try (Statement statement = connex.createStatement()) {
            for (String singleQuery : queries) {
                singleQuery = singleQuery.trim();
                if (!singleQuery.isEmpty()) {
                    if (isControlFlowStatement(singleQuery)) {
                        // Execute control-of-flow statements individually
                        statement.execute(singleQuery);
                    } else {
                        // Add non-control-of-flow statements to the batch
                        statement.addBatch(singleQuery);
                    }
                }
            }

            // Execute batched statements
            statement.executeBatch();
            connex.commit();
        } catch (SQLException e) {
            connex.rollback();
            throw formatException(e);
        }
    }

    /**
     * Checks if the query starts with a control-of-flow keyword.
     *
     * @param query The SQL query string.
     * @return True if it's a control-of-flow statement, false otherwise.
     */
    private static boolean isControlFlowStatement(String query) {
        String upperQuery = query.trim().toUpperCase();
        return CONTROL_FLOW_STARTERS.stream().anyMatch(upperQuery::startsWith);
    }

    /**
     * Formats a SQLException for better readability.
     *
     * @param e The SQLException.
     * @return A formatted exception to throw.
     */
    private static Exception formatException(SQLException e) {
        String[] errorMessages = e.getMessage().split("\\.");
        String formattedMessage = String.join(".\n", errorMessages);
        return new Exception("Batch execution failed: " + formattedMessage, e);
    }
}
