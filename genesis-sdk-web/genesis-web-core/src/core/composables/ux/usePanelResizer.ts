// genesis-sdk-web/genesis-web-core/src/core/composables/ux/usePanelResizer.ts
import { ref, onUnmounted } from 'vue';

// ============================================================================
// TYPES & INTERFACES
// ============================================================================

export interface UsePanelResizerReturn {
  width: ReturnType<typeof ref<number>>;
  isResizing: ReturnType<typeof ref<boolean>>;
  startResize: (e: MouseEvent) => void;
}

// ============================================================================
// COMPOSABLE PRINCIPAL
// ============================================================================

/**
 * Composable pour redimensionner un panneau via un diviseur (resizer).
 * 
 * @param initialWidth Largeur de départ en pixels.
 * @param minWidth Largeur minimale autorisée en pixels.
 * @param maxWidth Largeur maximale autorisée en pixels.
 * @param invertDirection Si true, le panneau est ancré à DROITE du diviseur. 
 *                        Dans ce cas, un déplacement de la souris vers la droite réduit la largeur du panneau.
 * @returns Un objet contenant la largeur réactive, l'état de redimensionnement et la fonction de démarrage.
 */
export function usePanelResizer(
  initialWidth: number,
  minWidth: number,
  maxWidth: number,
  invertDirection: boolean = false
): UsePanelResizerReturn {
  
  // --- 1. État Réactif ---
  const width = ref(initialWidth);
  const isResizing = ref(false);

  // --- 2. État Local (Non réactif, utilisé uniquement pendant le drag) ---
  let startX = 0;
  let startWidth = 0;

  // --- 3. Actions (Gestionnaires d'événements) ---

  /**
   * Met à jour la largeur du panneau en fonction du déplacement de la souris.
   * Applique un "clamp" pour respecter les contraintes minWidth et maxWidth.
   */
  const onMouseMove = (e: MouseEvent) => {
    if (!isResizing.value) return;
    
    const delta = e.clientX - startX;
    
    // Si invertDirection est true, le panneau est à droite. 
    // Un delta positif (souris vers la droite) doit réduire la largeur.
    const newWidth = startWidth + (invertDirection ? -delta : delta);
    
    // Clamp la valeur entre les bornes min et max
    width.value = Math.max(minWidth, Math.min(maxWidth, newWidth));
  };

  /**
   * Termine l'opération de redimensionnement et nettoie les écouteurs et styles globaux.
   */
  const onMouseUp = () => {
    if (!isResizing.value) return;
    
    isResizing.value = false;
    
    document.removeEventListener('mousemove', onMouseMove);
    document.removeEventListener('mouseup', onMouseUp);
    
    // Restauration des styles par défaut du body
    document.body.style.cursor = '';
    document.body.style.userSelect = '';
  };

  /**
   * Initialise l'opération de redimensionnement.
   * Capture la position initiale et applique les styles globaux pour une meilleure UX.
   */
  const startResize = (e: MouseEvent) => {
    e.preventDefault(); // Empêche la sélection de texte accidentelle
    
    isResizing.value = true;
    startX = e.clientX;
    startWidth = width.value;
    
    document.addEventListener('mousemove', onMouseMove);
    document.addEventListener('mouseup', onMouseUp);
    
    // Application de styles globaux pour indiquer l'état de redimensionnement
    document.body.style.cursor = 'col-resize';
    document.body.style.userSelect = 'none';
  };

  // --- 4. Lifecycle (Nettoyage) ---

  /**
   * Garantit qu'aucun écouteur d'événement global ne reste attaché 
   * et que les styles du body sont restaurés si le composant est détruit pendant un redimensionnement.
   */
  onUnmounted(() => {
    document.removeEventListener('mousemove', onMouseMove);
    document.removeEventListener('mouseup', onMouseUp);
    document.body.style.cursor = '';
    document.body.style.userSelect = '';
  });

  // --- 5. Retour ---
  return {
    width,
    isResizing,
    startResize
  };
}