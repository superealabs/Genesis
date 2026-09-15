# 1. Builder le package partagé en premier
cd genesis-shared-types
npm install
npm run build       # génère dist/index.js + dist/index.d.ts

# 2. Puis installer dans les deux projets
cd ../genesis-web-core
npm install

cd ../genesis-vsc
npm install
npm run compile