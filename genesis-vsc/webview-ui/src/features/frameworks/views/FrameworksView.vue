<template>
    <GenesisCollectionLayout
        title="Frameworks"
        v-model:searchValue="search"
        v-model:displayMode="displayMode"
        :mode="mode"
        searchPlaceholder="Rechercher par nom, core, type..."
        :showBackButton="showBackButton"
        @back="handleBack"
        @openFilter="openFilter"
        @update:mode="handleModeChange"
    >
        <template #filter>
            <FrameworkFilter v-model:filters="filters" />
        </template>

        <!-- Un seul composant pour les deux modes -->
        <FrameworkList
            :frameworks="filtered"
            :selectedId="mode === 'selection' ? selectedItem?.id : undefined"
            :display="displayMode"
            :frameworkSlots="frameworkSlots"
            @select="handleSelect"
            @info="detailFramework = $event"
        />
    </GenesisCollectionLayout>

    <!-- Popup de remplacement quand tous les slots sont pleins -->
    <SimpleSelectionPopup
        v-if="showReplacePopup"
        title="Remplacer un slot"
        :options="replaceOptions"
        @select="handleReplace"
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
import { ref, computed } from 'vue';
import { useFrameworks } from '@/features/frameworks/composables/useFrameworks';
import { useCompareSlots } from '@/core/composables/ux/useCompareSlots';
import FrameworkList from '@/features/frameworks/components/FrameworkList.vue';
import FrameworkFilter from '@/features/frameworks/components/FrameworkFilter.vue';
import GenesisCollectionLayout from '@/core/components/layouts/GenesisCollectionLayout.vue';
import SimpleSelectionPopup from '@/core/components/layouts/Popup/SimpleSelectionPopup.vue';
import type { SelectionOption } from '@/core/components/layouts/Popup/SimpleSelectionPopup.vue';
import FrameworkDetail from '../components/FrameworkDetail.vue';
import type { Framework } from '../types/framework.types';

const detailFramework = ref<Framework | null>(null);

interface FrameworkFilters {
    language?: string;
    type?: 'MVC' | 'REST API';
    coreFramework?: string;
    isProd?: boolean;
    useDB?: boolean;
    useCloud?: boolean;
    useEurekaServer?: boolean;
    isGateway?: boolean;
    useFrontendApp?: boolean;
    viewTemplateEngine?: string;
    viewExtension?: string;
}

const props = withDefaults(defineProps<{
    showBackButton?: boolean;
    autoSelect?: boolean;
}>(), {
    showBackButton: true,
    autoSelect: false
});

const emit = defineEmits<{
    'select': [framework: Framework];
    'back': [];
}>();

const { list, displayMode } = useFrameworks();
const search = ref('');
const filters = ref<FrameworkFilters>({});

// Initialiser le système de comparaison avec 4 slots (A, B, C, D)
const compare = useCompareSlots<Framework>({
    slots: ['A', 'B', 'C', 'D'],
    getId: (f) => f.id
});

const { mode, slots, selectedItem } = compare;

// Map des framework.id → slot (A, B, C, D)
const frameworkSlots = computed(() => {
    if (mode.value !== 'compare') {
        return new Map<number, string>(); // Map vide = pas de badge en mode sélection
    }
    
    const map = new Map<number, string>();
    for (const [key, framework] of Object.entries(slots.value)) {
        if (framework) {
            map.set(framework.id, key);
        }
    }
    return map;
});

// État pour le popup de remplacement
const showReplacePopup = ref(false);
const pendingFramework = ref<Framework | null>(null);

const replaceOptions = computed<SelectionOption[]>(() => {
    return Object.entries(slots.value)
        .filter(([, framework]) => framework !== null)
        .map(([slot, framework]) => ({
            id: slot,
            label: `Slot ${slot}`,
            description: (framework as Framework).name
        }));
});

const filtered = computed(() => {
    let result = list.value;

    if (search.value.trim()) {
        const q = search.value.toLowerCase();
        result = result.filter(f =>
            f.name.toLowerCase().includes(q) ||
            f.coreFramework.toLowerCase().includes(q) ||
            f.type.toLowerCase().includes(q)
        );
    }

    if (filters.value.type) result = result.filter(f => f.type === filters.value.type);
    if (filters.value.coreFramework) result = result.filter(f => f.coreFramework === filters.value.coreFramework);
    if (filters.value.isProd) result = result.filter(f => f.isProd);
    if (filters.value.useDB) result = result.filter(f => f.useDB);
    if (filters.value.useCloud) result = result.filter(f => f.useCloud);
    if (filters.value.useEurekaServer) result = result.filter(f => f.useEurekaServer);
    if (filters.value.isGateway) result = result.filter(f => f.isGateway);
    if (filters.value.useFrontendApp) result = result.filter(f => f.useFrontendApp);

    return result;
});

function handleSelect(framework: Framework) {
    const result = compare.handleSelect(framework);

    if (result.action === 'replace-needed') {
        pendingFramework.value = framework;
        showReplacePopup.value = true;
    } else if (result.action === 'select' && mode.value === 'selection') {
        emit('select', framework);
    }
}

function handleReplace(slotId: string | number) {
    if (pendingFramework.value) {
        compare.replaceSlot(slotId, pendingFramework.value);
    }
    cancelReplace();
}

function cancelReplace() {
    showReplacePopup.value = false;
    pendingFramework.value = null;
}

function handleModeChange(newMode: 'selection' | 'compare') {
    compare.switchMode(newMode);
}

function handleBack() {
    emit('back');
}

function openFilter() {
    // Le dropdown gère lui-même l'ouverture
}
</script>