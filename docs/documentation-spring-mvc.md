# Documentation Spring MVC — Genesis

## 1. Présentation

Genesis prend en charge la génération d’applications **Spring Boot MVC avec Thymeleaf**. Cette technologie complète les frameworks déjà pris en charge par Genesis en proposant une solution Java de rendu serveur basée sur l’architecture MVC.

La configuration Spring MVC est déclarée dans Genesis sous le nom :

```text
Spring MVC - Thymeleaf
```

Elle repose sur :

- **Spring Boot**
- **Spring MVC**
- **Thymeleaf**
- **Spring Data JPA**
- **Jakarta Validation**
- **Hibernate**
- **Lombok**
- **Thymeleaf Layout Dialect**
- **Maven ou Gradle** comme outil de build
- une base de données relationnelle prise en charge par Genesis

La génération conserve la philosophie générale de Genesis : les métadonnées de la base de données, les paramètres de génération et les choix réalisés dans le plugin servent à produire automatiquement la structure du projet et les composants nécessaires.

---

# Partie I — Documentation utilisateur

## 2. Prérequis

Pour utiliser la génération Spring MVC de Genesis, il faut disposer de :

- Genesis installé et fonctionnel ;
- une base de données accessible ;
- un JDK compatible avec la version Spring Boot choisie ;
- IntelliJ IDEA si le plugin Genesis IntelliJ est utilisé ;
- Maven ou Gradle selon l’outil de build choisi.


Pour Gradle, le projet généré utilise le **Gradle Wrapper 8.11.1**.

---

## 3. Création d’un projet Spring MVC

Depuis l’assistant de génération Genesis :

1. créer ou ouvrir une configuration de projet ;
2. renseigner les informations générales du projet ;
3. sélectionner le framework principal **Spring Boot** ;
4. sélectionner **Spring MVC - Thymeleaf** ;
5. sélectionner l’outil de build : **Maven** ou **Gradle** ;
6. configurer la connexion à la base de données ;
7. sélectionner les tables ou entités à générer ;
8. configurer éventuellement la sécurité, le thème, le logo, le favicon et le dashboard automatique ;
9. lancer la génération.

---

## 4. Paramètres Spring MVC disponibles

### 4.1 Version Spring Boot

Les versions déclarées dans la configuration Genesis sont :

```text
3.3.6
3.2.12
```

La version par défaut est `3.3.6`.

### 4.2 Hibernate DDL Auto

Genesis permet de définir la stratégie Hibernate :

```text
create
create-drop
none
update
```

La valeur par défaut est `none`.

### 4.3 Sécurité

Les choix disponibles sont :

```text
NONE
Spring Security - Session Authentication
```

Par défaut, aucune sécurité n’est activée.

---

## 5. Structure d’un projet généré

Un projet Spring MVC généré possède une structure proche de :

```text
MonProjet/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── <group>/<projet>/
│   │   │       ├── config/
│   │   │       ├── controllers/
│   │   │       ├── models/
│   │   │       ├── repositories/
│   │   │       ├── services/
│   │   │       └── utils/
│   │   └── resources/
│   │       ├── static/
│   │       │   ├── css/
│   │       │   ├── images/
│   │       │   ├── i18n/
│   │       │   └── js/
│   │       ├── templates/
│   │       │   ├── layout/
│   │       │   ├── dashboard/
│   │       │   └── <entite>/
│   │       └── application.properties
│   └── test/
│       └── java/
└── fichiers de build
```

---

## 6. Composants générés par entité

Selon les options sélectionnées et la structure de la base, Genesis peut générer plusieurs composants.

### 6.1 Modèle JPA

Une classe modèle peut contenir notamment :

- `@Entity`
- `@Table`
- `@Id`
- `@GeneratedValue`
- `@Column`
- `@ManyToOne`
- `@JoinColumn`
- les contraintes Jakarta Validation
- les annotations Lombok

### 6.2 Repository

Genesis génère la couche d’accès aux données à partir de Spring Data JPA.

### 6.3 Service

Le service contient la logique d’accès aux données utilisée par le contrôleur.

### 6.4 Contrôleur MVC

Le contrôleur Spring MVC gère notamment :

