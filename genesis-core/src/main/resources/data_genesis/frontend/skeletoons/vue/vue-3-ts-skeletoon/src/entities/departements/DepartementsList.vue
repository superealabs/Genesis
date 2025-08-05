<template>
  <table class="table align-middle table-rounded text-nowrap">
    <thead class="table-light">
      <tr>
        <th>Id</th>
        <th>Nom_departement</th>
        <th>Code_departement</th>

        <th class="text-end">Actions</th>
      </tr>
    </thead>
    <tbody>
      <tr v-if="loading" class="py-4 text-center">
        <td colspan="10" class="text-muted text-center py-4">
          <span>Loading...</span>
        </td>
      </tr>

      <tr v-else-if="error">
        <td colspan="10" class="text-muted text-center py-4">
          {{ error }}
        </td>
      </tr>
      <DepartementRow
        v-else
        v-for="departements in departementss"
        :key="departements.id"
        :departement="departements"
      >
      </DepartementRow>
    </tbody>
  </table>
</template>

<script lang="ts">
import { useDepartements } from "@/composables/useDepartements";
import { defineComponent } from "vue";
import DepartementRow from "./DepartementRow.vue";

export default defineComponent({
  name: "DepartementsList",
  components: { DepartementRow },
  setup() {
    const { departements: departementss, error, loading } = useDepartements();
    return {
      departementss,
      error,
      loading,
    };
  },
});
</script>

<style scoped>
.table thead th {
  background-color: #537cc226;
  color: #4e4e4e;
  font-weight: 600;
  vertical-align: middle;
  border-bottom: 2px solid #dee2e6;
}

.table td,
.table th {
  vertical-align: middle;
  padding: 0.75rem;
}

.btn-light {
  border-radius: 0.5rem;
  transition: background 0.2s ease-in-out;
}

.btn-light:hover {
  background-color: #f1f3f5;
}
</style>
