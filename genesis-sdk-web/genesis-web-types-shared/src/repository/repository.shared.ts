// genesis-sdk-web/genesis-web-types-shared/src/repository/repository.shared.ts

/**
 * Identifiants des fournisseurs de dépôt supportés.
 * Facilement extensible sans casser les types existants.
 */
export type RepositoryProviderType = 
  | 'github' 
  | 'gitlab' 
  | 'bitbucket' 
  | 'azure-devops' 
  | 'gitea' 
  | 'custom';

/**
 * Méthodes d'authentification génériques supportées par les fournisseurs Git.
 */
export type RepositoryAuthMethod = 
  | 'personal_access_token' // Couvre GitHub PAT, GitLab PAT, Bitbucket App Password
  | 'ssh_key'
  | 'username_password';

/**
 * Définit si le projet utilise un seul dépôt ou des dépôts séparés.
 */
export type RepositoryScope = 'monorepo' | 'separated';

/**
 * Métadonnées décrivant les capacités et les exigences d'un fournisseur.
 * Permet à l'UI de s'adapter dynamiquement (ex: afficher le champ "Workspace" pour Bitbucket).
 */
export interface RepositoryProviderMetadata {
  id: RepositoryProviderType;
  name: string; // Ex: "GitHub", "GitLab"
  defaultBaseUrl: string; // Ex: "https://github.com"
  supportedAuthMethods: RepositoryAuthMethod[];
  requiresNamespace: boolean; // Ex: true pour Bitbucket (Workspace), false pour GitHub (optionnel)
  namespaceLabel: string; // Ex: "Organization", "Workspace", "Group", "Project"
}

/**
 * Configuration riche et générique d'un dépôt distant.
 * C'est l'état "vérité" du frontend avant la transformation vers le DTO backend.
 */
export interface RepositoryConfig {
  provider: RepositoryProviderType;
  useRemote: boolean;
  isNewRemote: boolean;
  scope: RepositoryScope;

  // --- Espace de nommage générique ---
  // Couvre : GitHub Organization, GitLab Group/Namespace, Bitbucket Workspace, Azure Project
  namespace?: string; 

  // --- Noms des dépôts ---
  monorepoName?: string;
  backendRepoName?: string;
  frontendRepoName?: string;

  // --- Authentification ---
  authMethod: RepositoryAuthMethod;
  username?: string; // Nom d'utilisateur ou Email selon le provider
  token?: string;    // PAT, App Password, etc.
  sshKeyPath?: string; // Chemin vers la clé privée (si authMethod === 'ssh_key')

  // --- Support Self-Hosted ---
  baseUrl?: string; // Ex: "https://gitlab.mon-entreprise.com" (requis pour 'custom' ou 'gitea')
}