// ═══ IMPORTS PARTAGÉS (Node.js & Browser safe) ═══
// RÈGLE STRICTE : Chemins relatifs uniquement vers les fichiers .shared.ts
import type { Framework } from './framework.shared';
import type { FrontendFramework } from './frontend.shared';

// ═══ Interfaces et Types (Purs, sans dépendance UI) ═══

export interface ProjectConfig {
    projectName: string;
    projectLocation: string;
    languageVersion: string;
    buildTool: 'maven' | 'gradle' | 'npm' | 'yarn' | 'pip';
    groupId: string;
    frameworkVersion: string;
    projectDescription: string;
    projectPort: string;
    loggingLevel: string;
    securityType: string;
    cacheProvider: string;
}

export interface DatabaseConfig {
    engine: 'mysql' | 'postgre' | 'sqlserver' | 'oracle';
    host: string;
    port: number;
    databaseName: string;
    schema: string;
    username: string;
    password: string;
    driverType: string;
    driverName: string;
    sid: string;
    trustCertificate: boolean;
    allowPublicKeyRetrieval: boolean;
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

export interface LanguageDto {
    code: string;
    name: string;
}

export interface FrontendLayoutConfig {
    selectedLanguages: string[];
    navbarType: 'side' | 'top' | '';
    primaryColor: string;
    secondaryColor: string;
    logoPath: string;
    port: string;
    faviconPath: string;
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
    database: DatabaseConfig;
    script: ScriptConfig;
    tableSelection: TableSelectionConfig;
    frontend: FrontendFramework | null; // ✅ Utilise maintenant le type importé !
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