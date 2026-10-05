<template>
    <button
        class="inline-flex items-center gap-2
               cursor-pointer
               transition-all duration-200
               focus:outline-none focus:ring-accent focus:ring-offset-2 focus:ring-offset-bg
               disabled:opacity-50 disabled:cursor-not-allowed
               whitespace-nowrap
               [&_svg]:flex-shrink-0"
        :class="[
            variantClasses,
            shapeClasses,
            sizeClasses,
            paddingClasses,
            fillWidthClasses,
            layoutClasses
        ]"
        :disabled="disabled"
        v-bind="$attrs"
    >
        <template v-if="hasRightIcon && shape === 'rectangle'">
            <div :class="wrapperClasses">
                <span class="left-part flex items-center gap-2">
                    <span v-if="hasLeftIcon" class="flex items-center">
                        <slot name="leftIcon" />
                    </span>
                    <span v-if="hasText">
                        <slot />
                    </span>
                </span>
                <span :class="['right-part flex items-center', rightIconAbsoluteClasses]">
                    <slot name="rightIcon" />
                </span>
            </div>
        </template>

        <template v-else>
            <span v-if="hasLeftIcon" class="flex items-center">
                <slot name="leftIcon" />
            </span>
            <span v-if="hasText && shape === 'rectangle'">
                <slot />
            </span>
            <span v-if="hasRightIcon" class="flex items-center">
                <slot name="rightIcon" />
            </span>
            <span v-if="shouldShowDefaultText">button</span>
        </template>
    </button>
</template>

<script setup lang="ts">
import { computed, useSlots } from 'vue';
import {
    type UI_Size_Unit,
    type UI_Variant,
    CONTROL_SIZES,
    BUTTON_RIGHT_ICON_POSITIONS,
    BUTTON_VARIANTS
} from '@genesis-labs/web-core/core/config/ui.config';

interface Props {
    disabled?: boolean;
    variant?: UI_Variant;
    shape?: 'rectangle' | 'square';
    size?: UI_Size_Unit;
    fillWidth?: boolean;
    contentAlign?: 'left' | 'center' | 'right';
    useDefaultText?: boolean;
    useDefaultHover?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
    disabled: false,
    variant: 'primary',
    shape: 'rectangle',
    size: 'md',
    fillWidth: false,
    contentAlign: 'left',
    useDefaultText: true,
    useDefaultHover: true
});

const slots = useSlots();

const hasLeftIcon  = computed(() => !!slots.leftIcon);
const hasText      = computed(() => !!slots.default?.());
const hasRightIcon = computed(() => !!slots.rightIcon);
const isEmpty      = computed(() => !hasLeftIcon.value && !hasText.value && !hasRightIcon.value);

const shouldShowDefaultText = computed(() =>
    props.useDefaultText && isEmpty.value && props.shape === 'rectangle'
);

// ═══ Tokens de la taille courante (échelle unique, voir ui.config.ts) ═══
const tokens = computed(() => CONTROL_SIZES[props.size]);

// ═══ Variants (Depuis la config centralisée) ═══
const variantClasses = computed(() => {
    const state = props.useDefaultHover ? 'hover' : 'default';
    return BUTTON_VARIANTS[props.variant][state];
});

// ═══ Shapes ═══
const shapeClasses = computed(() => ({
    'rounded': props.shape === 'rectangle',
    'aspect-square rounded overflow-hidden min-w-0 min-h-0': props.shape === 'square'
}));

// ═══ Sizes : hauteur IMPOSÉE (rectangle) ou côté imposé (carré) ═══
const sizeClasses = computed(() => {
    const t = tokens.value;
    return props.shape === 'square'
        ? `${t.square} ${t.icon}`
        : `${t.box} ${t.text} ${t.icon} ${t.rightIcon}`;
});

// ═══ Paddings : horizontal uniquement (la hauteur est imposée, pas de py) ═══
const paddingClasses = computed(() => {
    if (props.shape === 'square') return '';
    return hasRightIcon.value ? tokens.value.pxWithRight : tokens.value.px;
});

// ═══ Gaps ═══
const wrapperGapClasses = computed(() => {
    if (!hasRightIcon.value || props.shape !== 'rectangle') return '';
    return tokens.value.gap;
});

const wrapperClasses = computed(() => {
    const isIconOnly = !hasText.value;
    const base = isIconOnly ? 'flex items-center' : 'w-full flex items-center';
    if (isIconOnly) return `${base} ${wrapperGapClasses.value}`;
    if (props.contentAlign === 'center') return `${base} justify-center relative`;
    if (props.contentAlign === 'right')  return `${base} justify-end ${wrapperGapClasses.value}`;
    return `${base} justify-between ${wrapperGapClasses.value}`;
});

// ═══ Right Icon Position (Depuis la config centralisée) ═══
const rightIconAbsoluteClasses = computed(() => {
    if (props.contentAlign !== 'center' || props.shape !== 'rectangle' || !hasRightIcon.value) return '';
    return BUTTON_RIGHT_ICON_POSITIONS[props.size];
});

const fillWidthClasses = computed(() =>
    props.fillWidth && props.shape === 'rectangle' ? 'w-full' : ''
);

// ═══ Layout ═══
const layoutClasses = computed(() => {
    if (props.shape === 'square') return 'justify-center';
    return '';
});

defineOptions({ inheritAttrs: false });
</script>