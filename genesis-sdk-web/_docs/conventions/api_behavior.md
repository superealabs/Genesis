# step 1
- onMounted
    - chargement de la liste des Frameworks

    * Filtre
    - chargement de la liste des languages
    - chargement de la liste des architectures(FrontEnd managed)
    - chargement de la liste des coresFramework
    - chargement des option d'intégration(Eureka)
    - chargement des templates Engine

- validator
    - choix du framework

- afterNext
    setFramework

# step 2
- onMounted
    - chargement des configs liés aux framework choisis. getConfigFramework()
    - chargement des loggings level
    - chargement des securityTypes(dépends du framework)
    - chargement des cacheproviders(dépends du framework ou du language)
    - chargement des buildTool(dépends du language ou du framework)

- Notabene
    - Les inputs peuvent différés selon le framework choisit (buildTool par exemple)
        - groupID
            - présent pour java
            - absent pour C#

        - Hibernate DDL Auto
            - présent pour springboot
            - absent pour C#
    
- validator
    - ProjectName
    - Location

- afterNext
    setProjectConfig

# step 3
- onMounted
    - chargement de la liste des bases de données

- afterNext
    setDatabase

# step 4
- onMounted
    - chargement des configs liés à cette base de donnée.

- capacité à tester la base donnée avec les credentials fournit sur le formulaire actuel
    payload sended to api :
        credentials

    payload received from api :
        exceptions or success message

- Notabene
    - MYSQL a besoin des champs : useSSLTificate, allowkeyRetrieval
    - ORACLE a besoin des champs : DriverName, SID

- toujours vérifier la connexion avant de continuer

- afterNext
    setDatabaseConfig

# step 5 (skippable)
- onMounted
    - chargement des models IA disponibles

- capacité à communiqué avec IA,
    payload request to api :
        prompt
        FileContent
        ShowSchema

    payload response from api :
        aiResponse
        NewFileContent

- afterNext
    setScriptMOdification

# step 6
- onMounted
    - chargement des tables et des vues disponibles
    - chargement des components disponibles

- afterNext
    - setConfigurationList()

# step 7
- onMounted
    - chargement des RelationsParameters

- afterNext
    - setRelationParameterList()

# step 8(skippable, if skipped, skip step 9 too)
- onMounted
    - chargement de la liste des frontEnds disponible en fonction du framework dans la mémoire
        - Si framework vide, on renvoie la liste de tout les frontends

    - chargement des languages(frontend) disponibles en fonction du framework choisit

- afterNext
    - setFrontend()


Choix
    si il n'y a qu'une seulFrontEnd disponible, alors on saute l'étape de choix ici.

# step 9
- onMounted
    - chargement des languages disponibles
    - chargement des languages de programmation disponible
    - chargement des types de NavbarDisponible

- afterNext
    - setFrontEndConfig

# step 10
- afterNext
    - gitInformation
