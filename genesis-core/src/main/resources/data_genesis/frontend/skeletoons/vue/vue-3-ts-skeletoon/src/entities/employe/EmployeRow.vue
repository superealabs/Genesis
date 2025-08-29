<template>
  <tr class="border-bottom">
    <td>{{ employe.id }}</td>
    <td>{{ employe.prenom }}</td>
    <td>{{ employe.nom }}</td>
    <td>{{ employe.email }}</td>
    <td>{{ employe.dateEmbauche }}</td>
    <td>{{ employe.salaire }}</td>
    <td>{{ getSecondValue(employe.departementidDepartements) }}</td>

    <td class="text-end">
      <div class="d-inline-flex rounded-3 shadow overflow-hidden">
        <EntityRowActions @delete-row="deleteRow" @view-row="viewRow" @edit-row="updateRow" />
      </div>
    </td>
  </tr>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { Employe } from '@/models/EmployeModel'
import EntityRowActions from '@/core/table/EntityRowActions.vue'
import { useObjectUtils } from '@/composables/useObjectUtils'

export default defineComponent({
  name: 'EmployeRow',
  components: { EntityRowActions },
  props: {
    employe: {
      required: true,
      type: Object as PropType<Employe>,
    },
  },
  emits: ['request-delete', 'request-view', 'request-update'],
  setup(props, { emit }) {
    const deleteRow = () => {
      emit('request-delete', props.employe)
    }

    const viewRow = () => {
      emit('request-view', props.employe)
    }

    const updateRow = () => {
      emit('request-update', props.employe)
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
