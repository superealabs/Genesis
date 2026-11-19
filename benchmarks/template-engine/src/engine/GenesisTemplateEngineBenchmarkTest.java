package engine;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.labs.genesis.engine.GenesisTemplateEngine;

import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/** Compare les sorties et performances de Genesis et FreeMarker sur une page complète. */
@Tag("template-benchmark")
class GenesisTemplateEngineBenchmarkTest {
    private static final String GENESIS = "data_genesis/templates/Template 1/ListViewTemplate.templ";
    private static final String FREEMARKER = "benchmark/spring-list.ftl";
    private static final int WARMUP = 500;
    private static final int ITERATIONS = 3000;
    private static volatile String sink;

    /** Compare les sorties et les durées de la page complète des deux moteurs. */
    @Test
    void compareCompleteSpringListView() throws Exception {
        Map<String, Object> model = new ObjectMapper().readValue(read("benchmark/spring-list-model.json"),
                new TypeReference<Map<String, Object>>() {});
        GenesisTemplateEngine genesis = new GenesisTemplateEngine();
        Configuration configuration = configuration();
        String genesisSource = read(GENESIS);
        Template freeMarkerTemplate = prepare(configuration, read(FREEMARKER));
        Path results = Path.of("build/reports/template-benchmark");
        Files.createDirectories(results);

        // Vérifier les modes édition et lecture seule ainsi que les branches de champs et de filtres.
        for (boolean readOnly : List.of(false, true)) {
            Map<String, Object> variant = new HashMap<>(model);
            variant.put("isView", readOnly);
            String g = renderGenesis(genesis, genesisSource, variant);
            String f = renderFreeMarker(freeMarkerTemplate, variant);
            Files.writeString(results.resolve("genesis-" + readOnly + ".html"), g);
            Files.writeString(results.resolve("freemarker-" + readOnly + ".html"), f);
            assertEquals(normalize(g), normalize(f), "Only indentation and empty lines may differ");
            assertFalse(g.contains("{{#"));
            assertFalse(g.contains("{{removeLine}}"));
            assertFalse(g.contains("$[thymeleafDollar]"));
            assertTrue(g.contains("${product.name}"), "Preserve runtime Thymeleaf expressions");
            assertTrue(g.contains("category.name"), "Render foreign-key display column");
            assertEquals(!readOnly, g.contains("id=\"customModal\""));
        }
        // Vérifier aussi les collections vides, sans les utiliser pour les mesures.
        Map<String, Object> empty = new HashMap<>(model);
        for (String key : List.of("fields", "fieldsFK", "inputsFilter", "pageSizesList")) empty.put(key, List.of());
        assertEquals(normalize(renderGenesis(genesis, genesisSource, empty)),
                normalize(renderFreeMarker(freeMarkerTemplate, empty)));

        System.out.printf(Locale.ROOT, "BENCHMARK Java=%s VM=%s OS=%s %s CPUs=%d maxHeap=%d FreeMarker=%s%n",
                System.getProperty("java.version"), System.getProperty("java.vm.name"),
                System.getProperty("os.name"), System.getProperty("os.arch"),
                Runtime.getRuntime().availableProcessors(), Runtime.getRuntime().maxMemory(), Configuration.getVersion());
        System.out.printf("Template=%s; fields=%d; warmup=%d per engine/scenario; iterations=%d%n",
                GENESIS, ((List<?>) model.get("fields")).size(), WARMUP, ITERATIONS);
        System.out.println("Equality passed for writable, read-only and empty collections (indentation/blank lines only).");
        run("A-render", () -> renderGenesis(genesis, genesisSource, model),
                () -> renderFreeMarker(freeMarkerTemplate, model), results);
        // Partager la configuration entre scénarios ; en B, réanalyser le template à chaque rendu.
        run("B-full", () -> renderGenesis(new GenesisTemplateEngine(), read(GENESIS), model),
                () -> renderFreeMarker(prepare(configuration, read(FREEMARKER)), model), results);
    }

    /** Configure FreeMarker pour les fixtures du benchmark. */
    private static Configuration configuration() {
        Configuration c = new Configuration(Configuration.VERSION_2_3_35);
        c.setDefaultEncoding("UTF-8");
        c.setLocale(Locale.ROOT);
        c.setNumberFormat("computer");
        c.setInterpolationSyntax(Configuration.SQUARE_BRACKET_INTERPOLATION_SYNTAX);
        c.setWhitespaceStripping(false);
        c.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        c.setLogTemplateExceptions(false);
        c.setFallbackOnNullLoopVariable(false);
        return c;
    }

