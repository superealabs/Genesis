import { ref, onUnmounted } from 'vue';

/**
 * Composable pour redimensionner un panneau via un diviseur.
 * 
 * @param initialWidth Largeur de départ en pixels
 * @param minWidth Largeur minimale autorisée
 * @param maxWidth Largeur maximale autorisée
 * @param invertDirection Si true, le panneau est à DROITE du diviseur (il rétrécit quand on tire vers la droite)
 */
export function usePanelResizer(
    initialWidth: number,
    minWidth: number,
    maxWidth: number,
    invertDirection: boolean = false
) {
    const width = ref(initialWidth);
    const isResizing = ref(false);

    let startX = 0;
    let startWidth = 0;

    const onMouseMove = (e: MouseEvent) => {
        if (!isResizing.value) return;
        
        const delta = e.clientX - startX;
        // Si invertDirection est true (panneau de droite), on soustrait le delta
        const newWidth = startWidth + (invertDirection ? -delta : delta);
        
        // Clamp la valeur entre min et max
        width.value = Math.max(minWidth, Math.min(maxWidth, newWidth));
    };

    const onMouseUp = () => {
        if (!isResizing.value) return;
        isResizing.value = false;
        
        document.removeEventListener('mousemove', onMouseMove);
        document.removeEventListener('mouseup', onMouseUp);
        
        // Restaurer le curseur et la sélection de texte
        document.body.style.cursor = '';
        document.body.style.userSelect = '';
    };

    const startResize = (e: MouseEvent) => {
        // Empêcher la sélection de texte pendant le drag
        e.preventDefault();
        
        isResizing.value = true;
        startX = e.clientX;
        startWidth = width.value;
        
        document.addEventListener('mousemove', onMouseMove);
        document.addEventListener('mouseup', onMouseUp);
        
        // Changer le curseur globalement pour une meilleure UX
        document.body.style.cursor = 'col-resize';
        document.body.style.userSelect = 'none';
    };

    // Nettoyage automatique si le composant est détruit pendant un resize
    onUnmounted(() => {
        document.removeEventListener('mousemove', onMouseMove);
        document.removeEventListener('mouseup', onMouseUp);
    });

    return {
        width,
        isResizing,
        startResize
    };
}