package org.labs.genesis.config.tools;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GitConfiguration {

    private boolean useGit;

    private boolean separateRepositories;

    private boolean isRepositoryPrivate;

    private String repositoryName;

    private String backendRepositoryName;

    private String frontendRepositoryName;

    private boolean createRemoteRepository;

    private String githubUsername;

    private String githubToken;

}