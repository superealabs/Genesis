<template>
  <div class="dropdown dropdown-start">
    <GenesisButton class="btn btn-ghost text-center flex items-center">
      <WorldIcon />
      <span class="md:inline text-sm font-medium">
        {{ languageStore.language.toUpperCase() }}
      </span>
    </GenesisButton>

    <ul
      tabindex="0"
      class="dropdown-content menu p-2 shadow bg-base-200 text-base-content rounded-box w-25"
    >
      <li
        v-for="locale in $i18n.availableLocales"
        :key="`locale-${locale}`"
        @click="languageStore.setLanguage(locale)"
      >
        <a class="flex items-center gap-2">
          <img :src="getFlagUrl(locale)" :alt="locale" class="w-5 h-5 rounded" />
          <span>{{ locale.toUpperCase() }}</span>
        </a>
      </li>
    </ul>
  </div>
</template>

<script setup lang="ts">
import { useLanguageStore } from '@/stores/useLanguageStore'
import { getFlagUrl } from '@/utils/flagsUtils'
import WorldIcon from '../icons/WorldIcon.vue'
import GenesisButton from '../button/GenesisButton.vue'

const languageStore = useLanguageStore()
</script>
