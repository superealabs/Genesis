<template>
  <table class="table align-middle table-rounded text-nowrap">
    <thead class="table-light">
      <tr>
        <th>Id</th>
        <th>Prenom</th>
        <th>Nom</th>
        <th>Email</th>
        <th>Date_embauche</th>
        <th>Salaire</th>
        <th>Departement_id</th>

        <th class="text-end">Actions</th>
      </tr>
    </thead>
    <tbody>
      <tr
        v-for="employes in employess"
        :key="employes.id"
        class="border-bottom"
      >
        <td>{{ employes.id }}</td>
        <td>{{ employes.prenom }}</td>
        <td>{{ employes.nom }}</td>
        <td>{{ employes.email }}</td>
        <td>{{ employes.date_embauche }}</td>
        <td>{{ employes.salaire }}</td>
        <td>{{ employes.departement_id }}</td>

        <td class="text-end">
          <div class="d-inline-flex gap-2">
            <GenesisButton icon="fas fa-eye" class="btn-light shadow-sm" />
            <GenesisButton icon="fas fa-edit" class="btn-light shadow-sm" />
            <GenesisButton icon="fas fa-trash" class="btn-light shadow-sm" />
          </div>
        </td>
      </tr>
    </tbody>
  </table>
</template>

<script lang="ts">
import GenesisButton from "@/core/button/GenesisButton.vue";
import { Employes } from "@/models/EmployesModel";
import { defineComponent, PropType } from "vue";

export default defineComponent({
  name: "EmployesList",
  components: { GenesisButton },
  props: {
    employess: {
      required: true,
      type: Array as PropType<Employes[]>,
    },
  },
  setup(props) {
    return {};
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
