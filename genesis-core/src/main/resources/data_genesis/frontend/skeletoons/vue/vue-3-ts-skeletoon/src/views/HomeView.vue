<template>
  <div class="w-full">
    <!-- Header -->
    <div class="items-center mb-6">
      <h2 class="text-xl font-bold text-primary">{{ t('greet') }} Developer 👋</h2>
      <span class="text-gray-500">Welcome on DEMO frontend app</span>
    </div>

    <h2 class="text-xl font-bold text-neutral">List of entities</h2>
    <hr class="border-gray-200 mb-4" />
    <!-- Quick Actions -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-4 mb-8">
      <div
        class="p-4 bg-base-100 shadow-md rounded-xl flex flex-col border border-gray-200 items-start"
      >
        <h3 class="font-semibold text-lg mb-2">Taches</h3>
        <ul class="menu w-full">
          <li @click="useTache.goToListView()">
            <span>
              <LinkIcon />
              View list of Tache
            </span>
          </li>

          <li @click="useTache.goToCreateFormView()">
            <span>
              <LinkIcon />
              Add new Tache
            </span>
          </li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { useTaches } from '@/composables/useTaches'
import LinkIcon from '@/core/icons/LinkIcon.vue'

// i18n
const { t } = useI18n({ useScope: 'global' })

const useTache = useTaches()
</script>
