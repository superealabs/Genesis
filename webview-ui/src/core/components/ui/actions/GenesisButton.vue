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
            ghostClasses,
            fillWidthClasses,
            layoutClasses
        ]"
        :disabled="disabled"
        v-bind="$attrs"
    >
        <!-- ═══════════════════════════════════════════════════════════ -->
        <!-- MODE WRAPPER : Quand rightIcon + rectangle -->
        <!-- Deux parties séparées (gauche | droite) -->
        <!-- ═══════════════════════════════════════════════════════════ -->
        <template v-if="hasRightIcon && shape === 'rectangle'">
            <div :class="wrapperClasses">
                <!-- Partie gauche : leftIcon + texte (optionnels) -->
                <span class="left-part flex items-center gap-2">
                    <span v-if="hasLeftIcon" class="flex items-center">
                        <slot name="leftIcon" />
                    </span>
                    <span v-if="hasText">
                        <slot />
                    </span>
                </span>
                
                <!-- Partie droite : rightIcon -->
                <span :class="['right-part flex items-center', rightIconAbsoluteClasses]">
                    <slot name="rightIcon" />
                </span>
            </div>
        </template>

        <!-- ═══════════════════════════════════════════════════════════ -->
        <!-- MODE SIMPLE : Tous les autres cas -->
        <!-- Contenu linéaire (leftIcon, texte, rightIcon) -->
        <!-- ═══════════════════════════════════════════════════════════ -->
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

            <!-- Fallback : texte par défaut si tout est vide -->
            <span v-if="shouldShowDefaultText">
                button
            </span>
        </template>
    </button>
</template>

<script setup lang="ts">
import { computed, useSlots } from 'vue';

interface Props {
    disabled?: boolean;
    variant?: 'primary' | 'secondary';
    shape?: 'rectangle' | 'square' | 'circle';
    size?: 'xs' | 'sm' | 'md' | 'lg' | 'xl' | '2xl';
    visibleBackground?: boolean;
    fillWidth?: boolean;
    contentAlign?: 'left' | 'center' | 'right';
    useDefaultText?: boolean;
    /**
     * Active/désactive le hover par défaut du bouton :
     * - true (défaut) : changement de background au hover
     * - false : pas de changement de background au hover
     * Utile quand on veut un effet hover custom (ex: juste l'icône qui change).
     * @default true
     */
    useDefaultHover?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
    disabled: false,
    variant: 'primary',
    shape: 'rectangle',
    size: 'md',
    visibleBackground: true,
    fillWidth: false,
    contentAlign: 'left',
    useDefaultText: true,
    useDefaultHover: true  // ← AJOUTÉ
});

const slots = useSlots();

// ═══════════════════════════════════════════════════════════
// DÉTECTION DU CONTENU (les 6 modes)
// ═══════════════════════════════════════════════════════════

/** Icône à gauche présente */
const hasLeftIcon = computed(() => !!slots.leftIcon);

/** Texte présent (slot default) */
const hasText = computed(() => !!slots.default?.());

/** Icône à droite présente */
const hasRightIcon = computed(() => !!slots.rightIcon);

/** Aucun contenu fourni */
const isEmpty = computed(() => !hasLeftIcon.value && !hasText.value && !hasRightIcon.value);

/**
 * Faut-il afficher le texte par défaut "button" ?
 * - useDefaultText doit être true
 * - Le contenu doit être vide
 * - La forme doit être rectangle (square/circle n'ont pas de texte)
 */
const shouldShowDefaultText = computed(() => {
    return props.useDefaultText && isEmpty.value && props.shape === 'rectangle';
});

// ═══════════════════════════════════════════════════════════
// CLASSES DE STYLE (inchangées)
// ═══════════════════════════════════════════════════════════

const variantClasses = computed(() => {
    if (!props.visibleBackground) {
        return { 'bg-transparent text-text border-none': true };
    }

    // Si hover désactivé → pas de classes hover
    if (!props.useDefaultHover) {
        return {
            'bg-accent text-bg border-none': props.variant === 'primary',
            'bg-transparent text-text border border-secondary': props.variant === 'secondary'
        };
    }

    // Comportement normal avec hover
    return {
        'bg-accent text-bg hover:bg-accent/80 hover:shadow-lg border-none disabled:hover:bg-accent disabled:hover:shadow-none': props.variant === 'primary',
        'bg-transparent text-text border border-secondary hover:bg-secondary disabled:hover:bg-transparent': props.variant === 'secondary'
    };
});

const shapeClasses = computed(() => ({
    'rounded': props.shape === 'rectangle',
    'aspect-square rounded overflow-hidden min-w-0 min-h-0': props.shape === 'square',
    'aspect-square rounded-full overflow-hidden min-w-0 min-h-0': props.shape === 'circle'
}));

