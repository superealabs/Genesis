<template>
  <aside
    class="bg-base-200/40 border-r border-base-content/10 flex flex-col justify-between shadow-sm"
  >
    <!-- Brand -->
    <router-link to="/" class="flex items-center px-6 border-b border-base-content/10">
      <AppLogo size="80" />
      <h1 class="text-lg font-semibold tracking-wide uppercase truncate">
        {{ $t('projectName') }}
      </h1>
    </router-link>

    <div class="flex flex-col flex-1 overflow-y-auto">
      <nav class="flex-1 flex flex-col">
        <ul class="menu flex-1 flex flex-col w-auto gap-2 overflow-hidden">
          <li>
            <div class="nav-item py-0 flex router-link-container">
              <router-link
                to="/home"
                title="Home"
                class="menu-title opacity-70 flex-1 flex items-center gap-2"
              >
                <HomeIcon /> {{ $t('navbar.home') }}
              </router-link>
            </div>
          </li>
          <li>
            <div class="nav-item py-0 flex router-link-container">
              <router-link
                to="/settings"
                title="Home"
                class="menu-title flex flex-1 opacity-70 items-center gap-2"
              >
                <GearIcon /> {{ $t('navbar.settings') }}
              </router-link>
            </div>
          </li>
          <li v-for="section in navigations" :key="section.sectionName">
            <details>
              <summary class="nav-item py-0">
                <!-- Section title -->
                <span class="menu-title flex opacity-70 items-center gap-2">
              <font-awesome-icon v-if="section.icon" :icon="section.icon" />
              {{ $t(section.sectionName) }}
            </span>
              </summary>
              <!-- Items -->
              <GenesisNavList :items="section.navChilds" class="" />
            </details>
          </li>
        </ul>
      </nav>
    </div>

    <!-- Bottom section -->
    <div class="border-t border-base-content/10 flex flex-col gap-3">
      <!-- User Profile -->
      <div class="">
        <UserNavProfile
          :name="authStore.getCurrentUsername"
          :dropdown-direction="{ top: true, center: true }"
        />
      </div>
    </div>
  </aside>
</template>

<script setup lang="ts">
import GenesisNavList from './GenesisNavList.vue'
import navigations from '@/config/navigations.ts'
import HomeIcon from '@/components/icons/HomeIcon.vue'
import GearIcon from '@/components/icons/GearIcon.vue'
import UserNavProfile from '@/components/user/UserNavProfile.vue'
import AppLogo from '@/components/AppLogo.vue'
import { useAuthenticationStore } from '@/stores/useAuthenticationStore.ts'

const authStore = useAuthenticationStore()
</script>

<style scoped>
.menu {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
}

.router-link-active {
  font-weight: bold;
  color: var(--color-primary);
  position: relative;
  opacity: 1;
}

.router-link-active::before {
  content: '';
  position: absolute;
  top: 0;
  left: -7px;
  width: 5px;
  height: 100%;
  background-color: var(--color-primary);
  border-radius: 10px;
}

.router-link-container:has(.router-link-active) {
  opacity: 1;
  transition: background-color 0.25s ease;
}
</style>
