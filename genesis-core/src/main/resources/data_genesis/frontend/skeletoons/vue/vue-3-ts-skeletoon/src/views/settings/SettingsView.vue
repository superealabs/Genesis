<template>
  <section class="space-y-6">
    <!-- Header -->
    <header class="flex items-center justify-between">
      <h2 class="text-2xl font-semibold tracking-tight text-base-content flex items-center gap-2">
        <font-awesome-icon icon="gear" class="text-primary" />
        {{ $t('navbar.settings') }}
      </h2>
    </header>

    <!-- Settings Content -->
    <div class="grid gap-6 md:grid-cols-2">
      <!-- General Settings -->
      <div class="card bg-base-200/50 shadow-sm border border-base-content/10">
        <div class="card-body">
          <h3 class="text-lg font-semibold mb-2 flex items-center gap-2">
            <font-awesome-icon icon="sliders" class="text-primary" />
            {{ $t('settings.general') || 'General' }}
          </h3>
          <div class="divider my-2"></div>

          <!-- Theme -->
          <div class="form-control w-full mb-3">
            <label class="label font-medium">
              <span class="label-text">{{ $t('settings.theme') }}</span>
            </label>
            <ThemeSwitcher />
          </div>

          <!-- Language -->
          <div class="form-control w-full">
            <label class="label font-medium">
              <span class="label-text">{{ $t('settings.language') }}</span>
            </label>
            <div class="flex">
              <LanguageSwitcher class="w-full" />
            </div>
          </div>

          <div class="form-control w-full">
            <label class="label font-medium">
              <span class="label-text">{{ $t('settings.layout') }}</span>
            </label>
            <div class="flex">
              <LayoutSwitcher class="" />
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import ThemeSwitcher from '@/components/switcher/ThemeSwitcher.vue'
import LanguageSwitcher from '@/components/switcher/LanguageSwitcher.vue'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import LayoutSwitcher from '@/components/switcher/LayoutSwitcher.vue'
</script>
