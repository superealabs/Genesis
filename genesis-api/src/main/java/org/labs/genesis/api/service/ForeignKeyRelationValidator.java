
package org.labs.genesis.api.service;

import org.labs.genesis.api.exception.RelationException;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

@Service
public class ForeignKeyRelationValidator {

    public void validate(Connection connection, String parentTable, String childTable) {
        if (connection == null) {
            throw new RelationException("Aucune connexion JDBC disponible");
        }

        if (parentTable == null || parentTable.isBlank() || childTable == null || childTable.isBlank()) {
            throw new RelationException("Les noms des tables sont obligatoires");
        }

        if (parentTable.equals(childTable)) {
            throw new RelationException("Les relations réflexives ne sont pas " + "prises en charge");
        }

        try {
            DatabaseMetaData metadata = connection.getMetaData();
            String catalog = connection.getCatalog();
            String schema = connection.getSchema();

            boolean valid = exists(metadata, catalog, schema, parentTable, childTable);

            if (!valid) {
                throw new RelationException("Aucune clé étrangère déclarée de " + childTable + " vers " + parentTable);
            }
        } catch (SQLException exception) {
            throw new RelationException("Impossible de vérifier les clés étrangères : " + exception.getMessage());
        }
    }

    private boolean exists(DatabaseMetaData metadata, String catalog, String schema, String parentTable, String childTable) throws SQLException {
        try (ResultSet keys = metadata.getImportedKeys(catalog, schema, childTable)) {
            while (keys.next()) {
                String referencedTable = keys.getString("PKTABLE_NAME");
                if (parentTable.equals(referencedTable)) {
                    return true;
                }
            }
        }
        return false;
    }
}
