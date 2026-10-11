<template>
    <!-- Remplit son parent (qui doit être positionné). Laisse passer les clics sauf sur ses boutons. -->
    <div class="absolute inset-0 flex items-center justify-center pointer-events-none">
        <!-- Label du slide (optionnel) -->
        <span
            v-if="label"
            class="text-white text-lg font-semibold drop-shadow-md z-10"
        >
            {{ label }}
        </span>

        <!-- Bouton Précédent -->
        <button
            v-if="count > 1"
            class="pointer-events-auto absolute left-2 top-1/2 -translate-y-1/2 w-8 h-8 rounded-full bg-black/40 hover:bg-black/60 text-white flex items-center justify-center transition-colors z-10 backdrop-blur-sm"
            aria-label="Slide précédent"
            @click="$emit('prev')"
        >
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="15 18 9 12 15 6" />
            </svg>
        </button>

        <!-- Bouton Suivant -->
        <button
            v-if="count > 1"
            class="pointer-events-auto absolute right-2 top-1/2 -translate-y-1/2 w-8 h-8 rounded-full bg-black/40 hover:bg-black/60 text-white flex items-center justify-center transition-colors z-10 backdrop-blur-sm"
            aria-label="Slide suivant"
            @click="$emit('next')"
        >
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="9 18 15 12 9 6" />
            </svg>
        </button>

        <!-- Indicateurs (dots) -->
        <div
            v-if="count > 1"
            class="absolute bottom-2 left-1/2 -translate-x-1/2 flex gap-2 z-10"
        >
            <button
                v-for="n in count"
                :key="n"
                class="pointer-events-auto h-2 rounded-full transition-all duration-300"
                :class="n - 1 === current ? 'bg-white w-6' : 'bg-white/50 hover:bg-white/80 w-2'"
                :aria-label="`Aller au slide ${n}`"
                @click="$emit('go', n - 1)"
            />
        </div>
    </div>
</template>

<script setup lang="ts">
defineProps<{
    /** Nombre total de slides */
    count: number;
    /** Index du slide courant */
    current: number;
    label?: string;
}>();

defineEmits<{
    prev: [];
    next: [];
    go: [index: number];
}>();
</script>