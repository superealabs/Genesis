<template>
  <tr class="border-bottom">
    <td>{{ projet.id }}</td>
    <td>{{ projet.nomProjet }}</td>
    <td>{{ projet.budget }}</td>
    <td>{{ projet.dateDebut }}</td>
    <td>{{ projet.dateFinPrevue }}</td>

    <td class="text-end">
      <div class="d-inline-flex rounded-3 shadow-sm overflow-hidden">
        <EntityRowActions />
      </div>
    </td>
  </tr>
</template>

<script lang="ts">
import { defineComponent, PropType } from "vue";
import { Projet } from "@/models/ProjetModel";
import EntityRowActions from "@/core/table/EntityRowActions.vue";

export default defineComponent({
  name: "ProjetRow",
  components: { EntityRowActions },
  props: {
    projet: {
      required: true,
      type: Object as PropType<Projet>,
    },
  },
  setup(props) {
    return {};
  },
});
</script>

<style scoped></style>
