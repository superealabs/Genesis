import { ref } from 'vue';

/**
 * Composable générique pour gérer l'état et le déclenchement d'un popup de remplacement.
 * @typeParam T - Le type de l'objet à remplacer (ex: Framework, DatabaseEngineDto)
 */
export function useReplacePopup<T>() {
    const showReplacePopup = ref(false);
    const pendingItem = ref<T | null>(null);
    const mouseX = ref<number | null>(null);
    const mouseY = ref<number | null>(null);

    function triggerReplace(item: T, event?: MouseEvent) {
        pendingItem.value = item;
        mouseX.value = event ? event.clientX : window.innerWidth / 2;
        mouseY.value = event ? event.clientY : window.innerHeight / 2;
        showReplacePopup.value = true;
    }

    function cancelReplace() {
        showReplacePopup.value = false;
        pendingItem.value = null;
        mouseX.value = null;
        mouseY.value = null;
    }

    return {
        showReplacePopup,
        pendingItem,
        mouseX,
        mouseY,
        triggerReplace,
        cancelReplace
    };
}