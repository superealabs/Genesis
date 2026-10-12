// genesis-sdk-web/genesis-web-core/src/features/generator/store/generator.initial-state.ts
import type { GeneratorData } from '@genesis-labs/shared-types';

export const INITIAL_STATE: GeneratorData = {
    framework: null,
    config: {
        projectName: '',
        projectLocation: '',
        languageVersion: '',
        buildTool: '',
        groupId: '',
        frameworkVersion: '',
        projectDescription: '',
        projectPort: '',
        loggingLevel: 'INFO',
        securityType: 'NONE',
        cacheProvider: 'NONE',
        hibernateDdlAuto: 'none'
    },
    database: {
        engine: 'postgre',
        host: 'localhost',
        port: 5432,
        databaseName: '',
        schema: 'public',
        username: '',
        password: '',
        driverType: 'jdbc',
        driverName: '',
        sid: '',
        trustCertificate: false,
        allowPublicKeyRetrieval: false
    },
    script: {
        path: '',
        content: ''
    },
    
    // MODIFIÉ : Remplace l'ancien tableSelection par le tableau final pour le backend
    generationParameters: [], 

    frontend: null,
    frontendLayout: {
        selectedInterfaceLanguages: [],
        navbarType: '',
        primaryColor: '#3B82F6',
        secondaryColor: '#64748B',
        logoPath: '',
        faviconPath: '',
        port: '3000'
    },
    git: {
        useGit: true,
        separateRepositories: false,
        useRemoteRepo: false,
        isNewRemoteRepo: true,
        repositoryName: '',
        backendRepositoryName: '',
        frontendRepositoryName: '',
        githubUsername: '',
        githubToken: ''
    }
};