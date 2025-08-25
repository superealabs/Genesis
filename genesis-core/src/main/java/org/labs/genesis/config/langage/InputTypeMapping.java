package org.labs.genesis.config.langage;

import lombok.Getter;
import lombok.Setter;
import org.labs.genesis.config.Constantes;
import org.labs.genesis.connexion.model.ColumnMetadata;
import org.labs.genesis.engine.GenesisTemplateEngine;
import org.labs.utils.FileUtils;

import java.util.*;

@Getter
@Setter
public class InputTypeMapping {
    private int id;
    private int languageId;
    private Map<String, String> types;
    private Map<String, String> validations;

    private static Optional<InputTypeMapping> getInputTypeMapping(int languageId) {
        try {
            return Arrays.stream(FileUtils.fromJson(InputTypeMapping[].class, Constantes.INPUT_TYPE_MAPPING_JSON))
                    .filter(itm -> itm.getLanguageId() == languageId)
                    .findFirst();
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    private static String getInputValidationByModelValidation(
            ColumnMetadata field,
            String modelValidation,
            InputTypeMapping inputTypeMapping,
            GenesisTemplateEngine engine) throws Exception {

        String validation = inputTypeMapping.getValidations()
                .getOrDefault(modelValidation, null);

        if (validation == null) {
            return null;
        }

        if ("maxSize".equalsIgnoreCase(modelValidation)) {
            Map<String, Object> context = Map.of("columnSize", field.getColumnSize());
            return engine.render(validation, context);
        }

        return validation;
    }

    private static List<String> getInputValidations(ColumnMetadata field,
                                                    InputTypeMapping inputTypeMapping,
                                                    GenesisTemplateEngine engine) throws Exception {
        return field.getValidationAnnotations().keySet().stream()
                .map(key -> {
                    try {
                        return getInputValidationByModelValidation(field, key, inputTypeMapping, engine);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .filter(Objects::nonNull)
                .toList();
    }

    private static String getInputType(ColumnMetadata field, InputTypeMapping inputTypeMapping) {
        return inputTypeMapping.getTypes().get(field.getType());
    }

    private static boolean getIsShowed(ColumnMetadata field) {
        return !(field.getValidationAnnotations().containsKey("defaultValue"));
    }

    private static boolean getIsRequired(ColumnMetadata field) {
        return field.getValidationAnnotations().containsKey("notNull")
                || field.getValidationAnnotations().containsKey("notBlank");
    }

    public static Input getInput(ColumnMetadata field, Language language, GenesisTemplateEngine engine) throws Exception {
        Input input = new Input();

        Optional<InputTypeMapping> inputTypeMapping = getInputTypeMapping(language.getId());

        input.setType(inputTypeMapping.map(mapping -> getInputType(field, mapping)).orElse(null));
        input.setValidations(inputTypeMapping.map(mapping -> {
            try {
                return getInputValidations(field, mapping, engine);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).orElse(List.of()));

        String fieldName = field.getName();
        input.setName(fieldName);
        input.setId(fieldName);
        input.setPlaceholder(fieldName);

        input.setIsShowed(getIsShowed(field));
        input.setIsRequired(getIsRequired(field));

        return input;
    }


    @Getter
    @Setter
    public static class Input {
        private String name;
        private String id;
        private String type;
        private List<String> validations;
        private String placeholder;
        private Boolean isShowed;
        private Boolean isRequired;
    }
}
