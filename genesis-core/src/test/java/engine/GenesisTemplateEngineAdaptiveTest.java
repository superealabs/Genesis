package engine;

import org.junit.jupiter.api.Test;
import org.labs.genesis.engine.GenesisTemplateEngine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Vérifie le routage adaptatif, les substitutions, le nettoyage et l’isolation des rendus. */
class GenesisTemplateEngineAdaptiveTest {

    /** Vérifie le chemin léger sur des conditions sans boucle. */
    @Test
    void rendersSimpleViewConditionsWithoutLoops() throws Exception {
        var engine = new GenesisTemplateEngine();
        String source = "list{{#if !isView}}:create{{/if}}{{#if isView}}:readonly{{/if}}";
        assertEquals("list:create", engine.render(source, Map.of("isView", false)));
        assertEquals("list:readonly", engine.render(source, Map.of("isView", true)));
    }

    /** Vérifie que les commentaires ne déclenchent pas le chemin des boucles. */
    @Test
    void preservesFakeLoopTagsInsideProtectedComments() throws Exception {
        var engine = new GenesisTemplateEngine();
        String source = "<#{{#each missing}}${literal}/#>{{#if enabled}}Y{{else}}N{{/if}}";
        assertEquals("{{#each missing}}${literal}Y", engine.render(source, Map.of("enabled", true)));
        assertEquals("{{#each missing}}${literal}N", engine.render(source, Map.of("enabled", false)));
        assertEquals("{{#each missing}}${literal}", engine.render("<#{{#each missing}}${literal}/#>", Map.of()));
    }

    /** Vérifie la réévaluation du corps partagé pour chaque élément. */
    @Test
    void reevaluatesConditionsForEveryItemOfARepeatedBodyWithoutNestedLoops() throws Exception {
        var engine = new GenesisTemplateEngine();
        String source = "{{#each fields}}{{#if @first}}[{{/if}}"
                + "{{#if this.visible}}${this.name}{{else}}-{{/if}}"
                + "{{#if @last}}]{{else}},{{/if}}{{/each}}";
        var fields = List.of(Map.of("visible", true, "name", "first"),
                Map.of("visible", false, "name", "hidden"),
                Map.of("visible", true, "name", "last"));
        assertEquals("[first,-,last]", engine.render(source, Map.of("fields", fields)));
        assertEquals("", engine.render(source, Map.of("fields", List.of())));
    }

    /** Vérifie la portée des variables dans les boucles imbriquées. */
    @Test
    void keepsParentVariablesAndItemVariablesInNestedLoops() throws Exception {
        var engine = new GenesisTemplateEngine();
        String source = "{{#each groups}}${this.label}:{{#each this.items}}"
                + "{{#if this.enabled}}${this.name}{{else}}-{{/if}}{{/each}};{{/each}}";
        var groups = List.of(
                Map.of("label", "A", "items", List.of(Map.of("enabled", true, "name", "a"), Map.of("enabled", false, "name", "hidden"))),
                Map.of("label", "B", "items", List.of(Map.of("enabled", true, "name", "b"))));
        assertEquals("A:a-;B:b;", engine.render(source, Map.of("groups", groups)));
    }

    /** Vérifie le repli historique pour les conditions très profondes. */
    @Test
    void handlesVeryDeepConditionsInsideALoopWithoutChangingTheResult() throws Exception {
        var engine = new GenesisTemplateEngine();
        String body = "{{#if enabled}}".repeat(1000) + "X" + "{{/if}}".repeat(1000);
        String source = "{{#each fields}}" + body + "{{/each}}";
        assertEquals("", engine.render(source, Map.of("fields", List.of("item"), "enabled", false)));
        assertEquals("X", engine.render(source, Map.of("fields", List.of("item"), "enabled", true)));
    }

