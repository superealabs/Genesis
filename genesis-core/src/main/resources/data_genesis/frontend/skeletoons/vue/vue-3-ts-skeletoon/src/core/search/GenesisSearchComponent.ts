import { defineComponent } from "vue";
import { useSearch } from "@/shared/composables/useSearch"; // adjust path
import GenesisInput from "../form/GenesisInput.vue";
import GenesisButton from "../button/GenesisButton.vue";

export default defineComponent({
  name: "GenesisSearch",
  props: {
    model: {
      required: true,
      type: Object,
    },
  },
  components: {
    GenesisInput,
    GenesisButton,
  },
  emits: ["search"],
  setup(props, { emit }) {
    const { filter, filterTypes, emitSearch, resetFilters } = useSearch(
      props.model,
      emit
    );

    return {
      filter,
      filterTypes,
      emitSearch,
      resetFilters,
    };
  },
});
