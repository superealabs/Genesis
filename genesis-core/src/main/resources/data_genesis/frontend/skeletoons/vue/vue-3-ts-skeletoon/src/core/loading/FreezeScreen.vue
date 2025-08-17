<template>
  <GenesisOverlay :visible="freezeState" :text="freezeMessage"></GenesisOverlay>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import GenesisOverlay from "./GenesisOverlay.vue";
import { useFreezeScreen } from "@/store/useFreezeScreen";

export default defineComponent({
  name: "FreezeScreen",
  components: { GenesisOverlay },
  setup() {
    const store = useFreezeScreen();
    store.debug("FreezeScreen setup");

    return {
      freezeState: store.freezeState,
      freezeMessage: store.freezeMessage,
    };
  },
});
</script>
