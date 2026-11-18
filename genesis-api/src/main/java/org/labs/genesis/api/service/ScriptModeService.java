package org.labs.genesis.api.service;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.labs.genesis.api.context.GenerationContextStore;
import org.labs.genesis.api.dto.RelationDtos.RelationResponse;
import org.labs.genesis.api.dto.RelationDtos.RelationUpdateRequest;
import org.labs.genesis.api.dto.ScriptModeDtos.ScriptDatabaseConfigRequest;
import org.labs.genesis.api.dto.ScriptModeDtos.ScriptGenerationRequest;
import org.labs.genesis.api.dto.ScriptModeDtos.ScriptGenerationResponse;
import org.labs.genesis.api.dto.ScriptModeDtos.ScriptTablesResponse;
import org.labs.genesis.api.exception.DatabaseConfigException;
import org.labs.genesis.api.exception.ProjectConfigException;
import org.labs.genesis.api.exception.TableSelectionException;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.config.langage.generator.indicator.NoOpProgressReporter;
import org.labs.genesis.config.langage.generator.project.ProjectGenerator;
import org.labs.genesis.config.tools.DockerConfiguration;
import org.labs.genesis.config.tools.GitConfiguration;
import org.labs.genesis.connexion.Credentials;
import org.labs.genesis.connexion.Database;
import org.labs.genesis.connexion.SQLRunner;
import org.labs.genesis.connexion.model.RelationParameter;
import org.springframework.stereotype.Service;
import org.labs.genesis.connexion.ScriptDatabaseChecker;
/**
 * Génération d'un projet à partir d'un script SQL, sans connexion JDBC.
 *
 * Utilise directement les fonctions de genesis-core ajoutées pour le ScriptMode :
 *   - SQLRunner.extractTableNames / extractViewNames : lire les noms dans le script
 *   - context.setSqlScript + context.setTables()   : construire les tables depuis le script
 *   - ProjectGenerator.generateProject(...)         : même appel que le plugin IntelliJ
 */
@Service
public class ScriptModeService {

    private final GenerationContextStore contextStore;
    private final MetadataService metadataService;

    public ScriptModeService(GenerationContextStore contextStore, MetadataService metadataService) {
        this.contextStore = contextStore;
        this.metadataService = metadataService;
    }

    /** Étape « Script » : choisir le type de base et enregistrer le script (aucune connexion). */
    public synchronized ScriptTablesResponse configure(ScriptDatabaseConfigRequest request) {
        if (request == null || request.script() == null || request.script().isBlank()) {
            throw new DatabaseConfigException("Le script SQL est obligatoire");
        }
        Database database = findDatabase(request.databaseId(), request.engine());
        checkScriptMatchesDatabase(database, request.script());
        List<String> tables = SQLRunner.extractTableNames(request.script());
        List<String> views = SQLRunner.extractViewNames(request.script());
        if (tables.isEmpty() && views.isEmpty()) {
            throw new DatabaseConfigException("Aucun CREATE TABLE ni CREATE VIEW trouvé dans le script");
        }

        ProjectGenerationContext context = contextStore.getContext();
        closeQuietly(context.getConnection());   // on quitte un éventuel mode « base connectée »
        context.setConnection(null);
        context.setCredentials(null);            // credentials == null => core passe en mode script
        context.setDatabase(database);
        context.setSqlScript(request.script());

        return new ScriptTablesResponse(true, database.getName(), tables, views);
    }

    /** Relit les tables / vues du script déjà enregistré. */
    public ScriptTablesResponse tables() {
        ProjectGenerationContext context = contextStore.getContext();
        requireScript(context);
        String script = context.getSqlScript();
        return new ScriptTablesResponse(true, context.getDatabase().getName(),
                SQLRunner.extractTableNames(script), SQLRunner.extractViewNames(script));
    }

    /** Relations parent → enfant détectées dans le script (FOREIGN KEY / REFERENCES). */
    public List<RelationResponse> relations() {
        ProjectGenerationContext source = contextStore.getContext();
        requireScript(source);
        if (source.getFramework() == null || source.getLanguage() == null) {
            throw new ProjectConfigException("Sélectionnez d'abord un framework");
        }
        ProjectGenerationContext copy = source.duplicateWithNoTables();   // ne modifie pas le contexte
        copy.setConnection(null);
        copy.setCredentials(null);
        copy.setEntityNames(SQLRunner.extractTableNames(source.getSqlScript()));
        copy.setViewNames(new ArrayList<>());
        copy.setTables();
        copy.setRelationParameters(new ArrayList<>());
        return copy.autoDetectRelationParameters().stream()
                .map(r -> new RelationResponse(r.getParentTable(), r.getChildTable(),
                        Boolean.TRUE.equals(r.getMandatory()), Boolean.TRUE.equals(r.getHasForm())))
                .toList();
    }