- l’affichage d’une liste ;
- la consultation d’un élément ;
- la création ;
- la modification ;
- la suppression ;
- l’export CSV ;
- la prévisualisation et le téléchargement de fichiers lorsque l’entité contient un champ fichier.

### 6.5 Vues Thymeleaf

Genesis génère les vues nécessaires, notamment :

```text
list
view
create
edit
```

Les vues utilisent le layout principal du projet.

---

## 7. Fonctionnalités CRUD

La génération Spring MVC prend en charge les opérations CRUD classiques :

- **Create** : création d’une nouvelle donnée ;
- **Read** : consultation et affichage ;
- **Update** : modification ;
- **Delete** : suppression.

---

## 8. Recherche, tri et pagination

Les listes générées disposent des mécanismes permettant :

- la recherche ;
- le filtrage ;
- le tri ;
- la pagination ;
- le choix de la taille de page.

Le contrôleur utilise notamment `Pageable`, `PageRequest` et `Sort`. Genesis génère également un DTO de recherche pour les entités concernées.

---

## 9. Gestion des relations

Les relations de clé étrangère sont prises en compte lors de la génération.

Exemple :

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "categorie_code")
private Categorie categorie;
```

Dans les formulaires, les entités étrangères disponibles peuvent être chargées afin de proposer leur sélection.

Genesis tient compte :

- de la colonne référencée ;
- du caractère nullable ou obligatoire ;
- des métadonnées de relation présentes dans le schéma.

---

## 10. Clés primaires

### 10.1 Clé primaire simple

Les routes utilisent la valeur de l’identifiant.

Exemple :

```text
/produit/1
/produit/1/edit
```

### 10.2 Clé primaire composite

Genesis peut générer une classe d’identifiant dédiée ainsi que les routes contenant les différentes parties de la clé. La logique de consultation, modification et suppression est adaptée à ce cas.

---

## 11. Gestion des fichiers

Lorsque la table contient un type supporté comme fichier binaire, Genesis peut générer :

- l’upload depuis le formulaire ;
- le stockage en base ;
- la prévisualisation ;
- le téléchargement.

Des routes dédiées sont produites, par exemple :

```text
/files/<champ>/preview
/files/<champ>/download
```

Le projet généré contient également une classe utilitaire `FileUtils`.

---

## 12. Export CSV

Les listes générées peuvent être exportées en CSV.

Le projet contient un service générique :

```text
CsvExportService
```

Le contrôleur transforme les données en lignes CSV puis renvoie un fichier de type `text/csv`.

---

## 13. Interface et internationalisation

Le template Spring MVC contient une gestion des langues basée sur :

```text
static/i18n/fr.json
static/i18n/en.json
```

Le script :

```text
static/js/lang.js
```

charge les traductions correspondant à la langue sélectionnée.

Les éléments HTML utilisent principalement :

```html
data-i18n="..."
```

---

## 14. Thème

L’interface générée utilise des variables CSS afin de rester cohérente avec le thème configuré par l’utilisateur.

Les composants concernés incluent notamment :

- navigation ;
- formulaires ;
- boutons ;
- listes ;
- dashboard ;
- graphiques.

---

## 15. Dashboard automatique

Genesis peut générer un dashboard Spring MVC à partir d’une configuration définie dans le plugin.

### 15.1 Types de visualisations

Les types présents dans le modèle Dashboard Genesis comprennent notamment :

```text
BAR_VERTICAL
BAR_HORIZONTAL
PIE
DONUT
LINE
KPI
TABLE
MAP
SCATTER
```

### 15.2 Organisation

Une configuration Dashboard est composée de pages. Chaque page contient des visualisations et chaque visualisation possède notamment :

- un identifiant ;
- un titre ;
- un type ;
- une source de données ;
- des champs ;
- des filtres ;
- des options ;
- une position dans la grille.

La position est représentée par :

```text
x
y
width
height
```

### 15.3 Fichiers générés

Pour Spring MVC, Genesis génère notamment :

```text
DashboardController.java
DashboardService.java
templates/dashboard/index.html
static/js/dashboard.js
```

La route principale est :

```text
/dashboard
```

Les graphiques sont rendus avec **Chart.js** et les cartes avec **Leaflet**.

---

## 16. Lancement avec Maven

Dans le dossier du projet généré :

```bash
./mvnw spring-boot:run
```

Sous Windows :

```powershell
mvnw.cmd spring-boot:run
```

Pour compiler :

```bash
./mvnw clean package
```

Les fichiers produits se trouvent dans `target/`.

---

## 17. Lancement avec Gradle

Le support Gradle permet d’utiliser le même projet Spring Boot avec un autre outil de build.

Le wrapper utilisé est :

```text
Gradle 8.11.1
```

Pour vérifier la version :

```bash
./gradlew --version
```

Pour compiler et exécuter les tests :

```bash
./gradlew clean build
```

Pour démarrer Spring Boot :

```bash
./gradlew bootRun
```

Sous Windows :

```powershell
gradlew.bat bootRun
```

Pour produire le JAR :

```bash
./gradlew bootJar
```

Le résultat est placé dans `build/libs/`.

> Il est recommandé d’utiliser `./gradlew` plutôt que la commande globale `gradle`, afin d’utiliser la version du wrapper définie par le projet.

---

## 18. Particularités Gradle

Le driver de base de données est déclaré comme dépendance de compilation lorsque le code généré utilise directement des classes fournies par le pilote.

Exemple conceptuel :

```gradle
implementation(
    group: 'org.postgresql',
    name: 'postgresql',
    version: '...'
)
```

---

## 19. Sécurité

Lorsque `Spring Security - Session Authentication` est sélectionné, Genesis ajoute les éléments liés à la sécurité définis dans la configuration de sécurité du générateur.

---

# Partie II — Documentation développeur

## 20. Architecture de l’intégration Spring MVC dans Genesis

L’intégration est principalement déclarée dans :

```text
genesis-core/src/main/resources/data_genesis/yaml/frameworks-mvc.yaml
```

L’entrée Spring MVC correspond à :

```yaml
id: 6
languageId: 1
coreFramework: "Spring Boot"
name: "Spring MVC - Thymeleaf"
```

La configuration précise notamment :

- les dépendances ;
- les fichiers supplémentaires ;
- les modèles ;
- les repositories ;
- les services ;
- les contrôleurs ;
- les vues ;
- les chemins de sortie ;
- les paramètres utilisateur ;
- les fichiers Dashboard.

---

## 21. Dépendances Spring MVC

La configuration actuelle utilise notamment :

```text
spring-boot-starter-web
spring-boot-starter-thymeleaf
spring-boot-starter-data-jpa
spring-boot-starter-validation
spring-boot-starter-test
thymeleaf-layout-dialect
commons-text
lombok
hypersistence-utils-hibernate-63
```

Le template de build choisit ensuite la représentation Maven ou Gradle.

---

## 22. Classe principale générée

Genesis génère une classe :

```text
<ProjectName>Application.java
```

contenant un `main` Spring Boot standard avec `SpringApplication.run(...)`.

---

## 23. WebConfig

Genesis génère :

```text
config/WebConfig.java
```

Cette classe implémente `WebMvcConfigurer` et configure notamment :

- `LayoutDialect` ;
- les formatters de dates ;
- certains convertisseurs de types ;
- CORS.

---

## 24. Utilitaires

### 24.1 WebUtils

La classe contient notamment :

- création d’un objet `Sort` ;
- filtres textuels ;
- filtres d’intervalle ;
- gestion de `Duration` ;
- gestion de `Period`.

### 24.2 FileUtils

Elle centralise notamment :

- conversion de `MultipartFile` ;
- détection MIME ;
- construction des noms de fichiers ;
- opérations nécessaires aux preview/download.

### 24.3 CsvExportService

Service générique chargé de produire le contenu CSV.

---

## 25. Génération des modèles

Dans `frameworks-mvc.yaml`, la section `model` définit :

- imports ;
- annotations ;
- contenu des champs ;
- clés primaires ;
- clés composites ;
- validations ;
- chemin de génération.

Chemin généré :

```text
src/main/java/<group>/<project>/models
```

---

## 26. Génération des services

La section `service` définit les interfaces et implémentations des services.

Chemin principal :

```text
src/main/java/<group>/<project>/services
```

---

## 27. Génération des contrôleurs

La section `controller` génère les contrôleurs Spring MVC.

Chemin :

```text
src/main/java/<group>/<project>/controllers
```

Le template gère conditionnellement :

- les relations ;
- les fichiers ;
- les clés primaires simples ;
- les clés primaires composites ;
- les formulaires ;
- la validation ;
- l’export CSV.

Lors d’une évolution du contrôleur généré, il faut modifier le template Genesis et non le fichier Java produit dans un projet de test.

---

## 28. Génération des vues

La section `view` est configurée avec :

```yaml
viewTemplateEngine: "Thymeleaf"
viewExtension: "html"
```

Chemin :

```text
src/main/resources/templates/<entite>
```

Le layout principal se trouve dans :

```text
src/main/resources/templates/layout
```

---

## 29. Ressources frontend

Le template principal utilisé par Spring MVC se trouve sous :

```text
genesis-core/src/main/resources/data_genesis/templates/Template 1/
```

On y trouve notamment :

```text
LayoutViewTemplate.templ
DashboardViewTemplate.templ
assets/css/style.css
assets/js/lang.js
assets/i18n/fr.json
assets/i18n/en.json
```

---

## 30. Dashboard — architecture commune

Le code commun se situe principalement dans :

```text
genesis-core/src/main/java/org/labs/genesis/dashboard/
```

### 30.1 `model`

Contient notamment :

```text
DashboardConfiguration
DashboardPage
DashboardVisualization
DashboardLayout
DashboardField
DashboardFilter
DashboardSort
DashboardQueryOptions
```

### 30.2 `rules`

Contient notamment :

```text
DashboardConfigurationValidator
DashboardDataTypeResolver
DashboardMetadataValidator
DashboardStatisticResolver
DashboardStatisticRules
DashboardVisualizationRules
```

### 30.3 `query`

Contient notamment :

```text
DashboardQueryBuilder
DashboardQueryPlan
DashboardJoin
DashboardJoinResolver
DashboardQuerySelection
DashboardStatisticExpression
```

### 30.4 `generation`

Contient notamment :

```text
DashboardGenerationModelBuilder
DashboardGenerator
DashboardGenerationModel
DashboardPageGenerationModel
DashboardVisualizationGenerationModel
```

Cette couche reste générique et ne dépend pas directement de Spring MVC.

---

## 31. Dashboard — couche Spring MVC

Les éléments spécifiques à Spring MVC se trouvent dans :

```text
genesis-core/src/main/java/org/labs/genesis/dashboard/technology/springmvc/
```

avec notamment :

```text
SpringMvcDashboardGenerator
SpringMvcDashboardMetadataProvider
SpringMvcDashboardQueryRenderer
SpringMvcRenderedQuery
```

### SpringMvcDashboardQueryRenderer

Transforme un plan générique de dashboard en requête exploitable par le projet Spring MVC.

### SpringMvcDashboardMetadataProvider

Prépare les métadonnées nécessaires au template de génération.

### SpringMvcDashboardGenerator

Orchestre la génération des éléments spécifiques à Spring MVC.

---

## 32. Flux simplifié de génération du Dashboard

```text
Configuration utilisateur
        ↓
