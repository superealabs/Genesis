package org.labs.genesis.api.dto;

import java.util.List;

/**
 * Mode « script SQL » : génération sans connexion à une base de données.
 * Équivalent API des étapes DataSourceChoiceWizardStep + ScriptDatabaseConfigurationWizardStep du plugin IntelliJ.
 */
public final class ScriptModeDtos {

    private ScriptModeDtos() {
    }

    /** Type de base (MySQL, PostgreSQL...) + le script SQL fourni par l'utilisateur. */
    public record ScriptDatabaseConfigRequest(
            Integer databaseId,
            String engine,
            String script
    ) {
    }

    /** Tables et vues trouvées dans le script (pour afficher la liste à cocher). */
    public record ScriptTablesResponse(
            boolean success,
            String database,
            List<String> tables,
            List<String> views
    ) {
    }

    /**
     * Tables / vues / composants à générer. Listes vides = tout générer.
     * relations : null = détectées automatiquement depuis les FOREIGN KEY / REFERENCES du script ;
     *             sinon, remplace la liste (pour choisir mandatory / hasForm).
     */
    public record ScriptGenerationRequest(
            List<String> selectedTables,
            List<String> selectedViews,
            List<String> selectedComponents,
            List<RelationDtos.RelationUpdateRequest> relations
    ) {
    }

    public record ScriptGenerationResponse(
            boolean success,
            String message,
            String destination,
            List<String> tables,
            List<String> views
    ) {
    }
}
