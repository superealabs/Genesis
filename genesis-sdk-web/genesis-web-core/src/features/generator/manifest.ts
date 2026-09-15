// ═══ Types & Contrats (Shared) ═══
export type {
    ProjectConfig,
    ScriptConfig,
    TableMetadataDto,
    ComponentType,
    TableSelectionConfig,
    RelationParameter,
    LanguageDto,
    FrontendLayoutConfig,
    GitConfiguration,
    GeneratorData,
    FileRequestField,
    FileRequestPayload,
    Framework,
    FrontendFramework,
} from '@genesis-labs/shared-types';

// ═══ Contrats & Clés d'injection spécifiques ═══
export type { IGeneratorService } from './types/generator.service.interface';
export { GENERATOR_SERVICE_KEY } from './types/generator.service.interface';

// ═══ STORE, COMPOSABLE & VUE ═══
export { useGeneratorStore } from './store/useGenerator.store';
export { useGenerator } from './composables/useGenerator';
export { default as GeneratorStepper } from './components/GeneratorStepper.vue';