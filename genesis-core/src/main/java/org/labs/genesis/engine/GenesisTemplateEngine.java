package org.labs.genesis.engine;

import org.labs.utils.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Pattern;

public class GenesisTemplateEngine {


    private static final String LOOP_START = "{{#each ";
    private static final String LOOP_INDEX = "@index";
    private static final String IS_LOOP_FIRST_INDEX = "@first";
    private static final String IS_LOOP_LAST_INDEX = "@last";
    private static final String LOOP_ITEM = "this";
    private static final String LOOP_END = "{{/each}}";

    private static final String IF_START = "{{#if ";
    private static final String ELSE_IF_TOKEN = "{{elseIf ";
    private static final String ELSE_TOKEN = "{{else}}";
    private static final String IF_END = "{{/if}}";

    private static final String VARIABLE_PLACEHOLDER_PREFIX = "${";

    private static final String VARIABLE_PLACEHOLDER_SUFFIX = "}";
    private static final String VARIABLE_PLACEHOLDER_PREFIX_ALT = "$[";
    private static final String VARIABLE_PLACEHOLDER_SUFFIX_ALT = "]";

    private static final String NEWLINE_TAG = "{{newline}}";

    private static final String BLOCK_END = "}}";
    private static final String TAB_TAG = "{{tab}}";
    private static final String START_TAB_TAG = "{{tab";

    private static final String FUNCTION_OPEN_PARENTHESIS = "(";
    private static final String FUNCTION_CLOSED_PARENTHESIS = ")";
    private static final String REMOVE_LINE_TAG = "{{removeLine}}";

    private static final String START_COMMENTARY_TAG = "<#";
    private static final String END_COMMENTARY_TAG = "/#>";
    // Liste des opérateurs de comparaison supportés, du plus long au plus court
    // Définir ces opérateurs comme des constantes de classe
    private static final String GREATER_EQUAL_OPERATOR = ">=";
    private static final String LESS_EQUAL_OPERATOR = "<=";
    private static final String NOT_EQUAL_OPERATOR = "!=";
    private static final String EQUAL_OPERATOR = "=";
    private static final String GREATER_OPERATOR = ">";
    private static final String LESS_OPERATOR = "<";


    private static final Map<String, Function<String, String>> FUNCTIONS_MAP = new HashMap<>();

    static {
        FUNCTIONS_MAP.put("upperCase", str -> str == null ? "" : str.toUpperCase());
        FUNCTIONS_MAP.put("lowerCase", str -> str == null ? "" : str.toLowerCase());
        FUNCTIONS_MAP.put("majStart", StringUtils::majStart);
        FUNCTIONS_MAP.put("minStart", StringUtils::minStart);
        FUNCTIONS_MAP.put("toCamelCase", StringUtils::toCamelCase);
        FUNCTIONS_MAP.put("toKebabCase", StringUtils::toKebabCase);
        FUNCTIONS_MAP.put("formatReadable", StringUtils::formatReadable);
        FUNCTIONS_MAP.put("formatReadableLowerCase", StringUtils::formatReadableLowerCase);
    }

    private final Map<String, String> commentMap = new HashMap<>();

    public String simpleRender(String template, Map<String, Object> variables) {
        if (template == null || template.isEmpty()) {
            throw new IllegalArgumentException("The template must not be empty.");
        }

        StringBuilder result = new StringBuilder(template);

        int start = 0;
        while ((start = result.indexOf(VARIABLE_PLACEHOLDER_PREFIX, start)) != -1) {
            int end = result.indexOf(VARIABLE_PLACEHOLDER_SUFFIX, start);
            if (end == -1) break;

            String placeholder = result.substring(start + VARIABLE_PLACEHOLDER_PREFIX.length(), end).trim();
            String value = evaluatePlaceholderSimple(placeholder, variables);
            result.replace(start, end + VARIABLE_PLACEHOLDER_SUFFIX.length(), value);

            start += value.length();
        }

        return result.toString();
    }

