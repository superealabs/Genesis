## Le principe en une phrase

Une vue fournit deux choses au conteneur de panneaux : un **registre** (quels types de panneau existent) et une **disposition** (comment ils sont arrangés au départ). Le reste est géré par l'utilisateur à l'exécution.

```
Registre (editors)        Disposition (arbre)           Résultat à l'écran
┌─────────────────┐       ┌───────────────────┐
│ import          │       │ column 0.4        │         ┌────────┬─────────┐
│ code            │  +    │ ├─ row 0.3        │   →     │ import │         │
│ assistant       │       │ │  ├─ import      │         ├────────┤  code   │
│ guide           │       │ │  └─ guide       │         │ guide  │         │
└─────────────────┘       │ └─ code           │         └────────┴─────────┘
                          └───────────────────┘
```

Le lien entre les deux est l'`id` : chaque panneau de l'arbre contient l'`id` d'un éditeur du registre.

## 1. Définir les panneaux disponibles (le registre)

C'est un tableau de `PanelEditorDefinition`, dans `ScriptConfigView.vue` :

```ts
const editors: PanelEditorDefinition[] = [
  { id: 'import', label: 'Import de script', icon: IconFolder, component: ScriptImportPanel,
    props: { onRequestFilePath: (p) => emit('request-file-path', p) } },
  { id: 'code', label: 'Éditeur de code', component: ScriptCodePanel },
  ...
];
```

| Champ | Rôle |
|---|---|
| `id` | Identifiant unique, utilisé par la disposition et la sauvegarde. Ne le renomme pas à la légère : une disposition mémorisée qui pointe vers un `id` disparu affiche « Éditeur indisponible ». |
| `label` | Texte du menu déroulant et du déclencheur de l'en-tête. |
| `icon` | Optionnel. |
| `component` | Le composant affiché dans le panneau. |
| `props` | Optionnel. Passé tel quel au composant. Pour recevoir un événement, passe une fonction `onNomEvenement` (par exemple `onGenerate`). |

Tout ce qui est dans ce tableau apparaît dans le menu de **chaque** panneau. Ce que tu n'y mets pas n'est jamais proposé.

### Règle d'or : l'état ne vit pas dans le composant

Un même éditeur peut être ouvert plusieurs fois, et tous doivent se comporter pareil. Chaque composant d'éditeur doit donc lire et écrire son état dans un endroit partagé :
- le script est dans le store, via `useScriptFields()` ;
- le chat est dans un état commun, via `useLlmChat()`.

Chaque panneau garde seulement son point de vue : défilement, curseur. La position de défilement du contenu est sauvegardée automatiquement. Un éditeur qui a sa propre zone de défilement (comme CodeMirror ou le chat) appelle `usePanelView()` pour mémoriser le sien, ce qui est déjà fait dans `GenesisIdeCm` et `LlmAssistantPanel`.

### Ajouter un nouvel éditeur

1. Crée le composant, avec son état dans un store ou un composable partagé.
2. Ajoute une entrée dans `editors`.
3. C'est tout : il apparaît dans les menus.

## 2. Définir les panneaux par défaut (la disposition)

C'est un arbre, écrit avec deux constructeurs :

```ts
panelLeaf('code')                          // un panneau affichant l'éditeur « code »
panelSplit(direction, ratio, first, second) // une division en deux
```

| Paramètre | Signification |
|---|---|
| `direction` | `'column'` : deux colonnes côte à côte. `'row'` : deux lignes empilées. |
| `ratio` | Part du **premier** enfant, entre 0 et 1 (0.4 = 40 %). |
| `first`, `second` | Gauche et droite pour `column`, haut et bas pour `row`. Chacun est un panneau ou une autre division. |

Voici la disposition actuelle, passée à `useGenesisPanel` :

```ts
const panel = useGenesisPanel({
  defaultEditor: 'code',
  initialLayout: panelSplit(
    'column', 0.4,
    panelSplit('row', 0.3, panelLeaf('import'), panelLeaf('guide')),
    panelLeaf('code')
  ),
  persist: { load, save }
});
```

On la lit de l'extérieur vers l'intérieur : une division en colonnes, avec 40 % à gauche, le reste à droite pour le code. La partie gauche est elle-même divisée en lignes, avec 30 % en haut pour l'import et le reste pour le guide.

Pour changer le défaut, modifie `initialLayout`, par exemple pour ajouter l'assistant à droite :

```ts
panelSplit('column', 0.4,
  panelSplit('row', 0.3, panelLeaf('import'), panelLeaf('guide')),
  panelSplit('column', 0.65, panelLeaf('code'), panelLeaf('assistant'))
)
```

`defaultEditor` ne sert que de secours : si aucune disposition n'est fournie ni mémorisée (ou si elle est invalide), le conteneur démarre avec un seul panneau affichant cet éditeur.

## 3. Ce qui se passe à l'exécution

1. **Au démarrage**, `useGenesisPanel` cherche d'abord une disposition mémorisée (`persist.load`), puis `initialLayout`, puis `defaultEditor`. Une disposition mémorisée invalide est ignorée.
2. **Le composable** transforme l'arbre en rectangles (en pourcentage) pour chaque panneau et chaque diviseur. Le conteneur les affiche à plat, ce qui conserve le défilement quand l'arbre change.
3. **L'utilisateur modifie l'arbre** : il change l'éditeur d'un panneau par le menu déroulant, ferme un panneau (son voisin reprend la place), redimensionne un diviseur, ou divise avec `Alt` + glisser depuis un bord. Chaque action modifie l'arbre.
4. **Chaque modification est sauvegardée**, avec un court délai, dans le `localStorage` sous la clé `genesis:script-config-panels:v1`.

## Piège principal : la disposition mémorisée l'emporte

Une fois que l'utilisateur (ou toi, en testant) a touché aux panneaux, la disposition mémorisée remplace `initialLayout`. Si tu modifies le défaut et que rien ne change à l'écran, c'est probablement ça. Deux solutions :

- changer la clé (`v1` → `v2`), ce qui force le nouveau défaut pour tout le monde ;
- supprimer l'entrée dans les outils de développement, ou appeler `panel.reset()`.

## Dans une autre vue

Le schéma est toujours le même : ton registre (`editors`) et ta disposition (`initialLayout`), une clé de sauvegarde propre à la vue, et `<GenesisPanelContainer :panel="panel" :editors="editors" />`. Les autres options de `useGenesisPanel` sont `minWidth` et `minHeight` (taille minimale d'un panneau, 240 × 120 par défaut). Côté conteneur, `gap` règle l'espace entre les panneaux.