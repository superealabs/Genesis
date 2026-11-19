package engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.labs.genesis.engine.GenesisTemplateEngine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Vérifie les opérateurs, conditions, boucles et commentaires du moteur historique. */
public class GenesisTemplateEngineTest {
    private GenesisTemplateEngine engine;
    private Map<String, Object> variables;

    /** Initialise le moteur et les variables communes aux tests. */
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

    /** Vérifie les comparaisons d’égalité numériques et textuelles. */
    @Test
    void testEqualOperator() throws Exception {
        String numericEqualTemplate = """
                {{#if a = c}}Numbers are equal{{else}}Numbers are not equal{{/if}}
                """;
        assertEquals("Numbers are equal\n", engine.render(numericEqualTemplate, variables));
        String stringEqualTemplate = """
                {{#if text1 = text3}}Strings are equal{{else}}Strings are not equal{{/if}}
                """;
        assertEquals("Strings are equal\n", engine.render(stringEqualTemplate, variables));
        String stringLiteralEqualTemplate = """
                {{#if text1 = hello}}Strings are equal{{else}}Strings are not equal{{/if}}
                """;
        assertEquals("Strings are equal\n", engine.render(stringLiteralEqualTemplate, variables));
    }

    /** Vérifie l’opérateur de différence. */
    @Test
    void testNotEqualOperator() throws Exception {
        String numericNotEqualTemplate = """
                {{#if a != b}}Numbers are not equal{{else}}Numbers are equal{{/if}}
                """;
        assertEquals("Numbers are not equal\n", engine.render(numericNotEqualTemplate, variables));
        String stringNotEqualTemplate = """
                {{#if text1 != text2}}Strings are not equal{{else}}Strings are equal{{/if}}
                """;
        assertEquals("Strings are not equal\n", engine.render(stringNotEqualTemplate, variables));
    }

    /** Vérifie la comparaison strictement supérieure. */
    @Test
    void testGreaterOperator() throws Exception {
        String greaterTemplate = """
                {{#if a > b}}A is greater than B{{else}}A is not greater than B{{/if}}
                """;
        assertEquals("A is greater than B\n", engine.render(greaterTemplate, variables));
        String notGreaterTemplate = """
                {{#if b > a}}B is greater than A{{else}}B is not greater than A{{/if}}
                """;
        assertEquals("B is not greater than A\n", engine.render(notGreaterTemplate, variables));
    }

    /** Vérifie la comparaison strictement inférieure. */
    @Test
    void testLessOperator() throws Exception {
        String lessTemplate = """
                {{#if b < a}}B is less than A{{else}}B is not less than A{{/if}}
                """;
        assertEquals("B is less than A\n", engine.render(lessTemplate, variables));
        String notLessTemplate = """
                {{#if a < b}}A is less than B{{else}}A is not less than B{{/if}}
                """;
        assertEquals("A is not less than B\n", engine.render(notLessTemplate, variables));
    }

    /** Vérifie la comparaison supérieure ou égale. */
    @Test
    void testGreaterEqualOperator() throws Exception {
        String greaterEqualTemplate1 = """
                {{#if a >= c}}A is greater than or equal to C{{else}}A is less than C{{/if}}
                """;
        assertEquals("A is greater than or equal to C\n", engine.render(greaterEqualTemplate1, variables));
        String greaterEqualTemplate2 = """
                {{#if a >= b}}A is greater than or equal to B{{else}}A is less than B{{/if}}
                """;
        assertEquals("A is greater than or equal to B\n", engine.render(greaterEqualTemplate2, variables));
    }

    /** Vérifie la comparaison inférieure ou égale. */
    @Test
    void testLessEqualOperator() throws Exception {
        String lessEqualTemplate1 = """
                {{#if c <= a}}C is less than or equal to A{{else}}C is greater than A{{/if}}
                """;
        assertEquals("C is less than or equal to A\n", engine.render(lessEqualTemplate1, variables));
        String lessEqualTemplate2 = """
                {{#if b <= a}}B is less than or equal to A{{else}}B is greater than A{{/if}}
                """;
        assertEquals("B is less than or equal to A\n", engine.render(lessEqualTemplate2, variables));
    }

    /** Vérifie les combinaisons de conditions et leur négation. */
    @Test
    void testComplexConditions() throws Exception {
        String nestedTemplate = """
                {{#if a > b}}{{#if a = c}}A is greater than B and equal to C{{/if}}{{else}}A is not greater than B{{/if}}
                """;
        assertEquals("A is greater than B and equal to C\n", engine.render(nestedTemplate, variables));
        String elseIfTemplate = """
                {{#if a < b}}A is less than B{{elseIf a > b}}A is greater than B{{elseIf a = c}}A equals C{{else}}None of the conditions are true{{/if}}
                """;
        assertEquals("A is greater than B\n", engine.render(elseIfTemplate, variables));
    }

    /** Vérifie le rendu des boucles imbriquées. */
    @Test
    void testNestedLoop() throws Exception {
        var variables = new HashMap<String, Object>();
        List<Map<String, Object>> categories = new ArrayList<>();
        Map<String, Object> category1 = new HashMap<>();
        category1.put("name", "Fruits");
        category1.put("products", List.of("Pomme", "Banane", "Cerise"));
        categories.add(category1);
        Map<String, Object> category2 = new HashMap<>();
        category2.put("name", "Légumes");
        category2.put("products", List.of("Carotte", "Pommes de terre", "Brocoli"));
        categories.add(category2);
        variables.put("categories", categories);
        var template = """
                Liste des catégories :
                {{#each categories}}
                  Catégorie : ${this.name}
                  Produits :
                  {{#each this.products}}
                    - ${this}
                  {{/each}}
                {{/each}}
                """;

        var result = engine.render(template, variables);

        System.out.println(result);

        /*
        Sortie attendue :
        Liste des catégories :
          Catégorie : Fruits
          Produits :
            - Pomme
            - Banane
            - Cerise
          Catégorie : Légumes
          Produits :
            - Carotte
            - Pommes de terre
            - Brocoli
         */
    }

    /** Vérifie la protection des commentaires pendant le rendu. */
    @Test
    void testComments() throws Exception {
        String comment = """
                <#test comment in template engine/#>
                <h1>Title</h1>
                <#add variable ${test}/#>
                <h1>This is a paragraph: ${test}</h1>
                """;
        String result = engine.render(comment, Map.of("test", "test comment"));

        System.out.println(result);
        String expectedOutput = """
                test comment in template engine
                <h1>Title</h1>
                add variable ${test}
                <h1>This is a paragraph: test comment</h1>
                """;
        assertEquals(expectedOutput, result);

        assertEquals(" untouched ", engine.render("<# untouched /#>", Map.of()));
    }

}
