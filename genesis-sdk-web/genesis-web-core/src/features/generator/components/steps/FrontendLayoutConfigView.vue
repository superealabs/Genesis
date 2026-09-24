<template>
    <div class="flex flex-col gap-4 p-4 max-w-3xl mx-auto">
        
        <!-- ═══ Section 1 : Framework Sélectionné & Port ═══ -->
        <GenesisDisclosure title="Framework & Port" default-open variant="primary">
            <div class="flex flex-col md:flex-row md:items-end gap-4 p-4 bg-bg-light/50 rounded-lg border border-secondary">
                <div class="flex-1 min-w-0">
                    <span class="text-sm font-medium text-text-muted block mb-1.5">
                        Framework Frontend choisi
                    </span>
                    <div class="flex items-center gap-2 text-text font-semibold truncate">
                        <span class="w-2 h-2 rounded-full bg-accent flex-shrink-0"></span>
                        {{ selectedFrontendName || 'Aucun framework sélectionné' }}
                    </div>
                </div>

                <div class="w-full md:w-auto">
                    <GenesisInput
                        v-model="layoutConfig.port"
                        type="number"
                        label="Port"
                        placeholder="ex: 3000"
                        fill-width
                    />
                </div>
            </div>
        </GenesisDisclosure>

        <!-- ═══ Section 2 : Langues Supportées ═══ -->
        <GenesisDisclosure :title="'interface language'" :variant="'secondary'">
            <template #title>
                Langues Supportées <span class="text-accent text-sm font-normal">*</span>
            </template>
            
            <p class="text-sm text-text-muted mb-3">
                Sélectionnez les langues à inclure dans le projet.
            </p>
            
            <GenesisInput
                v-model="selectedLanguageToAdd"
                type="select"
                placeholder="Sélectionner une langue..."
                label="Ajouter une langue"
                fill-width
            >
                <div class="p-1 space-y-1 max-h-60 overflow-y-auto">
                    <button
                        v-for="lang in availableLanguages"
                        :key="lang.code"
                        type="button"
                        class="w-full text-left px-3 py-2 text-sm text-text hover:bg-[var(--color-hover-ghost)] rounded-md transition-colors flex items-center justify-between"
                        :class="{ 'text-accent font-medium': selectedLanguageToAdd === lang.code }"
                        @click="handleLanguageSelect(lang.code)"
                    >
                        <span>{{ lang.name }}</span>
                        <svg v-if="selectedLanguageToAdd === lang.code" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                            <polyline points="20 6 9 17 4 12"></polyline>
                        </svg>
                    </button>
                </div>
            </GenesisInput>

            <div v-if="layoutConfig.selectedInterfaceLanguages.length > 0" class="flex flex-wrap gap-1.5 mt-3">
                <GenesisLabel
                    v-for="code in layoutConfig.selectedInterfaceLanguages"
                    :key="code"
                    :text="getLanguageName(code)"
                    @remove="() => toggleLanguage(code)" 
                />
            </div>
        </GenesisDisclosure>

        <!-- ═══ Section 3 : Structure et Navigation ═══ -->
        <GenesisDisclosure title="Structure et Navigation" variant="primary">
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <GenesisInput
                    v-model="layoutConfig.navbarType"
                    type="select"
                    label="Type de Navbar"
                    is-mandatory
                    fill-width
                    placeholder="Sélectionner..."
                >
                    <div class="p-1 space-y-1">
                        <button
                            v-for="option in navbarOptions"
                            :key="option.value"
                            type="button"
                            class="w-full text-left px-3 py-2 text-sm text-text hover:bg-[var(--color-hover-ghost)] rounded-md transition-colors flex items-center justify-between"
                            :class="{ 'text-accent font-medium': layoutConfig.navbarType === option.value }"
                            @click="updateFrontendLayout('navbarType', option.value)"
                        >
                            <span>{{ option.label }}</span>
                            <svg v-if="layoutConfig.navbarType === option.value" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                                <polyline points="20 6 9 17 4 12"></polyline>
                            </svg>
                        </button>
                    </div>
                </GenesisInput>

                <div class="flex items-end pb-2">
                    <span class="text-sm text-text-muted italic">D'autres options de structure à venir...</span>
                </div>
            </div>
        </GenesisDisclosure>

        <!-- ═══ Section 4 : Charte Graphique ═══ -->
        <GenesisDisclosure title="Charte Graphique" variant="primary">
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <GenesisInput
                    v-model="layoutConfig.primaryColor"
                    type="color"
                    label="Couleur Primaire"
                    placeholder="#3B82F6"
                    is-mandatory
                    fill-width
                />
                <GenesisInput
                    v-model="layoutConfig.secondaryColor"
                    type="color"
                    label="Couleur Secondaire"
                    placeholder="#64748B"
                    is-mandatory
                    fill-width
                />
            </div>

            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <GenesisInput
                    v-model="layoutConfig.logoPath"
                    type="file"
                    label="Fichier Logo"
                    placeholder="Aucun fichier sélectionné"
                    accept=".png,.jpg,.jpeg,.svg"
                    fill-width
                    @browse="() => emit('request-file-path', { field: 'logoPath', extensions: ['png', 'jpg', 'jpeg', 'svg'] })"
                />
                <GenesisInput
                    v-model="layoutConfig.faviconPath"
                    type="file"
                    label="Fichier Favicon"
                    placeholder="Aucun fichier sélectionné"
                    accept=".ico,.png,.svg"
                    fill-width
                    @browse="() => emit('request-file-path', { field: 'faviconPath', extensions: ['ico', 'png', 'svg'] })"
                />
            </div>
        </GenesisDisclosure>
    </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useGenerator } from '@genesis-labs/web-core/features/generator/composables/useGenerator';
