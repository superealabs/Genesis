- trouver un moyen de faire en sorte que GenesisCard et GenesisRow affiche la même information et devient un seul et unique composant(OK)


- comparer simple selection et dropdownList.


- DRY
    - SimpleSelectionPopup
    - BasePopup
    - GenesisDropdownList


- modification de BasePopup(utilisé pour faire le choix des petites options)
    - suppression du header
    - suppression de la position fixe
    - nécessite une position d'apparition
    - fermable avec un clic extérieur
    - n'est pas resizable
- BasePopup devient -> BaseFormPopup(utilisé pour les formulaires, les popup importantes)
    - une position fixe(center(default), left, top, right, bottom, etc) all 9 positions on the viewport
    - rajout du header(titre + bouton de fermeture)(Facultatif)
    - rajoute un overlay