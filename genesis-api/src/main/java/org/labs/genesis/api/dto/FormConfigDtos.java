package org.labs.genesis.api.dto;

import java.util.List;
import java.util.Map;

/**
 * Étape « Form configuration » (même étape que FormConfigurationWizardStep du plugin IntelliJ) :
 * choisir comment chaque colonne est rendue dans les formulaires générés.
 *
 * Format stocké (identique au plugin, lu par FieldConfiguration.findConfiguration) :
 * { "table": { "colonne_sql": { "component": "currency",
 *                               "configuration": { "currency": { "value": "EUR", "type": "select" } } } } }
 */
public final class FormConfigDtos {
    private FormConfigDtos() {}

    // ── Catalogue (form-field-configuration.json) ──────────────────────────────

    /** Paramètre d'un composant : type "number", "select", "multi-select" ou "column". */
    public record FieldOption(String name, String type, List<String> options) {}

    public record FormFieldCatalogResponse(
            Map<String, List<String>> types,               // "number" -> [number, range, quantity, currency, map]
            Map<String, List<FieldOption>> components      // "currency" -> [currency, min, max, step]
    ) {}

    // ── Colonnes configurables ──────────────────────────────────────────────────

    public record FormColumnResponse(
            String column,                    // nom SQL (clé à utiliser dans PUT)
            String name,                      // nom de l'attribut généré (camelCase)
            String sqlType,
            String category,                  // number | text | binary | json
            List<String> components,          // composants proposés pour cette colonne
            List<String> coordinateColumns,   // colonnes numériques de la table (champ "column" du composant map)
            FieldConfigurationDto configured  // configuration enregistrée, ou null (rendu par défaut)
    ) {}

    public record FormTableResponse(
            String table,
            List<FormColumnResponse> columns,
            int hiddenColumns                 // clés étrangères et types non configurables
    ) {}

    // ── Configuration ───────────────────────────────────────────────────────────

    public record FieldValue(String value, String type) {}

    /** component vide ou null = rendu par défaut (la colonne n'est pas enregistrée). */
    public record FieldConfigurationDto(String component, Map<String, FieldValue> configuration) {}

    public record FormConfigResponse(
            boolean success,
            String message,
            int configuredColumns,
            Map<String, Map<String, FieldConfigurationDto>> formConfiguration
    ) {}
}
