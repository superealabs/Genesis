<template>
  <label v-if="label" :for="inputId" class="form-label">{{ label }}</label>
  <input
    v-bind="$attrs"
    :value="modelValue"
    :id="inputId"
    @input="onInput"
    class="form-control form-md mb-3"
  />
</template>

<script lang="ts">
import { computed, defineComponent, PropType } from "vue";

export default defineComponent({
  name: "GenesisInput",
  props: {
    label: { type: String, required: false },
    modelValue: {
      type: [String, Number, Date] as PropType<string | number | Date>,
      required: false,
    },
  },
  emits: ["update:modelValue"],
  setup(props, { emit }) {
    const inputId = computed(() => {
      return props.label
        ? "inpt-" + props.label.replace(/\s+/g, "-").toLowerCase()
        : "inpt-" + Math.random().toString(36).substring(2, 8);
    });

    const onInput = (e: Event) => {
      emit("update:modelValue", (e.target as HTMLInputElement).value);
    };

    return { inputId, onInput };
  },
});
</script>
