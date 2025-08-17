<template>
  <table class="table align-middle table-rounded text-nowrap">
    <thead class="table-light">
      <tr>
        <th>Id</th>
        <th>Nom projet</th>
        <th>Budget</th>
        <th>Date debut</th>
        <th>Date fin prevue</th>

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
      <ProjetRow
        v-else
        v-for="projet in data"
        :key="projet.id"
        :projet="projet"
      >
      </ProjetRow>
    </tbody>
  </table>
</template>

<script lang="ts">
import { defineComponent, PropType } from "vue";
import ProjetRow from "./ProjetRow.vue";
import { Projet } from "@/models/ProjetModel";

export default defineComponent({
  name: "ProjetList",
  components: { ProjetRow },
  props: {
    data: {
      required: true,
      type: Array as PropType<Projet[]>,
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
