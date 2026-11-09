<template>
  <main class="flex flex-col h-full gap-8 overflow-hidden">
    <div class="flex items-center justify-between px-6 pt-6 pb-3 shrink-0 border-b border-bg-neutral-genesis">
      <!-- ✅ Titre avec bouton tutoriel à gauche -->
      <div class="flex items-center gap-2">
        <h3 class="text-xl font-semibold text-text">Configuration des Relations</h3>
        <GenesisButtonIcon
          variant="neutral"
          size="sm"
          @click="restartActiveTutorial"
          title="Lancer le tutoriel"
        >
          <IconHelp />
        </GenesisButtonIcon>
      </div>

      <GenesisSegmentedControl
        v-model="viewMode"
        :options="[
          { label: 'Simple', value: 'simple' },
          { label: 'Advanced', value: 'advanced' }
        ]"
        size="md"
      />
    </div>

    <RelationConfigSimpleView
      v-if="viewMode === 'simple'"
      ref="simpleViewRef"
    />
    <RelationConfigAdvancedView
      v-else
      ref="advancedViewRef"
    />
  </main>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import GenesisSegmentedControl from '@genesis-labs/web-core/core/components/ui/actions/GenesisSegmentedControl.vue';
import GenesisButtonIcon from '@genesis-labs/web-core/core/components/ui/actions/GenesisButtonIcon.vue';
import IconHelp from '@genesis-labs/web-core/core/components/ui/icons/IconHelp.vue';
import RelationConfigSimpleView from '../relation/RelationConfigSimpleView.vue';
import RelationConfigAdvancedView from '../relation/RelationConfigAdvancedView.vue';

const viewMode = ref<'simple' | 'advanced'>('simple');

// ✅ Refs sur les vues enfants pour déléguer le restart du tutoriel
const simpleViewRef = ref<InstanceType<typeof RelationConfigSimpleView> | null>(null);
const advancedViewRef = ref<InstanceType<typeof RelationConfigAdvancedView> | null>(null);

/**
 * Déclenche le tutoriel de la vue ACTIVE.
 * Chaque vue expose `restart()` via defineExpose — contrat commun.
 */
function restartActiveTutorial() {
  if (viewMode.value === 'simple') {
    simpleViewRef.value?.restart();
  } else {
    advancedViewRef.value?.restart();
  }
}
</script>