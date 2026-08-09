# Plan de fusion et de nettoyage des branches vers `dev`

**Date de l'audit :** 10 juillet 2026

**Dernière mise à jour du plan :** 11 juillet 2026

**Références Git synchronisées :** `git fetch --prune origin` exécuté le 10 juillet 2026

**Branche d'intégration de référence :** `dev-frontend-async-merge-integration`

**Commit audité :** `cc57206192ef87b41c4a5d2b02cd53a072c627b5`

**Branche cible :** `dev` (`1ce841aae5fa237693f34acaaef7562934c7985e`)

## 1. Décision exécutive

Une seule branche doit être fusionnée directement dans `dev` :

```text
dev-frontend-async-merge-integration -> dev
```

À la date de l'audit, `origin/dev` est un ancêtre de la branche d'intégration. La branche d'intégration a **796 commits d'avance et 0 commit de retard** sur `origin/dev`.

Les anciennes branches fonctionnelles ne doivent pas être mergées directement dans `dev`. Les rares changements encore utiles doivent d'abord être réconciliés dans `dev-frontend-async-merge-integration`, testés, puis livrés dans le merge final.

La stratégie retenue est donc :

1. corriger les bloqueurs de build et de matrice de compatibilité sur la branche d'intégration ;
2. réconcilier sélectivement `dev-dotnet-mvc` ;
3. restaurer sélectivement Google OAuth2 si cette fonctionnalité est conservée ;
4. exécuter la matrice de tests issue du cahier Excel ;
5. fusionner `dev-frontend-async-merge-integration` dans `dev` par pull request ;
6. fusionner le `dev` final validé dans `groovy` et `feat/dockerize-java-dotnet` ;
7. supprimer les branches obsolètes seulement après merge, tag d'archive et vérification de la présence des commits dans `dev`.

## 2. Résultat de l'inventaire Git

L'inventaire porte sur **43 branches distantes** et **17 branches locales**.

| Catégorie distante | Nombre | Décision |
|---|---:|---|
| Branches protégées ou temporaires à conserver | 3 | `main`, `dev`, branche d'intégration jusqu'au merge final |
| Branches totalement contenues dans la branche d'intégration | 29 | suppression sûre après le merge final |
| Branches non ancêtres mais sans patch unique | 2 | suppression sûre après le merge final |
| Branches avec des patches uniques à réconcilier ou archiver | 6 | réconcilier ou archiver avant suppression |
| Branches avec des patches uniques conservées hors périmètre | 3 | `dev-apj-project`, `feat/dockerize-java-dotnet` et `groovy` |

Les compteurs `avance/retard` ci-dessous sont calculés par rapport à `origin/dev-frontend-async-merge-integration` après synchronisation des références distantes.

## 3. Branches à intégrer avant le merge final

### 3.1 `origin/dev-dotnet-mvc` — réconciliation sélective obligatoire

| Indicateur | Valeur |
|---|---:|
| Commits de retard | 283 |
| Commits d'avance | 20 |
| Patches non équivalents | 20 |
| Fichiers modifiés par rapport au point de divergence | 42 |
| Chevauchements détectés par `git merge-tree` | 25 fichiers modifiés des deux côtés + 1 fichier ajouté des deux côtés |

La branche d'intégration contient déjà :

- Java 21 ;
- IntelliJ Platform Gradle Plugin 2.16.0 et IntelliJ IDEA 2026.1.3 ;
- les générateurs ASP.NET Core REST et MVC ;
- `GenesisGenerateAction` et `GenesisWizardDialog` ;
- la création du `venv` Django ;
- les configurations frontend Angular, Vue et React.

Un merge brut de `dev-dotnet-mvc` risquerait donc de réintroduire les anciennes configurations Gradle/IntelliJ et d'écraser des évolutions frontend plus récentes.

Procédure :

1. créer une branche de travail depuis `dev-frontend-async-merge-integration` ;
2. examiner les 20 commits uniques un par un avec `git show` ;
3. ne reporter que les comportements absents ou meilleurs : corrections de contraintes SGBD, mappings C#, améliorations de templates MVC et jeux de données utiles ;
4. ne pas reprendre les anciens fichiers de build, les anciennes bornes IntelliJ ni les anciennes documentations ;
5. ajouter un test de non-régression pour chaque patch reporté ;
6. fusionner cette branche de réconciliation dans la branche d'intégration ;
7. taguer puis supprimer `dev-dotnet-mvc` et `det-dotnet-mvc-release`.

