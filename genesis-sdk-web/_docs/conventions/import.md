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