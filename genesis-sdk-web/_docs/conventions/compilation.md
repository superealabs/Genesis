```mdc
# Ordre de compilation du projet genesis-sdk-web

## Règle fondamentale
Un module ne peut être compilé que si tous ses modules dont il dépend sont déjà compilés.

## Ordre

**Étape 1 — `genesis-web-types-shared`**
Doit être compilé en premier. Aucune dépendance externe.
Produit : `dist/index.js` + `dist/index.d.ts`

**Étape 2 — `genesis-web-core`**
Dépend de : `genesis-web-types-shared` (via `@genesis-labs/shared-types`)
Peut être compilé une fois l'étape 1 terminée.

**Étape 3 — `genesis-vsc/packages/webview`**
Dépend de : `genesis-web-types-shared` + `genesis-web-core`
Peut être compilé une fois les étapes 1 et 2 terminées.
Produit : les assets statiques du webview (HTML, JS, CSS)

**Étape 4 — `genesis-vsc/src` (Extension Host)**
Dépend de : `genesis-web-types-shared` + assets du webview (étape 3)
Peut être compilé une fois les étapes 1 et 3 terminées.
Produit : `dist/extension.js`

## Schéma

```
genesis-web-types-shared     (étape 1)
        |
        |-----> genesis-web-core           (étape 2)
        |               |
        |               |-----> genesis-vsc/packages/webview   (étape 3)
        |                                   |
        |------------------------------> genesis-vsc/src        (étape 4)
```

## Commandes dans l'ordre

```bash
# Étape 1
cd genesis-web-types-shared && npm run build

# Étape 2
cd genesis-web-core && npm run build

# Étape 3
cd genesis-vsc/packages/webview && npm run build

# Étape 4
cd genesis-vsc && npm run compile
```

## Avec le package.json racine (recommandé)

```bash
# Une seule commande depuis genesis-sdk-web/
npm run build
```

```json
"scripts": {
    "build": "npm run build:types && npm run build:core && npm run build:webview && npm run build:vsc",
    "build:types":   "npm run build --workspace=genesis-web-types-shared",
    "build:core":    "npm run build --workspace=@genesis-labs/core",
    "build:webview": "npm run build --workspace=@genesis/webview-vscode",
    "build:vsc":     "npm run compile --workspace=genesis"
}
```
```