### 3.2 `origin/dev-spring-security-oauth2-google` — portage manuel recommandé

Cette branche a 922 commits de retard, 1 commit d'avance et un seul patch unique. Ce patch ajoute 21 lignes de documentation, mais la branche contient aussi le bloc complet `Spring Security - OAuth 2.0 (Google)` dans son état de fichier.

Le bloc OAuth2 Google n'est plus présent dans le `framework-securities.yaml` courant. Un merge classique du seul commit unique ne restaurera donc pas la fonctionnalité complète.

Décision recommandée :

- si OAuth2 Google reste dans le périmètre produit, reporter manuellement le bloc complet de sécurité et sa documentation dans le YAML courant, puis générer et compiler un projet Spring Boot de démonstration ;
- sinon, créer un tag d'archive et supprimer la branche en indiquant explicitement que cette capacité n'appartient pas au cahier Excel actuel.

Cette fonctionnalité n'est pas représentée dans le cahier Excel ; elle doit être traitée comme une extension transversale, pas comme une neuvième combinaison backend.

## 4. Branches distantes à supprimer

### 4.1 Suppression après le merge final — branches entièrement contenues

Les 29 branches suivantes sont des ancêtres de la branche d'intégration et n'apportent aucun commit manquant :

- `origin/dev-ai`
- `origin/dev-ai-fix`
- `origin/dev-ai-sql`
- `origin/dev-angular-fix`
- `origin/dev-demo-web`
- `origin/dev-django-module`
- `origin/dev-fix-trouble-oracle`
- `origin/dev-framework-caching`
- `origin/dev-frontend`
- `origin/dev-frontend-angular`
- `origin/dev-frontend-async`
- `origin/dev-frontend-async-merge`
- `origin/dev-frontend-mere-fille`
- `origin/dev-frontend-merge`
- `origin/dev-frontend-merge-fix-caching`
- `origin/dev-frontend-react`
- `origin/dev-multiProject-eureka`
- `origin/dev-nodejs-module`
- `origin/dev-spring-mvc`
- `origin/dev-spring-security-jwt`
- `origin/dev-springboot-multicriteria-improvement`
- `origin/feat/api-securisation`
- `origin/feat/export-java`
- `origin/feat/generate-controllers`
- `origin/feat/refactor-structure`
- `origin/feat/toNestJs`
- `origin/feat/unit-test`
- `origin/refactor-savePath`
- `origin/test-merge-cachhe`

### 4.2 Suppression après le merge final — branches sans patch unique

Ces branches ne sont pas des ancêtres directs à cause de leur topologie de merges, mais `git log --cherry-pick --right-only --no-merges` ne trouve aucun patch unique :

- `origin/dev-frontend-merge-django`
- `origin/feat/merge-cache`

### 4.3 Archiver puis supprimer — branches avec travail unique non retenu

| Branche | Patches uniques | Décision | Justification |
|---|---:|---|---|
| `origin/det-dotnet-mvc-release` | 1 | supprimer après réconciliation .NET | ancêtre de `dev-dotnet-mvc`; ne pas merger séparément |
| `origin/dev-disperse-configuration-file` | 7 | taguer puis supprimer | architecture de configuration dispersée obsolète; 330 fichiers et 16 chevauchements |
| `origin/feat/add-groovy` | 2 | taguer puis supprimer | Groovy/Grails absent du cahier Excel; contenu repris sous une autre forme dans `groovy` |
| `origin/fix-dev-frontend-merge-python` | 3 | supprimer | le `venv` Django existe déjà avec une option plus souple; la branche forçait sa création et retirait l'option UI |

`origin/dev-dotnet-mvc` et `origin/dev-spring-security-oauth2-google` rejoignent cette liste uniquement après la réconciliation décrite en section 3.

### 4.4 Branches distantes à conserver

- `origin/main` : branche principale protégée ;
- `origin/dev` : cible du merge ;
- `origin/dev-frontend-async-merge-integration` : conserver jusqu'à validation de `dev`, puis supprimer si aucune maintenance longue n'est prévue.
- `origin/dev-apj-project` : conserver et mettre de côté, sans l'intégrer au merge principal et sans synchronisation automatique depuis `dev` ;
- `origin/feat/dockerize-java-dotnet` : conserver comme branche Docker dédiée, puis y fusionner le `dev` final validé ;
- `origin/groovy` : conserver comme branche Groovy/Grails, puis y fusionner le `dev` final validé.

