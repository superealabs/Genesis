package org.labs.genesis.connexion.providers;

import org.labs.genesis.config.langage.Framework;
import org.labs.genesis.config.langage.Language;
import org.labs.genesis.connexion.Credentials;
import org.labs.genesis.connexion.Database;
import org.labs.genesis.connexion.model.ColumnMetadata;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.labs.utils.StringUtils.toCamelCase;

public class OracleDatabase extends Database {

    @Override
    public List<ColumnMetadata> fetchColumns(DatabaseMetaData metaData, String tableName, Language language,Connection connex,Framework framework) throws SQLException {
        List<ColumnMetadata> listeCols = new ArrayList<>();

        String schema = this.getCredentials().getSchemaName();
        schema = (schema != null && schema.trim().isEmpty()) ? null : schema;

        try (ResultSet columns = metaData.getColumns(null, schema, tableName, null)) {
            Map<String, Object> frameworkValidationAnnotations = framework.getModel().getValidationAnnotations();
            while (columns.next()) {
                ColumnMetadata column = new ColumnMetadata();
                String columnName = columns.getString("COLUMN_NAME");
                String columnType = columns.getString("TYPE_NAME");

                String isNullable = columns.getString("IS_NULLABLE");

                int decimalDigits = columns.getInt("DECIMAL_DIGITS");
                int columnSize = columns.getInt("COLUMN_SIZE");
                String defaultValue = columns.getString("COLUMN_DEF");
                boolean isColumnNumeric = isColumnNumeric(columns);
                boolean isColumnNumericWithPrecision = isColumnNumericWithPrecision(columns);
                boolean isColumnText = isColumnText(columns);
                boolean isColumnDate = isColumnDate(columns);

                column.setName(toCamelCase(columnName.toLowerCase()));
                column.setReferencedColumn(columnName);
                column.setNumeric(isColumnNumeric);
                column.setNumericWithPrecision(isColumnNumericWithPrecision);
                column.setText(isColumnText);
                column.setDate(isColumnDate);

                column.setNullable(isNullable,frameworkValidationAnnotations,engine);
                column.setDefaultValue(defaultValue,frameworkValidationAnnotations,engine);
                column.setColumnSize(columnSize,frameworkValidationAnnotations,engine);
                column.setDecimalDigits(decimalDigits,frameworkValidationAnnotations,engine);

                if (language.getTypes().get(getDatabaseType(columns)) == null)
                    throw new RuntimeException("Database type not supported yet : " + columnType);
                else
                    column.setType(language.getTypes().get(getDatabaseType(columns)));

                column.setColumnType(columnType);
                listeCols.add(column);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        try {
            checkUnique(metaData, tableName, listeCols,framework);
            checkStrictMinConstraint(connex, tableName, listeCols,framework);
            checkMinConstraint(connex, tableName, listeCols,framework);
            checkStrictMaxConstraint(connex, tableName, listeCols,framework);
            checkMaxConstraint(connex, tableName, listeCols,framework);
            checkStrictPastDateConstraint(connex, tableName, listeCols,framework);
            checkPastDateConstraint(connex, tableName, listeCols,framework);
            checkStrictFutureDateConstraint(connex, tableName, listeCols,framework);
            checkFutureDateConstraint(connex, tableName, listeCols,framework);
            checkNotBlankConstraint(connex, tableName, listeCols,framework);
            checkMinLengthConstraint(connex, tableName, listeCols,framework);
            checkRegexConstraint(connex, tableName, listeCols,framework);
            removeUnusedData(listeCols);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return listeCols;
    }

    @Override
    public List<String> getAllTableTypeNames(Connection connection, String tableType) throws SQLException {
        List<String> tableNames = new ArrayList<>();

        String sql = "SELECT tname FROM tab WHERE tabtype = ? AND tname NOT LIKE 'BIN$%'";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, tableType.toUpperCase()); // Oracle stocke généralement en majuscules
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tableNames.add(rs.getString("tname"));
                }
            }
        }
        return tableNames;
    }

    @Override
    public Connection getConnection(Credentials credentials) throws ClassNotFoundException, SQLException {
        setCredentials(credentials);
        Class.forName(getDriver());
        String url = getJdbcUrl(credentials);
        Connection connection = DriverManager.getConnection(url, credentials.getUser(), credentials.getPwd());
        connection.setAutoCommit(false);
        return connection;
    }

    @Override
    public Connection getConnection(Credentials credentials, String url) throws ClassNotFoundException, SQLException {
        setCredentials(credentials);
        Class.forName(getDriver());
        Connection connection = DriverManager.getConnection(url, credentials.getUser(), credentials.getPwd());
        connection.setAutoCommit(false);
        return connection;
    }

    @Override
    public String getJdbcUrl(Credentials credentials) {
        String port;
        if (credentials.getPort() != null)
            port = credentials.getPort();
        else port = getPort();
        return String.format("jdbc:oracle:%s:@//%s:%s/%s",
                getDriverType(),
                credentials.getHost(),
                port,
                credentials.getSID());
    }
}
