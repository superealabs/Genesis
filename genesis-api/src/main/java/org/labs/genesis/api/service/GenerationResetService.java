package org.labs.genesis.api.service;

import org.labs.genesis.api.context.GenerationContextStore;
import org.labs.genesis.config.ProjectGenerationContext;
import org.springframework.stereotype.Service;

import java.sql.Connection;

@Service
public class GenerationResetService {

    private final GenerationExecutionService executionService;
    private final GenerationContextStore contextStore;
    private final GenerationProfilesStore profilesStore;
    private final ProfileRelationService profileRelationService;
    private final SqlScriptService sqlScriptService;
    private final GenerationPublicationService publicationService;
    private final FrontendAssetService frontendAssetService;

    public GenerationResetService(
            GenerationExecutionService executionService,
            GenerationContextStore contextStore,
            GenerationProfilesStore profilesStore,
            ProfileRelationService profileRelationService,
            SqlScriptService sqlScriptService,
            GenerationPublicationService publicationService,
            FrontendAssetService frontendAssetService
    ) {
        this.executionService = executionService;
        this.contextStore = contextStore;
        this.profilesStore = profilesStore;
        this.profileRelationService = profileRelationService;
        this.sqlScriptService = sqlScriptService;
        this.publicationService = publicationService;
        this.frontendAssetService = frontendAssetService;
    }

    public void resetAll() throws Exception {
        executionService.runExclusively(() -> {

            // Remplacer le contexte par un contexte neuf.
            ProjectGenerationContext previous = contextStore.reset();

            try {
                // Profils et relations manuelles.
                profileRelationService.clear();
                profilesStore.clear();

                // Script SQL mémorisé.
                sqlScriptService.clearSavedScript();

                // Référence à la dernière publication.
                publicationService.clearLastPublishedTarget();

                // Logo et favicon temporaires.
                frontendAssetService.clear();

            } finally {
                // Fermer l'ancienne connexion JDBC.
                Connection connection = previous.getConnection();

                if (connection != null) {
                    connection.close();
                }
            }
            return null;
        });
    }
}