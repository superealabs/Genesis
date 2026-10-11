<template>
    <span
        role="img"
        :aria-label="alt"
        class="inline-block flex-shrink-0 text-text transition-opacity duration-200"
        :class="ready ? 'opacity-100' : 'opacity-0'"
        :style="style"
    />
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';

const props = withDefaults(defineProps<{
    src: string;
    alt?: string;
    /** Surface cible en px² (48 x 48 = 2304) */
    area?: number;
    /** Plafond par côté, pour éviter qu'un logo très allongé déborde */
    maxSide?: number;
}>(), {
    alt: '',
    area: 2304,
    maxSide: 96,
});

const ratio = ref(1);
const ready = ref(false);

watch(() => props.src, (src) => {
    ready.value = false;
    const img = new Image();
    img.onload = () => {
        if (src !== props.src) return; // réponse périmée
        ratio.value = img.naturalWidth && img.naturalHeight
            ? img.naturalWidth / img.naturalHeight
            : 1;
        ready.value = true;
    };
    img.onerror = () => {
        if (src !== props.src) return;
        ratio.value = 1;
        ready.value = true;
    };
    img.src = src;
}, { immediate: true });

const size = computed(() => {
    const w = Math.sqrt(props.area * ratio.value);
    const h = w / ratio.value;
    const k = Math.min(1, props.maxSide / w, props.maxSide / h);
    return { w: Math.round(w * k), h: Math.round(h * k) };
});

const style = computed(() => {
    const mask = `url("${props.src}")`;
    return {
        width: `${size.value.w}px`,
        height: `${size.value.h}px`,
        backgroundColor: 'currentColor',
        maskImage: mask,
        WebkitMaskImage: mask,
        maskRepeat: 'no-repeat',
        WebkitMaskRepeat: 'no-repeat',
        maskPosition: 'center',
        WebkitMaskPosition: 'center',
        maskSize: 'contain',
        WebkitMaskSize: 'contain',
    };
});
</script>