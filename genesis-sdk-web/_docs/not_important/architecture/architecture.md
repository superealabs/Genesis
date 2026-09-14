## workflow
INPUT (Webview → JAR) : Views → Composables → Store → Services → VSCODE_service → Extension Host → Extension Service → JAR 
OUTPUT (JAR → Webview) : JAR → Extension Service → Extension Host → VSCODE_service → Services → Store → Composables → Views




## ✅ Architecture corrigée

```
VIEWS (.vue)
├─ Affiche les données depuis le Store
├─ Appelle UNIQUEMENT les Composables
└─ Écoute les changements du Store

COMPOSABLES (.composable.ts)
├─ Logique UI réusable (hooks)
├─ Peut LIRE le Store (via getters)
├─ Appelle les Services pour ÉCRIRE dans le Store
└─ Interface unique entre Views et Services

STORE (.store.ts)
├─ État global passif (pas d'appels externes)
├─ Getters pour lire l'état
├─ Mutations/Actions pour modifier l'état
└─ Utilisé par Composables et Services

SERVICES (.service.ts)
├─ Hérite de VscodeService
├─ INPUT : Envoie des données via postMessage
├─ OUTPUT : Reçoit les données, les transforme, met à jour le Store
└─ Validation et mapping des données

VSCODE_SERVICE (.service.ts) - Classe abstraite
├─ Abstraction de postMessage
├─ Gestion des listeners
├─ Typage des messages
└─ Méthodes protégées pour les services enfants

EXTENSION HOST (extension.ts)
├─ Routeur de messages (onDidReceiveMessage)
├─ Dispatch vers le bon Extension Service
└─ Pas de logique métier

EXTENSION SERVICE (.ts)
├─ Reçoit les messages du webview
├─ Valide et appelle l'API (JAR)
├─ Reçoit la réponse de l'API
└─ Renvoie la réponse au webview via postMessage
```