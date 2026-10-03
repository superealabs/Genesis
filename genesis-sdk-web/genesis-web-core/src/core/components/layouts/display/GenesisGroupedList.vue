<template>
  <div class="space-y-6">
    <!-- ═══ CONTRÔLE DE REGROUPEMENT (Optionnel) ═══ -->
    <div v-if="groupOptions && groupOptions.length > 0" class="flex justify-end mb-2">
      <select 
        :value="groupBy ?? 'null'" 
        @change="$emit('update:groupBy', ($event.target as HTMLSelectElement).value === 'null' ? null : ($event.target as HTMLSelectElement).value)"
        class="text-xs bg-bg-light border border-secondary rounded px-2 py-1 text-muted focus:outline-none focus:ring-1 focus:ring-primary cursor-pointer hover:border-primary transition-colors"
      >
        <option v-for="opt in groupOptions" :key="String(opt.value ?? 'null')" :value="String(opt.value ?? 'null')">
          {{ opt.label }}
        </option>
      </select>
    </div>

    <!-- ═══ BOUCLE DES SECTIONS ═══ -->
    <div v-for="section in groupedItems" :key="section.title" class="animate-fade-in">

      <GenesisDisclosure
        :title="section.title"
        :default-open="true"
        :variant="'secondary'"
        content-class="!pt-10"
      >
        <!-- Titre de la section avec compteur -->
        <template #title>
          <span class="text-2xl font-semibold">{{ section.title }}</span>
          <span class="text-xs font-normal text-muted bg-bg-light px-2 py-0.5 rounded-full border border-secondary">
            {{ section.items.length }}
          </span>
        </template>

        <!-- Liste générique (reçoit les éléments via le scoped slot) -->
         <div class="section-body">
          <GenesisList
            :display="display"
            :minColWidth="minColWidth"
            :headers="headers"
            :showHeader="showHeader"
            :haveActions="haveActions"
          >
            <slot :items="section.items" />
          </GenesisList>
         </div>
      </GenesisDisclosure>

    </div>
  </div>
</template>

<script setup lang="ts" generic="T extends Record<string, any>">
import { computed } from 'vue';
import GenesisList from './GenesisList.vue';
import type { DisplayMode } from './items/GenesisItem.types.ts';
import GenesisDisclosure from '../GenesisDisclosure.vue';

const props = withDefaults(defineProps<{
  items: T[];
  groupBy?: keyof T | null;
  groupOptions?: { label: string; value: keyof T | null }[];
  // ✅ CORRECTION : On retire 'boolean' car les clés d'objet sont toujours string ou number en JS/TS
  groupLabels?: Record<string | number, string>;
  display?: DisplayMode;
  minColWidth?: string;
  headers?: { label: string; class?: string }[];
  showHeader?: boolean;
  haveActions?: boolean;
}>(), {
  groupBy: null,
  groupOptions: () => [],
  display: 'grid',
  minColWidth: '120px',
  showHeader: true,
  haveActions: false,
});

defineEmits<{
  'update:groupBy': [value: keyof T | null];
}>();

const groupedItems = computed(() => {
  if (!props.groupBy) {
    return [{ title: 'Tous', items: props.items }];
  }

  const map = new Map<string, T[]>();
  
  props.items.forEach(item => {
    const rawValue = item[props.groupBy as keyof T];
    let displayTitle = 'Non spécifié';

    if (rawValue !== null && rawValue !== undefined && rawValue !== '') {
      // Si c'est un booléen, on le convertit en string ("true" ou "false") pour matcher le Record.
      const lookupKey = typeof rawValue === 'boolean' ? String(rawValue) : rawValue;

      if (props.groupLabels && lookupKey in props.groupLabels) {
        displayTitle = props.groupLabels[lookupKey as keyof typeof props.groupLabels];
      } else {
        displayTitle = String(rawValue);
      }
    }

    if (!map.has(displayTitle)) {
      map.set(displayTitle, []);
    }
    map.get(displayTitle)!.push(item);
  });

  return Array.from(map.entries())
    .map(([title, items]) => ({ title, items }))
    .sort((a, b) => a.title.localeCompare(b.title));
});
</script>

<style scoped>

/* Laisse la place à la mascotte (32px) qui dépasse au-dessus de la première rangée */
.section-body {
  padding-top: 1rem; /* 32px, s'ajoute au pt-2 du panneau : 40px au total */
}

.animate-fade-in { 
  animation: fadeIn 0.3s ease-out; 
}
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(5px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>