
# Genesis SDK Web — Adaptations REST à réaliser

**Destinataire :** Responsable `genesis-sdk-web` (VS Code).

**Périmètre :** Spring Boot Web API + React / Angular / Vue + PostgreSQL / MySQL. Toutes les fonctionnalités sauf l'intelligence artificielle.

## 1. Correction prioritaire : préfixe Axios

Fichier :

`genesis-vsc/src/services/http/genesisAxiosInstance.ts`

Le SDK possède déjà :

```typescript
baseURL: `http://127.0.0.1:${port}/api`
```

Les services doivent donc utiliser des URL relatives à `/api`.

Exemple :

AVANT :
```typescript
getAxiosInstance().get('/api/frontend/frameworks');
```

APRÈS :
```typescript
getAxiosInstance().get('/frontend/frameworks');
```

Appliquer cette correction dans :

- `VsCodeFrameworkService.ts`
- `VsCodeFrontendService.ts`
- `VsCodeGeneratorService.ts`
- `VsCodeDatabaseService.ts`

Ne pas créer d'alias `/api/api/...` dans Genesis API.

### Gestion des mocks

Plusieurs services SDK retournent automatiquement des données `MOCK_*` lorsqu'une requête HTTP échoue.

En fonctionnement réel :

- Ne pas masquer les erreurs du serveur.
- Afficher le message HTTP réel.
- Réserver les mocks à un mode démonstration explicite.
- Ne pas considérer des données mockées comme des résultats de Genesis API.

---

## 2. Catalogue backend — VsCodeFrameworkService.ts

Routes relatives au `baseURL` Axios :

| Méthode SDK | Route à utiliser | État |
|---|---|---|
| fetchFrameworks | GET /frameworks | Confirmée |
| fetchLanguages | GET /frameworks/languages | Confirmée |
| fetchCoreFrameworks | GET /frameworks/cores | Confirmée |
| fetchViewTemplates | GET /frameworks/templates | Confirmée |
| fetchLoggingLevels | GET /logging_levels/{frameworkId} | Ajout API proposé |
| fetchSecurityTypes | GET /security_types/{frameworkId} | Ajout API proposé |
| fetchCacheProviders | GET /cache_providers/{frameworkId} | Ajout API proposé |
| fetchLanguageVersions | GET /generator/language-versions/{languageId} | Ajout API proposé |
| fetchFrameworkVersions | GET /generator/framework-versions/{frameworkId} | Ajout API proposé |
| fetchBuildTools | GET /generator/build-tools/{frameworkId} | Ajout API proposé |
| fetchHibernateDdlAutoOptions | GET /hibernate_ddl_auto_options/{frameworkId} | Ajout API proposé |

L'API possède également :

```http
GET /api/project/config/options
```

Cet endpoint peut fournir plusieurs options regroupées.

### Points importants

- Ne pas inventer de valeurs si Core retourne une liste vide.
- Ne pas confondre `languageId` et `frameworkId`.
- Utiliser les identifiants réellement retournés par les catalogues.
- Vérifier les formats de réponse avant de les convertir en types TypeScript.

---

## 3. Catalogue et configuration frontend

Fichier :

`VsCodeFrontendService.ts`

| Fonctionnalité | Route relative | État |
|---|---|---|
| Frameworks React / Angular / Vue | GET /frontend/frameworks | Confirmée |
| Langages JavaScript / TypeScript | GET /frontend/programming-languages | Ajout proposé |
| Langues d'interface | GET /frontend/interface-languages | Ajout proposé |
| Types de navbar | GET /frontend/navbar-types | Ajout proposé |
| Configuration frontend | POST /generator/frontend-config | Confirmée |
| Personnalisation visuelle | POST /generator/frontend-layout-config | Ajout proposé |
| Upload logo | POST /generator/frontend/assets/logo | Ajout proposé |
| Upload favicon | POST /generator/frontend/assets/favicon | Ajout proposé |

### Configuration frontend

Ancienne sélection SDK :

```http
POST /api/frontend/{frameworkId}/select
```

Cette route ne doit plus être utilisée.

Utiliser :

```http
POST /api/generator/frontend-config
```

Exemple :

```json
{
  "frameworkId": 1,
  "languageId": null,
  "port": "5173"
}
```

L'identifiant `1` est illustratif. Utiliser celui retourné par le catalogue React, Angular ou Vue.

Le langage doit correspondre aux possibilités réellement proposées par Core.

---

## 4. Layout, couleurs, langues, navbar, logo et favicon

Le SDK initial possède :

```typescript
interface FrontendLayoutConfig {
  selectedInterfaceLanguages: string[];
  navbarType: 'side' | 'top' | '';
  primaryColor: string;
  secondaryColor: string;
  logoPath: string;
  faviconPath: string;
  port: string;
}
```

Le contrat API proposé est désormais :

```typescript
interface FrontendLayoutConfig {
  selectedInterfaceLanguages: string[];
  navbarType: 'side' | 'top' | '';
  primaryColor: string;
  secondaryColor: string;
  logoPath: string;
  faviconPath: string;
  port: string;

