package org.labs.genesis.api.service;

import org.labs.genesis.api.context.GenerationContextStore;
import org.labs.genesis.api.dto.FormConfigDtos.*;
import org.labs.genesis.api.exception.FormConfigException;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.config.form.FieldConfiguration;
import org.labs.genesis.config.form.FormConfiguration;
import org.labs.genesis.config.form.FormFieldTypeResolver;
import org.labs.genesis.config.langage.generator.project.ProjectGenerator;
import org.labs.genesis.connexion.SQLRunner;
import org.labs.genesis.connexion.model.ColumnMetadata;
import org.labs.genesis.connexion.model.TableMetadata;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

/**
 * Étape « Form configuration » : rendu de chaque colonne dans les formulaires générés.
 *
 * Remplit {@link ProjectGenerationContext#setFormConfiguration}, lu par le générateur
 * (FrameworkMetadataProvider / FrameworkFrontendMetadataProvider) pour le frontend et les vues MVC.
 * La configuration est effacée par DELETE /api/generator/reset (nouveau contexte).
 */
@Service
public class FormConfigService {

    private static final String FIELD_NUMBER = "number";
    private static final String FIELD_SELECT = "select";
    private static final String FIELD_MULTI_SELECT = "multi-select";
    private static final String FIELD_COLUMN = "column";
    private static final String COMPONENT_MAP = "map";

    private final GenerationContextStore store;

    public FormConfigService(GenerationContextStore store) {
        this.store = store;
    }

    // ── Catalogue ───────────────────────────────────────────────────────────────

    public FormFieldCatalogResponse catalog() {
        FormConfiguration catalog = catalogOrFail();
        Map<String, List<String>> types = new LinkedHashMap<>();
        catalog.getTypes().forEach((type, entry) ->
                types.put(type, entry.getComponents() == null ? List.of() : List.copyOf(entry.getComponents())));

        Map<String, List<FieldOption>> components = new LinkedHashMap<>();
        types.values().stream().flatMap(List::stream).distinct()
                .forEach(component -> components.put(component, fieldsOf(catalog, component).stream()
                        .map(f -> new FieldOption(f.getName(), f.getType(), f.getOptions() == null ? List.of() : List.copyOf(f.getOptions())))
                        .toList()));
        return new FormFieldCatalogResponse(types, components);
    }

    // ── Colonnes configurables ──────────────────────────────────────────────────

    /** @param tableNames tables à décrire ; vide = tables sélectionnées, sinon toutes celles du script / de la base */
    public List<FormTableResponse> tables(List<String> tableNames) {
        FormConfiguration catalog = catalogOrFail();
        Map<String, Map<String, FieldConfiguration>> saved = store.getContext().getFormConfiguration();

        List<FormTableResponse> result = new ArrayList<>();
        for (TableMetadata table : loadTables(tableNames)) {
            ColumnMetadata[] columns = table.getColumns() == null ? new ColumnMetadata[0] : table.getColumns();
            List<String> numericColumns = Arrays.stream(columns)
                    .filter(c -> !c.isForeign() && c.isNumeric())
                    .map(FormConfigService::sqlName)
                    .toList();

            List<FormColumnResponse> configurable = new ArrayList<>();
            for (ColumnMetadata column : columns) {
                if (!FormFieldTypeResolver.isConfigurable(column, catalog)) {
                    continue;
                }
                String key = sqlName(column);
                FieldConfiguration current = FieldConfiguration.findConfiguration(saved, table.getTableName(), column);
                configurable.add(new FormColumnResponse(
                        key,
                        column.getName(),
                        column.getDatabaseColumnType() != null ? column.getDatabaseColumnType() : column.getColumnType(),
                        FormFieldTypeResolver.resolve(column),
                        FormFieldTypeResolver.components(column, catalog),
                        numericColumns.stream().filter(n -> !n.equalsIgnoreCase(key)).toList(),
                        current == null ? null : toDto(current)));
            }
            result.add(new FormTableResponse(table.getTableName(), configurable, columns.length - configurable.size()));
        }
        return result;
    }

    // ── Lecture / écriture ──────────────────────────────────────────────────────

    public FormConfigResponse current() {
        Map<String, Map<String, FieldConfiguration>> saved = store.getContext().getFormConfiguration();
        return response("Configuration actuelle", saved == null ? Map.of() : saved);
    }

