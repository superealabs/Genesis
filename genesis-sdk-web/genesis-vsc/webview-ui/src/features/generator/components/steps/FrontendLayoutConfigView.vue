<template>
    <div class="flex flex-col gap-6 p-4 max-w-3xl mx-auto">
        
        <!-- ═══ Section 1 : Framework Sélectionné & Port ═══ -->
        <div class="space-y-3">
            <h3 class="text-lg font-semibold text-text flex items-center gap-2">
                Framework & Port
            </h3>
            
            <div class="flex flex-col md:flex-row md:items-end gap-4 p-4 bg-bg-light/50 rounded-lg border border-secondary">
                <!-- Affichage du choix précédent (Lecture seule) -->
                <div class="flex-1 min-w-0">
                    <span class="text-sm font-medium text-text-muted block mb-1.5">
                        Framework Frontend choisi
                    </span>
                    <div class="flex items-center gap-2 text-text font-semibold truncate">
                        <span class="w-2 h-2 rounded-full bg-accent flex-shrink-0"></span>
                        {{ selectedFrontendName || 'Aucun framework sélectionné' }}
                    </div>
                </div>

                <!-- Input pour le port -->
                <div>
                    <GenesisInput
                        v-model="layoutConfig.port"
                        type="number"
                        label="Port"
                        placeholder="ex: 3000"
                    />
                </div>
            </div>
        </div>

        <div class="border-t border-secondary"></div>

        <!-- ═══ Section 2 : Langues Supportées ═══ -->
        <div class="space-y-3">
            <h3 class="text-lg font-semibold text-text flex items-center gap-2">
                Langues Supportées
                <span class="text-accent text-sm font-normal">*</span>
            </h3>
            <p class="text-sm text-text-muted">
                Sélectionnez les langues à inclure dans le projet.
            </p>
            
            <!-- Dropdown pour ajouter une langue (utilise le type "select" de GenesisInput) -->
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

            <!-- Affichage des langues choisies en dessous (Labels) -->
            <div v-if="layoutConfig.selectedLanguages.length > 0" class="flex flex-wrap gap-1.5 mt-1.5">
                <GenesisLabel
                    v-for="code in layoutConfig.selectedLanguages"
                    :key="code"
                    :text="getLanguageName(code)"
                    @remove="toggleLanguage(code)"
                />
            </div>
        </div>

        <div class="border-t border-secondary"></div>

        <!-- ═══ Section 3 : Structure et Navigation ═══ -->
        <div class="space-y-3">
            <h3 class="text-lg font-semibold text-text">Structure et Navigation</h3>
            
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <!-- Type de Navbar (Uniformisé avec GenesisInput type="select") -->
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
                            @click="updateLayout('navbarType', option.value)"
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
        </div>

        <div class="border-t border-secondary"></div>

        <!-- ═══ Section 4 : Charte Graphique ═══ -->
        <div class="space-y-3">
            <h3 class="text-lg font-semibold text-text">Charte Graphique</h3>
            
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
        </div>

        <div class="border-t border-secondary"></div>

        <!-- ═══ Section 5 : Assets ═══ -->
        <div class="space-y-3">
            <h3 class="text-lg font-semibold text-text">Assets (Logo & Favicon)</h3>
            
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <GenesisInput
                    v-model="layoutConfig.logoPath"
                    type="file"
                    label="Fichier Logo"
                    placeholder="Aucun fichier sélectionné"
                    accept=".png,.jpg,.jpeg,.svg"
                    fill-width
                    @browse="handleBrowseFile('logoPath', ['png', 'jpg', 'jpeg', 'svg'])"
                />
                <GenesisInput
                    v-model="layoutConfig.faviconPath"
                    type="file"
                    label="Fichier Favicon"
                    placeholder="Aucun fichier sélectionné"
                    accept=".ico,.png,.svg"
                    fill-width
                    @browse="handleBrowseFile('faviconPath', ['ico', 'png', 'svg'])"
                />
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useGenerator } from '../../composables/useGenerator';
import { generatorService } from '../../services/generator.service';
import GenesisInput from '@/core/components/ui/inputs/GenesisInput.vue';
import GenesisLabel from '@/core/components/ui/labels/GenesisLabel.vue';

const { 
    stepperData, 
    availableLanguages, 
    fetchAvailableLanguages, 
    updateFrontendLayout, 
    toggleLanguage 
} = useGenerator();

const layoutConfig = computed(() => stepperData.value.frontendLayout);

// ═══ Gestion du Framework & Port ═══
const selectedFrontendName = computed(() => {
    return stepperData.value.frontend?.name || 'Non défini (Étape 7)';
});

// ═══ Gestion des Langues ═══
const selectedLanguageToAdd = ref('');

type NavbarType = 'side' | 'top' | '';

const navbarOptions: { label: string; value: NavbarType }[] = [
    { label: 'Barre latérale (Side)', value: 'side' },
    { label: 'Barre supérieure (Top)', value: 'top' }
];

function handleLanguageSelect(code: string) {
    if (!code) return;
    
    selectedLanguageToAdd.value = code;
    
    if (!layoutConfig.value.selectedLanguages.includes(code)) {
        toggleLanguage(code);
    }
    
    setTimeout(() => {
        selectedLanguageToAdd.value = '';
    }, 150);
}

function getLanguageName(code: string) {
    const lang = availableLanguages.value.find(l => l.code === code);
    return lang ? lang.name : code;
}

// ═══ Gestion des Fichiers ═══
function handleBrowseFile(field: 'logoPath' | 'faviconPath', extensions: string[]) {
    generatorService.requestFilePath(extensions);
    console.log(`[DEBUG] Demande d'ouverture de fichier pour ${field} avec extensions:`, extensions);
}

function updateLayout<K extends keyof typeof layoutConfig.value>(key: K, value: typeof layoutConfig.value[K]) {
    updateFrontendLayout(key, value);
}

// ═══ Lifecycle ═══
onMounted(() => {
    fetchAvailableLanguages();
});
</script>