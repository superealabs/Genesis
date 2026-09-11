<template>
    <GenesisDropdown
        dropdownSize="lg"
        :closeOnSelect="false"
        triggerSize="2xl"
        triggerVariant="secondary"
        :hideChevron="true"
    >
        <template #triggerIcon>
            <IconFilter />
        </template>
        
        <!-- <template #trigger>
            Filtres
            <span 
                v-if="activeFiltersCount > 0" 
                class="ml-1 px-1.5 py-0.5 bg-accent text-bg text-[10px] rounded-full font-medium"
            >
                {{ activeFiltersCount }}
            </span>
        </template> -->

        <div class="p-3 space-y-3">
            <!-- Filtres niveau 1 -->
            <div class="space-y-2">
                <h4 class="text-xs font-semibold text-text-muted uppercase">Filtres de base</h4>
                
                <!-- Language -->
                <FilterSelect
                    label="Language"
                    :options="languageOptions"
                    v-model="filters.language"
                />

                <!-- Type -->
                <FilterSelect
                    label="Type"
                    :options="typeOptions"
                    v-model="filters.type"
                />

                <!-- Core Framework -->
                <FilterSelect
                    label="Core Framework"
                    :options="coreOptions"
                    v-model="filters.coreFramework"
                />

                <!-- Prod Ready -->
                <FilterCheckbox
                    label="Prod Ready uniquement"
                    v-model="filters.isProd"
                />
            </div>

            <!-- Séparateur -->
            <div class="border-t border-secondary"></div>

            <!-- Bouton pour filtres avancés -->
            <button
                @click.stop="showAdvanced = !showAdvanced"
                class="w-full text-left text-xs text-accent hover:text-accent/80 flex items-center gap-1"
            >
                <span>{{ showAdvanced ? '▼' : '▶' }}</span>
                Filtres avancés
            </button>

            <!-- Filtres niveau 2 (conditionnel) -->
            <div v-if="showAdvanced" class="space-y-2 pl-2 border-l-2 border-secondary">
                <h4 class="text-xs font-semibold text-text-muted uppercase">Options techniques</h4>

                <FilterCheckbox label="Support DB" v-model="filters.useDB" />
                <FilterCheckbox label="Support Cloud" v-model="filters.useCloud" />
                <FilterCheckbox label="Eureka Server" v-model="filters.useEurekaServer" />
                <FilterCheckbox label="Gateway" v-model="filters.isGateway" />
                <FilterCheckbox label="Frontend App" v-model="filters.useFrontendApp" />

                <!-- Filtres spécifiques MVC -->
                <template v-if="filters.type === 'MVC' || !filters.type">
                    <FilterSelect
                        label="Template Engine"
                        :options="templateEngineOptions"
                        v-model="filters.viewTemplateEngine"
                    />
                    <FilterSelect
                        label="View Extension"
                        :options="viewExtensionOptions"
                        v-model="filters.viewExtension"
                    />
                </template>
            </div>

            <!-- Actions -->
            <div class="flex gap-2 pt-2 border-t border-secondary">
                <GenesisButton
                    variant="secondary"
                    size="sm"
                    :fillWidth="true"
                    @click.stop="resetFilters"
                >
                    Réinitialiser
                </GenesisButton>
            </div>
        </div>
    </GenesisDropdown>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import GenesisDropdown from '@/core/components/ui/dropdown/GenesisDropdown.vue';
import GenesisButton from '@/core/components/ui/actions/GenesisButton.vue';
import IconFilter from '@/core/components/ui/icons/IconFilter.vue';
import FilterSelect from './FilterSelect.vue';
import FilterCheckbox from './FilterCheckbox.vue';

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

const emit = defineEmits<{
    'update:filters': [filters: FrameworkFilters];
}>();

// État des filtres
const filters = ref<FrameworkFilters>({});
const showAdvanced = ref(false);

// Options disponibles (à remplacer par des données réelles)
const languageOptions = [
    { label: 'Tous', value: '' },
    { label: 'Java', value: 'Java' },
    { label: 'C#', value: 'C#' },
    { label: 'Node.js', value: 'Node.js' }
];

const typeOptions = [
    { label: 'Tous', value: '' },
    { label: 'MVC', value: 'MVC' },
    { label: 'REST API', value: 'REST API' }
];

const coreOptions = [
    { label: 'Tous', value: '' },
    { label: 'Spring Boot', value: 'Spring Boot' },
    { label: '.NET Core', value: '.NET Core' },
    { label: 'Express', value: 'Express' }
];

const templateEngineOptions = [
    { label: 'Tous', value: '' },
    { label: 'Thymeleaf', value: 'Thymeleaf' },
    { label: 'JSP', value: 'JSP' },
    { label: 'Razor', value: 'Razor' }
];

const viewExtensionOptions = [
    { label: 'Tous', value: '' },
    { label: '.html', value: '.html' },
    { label: '.jsp', value: '.jsp' },
    { label: '.cshtml', value: '.cshtml' }
];

// Compter les filtres actifs
// const activeFiltersCount = computed(() => {
//     return Object.values(filters.value).filter(v => v !== undefined && v !== '' && v !== false).length;
// });

// Émettre les changements
watch(filters, (newFilters) => {
    emit('update:filters', { ...newFilters });
}, { deep: true });

// Réinitialiser les filtres
function resetFilters() {
    filters.value = {};
    showAdvanced.value = false;
}
</script>