    /** Remplace toute la configuration. */
    public synchronized FormConfigResponse replace(Map<String, Map<String, FieldConfigurationDto>> request) {
        Map<String, Map<String, FieldConfiguration>> validated = validate(request == null ? Map.of() : request);
        store.getContext().setFormConfiguration(validated);
        return response("Configuration des formulaires enregistrée", validated);
    }

    /** Remplace la configuration d'une seule table (les autres tables sont conservées). */
    public synchronized FormConfigResponse replaceTable(String tableName, Map<String, FieldConfigurationDto> columns) {
        Map<String, Map<String, FieldConfiguration>> validated =
                validate(Map.of(tableName, columns == null ? Map.of() : columns));

        Map<String, Map<String, FieldConfiguration>> merged = new LinkedHashMap<>();
        Map<String, Map<String, FieldConfiguration>> saved = store.getContext().getFormConfiguration();
        if (saved != null) {
            saved.forEach((table, conf) -> {
                if (!table.equalsIgnoreCase(tableName)) {
                    merged.put(table, conf);
                }
            });
        }
        merged.putAll(validated);
        store.getContext().setFormConfiguration(merged);
        return response("Configuration de la table " + tableName + " enregistrée", merged);
    }

    public synchronized FormConfigResponse clear() {
        store.getContext().setFormConfiguration(null);
        return response("Configuration des formulaires effacée", Map.of());
    }

    // ── Validation (mêmes règles que FormConfigurationForm du plugin) ───────────

    private Map<String, Map<String, FieldConfiguration>> validate(Map<String, Map<String, FieldConfigurationDto>> request) {
        FormConfiguration catalog = catalogOrFail();
        if (request.isEmpty()) {
            return new LinkedHashMap<>();
        }
        Map<String, TableMetadata> tables = new LinkedHashMap<>();
        for (TableMetadata t : loadTables(new ArrayList<>(request.keySet()))) {
            tables.put(t.getTableName().toLowerCase(Locale.ROOT), t);
        }

        Map<String, Map<String, FieldConfiguration>> result = new LinkedHashMap<>();
        for (var tableEntry : request.entrySet()) {
            TableMetadata table = tables.get(tableEntry.getKey().toLowerCase(Locale.ROOT));
            if (table == null) {
                throw new FormConfigException("Table inconnue : " + tableEntry.getKey());
            }
            ColumnMetadata[] columns = table.getColumns() == null ? new ColumnMetadata[0] : table.getColumns();
            Map<String, FieldConfiguration> tableResult = new LinkedHashMap<>();

            for (var columnEntry : (tableEntry.getValue() == null ? Map.<String, FieldConfigurationDto>of() : tableEntry.getValue()).entrySet()) {
                FieldConfigurationDto dto = columnEntry.getValue();
                if (dto == null || dto.component() == null || dto.component().isBlank()) {
                    continue;   // rendu par défaut : rien à enregistrer
                }
                String where = table.getTableName() + "." + columnEntry.getKey();
                ColumnMetadata column = findColumn(columns, columnEntry.getKey())
                        .orElseThrow(() -> new FormConfigException("Colonne inconnue : " + where));
                if (!FormFieldTypeResolver.isConfigurable(column, catalog)) {
                    throw new FormConfigException("Colonne non configurable (clé étrangère ou type non pris en charge) : " + where);
                }
                List<String> allowed = FormFieldTypeResolver.components(column, catalog);
                String component = dto.component().trim();
                if (!allowed.contains(component)) {
                    throw new FormConfigException("Composant « " + component + " » non autorisé pour " + where
                            + " (" + FormFieldTypeResolver.resolve(column) + ") ; autorisés : " + allowed);
                }

                FieldConfiguration field = new FieldConfiguration();
                field.setComponent(component);
                Map<String, FormConfiguration.Field> declared = new LinkedHashMap<>();
                fieldsOf(catalog, component).forEach(f -> declared.put(f.getName(), f));

                Map<String, FieldValue> values = dto.configuration() == null ? Map.of() : dto.configuration();
                for (var v : values.entrySet()) {
                    FormConfiguration.Field spec = declared.get(v.getKey());
                    if (spec == null) {
                        throw new FormConfigException("Paramètre « " + v.getKey() + " » inconnu pour le composant " + component
                                + " (" + where + ") ; attendus : " + declared.keySet());
                    }
                    String value = v.getValue() == null || v.getValue().value() == null ? "" : v.getValue().value().trim();
                    if (value.isEmpty()) {
                        continue;   // valeur vide = paramètre non renseigné (comme le plugin)
                    }
                    field.addConfig(spec.getName(), checkValue(spec, value, column, columns, where), spec.getType());
                }

                checkRanges(field, where);
                if (COMPONENT_MAP.equals(component)) {
                    checkMap(field, where);
                }
                tableResult.put(sqlName(column), field);
            }
            if (!tableResult.isEmpty()) {
                result.put(table.getTableName(), tableResult);
            }
        }
        return result;
    }

