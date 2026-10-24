# Genesis API : Spring Boot et frontend séparé

Les outils de build acceptés pour les projets générés sont annoncés par
`GET /api/project/config/options`. Pour Spring Boot, utiliser `maven`.
Genesis lui-même se compile avec Gradle et Java 21.

## Sélection du frontend

Après la sélection du framework backend, charger les frontends disponibles :

```sh
curl http://localhost:8081/api/frontend/frameworks
```

Configurer le frontend avant le lancement :

```sh
curl -X POST http://localhost:8081/api/generator/frontend-config \
  -H 'Content-Type: application/json' \
  -d '{"frameworkId":3}'
```

Les identifiants actuels sont `1` pour Angular, `2` pour Vue et `3` pour React.
`languageId` et `port` sont optionnels ; les valeurs par défaut proviennent du
catalogue des frontends. Un langage incompatible ou un port invalide renvoie 400.

## Ordre de configuration

1. `POST /api/frameworks/1/select`
2. `POST /api/generation/modes/select` avec `{"mode":"GENERATE_NEW_PROJECT"}`
3. `GET /api/project/config/options`
4. `POST /api/generator/project-config`
5. `POST /api/generator/frontend-config`
6. `POST /api/database/test-connection`
7. `POST /api/generator/database-config`
8. `GET /api/tables_metadata_all`
9. `POST /api/generator/generation-parameters`
10. `GET /api/generator/profiles/{profileName}/relations`
11. `GET /api/generator/validate-relations`
12. `GET /api/generator/execution-plan`
13. `POST /api/generator/launch`

Les profils utilisent `model`, `dao`, `service`, `controller`. Les composants
frontend sont dérivés des composants backend sélectionnés. Pour PostgreSQL,
`databaseName` désigne la base et `schema` désigne le schéma ; le schéma est
conservé dans la configuration du projet généré.

Une réponse de génération réussie doit être suivie de la compilation Maven du
backend, de son démarrage et de requêtes sur ses routes, puis de `npm install`
et `npm run build` dans le dossier frontend. Le backend généré utilise un
contexte HTTP basé sur le nom du projet, indiqué dans son `application.yml`.

## Générations partielles

`selectedComponents` peut contenir n'importe quelle combinaison non vide de
`model`, `dao`, `service`, `controller`. Les composants métier non demandés ne
sont pas ajoutés automatiquement. Les fichiers de structure et les auxiliaires
nécessaires aux composants sélectionnés peuvent aussi être produits.

Le frontend reçoit les modèles si `model` est sélectionné et les services si
`service` est sélectionné. Les pages CRUD et les formulaires de relations ne
sont générés que lorsque les quatre composants backend sont réunis pour une
entité. La présence de modèles et de services seuls ne suffit pas à activer
ces écrans.

Un service ou contrôleur généré seul doit être intégré dans un projet qui
fournit déjà ses dépendances. Cette génération partielle ne constitue pas à
elle seule une application autonome et compilable sans modèles ou repository.

## Tests PostgreSQL existants

`database.TableMetadataTestPostgres` accepte les variables `GENESIS_PG_HOST`,
`GENESIS_PG_PORT`, `GENESIS_PG_DATABASE`, `GENESIS_PG_SCHEMA`, `GENESIS_PG_USER`
et `GENESIS_PG_PASSWORD`. Elles permettent de cibler une base de recette sans
modifier les identifiants dans le test.
