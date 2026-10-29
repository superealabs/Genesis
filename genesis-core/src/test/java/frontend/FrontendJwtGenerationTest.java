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

class FrontendJwtGenerationTest {
    @TempDir Path directory;

    @Test
    void angularKeepsAuthenticationAndProtectedEntityRoutes() throws Exception {
        Path app = generate(1, "Spring Security - JWT");
        assertThat(Files.readString(app.resolve("src/app/app.routes.ts")))
                .contains("...loginRoutes,", "...villeRoutes", "canActivateChild", "path: 'welcome'");
        assertThat(Files.readString(app.resolve("src/app/app.component.ts")))
                .contains("isLoggedIn", "AuthService");
        assertThat(Files.readString(app.resolve("src/app/features/authentication/services/authentication.service.ts")))
                .contains("localStorage.getItem('authToken')", "localStorage.setItem('authToken', response.data)");
    }

    @Test
    void reactKeepsJwtRoutesInTheFinalRouter() throws Exception {
        Path app = generate(3, "Spring Security - JWT");
        assertThat(Files.readString(app.resolve("src/router.tsx")))
                .contains("'/login'", "<ProtectedRoute", "'/profile'", "'/change-password'");
        assertThat(Files.readString(app.resolve("src/index.tsx"))).contains("<AuthProvider>");
        assertThat(Files.readString(app.resolve("src/services/auth.ts")))
                .contains("headers: token ? { Authorization: `Bearer ${token}` } : {}");
    }

    @Test
    void vueRegistrationAndUnauthorizedHandlingMatchTheBackend() throws Exception {
        Path app = generate(2, "Spring Security - JWT");
        assertThat(Files.readString(app.resolve("src/components/auth/RegisterForm.vue")))
                .contains("RegisterValidator.usernameRules) ?? null", "RegisterValidator.emailRules) ?? null",
                        "RegisterValidator.passwordRules) ?? null")
                .doesNotContain("password.value, 1)");
        assertThat(Files.readString(app.resolve("src/models/auth/RegisterDTO.ts")))
                .contains("this.name = name").doesNotContain("this.username", "roleidRoleEntity");
        assertThat(Files.readString(app.resolve("src/services/api.ts")))
                .doesNotContain("authentication.logout()", "const authentication = useAuth()");
    }

    @Test
    void nonJwtGenerationDoesNotAddAuthenticationDependencies() throws Exception {
        for (int id : new int[]{1, 2, 3}) {
            Path app = generate(id, "NONE");
            String routes = Files.readString(app.resolve(id == 1 ? "src/app/app.routes.ts"
                    : id == 2 ? "src/router/routes.ts" : "src/router.tsx"));
            assertThat(routes).doesNotContain("loginRoutes", "ProtectedRoute", "requiresAuth", "authRoutes");
        }
    }

    private Path generate(int identifier, String security) throws Exception {
        var mapper = new ObjectMapper();
        var original = ProjectGenerator.frontendFrameworks.get(identifier);
        String before = mapper.writeValueAsString(original);
        var context = new ProjectGenerationContext();
        Path root = Files.createTempDirectory(directory, "jwt-");
        context.setProjectName("JwtProbe").setDestinationFolder(root.toString());
        context.setFramework(ProjectGenerator.frameworks.get(1));
        context.setLanguage(ProjectGenerator.findLanguageById(1));
        context.setFrontendFramework(mapper.readValue(before, FrontendFramework.class));
        context.setFrontendLanguage(ProjectGenerator.frontendLanguage.get(1));
        context.setGenerateFrontendApp(true);
        context.setFrontendPort("14300");
        context.setProjectPort("18100");
        context.setFrameworkConfiguration(Map.of("securityType", security));
        var entity = new org.labs.genesis.connexion.model.TableMetadata();
        entity.setTableName("ville");
        entity.setClassName("Ville");
        entity.setIsView(false);
        entity.setColumns(new org.labs.genesis.connexion.model.ColumnMetadata[0]);
        try (var scope = GenerationWriteScope.open(root);
             var mode = FileUtils.useExecutionMode(FilesUtilsMode.GENERATION)) {
            ProjectGenerator.initFrontendProjectFiles(context);
            new ProjectGenerator().generateFrontendSharedFiles(context, List.of(entity));
        }
        assertThat(mapper.writeValueAsString(original)).isEqualTo(before);
        return Path.of(FrameworkFrontendMetadataProvider.getWebappFolder(context));
    }
}
