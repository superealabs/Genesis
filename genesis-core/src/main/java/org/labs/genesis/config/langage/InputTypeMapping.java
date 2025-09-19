package org.labs.genesis.config.langage;

import lombok.Getter;
import lombok.Setter;
import org.labs.genesis.config.Constantes;
import org.labs.genesis.connexion.model.ColumnMetadata;
import org.labs.genesis.engine.GenesisTemplateEngine;
import org.labs.utils.FileUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
            String validationType,
            Object validationValue,
            InputTypeMapping inputTypeMapping,
            GenesisTemplateEngine engine) throws Exception {

        String validation = inputTypeMapping.getValidations()
                .getOrDefault(validationType, null);
//        Map<String, Object> values = parseAnnotation((String) validationValue);

        if (validation == null) {
            return null;
        }

        switch (validationType) {
//            case "maxSize":
//            case "minAndMaxSize":
//                return engine.render(validation, values);
//
//            case "numericMinimumValue":
//            case "numericMinimumInclusiveValue":
//            case "numericMaximumValue":
//            case "numericMaximumInclusiveValue":
//            case "numericMinimumAndMaximumValue":
//                return engine.render(validation, values);
//
//            case "regexPattern":
//                return engine.render(validation, values);
//
//            case "defaultValue":
//                return engine.render(validation, values);

            default:
                return "";
        }
    }

    private static List<String> getInputValidations(ColumnMetadata field,
                                                    InputTypeMapping inputTypeMapping,
                                                    GenesisTemplateEngine engine) throws Exception {
        Map<String, Object> validations = field.getValidationAnnotations();
        List<String> results = new ArrayList<>();

        if (validations != null) {
            validations.forEach((type, value) -> {
                String validation = null;
                try {
                    validation = getInputValidationByModelValidation(type, value, inputTypeMapping, engine);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                if (!validation.isEmpty()) results.add(validation);
            });
        }

        return results;
    }

    private static String getInputType(ColumnMetadata field, InputTypeMapping inputTypeMapping) {
        return inputTypeMapping.getTypes().get(field.getType());
    }

    private static boolean getIsShowed(ColumnMetadata field) {
        return !field.isPrimary();
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