  logoAssetId?: string | null;
  faviconAssetId?: string | null;
}
```

Endpoint :

```http
POST /api/generator/frontend-layout-config
```

Exemple :

```json
{
  "selectedInterfaceLanguages": ["fr"],
  "navbarType": "side",
  "primaryColor": "#2563EB",
  "secondaryColor": "#10B981",
  "logoPath": "",
  "faviconPath": "",
  "port": "5173",
  "logoAssetId": null,
  "faviconAssetId": null
}
```

### Couleurs

La validation API proposée accepte :

```text
#RRGGBB
```

Exemples :

```text
#2563EB
#10B981
```

Attention : le SDK initial contient certains exemples avec huit chiffres hexadécimaux, comme `#537cc240`.

Il faut harmoniser les valeurs du sélecteur de couleurs avec celles réellement acceptées par Genesis API.

### Navbar

La valeur actuellement garantie est :

```json
{
  "navbarType": "side"
}
```

Elle correspond à `Sidebar` dans Core.

La valeur `top` doit rester désactivée tant que son fonctionnement n'a pas été confirmé pour React, Angular et Vue.

### Logo et favicon par URL

Le SDK peut envoyer :

```json
{
  "logoPath": "https://exemple.com/logo.png",
  "faviconPath": "https://exemple.com/favicon.ico"
}
```

Les URL doivent être HTTPS.

Ne pas envoyer directement les chemins locaux de l'ordinateur de l'utilisateur.

---

## 5. Upload de logo et favicon locaux

Pour les images sélectionnées sur le poste VS Code, effectuer un transfert HTTP.

Endpoints :

```http
POST /api/generator/frontend/assets/logo
POST /api/generator/frontend/assets/favicon
```

Content-Type :

```http
multipart/form-data
```

Nom du champ :

```text
file
```

Exemple de réponse :

```json
{
  "assetId": "UUID_RETOURNE_PAR_API"
}
```

Ensuite, transmettre cet identifiant à la configuration du layout :

```json
{
  "selectedInterfaceLanguages": ["fr"],
  "navbarType": "side",
  "primaryColor": "#2563EB",
  "secondaryColor": "#10B981",
  "logoPath": "",
  "faviconPath": "",
  "port": "5173",
  "logoAssetId": "UUID_RETOURNE_PAR_API",
  "faviconAssetId": null
}
```

Règles :

1. Ne pas fournir simultanément une URL et un assetId pour une même image.
2. Ne pas envoyer un chemin local en supposant que le serveur y a accès.
3. Effectuer l'upload et la génération dans la même session serveur.
4. Vérifier la présence effective des images dans le frontend généré.

Les ressources sont actuellement conservées temporairement côté API.