    public String simpleRenderAlt(String template, Map<String, Object> variables) {
        if (template == null || template.isEmpty()) {
            throw new IllegalArgumentException("The template must not be empty.");
        }

        StringBuilder result = new StringBuilder(template);

        int start = 0;
        while ((start = result.indexOf(VARIABLE_PLACEHOLDER_PREFIX_ALT, start)) != -1) {
            int end = result.indexOf(VARIABLE_PLACEHOLDER_SUFFIX_ALT, start);
            if (end == -1) break;

            String placeholder = result.substring(start + VARIABLE_PLACEHOLDER_PREFIX_ALT.length(), end).trim();
            String value = evaluatePlaceholderSimple(placeholder, variables);
            result.replace(start, end + VARIABLE_PLACEHOLDER_SUFFIX_ALT.length(), value);

            start += value.length();
        }

        return result.toString();
    }

    private String evaluatePlaceholderSimple(String placeholder, Map<String, Object> variables) {
        int funcStart = placeholder.indexOf(FUNCTION_OPEN_PARENTHESIS);
        int funcEnd = placeholder.indexOf(FUNCTION_CLOSED_PARENTHESIS);

        if (funcStart != -1 && funcEnd != -1 && funcStart < funcEnd) {
            String functionName = placeholder.substring(0, funcStart).trim();
            String variableName = placeholder.substring(funcStart + 1, funcEnd).trim();

            Object valueObj = variables.get(variableName);
            String value = valueObj != null ? valueObj.toString() : VARIABLE_PLACEHOLDER_PREFIX + variableName + VARIABLE_PLACEHOLDER_SUFFIX;

            Function<String, String> function = FUNCTIONS_MAP.get(functionName);
            if (function != null) {
                return function.apply(value);
            } else {
                return VARIABLE_PLACEHOLDER_PREFIX + placeholder + VARIABLE_PLACEHOLDER_SUFFIX;
            }
        } else {
            Object valueObj = variables.get(placeholder);
            return valueObj != null ? valueObj.toString() : VARIABLE_PLACEHOLDER_PREFIX + placeholder + VARIABLE_PLACEHOLDER_SUFFIX;
        }
    }

    public String render(String template, Map<String, Object> variables) throws Exception {
        if (template == null || template.isEmpty()) {
            throw new IllegalArgumentException("The template must not be empty.");
        }

        if (variables == null || variables.isEmpty()) {
            return template;
        }

        StringBuilder result = new StringBuilder(template);

        protectComments(result);

        evaluateLoops(result, variables);
        evaluateConditionals(result, variables);
        replaceVariables(result, variables);
        processSpecialTags(result);

        restoreComments(result);

        return result.toString();
    }

    private void protectComments(StringBuilder template) {
        int startIndex = 0;
        while ((startIndex = template.indexOf(START_COMMENTARY_TAG, startIndex)) != -1) {
            int endIndex = template.indexOf(END_COMMENTARY_TAG, startIndex);
            if (endIndex == -1) break;

            endIndex += END_COMMENTARY_TAG.length();

            String comment = template.substring(startIndex, endIndex);
            String marker = generateUniqueMarker(startIndex);
            commentMap.put(marker, comment);

            template.replace(startIndex, endIndex, marker);
            startIndex += marker.length();
        }
    }

    public void dropCommentary(StringBuilder template) {
        replaceAllOccurrences(template, START_COMMENTARY_TAG, "");
        replaceAllOccurrences(template, END_COMMENTARY_TAG, "");
    }

    private void restoreComments(StringBuilder template) {
        for (Map.Entry<String, String> entry : commentMap.entrySet()) {
            String marker = entry.getKey();
            String comment = entry.getValue();

            int index = template.indexOf(marker);
            if (index != -1) {
                template.replace(index, index + marker.length(), comment);
            }
        }
        commentMap.clear();
    }

    private String generateUniqueMarker(int position) {
        return String.format("__COMMENT_%d__", position);
    }

    private void evaluateLoops(StringBuilder template, Map<String, Object> variables) throws Exception {
        int start;
        while ((start = template.indexOf(LOOP_START)) != -1) {
            LoopInfo loopInfo = extractLoopInfo(template, start);
            processLoopContent(template, loopInfo, variables);
        }
    }

