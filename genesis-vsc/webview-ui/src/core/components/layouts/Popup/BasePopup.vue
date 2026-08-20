<template>
    <div 
        class="fixed inset-0 bg-black/50 flex items-center justify-center z-100 p-4"
        @click.self="isClosable ? $emit('close') : null"
    >
        <div
            class="bg-bg text-text border border-secondary rounded-lg w-full flex flex-col"
            :class="sizeClasses"
            :style="draggableStyle"
        >
            
            <!-- Header avec titre -->
            <div 
                v-if="title" 
                class="flex items-center justify-between px-4 py-3 border-secondary select-none"
                :class="{ 'cursor-move': isDraggable }"
                @mousedown="startDrag"
            >
                <span class="font-semibold text-sm">{{ title }}</span>
                <GenesisButtonIcon
                    v-if="isClosable"
                    :visibleBackground="false"
                    size="md"
                    @click.stop="$emit('close')"
                >
                    <IconX color="var(--color-text-muted)" />
                </GenesisButtonIcon>
            </div>

            <!-- Header sans titre -->
            <div 
                v-else 
                class="flex justify-end px-4 py-3 border-secondary select-none"
                :class="{ 'cursor-move': isDraggable }"
                @mousedown="startDrag"
            >
                <GenesisButtonIcon
                    v-if="isClosable"
                    :visibleBackground="false"
                    size="md"
                    @click.stop="$emit('close')"
                >
                    <IconX color="var(--color-text-muted)" />
                </GenesisButtonIcon>
            </div>

            <!-- Contenu -->
            <div class="p-4 flex flex-col flex-1 overflow-auto">
                <slot />
            </div>

        </div>
    </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';

import { useDraggable } from '@/core/composables/ux/useDraggable.ts';
import IconX from '@/core/components/ui/icons/IconX.vue';
import GenesisButtonIcon from '@/core/components/ui/actions/GenesisButtonIcon.vue';

const props = withDefaults(defineProps<{
    title?: string;
    isClosable?: boolean;
    draggable?: boolean;
    size?: 'sm' | 'md' | 'lg' | 'xl' | '2xl' | '3xl' | 'full';
}>(), {
    isClosable: true,
    draggable: true,
    size: 'md'
});

defineEmits<{
    close: [];
}>();

// ═══ Computed pour savoir si le drag est activé ═══
const isDraggable = computed(() => 
    props.draggable
);

// ═══ Composable de drag ═══
const { startDrag, draggableStyle } = useDraggable({
    disabled: computed(() => !isDraggable.value)
});

// ═══ Classes de taille ═══
const sizeClasses = computed(() => ({
    'max-w-[min(400px,90vw)] min-h-[200px] max-h-[90vh]':   props.size === 'sm',
    'max-w-[min(600px,90vw)] min-h-[300px] max-h-[90vh]':   props.size === 'md',
    'max-w-[min(800px,90vw)] min-h-[400px] max-h-[90vh]':   props.size === 'lg',
    'max-w-[min(1000px,90vw)] min-h-[500px] max-h-[90vh]':  props.size === 'xl',
    'max-w-[min(1200px,90vw)] min-h-[600px] max-h-[90vh]':  props.size === '2xl',
    'max-w-[min(1400px,90vw)] min-h-[800px] max-h-[90vh]':  props.size === '3xl',
    'max-w-[90vw] min-h-[90vh] max-h-[90vh]':               props.size === 'full',
}));
</script>