    /** Analyse le source du template FreeMarker. */
    private static Template prepare(Configuration c, String source) throws Exception {
        return new Template(FREEMARKER, new StringReader(source), c);
    }

    /** Rend la page avec Genesis et les données du scénario. */
    private static String renderGenesis(GenesisTemplateEngine engine, String source, Map<String, Object> model)
            throws Exception {
        // Appliquer la finalisation HTML utilisée par ProjectGenerator.renderFilesEdits.
        return engine.simpleRenderAlt(engine.render(source, model), Map.of("thymeleafDollar", "$"));
    }

    /** Rend le template FreeMarker dans un tampon texte. */
    private static String renderFreeMarker(Template template, Map<String, Object> model) throws Exception {
        StringWriter writer = new StringWriter();
        template.process(model, writer);
        return writer.toString();
    }

    /** Charge une ressource du classpath pour le benchmark. */
    private static String read(String name) throws Exception {
        try (InputStream in = GenesisTemplateEngineBenchmarkTest.class.getClassLoader().getResourceAsStream(name)) {
            if (in == null) throw new IllegalArgumentException("Missing resource: " + name);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    /** Retire l’indentation et les lignes vides avant comparaison des moteurs. */
    private static String normalize(String value) {
        // Conserver les espaces internes, les attributs, les expressions et leur ordre.
        return value.lines().map(String::strip).filter(line -> !line.isEmpty()).collect(Collectors.joining("\n"));
    }

    /** Mesure les deux moteurs en alternant leur ordre puis écrit les résultats. */
    private static void run(String scenario, Render genesis, Render freeMarker, Path results) throws Exception {
        for (int i = 0; i < WARMUP; i++) {
            if ((i & 1) == 0) { sink = genesis.render(); sink = freeMarker.render(); }
            else { sink = freeMarker.render(); sink = genesis.render(); }
        }
        long[] g = new long[ITERATIONS];
        long[] f = new long[ITERATIONS];
        StringBuilder csv = new StringBuilder("iteration,genesis_ns,freemarker_ns\n");
        for (int i = 0; i < ITERATIONS; i++) {
            // Alterner l’ordre pour réduire le biais lié à la position de chaque moteur.
            if ((i & 1) == 0) { g[i] = time(genesis); f[i] = time(freeMarker); }
            else { f[i] = time(freeMarker); g[i] = time(genesis); }
            csv.append(i + 1).append(',').append(g[i]).append(',').append(f[i]).append('\n');
        }
        Files.writeString(results.resolve(scenario + "-samples.csv"), csv);
        String go = genesis.render();
        String fo = freeMarker.render();
        assertEquals(normalize(go), normalize(fo));
        print(scenario, "GenesisTemplateEngine", g, go);
        print(scenario, "Apache FreeMarker", f, fo);
        double ratio = Arrays.stream(f).average().orElseThrow() / Arrays.stream(g).average().orElseThrow();
        System.out.printf(Locale.ROOT, "%s FreeMarker/Genesis=%.6f; %s est %.3f fois plus rapide (moyennes).%n",
                scenario, ratio, ratio >= 1 ? "GenesisTemplateEngine" : "FreeMarker", ratio >= 1 ? ratio : 1 / ratio);
    }

    /** Chronomètre un appel de rendu en conservant sa sortie. */
    private static long time(Render render) throws Exception {
        long start = System.nanoTime();
        String output = render.render();
        long elapsed = System.nanoTime() - start;
        sink = output; // Consommer la sortie hors de la région chronométrée.
        return elapsed;
    }

    /** Calcule et affiche les statistiques des échantillons mesurés. */
    private static void print(String scenario, String engine, long[] samples, String output) {
        long[] sorted = samples.clone();
        Arrays.sort(sorted);
        double median = (sorted[ITERATIONS / 2 - 1] + sorted[ITERATIONS / 2]) / 2.0;
        System.out.printf(Locale.ROOT,
                "%s | %s | n=%d | mean_ms=%.6f | median_ms=%.6f | p95_ms=%.6f | min_ms=%.6f | max_ms=%.6f | chars=%d | bytes_utf8=%d%n",
                scenario, engine, ITERATIONS, Arrays.stream(samples).average().orElseThrow() / 1e6,
                median / 1e6, sorted[(int) Math.ceil(.95 * ITERATIONS) - 1] / 1e6,
                sorted[0] / 1e6, sorted[ITERATIONS - 1] / 1e6, output.length(), output.getBytes(StandardCharsets.UTF_8).length);
    }

    @FunctionalInterface
    private interface Render {
        /** Exécute le rendu du scénario mesuré. */
        String render() throws Exception;
    }
}