    private LoopInfo extractLoopInfo(StringBuilder template, int start) throws Exception {
        int loopEndIdx = -1;
        int nestedCount = 0;

        for (int i = start; i < template.length(); i++) {
            if (template.substring(i).startsWith(LOOP_START)) {
                nestedCount++;
            } else if (template.substring(i).startsWith(LOOP_END)) {
                nestedCount--;
                if (nestedCount == 0) {
                    loopEndIdx = i;
                    break;
                }
            }
        }

        if (loopEndIdx == -1) {
            System.out.println("Template: " + template);
            throw new Exception("Loop end tag not found.");
        }

        int loopVarStartIdx = start + LOOP_START.length();
        int loopVarEndIdx = template.indexOf(BLOCK_END, loopVarStartIdx);
        String loopVarName = template.substring(loopVarStartIdx, loopVarEndIdx).trim();

        int contentStartIdx = loopVarEndIdx + BLOCK_END.length();
        String loopContent = template.substring(contentStartIdx, loopEndIdx);

        template.delete(start, loopEndIdx + LOOP_END.length());

        return new LoopInfo(loopVarName, loopContent, start);
    }

    @SuppressWarnings("unchecked")
    private void processLoopContent(StringBuilder template, LoopInfo loopInfo, Map<String, Object> variables) throws Exception {
        String loopVarName = loopInfo.loopVarName();
        String loopContent = loopInfo.loopContent();
        int start = loopInfo.start();

        Object loopVar = variables.get(loopVarName);
        if (!(loopVar instanceof List<?> loopList)) {
            return; // Ignore if loop variable is not a List
        }

        StringBuilder loopResult = new StringBuilder();

        for (int i = 0; i < loopList.size(); i++) {
            Object item = loopList.get(i);
            Map<String, Object> loopVariables = new HashMap<>(variables);

            // Ajouter directement l'élément actuel sous `this`
            loopVariables.put(LOOP_ITEM, item);

            // Ne stocker que les valeurs sous 'this' et pas toute la structure de l'objet
            if (item instanceof Map) {
                Map<String, Object> itemMap = (Map<String, Object>) item;
                for (Map.Entry<String, Object> entry : itemMap.entrySet()) {
                    loopVariables.put(LOOP_ITEM + "." + entry.getKey(), entry.getValue());
                }
            }

            loopVariables.put(LOOP_INDEX, i);
            loopVariables.put(IS_LOOP_FIRST_INDEX, (i == 0));
            loopVariables.put(IS_LOOP_LAST_INDEX, (i == loopList.size() - 1));

            // Recursive rendering for nested structures
            String renderedContent = render(loopContent, loopVariables).stripLeading();
            loopResult.append(renderedContent);
        }

        template.insert(start, loopResult);
    }

    private void evaluateConditionals(StringBuilder template, Map<String, Object> variables) throws Exception {
        int start;
        while ((start = template.indexOf(IF_START)) != -1) {
            int ifEndIdx = findBlockEnd(template, start);
            if (ifEndIdx == -1) break;

            String condition = extractCondition(template, start);
            int contentStartIdx = findContentStartIdx(template, start);

            StringBuilder resultContent = new StringBuilder();
            if (evaluateCondition(condition, variables)) {
                resultContent.append(extractIfContent(template, contentStartIdx, ifEndIdx));
            } else {
                processElseIfBlocks(template, variables, contentStartIdx, ifEndIdx, resultContent);
            }

            replaceBlockWithResult(template, start, ifEndIdx, resultContent);
        }
    }


    private int findBlockEnd(StringBuilder template, int start) {
        return template.indexOf(IF_END, start);
    }

    private String extractCondition(StringBuilder template, int start) {
        int conditionStartIdx = start + IF_START.length();
        int conditionEndIdx = template.indexOf(BLOCK_END, conditionStartIdx);
        return template.substring(conditionStartIdx, conditionEndIdx).trim();
    }

    private int findContentStartIdx(StringBuilder template, int start) {
        int conditionEndIdx = template.indexOf(BLOCK_END, start + IF_START.length());
        return conditionEndIdx + BLOCK_END.length();
    }

