# modules genesis-web-types-shared
**Règle 1 :** genesis-web-types-shared n'utilise JAMAIS d'alias.
Tous les imports entre ses fichiers sont des chemins relatifs directs.

Raison : ce fichier doit être reutilisable même par des écosystèmes non Vite

```typescript
// Correct
import type { Framework } from './framework.shared';

// Interdit
import type { Framework } from '@genesis-labs/shared-types/framework';
import type { Framework } from '@/framework.shared';
```

**Règle 2 :** `index.ts` est le SEUL point d'export public du module. Les fichiers `.shared.ts` n'exportent PAS les types des autres fichiers `.shared.ts` — ils les importent uniquement pour usage interne. Seul `index.ts` centralise et expose tous les types.

Raison : éviter les ré-exports en cascade entre fichiers `.shared.ts` qui créent de la redondance avec `index.ts` et rendent les dépendances internes ambiguës.

```typescript
// Correct — index.ts centralise tout
export * from './framework.shared';
export * from './frontend.shared';
export * from './generator.shared';
export * from './database.shared';

// Correct — generator.shared.ts importe pour usage interne uniquement
import type { Framework } from './framework.shared';
import type { DatabaseConfig } from './database.shared';

// Interdit — generator.shared.ts ne ré-exporte pas ce qu'il importe
export type { DatabaseConfig } from './database.shared';

// Correct — le consommateur importe toujours depuis le package
import type { Framework, DatabaseConfig, GeneratorData } from '@genesis-labs/shared-types';

// Interdit — import par fichier thématique
import type { Framework } from '@genesis-labs/shared-types/framework';
```

# module genesis-web-core

**Règle 1 :** Les imports depuis `genesis-web-types-shared` utilisent TOUJOURS le nom du package, jamais un chemin relatif vers ses sources.

Raison : le package est installé via npm workspace. Un chemin relatif vers les sources brutes est fragile et contourne le système de résolution de modules.

```typescript
// Correct
import type { Framework } from '@genesis-labs/shared-types';

// Interdit
import type { Framework } from '../../../../../genesis-web-types-shared/src/framework.shared';
import type { Framework } from '../genesis-web-types-shared/src/framework.shared';
```

**Règle 2 :** `genesis-web-types-shared` est le SEUL module externe autorisé dans `genesis-web-core`. Aucune autre dépendance externe ne doit fournir des types métier.

Raison : le core doit rester agnostique de la plateforme. Tout type métier partagé entre les modules appartient à `genesis-web-types-shared`.

```typescript
// Correct
import type { GeneratorData } from '@genesis-labs/shared-types';

// Interdit
import type { GeneratorData } from '@genesis-labs/vsc-types';
import type { GeneratorData } from '../../genesis-vsc/src/types/generator.types';
```

**Règle 3 :** Les fichiers de contrat (`.service.interface.ts`) importent les types métier DIRECTEMENT depuis `@genesis-labs/shared-types`, jamais depuis un fichier `.types.ts` intermédiaire.

Raison : un contrat de service ne doit pas dépendre d'un fichier UI. La chaîne de dépendance doit être directe et explicite.

```typescript
// Correct
import type { Framework } from '@genesis-labs/shared-types';

// Interdit
import type { Framework } from './framework.types';
import type { Framework } from '@/features/frameworks/types/framework.types';
```

**Règle 4 :** Les fichiers `.types.ts` UI du core ré-exportent les types de `@genesis-labs/shared-types` et peuvent y ajouter des types spécifiques à l'UI. Ils utilisent l'alias `@/` pour les imports internes au core.

Raison : centraliser les ré-exports permet aux composants Vue de n'avoir qu'un seul point d'import pour les types d'une feature.

