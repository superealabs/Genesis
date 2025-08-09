<template>
  <div class="d-flex align-items-center justify-content-between gap-2">
    <div class="row align-items-center g-2">
      <div class="col">
        <div
          class="avatar d-flex align-items-center justify-content-center rounded-circle bg-secondary text-muted fw-bold"
          style="width: 40px; height: 40px"
        >
          <i class="bi bi-person"></i>
        </div>
      </div>
      <div class="col-auto">
        <p class="mb-0 fw-medium text-dark">{{ name }}</p>
      </div>
    </div>
    <div>
      <GenesisButton
        icon="bi bi-box-arrow-right"
        class=""
        style="font-size: large"
      ></GenesisButton>
    </div>
  </div>
</template>
<script lang="ts">
import { computed, defineComponent } from "vue";
import GenesisButton from "../button/GenesisButton.vue";

export default defineComponent({
  name: "UserNavProfile",
  props: {
    name: {
      required: true,
      type: String,
    },
  },
  components: { GenesisButton },
  setup(props) {
    const initials = computed(() => {
      if (!props.name) return "";
      return props.name
        .split(" ")
        .map((n) => n[0])
        .join("")
        .toUpperCase();
    });
    return { initials };
  },
});
</script>
