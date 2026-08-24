# Api
## Step 1
- choisir entre :
    - generate new Project
    - Add rule in Project
    - Sync Project
## Step 2

* intérêt :
    sur cette interface, l'utilisateur choisira directement les frameworks

* Requierement :
    - List des Frameworks

* Mode d'affichage :
  - Grid (défaut)
  - Liste

* Barre de recherche "contain" :
  - Nom, Core, Type, Template Engine

* Filtres niveau 1 (affiché grâce à une popup par dessus) :
  - Language
  - Type (MVC / REST API)
  - Core Framework
  - Prod Ready (isProd)

* Filtres niveau 2 — "Filtres avancés" (ouvrable uniquement dans le popup de niveau 1) :
  - Support DB (useDB)
  - Support Cloud (useCloud)
  - Eureka Server (useEurekaServer)
  - Gateway (isGateway)
  - Frontend App (useFrontendApp)
  - Template Engine (viewTemplateEngine) ← MVC uniquement
  - View Extension (viewExtension) ← MVC uniquement

* Panneau détail (apparaît d'en bas au clic) :
  Commun :
  - Nom
  - Core Framework
  - Type (MVC / REST API)
  - Language
  - Support DB, Cloud, Frontend, Gateway
  - Dépendances principales
  - Version ← à ajouter Java (je dois trouver comment c'est afficher sur le formulaire)
  - Description ← à ajouter Java (à ajouter si besoin)

  MVC uniquement :
  - Template Engine
  - View Extension

  la mascoot se pose sur l'élément sléctionnée

# step 3
configuration du framework
- build tool
- groupe ID
- Framework Version

# step 4
connexion à la base de donnée

# step 5
import your own data script
use LLM to edit it

# step 6
table, Views & component selection
preview mode

# step 7
configuration relation

# step 8
frontEnd selection(can be ignored)

# step 9
Git initializer

# step 10
configuration Project Backend