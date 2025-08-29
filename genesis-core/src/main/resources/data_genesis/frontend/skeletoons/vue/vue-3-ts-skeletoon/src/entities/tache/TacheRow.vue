<template>
  <tr class="hover:bg-gray-50">
    <td class="px-3 py-2">{{ tache.id }}</td>
    <td class="px-3 py-2">{{ tache.titre }}</td>
    <td class="px-3 py-2">{{ tache.description }}</td>
    <td class="px-3 py-2">{{ tache.priorite }}</td>
    <td class="px-3 py-2">{{ tache.projetidProjets?.getReferenceValue() }}</td>
    <td class="px-3 py-2">{{ tache.assigneaidEmployes?.getReferenceValue() }}</td>

    <td class="px-3 py-2 text-right">
      <div class="inline-flex rounded-md shadow overflow-hidden">
        <EntityRowActions @delete-row="deleteRow" @view-row="viewRow" @edit-row="updateRow" />
      </div>
    </td>
  </tr>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import type { PropType } from 'vue'
import { Tache } from '@/models/TacheModel'
import EntityRowActions from '@/core/table/EntityRowActions.vue'
import { useObjectUtils } from '@/composables/useObjectUtils'

export default defineComponent({
  name: 'TacheRow',
  components: { EntityRowActions },
  props: {
    tache: {
      required: true,
      type: Object as PropType<Tache>,
    },
  },
  emits: ['request-delete', 'request-view', 'request-update'],
  setup(props, { emit }) {
    const deleteRow = () => {
      emit('request-delete', props.tache)
    }

    const viewRow = () => {
      emit('request-view', props.tache)
    }

    const updateRow = () => {
      emit('request-update', props.tache)
    }

    const { getSecondValue } = useObjectUtils()

    return {
      deleteRow,
      viewRow,
      updateRow,
      getSecondValue,
    }
  },
})
</script>

<style scoped></style>
