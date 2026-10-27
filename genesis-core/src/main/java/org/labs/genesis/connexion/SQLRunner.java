package org.labs.genesis.connexion;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.regex.Pattern;

public class SQLRunner {
    private static final List<String> CONTROL_FLOW_STARTERS = Arrays.asList("IF", "BEGIN", "CASE");

    private static boolean isCreateViewStatement(String query) {
        String upperQuery = query.trim().toUpperCase();
        return upperQuery.startsWith("CREATE VIEW")
                || upperQuery.contains("CREATE OR ALTER VIEW")
                || upperQuery.contains("CREATE OR REPLACE VIEW");
    }

    public static String removeComments(String sql) {
        if (sql == null || sql.isBlank()) {
            return sql;
        }

        // Supprime les commentaires multilignes /* ... */
        sql = Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL)
                .matcher(sql)
                .replaceAll("");

        // Supprime les commentaires sur une ligne -- ...
        sql = Pattern.compile("(?m)--.*$")
                .matcher(sql)
                .replaceAll("");

        return sql.trim();
    }

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

        query = removeComments(query);
        List<String> viewQueries = new ArrayList<>();

        String[] queries = query.split(";"); // Split script into individual statements

        try (Statement statement = connex.createStatement()) {
            for (String singleQuery : queries) {
                singleQuery = singleQuery.trim();
                if (!singleQuery.isEmpty()) {
                    if (isCreateViewStatement(singleQuery)) {
                        viewQueries.add(singleQuery);
                    } else if (isControlFlowStatement(singleQuery)) {
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

            for (String viewQuery : viewQueries) {
                statement.execute(viewQuery);
            }

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

    /**
     * Extrait les noms des tables depuis le script SQL (utilisé pour peupler l'UI).
     */
    public static List<String> extractTableNames(String sql) {
        List<String> tables = new ArrayList<>();
        if (sql == null || sql.isBlank())
            return tables;

        String clean = removeComments(sql);
        for (String query : clean.split(";")) {
            String trimmed = query.trim();
            String upper = trimmed.toUpperCase();
            if (upper.startsWith("CREATE TABLE") || upper.contains("CREATE TABLE")) {
                int idx = upper.indexOf("TABLE");
                if (idx != -1) {
                    String afterTable = trimmed.substring(idx + 5).trim();
                    if (afterTable.toUpperCase().startsWith("IF NOT EXISTS")) {
                        afterTable = afterTable.substring(13).trim();
                    }
                    String[] tokens = afterTable.split("[\\s(]+");
                    if (tokens.length > 0 && !tokens[0].isBlank()) {
                        String name = tokens[0].replaceAll("[\"``\\[\\]]", "").trim();
                        if (name.contains(".")) {
                            name = name.substring(name.lastIndexOf('.') + 1);
                        }
                        if (!name.isBlank() && !tables.contains(name)) {
                            tables.add(name);
                        }
                    }
                }
            }
        }
        return tables;
    }

    /**
     * Extrait les noms des vues depuis le script SQL (utilisé pour peupler l'UI).
     */
    public static List<String> extractViewNames(String sql) {
        List<String> views = new ArrayList<>();
        if (sql == null || sql.isBlank())
            return views;

        String clean = removeComments(sql);
        for (String query : clean.split(";")) {
            String trimmed = query.trim();
            if (isCreateViewStatement(trimmed)) {
                String upper = trimmed.toUpperCase();
                int idx = upper.indexOf("VIEW");
                if (idx != -1) {
                    String afterView = trimmed.substring(idx + 4).trim();
                    String[] tokens = afterView.split("[\\s(]+");
                    if (tokens.length > 0 && !tokens[0].isBlank()) {
                        String name = tokens[0].replaceAll("[\"``\\[\\]]", "").trim();
                        if (name.contains(".")) {
                            name = name.substring(name.lastIndexOf('.') + 1);
                        }
                        if (!name.isBlank() && !views.contains(name)) {
                            views.add(name);
                        }
                    }
                }
            }
        }
        return views;
    }

    /**
     * Découpe le corps d'un CREATE TABLE (entre parenthèses) en clauses
     * individuelles
     * en respectant les parenthèses imbriquées (ex: DECIMAL(10,2), CHECK(...)).
     */
    public static List<String> splitClauses(String body) {
        List<String> clauses = new ArrayList<>();
        if (body == null || body.isBlank())
            return clauses;

        StringBuilder current = new StringBuilder();
        int depth = 0;

        for (char c : body.toCharArray()) {
            if (c == '(') {
                depth++;
                current.append(c);
            } else if (c == ')') {
                depth--;
                current.append(c);
            } else if (c == ',' && depth == 0) {
                String clause = current.toString().trim();
                if (!clause.isEmpty())
                    clauses.add(clause);
                current.setLength(0);
            } else {
                current.append(c);
            }
        }

        String last = current.toString().trim();
        if (!last.isEmpty())
            clauses.add(last);

        return clauses;
    }
}
