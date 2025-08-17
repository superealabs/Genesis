<template>
  <GenesisButton
    icon="bi bi-eye"
    class="border-0 btn-secondary rounded-0"
    title="Voir Details"
    @click="viewRow"
  />
  <GenesisButton
    icon="bi bi-pen"
    class="border-0 btn-secondary rounded-0"
    title="Modifier"
    @click="editRow"
  />
  <GenesisButton
    icon="bi bi-trash"
    class="border-0 btn-secondary rounded-0"
    title="Supprimer"
    @click="deleteRow"
  />
</template>

<script lang="ts">
import GenesisButton from "@/core/button/GenesisButton.vue";
import { defineComponent } from "vue";

export default defineComponent({
  name: "EntityRowActions",
  components: { GenesisButton },
  emits: ["delete-row", "view-row", "edit-row"],
  setup(props, { emit }) {
    const deleteRow = () => {
      emit("delete-row");
    };

    const viewRow = () => {
      emit("view-row");
    };

    const editRow = () => {
      emit("edit-row");
    };
    return {
      deleteRow,
      viewRow,
      editRow,
    };
  },
});
</script>

<style scoped></style>
