// genesis-web-core/src/node.ts
// Point d'entrée EXCLUSIF pour les environnements Node.js (Extension Host, tests, scripts)
// NE JAMAIS ajouter d'imports Vue, Pinia, ou alias @/ ici.

// Generator
export type {
    TableMetadataDto,
    RelationParameter,
    LanguageDto,
    GeneratorData,
    ProjectConfig,
    DatabaseConfig,
    ScriptConfig,
    ComponentType,
    TableSelectionConfig,
    FrontendLayoutConfig,
    GitConfiguration,
    FileRequestField,
    FileRequestPayload,
} from './features/generator/types/generator.shared';

// Frameworks
export type { Framework } from './features/frameworks/types/framework.shared';

// Frontend
export type { FrontendFramework } from './features/frontend/types/frontend.shared';