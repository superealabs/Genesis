<template>
  <tr class="border-bottom">
    <td>{{ projet.id }}</td>
    <td>{{ projet.nomProjet }}</td>
    <td>{{ projet.budget }}</td>
    <td>{{ projet.dateDebut }}</td>
    <td>{{ projet.dateFinPrevue }}</td>

    <td class="text-end">
      <div class="d-inline-flex rounded-3 shadow overflow-hidden">
        <EntityRowActions @delete-row="deleteRow" @view-row="viewRow" @edit-row="updateRow" />
      </div>
    </td>
  </tr>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { Projet } from '@/models/ProjetModel'
import EntityRowActions from '@/core/table/EntityRowActions.vue'
import { useObjectUtils } from '@/composables/useObjectUtils'

export default defineComponent({
  name: 'ProjetRow',
  components: { EntityRowActions },
  props: {
    projet: {
      required: true,
      type: Object as PropType<Projet>,
    },
  },
  emits: ['request-delete', 'request-view', 'request-update'],
  setup(props, { emit }) {
    const deleteRow = () => {
      emit('request-delete', props.projet)
    }

    const viewRow = () => {
      emit('request-view', props.projet)
    }

    const updateRow = () => {
      emit('request-update', props.projet)
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
