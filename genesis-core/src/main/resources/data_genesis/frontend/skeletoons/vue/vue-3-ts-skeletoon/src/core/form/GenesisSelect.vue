<template>
  <label
    v-if="label"
    class="text-nowrap me-1"
    :class="{ 'form-label': !rowInput }"
    :for="inputId"
    >{{ label }}</label
  >
  <select
    v-bind="$attrs"
    :id="inputId"
    class="form-select"
    :value="modelValue ?? ''"
    :disabled="loading"
    @change="onChange"
  >
    <option value="">
      {{ placeholder ?? `-- Select an option --` }}
    </option>
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
    placeholder: { type: String, required: false },
    modelValue: {
      type: [String, Number, null] as PropType<string | number | null>,
      required: false,
      default: null,
    },
    options: {
      type: Array as PropType<SelectOption[]>,
      required: true,
    },
    loading: { type: Boolean, default: false },
    rowInput: { type: Boolean, default: false },
  },
  emits: ["update:modelValue"],
  setup(props, { emit }) {
    const inputId = computed(() => {
      return props.label
        ? "select-" + props.label.replace(/\s+/g, "-").toLowerCase()
        : "select-" + Math.random().toString(36).substring(2, 8);
    });

    const onChange = (e: Event) => {
      const value = (e.target as HTMLSelectElement).value;
      emit("update:modelValue", value === "" ? null : value);
    };

    return { inputId, onChange };
  },
});
</script>
