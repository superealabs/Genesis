<template>
  <div class="dropdown dropdown-start">
    <label tabindex="0" class="select gap-3">
      <img
        :src="getFlagUrl(languageStore.language)"
        :alt="languageStore.language"
        class="w-5 h-5 rounded"
      />
      <span class="hidden md:inline text-sm font-medium">
        {{ languageStore.language.toUpperCase() }}
      </span>
    </label>

    <ul
      tabindex="0"
      class="dropdown-content menu p-2 shadow bg-base-200 text-base-content rounded-box w-75"
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

const languageStore = useLanguageStore()
</script>
