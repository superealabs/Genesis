<template>
  <ul class="nav flex-column">
    <li class="nav-item" v-for="item in items" :key="item.navTitle">
      <router-link
        v-if="item.navLink"
        class="nav-link nav-link-action"
        :class="{ 'router-link-active': isActive(item) }"
        :to="item.navLink"
      >
        {{ item.navTitle }}
      </router-link>

      <span
        v-else
        class="nav-link text-muted"
        :class="{ active: hasActiveChild(item) }"
      >
        {{ item.navTitle }}
      </span>

      <!-- Recursive children -->
      <GenesisNavList
        v-if="item.navChilds"
        :items="item.navChilds"
        class="ms-3"
      />
    </li>
  </ul>
</template>

<script lang="ts">
import { defineComponent, PropType } from "vue";
import { useRoute } from "vue-router";

interface NavItem {
  navTitle: string;
  navLink?: string;
  navChilds?: NavItem[];
}

export default defineComponent({
  name: "GenesisNavList",
  props: {
    items: {
      type: Array as PropType<NavItem[]>,
      required: true,
    },
  },
  setup(props) {
    const route = useRoute();

    function isActive(item: NavItem) {
      //   if (!item.navLink) return false;
      //   return route.path.startsWith(item.navLink); // partial match
      return false;
    }

    function hasActiveChild(item: NavItem): boolean {
      if (!item.navChilds) return false;
      //   return item.navChilds.some(
      //     (child) => isActive(child) || hasActiveChild(child)
      //   );
      return false;
    }

    return { isActive, hasActiveChild };
  },
});
</script>

<style scoped>
.nav-link.active {
  font-weight: bold;
  color: var(--bs-primary);
}
.nav-link-action:hover,
.nav-link-action:focus {
  color: var(--bs-primary);
  background-color: var(--bs-secondary);
  border-radius: 0.25rem;
}
</style>
