package org.labs.genesis.api.service;

import org.labs.genesis.api.context.GenerationContextStore;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.config.git.GitConfiguration;
import org.labs.genesis.config.langage.FilesEdit;
import org.labs.genesis.config.langage.generator.project.ProjectGenerator;
import org.labs.genesis.frontend.generator.frameworkFrontend.FrameworkFrontendMetadataProvider;
import org.labs.utils.GitUtils;
import org.labs.utils.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class GitLocalService {

    private final GenerationPublicationService publicationService;

    private final GenerationContextStore contextStore;

    public GitLocalService(GenerationPublicationService publicationService, GenerationContextStore contextStore) {
        this.publicationService = publicationService;
        this.contextStore = contextStore;
    }

    public GitConfiguration configuration() {
        GitConfiguration config = contextStore.getContext().getGitConfiguration();
        return config == null ? new GitConfiguration() : config;
    }

    public static void validateConfiguration(GitConfiguration config) {
        if (config == null)
            throw invalid("Configuration Git absente");
        if (!config.isUseGit())
            return;
        if (config.isCreateRemoteRepository()) {
            if (config.getGithubToken() == null || config.getGithubToken().isBlank()) {
                throw invalid("Le token GitHub est obligatoire pour la création distante");
            }
            if (config.getGithubUsername() == null || !config.getGithubUsername().matches("[A-Za-z0-9][A-Za-z0-9-]{0,38}")) {
                throw invalid("Utilisateur GitHub invalide");
            }
            if (config.isSeparateRepositories()) {
                validateName(config.getBackendRepositoryName());
                // Le frontend est facultatif : son nom sera vérifié si un dépôt frontend est prévu.
            } else
                validateName(config.getRepositoryName());
        }
    }

    private static void validateName(String name) {
        if (name == null || !name.matches("[A-Za-z0-9_.-]{1,100}") || name.equals(".") || name.equals("..")) {
            throw invalid("Nom de dépôt GitHub invalide");
        }
    }

    /**
     * Capture les chemins produits par Core avant le remplacement de la destination par le staging.
     */
    public GitPlan plan(ProjectGenerationContext context, GitConfiguration config) {
        validateConfiguration(config);
        if (!config.isUseGit())
            return new GitPlan(false, null, List.of(), List.of());
        if (context.getFramework() == null || context.getProjectName() == null) {
            throw invalid("Le framework et le nom du projet sont obligatoires");
        }
        Path root = Path.of(context.getDestinationFolder()).toAbsolutePath().normalize();
        FilesEdit backendIgnore = GitUtils.getGitIgnore(context.getFramework().getConditionalFiles());
        Path backend = Path.of(backendIgnore == null ? root + "/" + StringUtils.majStart(context.getProjectName()) : ProjectGenerator.engine.simpleRender(backendIgnore.getDestinationPath(), Map.of("projectName", context.getProjectName(), "destinationFolder", context.getDestinationFolder()))).toAbsolutePath().normalize();
        List<IgnoreFile> ignores = new ArrayList<>();
        if (backendIgnore != null)
            ignores.add(new IgnoreFile(backend, backendIgnore.getContent()));
        List<Repository> repositories = new ArrayList<>();
        if (config.isSeparateRepositories())
            repositories.add(repository(backend, config.getBackendRepositoryName(), config));
        else
            repositories.add(repository(root, config.getRepositoryName(), config));
        boolean frontend = context.isGenerateProjectStructure() && context.isGenerateFrontendApp() && Boolean.TRUE.equals(context.getFramework().getUseFrontendApp()) && context.getFrontendFramework() != null;
        if (frontend) {
            Path frontendPath = Path.of(FrameworkFrontendMetadataProvider.getWebappFolder(context)).toAbsolutePath().normalize();
            FilesEdit frontendIgnore = GitUtils.getGitIgnore(context.getFrontendFramework().getConditionalFiles());
            if (frontendIgnore != null)
                ignores.add(new IgnoreFile(frontendPath, frontendIgnore.getContent()));
            if (config.isSeparateRepositories()) {
                if (config.isCreateRemoteRepository())
                    validateName(config.getFrontendRepositoryName());
                repositories.add(repository(frontendPath, config.getFrontendRepositoryName(), config));
            }
        }
        return new GitPlan(true, root, List.copyOf(repositories), List.copyOf(ignores));
    }

    private Repository repository(Path path, String name, GitConfiguration config) {
        // Le jeton n'entre jamais dans une réponse REST ni dans le toString du plan.
        return new Repository(path, name, config.isCreateRemoteRepository(), config.getGithubUsername(), config.getGithubToken());
    }

    public synchronized Map<String, Object> init() throws Exception {
        return init(plan(contextStore.getContext(), configuration()));
    }

    public synchronized Map<String, Object> init(GitPlan plan) throws Exception {
        if (!plan.enabled())
            return Map.of("success", true, "initialized", false, "repositories", List.of());
        for (String variable : List.of("GIT_DIR", "GIT_WORK_TREE", "GIT_COMMON_DIR", "GIT_INDEX_FILE", "GIT_OBJECT_DIRECTORY", "GIT_ALTERNATE_OBJECT_DIRECTORIES")) {
            if (System.getenv(variable) != null)
                throw conflict("Une variable d'environnement redirige les opérations Git");
        }
        Path published = publicationService.getLastPublishedTarget();
        if (published == null)
            throw conflict("Aucun projet publié pendant cette session");
        try {
            Path root = checkedDirectory(published, published.toAbsolutePath().normalize());
            if (!root.equals(plan.root()))
                throw conflict("La configuration Git ne correspond pas au projet publié");
            // Prévalidation de TOUS les dépôts avant toute écriture, y compris les .git imbriqués.
            for (Repository repository : plan.repositories()) {
                Path path = checkedDirectory(repository.path(), root);
                for (Path parent = path; parent != null; parent = parent.getParent()) {
                    if (Files.exists(parent.resolve(".git"), LinkOption.NOFOLLOW_LINKS)) {
                        throw conflict("Un dépôt Git existe déjà dans le projet ou un parent");
                    }
                }
            }
            try (var paths = Files.walk(root)) {
                if (paths.anyMatch(path -> Files.isSymbolicLink(path) || path.getFileName().toString().equals(".git"))) {
                    throw conflict("Un dépôt imbriqué ou un lien symbolique existe dans le projet");
                }
            }
            for (IgnoreFile ignore : plan.ignores()) {
                Path directory = checkedDirectory(ignore.directory(), root);
                Path file = directory.resolve(".gitignore");
                if (Files.exists(file, LinkOption.NOFOLLOW_LINKS) && !Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS)) {
                    throw conflict("Le .gitignore existant n'est pas un fichier ordinaire");
                }
            }
            for (IgnoreFile ignore : plan.ignores()) {
                // Core écrit sans fusion : conserver intégralement tout fichier déjà publié.
                if (!Files.exists(ignore.directory().resolve(".gitignore"), LinkOption.NOFOLLOW_LINKS)) {
                    FilesEdit file = new FilesEdit();
                    file.setContent(ignore.content());
                    GitUtils.generateGitIgnoreIfNeeded(file, ignore.directory().toString());
                }
            }
            ProjectGenerator generator = new ProjectGenerator();
            for (Repository repository : plan.repositories()) {
                generator.initGit(repository.path().toString(), repository.remote(), repository.name(), repository.username(), repository.token());
            }
            return Map.of("success", true, "initialized", true, "branch", "main", "repositories", plan.repositories().stream().map(repository -> root.relativize(repository.path()).toString()).toList());
        } catch (ResponseStatusException exception) {
            throw exception;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Opération Git interrompue ; vérifier les dépôts créés");
        } catch (Exception exception) {
            // Les erreurs de Core peuvent contenir un message GitHub : ne jamais les restituer.
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Échec Git ; les fichiers et dépôts éventuellement créés sont conservés pour vérification");
        }
    }

    private Path checkedDirectory(Path path, Path root) throws IOException {
        Path normalized = path.toAbsolutePath().normalize();
        if (!normalized.startsWith(root))
            throw conflict("Le chemin Git sort du projet publié");
        for (Path current = normalized; current != null; current = current.getParent()) {
            if (Files.isSymbolicLink(current))
                throw conflict("Lien symbolique interdit dans le chemin Git");
        }
        if (!Files.isDirectory(normalized, LinkOption.NOFOLLOW_LINKS))
            throw conflict("Dossier Git publié introuvable");
        if (!normalized.toRealPath().equals(normalized))
            throw conflict("Le chemin réel Git diffère du chemin publié");
        return normalized;
    }

    private static ResponseStatusException invalid(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private static ResponseStatusException conflict(String message) {
        return new ResponseStatusException(HttpStatus.CONFLICT, message);
    }

    public record GitPlan(boolean enabled, Path root, List<Repository> repositories, List<IgnoreFile> ignores) {
    }

    public record IgnoreFile(Path directory, String content) {
    }

    public record Repository(Path path, String name, boolean remote, String username, String token) {

        @Override
        public String toString() {
            return "Repository[path=" + path + ", remote=" + remote + "]";
        }
    }

    public Map<String, Object> configure(GitConfiguration configuration) {
        validateConfiguration(configuration);
        contextStore.getContext().setGitConfiguration(configuration);
        return Map.of("success", true, "useGit", configuration.isUseGit(), "separateRepositories", configuration.isSeparateRepositories(), "createRemoteRepository", configuration.isCreateRemoteRepository());
    }

    public void reset() {
        contextStore.getContext().setGitConfiguration(null);
    }

}
