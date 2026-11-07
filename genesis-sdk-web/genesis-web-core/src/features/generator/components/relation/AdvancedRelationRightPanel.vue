<template>
  <div class="flex flex-col gap-4 h-full overflow-y-auto custom-scrollbar pr-1">
    <!-- Partie 1 : Modèle de relation -->
    <div class="p-4 rounded-lg border border-bg-neutral-genesis space-y-3 shrink-0">
      <div class="flex items-center justify-between">
        <h4 class="text-sm font-semibold text-text">Modèle de relation</h4>
      </div>
      <div class="flex gap-4">
        <GenesisInput
          v-model="hasForm"
          type="boolean"
          label="Has Form"
          size="md"
          one-line
        />
        <GenesisInput
          v-model="isMandatory"
          type="boolean"
          label="Mandatory"
          size="md"
          one-line
        />
      </div>
      <div class="flex gap-2 justify-end">
        <GenesisButton
          variant="secondary"
          size="md"
          :disabled="selectedRelations.length === 0"
          @click="$emit('assign')"
        >
          Assign
        </GenesisButton>
        <GenesisButton
          variant="neutral"
          size="md"
          :disabled="relations.length === 0"
          @click="$emit('assignAll')"
        >
          Assign All
        </GenesisButton>
      </div>
    </div>

    <!-- Partie 2 : Liste cochable (recherche, filtres et Select All inclus) -->
    <div class="shrink-0">
      <RelationTableViewEditable
        :relations="relations"
        :selected-relations="selectedRelations"
        :max-height="600"
        @update:selected-relations="$emit('update:selectedRelations', $event)"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import type { RelationParameter } from '@genesis-labs/shared-types';
import GenesisInput from '@genesis-labs/web-core/core/components/ui/inputs/GenesisInput.vue';
import GenesisButton from '@genesis-labs/web-core/core/components/ui/actions/GenesisButton.vue';
import RelationTableViewEditable from '../relation/RelationTableViewEditable.vue';

const props = defineProps<{
  hasForm: boolean;
  isMandatory: boolean;
  relations: RelationParameter[];
  selectedRelations: string[];
}>();

const emit = defineEmits<{
  'update:hasForm': [value: boolean];
  'update:isMandatory': [value: boolean];
  'update:selectedRelations': [value: string[]];
  assign: [];
  assignAll: [];
}>();

const hasForm = computed({
  get: () => props.hasForm,
  set: (v) => emit('update:hasForm', v),
});
const isMandatory = computed({
  get: () => props.isMandatory,
  set: (v) => emit('update:isMandatory', v),
});
</script>