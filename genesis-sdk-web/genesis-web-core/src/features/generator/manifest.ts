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
export type { IGeneratorService } from '@genesis-labs/web-core/features/generator/types/generator.service.interface';
export { GENERATOR_SERVICE_KEY } from '@genesis-labs/web-core/features/generator/types/generator.service.interface';

// ═══ STORE, COMPOSABLE & VUE ═══
export { useGeneratorStore } from '@genesis-labs/web-core/features/generator/store/useGenerator.store';
export { useGenerator } from '@genesis-labs/web-core/features/generator/composables/useGenerator';
export { default as GeneratorStepper } from '@genesis-labs/web-core/features/generator/components/GeneratorStepper.vue';