    /** Vérifie les espaces et la négation des conditions composites. */
    @Test
    void retainsCompositeConditionWhitespaceAndExistingNegationSemantics() throws Exception {
        var engine = new GenesisTemplateEngine();
        Map<String, Object> model = Map.of("enabled", false, "other", false, "third", true);
        assertInBothPaths(engine, "!enabled and other", model, "Y");
        assertInBothPaths(engine, "enabled\tor\tthird", model, "Y");
        assertInBothPaths(engine, "third\nand\nother", model, "N");
        assertInBothPaths(engine, "enabled or third and other", model, "N");
        assertInBothPaths(engine, "!third or enabled", model, "N");
        assertInBothPaths(engine, "true", model, "Y");
        assertInBothPaths(engine, "false", model, "N");
    }

    /** Vérifie les noms ambigus et les comparaisons sans espaces. */
    @Test
    void retainsNamesContainingAndOrAndComparisonsWithoutSpaces() throws Exception {
        var engine = new GenesisTemplateEngine();
        Map<String, Object> model = Map.of("brand", true, "origin", false, "candy", true,
                "or", true, "and", false, "left", 3, "right", 2);
        assertInBothPaths(engine, "brand", model, "Y");
        assertInBothPaths(engine, "origin", model, "N");
        assertInBothPaths(engine, "candy", model, "Y");
        assertInBothPaths(engine, "or", model, "Y");
        assertInBothPaths(engine, "and", model, "N");
        assertInBothPaths(engine, "left>=right", model, "Y");
        assertInBothPaths(engine, "left<right", model, "N");
    }

    /** Vérifie la priorité des variables sur les conversions mémorisées. */
    @Test
    void variableValuesTakePriorityOverFallbackLiteralsAcrossIterationsAndCalls() throws Exception {
        var engine = new GenesisTemplateEngine();
        String source = "{{#each fields}}{{#if this.value = this.literal}}Y{{else}}N{{/if}}{{/each}}";
        var fallback = Map.of("value", "this.literal");
        var explicit = Map.of("value", "changed", "literal", "changed");
        assertEquals("YY", engine.render(source, Map.of("fields", List.of(fallback, explicit))));
        assertEquals("YY", engine.render(source, Map.of("fields", List.of(explicit, fallback))));
        assertEquals("N", engine.render(source, Map.of("fields", List.of(Map.of("value", "changed")))));
    }

    /** Vérifie l’étape d’interprétation des conditions injectées par les données. */
    @Test
    void interpretsConditionsInjectedByDataAtTheSameStageAsBefore() throws Exception {
        var engine = new GenesisTemplateEngine();
        String source = "{{#each fields}}${this.fragment}{{/each}}";
        var fields = List.of(Map.of("fragment", "{{#if enabled}}Y{{else}}N{{/if}}"),
                Map.of("fragment", "{{#if !enabled}}N{{else}}Y{{/if}}"));
        assertEquals("YY", engine.render(source, Map.of("fields", fields, "enabled", true)));
        assertEquals("NN", engine.render(source, Map.of("fields", fields, "enabled", false)));
    }

    /** Vérifie les substitutions imbriquées et les fonctions. */
    @Test
    void resolvesInsertedPlaceholdersAndFunctionCalls() throws Exception {
        var engine = new GenesisTemplateEngine();
        assertEquals("Hello ADA!", engine.render("Hello ${fragment}!",
                Map.of("fragment", "${upperCase(name)}", "name", "Ada")));
        assertEquals("Ada", engine.render("${first}",
                Map.of("first", "${second}", "second", "${name}", "name", "Ada")));
    }

    /** Vérifie les placeholders créés à la frontière d’un remplacement. */
    @Test
    void recognizesPlaceholdersFormedByJoiningTheSurroundingText() throws Exception {
        var engine = new GenesisTemplateEngine();
        assertEquals("ADA", engine.render("$${fragment}",
                Map.of("fragment", "{upperCase(name)}", "name", "Ada")));
        assertEquals("xAda", engine.render("x$${empty}{name}",
                Map.of("empty", "", "name", "Ada")));
    }

