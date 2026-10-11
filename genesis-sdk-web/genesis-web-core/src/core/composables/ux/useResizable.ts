// genesis-sdk-web/genesis-web-core/src/core/composables/ux/useResizable.ts
import { ref, computed, onUnmounted, type Ref, type ComputedRef } from 'vue';

// ============================================================================
// TYPES & INTERFACES
// ============================================================================

export interface UseResizableOptions {
  minWidth?: number;
  maxWidth?: number | (() => number);
  minHeight?: number;
  maxHeight?: number | (() => number);
  resizableX?: Ref<boolean>;
  resizableY?: Ref<boolean>;
}

export interface UseResizableReturn {
  resizeStyle: ComputedRef<Record<string, string>>;
  isResizing: Ref<boolean>;
  startResizeBottom: (e: MouseEvent) => void;
  startResizeLeft: (e: MouseEvent) => void;
  startResizeRight: (e: MouseEvent) => void;
}

// ============================================================================
// COMPOSABLE PRINCIPAL
// ============================================================================

export function useResizable(options: UseResizableOptions = {}): UseResizableReturn {
  // --- 1. Options avec valeurs par défaut ---
  const {
    minWidth = 250,
    maxWidth = () => window.innerWidth * 0.9,
    minHeight = 150,
    maxHeight = () => window.innerHeight * 0.9,
    resizableX = ref(true),
    resizableY = ref(true),
  } = options;

  // --- 2. État Réactif ---
  const resizeWidth = ref<number | null>(null);
  const resizeHeight = ref<number | null>(null);
  const isResizing = ref(false);

  // --- 3. Fonctions Utilitaires ---
  const getMaxWidthVal = () => (typeof maxWidth === 'function' ? maxWidth() : maxWidth);
  const getMaxHeightVal = () => (typeof maxHeight === 'function' ? maxHeight() : maxHeight);

  // --- 4. Computeds ---
  const resizeStyle = computed(() => {
    const styles: Record<string, string> = {};
    if (resizeWidth.value !== null) styles.width = `${resizeWidth.value}px`;
    if (resizeHeight.value !== null) styles.height = `${resizeHeight.value}px`;
    return styles;
  });

  // --- 5. Logique Centralisée (DRY) ---
  type ResizeAxis = 'x' | 'y';
  type ResizeDirection = 'left' | 'right' | 'bottom';

  /**
   * Factory créant un gestionnaire d'événements de redimensionnement.
   * Centralise la logique pour éviter la duplication entre les bords gauche, droit et bas.
   */
  function createResizeHandler(
    axis: ResizeAxis,
    direction: ResizeDirection,
    isEnabled: Ref<boolean>
  ) {
    return (e: MouseEvent) => {
      if (!isEnabled.value) return;
      
      e.preventDefault(); // Empêche la sélection de texte native
      isResizing.value = true;
      document.body.classList.add('is-resizing');
      document.body.style.userSelect = 'none'; // Sécurité supplémentaire contre la sélection

      const el = (e.currentTarget as HTMLElement).parentElement;
      if (!el) return;

      const startSize = axis === 'x' ? el.offsetWidth : el.offsetHeight;
      const startPos = axis === 'x' ? e.clientX : e.clientY;

      const onMove = (moveEvent: MouseEvent) => {
        const currentPos = axis === 'x' ? moveEvent.clientX : moveEvent.clientY;
        
        // Calcul du delta en fonction de la direction du bord tiré
        let delta = 0;
        if (direction === 'left') {
          delta = startPos - currentPos; // Tirer vers la gauche augmente la largeur
        } else {
          delta = currentPos - startPos; // Tirer vers la droite/bas augmente la taille
        }

        const newSize = startSize + delta;
        const min = axis === 'x' ? minWidth : minHeight;
        const max = axis === 'x' ? getMaxWidthVal() : getMaxHeightVal();

        // Clamp la valeur pour respecter les contraintes
        if (axis === 'x') {
          resizeWidth.value = Math.max(min, Math.min(max, newSize));
        } else {
          resizeHeight.value = Math.max(min, Math.min(max, newSize));
        }
      };

      const onUp = () => {
        document.removeEventListener('mousemove', onMove);
        document.removeEventListener('mouseup', onUp);
        document.body.classList.remove('is-resizing');
        document.body.style.userSelect = '';
        
        // Délai d'une tick pour éviter qu'un événement 'click' parasite ne se déclenche 
        // sur l'élément situé sous la souris à la fin du redimensionnement.
        setTimeout(() => {
          isResizing.value = false;
        }, 0);
      };

      document.addEventListener('mousemove', onMove);
      document.addEventListener('mouseup', onUp);
    };
  }

  // --- 6. Actions Publiques ---
  const startResizeBottom = createResizeHandler('y', 'bottom', resizableY);
  const startResizeLeft = createResizeHandler('x', 'left', resizableX);
  const startResizeRight = createResizeHandler('x', 'right', resizableX);

  // --- 7. Lifecycle (Nettoyage) ---
  onUnmounted(() => {
    // Garantit que l'état global est nettoyé si le composant est détruit pendant un resize
    document.body.classList.remove('is-resizing');
    document.body.style.userSelect = '';
  });

  // --- 8. Retour ---
  return {
    resizeStyle,
    isResizing,
    startResizeBottom,
    startResizeLeft,
    startResizeRight
  };
}