<!-- webview-ui/src/features/frameworks/views/FrameworksView.vue -->
<template>
    <GenesisCollectionLayout
        title="Frameworks"
        v-model:searchValue="search"
        v-model:displayMode="displayMode"
        searchPlaceholder="Rechercher par nom, core, type..."
        :showBackButton="showBackButton"
        @back="handleBack"
        @openFilter="openFilter"
    >
        <template #filter>
            <FrameworkFilter v-model:filters="filters" />
        </template>

        <FrameworkGrid
            v-if="displayMode === 'grid'"
            :frameworks="filtered"
            :selectedId="selectedFramework?.id"
            @select="handleSelect"
            @info="detailFramework = $event"
        />

        <FrameworkList
            v-else
            :frameworks="filtered"
            :selectedId="selectedFramework?.id"
            @select="handleSelect"
        />
    </GenesisCollectionLayout>
    <!-- Ajoute dans le template -->
    <FrameworkDetail
        v-if="detailFramework"
        :framework="detailFramework"
        @close="detailFramework = null"
    />
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { useFrameworks } from '@/features/frameworks/composables/useFrameworks';
import FrameworkGrid from '@/features/frameworks/components/FrameworkGrid.vue';
import FrameworkList from '@/features/frameworks/components/FrameworkList.vue';
import FrameworkFilter from '@/features/frameworks/components/FrameworkFilter.vue';
import GenesisCollectionLayout from '@/core/components/layouts/GenesisCollectionLayout.vue';
import type { Framework } from '../types/framework.types';
import FrameworkDetail from '../components/FrameworkDetail.vue';
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

// Nouveaux props pour contrôler le comportement
const props = withDefaults(defineProps<{
    showBackButton?: boolean;
    autoSelect?: boolean; // Si true, sélectionne et émet immédiatement
}>(), {
    showBackButton: true,
    autoSelect: false
});

const emit = defineEmits<{
    'select': [framework: Framework];
    'back': [];
}>();

const { list, displayMode } = useFrameworks();
const selectedFramework = ref<Framework | null>(null);
const search = ref('');
const filters = ref<FrameworkFilters>({});

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

    if (filters.value.type) {
        result = result.filter(f => f.type === filters.value.type);
    }
    if (filters.value.coreFramework) {
        result = result.filter(f => f.coreFramework === filters.value.coreFramework);
    }
    if (filters.value.isProd) {
        result = result.filter(f => f.isProd);
    }
    if (filters.value.useDB) {
        result = result.filter(f => f.useDB);
    }
    if (filters.value.useCloud) {
        result = result.filter(f => f.useCloud);
    }
    if (filters.value.useEurekaServer) {
        result = result.filter(f => f.useEurekaServer);
    }
    if (filters.value.isGateway) {
        result = result.filter(f => f.isGateway);
    }
    if (filters.value.useFrontendApp) {
        result = result.filter(f => f.useFrontendApp);
    }

    return result;
});

function handleSelect(framework: Framework) {
    selectedFramework.value = framework;
    emit('select', framework);
}

function handleBack() {
    emit('back');
}

function openFilter() {
    // Le dropdown gère lui-même l'ouverture
}
</script>