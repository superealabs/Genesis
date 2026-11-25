# Déployer le site Genesis sur un VPS (Docker)

Tout tourne dans Docker avec **une seule commande**. Le site ne prend **qu'un port local** sur le VPS (`127.0.0.1:8080` par défaut). Le Nginx (ou Traefik) déjà utilisé pour les autres sites du VPS envoie le nom de domaine vers ce port.

```
Internet ──HTTPS──▶ Nginx du VPS (déjà en place) ──▶ 127.0.0.1:8080
                                                        │
                    ┌─────────────── réseau Docker « genesis-site » ───────────────┐
                    │  gateway (nginx) ── /             → web (Nuxt, :3000)        │
                    │                  ── /siteback/     → siteback (:9000)        │
                    │                  ── /genesis-api/  → genesis-api (:8081)     │
                    │                                     db (PostgreSQL 16)       │
                    └──────────────────────────────────────────────────────────────┘
```
PostgreSQL, Siteback et genesis-api **ne sont pas joignables depuis Internet**. Seule la passerelle l'est, et seulement via le Nginx du VPS.

---

## 1. Ce qu'il faut sur le VPS

- Docker et Docker Compose v2 : `docker compose version`.
- Environ **3 Go de RAM libres** : deux applications Java, Nuxt et PostgreSQL.
- Un nom de domaine (ou un sous-domaine) qui pointe vers le VPS.
- Le projet **Genesis** complet : le site est dedans, à côté du design system qu'il utilise :
  ```
  /srv/genesis/Genesis/
  ├── genesis-core/, genesis-api/          (moteur de génération)
  └── genesis-sdk-web/
      ├── genesis-web-core/                (design system partagé avec genesis-vsc)
      ├── genesis-web-types-shared/
      └── genesis-web-site-vitrine/
          ├── web-genesis/                 (site Nuxt)
          ├── Siteback/                    (API du contenu)
          └── deploy/                      (ce dossier)
              └── genesis-api/genesis-api.jar   (à copier : voir ci-dessous)
  ```
- Le **jar de genesis-api**, compilé sur ton PC puis copié dans `deploy/genesis-api/` :
  ```powershell
  cd D:\stage genesis\Genesis
  .\gradlew.bat :genesis-api:bootJar
  Copy-Item (Get-ChildItem genesis-api\build\libs\*.jar | Where-Object Name -notlike '*-plain*').FullName `
            genesis-sdk-web\genesis-web-site-vitrine\deploy\genesis-api\genesis-api.jar
  ```
  Vérifie avant que `genesis-core.properties` ne contient pas ta clé Groq : elle serait dans le jar.
  La clé se donne plutôt dans `.env` (`GENESIS_LLM_API_KEY`).

## 2. Premier lancement

```bash
cd /srv/genesis/Genesis/genesis-sdk-web/genesis-web-site-vitrine/deploy

# 1) Configuration
cp .env.example .env
nano .env                       # mot de passe PostgreSQL, port libre, clé Groq

# 2) Mot de passe de l'administration (utilisateur « admin »)
printf "admin:$(openssl passwd -apr1 'MotDePasseAdmin')\n" > .htpasswd

# 3) Construction et démarrage (la première fois : 5 à 10 minutes)
docker compose up -d --build

# 4) Vérification
docker compose ps
curl -I http://127.0.0.1:8080/
```

Au premier lancement :
- PostgreSQL crée la base et exécute `web-genesis/database/schema.sql`, puis `data.sql` ;
- le volume `uploads` reçoit les pages HTML, les images et la charte présentes dans `web-genesis/public/upload/`.

## 3. Brancher le domaine (Nginx du VPS)

Copie `nginx/vps-exemple.conf` dans le Nginx du VPS, puis remplace le domaine et le port :
```bash
sudo cp nginx/vps-exemple.conf /etc/nginx/sites-available/genesis.conf
sudo nano /etc/nginx/sites-available/genesis.conf      # server_name et port 8080
sudo ln -s /etc/nginx/sites-available/genesis.conf /etc/nginx/sites-enabled/
sudo nginx -t && sudo systemctl reload nginx
sudo certbot --nginx -d genesis.exemple.com            # HTTPS
```
Si le VPS utilise **Traefik** ou **Caddy** à la place de Nginx, il suffit de faire pointer le domaine vers `127.0.0.1:8080` de la même façon. Demande à ton chef de projet ce qu'il utilise.

## 4. Reprendre le contenu de ton PC (facultatif)

Par défaut, le site démarre avec les données de `data.sql`. Pour reprendre **tes** navbars, pages, équipe et charte :

```bash
# Sur ton PC (PowerShell) : exporter la base
pg_dump -U postgres -d site_genesis --data-only --no-owner --no-privileges -f site_genesis_data.sql

