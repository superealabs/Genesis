<template>
  <GenesisButton icon="fa fa-plus">
    <span v-if="label">{{ label }}</span>
    <span v-else><slot /></span>
  </GenesisButton>
</template>

<script lang="ts">
import GenesisButton from "./GenesisButton.vue";
import { defineComponent } from "vue";

export default defineComponent({
  name: "GenesisAddButton",
  components: { GenesisButton },
  props: {
    label: {
      required: false,
      type: String,
    },
  },
});
</script>
