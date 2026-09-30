Bouton Ajouter
    - rajoute une nouvelle configuration, cette nouvelle configuration est directement Séléctionner
    - Si l'utilisateur a fait une selection
        - le contenu du tableau de configuration est
            - sont celle de la configuration séléctionnée
            - sinon les valeurs actuelle du tableau de configuration(dropdown/liste de checkbox/ etc etc)
    - cet ajout est directement sauvegardé
    Après un Add, effacer automatiquement le filtre de recherche pour que l'utilisateur voie immédiatement la nouvelle config.

Bouton Supprimer
    - supprime la configuration
    - La sélection va automatiquement vers l'élément le plus proche (comportement précédent)
    - cette suppression est directement sauvegardé

Rename :
    - renomme la configuration
    - effectue directement une sauvegarde après le rename

Move :
    MoveUp/MoveDown doivent naviguer dans la liste filtrée (filteredConfigs), pas dans la liste complète. Cela garantit que la surbrillance reste toujours visible à l'écran.
    MoveUp :
        - déplace la séléction vers le haut

    MoveDOwn :
        - déplace la séléction vers le bas

    Le scénario : L'utilisateur est sur le premier élément de la liste filtrée et clique sur MoveUp. Ou il est sur le dernier et clique sur MoveDown.
    Comportement actuel de useConfigurationManager : Rien ne se passe (la sélection reste sur le même élément).

Search :
    - cherche une configuration par son nom(recherche dynamique)

Selection :
    Séléctionner une configuration permet de charger le contenu de cette dernière :
        - peut varier(dropdown, checkbox, etc etc)

    Cliquer sur une selection permet de la déséléctionné
        - la déséléctionné réinitialise le tableau de configuration avec des valeurs par defaut(on va encore voir les valeurs par defaut)
