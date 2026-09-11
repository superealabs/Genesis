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
import GenesisButton from '@/core/components/ui/actions/GenesisButton.vue';

interface Props {
    disabled?: boolean;
    variant?: 'primary' | 'secondary' | 'tertiary';
    shape?: 'rectangle' | 'square' | 'circle';
    size?: 'xs' | 'sm' | 'md' | 'lg' | 'xl' | '2xl';
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