L'absence de Docker et Groovy/Grails dans le cahier Excel n'est pas un motif de suppression. Ces fonctionnalités restent hors de la recette du merge principal, mais leurs branches sont maintenues et doivent bénéficier de toutes les corrections consolidées dans `dev` après le merge final.

## 5. Nettoyage des branches locales

Les branches locales suivantes sont déjà contenues dans la branche d'intégration et peuvent être supprimées après avoir vérifié qu'aucun worktree ne les utilise :

- `dev-fix-trouble-oracle`
- `dev-framework-caching`
- `dev-frontend`
- `dev-frontend-angular`
- `dev-frontend-async-merge`
- `dev-nodejs-module`
- `dev-spring-mvc`
- `dev-spring-security-jwt`
- `dev-spring-security-oauth2-google`
- `dev-validation-mssql` dont l'upstream a déjà disparu

Attention : la branche locale `dev-spring-security-oauth2-google` n'est pas alignée sur sa branche distante (`ahead 84, behind 1`). Son tip local est déjà contenu dans l'intégration, mais le commit distant de documentation ne l'est pas. Utiliser directement `origin/dev-spring-security-oauth2-google` pour le portage.

Les branches locales suivantes ne doivent être supprimées qu'après archivage ou réconciliation :

- `det-dotnet-mvc-release` : 2 patches uniques, dont un commit local non poussé qui élargissait `untilBuild` à `255.*`; cette modification est déjà fonctionnellement dépassée par la borne courante `262.*` ;
- `dev-dotnet-mvc` ;
- `dev-disperse-configuration-file`.

Conserver `dev-apj-project` en l'état et hors du flux de merge. Conserver également `main`, `dev` et la branche d'intégration jusqu'à la fin de l'opération.

## 6. Périmètre fonctionnel du cahier Excel

Le fichier [`Cahier de caractéristique Genesis.xlsx`](./Cahier%20de%20caracte%CC%81ristique%20Genesis.xlsx) définit :

- 4 bases : MySQL, PostgreSQL, SQL Server, Oracle ;
- 4 langages : Java, TypeScript, C#, Python ;
- 8 backends : Spring Boot REST, Eureka Server, API Gateway, ASP.NET Core REST, ASP.NET Core MVC, NestJS, Django MVT et Django API ;
- 3 frontends : Vue, React et Angular ;
- 96 lignes de combinaisons théoriques, soit `4 × 8 × 3`.

La branche d'intégration déclare bien les 4 langages, les 8 backends et les 3 frontends. Cela ne signifie pas que les 96 combinaisons sont toutes exécutables.

Points à corriger avant d'utiliser les 96 lignes comme contrat de recette :

1. Eureka Server et API Gateway ont `useDB: false`; associer chaque SGBD à ces backends n'apporte aucun comportement différent.
2. Django ne supporte réellement que MySQL dans les dépendances Python actuelles. PostgreSQL n'a pas de package Python déclaré.
3. SQL Server et Oracle sont explicitement redirigés vers SQLite dans `ProjectMetadataProvider` pour Django.
4. Le template `requirements.txt` référence `${DBdriver}`, alors que `ProjectMetadataProvider` alimente `DBartifactId` et `DBversion`, pas `DBdriver`.
5. Les versions des providers Entity Framework sont absentes de `databases.json` et sont forcées à `${frameworkVersion}.0` dans le `.csproj`.
6. La feuille `Projects Combination` provient d'une formule Google Sheets `ARRAYFORMULA/SPLIT/FLATTEN`. Dans le fichier XLSX, elle est convertie en `__xludf.DUMMYFUNCTION`; les valeurs de repli décrivent les 96 cas, mais la formule n'est pas une source de test Excel portable.

Le cahier doit donc être considéré comme la **cible produit**, et non comme la preuve que toutes les combinaisons sont déjà supportées.

## 7. Versions des frameworks et outils supportés

### 7.1 Projet Genesis

