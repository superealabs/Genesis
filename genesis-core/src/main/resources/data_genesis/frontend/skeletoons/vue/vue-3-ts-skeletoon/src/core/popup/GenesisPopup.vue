<template>
  <!-- Backdrop -->
  <div v-if="visible" class="modal-backdrop fade show" @click="close"></div>
  <div
    class="modal fade overflow-hidden"
    data-bs-backdrop="static"
    data-bs-keyboard="false"
    tabindex="-1"
    aria-labelledby="staticBackdropLabel"
    :class="{ show: visible, 'd-block': visible }"
    aria-hidden="true"
    @click.self="close"
  >
    <div class="modal-dialog d-flex h-75 align-items-center">
      <div class="modal-content">
        <div class="modal-header border-0">
          <h5 class="modal-title">{{ title }}</h5>
          <button
            type="button"
            class="btn btn-close"
            data-bs-dismiss="modal"
            aria-label="Close"
            @click="close"
          ></button>
        </div>
        <div class="modal-body">
          <slot />
        </div>
        <div class="modal-footer border-0">
          <button class="btn"></button>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";

export default defineComponent({
  name: "GenesisPopup",
  props: {
    title: { type: String, default: "" },
    visible: { type: Boolean, required: true },
  },
  emits: ["close"],
  setup(props, { emit }) {
    const close = () => {
      emit("close", false);
    };

    return {
      close,
    };
  },
});
</script>

<style scoped>
/* Smooth fade-in/out animation */
.modal.fade {
  transition: opacity 0.15s linear;
}
</style>
