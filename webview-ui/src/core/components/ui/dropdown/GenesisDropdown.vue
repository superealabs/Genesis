<template>
    <div 
        class="relative inline-block" 
        ref="containerRef"
        @mouseenter="handleMouseEnter"
        @mouseleave="handleMouseLeave"
    >

    <!-- Cas 1 : Slot trigger présent → GenesisButton avec texte + icônes -->
    <template v-if="$slots.trigger">
        <GenesisButton
            :variant="triggerVariant"
            :size="triggerSize"
            :visibleBackground="triggerVisibleBackground"
            :disabled="triggerDisabled"
            :use-default-text="false"
            @click="toggle"
        >
            <template v-if="$slots.triggerIcon" #leftIcon>
                <slot name="triggerIcon" />
            </template>
            
            <slot name="trigger" />
            
            <template #rightIcon>
                <IconChevronDown 
                    v-if="!hideChevron"
                    class="transition-transform duration-200"
                    :class="{ 'rotate-180': isOpen }"
                />
            </template>
        </GenesisButton>
    </template>

    <!-- Cas 2 : Pas de texte mais chevron visible → GenesisButton avec icône + chevron -->
    <template v-else-if="!hideChevron">
        <GenesisButton
            :variant="triggerVariant"
            :size="triggerSize"
            :visibleBackground="triggerVisibleBackground"
            :disabled="triggerDisabled"
            :use-default-text="false"
            @click="toggle"
        >
            <template v-if="$slots.triggerIcon" #leftIcon>
                <slot name="triggerIcon" />
            </template>
            
            <template #rightIcon>
                <IconChevronDown 
                    class="transition-transform duration-200"
                    :class="{ 'rotate-180': isOpen }"
                />
            </template>
        </GenesisButton>
    </template>

    <!-- Cas 3 : Pas de texte ET chevron masqué → GenesisButtonIcon (icône seule) -->
    <template v-else>
        <GenesisButtonIcon
            :variant="triggerVariant"
            :size="triggerSize"
            :visibleBackground="triggerVisibleBackground"
            :disabled="triggerDisabled"
            @click="toggle"
        >
            <slot name="triggerIcon" />
        </GenesisButtonIcon>
    </template>

        <!-- Liste -->
        <GenesisDropdownList
            v-if="isOpen"
            :position="resolvedPosition"
            :size="dropdownSize"
            :style="dropdownStyle"
        >
            <div @click="handleItemClick">
                <slot />
            </div>
        </GenesisDropdownList>

    </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, provide, nextTick } from 'vue';
import GenesisDropdownList from '@/core/components/ui/dropdown/GenesisDropdownList.vue';
import GenesisButton from '@/core/components/ui/actions/GenesisButton.vue';
import GenesisButtonIcon from '@/core/components/ui/actions/GenesisButtonIcon.vue';
import IconChevronDown from '@/core/components/ui/icons/IconChevronDown.vue';

const props = withDefaults(defineProps<{
    position?: 'top-left' | 'top-right' | 'bottom-left' | 'bottom-right';
    align?: 'left' | 'right';
    dropdownSize?: 'sm' | 'md' | 'lg' | 'xl' | '2xl' | '3xl';
    closeOnSelect?: boolean;
    openAtHover?: boolean;
    hideChevron?: boolean;
    matchTriggerWidth?: boolean;
    triggerVariant?: 'primary' | 'secondary';
    triggerSize?: 'xs' | 'sm' | 'md' | 'lg' | 'xl' | '2xl';
    triggerVisibleBackground?: boolean;
    triggerDisabled?: boolean;
}>(), {
    align: 'right',
    dropdownSize: 'md',
    closeOnSelect: true,
    openAtHover: false,
    hideChevron: false,
    matchTriggerWidth: false,
    triggerVariant: 'secondary',
    triggerSize: 'md',
    triggerVisibleBackground: true,
    triggerDisabled: false
});

const emit = defineEmits<{
    close: [];
}>();

const isOpen = ref(false);
const openedByHover = ref(false);
const containerRef = ref<HTMLElement | null>(null);
const triggerWidth = ref<number | null>(null);
let hoverTimeout: number | null = null;

const resolvedPosition = computed(() => {
    if (props.position) return props.position;
    if (!containerRef.value) return `bottom-${props.align}` as const;

    const rect = containerRef.value.getBoundingClientRect();
    const spaceBelow = window.innerHeight - rect.bottom;
    
    const vertical = spaceBelow >= 150 ? 'bottom' : 'top';
    
    return `${vertical}-${props.align}` as 'top-left' | 'top-right' | 'bottom-left' | 'bottom-right';
});

// ═══ Style dynamique pour forcer la largeur ═══
const dropdownStyle = computed(() => {
    if (!props.matchTriggerWidth || triggerWidth.value === null) {
        return {};
    }
    
    const widthPx = `${triggerWidth.value}px`;
    return {
        width: widthPx,
        minWidth: widthPx,
        maxWidth: widthPx
    };
});

function measureTriggerWidth() {
    if (containerRef.value) {
        triggerWidth.value = containerRef.value.offsetWidth;
    }
}

function toggle() {
    clearHoverTimeout();
    
    if (!isOpen.value && props.matchTriggerWidth) {
        measureTriggerWidth();
    }
    
    isOpen.value = !isOpen.value;
    
    if (isOpen.value) {
        openedByHover.value = false;
    } else {
        emit('close');
    }
}

function handleMouseEnter() {
    if (!props.openAtHover) return;
    
    clearHoverTimeout();
    
    if (!isOpen.value) {
        if (props.matchTriggerWidth) {
            measureTriggerWidth();
        }
        
        isOpen.value = true;
        openedByHover.value = true;
    }
}

function handleMouseLeave() {
    if (!props.openAtHover) return;
    
    if (openedByHover.value && isOpen.value) {
        hoverTimeout = window.setTimeout(() => {
            isOpen.value = false;
            openedByHover.value = false;
            emit('close');
        }, 150);
    }
}

function clearHoverTimeout() {
    if (hoverTimeout !== null) {
        clearTimeout(hoverTimeout);
        hoverTimeout = null;
    }
}

function handleItemClick() {
    if (props.closeOnSelect) {
        setTimeout(() => {
            isOpen.value = false;
            openedByHover.value = false;
            emit('close');
        }, 0);
    }
}

function onClickOutside(event: MouseEvent) {
    if (containerRef.value && !containerRef.value.contains(event.target as Node)) {
        isOpen.value = false;
        openedByHover.value = false;
        emit('close');
    }
}

function closeFromItem() {
    isOpen.value = false;
    openedByHover.value = false;
    emit('close');
}

provide('closeDropdown', closeFromItem);

onMounted(() => {
    document.addEventListener('mousedown', onClickOutside);
    
    if (props.matchTriggerWidth) {
        nextTick(() => measureTriggerWidth());
    }
});

onUnmounted(() => {
    document.removeEventListener('mousedown', onClickOutside);
    clearHoverTimeout();
});
</script>