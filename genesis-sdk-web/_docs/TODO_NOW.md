- le système de set des étapes(revenir à une étape précise du formulaire)
    - gestion des exceptions(framework & backend)

- désactivation du bouton suivant tant qu'on est pas à la dernière étape du résumé

- créer un component :
    GenesisTutorial

        s'attache à l'élément si possible, sinon on fait en fixed
        utilise GenesisPopup 
        et un text, 
        et un slot avec un bouton ok dedans.

        Chaque vue possède un liste de cette dernière à parcourir dans l'ordre