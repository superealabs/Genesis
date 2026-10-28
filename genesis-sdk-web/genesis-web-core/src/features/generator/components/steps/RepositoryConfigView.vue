<template>
  <main class="flex gap-4 h-full overflow-hidden">
    
    <!-- PARTIE GAUCHE : FORMULAIRE DE CONFIGURATION -->
    <div class="flex-1 min-h-0 overflow-y-auto p-6 space-y-6 custom-scrollbar">
      
      <div class="space-y-4">
        <h3 class="text-lg font-semibold text-text border-b border-neutral-light pb-2">
          Configuration Repository
        </h3>

        <div>
          <!-- 1. SÉLECTION DU FOURNISSEUR -->
          <div ref="providerSelectorRef" class="flex items-end gap-3 pb-4">
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
        </div>
      </div>

      <!-- 2. INITIALISATION GIT (Section 1 du tutoriel) -->
      <div class="space-y-4">
        <div class="flex items-center justify-between border-b border-bg-neutral-genesis pb-2">
          <h3 class="text-lg font-semibold text-text">
            Initialisation Git
          </h3>
          <GenesisButtonIcon 
            variant="neutral" 
            size="sm" 
            @click="tutorial.restart('remote-switch')"
            title="Aide sur l'initialisation Git"
          >
            <IconHelp />
          </GenesisButtonIcon>
        </div>
        <p class="text-sm text-text-muted">
          Un dépôt Git local sera initialisé par défaut. Activez l'option ci-dessous pour configurer un dépôt distant.
        </p>

        <div class="w-full">
          <!-- ✅ ÉTAPE 1 : Switch Remote -->
          <GenesisTutorialItem id="remote-switch">
            <GenesisInput
              :model-value="repoConfig?.useRemote ?? false"
              @update:model-value="(val) => updateRepositoryConfig('useRemote', val)"
              type="boolean"
              label="Utiliser un dépôt distant (Remote)"
              one-line
              size="lg"
            />
          </GenesisTutorialItem>
        </div>
      </div>

      <!-- 3. CONFIGURATION DU DÉPÔT DISTANT (Sections 2 et 3 du tutoriel) -->
      <div 
        class="space-y-4 transition-all duration-200"
        :class="{ 'opacity-50 pointer-events-none': !repoConfig?.useRemote }"
      >
        <!-- ✅ DÉCLENCHEUR SECTION 2 -->
        <div class="flex items-center justify-between border-b border-bg-neutral-genesis pb-2">
          <h3 class="text-lg font-semibold text-text">
            Configuration du Dépôt Distant
          </h3>
          <GenesisButtonIcon 
            variant="neutral" 
            size="sm" 
            @click="tutorial.restart('remote-config')"
            title="Aide sur la configuration distante"
          >
            <IconHelp />
          </GenesisButtonIcon>
        </div>

        <!-- ✅ ÉTAPE 2 : Inputs de configuration distante -->
        <GenesisTutorialItem id="remote-config">
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

          <div v-if="repoConfig?.isNewRemote" class="w-full space-y-3 pt-2">
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
        </GenesisTutorialItem>

        <!-- Séparateur visuel pour la Section 3 -->
        <div class="pt-4 border-t border-bg-neutral-genesis mt-4 space-y-4">
          <!-- ✅ DÉCLENCHEUR SECTION 3 -->
          <div class="flex items-center justify-between pb-2">
            <h4 class="text-md font-semibold text-text">
              Structure des Dépôts
            </h4>
            <GenesisButtonIcon 
              variant="neutral" 
              size="sm" 
              @click="tutorial.restart('repo-structure')"
              title="Aide sur la structure des dépôts"
            >
              <IconHelp />
            </GenesisButtonIcon>
          </div>

          <!-- ✅ ÉTAPE 3 : Switch de séparation et inputs de noms -->
          <GenesisTutorialItem id="repo-structure">
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
            <div v-if="repoConfig?.scope === 'monorepo'" class="w-full pt-2">
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
            <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-4 pt-2">
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
          </GenesisTutorialItem>
        </div>

        <!-- 4. TEST DE CONNEXION (Hors tutoriel pour ne pas interrompre le flux) -->
        <div class="flex flex-col md:flex-row items-end gap-2 pt-4">
          <div class="flex-1"></div>
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

// ✅ AJOUTS POUR LE TUTORIEL
import { useGenesisTutorial } from '@genesis-labs/web-core/core/features/tutorial/composables/useGenesisTutorial';
import GenesisTutorialItem from '@genesis-labs/web-core/core/features/tutorial/components/GenesisTutorialItem.vue';
import GenesisButtonIcon from '@genesis-labs/web-core/core/components/ui/actions/GenesisButtonIcon.vue';
import IconHelp from '@genesis-labs/web-core/core/components/ui/icons/IconHelp.vue';

import GenesisInput from '@genesis-labs/web-core/core/components/ui/inputs/GenesisInput.vue';
import GenesisButton from '@genesis-labs/web-core/core/components/ui/actions/GenesisButton.vue';
import CarrouselPanel from '@genesis-labs/web-core/core/components/ui/carrousel/CarrouselPanel.vue';
import type { CarouselSlide } from '@genesis-labs/web-core/core/composables/ux/useCarousel';

const emit = defineEmits<{ 'test-connection-error': [message: string] }>();

const { 
  stepperData, 
  pendingRepositoryProvider,
  updateRepositoryConfig, 
  setPendingRepositoryProvider 
} = useGenerator();

const repositoryStore = useRepositoryStore();
const repoSvc = inject(REPOSITORY_SERVICE_KEY) as IRepositoryService;

const repoConfig = computed(() => stepperData.value.repositoryConfig);
const isTesting = ref(false);

// ============================================================================
// CONFIGURATION DU TUTORIEL
// ============================================================================
const tutorial = useGenesisTutorial('repository-config', {
  'remote-switch': [
    { id: 'remote-switch', text: 'Activez cette option pour configurer un dépôt Git distant au lieu d\'un dépôt local uniquement.' }
  ],
  'remote-config': [
    { id: 'remote-config', text: 'Renseignez ici vos identifiants (username, token) pour vous connecter à votre fournisseur Git.' }
  ],
  'repo-structure': [
    { id: 'repo-structure', text: 'Choisissez si vous souhaitez un seul dépôt (monorepo) ou séparer le frontend et le backend dans deux dépôts distincts.' }
  ]
});

// ============================================================================
// GESTION DE L'ÉDITION DU FOURNISSEUR
// ============================================================================
const providerSelectorRef = ref<HTMLElement | null>(null);
const isEditingProvider = ref(false);
const tempProviderName = ref('');

const selectedProviderName = computed(() => {
  if (pendingRepositoryProvider.value) {
    return pendingRepositoryProvider.value.name;
  }
  
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
    }
  } catch (error) {
    emit('test-connection-error', 'Erreur inattendue lors du test de connexion.');
  } finally {
    isTesting.value = false;
  }
}
</script>