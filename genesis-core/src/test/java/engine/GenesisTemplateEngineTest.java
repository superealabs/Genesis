package engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.labs.genesis.engine.GenesisTemplateEngine;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

public class GenesisTemplateEngineTest {
    private GenesisTemplateEngine engine;
    private Map<String, Object> variables;

    @BeforeEach
    void setup() {
        engine = new GenesisTemplateEngine();
        variables = new HashMap<>();
        variables.put("a", 10);
        variables.put("b", 5);
        variables.put("c", 10);
        variables.put("text1", "hello");
        variables.put("text2", "world");
        variables.put("text3", "hello");
    }

    @Test
    void testEqualOperator() throws Exception {
        // Test égalité numérique
        String numericEqualTemplate = """
                {{#if a = c}}Numbers are equal{{else}}Numbers are not equal{{/if}}
                """;
        assertEquals("Numbers are equal\n", engine.render(numericEqualTemplate, variables));

        // Test égalité de chaînes
        String stringEqualTemplate = """
                {{#if text1 = text3}}Strings are equal{{else}}Strings are not equal{{/if}}
                """;
        assertEquals("Strings are equal\n", engine.render(stringEqualTemplate, variables));

        // Test égalité de chaînes
        String stringLiteralEqualTemplate = """
                {{#if text1 = hello}}Strings are equal{{else}}Strings are not equal{{/if}}
                """;
        assertEquals("Strings are equal\n", engine.render(stringLiteralEqualTemplate, variables));
    }

    @Test
    void testNotEqualOperator() throws Exception {
        // Test inégalité numérique
        String numericNotEqualTemplate = """
                {{#if a != b}}Numbers are not equal{{else}}Numbers are equal{{/if}}
                """;
        assertEquals("Numbers are not equal\n", engine.render(numericNotEqualTemplate, variables));

        // Test inégalité de chaînes
        String stringNotEqualTemplate = """
                {{#if text1 != text2}}Strings are not equal{{else}}Strings are equal{{/if}}
                """;
        assertEquals("Strings are not equal\n", engine.render(stringNotEqualTemplate, variables));
    }

    @Test
    void testGreaterOperator() throws Exception {
        // Test opérateur supérieur
        String greaterTemplate = """
                {{#if a > b}}A is greater than B{{else}}A is not greater than B{{/if}}
                """;
        assertEquals("A is greater than B\n", engine.render(greaterTemplate, variables));

        // Test cas où la condition n'est pas satisfaite
        String notGreaterTemplate = """
                {{#if b > a}}B is greater than A{{else}}B is not greater than A{{/if}}
                """;
        assertEquals("B is not greater than A\n", engine.render(notGreaterTemplate, variables));
    }

    @Test
    void testLessOperator() throws Exception {
        // Test opérateur inférieur
        String lessTemplate = """
                {{#if b < a}}B is less than A{{else}}B is not less than A{{/if}}
                """;
        assertEquals("B is less than A\n", engine.render(lessTemplate, variables));

        // Test cas où la condition n'est pas satisfaite
        String notLessTemplate = """
                {{#if a < b}}A is less than B{{else}}A is not less than B{{/if}}
                """;
        assertEquals("A is not less than B\n", engine.render(notLessTemplate, variables));
    }

    @Test
    void testGreaterEqualOperator() throws Exception {
        // Test supérieur ou égal (cas égalité)
        String greaterEqualTemplate1 = """
                {{#if a >= c}}A is greater than or equal to C{{else}}A is less than C{{/if}}
                """;
        assertEquals("A is greater than or equal to C\n", engine.render(greaterEqualTemplate1, variables));

        // Test supérieur ou égal (cas supérieur)
        String greaterEqualTemplate2 = """
                {{#if a >= b}}A is greater than or equal to B{{else}}A is less than B{{/if}}
                """;
        assertEquals("A is greater than or equal to B\n", engine.render(greaterEqualTemplate2, variables));
    }

    @Test
    void testLessEqualOperator() throws Exception {
        // Test inférieur ou égal (cas égalité)
        String lessEqualTemplate1 = """
                {{#if c <= a}}C is less than or equal to A{{else}}C is greater than A{{/if}}
                """;
        assertEquals("C is less than or equal to A\n", engine.render(lessEqualTemplate1, variables));

        // Test inférieur ou égal (cas inférieur)
        String lessEqualTemplate2 = """
                {{#if b <= a}}B is less than or equal to A{{else}}B is greater than A{{/if}}
                """;
        assertEquals("B is less than or equal to A\n", engine.render(lessEqualTemplate2, variables));
    }

    @Test
    void testComplexConditions() throws Exception {
        // Test conditions imbriquées
        String nestedTemplate = """
                {{#if a > b}}{{#if a = c}}A is greater than B and equal to C{{/if}}{{else}}A is not greater than B{{/if}}
                """;
        assertEquals("A is greater than B and equal to C\n", engine.render(nestedTemplate, variables));

        // Test avec elseIf
        String elseIfTemplate = """
                {{#if a < b}}A is less than B{{elseIf a > b}}A is greater than B{{elseIf a = c}}A equals C{{else}}None of the conditions are true{{/if}}
                """;
        assertEquals("A is greater than B\n", engine.render(elseIfTemplate, variables));
    }
}