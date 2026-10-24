<template>
  <main class="flex gap-4 h-full overflow-hidden">
    <div class="flex-1 min-h-0 overflow-y-auto p-6 space-y-6 custom-scrollbar">
      
      <!-- 1. INFORMATIONS DU FOURNISSEUR -->
      <div class="space-y-4">
        <h3 class="text-lg font-semibold text-text border-b border-neutral-light pb-2">
          Fournisseur de Dépôt
        </h3>
        <div class="flex items-center gap-3 p-3 bg-bg-secondary/30 rounded border border-transparent">
          <span class="text-sm font-medium text-muted">Fournisseur sélectionné :</span>
          <span class="text-sm font-semibold text-text">{{ repoConfig?.provider || 'Non sélectionné' }}</span>
        </div>
      </div>

      <div class="border-t border-secondary"></div>

      <!-- 2. CONFIGURATION DISTANTE (REMOTE) -->
      <div class="space-y-4">
        <h3 class="text-lg font-semibold text-text border-b border-secondary pb-2">
          Configuration du Dépôt
        </h3>
        
        <div class="w-full">
          <GenesisInput
            :model-value="repoConfig?.useRemote ?? false"
            @update:model-value="(val) => updateRepositoryConfig('useRemote', val)" 
            type="boolean" label="Utiliser un dépôt distant (Remote)" one-line size="lg"
          />
        </div>

        <div v-if="repoConfig?.useRemote" class="space-y-4 p-4 bg-bg-light/50 rounded-lg border border-secondary/50">
          <div class="w-full">
            <GenesisInput
              :model-value="repoConfig?.isNewRemote ?? true"
              @update:model-value="(val) => updateRepositoryConfig('isNewRemote', val)"
              type="boolean" label="Créer un nouveau dépôt distant" one-line size="lg"
            />
          </div>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <GenesisInput
              :model-value="repoConfig?.username || ''"
              @update:model-value="(val) => updateRepositoryConfig('username', val)"
              type="text" label="Nom d'utilisateur" placeholder="votre-username" size="lg" fill-width :is-mandatory="true"
            />
            <GenesisInput
              :model-value="repoConfig?.token || ''"
              @update:model-value="(val) => updateRepositoryConfig('token', val)"
              type="password" label="Personal Access Token (PAT)" placeholder="ghp_..." size="lg" fill-width :is-mandatory="true"
            />
          </div>

          <div class="w-full">
            <GenesisInput
              :model-value="repoConfig?.scope === 'separated'"
              @update:model-value="(val) => updateRepositoryConfig('scope', val ? 'separated' : 'monorepo')"
              type="boolean" label="Séparer les dépôts (Frontend & Backend)" one-line size="lg"
            />
          </div>

          <div v-if="repoConfig?.scope === 'monorepo'" class="w-full">
            <GenesisInput
              :model-value="repoConfig?.monorepoName || ''"
              @update:model-value="(val) => updateRepositoryConfig('monorepoName', val)"
              type="text" label="Nom du dépôt unique" placeholder="mon-projet-complet" size="lg" fill-width :is-mandatory="true"
            />
          </div>

          <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <GenesisInput
              :model-value="repoConfig?.backendRepoName || ''"
              @update:model-value="(val) => updateRepositoryConfig('backendRepoName', val)"
              type="text" label="Nom du dépôt Backend" placeholder="mon-projet-api" size="lg" fill-width :is-mandatory="true"
            />
            <GenesisInput
              :model-value="repoConfig?.frontendRepoName || ''"
              @update:model-value="(val) => updateRepositoryConfig('frontendRepoName', val)"
              type="text" label="Nom du dépôt Frontend" placeholder="mon-projet-ui" size="lg" fill-width :is-mandatory="true"
            />
          </div>

          <div class="flex justify-end pt-2">
            <GenesisButton variant="secondary" size="md" @click="testConnection" :disabled="isTesting">
              {{ isTesting ? 'Test en cours...' : 'Tester la connexion' }}
            </GenesisButton>
          </div>
        </div>
      </div>
    </div>
    
    <div class="w-1/2 h-full shrink-0">
      <CarrouselPanel :slides="panelSlides" />
    </div>
  </main>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { useGenerator } from '@genesis-labs/web-core/features/generator/composables/useGenerator';
import { REPOSITORY_SERVICE_KEY } from '@genesis-labs/web-core/features/repository/manifest';
import { inject } from 'vue';
import type { IRepositoryService } from '@genesis-labs/shared-types';

import GenesisInput from '@genesis-labs/web-core/core/components/ui/inputs/GenesisInput.vue';
import GenesisButton from '@genesis-labs/web-core/core/components/ui/actions/GenesisButton.vue';
import CarrouselPanel from '@genesis-labs/web-core/core/components/ui/carrousel/CarrouselPanel.vue';
import type { CarouselSlide } from '@genesis-labs/web-core/core/composables/ux/useCarousel';

const emit = defineEmits<{ 'test-connection-error': [message: string] }>();

// CORRECTION : On destructure bien updateRepositoryConfig (et on l'utilise dans le template)
const { stepperData, updateRepositoryConfig } = useGenerator();
const repoSvc = inject(REPOSITORY_SERVICE_KEY) as IRepositoryService;

const repoConfig = computed(() => stepperData.value.repositoryConfig);
const isTesting = ref(false);

const panelSlides: CarouselSlide[] = [
  { color: '#3B82F6', label: 'Étape 1 : Choix du mode (Local ou Distant)' },
  { color: '#EF4444', label: 'Étape 2 : Authentification (Token ou SSH)' },
  { color: '#10B981', label: 'Étape 3 : Structure des dépôts (Mono ou Séparé)' },
];

async function testConnection() {
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