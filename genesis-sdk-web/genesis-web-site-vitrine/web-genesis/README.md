# Genesis — site vitrine (Nuxt 4)

Site vitrine de Genesis réalisé à partir du `TODO` : documentation, Playground, Ecosystem et About.

## Lancer le site

```bash
pnpm install
pnpm dev          # http://localhost:3000
```

Le site utilise deux backends :

| Backend | Rôle | Adresse par défaut |
|---|---|---|
| **Siteback** (Spring Boot, généré par Genesis) | Tables du site : `t_navbar`, `t_content`, `t_equipe`, `t_charte_graphique` | `http://localhost:9000/siteback` |
| **genesis-api** | Playground, listes de frameworks / bases | `http://127.0.0.1:8081/api` |

Base Siteback (PostgreSQL `site_genesis`) : exécuter `database/schema.sql` puis `database/data.sql`.

Pour que le **Playground** et les listes de frameworks fonctionnent, genesis-api doit tourner en local :

```bash
java -jar genesis-api/build/libs/genesis-api-1.0.0.jar   # http://127.0.0.1:8081
```

Autres adresses : `NUXT_PUBLIC_SITEBACK=http://hote:9000/siteback` et `NUXT_PUBLIC_GENESIS_API=http://hote:port/api`.
Sans l'API, les pages Docs / Ecosystem affichent leur contenu statique de secours.

Production : `pnpm build`, puis `node .output/server/index.mjs`.

## Organisation

| Dossier | Contenu |
|---|---|
| `app/pages/` | Une page par entrée du TODO (`docs/`, `ecosystem/`, `about/`, `playground.vue`, `admin.vue`) |
| `app/services/` | Services du TODO : `Navbar`, `Content`, `Equipe`, `CharteGraphique` (Siteback) et `Language`, `Framework`, `Database`, `Project`, `Script`, `Generate` (genesis-api) |
| `app/components/` | Topbar, footer, mise en page des docs, tableaux branchés sur genesis-api |
| `app/components/playground/` | Étapes du Playground (style genesis-vsc) |
| `app/composables/usePlayground.ts` | Logique du Playground et ordre des appels à genesis-api |
| `app/genesis-core/` | Copie de `genesis-sdk-web/genesis-web-core/src` (composants, CSS, polices, icônes et logos de genesis-vsc) |
| `app/assets/css/site.css` | CSS du site vitrine |
| `server/api/` | Fichiers : `POST /api/upload`, `GET/PUT /api/content-file` ; génération zip `POST /api/generate` ; dossiers `/api/folders` |
| `database/` | `schema.sql` (tables de Siteback) et `data.sql` (données du TODO) |
| `public/upload/navbar/` | Fichiers HTML des pages (chemins dans `t_content.fichier`) |

## Contenu des pages

Le texte de chaque page est un **fichier HTML envoyé** dans `public/upload/navbar/`.
Son chemin est enregistré dans `t_content.fichier` (ex. `/upload/navbar/docs-guide.html`).
L'accueil n'a pas de ligne dans `t_content` : son fichier est `/upload/navbar/accueil.html`.

Les photos (`t_equipe.photo`) vont dans `public/upload/images/`, la charte (`t_charte_graphique.file`) dans `public/upload/icon/`.

Page d'administration : **http://localhost:3000/admin** (pas de lien dans le menu) : CRUD via Siteback,
envoi de fichiers et éditeur HTML des pages avec aperçu.

## Playground

Génération **sans base de données** : choix du framework, configuration du projet, type de base,
script SQL (lecture des tables), relations (clés étrangères du script), frontend, résumé, puis génération.

Appels genesis-api dans l'ordre :
`DELETE /generator/reset` → `POST /generator/frameworks/{id}/select` → `POST /generator/script-database-config`
→ `POST /generator/project-config` → `POST /generator/frontend-config` (si frontend) → `POST /generator/script/launch`.

Le projet est écrit dans le dossier indiqué, **sur la machine qui exécute genesis-api**.
