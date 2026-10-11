- reste à faire
    - Generator
        - liason de chaque composable au service correpondant pour chaque étape du wizard
        - test de génération EndToEnd avec Genesis API
    - UI/UX
        - UX
            - fonctionnalité de comparaison entre différentes entités
            - fonctionnalité de sauvegarde de configuration
                - stack_technique-Framework
                - configuration_avancée-Framework
                - option_generation-Generation_option
            - fonctionnalité supplémentaire pour les types d'affichages
                - affichage par section()
                - créer une section dédié pour les éléments générés récemment
        - UI
            - correction et amélioration visuelle de chaque étape du Wizard
            - amélioration de l'UI
            - correction des couleurs en fonction du thème
            - import des assets
            - optimisation de la gestion des unités de taille des différents composants UI
    - Maintenance
        - optimisation de la gestion du wizard
        - optimisation du code extension host
        - optimisation du code webview
        - optimisation du code web-core


- Normalement le chargement est gérer par useWizard, donc on corrige plus tard


# ajouter
Rajouter une image sur
    - ProjecConfig
    - DatabaseConfig

Modifier la valeur par defaut de l'affichage :
    RelationView : Grid -> Table

Modifier la structure des disclosures sur FrontEndConfiguration 11
    - associer Framework&Port et rajouter la langue
    - associer structure et navigation & charte graphique

modifier l'étape de git :
    - modifier l'interface pour que le hide -> devienne le disabled

Rajouter une étape de résumer

corriger l'interface de configuration de relation


DRY
UR



Corriger la gestion des étapes
    : un composant est associé à un Nom
    : un traitement est associé à un nom
    : un numéro est associé à un nom



Correction à apporter :
wizard-step-config.ts (La nouvelle source de vérité)
Chemin : genesis-sdk-web/genesis-web-core/src/features/generator/composables/wizard-step-config.ts
Action : Réécriture complète pour introduire les StepId, l'ordre WIZARD_STEP_ORDER, la configuration par nom, et les fonctions utilitaires d'adaptation (Nom ↔ Numéro).
useGenerator.ts (L'orchestrateur)
Chemin : genesis-sdk-web/genesis-web-core/src/features/generator/composables/useGenerator.ts
Action : Remplacer les constantes en dur (totalSteps: 10, SKIPPABLE_CONFIG) par les appels aux nouvelles fonctions utilitaires. Mettre à jour les hooks onStepEnter et onBeforeNext pour qu'ils traduisent le numéro d'étape en StepId avant d'exécuter la logique.
GeneratorStepper.vue (La vue)
Chemin : genesis-sdk-web/genesis-web-core/src/features/generator/components/GeneratorStepper.vue
Action : Supprimer la longue chaîne de v-else-if. Ajouter le dictionnaire STEP_COMPONENTS et utiliser un <component :is="..."> dynamique pour afficher la bonne vue en fonction de l'ID de l'étape courante.