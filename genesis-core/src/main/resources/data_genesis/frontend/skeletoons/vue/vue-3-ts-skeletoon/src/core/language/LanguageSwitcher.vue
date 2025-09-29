<template>
  <div class="flex items-center gap-2">
    <!-- Custom Dropdown -->
    <div class="dropdown">
      <label tabindex="0" class="btn m-1 bg-transparent border-0 flex items-center gap-2">
        <WorldIcon />
        <img
          :src="'https://flagsapi.com/' + $i18n.locale.toUpperCase() + '/shiny/32.png'"
          :alt="$i18n.locale"
          class="w-5 h-5 rounded"
        />
        <!--        <span>{{ $i18n.locale.toUpperCase() }}</span>-->
      </label>

      <ul
        tabindex="0"
        class="dropdown-content p-2 shadow bg-base-100 text-base-content rounded-box w-48 menu"
      >
        <li
          v-for="locale in $i18n.availableLocales"
          :key="`locale-${locale}`"
          @click="$i18n.locale = locale"
        >
          <a class="flex items-center gap-2">
            <img
              :src="'https://flagsapi.com/' + locale.toUpperCase() + '/shiny/32.png'"
              :alt="locale"
              class="w-5 h-5 rounded"
            />
            <span>{{ locale.toUpperCase() }}</span>
          </a>
        </li>
      </ul>
    </div>
  </div>
</template>

<script setup lang="ts">
import WorldIcon from '@/core/icons/WorldIcon.vue'
</script>

<style scoped>
.menu li {
  cursor: pointer;
}
</style>
