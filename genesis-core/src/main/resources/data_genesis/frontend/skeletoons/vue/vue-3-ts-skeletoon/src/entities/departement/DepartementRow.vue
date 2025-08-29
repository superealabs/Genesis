<template>
  <tr class="border-bottom">
    <td>{{ departement.id }}</td>
    <td>{{ departement.nomDepartement }}</td>
    <td>{{ departement.codeDepartement }}</td>

    <td class="text-end">
      <div class="d-inline-flex rounded-3 shadow overflow-hidden">
        <EntityRowActions @delete-row="deleteRow" @view-row="viewRow" @edit-row="updateRow" />
      </div>
    </td>
  </tr>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { Departement } from '@/models/DepartementModel'
import EntityRowActions from '@/core/table/EntityRowActions.vue'
import { useObjectUtils } from '@/composables/useObjectUtils'

export default defineComponent({
  name: 'DepartementRow',
  components: { EntityRowActions },
  props: {
    departement: {
      required: true,
      type: Object as PropType<Departement>,
    },
  },
  emits: ['request-delete', 'request-view', 'request-update'],
  setup(props, { emit }) {
    const deleteRow = () => {
      emit('request-delete', props.departement)
    }

    const viewRow = () => {
      emit('request-view', props.departement)
    }

    const updateRow = () => {
      emit('request-update', props.departement)
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
