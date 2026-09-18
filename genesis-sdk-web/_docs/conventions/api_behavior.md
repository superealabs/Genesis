# step 1
- onMounted
    - chargement de la liste des Frameworks

    * Filtre
    - chargement de la liste des languages
    - chargement de la liste des architectures
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

- Notabene
    - Les inputs peuvent différés selon le framework choisit

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

- afterNext
    setCredentials

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
    setModification

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

    - chargement des languages disponibles en fonction du framework choisit

- afterNext
    - setFrontend()


Choix
    si il n'y a qu'une seulFrontEnd disponible, alors on saute l'étape de choix ici.

# step 9
- onMounted
    - chargement des languages disponibles
    - chargement des types de NavbarDisponible

- afterNext
    - setFrontEndConfig

# step 10
- afterNext
    - gitInformation
