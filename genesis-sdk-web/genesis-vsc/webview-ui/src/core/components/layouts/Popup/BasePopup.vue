<template>
    <div
        v-if="show"
        ref="popupRef"
        class="bg-bg border border-secondary rounded-lg shadow-lg overflow-hidden"
        :style="popupStyle"
    >
        <slot />
    </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue';
import type { PopupAnchorPosition } from './popup.types';
import { Z_INDEX } from './popup.types';

const props = withDefaults(defineProps<{
    /** Contrôle la visibilité du popup */
    show?: boolean;
    /** Élément de référence pour le positionnement (ex: bouton dropdown) */
    anchor?: HTMLElement | null;
    /** Coordonnée X de la souris (pour menu contextuel) */
    mouseX?: number | null;
    /** Coordonnée Y de la souris (pour menu contextuel) */
    mouseY?: number | null;
    /** Position par rapport à l'ancre ou à la souris */
    position?: PopupAnchorPosition;
    /** Espace en pixels entre la cible et le popup */
    offset?: number;
    /** Ferme le popup au clic en dehors */
    closeOnOutsideClick?: boolean;
    /** Ferme le popup à la touche Échap */
    closeOnEscape?: boolean;
    /** Niveau z-index du popup */
    zIndex?: number;
}>(), {
    show: true,
    anchor: null,
    mouseX: null,
    mouseY: null,
    position: 'bottom-left',
    offset: 4,
    closeOnOutsideClick: true,
    closeOnEscape: true,
    zIndex: Z_INDEX.dropdown
});

const emit = defineEmits<{
    close: [];
}>();

const popupRef = ref<HTMLElement | null>(null);

// ═══ Calcul de la position dynamique ═══
const popupStyle = computed(() => {
    const style: Record<string, string> = {
        position: 'fixed',
        zIndex: props.zIndex.toString()
    };

    // Cas 1 : Positionnement par rapport à un élément ancre
    if (props.anchor) {
        const rect = props.anchor.getBoundingClientRect();
        
        switch (props.position) {
            case 'top-left':
                style.bottom = `${window.innerHeight - rect.top + props.offset}px`;
                style.right = `${window.innerWidth - rect.left}px`;
                break;
            case 'top':
                style.bottom = `${window.innerHeight - rect.top + props.offset}px`;
                style.left = `${rect.left + rect.width / 2}px`;
                style.transform = 'translateX(-50%)';
                break;
            case 'top-right':
                style.bottom = `${window.innerHeight - rect.top + props.offset}px`;
                style.left = `${rect.right}px`;
                break;
            case 'bottom-left':
                style.top = `${rect.bottom + props.offset}px`;
                style.right = `${window.innerWidth - rect.left}px`;
                break;
            case 'bottom':
                style.top = `${rect.bottom + props.offset}px`;
                style.left = `${rect.left + rect.width / 2}px`;
                style.transform = 'translateX(-50%)';
                break;
            case 'bottom-right':
                style.top = `${rect.bottom + props.offset}px`;
                style.left = `${rect.right}px`;
                break;
            case 'left':
                style.top = `${rect.top + rect.height / 2}px`;
                style.right = `${window.innerWidth - rect.left + props.offset}px`;
                style.transform = 'translateY(-50%)';
                break;
            case 'right':
                style.top = `${rect.top + rect.height / 2}px`;
                style.left = `${rect.right + props.offset}px`;
                style.transform = 'translateY(-50%)';
                break;
        }
    }
    // Cas 2 : Positionnement par rapport aux coordonnées de la souris
    else if (props.mouseX !== null && props.mouseY !== null) {
        style.top = `${props.mouseY + props.offset}px`;
        style.left = `${props.mouseX + props.offset}px`;
    }

    return style;
});

// ═══ Gestion du clic extérieur ═══
function handleOutsideClick(event: MouseEvent) {
    if (!props.closeOnOutsideClick || !popupRef.value) return;
    
    if (!popupRef.value.contains(event.target as Node)) {
        emit('close');
    }
}

// ═══ Gestion de la touche Échap ═══
function handleKeydown(event: KeyboardEvent) {
    if (props.closeOnEscape && event.key === 'Escape') {
        emit('close');
    }
}

// ═══ Cycle de vie ═══
watch(() => props.show, (newShow) => {
    if (newShow) {
        // Attendre le prochain tick pour que le DOM soit mis à jour
        setTimeout(() => {
            document.addEventListener('mousedown', handleOutsideClick);
            document.addEventListener('keydown', handleKeydown);
        }, 0);
    } else {
        document.removeEventListener('mousedown', handleOutsideClick);
        document.removeEventListener('keydown', handleKeydown);
    }
});

onMounted(() => {
    if (props.show) {
        document.addEventListener('mousedown', handleOutsideClick);
        document.addEventListener('keydown', handleKeydown);
    }
});

onUnmounted(() => {
    document.removeEventListener('mousedown', handleOutsideClick);
    document.removeEventListener('keydown', handleKeydown);
});
</script>