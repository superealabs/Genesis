<template>
  <main class="flex gap-4 h-full overflow-hidden">
    
    <!-- PARTIE GAUCHE : CONTENU PRINCIPAL (Scrollable) -->
    <div class="flex-1 min-h-0 overflow-y-auto p-6 space-y-6 custom-scrollbar">
      
      <!-- 1. CONFIGURATION DE BASE (Framework, Port) -->
      <div class="space-y-4">
        <h3 class="text-lg font-semibold text-text border-b border-neutral-light pb-2">
          Configuration Frontend
        </h3>
        
        <div ref="frontendSelectorRef" class="flex items-end gap-3 pb-4">
          <div class="flex-1">
            <template v-if="isEditingFrontend">
              <GenesisInput
                type="combobox"
                :options="availableFrontendsOptions"
                :model-value="tempFrontendName"
                @update:model-value="handleFrontendSelect"
                label="Framework Frontend"
                placeholder="Rechercher un framework..."
                size="lg"
                fill-width
                variant="primary"
              />
            </template>
            <template v-else>
              <div class="flex flex-col gap-1">
                <label class="text-sm font-medium text-muted">Framework Frontend sélectionné</label>
                <div class="flex items-center gap-2 h-10 px-3 bg-bg-secondary/30 rounded text-text border border-transparent">
                  <span class="w-2 h-2 rounded-full bg-accent shrink-0"></span>
                  <span class="truncate font-medium">{{ selectedFrontendName }}</span>
                </div>
              </div>
            </template>
          </div>
          
          <div class="pb-0.5">
            <GenesisButton v-if="!isEditingFrontend" variant="secondary" size="lg" @click="startEditingFrontend">
              Modifier
            </GenesisButton>
            <GenesisButton v-else variant="tertiary" size="lg" @click="cancelEditingFrontend">
              Annuler
            </GenesisButton>
          </div>
        </div>

        <div class="w-full">
          <GenesisInput 
            :model-value="layoutConfig.port" 
            @update:model-value="(val) => updateFrontendLayout('port', val)"
            type="number" 
            size="lg" 
            label="Port d'exécution" 
            placeholder="ex: 3000" 
            fill-width 
          />
        </div>
      </div>

      <!-- 2. LANGUES SUPPORTÉES (Avec Panneau de Configuration) -->
      <div class="space-y-4">
        <!-- ✅ DÉCLENCHEUR TUTORIEL SECTION 1 -->
        <div class="flex items-center justify-between border-b border-bg-neutral-genesis pb-2">
          <h3 class="text-lg font-semibold text-text">
            Langues Supportées
          </h3>
          <GenesisButtonIcon 
            variant="neutral" 
            size="sm" 
            @click="tutorial.restart('languages')"
            title="Aide sur la configuration des langues"
          >
            <IconHelp />
          </GenesisButtonIcon>
        </div>
        
        <div class="flex flex-col lg:flex-row gap-6">
          <div class="flex-1">
            <!-- ✅ ÉTAPE 1 : Barre de recherche -->
            <GenesisTutorialItem id="lang-search">
              <GenesisInput
                type="combobox"
                :options="availableLanguagesOptions"
                :multiChoice="true"
                :choices="layoutConfig.selectedInterfaceLanguages.map(code => getLanguageName(code))"
                placeholder="Rechercher une langue..."
                label="Ajouter une langue"
                size="lg"
                fill-width
                @add-choice="(name) => {
                  const lang = availableLanguages.find(l => l.name === name);
                  if (lang && !layoutConfig.selectedInterfaceLanguages.includes(lang.code)) {
                    toggleLanguage(lang.code);
                  }
                }"
                @remove-choice="(name) => {
                  const lang = availableLanguages.find(l => l.name === name);
                  if (lang) toggleLanguage(lang.code);
                }"
              />
            </GenesisTutorialItem>
          </div>

          <!-- Panneau de gestion des configurations de Langues -->
          <div class="w-full lg:w-[350px] flex flex-col gap-4 flex-shrink-0">
            <!-- ✅ ÉTAPE 2 : Genesis Configuration Panel -->
            <GenesisTutorialItem id="lang-config-panel">
              <GenesisConfigurationPanel
                :configurations="languagesConfigManager.configurations.value"
                :selected-config-id="languagesConfigManager.selectedConfigId.value"
                :filtered-configs="languagesConfigManager.filteredConfigs.value"
                :search-query="languagesConfigManager.searchQuery.value"
                :can-move-up="languagesConfigManager.canMoveUp.value"
                :can-move-down="languagesConfigManager.canMoveDown.value"
                @update:search-query="(val) => languagesConfigManager.searchQuery.value = val"
                @add="languagesLogic.handleAdd"
                @delete="languagesLogic.handleDelete"
                @rename="languagesLogic.handleRename"
                @toggle-visibility="languagesConfigManager.toggleVisibility"
                @move-up="languagesConfigManager.moveUp"
                @move-down="languagesConfigManager.moveDown"
                @select-configuration="languagesLogic.handleSelect"
              />
            </GenesisTutorialItem>
            
            <div class="flex gap-2 justify-end">
              <GenesisButtonIcon title="Exporter la configuration de langues sélectionnée" :disabled="!languagesConfigManager.selectedConfigId.value" @click="languagesLogic.handleExport">
                <IconUpload />
              </GenesisButtonIcon>
              <GenesisButtonIcon title="Exporter toutes les configurations de langues" @click="languagesLogic.handleExportAll">
                <IconSave />
              </GenesisButtonIcon>
            </div>
          </div>
        </div>
      </div>

      <!-- 3. CHARTE GRAPHIQUE & LAYOUT -->
      <div class="space-y-4">
        <!-- ✅ DÉCLENCHEUR TUTORIEL SECTION 2 -->
        <div class="flex items-center justify-between border-b border-bg-neutral-genesis pb-2">
          <h3 class="text-lg font-semibold text-text">
            Charte Graphique & Layout
          </h3>
          <GenesisButtonIcon 
            variant="neutral" 
            size="sm" 
            @click="tutorial.restart('charte')"
            title="Aide sur la charte graphique et le layout"
          >
            <IconHelp />
          </GenesisButtonIcon>
        </div>
        
        <div class="space-y-4 flex flex-col gap-4">
          <!-- ✅ ÉTAPE 1 : Type de barre -->
          <GenesisTutorialItem id="charte-navbar">
            <GenesisInput
              :model-value="layoutConfig.navbarType"
              @update:model-value="(val) => updateFrontendLayout('navbarType', val)"
              type="select"
              :options="navbarOptions"
              label="Type de Navbar"
              is-mandatory
              size="lg"
              fill-width
              placeholder="Sélectionner..."
            />
          </GenesisTutorialItem>

          <!-- ✅ ÉTAPE 2 : Couleur primaire et secondaire -->
          <GenesisTutorialItem id="charte-colors">
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <GenesisInput
                :model-value="layoutConfig.primaryColor"
                @update:model-value="(val) => updateFrontendLayout('primaryColor', val)"
                type="color"
                label="Couleur Primaire"
                placeholder="#3B82F6"
                is-mandatory
                size="lg"
                fill-width
              />
              <GenesisInput
                :model-value="layoutConfig.secondaryColor"
                @update:model-value="(val) => updateFrontendLayout('secondaryColor', val)"
                type="color"
                label="Couleur Secondaire"
                placeholder="#64748B"
                is-mandatory
                size="lg"
                fill-width
              />
            </div>
          </GenesisTutorialItem>

          <!-- ✅ ÉTAPE 3 : Brand assets -->
          <GenesisTutorialItem id="charte-assets">
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <GenesisInput
                :model-value="layoutConfig.logoPath"
                @update:model-value="(val) => updateFrontendLayout('logoPath', val)"
                type="file"
                label="Fichier Logo"
                placeholder="Aucun fichier sélectionné"
                accept=".png,.jpg,.jpeg,.svg"
                size="lg"
                fill-width
                @browse="() => emit('request-file-path', { field: 'logoPath', extensions: ['png', 'jpg', 'jpeg', 'svg'] })"
              />
              <GenesisInput
                :model-value="layoutConfig.faviconPath"
                @update:model-value="(val) => updateFrontendLayout('faviconPath', val)"
                type="file"
                label="Fichier Favicon"
                placeholder="Aucun fichier sélectionné"
                accept=".ico,.png,.svg"
                size="lg"
                fill-width
                @browse="() => emit('request-file-path', { field: 'faviconPath', extensions: ['ico', 'png', 'svg'] })"
              />
            </div>
          </GenesisTutorialItem>
        </div>
      </div>
    </div>
    
    <!-- PARTIE DROITE : CARROUSEL TUTORIEL (Fixe) -->
    <div class="w-1/2 h-full shrink-0">
      <CarrouselPanel :slides="panelSlides" />
    </div>

  </main>
