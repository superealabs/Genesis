package org.example.siteback.utils;


import org.postgresql.util.PGobject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.commons.text.StringEscapeUtils;

public class ConstraintDisabler {

  public enum DbType { POSTGRESQL, MYSQL, SQLSERVER, ORACLE }
  
  private final JdbcTemplate jdbc;
  private final DbType dbType;
  private final Deque<String> restoreDdls = new LinkedList<>();
  private final Set<String> processedTables = new HashSet<>();

  public ConstraintDisabler(JdbcTemplate jdbc) throws SQLException {
    this.jdbc = jdbc;
    this.dbType = detectDatabaseType();
  }

  private DbType detectDatabaseType() throws SQLException {
    DataSource dataSource = jdbc.getDataSource();
    Connection conn = DataSourceUtils.getConnection(dataSource);
    try {
      String product = conn.getMetaData().getDatabaseProductName();
      if (product.contains("PostgreSQL")) return DbType.POSTGRESQL;
      else if (product.contains("MySQL")) return DbType.MYSQL;
      else if (product.contains("Microsoft SQL Server")) return DbType.SQLSERVER;
      else if (product.contains("Oracle")) return DbType.ORACLE;
      else throw new UnsupportedOperationException("SGBD non pris en charge: " + product);
    } finally {
      DataSourceUtils.releaseConnection(conn, dataSource);
    }
  }

  public void startTransaction() {
    jdbc.execute("SET CONSTRAINTS ALL DEFERRED");
  }

  public void disableConstraintsForTable(String tableName) {
    if (processedTables.contains(tableName)) return;
    processedTables.add(tableName);
    
    switch (dbType) {
      case POSTGRESQL -> disablePostgres(tableName);
      case MYSQL -> disableMySql(tableName);
      case SQLSERVER -> disableSqlServer(tableName);
      case ORACLE -> disableOracle(tableName);
    }
  }

  public void restoreConstraints() {
    System.out.println("Restoring " + restoreDdls.size() + " constraints");
    
    while (!restoreDdls.isEmpty()) {
      String ddl = restoreDdls.pop();
      try {
        System.out.println("Executing: " + ddl);
        jdbc.execute(ddl);
      } catch (DataAccessException e) {
        System.err.println("Error restoring constraint: " + ddl);
        System.err.println("Reason: " + e.getCause().getMessage());
      }
    }
    processedTables.clear();
  }

  private String safeGetString(Map<String, Object> row, String key) {
      Object value = row.get(key);
      
      if (value instanceof PGobject) {
          return ((PGobject) value).getValue();
      }
      
      return value != null ? value.toString() : null;
  }

  private void disablePostgres(String tableName) {
      // 1. Désactiver les contraintes référencées (FK pointant vers cette table)
      jdbc.queryForList(
        "SELECT conname, conrelid::regclass::text AS source_table, " +
                "pg_get_constraintdef(oid) AS definition " +
                "FROM pg_constraint " +
                "WHERE confrelid = to_regclass(?)",
        tableName
      ).forEach(con -> {
        String constraintName = safeGetString(con, "conname");
        String sourceTable = safeGetString(con, "source_table");
        String definition = safeGetString(con, "definition");
        String restoreSQL = String.format(
          "ALTER TABLE %s ADD CONSTRAINT %s %s",
          sourceTable, constraintName, definition
        );

        restoreDdls.push(restoreSQL);
        jdbc.execute(String.format(
        "ALTER TABLE %s DROP CONSTRAINT %s CASCADE",
        sourceTable, constraintName
        ));
      });

      // 2. Désactiver les contraintes locales
      jdbc.queryForList(
      "SELECT conname, pg_get_constraintdef(oid) AS def " +
      "FROM pg_constraint " +
      "WHERE conrelid = to_regclass(?)",
      tableName
      ).forEach(row -> {
        String name = safeGetString(row, "conname");
        String def = safeGetString(row, "def");
        
        restoreDdls.push("ALTER TABLE " + tableName + " ADD CONSTRAINT " + name + " " + def);
        jdbc.execute("ALTER TABLE " + tableName + " DROP CONSTRAINT " + name + " CASCADE");
      });

      // 3. Gérer les contraintes NOT NULL (sauf colonnes d'identité)
      jdbc.queryForList(
      "SELECT a.attname AS column_name " +
      "FROM pg_attribute a " +
      "JOIN pg_class c ON c.oid = a.attrelid " +
      "WHERE c.relname = ? " +
      "  AND a.attnotnull " +
      "  AND a.attidentity = '' " +  // Exclure les colonnes d'identité
      "  AND a.attnum > 0 " +
      "  AND NOT a.attisdropped",
      tableName
      ).forEach(col -> {
        String colName = safeGetString(col, "column_name");
        restoreDdls.push("ALTER TABLE " + tableName + " ALTER COLUMN " + colName + " SET NOT NULL");
        jdbc.execute("ALTER TABLE " + tableName + " ALTER COLUMN " + colName + " DROP NOT NULL");
      });
  }

