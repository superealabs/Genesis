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





BUG INTERFACE :
    LLM_ASSISTANT Panel need an optimization




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
                - créer un component GenesisContainer, peux être resizable mais uniquement en y
                - créer un component List qui utilise GenesisContainer pour genesis et l'utiliser sur GenerationCOnfigView et GenerationSummary
                - afficher le profil de génération
            - UX
                - correction de GenesisItemConfig et de GenesisConfigurationPanel, Normalement on peut séléction même en readonly
                - on affiche la liste des profils dans GeneratorData

                - séléction
                    - séléctionner un profil affiche uniquement les tables et vues assignées avec
                    - si aucune séléction, alors on affiche tout
        RelationSummary
            - UI
                - retirer les borders, bg
                - remplacer les oui non par des icônes true et false
                - le tableau doit être resizable
        FrontendSummary
            - UI
                - retirer les les borders et le bg
                - corriger la couleur d'arrière plan