```typescript
// Correct — framework.types.ts
export type { Framework, FrameworkType, FrameworkFilters } from '@genesis-labs/shared-types';

export interface FrameworkUiState {
    isLoading: boolean;
    selectedId: number | null;
}

// Correct — import interne au core
import type { FrameworkUiState } from '@/features/frameworks/types/framework.types';

// Interdit
export type { Framework } from '../../../../../genesis-web-types-shared/src/framework.shared';
export type { Framework } from '@genesis-labs/shared-types/framework.shared';
```

**Règle 5 :** L'alias `@/` pointe vers `genesis-web-core/src/` et est EXCLUSIVEMENT réservé aux imports internes au core. Il ne doit jamais être utilisé pour importer depuis un module externe.

Raison : l'alias `@/` est défini dans la configuration Vite/tsconfig du core. L'utiliser pour pointer vers l'extérieur crée une ambiguïté et casse la résolution dans d'autres environnements.

```typescript
// Correct — import interne au core
import GenesisButton from '@/core/components/ui/actions/GenesisButton.vue';
import { useFrameworks } from '@/features/frameworks/composables/useFrameworks';
import type { FrameworkUiState } from '@/features/frameworks/types/framework.types';

// Interdit
import type { Framework } from '@/genesis-web-types-shared/src/framework.shared';
```

**Règle 6 :** Les manifestes (`manifest.ts`) de chaque feature sont le SEUL point d'export public autorisé vers les modules consommateurs. Aucun import profond dans les fichiers internes du core n'est autorisé depuis l'extérieur.

Raison : le manifeste définit la surface d'API stable d'une feature. Les imports profonds créent un couplage fort qui casse au moindre refactoring interne.

```typescript
// Correct — depuis un module consommateur (vsc, web)
import { useFrameworks, FRAMEWORK_SERVICE_KEY } from '@genesis-labs/core/features/frameworks/manifest';

// Interdit
import { useFrameworks } from '@genesis-labs/core/features/frameworks/composables/useFrameworks';
import { FRAMEWORK_SERVICE_KEY } from '@genesis-labs/core/features/frameworks/types/framework.service.interface';
```


**Règle 7 :** Chaque feature expose un `manifest.ts` local. Un `index.ts` global centralise tous les manifestes de features et constitue le point d'entrée racine du core. Les modules consommateurs utilisent le manifeste de feature pour les imports granulaires, et le `index.ts` global uniquement quand plusieurs features sont nécessaires simultanément.

Raison : le manifeste par feature garantit que le tree-shaking fonctionne correctement — seules les features importées sont incluses dans le bundle. Le manifeste global est un raccourci pratique qui ne doit pas devenir la norme dans les modules où la taille du bundle compte (webview VSC, mobile).

```typescript
// Recommande — import granulaire par feature
import { useFrameworks, FRAMEWORK_SERVICE_KEY } from '@genesis-labs/core/features/frameworks/manifest';
import { useGenerator, GENERATOR_SERVICE_KEY } from '@genesis-labs/core/features/generator/manifest';

// Acceptable — import global quand plusieurs features sont utilisées ensemble
import { useFrameworks, useGenerator } from '@genesis-labs/core';

// Interdit — import profond hors manifeste
import { useFrameworks } from '@genesis-labs/core/features/frameworks/composables/useFrameworks';
```


# module genesis-vsc — Extension Host (src/)

**Règle 1 :** L'Extension Host utilise UNIQUEMENT `@genesis-labs/shared-types` comme module externe Genesis. Il n'importe jamais depuis `@genesis-labs/core`.

Raison : `@genesis-labs/core` contient des composants Vue, des composables et des imports Vite qui sont incompatibles avec l'environnement Node.js/CommonJS de l'Extension Host.

```typescript
// Correct
import type { TableMetadataDto, RelationParameter } from '@genesis-labs/shared-types';

// Interdit
import type { TableMetadataDto } from '@genesis-labs/core/features/generator/manifest';
import type { TableMetadataDto } from '@genesis-labs/core';
```

**Règle 2 :** Les imports depuis `@genesis-labs/shared-types` utilisent TOUJOURS le nom du package. Jamais un chemin relatif vers les sources ou le dist du package.

