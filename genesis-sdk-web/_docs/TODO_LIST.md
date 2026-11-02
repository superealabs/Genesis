CORRIGER LE BOUTON AddALl avec le mode B

BUG :
Vous passez de ASP.NET Core - MVC à Spring Boot - WEB API REST. Certaines données de la configuration seront réinitialisées.

Versions du langage et du framework, build tool
Configuration avancée (logging, sécurité, cache, Hibernate)

se déclenche même après la dernière génération



NOTE IMPORTANTE, on ne manipule que des tableaux d'élément visible


- Generation
    - créer un composable à multiple séléction
        récupère le tableau des éléments visibles
        permet de faire une séléction multiple, clic gauche(check box) sur le premier élément, clic gauche + Maj pour le dernier élément
            - tous les éléments entre ces deux séléctions sont impactée
                - une fonction s'occupe de l'impacte==selectMode(a un paramètre nullable : 'Select', 'UnSelect', 'Invert')
                    comportement par defaut Invert
                    Note, si tout les éléments du tableau sont séléctionnée, alors on déséléction et inversement

    - rajouter ce système sur le tableau de generation de la liste des tables
    - rajouter un bouton AddAll(assigne toutes les tables/vue visibles non assignées à la config actuelle) et un bouton RemoveAll(retire toutes les tables/vues assignées visibles de )


- Relation
    - créer une copie de Relation tableView avec les colonnes : checkbox, parent, child, hasForm(readOnly), isMandatory(readOnly)
    - créer une vue AdvancedRelationConfiguration 
        - Partie gauche (gestion de séléction de parent enfant)
            - modes a(on cherche à rajouter quelques relation d'un parent/enfant à quelques enfants/parents)
                - l'utilisateur cherche à rajouter que quelques relation parent-enfant, pas toutes
                - donc il séléctionne un parent/enfant et séléction les enfants/parents associés avec
                    - le bouton add au centre permet de rajouter cette selection suivant le modèle d'assignation (dans la partie à droite avec hasForm et isMandatory)
            - modes b(on cherche à rajouter toutes les relation de plusieurs parents/enfant en même temps)
                - l'utilisateur cherche à rajouter toutes les relations possibles de plusieurs parent/enfants en une fois
                - donc il séléctionne les parent/enfant et en cliquant sur le bouton add,
                    il rajoute toutes les relation enfant/parent possible liés à chaque parent/enfant séléctionnée
        - au centre (bouton : Add All, add, remove, remove All)
            (note, si relation déjà existante, on les liste toutes et on mets une confirmation côté utilisateur que ces relations existe déjà dans la relation, simple avertissement)
            - le bouton Add & Add All permettent d'ajouter les séléction de la partie gauche en relation suivant le modèle de relation dans la partie droite
            - le bouton remove & remove All permettent de retirer les séléction de la partie droite
        - à droite (gestion d'assignation de relation multiple)
            - partie 1(modèle de relation)
                - contient les inputs
                    - hasForm (rajouter un mode de selection plus tard)
                    - isMandatory (rajouter un mode de selection plus tard)
                - à droite des inputs, rajouter le bouton assign qui permet d'assigner ce modèle de relation au éléments séléctionnée de la partie 2

            - partie 2(liste des relations en selection Only via checkbox)
                - c'est la liste des relations avec la copie de relationTableView

