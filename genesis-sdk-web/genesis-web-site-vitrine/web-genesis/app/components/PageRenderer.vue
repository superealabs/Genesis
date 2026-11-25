<script setup lang="ts">
                                         
import type { Component } from 'vue'
import { CharteGraphiqueService } from '~/services/CharteGraphique'
import TeamGrid from './TeamGrid.vue'
import CompatibilityTable from './CompatibilityTable.vue'
import FrameworkCatalog from './FrameworkCatalog.vue'

const props = defineProps<{ html: string }>()
const COMPONENTS: Record<string, Component> = { TeamGrid, CompatibilityTable, FrameworkCatalog }

const { data: charte } = await useAsyncData('charte', () => CharteGraphiqueService.Getall().catch(() => []), { default: () => [] })
const charteFile = computed(() => charte.value?.at(-1)?.file || '/upload/icon/charte_graphique-V2.pdf')

type Segment = { html: string } | { component: string; inner?: string }
const segments = computed<Segment[]>(() => {
  const src = (props.html || '').replaceAll('{{charte}}', charteFile.value)
  const re = /<!--component:(\w+)\/-->|<!--component:(\w+)-->([\s\S]*?)<!--\/component-->/g
  const out: Segment[] = []
  let last = 0
  for (const m of src.matchAll(re)) {
    if (m.index! > last) out.push({ html: src.slice(last, m.index) })
    out.push(m[1] ? { component: m[1] } : { component: m[2]!, inner: m[3] })
    last = m.index! + m[0].length
  }
  if (last < src.length) out.push({ html: src.slice(last) })
  return out
})

function onClick(e: MouseEvent) {
  const target = e.target as HTMLElement
  const tab = target.closest<HTMLElement>('[data-tabs] [role="tab"]')
  if (tab) {
    tab.closest('[data-tabs]')!.querySelectorAll<HTMLElement>('[role="tab"]').forEach(b => {
      const on = b === tab
      b.setAttribute('aria-selected', String(on))
      const panel = document.getElementById(b.getAttribute('aria-controls') || '')
      if (panel) panel.hidden = !on
    })
    return
  }
  const strip = target.closest<HTMLElement>('[data-strip]')
  if (strip) {
    const list = strip.closest('section, div')?.parentElement?.querySelector('[data-strip-list]') ?? document.querySelector('[data-strip-list]')
    list?.scrollBy({ left: Number(strip.dataset.strip) * 240, behavior: 'smooth' })
    return
  }
  const a = target.closest<HTMLAnchorElement>('a[href^="/"]')
  if (a && !a.target && !a.hasAttribute('download') && !a.getAttribute('href')!.startsWith('/assets/')
      && !a.getAttribute('href')!.startsWith('/upload/') && !e.ctrlKey && !e.metaKey) {
    e.preventDefault()
    navigateTo(a.getAttribute('href')!)
  }
}
</script>

<template>
  <div class="page-html" @click="onClick">
    <template v-for="(seg, i) in segments" :key="i">
      <div v-if="'html' in seg" class="page-html__part" v-html="seg.html" />
      <component :is="COMPONENTS[seg.component]" v-else-if="COMPONENTS[seg.component]">
        <div v-if="seg.inner" v-html="seg.inner" />
      </component>
    </template>
  </div>
</template>

<style scoped>
.page-html, .page-html__part { display: contents; }
</style>
