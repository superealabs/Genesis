# Genesis

Genesis donne vie à vos applications en générant rapidement des projets complets à partir de votre base de données,
idéal pour vos MVP.

## Pourquoi Genesis ?

Genesis simplifie le démarrage de vos projets en réduisant le boilerplate code et en vous permettant de générer des
applications prêtes à l'emploi – que ce soit des microservices, des applications MVC ou des front-end JS – grâce à une
approche Database First et son moteur de template intégré flexible.


## Technologies Supportées
Genesis permet de générer des projets utilisant diverses technologies selon les types de combinaisons :

### Types de Projets Générés

1. **Web API Spring Boot**

2. **Web API .NET**

3. **Web API Spring Boot + API Gateway**

4. **Web API .NET + API Gateway**

5. **Web API Spring Boot + Web API .NET + Eureka Server + API Gateway**


#### Technologies Transversales

* Pour les projets basés sur Spring (Types 1, 3, 5) :
  * Java (17-23), Spring Boot (3.3.6, 3.2.12), Spring Web, Spring Actuator, Spring Test, Spring Data JPA, Maven (3.9.9), Swagger (springdoc OpenAPI Starter)

* Pour les projets basés sur .NET (Types 2, 4) :
  * C# (8.0-9.0), Entity Framework Core, ASP.NET Core, Swashbuckle

* Commun aux projets avec API Gateway (Types 3, 4, 5) :
  * Spring Cloud (2023) Gateway Reactive Server, Spring Security

* Spécifique au Type 5 :
  * Spring Cloud Netflix Eureka Server & Client, Steeltoe


### Bases de Données Supportées

Genesis prend en charge les bases de données suivantes :

1. **PostgreSQL** : versions 15 à 16

2. **SQL Server** : version 2022

3. **Oracle** : version 19c

4. **MySQL** : version 8.4.2



## Prérequis Système

- **Java 21**
- **Gradle 8.11**
- **IntelliJ IDEA 2024.3** (requis pour le plugin)
    - Ultimate Edition de préférence, mais compatible avec Community Edition

**Disclaimer** : Si Gradle n'est pas installé localement, vous pouvez utiliser le wrapper Gradle en exécutant les
commandes via ./gradlew (ou gradlew.bat sur Windows) au lieu de gradle.

## Cloner et Configurer le Projet

Le projet est structuré en plusieurs modules. Voici les étapes pour cloner et configurer le projet localement.

1. Clonez ce dépôt :
   ```bash
   git clone https://github.com/yourusername/genesis-project.git
   ```
2. Ouvrez le projet dans IntelliJ IDEA.
3. Pour synchroniser les dépendances, exécutez la commande suivante :
   ```bash
   gradle build
   ```

### Structure Multi-Module

Le projet Genesis est un projet **multi-module**, avec les modules suivants :

- `genesis-core` : Ce module contient la librairie principale utilisée par les autres modules (CLI et plugin IntelliJ).
- `genesis-cli` : Ce module fournit l'outil en ligne de commande (CLI) pour générer des projets.
- `genesis-intellij` : Ce module est un plugin pour IntelliJ IDEA permettant d'intégrer Genesis directement dans l'IDE.

## Prise en main

### Utilisation en ligne de commande (CLI)

1. **Cloner, construire et lancer**
    - Clonez ce dépôt.
    - Exécutez `gradle genesis-cli:run` pour démarrer l'application.

2. **Build et exécuter le JAR**
    - Build le fichier JAR avec `gradle genesis-cli:build`.
    - Exécutez-le avec la commande suivante :
       ```bash
       java -jar genesis-cli-0.0.1.jar
       ```

3. **Télécharger et exécuter le JAR**
    - Allez sur la page des releases GitHub de ce projet.
    - Téléchargez la dernière version du fichier .jar.
    - Exécutez-le avec la commande suivante :
       ```bash
       java -jar chemin/vers/genesis-cli-0.0.1.jar
        ```

### Utilisation du Plugin IntelliJ

Le plugin IntelliJ permet d'utiliser Genesis directement dans l'IDE.

1. **Développement en live**
    - Clonez le dépôt.
    - Exécutez `gradle genesis-intellij:runIde` pour lancer une instance d'IntelliJ IDEA avec le plugin en mode live.

2. **Installation via le .zip : genesis-intellij.zip**
    - Construisez le projet via `gradle genesis-intellij:buildPlugin` pour générer le fichier
      `genesis-intellij/build/distributions/genesis-intellij.zip`.
    - Chargez ce fichier depuis une autre instance d'IntelliJ IDEA.

3. **Installation depuis JetBrains Marketplace**
    - Rendez-vous sur la JetBrains Marketplace ou ouvrez la section Plugins depuis IntelliJ IDEA (Ultimate ou Community
      Edition).
    - Recherchez « Genesis » et installez le plugin.

## Le module `genesis-core`

Le module `genesis-core` est au cœur du projet. Il contient la logique de base de génération de code et est utilisé à la
fois par le module `genesis-cli` pour l'outil en ligne de commande et par le plugin `genesis-intellij` pour
l'intégration dans l'IDE. Ce module comprend des fonctionnalités clés telles que la gestion des bases de données et la
génération de modèles de code.

## Obtenir de l'aide

Pour toute question, consultez notre documentation ou ouvrez une issue sur GitHub. Vos retours et contributions sont
toujours les bienvenus.

## Contribuer

Les contributions à Genesis sont encouragées ! N'hésitez pas à forker le dépôt, à soumettre des pull requests ou à nous
contacter pour partager vos idées et améliorations.

## Licence

Ce projet est sous licence [MIT](LICENSE).

---