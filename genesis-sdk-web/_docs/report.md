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


- Présentation
    - Créer la page de chargement
    - Créer la première page d'affichage
    - popup de séléction des tâches à faire
        - créer un nouveau projet
        - add new Rules
        - etc
    - 

- Steps
    - GenesisInput
        - amélioration du type color
        - amélioration du type number
    - GenesisCollection layout
        UI :
            - améliorer l'UI du tableau en se basant sur le 
            - amélioration de l'interface de détails
        UX :
            - fonctionnalité de compare
            - fonctionnalité de détail
    - Project Config
        UI :
            - TextArea Input : le texte semble bizarre quand c'est super long
            - disclosure backend advanced
                - modification de la size des inputs en lg
                - 

        Content :
            - Corriger les entêtes
    - Script Config
        UI : 
            LLM_ASSISTANT Panel need an optimization
    - Relation Parameter
        - remplacer le layout switcher en tableau de configuration mais provenant du résumé
        - comment rajouter un système de tri avec le peu d'élément d'interface ?





Summary
        Pour toute :             
            - rajouter une partie image à droite
            - modifier le curseur en pointer une fois sur une étape
            - il faut corriger le component resizable
            - rajouter un bouton edit qui permet de rediriger vers l'étape correspondante

            - si une étape est skippé, elle ne fait pas partie du résumer
        Conteneur
            UI
                - corriger la couleur des bg des step indicator non actif
        ProjectSummary
            UI
                - retirez les bg, les borders, les icônes et les rounded
                - jouer sur la hierarchie des titres des labels

        DatabaseSummary
            - UI
                - retirez les bg, les borders, les icônes et les rounded

        GenerationSummary
            - UI
                - créer un component GenesisContainerList qui utilise GenesisContainer pour lister le contenu du tableau.
                    un item dans GenesisContainerList à la partie gauche et la partie droite(les deux sont en slots)
                
                - utiliser le component GenesisContainer, peux être resizable
                - afficher le profil de génération en bas de GenesisConfigurationPanel
            - UX
                - correction de GenesisItemConfig et de GenesisConfigurationPanel, Normalement on peut séléction même en readonly
                - on affiche la liste des profils dans GeneratorData

                - séléction
                    - séléctionner un profil affiche uniquement les tables et vues assignées avec
                    - si aucune séléction, alors on affiche tout
        RelationSummary
            - UI

        FrontendSummary
            - UI