DashboardConfiguration
        ↓
Validation des règles
        ↓
DashboardGenerationModelBuilder
        ↓
DashboardQueryBuilder
        ↓
DashboardQueryPlan
        ↓
SpringMvcDashboardQueryRenderer
        ↓
SpringMvcDashboardMetadataProvider
        ↓
Template Spring MVC
        ↓
DashboardService + Controller + Vue + JavaScript
```

---

## 33. Grille du Dashboard

La disposition d’une visualisation repose sur :

```java
public class DashboardLayout {
    private int x;
    private int y;
    private int width;
    private int height;
}
```

Ces coordonnées permettent de conserver la disposition préparée dans le plugin lors de la génération web.

---

## 34. Plugin IntelliJ et prévisualisation

La prévisualisation graphique du dashboard dans le plugin repose principalement sur :

```text
genesis-intellij/src/main/java/org/labs/genesis/forms/ui/visualization/
```

avec notamment :

```text
GridCanvas
DashboardVisualComponent
VisualizationPanel
DashboardVisualizationMapper
```

### GridCanvas

Responsable de la grille, du positionnement, du drag & drop, du redimensionnement et du calcul des coordonnées.

### DashboardVisualComponent

Représente une visualisation dans la zone de conception.

### DashboardVisualizationMapper

Transforme le composant graphique configuré dans le plugin vers le modèle commun `DashboardVisualization`.

Lors d’une modification de la disposition du preview, il faut préserver la cohérence entre les coordonnées de grille du plugin et `DashboardLayout.x/y/width/height`.

---

## 35. Ajouter ou modifier un type de visualisation

Pour ajouter un type de graphique, plusieurs couches peuvent être concernées :

1. enum de visualisation ;
2. règles de compatibilité ;
3. modèle de configuration ;
4. renderer plugin ;
5. query builder si nécessaire ;
6. renderer technologique ;
7. template ou renderer JavaScript ;
8. tests.

Il faut éviter de mettre les règles métier d’une visualisation directement dans le template Spring MVC si elles sont communes à plusieurs technologies.

---

## 36. Outils de build

Les projets de build sont configurés dans :

```text
genesis-core/src/main/resources/data_genesis/yaml/projects.yaml
```

Le principe est que le framework fournit des métadonnées de dépendances génériques :

```text
groupId
artifactId
version
scope
```

et le projet Maven ou Gradle décide comment les sérialiser.

Il faut éviter d’ajouter une dépendance artificielle de `ProjectMetadataProvider` à Maven ou Gradle si l’information est générique.

---

## 37. Ajouter une dépendance Spring MVC

Pour ajouter une dépendance propre à Spring MVC :

1. ouvrir `frameworks-mvc.yaml` ;
2. trouver `name: "Spring MVC - Thymeleaf"` ;
3. ajouter la dépendance dans `dependencies`.

Exemple :

```yaml
- groupId: "org.example"
  artifactId: "example-library"
  version: "1.0.0"
  scope: "{{removeLine}}"
