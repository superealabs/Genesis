<template>
  <label v-if="label" class="text-nowrap" :for="inputId">{{ label }}</label>
  <select
    v-bind="$attrs"
    :id="inputId"
    class="form-select"
    :value="modelValue"
    @change="onChange"
  >
    <option v-for="option in options" :key="option.value" :value="option.value">
      {{ option.label }}
    </option>
  </select>
</template>

<script lang="ts">
import { defineComponent, computed, PropType } from "vue";
import { SelectOption } from "../../models/SelectOption";

export default defineComponent({
  name: "GenesisSelect",
  props: {
    label: { type: String, required: false },
    modelValue: {
      type: [String, Number] as PropType<string | number>,
      required: false,
    },
    options: {
      type: Array as PropType<SelectOption[]>,
      required: true,
    },
  },
  emits: ["update:modelValue"],
  setup(props, { emit }) {
    const inputId = computed(() => {
      return props.label
        ? "select-" + props.label.replace(/\s+/g, "-").toLowerCase()
        : "select-" + Math.random().toString(36).substring(2, 8);
    });

    const onChange = (e: Event) => {
      emit("update:modelValue", (e.target as HTMLSelectElement).value);
    };

    return { inputId, onChange };
  },
});
</script>
