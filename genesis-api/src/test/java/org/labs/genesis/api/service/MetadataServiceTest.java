package org.labs.genesis.api.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.labs.genesis.api.context.GenerationContextStore;
import org.labs.genesis.api.dto.MetadataDtos.*;
import org.labs.genesis.config.Constantes;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.config.langage.generator.project.ProjectGenerator;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MetadataServiceTest {

    private static ProjectGenerator projectGenerator;

    @BeforeAll
    static void setUp() {
        projectGenerator = new ProjectGenerator();
    }

    @Test
    void loadAllInScriptModeWithoutConnectionReturnsParsedTablesAndViews() {
        GenerationContextStore store = new GenerationContextStore();
        ProjectGenerationContext context = store.getContext();
        context.setProjectName("demo_app");
        context.setFramework(ProjectGenerator.frameworks.get(Constantes.Spring_REST_API_ID));
        context.setLanguage(ProjectGenerator.languages.get(Constantes.Java_ID));
        context.setDatabase(ProjectGenerator.databases.get(Constantes.PostgreSQL_ID));

        String sql = """
                CREATE TABLE users (
                    id SERIAL PRIMARY KEY,
                    name VARCHAR(100) NOT NULL
                );
                CREATE VIEW active_users AS SELECT id, name FROM users;
                """;
        context.setSqlScript(sql);

        MetadataService service = new MetadataService(store);
        TablesMetadataResponse response = service.loadAll();

        assertThat(response).isNotNull();
        assertThat(response.tables()).hasSize(1);
        assertThat(response.tables().get(0).tableName()).isEqualTo("users");
        assertThat(response.views()).hasSize(1);
        assertThat(response.views().get(0).tableName()).isEqualTo("active_users");
        assertThat(context.getConnection()).isNull();
    }

    @Test
    void selectInScriptModeWithoutConnectionSucceeds() {
        GenerationContextStore store = new GenerationContextStore();
        ProjectGenerationContext context = store.getContext();
        context.setProjectName("demo_app");
        context.setFramework(ProjectGenerator.frameworks.get(Constantes.Spring_REST_API_ID));
        context.setLanguage(ProjectGenerator.languages.get(Constantes.Java_ID));
        context.setDatabase(ProjectGenerator.databases.get(Constantes.PostgreSQL_ID));

        String sql = """
                CREATE TABLE products (
                    id SERIAL PRIMARY KEY,
                    title VARCHAR(150) NOT NULL,
                    price NUMERIC(10,2)
                );
                """;
        context.setSqlScript(sql);

        MetadataService service = new MetadataService(store);
        TableSelectionResponse response = service.select(new TableSelectionRequest(
                List.of("products"),
                List.of(),
                List.of("model", "dao", "service", "controller")
        ));

        assertThat(response.success()).isTrue();
        assertThat(response.selectedTables()).containsExactly("products");
        assertThat(context.getEntityTables()).hasSize(1);
        assertThat(context.getEntityTables().get(0).getTableName()).isEqualTo("products");
        assertThat(context.getEntityTables().get(0).getColumns()).isNotEmpty();
    }
}
