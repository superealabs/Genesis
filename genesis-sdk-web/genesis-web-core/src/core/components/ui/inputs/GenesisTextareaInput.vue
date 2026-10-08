<template>
    <div
        class="flex flex-col min-h-0 overflow-hidden rounded-lg relative transition-all duration-200"
        :class="[containerVariantClasses, { 'opacity-50': disabled }]"
        :style="resizeStyle"
    >
        <textarea
            class="flex-1 w-full min-h-0 bg-transparent outline-none text-text placeholder:text-muted disabled:cursor-not-allowed p-3 custom-scrollbar resize-none"
            :disabled="disabled"
            :placeholder="placeholder"
            :value="String(modelValue ?? '')"
            v-bind="$attrs"
            @input="handleInput"
        />
        <GenesisResizeHandle @resize-start="startResizeBottom" />
    </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { useResizable } from '@genesis-labs/web-core/core/composables/ux/useResizable.ts';
import GenesisResizeHandle from '../actions/GenesisResizeHandle.vue';
import { FIELD_VARIANTS, type UI_Variant, type UI_Size_Unit } from '@genesis-labs/web-core/core/config/ui.config';

const props = withDefaults(defineProps<{
    modelValue?: string | number;
    placeholder?: string;
    disabled?: boolean;
    variant?: UI_Variant;
    shape?: 'rectangle' | 'pill';
    size?: UI_Size_Unit;
}>(), {
    modelValue: '',
    placeholder: '',
    disabled: false,
    variant: 'neutral',
    shape: 'rectangle',
    size: 'md',
});

const emit = defineEmits<{ (e: 'update:modelValue', value: string): void }>();
defineOptions({ inheritAttrs: false });

const MIN_HEIGHT = 100;
const { resizeStyle, startResizeBottom } = useResizable({
    minHeight: MIN_HEIGHT,
    maxHeight: () => MIN_HEIGHT * 2,
    resizableX: ref(false),
    resizableY: ref(true)
});

const fieldTokens = computed(() => FIELD_VARIANTS[props.variant] ?? FIELD_VARIANTS.neutral);
const containerVariantClasses = computed(() => fieldTokens.value.container);

function handleInput(event: Event) {
    const target = event.target as HTMLTextAreaElement;
    emit('update:modelValue', target.value);
}
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar { width: 6px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background-color: var(--color-secondary, #cbd5e1); border-radius: 3px; }
</style>