package org.labs.genesis.connexion.providers;

import org.labs.genesis.connexion.Credentials;
import org.labs.genesis.connexion.Database;
import org.labs.genesis.connexion.model.ColumnMetadata;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MySQLDatabase extends Database {

    @Override
    public String getJdbcUrl(Credentials credentials) {
        String port;
        if (credentials.getPort() != null)
            port = credentials.getPort();
        else port = getPort();
        return String.format("jdbc:mysql://%s:%s/%s?user=%s&password=%s&useSSL=%s&allowPublicKeyRetrieval=%s",
                credentials.getHost(),
                port,
                credentials.getDatabaseName(),
                credentials.getUser(),
                credentials.getPwd(),
                credentials.isUseSSL(),
                credentials.isAllowPublicKeyRetrieval());
    }


    @Override
    public List<String> getAllTableNames(Connection connection) throws SQLException {
        List<String> tableNames = new ArrayList<>();

        try (Statement statement = connection.createStatement();
             ResultSet tables = statement.executeQuery("SHOW FULL TABLES WHERE Table_type = 'BASE TABLE'")) {
            while (tables.next()) {
                String tableName = tables.getString(1);
                tableNames.add(tableName);
            }
        }

        return tableNames;
    }

    @Override
    public List<String> getAllViewNames(Connection connection) throws SQLException {
        List<String> viewNames = new ArrayList<>();
        DatabaseMetaData metaData = connection.getMetaData();

        try (Statement statement = connection.createStatement();
             ResultSet views = statement.executeQuery("SHOW FULL TABLES WHERE Table_type = 'VIEW'")) {
            while (views.next()) {
                String viewName = views.getString(1);
                viewNames.add(viewName);
            }
        }

        return viewNames;
    }

    private static final String STRICT_NUMERIC_CONSTRAINT_QUERY =
            """
                WITH parsed_constraints AS ( 
                    SELECT
                    CONSTRAINT_SCHEMA AS database_name,
                    TABLE_NAME,
                    CONSTRAINT_NAME,
                    CHECK_CLAUSE,
                    REPLACE(REPLACE(CHECK_CLAUSE, '(', ''), ')', '') AS cleaned_clause
                    FROM information_schema.CHECK_CONSTRAINTS
                    WHERE
                    (%s)
                ),\s
                matches AS (\s
                    SELECT
                    database_name,
                    TABLE_NAME,
                    CONSTRAINT_NAME,
                    CHECK_CLAUSE,
                    cleaned_clause,
                    REGEXP_SUBSTR(cleaned_clause, %s) AS match_direct,
                    REGEXP_SUBSTR(cleaned_clause, '`(\\\\w+)`') AS col_direct,
                    REGEXP_SUBSTR(cleaned_clause, '([0-9]+(?:\\\\.[0-9]+)?)') AS val_direct,
                    REGEXP_SUBSTR(cleaned_clause, '`(\\\\w+)`$') AS col_inverse,
                    REGEXP_SUBSTR(cleaned_clause, '^([0-9]+(?:\\\\.[0-9]+)?)') AS val_inverse\s
                FROM parsed_constraints)
                SELECT
                    database_name,
                    TABLE_NAME,
                    COALESCE(TRIM(BOTH '`' FROM col_direct), TRIM(BOTH '`' FROM col_inverse)) AS column_name,
                    COALESCE(val_direct, val_inverse) + 0 AS %s\s
                FROM matches
                WHERE table_name = ? and database_name = ?\s
                    and (match_direct IS NOT NULL)
            """;

    @Override
    protected void checkStrictMinConstraint(Connection connex, String tableName, List<ColumnMetadata> columns) throws SQLException {
        String sql = String.format(
                STRICT_NUMERIC_CONSTRAINT_QUERY,
                // WHERE
                " (CHECK_CLAUSE LIKE '%>%' AND CHECK_CLAUSE NOT LIKE '%>=%')  ",
                // match_direct
                "'`(\\\\w+)`\\\\s*>\\\\s*([0-9]+(?:\\\\.[0-9]+)?)'",
                // alias final
                "min_value"
        );

        try (PreparedStatement stmt = connex.prepareStatement(sql)) {
            stmt.setString(1, tableName);
            stmt.setString(2, this.getCredentials().getDatabaseName());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String columnName = rs.getString("column_name");
                    String value = rs.getString("min_value");

                    for (ColumnMetadata col : columns) {
                        if (col.getReferencedColumn().equalsIgnoreCase(columnName) && col.isNumeric()) {
                            col.setHasStrictMinimumConstraint(true);
                            col.setStrictMinimumConstraint(value);
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void checkStrictMaxConstraint(Connection connex, String tableName, List<ColumnMetadata> columns) throws SQLException {
        String sql = String.format(
                STRICT_NUMERIC_CONSTRAINT_QUERY,
                // WHERE
                " CHECK_CLAUSE REGEXP '`\\\\w+`\\\\s*<\\\\s*[0-9]+(?:\\\\.[0-9]+)?' OR CHECK_CLAUSE REGEXP '[0-9]+(?:\\\\.[0-9]+)?\\\\s*>\\\\s*`\\\\w+`' ",
                // match_direct
                "'`(\\\\w+)`\\\\s*<\\\\s*([0-9]+(?:\\\\.[0-9]+)?)'",
                // alias final
                "max_value"
        );

        try (PreparedStatement stmt = connex.prepareStatement(sql)) {
            stmt.setString(1, tableName);
            stmt.setString(2, this.getCredentials().getDatabaseName());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String columnName = rs.getString("column_name");
                    String value = rs.getString("max_value");

                    for (ColumnMetadata col : columns) {
                        if (col.getReferencedColumn().equalsIgnoreCase(columnName) && col.isNumeric()) {
                            col.setHasStrictMaximumConstraint(true);
                            col.setStrictMaximumConstraint(value);
                            break;
                        }
                    }
                }
            }
        }
    }

    private static final String MINIMUM_NUMERIC_CONSTRAINT_QUERY = """
                WITH parsed_constraints AS (
                    SELECT
                        CONSTRAINT_SCHEMA AS database_name,
                        TABLE_NAME,
                        CONSTRAINT_NAME,
                        CHECK_CLAUSE,
                        REPLACE(REPLACE(CHECK_CLAUSE, '(', ''), ')', '') AS cleaned_clause
                    FROM information_schema.CHECK_CONSTRAINTS
                    WHERE
                        -- Inclure seulement celles avec >=, <= ou between
                        (CHECK_CLAUSE LIKE '%>=%' OR CHECK_CLAUSE LIKE '%between%')
                        -- MAIS exclure les contraintes qui contiennent AUSSI une borne basse strictement >
                        AND CHECK_CLAUSE NOT REGEXP '^\\\\s*[0-9]+\\\\s*>='
                ),
                matches AS (
                    SELECT
                        database_name,
                        TABLE_NAME,
                        CONSTRAINT_NAME,
                        CHECK_CLAUSE,
                        cleaned_clause,
                
                        -- >=
                        REGEXP_SUBSTR(cleaned_clause, '`(\\\\w+)`\\\\s*>=\\\\s*([0-9]+(?:\\\\.[0-9]+)?)') AS match_gte,
                        -- BETWEEN
                        REGEXP_SUBSTR(cleaned_clause, '`(\\\\w+)`\\\\s+between\\\\s+[0-9]+(?:\\\\.[0-9]+)?\\\\s+and\\\\s+[0-9]+(?:\\\\.[0-9]+)?') AS match_between,
                
                        -- colonnes et valeurs
                        REGEXP_SUBSTR(cleaned_clause, '`(\\\\w+)`') AS col_gte,
                        REGEXP_SUBSTR(cleaned_clause, '([0-9]+(?:\\\\.[0-9]+)?)') AS val_gte,
                
                        -- BETWEEN
                        CASE\s
                            WHEN cleaned_clause REGEXP '`(\\\\w+)`\\\\s+between\\\\s+([0-9.]+)\\\\s+and\\\\s+([0-9.]+)' THEN
                                TRIM(BOTH '`' FROM REGEXP_SUBSTR(cleaned_clause, '`(\\\\w+)`'))
                        END AS col_between,
                        CASE\s
                            WHEN cleaned_clause REGEXP 'between\\\\s+([0-9.]+)\\\\s+and' THEN
                                REGEXP_SUBSTR(cleaned_clause, '(?<=between\\\\s)[0-9]+(?:\\\\.[0-9]+)?')
                        END AS val_between_min
                from parsed_constraints
                )
                SELECT
                    database_name,
                    TABLE_NAME,
                    COALESCE(col_between, TRIM(BOTH '`' FROM col_gte)) AS column_name,
                    COALESCE(val_between_min, val_gte) + 0 AS min_value
                FROM matches
                WHERE table_name = ? and database_name = ? 
                and (match_gte IS NOT NULL OR match_between IS NOT NULL);
    """;

    @Override
    protected void checkMinConstraint(Connection connex, String tableName, List<ColumnMetadata> columns) throws SQLException {
        String sql = MINIMUM_NUMERIC_CONSTRAINT_QUERY;

        try (PreparedStatement stmt = connex.prepareStatement(sql)) {
            stmt.setString(1, tableName);
            stmt.setString(2, this.getCredentials().getDatabaseName());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String columnName = rs.getString("column_name");
                    String value = rs.getString("min_value");

                    for (ColumnMetadata col : columns) {
                        if (col.getReferencedColumn().equalsIgnoreCase(columnName) && col.isNumeric()) {
                            col.setHasMinimumConstraint(true);
                            col.setMinimumConstraint(value);
                            break;
                        }
                    }
                }
            }
        }
    }

    private static final String MAXIMUM_NUMERIC_CONSTRAINT_QUERY =
            """
                    WITH parsed_constraints AS (
                        SELECT
                            CONSTRAINT_SCHEMA AS database_name,
                            TABLE_NAME,
                            CONSTRAINT_NAME,
                            CHECK_CLAUSE,
                            REPLACE(REPLACE(CHECK_CLAUSE, '(', ''), ')', '') AS cleaned_clause
                        FROM information_schema.CHECK_CONSTRAINTS
                        WHERE
                            -- Inclure seulement celles avec <= ou between
                            (CHECK_CLAUSE LIKE '%<=%' OR CHECK_CLAUSE LIKE '%between%')
                            -- MAIS exclure les contraintes qui contiennent AUSSI une borne haute stricte <
                            AND CHECK_CLAUSE NOT REGEXP '`\\\\w+`\\\\s*<\\\\s*[0-9]'
                    ),
                    matches AS (
                        SELECT
                            database_name,
                            TABLE_NAME,
                            CONSTRAINT_NAME,
                            CHECK_CLAUSE,
                            cleaned_clause,
                    
                            -- <= (forme directe)
                            REGEXP_SUBSTR(cleaned_clause, '`(\\\\w+)`\\\\s*<=\\\\s*([0-9]+(?:\\\\.[0-9]+)?)') AS match_lte,
                    
                            -- BETWEEN
                            REGEXP_SUBSTR(cleaned_clause, '`(\\\\w+)`\\\\s+between\\\\s+[0-9]+(?:\\\\.[0-9]+)?\\\\s+and\\\\s+[0-9]+(?:\\\\.[0-9]+)?') AS match_between,
                    
                            -- colonnes et valeurs
                            REGEXP_SUBSTR(cleaned_clause, '`(\\\\w+)`') AS col_lte,
                            REGEXP_REPLACE(cleaned_clause, '.*`\\\\w+`\\\\s*<=\\\\s*([0-9]+(?:\\\\.[0-9]+)?).*', '\\\\1') AS val_lte,
                    
                            -- BETWEEN
                            CASE\s
                                WHEN cleaned_clause REGEXP '`(\\\\w+)`\\\\s+between\\\\s+([0-9.]+)\\\\s+and\\\\s+([0-9.]+)' THEN
                                    TRIM(BOTH '`' FROM REGEXP_SUBSTR(cleaned_clause, '`(\\\\w+)`'))
                            END AS col_between,
                            CASE\s
                                WHEN cleaned_clause REGEXP 'between\\\\s+[0-9.]+\\\\s+and\\\\s+([0-9.]+)' THEN
                                    REGEXP_SUBSTR(cleaned_clause, '(?<=and\\\\s)[0-9]+(?:\\\\.[0-9]+)?')
                            END AS val_between_max
                        FROM parsed_constraints
                    )
                    SELECT
                        database_name,
                        TABLE_NAME,
                        COALESCE(col_between, TRIM(BOTH '`' FROM col_lte)) AS column_name,
                        COALESCE(val_between_max, val_lte) + 0 AS max_value
                    FROM matches
                    WHERE table_name = ? and database_name = ? 
                    and (match_lte IS NOT NULL OR match_between IS NOT NULL);
            """;

    @Override
    protected void checkMaxConstraint(Connection connex, String tableName, List<ColumnMetadata> columns) throws SQLException {
        String sql = MAXIMUM_NUMERIC_CONSTRAINT_QUERY;

        try (PreparedStatement stmt = connex.prepareStatement(sql)) {
            stmt.setString(1, tableName);
            stmt.setString(2, this.getCredentials().getDatabaseName());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String columnName = rs.getString("column_name");
                    String value = rs.getString("max_value");

                    for (ColumnMetadata col : columns) {
                        if (col.getReferencedColumn().equalsIgnoreCase(columnName) && col.isNumeric()) {
                            col.setHasMaximumConstraint(true);
                            col.setMaximumConstraint(value);
                            break;
                        }
                    }
                }
            }
        }
    }

    private static final String TRIM_NOT_BLANK_SQL = """
        with cc1 as (SELECT
            cc.CONSTRAINT_NAME,
            cc.CONSTRAINT_SCHEMA,
            cc.TABLE_NAME,
            cc.CHECK_CLAUSE,
            CASE
                -- cas : trim(colonne) <> ''
                WHEN cc.CHECK_CLAUSE REGEXP 'trim\\\\(\\\\s*`?([a-zA-Z0-9_]+)`?\\\\s*\\\\)\\\\s*<>\\\\s*''''|''''\\\\s*<>\\\\s*trim\\\\(\\\\s*`?([a-zA-Z0-9_]+)`?\\\\s*\\\\)'
                    THEN
                        CASE
                            WHEN cc.CHECK_CLAUSE LIKE 'trim(%' THEN
                                TRIM(BOTH '`' FROM SUBSTRING_INDEX(SUBSTRING_INDEX(cc.CHECK_CLAUSE, 'trim(', -1), ')', 1))
                            ELSE
                                TRIM(BOTH '`' FROM SUBSTRING_INDEX(SUBSTRING_INDEX(cc.CHECK_CLAUSE, '<>', -1), ')', 1))
                        END
                -- cas : colonne <> ''
                WHEN cc.CHECK_CLAUSE REGEXP '`?([a-zA-Z0-9_]+)`?\\\\s*<>\\\\s*''''|''''\\\\s*<>\\\\s*`?([a-zA-Z0-9_]+)`?'
                    THEN
                        CASE
                            WHEN cc.CHECK_CLAUSE LIKE "'' <>%" THEN
                                TRIM(BOTH ' `' FROM SUBSTRING_INDEX(cc.CHECK_CLAUSE, '>', -1))
                            ELSE
                                TRIM(BOTH ' `' FROM SUBSTRING_INDEX(cc.CHECK_CLAUSE, '<>', 1))
                        END
                ELSE NULL
            END AS column_name
        FROM information_schema.check_constraints cc
        WHERE cc.CHECK_CLAUSE REGEXP '(trim\\\\(\\\\s*`?[a-zA-Z0-9_]+`?\\\\s*\\\\)\\\\s*<>\\\\s*''''|''''\\\\s*<>\\\\s*trim\\\\(\\\\s*`?[a-zA-Z0-9_]+`?\\\\)|`?[a-zA-Z0-9_]+`?\\\\s*<>\\\\s*''''|''''\\\\s*<>\\\\s*`?[a-zA-Z0-9_]+`?)'),
        cc as (
        SELECT
            cc.CONSTRAINT_NAME,
            cc.CONSTRAINT_SCHEMA,
            cc.TABLE_NAME,
            cc.CHECK_CLAUSE,
            trim(REPLACE(REPLACE(cc.column_name, '`', ''), '`', ''))AS column_name
            from cc1 cc)
            select * from cc
        WHERE table_name = ? and CONSTRAINT_SCHEMA = ? 
        and cc.CHECK_CLAUSE REGEXP '(trim\\\\(\\\\s*`?[a-zA-Z0-9_]+`?\\\\s*\\\\)\\\\s*<>\\\\s*''''|''''\\\\s*<>\\\\s*trim\\\\(\\\\s*`?[a-zA-Z0-9_]+`?\\\\)|`?[a-zA-Z0-9_]+`?\\\\s*<>\\\\s*''''|''''\\\\s*<>\\\\s*`?[a-zA-Z0-9_]+`?)'
        ORDER BY cc.TABLE_NAME, column_name;
    """;

    protected void checkNotBlankConstraint(Connection connex, String tableName, List<ColumnMetadata> columns) throws SQLException {
        try (PreparedStatement stmt = connex.prepareStatement(TRIM_NOT_BLANK_SQL)) {
            stmt.setString(1, tableName);
            stmt.setString(2, this.getCredentials().getDatabaseName());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String colName = rs.getString("column_name");
                    for (ColumnMetadata col : columns) {
                        if (col.getReferencedColumn().equalsIgnoreCase(colName) && col.isText()) {
                            col.setHasNotBlankConstraint(true);
                            break;
                        }
                    }
                }
            }
        }
    }

    private static final String MIN_LENGTH_SQL = """
        SELECT
            table_name
            constraint_name,
            check_clause,
            REPLACE(REGEXP_SUBSTR(check_clause, '`(\\\\w+)`'), '`', '') AS column_name,
            REGEXP_SUBSTR(check_clause, '(>=|>)') AS operator,
            CAST(REGEXP_SUBSTR(check_clause, '\\\\d+$') AS UNSIGNED) AS min_length
        FROM information_schema.check_constraints
        WHERE table_name = ? and CONSTRAINT_SCHEMA = ?
        and check_clause REGEXP 'char_length\\\\s*\\\\(.*\\\\)\\\\s*(>=|>)\\\\s*\\\\d+';
    """;

    protected void checkMinLengthConstraint(Connection connex, String tableName, List<ColumnMetadata> columns) throws SQLException {
        try (PreparedStatement stmt = connex.prepareStatement(MIN_LENGTH_SQL)) {
            stmt.setString(1, tableName);
            stmt.setString(2, this.getCredentials().getDatabaseName());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String colName = rs.getString("column_name");
                    String minLength = rs.getString("min_length");
                    for (ColumnMetadata col : columns) {
                        if (col.getReferencedColumn().equalsIgnoreCase(colName) && col.isText()) {
                            col.setHasMinimumLengthConstraint(true);
                            col.setMinimumLengthConstraint(minLength);
                            break;
                        }
                    }
                }
            }
        }
    }
}
