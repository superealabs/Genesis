<template>
    <div class="space-y-4">
        
        <!-- ═══ DISCLOSURE 1 : Langages ═══ -->
        <GenesisDisclosure title="Langages" default-open :variant="'transparent'">
            <div class="grid grid-cols-2 md:grid-cols-4 gap-3 pt-2">
                <GenesisInput
                    v-for="lang in availableLanguages"
                    :key="lang"
                    type="checkbox-3-state"
                    :modelValue="filters.languages[lang]"
                    :label="lang"
                    size="lg"
                    @update:modelValue="updateFilterState('languages', lang, $event)"
                />
            </div>
            <div v-if="availableLanguages.length === 0" class="text-sm text-muted pt-2">Aucun langage disponible.</div>
        </GenesisDisclosure>

        <!-- ═══ DISCLOSURE 2 : Architecture ═══ -->
        <GenesisDisclosure title="Architecture" :variant="'transparent'">
            <div class="grid grid-cols-2 md:grid-cols-4 gap-3 pt-2">
                <GenesisInput
                    v-for="type in availableTypes"
                    :key="type"
                    type="checkbox-3-state"
                    :modelValue="filters.types[type]"
                    :label="type"
                    size="lg"
                    @update:modelValue="updateFilterState('types', type, $event)"
                />
            </div>
        </GenesisDisclosure>

        <!-- ═══ DISCLOSURE 3 : Core Frameworks ═══ -->
        <GenesisDisclosure title="Core Frameworks" :variant="'transparent'">
            <div class="grid grid-cols-2 md:grid-cols-4 gap-3 pt-2">
                <GenesisInput
                    v-for="core in availableCoreFrameworks"
                    :key="core"
                    type="checkbox-3-state"
                    :modelValue="filters.coreFrameworks[core]"
                    :label="core"
                    size="lg"
                    @update:modelValue="updateFilterState('coreFrameworks', core, $event)"
                />
            </div>
            <div v-if="availableCoreFrameworks.length === 0" class="text-sm text-muted pt-2">Aucun core framework disponible.</div>
        </GenesisDisclosure>

        <!-- ═══ DISCLOSURE 4 : Options d'intégration ═══ -->
        <GenesisDisclosure title="Options d'intégration" :variant="'transparent'">
            <div class="grid grid-cols-2 md:grid-cols-4 gap-3 pt-2">
                <GenesisInput
                    v-for="opt in integrationOptions"
                    :key="opt.key"
                    type="checkbox-3-state"
                    :modelValue="filters.integrations[opt.key]"
                    :label="opt.label"
                    size="lg"
                    @update:modelValue="updateFilterState('integrations', opt.key, $event)"
                />
            </div>
        </GenesisDisclosure>

        <!-- ═══ Actions ═══ -->
        <div class="flex gap-2 pt-4 border-t border-bg-neutral-genesis">
            <GenesisButton variant="secondary" size="md" :fillWidth="true" @click.stop="resetFilters">
                Réinitialiser
            </GenesisButton>
            <GenesisButton variant="primary" size="md" :fillWidth="true" @click.stop="apply">
                Appliquer
            </GenesisButton>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, computed /*, watch*/ } from 'vue';
import type { Framework, Language } from '@genesis-labs/shared-types';
import type { CheckboxState } from '@genesis-labs/web-core/core/components/ui/inputs/GenesisCheckbox.vue';
import GenesisInput from '@genesis-labs/web-core/core/components/ui/inputs/GenesisInput.vue';
import GenesisButton from '@genesis-labs/web-core/core/components/ui/actions/GenesisButton.vue';
import GenesisDisclosure from '@genesis-labs/web-core/core/components/layouts/GenesisDisclosure.vue';

// ============================================================================
// 1. PROPS & EMITS
// ============================================================================
const props = defineProps<{
    languages: Language[];
    frameworks: Framework[];                      // liste COMPLÈTE
    filters?: Partial<FrameworkFilters>;
}>();

const emit = defineEmits<{
    'update:filters': [filters: FrameworkFilters];
    'close': [];
}>();