const sizeClasses = computed(() => {
    const config = {
        rectangle: {
            xs: 'text-[10px] h-fit [&_svg]:!w-3.5 [&_svg]:!h-3.5 [&_.right-part_svg]:!w-2.5 [&_.right-part_svg]:!h-2.5',
            sm: 'text-xs h-fit [&_svg]:!w-4 [&_svg]:!h-4 [&_.right-part_svg]:!w-3 [&_.right-part_svg]:!h-3',
            md: 'text-sm h-fit [&_svg]:!w-5 [&_svg]:!h-5 [&_.right-part_svg]:!w-4 [&_.right-part_svg]:!h-4',
            lg: 'text-base h-fit [&_svg]:!w-6 [&_svg]:!h-6 [&_.right-part_svg]:!w-5 [&_.right-part_svg]:!h-5',
            xl: 'text-lg h-fit [&_svg]:!w-7 [&_svg]:!h-7 [&_.right-part_svg]:!w-6 [&_.right-part_svg]:!h-6',
            '2xl': 'text-xl h-fit [&_svg]:!w-8 [&_svg]:!h-8 [&_.right-part_svg]:!w-7 [&_.right-part_svg]:!h-7'
        },
        square: {
            xs: 'w-6 h-6 [&_svg]:!w-3.5 [&_svg]:!h-3.5',
            sm: 'w-7 h-7 [&_svg]:!w-4 [&_svg]:!h-4',
            md: 'w-8 h-8 [&_svg]:!w-5 [&_svg]:!h-5',
            lg: 'w-9 h-9 [&_svg]:!w-6 [&_svg]:!h-6',
            xl: 'w-10 h-10 [&_svg]:!w-7 [&_svg]:!h-7',
            '2xl': 'w-11 h-11 [&_svg]:!w-8 [&_svg]:!h-8'
        },
        circle: {
            xs: 'w-6 h-6 [&_svg]:!w-3.5 [&_svg]:!h-3.5',
            sm: 'w-7 h-7 [&_svg]:!w-4 [&_svg]:!h-4',
            md: 'w-8 h-8 [&_svg]:!w-5 [&_svg]:!h-5',
            lg: 'w-9 h-9 [&_svg]:!w-6 [&_svg]:!h-6',
            xl: 'w-10 h-10 [&_svg]:!w-7 [&_svg]:!h-7',
            '2xl': 'w-11 h-11 [&_svg]:!w-8 [&_svg]:!h-8'
        }
    };
    
    return config[props.shape][props.size];
});

const paddingClasses = computed(() => {
    if (props.shape === 'square' || props.shape === 'circle') return '';
    
    if (hasRightIcon.value) {
        return {
            xs: 'px-1.5 py-0.5',
            sm: 'px-2 py-1',
            md: 'px-2.5 py-1.5',
            lg: 'px-3 py-2',
            xl: 'px-3.5 py-2.5',
            '2xl': 'px-4 py-3'
        }[props.size];
    }
    
    return {
        xs: 'px-2 py-0.5',
        sm: 'px-3 py-1',
        md: 'px-4 py-1.5',
        lg: 'px-5 py-2',
        xl: 'px-6 py-2.5',
        '2xl': 'px-7 py-3'
    }[props.size];
});

// ═══ Gap du wrapper : petit si icône+icône, grand si texte ═══
// ═══ Gap du wrapper ═══
const wrapperGapClasses = computed(() => {
    if (!hasRightIcon.value || props.shape !== 'rectangle') return '';
    
    // Cas "icon-icon" : gap compact
    if (!hasText.value) {
        return {
            xs: 'gap-0.5',   // 2px
            sm: 'gap-1',     // 4px
            md: 'gap-1.5',   // 6px
            lg: 'gap-2',     // 8px
            xl: 'gap-2.5',   // 10px
            '2xl': 'gap-3'   // 12px
        }[props.size];
    }
    
    // Cas avec texte : gap large (4× padding)
    return {
        xs: 'gap-6',
        sm: 'gap-8',
        md: 'gap-10',
        lg: 'gap-12',
        xl: 'gap-14',
        '2xl': 'gap-16'
    }[props.size];
});

// ═══ Classes du wrapper selon le contenu et contentAlign ═══
const wrapperClasses = computed(() => {
    const isIconOnly = !hasText.value;
    const base = isIconOnly ? 'flex items-center' : 'w-full flex items-center';
    
    // Cas icon-icon : pas de w-full, gap naturel suffit
    if (isIconOnly) {
        return `${base} ${wrapperGapClasses.value}`;
    }
    
    // Cas avec texte : utiliser contentAlign
    if (props.contentAlign === 'left') {
        return `${base} justify-between ${wrapperGapClasses.value}`;
    }
    
    if (props.contentAlign === 'center') {
        return `${base} justify-center relative`;
    }
    
    return `${base} justify-end ${wrapperGapClasses.value}`;
});

const rightIconAbsoluteClasses = computed(() => {
    if (props.contentAlign !== 'center' || props.shape !== 'rectangle' || !hasRightIcon.value) {
        return '';
    }
    
    return {
        xs: 'absolute right-1.5',
        sm: 'absolute right-2',
        md: 'absolute right-2.5',
        lg: 'absolute right-3',
        xl: 'absolute right-3.5',
        '2xl': 'absolute right-4'
    }[props.size];
});

const ghostClasses = computed(() => {
    // Pas de background visible → pas de ghostClasses
    if (props.visibleBackground) return '';
    
    // Hover désactivé → pas de background au hover
    if (!props.useDefaultHover) return '';
    
    // Comportement normal : overlay ghost au hover
    return 'hover:bg-[var(--color-hover-ghost)]';
});

const fillWidthClasses = computed(() => {
    if (!props.fillWidth || props.shape !== 'rectangle') return '';
    return 'w-full';
});

const layoutClasses = computed(() => {
    if (props.shape === 'square' || props.shape === 'circle') {
        return 'justify-center';
    }
    return '';
});

defineOptions({
    inheritAttrs: false,
});
</script>