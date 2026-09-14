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

# 