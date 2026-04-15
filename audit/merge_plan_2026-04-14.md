# Plan de Fusion vers `dev`

**Date :** 14 Avril 2026
**Objectif :** Fusionner `dev-frontend-async-merge-integration` et `dev-dotnet-mvc` vers la branche principale `dev`.

---

## 1️⃣ Phase 1 : Intégration du Frontend dans `dev`

**Contexte :** La branche `dev-frontend-async-merge-integration` possède une avance directe sur `dev` (788 commits d'avance, 0 de retard). Son intégration devrait donc être naturelle et sans conflit de code intra-branche.

**Commandes à exécuter :**
```bash
git checkout dev
git pull origin dev
git merge dev-frontend-async-merge-integration
git push origin dev
```
> **Point d'attention :** Assurez-vous que les pipelines CI/CD concernant les tests unitaires frontend (qui ont été massivement introduits) passent au vert après ce push public.

---

## 2️⃣ Phase 2 : Synchronisation de `dev-dotnet-mvc` avec `dev`

**Contexte :** Une fois `dev` mise à jour avec le Frontend, `dev-dotnet-mvc` va accuser un lourd retard structurel. Il est **fortement déconseillé** de fusionner `dev-dotnet-mvc` directement dans `dev` sans l'avoir mise à niveau au préalable afin d'éviter de casser le projet global.

**Commandes à exécuter :**
```bash
git checkout dev-dotnet-mvc
git pull origin dev-dotnet-mvc
git merge dev 
# (Cette commande va générer des conflits)
```

---

## 3️⃣ Phase 3 : Typologie et Résolution des Conflits Identifiés

La fusion va déclencher de lourds conflits sur environ 10 à 15 fichiers fondamentaux. Voici comment aborder les résolutions :

### A. Fichiers de configuration transverses (`Resources / YAML / JSON`)
- `genesis-core/src/main/resources/data_genesis/yaml/constraint-queries.yaml`
- `genesis-core/src/main/resources/data_genesis/yaml/frameworks-mvc.yaml`
> **Point d'attention :** Ne laissez pas l'outil de merge automatique écraser vos nœuds. La branche front-end modifie énormément le dictionnaire de templates. Si des configurations `.NET` ont été ajoutées sur votre branche, choisissez d'**accoupler** les deux blocs (garder les ajouts des deux côtés).

### B. Moteur Core & Entités Java
- `org/labs/genesis/config/Constantes.java`
- `org/labs/genesis/connexion/model/TableMetadata.java`
> **Point d'attention :** Vous aurez à fusionner à la fois les nouvelles constantes injectées par le front-end et les modifications de types de données SQL induites par le dev dotnet (notamment pour SQL Server et Oracle). Privilégiez une relecture visuelle sur Intellij IDEA.

### C. Interface Utilisateur du Plugin (`genesis-intellij`)
- `FrontendConfigurationForm.java`
- `GenerationOptionWizardStep.java`
- `SpecificConfigurationWizardStep.java`
> **Point d'attention critique :** Le front-end a **intégralement refondu** certaines parties de l'assistant de configuration GUI. Résoudre textuellement le conflit risque de casser la logique d'Interface. L'idéal est d'accepter les modifications "Entrantes" (celles du Frontend) et de **ré-injecter manuellement** les composants de boutons ou de listes déroulantes dédiés au `.NET` qui étaient dans l'ancienne vue.

### D. Fichiers de Build
- `genesis-core/build.gradle.kts`
- `genesis-intellij/build.gradle.kts`
> **Point d'attention :** Gardez toutes les librairies ajoutées par l'une ou l'autre branche.

---

## 4️⃣ Phase 4 : Tests Post-Résolution

Une fois les conflits marqués comme résolus de votre côté, effectuez les vérifications suivantes sur `dev-dotnet-mvc` :
1. **Compilation complète :** Re-buildez le projet core `gradlew build` (ou équivalent Maven) pour vérifier la syntaxe Java.
2. **Exécution des tests (`GenesisCoreTest.java`) :** Les tests devront valider que le scaffolding Front et le générateur C# cohabitent sans conflits d'injection.
3. **Execution de l'IDE en Sandbox :** Lancez le plugin Genesis dans l'environnement de test IntelliJ. Le _wizard_ doit s'ouvrir sans exception et afficher la possibilité de scripter du Vue/Django et du .NET.

---

## 5️⃣ Phase 5 : Fusion Finale vers `dev`

Une fois que la branche `dev-dotnet-mvc` possède les ajouts Front-end, compile correctement et que le code .NET a été testé couplé à ce nouvel environnement, vous pouvez ramener le tout vers le dépôt principal :

```bash
git commit -m "chore: resolve conflicts with frontend features"
git push origin dev-dotnet-mvc

# Redirection vers dev pour propulser
git checkout dev
git pull origin dev
git merge dev-dotnet-mvc
git push origin dev
```
