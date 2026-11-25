<script setup lang="ts">
// Page dont le texte est dans un fichier HTML envoyé : t_content.fichier (Siteback) → public/upload/navbar/...
// L'accueil n'a pas de ligne dans t_content : son fichier est /upload/navbar/accueil.html
import { ContentService, HOME_FILE } from '~/services/Content'
import type { Navbar } from '~/services/Navbar'

const props = defineProps<{ route: string }>()

const { data: page, error } = await useAsyncData(`page:${props.route}`, async () => {
  if (props.route === '/') {
    return { title: 'Accueil', crumb: '', layout: 'full', html: await ContentService.ReadFile(HOME_FILE).catch(() => '') }
  }
  const content = await ContentService.GetByRoute(props.route)
  if (!content) return null
  const html = content.fichier ? await ContentService.ReadFile(content.fichier).catch(() => '') : ''
  return { title: content.nom, crumb: `${(content.navbaridTNavbar as Navbar)?.nom ?? ''} / ${content.nom}`, layout: 'docs', html }
})
if (error.value || !page.value) {
  throw createError({ statusCode: error.value ? 502 : 404, statusMessage: error.value?.message || `Page introuvable : ${props.route}`, fatal: true })
}
useHead({ title: () => page.value?.layout === 'full' ? 'Genesis · Votre base de données devient une application' : `${page.value?.title} · Genesis` })
</script>

<template>
  <main v-if="page?.layout === 'full'" id="contenu">
    <PageRenderer :html="page.html" />
  </main>
  <DocsShell v-else-if="page" :key="`${route}-${page.html.length}`" :crumb="page.crumb" :title="page.title">
    <PageRenderer :html="page.html" />
  </DocsShell>
</template>
