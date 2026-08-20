import { ref, computed, onUnmounted, type Ref, unref } from 'vue';

interface UseDraggableOptions {
    /**
     * Désactive le drag (peut être réactif)
     */
    disabled?: Ref<boolean> | boolean;
    
    /**
     * Callback appelé au début du drag
     */
    onDragStart?: () => void;
    
    /**
     * Callback appelé à la fin du drag
     */
    onDragEnd?: () => void;
    
    /**
     * Empêche le drag si on clique sur ces sélecteurs CSS
     */
    ignoreSelectors?: string[];
}

interface Position {
    x: number;
    y: number;
}

/**
 * Composable pour rendre un élément draggable
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
 *             Header draggable
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

    // ═══ État du drag ═══
    const isDragging = ref(false);
    const hasMoved = ref(false);
    const position = ref<Position>({ x: 0, y: 0 });
    const dragStart = ref<Position>({ x: 0, y: 0 });

    // ═══ Vérification si le drag est désactivé ═══
    function isDisabled(): boolean {
        return unref(disabled);
    }

    // ═══ Vérification si l'élément cliqué doit être ignoré ═══
    function shouldIgnoreTarget(target: HTMLElement): boolean {
        return ignoreSelectors.some(selector => target.closest(selector));
    }

    // ═══ Démarrage du drag ═══
    function startDrag(event: MouseEvent) {
        // Vérifications
        if (isDisabled()) return;
        if (shouldIgnoreTarget(event.target as HTMLElement)) return;

        isDragging.value = true;
        hasMoved.value = true;

        // Position initiale de la souris
        dragStart.value = {
            x: event.clientX - position.value.x,
            y: event.clientY - position.value.y
        };

        // Écouter les mouvements sur tout le document
        document.addEventListener('mousemove', onDrag);
        document.addEventListener('mouseup', stopDrag);

        // Empêcher la sélection de texte pendant le drag
        event.preventDefault();

        // Callback
        onDragStart?.();
    }

    // ═══ Pendant le drag ═══
    function onDrag(event: MouseEvent) {
        if (!isDragging.value) return;

        position.value = {
            x: event.clientX - dragStart.value.x,
            y: event.clientY - dragStart.value.y
        };
    }

    // ═══ Fin du drag ═══
    function stopDrag() {
        isDragging.value = false;

        // Nettoyer les listeners
        document.removeEventListener('mousemove', onDrag);
        document.removeEventListener('mouseup', stopDrag);

        // Callback
        onDragEnd?.();
    }

    // ═══ Réinitialiser la position ═══
    function resetPosition() {
        position.value = { x: 0, y: 0 };
        hasMoved.value = false;
    }

    // ═══ Style dynamique pour l'élément draggable ═══
    const draggableStyle = computed(() => {
        if (!hasMoved.value) return {};

        return {
            transform: `translate(${position.value.x}px, ${position.value.y}px)`,
            transition: isDragging.value ? 'none' : 'transform 0.15s ease-out',
            willChange: isDragging.value ? 'transform' : 'auto'
        };
    });

    // ═══ Cleanup au démontage ═══
    onUnmounted(() => {
        document.removeEventListener('mousemove', onDrag);
        document.removeEventListener('mouseup', stopDrag);
    });

    // ═══ Return API ═══
    return {
        // État
        isDragging,
        hasMoved,
        position,
        
        // Fonctions
        startDrag,
        resetPosition,
        
        // Style
        draggableStyle
    };
}