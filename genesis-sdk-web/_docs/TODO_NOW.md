- le rollBack de donnée(réinitialiser à la donnée d'origine pour la configuration de base de donnée)



- choisir le mode de génération dès le début
    - rajouter un set (associer avec le bouton créer un nouveau projet)


Créer les endpoints pour clear les données api



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



