<template>
    <!-- Masque fixe : il rogne, il ne bouge jamais.
         Les coins arrondis se passent via `class` depuis le parent. -->
    <div class="absolute inset-0 overflow-hidden">
        <!-- Bande mobile : elle porte le translateX -->
        <div
            class="flex h-full transition-transform duration-500 ease-in-out"
            :style="{ transform: `translateX(-${currentSlide * 100}%)` }"
        >
            <div
                v-for="(slide, index) in slides"
                :key="index"
                class="w-full h-full flex-shrink-0"
            >
                <img
                    v-if="slide.image"
                    :src="slide.image"
                    :alt="slide.label || 'Slide'"
                    class="w-full h-full object-cover"
                />
                <div
                    v-else
                    class="w-full h-full"
                    :style="{ backgroundColor: slide.color || '#3B82F6' }"
                />
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import type { CarouselSlide } from '@genesis-labs/web-core/core/composables/ux/useCarousel';

defineProps<{
    slides: CarouselSlide[];
    currentSlide: number;
}>();
</script>