Le SDK doit traiter les erreurs de transfert et empêcher l'utilisateur de lancer une génération avec une référence d'image invalide.

---

## 6. Base de données

Fichiers concernés :

- `VsCodeDatabaseService.ts`
- `VsCodeGeneratorService.ts`

| Fonctionnalité | Route relative |
|---|---|
| Catalogue des moteurs | GET /database/engines |
| Test de connexion | POST /database/test-connection |
| Enregistrement DB | POST /generator/database-config |
| Métadonnées des tables | GET /tables_metadata_all |
| Tables parentes | GET /tables_metadata/parents |
| Tables enfants | GET /tables_metadata/childs |
| Relations | GET /relations |
| Détection des relations | POST /relations/detect |

### Ancienne sélection du moteur

Le SDK appelle :

```http
POST /api/database/{engineId}/select
```

Cette route n'est pas exposée dans Genesis API.

Il faut conserver l'identifiant du moteur choisi dans l'état du formulaire, puis enregistrer la configuration JDBC complète :

```http
POST /api/generator/database-config
```

Exemple PostgreSQL :

```json
{
  "databaseId": 2,
  "engine": "PostgreSQL",
  "host": "localhost",
  "port": "5432",
  "databaseName": "genesis_test",
  "schema": "public",
  "username": "postgres",
  "password": "MOT_DE_PASSE"
}
```

Les identifiants doivent correspondre au catalogue réel des moteurs.

Ne jamais afficher les mots de passe dans les logs VS Code.

---

## 7. Paramètres de génération et profils multiples

Fichier concerné :

`VsCodeGeneratorService.ts`

| Fonctionnalité | Route relative |
|---|---|
| Enregistrer les profils | POST /generator/generation-parameters |
| Lire les profils | GET /generator/generation-parameters |
| Lire les relations d'un profil | GET /generator/profiles/{profileName}/relations |
| Enregistrer les relations d'un profil | PUT /generator/profiles/{profileName}/relations |
| Plan logique | GET /generator/plan |
| Plan consolidé | GET /generator/execution-plan |
| Validation des relations | GET /generator/validate-relations |

### Format des profils

Envoyer un tableau JSON :

```json
[
  {
    "profileName": "Administration",
    "selectedTables": ["client", "commande"],
    "selectedViews": [],
    "selectedComponents": [
      "model",
      "dao",
      "service"
    ]
  },
  {
    "profileName": "Catalogue",
    "selectedTables": ["client"],
    "selectedViews": [],
    "selectedComponents": [
      "controller"
    ]
  }
]
```

Les composants reconnus sont :

- model
- dao
- service
- controller

### Relations par profil

Utiliser :

```http
PUT /api/generator/profiles/{profileName}/relations
```

Exemple :

```json
{
  "relations": [
    {
      "parentTable": "client",
      "childTable": "commande",
      "mandatory": false,
      "hasForm": true
    }
  ]
}
```

Les relations doivent correspondre aux clés étrangères de la base.

Une liste :

```json
{
  "relations": []
}
```

est une sélection manuelle volontairement vide.

Elle ne doit pas être remplacée automatiquement par des relations détectées.

### Règles de génération

- Les composants sélectionnés dans différents profils sont cumulés.
- Un composant déjà généré ne doit pas être régénéré.
- La génération complète de l'interface exige les composants nécessaires cumulés.
- Les sélections partielles doivent être respectées.
- Les configurations contradictoires doivent être refusées.
- Les fichiers existants ne doivent pas être écrasés.

---

## 8. Scripts SQL

Fichier concerné :

`VsCodeGeneratorService.ts`

### Configuration du script

Le SDK initial appelle :

```http
POST /api/generator/script-config
```

Format attendu :

```json
{
  "path": "scripts/init.sql",
  "content": "CREATE TABLE exemple (id BIGINT PRIMARY KEY);"
}
```

Cet endpoint doit uniquement enregistrer la configuration.

