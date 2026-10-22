package org.labs.genesis.api.service;

import org.labs.genesis.api.exception.RelationException;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.api.context.GenerationContextStore;

import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

@Service
public class GenerationExecutionContextFactory {
    private final ProfileRelationService profileRelationService;
    private final ConsolidatedGenerationContextService consolidatedService;
    private final FrontendFrameworkIsolationService frontendIsolationService;
    private final GenerationContextStore contextStore;

    public GenerationExecutionContextFactory(ProfileRelationService profileRelationService,  ConsolidatedGenerationContextService consolidatedService, FrontendFrameworkIsolationService frontendIsolationService, GenerationContextStore contextStore) {
        this.profileRelationService = profileRelationService;
        this.consolidatedService = consolidatedService;
        this.frontendIsolationService = frontendIsolationService;
        this.contextStore = contextStore;
    }

    public ExecutionContext open(String profileName) {
        ProjectGenerationContext source = contextStore.getContext();
        if (source.getDatabase() == null || source.getCredentials() == null) {
            throw new RelationException("Configuration JDBC absente pour le profil " + profileName);
        }
        Connection connection = null;
        try {
            // 1. Ouvrir la connexion dédiée avant de charger les tables.
            connection = source.getDatabase().getConnection(source.getCredentials());
            if (connection == null
                    || connection.isClosed()
                    || !connection.isValid(5)) {
                throw new SQLException("Connexion JDBC d'exécution invalide");
            }

            // 2. Charger directement les métadonnées
            // sur cette connexion.
            ProjectGenerationContext context = profileRelationService.buildContext(profileName, connection);

            // 3. Isoler le framework frontend si le service
            // d'isolation a été intégré :
            //
             frontendIsolationService.isolate(context);

            // 4. Transférer la responsabilité de la connexion
            // au contexte d'exécution retourné.
            return new ExecutionContext(profileName, context, connection);

        } catch (Exception exception) {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException closeException) {
                    exception.addSuppressed(closeException);
                }
            }

            throw new IllegalStateException("Impossible de préparer le contexte du profil " + profileName, exception);
        }
    }

    public ExecutionContext open(String profileName, GenerationConfigurationSnapshot snapshot) {
        if (snapshot == null) {
            throw new IllegalArgumentException("Instantané de génération obligatoire");
        }

        var profile = snapshot.profiles().stream()
                .filter(p -> p.profileName().equals(profileName))
                .findFirst()
                .orElseThrow(() -> new RelationException("Profil absent de l'instantané : " + profileName));

        ProjectGenerationContext source = contextStore.getContext();

        if (source.getDatabase() == null || source.getCredentials() == null) {
            throw new RelationException("Configuration JDBC indisponible");
        }

        Connection connection = null;
        try {
            connection = source.getDatabase().getConnection(source.getCredentials());
            if (connection == null
                    || connection.isClosed()
                    || !connection.isValid(5)) {
                throw new SQLException("Connexion JDBC d'exécution invalide");
            }

            ProjectGenerationContext context = profileRelationService.buildContext(profile, snapshot.relationsFor(profileName), connection);

            // Activer ici uniquement lorsque la copie profonde
            // du framework frontend a été validée.
            frontendIsolationService.isolate(context);
            return new ExecutionContext(profileName, context, connection);
        } catch (Exception exception) {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException closeException) {
                    exception.addSuppressed(closeException);
                }
            }
            throw new IllegalStateException("Impossible d'ouvrir le profil " + profileName, exception);
        }
    }

    public ExecutionContext openConsolidated() {
        ProjectGenerationContext source = contextStore.getContext();
        if (source.getDatabase() == null || source.getCredentials() == null) {
            throw new IllegalStateException("Configuration JDBC indisponible");
        }
        Connection connection = null;
        try {
            // 1. Ouvrir la connexion dédiée en premier.
            connection = source.getDatabase().getConnection(source.getCredentials());
            if (connection == null || connection.isClosed() || !connection.isValid(5)) {
                throw new SQLException("Connexion JDBC d'exécution invalide");
            }

            // 2. Construire les métadonnées consolidées
            // directement sur cette connexion.
            ProjectGenerationContext context = consolidatedService.build(connection);

            // 3. Les options seront fournies par chaque tâche.
            context.setGenerationOptions(new ArrayList<>());
            context.setFrontendGenerationOptions(new ArrayList<>());

            // Si FrontendFrameworkIsolationService est intégré :
             frontendIsolationService.isolate(context);

            // 4. La connexion appartient au résultat de la factory.
            return new ExecutionContext("consolidated", context, connection);
        } catch (Exception exception) {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException closeException) {
                    exception.addSuppressed(closeException);
                }
            }
            throw new IllegalStateException("Impossible de préparer le contexte consolidé", exception);
        }
    }

    public ExecutionContext openConsolidated(GenerationConfigurationSnapshot snapshot) {
        if (snapshot == null) {
            throw new IllegalArgumentException("Instantané de génération obligatoire");
        }
        ProjectGenerationContext source = contextStore.getContext();
        if (source.getDatabase() == null || source.getCredentials() == null) {
            throw new IllegalStateException("Configuration JDBC indisponible");
        }

        Connection connection = null;
        try {
            connection = source.getDatabase().getConnection(source.getCredentials());

            if (connection == null || connection.isClosed() || !connection.isValid(5)) {
                throw new SQLException("Connexion JDBC d'exécution invalide");
            }

            ProjectGenerationContext context = consolidatedService.build(connection, snapshot);

            context.setGenerationOptions(new ArrayList<>());
            context.setFrontendGenerationOptions(new ArrayList<>());

            // Activer après validation de l'isolation.
             frontendIsolationService.isolate(context);

            return new ExecutionContext("consolidated", context, connection);
        } catch (Exception exception) {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException closeException) {
                    exception.addSuppressed(closeException);
                }
            }
            throw new IllegalStateException("Impossible d'ouvrir le contexte consolidé", exception);
        }
    }

    public record ExecutionContext(String profileName, ProjectGenerationContext context, Connection connection) implements AutoCloseable {

        @Override
        public void close() throws SQLException {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        }
    }
}
