# ─────────────────────────────────────────────────────────────
#  web-genesis — site vitrine (Nuxt 4)
#  Le site utilise le design system genesis-web-core (../../genesis-web-core) :
#  le contexte de build est donc le dossier genesis-sdk-web, pas web-genesis.
#
#    cd Genesis/genesis-sdk-web
#    docker build -f genesis-web-site-vitrine/web-genesis/Dockerfile -t genesis-web .
#
#  Lancé par genesis-web-site-vitrine/deploy/docker-compose.yml (service « web »)
#  Fichiers ignorés : Dockerfile.dockerignore (à côté de ce fichier)
# ─────────────────────────────────────────────────────────────

# 1) Dépendances + build
FROM node:22-alpine AS build
WORKDIR /src
RUN corepack enable

# Dépendances de genesis-web-core (son main.css importe « tailwindcss » depuis son dossier)
COPY genesis-web-core/package.json genesis-web-core/package-lock.json genesis-web-core/
RUN cd genesis-web-core && npm ci --no-audit --no-fund

# Dépendances du site (couche gardée en cache tant que les fichiers de verrouillage ne changent pas)
COPY genesis-web-site-vitrine/web-genesis/package.json \
     genesis-web-site-vitrine/web-genesis/pnpm-lock.yaml \
     genesis-web-site-vitrine/web-genesis/pnpm-workspace.yaml \
     genesis-web-site-vitrine/web-genesis/
RUN cd genesis-web-site-vitrine/web-genesis && pnpm install --frozen-lockfile

# Code : design system, types partagés, site
COPY genesis-web-core/src genesis-web-core/src
COPY genesis-web-types-shared/src genesis-web-types-shared/src
COPY genesis-web-site-vitrine/web-genesis genesis-web-site-vitrine/web-genesis

WORKDIR /src/genesis-web-site-vitrine/web-genesis
# Les fichiers envoyés (pages, images, charte) ne sont pas figés dans le build :
# ils vivent dans le volume « uploads ».
RUN mv public/upload /upload-seed \
 && pnpm build

# 2) Image finale : seulement le serveur Nitro + le contenu de départ des uploads
FROM node:22-alpine
WORKDIR /app
ENV NODE_ENV=production \
    HOST=0.0.0.0 \
    PORT=3000

COPY --from=build /src/genesis-web-site-vitrine/web-genesis/.output ./.output
# Contenu de départ du volume « uploads », recopié par Docker au premier lancement
COPY --from=build /upload-seed ./public/upload

RUN mkdir -p /work && chown -R node:node /app /work
USER node

EXPOSE 3000
CMD ["node", ".output/server/index.mjs"]
