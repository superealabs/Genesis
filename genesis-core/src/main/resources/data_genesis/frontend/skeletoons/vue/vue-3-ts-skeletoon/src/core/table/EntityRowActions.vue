<template>
  <GenesisButton
    icon="bi bi-eye"
    class="border-0 btn-secondary rounded-0"
    title="Voir Details"
  />
  <GenesisButton
    icon="bi bi-pen"
    class="border-0 btn-secondary rounded-0"
    title="Modifier"
  />
  <GenesisButton
    icon="bi bi-trash"
    class="border-0 btn-secondary rounded-0"
    title="Supprimer"
  />
</template>

<script lang="ts">
import GenesisButton from "@/core/button/GenesisButton.vue";
import { defineComponent } from "vue";

export default defineComponent({
  name: "DepartementRow",
  components: { GenesisButton },
  emits: ["delete-row", "view-row", "edit-row"],
  setup(props, { emit }) {
    return {};
  },
});
</script>

<style scoped></style>
