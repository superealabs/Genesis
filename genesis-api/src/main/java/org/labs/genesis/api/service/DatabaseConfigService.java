package org.labs.genesis.api.service;

import org.labs.genesis.api.context.GenerationContextStore;
import org.labs.genesis.api.dto.DatabaseDtos.*;
import org.labs.genesis.api.exception.DatabaseConfigException;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.config.langage.generator.project.ProjectGenerator;
import org.labs.genesis.connexion.Credentials;
import org.labs.genesis.connexion.Database;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
public class DatabaseConfigService {

    private final GenerationContextStore generationContextStore;

    public DatabaseConfigService(GenerationContextStore generationContextStore) {
        this.generationContextStore = generationContextStore;
    }

    public List<DatabaseEngineResponse> getEngines() {
        return ProjectGenerator.databases.values()
                .stream()
                .sorted(Comparator.comparingInt(Database::getId))
                .map(database -> new DatabaseEngineResponse(
                        database.getId(),
                        database.getName(),
                        database.getDriver(),
                        database.getDriverName(),
                        database.getPort(),
                        database.getDriverType(),
                        database.getSid(),
                        database.getDriverVersion(),
                        database.getDatabaseMajorVersion()
                ))
                .toList();
    }

    public DatabaseConnectionTestResponse testConnection(DatabaseConfigRequest request) {
        ResolvedDatabase resolved = resolveRequest(request);
        try (Connection connection = resolved.database().getConnection(resolved.credentials())) {
            if (!connection.isValid(5)) {
                return new DatabaseConnectionTestResponse(false, "La connexion JDBC n'est pas valide");
            }
            return new DatabaseConnectionTestResponse(true, "Connexion à la base de données réussie");
        } catch (Exception exception) {
            ConnectionFailure failure = classifyConnectionFailure(exception);
            return new DatabaseConnectionTestResponse(false, failure.message());
        }
    }

    public synchronized DatabaseConfigResponse configure(DatabaseConfigRequest request) {
        ResolvedDatabase resolved = resolveRequest(request);
        Connection newConnection = null;
        try {
            newConnection = resolved.database().getConnection(resolved.credentials());
            if (!newConnection.isValid(5)) {
                throw new DatabaseConfigException("La connexion JDBC n'est pas valide");
            }

            ProjectGenerationContext context = generationContextStore.getContext();

            Connection previousConnection = context.getConnection();

            context.setDatabase(resolved.database());
            context.setCredentials(resolved.credentials());
            context.setConnection(newConnection);

            newConnection = null;

            if (previousConnection != null && previousConnection != context.getConnection()) {
                try {
                    previousConnection.close();
                } catch (Exception ignored) {
                    // Nouvelle connexion déjà enregistrée.
                }
            }

            return new DatabaseConfigResponse(true, true, "Configuration de la base enregistrée avec succès");
        } catch (DatabaseConfigException exception) {
            throw exception;
        } catch (Exception exception) {
            ConnectionFailure failure = classifyConnectionFailure(exception);
            throw new DatabaseConfigException(failure.code(), failure.message(), exception);
        } finally {
            if (newConnection != null) {
                try {
                    newConnection.close();
                } catch (Exception ignored) {
                    // Ne pas masquer l'erreur initiale.
                }
            }
        }
    }

    private ResolvedDatabase resolveRequest(DatabaseConfigRequest request) {
        validateRequest(request);

        Database database = resolveDatabase(request);

        Credentials credentials = new Credentials();
        credentials.setHost(request.host().trim());
        credentials.setPort(request.port().trim());
        credentials.setDatabaseName(request.databaseName().trim());
        credentials.setSchemaName(normalizeOptional(request.schema()));
        credentials.setUser(request.username().trim());
        credentials.setPwd(Objects.requireNonNullElse(request.password(), ""));
        credentials.setDriverType(normalizeOptional(request.driverType()));
        credentials.setSID(normalizeOptional(request.sid()));
        credentials.setTrustCertificate(Boolean.TRUE.equals(request.trustCertificate()));
        credentials.setAllowPublicKeyRetrieval(Boolean.TRUE.equals(request.allowPublicKeyRetrieval()));

        return new ResolvedDatabase(database, credentials);
    }