    /** Lance la génération complète (backend + frontend/vues) depuis le script. */
    public synchronized ScriptGenerationResponse launch(ScriptGenerationRequest request) {
        ProjectGenerationContext context = contextStore.getContext();
        requireScript(context);
        if (context.getFramework() == null || context.getLanguage() == null) {
            throw new ProjectConfigException("Sélectionnez d'abord un framework");
        }
        if (context.getProjectName() == null || context.getDestinationFolder() == null) {
            throw new ProjectConfigException("Configurez d'abord le projet (POST /api/generator/project-config)");
        }

        String script = context.getSqlScript();
        List<String> tables = pick(request == null ? null : request.selectedTables(), SQLRunner.extractTableNames(script));
        List<String> views = pick(request == null ? null : request.selectedViews(), SQLRunner.extractViewNames(script));
        if (tables.isEmpty() && views.isEmpty()) {
            throw new TableSelectionException("Sélectionnez au moins une table ou une vue");
        }
        List<String> components = request == null || request.selectedComponents() == null || request.selectedComponents().isEmpty()
                ? List.of("model", "dao", "service", "controller")
                : request.selectedComponents();

        // Mode script : pas de connexion, pas de credentials
        context.setConnection(null);
        context.setCredentials(null);
        context.setEntityNames(new ArrayList<>(tables));
        context.setViewNames(new ArrayList<>(views));
        context.setGenerationOptions(metadataService.mapComponents(components));
        context.setGenerateProjectStructure(true);
        if (context.getFrontendFramework() == null) {
            context.setGenerateFrontendApp(false);   // aucun frontend choisi
        }
        if (context.getGitConfiguration() == null) {
            context.setGitConfiguration(new GitConfiguration());        // useGit = false
        }
        if (context.getDockerConfiguration() == null) {
            context.setDockerConfiguration(new DockerConfiguration());  // useDocker = false
        }

        // Identifiants « par défaut » écrits dans application.properties / appsettings.json
        // (localhost, root/postgres...). À modifier dans le projet généré.
        context.getDatabase().setCredentials(defaultCredentials(context));

        // Construit les TableMetadata depuis le script, puis les relations (clés étrangères)
        context.setTables();
        context.setRelationParameters(new ArrayList<>());
        context.autoDetectRelationParameters();          // relations lues dans les FOREIGN KEY du script
        if (request != null && request.relations() != null) {
            context.setRelationParameters(customRelations(request.relations(), tables));
        }

        try {
            new ProjectGenerator().generateProject(context, new NoOpProgressReporter());
        } catch (Exception exception) {
            throw new ProjectConfigException("Échec de la génération : " + rootMessage(exception));
        }

        return new ScriptGenerationResponse(true, "Projet généré depuis le script SQL",
                context.getDestinationFolder(), tables, views);
    }
        /** Refuse un script dont les types sont inconnus de la base choisie (ex. SERIAL avec MySQL). */
    private void checkScriptMatchesDatabase(Database database, String script) {
        String error = ScriptDatabaseChecker.mismatchMessage(database, script, ProjectGenerator.databases.values());
        if (error != null) {
            throw new DatabaseConfigException(error);
        }
    }

    // ---------- utilitaires ----------

    private Database findDatabase(Integer id, String engine) {
        Database database = null;
        if (id != null) {
            database = ProjectGenerator.databases.get(id);
        } else if (engine != null && !engine.isBlank()) {
            database = ProjectGenerator.databases.values().stream()
                    .filter(db -> db.getName().equalsIgnoreCase(engine.trim()))
                    .findFirst().orElse(null);
        }
        if (database == null) {
            throw new DatabaseConfigException("Type de base de données inconnu ou manquant");
        }
        return database;
    }

    private void requireScript(ProjectGenerationContext context) {
        if (context.getSqlScript() == null || context.getSqlScript().isBlank() || context.getDatabase() == null) {
            throw new DatabaseConfigException("Envoyez d'abord le script (POST /api/generator/script-database-config)");
        }
    }

    private Credentials defaultCredentials(ProjectGenerationContext context) {
        Map<String, Object> meta = context.getDatabase()
                .getDatabaseMetadataHashMapFromScript(context.getProjectName().toLowerCase());
        Credentials credentials = new Credentials();
        credentials.setHost(String.valueOf(meta.get("host")));
        credentials.setPort(String.valueOf(meta.get("port")));
        credentials.setDatabaseName(String.valueOf(meta.get("database")));
        credentials.setSchemaName(String.valueOf(meta.get("schema")));
        credentials.setUser(String.valueOf(meta.get("username")));
        credentials.setPwd(String.valueOf(meta.get("password")));
        credentials.setDriverType(String.valueOf(meta.get("driverType")));
        credentials.setSID(String.valueOf(meta.get("sid")));
        credentials.setAllowPublicKeyRetrieval(true);
        return credentials;
    }

    private List<RelationParameter> customRelations(List<RelationUpdateRequest> requested, List<String> tables) {
        List<RelationParameter> result = new ArrayList<>();
        for (RelationUpdateRequest r : requested) {
            String parent = tables.stream().filter(t -> t.equalsIgnoreCase(r.parentTable())).findFirst()
                    .orElseThrow(() -> new TableSelectionException("Table parente non sélectionnée : " + r.parentTable()));
            String child = tables.stream().filter(t -> t.equalsIgnoreCase(r.childTable())).findFirst()
                    .orElseThrow(() -> new TableSelectionException("Table enfant non sélectionnée : " + r.childTable()));
            result.add(new RelationParameter(parent, child, r.mandatory(), r.hasForm()));
        }
        return result;
    }

    /** Garde la sélection de l'utilisateur, ou tout ce qui est dans le script si rien n'est envoyé. */
    private List<String> pick(List<String> selected, List<String> available) {
        if (selected == null || selected.isEmpty()) {
            return available;
        }
        List<String> result = new ArrayList<>();
        for (String name : selected) {
            String found = available.stream().filter(a -> a.equalsIgnoreCase(name.trim())).findFirst()
                    .orElseThrow(() -> new TableSelectionException("Introuvable dans le script : " + name));
            result.add(found);
        }
        return result;
    }

    private String rootMessage(Throwable error) {
        Throwable root = error;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        return root.getMessage() == null ? root.getClass().getSimpleName() : root.getMessage().trim();
    }

    private void closeQuietly(Connection connection) {
        if (connection == null) return;
        try {
            connection.close();
        } catch (Exception ignored) {
            // rien à faire
        }
    }
}
