<template>
    <Disclosure
        v-slot="{ open }"
        :default-open="defaultOpen"
        as="div"
        class="w-full"
        v-bind="$attrs"
    >
        <!-- ═══ Header / Bouton ═══ -->
        <DisclosureButton
            class="flex justify-between items-center w-full text-left text-sm font-medium text-text
                   px-4 py-3 rounded-lg
                   focus:outline-none focus-visible:ring-2 focus-visible:ring-accent/50
                   transition-all duration-200"
            :class="headerVariantClasses"
        >
            <!-- Slot title custom ou texte par défaut -->
            <span class="flex items-center gap-2">
                <slot name="title">{{ title }}</slot>
            </span>

            <!-- Chevron -->
            <IconChevronDown
                class="flex-shrink-0 transition-transform duration-200"
                :class="{ 'rotate-180': open }"
            />
        </DisclosureButton>

        <!-- ═══ Panel ═══ -->
        <transition
            enter-active-class="transition duration-200 ease-out"
            enter-from-class="transform scale-95 opacity-0"
            enter-to-class="transform scale-100 opacity-100"
            leave-active-class="transition duration-150 ease-in"
            leave-from-class="transform scale-100 opacity-100"
            leave-to-class="transform scale-95 opacity-0"
        >
            <!--  Padding dynamique appliqué via la classe calculée -->
            <DisclosurePanel
                class="px-4 pb-4"
                :class="[paddingClasses, contentClass]"
            >
                <slot />
            </DisclosurePanel>
        </transition>
    </Disclosure>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { Disclosure, DisclosureButton, DisclosurePanel } from '@headlessui/vue';
import IconChevronDown from '@genesis-labs/web-core/core/components/ui/icons/IconChevronDown.vue';

interface Props {
    title: string;
    defaultOpen?: boolean;
    variant?: 'primary' | 'secondary' | 'transparent'; //  Ajout de 'transparent'
    contentClass?: string;
    padding?: 'sm' | 'md' | 'lg'; //  Nouvelle prop pour le padding-top
}

const props = withDefaults(defineProps<Props>(), {
    defaultOpen: false,
    variant: 'transparent', //  Valeur par défaut changée
    contentClass: '',
    padding: 'sm', //  Valeur par défaut (pt-2)
});

// ═══ Variant du header ═══
const headerVariantClasses = computed(() => {
    if (props.variant === 'primary') {
        return 'bg-bg-light hover:bg-secondary/50 border border-secondary';
    }
    if (props.variant === 'secondary') {
        return 'bg-bg-ghost hover:bg-secondary/30';
    }
    //  Fallback pour transparent (valeur par défaut)
    return 'bg-transparent hover:bg-secondary/30';
});

// ═══ Classes de padding dynamique ═══
const paddingClasses = computed(() => {
    switch (props.padding) {
        case 'sm': return 'pt-2';
        case 'md': return 'pt-4';
        case 'lg': return 'pt-6';
        default: return 'pt-2';
    }
});

defineOptions({ inheritAttrs: false });
</script>