    private String checkValue(FormConfiguration.Field spec, String value, ColumnMetadata column, ColumnMetadata[] columns, String where) {
        String label = spec.getName() + " (" + where + ")";
        switch (spec.getType() == null ? "" : spec.getType()) {
            case FIELD_NUMBER -> {
                try {
                    return new BigDecimal(value.replace(',', '.')).stripTrailingZeros().toPlainString();
                } catch (NumberFormatException e) {
                    throw new FormConfigException("Nombre attendu pour " + label + " : " + value);
                }
            }
            case FIELD_SELECT -> {
                if (spec.getOptions() != null && !spec.getOptions().contains(value)) {
                    throw new FormConfigException("Valeur « " + value + " » invalide pour " + label + " ; choix : " + spec.getOptions());
                }
                return value;
            }
            case FIELD_MULTI_SELECT -> {
                List<String> picked = Arrays.stream(value.split(",")).map(String::trim).filter(s -> !s.isEmpty()).distinct().toList();
                for (String p : picked) {
                    if (spec.getOptions() != null && !spec.getOptions().contains(p)) {
                        throw new FormConfigException("Valeur « " + p + " » invalide pour " + label + " ; choix : " + spec.getOptions());
                    }
                }
                return String.join(",", picked);
            }
            case FIELD_COLUMN -> {
                ColumnMetadata other = findColumn(columns, value)
                        .orElseThrow(() -> new FormConfigException("Colonne inconnue pour " + label + " : " + value));
                if (sqlName(other).equalsIgnoreCase(sqlName(column))) {
                    throw new FormConfigException(label + " doit désigner une autre colonne que " + sqlName(column));
                }
                if (other.isForeign() || !other.isNumeric()) {
                    throw new FormConfigException(label + " doit désigner une colonne numérique : " + value);
                }
                return sqlName(other);
            }
            default -> {
                return value;
            }
        }
    }

    private void checkRanges(FieldConfiguration field, String where) {
        BigDecimal min = number(field, "min");
        BigDecimal max = number(field, "max");
        BigDecimal step = number(field, "step");
        if (min != null && max != null && min.compareTo(max) > 0) {
            throw new FormConfigException("min doit être inférieur ou égal à max (" + where + ")");
        }
        if (step != null && step.signum() <= 0) {
            throw new FormConfigException("step doit être strictement positif (" + where + ")");
        }
    }

    /** map : la colonne courante joue un rôle, l'autre coordonnée est donnée par latitude OU longitude. */
    private void checkMap(FieldConfiguration field, String where) {
        boolean hasLat = field.getConfiguration() != null && field.getConfiguration().containsKey("latitude");
        boolean hasLng = field.getConfiguration() != null && field.getConfiguration().containsKey("longitude");
        if (hasLat == hasLng) {
            throw new FormConfigException("Composant map (" + where + ") : renseignez soit « latitude » "
                    + "(la colonne courante est alors la longitude), soit « longitude » (la colonne courante est la latitude)");
        }
    }

    // ── Chargement des tables (script ou base connectée) ───────────────────────

