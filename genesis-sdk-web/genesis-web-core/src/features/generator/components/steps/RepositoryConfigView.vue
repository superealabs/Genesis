<template>
  <main class="flex gap-4 h-full overflow-hidden">
    
    <!-- PARTIE GAUCHE : FORMULAIRE DE CONFIGURATION -->
    <div class="flex-1 min-h-0 overflow-y-auto p-6 space-y-6 custom-scrollbar">
      
      <!-- 1. SÉLECTION DU FOURNISSEUR (Pattern DatabaseConfigView) -->
      <div ref="providerSelectorRef" class="flex items-end gap-3 pb-4 border-b border-neutral-light">
        <div class="flex-1">
          <template v-if="isEditingProvider">
            <GenesisInput
              type="combobox"
              :options="availableProvidersOptions"
              :model-value="tempProviderName"
              @update:model-value="handleProviderSelect"
              label="Fournisseur de Dépôt"
              placeholder="Rechercher un fournisseur..."
              size="lg"
              fill-width
            />
          </template>
          <template v-else>
            <div class="flex flex-col gap-1">
              <label class="text-sm font-medium text-muted">
                Fournisseur Sélectionné <span class="text-accent ml-0.5">*</span>
              </label>
              <div class="flex items-center gap-2 h-10 px-3 bg-bg-secondary/30 rounded text-text border border-transparent">
                <span class="truncate font-medium capitalize">{{ selectedProviderName }}</span>
              </div>
            </div>
          </template>
        </div>
        
        <div class="pb-0.5">
          <GenesisButton v-if="!isEditingProvider" variant="secondary" size="lg" @click="startEditingProvider">
            Modifier
          </GenesisButton>
          <GenesisButton v-else variant="tertiary" size="lg" @click="cancelEditingProvider">
            Annuler
          </GenesisButton>
        </div>
      </div>

      <!-- 2. INITIALISATION GIT (Pattern GitConfigView) -->
      <div class="space-y-4">
        <h3 class="text-lg font-semibold text-text border-b border-neutral-light pb-2">
          Initialisation Git
        </h3>
        <p class="text-sm text-text-muted">
          Un dépôt Git local sera initialisé par défaut. Activez l'option ci-dessous pour configurer un dépôt distant.
        </p>

        <div class="w-full">
          <GenesisInput
            :model-value="repoConfig?.useRemote ?? false"
            @update:model-value="(val) => updateRepositoryConfig('useRemote', val)"
            type="boolean"
            label="Utiliser un dépôt distant (Remote)"
            one-line
            size="lg"
          />
        </div>
      </div>

      <div class="border-t border-secondary"></div>

      <!-- 3. CONFIGURATION DU DÉPÔT DISTANT -->
      <!-- Ce bloc reste dans le DOM mais est désactivé visuellement et fonctionnellement si useRemote est faux -->
      <div 
        class="space-y-4 transition-all duration-200"
        :class="{ 'opacity-50 pointer-events-none': !repoConfig?.useRemote }"
      >
        <h3 class="text-lg font-semibold text-text border-b border-secondary pb-2">
          Configuration du Dépôt Distant
        </h3>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <GenesisInput
            :model-value="repoConfig?.isNewRemote ?? true"
            @update:model-value="(val) => updateRepositoryConfig('isNewRemote', val)"
            type="boolean"
            label="Créer un nouveau dépôt distant"
            one-line
            size="lg"
          />
          <GenesisInput
            :model-value="repoConfig?.username || ''"
            @update:model-value="(val) => updateRepositoryConfig('username', val)"
            type="text"
            label="Nom d'utilisateur"
            placeholder="votre-username"
            is-mandatory
            size="lg"
            fill-width
          />
        </div>

        <div v-if="repoConfig?.isNewRemote" class="w-full space-y-3">
          <GenesisInput
            :model-value="repoConfig?.token || ''"
            @update:model-value="(val) => updateRepositoryConfig('token', val)"
            type="password"
            label="Personal Access Token (PAT)"
            placeholder="ghp_..."
            is-mandatory
            size="lg"
            fill-width
          />
          <p class="text-xs text-text-muted flex items-start gap-1.5">
            <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="mt-0.5 shrink-0 text-accent">
              <circle cx="12" cy="12" r="10"/>
              <line x1="12" y1="16" x2="12" y2="12"/>
              <line x1="12" y1="8" x2="12.01" y2="8"/>
            </svg>
            <span>Le token doit impérativement posséder les permissions <code class="bg-secondary px-1 rounded text-text font-mono">repo</code>.</span>
          </p>
        </div>

        <div class="border-t border-secondary my-2"></div>

        <div class="w-full">
          <GenesisInput
            :model-value="repoConfig?.scope === 'separated'"
            @update:model-value="(val) => updateRepositoryConfig('scope', val ? 'separated' : 'monorepo')"
            type="boolean"
            label="Séparer les dépôts (Frontend & Backend)"
            one-line
            size="lg"
          />
        </div>

        <!-- Cas 1 : Mono-repo -->
        <div v-if="repoConfig?.scope === 'monorepo'" class="w-full">
          <GenesisInput
            :model-value="repoConfig?.monorepoName || ''"
            @update:model-value="(val) => updateRepositoryConfig('monorepoName', val)"
            type="text"
            label="Nom du dépôt unique"
            placeholder="mon-projet-complet"
            is-mandatory
            size="lg"
            fill-width
          />
        </div>

        <!-- Cas 2 : Deux dépôts séparés -->
        <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <GenesisInput
            :model-value="repoConfig?.backendRepoName || ''"
            @update:model-value="(val) => updateRepositoryConfig('backendRepoName', val)"
            type="text"
            label="Nom du dépôt Backend"
            placeholder="mon-projet-api"
            is-mandatory
            size="lg"
            fill-width
          />
          <GenesisInput
            :model-value="repoConfig?.frontendRepoName || ''"
            @update:model-value="(val) => updateRepositoryConfig('frontendRepoName', val)"
            type="text"
            label="Nom du dépôt Frontend"
            placeholder="mon-projet-ui"
            is-mandatory
            size="lg"
            fill-width
          />
        </div>

        <!-- 4. TEST DE CONNEXION -->
        <div class="flex flex-col md:flex-row items-end gap-2 pt-2">
          <div class="flex-1"></div> <!-- Spacer pour pousser le bouton à droite -->
          <GenesisButton
            variant="secondary" 
            content-align="center"
            :disabled="isTesting || !repoConfig?.useRemote"
            size="lg"
            @click="handleTestConnection"
            class="w-full md:w-auto"
          >
            <span v-if="isTesting">Test en cours...</span>
            <span v-else>Test Connexion</span>
          </GenesisButton>
        </div>
      </div>
    </div>

    <!-- PARTIE DROITE : CARROUSEL TUTORIEL -->
    <div class="w-1/2 h-full shrink-0">
      <CarrouselPanel :slides="panelSlides" />
    </div>

  </main>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { useGenerator } from '@genesis-labs/web-core/features/generator/composables/useGenerator';
