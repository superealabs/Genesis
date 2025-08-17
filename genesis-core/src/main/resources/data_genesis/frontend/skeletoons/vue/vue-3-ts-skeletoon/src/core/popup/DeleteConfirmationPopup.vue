<template>
  <GenesisPopup :visible="visible" @close="cancel">
    <div class="d-flex flex-column text-center">
      <div>
        <i class="bi bi-shield-exclamation" style="font-size: 4rem"></i>
      </div>
      <div>
        <p>{{ message }}</p>
        <p>{{ subMessage }}</p>
      </div>
      <div class="d-flex justify-content-center gap-2">
        <GenesisButton
          @click="confirm"
          class="btn-danger"
          label="Confirm Delete"
        />
        <GenesisButton @click="cancel" class="btn-secondary" label="Cancel" />
      </div>
    </div>
  </GenesisPopup>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import GenesisPopup from "@/core/popup/GenesisPopup.vue";
import GenesisButton from "@/core/button/GenesisButton.vue";

export default defineComponent({
  name: "DeleteConfirmationPopup",
  components: { GenesisPopup, GenesisButton },
  props: {
    visible: { type: Boolean, required: true },
    message: { type: String, required: true },
    subMessage: { type: String, default: "This action is irreversible." },
  },
  emits: ["confirm", "cancel"],
  setup(props, { emit }) {
    return {
      confirm: () => emit("confirm"),
      cancel: () => emit("cancel"),
    };
  },
});
</script>
