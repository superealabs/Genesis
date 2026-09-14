<template>
    <div class="space-y-4">
        
        <!-- ═══ DISCLOSURE 1 : Langages ═══ -->
        <GenesisDisclosure title="Langages" default-open variant="primary">
            <div class="grid grid-cols-2 md:grid-cols-4 gap-3 pt-2">
                <GenesisInput
                    v-for="(state, label) in filters.languages"
                    :key="label"
                    type="checkbox-3-state"
                    :modelValue="state"
                    :label="label"
                    size="lg"
                    @update:modelValue="filters.languages[label] = $event as CheckboxState"
                />
            </div>
        </GenesisDisclosure>

        <GenesisDisclosure title="Architecture" variant="primary">
            <div class="grid grid-cols-2 md:grid-cols-4 gap-3 pt-2">
                <GenesisInput
                    v-for="(state, label) in filters.types"
                    :key="label"
                    type="checkbox-3-state"
                    :modelValue="state"
                    @update:modelValue="filters.types[label] = $event as CheckboxState"
                    :label="label"
                    size="lg"
                />
            </div>
        </GenesisDisclosure>

        <!-- ═══ DISCLOSURE 3 : Core Frameworks ═══ -->
        <GenesisDisclosure title="Core Frameworks" variant="primary">
            <div class="grid grid-cols-2 md:grid-cols-4 gap-3 pt-2">
                <GenesisInput
                    v-for="(state, label) in filters.coreFrameworks"
                    :key="label"
                    type="checkbox-3-state"
                    :modelValue="state"
                    :label="label"
                    size="lg"
                    @update:modelValue="filters.coreFrameworks[label] = $event as CheckboxState"
                />
            </div>
        </GenesisDisclosure>

        <!-- ═══ DISCLOSURE 4 : Options d'intégration ═══ -->
        <GenesisDisclosure title="Options d'intégration" variant="primary">
            <div class="grid grid-cols-2 md:grid-cols-4 gap-3 pt-2">
                <GenesisInput
                    v-for="(state, label) in filters.integrations"
                    :key="label"
                    type="checkbox-3-state"
                    :modelValue="state"
                    :label="label"
                    size="lg"
                    @update:modelValue="filters.integrations[label] = $event as CheckboxState"
                />
            </div>
        </GenesisDisclosure>

        <!-- ═══ DISCLOSURE 5 : Options MVC (Conditionnel) ═══ -->
        <GenesisDisclosure 
            v-if="filters.types['MVC'] === 'checked' || filters.types['MVC'] === 'neutral'" 
            title="Options MVC" 
            variant="primary"
        >
            <div class="space-y-6 pt-2">
                <!-- Template Engines -->
                <div>
                    <h4 class="text-xs font-semibold text-text-muted uppercase mb-3">Template Engine</h4>
                    <div class="grid grid-cols-2 md:grid-cols-4 gap-3">
                        <GenesisInput
                            v-for="(state, label) in filters.templateEngines"
                            :key="label"
                            type="checkbox-3-state"
                            :modelValue="state"
                            @update:modelValue="filters.templateEngines[label] = $event as CheckboxState"
                            :label="label"
                            size="lg"
                        />
                    </div>
                </div>

                <!-- View Extensions -->
                <div>
                    <h4 class="text-xs font-semibold text-text-muted uppercase mb-3">View Extension</h4>
                    <div class="grid grid-cols-2 md:grid-cols-4 gap-3">
                        <GenesisInput
                            v-for="(state, label) in filters.viewExtensions"
                            :key="label"
                            type="checkbox-3-state"
                            :modelValue="state"
                            @update:modelValue="filters.viewExtensions[label] = $event as CheckboxState"
                            :label="label"
                            size="lg"
                        />
                    </div>
                </div>
            </div>
        </GenesisDisclosure>

        <!-- ═══ Actions ═══ -->
        <div class="flex gap-2 pt-4 border-t border-secondary">
            <GenesisButton variant="secondary" size="md" :fillWidth="true" @click.stop="resetFilters">
                Réinitialiser
            </GenesisButton>
            <GenesisButton variant="primary" size="md" :fillWidth="true" @click.stop="$emit('close')">
                Appliquer
            </GenesisButton>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import type { CheckboxState } from '@/core/components/ui/inputs/GenesisCheckbox.vue';
import GenesisInput from '@/core/components/ui/inputs/GenesisInput.vue';
import GenesisButton from '@/core/components/ui/actions/GenesisButton.vue';
import GenesisDisclosure from '@/core/components/layouts/GenesisDisclosure.vue';

// Nouvelle structure : Record<string, CheckboxState> au lieu de string[]
interface FrameworkFilters {
    languages: Record<string, CheckboxState>;
    types: Record<string, CheckboxState>;
    coreFrameworks: Record<string, CheckboxState>;
    integrations: Record<string, CheckboxState>;
    templateEngines: Record<string, CheckboxState>;
    viewExtensions: Record<string, CheckboxState>;
}

const emit = defineEmits<{
    'update:filters': [filters: FrameworkFilters];
    'close': [];
}>();

// ═══ Initialisation des états (tous à 'neutral' par défaut) ═══
const createNeutralState = (keys: string[]) => {
    return keys.reduce((acc, key) => {
        acc[key] = 'neutral';
        return acc;
    }, {} as Record<string, CheckboxState>);
};

const filters = ref<FrameworkFilters>({
    languages: createNeutralState(['Java', 'C#', 'Node.js', 'Python']),
    types: createNeutralState(['MVC', 'REST API']),
    coreFrameworks: createNeutralState(['Spring Boot', '.NET Core', 'Express', 'Django']),
    integrations: createNeutralState(['Support DB', 'Support Cloud', 'Eureka Server', 'Gateway', 'Frontend App']),
    templateEngines: createNeutralState(['Thymeleaf', 'JSP', 'Razor', 'Blade']),
    viewExtensions: createNeutralState(['.html', '.jsp', '.cshtml', '.blade.php'])
});

// ═══ Helpers ═══

// Extrait uniquement les valeurs 'checked' pour la compatibilité avec l'API/Composable si nécessaire
// function getCheckedValues(stateRecord: Record<string, CheckboxState>): string[] {
//     return Object.entries(stateRecord)
//         .filter(([_, state]) => state === 'checked')
//         .map(([key]) => key);
// }

function resetFilters() {
    filters.value = {
        languages: createNeutralState(['Java', 'C#', 'Node.js', 'Python']),
        types: createNeutralState(['MVC', 'REST API']),
        coreFrameworks: createNeutralState(['Spring Boot', '.NET Core', 'Express', 'Django']),
        integrations: createNeutralState(['Support DB', 'Support Cloud', 'Eureka Server', 'Gateway', 'Frontend App']),
        templateEngines: createNeutralState(['Thymeleaf', 'JSP', 'Razor', 'Blade']),
        viewExtensions: createNeutralState(['.html', '.jsp', '.cshtml', '.blade.php'])
    };
}

// ═══ Watcher ═══
watch(filters, (newFilters) => {
    // On émet l'état complet (riche). 
    // Si ton composable a besoin de tableaux, tu peux mapper ici :
    // const arrayFilters = {
    //     languages: getCheckedValues(newFilters.languages),
    //     types: getCheckedValues(newFilters.types),
    //     // ...
    // };
    // emit('update:filters', arrayFilters as any);
    
    emit('update:filters', newFilters);
}, { deep: true });
</script>