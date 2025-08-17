<template>
  <button
    v-bind="$attrs"
    class="btn d-flex align-items-center"
    :class="btnClassNames"
    @click="onClick"
  >
    <i v-if="icon" :class="icon" />
    <span v-if="label">{{ label }}</span>
    <span v-else><slot></slot></span>
  </button>
</template>

<script lang="ts">
import { computed, defineComponent, useAttrs } from "vue";

export default defineComponent({
  name: "GenesisButton",
  props: {
    icon: { type: String, required: false }, // icône optionnelle
    label: { type: String, required: false },
  },
  emits: {
    click: (event: MouseEvent) => event instanceof MouseEvent,
  },
  setup(props, { emit }) {
    const onClick = (event: MouseEvent) => {
      emit("click", event);
    };
    const attrs = useAttrs();
    const btnClassNames = computed(() => {
      return attrs.class ? "" : "btn-primary text-white";
    });

    return {
      onClick,
      btnClassNames,
    };
  },
});
</script>
