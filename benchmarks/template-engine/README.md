# Optimisation de GenesisTemplateEngine

Le [rapport final](ADAPTIVE-INTEGRATION-2026-11-18.md) décrit le patch, sa référence
figée, les mesures sur cinq JVM par version et les limites de la comparaison.
La [synthèse condensée](adaptive-integration-2026-11-18-summary.json) conserve les
critères d'acceptation, les 24 cas de listes Vue et les résultats agrégés.

## Vérification fonctionnelle

Utiliser un JDK 21 (`JAVA_HOME`), depuis la racine :

```sh
./gradlew :genesis-core:test --tests engine.GenesisTemplateEngineTest --tests engine.GenesisTemplateEngineAdaptiveTest
./gradlew :genesis-core:classes
python3 benchmarks/template-engine/prototype/run_adaptive_study.py --prepare-only
./gradlew -I benchmarks/template-engine/benchmark.init.gradle :genesis-core:perRenderCompatibility -PreferenceClasses="$PWD/genesis-core/build/adaptive-study/reference/classes"
```

Les deux premiers tests couvrent les conditions, boucles, substitutions,
commentaires, erreurs, récursions et appels concurrents. La tâche différentielle
compare 1 908 sorties ou erreurs avec la référence figée compilée séparément.
Pour les tests frontend JWT sur macOS, utiliser
`JAVA_TOOL_OPTIONS=-Djava.io.tmpdir=/private/tmp` pour éviter l'alias `/var`.

## Mesures reproductibles

```sh
python3 benchmarks/template-engine/prototype/run_adaptive_study.py
```

Le runner prépare les classes de référence, vérifie leurs empreintes et lance
cinq JVM indépendantes par version, en alternant l'ordre. Il compare 198 cas de
templates réels et les 250 fichiers d'un lot avec écritures. Les listes Vue ont
au moins 500 ms d'échauffement et une seconde mesurée par cas. Les rapports bruts
sont écrits sous `genesis-core/build/reports/adaptive-study/`.

La référence historique est reconstruite depuis une révision Git figée, puis
vérifiée par SHA-256. Le script accepte aussi `--reference-source` et
`--reference-sha256` pour fournir explicitement une autre référence contrôlée.
Les gains documentés sont ceux de la campagne du 18 novembre, pas une mesure
contre une autre branche ou un classement universel.

FreeMarker 2.3.35 reste réservé au source set de benchmark : aucune dépendance
n'est ajoutée au produit. La comparaison distingue lecture + analyse + rendu et
template analysé réutilisé, sur une page complète à 5/20/50 champs, en édition et
lecture seule. Le reste du corpus n'est pas converti en FTL.

## Fixture fournie par Nomena

`GenesisTemplateEngineBenchmarkTest.java` conserve son comportement ; ses méthodes sont désormais commentées.
Les ressources manquantes ont été reconstituées à sa demande à partir du vrai
`ListViewTemplate.templ`. Le modèle contient des fragments dynamiques, différents
types de colonnes, des filtres et des tailles de page. Le FTL utilise les boucles
et conditions natives de FreeMarker ; certains fragments y sont intégrés.

```sh
./gradlew -I benchmarks/template-engine/benchmark.init.gradle :genesis-core:templateBenchmark
python3 benchmarks/template-engine/prepare_fixtures.py
```

La seconde commande régénère les deux fixtures ; elle n'est pas nécessaire aux
mesures habituelles. L'égalité FreeMarker est contrôlée après suppression de
l'indentation et des lignes vides. Le corpus Genesis exige l'égalité exacte ; le
lot avec écritures normalise uniquement le chemin temporaire.
