<template>
    <div
        ref="rootRef"
        class="sticky z-30 w-full flex-shrink-0 bg-bg rounded-t-lg rounded-bl-lg"
        :style="{ height: totalHeight, top: `calc(-1 * ${slideHeight})` }"
    >
        <!-- Sentinelle : détecte le moment où le carrousel se colle -->
        <div
            ref="sentinelRef"
            class="absolute left-0 right-0 h-px pointer-events-none"
            :style="{ top: `calc(${slideHeight} - 2px)` }"
            aria-hidden="true"
        />

        <!-- ═══ BACKGROUND ANIMÉ — partagé entre slide et slot ═══ -->
        <CarouselBackground
            class="rounded-t-lg rounded-bl-lg"
            :slides="slides"
            :current-slide="currentSlide"
        />

        <!-- Voile : fondu vers bg-bg-dark quand la barre est collée -->
        <div
            class="absolute inset-0 rounded-t-lg rounded-bl-lg bg-bg-dark pointer-events-none transition-opacity duration-300 ease-in-out"
            :class="isStuck ? 'opacity-100' : 'opacity-0'"
            aria-hidden="true"
        />

        <!-- ═══ PARTIE SLIDE — hauteur configurable ═══ -->
        <div
            class="absolute top-0 left-0 right-0"
            :style="{ height: slideHeight }"
        >
            <CarouselOverlay
                :count="slides.length"
                :current="currentSlide"
                :label="currentActiveSlide?.label"
                @prev="prev"
                @next="next"
                @go="goTo"
            />
        </div>

        <!-- ═══ PARTIE SLOT — ancrée en bas, sur le même background ═══ -->
        <div
            class="absolute bottom-0 left-0 right-0 z-10 flex flex-col [&>*]:flex-1 [&>*]:min-h-0"
            :style="{ height: slotHeight }"
        >
            <slot name="bottom" :is-stuck="isStuck" />
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue';
import {
    useCarousel,
    type CarouselSlide as BaseCarouselSlide
} from '@genesis-labs/web-core/core/composables/ux/useCarousel';
import CarouselBackground from './CarouselBackground.vue';
import CarouselOverlay from './CarouselOverlay.vue';

// Ré-exporté pour ne pas casser les imports existants (ex. GenesisCollectionLayout)
export type CarouselSlide = BaseCarouselSlide;

const props = withDefaults(defineProps<{
    slides: CarouselSlide[];
    slideHeight?: string;   // hauteur de la zone slide seule
    slotHeight?: string;    // hauteur de la zone slot seule
    autoPlay?: boolean;
    interval?: number;
}>(), {
    slideHeight: '220px',
    slotHeight:  '80px',
    autoPlay:    true,
    interval:    4000,
});

const emit = defineEmits<{
    'update:currentSlide': [value: number];
    'slide-change': [slide: CarouselSlide, direction: 'left' | 'right'];
}>();

// ═══ Logique commune (slides, navigation, auto-play) ═══
const { currentSlide, currentActiveSlide, next, prev, goTo } = useCarousel({
    slides: () => props.slides,
    autoPlay: () => props.autoPlay,
    interval: () => props.interval,
    onChange: (slide, direction) => emit('slide-change', slide, direction),
});

// Hauteur totale = slide + slot
const totalHeight = computed(() => `calc(${props.slideHeight} + ${props.slotHeight})`);

// ═══ Spécifique au Carrousel : détection du collage (sticky) ═══
const rootRef = ref<HTMLElement | null>(null);
const sentinelRef = ref<HTMLElement | null>(null);
const isStuck = ref(false);
let observer: IntersectionObserver | null = null;

function getScrollParent(el: HTMLElement | null): HTMLElement | null {
    let node = el?.parentElement ?? null;
    while (node) {
        if (/(auto|scroll|overlay)/.test(getComputedStyle(node).overflowY)) return node;
        node = node.parentElement;
    }
    return null;
}

onMounted(() => {
    if (!sentinelRef.value) return;
    observer = new IntersectionObserver(
        ([entry]) => {
            const top = entry.rootBounds?.top ?? 0;
            isStuck.value = !entry.isIntersecting && entry.boundingClientRect.top < top;
        },
        { root: getScrollParent(rootRef.value), threshold: 0 }
    );
    observer.observe(sentinelRef.value);
});

onUnmounted(() => observer?.disconnect());
</script>