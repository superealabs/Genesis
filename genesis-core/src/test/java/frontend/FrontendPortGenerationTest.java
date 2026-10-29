package frontend;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.config.langage.generator.project.ProjectGenerator;
import org.labs.genesis.frontend.generator.FrontendFramework;
import org.labs.genesis.frontend.generator.frameworkFrontend.FrameworkFrontendMetadataProvider;
import org.labs.utils.FileUtils;
import org.labs.utils.GenerationWriteScope;
import org.labs.utils.enums.FilesUtilsMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;

class FrontendPortGenerationTest {
    @TempDir Path directory;

    @Test
    void rendersEachRequestedPortWithoutChangingTheRegistry() throws Exception {
        for (int framework : new int[]{1, 3}) {
            for (String port : new String[]{"14220", "14221"}) {
                assertGeneratedPort(framework, port, port);
            }
        }
    }

    @Test
    void preservesDefaultPortsWhenNoPortIsProvided() throws Exception {
        assertGeneratedPort(1, null, "4200");
        assertGeneratedPort(3, null, "3000");
    }

    private void assertGeneratedPort(int identifier, String port, String expected) throws Exception {
        var mapper = new ObjectMapper();
        var original = ProjectGenerator.frontendFrameworks.get(identifier);
        String originalConfiguration = mapper.writeValueAsString(original);
        var frontend = mapper.readValue(originalConfiguration, FrontendFramework.class);
        Path root = Files.createTempDirectory(directory, "generation-");
        var context = new ProjectGenerationContext();
        context.setProjectName("PortProbe").setDestinationFolder(root.toString());
        context.setFramework(ProjectGenerator.frameworks.get(1));
        context.setLanguage(ProjectGenerator.findLanguageById(1));
        context.setFrontendFramework(frontend);
        context.setFrontendLanguage(ProjectGenerator.frontendLanguage.get(1));
        context.setGenerateFrontendApp(true);
        context.setFrontendPort(port);
        context.setProjectPort("18300");
        context.setFrameworkConfiguration(Map.of("securityType", "NONE"));
        try (var scope = GenerationWriteScope.open(root);
             var mode = FileUtils.useExecutionMode(FilesUtilsMode.GENERATION)) {
            ProjectGenerator.initFrontendProjectFiles(context);
            new ProjectGenerator().generateFrontendSharedFiles(context, List.of());
        }
        Path webapp = Path.of(FrameworkFrontendMetadataProvider.getWebappFolder(context));
        if (identifier == 1) {
            var result = mapper.readTree(Files.readString(webapp.resolve("package.json")));
            assertThat(result.at("/scripts/start").asText()).isEqualTo("ng serve --port " + expected);
            assertThat(result.at("/dependencies/@angular~1core").asText()).isEqualTo("^17.3.0");
            assertThat(result.at("/scripts/build").asText()).isEqualTo("ng build");
        } else {
            assertThat(Files.readString(webapp.resolve("vite.config.ts")))
                    .contains("port: " + expected + ",")
                    .contains("react()")
                    .doesNotContain("${frontendPort}");
        }
        assertThat(mapper.writeValueAsString(original)).isEqualTo(originalConfiguration);
    }
}
