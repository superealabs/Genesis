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
    <div 
      class="w-2/5 min-w-[280px] shrink-0 flex flex-col items-center justify-center rounded-lg overflow-hidden relative min-h-[300px]" 
      :class="relationIllustration ? '' : 'border border-secondary border-dashed bg-secondary/10'"
    >
      <img 
        v-if="relationIllustration"
        :src="relationIllustration" 
        alt="Relation" 
        class="absolute inset-0 w-full h-full object-contain"
      />
      <template v-else>
        <svg class="w-12 h-12 opacity-60" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"/>
        </svg>
        <span class="text-sm font-medium">Aperçu du projet</span>
      </template>
    </div>
  </main>
</template>

<script setup lang="ts">
import { useGenerator } from '@genesis-labs/web-core/features/generator/composables/useGenerator';
import RelationTableView from '../relation/RelationTableView.vue';
import relationIllustration from '../../assets/relation-summary.png'

const { getRelations } = useGenerator();
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar { width: 6px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: var(--color-neutral-light); border-radius: 4px; }
.custom-scrollbar::-webkit-scrollbar-thumb:hover { background: var(--color-neutral); }
</style>