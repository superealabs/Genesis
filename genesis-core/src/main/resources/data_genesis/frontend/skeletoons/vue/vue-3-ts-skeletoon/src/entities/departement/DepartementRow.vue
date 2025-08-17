<template>
  <tr class="border-bottom">
    <td>{{ departement.id }}</td>
    <td>{{ departement.nomDepartement }}</td>
    <td>{{ departement.codeDepartement }}</td>

    <td class="text-end">
      <div class="d-inline-flex rounded-3 shadow-sm overflow-hidden">
        <EntityRowActions />
      </div>
    </td>
  </tr>
</template>

<script lang="ts">
import { defineComponent, PropType } from "vue";
import { Departement } from "@/models/DepartementModel";
import EntityRowActions from "@/core/table/EntityRowActions.vue";

export default defineComponent({
  name: "DepartementRow",
  components: { EntityRowActions },
  props: {
    departement: {
      required: true,
      type: Object as PropType<Departement>,
    },
  },
  setup(props) {
    return {};
  },
});
</script>

<style scoped></style>
