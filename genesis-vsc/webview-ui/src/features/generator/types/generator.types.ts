import type { Framework } from '@/features/frameworks/types/framework.types';

export interface ProjectConfig {
    projectName: string;
    projectLocation: string;
    languageVersion: string;
    buildTool: 'maven' | 'gradle' | 'npm' | 'yarn' | 'pip';
    groupId: string;
    frameworkVersion: string;
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

// ═══ Interface principale ═══
export interface GeneratorData {
    framework: Framework | null;
    config: ProjectConfig;
    database: DatabaseConfig;
    script: ScriptConfig;
    tableSelection: TableSelectionConfig;
}

export const MOCK_BUILD_TOOLS = [
    { label: 'Maven', value: 'maven' },
    { label: 'Gradle', value: 'gradle' },
    { label: 'npm', value: 'npm' },
    { label: 'yarn', value: 'yarn' }
];

export const MOCK_JAVA_VERSIONS = ['8', '11', '17', '21'];
export const MOCK_NODE_VERSIONS = ['18', '20', '22'];
export const MOCK_PYTHON_VERSIONS = ['3.9', '3.10', '3.11', '3.12'];

export const AVAILABLE_COMPONENTS: { label: string; value: ComponentType }[] = [
    { label: 'Model', value: 'model' },
    { label: 'DAO', value: 'dao' },
    { label: 'Service', value: 'service' },
    { label: 'Controller', value: 'controller' },
];
