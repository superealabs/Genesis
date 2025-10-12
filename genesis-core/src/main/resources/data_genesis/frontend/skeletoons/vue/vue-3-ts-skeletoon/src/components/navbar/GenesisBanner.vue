<template>
  <header
    class="w-full fixed top-0 left-0 z-30 backdrop-blur-md bg-base-100/80 shadow-sm border-b border-base-200"
    :style="{ height }"
    aria-region="topbar"
  >
    <div class="h-full flex items-center justify-between px-4">
      <!-- Brand -->
      <div class="flex items-center gap-3">
        <AppLogo size="40" />
        <h1 class="text-lg md:text-xl font-semibold tracking-wide uppercase">
          {{ $t('projectName') }}
        </h1>
      </div>

      <!-- Actions -->
      <div class="flex items-center gap-2 md:gap-3">
        <router-link
          to="/home"
          class="btn btn-sm btn-ghost bg-transparent hover:bg-base-200"
          title="Home"
        >
          <HomeIcon />
        </router-link>

        <router-link
          to="/customisation"
          class="btn btn-sm btn-ghost bg-transparent hover:bg-base-200"
          title="Settings"
        >
          <GearIcon />
        </router-link>

        <!-- Language Switcher -->
        <LanguageSwitcher />

        <!-- User Menu -->
        <div class="dropdown dropdown-end">
          <label
            tabindex="0"
            class="btn btn-sm btn-ghost bg-transparent flex items-center gap-2 hover:bg-base-200"
          >
            <div class="avatar">
              <div class="w-8 rounded-full ring ring-primary ring-offset-base-100 ring-offset-1">
                <img src="https://i.pravatar.cc/40" alt="User avatar" />
              </div>
            </div>
            <span class="hidden md:inline font-medium">Alex</span>
          </label>

          <ul tabindex="0" class="dropdown-content menu p-2 shadow bg-base-100 rounded-box w-44">
            <li><router-link to="/profile">Profile</router-link></li>
            <li><router-link to="/settings">Account settings</router-link></li>
            <li><a @click="logout">Logout</a></li>
          </ul>
        </div>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import AppLogo from '@/components/AppLogo.vue'
import HomeIcon from '@/components/icons/HomeIcon.vue'
import GearIcon from '@/components/icons/GearIcon.vue'
import LanguageSwitcher from '@/components/language/LanguageSwitcher.vue'

defineProps<{
  height?: string
}>()

const logout = () => {
  console.log('Logout clicked')
}
</script>

<style scoped>
header {
  transition:
    background 0.3s ease,
    backdrop-filter 0.3s ease;
}
</style>
