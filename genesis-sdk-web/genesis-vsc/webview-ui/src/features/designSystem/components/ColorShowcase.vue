<template>
    <ShowcaseLayout title="Color">
        <div class="space-y-2">
            <h3 class="text-text-muted text-sm">Backgrounds</h3>
            <div class="flex gap-3">
                <div class="flex flex-col items-center gap-1"
                    v-for="color in backgrounds" :key="color.label">
                    <div
                        class="w-12 h-12 rounded border border-secondary"
                        :class="color.class"
                    />
                    <span class="text-xs text-text-muted">{{ color.label }}</span>
                </div>
            </div>
        </div>

        <!-- Text -->
        <div class="space-y-2">
            <h3 class="text-text-muted text-sm">Text</h3>
            <div class="flex flex-col gap-1">
                <span class="text-sm text-text">text — texte principal</span>
                <span class="text-sm text-text-muted">text-muted — texte secondaire</span>
            </div>
        </div>

        <!-- Accent & Secondary -->
        <div class="space-y-2">
            <h3 class="text-text-muted text-sm">Accent & Secondary</h3>
            <div class="flex gap-3">
                <div class="flex flex-col items-center gap-1"
                    v-for="color in accents" :key="color.label">
                    <div
                        class="w-12 h-12 rounded"
                        :class="color.class"
                    />
                    <span class="text-xs text-text-muted">{{ color.label }}</span>
                </div>
            </div>
        </div>

    </ShowcaseLayout>
</template>

<script setup lang="ts">
import ShowcaseLayout from '@/features/designSystem/components/layouts/ShowcaseLayout.vue';

const backgrounds = [
    { label: 'bg-light', class: 'bg-bg-light' },
    { label: 'bg',       class: 'bg-bg'       },
    { label: 'bg-dark',  class: 'bg-bg-dark'  },
];

const accents = [
    { label: 'accent',    class: 'bg-accent'    },
    { label: 'secondary', class: 'bg-secondary' },
];
</script>