```

---

## 38. Modifier un fichier généré

Les fichiers Java supplémentaires sont principalement définis dans `additionalFiles`.

Pour modifier par exemple `WebConfig.java`, la modification doit être faite dans son bloc de génération.

Il ne faut pas modifier uniquement un projet de test déjà généré, car la modification serait perdue lors d’une génération suivante.

---

## 39. Modifier le CSS commun

Fichier :

```text
genesis-core/src/main/resources/data_genesis/templates/Template 1/assets/css/style.css
```

Avant d’ajouter une nouvelle classe :

- vérifier qu’une classe existante ne répond pas déjà au besoin ;
- réutiliser les variables du thème ;
- éviter les styles dupliqués ;
- supprimer les règles devenues inutilisées lorsque leur HTML a été supprimé.

Pour le dashboard, vérifier les usages dans `DashboardViewTemplate.templ`, `dashboard.js` et `style.css` avant de supprimer une classe.

---

## 40. Modifier les traductions

Fichiers :

```text
assets/i18n/fr.json
assets/i18n/en.json
```

Ajouter la même clé dans les deux fichiers.

Dans l’HTML :

```html
<span data-i18n="dashboard"></span>
```

---

## 41. Modifier la sécurité

La définition des mécanismes de sécurité est centralisée dans :

```text
genesis-core/src/main/resources/data_genesis/yaml/framework-securities.yaml
```

Toute ressource requise avant authentification doit être explicitement autorisée dans la configuration générée.

Par exemple, les traductions utilisées par la page de connexion nécessitent l’accès :

```text
/i18n/**
```

---

## 42. Résumé

L’intégration Spring MVC de Genesis permet de générer une application web complète reposant sur Spring Boot et Thymeleaf tout en restant compatible avec les mécanismes génériques du générateur.

Elle couvre principalement :

- génération JPA ;
- CRUD MVC ;
- validation ;
- relations ;
- recherche ;
- tri ;
- pagination ;
- fichiers ;
- export CSV ;
- internationalisation ;
- thème ;
- sécurité session ;
- dashboard automatique ;
- Maven ;
- Gradle.

L’architecture cherche à séparer les règles génériques de Genesis des adaptations propres à Spring MVC afin de faciliter l’évolution future vers d’autres technologies.
