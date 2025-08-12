<template>
  <!-- Sidebar -->
  <nav
    id="sidebar"
    class="d-flex flex-column bg-light border-end py-4 px-2 vh-100 shadow-sm"
    style="width: 250px; min-width: 250px"
  >
    <h3 class="px-3 nav-brand">
      <span class="text-dark">Genesis </span
      ><span class="text-primary">App</span>
    </h3>
    <ul class="nav flex-column flex-grow-1">
      <li
        class="nav-item mb-3"
        v-for="navigation in navigations"
        :key="navigation.sectionName"
      >
        <span class="nav-group-title nav-link text-muted text-uppercase">
          {{ navigation.sectionName }}
        </span>
        <ul class="nav flex-column">
          <li
            class="nav-item"
            v-for="navRoute in navigation.navChilds"
            :key="navRoute.navTitle"
          >
            <router-link class="nav-link" :to="navRoute.navLink">{{
              navRoute.navTitle
            }}</router-link>
          </li>
        </ul>
      </li>
    </ul>
    <UserNavProfile name="John Doe" />
  </nav>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import UserNavProfile from "../user/UserNavProfile.vue";
import { useNavigationsStore } from "@/store/useNavigationsStore";
import { storeToRefs } from "pinia";

export default defineComponent({
  name: "GenesisSidebarComponent",
  components: { UserNavProfile },
  setup() {
    const navigationsStore = useNavigationsStore();
    const { navigations } = storeToRefs(navigationsStore);
    return { navigations };
  },
});
</script>

<style scoped>
.nav-group-title {
  font-weight: bold;
  font-size: 12px;
}
</style>
