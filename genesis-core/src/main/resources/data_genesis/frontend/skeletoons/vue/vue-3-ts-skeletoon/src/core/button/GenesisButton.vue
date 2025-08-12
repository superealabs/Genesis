<template>
  <button v-bind="$attrs" :class="btnClassNames" @click="onClick">
    <i v-if="icon" :class="icon" />
    <span v-if="label">{{ label }}</span>
    <span v-else><slot></slot></span>
  </button>
</template>

<script lang="ts">
import { computed, defineComponent } from "vue";

export default defineComponent({
  name: "GenesisButton",
  props: {
    icon: { type: String, required: false }, // icône optionnelle
    label: { type: String, required: false },
    class: { type: String, required: false },
  },
  emits: {
    click: (event: MouseEvent) => event instanceof MouseEvent,
  },
  setup(props, { emit }) {
    const onClick = (event: MouseEvent) => {
      emit("click", event);
    };

    const btnClassNames = computed(() => {
      const base = "btn d-flex align-items-center ";
      return base + (props.class ?? "btn-primary text-white");
    });

    return {
      onClick,
      btnClassNames,
    };
  },
});
</script>
