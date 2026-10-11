# 🧑‍💻 Les Bonnes Pratiques du Développeur

---

## 1. 🏛️ Principes Fondamentaux

| Acronyme | Signification | Définition |
|----------|--------------|------------|
| **DRY** | *Don't Repeat Yourself* | Ne jamais dupliquer de logique. Chaque connaissance ou comportement doit avoir une **source unique et fiable** dans le code. Si tu copies-colles, c'est un signal d'alarme. |
| **KISS** | *Keep It Simple, Stupid* | La solution la plus simple est (presque) toujours la meilleure. Évite la sur-ingénierie. Un code lisible par un junior est un code réussi. |
| **YAGNI** | *You Aren't Gonna Need It* | N'implémente pas une fonctionnalité « au cas où ». Code uniquement ce qui est **nécessaire maintenant**, pas ce qui *pourrait* l'être un jour. |
| **SoC** | *Separation of Concerns* | Chaque module/classe/fonction doit gérer **une seule préoccupation**. La logique métier ne se mélange pas avec l'affichage ni avec l'accès aux données. |
| **LoD** | *Law of Demeter* | Aussi appelée « loi du moindre savoir ». Un objet ne doit parler qu'à ses **voisins directs**, pas aux amis de ses amis. Évite les chaînes `a.getB().getC().doSomething()`. |
| **CoC** | *Convention over Configuration* | Privilégier les **conventions** par défaut plutôt que de tout configurer manuellement. Exemple : un fichier nommé `UserController` gère automatiquement les routes `/user`. |
| **POLA** | *Principle of Least Astonishment* | Le code doit se comporter de la manière la plus **prévisible et intuitive** possible. Si un développeur est surpris par le comportement, c'est un mauvais signe. |
| **TDA** | *Tell, Don't Ask* | Dis à un objet **quoi faire** plutôt que de lui demander son état puis décider à sa place. Favorise l'encapsulation. |
| **POLE** | *Principle of Least Exposure* | N'expose que le **strict minimum** (variables, méthodes, API). Tout ce qui est interne doit rester privé. |

---

## 2. 🧱 Principes SOLID (Conception Orientée Objet)

| Acronyme | Signification | Définition |
|----------|--------------|------------|
| **S** – **SRP** | *Single Responsibility Principle* | Une classe ne doit avoir qu'**une seule raison de changer**. Elle ne fait qu'une seule chose, et elle la fait bien. |
| **O** – **OCP** | *Open/Closed Principle* | Le code doit être **ouvert à l'extension** mais **fermé à la modification**. On ajoute du comportement sans toucher au code existant (via héritage, interfaces, composition…). |
| **L** – **LSP** | *Liskov Substitution Principle* | Un objet fils doit pouvoir **remplacer** son parent sans casser le programme. Si `Canard` hérite de `Oiseau`, il doit pouvoir voler comme un oiseau. |
| **I** – **ISP** | *Interface Segregation Principle* | Mieux vaut **plusieurs petites interfaces** spécifiques qu'une grosse interface fourre-tout. Un client ne doit pas dépendre de méthodes qu'il n'utilise pas. |
| **D** – **DIP** | *Dependency Inversion Principle* | Les modules de haut niveau ne doivent pas dépendre des modules de bas niveau. Les deux doivent dépendre d'**abstractions** (interfaces). |

---

## 3. 🧪 Pratiques de Test & Qualité

| Acronyme | Signification | Définition |
|----------|--------------|------------|
| **TDD** | *Test-Driven Development* | Écrire le **test avant le code**. Cycle : 🔴 Red (test échoue) → 🟢 Green (test passe) → 🔵 Refactor (nettoyer). |
| **BDD** | *Behavior-Driven Development* | Extension du TDD où les tests sont écrits en **langage naturel** compréhensible par les non-techs (Given / When / Then). |
| **AAA** | *Arrange, Act, Assert* | Structure d'un test unitaire : **préparer** les données, **exécuter** l'action, **vérifier** le résultat. |
| **FIRST** | *Fast, Independent, Repeatable, Self-validating, Timely* | Les 5 qualités d'un bon test : **rapide**, **indépendant**, **reproductible**, **auto-vérifiable**, écrit **à temps**. |
| **CQRS** | *Command Query Responsibility Segregation* | Séparer les opérations de **lecture** (queries) des opérations d'**écriture** (commands) pour optimiser chaque côté indépendamment. |

---

## 4. 🔄 Architecture & Patterns