  private void disableMySql(String tableName) {
    String schema = jdbc.queryForObject("SELECT DATABASE()", String.class);

    // FOREIGN KEYS
    jdbc.queryForList(
      "SELECT constraint_name, column_name, referenced_table_name, referenced_column_name " +
      "FROM information_schema.key_column_usage " +
      "WHERE table_schema = ? AND table_name = ? AND referenced_table_name IS NOT NULL",
      schema, tableName
    ).forEach(fk -> {
      String name = safeGetString(fk, "constraint_name");
      String col = safeGetString(fk, "column_name");
      String refTable = safeGetString(fk, "referenced_table_name");
      String refCol = safeGetString(fk, "referenced_column_name");

      String restoreSQL = "ALTER TABLE " + tableName +
      " ADD CONSTRAINT `" + name + "`" +
      " FOREIGN KEY(`" + col + "`) REFERENCES `" +
      refTable + "`(`" + refCol + "`)";

      restoreDdls.push(restoreSQL);
      jdbc.execute("ALTER TABLE " + tableName + " DROP FOREIGN KEY `" + name + "`");
    });

    // Contraintes UNIQUE - Version corrigée
    jdbc.queryForList(
      "SELECT DISTINCT tc.constraint_name " +
      "FROM information_schema.table_constraints tc " +
      "WHERE tc.table_schema = ? " +
      "  AND tc.table_name = ? " +
      "  AND tc.constraint_type = 'UNIQUE' " +
      "  AND tc.constraint_name != 'PRIMARY'",
      schema, tableName
    ).forEach(unique -> {
      Map<String, Object> row = (Map<String, Object>) unique;
      String name = safeGetString(row, "constraint_name");

      try {
        // Récupérer toutes les colonnes de la contrainte UNIQUE
          List<String> columns = jdbc.queryForList(
          "SELECT column_name " +
          "FROM information_schema.key_column_usage " +
          "WHERE constraint_schema = ? " +
          "  AND table_name = ? " +
          "  AND constraint_name = ? " +
          "ORDER BY ordinal_position",
          String.class, schema, tableName, name
          );

          String columnsStr = columns.stream()
          .map(col -> "`" + col + "`")
          .collect(Collectors.joining(","));

          // Créer la commande de restauration
          String restoreSQL = "ALTER TABLE " + tableName +
          " ADD CONSTRAINT `" + name + "`" +
          " UNIQUE (" + columnsStr + ")";
          
          restoreDdls.push(restoreSQL);
          
          // Supprimer la contrainte UNIQUE
          jdbc.execute("ALTER TABLE " + tableName + " DROP INDEX `" + name + "`");
          System.out.println("Dropped UNIQUE constraint: " + name);

        } catch (DataAccessException e) {
          System.err.println("Failed to drop UNIQUE constraint: " + name);
          System.err.println("Reason: " + e.getCause().getMessage());
        }
      });

      // Contraintes CHECK
      disableMySqlCheckConstraints(tableName, schema);

      // Récupère les colonnes avec valeur par défaut explicite (sauf auto_increment)
      jdbc.queryForList(
        "SELECT column_name, column_type, data_type, column_default, extra " +
        "FROM information_schema.columns " +
        "WHERE table_schema = ? AND table_name = ? " +
        "AND column_default IS NOT NULL", // Colonnes avec DEFAULT explicite
        schema, tableName
      ).forEach(col -> {
        String colName = safeGetString(col, "column_name");
        String typeDef = safeGetString(col, "column_type");
        String dataType = safeGetString(col, "data_type");
        String defaultValue = safeGetString(col, "column_default");
        String extra = safeGetString(col, "extra");
        
        // Exclure les colonnes auto_increment
        if (extra == null || !extra.contains("auto_increment")) {
          // Gestion spéciale de CURDATE() et autres fonctions
          String defaultValueExpr = formatDefaultValueMySql(defaultValue);
          
          // Enregistre la commande de restauration
          String restoreSQL = "ALTER TABLE " + tableName + " ALTER COLUMN `" + colName + "` SET DEFAULT " + defaultValueExpr;
          restoreDdls.push(restoreSQL);
        }
      });

      // NOT NULL (sauf colonnes AUTO_INCREMENT)
      jdbc.queryForList(
        "SELECT column_name, column_type, extra " +
        "FROM information_schema.columns " +
        "WHERE table_schema = ? AND table_name = ? AND is_nullable = 'NO'",
        schema, tableName                                                                                                                                            
      ).forEach(col -> {
        String colName = safeGetString(col, "column_name");
        String typeDef = safeGetString(col, "column_type");
        String extra = safeGetString(col, "extra");

        if (extra == null || !extra.contains("auto_increment")) {
          String modifySQL = "ALTER TABLE " + tableName + " MODIFY `" + colName + "` " + typeDef;
          jdbc.execute(modifySQL);
          restoreDdls.push(modifySQL + " NOT NULL");
        }
    });
  }

