import GenesisButton from "./GenesisButton.vue";
import { defineComponent } from "vue";

export default defineComponent({
  name: "GenesisAddButton",
  components: { GenesisButton },
  props: {
    label: {
      required: false,
      type: String,
    },
  },
});
