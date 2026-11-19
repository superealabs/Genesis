package engine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.management.ThreadMXBean;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.labs.genesis.config.Constantes;
import org.labs.genesis.config.langage.*;
import org.labs.genesis.config.langage.generator.framework.FrameworkMetadataProvider;
import org.labs.genesis.config.langage.generator.framework.FrameworkTemplateLoader;
import org.labs.genesis.config.langage.generator.project.ProjectGenerator;
import org.labs.genesis.connexion.model.TableMetadata;
import org.labs.genesis.engine.GenesisTemplateEngine;
import org.labs.genesis.frontend.generator.frameworkFrontend.FrameworkFrontendMetadataProvider;
import org.labs.utils.FileUtils;

import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/** Mesure les templates réels et conserve leurs sorties pour vérifier la compatibilité. */
@Tag("template-corpus")
class GenesisTemplateCorpusTest {
    private final GenesisTemplateEngine engine = new GenesisTemplateEngine();
    private static volatile String sink;
    private static final Map<String, Object> OPTIONS = Map.of(
            "cacheProvider", "none", "securityType", "none", "enableAuth", false);
    private final List<Workload> workloads = new ArrayList<>();

    /** Contrôle et mesure le corpus de templates de production. */
    @Test
    void measureProductionTemplateCorpus() throws Exception {
        String variant = System.getProperty("corpus.variant", "baseline");
        String fork = System.getProperty("corpus.fork", "1");
        Path output = Path.of("build/reports/template-corpus", variant, fork);
        Files.createDirectories(output.resolve("snapshots"));
        try (var oldSnapshots = Files.list(output.resolve("snapshots"))) {
            for (Path snapshot : oldSnapshots.filter(path -> path.toString().endsWith(".txt")).toList()) {
                Files.delete(snapshot);
            }
        }
        for (int columns : List.of(5, 20, 50)) {
            addBackend(columns, Constantes.Spring_REST_API_ID, "spring");
            addBackend(columns, Constantes.NET_ID, "dotnet");
            addMvc(columns);
            addFrontend(columns);
        }
        assertTrue(workloads.size() >= 60, "Corpus must cover multiple stacks and sizes");
        String selectedCases = System.getProperty("corpus.cases", "");
        if (!selectedCases.isBlank()) {
            Set<String> names = new HashSet<>(Arrays.asList(selectedCases.split(",")));
            workloads.removeIf(workload -> !names.contains(workload.name()));
            assertEquals(names.size(), workloads.size(), "Every requested corpus case must exist");
        }
        List<Map<String, Object>> results = new ArrayList<>();
        for (Workload workload : workloads) {
            String rendered = workload.render().run();
            assertFalse(rendered.isBlank(), workload.name());
            // Les expressions destinées à l’exécution peuvent subsister ; le runner compare les sorties complètes.
            Files.writeString(output.resolve("snapshots").resolve(workload.name() + ".txt"), rendered);
            results.add(measure(workload, rendered));
        }
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("variant", variant);
        report.put("fork", fork);
        report.put("java", System.getProperty("java.version"));
        report.put("engineLocation", GenesisTemplateEngine.class.getProtectionDomain().getCodeSource().getLocation().toString());
        report.put("maxHeap", Runtime.getRuntime().maxMemory());
        report.put("results", results);
        new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(output.resolve("results.json").toFile(), report);
        System.out.printf(Locale.ROOT, "CORPUS variant=%s fork=%s workloads=%d engine=%s%n",
                variant, fork, workloads.size(), report.get("engineLocation"));
    }

    /** Construit les métadonnées de table utilisées par le scénario. */
    private TableMetadata table(int count, Language language) {
        StringBuilder ddl = new StringBuilder("id BIGSERIAL PRIMARY KEY, name VARCHAR(120) NOT NULL, "
                + "category_id BIGINT REFERENCES categories(id), active BOOLEAN DEFAULT true, created_at TIMESTAMP");
        String[] types = {"NUMERIC(12,2)", "VARCHAR(200)", "INTEGER", "DATE", "BOOLEAN"};
        for (int i = 5; i < count; i++) ddl.append(", field_").append(i).append(' ').append(types[i % types.length]);
        TableMetadata table = new TableMetadata();
        table.setTableName("products");
        table.initializeFromScript(ddl.toString(), ProjectGenerator.databases.get(Constantes.PostgreSQL_ID), language);
        assertEquals(count, table.getColumns().length);
        return table;
    }