    private Database resolveDatabase(DatabaseConfigRequest request) {
        Database byId = request.databaseId() == null
                ? null
                : ProjectGenerator.databases.get(request.databaseId());

        Database byName = null;

        if (request.engine() != null && !request.engine().isBlank()) {
            byName = ProjectGenerator.databases.values()
                    .stream()
                    .filter(database -> database.getName()
                            .equalsIgnoreCase(request.engine().trim()))
                    .findFirst()
                    .orElse(null);
        }

        if (request.databaseId() != null && byId == null) {
            throw new DatabaseConfigException("Identifiant de base de données inconnu");
        }

        if (request.engine() != null && !request.engine().isBlank() && byName == null) {
            throw new DatabaseConfigException("Moteur de base de données inconnu");
        }

        if (byId != null && byName != null && byId.getId() != byName.getId()) {
            throw new DatabaseConfigException("Le moteur et l'identifiant de base sont incompatibles");
        }

        Database database = byId != null ? byId : byName;

        if (database == null) {
            throw new DatabaseConfigException("Un moteur de base de données est obligatoire");
        }

        return database;
    }

    private void validateRequest(DatabaseConfigRequest request) {
        if (request == null) {
            throw new DatabaseConfigException("La configuration de la base est obligatoire");
        }

        if (isBlank(request.host())
                || isBlank(request.port())
                || isBlank(request.databaseName())
                || isBlank(request.username())) {
            throw new DatabaseConfigException(
                    "Hôte, port, nom de base et utilisateur sont obligatoires"
            );
        }

        try {
            int port = Integer.parseInt(request.port().trim());

            if (port < 1 || port > 65535) {
                throw new NumberFormatException();
            }

        } catch (NumberFormatException exception) {
            throw new DatabaseConfigException("Le port doit être compris entre 1 et 65535");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String normalizeOptional(String value) {
        return value == null ? "" : value.trim();
    }

    private record ResolvedDatabase(Database database, Credentials credentials) {
    }


    private ConnectionFailure classifyConnectionFailure(
            Exception exception
    ) {
        for (Throwable cause = exception;
             cause != null;
             cause = cause.getCause()) {

            if (cause instanceof ClassNotFoundException) {
                return new ConnectionFailure(
                        "DATABASE_DRIVER_NOT_FOUND",
                        "Le pilote JDBC du moteur sélectionné est introuvable."
                );
            }

            if (cause instanceof SQLException sqlException) {
                for (SQLException current = sqlException;
                     current != null;
                     current = current.getNextException()) {

                    ConnectionFailure failure =
                            classifySqlException(current);

                    if (failure != null) {
                        return failure;
                    }
                }
            }
        }

        return new ConnectionFailure(
                "DATABASE_CONNECTION_FAILED",
                "Impossible d'établir la connexion à la base de données."
        );
    }

    private ConnectionFailure classifySqlException(SQLException exception) {
        String state = exception.getSQLState();
        int errorCode = exception.getErrorCode();

        if ("28P01".equals(state)
                || "28000".equals(state)
                || errorCode == 1045
                || errorCode == 18456
                || errorCode == 1017) {

            return new ConnectionFailure("DATABASE_AUTHENTICATION_FAILED", "Identifiant ou mot de passe incorrect.");
        }

        // PostgreSQL : 3D000
        // MySQL : 1049 (Unknown database)
        if ("3D000".equals(state) || errorCode == 1049) {
            return new ConnectionFailure("DATABASE_NOT_FOUND", "La basede données demandée n'existe pas. " + "Vérifiez son nom.");
        }

        // MySQL : 1044 (Access denied to database)
        if (errorCode == 1044) {
            return new ConnectionFailure("DATABASE_ACCESS_DENIED", "L'utilisateur ne dispose pas des droits " + "d'accès à cette base de données.");
        }

        // SQL Server : 4060
        // Ce code peut indiquer une base inexistante
        // ou simplement inaccessible.
        if (errorCode == 4060) {
            return new ConnectionFailure("DATABASE_NOT_ACCESSIBLE", "La base de données demandée est introuvable " + "ou inaccessible pour cet utilisateur.");
        }

        // SQLState classe 08 : problème de connexion.
        if (state != null && state.startsWith("08")) {
            return new ConnectionFailure("DATABASE_CONNECTION_FAILED", "Impossible de joindre le serveur de base " + "de données. Vérifiez l'hôte, leport " + "et la disponibilité du service.");
        }
        return null;
    }

    private record ConnectionFailure(
            String code,
            String message
    ) {
    }
}
