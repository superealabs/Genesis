<template>
  <GenesisPopup
    title="Configurer les colonnes"
    :visible="visible"
    @close="emit('update:visible', false)"
  >
    <!-- Le sélecteur utilise l'état temporaire (pending) -->
    <ColumnVisibilitySelector
      :fields="fields"
      :visible-fields="pendingVisibleFields"
      @update:visible-fields="handleColumnToggle"
    />

    <!-- Footer du popup -->
    <template #footer>
      <button class="btn btn-ghost btn-sm" @click="emit('update:visible', false)">Fermer</button>
      <button class="btn btn-primary btn-sm" @click="handleApply">Appliquer</button>
    </template>
  </GenesisPopup>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import GenesisPopup from '@/components/popup/GenesisPopup.vue'
import ColumnVisibilitySelector from '@/components/table/ColumnVisibilitySelector.vue'
import type { EntitySearchField } from '@/models/EntityModel'

const props = defineProps<{
  visible: boolean
  fields: EntitySearchField[]
  visibleFields: string[]
}>()

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'apply', value: string[]): void
}>()

// État temporaire pour les modifications en cours dans le popup
const pendingVisibleFields = ref<string[]>([...props.visibleFields])

// Met à jour l'état temporaire quand l'utilisateur clique sur un toggle
const handleColumnToggle = (newFields: string[]) => {
  pendingVisibleFields.value = newFields
}

// Valide les changements et les envoie au parent
const handleApply = () => {
  emit('apply', pendingVisibleFields.value)
  emit('update:visible', false)
}

// Si le parent met à jour visibleFields (ex: au chargement initial), on synchronise l'état temporaire
watch(
  () => props.visibleFields,
  (newVal) => {
    pendingVisibleFields.value = [...newVal]
  },
  { deep: true },
)
</script>
