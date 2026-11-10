package org.labs.genesis.config.form;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.labs.genesis.config.Constantes;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.config.langage.Language;
import org.labs.genesis.config.langage.Framework;
import org.labs.genesis.config.langage.generator.project.ProjectMetadataProvider;
import org.labs.genesis.connexion.model.ColumnMetadata;
import org.labs.genesis.connexion.model.TableMetadata;
import org.labs.genesis.frontend.generator.frameworkFrontend.FrameworkFrontendMetadataProvider;
import org.labs.utils.FileUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FormConfiguration - Tests unitaires en mémoire")
public class FormConfigurationTest {

    @Test
    @DisplayName("Chargement et désérialisation du fichier form-field-configuration.json")
    void testFormConfigurationJsonLoading() throws Exception {
        FormConfiguration config = FileUtils.fromJson(
                FormConfiguration.class,
                Constantes.FORM_CONFIGURATION_JSON
        );

        assertNotNull(config, "La configuration des formulaires ne doit pas être nulle");
        assertNotNull(config.getTypes(), "Les types ne doivent pas être nuls");
        assertTrue(config.getTypes().containsKey("number"), "Doit contenir le type 'number'");
        assertTrue(config.getTypes().containsKey("text"), "Doit contenir le type 'text'");
        assertTrue(config.getTypes().containsKey("binary"), "Doit contenir le type 'binary'");
        assertTrue(config.getTypes().containsKey("json"), "Doit contenir le type 'json'");

        List<String> numberComponents = config.getTypes().get("number").getComponents();
        assertTrue(numberComponents.contains("number"));
        assertTrue(numberComponents.contains("range"));
        assertTrue(numberComponents.contains("quantity"));
        assertTrue(numberComponents.contains("currency"));
        assertTrue(numberComponents.contains("map"));

        assertNotNull(config.getComponents(), "Les composants ne doivent pas être nuls");
        assertTrue(config.getComponents().containsKey("number"));
        assertTrue(config.getComponents().containsKey("range"));
        assertTrue(config.getComponents().containsKey("quantity"));
        assertTrue(config.getComponents().containsKey("currency"));
        assertTrue(config.getComponents().containsKey("file"));
        assertTrue(config.getComponents().containsKey("map"));
    }

    @Test
    @DisplayName("Méthodes d'assistance et formatage de FieldConfiguration")
    void testFieldConfigurationHelpers() {
        FieldConfiguration fieldConf = new FieldConfiguration();
        fieldConf.setComponent("range");

        fieldConf.addConfig("min", "10", "number");
        fieldConf.addConfig("max", "100", "number");
        fieldConf.addConfig("step", "5", "number");
        fieldConf.addConfig("currency", "EUR", "select");
        fieldConf.addConfig("accept", ".png,.jpg", "multi-select");

        assertEquals("range", fieldConf.getComponent());
        assertEquals("min=\"10\" max=\"100\" step=\"5\"", fieldConf.getInputConfiguration());
        assertEquals("accept=\".png,.jpg\"", fieldConf.getFileConfiguration());
        assertEquals("currency=\"EUR\"", fieldConf.getCurrency());

        fieldConf.addConfig("latitude", "lat_col", "column");
        fieldConf.addConfig("longitude", "lon_col", "column");
        assertEquals("lat_col", fieldConf.getLatitude());
        assertEquals("lon_col", fieldConf.getLongitude());

        fieldConf.removeConf("step");
        assertEquals("min=\"10\" max=\"100\"", fieldConf.getInputConfiguration());

        fieldConf.clearConfiguration();
        assertEquals("", fieldConf.getInputConfiguration());
        assertEquals("", fieldConf.getFileConfiguration());
        assertEquals("", fieldConf.getCurrency());
        assertNull(fieldConf.getLatitude());
        assertNull(fieldConf.getLongitude());
    }

    @Test
    @DisplayName("Résolution de la configuration : priorité referencedColumn puis fallback name et insensible à la casse")
    void testFindConfigurationPriorityAndFallbacks() {
        Map<String, Map<String, FieldConfiguration>> formConfig = new HashMap<>();
        Map<String, FieldConfiguration> tableConfig = new HashMap<>();

        FieldConfiguration priceConfig = new FieldConfiguration();
        priceConfig.setComponent("currency");
        tableConfig.put("prix_unitaire", priceConfig);

        FieldConfiguration emailConfig = new FieldConfiguration();
        emailConfig.setComponent("email");
        tableConfig.put("userEmail", emailConfig);

        formConfig.put("produits", tableConfig);

        // 1. Recherche par referencedColumn (priorité 1)
        ColumnMetadata col1 = new ColumnMetadata();
        col1.setName("prix");
        col1.setReferencedColumn("prix_unitaire");
        FieldConfiguration found1 = FieldConfiguration.findConfiguration(formConfig, "produits", col1);
        assertNotNull(found1);
        assertEquals("currency", found1.getComponent());

        // 2. Fallback sur name quand referencedColumn est null ou absent (priorité 2)
        ColumnMetadata col2 = new ColumnMetadata();
        col2.setName("userEmail");
        col2.setReferencedColumn(null);
        FieldConfiguration found2 = FieldConfiguration.findConfiguration(formConfig, "produits", col2);
        assertNotNull(found2);
        assertEquals("email", found2.getComponent());

        // 3. Fallback insensible à la casse (nom de table en majuscule, nom de colonne en majuscule)
        ColumnMetadata col3 = new ColumnMetadata();
        col3.setName("PRIX");
        col3.setReferencedColumn("PRIX_UNITAIRE");
        FieldConfiguration found3 = FieldConfiguration.findConfiguration(formConfig, "PRODUITS", col3);
        assertNotNull(found3);
        assertEquals("currency", found3.getComponent());

        // 4. Champ non configuré -> null
        ColumnMetadata col4 = new ColumnMetadata();
        col4.setName("non_existant");
        col4.setReferencedColumn("non_existant");
        assertNull(FieldConfiguration.findConfiguration(formConfig, "produits", col4));

        // 5. Paramètres nuls -> null sans exception
        assertNull(FieldConfiguration.findConfiguration(null, "produits", col1));
        assertNull(FieldConfiguration.findConfiguration(formConfig, null, col1));
        assertNull(FieldConfiguration.findConfiguration(formConfig, "produits", null));
    }