    private String extractIfContent(StringBuilder template, int contentStartIdx, int ifEndIdx) {
        int elseIfIdx = template.indexOf(ELSE_IF_TOKEN, contentStartIdx);
        int elseIdx = template.indexOf(ELSE_TOKEN, contentStartIdx);
        int ifBlockEnd = determineBlockEnd(elseIfIdx, elseIdx, ifEndIdx);
        return template.substring(contentStartIdx, ifBlockEnd);
    }

    private void processElseIfBlocks(StringBuilder template, Map<String, Object> variables,
                                     int contentStartIdx, int ifEndIdx, StringBuilder resultContent) throws Exception {
        int elseIfIdx = template.indexOf(ELSE_IF_TOKEN, contentStartIdx);
        int elseIdx = template.indexOf(ELSE_TOKEN, contentStartIdx);
        boolean conditionMatched = false;

        while (elseIfIdx != -1 && elseIfIdx < ifEndIdx && !conditionMatched) {
            String elseIfCondition = extractElseIfCondition(template, elseIfIdx);
            contentStartIdx = findContentStartIdx(template, elseIfIdx);

            int blockEnd = determineNextBlockEnd(template, contentStartIdx, ifEndIdx);
            String falseContent = template.substring(contentStartIdx, blockEnd);

            if (evaluateCondition(elseIfCondition, variables)) {
                resultContent.append(falseContent);
                conditionMatched = true;
            }

            elseIfIdx = template.indexOf(ELSE_IF_TOKEN, contentStartIdx);
        }

        if (!conditionMatched) {
            processElseBlock(template, elseIdx, ifEndIdx, resultContent);
        }
    }

    private String extractElseIfCondition(StringBuilder template, int elseIfIdx) {
        int elseIfConditionEndIdx = template.indexOf(BLOCK_END, elseIfIdx);
        return template.substring(elseIfIdx + ELSE_IF_TOKEN.length(), elseIfConditionEndIdx).trim();
    }

    private int determineNextBlockEnd(StringBuilder template, int contentStartIdx, int ifEndIdx) {
        int nextElseIfIdx = template.indexOf(ELSE_IF_TOKEN, contentStartIdx);
        int nextElseIdx = template.indexOf(ELSE_TOKEN, contentStartIdx);
        return determineBlockEnd(nextElseIfIdx, nextElseIdx, ifEndIdx);
    }

    private void processElseBlock(StringBuilder template, int elseIdx, int ifEndIdx, StringBuilder resultContent) {
        if (elseIdx != -1 && elseIdx < ifEndIdx) {
            String elseContent = template.substring(elseIdx + ELSE_TOKEN.length(), ifEndIdx);
            resultContent.append(elseContent);
        }
    }

    private int determineBlockEnd(int elseIfIdx, int elseIdx, int ifEndIdx) {
        if (elseIfIdx != -1 && elseIfIdx < ifEndIdx) {
            return elseIfIdx;
        }
        if (elseIdx != -1 && elseIdx < ifEndIdx) {
            return elseIdx;
        }
        return ifEndIdx;
    }

    private void replaceBlockWithResult(StringBuilder template, int start, int ifEndIdx, StringBuilder resultContent) {
        template.delete(start, ifEndIdx + IF_END.length());
        template.insert(start, resultContent);
    }


    private void replaceVariables(StringBuilder template, Map<String, Object> variables) {
        int start;
        while ((start = template.indexOf(VARIABLE_PLACEHOLDER_PREFIX)) != -1) {
            int end = template.indexOf(VARIABLE_PLACEHOLDER_SUFFIX, start);
            if (end == -1) break;

            String placeholder = template.substring(start + VARIABLE_PLACEHOLDER_PREFIX.length(), end).trim();
            String value = evaluatePlaceholder(placeholder, variables);
            template.replace(start, end + VARIABLE_PLACEHOLDER_SUFFIX.length(), value);
        }
    }

