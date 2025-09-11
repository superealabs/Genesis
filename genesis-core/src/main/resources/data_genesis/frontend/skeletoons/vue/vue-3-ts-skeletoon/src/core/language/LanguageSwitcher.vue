<template>
  <div class="flex items-center gap-2">
    <select id="lang-select" v-model="$i18n.locale" class="select select-bordered">
      <option v-for="locale in $i18n.availableLocales" :key="`locale-${locale}`" :value="locale">
        <div>
          <span>{{ String(locale).toUpperCase() }}</span>
        </div>
      </option>
    </select>
  </div>
</template>

<script setup lang="ts"></script>

<style scoped>
.select {
  min-width: 120px;
}
</style>
