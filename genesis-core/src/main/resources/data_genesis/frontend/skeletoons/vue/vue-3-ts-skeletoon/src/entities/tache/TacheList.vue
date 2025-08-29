<template>
  <div class="overflow-x-auto rounded-box border border-base-content/5 bg-base-100">
    <table class="table w-full border-gray-200 rounded-md text-left">
      <thead class="bg-secondary">
        <tr>
          <th class="px-3 py-2 font-bold">Id</th>
          <th class="px-3 py-2 font-bold">Titre</th>
          <th class="px-3 py-2 font-bold">Description</th>
          <th class="px-3 py-2 font-bold">Priorité</th>
          <th class="px-3 py-2 font-bold">Projet</th>
          <th class="px-3 py-2 font-bold">Assigné à</th>
          <th class="px-3 py-2 font-bold text-right">Actions</th>
        </tr>
      </thead>
      <tbody>
        <!-- Empty message -->
        <tr v-if="!loading && message">
          <td colspan="7" class="text-center py-4 text-gray-500">
            {{ message }}
          </td>
        </tr>

        <!-- Task rows -->
        <TacheRow
          v-else
          v-for="tache in data"
          :key="tache.id"
          :tache="tache"
          @request-delete="openDeletePopup"
          @request-view="viewTache"
          @request-update="goToUpdateFormView"
        />
      </tbody>
    </table>

    <!-- Delete confirmation popup -->
    <DeleteConfirmationPopup
      :visible="deletePopup"
      message="Are you sure you want to delete this Tache?"
      @confirm="confirmDelete"
      @cancel="closePopup"
    />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import type { PropType } from 'vue'
import TacheRow from './TacheRow.vue'
import DeleteConfirmationPopup from '@/core/popup/DeleteConfirmationPopup.vue'
import { usePopup } from '@/composables/usePopup'
import { useTaches } from '@/composables/useTaches'
import type { Tache } from '@/models/TacheModel'

// Props
defineProps({
  data: {
    type: Array as PropType<Tache[]>,
    required: true,
  },
  message: String,
  loading: {
    type: Boolean,
    default: false,
  },
})

// Emits
const emit = defineEmits<{
  (e: 'request:refresh'): void
}>()

// Reactive state
const selectedEntity = ref<Partial<Tache>>({})

// Composables
const { visible: deletePopup, closePopup } = usePopup(false)
const { deleteTache, viewTache, goToUpdateFormView } = useTaches()

// Methods
function openDeletePopup(entity: Tache) {
  if (!entity) return
  selectedEntity.value = entity
  deletePopup.value = true
}

async function confirmDelete() {
  const { success, error } = await deleteTache(selectedEntity.value)
  if (success) {
    deletePopup.value = false
    emit('request:refresh')
  } else {
    console.error(error)
  }
}
</script>

<style scoped></style>
