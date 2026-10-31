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
        <!-- ✅ WRAPPER UNIFIÉ pour shape='rectangle' (même structure quelle que soit la position de l'icône) :
             div.wrapper(justify-between si icône droite)
               > div.left-part  (icône gauche + texte : toujours côte à côte)
               > div.right-part (icône droite : collée au bord droit) -->
        <div v-if="shape === 'rectangle'" :class="wrapperClasses">
            <div v-if="hasLeftIcon || hasText" :class="['left-part flex items-center', groupGapClasses]">
                <span v-if="hasLeftIcon" class="flex items-center">
                    <slot name="leftIcon" />
                </span>
                <span v-if="hasText" class="flex items-center">
                    <slot />
                </span>
            </div>
            <div v-if="hasRightIcon" :class="['right-part flex items-center shrink-0', rightIconAbsoluteClasses]">
                <slot name="rightIcon" />
            </div>
        </div>

        <!-- Fallback pour les boutons carrés (shape === 'square') -->
        <template v-else>
            <span v-if="hasLeftIcon" class="flex items-center">
                <slot name="leftIcon" />
            </span>
            <span v-if="hasText">
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
    type ButtonVariant,
    CONTROL_SIZES,
    BUTTON_RIGHT_ICON_POSITIONS,
    BUTTON_VARIANTS
} from '@genesis-labs/web-core/core/config/ui.config';

interface Props {
    disabled?: boolean;
    variant?: ButtonVariant;
    shape?: 'rectangle' | 'square';
    size?: UI_Size_Unit;
    fillWidth?: boolean;
    contentAlign?: 'left' | 'center' | 'right';
    useDefaultText?: boolean;
    useDefaultHover?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
    disabled: false,
    variant: 'neutral',
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
const hasIcon      = computed(() => hasLeftIcon.value || hasRightIcon.value);
const isEmpty      = computed(() => !hasIcon.value && !hasText.value);

const shouldShowDefaultText = computed(() =>
    props.useDefaultText && isEmpty.value && props.shape === 'rectangle'
);

// ═══ Tokens de la taille courante (échelle unique, voir ui.config.ts) ═══
const tokens = computed(() => CONTROL_SIZES[props.size]);

// ═══ Variants (Depuis la config centralisée) ═══
const variantClasses = computed(() => {
    const state = props.useDefaultHover ? 'default' : 'default';
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
// Source de vérité : le padding d'un bouton texte sans icône (tokens.px), avec ou sans icône à gauche.
const paddingClasses = computed(() => {
    if (props.shape === 'square') return '';

    // Bouton rectangle réduit à une icône (sans texte) : padding serré et symétrique
    if (hasIcon.value && !hasText.value) return tokens.value.pxWithRight;

    // Icône à droite : padding droit < padding gauche. Pas en mode centré : l'icône y est
    // positionnée en absolu et un padding asymétrique décalerait le texte hors du centre.
    const asymmetric = hasRightIcon.value && props.contentAlign !== 'center';
    return asymmetric ? tokens.value.pxAsymmetric : tokens.value.px;
});

// ═══ Gaps ═══
const wrapperGapClasses = computed(() => {
    if (props.shape !== 'rectangle') return '';
    // Gap dès qu'il y a une icône (gauche ou droite) OU si c'est un bouton icône seul
    return (hasIcon.value || !hasText.value) ? tokens.value.gap : '';
});

// Écart entre l'icône de gauche et le texte (à l'intérieur du groupe gauche)
const groupGapClasses = computed(() => tokens.value.gap);

const wrapperClasses = computed(() => {
    const isIconOnly = !hasText.value;
    const base = isIconOnly ? 'flex items-center' : 'w-full flex items-center';

    if (isIconOnly) return `${base} ${wrapperGapClasses.value}`;
    if (props.contentAlign === 'center') return `${base} justify-center relative`;
    if (props.contentAlign === 'right')  return `${base} justify-end ${wrapperGapClasses.value}`;

    // contentAlign === 'left' : icône droite collée au bord droit, sinon tout regroupé à gauche
    const justify = hasRightIcon.value ? 'justify-between' : 'justify-start';
    return `${base} ${justify} ${wrapperGapClasses.value}`;
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