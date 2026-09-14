// ═══ Types & Contrats (Shared - 100% Node.js & Browser safe) ═══
// ✅ CORRECTION RÈGLE 1 : Tout ce qui vient du package partagé est importé via son nom, en un seul bloc.
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

// ═══ Contrats & Clés d'injection spécifiques à la feature ═══
export type { IGeneratorService } from '@/features/generator/types/generator.service.interface';
export { GENERATOR_SERVICE_KEY } from '@/features/generator/types/generator.service.interface';

// ═══ STORE & COMPOSABLES ═══
export { useGeneratorStore } from '@/features/generator/store/useGenerator.store';
export { useGenerator } from '@/features/generator/composables/useGenerator';

// ═══ VUE ═══
export { default as GeneratorStepper } from '@/features/generator/components/GeneratorStepper.vue';