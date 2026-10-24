package org.labs.genesis.api.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.labs.genesis.api.GenesisApiApplication;
import org.labs.genesis.api.context.GenerationContextStore;
import org.labs.genesis.config.git.GitConfiguration;
import org.labs.genesis.config.langage.FilesEdit;
import org.labs.utils.GitUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.net.http.*;
import java.nio.file.*;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Vrai serveur HTTP, publication réelle et commandes Git de Core sur @TempDir uniquement. */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GitIntegrationTest {
    private ConfigurableApplicationContext app;
    private String url;
    private GenerationContextStore store;
    private GenerationPublicationService publication;
    private GitLocalService service;
    // /tmp peut contenir un marqueur .git : ne jamais le supprimer pour les tests.
    @TempDir(factory = RecipeTempDir.class) Path temporary;
    public static class RecipeTempDir implements org.junit.jupiter.api.io.TempDirFactory {
        @Override public Path createTempDirectory(org.junit.jupiter.api.extension.AnnotatedElementContext element,
                org.junit.jupiter.api.extension.ExtensionContext context) throws java.io.IOException {
            return Files.createTempDirectory(Path.of("/var/tmp"), "genesis-git-test-");
        }
    }
    private Path root;

    @BeforeAll void start() {
        app = SpringApplication.run(GenesisApiApplication.class, "--server.port=0", "--spring.main.banner-mode=off");
        url = "http://127.0.0.1:" + app.getEnvironment().getProperty("local.server.port") + "/api/generator/git";
        store = app.getBean(GenerationContextStore.class);
        publication = app.getBean(GenerationPublicationService.class);
        service = app.getBean(GitLocalService.class);
    }
    @AfterAll void stop() { if (app != null) app.close(); }

    @BeforeEach void fixture() throws Exception {
        var context = store.getContext();
        context.setProjectName("Demo");
        context.setDestinationFolder(Files.createTempDirectory(temporary, "case-").toString());
        context.setWebappFolder("webapp");
        context.setGenerateProjectStructure(true);
        context.setGenerateFrontendApp(true);
        new FrameworkService(store).select(1);
        new FrontendConfigService(store, new FrontendFrameworkIsolationService()).configure(
                new FrontendConfigService.FrontendConfigRequest(1, null, null));
        context.setGitConfiguration(new GitConfiguration());
        root = Path.of(context.getDestinationFolder());
        var staging = new GenerationStagingService();
        Path workspace = staging.createWorkspace();
        try {
            Path batch = staging.createBatchDirectory(workspace, 0);
            Files.createDirectories(batch.resolve("Demo"));
            Files.createDirectories(batch.resolve("DemoWebapp"));
            Files.writeString(batch.resolve("Demo/backend.txt"), "backend");
            Files.writeString(batch.resolve("DemoWebapp/frontend.txt"), "frontend");
            publication.consolidateAndPublish(List.of(batch), workspace, root);
        } finally { staging.deleteWorkspace(workspace); }
    }

    private HttpResponse<String> post(String endpoint, String body) throws Exception {
        return HttpClient.newHttpClient().send(HttpRequest.newBuilder(URI.create(url + endpoint))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofString());
    }
    private void configure(boolean separate) throws Exception {
        assertThat(post("/config", "{\"useGit\":true,\"separateRepositories\":" + separate + "}").statusCode()).isEqualTo(200);
    }
    private String git(Path path, String... args) throws Exception {
        var command = new java.util.ArrayList<>(List.of("git", "-C", path.toString()));
        command.addAll(List.of(args));
        Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
        String output = new String(process.getInputStream().readAllBytes());
        assertThat(process.waitFor()).as(output).isZero();
        return output.trim();
    }
    private void committed(Path path) throws Exception {
        assertThat(git(path, "branch", "--show-current")).isEqualTo("main");
        assertThat(git(path, "rev-list", "--count", "HEAD")).isEqualTo("1");
        assertThat(git(path, "log", "-1", "--format=%s")).isEqualTo("Initialisation du projet");
        assertThat(git(path, "status", "--porcelain")).isEmpty();
    }

    @Test void disabledDoesNotInitialize() throws Exception {
        assertThat(post("/init", "{}").statusCode()).isEqualTo(200);
        assertThat(root.resolve(".git")).doesNotExist();
    }
    @Test void singleRepositoryUsesRealPublishedRootAndCoreIgnores() throws Exception {
        configure(false);
        assertThat(post("/init", "{}").statusCode()).isEqualTo(200);
        committed(root);
        assertThat(root.resolve("Demo/.git")).doesNotExist();
        assertThat(root.resolve("Demo/.gitignore")).hasContent(GitUtils.getGitIgnore(store.getContext().getFramework().getConditionalFiles()).getContent());
        assertThat(root.resolve("DemoWebapp/.gitignore")).exists();
        assertThat(git(root, "ls-files")).contains("Demo/backend.txt", "DemoWebapp/frontend.txt");
    }
    @Test void separateRepositoriesAreBothCommitted() throws Exception {
        configure(true);
        assertThat(post("/init", "{}").statusCode()).isEqualTo(200);
        committed(root.resolve("Demo")); committed(root.resolve("DemoWebapp"));
        assertThat(root.resolve(".git")).doesNotExist();
    }
    @Test void existingFilesAndIgnoreArePreserved() throws Exception {
        Files.writeString(root.resolve("Demo/.gitignore"), "personal.tmp\n");
        Files.writeString(root.resolve("personal.txt"), "existing content");
        Files.writeString(root.resolve("Demo/personal.tmp"), "ignored");
        configure(false);
        assertThat(post("/init", "{}").statusCode()).isEqualTo(200);
        assertThat(root.resolve("Demo/.gitignore")).hasContent("personal.tmp\n");
        assertThat(root.resolve("personal.txt")).hasContent("existing content");
        assertThat(git(root, "ls-files")).doesNotContain("personal.tmp");
        committed(root);
    }
    @Test void existingRepositoryAndBranchSurviveRetry() throws Exception {
        configure(false); post("/init", "{}");
        git(root, "checkout", "-b", "keep-me");
        String head = git(root, "rev-parse", "HEAD");
        assertThat(post("/init", "{}").statusCode()).isEqualTo(409);
        assertThat(git(root, "branch", "--show-current")).isEqualTo("keep-me");
        assertThat(git(root, "rev-parse", "HEAD")).isEqualTo(head);
    }
    @Test void existingSecondRepositoryRejectsBeforeFirstIsCreated() throws Exception {
        GitUtils.gitInit(root.resolve("DemoWebapp").toString());
        configure(true);
        assertThat(post("/init", "{}").statusCode()).isEqualTo(409);
        assertThat(root.resolve("Demo/.git")).doesNotExist();
        assertThat(root.resolve("Demo/.gitignore")).doesNotExist();
    }
    @Test void missingFrontendRejectsBeforeAnyWrite() throws Exception {
        Files.delete(root.resolve("DemoWebapp/frontend.txt")); Files.delete(root.resolve("DemoWebapp"));
        configure(true);
        assertThat(post("/init", "{}").statusCode()).isEqualTo(409);
        assertThat(root.resolve("Demo/.git")).doesNotExist();
        assertThat(root.resolve("Demo/.gitignore")).doesNotExist();
    }
    @Test void backendOnlyWorksWithoutFrontend() throws Exception {
        store.getContext().setFrontendFramework(null);
        store.getContext().setGenerateFrontendApp(false);
        configure(true);
        assertThat(post("/init", "{}").statusCode()).isEqualTo(200);
        committed(root.resolve("Demo"));
        assertThat(root.resolve("DemoWebapp/.git")).doesNotExist();
    }
    @Test void componentsOnlyDoesNotCreateFrontendRepository() throws Exception {
        store.getContext().setGenerateProjectStructure(false);
        configure(true); assertThat(post("/init", "{}").statusCode()).isEqualTo(200);
        committed(root.resolve("Demo"));
        assertThat(root.resolve("DemoWebapp/.git")).doesNotExist();
    }
    @Test void mismatchedPublishedProjectIsRejected() throws Exception {
        store.getContext().setProjectName("Other"); configure(false);
        assertThat(post("/init", "{}").statusCode()).isEqualTo(409);
        assertThat(root.resolve(".git")).doesNotExist();
    }
    @Test void symlinkInPublishedTreeIsRejected() throws Exception {
        Files.createSymbolicLink(root.resolve("link"), temporary);
        configure(false); assertThat(post("/init", "{}").statusCode()).isEqualTo(409);
        assertThat(root.resolve(".git")).doesNotExist();
    }
    @Test void nestedWorktreeMarkerIsPreserved() throws Exception {
        Files.writeString(root.resolve("Demo/.git"), "gitdir: somewhere\n");
        configure(false); assertThat(post("/init", "{}").statusCode()).isEqualTo(409);
        assertThat(root.resolve("Demo/.git")).hasContent("gitdir: somewhere\n");
        assertThat(root.resolve(".git")).doesNotExist();
    }
    @Test void ancestorRepositoryIsPreserved() throws Exception {
        GitUtils.gitInit(root.getParent().toString());
        configure(false); assertThat(post("/init", "{}").statusCode()).isEqualTo(409);
        assertThat(root.resolve(".git")).doesNotExist();
        assertThat(git(root.getParent(), "branch", "--show-current")).isEqualTo("main");
    }
    @Test void ignoreDirectoryIsRejectedBeforeInit() throws Exception {
        Files.createDirectory(root.resolve("Demo/.gitignore"));
        configure(false); assertThat(post("/init", "{}").statusCode()).isEqualTo(409);
        assertThat(root.resolve(".git")).doesNotExist();
    }
    @Test void corePathOutsidePublicationIsRejected() throws Exception {
        var framework = store.getContext().getFramework();
        FilesEdit bad = new FilesEdit(); bad.setFileName(".gitignore");
        bad.setDestinationPath(temporary.resolve("outside").toString());
        var original = framework.getConditionalFiles();
        try {
            framework.setConditionalFiles(List.of(bad)); configure(true);
            assertThat(post("/init", "{}").statusCode()).isEqualTo(409);
            assertThat(temporary.resolve("outside")).doesNotExist();
        } finally { framework.setConditionalFiles(original); }
    }
    @Test void noPublicationIsRejected() {
        var fresh = new GenerationPublicationService(new GenerationStagingService(), new GeneratedFilesPublisher());
        var local = new GitLocalService(fresh, store);
        store.getContext().setGitConfiguration(enabled());
        assertThatThrownBy(local::init).isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("409");
    }
    private GitConfiguration enabled() { var config = new GitConfiguration(); config.setUseGit(true); return config; }
    @Test void invalidRemoteConfigurationDoesNotReplacePreviousConfig() throws Exception {
        configure(false);
        var previous = store.getContext().getGitConfiguration();
        assertThat(post("/config", "{\"useGit\":true,\"createRemoteRepository\":true}").statusCode()).isEqualTo(400);
        assertThat(store.getContext().getGitConfiguration()).isSameAs(previous);
    }
    @Test void githubConfigurationNeverEchoesToken() throws Exception {
        var response = post("/config", "{\"useGit\":true,\"createRemoteRepository\":true,\"repositoryName\":\"demo\",\"githubUsername\":\"example\",\"githubToken\":\"FAKE-TEST-SECRET\"}");
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).doesNotContain("FAKE-TEST-SECRET", "githubToken");
        assertThat(service.plan(store.getContext(), service.configuration()).toString()).doesNotContain("FAKE-TEST-SECRET");
    }
    private org.mockito.MockedStatic<GitUtils> localCore() {
        return mockStatic(GitUtils.class, call -> {
            if (call.getMethod().getName().equals("createRemoteRepo")) return "{}";
            if (call.getMethod().getName().equals("gitPush")) return null;
            return call.callRealMethod();
        });
    }

    @Test void githubCallsAreDelegatedToCoreWithNetworkFullyStubbed() throws Exception {
        var config = enabled(); config.setCreateRemoteRepository(true); config.setRepositoryName("demo");
        config.setGithubUsername("example"); config.setGithubToken("FAKE-TEST-SECRET");
        store.getContext().setGitConfiguration(config);
        try (var core = localCore()) {
            core.when(() -> GitUtils.createRemoteRepo(anyString(), anyString(), anyBoolean())).thenReturn("{}");
            core.when(() -> GitUtils.gitPush(anyString(), anyString(), anyString())).thenAnswer(call -> null);
            service.init();
            core.verify(() -> GitUtils.createRemoteRepo("FAKE-TEST-SECRET", "demo", false));
            core.verify(() -> GitUtils.gitPush(root.toString(), "example", "FAKE-TEST-SECRET"));
        }
        committed(root);
        assertThat(git(root, "remote", "get-url", "origin")).isEqualTo("https://github.com/example/demo.git");
    }
    @Test void existingRemoteOptionAddsOriginWithoutCreatingOrPushing() throws Exception {
        var config = enabled(); config.setRepositoryName("existing"); config.setGithubUsername("example");
        store.getContext().setGitConfiguration(config);
        try (var core = localCore()) {
            core.when(() -> GitUtils.createRemoteRepo(anyString(), anyString(), anyBoolean())).thenThrow(new AssertionError("network forbidden"));
            core.when(() -> GitUtils.gitPush(anyString(), anyString(), anyString())).thenThrow(new AssertionError("push forbidden"));
            service.init();
        }
        assertThat(git(root, "remote", "get-url", "origin")).isEqualTo("https://github.com/example/existing.git");
    }
    @Test void launchInitializesOnlyAfterRealPublication() throws Exception {
        store.getContext().setGenerateProjectStructure(false);
        store.getContext().setGenerateFrontendApp(false);
        store.getContext().setDestinationFolder(Files.createTempDirectory(temporary, "launch-").toString());
        root = Path.of(store.getContext().getDestinationFolder());
        configure(true);
        var snapshot = new GenerationConfigurationSnapshot(List.of(), java.util.Map.of());
        var snapshots = mock(GenerationConfigurationSnapshotService.class);
        when(snapshots.capture()).thenReturn(snapshot);
        var preflight = mock(GenerationPreflightService.class);
        var batch = new org.labs.genesis.api.dto.GenerationExecutionPlanDtos.ExecutionBatch("test", "Demo", false, List.of("Model"));
        var executionPlan = new org.labs.genesis.api.dto.GenerationExecutionPlanDtos.ExecutionPlanResponse(1, 1, 1, 0, List.of(batch));
        when(preflight.prepare(snapshot)).thenReturn(new GenerationPreflightService.PreflightResult(executionPlan, null, List.of()));
        var contexts = mock(GenerationExecutionContextFactory.class);
        when(contexts.openConsolidated(snapshot)).thenAnswer(call ->
                new GenerationExecutionContextFactory.ExecutionContext("test", store.getContext().duplicateWithNoTables(), null));
        var backend = mock(BackendBatchGenerationService.class);
        when(backend.generate(anyList(), any(Path.class), eq(snapshot))).thenAnswer(call -> {
            Path workspace = call.getArgument(1);
            Path generated = Files.createDirectories(workspace.resolve("fixture/Demo/Demo"));
            Files.writeString(generated.resolve("generated.txt"), "published before Git");
            // Le plan Git capturé doit survivre à une modification ultérieure de l'option globale.
            store.getContext().setGitConfiguration(new GitConfiguration());
            return List.of(generated.getParent());
        });
        var launcher = new GenerationExecutionService(snapshots, preflight, new GenerationStagingService(), backend,
                contexts, mock(MvcSharedResourcesGenerationService.class), new GenerationOutputPathService(),
                mock(ConsolidatedFrontendGenerationService.class), publication, service);
        assertThat(launcher.launch().success()).isTrue();
        assertThat(publication.getLastPublishedTarget()).isEqualTo(root.toRealPath());
        committed(root.resolve("Demo"));
        assertThat(git(root.resolve("Demo"), "show", "HEAD:generated.txt")).isEqualTo("published before Git");
    }

    @Test void customFrontendFolderUsesCoreMetadata() throws Exception {
        Files.move(root.resolve("DemoWebapp"), root.resolve("DemoClient"));
        store.getContext().setWebappFolder("client");
        configure(true);
        assertThat(post("/init", "{}").statusCode()).isEqualTo(200);
        committed(root.resolve("DemoClient"));
        assertThat(root.resolve("DemoWebapp")).doesNotExist();
    }
    @Test void backendFallbackWorksWithoutGitignoreTemplate() throws Exception {
        var framework = store.getContext().getFramework();
        var original = framework.getConditionalFiles();
        try {
            framework.setConditionalFiles(List.of());
            configure(true);
            assertThat(post("/init", "{}").statusCode()).isEqualTo(200);
            committed(root.resolve("Demo"));
            assertThat(root.resolve("Demo/.gitignore")).doesNotExist();
        } finally { framework.setConditionalFiles(original); }
    }
    @Test void invalidFrontendRemoteNameRejectsBeforeWriting() {
        var config = enabled(); config.setSeparateRepositories(true); config.setCreateRemoteRepository(true);
        config.setBackendRepositoryName("backend"); config.setFrontendRepositoryName("../unsafe");
        config.setGithubUsername("example"); config.setGithubToken("FAKE-TEST-SECRET");
        store.getContext().setGitConfiguration(config);
        assertThatThrownBy(service::init).isInstanceOf(ResponseStatusException.class).hasMessageContaining("400");
        assertThat(root.resolve("Demo/.git")).doesNotExist();
        assertThat(root.resolve("Demo/.gitignore")).doesNotExist();
    }
    @Test void coreErrorsAreSanitized() throws Exception {
        configure(false);
        try (var core = localCore()) {
            core.when(() -> GitUtils.gitCommit(anyString(), anyString())).thenThrow(new RuntimeException("FAKE-TEST-SECRET"));
            assertThatThrownBy(service::init).isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("500").hasMessageNotContaining("FAKE-TEST-SECRET").hasNoCause();
        }
        assertThat(root.resolve(".git")).isDirectory();
        assertThat(root.resolve("Demo/backend.txt")).hasContent("backend");
    }

    @Test void emptyCommitFailureKeepsRepositoryAndFiles() throws Exception {
        Files.writeString(root.resolve(".gitignore"), "*\n");
        configure(false);
        assertThat(post("/init", "{}").statusCode()).isEqualTo(500);
        assertThat(root.resolve(".git")).isDirectory();
        assertThat(root.resolve("Demo/backend.txt")).hasContent("backend");
        assertThat(post("/init", "{}").statusCode()).isEqualTo(409);
    }
}
