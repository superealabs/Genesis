Voici les règles claires et définitives.

---

## Règle 1 — Dans le core (`genesis-web-core`)

**Entre features du core → toujours par chemin relatif vers `.shared.ts`**

```typescript
// ✅ Dans generator.types.ts qui a besoin de Framework
import type { Framework } from '../frameworks/types/framework.shared';

// ❌ Jamais via le manifest d'une autre feature (import circulaire potentiel)
import type { Framework } from '../frameworks/manifest';

// ❌ Jamais via l'alias @/ vers un .types.ts qui a lui-même des imports @/
import type { Framework } from '@/features/frameworks/types/framework.types';
```

**Dans une vue ou un composable du core → alias `@/` autorisé**

```typescript
// ✅ Les vues et composables vivent dans l'environnement Vite
import GenesisButton from '@/core/components/ui/actions/GenesisButton.vue';
import { useFrameworks } from '@/features/frameworks/composables/useFrameworks';

// ✅ Pour les types, @/ vers .types.ts est ok ici
import type { Framework } from '@/features/frameworks/types/framework.types';
```

---

## Règle 2 — Depuis un module Vite externe (webview)

**Toujours via le manifest, jamais en profondeur**

```typescript
// ✅ Import via manifest
import type { TableMetadataDto, Framework } from '@genesis-labs/core/features/generator/manifest';
import { useGenerator, GeneratorStepper } from '@genesis-labs/core/features/generator/manifest';

// ✅ Ou via l'index central
import type { TableMetadataDto } from '@genesis-labs/core';

// ❌ Jamais en profondeur
import type { TableMetadataDto } from '@genesis-labs/core/features/generator/types/generator.types';
import { useGenerator } from '@genesis-labs/core/features/generator/composables/useGenerator';
```

**Alias dans le webview (`vite.config.ts`)**

```typescript
resolve: {
    alias: {
        '@':                  path.resolve(__dirname, '../../../genesis-web-core/src'),
        '@genesis-labs/core': path.resolve(__dirname, '../../../genesis-web-core/src'),
        '@vsc':               path.resolve(__dirname, './src'),
    }
}
```

---

## Règle 3 — Depuis un module TS pur externe (Extension Host)

**Toujours via `.shared.ts` par chemin relatif ou alias `tsconfig`**

```typescript
// ✅ Chemin relatif direct vers le shared
import type { TableMetadataDto } from '../../../genesis-web-core/src/features/generator/types/generator.shared';

// ✅ Ou via alias tsconfig (plus propre)
import type { TableMetadataDto } from '@genesis-labs/shared/generator';

// ❌ Jamais via le manifest (contient des imports Vue/Vite)
import type { TableMetadataDto } from '@genesis-labs/core/features/generator/manifest';

// ❌ Jamais via .types.ts (peut contenir des alias @/)
import type { TableMetadataDto } from '../../../genesis-web-core/src/features/generator/types/generator.types';
```

**`tsconfig.json` de l'Extension Host**

```json
{
    "compilerOptions": {
        "paths": {
            "@genesis-labs/shared/*": [
                "../../../genesis-web-core/src/features/*/types/*.shared"
            ]
        }
    }
}
```

---

## Tableau récapitulatif

| Contexte | Importe depuis | Exemple |
|---|---|---|
| Core — feature vers feature (types) | `.shared.ts` relatif | `'../frameworks/types/framework.shared'` |
| Core — vue/composable vers type | `@/` vers `.types.ts` | `'@/features/frameworks/types/framework.types'` |
| Core — vue/composable vers composant | `@/` | `'@/core/components/ui/...'` |
| Webview (Vite) vers core | `manifest.ts` via alias | `'@genesis-labs/core/features/generator/manifest'` |
| Extension Host (Node.js) vers core | `.shared.ts` via alias tsconfig | `'@genesis-labs/shared/generator'` |

---

## Règle anti-import circulaire

```
manifest.ts        → peut importer depuis .shared.ts, .types.ts, store, composable, vue
.types.ts          → peut importer depuis .shared.ts et @/ (autres features)
.shared.ts         → N'IMPORTE RIEN (ou uniquement d'autres .shared.ts par chemin relatif)
store              → peut importer depuis .types.ts et @/
composable         → peut importer depuis store, .types.ts, service.interface
vue                → peut importer depuis composable, store, @/core/components
```

La règle de base : **`.shared.ts` est le bas de la hiérarchie — il ne dépend de rien. Tout le reste peut en dépendre, mais lui ne dépend de personne.**