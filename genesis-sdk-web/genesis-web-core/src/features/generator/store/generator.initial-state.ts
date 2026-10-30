// genesis-sdk-web/genesis-web-core/src/features/generator/store/generator.initial-state.ts
import type { GeneratorData } from '@genesis-labs/shared-types';

export const INITIAL_STATE: GeneratorData = {
    framework: null,
    config: {
        projectName: 'test',
        projectLocation: 'T:/_STAGE-OFFICIAL/Dev_part/workspace',
        languageVersion: '',
        buildTool: '',
        groupId: '',
        frameworkVersion: '',
        projectDescription: "Ce projet vise à développer une plateforme SaaS enterprise de gestion de ressources et de planification de projets, conçue pour optimiser la collaboration inter-équipes au sein des grandes organisations internationales. L'application offrira un tableau de bord centralisé et intuitif, permettant aux chefs de projet de suivre en temps réel l'avancement des tâches, d'allouer les ressources humaines et matérielles de manière optimale, et de générer des rapports analytiques détaillés et exportables. Les fonctionnalités principales incluent la gestion agile des workflows complexes, l'intégration native et sécurisée avec des outils de communication tiers (tels que Slack, Microsoft Teams et Jira), ainsi qu'un module avancé de contrôle d'accès basé sur les rôles (RBAC) pour garantir la confidentialité absolue des données sensibles. L'architecture technique repose sur une approche microservices moderne, assurant une haute disponibilité, une scalabilité horizontale sans friction et une maintenance simplifiée. L'interface utilisateur sera développée avec un framework frontend réactif pour garantir une expérience fluide, responsive et accessible sur tous les appareils. Ce système permettra aux organisations de réduire leurs délais de livraison de 30 %, d'améliorer significativement la transparence opérationnelle globale, et de s'adapter facilement aux futures évolutions métier ainsi qu'aux normes de conformité réglementaire en vigueur.",
        projectPort: '',
        loggingLevel: '',
        securityType: '',
        cacheProvider: '',
        hibernateDdlAuto: ''
    },
    database: {
        engine: '',
        host: 'localhost',
        port: 5432,
        databaseName: 'my_db',
        schema: 'public',
        username: 'root',
        password: '',
        driverType: '',
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
    },
    repositoryConfig : null
};