import type { InterfaceLanguage } from '@genesis-labs/shared-types';
import GenesisInput from '@genesis-labs/web-core/core/components/ui/inputs/GenesisInput.vue';
import GenesisLabel from '@genesis-labs/web-core/core/components/ui/labels/GenesisLabel.vue';
import GenesisDisclosure from '@genesis-labs/web-core/core/components/layouts/GenesisDisclosure.vue';

const emit = defineEmits<{
    'request-file-path': [payload: { field: 'logoPath' | 'faviconPath', extensions: string[] }];
}>();

const { 
    stepperData, 
    fetchAvailableLanguages, 
    updateFrontendLayout, 
    toggleLanguage 
} = useGenerator();

// on corrige plus tard 
const availableLanguages = ref<InterfaceLanguage[]>([
    {
        id: 0,
        code: "none",
        name: "sqdqsd"
    }
]);


const layoutConfig = computed(() => stepperData.value.frontendLayout);
const selectedFrontendName = computed(() => stepperData.value.frontend?.name || 'Non défini (Étape 7)');

const selectedLanguageToAdd = ref('');

type NavbarType = 'side' | 'top' | '';
const navbarOptions: { label: string; value: NavbarType }[] = [
    { label: 'Barre latérale (Side)', value: 'side' },
    { label: 'Barre supérieure (Top)', value: 'top' }
];

function handleLanguageSelect(code: string) {
    if (!code) return;
    selectedLanguageToAdd.value = code;
    
    if (!layoutConfig.value.selectedInterfaceLanguages.includes(code)) {
        toggleLanguage(code);
    }
    
    // Petit délai pour permettre à l'UI de se mettre à jour avant de réinitialiser le input
    setTimeout(() => {
        selectedLanguageToAdd.value = '';
    }, 150);
}

function getLanguageName(code: string) {
    const lang = availableLanguages.value.find((l: InterfaceLanguage) => l.code === code);
    return lang ? lang.name : code;
}

// ═══ Lifecycle ═══
onMounted(() => {
    fetchAvailableLanguages();
});
</script>