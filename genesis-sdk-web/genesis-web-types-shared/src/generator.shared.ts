// genesis-sdk-web/genesis-web-types-shared/src/generator.shared.ts

// ═══ IMPORTS PARTAGÉS (Node.js & Browser safe) ═══
// RÈGLE STRICTE : Chemins relatifs uniquement vers les fichiers .shared.ts
import type { Framework } from './framework.shared';
import type { FrontendFramework } from './frontend.shared';
import type { DatabaseConfig } from './database.shared';

// ═══ Interfaces et Types (Purs, sans dépendance UI) ═══

export interface ProjectConfig {
    projectName: string;
    projectLocation: string;
    languageVersion: string;
    buildTool: string; // Ex: 'maven', 'npm'
    groupId: string;
    frameworkVersion: string;
    projectDescription: string;
    projectPort: string;
    
    // CORRECTION : Ce sont de simples strings, pas des objets
    loggingLevel: string;    // Ex: 'INFO', 'DEBUG'
    securityType: string;    // Ex: 'JWT', 'OAuth2', 'NONE'
    cacheProvider: string;   // Ex: 'Redis', 'NONE'
    hibernateDdlAuto?: string;
}

export interface ScriptConfig {
    path: string;
    content: string;
}

export interface TableMetadataDto {
    tableName: string;
    className: string;
    isView: boolean;
}

export type ComponentType = 'model' | 'dao' | 'service' | 'controller';

export interface TableSelectionConfig {
    selectedTables: string[];
    selectedViews: string[];
    selectedComponents: ComponentType[];
}

export interface RelationParameter {
    parentTable: string;
    childTable: string;
    mandatory: boolean;
    hasForm: boolean;
}

export interface FrontendLayoutConfig {
    selectedInterfaceLanguages: string[]; // ex: ['fr', 'en'] (codes des langues)
    navbarType: 'side' | 'top' | '';      // Correspond à "Sidebar" / "Topbar" en Java
    primaryColor: string;                 // ex: '#537cc2'
    secondaryColor: string;               // ex: '#537cc240'
    logoPath: string;                     // Chemin du fichier ou URL
    faviconPath: string;                  // Chemin du fichier ou URL
    port: string;                         // Port d'exécution du frontend (ex: '3000')
}

export interface GitConfiguration {
    useGit: boolean;
    separateRepositories: boolean;
    useRemoteRepo: boolean;
    isNewRemoteRepo: boolean;
    repositoryName: string;
    backendRepositoryName: string;
    frontendRepositoryName: string;
    githubUsername: string;
    githubToken: string;
}

// ═══ Interface principale ═══
export interface GeneratorData {
    framework: Framework | null;
    config: ProjectConfig;
    database: DatabaseConfig; // Utilise le type importé en interne
    script: ScriptConfig;
    tableSelection: TableSelectionConfig;
    frontend: FrontendFramework | null;
    frontendLayout: FrontendLayoutConfig;
    git: GitConfiguration;
}

export type FileRequestField = 'script' | 'logoPath' | 'faviconPath';

export interface FileRequestPayload {
    field: FileRequestField;
    extensions?: string[];
}

// ═══ Constantes (Pures données, safe pour Node.js) ═══
export const MOCK_BUILD_TOOLS = [
    { label: 'Maven', value: 'maven' },
    { label: 'Gradle', value: 'gradle' },
    { label: 'npm', value: 'npm' },
    { label: 'yarn', value: 'yarn' }
] as const;

export const MOCK_JAVA_VERSIONS = ['8', '11', '17', '21'] as const;
export const MOCK_NODE_VERSIONS = ['18', '20', '22'] as const;
export const MOCK_PYTHON_VERSIONS = ['3.9', '3.10', '3.11', '3.12'] as const;

export const AVAILABLE_COMPONENTS: { label: string; value: ComponentType }[] = [
    { label: 'Model', value: 'model' },
    { label: 'DAO', value: 'dao' },
    { label: 'Service', value: 'service' },
    { label: 'Controller', value: 'controller' },
];


export interface LlmModelDto {
    id: string;       // ex: 'gpt-4o', 'claude-sonnet-3-5'
    name: string;     // ex: 'GPT-4o', 'Claude 3.5 Sonnet'
    provider?: string; // ex: 'OpenAI', 'Anthropic' (optionnel, pour affichage)
}

export interface AiPromptPayload {
    model: string;          // Le modèle LLM sélectionné par l'utilisateur
    prompt: string;         // La demande textuelle de l'utilisateur
    fileContent?: string;   // Le contenu actuel de l'éditeur (pour modification/contexte)
    showSchema: boolean;    // Correspond au checkbox "includeDbSchema" (injecter le schéma DB)
    token?: string;         // Token d'API personnel (optionnel, si fourni par l'utilisateur)
}

export interface AiResponseDto {
    success: boolean;
    aiResponse: string;      // La réponse textuelle de l'IA (explications, chat)
    newFileContent?: string; // Le code SQL généré ou modifié (à injecter dans GenesisIdeCm)
    error?: string;          // Message d'erreur détaillé si success === false
}

export interface GenerationResult {
    success: boolean;
    message: string;
    projectPath?: string; // Le chemin où le projet a été généré
}