    private String evaluatePlaceholder(String placeholder, Map<String, Object> variables) {
        int funcStart = placeholder.indexOf("(");
        int funcEnd = placeholder.indexOf(")");

        if (funcStart != -1 && funcEnd != -1 && funcStart < funcEnd) {
            String functionName = placeholder.substring(0, funcStart).trim();
            String variableName = placeholder.substring(funcStart + 1, funcEnd).trim();

            Object valueObj = variables.get(variableName);
            String value = valueObj != null ? valueObj.toString() : "";


            Function<String, String> function = FUNCTIONS_MAP.get(functionName);
            if (function != null) {
                return function.apply(value);
            } else {
                return value;
            }
        } else {

            Object valueObj = variables.get(placeholder);
            return valueObj != null ? valueObj.toString() : "";
        }
    }


    private void replaceAllOccurrences(StringBuilder template, String placeholder, String value) {
        int start;
        while ((start = template.indexOf(placeholder)) != -1) {
            template.replace(start, start + placeholder.length(), value);
        }
    }

    private boolean evaluateCondition(String condition, Map<String, Object> variables) throws Exception {
        condition = condition.trim();

        if (condition.startsWith("!")) {
            String innerCondition = condition.substring(1).trim();
            return !evaluateCondition(innerCondition, variables);
        }

        return evaluateCompositeCondition(condition, variables);
    }

    private boolean evaluateCompositeCondition(String condition, Map<String, Object> variables) throws Exception {

        String[] orConditions = condition.split("\\s+or\\s+");
        for (String orCondition : orConditions) {

            String[] andConditions = orCondition.split("\\s+and\\s+");
            boolean andResult = true;
            for (String andCondition : andConditions) {

                andCondition = andCondition.trim();
                boolean result = evaluateSimpleCondition(andCondition, variables);
                andResult = andResult && result;
            }

            if (andResult) {
                return true;
            }
        }

        return false;
    }


    /**
     * Évalue une condition simple qui peut inclure des comparaisons.
     */
    private boolean evaluateSimpleCondition(String condition, Map<String, Object> variables) throws Exception {
        condition = condition.trim();

        // Cas des valeurs booléennes littérales
        final String TRUE_VALUE = "true";
        final String FALSE_VALUE = "false";
        if (TRUE_VALUE.equalsIgnoreCase(condition)) {
            return true;
        }
        if (FALSE_VALUE.equalsIgnoreCase(condition)) {
            return false;
        }

        // Cas spécial pour la variable spéciale du dernier élément d'une boucle
        if (IS_LOOP_LAST_INDEX.equals(condition)) {
            return evaluateLoopFlagValue(variables);
        }

        // Recherche d'opérateurs de comparaison
        String operator = findComparisonOperator(condition);

        if (operator != null) {
            // Si un opérateur est trouvé, évaluer la comparaison
            return evaluateComparison(condition, operator, variables);
        } else {
            // Comportement d'origine pour les conditions sans opérateurs
            Object conditionValue = variables.get(condition);
            if (conditionValue == null) {
                return false;
            }
            return conditionValue instanceof Boolean && (Boolean) conditionValue;
        }
    }

    /**
     * Évalue une valeur de drapeau de boucle (comme @last ou @first).
     */
    private boolean evaluateLoopFlagValue(Map<String, Object> variables) throws Exception {
        Object flagValue = variables.get(GenesisTemplateEngine.IS_LOOP_LAST_INDEX);
        if (flagValue instanceof Boolean) {
            return (Boolean) flagValue;
        } else {
            throw new Exception("Variable '" + GenesisTemplateEngine.IS_LOOP_LAST_INDEX + "' is not a boolean.");
        }
    }

    /**
     * Trouve le premier opérateur de comparaison dans la condition.
     * Retourne l'opérateur trouvé ou null si aucun opérateur n'est présent.
     */
    private String findComparisonOperator(String condition) {

        String[] operators = {
                GREATER_EQUAL_OPERATOR,
                LESS_EQUAL_OPERATOR,
                NOT_EQUAL_OPERATOR,
                EQUAL_OPERATOR,
                GREATER_OPERATOR,
                LESS_OPERATOR
        };

        for (String op : operators) {
            if (condition.contains(op)) {
                return op;
            }
        }

        return null;
    }

