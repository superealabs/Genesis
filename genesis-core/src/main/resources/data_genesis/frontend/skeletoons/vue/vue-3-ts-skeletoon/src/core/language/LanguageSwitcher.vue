<template>
  <div class="flex items-center gap-2">
    <div class="dropdown">
      <input class="select cursor-default" id="lang-select" v-model="$i18n.locale" readonly  />
      <ul
        tabindex="1"
        class="dropdown-content p-2 shadow bg-base-100 rounded-box w-72 h-72 menu"
      >
        <template v-if="$i18n.availableLocales.length">
          <li v-for="locale in $i18n.availableLocales" :key="`locale-${locale}`">
            <span> {{ String(locale).toUpperCase() }}</span>
          </li>
        </template>
        <li v-else class="text-center text-gray-400 italic">No available filter</li>
      </ul>
    </div>
    <select v-model="$i18n.locale" class="select select-bordered">
      <option v-for="locale in $i18n.availableLocales" :key="`locale-${locale}`" :value="locale">
        {{ String(locale).toUpperCase() }}
      </option>
    </select>
  </div>
</template>

<script setup lang="ts">
</script>

<style scoped>
.select {
  min-width: 120px;
}
</style>