| Composant | Version configurée |
|---|---|
| Java du projet | 21 (`sourceCompatibility` et `targetCompatibility`) |
| Gradle Wrapper | 9.5.1 |
| Kotlin | 1.9.24 |
| IntelliJ Platform Gradle Plugin | 2.16.0 |
| IDE de build du plugin | IntelliJ IDEA Ultimate 2026.1.3 |
| Compatibilité déclarée du plugin | builds `251` à `262.*` |
| Version du projet | 0.0.1 |
| JUnit BOM | 5.10.3 |
| Jackson | 2.18.1 |
| Lombok | 1.18.36 |
| JavaParser | 3.25.4 |

Le README est obsolète sur ces points : il annonce encore Gradle 8.11 et IntelliJ IDEA 2024.3.

### 7.2 Backends générés

| Backend | Versions déclarées | Défaut | Dépendances structurantes |
|---|---|---|---|
| Spring Boot REST | 3.3.6, 3.2.12 | 3.3.6 | Spring Data JPA, Web, Validation, springdoc 2.6.0, Flyway 10.19.0 |
| Spring Boot Eureka Server | 3.3.6, 3.2.12 | 3.3.6 | Spring Cloud 2023.0.3, Eureka Server |
| Spring Boot API Gateway | 3.3.6, 3.2.12 | 3.3.6 | Spring Cloud 2023.0.3, Gateway, WebFlux, Security/OAuth2 |
| ASP.NET Core REST | 8.0, 7.0, 6.0 | 8.0 | EF Core, Swashbuckle 6.9.0, Steeltoe 3.2.0 |
| ASP.NET Core MVC | 8.0, 7.0, 6.0 | 8.0 | EF Core, Cookie Authentication |
| NestJS REST | NestJS 10.x | 10.x | TypeORM 0.3.25, TypeScript 5.8.2, image Node 20.4 |
| Django API | 5.2.6, 4.2.7, 4.1.13 | 5.2.6 | DRF 3.15.2, drf-yasg 1.21.7 |
| Django MVT | 4.2.23, 4.2.24, 4.2.25 | **5.2.6 invalide car absent des options** | Django, mysqlclient 2.2.4 |

Langages générés : Java 17/21/23 (défaut 21), Python 3.8/3.10/3.12 (défaut 3.10), C# et TypeScript.

### 7.3 Frontends générés

| Frontend | Version principale | Versions associées |
|---|---|---|
| Angular | 17.3.x | Angular CLI/build 17.3.17, TypeScript 5.4.2 |
| Vue | 3.5.18 | Vite 7.0.6, TypeScript 5.8.x, Vue Router 4.5.1 |
| React | 18.2.0 | Vite 7.0.6, TypeScript 4.9.5, React Router 6.22.3 |

## 8. Bases de données et versions des drivers

### 8.1 Versions serveur documentées

| SGBD | Version qualifiée dans le README | Minimum technique indiqué dans l'audit du code |
|---|---|---|
| MySQL | 8.4.2 | 8.0.16 à cause des CTE, `CHECK_CONSTRAINTS` et `REGEXP_SUBSTR` |
| PostgreSQL | 15 à 16 | 9.3 à cause de `CROSS JOIN LATERAL` |
| SQL Server | 2022 | 2012 à cause de `TRY_CAST` |
| Oracle | 19c | 12.2 selon la famille `ojdbc8` utilisée |

Les minima techniques ne constituent pas une certification. Pour le merge, les versions qualifiées du README doivent servir de matrice de recette.

### 8.2 Drivers du runtime Genesis et des projets générés

| SGBD | Driver du runtime `genesis-core` | Driver Java généré | Driver Node généré | Driver Python généré | Provider .NET déclaré |
|---|---|---|---|---|---|
| MySQL | `mysql-connector-j` 9.0.0 | 9.0.0 | `mysql2` 3.11.0 | `mysqlclient` 2.2.4 | `Pomelo.EntityFrameworkCore.MySql` |
| PostgreSQL | `postgresql` 42.7.3 | 42.7.3 | `pg` 8.13.0 | **absent** | `Npgsql.EntityFrameworkCore.PostgreSQL` |
| SQL Server | `mssql-jdbc` 12.8.1.jre11 | 12.8.1.jre11 | `mssql` 11.0.1 | **absent; fallback SQLite** | `Microsoft.EntityFrameworkCore.SqlServer` |
| Oracle | `ojdbc8` 23.5.0.24.07 | `ojdbc8` 19.8.0.0 | `oracledb` 6.6.0 | **absent; fallback SQLite** | `Oracle.EntityFrameworkCore` |

