<script setup lang="ts">
import GenesisInput from '@genesis-labs/web-core/core/components/ui/inputs/GenesisInput.vue'
import IconSearch from '@genesis-labs/web-core/core/components/ui/icons/IconSearch.vue'
import illus from '~/assets/illustrations/project-illustration.png'

const { s } = usePlayground()
const search = ref('')
const lang = ref<number | null>(null)
const list = computed(() => s.value.frameworks.filter(f =>
  (lang.value == null || f.languageId === lang.value) &&
  `${f.name} ${f.coreFramework} ${f.type}`.toLowerCase().includes(search.value.toLowerCase())))
const langName = (id: number) => s.value.languages.find(l => l.id === id)?.name ?? ''
const usedLangs = computed(() => s.value.languages.filter(l => s.value.frameworks.some(f => f.languageId === l.id)))
</script>

<template>
  <div>
    <PgStepHeader title="Framework backend" description="Choisissez le langage puis le framework de votre backend." :illustration="illus" />
    <div class="mb-4 flex flex-wrap items-center gap-3">
      <div class="flex rounded-md bg-bg-light p-1">
        <button type="button" class="rounded px-3 py-1 text-xs font-medium"
          :class="lang == null ? 'bg-primary text-primary-text' : 'text-text-muted'" @click="lang = null">Tous</button>
        <button v-for="l in usedLangs" :key="l.id" type="button" class="rounded px-3 py-1 text-xs font-medium"
          :class="lang === l.id ? 'bg-primary text-primary-text' : 'text-text-muted'" @click="lang = l.id">{{ l.name }}</button>
      </div>
      <GenesisInput v-model="search" placeholder="Rechercher par nom, core, type…" class="min-w-64 flex-1">
        <template #left><IconSearch color="var(--color-text-muted)" /></template>
      </GenesisInput>
    </div>
    <p class="mb-2 text-xs font-semibold uppercase tracking-wide text-text-muted opacity-70">Tous <span class="ml-1 rounded-full bg-bg-light px-2">{{ list.length }}</span></p>
    <div class="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
      <PgCard v-for="f in list" :key="f.id" :title="f.name" :subtitle="`${langName(f.languageId)} · ${f.type}`"
        :logo="logoOf(f.name)" :selected="s.framework?.id === f.id" @select="s.framework = f" />
    </div>
  </div>
</template>
