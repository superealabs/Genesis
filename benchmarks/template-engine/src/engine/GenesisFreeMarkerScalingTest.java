package engine;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.management.ThreadMXBean;
import freemarker.template.*;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.labs.genesis.engine.GenesisTemplateEngine;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.*;

/** Compare Genesis et FreeMarker sur les mêmes pages, selon leur taille et leur réutilisation. */
@Tag("freemarker-scaling")
class GenesisFreeMarkerScalingTest {
    private static final String GENESIS = "data_genesis/templates/Template 1/ListViewTemplate.templ";
    private static final String FREEMARKER = "benchmark/spring-list.ftl";
    private static volatile String sink;

    /** Compare Genesis et FreeMarker selon la taille et le mode de la page. */
    @Test void compareCompleteViewsAtSeveralSizes() throws Exception {
        String measuredEngine = System.getProperty("scaling.engine", "genesis");
        String variant = System.getProperty("corpus.variant", "current");
        Path reports = Path.of("build/reports/freemarker-scaling", variant, System.getProperty("corpus.fork", "1"));
        Files.createDirectories(reports);
        Map<String,Object> base = new ObjectMapper().readValue(read("benchmark/spring-list-model.json"), new TypeReference<>() {});
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_35);
        configuration.setDefaultEncoding("UTF-8");
        configuration.setLocale(Locale.ROOT);
        configuration.setNumberFormat("computer");
        configuration.setInterpolationSyntax(Configuration.SQUARE_BRACKET_INTERPOLATION_SYNTAX);
        configuration.setWhitespaceStripping(false);
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        configuration.setLogTemplateExceptions(false);
        configuration.setFallbackOnNullLoopVariable(false);
        String genesisSource = read(GENESIS);
        Template parsed = new Template(FREEMARKER, new StringReader(read(FREEMARKER)), configuration);
        GenesisTemplateEngine genesis = new GenesisTemplateEngine();
        List<Map<String,Object>> results = new ArrayList<>();
        for (int fields : List.of(5,20,50)) for (boolean readOnly : List.of(false,true)) {
            Map<String,Object> model = model(base, fields, readOnly);
            String expected = renderFreeMarker(parsed, model);
            assertEquals(normalize(expected), normalize(renderGenesis(genesis, genesisSource, model)));
            for (String scenario : List.of("reused", "one-use")) {
                Render render;
                if (measuredEngine.equals("freemarker")) {
                    render = scenario.equals("reused") ? () -> renderFreeMarker(parsed, model) :
                            () -> renderFreeMarker(new Template(FREEMARKER, new StringReader(read(FREEMARKER)), configuration), model);
                } else {
                    render = scenario.equals("reused") ? () -> renderGenesis(genesis, genesisSource, model) :
                            () -> renderGenesis(new GenesisTemplateEngine(), read(GENESIS), model);
                }
                results.add(measure(scenario + "-c" + fields + "-readonly-" + readOnly, render));
            }
        }
        Map<String,Object> report = new LinkedHashMap<>();
        report.put("variant",variant);
        report.put("measuredEngine",measuredEngine);
        report.put("java",System.getProperty("java.version"));
        report.put("freeMarkerVersion",Configuration.getVersion().toString());
        report.put("engineLocation",GenesisTemplateEngine.class.getProtectionDomain().getCodeSource().getLocation().toString());
        report.put("results",results);
        new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(reports.resolve("results.json").toFile(),report);
        System.out.printf("FREEMARKER_SCALING variant=%s engine=%s cases=%d equality=passed%n", variant,measuredEngine,results.size());
    }

    /** Adapte le modèle au nombre de colonnes et au mode de lecture. */
    @SuppressWarnings("unchecked")
    private Map<String,Object> model(Map<String,Object> base,int count,boolean readOnly) {
        Map<String,Object> model = new HashMap<>(base);
        List<Map<String,Object>> original = (List<Map<String,Object>>) base.get("fields");
        List<Map<String,Object>> fields = new ArrayList<>();
        for(int i=0;i<count;i++) {
            Map<String,Object> field = new HashMap<>(original.get(i % original.size()));
            if(i>=original.size()) { field.put("name",field.get("name")+"Extra"+i); field.put("isPrimaryKey",false); }
            fields.add(field);
        }
        model.put("fields",fields);
        model.put("fieldsFK",fields.stream().filter(f -> Boolean.TRUE.equals(f.get("isForeignKey"))).toList());
        model.put("isView",readOnly);
        return model;
    }
    /** Échauffe le scénario puis mesure ses durées et allocations. */
    private Map<String,Object> measure(String name,Render render) throws Exception {
        long start = System.nanoTime();
        int warmup = 0;
        do { sink=render.run(); warmup++; } while(System.nanoTime()-start<100_000_000L && warmup<5000);
        start=System.nanoTime(); sink=render.run();
        int count=(int)Math.clamp(300_000_000L/Math.max(1,System.nanoTime()-start),10,5000);
        long[] samples=new long[count];
        ThreadMXBean allocations=(ThreadMXBean)ManagementFactory.getThreadMXBean();
        if(!allocations.isThreadAllocatedMemoryEnabled())allocations.setThreadAllocatedMemoryEnabled(true);
        long thread=Thread.currentThread().threadId();
        long before=allocations.getThreadAllocatedBytes(thread);
        for(int i=0;i<count;i++) { start=System.nanoTime(); String value=render.run(); samples[i]=System.nanoTime()-start; sink=value; }
        double bytes=(double)(allocations.getThreadAllocatedBytes(thread)-before)/count;
        long[] sorted=samples.clone(); Arrays.sort(sorted);
        return Map.of("name",name,"iterations",count,"warmup",warmup,
                "meanMs",Arrays.stream(samples).average().orElseThrow()/1e6,
                "medianMs",(sorted[(count-1)/2]/2.0+sorted[count/2]/2.0)/1e6,
                "allocatedBytesPerOp",bytes,"samplesNs",samples);
    }
    /** Rend la page avec Genesis et les données du scénario. */
    private String renderGenesis(GenesisTemplateEngine engine,String source,Map<String,Object> model)throws Exception {
        return engine.simpleRenderAlt(engine.render(source,model),Map.of("thymeleafDollar","$"));
    }
    /** Rend le template FreeMarker dans un tampon texte. */
    private String renderFreeMarker(Template template,Map<String,Object> model)throws Exception {
        StringWriter writer=new StringWriter(); template.process(model,writer); return writer.toString();
    }
    /** Charge une ressource du classpath pour le benchmark. */
    private String read(String name)throws IOException {
        try(InputStream input=getClass().getClassLoader().getResourceAsStream(name)) {
            if(input==null)throw new FileNotFoundException(name);
            return new String(input.readAllBytes(),StandardCharsets.UTF_8);
        }
    }
    /** Retire l’indentation et les lignes vides avant comparaison des moteurs. */
    private String normalize(String value) {
        return value.lines().map(String::strip).filter(line -> !line.isEmpty()).collect(Collectors.joining("\n"));
    }
    @FunctionalInterface private interface Render {
        /** Exécute le scénario de rendu fourni par le test. */
        String run() throws Exception;
    }
}
