<template>
  <table class="table align-middle table-rounded text-nowrap">
    <thead class="table-light">
      <tr>
        <th>Id</th>
        <th>Nom_projet</th>
        <th>Budget</th>
        <th>Date_debut</th>
        <th>Date_fin_prevue</th>

        <th class="text-end">Actions</th>
      </tr>
    </thead>
    <tbody>
      <tr v-for="projets in projetss" :key="projets.id" class="border-bottom">
        <td>{{ projets.id }}</td>
        <td>{{ projets.nom_projet }}</td>
        <td>{{ projets.budget }}</td>
        <td>{{ projets.date_debut }}</td>
        <td>{{ projets.date_fin_prevue }}</td>

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
import { Projets } from "@/models/ProjetsModel";
import { defineComponent, PropType } from "vue";

export default defineComponent({
  name: "ProjetsList",
  components: { GenesisButton },
  props: {
    projetss: {
      required: true,
      type: Array as PropType<Projets[]>,
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
