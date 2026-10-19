C'est un excellent choix d'UX. Ce que tu décris est un pattern classique et très efficace appelé **LRU (Least Recently Used)**, que l'on retrouve partout dans les IDE modernes (comme les "Projets récents" dans IntelliJ ou VS Code). C'est beaucoup plus puissant qu'un simple snapshot, car cela offre un historique flexible à l'utilisateur.

Voici mes réponses à tes questions et mes remarques architecturales pour que cette fonctionnalité soit robuste.

---

### 1. Faut-il limiter le nombre d'éléments récents en mémoire ?

**OUI, absolument.** Et voici pourquoi :
* **Performance UI** : Afficher une liste de 50 éléments "récents" n'a aucun sens pour l'utilisateur et alourdit le rendu.
* **Propreté du stockage** : Même si le `globalState` est généreux, garder un historique infini est une mauvaise pratique.
* **Clarté cognitive** : L'utilisateur veut retrouver *rapidement* sa dernière habitude, pas fouiller dans un historique de 6 mois.

**👉 Recommandation :** Fixer une limite stricte, par exemple **`MAX_RECENT_ITEMS = 5`** (ou 10 maximum). Dès qu'un 6ème élément est ajouté, le plus ancien est automatiquement éjecté de la liste.

---

### 2. Mes remarques et points de vigilance (Edge Cases)

Pour que ce système soit infaillible, voici les règles que nous devons respecter :

#### A. Ce qu'on sauvegarde (Payload léger et sécurisé)
Nous ne devons **jamais** sauvegarder l'objet complet (surtout pour la DB). 
* **Pour les Frameworks / Frontends** : On sauvegarde `{ id: number, name: string }`.
* **Pour les Bases de données** : On sauvegarde uniquement le type de moteur (ex: `{ engine: 'mysql' }` ou `{ engine: 'postgre' }`). **Jamais** le host, l'utilisateur ou le mot de passe.

#### B. La validation des données (Data Drift)
* **Scénario** : L'utilisateur a utilisé un framework avec l'ID `42`. Entre-temps, ce framework a été supprimé du catalogue backend.
* **Risque** : L'élément "récent" pointe vers une donnée inexistante, ce qui peut faire planter le sélecteur.
* **Solution** : Au moment d'afficher la liste des "Récents", le système doit la croiser avec le catalogue actuel (`availableFrameworks`). Si un élément récent n'existe plus, il est **silencieusement filtré** de la liste affichée.

#### C. Le déclencheur de sauvegarde
La sauvegarde dans l'historique ne doit se faire **qu'au moment où la génération du projet est réussie** (`GenerationResult.success === true`). 
* *Pourquoi ?* Si l'utilisateur joue avec les sélecteurs mais annule la génération, on ne veut pas polluer son historique avec des choix abandonnés.

#### D. L'expérience Utilisateur (UI)
Au lieu de mélanger les éléments récents avec la liste principale (ce qui peut être confus), la meilleure pratique est d'avoir une **section dédiée "Récents"** tout en haut de la liste ou du dropdown, séparée par un petit titre ou une ligne de séparation.
* Exemple dans un dropdown : 
  * `🕒 Récents`
    * `MySQL`
    * `PostgreSQL`
  * `─────────────`
  * `Tous les moteurs`
    * `MySQL`
    * `PostgreSQL`
    * `Oracle`
    * `SQL Server`

---

### 3. Proposition de structure de données (TypeScript)

Pour gérer cela proprement dans le `globalState`, je propose d'avoir trois tableaux distincts (ou un objet regroupé) :

```typescript
export interface RecentItem {
    id: number | string; // ID du framework/frontend OU nom du moteur DB
    name: string;        // Nom affiché (ex: 'React', 'MySQL')
    lastUsed: number;    // Timestamp (Date.now()) pour trier si besoin
}

export interface GeneratorHistory {
    recentFrameworks: RecentItem[];     // Max 5 éléments
    recentDatabases: RecentItem[];      // Max 5 éléments (ici, id = engine string)
    recentFrontends: RecentItem[];      // Max 5 éléments
}
```

---

### 4. Algorithme de mise à jour (La logique "Sans Doublon")

Voici la logique exacte que nous implémenterons dans le composable de gestion d'historique :

```typescript
function addToHistory<T extends RecentItem>(
    currentHistory: T[], 
    newItem: T, 
    maxItems: number = 5
): T[] {
    // 1. On retire l'élément s'il existe déjà (pour éviter les doublons et le mettre à jour)
    const filteredHistory = currentHistory.filter(item => item.id !== newItem.id);
    
    // 2. On ajoute le nouvel élément au tout début du tableau (index 0)
    const updatedHistory = [newItem, ...filteredHistory];
    
    // 3. On coupe le tableau pour respecter la limite maximale (ex: 5)
    return updatedHistory.slice(0, maxItems);
}
```

---

### Prochaine étape

Si tu valides cette approche (Limite à 5, sauvegarde légère sans données sensibles, section UI "Récents" dédiée, et sauvegarde uniquement au succès de la génération), je peux te fournir :

1. L'interface `GeneratorHistory` à ajouter dans tes types partagés.
2. Le composable `useGeneratorHistory.ts` (avec les fonctions `addToHistory` et `getHistory`, prêt à utiliser l'adaptateur de stockage `globalState` dont on a parlé).
3. Les modifications à apporter dans les vues de sélection (Framework, Database, Frontend) pour afficher cette section "Récents" en haut de liste.

Qu'en penses-tu ? On part là-dessus ?