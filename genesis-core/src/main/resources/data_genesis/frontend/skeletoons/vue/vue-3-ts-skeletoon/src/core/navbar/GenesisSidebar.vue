<template>
  <nav
    id="sidebar"
    class="d-flex flex-column vh-100 position-sticky top-0"
    style="min-width: 250px"
  >
    <h3 class="px-3 nav-brand">
      <span class="text-dark">Genesis</span>
      <span class="text-primary">App</span>
    </h3>

    <ul class="nav flex-column flex-grow-1">
      <li
        class="nav-item mb-3"
        v-for="section in navigations"
        :key="section.sectionName"
      >
        <span class="nav-group-title nav-link text-muted text-uppercase">
          <i v-if="section.icon" :class="section.icon" class="me-2"></i>
          {{ section.sectionName }}
        </span>
        <GenesisNavList :items="section.navChilds" />
      </li>
    </ul>

    <UserNavProfile name="John Doe" />
  </nav>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { useNavigationsStore } from "@/store/useNavigationsStore";
import { storeToRefs } from "pinia";
import UserNavProfile from "../user/UserNavProfile.vue";
import GenesisNavList from "./GenesisNavList.vue";

export default defineComponent({
  name: "GenesisSidebarComponent",
  components: { UserNavProfile, GenesisNavList },
  setup() {
    const navigationsStore = useNavigationsStore();
    const { navigations } = storeToRefs(navigationsStore);
    return { navigations };
  },
});
</script>

<style scoped></style>
