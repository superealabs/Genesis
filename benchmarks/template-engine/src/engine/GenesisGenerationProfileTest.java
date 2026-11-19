package engine;

import com.fasterxml.jackson.databind.ObjectMapper;
import jdk.jfr.Configuration;
import jdk.jfr.Recording;
import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordingFile;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.labs.genesis.config.Constantes;
import org.labs.genesis.config.langage.Framework;
import org.labs.genesis.config.langage.Language;
import org.labs.genesis.config.langage.generator.framework.APIGenerator;
import org.labs.genesis.config.langage.generator.project.ProjectGenerator;
import org.labs.genesis.connexion.model.TableMetadata;
import org.labs.genesis.engine.GenesisTemplateEngine;
import org.labs.genesis.frontend.generator.FontendGenerator;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/** Mesure un lot de génération avec écritures et conserve les empreintes des fichiers. */
@Tag("generation-profile")
class GenesisGenerationProfileTest {
    @TempDir Path temporary;
    private static final Map<String, Object> OPTIONS = Map.of("cacheProvider", "none", "securityType", "none", "enableAuth", false);

    /** Mesure une génération réelle hors ligne et relève les fichiers produits. */
    @Test
    void profileOfflineComponentGeneration() throws Exception {
        var engine = new ObservedEngine();
        Path workspace = Files.createDirectory(temporary.resolve("workspace")).toRealPath();
        String variant = System.getProperty("corpus.variant", "current");
        Path reports = Path.of("build/reports/generation-profile", variant, System.getProperty("corpus.fork", "1"));
        Files.createDirectories(reports);
        try (var recording = new Recording(Configuration.getConfiguration("profile"))) {
            // Échauffer les générateurs avec des noms d’entités distincts de ceux mesurés.
            generate(engine, Files.createDirectory(workspace.resolve("warmup")), "Warmup", 1);
            engine.reset();
            recording.enable("jdk.FileRead").withThreshold(Duration.ZERO);
            recording.enable("jdk.FileWrite").withThreshold(Duration.ZERO);
            recording.enable("jdk.ExecutionSample").withPeriod(Duration.ofMillis(5));
            recording.start();
            long start = System.nanoTime();
            generate(engine, workspace, "Measured", 10);
            double totalMs = (System.nanoTime() - start) / 1e6;
            recording.stop();
            Path jfr = reports.resolve("generation.jfr");
            recording.dump(jfr);
            Map<String, String> hashes = new TreeMap<>();
            try (var files = Files.walk(workspace)) {
                for (Path path : files.filter(Files::isRegularFile).toList()) {
                    String relative = workspace.relativize(path).toString();
                    if (relative.startsWith("warmup/")) continue;
                    byte[] normalized = Files.readString(path).replace(workspace.toString(), "<WORKSPACE>").getBytes(StandardCharsets.UTF_8);
                    hashes.put(relative, HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(normalized)));
                }
            }
            assertTrue(hashes.size() >= 100, "Real generators must publish a substantial file set");
            Map<String, Long> cpu = new HashMap<>();
            Map<String, Long> allocations = new HashMap<>();
            long fileReadNs = 0, fileWriteNs = 0;
            for (RecordedEvent event : RecordingFile.readAllEvents(jfr)) {
                String type = event.getEventType().getName();
                if (type.equals("jdk.FileRead") || type.equals("jdk.FileWrite")) {
                    String path = event.getString("path");
                    if (path != null && path.startsWith(workspace.toString())) {
                        if (type.equals("jdk.FileRead")) fileReadNs += event.getDuration().toNanos();
                        else fileWriteNs += event.getDuration().toNanos();
                    }
                }
                if ((type.equals("jdk.ExecutionSample") || type.equals("jdk.ObjectAllocationSample")) && event.getStackTrace() != null) {
                    for (var frame : event.getStackTrace().getFrames()) {
                        String owner = frame.getMethod().getType().getName();
                        if (owner.startsWith("org.labs.genesis") && !owner.endsWith("ObservedEngine")) {
                            String key = owner + "." + frame.getMethod().getName();
                            if (type.equals("jdk.ExecutionSample")) cpu.merge(key, 1L, Long::sum);
                            else allocations.merge(key, event.getLong("weight"), Long::sum);
                            break;
                        }
                    }
                }
            }
            Map<String, Object> report = new LinkedHashMap<>();
            report.put("variant", variant);
            report.put("engineLocation", GenesisTemplateEngine.class.getProtectionDomain().getCodeSource().getLocation().toString());
            report.put("totalMs", totalMs);
            report.put("observedEngineMs", engine.elapsedNs / 1e6);
            report.put("publishedFileReadMs", fileReadNs / 1e6);
            report.put("publishedFileWriteMs", fileWriteNs / 1e6);
            report.put("topLevelRenderCalls", engine.topRenderCalls);
            report.put("uniqueTopLevelRenderInputs", engine.topInputs.size());
            report.put("largeTopLevelRenderCalls", engine.largeCalls);
            report.put("uniqueLargeTopLevelRenderInputs", engine.largeInputs.size());
            report.put("allRenderCallsIncludingLoopRecursion", engine.allRenderCalls);
            report.put("uniqueAllRenderInputs", engine.allInputs.size());
            report.put("uniqueInputLengths", engine.topInputs.values());
            report.put("cpuSampleCounts", cpu);
            report.put("sampledAllocationBytes", allocations);
            report.put("files", hashes);
            new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(reports.resolve("profile.json").toFile(), report);
            System.out.printf(Locale.ROOT, "GENERATION_PROFILE variant=%s files=%d total_ms=%.3f engine_ms=%.3f top_calls=%d unique=%d recursive_calls=%d%n",
                    variant, hashes.size(), totalMs, engine.elapsedNs / 1e6, engine.topRenderCalls, engine.topInputs.size(), engine.allRenderCalls);
        }
    }

    /** Produit les composants backend et frontend du lot de mesure. */
    private void generate(GenesisTemplateEngine engine, Path root, String project, int count) throws Exception {
        var backend = new APIGenerator(engine);
        var frontend = new FontendGenerator(engine);
        for (int frameworkId : List.of(Constantes.Spring_REST_API_ID, Constantes.NET_ID)) {
            Framework framework = ProjectGenerator.findFrameworkById(frameworkId);
            Language language = ProjectGenerator.languages.get(framework.getLanguageId());
            String name = project + (frameworkId == Constantes.NET_ID ? "Dotnet" : "Spring");
            for (int i = 0; i < count; i++) {
                TableMetadata table = table(language, i);
                backend.generateModel(framework, OPTIONS, language, table, root.toString(), name, "org.profile", true);
                backend.generateDao(framework, OPTIONS, language, table, root.toString(), name, "org.profile", true);
                backend.generateService(framework, OPTIONS, language, table, root.toString(), name, "org.profile", true);
                backend.generateController(framework, OPTIONS, language, table, root.toString(), name, "org.profile", true);
            }
        }
        var vue = ProjectGenerator.findFrontendFrameworkById(Constantes.VUE_JS_ID);
        var typescript = ProjectGenerator.frontendLanguage.get(vue.getLanguageId());
        for (int i = 0; i < count; i++) {
            TableMetadata table = table(ProjectGenerator.languages.get(Constantes.Java_ID), i);
            for (var column : table.getColumns()) column.setColumnType(column.getColumnType().toLowerCase(Locale.ROOT));
            table.setColumnsFrontendTypes(typescript, table.getDatabase());
            frontend.generateComponent("none", table.getDatabase(), typescript, vue, table, root.toString(), project + "Vue", true);
        }
    }

    /** Construit les métadonnées de table utilisées par le scénario. */
    private TableMetadata table(Language language, int index) {
        StringBuilder ddl = new StringBuilder("id BIGSERIAL PRIMARY KEY, name VARCHAR(120), category_id BIGINT REFERENCES categories(id), active BOOLEAN, created_at TIMESTAMP");
        for (int i = 5; i < 20; i++) ddl.append(", field_").append(i).append(i % 2 == 0 ? " VARCHAR(100)" : " NUMERIC(12,2)");
        TableMetadata table = new TableMetadata();
        table.setTableName("products_" + index);
        table.initializeFromScript(ddl.toString(), ProjectGenerator.databases.get(Constantes.PostgreSQL_ID), language);
        return table;
    }

    private static class ObservedEngine extends GenesisTemplateEngine {
        long elapsedNs, topRenderCalls, allRenderCalls;
        long largeCalls;
        int depth;
        final Map<String, Integer> topInputs = new HashMap<>();
        final Set<String> allInputs = new HashSet<>();
        final Set<String> largeInputs = new HashSet<>();
        /** Remet à zéro les compteurs du moteur instrumenté. */
        void reset() { elapsedNs = topRenderCalls = allRenderCalls = largeCalls = 0; topInputs.clear(); allInputs.clear(); largeInputs.clear(); }
        /** Chronomètre le rendu externe et compte les appels récursifs. */
        @Override public String render(String source, Map<String, Object> variables) throws Exception {
            boolean outer = depth++ == 0;
            allRenderCalls++;
            if (source != null) allInputs.add(source);
            if (outer) { topRenderCalls++; topInputs.put(source, source == null ? 0 : source.length()); }
            if (outer && source != null && source.length() >= 256) { largeCalls++; largeInputs.add(source); }
            long start = System.nanoTime();
            try { return super.render(source, variables); }
            finally { if (outer) elapsedNs += System.nanoTime() - start; depth--; }
        }
        /** Chronomètre la première passe de substitution. */
        @Override public String simpleRender(String source, Map<String, Object> variables) {
            long start = System.nanoTime();
            try { return super.simpleRender(source, variables); }
            finally { elapsedNs += System.nanoTime() - start; }
        }
        /** Chronomètre la finalisation utilisant les délimiteurs alternatifs. */
        @Override public String simpleRenderAlt(String source, Map<String, Object> variables) {
            long start = System.nanoTime();
            try { return super.simpleRenderAlt(source, variables); }
            finally { elapsedNs += System.nanoTime() - start; }
        }
    }
}
