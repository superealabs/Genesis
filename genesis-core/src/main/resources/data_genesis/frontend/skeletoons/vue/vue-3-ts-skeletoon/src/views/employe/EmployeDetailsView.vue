<template>
  <div class="container-fluid">
    <div class="mb-4 d-flex justify-content-between align-items-center">
      <div>
        <h3 class="text-dark">Employe / <span class="text-muted">Details</span></h3>
      </div>
      <div>
        <GenesisButton
          icon="bi bi-arrow-left me-2"
          title="Go back to list view"
          @click="goToListView"
        >
          <span>Back to list</span>
        </GenesisButton>
      </div>
    </div>
    <EmployeDetails v-if="entity" :employe="entity" />
    <p v-else>Chargement...</p>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import EmployeDetails from '@/entities/employe/EmployeDetails.vue'
import { useEmployes } from '@/composables/useEmployes'
import { Employe } from '@/models/EmployeModel'
import GenesisButton from '@/core/button/GenesisButton.vue'

export default defineComponent({
  name: 'EmployeDetailsView',
  components: {
    EmployeDetails,
    GenesisButton,
  },
  setup() {
    const route = useRoute()
    const { getEmployeById, goToListView } = useEmployes()
    const entity = ref<Employe | null>(null)

    onMounted(async () => {
      const result = await getEmployeById(Number(route.params.id))
      entity.value = result.data || null
    })

    return { entity, route, goToListView }
  },
})
</script>

<style scoped></style>