Il ne doit pas exécuter le script.

### Validation SQL

Endpoint :

```http
POST /api/generator/sql/validate
```

Exemple :

```json
{
  "script": "CREATE TABLE exemple (id BIGINT PRIMARY KEY);",
  "confirmExecution": false
}
```

La validation vérifie le format de base, mais ne garantit pas la validité SQL complète.

### Exécution SQL

Endpoint :

```http
POST /api/generator/sql/execute
```

Exemple :

```json
{
  "script": "INSERT INTO exemple (id) VALUES (1);",
  "confirmExecution": true
}
```

L'exécution doit être déclenchée explicitement.

**Ne jamais exécuter automatiquement un script lors de l'enregistrement des paramètres du wizard.**

Le test manuel d'insertion SQL a déjà été déclaré réussi.

---

## 9. Configuration Git

Fichier concerné :

`VsCodeGeneratorService.ts`

### Ancienne route SDK

```http
POST /api/generator/git-config
```

### Nouvelle route API

```http
POST /api/generator/git/config
```

### Fonctionnalités existantes

Genesis Core prend déjà en charge :

- git init
- .gitignore
- git add
- git commit
- dépôt unique
- dépôts backend/frontend séparés
- dépôt distant
- GitHub
- git push

Le SDK doit réutiliser la configuration existante de Core.

### Différence entre les DTO

Ancien type SDK :

```typescript
interface GitConfiguration {
  useGit: boolean;
  separateRepositories: boolean;
  useRemoteRepo: boolean;
  isNewRemoteRepo: boolean;
  repositoryName: string;
  backendRepositoryName: string;
  frontendRepositoryName: string;
  githubUsername: string;
  githubToken: string;
}
```

Dans Core/API, vérifier notamment la propriété :

```typescript
createRemoteRepository
```

Elle doit être mise en correspondance avec `isNewRemoteRepo` selon la sémantique exacte de Core.

Ne pas supprimer arbitrairement `useRemoteRepo`.

### Précautions

- Ne jamais journaliser un token GitHub.
- Ne jamais envoyer le token dans une réponse REST.
- Ne pas effectuer de push distant sans confirmation.
- Vérifier si `/launch` initialise déjà Git pour éviter une seconde initialisation.
- Ne jamais remplacer un dépôt Git existant.

---

## 10. Lancement de la génération

C'est l'une des adaptations les plus importantes.

### Ancien fonctionnement SDK

```typescript
launchGeneration(data: GeneratorData) {
  return getAxiosInstance().post(
    '/generator/launch',
    data
  );
}
```

Le SDK transmet un objet complet au dernier moment.

### Fonctionnement Genesis API

Endpoint :

```http
POST /api/generator/launch
```

Dans `genesis-api(6).zip`, cet endpoint ne lit pas le corps de la requête.

Il utilise les configurations préalablement enregistrées.

Le SDK doit donc procéder séquentiellement :

1. Sélectionner le framework backend.
2. Enregistrer la configuration du projet.
3. Enregistrer la configuration frontend.
4. Configurer PostgreSQL ou MySQL.
5. Enregistrer les paramètres visuels.
6. Configurer les scripts SQL si nécessaire.
7. Enregistrer les profils et relations.
8. Enregistrer les options Git.
9. Consulter le plan d'exécution.
10. Appeler `/generator/launch`.

Il ne faut pas supposer que `GeneratorData` transmis uniquement à `/launch` remplace toutes les configurations enregistrées.

### Réponse de génération

L'ancien SDK attend notamment :

```typescript
interface GenerationResult {
  success: boolean;
  message: string;
  projectPath?: string;
}
```

L'API renvoie un résultat contenant notamment :

```json
{
  "success": true,
  "backendBatchCount": 3,
  "frontendTaskCount": 2,
  "publication": {
    "preparedFileCount": 25,
    "createdFiles": [],
    "skippedExistingFiles": []
  }
}
```

