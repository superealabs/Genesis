<template>
    <GenesisCollectionLayout
        title="Frameworks"
        v-model:searchValue="searchQuery"
        v-model:displayMode="displayMode"
        :mode="compareMode"
        searchPlaceholder="Rechercher par nom, core, type..."
        :showBackButton="showBackButton"
        @back="handleBack"
        @openFilter="openFilter"
        @update:mode="handleModeChange"
        @update:searchValue="setSearch"
        @update:displayMode="toggleDisplayMode"
    >
        <template #filter>
            <FrameworkFilter v-model:filters="filters" @update:filters="setFilters" />
        </template>

        <FrameworkList
            :frameworks="frameworks"
            :selectedId="selectedId"
            :display="displayMode"
            :frameworkSlots="frameworkSlots"
            @select="handleSelectWrapper"
            @info="detailFramework = $event"
        />
    </GenesisCollectionLayout>

    <!-- Popup de remplacement -->
    <SimpleSelectionPopup
        :show="showReplacePopup"
        :mouseX="mouseX"
        :mouseY="mouseY"
        :options="replaceOptions"
        position="bottom-right"
        @select="handleReplaceSelection"
        @close="cancelReplace"
    />

    <!-- Panneau de détails -->
    <FrameworkDetail
        v-if="detailFramework"
        :framework="detailFramework"
        @close="detailFramework = null"
    />
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useFrameworks } from '@/features/frameworks/composables/useFrameworks';
import FrameworkList from '@/features/frameworks/components/FrameworkList.vue';
import FrameworkFilter from '@/features/frameworks/components/FrameworkFilter.vue';
import GenesisCollectionLayout from '@/core/components/layouts/GenesisCollectionLayout.vue';
import SimpleSelectionPopup from '@/core/components/layouts/Popup/SimpleSelectionPopup.vue';
import type { SelectionOption } from '@/core/components/layouts/Popup/SimpleSelectionPopup.vue';
import FrameworkDetail from '../components/FrameworkDetail.vue';
import type { Framework } from '../types/framework.types';

const props = withDefaults(defineProps<{
    showBackButton?: boolean;
}>(), {
    showBackButton: true
});

const emit = defineEmits<{ 'back': [] }>();

// ═══ Injection du composable (seule source de vérité) ═══
const { 
    frameworks, selectedId, displayMode, compareMode, frameworkSlots, filters, searchQuery,
    setSearch, setFilters, toggleDisplayMode, handleModeChange,
    handleSelect, handleReplace, compare,
    initialize
} = useFrameworks();

// ═══ État local UI (uniquement pour la gestion du popup) ═══
const detailFramework = ref<Framework | null>(null);
const showReplacePopup = ref(false);
const pendingFramework = ref<Framework | null>(null);
const mouseX = ref<number | null>(null);
const mouseY = ref<number | null>(null);

const replaceOptions = computed<SelectionOption[]>(() => {
    return Object.entries(compare.slots.value)
        .filter(([, framework]) => framework !== null)
        .map(([slot, framework]) => ({
            id: slot,
            label: `Slot ${slot}`,
            description: (framework as Framework).name
        }));
});

// ═══ Handlers ═══
function handleSelectWrapper(framework: Framework, event?: MouseEvent) {
    const result = handleSelect(framework, event);
    
    if (result.action === 'replace-needed') {
        pendingFramework.value = result.framework;
        mouseX.value = result.event ? result.event.clientX : window.innerWidth / 2;
        mouseY.value = result.event ? result.event.clientY : window.innerHeight / 2;
        showReplacePopup.value = true;
    }
}

function handleReplaceSelection(slotId: string | number) {
    if (pendingFramework.value) {
        handleReplace(slotId, pendingFramework.value);
    }
    cancelReplace();
}

function cancelReplace() {
    showReplacePopup.value = false;
    pendingFramework.value = null;
    mouseX.value = null;
    mouseY.value = null;
}

function handleBack() {
    emit('back');
}

function openFilter() {
    // Le dropdown gère lui-même l'ouverture
}

// Initialisation au montage
onMounted(() => {
    initialize();
});
</script>