actuellement, sdk web genesis prévoit 4 modules :
- genesis-web : la version web du projet
- genesis-vsc : la version extension Visual Studio Code du projet
- genesis-jcef-intellij : la version plugin intellij du projet
- genesis-mobile : la version application mobile du projet, c'est possible mais pour le moment, aucun intérêt mais à prévoir juste

Chacun de ces modules ont tous pour point commun l'interface graphique, les types, une partie des gestions de routes.
Il existe deux autres modules qui sont des modules partagés, il s'agit de :
- genesis-web-types-shared : contient tous les types partagés entre les modules pour une unification et une facilité de maintenance
- genesis-web-core : contient tous les components, vues, composables, stores et les contrats de service partagés entre les modules

Nota Bene : En suivant cette logique, le genesis-web-core regroupe toutes les fonctionnalités globalisés et similaire entre chaque module, cependant chaque module peut avoir une fonctionnalité/contrainte propre à cette dernière. Afin que vous compreniez les points de ruptures possibles, il vous est conseiller de regarder le schema de workflow.

Voici le workflow en partant d'une interface du module genesis vsc :

Chemin Vue vers Composables
App(vsc) -> router(core + vsc) -> views(vsc) -wrappe-> views(core) -emét un event-> views(vsc) -return data->

Chemin Composables vers store :
composables(core + vsc) -load/save/read/update/delete data-> store(core) 

Chemin Composables vers Interface(Core) :
composables(core + vsc) -> service.interface(core) -déclare les fonctions abstraites et la clés de service->

Chemin Inteface(core) vers Service(vsc + core) :
manifest(core) -expose les fonctionnalités importantes et une clé de service de la feature-> 
main(vsc) ->fournit les services correspondants à la clés de service -> service(core)

Chemin Service(vsc) vers ServiceExtensionHost :
Service(vsc) -postMessage(request)-> WebViewMessageRouter -> Service(ExtensionVSC) -postMessage(response)-> Service(vsc)