  private String formatDefaultValueMySql(String defaultValue) {
    if (defaultValue == null) return "";

    // Convertir CURDATE() en CURRENT_DATE
    if ("CURDATE()".equalsIgnoreCase(defaultValue)) {
      return "(CURRENT_DATE)";
    }
    // Convertir les autres fonctions courantes si nécessaire
    else if ("CURTIME()".equalsIgnoreCase(defaultValue)) {
      return "(CURRENT_TIME)";
    }
    else if ("NOW()".equalsIgnoreCase(defaultValue) || "CURRENT_TIMESTAMP()".equalsIgnoreCase(defaultValue)) {
      return "(CURRENT_TIMESTAMP)";
    }
    // Conserver les expressions spéciales entre parenthèses
    else if (defaultValue.startsWith("(") && defaultValue.endsWith(")")) {
      return defaultValue; // e.g. (UUID()), (RAND() * 100)
    }
    // Chaînes de caractères (déjà correctement échappées par MySQL)
    else {
      return defaultValue;
    }
  }

  private void disableMySqlCheckConstraints(String tableName, String schema) {
    // 1. Contraintes CHECK au niveau TABLE et niveau COLONNE
    jdbc.queryForList(
      "SELECT cc.constraint_name, cc.check_clause " +
      "FROM information_schema.table_constraints tc " +
      "JOIN information_schema.check_constraints cc " +
      "  ON tc.constraint_schema = cc.constraint_schema " +
      " AND tc.constraint_name = cc.constraint_name " +
      "WHERE tc.table_schema = ? " +
      "  AND tc.table_name = ? " +
      "  AND tc.constraint_type = 'CHECK' ",
      schema, tableName
    ).forEach(ch -> {
      String name = safeGetString(ch, "constraint_name");
      String clause = safeGetString(ch, "check_clause");
      clause = StringEscapeUtils.unescapeJava(clause);
      clause = clause.replaceAll("_utf8mb4'", "'"); // remplace \' par '

      if (clause.toLowerCase().contains("regexp_like")) {
          clause = "(" + clause + ")";
      }

      try {
        jdbc.execute("ALTER TABLE " + tableName + " DROP CONSTRAINT `" + name + "`");
        restoreDdls.push("ALTER TABLE " + tableName + " ADD CONSTRAINT `" + name + "` CHECK " + clause);
      } catch (DataAccessException e) {
        System.err.println("Failed to drop table-level CHECK constraint: " + name);
      }
    });
  }
    private void disableSqlServer(String tableName) {
        Logger logger = LoggerFactory.getLogger(ConstraintDisabler.class);
        String schema = "dbo";

        String qualifiedTableName = String.format("[%s].[%s]", schema, tableName);

        try {
            // Begin transaction
            jdbc.execute("BEGIN TRANSACTION");

            // 1. UNIQUE Constraints
            jdbc.queryForList(
                "SELECT i.name AS constraint_name, " +
                "STRING_AGG(QUOTENAME(COL_NAME(ic.object_id, ic.column_id)), ',') AS columns " +
                "FROM sys.indexes i " +
                "JOIN sys.index_columns ic ON i.object_id = ic.object_id AND i.index_id = ic.index_id " +
                "JOIN sys.tables t ON i.object_id = t.object_id " +
                "JOIN sys.schemas s ON t.schema_id = s.schema_id " +
                "WHERE i.is_unique = 1 AND i.is_primary_key = 0 " +
                "AND t.name = ? AND s.name = ? " +
                "GROUP BY i.name",
                tableName,
                schema
            ).forEach(unique -> {
                String name = safeGetString(unique, "constraint_name");
                String columns = safeGetString(unique, "columns");
                if (name == null || columns == null) {
                    logger.warn(
                        "Skipping invalid UNIQUE constraint for table {}.{}: name={}, columns={}",
                        schema, tableName, name, columns
                    );
                    return;
                }

                String restoreSQL = String.format(
                    "ALTER TABLE %s ADD CONSTRAINT [%s] UNIQUE ([%s])",
                    qualifiedTableName, name, columns.replace(",", "],[")
                );
                restoreDdls.push(restoreSQL);
                try {
                    jdbc.execute(
                        String.format("ALTER TABLE %s DROP CONSTRAINT [%s]", qualifiedTableName, name)
                    );
                    logger.debug("Dropped UNIQUE constraint [{}] on table {}.{}", name, schema, tableName);
                } catch (DataAccessException e) {
                    logger.error(
                        "Failed to drop UNIQUE constraint [{}] on table {}.{}: {}",
                        name, schema, tableName, e.getCause().getMessage()
                    );
                    throw e;
                }
            });

            // 2. CHECK Constraints
            jdbc.queryForList(
                "SELECT cc.name AS name, cc.definition AS def " +
                "FROM sys.check_constraints cc " +
                "JOIN sys.objects t ON cc.parent_object_id = t.object_id " +
                "JOIN sys.schemas s ON t.schema_id = s.schema_id " +
                "WHERE t.name = ? AND s.name = ?",
                tableName,
                schema
            ).forEach(ch -> {
                String name = safeGetString(ch, "name");
                String def = safeGetString(ch, "def");
                if (name == null || def == null) {
                    logger.warn(
                        "Skipping invalid CHECK constraint for table {}.{}: name={}, def={}",
                        schema, tableName, name, def
                    );
                    return;
                }

                String restoreSQL = String.format(
                    "ALTER TABLE %s ADD CONSTRAINT [%s] CHECK (%s)",
                    qualifiedTableName, name, def
                );
                restoreDdls.push(restoreSQL);
                try {
                    jdbc.execute(
                        String.format("ALTER TABLE %s DROP CONSTRAINT [%s]", qualifiedTableName, name)
                    );
                    logger.debug("Dropped CHECK constraint [{}] on table {}.{}", name, schema, tableName);
                } catch (DataAccessException e) {
                    logger.error(
                        "Failed to drop CHECK constraint [{}] on table {}.{}: {}",
                        name, schema, tableName, e.getCause().getMessage()
                    );
                    throw e;
                }
            });

            // 3. FOREIGN KEY Constraints
            jdbc.queryForList(
                "SELECT fk.name AS name, " +
                "COL_NAME(fkc.parent_object_id, fkc.parent_column_id) AS col, " +
                "OBJECT_NAME(fk.referenced_object_id) AS refTable, " +
                "COL_NAME(fkc.referenced_object_id, fkc.referenced_column_id) AS refCol " +
                "FROM sys.foreign_keys fk " +
                "JOIN sys.foreign_key_columns fkc ON fk.object_id = fkc.constraint_object_id " +
                "JOIN sys.tables t ON fk.parent_object_id = t.object_id " +
                "JOIN sys.schemas s ON t.schema_id = s.schema_id " +
                "WHERE t.name = ? AND s.name = ?",
                tableName,
                schema
            ).forEach(fk -> {
                String name = safeGetString(fk, "name");
                String col = safeGetString(fk, "col");
                String refTable = safeGetString(fk, "refTable");
                String refCol = safeGetString(fk, "refCol");
                if (name == null || col == null || refTable == null || refCol == null) {
                    logger.warn(
                        "Skipping invalid FOREIGN KEY constraint for table {}.{}: name={}, col={}, refTable={}, refCol={}",
                        schema, tableName, name, col, refTable, refCol
                    );
                    return;
                }

                String restoreSQL = String.format(
                    "ALTER TABLE %s ADD CONSTRAINT [%s] FOREIGN KEY ([%s]) REFERENCES [%s]([%s])",
                    qualifiedTableName, name, col, refTable, refCol
                );
                restoreDdls.push(restoreSQL);
                try {
                    jdbc.execute(
                        String.format("ALTER TABLE %s DROP CONSTRAINT [%s]", qualifiedTableName, name)
                    );
                    logger.debug("Dropped FOREIGN KEY constraint [{}] on table {}.{}", name, schema, tableName);
                } catch (DataAccessException e) {
                    logger.error(
                        "Failed to drop FOREIGN KEY constraint [{}] on table {}.{}: {}",
                        name, schema, tableName, e.getCause().getMessage()
                    );
                    throw e;
                }
            });

            // 4. NOT NULL Constraints (excluding IDENTITY columns)
            jdbc.queryForList(
                "SELECT " +
                "    c.name, " +
                "    t.name AS type_name, " +
                "    CASE " +
                "        WHEN t.name IN ('decimal', 'numeric') " +
                "            THEN t.name + '(' + CAST(c.precision AS VARCHAR) + ',' + CAST(c.scale AS VARCHAR) + ')' " +
                "        WHEN t.name IN ('varchar', 'char', 'varbinary', 'binary') " +
                "            THEN t.name + '(' + CAST(c.max_length AS VARCHAR) + ')' " +
                "        WHEN t.name IN ('nvarchar', 'nchar') " +
                "            THEN t.name + '(' + CAST(c.max_length / 2 AS VARCHAR) + ')' " +
                "        ELSE t.name " +
                "    END AS def " +
                "FROM sys.columns c " +
                "JOIN sys.types t ON c.user_type_id = t.user_type_id " +
                "WHERE c.object_id = OBJECT_ID(?) " +
                "  AND c.is_nullable = 0 " +
                "  AND c.is_identity = 0",
                qualifiedTableName
            ).forEach(col -> {
                String colName = safeGetString(col, "name");
                String def = safeGetString(col, "def");
                if (colName == null || def == null) {
                    logger.warn(
                        "Skipping invalid NOT NULL column for table {}.{}: name={}, def={}",
                        schema, tableName, colName, def
                    );
                    return;
                }

                String restoreSQL = String.format(
                    "ALTER TABLE %s ALTER COLUMN [%s] %s NOT NULL",
                    qualifiedTableName, colName, def
                );
                restoreDdls.push(restoreSQL);
                try {
                    jdbc.execute(
                        String.format(
                            "ALTER TABLE %s ALTER COLUMN [%s] %s NULL",
                            qualifiedTableName, colName, def
                        )
                    );
                    logger.debug(
                        "Dropped NOT NULL constraint on column [{}] in table {}.{}",
                        colName, schema, tableName
                    );
                } catch (DataAccessException e) {
                    logger.error(
                        "Failed to drop NOT NULL constraint on column [{}] in table {}.{}: {}",
                        colName, schema, tableName, e.getCause().getMessage()
                    );
                    throw e;
                }
            });

            // Commit transaction
            jdbc.execute("COMMIT TRANSACTION");
        } catch (DataAccessException e) {
            // Rollback transaction on error
            try {
                jdbc.execute("ROLLBACK TRANSACTION");
            } catch (DataAccessException rollbackEx) {
                logger.error(
                    "Failed to rollback transaction for table {}.{}: {}",
                    schema, tableName, rollbackEx.getCause().getMessage()
                );
            }
            throw e;
        }
    }

