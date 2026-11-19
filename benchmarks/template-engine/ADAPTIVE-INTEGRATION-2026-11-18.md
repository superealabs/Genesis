# Moteur adaptatif — intégration et vérification

## Changement

Le moteur choisit son chemin après protection des commentaires et avant expansion
des boucles. Un rendu sans boucle et sans contexte parent utilise l'évaluation
historique des conditions. Un rendu avec boucle crée un contexte temporaire,
partagé avec ses récursions même si le corps ne contient pas de boucle interne.

Les structures de plans proviennent du prototype existant. Les maps sont créées
à la demande ; 128 plans au maximum sont conservés pendant l'appel. Les variables
restent prioritaires sur les conversions de littéraux mises en mémoire. Le
contexte est nettoyé en `finally`, après succès comme après erreur, et isolé par
thread. Les surcharges de la méthode publique `render` continuent à être appelées.

Une condition ne contenant ni `and` ni `or` passe directement dans
`evaluateSimpleCondition`, après le traitement existant de la négation. Les
autres conditions gardent les mêmes règles de découpage et de comparaison.

Le curseur de substitution ne repart plus au début du texte à chaque variable.
Il reprend un caractère avant le remplacement, pour détecter également `${...}`
formé par une insertion ou une suppression. Les placeholders imbriqués et les
deux passes existantes sont conservés.

L'analyse des plans revient à l'évaluateur itératif historique si les branches
sont mal formées ou si la profondeur atteint 128 niveaux. Ce repli évite un
débordement de pile sans rejeter des templates précédemment acceptés.

L'API, la syntaxe et les templates de production restent inchangés. Les changements
de production sont concentrés dans `GenesisTemplateEngine.java` ; FreeMarker
reste une dépendance du harnais, sans dépendance ajoutée au produit.

## Référence et reproduction

La référence a été copiée et compilée avant modification. Elle contient déjà le
patch des deux copies de suffixes, et son SHA-256 est :

```text
189b9dc68addc8dde90d88ab74fcadb832c22987ad16520786c2e8246ac5e12c
```

Depuis un JDK 21 :

```sh
./gradlew :genesis-core:classes
python3 benchmarks/template-engine/prototype/run_adaptive_study.py
```

Le runner vérifie les empreintes du moteur, du harnais, du JDK et des sources et
ressources de Core. Il compile séparément référence et candidat et vérifie la
provenance de la classe chargée. Après nettoyage du build, la référence peut être
reconstruite depuis une révision Git figée et le patch des suffixes ; son empreinte
doit rester identique. Un autre source de référence peut être fourni explicitement.

## Protocole d'acceptation

- Cinq JVM indépendantes pour référence, version adaptative et FreeMarker ; ordre
  des versions alterné et heap 512 Mio.
- 198 charges de corpus, dont 24 mesures de listes Vue : `List Component` et
  `List View`, 5/20/50 colonnes, édition/lecture seule, rendu final/deux passes.
- Pour les listes : échauffement d'au moins 500 ms et au moins une seconde dans
  les régions chronométrées, sans arrêt au plafond de 2 000 rendus.
- Égalité exacte des snapshots de corpus ; égalité des 250 fichiers générés après
  normalisation du chemin temporaire uniquement.
- Gain médian visé de 5 % sur chaque cas liste ; gain du panier global d'au moins
  1,5× par rapport à la référence. Agrégation : médiane des moyennes par JVM,
  puis somme à poids égal des rendus en deux passes.
- Tout ralentissement dépassant 5 % **et** 1 µs est confirmé dans cinq JVM
  supplémentaires neuves. Une moyenne favorable ne masque pas un cas défavorable.

Le harnais conserve les mesures brutes, durées d'échauffement, allocations par
thread, identifiants des classes et hashes des sorties. Les rapports se trouvent
dans `genesis-core/build/reports/adaptive-study/`. Les mesures restent locales et
ne constituent pas un classement universel des moteurs ni un test JMH.

Le lot avec écritures utilise les vrais générateurs Spring, .NET et Vue sur dix
entités de vingt colonnes. Il ne comprend pas JDBC, Git, Docker, LLM ou le build
des applications générées. Le test FreeMarker couvre les douze cas de la page
complète à 5/20/50 champs : lecture seule/modifiable et un usage/réutilisation.
Le reste du corpus n'a pas été réécrit en FTL.

## Résultats

La campagne finale termine avec `acceptancePassed=true` : cinq JVM indépendantes
par moteur, 198 charges, aucun cas dépassant simultanément 5 % de ralentissement
et 1 µs, aucun cas liste manquant la cible de gain de 5 %. Aucune confirmation
supplémentaire n'a donc été nécessaire. La référence et l'adaptatif produisent
les mêmes 198 snapshots et les mêmes 250 fichiers normalisés.

| Mesure | Référence | Adaptatif | Résultat |
| --- | ---: | ---: | --- |
| Panier des rendus en deux passes | 122,898 ms | 48,297 ms | 2,545× plus rapide |
| Allocations de ce panier | 74,99 Mo | 48,15 Mo | 35,79 % de moins |
| Lot de génération avec écritures | 533,719 ms | 352,399 ms | 1,515× plus rapide |
| Vue List Component, 20 colonnes, édition | 42,551 µs | 28,967 µs | 31,92 % de temps en moins |
| Vue List Component, 20 colonnes, lecture seule | 33,177 µs | 24,510 µs | 26,12 % de temps en moins |
| Vue List View, 20 colonnes, édition | 35,451 µs | 23,795 µs | 32,88 % de temps en moins |
| Vue List View, 20 colonnes, lecture seule | 30,915 µs | 21,823 µs | 29,41 % de temps en moins |