    /** Ajoute les templates backend au corpus pour la stack demandée. */
    private void addBackend(int columns, int frameworkId, String stack) throws Exception {
        Framework framework = ProjectGenerator.findFrameworkById(frameworkId);
        Language language = ProjectGenerator.languages.get(framework.getLanguageId());
        TableMetadata table = table(columns, language);
        String source = FrameworkTemplateLoader.loadModelTemplate(framework);
        for (String role : List.of("model", "dao", "service", "controller")) {
            Map<String, Object> primary = switch (role) {
                case "model" -> FrameworkMetadataProvider.getModelHashMap(framework, language, table);
                case "dao" -> FrameworkMetadataProvider.getModelDaoHashMap(framework, language, table);
                case "service" -> FrameworkMetadataProvider.getServiceHashMap(framework, language, table);
                default -> FrameworkMetadataProvider.getControllerHashMap(framework, language, table);
            };
            Map<String, Object> metadata = FrameworkMetadataProvider.getHashMapIntermediaire(
                    language, table, framework, OPTIONS, "/benchmark", "Corpus", "org.benchmark");
            if (role.equals("dao")) metadata.putAll(FrameworkMetadataProvider.getPrimaryModelDaoHashMap(framework, table));
            add(stack + "-" + role + "-c" + columns, source, primary, metadata, false);
        }
    }

    /** Ajoute les vues MVC et leurs métadonnées au corpus. */
    private void addMvc(int columns) throws Exception {
        FrameworkMVC framework = (FrameworkMVC) ProjectGenerator.findFrameworkById(Constantes.DOTNET_MVC_ID);
        Language language = ProjectGenerator.languages.get(framework.getLanguageId());
        TableMetadata table = table(columns, language);
        ViewsTemplate template = framework.findViewsTemplateById(Constantes.Template_1_ID);
        for (String view : List.of("list", "detail", "create", "edit")) {
            String templateName = switch (view) {
                case "list" -> template.getListTemplate();
                case "detail" -> template.getDetailTemplate();
                case "create" -> template.getCreateTemplate();
                default -> template.getEditTemplate();
            };
            Map<String, Object> primary = switch (view) {
                case "list" -> FrameworkMetadataProvider.getAltViewListHashMap(framework);
                case "detail" -> FrameworkMetadataProvider.getAltViewDetailHashMap(framework);
                case "create" -> FrameworkMetadataProvider.getAltViewCreateHashMap(framework);
                default -> FrameworkMetadataProvider.getAltViewEditHashMap(framework);
            };
            String source = FileUtils.getFileContent(Constantes.TEMPLATES_PATH + "/" + template.getTemplate()
                    + "/" + templateName + "." + Constantes.TEMPLATE_EXT);
            Map<String, Object> metadata = FrameworkMetadataProvider.getMvcHashMapIntermediaire(
                    language, table, framework, OPTIONS, "/benchmark", "Corpus", "org.benchmark");
            add("mvc-" + view + "-c" + columns, source, primary, metadata, false);
            if (view.equals("list")) {
                Map<String, Object> readOnly = new HashMap<>(metadata);
                readOnly.put("isView", true);
                add("mvc-list-readonly-c" + columns, source, primary, readOnly, false);
            }
        }
    }

    /** Ajoute les composants frontend et leurs variantes au corpus. */
    private void addFrontend(int columns) throws Exception {
        Language language = ProjectGenerator.languages.get(Constantes.Java_ID);
        for (int id : List.of(Constantes.ANGULAR_ID, Constantes.VUE_JS_ID, Constantes.REACT_ID)) {
            var framework = ProjectGenerator.findFrontendFrameworkById(id);
            var frontendLanguage = ProjectGenerator.frontendLanguage.get(framework.getLanguageId());
            TableMetadata table = table(columns, language);
            // JDBC PostgreSQL fournit les types en minuscules ; le parseur DDL conserve la casse SQL.
            for (var column : table.getColumns()) column.setColumnType(column.getColumnType().toLowerCase(Locale.ROOT));
            table.setColumnsFrontendTypes(frontendLanguage, table.getDatabase());
            Map<String, Object> metadata = FrameworkFrontendMetadataProvider.getHashMapIntermediaire(table, "/benchmark", "Corpus");
            metadata.putAll(FrameworkFrontendMetadataProvider.getHashMapForSecurity("none"));
            String source = FileUtils.getFileContent(Constantes.FRONTEND_TEMPLATE_DIRECTORY + "/"
                    + framework.getTemplate() + "." + Constantes.MODEL_TEMPLATE_EXT);
            for (var component : framework.getComponents()) {
                String name = "frontend-" + id + "-" + component.getComponentType().replaceAll("[^a-zA-Z0-9]", "-") + "-c" + columns;
                add(name, source, FrameworkFrontendMetadataProvider.getComponentHashMap(component, frontendLanguage, table), metadata, true);
                if (id == Constantes.VUE_JS_ID && (component.getComponentType().equals("List Component")
                        || component.getComponentType().equals("List View"))) {
                    Map<String, Object> readOnly = new HashMap<>(metadata);
                    readOnly.put("isView", true);
                    add(name + "-readonly", source,
                            FrameworkFrontendMetadataProvider.getComponentHashMap(component, frontendLanguage, table), readOnly, true);
                }
            }
        }
    }

