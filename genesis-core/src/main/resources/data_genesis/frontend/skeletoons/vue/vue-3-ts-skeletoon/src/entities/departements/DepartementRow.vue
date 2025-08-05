<template>
  <tr class="border-bottom">
    <td>{{ departement.id }}</td>
    <td>{{ departement.nomDepartement }}</td>
    <td>{{ departement.codeDepartement }}</td>

    <td class="text-end">
      <div class="d-inline-flex gap-2">
        <GenesisButton icon="fas fa-eye" class="btn-light shadow-sm" />
        <GenesisButton icon="fas fa-edit" class="btn-light shadow-sm" />
        <GenesisButton icon="fas fa-trash" class="btn-light shadow-sm" />
      </div>
    </td>
  </tr>
</template>

<script lang="ts">
import GenesisButton from "@/core/button/GenesisButton.vue";
import { defineComponent } from "vue";
import { Departements } from "@/models/DepartementsModel";

export default defineComponent({
  name: "DepartementsRow",
  components: { GenesisButton },
  props: {
    departement: {
      required: true,
      type: Departements,
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
