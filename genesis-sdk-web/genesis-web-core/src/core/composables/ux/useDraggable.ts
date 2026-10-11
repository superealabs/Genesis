// genesis-sdk-web/genesis-web-core/src/core/composables/ux/useDraggable.ts
import { ref, computed, onUnmounted, type Ref, unref } from 'vue';

// ============================================================================
// TYPES & INTERFACES
// ============================================================================

export interface Position {
  x: number;
  y: number;
}

export interface UseDraggableOptions {
  /**
   * Désactive le comportement de glisser-déposer (peut être une valeur réactive).
   */
  disabled?: Ref<boolean> | boolean;
  
  /**
   * Callback appelé au début de l'action de glisser.
   */
  onDragStart?: () => void;
  
  /**
   * Callback appelé à la fin de l'action de glisser.
   */
  onDragEnd?: () => void;
  
  /**
   * Liste de sélecteurs CSS sur lesquels le glisser-déposer doit être ignoré 
   * (ex: pour permettre la sélection de texte ou le clic sur des boutons à l'intérieur de la zone).
   */
  ignoreSelectors?: string[];
}

/**
 * Composable pour rendre un élément HTML déplaçable (draggable) via la souris.
 * 
 * @example
 * ```vue
 * <script setup lang="ts">
 * const { isDragging, startDrag, draggableStyle, resetPosition } = useDraggable({
 *     disabled: computed(() => size.value === 'full')
 * });
 * </script>
 * 
 * <template>
 *     <div :style="draggableStyle">
 *         <div @mousedown="startDrag" class="cursor-move">
 *             Zone de préhension
 *         </div>
 *     </div>
 * </template>
 * ```
 */
export function useDraggable(options: UseDraggableOptions = {}) {
  const {
    disabled = false,
    onDragStart,
    onDragEnd,
    ignoreSelectors = ['button', 'input', 'textarea', 'select', 'a']
  } = options;

  // ==========================================================================
  // 1. ÉTAT RÉACTIF
  // ==========================================================================
  const isDragging = ref(false);
  const hasMoved = ref(false);
  const position = ref<Position>({ x: 0, y: 0 });
  const dragStart = ref<Position>({ x: 0, y: 0 });

  // ==========================================================================
  // 2. FONCTIONS UTILITAIRES
  // ==========================================================================

  /**
   * Vérifie si le glisser-déposer est actuellement désactivé.
   * Utilise unref pour supporter à la fois les valeurs statiques et les Refs.
   */
  function isDisabled(): boolean {
    return unref(disabled);
  }

  /**
   * Vérifie si l'élément ciblé par le clic doit ignorer l'action de glisser.
   */
  function shouldIgnoreTarget(target: HTMLElement): boolean {
    return ignoreSelectors.some(selector => target.closest(selector) !== null);
  }

  // ==========================================================================
  // 3. ACTIONS (GESTIONNAIRES D'ÉVÉNEMENTS)
  // ==========================================================================

  /**
   * Initialise le glisser-déposer.
   * Calcule le décalage (offset) entre la position actuelle de l'élément et 
   * la position de la souris pour éviter que l'élément ne "saute" sous le curseur.
   */
  function startDrag(event: MouseEvent) {
    if (isDisabled()) return;
    if (shouldIgnoreTarget(event.target as HTMLElement)) return;

    isDragging.value = true;
    hasMoved.value = true;

    // Calcul de l'offset pour un déplacement fluide
    dragStart.value = {
      x: event.clientX - position.value.x,
      y: event.clientY - position.value.y
    };

    // Écouteurs globaux pour capturer le mouvement même si la souris sort de l'élément
    document.addEventListener('mousemove', onDrag);
    document.addEventListener('mouseup', stopDrag);

    // Empêche la sélection de texte native du navigateur pendant le déplacement
    event.preventDefault();

    onDragStart?.();
  }

  /**
   * Met à jour la position de l'élément pendant le déplacement.
   */
  function onDrag(event: MouseEvent) {
    if (!isDragging.value) return;

    position.value = {
      x: event.clientX - dragStart.value.x,
      y: event.clientY - dragStart.value.y
    };
  }

  /**
   * Termine le glisser-déposer et nettoie les écouteurs d'événements globaux.
   */
  function stopDrag() {
    if (!isDragging.value) return;
    
    isDragging.value = false;

    document.removeEventListener('mousemove', onDrag);
    document.removeEventListener('mouseup', stopDrag);

    onDragEnd?.();
  }

  /**
   * Réinitialise la position de l'élément à son origine (0, 0).
   */
  function resetPosition() {
    position.value = { x: 0, y: 0 };
    hasMoved.value = false;
  }

  // ==========================================================================
  // 4. COMPUTEDS (STYLE DYNAMIQUE)
  // ==========================================================================

  /**
   * Génère les styles CSS nécessaires au déplacement.
   * Optimisation : désactive la transition pendant le drag pour éviter la latence, 
   * et l'active à la fin pour un retour fluide si nécessaire. Utilise willChange 
   * pour informer le moteur de rendu des changements à venir.
   */
  const draggableStyle = computed(() => {
    if (!hasMoved.value) return {};

    return {
      transform: `translate(${position.value.x}px, ${position.value.y}px)`,
      transition: isDragging.value ? 'none' : 'transform 0.15s ease-out',
      willChange: isDragging.value ? 'transform' : 'auto'
    };
  });

  // ==========================================================================
  // 5. LIFECYCLE (NETTOYAGE)
  // ==========================================================================

  /**
   * Garantit qu'aucun écouteur d'événement global ne reste attaché 
   * si le composant utilisant ce composable est détruit pendant un drag.
   */
  onUnmounted(() => {
    document.removeEventListener('mousemove', onDrag);
    document.removeEventListener('mouseup', stopDrag);
  });

  // ==========================================================================
  // 6. RETOUR
  // ==========================================================================
  return {
    // État
    isDragging,
    hasMoved,
    position,
    
    // Actions
    startDrag,
    resetPosition,
    
    // Style
    draggableStyle
  };
}