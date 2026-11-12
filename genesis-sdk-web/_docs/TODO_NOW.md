Remplacer l'image :
la taille de l'image est à mettre en cover et non en taille-fixe


    - la fonction qui charge les tables parents et les tables enfants ne charge plus les tables avec des relations
    Exemple :
        Scénario à tester :
            - créer toutes les relations possibles avec la table mère personne
            - faire suivant
            - revenir et regarder la liste des tables mères


    Generation :
        Si on a une séléction à droite, cette séléction change de configuration


- RELATION
    - Si on assigne des relations, ensuite, on fait suivant, ensuite on revient,
    On désasigner les relation, on fait suivant, on cherche la relation dans le parent correspondant dans la liste des parents, l'entité removed n'apparaît pas

    - le champs de séléction de parent/enfant ne se réinitialise pas correctement dans le modeA après assignation/extinction du formulaire/sortie de l'étape

BUG
    - Quand on fait Ctrl + f5,
    et qu'on crée une nouvelle fenêtre VSC, et quand démarre l'api en parallèle, il y a 2/5 chances pour que le jar de l'extension ne démarre pas

    - Absence de nettoyage de ScriptConfig et de Database config

    - loadAll table/view ne fonctionne plus quand on switch entre les deux modes
        - gestion de l'état des deux modes

    - Relation
        Script mode n'a pas besoin de vérification de databaseConfig
        Afficher le nombre de relation

    


API :
    besoin d'une api pour réinitialiser les config de la base de donnée

Correction
    Mode Script Mode
    - retirer Script Config de l'étape de configuration(le mettre en tant que option plutôt que étape à suivre)
        - rajouter un bouton éditer à côté de l'import, ce bouton permettra d'ouvrir l'interface de SCriptCOnfig par dessus StepperPopup.

        Et là l'utilisateur configure ce qu'il veut, quand c'est ok, 
    Mode connexion
    - Si la connexion est Success et que l'utilisateur ne change aucune information du champs, on active le bouton d'édition de à côté de test connexion
    SI l'utilisateur change une information et n'a pas fais test(avec confirmation que la connexion est ok), on désactive le bouton d'édition











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



