package org.labs.genesis.api.controller;

import org.labs.genesis.api.dto.FormConfigDtos.*;
import org.labs.genesis.api.service.FormConfigService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Étape « Form configuration » (FormConfigurationWizardStep du plugin IntelliJ).
 *
 * Ordre conseillé : … > script-database-config (ou connexion) > project-config > frontend-config
 *                   > GET form-config/tables > PUT form-config > … > launch
 */
@RestController
public class FormConfigController {

    private final FormConfigService service;

    public FormConfigController(FormConfigService service) {
        this.service = service;
    }

    /** Catalogue : composants par catégorie de colonne et paramètres de chaque composant. */
    @GetMapping("/api/frontend/form-field-configuration")
    public FormFieldCatalogResponse catalog() {
        return service.catalog();
    }

    /** Colonnes configurables des tables (toutes celles sélectionnées si ?tables est absent). */
    @GetMapping("/api/generator/form-config/tables")
    public List<FormTableResponse> tables(@RequestParam(name = "tables", required = false) List<String> tables) {
        return service.tables(tables);
    }

    @GetMapping("/api/generator/form-config")
    public FormConfigResponse current() {
        return service.current();
    }

    /** Remplace toute la configuration : { table: { colonne: { component, configuration } } } */
    @PutMapping("/api/generator/form-config")
    public FormConfigResponse replace(@RequestBody Map<String, Map<String, FieldConfigurationDto>> request) {
        return service.replace(request);
    }

    /** Remplace la configuration d'une table : { colonne: { component, configuration } } */
    @PutMapping("/api/generator/form-config/{table}")
    public FormConfigResponse replaceTable(@PathVariable("table") String table,
                                           @RequestBody Map<String, FieldConfigurationDto> request) {
        return service.replaceTable(table, request);
    }

    @DeleteMapping("/api/generator/form-config")
    public FormConfigResponse clear() {
        return service.clear();
    }
}
