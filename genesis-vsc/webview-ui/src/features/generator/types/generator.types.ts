import type { Framework } from '@/features/frameworks/types/framework.types';

export interface ProjectConfig {
    projectName: string;
    projectLocation: string;
    languageVersion: string;
    buildTool: 'maven' | 'gradle' | 'npm' | 'yarn' | 'pip';
    groupId: string;
    frameworkVersion: string;
}

export interface GeneratorData {
    framework: Framework | null;
    config: ProjectConfig;
}

// Valeurs statiques pour le mock (à remplacer par des appels API plus tard)
export const MOCK_BUILD_TOOLS = [
    { label: 'Maven', value: 'maven' },
    { label: 'Gradle', value: 'gradle' },
    { label: 'npm', value: 'npm' },
    { label: 'yarn', value: 'yarn' }
];

export const MOCK_JAVA_VERSIONS = ['8', '11', '17', '21'];
export const MOCK_NODE_VERSIONS = ['18', '20', '22'];
export const MOCK_PYTHON_VERSIONS = ['3.9', '3.10', '3.11', '3.12'];


// webview-ui/src/features/generator/types/generator.types.ts

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

export interface GeneratorData {
    framework: Framework | null;
    config: ProjectConfig;
    database: DatabaseConfig; // <-- AJOUT
}