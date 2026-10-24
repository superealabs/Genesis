- normaliser la gestion des Frameworks Layout durant le select(ok)
- corriger le nom des étapes(ok)



- le système de set des étapes(revenir à une étape précise du formulaire)
    - gestion des exceptions

- désactivation du bouton suivant tant qu'on est pas à la dernière étape du résumé


cliquer une deuxième fois sur le stetrail actuel permet d'acceder à une vue donnée ou la configuration
rajouter un bouton backToSummary(apparait uniquement quand l'utilisateur a atteint l'étape de summary, quand l'utilisateur arrive sur summary, on a une variable set en boolean true donc qui permet l'affichage du bouton à côté du bouton précédent)


Scénario :
l'utilisateur est arrivé au summary,
Le bouton backToSummary a maintenant le droit d'apparaitre dans toutes les autres vues.

l'utilisateur est à l'étape 1 du summary, si il clique une deuxième fois sur le stepRail, alors l'utilisateur est redirigé vers l'étape ProjectConfigView.

Si il clique sur backToSummary, on revient au résumé à l'étape exacte de la configuration du summary lié avec.

C'est à dire :


Framework_select & ProjectConfig sont lié à ProjectSummary

Si l'utilisateur fait back To summary depsuis Framework_select alors, on va vers le summary à l'étape de ProjectSummary

de même pour Database_select & Database_config sont lié à DatabaseSummary.

Ce dont on a besoin :
- une fonction pour set une step donnée
    - dans le wizard
    - dans le summaryView
- la gestion du deuxième clic
- un système qui permet de lié les étapes du wizard à un summary

quel est la portée des modifications à effectuer donc ?