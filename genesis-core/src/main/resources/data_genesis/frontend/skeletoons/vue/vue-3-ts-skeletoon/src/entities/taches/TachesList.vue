<template>
  <table class="table align-middle table-rounded text-nowrap">
    <thead class="table-light">
      <tr>
        <th>Id</th>
        <th>Titre</th>
        <th>Description</th>
        <th>Priorite</th>
        <th>Projet_id</th>
        <th>Assigne_a_id</th>

        <th class="text-end">Actions</th>
      </tr>
    </thead>
    <tbody>
      <tr v-for="taches in tachess" :key="taches.id" class="border-bottom">
        <td>{{ taches.id }}</td>
        <td>{{ taches.titre }}</td>
        <td>{{ taches.description }}</td>
        <td>{{ taches.priorite }}</td>
        <td>{{ taches.projet_id }}</td>
        <td>{{ taches.assigne_a_id }}</td>

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
import { Taches } from "@/models/TachesModel";
import { defineComponent, PropType } from "vue";

export default defineComponent({
  name: "TachesList",
  components: { GenesisButton },
  props: {
    tachess: {
      required: true,
      type: Array as PropType<Taches[]>,
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