    /** Vérifie les insertions de dollar et les substitutions vides. */
    @Test
    void resolvesAPlaceholderCompletedByAnInsertedDollarAndRemovesEmptyValues() throws Exception {
        var engine = new GenesisTemplateEngine();
        assertEquals("xAday", engine.render("x${prefix}{name}${empty}y",
                Map.of("prefix", "$", "name", "Ada", "empty", "")));
        assertEquals("beforeafter:", engine.render("before${empty}after:${missing}", Map.of("empty", "")));
    }

    /** Vérifie le nettoyage du contexte après une erreur de rendu. */
    @Test
    void reusesTheEngineAfterErrorsInBothRenderingPaths() throws Exception {
        var engine = new GenesisTemplateEngine();
        String expected = "Variable '@last' is not a boolean.";
        assertEquals(expected, assertThrows(Exception.class,
                () -> engine.render("{{#if @last}}X{{/if}}", Map.of("enabled", true))).getMessage());
        assertEquals(expected, assertThrows(Exception.class,
                () -> engine.render("{{#each empty}}unused{{/each}}{{#if @last}}X{{/if}}", Map.of("empty", List.of()))).getMessage());
        assertEquals("N", engine.render("{{#if enabled}}Y{{else}}N{{/if}}", Map.of("enabled", false)));
        assertEquals("YN", engine.render("{{#each fields}}{{#if this}}Y{{else}}N{{/if}}{{/each}}",
                Map.of("fields", List.of(true, false))));
    }

    /** Vérifie que les récursions appellent toujours la surcharge publique. */
    @Test
    void preservesRecursiveRenderOverrides() throws Exception {
        class ObservedEngine extends GenesisTemplateEngine {
            final List<String> sources = new ArrayList<>();
            /** Compte les appels pour vérifier le passage par la surcharge. */
            @Override
            public String render(String source, Map<String, Object> model) throws Exception {
                sources.add(source);
                return super.render(source, model);
            }
        }
        var engine = new ObservedEngine();
        String body = "{{#if this.visible}}${this.name}{{else}}-{{/if}}";
        String source = "{{#each fields}}" + body + "{{/each}}";
        assertEquals("a-", engine.render(source,
                Map.of("fields", List.of(Map.of("visible", true, "name", "a"), Map.of("visible", false, "name", "b")))));
        assertEquals(List.of(source, body, body), engine.sources);
    }

    /** Vérifie l’isolation des rendus concurrents sur une même instance. */
    @Test
    void isolatesConcurrentRendersOfTheSameEngine() throws Exception {
        var engine = new GenesisTemplateEngine();
        try (var executor = Executors.newFixedThreadPool(4)) {
            List<Callable<String>> jobs = new ArrayList<>();
            for (int i = 0; i < 40; i++) {
                int value = i;
                jobs.add(() -> engine.render("{{#each fields}}{{#if enabled}}${this}{{else}}-{{/if}}{{/each}}",
                        Map.of("fields", List.of(value, value), "enabled", value % 2 == 0)));
            }
            var results = executor.invokeAll(jobs);
            for (int i = 0; i < results.size(); i++) {
                assertEquals(i % 2 == 0 ? "" + i + i : "--", results.get(i).get());
            }
        }
    }

    /** Vérifie la même expression avec et sans contexte de boucle. */
    private void assertInBothPaths(GenesisTemplateEngine engine, String expression,
                                   Map<String, Object> model, String expected) throws Exception {
        String body = "{{#if " + expression + "}}Y{{else}}N{{/if}}";
        assertEquals(expected, engine.render(body, model), expression);
        Map<String, Object> loopModel = new HashMap<>(model);
        loopModel.put("fields", List.of("first", "second"));
        assertEquals(expected + expected, engine.render("{{#each fields}}" + body + "{{/each}}", loopModel), expression);
    }
}
