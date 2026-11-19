package org.labs.genesis.engine;

import org.labs.utils.StringUtils;

import java.util.ArrayList;
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
    private static final String TAB_TAG = "{{tab}}";
    private static final String SPACE_TAG = "{{space}}";
    private static final String REMOVE_LINE_TAG = "{{removeLine}}";
    private static final String BLOCK_END = "}}";
    private static final String FUNCTION_OPEN_PARENTHESIS = "(";
    private static final String FUNCTION_CLOSED_PARENTHESIS = ")";
    private static final String START_COMMENTARY_TAG = "<#";
    private static final String END_COMMENTARY_TAG = "/#>";
    // Liste des opérateurs de comparaison supportés, du plus long au plus court
    private static final String GREATER_EQUAL_OPERATOR = ">=";
    private static final String LESS_EQUAL_OPERATOR = "<=";
    private static final String NOT_EQUAL_OPERATOR = "!=";
    private static final String EQUAL_OPERATOR = "=";
    private static final String GREATER_OPERATOR = ">";
    private static final String LESS_OPERATOR = "<";
    private static final int MAX_CACHED_PLANS = 128;
    private static final int MAX_PLAN_DEPTH = 128;


    private static final Map<String, Function<String, String>> FUNCTIONS_MAP = new HashMap<>();

    static {
        FUNCTIONS_MAP.put("upperCase", str -> str == null ? "" : str.toUpperCase());
        FUNCTIONS_MAP.put("lowerCase", str -> str == null ? "" : str.toLowerCase());
        FUNCTIONS_MAP.put("majStart", StringUtils::majStart);
        FUNCTIONS_MAP.put("minStart", StringUtils::minStart);
        FUNCTIONS_MAP.put("removeLastS", StringUtils::removeLastS);
        FUNCTIONS_MAP.put("toCamelCase", StringUtils::toCamelCase);
        FUNCTIONS_MAP.put("toKebabCase", StringUtils::toKebabCase);
        FUNCTIONS_MAP.put("formatReadable", StringUtils::formatReadable);
        FUNCTIONS_MAP.put("formatReadableLowerCase", StringUtils::formatReadableLowerCase);
        FUNCTIONS_MAP.put("skipEscape", StringUtils::skipEscape);
        FUNCTIONS_MAP.put("pluralize", StringUtils::pluralize);
        FUNCTIONS_MAP.put("minPluralize", StringUtils::minPluralize);
        FUNCTIONS_MAP.put("majPluralize", StringUtils::majPluralize);
        FUNCTIONS_MAP.put("toPascalCase", StringUtils::toPascalCase);
    }

    /** Remplace les variables simples sans interpréter les blocs. */
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

    /** Remplace les variables utilisant les délimiteurs alternatifs. */
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

    /** Résout une variable simple ou sa fonction de transformation. */
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

    private final ThreadLocal<RenderContext> activeRender = new ThreadLocal<>();

    /** Exécute les étapes du rendu en partageant le contexte des récursions. */
    public String render(String template, Map<String, Object> variables) throws Exception {
        if (template == null || template.isEmpty()) {
            throw new IllegalArgumentException("The template must not be empty.");
        }

        StringBuilder result = new StringBuilder(template);
        Map<String, String> protectedComments = new HashMap<>();
        protectComments(result, protectedComments);

        if (variables == null || variables.isEmpty()) {
            restoreComments(result, protectedComments);
            return result.toString();
        }

        RenderContext context = activeRender.get();
        boolean ownsContext = context == null;
        // Le corps hérite du contexte parent même sans boucle interne.
        if (ownsContext && result.indexOf(LOOP_START) >= 0) {
            context = new RenderContext();
            activeRender.set(context);
        }

        try {
            evaluateLoops(result, variables, context);
            evaluateConditionals(result, variables, context);
            replaceVariables(result, variables);
            replaceVariables(result, variables);
            processSpecialTags(result);
            restoreComments(result, protectedComments);
            return result.toString();
        } finally {
            // get() crée aussi une entrée ThreadLocal vide sur le chemin léger.
            if (ownsContext) activeRender.remove();
        }
    }

    /** Masque les commentaires pour préserver leur contenu pendant le rendu. */
    private void protectComments(StringBuilder template, Map<String, String> protectedComments) {
        int startIndex = 0;
        while ((startIndex = template.indexOf(START_COMMENTARY_TAG, startIndex)) != -1) {
            int endIndex = template.indexOf(END_COMMENTARY_TAG, startIndex);
            if (endIndex == -1) break;

            String commentContent = template.substring(startIndex + START_COMMENTARY_TAG.length(), endIndex);
            int commentEndIndex = endIndex + END_COMMENTARY_TAG.length();
            String marker = generateUniqueMarker(startIndex);
            protectedComments.put(marker, commentContent);

            template.replace(startIndex, commentEndIndex, marker);
            startIndex += marker.length();
        }
    }

    /** Restaure les commentaires à partir de leurs marqueurs temporaires. */
    private void restoreComments(StringBuilder template, Map<String, String> protectedComments) {
        for (Map.Entry<String, String> entry : protectedComments.entrySet()) {
            String marker = entry.getKey();
            String commentContent = entry.getValue();

            replaceAllOccurrences(template, marker, commentContent);
        }
    }

    /** Construit le marqueur temporaire d’un commentaire. */
    private String generateUniqueMarker(int position) {
        return String.format("__COMMENT_%d__", position);
    }

    /** Développe les boucles présentes dans le template. */
    private void evaluateLoops(StringBuilder template, Map<String, Object> variables, RenderContext context) throws Exception {
        int start;
        while ((start = template.indexOf(LOOP_START)) != -1) {
            LoopInfo loopInfo = extractLoopInfo(template, start);
            processLoopContent(template, loopInfo, variables, context);
        }
    }

    /** Repère les limites et le contenu d’une boucle. */
    private LoopInfo extractLoopInfo(StringBuilder template, int start) throws Exception {
        int loopEndIdx = -1;
        int nestedCount = 0;

        for (int i = start; i < template.length(); i++) {
            if (tokenAt(template, LOOP_START, i)) {
                nestedCount++;
            } else if (tokenAt(template, LOOP_END, i)) {
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

    /** Rend chaque élément de la collection et remplace le bloc de boucle. */
    @SuppressWarnings("unchecked")
    private void processLoopContent(StringBuilder template, LoopInfo loopInfo, Map<String, Object> variables, RenderContext context) throws Exception {
        String loopVarName = loopInfo.loopVarName();
        String loopContent = loopInfo.loopContent();
        int start = loopInfo.start();

        Object loopVar = variables.get(loopVarName);
        if (!(loopVar instanceof List<?> loopList)) {
            return; // Ignorer les valeurs qui ne sont pas des listes.
        }

        StringBuilder loopResult = new StringBuilder();

        for (int i = 0; i < loopList.size(); i++) {
            Object item = loopList.get(i);
            Map<String, Object> loopVariables = new HashMap<>(variables);
            loopVariables.put(LOOP_ITEM, item);
            if (item instanceof Map) {
                Map<String, Object> itemMap = (Map<String, Object>) item;
                for (Map.Entry<String, Object> entry : itemMap.entrySet()) {
                    loopVariables.put(LOOP_ITEM + "." + entry.getKey(), entry.getValue());
                }
            }

            loopVariables.put(LOOP_INDEX, i);
            loopVariables.put(IS_LOOP_FIRST_INDEX, (i == 0));
            loopVariables.put(IS_LOOP_LAST_INDEX, (i == loopList.size() - 1));

            // Rendre récursivement les structures imbriquées.
            String renderedContent = render(loopContent, loopVariables).stripLeading();
            loopResult.append(renderedContent);
        }

        template.insert(start, loopResult);
    }

    /** Évalue les blocs conditionnels avec le parcours historique. */
    private void evaluateConditionalsLegacy(StringBuilder template, Map<String, Object> variables, RenderContext context) throws Exception {
        int start;
        while ((start = template.indexOf(IF_START)) != -1) {
            int ifEndIdx = findBlockEnd(template, start);
            if (ifEndIdx == -1) break;

            String condition = extractCondition(template, start);
            int contentStartIdx = findContentStartIdx(template, start);

            StringBuilder resultContent = new StringBuilder();
            if (evaluateCondition(condition, variables, context)) {
                resultContent.append(extractIfContent(template, contentStartIdx, ifEndIdx));
            } else {
                processElseIfBlocks(template, variables, contentStartIdx, ifEndIdx, resultContent, context);
            }

            replaceBlockWithResult(template, start, ifEndIdx, resultContent);
        }
    }


    /** Recherche la fermeture correspondant au niveau du bloc conditionnel. */
    private int findBlockEnd(StringBuilder template, int start) {
        int depth = 0;
        for (int index = start; index < template.length(); index++) {
            if (tokenAt(template, IF_START, index)) {
                depth++;
            } else if (tokenAt(template, IF_END, index)) {
                depth--;
                if (depth == 0) {
                    return index;
                }
            }
        }
        return -1;
    }

    /** Recherche une branche en ignorant les conditions imbriquées. */
    private int findBranchToken(StringBuilder template, String token, int start, int end) {
        int depth = 0;
        for (int index = start; index < end; index++) {
            if (tokenAt(template, IF_START, index)) {
                depth++;
            } else if (tokenAt(template, IF_END, index)) {
                depth--;
            } else if (depth == 0 && tokenAt(template, token, index)) {
                return index;
            }
        }
        return -1;
    }

    /** Compare un token à une position sans copier le suffixe du texte. */
    private boolean tokenAt(StringBuilder template, String token, int index) {
        if (index + token.length() > template.length()) {
            return false;
        }
        for (int offset = 0; offset < token.length(); offset++) {
            if (template.charAt(index + offset) != token.charAt(offset)) {
                return false;
            }
        }
        return true;
    }

    /** Extrait l’expression de l’en-tête conditionnel. */
    private String extractCondition(StringBuilder template, int start) {
        int conditionStartIdx = start + IF_START.length();
        int conditionEndIdx = template.indexOf(BLOCK_END, conditionStartIdx);
        return template.substring(conditionStartIdx, conditionEndIdx).trim();
    }

    /** Repère le début du corps après l’en-tête du bloc. */
    private int findContentStartIdx(StringBuilder template, int start) {
        int conditionEndIdx = template.indexOf(BLOCK_END, start + IF_START.length());
        return conditionEndIdx + BLOCK_END.length();
    }

    /** Extrait le contenu de la première branche conditionnelle. */
    private String extractIfContent(StringBuilder template, int contentStartIdx, int ifEndIdx) {
        int elseIfIdx = findBranchToken(template, ELSE_IF_TOKEN, contentStartIdx, ifEndIdx);
        int elseIdx = findBranchToken(template, ELSE_TOKEN, contentStartIdx, ifEndIdx);
        int ifBlockEnd = determineBlockEnd(elseIfIdx, elseIdx, ifEndIdx);
        return template.substring(contentStartIdx, ifBlockEnd);
    }

    /** Sélectionne la première branche alternative dont la condition est vraie. */
    private void processElseIfBlocks(StringBuilder template, Map<String, Object> variables,
                                     int contentStartIdx, int ifEndIdx, StringBuilder resultContent, RenderContext context) throws Exception {
        int elseIfIdx = findBranchToken(template, ELSE_IF_TOKEN, contentStartIdx, ifEndIdx);
        int elseIdx = findBranchToken(template, ELSE_TOKEN, contentStartIdx, ifEndIdx);
        boolean conditionMatched = false;

        while (elseIfIdx != -1 && elseIfIdx < ifEndIdx && !conditionMatched) {
            String elseIfCondition = extractElseIfCondition(template, elseIfIdx);
            contentStartIdx = findContentStartIdx(template, elseIfIdx);

            int blockEnd = determineNextBlockEnd(template, contentStartIdx, ifEndIdx);
            String falseContent = template.substring(contentStartIdx, blockEnd);

            if (evaluateCondition(elseIfCondition, variables, context)) {
                resultContent.append(falseContent);
                conditionMatched = true;
            }

            elseIfIdx = findBranchToken(template, ELSE_IF_TOKEN, contentStartIdx, ifEndIdx);
        }

        if (!conditionMatched) {
            processElseBlock(template, elseIdx, ifEndIdx, resultContent);
        }
    }

    /** Extrait la condition d’une branche elseIf. */
    private String extractElseIfCondition(StringBuilder template, int elseIfIdx) {
        int elseIfConditionEndIdx = template.indexOf(BLOCK_END, elseIfIdx);
        return template.substring(elseIfIdx + ELSE_IF_TOKEN.length(), elseIfConditionEndIdx).trim();
    }

    /** Repère la prochaine branche ou la fin du bloc courant. */
    private int determineNextBlockEnd(StringBuilder template, int contentStartIdx, int ifEndIdx) {
        int nextElseIfIdx = findBranchToken(template, ELSE_IF_TOKEN, contentStartIdx, ifEndIdx);
        int nextElseIdx = findBranchToken(template, ELSE_TOKEN, contentStartIdx, ifEndIdx);
        return determineBlockEnd(nextElseIfIdx, nextElseIdx, ifEndIdx);
    }

    /** Recopie le contenu de la branche else lorsqu’elle existe. */
    private void processElseBlock(StringBuilder template, int elseIdx, int ifEndIdx, StringBuilder resultContent) {
        if (elseIdx != -1 && elseIdx < ifEndIdx) {
            String elseContent = template.substring(elseIdx + ELSE_TOKEN.length(), ifEndIdx);
            resultContent.append(elseContent);
        }
    }

    /** Choisit la première limite parmi les branches et la fermeture. */
    private int determineBlockEnd(int elseIfIdx, int elseIdx, int ifEndIdx) {
        if (elseIfIdx != -1 && elseIfIdx < ifEndIdx) {
            return elseIfIdx;
        }
        if (elseIdx != -1 && elseIdx < ifEndIdx) {
            return elseIdx;
        }
        return ifEndIdx;
    }

    /** Remplace le bloc source par le contenu de la branche retenue. */
    private void replaceBlockWithResult(StringBuilder template, int start, int ifEndIdx, StringBuilder resultContent) {
        template.delete(start, ifEndIdx + IF_END.length());
        template.insert(start, resultContent);
    }


    /** Substitue les variables en réexaminant la frontière des insertions. */
    private void replaceVariables(StringBuilder template, Map<String, Object> variables) {
        int start = 0;
        while ((start = template.indexOf(VARIABLE_PLACEHOLDER_PREFIX, start)) != -1) {
            int end = template.indexOf(VARIABLE_PLACEHOLDER_SUFFIX, start);
            if (end == -1) break;

            String placeholder = template.substring(start + VARIABLE_PLACEHOLDER_PREFIX.length(), end).trim();
            String value = evaluatePlaceholder(placeholder, variables);
            template.replace(start, end + VARIABLE_PLACEHOLDER_SUFFIX.length(), value);
            // Rechercher aussi les placeholders formés à la jonction du texte.
            start = Math.max(0, start - VARIABLE_PLACEHOLDER_PREFIX.length() + 1);
        }
    }

    /** Résout la valeur ou la fonction d’un placeholder. */
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


    /** Remplace chaque occurrence du marqueur demandé. */
    private void replaceAllOccurrences(StringBuilder template, String placeholder, String value) {
        int start;
        while ((start = template.indexOf(placeholder)) != -1) {
            template.replace(start, start + placeholder.length(), value);
        }
    }

    /** Évalue une condition après traitement de la négation. */
    private boolean evaluateCondition(String condition, Map<String, Object> variables, RenderContext context) throws Exception {
        condition = condition.trim();

        if (condition.startsWith("!")) {
            String innerCondition = condition.substring(1).trim();
            return !evaluateCondition(innerCondition, variables, context);
        }

        return evaluateCompositeCondition(condition, variables, context);
    }

    /** Évalue les combinaisons and/or selon les règles historiques. */
    private boolean evaluateCompositeCondition(String condition, Map<String, Object> variables, RenderContext context) throws Exception {

        // Ce filtre conservateur préserve le traitement des noms contenant and ou or.
        if (!condition.contains("and") && !condition.contains("or")) {
            return evaluateSimpleCondition(condition, variables, context);
        }

        String[] orConditions = condition.split("\\s+or\\s+");
        for (String orCondition : orConditions) {

            String[] andConditions = orCondition.split("\\s+and\\s+");
            boolean andResult = true;
            for (String andCondition : andConditions) {

                andCondition = andCondition.trim();
                boolean result = evaluateSimpleCondition(andCondition, variables, context);
                andResult = andResult && result;
            }

            if (andResult) {
                return true;
            }
        }

        return false;
    }


    /** Évalue une condition simple qui peut inclure des comparaisons. */
    private boolean evaluateSimpleCondition(String condition, Map<String, Object> variables, RenderContext context) throws Exception {
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
        String operator = findComparisonOperator(condition);

        if (operator != null) {
            return evaluateComparison(condition, operator, variables, context);
        } else {
            // Comportement d'origine pour les conditions sans opérateurs
            Object conditionValue = variables.get(condition);
            if (conditionValue == null) {
                return false;
            }
            return conditionValue instanceof Boolean && (Boolean) conditionValue;
        }
    }

    /** Évalue une valeur de drapeau de boucle (comme @last ou @first). */
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

    /** Évalue une comparaison avec un opérateur donné. */
    private boolean evaluateComparison(String condition, String operator, Map<String, Object> variables, RenderContext context) throws Exception {
        String[] parts = condition.split(Pattern.quote(operator), 2);
        if (parts.length != 2) {
            throw new Exception("Invalid comparison condition: " + condition);
        }

        String leftPart = parts[0].trim();
        String rightPart = parts[1].trim();
        Object leftValue = extractValue(leftPart, variables, context);
        Object rightValue = extractValue(rightPart, variables, context);
        return compareValues(leftValue, rightValue, operator);
    }

    /** Extrait la valeur d'une expression qui peut être une variable ou une valeur littérale. */
    private Object extractValue(String expression, Map<String, Object> variables, RenderContext context) {
        var variable = variables.get(expression);
        if (variable != null) {
            return variable;
        }
        if (context == null) return convertToNumericIfPossible(expression);
        if (context.literals == null) context.literals = new HashMap<>();
        return context.literals.computeIfAbsent(expression, this::convertToNumericIfPossible);
    }

    /** Vérifie si l'expression est une variable délimitée par les préfixes et suffixes donnés. */
    private boolean isVariable(String expression, String prefix, String suffix) {
        return expression.startsWith(prefix) && expression.endsWith(suffix);
    }

    /** Extrait le nom de la variable à partir de l'expression complète. */
    private String extractVariableName(String expression, String prefix, String suffix) {
        return expression.substring(
                prefix.length(),
                expression.length() - suffix.length()
        ).trim();
    }

    /** Tente de convertir une chaîne en valeur numérique. */
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

    /** Compare deux valeurs avec l'opérateur donné. */
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

        // Si les types sont différents mais numériques, convertir en Double pour la comparaison
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

        // Si les objets sont Comparable et du même type
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

    /** Effectue une comparaison numérique. */
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

    /** Effectue une comparaison de chaînes. */
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

    /** Effectue une comparaison avec des objets implémentant Comparable. */
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

    /** Interprète les tags de mise en forme et de suppression de ligne. */
    private void processSpecialTags(StringBuilder template) {
        replaceAllOccurrences(template, NEWLINE_TAG, "\n");
        replaceAllOccurrences(template, TAB_TAG, "\t");
        replaceAllOccurrences(template, SPACE_TAG, "    "); // Quatre espaces selon PEP 8.
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

    /** Utilise un plan partagé ou le parcours historique selon le contexte. */
    private void evaluateConditionals(StringBuilder template, Map<String, Object> variables,
                                      RenderContext context) throws Exception {
        if (template.indexOf(IF_START) < 0) return;
        if (context == null) {
            evaluateConditionalsLegacy(template, variables, null);
            return;
        }
        String source = template.toString();
        ConditionalPlan plan = context.conditions == null ? null : context.conditions.get(source);
        if (plan == null) {
            plan = parseConditionalPlan(template, 0, template.length(), 0);
            if (plan == null) {
                evaluateConditionalsLegacy(template, variables, context);
                return;
            }
            // Limiter la mémoire temporaire des expansions de boucles distinctes.
            if (context.conditions == null) context.conditions = new HashMap<>();
            if (context.conditions.size() < MAX_CACHED_PLANS) context.conditions.put(source, plan);
        }
        StringBuilder rendered = new StringBuilder(template.length());
        appendConditionalPlan(plan, variables, context, rendered);
        template.setLength(0);
        template.append(rendered);
    }

    /** Analyse les branches et demande un repli si le plan ne convient pas. */
    private ConditionalPlan parseConditionalPlan(StringBuilder source, int begin, int end, int depth) {
        // Confier les templates profonds à l’évaluateur itératif.
        if (depth >= MAX_PLAN_DEPTH) return null;
        List<PlannedNode> nodes = new ArrayList<>();
        int cursor = begin;
        while (cursor < end) {
            int start = source.indexOf(IF_START, cursor);
            if (start < 0 || start >= end) {
                nodes.add(new PlannedText(source.substring(cursor, end)));
                break;
            }
            if (start > cursor) nodes.add(new PlannedText(source.substring(cursor, start)));
            int close = findBlockEnd(source, start);
            int headerEnd = source.indexOf(BLOCK_END, start + IF_START.length());
            if (close < 0 || close >= end || headerEnd < 0 || headerEnd > close) return null;
            List<PlannedBranch> branches = new ArrayList<>();
            String condition = source.substring(start + IF_START.length(), headerEnd).trim();
            int bodyStart = headerEnd + BLOCK_END.length();
            while (true) {
                int nextElseIf = findBranchToken(source, ELSE_IF_TOKEN, bodyStart, close);
                int nextElse = findBranchToken(source, ELSE_TOKEN, bodyStart, close);
                // Préserver le traitement historique des branches mal formées.
                if (nextElse >= 0 && nextElseIf >= 0 && nextElse < nextElseIf) return null;
                int branchEnd = determineBlockEnd(nextElseIf, nextElse, close);
                ConditionalPlan body = parseConditionalPlan(source, bodyStart, branchEnd, depth + 1);
                if (body == null) return null;
                branches.add(new PlannedBranch(condition, body));
                if (nextElseIf >= 0) {
                    int nextHeaderEnd = source.indexOf(BLOCK_END, nextElseIf);
                    if (nextHeaderEnd < 0 || nextHeaderEnd > close) return null;
                    condition = source.substring(nextElseIf + ELSE_IF_TOKEN.length(), nextHeaderEnd).trim();
                    bodyStart = nextHeaderEnd + BLOCK_END.length();
                } else {
                    if (nextElse >= 0) {
                        ConditionalPlan otherwise = parseConditionalPlan(source, nextElse + ELSE_TOKEN.length(), close, depth + 1);
                        if (otherwise == null) return null;
                        branches.add(new PlannedBranch(null, otherwise));
                    }
                    break;
                }
            }
            nodes.add(new PlannedIf(List.copyOf(branches)));
            cursor = close + IF_END.length();
        }
        return new ConditionalPlan(List.copyOf(nodes));
    }

    /** Ajoute le texte des branches retenues au résultat. */
    private void appendConditionalPlan(ConditionalPlan plan, Map<String, Object> variables,
                                       RenderContext context, StringBuilder result) throws Exception {
        for (PlannedNode node : plan.nodes()) {
            if (node instanceof PlannedText text) result.append(text.value());
            else if (node instanceof PlannedIf conditional) {
                for (PlannedBranch branch : conditional.branches()) {
                    if (branch.condition() == null || evaluateCondition(branch.condition(), variables, context)) {
                        appendConditionalPlan(branch.body(), variables, context, result);
                        break;
                    }
                }
            }
        }
    }

}
