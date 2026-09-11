<template>
    <GenesisCollectionLayout
        title="Framework Frontend"
        v-model:searchValue="searchQuery"
        v-model:displayMode="displayMode"
        searchPlaceholder="Rechercher par nom (ex: React, Vue)..."
        :showBackButton="showBackButton"
        @back="handleBack"
        @openFilter="openFilter"
        @update:searchValue="setSearch"
        @update:displayMode="toggleDisplayMode"
    >
        <!-- Emplacement réservé pour le filtre (à implémenter plus tard) -->
        <template #filter>
            <div class="p-4 text-sm text-text-muted">
                Filtres avancés frontend (à venir)
            </div>
        </template>

        <FrontendList
            :frontends="filteredFrontends"
            :selectedId="selectedId"
            :display="displayMode"
            @select="handleSelectWrapper"
            @info="detailFramework = $event"
        />
    </GenesisCollectionLayout>

    <!-- Panneau de détails (réservé pour plus tard) -->
    <!-- <FrontendDetail
        v-if="detailFramework"
        :framework="detailFramework"
        @close="detailFramework = null"
    /> -->
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useFrontend } from '@/features/frontend/composables/useFrontend';
import FrontendList from '../components/FrontendList.vue';
import GenesisCollectionLayout from '@/core/components/layouts/GenesisCollectionLayout.vue';
import type { FrontendFramework } from '../types/frontend.types';

const props = withDefaults(defineProps<{
    showBackButton?: boolean;
}>(), {
    showBackButton: true
});

const emit = defineEmits<{ 
    'back': [],
    'select': [framework: FrontendFramework]; 
}>();

// ═══ Injection du composable ═══
const { 
    availableFrameworks, 
    selectedFramework,
    initialize, 
    selectFramework 
} = useFrontend();

// ═══ État local UI (Recherche et Affichage) ═══
const searchQuery = ref('');
const displayMode = ref<'grid' | 'list'>('grid');
const detailFramework = ref<FrontendFramework | null>(null);

// ═══ Computed ═══
const selectedId = computed(() => selectedFramework.value?.id);

const filteredFrontends = computed(() => {
    if (!searchQuery.value.trim()) {
        return availableFrameworks.value;
    }
    const q = searchQuery.value.toLowerCase();
    return availableFrameworks.value.filter(fw =>
        fw.name.toLowerCase().includes(q) ||
        fw.coreFramework.toLowerCase().includes(q)
    );
});

// ═══ Handlers ═══
function handleSelectWrapper(framework: FrontendFramework, event?: MouseEvent) {
    // 1. Met à jour le store via le composable
    selectFramework(framework);
    
    if (event) {
        console.log("hi");
    }
    // 2. Émet vers le parent (GeneratorStepper) pour sauvegarde dans le flux global
    emit('select', framework);
}

function handleBack() {
    emit('back');
}

function setSearch(query: string) {
    searchQuery.value = query;
}

function toggleDisplayMode() {
    displayMode.value = displayMode.value === 'grid' ? 'grid' : 'list';
}

function openFilter() {
    // Le dropdown gère lui-même l'ouverture (placeholder pour l'instant)
    console.log('Ouverture des filtres frontend (à implémenter)');
}

// ═══ Lifecycle ═══
onMounted(() => {
    initialize();
});
</script>