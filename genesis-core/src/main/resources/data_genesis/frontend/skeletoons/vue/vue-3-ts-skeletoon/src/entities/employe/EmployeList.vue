<template>
  <table class="table align-middle table-rounded text-nowrap">
    <thead class="table-light">
      <tr>
        <th>Id</th>
        <th>Prenom</th>
        <th>Nom</th>
        <th>Email</th>
        <th>Date embauche</th>
        <th>Salaire</th>
        <th>Departementid departements</th>

        <th scope="col" class="text-end">Actions</th>
      </tr>
    </thead>
    <tbody>
      <tr v-if="loading" class="py-4 text-center">
        <td colspan="10" class="text-muted text-center py-4">
          <span>Loading...</span>
        </td>
      </tr>

      <tr v-else-if="message">
        <td colspan="10" class="text-muted text-center py-4">
          {{ message }}
        </td>
      </tr>
      <EmployeRow
        v-else
        v-for="employe in data"
        :key="employe.id"
        :employe="employe"
      >
      </EmployeRow>
    </tbody>
  </table>
</template>

<script lang="ts">
import { defineComponent, PropType } from "vue";
import EmployeRow from "./EmployeRow.vue";
import { Employe } from "@/models/EmployeModel";

export default defineComponent({
  name: "EmployeList",
  components: { EmployeRow },
  props: {
    data: {
      required: true,
      type: Array as PropType<Employe[]>,
    },
    message: {
      required: false,
      type: String,
    },
    loading: {
      type: Boolean,
      required: false,
      defauld: false,
    },
  },
  setup() {
    return {};
  },
});
</script>

<style scoped></style>
