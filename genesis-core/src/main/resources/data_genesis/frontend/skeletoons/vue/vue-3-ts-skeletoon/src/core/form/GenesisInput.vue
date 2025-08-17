<template>
  <label
    v-if="label"
    :for="inputFormId"
    :class="{ 'form-label': !rowInput }"
    class="text-nowrap me-1"
    >{{ label }}</label
  >
  <input
    v-bind="$attrs"
    :value="modelValue"
    :id="inputFormId"
    class="form-control"
    @input="onInput"
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
    inputId: {
      type: String,
      required: false,
    },
    rowInput: { type: Boolean, default: false },
  },
  emits: ["update:modelValue"],
  setup(props, { emit }) {
    const inputFormId = computed(() => {
      if (props.inputId) {
        return props.inputId;
      }
      return props.label
        ? "inpt-" + props.label.replace(/\s+/g, "-").toLowerCase()
        : "inpt-" + Math.random().toString(36).substring(2, 8);
    });

    const onInput = (e: Event) => {
      emit("update:modelValue", (e.target as HTMLInputElement).value);
    };

    return { inputFormId, onInput };
  },
});
</script>