Raison : l'Extension Host résout `@genesis-labs/shared-types` via `node_modules` + `paths` dans `tsconfig.json`. Un chemin relatif contourne cette résolution et crée une dépendance fragile à la position des dossiers.

```typescript
// Correct
import type { Framework, GeneratorData } from '@genesis-labs/shared-types';

// Interdit
import type { Framework } from '../genesis-web-types-shared/src/framework.shared';
import type { Framework } from '../genesis-web-types-shared/dist/index.d.ts';
```

**Règle 3 :** L'Extension Host n'a pas d'alias internes. Tous les imports entre ses propres fichiers utilisent des chemins relatifs directs.

Raison : l'Extension Host compile avec `tsc` + webpack en CommonJS. Les alias Vite (`@/`, `@vsc/`) ne sont pas disponibles dans cet environnement.

```typescript
// Correct
import { GeneratorHandler } from './services/Generator/GeneratorHandler';
import { vscodeService } from '../core/services/vscode.service';

// Interdit
import { GeneratorHandler } from '@vsc/services/Generator/GeneratorHandler';
import { GeneratorHandler } from '@/services/Generator/GeneratorHandler';
```

**Règle 4 :** Les types spécifiques à l'Extension Host (non partagés avec le webview) sont définis localement dans `src/types/`. Ils ne doivent jamais être placés dans `genesis-web-types-shared`.

Raison : `genesis-web-types-shared` ne contient que les types partagés entre tous les modules. Les types internes à l'Extension Host (structures de messages VSCode, types de configuration d'extension) n'ont pas vocation à être partagés.

```typescript
// Correct — type interne à l'Extension Host
import type { WebviewMessage } from '../types/messages';

// Interdit — ajouter des types VSCode-only dans le shared
// Ne pas faire dans genesis-web-types-shared :
export interface VscodeWebviewMessage { ... }
```

**Règle 5 :** Les messages `postMessage` entre l'Extension Host et le Webview sont typés via `@genesis-labs/shared-types`. Le type des payloads de chaque message doit exister dans le module partagé.

Raison : le Webview et l'Extension Host communiquent via `postMessage`. Pour que les deux côtés soient en accord sur la structure des données échangées, les types de payload doivent être partagés.

```typescript
// Correct — payload typé depuis le module partagé
import type { TableMetadataDto } from '@genesis-labs/shared-types';

panel.webview.postMessage({ 
    type: 'TABLES_METADATA_LOADED', 
    payload: data as TableMetadataDto[]
});

// Interdit — payload typé localement
import type { TableMetadataDto } from '../types/local.types';
```

# module genesis-vsc — Webview (packages/webview/src/)

**Règle 1 :** L'alias `@/` pointe EXCLUSIVEMENT vers les fichiers locaux du webview (`packages/webview/src/`). Il ne doit jamais pointer vers un module externe.

Raison : `@/` désigne toujours "moi-même" — les fichiers du module courant. Le faire pointer vers le core crée une confusion de responsabilité et des imports silencieusement incorrects.

```typescript
// Correct — import d'un fichier local du webview
import { useGeneratorVsc } from '@/features/generator/composables/useGeneratorVsc';
import { vscodeService } from '@/core/services/vscode.service';

// Interdit — @/ ne pointe pas vers le core
import { useGenerator } from '@/features/generator/composables/useGenerator';
import GenesisButton from '@/core/components/ui/actions/GenesisButton.vue';
```

**Règle 2 :** Les imports depuis `genesis-web-core` utilisent TOUJOURS l'alias `@genesis-labs/core` suivi du chemin vers le manifeste de la feature. Jamais un chemin relatif vers les sources du core.

Raison : l'alias `@genesis-labs/core` est le nom du package déclaré dans `package.json`. Il est cohérent avec la résolution npm et stable quel que soit l'emplacement des dossiers.

