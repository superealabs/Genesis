package engine;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.labs.genesis.engine.GenesisTemplateEngine;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.Executors;
import static org.junit.jupiter.api.Assertions.*;

/** Compare le moteur à une référence figée, y compris les syntaxes dynamiques et mal formées. */
@Tag("per-render-compatibility")
class GenesisPerRenderCompatibilityTest {
    /** Compare les sorties et erreurs avec celles de la référence figée. */
    @Test
    void comparesDynamicAndMalformedTemplatesWithTheProductionEngine() throws Exception {
        try (var loader = new URLClassLoader(new java.net.URL[]{Path.of(System.getProperty("reference.classes")).toUri().toURL()}, getClass().getClassLoader()) {
            /** Isole les classes du moteur de référence du moteur testé. */
            @Override protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                if (name.equals("org.labs.genesis.engine.GenesisTemplateEngine") || name.startsWith("org.labs.genesis.engine.GenesisTemplateEngine$")) {
                    synchronized (getClassLoadingLock(name)) {
                        Class<?> type = findLoadedClass(name);
                        if (type == null) type = findClass(name);
                        if (resolve) resolveClass(type);
                        return type;
                    }
                }
                return super.loadClass(name, resolve);
            }
        }) {
            var type = loader.loadClass("org.labs.genesis.engine.GenesisTemplateEngine");
            var reference = type.getConstructor().newInstance();
            var method = type.getMethod("render", String.class, Map.class);
            var candidate = new GenesisTemplateEngine();
            List<String> templates = new ArrayList<>(List.of(
                    "${name}", "${snippet}", "${upperCase(name)}", "<#${name} {{#if enabled}}x{{/if}}/#>",
                    "{{#if enabled}}A{{#if other}}B{{else}}C{{/if}}{{else}}D{{/if}}",
                    "{{#each fields}}${this.name}:{{#if this.type = text}}T{{elseIf this.type = number}}N{{else}}?{{/if}};{{/each}}",
                    "{{#each fields}}${@index}{{#if @first}}F{{/if}}{{#if @last}}L{{/if}}${this.name}{{/each}}",
                    "{{#each fields}}{{#each nested}}${this}{{/each}}{{#if enabled}}Y{{/if}}{{/each}}",
                    "{{#each fields}}${this.dynamic}{{/each}}", "{{#if enabled}}${snippet}{{else}}N{{/if}}",
                    "a\n{{#if enabled}}{{removeLine}}\nb\n{{else}}\nc\n{{/if}}",
                    "{{#if enabled}}unclosed", "{{#each fields}}unclosed", "${unclosed",
                    "{{#if enabled}}A{{else}}B{{elseIf other}}C{{/if}}", "{{#if @last}}x{{/if}}",
                    "{{#if a = b}}equal{{else}}different{{/if}}", "{{#if !enabled and other}}Y{{else}}N{{/if}}",
                    "<#{{#each missing}}${literal}/#>{{#if enabled}}Y{{else}}N{{/if}}",
                    "{{#if enabled\tor\tother}}Y{{else}}N{{/if}}",
                    "{{#if enabled\nand\nother}}Y{{else}}N{{/if}}",
                    "{{#if brand}}Y{{else}}N{{/if}}{{#if origin}}O{{else}}X{{/if}}",
                    "{{#if candy}}Y{{else}}N{{/if}}{{#if or}}O{{else}}X{{/if}}",
                    "{{#if and}}Y{{else}}N{{/if}}{{#if a>=b}}G{{else}}L{{/if}}",
                    "{{#each fields}}{{#if enabled\tor\tother}}${this.name}{{else}}N{{/if}}{{/each}}",
                    "{{#each fields}}{{#if brand}}${this.name}{{else}}N{{/if}}{{/each}}",
                    "{{#each fields}}${this.dynamic}{{/each}}{{#if !enabled}}N{{else}}Y{{/if}}"));
            Random random = new Random(20261118);
            for (int i = 0; i < 150; i++) templates.add(randomTemplate(random, 0));
            String[] tokens = {"{{#if enabled}}", "{{#if other}}", "{{elseIf other}}", "{{else}}", "{{/if}}", "A", "${name}"};
            for (int i = 0; i < 300; i++) {
                StringBuilder malformed = new StringBuilder();
                for (int j = 0; j < 8; j++) malformed.append(tokens[random.nextInt(tokens.length)]);
                templates.add(malformed.toString());
            }
            int checked = 0;
            for (boolean enabled : List.of(false, true)) for (boolean other : List.of(false, true)) {
                Map<String, Object> model = new HashMap<>();
                model.putAll(Map.of("name", "Alpha", "enabled", enabled, "other", other, "a", 2, "b", "2",
                        "snippet", "${name}", "nested", List.of("one", "two")));
                model.putAll(Map.of("brand", true, "origin", false, "candy", true, "or", true, "and", false));
                model.put("fields", List.of(Map.of("name", "first", "type", "text", "dynamic", "{{#if enabled}}Y{{else}}N{{/if}}"),
                        Map.of("name", "second", "type", "number", "dynamic", "<#${name}/#>")));
                for (String template : templates) {
                    String expected = outcome(() -> {
                        try { return (String) method.invoke(reference, template, model); }
                        catch (InvocationTargetException error) { throw (Exception) error.getCause(); }
                    });
                    assertEquals(expected, outcome(() -> candidate.render(template, model)), template);
                    checked++;
                }
            }
            // Les variables restent prioritaires sur les littéraux déjà convertis.
            String dynamic = "{{#each fields}}{{#if this.type = number}}N{{else}}T{{/if}}{{/each}}";
            for (Map<String, Object> model : List.of(Map.<String,Object>of("fields", List.of(Map.of("type", "number"))),
                    Map.<String,Object>of("number", "text", "fields", List.of(Map.of("type", "text"))))) {
                assertEquals(method.invoke(reference, dynamic, model), candidate.render(dynamic, model));
            }
            System.out.println("PER_RENDER_COMPATIBILITY exact outcomes=" + checked);
        }
    }

    /** Vérifie les surcharges récursives et les appels concurrents. */
    @Test void preservesRecursiveRenderOverridesAndConcurrentCalls() throws Exception {
        class Observed extends GenesisTemplateEngine {
            int calls;
            /** Compte les appels pour vérifier le passage par la surcharge. */
            @Override public String render(String source, Map<String,Object> variables) throws Exception {
                calls++; return super.render(source, variables);
            }
        }
        var observed = new Observed();
        assertEquals("ab", observed.render("{{#each fields}}${this}{{/each}}", Map.of("fields", List.of("a", "b"))));
        assertEquals(3, observed.calls);
        var shared = new GenesisTemplateEngine();
        try (var executor = Executors.newFixedThreadPool(4)) {
            List<java.util.concurrent.Callable<String>> jobs = new ArrayList<>();
            for (int i = 0; i < 40; i++) {
                int value = i;
                jobs.add(() -> shared.render("{{#each fields}}{{#if enabled}}${this}{{else}}N{{/if}}{{/each}}",
                        Map.of("enabled", true, "fields", List.of(value))));
            }
            var results = executor.invokeAll(jobs);
            for (int i = 0; i < results.size(); i++) assertEquals("" + i, results.get(i).get());
        }
    }

    /** Construit un template aléatoire reproductible pour la comparaison. */
    private String randomTemplate(Random random, int depth) {
        if (depth > 2) return random.nextBoolean() ? "${name}" : "X\n";
        return switch (random.nextInt(4)) {
            case 0 -> "{{#if enabled}}" + randomTemplate(random, depth + 1) + "{{elseIf other}}B{{else}}C{{/if}}";
            case 1 -> "{{#each fields}}" + randomTemplate(random, depth + 1) + "{{/each}}";
            case 2 -> "<#${name}/#>" + randomTemplate(random, depth + 1);
            default -> "A${name}Z";
        };
    }
    /** Capture le résultat ou l’erreur d’un rendu pour les comparer. */
    private String outcome(Render render) {
        try { return "OUTPUT:" + render.run(); }
        catch (Exception error) { return "ERROR:" + error.getClass().getName() + ":" + error.getMessage(); }
    }
    @FunctionalInterface private interface Render {
        /** Exécute le scénario de rendu fourni par le test. */
        String run() throws Exception;
    }
}