    /**
     * Évalue une comparaison avec un opérateur donné.
     */
    private boolean evaluateComparison(String condition, String operator, Map<String, Object> variables) throws Exception {
        String[] parts = condition.split(Pattern.quote(operator), 2);
        if (parts.length != 2) {
            throw new Exception("Invalid comparison condition: " + condition);
        }

        String leftPart = parts[0].trim();
        String rightPart = parts[1].trim();

        // Extraire les valeurs des deux côtés
        Object leftValue = extractValue(leftPart, variables);
        Object rightValue = extractValue(rightPart, variables);

        // Appliquer l'opérateur de comparaison
        return compareValues(leftValue, rightValue, operator);
    }

    /**
     * Extrait la valeur d'une expression qui peut être une variable ou une valeur littérale.
     */
    private Object extractValue(String expression, Map<String, Object> variables) {
        var variable = variables.get(expression);

        // Vérifier si c'est une référence à une variable
        if (variable != null) {
            return variable;
        }

        // Essayer de convertir en nombre si possible
        return convertToNumericIfPossible(expression);
    }

    /**
     * Vérifie si l'expression est une variable délimitée par les préfixes et suffixes donnés.
     */
    private boolean isVariable(String expression, String prefix, String suffix) {
        return expression.startsWith(prefix) && expression.endsWith(suffix);
    }

    /**
     * Extrait le nom de la variable à partir de l'expression complète.
     */
    private String extractVariableName(String expression, String prefix, String suffix) {
        return expression.substring(
                prefix.length(),
                expression.length() - suffix.length()
        ).trim();
    }

    /**
     * Tente de convertir une chaîne en valeur numérique.
     */
    private Object convertToNumericIfPossible(String expression) {
        try {
            // Tenter la conversion en entier
            return Integer.parseInt(expression);
        } catch (NumberFormatException e1) {
            try {
                // Tenter la conversion en nombre décimal
                return Double.parseDouble(expression);
            } catch (NumberFormatException e2) {
                // Si ce n'est pas un nombre, retourner la chaîne comme valeur littérale
                return expression;
            }
        }
    }

    /**
     * Compare deux valeurs avec l'opérateur donné.
     */
    @SuppressWarnings("unchecked")
    private boolean compareValues(Object left, Object right, String operator) throws Exception {
        // Gestion des valeurs null
        if (left == null || right == null) {
            if (operator.equals(EQUAL_OPERATOR)) {
                return left == right; // Tous deux null
            } else if (operator.equals(NOT_EQUAL_OPERATOR)) {
                return left != right; // L'un est null, l'autre non
            } else {
                // Pour les autres opérateurs, on ne peut pas comparer avec null
                return false;
            }
        }

        // Si les types sont différents, mais numériques, convertir en Double pour la comparaison
        if ((left instanceof Number) && (right instanceof Number)) {
            double leftDouble = ((Number) left).doubleValue();
            double rightDouble = ((Number) right).doubleValue();

            return performNumericComparison(leftDouble, rightDouble, operator
            );
        }

        // Si les types sont différents et non numériques, convertir en String
        if (left.getClass() != right.getClass()) {
            return performStringComparison(left.toString(), right.toString(), operator
            );
        }

        // Si les objets sont comparables et du même type
        if (left instanceof Comparable) {
            return performComparableComparison((Comparable<Object>) left, right, operator
            );
        }

        // Pour les autres types, la seule comparaison possible est l'égalité
        if (operator.equals(EQUAL_OPERATOR)) {
            return Objects.equals(left, right);
        } else if (operator.equals(NOT_EQUAL_OPERATOR)) {
            return !Objects.equals(left, right);
        } else {
            throw new Exception("Cannot compare objects of type " + left.getClass().getName() +
                    " with operator " + operator);
        }
    }

    /**
     * Effectue une comparaison numérique.
     */
    private boolean performNumericComparison(double left, double right, String operator) {
        return switch (operator) {
            case GenesisTemplateEngine.EQUAL_OPERATOR -> left == right;
            case GenesisTemplateEngine.NOT_EQUAL_OPERATOR -> left != right;
            case GenesisTemplateEngine.GREATER_OPERATOR -> left > right;
            case GenesisTemplateEngine.LESS_OPERATOR -> left < right;
            case GenesisTemplateEngine.GREATER_EQUAL_OPERATOR -> left >= right;
            case GenesisTemplateEngine.LESS_EQUAL_OPERATOR -> left <= right;
            default -> false;
        };
    }

