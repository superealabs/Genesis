<template>
  <GenesisPopup :visible="visible" @close="close">
    <div class="d-flex flex-column text-center p-3">
      <!-- Icon -->
      <div>
        <i
          class="bi bi-exclamation-triangle text-danger"
          style="font-size: 4rem"
        ></i>
      </div>

      <!-- Message -->
      <div class="">
        <h5 class="text-danger">{{ title || "Error" }}</h5>
        <p class="mb-1">{{ message }}</p>
        <small v-if="subMessage" class="text-muted">{{ subMessage }}</small>
      </div>

      <!-- Button -->
      <div class="d-flex justify-content-center mt-4">
        <GenesisButton @click="close" class="btn-danger" label="Close" />
      </div>
    </div>
  </GenesisPopup>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import GenesisPopup from "@/core/popup/GenesisPopup.vue";
import GenesisButton from "@/core/button/GenesisButton.vue";

export default defineComponent({
  name: "AlertPopup",
  components: { GenesisPopup, GenesisButton },
  props: {
    visible: { type: Boolean, required: true },
    title: { type: String, default: "Error" },
    message: { type: String, default: "Alert Message" },
    subMessage: { type: String, default: "" },
  },
  emits: ["close"],
  setup(props, { emit }) {
    const close = () => emit("close", false);
    return { close };
  },
});
</script>