| Acronyme | Signification | Définition |
|----------|--------------|------------|
| **MVC** | *Model-View-Controller* | Séparation en 3 couches : **Modèle** (données), **Vue** (affichage), **Contrôleur** (logique de routage). |
| **MVVM** | *Model-View-ViewModel* | Variante de MVC où le **ViewModel** fait le pont entre la Vue et le Modèle via du data-binding (très utilisé en front). |
| **DDD** | *Domain-Driven Design* | Concevoir le logiciel autour du **métier** (domaine). Le code reflète le langage des experts métier (*ubiquitous language*). |
| **GRASP** | *General Responsibility Assignment Software Patterns* | Ensemble de 9 principes pour **assigner les responsabilités** aux objets (ex : Créateur, Contrôleur, Haute Cohésion, Faible Couplage…). |
| **HEX** | *Hexagonal Architecture* | Aussi appelée *Ports & Adapters*. Le cœur métier est **isolé** au centre et communique avec l'extérieur via des ports/adapters interchangeables. |
| **CLEAN** | *Clean Architecture* | Architecture en **couches concentriques** (Entités → Cas d'usage → Interfaces → Frameworks). La dépendance pointe toujours vers l'intérieur. |
| **EDA** | *Event-Driven Architecture* | Les composants communiquent via des **événements** asynchrones plutôt que des appels directs. Favorise le découplage. |

---

## 5. 🚀 DevOps & Déploiement

| Acronyme | Signification | Définition |
|----------|--------------|------------|
| **CI** | *Continuous Integration* | Intégrer (merger + tester) le code **plusieurs fois par jour** dans une branche commune pour détecter les conflits tôt. |
| **CD** | *Continuous Delivery / Deployment* | **Delivery** : le code est toujours prêt à être déployé. **Deployment** : le déploiement en production est **automatisé**. |
| **IaC** | *Infrastructure as Code* | Gérer l'infrastructure (serveurs, réseaux…) via du **code versionné** (Terraform, Ansible…) plutôt que manuellement. |
| **12FA** | *Twelve-Factor App* | 12 principes pour construire des **apps cloud-native** (config dans l'environnement, stateless, logs comme flux d'événements…). |
| **DORA** | *DevOps Research and Assessment* | 4 métriques clés de performance : **fréquence de déploiement**, **lead time**, **temps de restauration**, **taux d'échec**. |

---

## 6. 🔒 Sécurité (Bonnes Pratiques)

| Acronyme | Signification | Définition |
|----------|--------------|------------|
| **PoLP** | *Principle of Least Privilege* | Chaque utilisateur/processus ne doit avoir que les **permissions minimales** nécessaires pour fonctionner. |
| **ZT** | *Zero Trust* | Ne **jamais faire confiance** par défaut, même à l'intérieur du réseau. Toujours vérifier et authentifier chaque requête. |
| **OWASP** | *Open Web Application Security Project* | Référence mondiale des **failles de sécurité web** (Top 10 : injections, XSS, authentification cassée…). À connaître par cœur. |

---

## 7. 📐 Qualité du Code (Règles Pratiques)

| Règle | Définition |
|-------|-----------|
| **Boy Scout Rule** | *« Laisse le code plus propre que tu ne l'as trouvé. »* Améliore un peu à chaque passage. |
| **Rule of Three** | Si tu dupliques une logique **3 fois**, c'est le moment d'abstraire (refactorer). |
| **Fail Fast** | Détecte et signale les erreurs **le plus tôt possible** (validation en entrée, assertions, types stricts). |
| **Code Review** | Relire le code d'un pair avant merge. Détecte les bugs, partage la connaissance, maintient la cohérence. |
| **Meaningful Naming** | Les noms de variables/fonctions doivent **décrire l'intention**, pas l'implémentation. `daysUntilExpiration` > `d`. |
| **Small Functions** | Une fonction doit être **courte** et faire **une seule chose**. Si tu as besoin d'un commentaire pour expliquer *quoi*, renomme-la. |

---

## 🗺️ Résumé Visuel

```
🎯 Principes de base     → DRY, KISS, YAGNI, SoC
🧱 Conception OO         → SOLID, LoD, TDA
🧪 Tests                 → TDD, BDD, AAA, FIRST
🏗️ Architecture          → MVC, DDD, CLEAN, HEX
🚀 DevOps                → CI/CD, IaC, 12FA
🔒 Sécurité              → PoLP, ZT, OWASP
✨ Qualité au quotidien  → Boy Scout, Fail Fast, Code Review
```

---

> 💡 **Le fil conducteur de tout ça ?** Écrire du code **lisible, maintenable, testable et évolutif** — pour toi dans 6 mois, et pour tes collègues demain.