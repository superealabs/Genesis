// ═══ Types & Contrats (Shared - 100% Node.js & Browser safe) ═══
export type {
    ProjectConfig,
    DatabaseConfig,
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
} from '../../../../genesis-web-types-shared/src/generator.shared.ts';

// On réexporte aussi les types des autres features pour faciliter la vie 
// de l'Extension Host qui n'aura qu'un seul import à faire :
export type { Framework } from '../../../../genesis-web-types-shared/src/framework.shared.ts';
export type { FrontendFramework } from '../../../../genesis-web-types-shared/src/frontend.shared.ts';

export type { IGeneratorService } from './types/generator.service.interface';
export { GENERATOR_SERVICE_KEY } from './types/generator.service.interface';

// ═══ Store & Composables ═══
export { useGeneratorStore } from './store/useGenerator.store';
export { useGenerator } from './composables/useGenerator';

// ═══ Vue ═══
export { default as GeneratorStepper } from './components/GeneratorStepper.vue';