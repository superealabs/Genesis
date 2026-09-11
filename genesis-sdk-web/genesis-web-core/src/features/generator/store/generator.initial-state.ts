// genesis-web-core/src/features/generator/store/generator.initial-state.ts
import type { GeneratorData } from '../types/generator.types';

export const INITIAL_STATE: GeneratorData = {
    framework: null,
    config: {
        projectName: '',
        projectLocation: '/home/user/projects/',
        languageVersion: '',
        buildTool: 'maven',
        groupId: 'com.example',
        frameworkVersion: '',
        projectDescription: '',
        projectPort: '',
        loggingLevel: '',
        securityType: '',
        cacheProvider: ''
    },
    database: {
        engine: 'postgre', host: 'localhost', port: 5432,
        databaseName: '', schema: 'public', username: '', password: '',
        driverType: 'org.postgresql.Driver',
        driverName: 'PostgreSQL JDBC Driver',
        sid: '', trustCertificate: false, allowPublicKeyRetrieval: false,
    },
    script: { path: '', content: '' },
    tableSelection: { selectedTables: [], selectedViews: [], selectedComponents: [] },
    frontend: null,
    frontendLayout: {
        selectedLanguages: [], navbarType: '', primaryColor: '#3B82F6',
        secondaryColor: '#64748B', logoPath: '', faviconPath: '', port: ''
    },
    git: {
        useGit: false, separateRepositories: false, useRemoteRepo: false,
        isNewRemoteRepo: true, repositoryName: '', backendRepositoryName: '',
        frontendRepositoryName: '', githubUsername: '', githubToken: ''
    }
};