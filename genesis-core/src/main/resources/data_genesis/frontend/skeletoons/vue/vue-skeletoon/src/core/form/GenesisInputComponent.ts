import { computed, defineComponent } from "vue";

export default defineComponent({
  props: {
    label: { type: String, required: false },
    modelValue: { type: [String, Number, Date], required: false },
  },
  setup(props) {
    const inputId = computed(() => {
      return props.label ? "inpt" + props.label : "";
    });

    return { inputId };
  },
});
