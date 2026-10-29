<template>
    <div
        v-if="phase !== 'done'"
        class="w-full min-h-[10rem] overflow-hidden bg-bg-dark"
        :class="[
            overlay ? 'fixed inset-0 z-50' : 'relative',
            fullPage ? 'min-h-screen' : 'h-full',
            phase === 'fading' ? 'pointer-events-none' : ''
        ]"
    >
        <!-- GIF : en noir & blanc pendant le chargement, en couleur ensuite.
             Le :key remonte l'élément pour relancer l'animation à la 1re image. -->
        <canvas
            ref="canvasRef"
            class="block w-full h-full object-cover transition-[filter] duration-700"
            :class="phase === 'loading' ? 'grayscale' : 'grayscale-0'"
        />

        <!-- SPINNER CENTRAL : visible uniquement pendant le chargement -->
        <div
            class="absolute inset-0 flex flex-col items-center justify-center gap-4 bg-bg-dark/40 transition-opacity duration-500"
            :class="phase === 'loading' ? 'opacity-100' : 'opacity-0 pointer-events-none'"
        >
            <svg
                :width="svgSize"
                :height="svgSize"
                viewBox="0 0 24 24"
                fill="none"
                xmlns="http://www.w3.org/2000/svg"
                class="animate-spin"
            >
                <circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="2.5" class="text-secondary opacity-30" />
                <path d="M 12 3 A 9 9 0 0 1 21 12" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" class="text-accent" />
            </svg>
            <div v-if="title || message" class="flex flex-col items-center gap-1 px-6">
                <span v-if="title" class="text-base font-medium text-text">{{ title }}</span>
                <span v-if="message" class="text-sm text-text-muted text-center">{{ message }}</span>
            </div>
        </div>
        <div
            class="absolute inset-0 bg-white pointer-events-none transition-opacity ease-in"
            :class="phase === 'fading' ? 'opacity-100' : 'opacity-0'"
            :style="{ transitionDuration: `${fadeDuration}ms` }"
        />
    </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue';

type Phase = 'loading' | 'playing' | 'fading' | 'done';

interface Props {
    loading?: boolean;
    src?: string;
    size?: 'sm' | 'md' | 'lg' | 'xl';
    title?: string;
    message?: string;
    fullPage?: boolean;
    fadeDuration?: number;
    overlay?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
    loading: true,
    src: () => new URL('../../../../assets/videos/loading-screen-V1.gif', import.meta.url).href,
    size: 'xl',
    fullPage: false,
    fadeDuration: 1500,
    overlay: false,
});

const emit = defineEmits<{ finished: [] }>();

const SIZES = { sm: 16, md: 24, lg: 40, xl: 80 } as const;
const svgSize = computed(() => SIZES[props.size]);

const phase = ref<Phase>('loading');
const canvasRef = ref<HTMLCanvasElement | null>(null);

interface Frame { bitmap: ImageBitmap; duration: number }
let frames: Frame[] = [];
let ready = false;
let failed = false;
let raf = 0;
let fadeTimer: ReturnType<typeof setTimeout> | undefined;

// ═══ Décodage : toutes les images sont préparées pendant le chargement de l'API ═══
async function decodeGif() {
    if (typeof ImageDecoder === 'undefined') throw new Error('ImageDecoder indisponible');

    const data = await (await fetch(props.src)).arrayBuffer();
    const decoder = new ImageDecoder({ data, type: 'image/gif' });
    await decoder.tracks.ready;
    const count = decoder.tracks.selectedTrack!.frameCount;

    const decoded: Frame[] = [];
    for (let i = 0; i < count; i++) {
        const { image } = await decoder.decode({ frameIndex: i });
        const bitmap = await createImageBitmap(image);
        const duration = Math.max((image.duration ?? 100_000) / 1000, 20); // µs → ms
        image.close();
        decoded.push({ bitmap, duration });
    }
    decoder.close();
    frames = decoded;

    const canvas = canvasRef.value;
    if (canvas) {
        canvas.width = frames[0].bitmap.width;
        canvas.height = frames[0].bitmap.height;
    }
    draw(0);
}

function draw(i: number) {
    const canvas = canvasRef.value;
    const frame = frames[i];
    if (!canvas || !frame) return;
    canvas.getContext('2d')?.drawImage(frame.bitmap, 0, 0);
}

// ═══ Lecture : s'arrête sur la dernière image, puis fondu ═══
function play() {
    let idx = 0;
    let frameStart = performance.now();
    draw(0);

    const tick = (now: number) => {
        if (phase.value !== 'playing') return;
        if (now - frameStart >= frames[idx].duration) {
            if (idx >= frames.length - 1) return fade(); // dernière image : reste figée
            idx++;
            frameStart = now;
            draw(idx);
        }
        raf = requestAnimationFrame(tick);
    };
    raf = requestAnimationFrame(tick);
}

function fade() {
    if (phase.value === 'fading' || phase.value === 'done') return;
    cancelAnimationFrame(raf);
    phase.value = 'fading';
    fadeTimer = setTimeout(() => {
        phase.value = 'done';
        emit('finished');
    }, props.fadeDuration);
}

/** Démarre quand l'API est prête ET que le GIF est décodé */
function start() {
    if (props.loading || phase.value !== 'loading') return;
    if (failed) return fade();
    if (!ready) return; // reprise après le décodage
    phase.value = 'playing';
    play();
}

function reset() {
    cancelAnimationFrame(raf);
    clearTimeout(fadeTimer);
    phase.value = 'loading';
    nextTick(() => draw(0)); // retour sur la première image
}

// ═══ Première image : affichée tout de suite, sans attendre le décodage complet ═══
async function drawFirstFrame() {
    const img = new Image();
    img.src = props.src;
    await img.decode();

    const canvas = canvasRef.value;
    // Si le décodage complet est déjà passé par là, on ne touche plus au canvas
    if (!canvas || frames.length || phase.value !== 'loading') return;

    canvas.width = img.naturalWidth;
    canvas.height = img.naturalHeight;
    canvas.getContext('2d')?.drawImage(img, 0, 0);
}

watch(() => props.loading, (isLoading) => (isLoading ? reset() : start()));

onMounted(async () => {
    drawFirstFrame().catch(e => console.warn('[GenesisLoader] première image impossible :', e));

    try {
        await decodeGif();
        ready = true;
    } catch (e) {
        console.warn('[GenesisLoader] décodage du GIF impossible :', e);
        failed = true;
    }
    start();
});

onBeforeUnmount(() => {
    cancelAnimationFrame(raf);
    clearTimeout(fadeTimer);
    frames.forEach(f => f.bitmap.close());
});
</script>