// ============================================================================
// 2. TYPES & CONSTANTES
// ============================================================================
export interface FrameworkFilters {
    languages: Record<string, CheckboxState>;
    types: Record<string, CheckboxState>;
    coreFrameworks: Record<string, CheckboxState>;
    integrations: Record<string, CheckboxState>;
}

// Mapping des clés de données vers des labels lisibles pour l'UI
const integrationOptions = [
    { key: 'useDB' as const, label: 'Base de données' },
    { key: 'useCloud' as const, label: 'Support Cloud' },
    { key: 'useEurekaServer' as const, label: 'Serveur Eureka' },
    { key: 'isGateway' as const, label: 'Passerelle (Gateway)' },
    { key: 'useFrontendApp' as const, label: 'Application Frontend' }
];

// ============================================================================
// 3. DONNÉES DYNAMIQUES (Computed)
// ============================================================================
const availableLanguages = computed(() => 
    [...new Set(props.languages.map(l => l.name))].sort()
);

const availableTypes = computed(() => 
    [...new Set(props.frameworks.map(f => f.type))].sort()
);

const availableCoreFrameworks = computed(() => 
    [...new Set(props.frameworks.map(f => f.coreFramework))].sort()
);

// ============================================================================
// 4. ÉTAT DES FILTRES
// ============================================================================
const createNeutralState = (keys: string[]) => {
    return keys.reduce((acc, key) => {
        acc[key] = 'neutral';
        return acc;
    }, {} as Record<string, CheckboxState>);
};

function emitFilters() {
    emit('update:filters', JSON.parse(JSON.stringify(filters.value)));
}

function updateFilterState(category: keyof FrameworkFilters, key: string, value: CheckboxState) {
    (filters.value[category] as Record<string, CheckboxState>)[key] = value;
}

function resetFilters() {
    filters.value = {
        languages: createNeutralState(availableLanguages.value),
        types: createNeutralState(availableTypes.value),
        coreFrameworks: createNeutralState(availableCoreFrameworks.value),
        integrations: createNeutralState(integrationOptions.map(o => o.key))
    };
}


function apply() {
    emitFilters();
    emit('close');
}

// Synchronise l'état existant avec les nouvelles clés disponibles (pour le chargement asynchrone)
// const syncStates = (current: Record<string, CheckboxState>, available: string[]) => {
//     const newState = { ...current };
//     available.forEach(key => {
//         if (newState[key] === undefined) newState[key] = 'neutral';
//     });

//     return newState;
// };

const filters = ref<FrameworkFilters>({
    languages:      { ...props.filters?.languages },
    types:          { ...props.filters?.types },
    coreFrameworks: { ...props.filters?.coreFrameworks },
    integrations:   { ...createNeutralState(integrationOptions.map(o => o.key)), ...props.filters?.integrations }
});

// Initialisation et mise à jour réactive quand les données arrivent
// watch([availableLanguages, availableTypes, availableCoreFrameworks], () => {
//     filters.value.languages = syncStates(filters.value.languages, availableLanguages.value);
//     filters.value.types = syncStates(filters.value.types, availableTypes.value);
//     filters.value.coreFrameworks = syncStates(filters.value.coreFrameworks, availableCoreFrameworks.value);
// }, { immediate: true });

// ============================================================================
// 5. ACTIONS
// ============================================================================

// ============================================================================
// 6. WATCHER POUR ÉMETTRE LES CHANGEMENTS
// ============================================================================
// let lastEmitted = '';
// watch(filters, (f) => {
//     const json = JSON.stringify(f);
//     if (json === lastEmitted) return;
//     lastEmitted = json;
//     emit('update:filters', JSON.parse(json));
// }, { deep: true });

// détecte les modifications
// watch([availableLanguages, availableTypes, availableCoreFrameworks], () => {
//     const sync = (current: Record<string, CheckboxState>, available: string[]) => {
//         const missing = available.filter(k => current[k] === undefined);
//         missing.forEach(k => { current[k] = 'neutral'; });   // mutation en place, seulement si besoin
//     };
//     sync(filters.value.languages, availableLanguages.value);
//     sync(filters.value.types, availableTypes.value);
//     sync(filters.value.coreFrameworks, availableCoreFrameworks.value);
// }, { immediate: true });
</script>