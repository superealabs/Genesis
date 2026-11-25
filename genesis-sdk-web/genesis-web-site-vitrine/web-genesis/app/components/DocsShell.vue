<script setup lang="ts">
// Mise en page des pages Docs / Ecosystem / About : sidebar (contenus de la navbar) + contenu + « On this page »
const props = defineProps<{ crumb: string; title: string }>()
const { current } = useNavigation()
const route = useRoute()
const main = ref<HTMLElement | null>(null)
const toc = ref<{ id: string; text: string }[]>([])
const active = ref('')

function buildToc() {
  toc.value = Array.from(main.value?.querySelectorAll('h2[id]') ?? [])
    .map(h => ({ id: h.id, text: h.textContent?.trim() || '' }))
}
let obs: IntersectionObserver | null = null
onMounted(() => {
  buildToc()
  obs = new IntersectionObserver(entries => {
    for (const e of entries) if (e.isIntersecting) active.value = e.target.id
  }, { rootMargin: '-80px 0px -70% 0px' })
  main.value?.querySelectorAll('h2[id]').forEach(h => obs!.observe(h))
})
onBeforeUnmount(() => obs?.disconnect())
useHead({ title: () => `${props.title} · Genesis`, bodyAttrs: { class: 'has-sidebar' } })
</script>

<template>
  <div class="docs">
    <aside class="sidebar" aria-label="Navigation de section">
      <NuxtLink class="brand sidebar__brand" to="/"><img src="/assets/img/genie-normal.svg" alt=""><span>Genesis</span></NuxtLink>
      <nav class="sidebar__links">
        <NuxtLink v-for="c in current?.contents" :key="c.id" :to="c.route"
          :aria-current="route.path === c.route ? 'page' : undefined">{{ c.nom }}</NuxtLink>
      </nav>
    </aside>
    <main id="contenu" ref="main" class="content">
      <header class="banner"><span class="crumb">{{ crumb }}</span><h1>{{ title }}</h1><div class="banner__cut" /></header>
      <slot />
    </main>
    <aside v-if="toc.length" class="toc" aria-label="Sur cette page">
      <p class="toc__title">On this page</p>
      <a v-for="t in toc" :key="t.id" :href="`#${t.id}`" :class="{ active: active === t.id }">{{ t.text }}</a>
    </aside>
  </div>
</template>
