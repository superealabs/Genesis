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

interface Props {
    disabled?: boolean;
    variant?: 'primary' | 'secondary' | 'tertiary';
    shape?: 'rectangle' | 'square' | 'circle';
    size?: 'xs' | 'sm' | 'md' | 'lg' | 'xl' | '2xl';
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

// ═══ Variants — primary, secondary, tertiary ═══
const variantClasses = computed(() => {
    // Hover désactivé → pas de classes hover sur aucun variant
    const hover = props.useDefaultHover;

    if (props.variant === 'primary') {
        return hover
            ? 'bg-accent text-bg border-none hover:bg-accent/80 hover:shadow-lg disabled:hover:bg-accent disabled:hover:shadow-none'
            : 'bg-accent text-bg border-none';
    }

    if (props.variant === 'secondary') {
        return hover
            ? 'bg-transparent text-text border border-secondary hover:bg-secondary disabled:hover:bg-transparent'
            : 'bg-transparent text-text border border-secondary';
    }

    // tertiary : pas de bg, pas de border — juste texte/icône + ghost hover optionnel
    return hover
        ? 'bg-transparent text-text border-none hover:bg-[var(--color-hover-ghost)]'
        : 'bg-transparent text-text border-none';
});

const shapeClasses = computed(() => ({
    'rounded': props.shape === 'rectangle',
    'aspect-square rounded overflow-hidden min-w-0 min-h-0': props.shape === 'square',
    'aspect-square rounded-full overflow-hidden min-w-0 min-h-0': props.shape === 'circle'
}));

const sizeClasses = computed(() => {
    const config = {
        rectangle: {
            xs:  'text-[10px] h-fit [&_svg]:!w-3.5 [&_svg]:!h-3.5 [&_.right-part_svg]:!w-2.5 [&_.right-part_svg]:!h-2.5',
            sm:  'text-xs h-fit [&_svg]:!w-4 [&_svg]:!h-4 [&_.right-part_svg]:!w-3 [&_.right-part_svg]:!h-3',
            md:  'text-sm h-fit [&_svg]:!w-5 [&_svg]:!h-5 [&_.right-part_svg]:!w-4 [&_.right-part_svg]:!h-4',
            lg:  'text-base h-fit [&_svg]:!w-6 [&_svg]:!h-6 [&_.right-part_svg]:!w-5 [&_.right-part_svg]:!h-5',
            xl:  'text-lg h-fit [&_svg]:!w-7 [&_svg]:!h-7 [&_.right-part_svg]:!w-6 [&_.right-part_svg]:!h-6',
            '2xl': 'text-xl h-fit [&_svg]:!w-8 [&_svg]:!h-8 [&_.right-part_svg]:!w-7 [&_.right-part_svg]:!h-7'
        },
        square: {
            xs:  'w-6 h-6 [&_svg]:!w-3.5 [&_svg]:!h-3.5',
            sm:  'w-7 h-7 [&_svg]:!w-4 [&_svg]:!h-4',
            md:  'w-8 h-8 [&_svg]:!w-5 [&_svg]:!h-5',
            lg:  'w-9 h-9 [&_svg]:!w-6 [&_svg]:!h-6',
            xl:  'w-10 h-10 [&_svg]:!w-7 [&_svg]:!h-7',
            '2xl': 'w-11 h-11 [&_svg]:!w-8 [&_svg]:!h-8'
        },
        circle: {
            xs:  'w-6 h-6 [&_svg]:!w-3.5 [&_svg]:!h-3.5',
            sm:  'w-7 h-7 [&_svg]:!w-4 [&_svg]:!h-4',
            md:  'w-8 h-8 [&_svg]:!w-5 [&_svg]:!h-5',
            lg:  'w-9 h-9 [&_svg]:!w-6 [&_svg]:!h-6',
            xl:  'w-10 h-10 [&_svg]:!w-7 [&_svg]:!h-7',
            '2xl': 'w-11 h-11 [&_svg]:!w-8 [&_svg]:!h-8'
        }
    };
    return config[props.shape][props.size];
});

const paddingClasses = computed(() => {
    if (props.shape === 'square' || props.shape === 'circle') return '';
    return hasRightIcon.value
        ? ({ xs: 'px-1.5 py-0.5', sm: 'px-2 py-1', md: 'px-2.5 py-1.5', lg: 'px-3 py-2', xl: 'px-3.5 py-2.5', '2xl': 'px-4 py-3' })[props.size]
        : ({ xs: 'px-2 py-0.5',   sm: 'px-3 py-1', md: 'px-4 py-1.5',   lg: 'px-5 py-2', xl: 'px-6 py-2.5',   '2xl': 'px-7 py-3' })[props.size];
});

const wrapperGapClasses = computed(() => {
    if (!hasRightIcon.value || props.shape !== 'rectangle') return '';
    return !hasText.value
        ? ({ xs: 'gap-0.5', sm: 'gap-1', md: 'gap-1.5', lg: 'gap-2', xl: 'gap-2.5', '2xl': 'gap-3' })[props.size]
        : ({ xs: 'gap-6',   sm: 'gap-8', md: 'gap-10',  lg: 'gap-12', xl: 'gap-14',  '2xl': 'gap-16' })[props.size];
});

const wrapperClasses = computed(() => {
    const isIconOnly = !hasText.value;
    const base = isIconOnly ? 'flex items-center' : 'w-full flex items-center';
    if (isIconOnly) return `${base} ${wrapperGapClasses.value}`;
    if (props.contentAlign === 'center') return `${base} justify-center relative`;
    if (props.contentAlign === 'right')  return `${base} justify-end ${wrapperGapClasses.value}`;
    return `${base} justify-between ${wrapperGapClasses.value}`;
});

const rightIconAbsoluteClasses = computed(() => {
    if (props.contentAlign !== 'center' || props.shape !== 'rectangle' || !hasRightIcon.value) return '';
    return ({ xs: 'absolute right-1.5', sm: 'absolute right-2', md: 'absolute right-2.5', lg: 'absolute right-3', xl: 'absolute right-3.5', '2xl': 'absolute right-4' })[props.size];
});

const fillWidthClasses = computed(() =>
    props.fillWidth && props.shape === 'rectangle' ? 'w-full' : ''
);

const layoutClasses = computed(() =>
    props.shape === 'square' || props.shape === 'circle' ? 'justify-center' : ''
);

defineOptions({ inheritAttrs: false });
</script>