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

// 1. Dictionnaire des tailles prédéfinies (Surface en px² et côté max)
// md (48x48 = 2304), sm (~32x32 = 1024), lg (~64x64 = 4096)
const SIZE_PRESETS = {
  xs: { area: 768, maxSide: 48 },
  sm: { area: 1024, maxSide: 64 },
  md: { area: 2304, maxSide: 96 },
  lg: { area: 4096, maxSide: 128 },
  xl: { area: 9216, maxSide: 192 }, // ✅ NOUVEAU : Extra Large
} as const;

type LogoSize = keyof typeof SIZE_PRESETS;

const props = withDefaults(defineProps<{
    src: string;
    alt?: string;
    size?: LogoSize;  
    area?: number;            
    maxSide?: number;       
}>(), {
    alt: '',
    size: 'md',               // ✅ Valeur par défaut
});

const ratio = ref(1);
const ready = ref(false);

// 2. Calcul des valeurs effectives (la prop explicite gagne sur le preset)
const effectiveArea = computed(() => props.area ?? SIZE_PRESETS[props.size].area);
const effectiveMaxSide = computed(() => props.maxSide ?? SIZE_PRESETS[props.size].maxSide);

watch(() => props.src, (src) => {
    ready.value = false;
    const img = new Image();
    img.onload = () => {
        if (src !== props.src) return; // réponse périmé
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

// 3. Utilisation des valeurs effectives pour le calcul
const size = computed(() => {
    const w = Math.sqrt(effectiveArea.value * ratio.value);
    const h = w / ratio.value;
    const k = Math.min(1, effectiveMaxSide.value / w, effectiveMaxSide.value / h);
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