    @Test
    @DisplayName("FrameworkFrontendMetadataProvider : résolution des coordonnées Map sans chaîne littérale ${minStart}")
    void testFrameworkFrontendMetadataProviderMapCoordinates() {
        Map<String, Map<String, FieldConfiguration>> formConfig = new HashMap<>();
        Map<String, FieldConfiguration> tableConfig = new HashMap<>();

        FieldConfiguration mapConfig = new FieldConfiguration();
        mapConfig.setComponent("map");
        mapConfig.addConfig("latitude", "latitude_col", "column");
        tableConfig.put("location", mapConfig);

        formConfig.put("magasins", tableConfig);

        ColumnMetadata col = new ColumnMetadata();
        col.setName("location");
        col.setReferencedColumn("location");
        col.setFrontEndType("number");
        col.setUiType("number");

        Map<String, Object> fieldMap = FrameworkFrontendMetadataProvider.getFieldHashMap(col, "magasins", formConfig);

        assertEquals("map", fieldMap.get("uiType"));
        // Vérifie que longitude est bien le nom de la colonne en camelCase et NON la chaîne littérale "${minStart(this.name)}"
        assertEquals("location", fieldMap.get("longitude"));
        assertNotEquals("${minStart(this.name)}", fieldMap.get("longitude"));
        assertEquals("latitudeCol", fieldMap.get("latitude"));
    }

    @Test
    @DisplayName("FrameworkFrontendMetadataProvider : résolution avec longitude compagne")
    void testFrameworkFrontendMetadataProviderCompanionLongitude() {
        Map<String, Map<String, FieldConfiguration>> formConfig = new HashMap<>();
        Map<String, FieldConfiguration> tableConfig = new HashMap<>();

        FieldConfiguration mapConfig = new FieldConfiguration();
        mapConfig.setComponent("map");
        mapConfig.addConfig("longitude", "longitude_col", "column");
        tableConfig.put("latField", mapConfig);

        formConfig.put("sites", tableConfig);

        ColumnMetadata col = new ColumnMetadata();
        col.setName("latField");
        col.setReferencedColumn("latField");
        col.setFrontEndType("number");
        col.setUiType("number");

        Map<String, Object> fieldMap = FrameworkFrontendMetadataProvider.getFieldHashMap(col, "sites", formConfig);

        assertEquals("map", fieldMap.get("uiType"));
        assertEquals("longitudeCol", fieldMap.get("longitude"));
        assertEquals("latField", fieldMap.get("latitude"));
        assertNotEquals("${minStart(this.name)}", fieldMap.get("latitude"));
    }

    @Test
    @DisplayName("ProjectMetadataProvider : absence de NPE quand database est null pour un projet .NET sans base de données")
    void testProjectMetadataProviderNullDatabaseSafety() {
        Framework framework = new Framework();
        framework.setId(Constantes.NET_ID);
        framework.setName(".NET Core");
        framework.setUseDB(false);

        Language language = new Language();
        language.setId(1);
        language.setName("C#");

        assertDoesNotThrow(() -> {
            Map<String, Object> map = ProjectMetadataProvider.getProjectFilesEditsHashMap(
                    "/tmp/dest",
                    "MonProjet",
                    "org.labs",
                    "8080",
                    null, // database null
                    null, // credentials null
                    language,
                    "Description du projet",
                    null,
                    framework,
                    null,
                    false
            );
            assertNotNull(map);
            assertFalse((Boolean) map.get("hasPoint"));
        });
    }

    @Test
    @DisplayName("ProjectMetadataProvider : topologyProvider pour .NET selon le SGBD")
    void testProjectMetadataProviderTopologyProviderForDotNet() {
        Framework framework = new Framework();
        framework.setId(Constantes.NET_ID);
        framework.setName(".NET Core");
        framework.setUseDB(false);

        Language language = new Language();
        language.setId(1);
        language.setName("C#");

        // PostgreSQL
        org.labs.genesis.connexion.providers.PostgreSQLDatabase pgDb = new org.labs.genesis.connexion.providers.PostgreSQLDatabase();
        pgDb.setId(Constantes.PostgreSQL_ID);
        assertDoesNotThrow(() -> {
            Map<String, Object> map = ProjectMetadataProvider.getProjectFilesEditsHashMap(
                    "/tmp/dest", "MonProjet", "org.labs", "8080",
                    pgDb, null, language, "Desc", null, framework, null, true
            );
            assertEquals("Npgsql.EntityFrameworkCore.PostgreSQL", map.get("topologyProvider"));
            assertTrue((Boolean) map.get("hasPoint"));
        });
    }
}
