<template>
  <div class="wrapper row">
    <FreezeScreen></FreezeScreen>
    <div class="p-0 col-md-auto position-relative bg-light shadow-sm">
      <GenesisSidebar class="py-4 px-2" />
    </div>
    <!-- Main content -->
    <div class="bg-light col-md py-4">
      <router-view />
    </div>
  </div>
</template>

<script lang="ts">
import GenesisSidebar from "./core/navbar/GenesisSidebar.vue";
import FreezeScreen from "./core/loading/FreezeScreen.vue";
import { defineComponent } from "vue";
export default defineComponent({
  name: "App",
  components: { FreezeScreen, GenesisSidebar },
  setup() {
    return {};
  },
});
</script>

<style scoped>
.wrapper.row {
  flex-wrap: nowrap;
  margin: 0;
}
</style>
