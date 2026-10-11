import { ref, computed, watch, onMounted, onUnmounted } from 'vue';

export interface CarouselSlide {
    color?: string;
    image?: string;
    label?: string;
}

export type CarouselDirection = 'left' | 'right';

interface UseCarouselOptions {
    /** Getters (et non des valeurs) pour rester réactif aux props du composant appelant */
    slides: () => CarouselSlide[];
    autoPlay: () => boolean;
    interval: () => number;
    /** Appelé à chaque changement de slide (et une fois au montage pour le slide 0) */
    onChange?: (slide: CarouselSlide, direction: CarouselDirection) => void;
}

/**
 * Logique commune à tous les carrousels : slide courant, navigation, auto-play.
 * Ne contient aucun rendu : les composants qui l'utilisent restent libres de leur mise en page.
 */
export function useCarousel({ slides, autoPlay, interval, onChange }: UseCarouselOptions) {
    const currentSlide = ref(0);
    let timer: ReturnType<typeof setInterval> | null = null;

    const currentActiveSlide = computed(() => slides()[currentSlide.value]);

    function change(index: number, direction: CarouselDirection) {
        currentSlide.value = index;
        const slide = slides()[index];
        if (slide) onChange?.(slide, direction);
    }

    function next() {
        const count = slides().length;
        if (!count) return;
        change((currentSlide.value + 1) % count, 'left');
    }

    function prev() {
        const count = slides().length;
        if (!count) return;
        change((currentSlide.value - 1 + count) % count, 'right');
    }

    function goTo(index: number) {
        const direction: CarouselDirection = index > currentSlide.value ? 'left' : 'right';
        change(index, direction);
        resetAutoPlay();
    }

    function startAutoPlay() {
        if (autoPlay() && slides().length > 1) {
            timer = setInterval(next, interval());
        }
    }

    function stopAutoPlay() {
        if (timer) {
            clearInterval(timer);
            timer = null;
        }
    }

    function resetAutoPlay() {
        stopAutoPlay();
        startAutoPlay();
    }

    watch(autoPlay, (enabled) => {
        if (enabled) startAutoPlay();
        else stopAutoPlay();
    });

    // Si la liste raccourcit sous l'index courant, on revient au début
    watch(() => slides().length, (count) => {
        if (currentSlide.value >= count) currentSlide.value = 0;
    });

    onMounted(() => {
        const first = slides()[0];
        if (first) onChange?.(first, 'right');
        startAutoPlay();
    });

    onUnmounted(stopAutoPlay);

    return { currentSlide, currentActiveSlide, next, prev, goTo };
}