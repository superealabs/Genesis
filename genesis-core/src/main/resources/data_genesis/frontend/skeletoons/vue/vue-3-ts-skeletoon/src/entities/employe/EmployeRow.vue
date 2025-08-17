<template>
  <tr class="border-bottom">
    <td>{{ employe.id }}</td>
    <td>{{ employe.prenom }}</td>
    <td>{{ employe.nom }}</td>
    <td>{{ employe.email }}</td>
    <td>{{ employe.dateEmbauche }}</td>
    <td>{{ employe.salaire }}</td>
    <td>{{ employe.departementidDepartements?.id }}</td>

    <td class="text-end">
      <div class="d-inline-flex rounded-3 shadow-sm overflow-hidden">
        <EntityRowActions />
      </div>
    </td>
  </tr>
</template>

<script lang="ts">
import { defineComponent, PropType } from "vue";
import { Employe } from "@/models/EmployeModel";
import EntityRowActions from "@/core/table/EntityRowActions.vue";

export default defineComponent({
  name: "EmployeRow",
  components: { EntityRowActions },
  props: {
    employe: {
      required: true,
      type: Object as PropType<Employe>,
    },
  },
  setup(props) {
    return {};
  },
});
</script>

<style scoped></style>
