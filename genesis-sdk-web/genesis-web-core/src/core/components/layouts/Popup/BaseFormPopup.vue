<template>
    <div 
        class="fixed inset-0 bg-black/50 flex p-4"
        :class="[positionClasses, overlayClasses]"
        :style="{ zIndex }"
        @click.self="handleOverlayClick()"
    >
        <div
            class="bg-bg-dark text-text rounded-lg w-full flex flex-col relative"
            :class="[sizeClasses, { 'pointer-events-auto': !showOverlay }]"
            :style="[draggableStyle, resizeStyle]"
        >
            <!-- Resize handles (inchangés) -->
            <div v-if="resizableY" class="absolute bottom-0 left-0 right-0 h-3 cursor-ns-resize z-10 flex items-center justify-center" @mousedown="startResizeBottom">
                <div class="w-8 h-1 rounded-full bg-secondary hover:bg-accent/50 transition-colors" />
            </div>
            <div v-if="resizableX" class="absolute top-0 left-0 bottom-0 w-3 cursor-ew-resize z-10 flex items-center justify-center" @mousedown="startResizeLeft">
                <div class="w-1 h-8 rounded-full bg-secondary hover:bg-accent/50 transition-colors" />
            </div>
            <div v-if="resizableX" class="absolute top-0 right-0 bottom-0 w-3 cursor-ew-resize z-10 flex items-center justify-center" @mousedown="startResizeRight">
                <div class="w-1 h-8 rounded-full bg-secondary hover:bg-accent/50 transition-colors" />
            </div>

            <!-- Header avec titre -->
            <div v-if="title" class="flex items-center justify-between select-none" :class="[paddingClasses.header, { 'cursor-move': isDraggable }]" @mousedown="startDrag">
                <span class="font-semibold text-sm">{{ title }}</span>
                <GenesisButtonIcon v-if="isClosable" :variant="'tertiary'" size="md" @click.stop="$emit('close')">
                    <IconX color="var(--color-text-muted)" />
                </GenesisButtonIcon>
            </div>

            <!-- Header sans titre -->
            <div v-else class="flex justify-end select-none" :class="[paddingClasses.header, { 'cursor-move': isDraggable }]" @mousedown="startDrag">
                <GenesisButtonIcon v-if="isClosable" :variant="'tertiary'" size="md" @click.stop="$emit('close')">
                    <IconX color="var(--color-text-muted)" />
                </GenesisButtonIcon>
            </div>

            <!-- ═══ CARROUSEL (Délégué au composant dédié) ═══ -->
            <Carrousel 
                v-if="showCarousel" 
                :slides="carouselSlides" 
                height="200px" 
                :auto-play="true"
            />

            <!-- Contenu -->
            <div :class="[paddingClasses.content, 'flex flex-col flex-1 overflow-hidden min-h-0']">
                <slot />
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { computed, toRef, onMounted, onUnmounted, ref } from 'vue';
import { useDraggable } from '@/core/composables/ux/useDraggable.ts';
import { useResizable } from '@/core/composables/ux/useResizable.ts';
import IconX from '@/core/components/ui/icons/IconX.vue';
import GenesisButtonIcon from '@/core/components/ui/actions/GenesisButtonIcon.vue';
// ✅ Import du nouveau composant
import Carrousel, { type CarouselSlide } from '@/core/components/ui/carrousel/Carrousel.vue';
import type { PopupPosition, PopupSize, PopupPadding } from './popup.types';
import { Z_INDEX, PADDING_CLASSES } from './popup.types';

const props = withDefaults(defineProps<{
    title?: string;
    isClosable?: boolean;
    draggable?: boolean;
    resizableX?: boolean;
    resizableY?: boolean;
    size?: PopupSize;
    position?: PopupPosition;
    showOverlay?: boolean;
    closeOnEscape?: boolean;
    closeOnOverlayClick?: boolean;
    zIndex?: number;
    padding?: PopupPadding;
    showCarousel?: boolean;
}>(), {
    isClosable: true,
    draggable: true,
    resizableX: false,
    resizableY: false,
    size: 'md',
    position: 'center',
    showOverlay: true,
    closeOnEscape: true,
    closeOnOverlayClick: true,
    zIndex: Z_INDEX.modal,
    padding: 'md',
    showCarousel: false
});

const emit = defineEmits<{ close: [] }>();

// ✅ Données de démo pour le carrousel (facilement remplaçables par des images plus tard)
const carouselSlides = ref<CarouselSlide[]>([
    { color: '#3B82F6', label: 'Slide 1 - Bleu' },
    { color: '#EF4444', label: 'Slide 2 - Rouge' },
    { color: '#10B981', label: 'Slide 3 - Vert' },
    { color: '#F59E0B', label: 'Slide 4 - Orange' },
    { color: '#8B5CF6', label: 'Slide 5 - Violet' },
]);

const isDraggable = computed(() => props.draggable);
const { startDrag, draggableStyle } = useDraggable({
    disabled: computed(() => !isDraggable.value)
});

const { resizeStyle, isResizing, startResizeBottom, startResizeLeft, startResizeRight } = useResizable({
    resizableX: toRef(props, 'resizableX'),
    resizableY: toRef(props, 'resizableY'),
});

function handleOverlayClick() {
    if (isResizing.value) return;
    if (props.isClosable && props.closeOnOverlayClick) emit('close');
}

const positionClasses = computed(() => {
    const positionMap: Record<PopupPosition, string> = {
        'center': 'items-center justify-center',
        'top-left': 'items-start justify-start',
        'top': 'items-start justify-center',
        'top-right': 'items-start justify-end',
        'left': 'items-center justify-start',
        'right': 'items-center justify-end',
        'bottom-left': 'items-end justify-start',
        'bottom': 'items-end justify-center',
        'bottom-right': 'items-end justify-end'
    };
    return positionMap[props.position];
});

const overlayClasses = computed(() => {
    if (props.showOverlay) return 'bg-black/50';
    return 'bg-transparent pointer-events-none';
});

const sizeClasses = computed(() => ({
    'max-w-[min(400px,90vw)] h-[50vh]':   props.size === 'sm',
    'max-w-[min(600px,90vw)] h-[60vh]':   props.size === 'md',
    'max-w-[min(800px,90vw)] h-[70vh]':   props.size === 'lg',
    'max-w-[min(1000px,90vw)] h-[80vh]':  props.size === 'xl',
    'max-w-[min(1200px,90vw)] h-[85vh]':  props.size === '2xl',
    'max-w-[min(1400px,90vw)] h-[90vh]':  props.size === '3xl',
    'max-w-[90vw] h-[90vh]':              props.size === 'full',
}));

function handleKeydown(event: KeyboardEvent) {
    if (event.key === 'Escape' && props.isClosable && props.closeOnEscape) {
        emit('close');
    }
}

const paddingClasses = computed(() => PADDING_CLASSES[props.padding]);

onMounted(() => {
    document.addEventListener('keydown', handleKeydown);
});

onUnmounted(() => {
    document.removeEventListener('keydown', handleKeydown);
});
</script>