import { useRepositoryStore } from '@genesis-labs/web-core/features/repository/store/useRepository.store';
import { REPOSITORY_SERVICE_KEY } from '@genesis-labs/web-core/features/repository/manifest';
import { inject } from 'vue';
import type { IRepositoryService } from '@genesis-labs/shared-types';

import GenesisInput from '@genesis-labs/web-core/core/components/ui/inputs/GenesisInput.vue';
import GenesisButton from '@genesis-labs/web-core/core/components/ui/actions/GenesisButton.vue';
import CarrouselPanel from '@genesis-labs/web-core/core/components/ui/carrousel/CarrouselPanel.vue';
import type { CarouselSlide } from '@genesis-labs/web-core/core/composables/ux/useCarousel';

const emit = defineEmits<{ 'test-connection-error': [message: string] }>();

const { 
  stepperData, 
  updateRepositoryConfig, 
  setPendingRepositoryProvider 
} = useGenerator();

const repositoryStore = useRepositoryStore();
const repoSvc = inject(REPOSITORY_SERVICE_KEY) as IRepositoryService;

const repoConfig = computed(() => stepperData.value.repositoryConfig);
const isTesting = ref(false);

// ============================================================================
// GESTION DE L'ÉDITION DU FOURNISSEUR
// ============================================================================
const providerSelectorRef = ref<HTMLElement | null>(null);
const isEditingProvider = ref(false);
const tempProviderName = ref('');

const selectedProviderName = computed(() => {
  const providerId = repoConfig.value?.provider;
  if (!providerId) return 'Aucun fournisseur sélectionné';
  
  const provider = repositoryStore.providers.find(p => p.id === providerId);
  return provider ? provider.name : providerId;
});

const availableProvidersOptions = computed(() => 
  repositoryStore.providers.map(p => ({ label: p.name, value: p }))
);

function startEditingProvider() {
  tempProviderName.value = '';
  isEditingProvider.value = true;
}

function cancelEditingProvider() {
  isEditingProvider.value = false;
  tempProviderName.value = '';
}

function handleProviderSelect(value: any) {
  if (typeof value === 'string') return;
  if (value && value.id) {
    setPendingRepositoryProvider(value);
    updateRepositoryConfig('provider', value.id);
    isEditingProvider.value = false;
  }
}

function handleClickOutside(event: MouseEvent) {
  if (isEditingProvider.value && providerSelectorRef.value && !providerSelectorRef.value.contains(event.target as Node)) {
    cancelEditingProvider();
  }
}

watch(isEditingProvider, (isEditing) => {
  if (isEditing) {
    setTimeout(() => window.addEventListener('mousedown', handleClickOutside), 0);
  } else {
    window.removeEventListener('mousedown', handleClickOutside);
  }
});

// ============================================================================
// CONSTANTES (CARROUSEL)
// ============================================================================
const panelSlides: CarouselSlide[] = [
  { color: '#3B82F6', label: 'Étape 1 : Choix du fournisseur' },
  { color: '#EF4444', label: 'Étape 2 : Configuration Remote' },
  { color: '#10B981', label: 'Étape 3 : Structure des dépôts' },
];

// ============================================================================
// ACTIONS
// ============================================================================
async function handleTestConnection() {
  if (!repoConfig.value) return;
  isTesting.value = true;
  try {
    const result = await repoSvc.testConnection(repoConfig.value);
    if (!result.success) {
      emit('test-connection-error', result.message);
    } else {
      console.log('Connexion réussie :', result.message);
      // Tu pourrais ajouter ici un toast de succès si ton système de notification le permet
    }
  } catch (error) {
    emit('test-connection-error', 'Erreur inattendue lors du test de connexion.');
  } finally {
    isTesting.value = false;
  }
}
</script>