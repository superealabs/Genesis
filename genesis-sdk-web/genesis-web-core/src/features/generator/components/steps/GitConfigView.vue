<template>
  <main class="flex gap-4 h-full overflow-hidden">
    
    <!-- PARTIE GAUCHE : CONTENU PRINCIPAL (Scrollable) -->
    <div class="flex-1 min-h-0 overflow-y-auto p-6 space-y-6 custom-scrollbar">
      
      <!-- 1. INITIALISATION GIT -->
      <div class="space-y-4">
        <h3 class="text-lg font-semibold text-text border-b border-neutral-light pb-2">
          Initialisation Git
        </h3>
        <p class="text-sm text-text-muted">
          Un dépôt Git local sera initialisé par défaut. Activez l'option ci-dessous pour configurer un dépôt distant (GitHub, GitLab, etc.).
        </p>

        <div class="w-full">
          <GenesisInput
            v-model="gitConfig.useRemoteRepo"
            type="boolean"
            label="Utiliser un dépôt distant (Remote)"
            one-line
            size="lg"
          />
        </div>
      </div>

      <div class="border-t border-secondary"></div>

      <!-- 2. CONFIGURATION DU DÉPÔT DISTANT -->
      <!-- Ce bloc reste toujours dans le DOM, mais est désactivé visuellement et fonctionnellement -->
      <div 
        class="space-y-4 transition-all duration-200"
        :class="{ 'opacity-50 pointer-events-none': !gitConfig.useRemoteRepo }"
      >
        <h3 class="text-lg font-semibold text-text border-b border-secondary pb-2">
          Configuration du Dépôt Distant
        </h3>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <GenesisInput
            v-model="gitConfig.isNewRemoteRepo"
            type="boolean"
            label="Créer un nouveau dépôt distant"
            one-line
            size="lg"
          />
          <GenesisInput
            v-model="gitConfig.githubUsername"
            type="text"
            label="Nom d'utilisateur GitHub"
            placeholder="votre-username"
            is-mandatory
            size="lg"
            fill-width
          />
        </div>

        <div v-if="gitConfig.isNewRemoteRepo" class="w-full space-y-3">
          <GenesisInput
            v-model="gitConfig.githubToken"
            type="password"
            label="Personal Access Token (GitHub)"
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
            v-model="gitConfig.separateRepositories"
            type="boolean"
            label="Séparer les dépôts (Frontend & Backend)"
            one-line
            size="lg"
          />
        </div>

        <!-- Cas 1 : Mono-repo -->
        <div v-if="!gitConfig.separateRepositories" class="w-full">
          <GenesisInput
            v-model="gitConfig.repositoryName"
            type="text"
            label="Nom du dépôt"
            placeholder="mon-projet-complet"
            is-mandatory
            size="lg"
            fill-width
          />
        </div>

        <!-- Cas 2 : Deux dépôts séparés (Grille responsive) -->
        <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <GenesisInput
            v-model="gitConfig.backendRepositoryName"
            type="text"
            label="Nom du dépôt Backend"
            placeholder="mon-projet-api"
            is-mandatory
            size="lg"
            fill-width
          />
          <GenesisInput
            v-model="gitConfig.frontendRepositoryName"
            type="text"
            label="Nom du dépôt Frontend"
            placeholder="mon-projet-ui"
            is-mandatory
            size="lg"
            fill-width
          />
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
import { computed } from 'vue';
import { useGenerator } from '@genesis-labs/web-core/features/generator/composables/useGenerator';
import GenesisInput from '@genesis-labs/web-core/core/components/ui/inputs/GenesisInput.vue';
import CarrouselPanel from '@genesis-labs/web-core/core/components/ui/carrousel/CarrouselPanel.vue';
import type { CarouselSlide } from '@genesis-labs/web-core/core/composables/ux/useCarousel';

const { stepperData } = useGenerator();

const gitConfig = computed(() => stepperData.value.git);

const panelSlides: CarouselSlide[] = [
  { color: '#3B82F6', label: 'Étape 1 : Initialisation Git' },
  { color: '#EF4444', label: 'Étape 2 : Configuration Remote' },
  { color: '#10B981', label: 'Étape 3 : Structure des dépôts' },
];
</script>