<template>
    <!-- Posée sur le bord haut du parent (qui doit être `relative`).
         `absolute` : n'influence jamais la taille du parent.
         `overflow-hidden` : la mascotte semble sortir de derrière le parent. -->
    <div
        class="absolute bottom-full left-1/2 -translate-x-1/2 -mb-px overflow-hidden pointer-events-none"
        :style="{ height: `${size}px` }"
        aria-hidden="true"
    >
        <img
            :src="src"
            alt=""
            class="gfx-mascot block h-full w-auto max-w-none select-none"
            :class="{ 'is-selected': selected }"
            draggable="false"
        />
    </div>
</template>

<script setup lang="ts">
import './genesisSelectFx.css';

withDefaults(defineProps<{
    src: string;
    /** Hauteur en px */
    size?: number;
    selected?: boolean;
}>(), {
    size: 32,
    selected: false
});
</script>