<template>
  <table class="table align-middle table-rounded text-nowrap">
    <thead class="table-light">
      <tr>
        <th>Id</th>
        <th>Nom departement</th>
        <th>Code departement</th>

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
      <DepartementRow
        v-else
        v-for="departement in data"
        :key="departement.id"
        :departement="departement"
      >
      </DepartementRow>
    </tbody>
  </table>
</template>

<script lang="ts">
import { defineComponent, PropType } from "vue";
import DepartementRow from "./DepartementRow.vue";
import { Departement } from "@/models/DepartementModel";

export default defineComponent({
  name: "DepartementList",
  components: { DepartementRow },
  props: {
    data: {
      required: true,
      type: Array as PropType<Departement[]>,
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