Cet exemple est illustratif.

Le SDK doit utiliser la réponse réelle du serveur pour afficher :

- Le succès ou l'échec.
- Le nombre de lots backend.
- Les tâches frontend.
- Les fichiers créés.
- Les fichiers existants ignorés.
- Les erreurs éventuelles.

Ne pas inventer un champ `projectPath` si l'API ne le retourne pas.

---

## 11. Contrats TypeScript à vérifier

Fichier :

`genesis-web-types-shared/src/generator.shared.ts`

Vérifier les interfaces :

- Framework
- FrontendFramework
- FrontendProgrammingLanguage
- InterfaceLanguage
- DatabaseEngineDto
- GenerationParameter
- FrontendLayoutConfig
- GitConfiguration
- GenerationResult

Ne pas supposer que les types actuels correspondent automatiquement aux DTO Java.

Vérifier notamment :

- Les champs obligatoires.
- Les valeurs nulles.
- Les identifiants numériques.
- Les tableaux et objets enveloppes.
- Les erreurs HTTP.
- Les compteurs de génération.
- Les références aux ressources graphiques.

Éviter les conversions TypeScript forcées destinées à masquer des incompatibilités.

---

## 12. Gestion des erreurs

Le SDK doit distinguer les principaux statuts REST :

| Code HTTP | Interprétation |
|---|---|
| 200 | Opération réussie |
| 400 | Configuration invalide |
| 403 | Opération interdite ou désactivée |
| 404 | Ressource ou route introuvable |
| 409 | Conflit, collision ou génération déjà en cours |
| 500 | Erreur serveur ou échec de génération |

Si Genesis signale une publication partielle, informer clairement l'utilisateur.

Ne jamais transformer silencieusement une erreur réelle en succès provenant des mocks.

---

## 14. Tests obligatoires du SDK

Le responsable SDK doit vérifier :

### Communication REST

- Plus aucun appel `/api/api/...`.
- Aucune erreur masquée par des mocks.
- Tous les catalogues utilisent les données réelles de l'API.
- Les DTO correspondent aux réponses Java.

### Configuration

- Spring Boot Web API sélectionnable.
- React, Angular et Vue disponibles.
- PostgreSQL et MySQL disponibles.
- Langues et versions correctes.
- Couleurs et paramètres frontend correctement enregistrés.
- Logo et favicon utilisables.
- Configuration SQL sauvegardable.
- Configuration Git correcte.

### Génération

- Plusieurs profils.
- Relations manuelles.
- Composants partiels.
- Déduplication.
- Prévisualisation du plan.
- Lancement réussi.
- Rapport de publication affiché.
- Conservation des fichiers déjà présents.

### Six combinaisons à valider

1. Spring Boot Web API + React + PostgreSQL.
2. Spring Boot Web API + Angular + PostgreSQL.
3. Spring Boot Web API + Vue + PostgreSQL.
4. Spring Boot Web API + React + MySQL.
5. Spring Boot Web API + Angular + MySQL.
6. Spring Boot Web API + Vue + MySQL.

Pour chaque combinaison :

- Générer le backend.
- Générer le frontend.
- Compiler les deux projets.
- Démarrer les applications.
- Vérifier les routes REST.
- Tester les opérations CRUD.
- Tester les relations.
- Vérifier les personnalisations visuelles.
- Relancer une génération et vérifier qu'aucun fichier existant n'est écrasé.

---

## Conclusion

Le SDK doit principalement être adapté aux contrats REST existants de Genesis API.

Il ne doit pas recréer les fonctionnalités déjà présentes dans Genesis Core.

Les priorités sont :

1. Corriger les URL Axios.
2. Aligner les DTO et les routes.
3. Supprimer les mocks silencieux en mode réel.
4. Raccorder correctement les configurations frontend, SQL et Git.
5. Adapter le lancement au parcours séquentiel de Genesis API.
6. Valider les six combinaisons de génération.
