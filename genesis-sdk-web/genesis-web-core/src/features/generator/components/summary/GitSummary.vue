<!-- genesis-sdk-web/genesis-web-core/src/features/generator/components/summary/GitSummary.vue -->
<template>
  <div class="flex-1 min-h-0 overflow-y-auto p-6 space-y-6 custom-scrollbar max-w-4xl mx-auto w-full">
    <h3 class="text-lg font-semibold text-text mb-4">Résumé de la Configuration Git</h3>

    <!-- Section 1 : Stratégie d'initialisation -->
    <section class="space-y-4">
      <h4 class="text-base font-semibold text-text flex items-center gap-2">
        <svg class="w-5 h-5 text-primary" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>
        </svg>
        Stratégie d'initialisation
      </h4>
      
      <div class="bg-bg-secondary/30 rounded-lg p-4 border border-neutral-light">
        <div class="flex items-center gap-3">
          <div 
            class="w-10 h-10 rounded-full flex items-center justify-center flex-shrink-0"
            :class="gitConfig.useRemoteRepo ? 'bg-blue-500/10 text-blue-500' : 'bg-green-500/10 text-green-500'"
          >
            <svg v-if="gitConfig.useRemoteRepo" class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3.055 11H5a2 2 0 012 2v1a2 2 0 002 2 2 2 0 012 2v2.945M8 3.935V5.5A2.5 2.5 0 0010.5 8h.5a2 2 0 012 2 2 2 0 104 0 2 2 0 012-2h1.064M15 20.488V18a2 2 0 012-2h3.064M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
            </svg>
            <svg v-else class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 12h14M5 12a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v4a2 2 0 01-2 2M5 12a2 2 0 00-2 2v4a2 2 0 002 2h14a2 2 0 002-2v-4a2 2 0 00-2-2m-2-4h.01M17 16h.01"/>
            </svg>
          </div>
          <div>
            <p class="text-sm font-medium text-text">
              {{ gitConfig.useRemoteRepo ? 'Dépôt Local + Dépôt Distant (Remote)' : 'Dépôt Local uniquement' }}
            </p>
            <p class="text-xs text-muted mt-1">
              {{ gitConfig.useRemoteRepo 
                ? 'Le projet sera initialisé localement et lié à un dépôt distant.' 
                : 'Seul un dépôt Git local (.git) sera initialisé dans le répertoire du projet.' }}
            </p>
          </div>
        </div>
      </div>
    </section>

    <!-- Section 2 : Configuration du Dépôt Distant (Conditionnelle) -->
    <section v-if="gitConfig.useRemoteRepo" class="space-y-4">
      <h4 class="text-base font-semibold text-text flex items-center gap-2">
        <svg class="w-5 h-5 text-primary" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"/>
        </svg>
        Détails du Dépôt Distant
      </h4>
      
      <div class="bg-bg-secondary/30 rounded-lg p-4 border border-neutral-light space-y-4">
        
        <!-- Identifiants -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div class="space-y-1">
            <span class="text-xs text-muted">Nom d'utilisateur</span>
            <p class="text-sm font-medium text-text font-mono">{{ gitConfig.githubUsername || 'Non défini' }}</p>
          </div>
          <div class="space-y-1">
            <span class="text-xs text-muted">Personal Access Token</span>
            <div class="flex items-center gap-2">
              <svg v-if="gitConfig.githubToken" class="w-4 h-4 text-green-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"/>
              </svg>
              <svg v-else class="w-4 h-4 text-red-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"/>
              </svg>
              <p class="text-sm font-medium text-text">
                {{ gitConfig.githubToken ? '••••••••••••••••' : 'Non fourni' }}
              </p>
            </div>
          </div>
        </div>

        <div class="border-t border-neutral-light"></div>

        <!-- Structure des dépôts -->
        <div class="space-y-3">
          <div class="flex items-center gap-2">
            <span class="text-xs text-muted uppercase font-semibold tracking-wider">Structure</span>
            <span class="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-primary/10 text-primary">
              {{ gitConfig.separateRepositories ? 'Multi-repo (Frontend & Backend)' : 'Mono-repo' }}
            </span>
          </div>

          <!-- Noms des dépôts -->
          <div v-if="!gitConfig.separateRepositories" class="space-y-1">
            <span class="text-xs text-muted">Nom du dépôt unique</span>
            <p class="text-sm font-medium text-text font-mono bg-bg-light p-2 rounded border border-neutral-light">
              {{ gitConfig.repositoryName || 'Non défini' }}
            </p>
          </div>

          <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div class="space-y-1">
              <span class="text-xs text-muted">Nom du dépôt Backend</span>
              <p class="text-sm font-medium text-text font-mono bg-bg-light p-2 rounded border border-neutral-light">
                {{ gitConfig.backendRepositoryName || 'Non défini' }}
              </p>
            </div>
            <div class="space-y-1">
              <span class="text-xs text-muted">Nom du dépôt Frontend</span>
              <p class="text-sm font-medium text-text font-mono bg-bg-light p-2 rounded border border-neutral-light">
                {{ gitConfig.frontendRepositoryName || 'Non défini' }}
              </p>
            </div>
          </div>
        </div>

      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useGenerator } from '@genesis-labs/web-core/features/generator/composables/useGenerator';

const { stepperData } = useGenerator();

const gitConfig = computed(() => stepperData.value.git);
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar {
  width: 8px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: var(--color-neutral-light);
  border-radius: 4px;
}
.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: var(--color-neutral);
}
</style>