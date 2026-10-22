<template>
    <div class="relative h-full w-full overflow-hidden">
        <!-- Zone des slides : on laisse de la place à droite pour la frise -->
        <div ref="viewport" class="absolute inset-y-0 left-0 right-16">
            <!-- `invisible` : tous cachés au départ, GSAP révèle le slide actif (style inline prioritaire) -->
            <section
                v-for="slide in SUMMARY_SLIDES"
                :key="slide.id"
                class="absolute inset-0 invisible overflow-y-auto"
            >
                <component :is="slide.component" />
            </section>
        </div>

        <SummaryStepRail
            class="absolute right-4 top-1/2 -translate-y-1/2"
            :labels="labels"
            :current="index"
            @select="goTo"
        />
    </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import SummaryStepRail from '../summary/SummaryStepRail.vue';
import { SUMMARY_SLIDES } from '../summary/summary-slides.ts';
import { useSlideTransition } from '@genesis-labs/web-core/core/composables/ux/useSlideTransition';
import { useStepSlider } from '@genesis-labs/web-core/core/composables/ux/useStepSlider';
import { useStepInput } from '@genesis-labs/web-core/core/composables/ux/useStepInput';

const labels = SUMMARY_SLIDES.map(s => s.label);

const viewport = ref<HTMLElement | null>(null);

// Assemblage : animation (GSAP) → état (index + verrou) → entrées (molette / clavier)
const { show, play } = useSlideTransition(viewport);
const { index, goTo, next, prev } = useStepSlider(SUMMARY_SLIDES.length, play);
useStepInput(viewport, { next, prev });

onMounted(() => show(index.value));
</script>