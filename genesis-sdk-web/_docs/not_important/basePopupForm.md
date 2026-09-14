## Fonctionnalités actuellement contrôlables par props

| Prop | Type | Défaut | Contrôle |
|---|---|---|---|
| `title` | `string` | `undefined` | Affichage du titre dans le header |
| `isClosable` | `boolean` | `true` | Bouton X + clic sur l'overlay pour fermer |
| `draggable` | `boolean` | `true` | Déplacement du popup via le header |
| `resizableX` | `boolean` | `false` | Poignées de resize gauche/droite |
| `resizableY` | `boolean` | `false` | Poignée de resize bas |
| `size` | `'sm' \| 'md' \| 'lg' \| 'xl' \| '2xl' \| '3xl' \| 'full'` | `'md'` | Dimensions via sizeClasses |

## Fonctionnalités hardcodées qui devraient être configurables

| Fonctionnalité | Valeur actuelle | Prop suggérée | Type |
|---|---|---|---|
| **Position** | Toujours centré (`flex items-center justify-center`) | `position` | `'center' \| 'top-left' \| 'top' \| 'top-right' \| 'left' \| 'right' \| 'bottom-left' \| 'bottom' \| 'bottom-right'` |
| **Overlay** | Toujours présent (`bg-black/50`) | `showOverlay` | `boolean` |
| **Couleur overlay** | Hardcodé (`bg-black/50`) | `overlayColor` | `string` |
| **Padding overlay** | Hardcodé (`p-4`) | `overlayPadding` | `string` |
| **Z-index** | Hardcodé (`z-100`) | `zIndex` | `number` |
| **Padding contenu** | Hardcodé (`p-4`) | `contentPadding` | `string` |
| **Border radius** | Hardcodé (`rounded-lg`) | `borderRadius` | `'none' \| 'sm' \| 'md' \| 'lg' \| 'xl'` |
| **Fermeture ESC** | Non implémenté | `closeOnEscape` | `boolean` |
| **Fermeture clic overlay** | Lié à `isClosable` | `closeOnOverlayClick` | `boolean` (séparé de `isClosable`) |
| **Animation** | Non implémenté | `animated` | `boolean` |

## Résumé

**6 props existantes** contrôlent déjà les fonctionnalités principales.

**10 fonctionnalités hardcodées** mériteraient d'être configurables, avec les plus importantes étant :
1. `position` (pour les 9 positions du viewport)
2. `showOverlay` (pour désactiver l'overlay)
3. `closeOnEscape` (fermeture avec touche ESC)
4. `closeOnOverlayClick` (séparer du bouton X)

Tu veux qu'on commence par implémenter lesquelles en priorité ?