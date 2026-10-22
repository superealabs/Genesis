<template>
  <main class="flex flex-row gap-12 h-full w-full p-8">
    <!-- ═══ GAUCHE : Tableau des relations (via le nouveau composant) ═══ -->
    <div class="flex-1 min-w-0 min-h-0 overflow-y-auto custom-scrollbar flex flex-col">
      <RelationTableView 
        :relations="getRelations" 
        :is-editable="false" 
      />
    </div>

    <!-- ═══ DROITE : visuel (statique, ne rétrécit jamais sous sa largeur) ═══ -->
    <div class="w-2/5 min-w-[280px] shrink-0 flex flex-col items-center justify-center gap-2 text-muted border border-secondary border-dashed bg-secondary/10 rounded-lg">
      <svg class="w-12 h-12 opacity-60" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M13.828 10.172a4 4 0 00-5.656 0l-4 4a4 4 0 105.656 5.656l1.102-1.101m-.758-4.899a4 4 0 005.656 0l4-4a4 4 0 00-5.656-5.656l-1.1 1.1"/>
      </svg>
      <span class="text-sm font-medium">Aperçu des relations</span>
      <span class="text-xs text-muted text-center px-4">La représentation visuelle du diagramme de relations sera intégrée ici.</span>
    </div>
  </main>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useGenerator } from '@genesis-labs/web-core/features/generator/composables/useGenerator';
import RelationTableView from '../relation/RelationTableView.vue';

const { getRelations } = useGenerator();
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar { width: 6px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: var(--color-neutral-light); border-radius: 4px; }
.custom-scrollbar::-webkit-scrollbar-thumb:hover { background: var(--color-neutral); }
</style>