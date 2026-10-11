<template>
    <div class="inline-flex flex-col gap-0" :class="inputWrapperClasses">
        <div class="inline-flex items-center gap-1" :class="inputWrapperClasses">
            <!-- Slot gauche extérieur -->
            <span v-if="hasOuterLeftSlot" class="flex items-center flex-shrink-0">
                <slot name="outer-left" />
            </span>

            <!-- Container input -->
            <div
                class="inline-flex items-center flex-1 transition-all duration-200"
                :class="[containerSizeClasses, containerShapeClasses, containerVariantClasses, { 'opacity-50': disabled }]"
            >
                <!-- Slot gauche intérieur -->
                <span v-if="hasLeftSlot" class="flex items-center flex-shrink-0 text-muted" :class="slotPaddingClasses">
                    <slot name="left" />
                </span>

                <!-- Input natif -->
                <input
                    class="flex-1 min-w-0 h-full bg-transparent outline-none text-text placeholder:text-muted disabled:cursor-not-allowed"
                    :class="inputPaddingClasses"
                    :disabled="disabled"
                    :placeholder="placeholder"
                    :type="type === 'color' || type === 'file' ? 'text' : type"
                    :value="modelValue"
                    v-bind="$attrs"
                    @input="handleInput"
                />

                <!-- Slot droit intérieur (Actions) -->
                <span v-if="hasRightContent" class="flex items-center shrink-0 text-muted" :class="slotPaddingClasses">
                    <template v-if="type === 'file'">
                        <GenesisButtonIcon :size="actionButtonSize" :class="actionButtonClass" variant="tertiary" :disabled="disabled" @click.stop="$emit('browse', accept)">
                            <IconFolder />
                        </GenesisButtonIcon>
                    </template>
                    <template v-else-if="type === 'path'">
                        <GenesisButtonIcon :size="actionButtonSize" :class="actionButtonClass" variant="tertiary" :disabled="disabled" @click.stop="$emit('request-folder-path')">
                            <IconFolder />
                        </GenesisButtonIcon>
                    </template>
                    <template v-else-if="type === 'color'">
                        <input
                            type="color"
                            class="p-0 border-0 rounded cursor-pointer bg-transparent"
                            :class="colorPickerClass"
                            :value="String(modelValue)"
                            :disabled="disabled"
                            @input="handleColorWheelInput"
                        />
                    </template>
                    <template v-else-if="multiChoice">
                        <GenesisButtonIcon :size="actionButtonSize" :class="actionButtonClass" variant="tertiary" :disabled="!modelValue" @click.stop="handleAddChoice">
                            <IconPlus />
                        </GenesisButtonIcon>
                    </template>
                    <template v-else-if="hasRightSlot">
                        <slot name="right" />
                    </template>
                </span>
            </div>

            <!-- Slot droit extérieur -->
            <span v-if="hasOuterRightSlot" class="flex items-center flex-shrink-0">
                <slot name="outer-right" />
            </span>
        </div>
    </div>
</template>

<script setup lang="ts">
import { computed, useSlots } from 'vue';
import GenesisButtonIcon from '@genesis-labs/web-core/core/components/ui/actions/GenesisButtonIcon.vue';
import IconPlus from '@genesis-labs/web-core/core/components/ui/icons/IconPlus.vue';
import IconFolder from '@genesis-labs/web-core/core/components/ui/icons/IconFolder.vue';
import { CONTROL_SIZES, FIELD_VARIANTS, INPUT_ACTION_SIZES, type UI_Variant, type UI_Size_Unit, type InputType } from '@genesis-labs/web-core/core/config/ui.config';

const props = withDefaults(defineProps<{
    modelValue?: string | number;
    placeholder?: string;
    type?: InputType;
    disabled?: boolean;
    variant?: UI_Variant;
    shape?: 'rectangle' | 'pill';
    size?: UI_Size_Unit;
    fillWidth?: boolean;
    oneLine?: boolean;
    multiChoice?: boolean;
    accept?: string;
}>(), {
    modelValue: '',
    placeholder: '',
    type: 'text',
    disabled: false,
    variant: 'neutral',
    shape: 'rectangle',
    size: 'md',
    fillWidth: false,
    oneLine: false,
    multiChoice: false,
    accept: '*',
});

const emit = defineEmits<{
    (e: 'update:modelValue', value: string | number): void;
    (e: 'add-choice', value: string): void;
    (e: 'browse', accept: string): void;
    (e: 'request-folder-path'): void;
}>();

defineOptions({ inheritAttrs: false });

const slots = useSlots();
const hasLeftSlot = computed(() => !!slots.left);
const hasRightSlot = computed(() => !!slots.right);
const hasOuterLeftSlot = computed(() => !!slots['outer-left']);
const hasOuterRightSlot = computed(() => !!slots['outer-right']);

const hasRightContent = computed(() =>
    hasRightSlot.value || props.multiChoice || props.type === 'color' || props.type === 'file' || props.type === 'path'
);

const tokens = computed(() => CONTROL_SIZES[props.size]);
const fieldTokens = computed(() => FIELD_VARIANTS[props.variant] ?? FIELD_VARIANTS.neutral);

const containerSizeClasses = computed(() => `${tokens.value.box} ${tokens.value.text} ${tokens.value.icon}`);
const containerShapeClasses = computed(() => ({ rectangle: 'rounded', pill: 'rounded-full' }[props.shape]));
const containerVariantClasses = computed(() => fieldTokens.value.container);

const inputPaddingClasses = computed(() => {
    return `${hasLeftSlot.value ? '' : tokens.value.pl} ${hasRightContent.value ? '' : tokens.value.pr}`.trim();
});

const slotPaddingClasses = computed(() => tokens.value.slotPx);
const actionButtonSize = computed(() => INPUT_ACTION_SIZES[props.size]);
const actionButtonClass = computed(() => props.size === 'xs' ? '!w-5 !h-5' : '');
const colorPickerClass = computed(() => props.size === 'xs' ? 'w-5 h-5' : 'w-6 h-6');

const inputWrapperClasses = computed(() => {
    if (props.fillWidth && props.oneLine) return 'flex-1 min-w-0';
    return props.fillWidth ? 'w-full' : 'w-fit';
});

function handleInput(event: Event) {
    const target = event.target as HTMLInputElement;
    emit('update:modelValue', props.type === 'number' && target.value !== '' ? Number(target.value) : target.value);
}

function handleColorWheelInput(event: Event) {
    emit('update:modelValue', (event.target as HTMLInputElement).value);
}

function handleAddChoice() {
    if (!props.modelValue) return;
    emit('add-choice', String(props.modelValue));
    emit('update:modelValue', '');
}
</script>