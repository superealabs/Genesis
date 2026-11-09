# Genesis Generator

Générateur de projets backend et frontend pour Visual Studio Code. Genesis permet aussi de générer uniquement le backend.

Son objectif est simple : faire gagner du temps aux développeurs en automatisant les tâches répétitives de création et de configuration d'un projet.

> **Statut : pré-production.** Les fonctionnalités et la compatibilité peuvent évoluer.

---

## Principe

1. Sélectionnez votre technologie backend.
2. Connectez votre base de données.
3. Choisissez, si besoin, votre frontend : Vue.js, React ou Angular.
4. Configurez votre dépôt Git, puis lancez la génération.

Tout se fait depuis une interface unifiée, pas à pas, qui centralise l'architecture technique, la base de données, le frontend et la configuration Git.

---

## Fonctionnalités

| Fonctionnalité | Description |
|---|---|
| **Assistant de configuration** | Interface pas à pas pour choisir le framework backend, le moteur de base de données, le framework frontend et les dépôts Git. |
| **Gestion des configurations** | Sauvegarde, export et réutilisation des profils de génération pour accélérer les futurs projets. |
| **Historique intelligent** | Mémorisation des frameworks, bases de données et frontends récemment utilisés pour un accès rapide. |
| **Interface personnalisable** | Adaptation au thème de VS Code ou utilisation de thèmes dédiés. |

---

## Compatibilité

### Backend

| Framework | Version | Type |
|---|---|---|
| Spring Boot | Java 21, 17 | REST Web API |
| ASP.NET Core | .NET 8 | REST Web API |
| ASP.NET Core | .NET 8 | MVC |

### Bases de données

Prises en charge pour chacun des frameworks backend ci-dessus :

| Moteur | Version |
|---|---|
| MySQL | 8 |
| PostgreSQL | 17 |
| Oracle | 19c |
| SQL Server | 2022 |

### Frontend

| Framework | Version |
|---|---|
| Vue | 3 |
| Angular | 17 |
| React | 18 |

### IDE

| IDE | Version |
|---|---|
| IntelliJ IDEA | 2026.1.3 |

---

## Commandes

Ouvrez la palette de commandes (`Ctrl+Shift+P` ou `Cmd+Shift+P`) et tapez **Genesis** pour accéder aux actions suivantes :

| Commande | Action |
|---|---|
| `Genesis : Home page` | Ouvre l'interface principale du générateur. |
| `Genesis: Réinitialiser les récents` | Efface l'historique des éléments récemment utilisés. |
| `Genesis: Réinitialiser les configurations` | Supprime les configurations sauvegardées. |

> **Remarque :** les deux commandes de réinitialisation sont aussi accessibles par des boutons dédiés dans la barre de titre du panneau Genesis lorsqu'il est actif.

---

## Paramètres de l'extension

Ces paramètres se trouvent dans les préférences de VS Code : recherchez **Genesis** dans les paramètres.

### Apparence

| Paramètre | Valeurs | Défaut | Description |
|---|---|---|---|
| `genesis.theme` | `genesis`, `vscode`, `mono` | `genesis` | Thème de l'interface. `vscode` suit le thème de l'éditeur, `mono` est monochrome. |
| `genesis.colorMode` | `auto`, `light`, `dark` | `auto` | Mode de couleur. `auto` suit VS Code. |

### Gestion des données

| Paramètre | Valeurs | Défaut | Description |
|---|---|---|---|
| `genesis.recentResetScope` | `all`, `frameworks`, `databases`, `frontends` | `all` | Portée de la réinitialisation des éléments récents. |
| `genesis.configResetScope` | `all`, `framework_technical-stack`, `framework_advanced-configuration`, `generator_generation-options`, `frontend_languages-configuration` | `all` | Portée de la réinitialisation des configurations sauvegardées. |

---

## Notes de version

### 0.0.1

- Version initiale de pré-production.
- Assistant de configuration et interface de base.
- Commandes et paramètres de réinitialisation des éléments récents et des configurations.