Anomalies à résoudre :

- Oracle n'utilise pas la même version dans le runtime Genesis (23.5.0.24.07) et dans les projets Java générés (19.8.0.0) ;
- la classe Oracle configurée est l'ancien alias `oracle.jdbc.driver.OracleDriver` ;
- les providers .NET n'ont pas de version dans `databases.json`; le template leur impose 6.0.0, 7.0.0 ou 8.0.0 selon le framework, ce qui doit être validé fournisseur par fournisseur ;
- le `.csproj` de tests produit `net${frameworkVersion}.0`, donc `net8.0.0` avec le défaut actuel, alors que le TFM attendu est `net8.0`.

## 9. Bloqueurs avant fusion dans `dev`

| Priorité | Bloqueur | Critère de sortie |
|---|---|---|
| P0 | `./gradlew test` échoue sous JDK 21 : JUnit Platform Launcher absent du runtime de test | ajouter le launcher compatible avec le BOM JUnit et obtenir tous les tests verts |
| P0 | Java système courant = 8 alors que Gradle 9.5.1 exige Java 17+ | CI et documentation fixées sur JDK 21 |
| P0 | support Django/SGBD non conforme aux 96 combinaisons du cahier | définir les combinaisons réellement supportées ou ajouter les drivers/engines manquants |
| P0 | TFM du projet de tests .NET invalide (`net8.0.0`) | générer et compiler les projets de tests en .NET 6/7/8 |
| P0 | valeur par défaut Django MVT 5.2.6 absente des options | aligner options et défaut, puis tester la génération |
| P1 | versions Oracle divergentes | choisir une version unique compatible avec Java 21 et les serveurs qualifiés |
| P1 | providers EF Core non versionnés | déclarer une matrice provider/.NET/SGBD explicite |
| P1 | README obsolète | documenter Gradle 9.5.1, IntelliJ 2025-2026 et la matrice réelle |
| P1 | avertissements Gradle de fonctionnalités dépréciées | exécuter avec `--warning-mode all` et préparer Gradle 10 |

État observé pendant l'audit : la compilation Java de `genesis-core` et la compilation des tests passent sous JDK 21, mais l'exécution de `:genesis-core:test` s'arrête avant les tests faute de JUnit Platform Launcher.

## 10. Séquence d'exécution

### Phase A — sécurisation

1. geler les pushes sur les branches concernées ;
2. taguer les six branches à réconcilier ou à supprimer qui possèdent des patches uniques ;
3. relever les commits exacts de `dev-apj-project`, `feat/dockerize-java-dotnet` et `groovy`, sans les classer comme archives ;
4. créer une sauvegarde du tip de la branche d'intégration ;
5. vérifier les protections de `main` et `dev` ;
6. corriger les bloqueurs P0 sans intégrer d'ancienne branche.

### Phase B — réconciliation .NET

1. partir de `dev-frontend-async-merge-integration` ;
2. reporter les changements utiles de `dev-dotnet-mvc` par petits commits thématiques ;
3. conserver systématiquement les versions Gradle/IntelliJ et les générateurs frontend de la branche d'intégration ;
4. compiler les sorties ASP.NET Core REST et MVC pour .NET 6, 7 et 8 ;
5. valider MySQL, PostgreSQL, SQL Server et Oracle avec leurs providers EF Core explicites.

### Phase C — sécurité OAuth2

1. décider si Google OAuth2 fait partie de la release ;
2. si oui, reporter le bloc complet et la documentation ;
3. générer un projet Spring Boot, compiler le code et vérifier le flux de redirection avec des secrets factices de test.

### Phase D — recette du cahier Excel

1. transformer les quatre feuilles de référence en données de tests stables ;
2. distinguer les backends avec base de ceux sans base ;
3. générer chaque combinaison retenue ;
4. parser et compiler les artefacts Maven, .NET, npm et Django ;
5. tester au minimum un CRUD et une relation parent/enfant pour chaque couple backend/SGBD supporté ;
6. compiler Angular, Vue et React contre chaque famille d'API applicable.

### Phase E — merge final