Les 24 cas de listes gagnent entre 25,76 % et 34,70 %. Sur le reste du corpus,
quelques variations de quelques dizaines de nanosecondes subsistent : le critère
est l'absence de régression dépassant les seuils convenus, pas une promesse que
chaque échantillon ou chaque template imaginable sera toujours plus rapide.

La première variante ne modifiait que le routage et les conditions simples. Son
essai a été interrompu avant acceptation : les gains de temps des listes étaient
insuffisants malgré une baisse des allocations. Le curseur de substitution a
ensuite été ajouté et la campagne finale est repartie à neuf, sans réutiliser les
mesures de cette première variante.

### Apache FreeMarker

Sur la page modifiable du benchmark à vingt champs, médiane des moyennes des
cinq JVM de chaque version :

| Scénario | Référence Genesis | Adaptatif Genesis | FreeMarker 2.3.35 |
| --- | ---: | ---: | ---: |
| Lecture + analyse + rendu | 2,576 ms | 1,031 ms | 4,601 ms |
| Template/source réutilisé | 2,580 ms | 1,000 ms | 0,044 ms |

Genesis adaptatif est environ 4,46× plus rapide dans le premier scénario de ce
cas ; FreeMarker est environ 22,6× plus rapide dans le second. Le coût des plans
adaptatifs reste inclus dans chaque rendu : aucun plan n'est conservé entre fichiers.

### Tests fonctionnels complémentaires

35 tests réussissent sur le moteur intégré : les 11 tests existants du moteur,
15 nouveaux tests adaptatifs, cinq tests de formulaires Vue parent/enfant et
quatre tests des ressources frontend JWT. Les deux tests différentiels du harnais
réussissent également, dont 1 908 comparaisons de sorties/erreurs avec la référence
figée, ainsi que les vérifications de concurrence et de surcharge récursive.

Les premiers essais JWT échouaient également avec la référence à cause de la
différence macOS entre `/var` et `/private/var` dans le répertoire temporaire.
La relance avec `JAVA_TOOL_OPTIONS=-Djava.io.tmpdir=/private/tmp` réussit sans
modifier les protections d'écriture ni le code de ces tests.

La synthèse condensée est `adaptive-integration-2026-11-18-summary.json`, à côté de
ce rapport. Les données brutes restent dans le dossier de build. Le comportement du fichier
Java initialement fourni par Nomena n'a pas été modifié ; des commentaires ont été ajoutés.

## Raccordement à `dev` — 19 novembre 2026

Branche créée depuis `dev` (`8f985625`). La résolution du conflit de stash conserve
exactement le moteur mesuré ci-dessus (SHA-256
`0524f7ad6543bc866ae5ad25610f84ac6c344a5dd18acbc9425effc3fefdaa1e`).
Elle rétablit aussi les helpers de délimitation des conditions imbriquées, absents
de cette version de `dev` mais présents dans la référence testée : ils sont
nécessaires aux plans conditionnels et au repli historique validés ensemble.
Les chiffres ci-dessus comparent la référence figée décrite dans ce rapport ;
ils ne constituent pas de nouvelles mesures contre le HEAD de `dev`.

Les prototypes abandonnés, journaux et rapports intermédiaires ont été retirés
du patch. Leur sauvegarde locale reste dans
`genesis-core/build/optimization-cleanup-2026-11-19/`, hors versionnement.

Après raccordement à `dev`, 29 tests disponibles dans cette branche réussissent
(9 historiques du moteur, 15 adaptatifs et 5 formulaires Vue), ainsi que les
2 tests différentiels et leurs 1 908 comparaisons avec la référence figée.
`dev` ne contient pas les tests JWT ni les deux tests moteur supplémentaires de
la branche d'étude : les 35 tests cités plus haut décrivent cette étude historique.
Le harnais de génération a été adapté en retirant sa référence à
`GenerationWriteScope`, classe absente de `dev`, sans modifier les générateurs.
Le lancement de contrôle du harnais sur cette branche réussit et produit 250
fichiers. Ce contrôle unique ne remplace pas une nouvelle campagne de mesures.

### Extraction des types utilitaires — 19 novembre 2026

`RenderContext`, les nœuds du plan conditionnel et `LoopInfo` sont maintenant
des types de package séparés. Leurs champs et leur durée de vie restent identiques.
Le runner compile ces sources dans la même sortie isolée que le moteur candidat.
Les empreintes et chiffres historiques ci-dessus décrivent le moteur avant cette
extraction ; ils ne sont pas présentés comme une nouvelle campagne de performance.
Les commentaires des tests précisent désormais leurs objectifs et leurs méthodes.

Après extraction : 29 tests moteur/Vue, les 2 tests différentiels (1 908
comparaisons), le lot de génération de 250 fichiers et le benchmark FreeMarker
réussissent. La compilation isolée via `--prepare-only` réussit également.
Cette relance valide le fonctionnement ; la campagne complète sur cinq JVM
n’a pas été rejouée pour ce déplacement de types et ces commentaires.
