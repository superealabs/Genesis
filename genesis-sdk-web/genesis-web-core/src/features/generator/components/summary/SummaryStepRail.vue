<template>
    <!-- Frise verticale : un carré numéroté par étape, relié au suivant par un trait -->
    <ol class="flex flex-col items-center">
        <li v-for="(label, i) in labels" :key="label" class="flex flex-col items-center">
            <!-- Trait de liaison : rempli dès que l'étape est atteinte -->
            <span
                v-if="i > 0"
                class="w-0.5 h-4 transition-colors duration-300"
                :class="i <= current ? 'bg-primary/50 text-primary-text' : 'bg-bg-neutral-genesis'"
            />

            <button
                type="button"
                class="w-8 h-8 grid place-items-center rounded text-xs font-semibold transition-colors duration-300"
                :class="stateClass(i)"
                :title="label"
                :aria-label="`Étape ${i + 1} : ${label}`"
                :aria-current="i === current ? 'step' : undefined"
                @click="emit('select', i)"
            >
                {{ i + 1 }}
            </button>
        </li>
    </ol>
</template>

<script setup lang="ts">
const props = defineProps<{
    labels: string[];
    /** Index (0-based) de l'étape affichée */
    current: number;
}>();

const emit = defineEmits<{ select: [index: number] }>();

// Actuelle : primary — passée : primary/50 — à venir : neutral-genesis
const stateClass = (i: number) => {
    if (i === props.current) return 'bg-primary text-bg';
    if (i < props.current) return 'bg-primary/50 text-bg';
    return 'bg-bg-neutral-genesis text-text-muted';
};
</script>