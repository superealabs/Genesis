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


DRY
UR

- Présentation
    - très urgent(hors de portée)
    - Urgence
        - mettre en place le set des étapes(nécessite l'intervention de Nomena)
            - corriger le comportement onSkip step aussi(quoi faire durant le onSkip quoi)
                - Script Config View
            - Corriger l'interface de LLM Assistants
            - Gestion des exceptions

        - rajouter un bouton executer le script en dehors de la gestion des étapes

        - GenesisConfigurationPanel
            - rajouter un slot/prop de niveau Notice au dessus de chaque configuration(indiquateur de ce qu'il faut faire)
            - Ajouter un petit titre pour dire a quoi sert la partie configuration, genre dire "importer une configuration"
            - pour les base de donnée utilisée récemment, on devrait peut etre avoir ne serait ce qu'un petit texte dessous

        - GenesisPanel
            - Vitesse du slide de tutoriel(désactiver l'autoplay)

        - rajouter un système de gestion de multiple repo à distance
            - GitHub, GitLab

        - StepperPopup et GenesisStepper et Summary
            - désactiver le bouton générer tant qu'on est pas à la dernière étape

        - Database Configuration
            - A voir les notions de Pluggable Database de Oracle avec les versions compatible multitenant (C'est quoi)

        - Créer une nouvelle étape de séléction de Repo
            - mise en place d'un code en dure dans le genesis

        - Créer une nouvelle étape de configuration de Repo

        - Corriger le système de filtre dans framework
            - récupérer les vrai donner
            - faire fonctionner les filtres

        - Popup de détail :
            - Amélioration de L'ui nécessaire
            - Logo pas encore dans la fiche de details d'un projet
            - information de version(framework, base de donnée)

    - Secondaire
        - Créer la page de chargement
        - Créer la première page d'affichage
        - popup de séléction des tâches à faire
            - créer un nouveau projet
            - add new Rules
            - etc

        GenesisItem
            - Lisibilité des textes vert fluo sur vert pomme (comment fixé cela ?)
            - On ne sait pas si c'est fait de quel langage qu'en cliquant sur le grouper par langage, le mieux serait par exemple de mettre une petite pile avec le langage
            - quand on est en hover, on peut afficher une information(language F, D, FE)

        - Fiche projet: 
            - ajouter un indicateur de version language prise en charge  (ex: php 6-8)
            - ajouter un indicateur de version framework prise en charge  (ex: laravel 6-8)

            - ordonner l'etape d'import par script (plusieurs script? dump au lieu de script? a voir)

        - Gestion mèreFille
            - comment on fait pour deviner les filles d'une table mère

        - Summary
            - Project Summary
                augmenter l'espacement entre les étapes
            - Screen Summary Skipped

        - Gestion des étapes skippées

        - quand le projet est générer, vsc ouvre automatiquement le chemin indiqué

