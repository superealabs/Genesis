package org.labs.genesis.api.dto;

public final class GitDtos {
    private GitDtos() {}

    public record GitConfigRequest(
            boolean useGit,
            boolean separateRepositories,
            boolean useRemoteRepo,
            boolean isNewRemoteRepo,
            String repositoryName,
            String backendRepositoryName,
            String frontendRepositoryName,
            String githubUsername,
            String githubToken
    ) {}
}