</template>

<script setup lang="ts">
import { computed, ref, watch, onMounted, nextTick } from 'vue';

import type { InterfaceLanguage } from '@genesis-labs/shared-types';
import { useGenerator } from '@genesis-labs/web-core/features/generator/composables/useGenerator';
import { useFrontendStore } from '@genesis-labs/web-core/features/frontend/store/useFrontend.store';
import { useConfigurationManager } from '@genesis-labs/web-core/core/composables/ux/useConfigurationManager';
import { useConfig } from '@genesis-labs/web-core/core/features/config/composables/useConfig';
import { useConfigManagerLogic } from '@genesis-labs/web-core/features/generator/composables/useConfigManagerLogic';
import { useGenesisTutorial } from '@genesis-labs/web-core/core/features/tutorial/composables/useGenesisTutorial'; // ✅ AJOUT

import GenesisInput from '@genesis-labs/web-core/core/components/ui/inputs/GenesisInput.vue';
import GenesisButton from '@genesis-labs/web-core/core/components/ui/actions/GenesisButton.vue';
import GenesisButtonIcon from '@genesis-labs/web-core/core/components/ui/actions/GenesisButtonIcon.vue'; // ✅ AJOUT
import GenesisConfigurationPanel from '@genesis-labs/web-core/core/components/layouts/display/configuration/GenesisConfigurationPanel.vue';
import GenesisTutorialItem from '@genesis-labs/web-core/core/features/tutorial/components/GenesisTutorialItem.vue'; // ✅ AJOUT
import IconSave from '@genesis-labs/web-core/core/components/ui/icons/IconSave.vue';
import IconUpload from '@genesis-labs/web-core/core/components/ui/icons/IconUpload.vue';
import IconHelp from '@genesis-labs/web-core/core/components/ui/icons/IconHelp.vue'; // ✅ AJOUT
import CarrouselPanel from '@genesis-labs/web-core/core/components/ui/carrousel/CarrouselPanel.vue';
import type { CarouselSlide } from '@genesis-labs/web-core/core/composables/ux/useCarousel';

