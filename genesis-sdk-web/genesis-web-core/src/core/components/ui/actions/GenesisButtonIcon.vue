<template>
    <GenesisButton
        :variant="variant"
        :shape="shape"
        :size="size"
        :disabled="disabled"
        :use-default-hover="!hoverHimself"
        :class="{ 'hover:[&_svg]:text-text transition-colors': hoverHimself }"
        v-bind="$attrs"
    >
        <template #leftIcon>
            <slot />
        </template>
    </GenesisButton>
</template>

<script setup lang="ts">
import GenesisButton from '@genesis-labs/web-core/core/components/ui/actions/GenesisButton.vue';
import type { ButtonVariant, UI_Size_Unit } from '@genesis-labs/web-core/core/config/ui.config';

interface Props {
    disabled?: boolean;
    variant?: ButtonVariant;
    shape?: 'rectangle' | 'square';
    size?: UI_Size_Unit;
    /**
     * Change le comportement du hover :
     * - false (défaut) : le background du bouton change au hover
     * - true : l'icône SVG change de couleur au hover,
     *   ET le hover de background est désactivé
     * @default false
     */
    hoverHimself?: boolean;
}

withDefaults(defineProps<Props>(), {
    disabled: false,
    variant: 'primary',
    shape: 'square',
    size: 'md',
    hoverHimself: false
});

defineOptions({ inheritAttrs: false });
</script>