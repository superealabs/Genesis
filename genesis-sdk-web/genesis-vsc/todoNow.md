Rajout de système de sauvegarde de configuration
    - stack_technique
        - information à sauvegarder :
            - nom de la configuration
            - language(dépendance de vérification)
            - framework(dépendance de vérification)
            - Version du language(donnée, dépend du framework)
            - Version du framework(donnée, dépend du framework)
            - hddl_auto(donnée, dépend du framework(optionnal))
            - build Tool(donnée, dépend du framework)

        une configuration = nom de configuration + une combinaison de stack_technique

    - configuration avancé
        - information à sauvegarder :
            - nom de la configuration
            - framework(dépendance de vérification)
            - niveau de logging(donnée, dépend du framework)
            - type de sécurité(donnée, dépend du framework)
            - fournisseur de cache(donnée, dépend du framework)

        une configuration = nom de configuration + combinaison de config avancée

    - option de Génération
        - information à sauvegarder :
            - nom de la configuration
            - liste des composants à générer

        une configuration = nom de configuration + combinaison de composant à générer



Fonctionnalité à configurer
    un composable pour simplifier l'utilisation de la configuration(navigation etc)
        pour rappel, une configuration est un nom + un objet(information de configuration) + configuration de quoi

    sauvegarde(sauvegarder la configuration actuelle dans la liste de configuration)
    import(importer des configurations dans la liste)
    export(est ce que c'est nécessaire ?)
    exportAll(est ce que c'est nécessaire ?)


Fonctionnalité de comparaison
 à faire

 correction de l'interface

 RAjouter un système de résumer