// ============================================================================
// 1. EMITS
// ============================================================================
const emit = defineEmits<{
  'request-file-path': [payload: { field: 'logoPath' | 'faviconPath', extensions: string[] }];
}>();

// ============================================================================
// 2. COMPOSABLES & STORES
// ============================================================================
const { 
  stepperData, 
  pendingFrontendFramework,
  setPendingFrontendFramework,
  fetchAvailableLanguages, 
  updateFrontendLayout, 
  toggleLanguage
} = useGenerator();

const { saveConfiguration, deleteConfiguration, configurations: persistedConfigs, loadFrontendLanguagesConfigurations } = useConfig();
const frontendStore = useFrontendStore();

// ============================================================================
// 3. ÉTAT LOCAL & CONSTANTES
// ============================================================================
type NavbarType = 'side' | 'top' | '';

const navbarOptions: { label: string; value: NavbarType }[] = [
  { label: 'Barre latérale (Side)', value: 'side' },
  { label: 'Barre supérieure (Top)', value: 'top' }
];

const panelSlides: CarouselSlide[] = [
  { color: '#3B82F6', label: 'Étape 1 : Framework & Port' },
  { color: '#EF4444', label: 'Étape 2 : Langues & Layout' },
  { color: '#10B981', label: 'Étape 3 : Charte graphique' },
];

// État pour l'édition du framework
const frontendSelectorRef = ref<HTMLElement | null>(null);
const isEditingFrontend = ref(false);
const tempFrontendName = ref('');

// État pour la gestion des configurations de langues
const isApplyingLanguagesConfig = ref(false);
const languagesConfigManager = useConfigurationManager([], { singleConfiguration: false });

// ============================================================================
// 3.5. CONFIGURATION DU TUTORIEL
// ============================================================================
const tutorial = useGenesisTutorial('frontend-layout-config', {
  'languages': [
    { id: 'lang-search', text: 'Recherchez et ajoutez les langues que votre application doit supporter.' },
    { id: 'lang-config-panel', text: 'Gérez, sauvegardez et réutilisez vos configurations de langues favorites ici.' }
  ],
  'charte': [
    { id: 'charte-navbar', text: 'Choisissez le type de navigation (barre latérale ou supérieure) pour votre application.' },
    { id: 'charte-colors', text: 'Définissez les couleurs primaires et secondaires de votre charte graphique.' },
    { id: 'charte-assets', text: 'Importez votre logo et votre favicon pour personnaliser l\'identité visuelle.' }
  ]
});