    private List<TableMetadata> loadTables(List<String> requested) {
        ProjectGenerationContext source = store.getContext();
        if (source.getDatabase() == null) {
            throw new FormConfigException("Configurez d'abord la base de données (script ou connexion)");
        }
        if (source.getFramework() == null || source.getLanguage() == null) {
            throw new FormConfigException("Sélectionnez d'abord un framework (POST /api/generator/frameworks/{id}/select)");
        }

        boolean scriptMode = source.getSqlScript() != null && !source.getSqlScript().isBlank();
        try {
            if (scriptMode) {
                List<String> available = SQLRunner.extractTableNames(source.getSqlScript());
                List<String> names = pick(requested, source.getEntityNames(), available);
                ProjectGenerationContext copy = source.duplicateWithNoTables();   // ne modifie pas le contexte
                copy.setConnection(null);
                copy.setCredentials(null);
                copy.setEntityNames(new ArrayList<>(names));
                copy.setViewNames(new ArrayList<>());
                copy.setTables();
                return copy.getEntityTables() == null ? List.of() : copy.getEntityTables();
            }
            if (source.getConnection() == null) {
                throw new FormConfigException("Aucune base connectée et aucun script envoyé");
            }
            List<TableMetadata> all = source.getDatabase().getEntities(
                    source.getConnection(), source.getCredentials(), source.getLanguage(), source.getFramework());
            List<String> names = pick(requested, source.getEntityNames(), all.stream().map(TableMetadata::getTableName).toList());
            return all.stream().filter(t -> names.stream().anyMatch(n -> n.equalsIgnoreCase(t.getTableName()))).toList();
        } catch (FormConfigException e) {
            throw e;
        } catch (Exception e) {
            throw new FormConfigException("Impossible de lire les colonnes : " + e.getMessage());
        }
    }

    private static List<String> pick(List<String> requested, List<String> selected, List<String> available) {
        List<String> base = requested != null && !requested.isEmpty() ? requested
                : selected != null && !selected.isEmpty() ? selected
                : available;
        List<String> result = new ArrayList<>();
        for (String name : base) {
            available.stream().filter(a -> a.equalsIgnoreCase(name)).findFirst().ifPresent(result::add);
        }
        return result;
    }

    // ── Utilitaires ─────────────────────────────────────────────────────────────

    private static FormConfiguration catalogOrFail() {
        FormConfiguration catalog = ProjectGenerator.formConfiguration;
        if (catalog == null || catalog.getTypes() == null) {
            throw new FormConfigException("Catalogue form-field-configuration.json introuvable");
        }
        return catalog;
    }

    private static List<FormConfiguration.Field> fieldsOf(FormConfiguration catalog, String component) {
        if (catalog.getComponents() == null) {
            return List.of();
        }
        FormConfiguration.Configuration c = catalog.getComponents().get(component);
        if (c == null || c.getConfiguration() == null || c.getConfiguration().getFields() == null) {
            return List.of();
        }
        return c.getConfiguration().getFields();
    }

    /** Clé de colonne : nom SQL (referencedColumn), comme le plugin et FieldConfiguration.findConfiguration. */
    private static String sqlName(ColumnMetadata column) {
        String ref = column.getReferencedColumn();
        return ref != null && !ref.isBlank() ? ref : column.getName();
    }

    private static Optional<ColumnMetadata> findColumn(ColumnMetadata[] columns, String key) {
        return Arrays.stream(columns)
                .filter(c -> key.equalsIgnoreCase(sqlName(c)) || key.equalsIgnoreCase(c.getName()))
                .findFirst();
    }

    private static BigDecimal number(FieldConfiguration field, String name) {
        if (field.getConfiguration() == null || !field.getConfiguration().containsKey(name)) {
            return null;
        }
        try {
            return new BigDecimal(field.getConfiguration().get(name).getValue());
        } catch (Exception e) {
            return null;
        }
    }

    private static FieldConfigurationDto toDto(FieldConfiguration field) {
        Map<String, FieldValue> values = new LinkedHashMap<>();
        if (field.getConfiguration() != null) {
            field.getConfiguration().forEach((k, v) -> values.put(k, new FieldValue(v.getValue(), v.getType())));
        }
        return new FieldConfigurationDto(field.getComponent(), values);
    }

    private static FormConfigResponse response(String message, Map<String, Map<String, FieldConfiguration>> conf) {
        Map<String, Map<String, FieldConfigurationDto>> dto = new LinkedHashMap<>();
        int count = 0;
        for (var t : conf.entrySet()) {
            Map<String, FieldConfigurationDto> cols = new LinkedHashMap<>();
            t.getValue().forEach((c, f) -> cols.put(c, toDto(f)));
            count += cols.size();
            dto.put(t.getKey(), cols);
        }
        return new FormConfigResponse(true, message, count, dto);
    }
}
