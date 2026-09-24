beau boulot, maintenant, on va un petit peu nettoyer notre code car tout par dans tous les sens et ce n'est pas très optimale.

Le plan c'est de rendre le code simple, propre, reutilisable et ordonnée.

Simple veut dire que l'imbrication des codes ne doit pas être trop profonde, le nom des variables et fonctions et autres doit être intuitifs.

propre veut dire, respect de la responsabilité unique. les variables d'états doivent être concervé dans le fichier correspondant, une fonction correspond à une fonctionnalité exacte.

Reutilisable, si une même logique peut revenir plusieurs fois selon le contexte, alors il faut soit le transformer en component, soit en composable, soit en fonction, principe de DRY. Si le store d'une feature peu être reutiliser, alors à reutiliser

Ordonnée car les variables doivent être regroupper entre eux, les setter et getter d'une même entité, les fonctions métiers dans un fichiers


Documentation ciblée : Ajout de commentaires explicatifs (de préférence au format JSDoc au-dessus de la fonction) pour toute fonction dont la logique n'est pas immédiatement évidente. Les fonctions triviales (comme les simples setters) n'en auront pas besoin pour éviter de polluer le code.
Zéro emoji : Suppression de tous les emojis dans les commentaires, les logs (console.log), les messages d'erreur et les chaînes de caractères. Le code doit rester sobre et professionnel.