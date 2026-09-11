import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type { Framework } from '@/features/frameworks/types/framework.types';
import type { FrontendFramework } from '@/features/frontend/types/frontend.types';
import type { GeneratorData, ProjectConfig, DatabaseConfig, ScriptConfig, ComponentType, TableMetadataDto, RelationParameter, LanguageDto, FrontendLayoutConfig, GitConfiguration } from '../types/generator.types';

export const useGeneratorStore = defineStore('generator', () => {
    // ═══ État ═══
    const currentStep = ref(1);
    const totalSteps = 9;
    const availableTables = ref<TableMetadataDto[]>([]);
    const availableViews = ref<TableMetadataDto[]>([]);
    const tablesParents = ref<TableMetadataDto[]>([]);
    const tablesChilds = ref<TableMetadataDto[]>([]);
    const relations = ref<RelationParameter[]>([]);
    const availableFrontendFrameworks = ref<FrontendFramework[]>([]);
    const availableLanguages = ref<LanguageDto[]>([]);

    const stepperData = ref<GeneratorData>({
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
        database: { // <-- AJOUT : État initial
            engine: 'postgre',
            host: 'localhost',
            port: 5432,
            databaseName: '',
            schema: 'public',
            username: '',
            password: '',
            driverType: 'org.postgresql.Driver',
            driverName: 'PostgreSQL JDBC Driver',
            sid: '',
            trustCertificate: false,
            allowPublicKeyRetrieval: false,
        },
        script: {
            path: '',
            content: '',
        },
        tableSelection: {
            selectedTables: [],
            selectedViews: [],
            selectedComponents: [],
        },
        frontend: null,
        frontendLayout: {
            selectedLanguages: [],
            navbarType: '',
            primaryColor: '#3B82F6', // Valeur par défaut exemple
            secondaryColor: '#64748B',
            logoPath: '',
            faviconPath: '',
            port: ''
        },
        git: {
            useGit: false,
            separateRepositories: false,
            useRemoteRepo: false,
            isNewRemoteRepo: true, // Par défaut on crée un nouveau repo
            repositoryName: '',
            backendRepositoryName: '',
            frontendRepositoryName: '',
            githubUsername: '',
            githubToken: ''
        }
    }
);

    // ═══ Getters ═══
    const isFirstStep = computed(() => currentStep.value === 1);
    const isLastStep = computed(() => currentStep.value === totalSteps);

    // ═══ Getter Relations ═══
    const getRelations = computed(() => relations.value);
    const getTablesParents = computed(() => tablesParents.value);
    const getTablesChilds = computed(() => tablesChilds.value);

    // ═══ Actions ═══
    function setAvailableTables(tables: TableMetadataDto[]) {
        availableTables.value = tables;
    }

    function setAvailableViews(views: TableMetadataDto[]) {
        availableViews.value = views;
    }

    function setTablesParents(tables: TableMetadataDto[]) {
        tablesParents.value = tables;
    }

    function setTablesChilds(tables: TableMetadataDto[]) {
        tablesChilds.value = tables;
    }

    function setRelations(data: RelationParameter[]) {
        relations.value = data;
    }

    function setAvailableFrontendFrameworks(frameworks: FrontendFramework[]) {
        availableFrontendFrameworks.value = frameworks;
    }

    function setSelectedFrontendFramework(framework: FrontendFramework | null) {
        stepperData.value.frontend = framework;
        if (framework !== null) {
            stepperData.value.frontendLayout.port = framework.defaultPort;
        }
    }

    function setAvailableLanguages(languages: LanguageDto[]) {
        availableLanguages.value = languages;
    }

    function updateFrontendLayout<K extends keyof FrontendLayoutConfig>(key: K, value: FrontendLayoutConfig[K]) {
        (stepperData.value.frontendLayout as any)[key] = value;
    }

    function toggleLanguage(code: string) {
        const list = stepperData.value.frontendLayout.selectedLanguages;
        const idx = list.indexOf(code);
        idx === -1 ? list.push(code) : list.splice(idx, 1);
    }

    function setFramework(framework: Framework) {
        stepperData.value.framework = framework;
        // Pré-remplissage intelligent basé sur le framework (mock)
        if (framework.coreFramework === 'Spring') {
            stepperData.value.config.buildTool = 'maven';
            stepperData.value.config.languageVersion = '17';
        } else if (framework.coreFramework === 'Express') {
            stepperData.value.config.buildTool = 'npm';
            stepperData.value.config.languageVersion = '20';
        }
    }

    function updateConfig<K extends keyof ProjectConfig>(key: K, value: ProjectConfig[K]) {
        (stepperData.value.config as any)[key] = value;
    }

    function updateDatabase<K extends keyof DatabaseConfig>(key: K, value: DatabaseConfig[K]) {
        (stepperData.value.database as any)[key] = value;
    }

    function updateScript<K extends keyof ScriptConfig>(key: K, value: ScriptConfig[K]) {
        (stepperData.value.script as any)[key] = value;
    }

    function toggleTable(tableName: string) {
        const list = stepperData.value.tableSelection.selectedTables;
        const idx = list.indexOf(tableName);
        idx === -1 ? list.push(tableName) : list.splice(idx, 1);
    }

    function toggleView(viewName: string) {
        const list = stepperData.value.tableSelection.selectedViews;
        const idx = list.indexOf(viewName);
        idx === -1 ? list.push(viewName) : list.splice(idx, 1);
    }

    function toggleComponent(component: ComponentType) {
        const list = stepperData.value.tableSelection.selectedComponents;
        const idx = list.indexOf(component);
        idx === -1 ? list.push(component) : list.splice(idx, 1);
    }

    function goToNextStep() {
        if (currentStep.value < totalSteps) {
            currentStep.value++;
        }
    }

    function goToPreviousStep() {
        if (currentStep.value > 1) {
            currentStep.value--;
        }
    }

    function reset() {
        currentStep.value = 1;
        stepperData.value = {
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
                engine: 'postgre',
                host: 'localhost',
                port: 5432,
                databaseName: '',
                schema: 'public',
                username: '',
                password: '',
                driverType: 'org.postgresql.Driver',
                driverName: 'PostgreSQL JDBC Driver',
                sid: '',
                trustCertificate: false,
                allowPublicKeyRetrieval: false,                
            },
            script: {
                path: '',
                content: '',
            },
            tableSelection: {
                selectedTables: [],
                selectedViews: [],
                selectedComponents: [],
            },
            frontend: null,
            frontendLayout: {
                selectedLanguages: [],
                navbarType: '',
                primaryColor: '',
                secondaryColor: '',
                logoPath: '',
                faviconPath: '',
                port: ''
            },
            git: {
                useGit: false,
                separateRepositories: false,
                useRemoteRepo: false,
                isNewRemoteRepo: true, // Par défaut on crée un nouveau repo
                repositoryName: '',
                backendRepositoryName: '',
                frontendRepositoryName: '',
                githubUsername: '',
                githubToken: ''
            }
        },
        availableFrontendFrameworks.value = [];
        availableLanguages.value = [];
    }

    function addRelation(relation: RelationParameter): boolean {
        // Logique métier : vérification des doublons
        const exists = relations.value.some(r => 
            r.parentTable === relation.parentTable && 
            r.childTable === relation.childTable
        );
        
        if (exists) {
            console.warn('Cette relation existe déjà.');
            return false; // Échec
        }
        
        relations.value.push(relation);
        return true; // Succès
    }

    function removeRelation(index: number) {
        relations.value.splice(index, 1);
    }

    function updateGitConfig<K extends keyof GitConfiguration>(key: K, value: GitConfiguration[K]) {
        (stepperData.value.git as any)[key] = value;
        
        // Nettoyage automatique
        if (key === 'useGit' && value === false) {
            // Reset tout si Git désactivé
            stepperData.value.git = {
                useGit: false,
                separateRepositories: false,
                useRemoteRepo: false,
                isNewRemoteRepo: true,
                repositoryName: '',
                backendRepositoryName: '',
                frontendRepositoryName: '',
                githubUsername: '',
                githubToken: ''
            };
        } else if (key === 'useRemoteRepo' && value === false) {
            // Reset les champs remote si on désactive le remote
            stepperData.value.git.isNewRemoteRepo = true;
            stepperData.value.git.githubUsername = '';
            stepperData.value.git.githubToken = '';
        }
    }

    return {
        currentStep,
        totalSteps,
        stepperData,
        isFirstStep,
        isLastStep,
        availableTables,
        availableViews,
        setAvailableTables,
        setAvailableViews,
        setTablesChilds,
        setTablesParents,
        availableFrontendFrameworks,
        setAvailableFrontendFrameworks,
        setSelectedFrontendFramework, 
        getTablesParents,
        getTablesChilds,
        setFramework,
        setRelations,
        getRelations,
        addRelation,
        removeRelation,
        updateConfig,
        updateDatabase,
        updateScript,
        toggleTable,
        toggleComponent,
        toggleView,
        goToNextStep,
        goToPreviousStep,
        reset,
        availableLanguages,
        setAvailableLanguages,
        updateFrontendLayout,
        toggleLanguage,
        updateGitConfig
    };
});