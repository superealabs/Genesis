<template>
  <!-- ✅ La Core View est maintenant autonome. On lui passe juste les props de base et on écoute ses événements. -->
  <CoreFrontendSelectionView
    ref="coreViewRef"
    :showBackButton="showBackButton"
    @back="$emit('back')"
    @select="handleSelectWrapper"
  >
    <template #header-actions>
      <!-- Slot pour des actions spécifiques VSC si besoin à l'avenir -->
      <slot name="header-actions"></slot>
    </template>
  </CoreFrontendSelectionView>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { 
  FrontendSelectionView as CoreFrontendSelectionView,
  type FrontendFramework 
} from '@genesis-labs/web-core/features/frontend/manifest'; // Ajuste le chemin si ton alias est différent

// ═══ PROPS ═══
const props = withDefaults(defineProps<{
  showBackButton?: boolean;
}>(), {
  showBackButton: true
});

// ═══ EMITS ═══
const emit = defineEmits<{
  'back': [];
  'select': [result: { action: string; framework: FrontendFramework; event?: MouseEvent }];
}>();

// ═══ RÉFÉRENCE (Optionnel, utile si le parent a besoin d'appeler des méthodes exposées) ═══
const coreViewRef = ref<InstanceType<typeof CoreFrontendSelectionView> | null>(null);

// ═══ HANDLERS ═══
/**
 * Reçoit le résultat complet de la Core View et le relaie simplement au parent (GeneratorStepper/View).
 * La logique métier (appel au service, gestion du popup) est déjà gérée en interne par la Core View.
 */
function handleSelectWrapper(result: { action: string; framework: FrontendFramework; event?: MouseEvent }) {
  emit('select', result);
}
</script>