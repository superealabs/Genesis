package org.labs.genesis.api.service;

import org.labs.genesis.api.context.GenerationContextStore;
import org.labs.genesis.api.dto.MetadataDtos.TableMetadataResponse;
import org.labs.genesis.api.dto.MetadataDtos.TablesMetadataResponse;
import org.labs.genesis.api.exception.MetadataException;
import org.labs.genesis.api.mapper.TableMetadataMapper;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.connexion.model.TableMetadata;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.util.List;

@Service
public class TableMetadataService {

    private final GenerationContextStore generationContextStore;

    public TableMetadataService(GenerationContextStore generationContextStore) {
        this.generationContextStore = generationContextStore;
    }

    public TablesMetadataResponse loadAll() {
        ProjectGenerationContext context = generationContextStore.getContext();
        validateContext(context);

        try {
            List<TableMetadata> tables = context.getDatabase().getEntities(
                                                context.getConnection(),
                                                context.getCredentials(),
                                                context.getLanguage(),
                                                context.getFramework()
                                        );

            List<TableMetadata> views = context.getDatabase().getViews(
                                                context.getConnection(),
                                                context.getCredentials(),
                                                context.getLanguage(),
                                                context.getFramework()
                                        );

            List<TableMetadataResponse> tableResponses =
                    tables.stream()
                            .map(TableMetadataMapper::toResponse)
                            .toList();

            List<TableMetadataResponse> viewResponses =
                    views.stream()
                            .map(TableMetadataMapper::toResponse)
                            .toList();

            return new TablesMetadataResponse(tableResponses, viewResponses);
        } catch (Exception exception) {
            throw new MetadataException("Impossible de charger les métadonnées : " + exception.getMessage());
        }
    }

    private void validateContext(ProjectGenerationContext context) {
        if (context.getFramework() == null) {
            throw new MetadataException("Le framework doit être sélectionné avant de charger les métadonnées");
        }

        if (context.getLanguage() == null) {
            throw new MetadataException("Aucun langage configuré");
        }

        if (context.getDatabase() == null) {
            throw new MetadataException("Aucune base de données configurée");
        }

        if (context.getCredentials() == null) {
            throw new MetadataException("Aucun identifiant de connexion configuré");
        }

        Connection connection = context.getConnection();

        if (connection == null) {
            throw new MetadataException("Aucune connexion à la base de données disponible");
        }

        try {
            if (connection.isClosed()) {
                throw new MetadataException("La connexion à la base de données est fermée");
            }
        } catch (Exception exception) {
            throw new MetadataException("Impossible de vérifier la connexion : " + exception.getMessage()
            );
        }
    }
}
