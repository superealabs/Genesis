<template>
  <div
    v-if="visible"
    class="overlay position-absolute top-0 start-0 w-100 h-100 d-flex justify-content-center align-items-center"
  >
    <!-- Custom overlay content -->
    <slot name="overlay">
      <!-- Default: Spinner + Loading text -->
      <div class="text-center">
        <div class="spinner-border text-dark" role="status"></div>
        <div class="mt-2">{{ text }}</div>
      </div>
    </slot>
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";

export default defineComponent({
  name: "GenesisOverlay",
  props: {
    visible: {
      type: Boolean,
      default: false,
    },
    text: {
      type: String,
      default: "Loading...",
    },
  },
});
</script>

<style scoped>
.overlay {
  background-color: rgba(255, 255, 255, 0.7);
  z-index: 100;
}
</style>
