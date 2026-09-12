<template>
    <div 
        class="relative w-full flex-shrink-0 bg-bg rounded-t-lg rounded-bl-lg"
        :style="{ height: totalHeight }"
    >
        <!-- ═══ BACKGROUND ANIMÉ — partagé entre slide et slot ═══ -->
        <div
            class="absolute inset-0 flex transition-transform duration-500 ease-in-out"
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

        <!-- ═══ PARTIE SLIDE — hauteur configurable ═══ -->
        <div
            class="absolute top-0 left-0 right-0 flex items-center justify-center"
            :style="{ height: slideHeight }"
        >
            <!-- Labels des slides (optionnel) -->
            <span
                v-if="currentActiveSlide?.label"
                class="text-white text-lg font-semibold drop-shadow-md z-10"
            >
                {{ currentActiveSlide.label }}
            </span>

            <!-- Bouton Précédent -->
            <button
                v-if="slides.length > 1"
                class="absolute left-2 top-1/2 -translate-y-1/2 w-8 h-8 rounded-full bg-black/40 hover:bg-black/60 text-white flex items-center justify-center transition-colors z-10 backdrop-blur-sm"
                @click="prevSlide"
                aria-label="Slide précédent"
            >
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                    <polyline points="15 18 9 12 15 6" />
                </svg>
            </button>

            <!-- Bouton Suivant -->
            <button
                v-if="slides.length > 1"
                class="absolute right-2 top-1/2 -translate-y-1/2 w-8 h-8 rounded-full bg-black/40 hover:bg-black/60 text-white flex items-center justify-center transition-colors z-10 backdrop-blur-sm"
                @click="nextSlide"
                aria-label="Slide suivant"
            >
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                    <polyline points="9 18 15 12 9 6" />
                </svg>
            </button>

            <!-- Indicateurs (dots) -->
            <div
                v-if="slides.length > 1"
                class="absolute bottom-2 left-1/2 -translate-x-1/2 flex gap-2 z-10"
            >
                <button
                    v-for="(_, index) in slides"
                    :key="index"
                    class="h-2 rounded-full transition-all duration-300"
                    :class="index === currentSlide ? 'bg-white w-6' : 'bg-white/50 hover:bg-white/80 w-2'"
                    @click="goToSlide(index)"
                    :aria-label="`Aller au slide ${index + 1}`"
                />
            </div>
        </div>

        <!-- ═══ PARTIE SLOT — ancrée en bas, sur le même background ═══ -->
        <div
            class="absolute bottom-0 left-0 right-0 z-10"
        >
            <slot name="bottom" />
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue';

export interface CarouselSlide {
    color?: string;
    image?: string;
    label?: string;
}

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

const currentSlide = ref(0);
let autoPlayInterval: ReturnType<typeof setInterval> | null = null;

// Hauteur totale = slide + slot
const totalHeight = computed(() => {
    return `calc(${props.slideHeight} + ${props.slotHeight})`;
});

const currentActiveSlide = computed(() => props.slides[currentSlide.value]);

function nextSlide() {
    currentSlide.value = (currentSlide.value + 1) % props.slides.length;
    emit('slide-change', props.slides[currentSlide.value], 'left');
}

function prevSlide() {
    currentSlide.value = (currentSlide.value - 1 + props.slides.length) % props.slides.length;
    emit('slide-change', props.slides[currentSlide.value], 'right');
}

function goToSlide(index: number) {
    const direction = index > currentSlide.value ? 'left' : 'right';
    currentSlide.value = index;
    emit('slide-change', props.slides[currentSlide.value], direction);
    resetAutoPlay();
}

function startAutoPlay() {
    if (props.autoPlay && props.slides.length > 1) {
        autoPlayInterval = setInterval(nextSlide, props.interval);
    }
}

function stopAutoPlay() {
    if (autoPlayInterval) {
        clearInterval(autoPlayInterval);
        autoPlayInterval = null;
    }
}

function resetAutoPlay() {
    stopAutoPlay();
    startAutoPlay();
}

watch(() => props.autoPlay, (val) => {
    if (val) startAutoPlay();
    else stopAutoPlay();
});

onMounted(() => {
    if (props.slides.length > 0) {
        emit('slide-change', props.slides[0], 'right');
    }
    startAutoPlay();
});

onUnmounted(() => stopAutoPlay());
</script>