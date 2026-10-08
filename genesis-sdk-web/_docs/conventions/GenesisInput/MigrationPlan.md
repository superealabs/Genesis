Voici le document de plan de modification détaillé et précis. Il est structuré en 4 phases logiques pour garantir une transition fluide, sans régression, et activer le basculement automatique Dropdown/Combobox.

---

# 📄 Plan de Modification : Automatisation des Selects dans GenesisInput

**Objectif :** Remplacer l'approche par slots (verbosité, pas d'automatisation) par l'approche par props `:options` (concision, cohérence, basculement automatique vers Combobox si ≥ 10 éléments).

**Fichiers concernés :**
1. `genesis-sdk-web/genesis-web-core/src/core/components/ui/inputs/GenesisInput.vue`
2. `genesis-sdk-web/genesis-web-core/src/features/generator/components/steps/ProjectConfigView.vue`

---

## 🟢 Phase 1 : Consolidation du composant de base (`GenesisInput.vue`)
*Objectif : S'assurer que la logique de basculement automatique est active et prête à recevoir les données.*

**Actions :**
1. Vérifier que la constante de seuil est réglée sur une valeur de production :
   ```typescript
   const MAXIMAL_DROPDOWN_OPTION = 10; // (Au lieu de 2 utilisé pour les tests)
   ```
2. Vérifier que le `computed` `useComboboxForSelect` est bien présent :
   ```typescript
   const useComboboxForSelect = computed(() => {
       return props.type === 'select' && props.options && props.options.length >= MAXIMAL_DROPDOWN_OPTION;
   });
   ```
3. Vérifier que le `computed` `selectedOptionLabel` gère bien l'affichage du texte sélectionné à partir de la prop `options`.
4. Vérifier que la fonction `handleDropdownSelect` existe pour gérer le clic et la fermeture (`close()`) en mode Dropdown classique.

*(Si ces éléments sont déjà en place suite à nos échanges précédents, cette phase est validée).*

---

## 🟡 Phase 2 : Préparation des données dans `ProjectConfigView.vue`
*Objectif : Transformer les tableaux de chaînes de caractères plats (ex: `['v1', 'v2']`) en tableaux d'objets `{ label, value }` attendus par `GenesisInput`.*

**Actions :**
Dans la section `<script setup>` de `ProjectConfigView.vue`, ajouter les `computed` suivants juste après les autres `computed` existants :

```typescript
// Mapping des options pour le basculement automatique
const languageVersionOptions = computed(() => 
  availableLanguageVersions.value.map(v => ({ label: v, value: v }))
);

const buildToolOptions = computed(() => 
  availableBuildTools.value.map(t => ({ label: t.charAt(0).toUpperCase() + t.slice(1), value: t }))
);

const frameworkVersionOptions = computed(() => 
  availableFrameworkVersions.value.map(v => ({ label: v, value: v }))
);

const loggingLevelOptions = computed(() => 
  availableLoggingLevels.value.map(opt => ({ label: opt, value: opt }))
);

const securityTypeOptions = computed(() => 
  [{ label: 'Aucune', value: 'NONE' }, ...availableSecurityTypes.value.map(opt => ({ label: opt, value: opt }))]
);

const cacheProviderOptions = computed(() => 
  availableCacheProviders.value.map(opt => ({ label: opt, value: opt }))
);

const hibernateDdlAutoOptions = computed(() => 
  availableHibernateDdlAutoOptions.value.map(opt => ({ label: opt, value: opt }))
);
```

---

## 🟠 Phase 3 : Refactorisation du Template dans `ProjectConfigView.vue`
*Objectif : Supprimer les blocs `<template #default>` verbeux et utiliser la prop `:options`.*

**Actions :**
Pour **chacun** des 7 inputs concernés, remplacer l'ancien bloc par la version concise.

**Exemple concret de transformation (à appliquer à tous) :**

❌ **AVANT (À supprimer) :**
```html
<GenesisInput v-model="config.languageVersion" type="select" size="lg" label="Version du Language" placeholder="Sélectionner..." fill-width>
  <template #default="{ close }">
    <div class="p-1 space-y-1">
      <button v-for="v in availableLanguageVersions" :key="v" type="button" class="w-full text-left px-3 py-2 text-sm text-text hover:bg-[var(--color-hover-ghost)] rounded-md transition-colors" :class="{ 'text-accent font-medium': config.languageVersion === v }" @click="() => { updateConfig('languageVersion', v); close(); }">
        {{ v }}
      </button>
    </div>
  </template>
</GenesisInput>
```

✅ **APRÈS (À insérer) :**
```html
<GenesisInput 
  v-model="config.languageVersion" 
  type="select" 
  :options="languageVersionOptions"
  @update:model-value="(val) => updateConfig('languageVersion', val)"
  size="lg" 
  label="Version du Language" 
  placeholder="Sélectionner..." 
  fill-width 
/>
```

**Liste des 7 inputs à modifier exactement de cette manière :**
1. `config.languageVersion` ➔ utiliser `:options="languageVersionOptions"`
2. `config.buildTool` ➔ utiliser `:options="buildToolOptions"`
3. `config.frameworkVersion` ➔ utiliser `:options="frameworkVersionOptions"`
4. `config.loggingLevel` ➔ utiliser `:options="loggingLevelOptions"`
5. `config.securityType` ➔ utiliser `:options="securityTypeOptions"`
6. `config.cacheProvider` ➔ utiliser `:options="cacheProviderOptions"`
7. `config.hibernateDdlAuto` ➔ utiliser `:options="hibernateDdlAutoOptions"`

---

## 🔴 Phase 4 : Validation et Tests
*Objectif : S'assurer qu'aucune régression n'a eu lieu et que la nouvelle fonctionnalité opère.*

**Checklist de validation :**
- [ ] **Chargement initial :** Les selects affichent bien la valeur actuelle de `config` (ou le placeholder si vide).
- [ ] **Sélection :** Cliquer sur une option met bien à jour la valeur dans le store (`updateConfig` est appelé) et ferme le menu.
- [ ] **Basculement automatique (Seuil < 10) :** Si un tableau a moins de 10 éléments, l'interface affiche bien le `GenesisDropdown` classique (liste simple).
- [ ] **Basculement automatique (Seuil ≥ 10) :** Si un tableau a 10 éléments ou plus, l'interface affiche bien le `Combobox` avec la barre de recherche et l'icône de loupe.
- [ ] **Recherche :** Taper dans le Combobox filtre bien la liste en temps réel.

---

### 🛑 Prochaine étape
Si ce plan te convient et que tu valides cette approche, réponds simplement **"Validé, on y va"**. 

Je générerai alors immédiatement le code complet et prêt à être copié-collé pour `ProjectConfigView.vue` (Phases 2 et 3 combinées), en m'assurant que tout est parfaitement aligné.