        private void disableOracle(String tableName) {
        String schema = jdbc.queryForObject("SELECT USER FROM DUAL", String.class).toUpperCase();
        String tableUpper = tableName.toUpperCase();
  
        List<Map<String, Object>> constraints = jdbc.queryForList(
        "SELECT constraint_name, constraint_type, search_condition, r_constraint_name " +
        "FROM all_constraints ac " +
        "WHERE owner = ? " +
        "  AND table_name = ? " +
        "  AND constraint_type IN ('R', 'C', 'U', 'P') " +
        "  AND ( " +
        "       constraint_name NOT LIKE 'SYS_%' " +
        "       OR ( " +
        "          constraint_name LIKE 'SYS_%' " +
        "          AND EXISTS ( " +
        "              SELECT 1 FROM all_cons_columns acc " +
        "              WHERE acc.owner = ac.owner " +
        "                AND acc.constraint_name = ac.constraint_name " +
        "          ) " +
        "       ) " +
        "  ) " +
        "ORDER BY constraint_name",
        schema, tableUpper
        );
  
        for (Map<String, Object> row : constraints) {
          String name = safeGetString(row, "CONSTRAINT_NAME");
          String type = safeGetString(row, "CONSTRAINT_TYPE");
          String searchCond = safeGetString(row, "SEARCH_CONDITION");
          
          if ("P".equals(type)) {
          System.out.println("Constraint " + name + " ignored (PRIMARY KEY)");
          continue;
          }
          if ("C".equals(type) && searchCond != null && searchCond.toUpperCase().contains("IS NOT NULL")) {
          System.out.println("Constraint " + name + " ignored (NOT NULL implicit)");
          continue;
          }
          
          restoreDdls.push("ALTER TABLE " + tableUpper + " ENABLE CONSTRAINT " + name);
          
          if ("R".equals(type)) {
          List<String> fkCols = jdbc.queryForList(
          "SELECT column_name FROM all_cons_columns WHERE owner = ? AND constraint_name = ? ORDER BY position",
          String.class, schema, name
          );
          
          for (String col : fkCols) {
            String alterToNull = "ALTER TABLE " + tableUpper + " MODIFY (" + col + " NULL)";
            try {
            jdbc.execute(alterToNull);
            System.out.println("Column " + tableUpper + "." + col + " made nullable");
            restoreDdls.push("ALTER TABLE " + tableUpper + " MODIFY (" + col + " NOT NULL)");
            } catch (DataAccessException e) {
            System.err.println("Error modifying NULL for column " + col + ": " + e.getMessage());
                    }
                }
            }
  
            try {
                jdbc.execute("ALTER TABLE " + tableUpper + " DISABLE CONSTRAINT " + name);
          System.out.println("Constraint " + name + " disabled");
        } catch (DataAccessException e) {
          System.err.println("Error disabling constraint " + name + ": " + e.getMessage());
            }
        }
  
        System.out.println("Processing finished for table " + tableUpper);
  }
}
