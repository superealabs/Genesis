<template>
  <!-- ✅ Plus besoin de passer toutes les props, la Core View est autonome -->
  <CoreFrameworksView
    ref="coreViewRef"
    :showBackButton="showBackButton"
    @back="$emit('back')"
    @select="handleSelectWrapper"
  >
    <template #header-actions>
      <!-- Slot spécifique VSC (ex: bouton Refresh) -->
    </template>
  </CoreFrameworksView>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { 
  FrameworksView as CoreFrameworksView, 
  type Framework 
} from '@genesis-labs/core/features/frameworks/manifest';

const props = withDefaults(defineProps<{
  showBackButton?: boolean;
}>(), {
  showBackButton: true
});

const emit = defineEmits<{
  'back': [];
  'select': [framework: Framework];
}>();

const coreViewRef = ref<InstanceType<typeof CoreFrameworksView> | null>(null);

// ✅ CORRECTION : On reçoit le résultat complet émis par la Core View
async function handleSelectWrapper(result: { action: string; framework: Framework; event?: MouseEvent }) {
  if (result.action === 'replace-needed') {
    coreViewRef.value?.triggerReplace(result.framework, result.event);
  } else {
    emit('select', result.framework);
  }
}
</script>