```typescript
// Correct
import { useGenerator, GENERATOR_SERVICE_KEY } from '@genesis-labs/core/features/generator/manifest';
import { useFrameworks, FRAMEWORK_SERVICE_KEY } from '@genesis-labs/core/features/frameworks/manifest';

// Interdit
import { useGenerator } from '../../../genesis-web-core/src/features/generator/composables/useGenerator';
import { useGenerator } from '@/features/generator/composables/useGenerator';
```

**Règle 3 :** Les imports depuis `genesis-web-types-shared` utilisent TOUJOURS le nom du package `@genesis-labs/shared-types`. Jamais un chemin relatif vers ses sources.

Raison : identique à la Règle 2 — cohérence avec la résolution npm et stabilité des imports.

```typescript
// Correct
import type { Framework, GeneratorData } from '@genesis-labs/shared-types';

// Interdit
import type { Framework } from '../../../genesis-web-types-shared/src/framework.shared';
import type { Framework } from '@genesis-labs/core/features/frameworks/types/framework.types';
```

**Règle 4 :** Le webview n'importe JAMAIS en profondeur dans les fichiers internes du core. Tout import depuis `@genesis-labs/core` passe obligatoirement par un manifeste de feature.

Raison : le manifeste est la surface d'API stable du core. Un import profond crée un couplage fort qui casse au moindre refactoring interne du core.

```typescript
// Correct
import { useGenerator } from '@genesis-labs/core/features/generator/manifest';

// Interdit
import { useGenerator } from '@genesis-labs/core/features/generator/composables/useGenerator';
import { useGeneratorStore } from '@genesis-labs/core/features/generator/store/useGenerator.store';
import GenesisButton from '@genesis-labs/core/core/components/ui/actions/GenesisButton.vue';
```

**Règle 5 :** Les fichiers locaux du webview s'importent entre eux UNIQUEMENT via l'alias `@/`. Les chemins relatifs entre fichiers du webview sont interdits.

Raison : les chemins relatifs profonds (`../../../`) sont fragiles et difficiles à maintenir. L'alias `@/` garantit des imports absolus stables quel que soit l'emplacement du fichier.

```typescript
// Correct
import { useGeneratorVsc } from '@/features/generator/composables/useGeneratorVsc';
import { vscodeService } from '@/core/services/vscode.service';

// Interdit
import { useGeneratorVsc } from '../composables/useGeneratorVsc';
import { vscodeService } from '../../core/services/vscode.service';
```

**Règle 6 :** Les services du webview (`*.service.ts`) implémentent les interfaces du core importées via le manifeste. Ils n'importent jamais directement les interfaces depuis leurs fichiers sources.

Raison : le contrat de service est exposé par le manifeste. L'importer depuis sa source interne contourne le manifeste et crée un import profond interdit par la Règle 4.

```typescript
// Correct
import type { IGeneratorService, GENERATOR_SERVICE_KEY } from '@genesis-labs/core/features/generator/manifest';

export class GeneratorServiceVsc implements IGeneratorService { ... }

// Interdit
import type { IGeneratorService } from '@genesis-labs/core/features/generator/types/generator.service.interface';
```

**Règle 7 :** `main.ts` est le SEUL fichier autorisé à appeler `app.provide()` pour fournir les services concrets aux composables du core. Aucun autre fichier du webview ne doit appeler `app.provide()`.

Raison : la fourniture des services est une responsabilité du bootstrap. La centraliser dans `main.ts` garantit qu'il n'existe qu'un seul endroit où les dépendances sont injectées, rendant le flux de dépendances prévisible et auditable.

```typescript
// Correct — uniquement dans main.ts
import { GENERATOR_SERVICE_KEY } from '@genesis-labs/core/features/generator/manifest';
import { generatorServiceVsc } from '@/features/generator/services/generator.service';

app.provide(GENERATOR_SERVICE_KEY, generatorServiceVsc);

// Interdit — dans un composable, une vue ou un store
app.provide(GENERATOR_SERVICE_KEY, generatorServiceVsc);
```