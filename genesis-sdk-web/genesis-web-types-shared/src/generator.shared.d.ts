import type { Framework } from './framework.shared';
import type { FrontendFramework } from './frontend.shared';
import type { DatabaseConfig } from './database.shared';
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
export interface GeneratorData {
    framework: Framework | null;
    config: ProjectConfig;
    database: DatabaseConfig;
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
export declare const MOCK_BUILD_TOOLS: readonly [{
    readonly label: "Maven";
    readonly value: "maven";
}, {
    readonly label: "Gradle";
    readonly value: "gradle";
}, {
    readonly label: "npm";
    readonly value: "npm";
}, {
    readonly label: "yarn";
    readonly value: "yarn";
}];
export declare const MOCK_JAVA_VERSIONS: readonly ["8", "11", "17", "21"];
export declare const MOCK_NODE_VERSIONS: readonly ["18", "20", "22"];
export declare const MOCK_PYTHON_VERSIONS: readonly ["3.9", "3.10", "3.11", "3.12"];
export declare const AVAILABLE_COMPONENTS: {
    label: string;
    value: ComponentType;
}[];
