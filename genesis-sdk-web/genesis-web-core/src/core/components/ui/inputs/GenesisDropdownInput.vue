<template>
    <div class="relative w-full">
        <GenesisDropdown
            trigger-variant="none"
            :trigger-class="selectTriggerClasses"
            :menu-class="fieldMenuClasses"
            :trigger-size="size"
            :trigger-disabled="disabled"
            match-trigger-width
            :align="'left'"
            :force-down="true"
        >
            <template #trigger>
                <span class="truncate">
                    {{ selectedOptionLabel || modelValue || placeholder || 'Sélectionner...' }}
                </span>
            </template>
            <template #default="{ close }">
                <template v-if="options && options.length > 0">
                    <div class="p-1 space-y-1">
                        <button
                            v-for="opt in options"
                            :key="getOptionValue(opt)"
                            type="button"
                            class="w-full text-left px-3 py-2 text-sm text-text hover:bg-bg-secondary hover:text-secondary rounded-md transition-colors"
                            :class="{ 'text-accent font-medium': getOptionValue(opt) === modelValue }"
                            @click="() => handleDropdownSelect(opt, close)"
                        >
                            {{ getOptionLabel(opt) }}
                        </button>
                    </div>
                </template>
                <slot v-else :close="close" />
            </template>
        </GenesisDropdown>

        <div v-if="multiChoice && choices && choices.length > 0" class="flex flex-wrap gap-1.5 mt-1.5">
            <GenesisLabel v-for="(choice, index) in choices" :key="index" :text="choice" @remove="$emit('remove-choice', choice)" />
        </div>
    </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import GenesisDropdown from '@genesis-labs/web-core/core/components/ui/dropdown/GenesisDropdown.vue';
import GenesisLabel from '@genesis-labs/web-core/core/components/ui/labels/GenesisLabel.vue';
import { FIELD_VARIANTS, type UI_Variant, type UI_Size_Unit } from '@genesis-labs/web-core/core/config/ui.config';

const props = withDefaults(defineProps<{
    modelValue?: string | number | string[];
    placeholder?: string;
    disabled?: boolean;
    variant?: UI_Variant;
    shape?: 'rectangle' | 'pill';
    size?: UI_Size_Unit;
    options?: { label: string, value: any }[];
    multiChoice?: boolean;
    choices?: string[];
}>(), {
    modelValue: undefined,
    placeholder: '',
    disabled: false,
    variant: 'neutral',
    shape: 'rectangle',
    size: 'md',
    options: () => [],
    multiChoice: false,
    choices: () => [],
});

const emit = defineEmits<{
    (e: 'update:modelValue', value: any): void;
    (e: 'remove-choice', value: string): void;
}>();

defineOptions({ inheritAttrs: false });

const containerShapeClasses = computed(() => ({ rectangle: 'rounded', pill: 'rounded-full' }[props.shape]));
const fieldTokens = computed(() => FIELD_VARIANTS[props.variant] ?? FIELD_VARIANTS.neutral);

const selectTriggerClasses = computed(() => `${fieldTokens.value.container} ${fieldTokens.value.trigger} ${containerShapeClasses.value}`);
const fieldMenuClasses = computed(() => fieldTokens.value.menu);

const selectedOptionLabel = computed(() => {
    if (!props.options || props.modelValue === undefined || props.modelValue === null) return '';
    if (Array.isArray(props.modelValue)) {
        return props.modelValue.length > 0 ? `${props.modelValue.length} élément(s)` : '';
    }
    const selected = props.options.find(opt => getOptionValue(opt) === props.modelValue);
    return selected ? getOptionLabel(selected) : '';
});

function getOptionLabel(option: any): string {
    return typeof option === 'string' ? option : (option.label || String(option));
}

function getOptionValue(option: any): any {
    return typeof option === 'string' ? option : (option.value !== undefined ? option.value : option);
}

function handleDropdownSelect(option: any, close: () => void) {
    emit('update:modelValue', getOptionValue(option));
    close();
}
</script>