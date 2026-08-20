Pour un projet de 20-30 personnes avec des dépendances entre composants, je te recommande une architecture **Feature-based** combinée avec les principes de **Atomic Design**.

---

## Pourquoi pas une structure classique `components/views/store` ?

Parce qu'avec 20-30 personnes, la structure classique devient ingérable — tout le monde touche aux mêmes dossiers, les conflits Git explosent, et personne ne sait quel composant appartient à quelle fonctionnalité.

---

## Structure recommandée

```
src/
├── assets/                        ← images, fonts, icônes globales
│
├── core/                          ← tout ce qui est partagé globalement
│   ├── components/                ← composants UI atomiques réutilisables
│   │   ├── BaseButton.vue
│   │   ├── BaseInput.vue
│   │   └── BaseModal.vue
│   ├── composables/               ← logique réutilisable (useVsCode, useTheme)
│   │   ├── useVsCode.ts           ← gestion postMessage
│   │   └── useTheme.ts
│   ├── types/                     ← types TypeScript globaux
│   │   └── messages.ts            ← types des messages extension ↔ webview
│   └── utils/                     ← fonctions utilitaires pures
│       └── format.ts
│
├── features/                      ← une feature = un domaine métier
│   ├── environments/              ← exemple : gestion des environnements
│   │   ├── components/            ← composants propres à cette feature
│   │   │   ├── EnvCard.vue
│   │   │   └── EnvForm.vue
│   │   ├── composables/
│   │   │   └── useEnvironments.ts
│   │   ├── store/
│   │   │   └── environments.store.ts
│   │   ├── types/
│   │   │   └── environment.types.ts
│   │   └── index.ts               ← barrel export de la feature
│   │
│   └── settings/                  ← exemple : feature settings
│       ├── components/
│       ├── composables/
│       ├── store/
│       └── index.ts
│
├── layouts/                       ← layouts globaux de l'app
│   ├── MainLayout.vue
│   └── SidebarLayout.vue
│
├── router/                        ← Vue Router
│   └── index.ts
│
├── store/                         ← store global Pinia (root)
│   └── index.ts
│
├── App.vue
└── main.ts
```

---

## Les règles de dépendance

C'est ce qui évite le chaos avec 30 personnes :

```
core/     ← aucune dépendance vers features/ (jamais)
features/ ← peut importer depuis core/ uniquement
features/ ← une feature N'importe PAS une autre feature directement
layouts/  ← peut importer depuis core/ et features/
```

Si deux features partagent quelque chose → ça monte dans `core/`.

---

## Le fichier `core/composables/useVsCode.ts`

C'est le plus important de tout le projet — il centralise toute la communication avec l'extension :

```typescript
const vscode = acquireVsCodeApi();

export function useVsCode() {
    function send(type: string, payload?: unknown) {
        vscode.postMessage({ type, payload });
    }

    function onMessage(handler: (message: { type: string, payload: unknown }) => void) {
        window.addEventListener('message', (event) => handler(event.data));
    }

    return { send, onMessage };
}
```

Ainsi personne ne touche à `acquireVsCodeApi()` directement — tout passe par ce composable.

---

Tu veux qu'on mette en place cette structure maintenant avant de continuer les exercices ?