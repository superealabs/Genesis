
ce script sert à génerer les vue en icons.
webview-ui\scripts\generate-icons.mjs


il va :
    - récupérer les icônes svg dans : src/assets/ICONS
    - créer les templatesVue correspondant avec le nom : Icon + filename
    - déposer les templates dans : src/core/components/ui/icons

pour l'activer, lance cette commande depuis webview-ui
npm run generate:icons