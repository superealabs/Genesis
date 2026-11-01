BUG
    URGENCE :
        onEnter ne se déclenche pas si on change d'étape depuis le stepper display (l'indicateur d'étape)

        un bug dans la génération (les données envoyées créent un état bloquant)
    tester la création de repository

    BUG BACKEND(
        .NET MVC nécessite une version de language que le backend ne fournit pas
    )

fonctionnalité globale(zone cible : generation & relation)
    Selection
        système de séléction multiple
            need :
                visibleArray
                last élément selected(defautl : first in array)
                limit
                Shift touch

            trois modes : 
                select (set selection to true)
                unselect (set selection to false)
                inverted (invert the current selection)

            on a une liste :
                - elément A(cochée)
                - élément B(non cochée)
                - élément C(Cochée)

    SearchBar
        Advanced SearchBar
            - searchBar Option
                - StartWith
                - endwith
                - contain
                - custom(use * no replace the unknow element, exemple : *car*white* => search all car that have the combination that contain car & white)
                - order(activate or not)

fonctionnalité ciblée :
    generation
        modification technique :
            - drag & drop the table & view
            - useAdvancedSearch(global)
            - useAdvancedSelection(global)

        Modification descriptif UX & UI :
            - Ajout d'un bouton advanced search, permet de faire des recherches personnalisée avec les options dites plutôt
            - Ajout de la fonctionnalité de séléction multiple (clic gauche élément de départ vers shif + clic gauche élément de fin, à maintenir)
                - cette fonctionnalité est associée à un dropdown contenant les options
                    - select
                    - unselect
                    - inverted
            - ajout de système de drag & drop(optionnelle)

    relation
        modification technique :
            - useAdvancedSelection(global)
            - remplacer le sytème de gestion de relation par assignation multiple
            - système qui permet de générer toutes les relations possibles d'un enfant à un parent et un inversement

        Rajouter une option pour créer un écran de multiple assignation :


        - l'affichage sera divisée en deux
            - en haut : système d'ajout de relation
                - on met le parent/enfant au dessus d'un tableau parent/enfant en fonction du si c'est parentFirst ou ChildFirst
                    au dessus, on a la séléction de parent en dropdown, à droite la séléction de (parentFirst, childFirst)

            - en bas : système de modification de relation
                - rajouter un switch qui passe en advanced selection
                - au dessus du tableau et de la barre de recherche, on a une ligne de model à deux colonnes : hasForm & isMandatory
                    - chacun utilise aussi les options de type de séléction (select, unSelect, inverted)
                    - ce modèle s'appliquera à toutes les lignes séléctionnée au clic du bouton assign
                - tableau de relation devient :
                    - nouvelle colonne : checkbox de séléction
                    - hasForm & isMandatory deviennent mandatory




- le rollBack de donnée(réinitialiser à la donnée d'origine pour la configuration de base de donnée)



- choisir le mode de génération dès le début
    - rajouter un set (associer avec le bouton créer un nouveau projet)



l'ajout d'une relation permet d'afficher le détail de fille :
    Créer une fonction qui permet de créer plusieurs relation


    Rajouter un bouton qui permet de rajouter toutes les relations d'un enfant/parent
        rajouter une fonctionnalité qui permet de set plusieurs colonnes en une fois




Absence de chargement


BUG :
    Quand on change de framework depuis la page de config, valeur par defaut du champs version du Framework est la première valeur du tableau de version de l'ancien framework






rajouter un raw par étape pour voir la forme des données envoyées


database ne contient pas de séléction mais on lui envoie directement databaseCOnfig :



- pull pour récupérer les modifications




















- Extension Host
    - vérification si le chemin existe vraiment(sinon, renvoyer une exception)
    - Correction des commandes possibles avec Genesis
    - gestion des exceptions à la fin du summary
        - si erreur, on revient au summary, sinon success

- wizard
    - comparé les données en sortie et celle envoyée
    - envoyer tout à la fin

- Repository Selection
    - Si passer repository select
        - corriger les données par defaut de l'objet gitConfiguration

- Frontend LayoutConfigView
    - système de prévisualisation des données de Favicon et Logo
    - identification de la manière d'envoyer les données frontends sachant qu'on envoie des .svg et des favicon

- Gestion de la réinitialisation de donnée
    - l'utilisateur revient à une étape de select, et passe au lieu de faire suivant(reinitialisation de toutes les données dépendantes)

- summary
    - gestion de l'affichage des summary pour les étapes sautées

- Corriger le filtre dans Framework(better UI et Filtre Fonctionnel)


- Scénario : (ok)
    l'utilisateur choisit un framework qui n'est pas un MVC
    il a terminé la configuration frontend, soudain il revient à framework et choisit un FrameworkMVC

    (la liste des frontends MVC diffère entre chaque FrameworkMVC qui sont eux même différents des frontends des frameworks headless)

    Exemple :
        - Spring MVC : Thymleaf
        - .NetMVC : Razor
        - Tous les framework Normal : Vue, React, Angular, etc

- Scénario : (ok)
    l'utilisateur choisit un frontend, le configure, puis revient à la séléction



