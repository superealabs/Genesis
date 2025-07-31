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
