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

    private static Map<String, Object> parseAnnotation(String annotation) {
        if (annotation == null || annotation.trim().isEmpty())
            return Map.of("type", "invalid", "error", "Annotation must not be null");

        if (annotation.contains("[Range(")) return parseRange(annotation);
        if (annotation.contains("[StringLength(")) return parseStringLength(annotation);
        if (annotation.contains("[RegularExpression("))
            return Map.of("type", "regex", "pattern", extract(annotation, "\"([^\"]+)\""), "original", annotation);
        if (annotation.contains("[DefaultValue("))
            return Map.of("type", "defaultValue", "value", extract(annotation, "\"([^\"]+)\""), "original", annotation);

        return Map.of("type", "unknown", "original", annotation);
    }

    private static Map<String, Object> parseRange(String annotation) {
        Matcher matcher = Pattern.compile("\\[Range\\(([^,]+),\\s*([^)]+)\\)\\]").matcher(annotation);
        if (!matcher.find()) return Map.of("type", "range", "error", "Invalid Range format");

        return Map.of("type", "range", "min", parseNumericValue(matcher.group(1)),
                "max", parseNumericValue(matcher.group(2)), "original", annotation);
    }

    private static Map<String, Object> parseStringLength(String annotation) {
        Matcher matcher = Pattern.compile("\\[StringLength\\(([^,]+)(?:,\\s*MinimumLength\\s*=\\s*([^)]+))?\\)\\]").matcher(annotation);
        if (!matcher.find()) return Map.of("type", "stringLength", "error", "Invalid StringLength format");

        Map<String, Object> result = new HashMap<>();
        result.put("type", "stringLength");
        result.put("maxLength", Integer.parseInt(matcher.group(1).trim()));
        if (matcher.group(2) != null) result.put("minLength", Integer.parseInt(matcher.group(2).trim()));
        result.put("original", annotation);
        return result;
    }

    private static String extract(String text, String regex) {
        Matcher m = Pattern.compile(regex).matcher(text);
        return m.find() ? m.group(1) : "";
    }

    private static Object parseNumericValue(String value) {
        value = value.trim();
        Map<String, Double> constants = Map.of(
                "double.MaxValue", Double.MAX_VALUE, "double.MinValue", Double.MIN_VALUE,
                "int.MaxValue", (double) Integer.MAX_VALUE, "int.MinValue", (double) Integer.MIN_VALUE
        );

        if (constants.containsKey(value)) return constants.get(value);
        try { return Double.parseDouble(value); }
        catch (NumberFormatException e) { return value; }
    }

    private static String getInputValidationByModelValidation(
            String validationType,
            Object validationValue,
            InputTypeMapping inputTypeMapping,
            GenesisTemplateEngine engine) throws Exception {

        String validation = inputTypeMapping.getValidations()
                .getOrDefault(validationType, null);
        Map<String, Object> values = parseAnnotation((String) validationValue);

        if (validation == null) {
            return null;
        }

        switch (validationType) {
            case "maxSize":
            case "minAndMaxSize":
                return engine.render(validation, values);

            case "numericMinimumValue":
            case "numericMinimumInclusiveValue":
            case "numericMaximumValue":
            case "numericMaximumInclusiveValue":
            case "numericMinimumAndMaximumValue":
                return engine.render(validation, values);

            case "regexPattern":
                return engine.render(validation, values);

            case "defaultValue":
                return engine.render(validation, values);

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
        return !(field.getValidationAnnotations().containsKey("defaultValue") && field.isPrimary());
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
