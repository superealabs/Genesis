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
    * Java (17-23), Spring Boot (3.3.6, 3.2.12), Spring Web, Spring Actuator, Spring Test, Spring Data JPA, Maven (
      3.9.9), Swagger (springdoc OpenAPI Starter)

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
- **Gradle 9.5.1**
- **IntelliJ IDEA 2026.1.3** (requis pour le plugin)
    - Ultimate Edition de préférence, mais compatible avec Community Edition

## Cloner et Configurer le Projet

Le projet est structuré en plusieurs modules. Voici les étapes pour cloner et configurer le projet localement.

1. Clonez ce dépôt :
   ```bash
   git clone https://github.com/superealabs/Genesis.git

## Développement

Cette section explique comment configurer et lancer Genesis en environnement de développement.

### Lancer le plugin IntelliJ en mode développement

Pour démarrer une instance d'IntelliJ IDEA avec le plugin chargé depuis les sources :

```bash
gradle genesis-intellij:runIde
```

## Screenshots

Cette section présente les différentes étapes du processus de configuration et de génération d’un projet avec Genesis.

---

### Step 02 : Insertion des informations du projet et choix de la technologie

Dans un premier temps, l’utilisateur renseigne les informations générales du projet, notamment le nom de l’application ainsi que le répertoire de destination du projet généré.

L’utilisateur sélectionne ensuite l’environnement technologique à utiliser parmi les stacks prises en charge par Genesis :

- Spring Boot

**Screen :** `pic\2-step.png`

---

### Step 03 : Connexion à la base de données

L’utilisateur sélectionne le système de gestion de base de données à utiliser parmi les bases actuellement prises en charge par Genesis :

- MySQL
- PostgreSQL
- Oracle
- SQL Server

Il renseigne ensuite les informations nécessaires à l’établissement de la connexion avec la base de données, notamment :

- Le nom de la base de données
- Le nom d’utilisateur
- Le mot de passe
- Le schéma de données (facultatif)

**Screen :** `pic\3-step.png`

---

### Step 04 : Interaction avec l’IA pour effectuer une modification du schéma

Genesis intègre un système d’assistance basé sur l’intelligence artificielle permettant de générer automatiquement des scripts SQL afin de modifier ou faire évoluer la structure de la base de données selon les besoins de l’utilisateur.

**Screen :** `pic\4-step.png`

---

### Step 05 : Sélection des entités et composants à générer

L’utilisateur choisit :

### Les entités à inclure

### Les composants à générer :

- Model
- Repository
- Service
- Controller

**Screen :** `pic\5-step.png`

---

### Step 06 : Gestion des contraintes de relations entre entités

Genesis permet à l'utilisateur de gérer les contraintes relationnelles entre chaque entité.

Par défaut, les relations sont nullables.

**Screen :** `pic\6-step.png`

---

### Step 07 : Personnalisation des paramètres du front-end et de l’interface utilisateur

L'utilisateur peut choisir la technologie front-end à utiliser pour son application ainsi que les configurations minimales de ce dernier :

- Port
- Langage de développement

Pour les projets incluant une interface front-end, l’utilisateur peut personnaliser :

- La couleur principale
- Le positionnement de la barre de navigation
- Le logo
- La favicon
- Les langues à utiliser pour le projet

**Screen :** `pic\7-step.png`

---

### Step 08 : Configuration des paramètres du backend

Genesis permet également d’intégrer automatiquement certaines fonctionnalités avancées telles que :

- La sécurisation de l’application
- L’utilisation du cache
- La configuration d’Eureka Server pour les architectures microservices
- Le port du backend
- Les paramètres de l’ORM

**Screen :** `pic\8-step.png`

---

## Génération du projet

Une fois les paramètres configurés, Genesis lance automatiquement le processus de génération de l’application.