    /** Enregistre les scénarios de rendu final et de génération en deux passes. */
    private void add(String name, String source, Map<String, Object> primary, Map<String, Object> metadata, boolean alternative) throws Exception {
        String prepared = engine.simpleRender(source, primary);
        workloads.add(new Workload(name, source.length(), () -> {
            String result = engine.render(prepared, metadata);
            return alternative ? engine.simpleRenderAlt(result, metadata) : result;
        }));
        // Mesurer séparément les deux passes de production et le rendu final seul.
        workloads.add(new Workload(name + "-pipeline", source.length(), () -> {
            String result = engine.render(engine.simpleRender(source, primary), metadata);
            return alternative ? engine.simpleRenderAlt(result, metadata) : result;
        }));
    }

    /** Échauffe le scénario puis mesure ses durées et allocations. */
    private Map<String, Object> measure(Workload workload, String output) throws Exception {
        boolean vueList = workload.name().startsWith("frontend-2-List-Component-")
                || workload.name().startsWith("frontend-2-List-View-");
        boolean preciseMeasurement = vueList || Boolean.getBoolean("corpus.confirmation");
        long warmupTarget = preciseMeasurement ? 500_000_000L : 100_000_000L;
        long warmupStart = System.nanoTime();
        int warmup = 0;
        do { sink = workload.render().run(); warmup++; }
        while (System.nanoTime() - warmupStart < warmupTarget && (preciseMeasurement || warmup < 2000));
        long warmupNs = System.nanoTime() - warmupStart;
        long probeStart = System.nanoTime();
        sink = workload.render().run();
        long probe = Math.max(1, System.nanoTime() - probeStart);
        int requestedIterations = (int) Math.clamp(300_000_000L / probe, 10, 2000);
        int capacity = preciseMeasurement ? (int) Math.clamp(1_500_000_000L / probe, 100, 1_000_000) : requestedIterations;
        long[] samples = new long[capacity];
        ThreadMXBean memory = (ThreadMXBean) ManagementFactory.getThreadMXBean();
        boolean allocations = memory.isThreadAllocatedMemorySupported();
        if (allocations && !memory.isThreadAllocatedMemoryEnabled()) memory.setThreadAllocatedMemoryEnabled(true);
        long thread = Thread.currentThread().threadId();
        long beforeBytes = allocations ? memory.getThreadAllocatedBytes(thread) : -1;
        long infrastructureBytes = 0;
        int iterations = 0;
        long measuredNs = 0;
        while (preciseMeasurement ? measuredNs < 1_000_000_000L : iterations < requestedIterations) {
            if (iterations == samples.length) {
                long beforeGrowth = allocations ? memory.getThreadAllocatedBytes(thread) : -1;
                samples = Arrays.copyOf(samples, Math.multiplyExact(samples.length, 2));
                if (allocations) infrastructureBytes += memory.getThreadAllocatedBytes(thread) - beforeGrowth;
            }
            long start = System.nanoTime();
            String value = workload.render().run();
            long elapsed = System.nanoTime() - start;
            samples[iterations++] = elapsed;
            measuredNs += elapsed;
            sink = value;
        }
        long allocated = allocations ? memory.getThreadAllocatedBytes(thread) - beforeBytes - infrastructureBytes : -1;
        samples = Arrays.copyOf(samples, iterations);
        long[] sorted = samples.clone();
        Arrays.sort(sorted);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("name", workload.name());
        result.put("sourceChars", workload.sourceChars());
        result.put("outputChars", output.length());
        result.put("outputSha256", HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(output.getBytes(StandardCharsets.UTF_8))));
        result.put("warmup", warmup);
        result.put("warmupNs", warmupNs);
        result.put("measuredNs", measuredNs);
        result.put("vueList", vueList);
        result.put("preciseMeasurement", preciseMeasurement);
        result.put("iterations", iterations);
        result.put("meanMs", Arrays.stream(samples).average().orElseThrow() / 1e6);
        result.put("medianMs", (sorted[(iterations - 1) / 2] / 2.0 + sorted[iterations / 2] / 2.0) / 1e6);
        result.put("allocatedBytesPerOp", allocations ? (double) allocated / iterations : null);
        result.put("samplesNs", samples);
        return result;
    }

    private record Workload(String name, int sourceChars, Render render) {}
    @FunctionalInterface private interface Render {
        /** Exécute le scénario de rendu fourni par le test. */
        String run() throws Exception;
    }
}
