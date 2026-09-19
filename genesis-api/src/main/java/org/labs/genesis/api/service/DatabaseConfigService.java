package org.labs.genesis.api.service;

import org.labs.genesis.api.context.GenerationContextStore;
import org.labs.genesis.api.dto.DatabaseDtos.DatabaseConfigRequest;
import org.labs.genesis.api.dto.DatabaseDtos.DatabaseConfigResponse;
import org.labs.genesis.api.dto.DatabaseDtos.DatabaseResponse;
import org.labs.genesis.api.exception.DatabaseConfigException;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.connexion.Credentials;
import org.labs.genesis.connexion.Database;
import org.labs.genesis.config.langage.generator.project.ProjectGenerator;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.util.Comparator;
import java.util.List;

@Service
public class DatabaseConfigService {

    private final GenerationContextStore generationContextStore;

    public DatabaseConfigService(GenerationContextStore generationContextStore) {
        this.generationContextStore = generationContextStore;
    }

    public List<DatabaseResponse> getDatabases() {
        return ProjectGenerator.databases.values().stream()
                .sorted(Comparator.comparingInt(Database::getId))
                .map(database -> new DatabaseResponse(
                        database.getId(),
                        database.getName(),
                        database.getPort()
                ))
                .toList();
    }

    public DatabaseConfigResponse configure(DatabaseConfigRequest request) {
        validateRequest(request);

        Database database = ProjectGenerator.databases.get(request.databaseId());
        if (database == null) {
            throw new DatabaseConfigException("Base de données inconnue : " + request.databaseId());
        }

        Credentials credentials = new Credentials();
        credentials.setHost(request.host());
        credentials.setPort(request.port());
        credentials.setDatabaseName(request.databaseName());
        credentials.setSchemaName(request.schema());
        credentials.setUser(request.username());
        credentials.setPwd(request.password());

        if (request.sid() != null) {
            credentials.setSID(request.sid());
        }

        ProjectGenerationContext context = generationContextStore.getContext();
        context.setDatabase(database);
        context.setCredentials(credentials);

        try {
            Connection connection = database.getConnection(credentials);
            context.setConnection(connection);
            return new DatabaseConfigResponse(true, true, "Connexion à la base de données réussie");
        } catch (Exception exception) {
            throw new DatabaseConfigException("Impossible de se connecter à la base de données : " + exception.getMessage());
        }
    }

    private void validateRequest(DatabaseConfigRequest request) {
        if (request == null) {
            throw new DatabaseConfigException("La configuration de la base est obligatoire");
        }

        if (request.databaseId() == null) {
            throw new DatabaseConfigException("La base de données est obligatoire");
        }

        if (request.host() == null || request.host().isBlank()) {
            throw new DatabaseConfigException("L'hôte est obligatoire");
        }

        if (request.port() == null || request.port().isBlank()) {
            throw new DatabaseConfigException("Le port est obligatoire");
        }

        if (request.databaseName() == null || request.databaseName().isBlank()) {
            throw new DatabaseConfigException("Le nom de la base est obligatoire");
        }

        if (request.username() == null || request.username().isBlank()) {
            throw new DatabaseConfigException("Le nom d'utilisateur est obligatoire");
        }
    }
}