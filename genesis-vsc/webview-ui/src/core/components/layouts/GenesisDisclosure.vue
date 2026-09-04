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
            <DisclosurePanel
                class="px-4 pb-4 pt-2 border-t border-secondary/50"
                :class="contentClass"
            >
                <slot />
            </DisclosurePanel>
        </transition>
    </Disclosure>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { Disclosure, DisclosureButton, DisclosurePanel } from '@headlessui/vue';
import IconChevronDown from '@/core/components/ui/icons/IconChevronDown.vue';

interface Props {
    title: string;
    defaultOpen?: boolean;
    variant?: 'primary' | 'secondary';
    contentClass?: string;
}

const props = withDefaults(defineProps<Props>(), {
    defaultOpen: false,
    variant: 'secondary',
    contentClass: '',
});

// ═══ Variant du header ═══
const headerVariantClasses = computed(() => {
    if (props.variant === 'primary') {
        return 'bg-bg-light hover:bg-secondary/50 border border-secondary';
    }
    return 'bg-transparent hover:bg-secondary/30';
});

defineOptions({ inheritAttrs: false });
</script>