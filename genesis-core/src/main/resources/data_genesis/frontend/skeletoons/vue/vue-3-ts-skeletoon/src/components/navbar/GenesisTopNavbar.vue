<template>
  <nav class="navbar bg-base-200/40 border-b border-base-content/10 shadow-sm">
    <div class="flex items-center justify-between w-full px-4">
      <!-- Left section: Brand -->
      <router-link to="/" class="flex items-center gap-2">
        <AppLogo size="60" />
        <h1 class="font-semibold uppercase tracking-wide text-base">
          {{ $t('projectName') }}
        </h1>
      </router-link>

      <!-- Center section: Navigation items -->
      <ul class="menu menu-horizontal px-2 flex justify-end flex-1 items-center gap-2">
        <li>
          <router-link
            to="/home"
            class=" btn btn-ghost btn-sm menu-title opacity-70 flex items-center gap-2"
          >
            <HomeIcon />
            {{ $t('navbar.home') }}
          </router-link>
        </li>
        <!-- Dynamic Sections -->
        <li
          v-for="section in navigations"
          :key="section.sectionName"
          class="relative"
        >
          <details class="dropdown dropdown-bottom">
            <summary class="btn btn-ghost btn-sm opacity-80 flex items-center gap-2">
              <font-awesome-icon v-if="section.icon" :icon="section.icon" />
              {{ $t(section.sectionName) }}
            </summary>
            <ul
              class="p-2 menu dropdown-content bg-base-200 rounded-box shadow-md w-52"
            >
              <GenesisTopNavList :items="section.navChilds" />
            </ul>
          </details>
        </li>
        <li>
          <router-link
            to="/settings"
            class="btn btn-ghost btn-sm menu-title opacity-70 flex items-center gap-2"
          >
            <GearIcon />
            {{ $t('navbar.settings') }}
          </router-link>
        </li>

      </ul>

      <div class="min-w-40">
        <!-- Right section: User -->
        <UserNavProfile
          :name="authStore.getCurrentUsername"
          :dropdown-direction="{ bottom: true, center: true }"
        />
      </div>
    </div>
  </nav>
</template>

<script setup lang="ts">
import { useAuthenticationStore } from '@/stores/useAuthenticationStore.ts'
import GenesisTopNavList from './GenesisTopNavList.vue'
import navigations from '@/config/navigations.ts'
import HomeIcon from '@/components/icons/HomeIcon.vue'
import GearIcon from '@/components/icons/GearIcon.vue'
import AppLogo from '@/components/AppLogo.vue'
import UserNavProfile from '@/components/user/UserNavProfile.vue'

const authStore = useAuthenticationStore()
</script>

<style scoped>
.router-link-active {
  font-weight: bold;
  color: var(--color-primary);
  position: relative;
}

.router-link-active::after {
  content: '';
  position: absolute;
  bottom: -4px;
  left: 0;
  width: 100%;
  height: 2px;
  background-color: var(--color-primary);
  border-radius: 4px;
}
</style>
