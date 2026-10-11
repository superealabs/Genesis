<template>
    <!-- Poignée de redimensionnement vertical.
         Élément DU FLUX (pas de position absolute) : le contenu qui la précède s'arrête
         au-dessus d'elle et ne peut jamais passer derrière.
         À placer en DERNIER enfant d'un conteneur `flex flex-col` (avec overflow-hidden
         si les coins sont arrondis, pour que son survol suive la forme). -->
    <div
        class="group shrink-0 h-4 flex items-center justify-center cursor-ns-resize select-none hover:text-primary transition-colors"
        role="separator"
        aria-orientation="horizontal"
        :aria-label="title"
        :title="title"
        @mousedown="$emit('resize-start', $event)"
    >
        <IconDragY
            class="text-text-muted"
            :size="16"
        />
    </div>
</template>

<script setup lang="ts">
import IconDragY from '@genesis-labs/web-core/core/components/ui/icons/IconDragY.vue';

withDefaults(defineProps<{
    title?: string;
}>(), {
    title: 'Redimensionner verticalement'
});

/** Émis au début du glissement : à brancher sur `startResizeBottom` de useResizable */
defineEmits<{
    'resize-start': [event: MouseEvent];
}>();
</script>