// ============================================================================
// 4. COMPUTEDS (Données dérivées)
// ============================================================================
const layoutConfig = computed(() => stepperData.value.frontendLayout);
const frontend = computed(() => pendingFrontendFramework.value || stepperData.value.frontend);

const selectedFrontendName = computed(() => 
  frontend.value?.name || 'Aucun framework sélectionné'
);

const availableLanguages = computed<InterfaceLanguage[]>(() => 
  frontendStore.availableInterfaceLanguages
);

const availableFrontendsOptions = computed(() => 
  frontendStore.availableFrontendFrameworks.map(f => ({ label: f.name, value: f }))
);

const availableLanguagesOptions = computed(() => 
  availableLanguages.value.map(lang => ({ label: lang.name, value: lang.code }))
);

// ============================================================================
// 5. ACTIONS : GESTION DE L'ÉDITION DU FRAMEWORK
// ============================================================================
function startEditingFrontend() {
  tempFrontendName.value = '';
  isEditingFrontend.value = true;
}

function cancelEditingFrontend() {
  isEditingFrontend.value = false;
  tempFrontendName.value = '';
}

function handleFrontendSelect(value: any) {
  if (typeof value === 'string') return; 
  if (value && value.id) { 
    setPendingFrontendFramework(value); 
    isEditingFrontend.value = false; 
  }
}

function handleClickOutside(event: MouseEvent) {
  if (isEditingFrontend.value && frontendSelectorRef.value && !frontendSelectorRef.value.contains(event.target as Node)) {
    cancelEditingFrontend();
  }
}

watch(isEditingFrontend, (isEditing) => {
  if (isEditing) {
    setTimeout(() => window.addEventListener('mousedown', handleClickOutside), 0);
  } else {
    window.removeEventListener('mousedown', handleClickOutside);
  }
});

// ============================================================================
// 6. LOGIQUE DE CONFIGURATION DES LANGUES (Via le Composable DRY)
// ============================================================================
const languagesLogic = useConfigManagerLogic({
  manager: languagesConfigManager,
  configType: 'frontend_languages-configuration',
  defaultValues: [],
  getCurrentValues: () => layoutConfig.value.selectedInterfaceLanguages || [],
  buildPayload: (values) => ({
    languages: values || layoutConfig.value.selectedInterfaceLanguages || []
  }),
  fileNamePrefix: 'languages',
  watchSource: () => layoutConfig.value.selectedInterfaceLanguages,
  applyValues: (vals) => {
    isApplyingLanguagesConfig.value = true;
    
    const currentLangs = [...layoutConfig.value.selectedInterfaceLanguages];
    currentLangs.forEach(code => toggleLanguage(code));
    
    vals.forEach(code => {
      if (!layoutConfig.value.selectedInterfaceLanguages.includes(code)) {
        toggleLanguage(code);
      }
    });
    
    nextTick(() => { isApplyingLanguagesConfig.value = false; });
  },
  isApplyingRef: isApplyingLanguagesConfig,
  framework: frontend,
  saveConfiguration,
  deleteConfiguration,
  persistedConfigs
});

// ============================================================================
// 7. UTILITAIRES & LIFECYCLE
// ============================================================================
function getLanguageName(code: string): string {
  const lang = availableLanguages.value.find((l) => l.code === code);
  return lang ? lang.name : code;
}

async function loadLanguagesConfigurations() {
  const languagesData = await loadFrontendLanguagesConfigurations(); 
  
  languagesConfigManager.configurations.value = languagesData.map(c => {
    const p = c.payload as any;
    return { 
      id: c.id, 
      name: c.name, 
      isHidden: false, 
      components: p.languages || [] 
    };
  });
  
  languagesConfigManager.selectConfiguration(null);
  languagesLogic.handleSelect(null);
}

onMounted(async () => {
  await fetchAvailableLanguages();
  await loadLanguagesConfigurations();
});
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar { width: 6px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background-color: var(--color-secondary, #cbd5e1); border-radius: 3px; }
</style>