<template>
  <main class="flex flex-col h-full overflow-hidden">
    <div class="flex items-center justify-between px-6 pt-6 pb-3 shrink-0 border-b border-bg-neutral-genesis">
      <h3 class="text-xl font-semibold text-text">Configuration des Relations</h3>
      <GenesisSegmentedControl
        v-model="viewMode"
        :options="[
          { label: 'Simple', value: 'simple' },
          { label: 'Advanced', value: 'advanced' }
        ]"
        size="md"
      />
    </div>

    <RelationConfigSimpleView v-if="viewMode === 'simple'" />
    <RelationConfigAdvancedView v-else />
  </main>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import GenesisSegmentedControl from '@genesis-labs/web-core/core/components/ui/actions/GenesisSegmentedControl.vue';
import RelationConfigSimpleView from '../relation/RelationConfigSimpleView.vue';
import RelationConfigAdvancedView from '../relation/RelationConfigAdvancedView.vue';

const viewMode = ref<'simple' | 'advanced'>('simple');
</script>