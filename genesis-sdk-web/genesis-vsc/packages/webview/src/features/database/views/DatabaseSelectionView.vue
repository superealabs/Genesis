<template>
  <CoreDatabaseSelection
    ref="coreViewRef"
    :showBackButton="showBackButton"
    @back="$emit('back')"
    @select="handleSelectWrapper" 
  >
    <template #header-actions>
      <slot name="header-actions"></slot>
    </template>
  </CoreDatabaseSelection>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { 
  DatabaseView as CoreDatabaseSelection, 
  type DatabaseEngineDto 
} from '@genesis-labs/web-core/features/database/manifest';

const props = withDefaults(defineProps<{
  showBackButton?: boolean;
}>(), {
  showBackButton: true
});

const emit = defineEmits<{
  'back': [];
  // CORRECTION : Le type doit correspondre exactement à ce que la Core View émet
  'select': [result: { action: string; engine: DatabaseEngineDto; event?: MouseEvent }];
}>();

const coreViewRef = ref<InstanceType<typeof CoreDatabaseSelection> | null>(null);

// CORRECTION : On reçoit l'objet résultat complet, comme pour les frameworks
function handleSelectWrapper(result: { action: string; engine: DatabaseEngineDto; event?: MouseEvent }) {
  // On émet le résultat complet vers le parent (GeneratorStepper)
  emit('select', result);
}
</script>