# Sur le VPS : vider les données de départ puis importer
docker compose exec -T db psql -U genesis -d site_genesis -c "TRUNCATE t_content, t_navbar, t_equipe, t_charte_graphique RESTART IDENTITY CASCADE;"
docker compose exec -T db psql -U genesis -d site_genesis < site_genesis_data.sql

# Copier tes fichiers envoyés (pages HTML, photos, charte) dans le volume
docker compose cp ../web-genesis/public/upload/. web:/app/public/upload/
docker compose exec -u root web chown -R node:node /app/public/upload
```
`genesis` est l'utilisateur `POSTGRES_USER` de `.env`.

## 5. Ce qui est protégé

| Adresse | Accès |
|---|---|
| `/` et toutes les pages, `/playground` | libre |
| `/admin` | **mot de passe** (`.htpasswd`) |
| `/siteback/…` en lecture (GET) | libre (le site en a besoin) |
| `/siteback/…` en écriture (POST, PUT, DELETE) | **mot de passe admin** |
| `/api/upload`, `PUT /api/content-file` | **mot de passe admin** |
| `/api/folders` (choix d'un dossier du serveur) | **bloqué**. Le Playground ne propose que le .zip |
| `/api/generate` (.zip du Playground) | libre, **10 par minute** et par adresse IP, **une génération à la fois** |
| `/genesis-api/…` routes du Playground | libre |
| `/genesis-api/…` connexion à une base, SQL, Git, écriture sur disque, génération directe | **bloqué** (403) |
| Assistant IA (`generate-sql`) | **10 par minute** et par adresse IP |

Dans l'admin, le navigateur demande le mot de passe à l'ouverture de `/admin`. Il peut le redemander une fois au premier enregistrement : c'est le même mot de passe.

## 6. Mises à jour

```bash
cd /srv/genesis/Genesis && git pull      # ou recopier les fichiers
cd genesis-sdk-web/genesis-web-site-vitrine/deploy
docker compose up -d --build
```
La base (`pgdata`) et les fichiers envoyés (`uploads`) sont des **volumes** : ils sont gardés.
Une page modifiée dans le dépôt (`public/upload/navbar/…`) **n'écrase donc pas** celle du serveur. Le contenu se modifie depuis `/admin`.

## 7. Sauvegardes

```bash
# Base
docker compose exec -T db pg_dump -U genesis site_genesis > sauvegarde-$(date +%F).sql
# Pages, images, charte
docker run --rm -v genesis-site_uploads:/u -v "$PWD":/b alpine tar czf /b/uploads-$(date +%F).tar.gz -C /u .
```
À mettre dans un cron quotidien, et à copier hors du VPS.

## 8. Commandes utiles

```bash
docker compose logs -f web            # logs du site (aussi : siteback, genesis-api, db, gateway)
docker compose restart siteback
docker compose down                   # arrête tout (les volumes restent)
docker compose exec db psql -U genesis site_genesis
```

## 9. En cas de problème

| Symptôme | Cause probable |
|---|---|
| `bind: address already in use` sur 8080 | Le port est pris par un autre site : change `HTTP_PORT` dans `.env` et dans le Nginx du VPS |
| Pages vides, « Siteback injoignable » | `docker compose logs siteback` : mot de passe PostgreSQL faux, ou base pas encore prête |
| Le Playground affiche « genesis-api injoignable » | `docker compose logs genesis-api` : le jar manque dans `deploy/genesis-api/`, ou il a planté au démarrage |
| L'Assistant IA répond « aucune clé API » | `GENESIS_LLM_API_KEY` est vide dans `.env` (puis `docker compose up -d genesis-api`) |
| L'admin n'enregistre pas les fichiers | Droits du volume : `docker compose exec -u root web chown -R node:node /app/public/upload` |
| `502 Bad Gateway` juste après le démarrage | Les applications Java démarrent en 20 à 40 secondes : attends un peu |

## Fichiers de ce dossier

| Fichier | Rôle |
|---|---|
| `docker-compose.yml` | Les 5 services, leurs variables et leurs volumes |
| `.env.example` | Modèle de `.env` (port, PostgreSQL, dossier Genesis, clé IA) |
| `nginx/default.conf` | Passerelle : routes, mot de passe admin, blocages, limites |
| `nginx/vps-exemple.conf` | Bloc à mettre dans le Nginx du VPS |
| `.gitignore` | Ne pas versionner `.env` ni `.htpasswd` |

Les images sont construites à partir de `web-genesis/Dockerfile` (contexte : `genesis-sdk-web`, pour avoir genesis-web-core), `Siteback/Siteback/Dockerfile` et `deploy/genesis-api/Dockerfile` (il copie seulement `genesis-api.jar`, pas de Gradle dans Docker).