    /**
     * Effectue une comparaison de chaînes.
     */
    private boolean performStringComparison(String left, String right, String operator) throws Exception {
        return switch (operator) {
            case GenesisTemplateEngine.EQUAL_OPERATOR -> left.equals(right);
            case GenesisTemplateEngine.NOT_EQUAL_OPERATOR -> !left.equals(right);
            case GenesisTemplateEngine.GREATER_OPERATOR -> left.compareTo(right) > 0;
            case GenesisTemplateEngine.LESS_OPERATOR -> left.compareTo(right) < 0;
            case GenesisTemplateEngine.GREATER_EQUAL_OPERATOR -> left.compareTo(right) >= 0;
            case GenesisTemplateEngine.LESS_EQUAL_OPERATOR -> left.compareTo(right) <= 0;
            default -> throw new Exception("Unsupported operator: " + operator);
        };
    }

    /**
     * Effectue une comparaison avec des objets implémentant Comparable.
     */
    @SuppressWarnings("unchecked")
    private <T> boolean performComparableComparison(Comparable<T> left, Object right, String operator) throws Exception {
        int comparisonResult;
        try {
            comparisonResult = left.compareTo((T) right);
        } catch (ClassCastException e) {
            throw new Exception("Cannot compare objects of different types with operator " + operator);
        }

        return switch (operator) {
            case GenesisTemplateEngine.EQUAL_OPERATOR -> comparisonResult == 0;
            case GenesisTemplateEngine.NOT_EQUAL_OPERATOR -> comparisonResult != 0;
            case GenesisTemplateEngine.GREATER_OPERATOR -> comparisonResult > 0;
            case GenesisTemplateEngine.LESS_OPERATOR -> comparisonResult < 0;
            case GenesisTemplateEngine.GREATER_EQUAL_OPERATOR -> comparisonResult >= 0;
            case GenesisTemplateEngine.LESS_EQUAL_OPERATOR -> comparisonResult <= 0;
            default -> throw new Exception("Unsupported operator: " + operator);
        };
    }

    private void processSpecialTags(StringBuilder template) {
        replaceAllOccurrences(template, NEWLINE_TAG, "\n");
        replaceAllOccurrences(template, TAB_TAG, "\t");

        int tabTagIndex;
        while ((tabTagIndex = template.indexOf(START_TAB_TAG)) != -1) {
            int start = tabTagIndex + START_TAB_TAG.length();
            int end = template.indexOf(BLOCK_END, start);

            if (end != -1) {
                String tabCountString = template.substring(start, end).trim();

                try {
                    // Convertir la valeur en entier
                    int tabCount = Integer.parseInt(tabCountString);

                    // Générer les tabulations
                    String tabs = "\t".repeat(Math.max(0, tabCount));

                    // Remplacer le tag entier par les tabulations générées
                    template.replace(tabTagIndex, end + BLOCK_END.length(), tabs);
                } catch (NumberFormatException e) {
                    // Si la conversion échoue, ignorer et supprimer le tag
                    template.replace(tabTagIndex, end + BLOCK_END.length(), "");
                }
            } else {
                break; // Tag mal formé, sortir de la boucle
            }
        }

        // Supprimer les lignes contenant le tag {{removeLine}}
        int start;
        while ((start = template.indexOf(REMOVE_LINE_TAG)) != -1) {
            int lineStart = template.lastIndexOf("\n", start);
            int lineEnd = template.indexOf("\n", start);

            if (lineStart == -1) {
                lineStart = 0; // Si c'est le début du fichier
            } else {
                lineStart++; // Ne pas inclure le \n lui-même
            }

            if (lineEnd == -1) {
                lineEnd = template.length(); // Si c'est la dernière ligne du fichier
            }

            template.delete(lineStart, lineEnd + 1);
        }
    }

    private record LoopInfo(String loopVarName, String loopContent, int start) {
    }
}
