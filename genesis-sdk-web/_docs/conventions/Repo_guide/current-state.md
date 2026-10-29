// Frontend (Riche) -> Backend (Strict)
const backendGitConfig: GitConfiguration = {
  useGit: frontendState.useGit,
  separateRepositories: frontendState.isSeparated,
  useRemoteRepo: frontendState.useRemote,
  isNewRemoteRepo: frontendState.isNew,
  repositoryName: frontendState.repoName, // Si mono-repo
  backendRepositoryName: frontendState.backendRepoName,
  frontendRepositoryName: frontendState.frontendRepoName,
  // Mapping spécifique au provider sélectionné
  githubUsername: frontendState.provider === 'github' ? frontendState.credentials.username : '',
  githubToken: frontendState.provider === 'github' ? frontendState.credentials.token : ''
};