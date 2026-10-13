<!-- genesis-sdk-web/genesis-web-core/src/features/generator/components/summary/RelationSummary.vue -->
<template>
  <main class="flex gap-4 h-full overflow-hidden">
    
    <!-- PARTIE GAUCHE : CONTENU PRINCIPAL (Scrollable) -->
    <div class="flex-1 min-h-0 overflow-y-auto p-6 space-y-6 custom-scrollbar">
      
      <div class="space-y-2">
        <h3 class="text-lg font-semibold text-text flex items-center gap-2">
          <svg class="w-5 h-5 text-primary" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.828 10.172a4 4 0 00-5.656 0l-4 4a4 4 0 105.656 5.656l1.102-1.101m-.758-4.899a4 4 0 005.656 0l4-4a4 4 0 00-5.656-5.656l-1.1 1.1"/>
          </svg>
          Résumé des Relations
        </h3>
        <p class="text-sm text-text-muted">
          Aperçu des liaisons configurées entre les entités de votre base de données.
        </p>
      </div>

      <!-- Tableau de résumé des relations -->
      <div class="bg-bg-secondary/30 rounded-lg border border-neutral-light overflow-hidden">
        <table class="w-full text-left text-sm">
          <thead class="bg-bg-light text-text-muted uppercase text-xs font-semibold tracking-wider">
            <tr>
              <th class="p-4">Table Parent (Mère)</th>
              <th class="p-4">Table Child (Fille)</th>
              <th class="p-4 text-center">Formulaire (Form)</th>
              <th class="p-4 text-center">Obligatoire (Mandatory)</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-neutral-light">
            <tr 
              v-for="(rel, index) in relations" 
              :key="`${rel.parentTable}-${rel.childTable}-${index}`" 
              class="hover:bg-bg-light/50 transition-colors"
            >
              <td class="p-4 font-medium text-text">
                {{ rel.parentTable }}
              </td>
              <td class="p-4 font-medium text-text">
                {{ rel.childTable }}
              </td>
              <td class="p-4 text-center">
                <span 
                  class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium"
                  :class="rel.hasForm ? 'bg-blue-100 text-blue-800' : 'bg-gray-100 text-gray-500'"
                >
                  {{ rel.hasForm ? 'Oui' : 'Non' }}
                </span>
              </td>
              <td class="p-4 text-center">
                <span 
                  class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium"
                  :class="rel.mandatory ? 'bg-red-100 text-red-800' : 'bg-gray-100 text-gray-500'"
                >
                  {{ rel.mandatory ? 'Oui' : 'Non' }}
                </span>
              </td>
            </tr>
            
            <!-- État vide -->
            <tr v-if="relations.length === 0">
              <td colspan="4" class="p-8 text-center text-text-muted">
                <div class="flex flex-col items-center gap-2">
                  <svg class="w-8 h-8 opacity-50" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.828 10.172a4 4 0 00-5.656 0l-4 4a4 4 0 105.656 5.656l1.102-1.101m-.758-4.899a4 4 0 005.656 0l4-4a4 4 0 00-5.656-5.656l-1.1 1.1"/>
                  </svg>
                  <span>Aucune relation configurée pour ce projet.</span>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- PARTIE DROITE : CARROUSEL TUTORIEL (Fixe) -->
    <div class="w-2/5 min-w-[300px] h-full shrink-0">
      <CarrouselPanel :slides="panelSlides" />
    </div>

  </main>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useGenerator } from '@genesis-labs/web-core/features/generator/composables/useGenerator';
import CarrouselPanel from '@genesis-labs/web-core/core/components/ui/carrousel/CarrouselPanel.vue';
import type { CarouselSlide } from '@genesis-labs/web-core/core/composables/ux/useCarousel';

// ============================================================================
// 1. COMPOSABLES
// ============================================================================
const { getRelations } = useGenerator();

// On s'assure que c'est bien un tableau réactif
const relations = computed(() => getRelations.value || []);

// ============================================================================
// 2. CONSTANTES (CARROUSEL)
// ============================================================================
const panelSlides: CarouselSlide[] = [
  { color: '#3B82F6', label: 'Étape 1 : Vue d\'ensemble' },
  { color: '#EF4444', label: 'Étape 2 : Relations Parent/Child' },
  { color: '#10B981', label: 'Étape 3 : Options de génération' },
];
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar { 
  width: 6px; 
}
.custom-scrollbar::-webkit-scrollbar-track { 
  background: transparent; 
}
.custom-scrollbar::-webkit-scrollbar-thumb { 
  background-color: var(--color-secondary, #cbd5e1); 
  border-radius: 3px; 
}
</style>