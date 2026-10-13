<!-- genesis-sdk-web/genesis-web-core/src/features/generator/components/summary/FrontendSummary.vue -->
<template>
  <div class="flex-1 min-h-0 overflow-y-auto p-6 space-y-6 custom-scrollbar max-w-4xl mx-auto w-full">
    <h3 class="text-lg font-semibold text-text mb-4">Résumé de la Configuration Frontend</h3>

    <!-- Section 1 : Framework et Environnement -->
    <section class="space-y-4">
      <h4 class="text-base font-semibold text-text flex items-center gap-2">
        <svg class="w-5 h-5 text-primary" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.75 17L9 20l-1 1h8l-1-1-.75-3M3 13h18M5 17h14a2 2 0 002-2V5a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"/>
        </svg>
        Framework et Environnement
      </h4>
      
      <div class="bg-bg-secondary/30 rounded-lg p-4 border border-neutral-light">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div class="space-y-1">
            <span class="text-xs text-muted">Framework Frontend</span>
            <p class="text-sm font-medium text-text">{{ frontend?.name || 'Non sélectionné' }}</p>
          </div>
          <div class="space-y-1">
            <span class="text-xs text-muted">Port d'exécution</span>
            <p class="text-sm font-medium text-text">{{ layoutConfig.port || 'Non défini' }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- Section 2 : Langues Supportées -->
    <section class="space-y-4">
      <h4 class="text-base font-semibold text-text flex items-center gap-2">
        <svg class="w-5 h-5 text-primary" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 5h12M9 3v2m1.048 9.5A18.022 18.022 0 016.412 9m6.088 9h7M11 21l5-10 5 10M12.751 5C11.783 10.77 8.07 15.61 3 18.129"/>
        </svg>
        Langues Supportées
      </h4>
      
      <div class="bg-bg-secondary/30 rounded-lg p-4 border border-neutral-light">
        <div v-if="layoutConfig.selectedInterfaceLanguages.length > 0" class="flex flex-wrap gap-2">
          <span 
            v-for="code in layoutConfig.selectedInterfaceLanguages" 
            :key="code"
            class="inline-flex items-center px-3 py-1 rounded-full text-xs font-medium bg-primary/10 text-primary border border-primary/20"
          >
            {{ getLanguageName(code) }}
          </span>
        </div>
        <p v-else class="text-sm text-muted italic">Aucune langue sélectionnée</p>
      </div>
    </section>

    <!-- Section 3 : Charte Graphique et Layout -->
    <section class="space-y-4">
      <h4 class="text-base font-semibold text-text flex items-center gap-2">
        <svg class="w-5 h-5 text-primary" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 21a4 4 0 01-4-4V5a2 2 0 012-2h4a2 2 0 012 2v12a4 4 0 01-4 4zm0 0h12a2 2 0 002-2v-4a2 2 0 00-2-2h-2.343M11 7.343l1.657-1.657a2 2 0 012.828 0l2.829 2.829a2 2 0 010 2.828l-8.486 8.485M7 17h.01"/>
        </svg>
        Charte Graphique et Layout
      </h4>
      
      <div class="bg-bg-secondary/30 rounded-lg p-4 border border-neutral-light space-y-4">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div class="space-y-1">
            <span class="text-xs text-muted">Type de Navbar</span>
            <p class="text-sm font-medium text-text capitalize">
              {{ layoutConfig.navbarType === 'side' ? 'Barre latérale (Side)' : layoutConfig.navbarType === 'top' ? 'Barre supérieure (Top)' : 'Non défini' }}
            </p>
          </div>
        </div>

        <div class="border-t border-neutral-light pt-4">
          <span class="text-xs text-muted block mb-2">Couleurs</span>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div class="flex items-center gap-3 p-3 bg-bg-light rounded border border-neutral-light">
              <div 
                class="w-8 h-8 rounded-full border border-neutral-light shadow-sm flex-shrink-0" 
                :style="{ backgroundColor: layoutConfig.primaryColor || '#cbd5e1' }"
              ></div>
              <div class="flex flex-col min-w-0">
                <span class="text-xs text-muted">Primaire</span>
                <span class="text-sm font-medium text-text font-mono truncate">{{ layoutConfig.primaryColor || 'Non défini' }}</span>
              </div>
            </div>
            <div class="flex items-center gap-3 p-3 bg-bg-light rounded border border-neutral-light">
              <div 
                class="w-8 h-8 rounded-full border border-neutral-light shadow-sm flex-shrink-0" 
                :style="{ backgroundColor: layoutConfig.secondaryColor || '#cbd5e1' }"
              ></div>
              <div class="flex flex-col min-w-0">
                <span class="text-xs text-muted">Secondaire</span>
                <span class="text-sm font-medium text-text font-mono truncate">{{ layoutConfig.secondaryColor || 'Non défini' }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="border-t border-neutral-light pt-4">
          <span class="text-xs text-muted block mb-2">Assets</span>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div class="space-y-1">
              <span class="text-xs text-muted">Logo</span>
              <p class="text-sm font-medium text-text truncate">
                {{ layoutConfig.logoPath ? getFileName(layoutConfig.logoPath) : 'Non défini' }}
              </p>
            </div>
            <div class="space-y-1">
              <span class="text-xs text-muted">Favicon</span>
              <p class="text-sm font-medium text-text truncate">
                {{ layoutConfig.faviconPath ? getFileName(layoutConfig.faviconPath) : 'Non défini' }}
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
import { useFrontendStore } from '@genesis-labs/web-core/features/frontend/store/useFrontend.store';

const { stepperData } = useGenerator();
const frontendStore = useFrontendStore();

const frontend = computed(() => stepperData.value.frontend);
const layoutConfig = computed(() => stepperData.value.frontendLayout);

// Résolution du nom de la langue à partir de son code
const getLanguageName = (code: string): string => {
  const lang = frontendStore.availableInterfaceLanguages.find((l) => l.code === code);
  return lang ? lang.name : code;
};

// Extraction du nom du fichier à partir du chemin complet
const getFileName = (path: string): string => {
  return path.split(/[\\/]/).pop() || path;
};
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