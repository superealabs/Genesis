Parfait OwO ! Voici une explication complète du fonctionnement des couleurs dans ton système.

---

## 🎨 Comment Tailwind v4 génère les classes de couleur

### 1. Le point de départ : `@theme`

Dans ton CSS, tu as défini :

```css
@theme {
    --color-bg-light:    hsl(0, 0%, 15%);
    --color-bg:          hsl(0, 0%, 10%);
    --color-bg-dark:     hsl(0, 0%, 5%);
    --color-text:        hsl(0, 0%, 95%);
    --color-text-muted:  hsl(0, 0%, 65%);
}
```

**Tailwind v4 détecte automatiquement** toutes les variables qui commencent par `--color-` et génère des utilities pour chacune.

---

## 2. La convention de nommage

Pour une variable `--color-{nom}`, Tailwind génère automatiquement :

| Variable CSS | Classes Tailwind générées |
|---|---|
| `--color-bg` | `bg-bg`, `text-bg`, `border-bg`, `ring-bg`, `fill-bg`, `stroke-bg`, `decoration-bg`, `placeholder-bg`, `caret-bg`, `accent-bg` |
| `--color-text` | `bg-text`, `text-text`, `border-text`, etc. |
| `--color-bg-light` | `bg-bg-light`, `text-bg-light`, etc. |

### Dans ton code :

```vue
<div class="bg-bg-light text-text">
```

- `bg-bg-light` → `background-color: var(--color-bg-light)`
- `text-text` → `color: var(--color-text)`

---

## 3. Les préfixes (utilitaires)

| Préfixe | Propriété CSS | Exemple |
|---|---|---|
| `bg-` | `background-color` | `bg-bg` |
| `text-` | `color` | `text-text` |
| `border-` | `border-color` | `border-text-muted` |
| `ring-` | `ring-color` (outline) | `ring-bg` |
| `fill-` | `fill` (SVG) | `fill-text` |
| `stroke-` | `stroke` (SVG) | `stroke-text` |
| `decoration-` | `text-decoration-color` | `decoration-text` |
| `placeholder-` | `::placeholder color` | `placeholder-text-muted` |
| `caret-` | `caret-color` (curseur input) | `caret-text` |
| `accent-` | `accent-color` (checkbox, radio) | `accent-bg` |

---

## 4. Les variants (états interactifs)

Tu peux combiner n'importe quelle classe avec des **variants** :

```vue
<button class="bg-bg-light hover:bg-bg-dark focus:bg-bg text-text">
```

| Variant | Déclencheur |
|---|---|
| `hover:` | Survole de la souris |
| `focus:` | Focus clavier/clic |
| `active:` | Clic maintenu |
| `disabled:` | Élément désactivé |
| `group-hover:` | Survole du parent `.group` |
| `dark:` | Mode sombre (pas utilisé ici, on a notre propre système) |
| `first:`, `last:`, `odd:`, `even:` | Position dans une liste |

### Exemple dans ton code :

```vue
<button class="bg-text text-bg hover:opacity-80">
```

- État normal : `bg-text` (fond = couleur texte)
- Au survol : `opacity: 0.8`

---

## 5. Les modificateurs d'opacité

Tu peux ajouter une opacité directement dans la classe :

```vue
<div class="bg-bg/50">  <!-- 50% d'opacité -->
<div class="text-text/75">  <!-- 75% d'opacité -->
<div class="border-text-muted/25">  <!-- 25% d'opacité -->
```

Cela génère :

```css
background-color: hsl(0 0% 10% / 0.5);
```

### Exemple pratique :

```vue
<div class="bg-bg/90 backdrop-blur-sm">
    <!-- Fond semi-transparent + flou d'arrière-plan -->
</div>
```

---

## 6. Les valeurs arbitraires

Si tu as besoin d'une couleur qui n'est pas dans ton `@theme`, tu peux utiliser des crochets :

```vue
<div class="bg-[#ff0000]">  <!-- Rouge pur -->
<div class="text-[hsl(200,50%,50%)]">  <!-- HSL custom -->
<div class="border-[rgb(255,255,255)]">  <!-- RGB custom -->
```

**Mais dans ton système, tu n'en as pas besoin** car tout passe par les variables `@theme`.

---

## 7. Analyse de ton code actuel

```vue
<div class="bg-bg-light p-4 rounded">
    <h1 class="text-lg font-bold text-text">Genesis</h1>
    <p class="text-text-muted mt-1">Texte secondaire muted</p>
</div>
```

| Classe | Signification |
|---|---|
| `bg-bg-light` | Fond avec `var(--color-bg-light)` |
| `p-4` | Padding 16px (1rem) |
| `rounded` | Border-radius par défaut |
| `text-lg` | Font-size large |
| `font-bold` | Font-weight 700 |
| `text-text` | Couleur du texte avec `var(--color-text)` |
| `text-text-muted` | Couleur avec `var(--color-text-muted)` |
| `mt-1` | Margin-top 4px |

---

## 8. Le bouton inversé

```vue
<button class="bg-text text-bg">
```

C'est un pattern classique pour créer un bouton avec **contraste maximal** :
- Le fond utilise la couleur du texte (`--color-text`)
- Le texte utilise la couleur du fond (`--color-bg`)

En mode sombre :
- Fond = blanc (95%)
- Texte = noir (10%)

En mode clair :
- Fond = noir (10%)
- Texte = blanc (95%)

**Résultat** : le bouton est toujours parfaitement visible, quel que soit le thème.

---

## 9. Ajouter de nouvelles couleurs

Si tu veux ajouter une couleur d'accent (ex: pour les erreurs, succès), ajoute dans `@theme` :

```css
@theme {
    /* ... tes couleurs existantes ... */
    
    --color-accent:        hsl(210, 100%, 50%);
    --color-accent-muted:  hsl(210, 50%, 70%);
    --color-error:         hsl(0, 100%, 50%);
    --color-success:       hsl(120, 100%, 40%);
}
```

Puis utilise-les :

```vue
<div class="bg-error text-white">Erreur !</div>
<button class="bg-accent hover:bg-accent-muted">Action</button>
<span class="text-success">Succès !</span>
```

---

## 10. Récapitulatif visuel

```
┌─────────────────────────────────────────┐
│  @theme (main.css)                       │
│  --color-bg: hsl(0, 0%, 10%)            │
│  --color-text: hsl(0, 0%, 95%)          │
└─────────────────────────────────────────┘
                 ↓
┌─────────────────────────────────────────┐
│  Tailwind génère automatiquement         │
│  • bg-bg → background: var(--color-bg)  │
│  • text-bg → color: var(--color-bg)     │
│  • border-bg → border-color: var(...)   │
└─────────────────────────────────────────┘
                 ↓
┌─────────────────────────────────────────┐
│  Dans ton template Vue                   │
│  <div class="bg-bg text-text">          │
└─────────────────────────────────────────┘
                 ↓
┌─────────────────────────────────────────┐
│  CSS final généré                        │
│  .bg-bg { background: hsl(0,0%,10%) }   │
│  .text-text { color: hsl(0,0%,95%) }    │
└─────────────────────────────────────────┘
```

---

## 💡 Astuce de pro

Pour voir **toutes les classes générées** par Tailwind, tu peux inspecter un élément dans les DevTools (`F12` ou `Ctrl+Shift+I` dans la webview) et regarder dans l'onglet **Styles**. Tu verras exactement quelle propriété CSS est appliquée.

Tu veux qu'on ajoute des couleurs d'accent (erreur, succès, info) pour Genesis ? ^w^