1. mettre à jour la branche d'intégration depuis `origin/dev` et vérifier qu'elle reste à 0 commit de retard ;
2. exécuter `./gradlew clean test` sous JDK 21 ;
3. exécuter `./gradlew build` et `./gradlew genesis-intellij:buildPlugin` ;
4. ouvrir une pull request de `dev-frontend-async-merge-integration` vers `dev` ;
5. imposer revue, CI verte et merge commit traçable ;
6. vérifier le commit de merge sur `dev` avant toute suppression.

### Phase F — mise à niveau des branches Docker et Groovy

Cette phase commence uniquement lorsque le merge final est présent et validé sur `origin/dev`. Le SHA du merge final devient la référence minimale à retrouver dans les deux branches.

`dev-apj-project` reste volontairement de côté : ne pas y fusionner automatiquement `dev` dans le cadre de ce plan.

Pour conserver des périmètres distincts, mettre à niveau `groovy` et `feat/dockerize-java-dotnet` séparément. Bien que la branche Docker soit un ancêtre historique de `groovy`, ne pas la faire pointer vers `groovy`, car cela lui ajouterait le périmètre Groovy/Grails.

Procédure pour `groovy` :

```bash
git fetch --prune origin
git switch -c sync/groovy-with-final-dev origin/groovy
git merge --no-ff origin/dev
```

1. résoudre les conflits en conservant toutes les corrections finales de `dev` ainsi que le support Groovy/Grails et Docker propre à la branche ;
2. exécuter les tests Genesis sous JDK 21 ;
3. générer et compiler un projet Grails/Groovy ;
4. valider les fichiers Docker générés ;
5. ouvrir une pull request de la branche de synchronisation vers `groovy`.

Procédure pour `feat/dockerize-java-dotnet` :

```bash
git fetch --prune origin
git switch -c sync/docker-with-final-dev origin/feat/dockerize-java-dotnet
git merge --no-ff origin/dev
```

1. résoudre les conflits en conservant toutes les corrections finales de `dev` et uniquement les ajouts Docker de cette branche ;
2. générer les variantes Spring Boot, ASP.NET Core et NestJS concernées ;
3. construire les images et valider le démarrage des services ;
4. ouvrir une pull request de la branche de synchronisation vers `feat/dockerize-java-dotnet`.

Après merge des deux pull requests, les commandes suivantes doivent retourner 0 :

```bash
git merge-base --is-ancestor origin/dev origin/groovy
git merge-base --is-ancestor origin/dev origin/feat/dockerize-java-dotnet
```

Ces branches restent hors du merge principal vers `dev`. Leur mise à niveau intervient après la release afin qu'elles récupèrent tout le socle consolidé sans imposer Groovy ou Docker au cahier de recette actuel.

### Phase G — nettoyage

Pour chaque branche candidate :

```bash
git merge-base --is-ancestor origin/<branche> origin/dev
```

Supprimer directement si la commande retourne 0. Pour les branches archivées dont le contenu a été réimplémenté, vérifier aussi le tag et la pull request de réconciliation.

Exemples de commandes, à exécuter seulement après validation :

```bash
git tag -a archive/<branche>-2026-07-11 origin/<branche> -m "Archive avant nettoyage Genesis"
git push origin archive/<branche>-2026-07-11
git push origin --delete <branche>
git branch -d <branche>
git fetch --prune origin
```

Ne jamais utiliser `git branch -D` tant que la branche n'est ni ancêtre de `dev`, ni couverte par un tag d'archive et une réconciliation documentée.

Ne pas inclure `dev-apj-project`, `feat/dockerize-java-dotnet` ou `groovy` dans les commandes de suppression.

## 11. Critères de fin

Le plan est terminé uniquement lorsque :

- `dev` contient le commit final de la branche d'intégration ;
- les tests Gradle passent sous JDK 21 ;
- les projets générés compilent pour les versions de frameworks déclarées ;
- la matrice SGBD est honnête et testée, notamment pour Django et .NET ;
- les versions de drivers sont cohérentes entre runtime et artefacts générés ;
- le README et le cahier de caractéristiques décrivent le même périmètre ;
- les tags d'archive sont poussés avant la suppression des branches à travail unique ;
- `groovy` et `feat/dockerize-java-dotnet` contiennent le commit final de `dev` et passent leurs recettes dédiées ;
- `dev-apj-project` reste disponible et explicitement mise de côté ;
- `git branch -a` ne conserve que `main`, `dev`, `dev-apj-project`, `feat/dockerize-java-dotnet`, `groovy` et les autres branches réellement actives.
