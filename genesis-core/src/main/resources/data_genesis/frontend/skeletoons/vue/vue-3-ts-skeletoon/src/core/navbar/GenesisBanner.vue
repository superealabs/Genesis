<template>
  <header
    class="w-full bg-base-200 fixed top-0 left-0 text-primary-content shadow-lg z-30"
    :style="{ height }"
    aria-region="topbar"
  >
    <div class="h-full flex items-center justify-between px-4 bg-primary">
      <!-- Brand -->
      <div class="flex items-center gap-2 uppercase">
        <PresenceLogo />
        <h1 class="text-xl md:text-2xl font-semibold">{{ $t('projectName') }}</h1>
      </div>

      <!-- Right side -->
      <div class="flex items-center gap-4">
        <LanguageSwitcher />
        <UserNavProfile name="John Doe" />
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import UserNavProfile from '@/core/user/UserNavProfile.vue'
import LanguageSwitcher from '@/core/language/LanguageSwitcher.vue'
import PresenceLogo from '@/core/PresenceLogo.vue'

defineProps<{
  height?: string
}>()
</script>
