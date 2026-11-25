<script setup lang="ts">
import GenesisInput from '@genesis-labs/web-core/core/components/ui/inputs/GenesisInput.vue'
import illus from '~/assets/illustrations/frontend.png'
const { s, isMvc, hasLayout, setAsset } = usePlayground()

// Libellés des types de navbar renvoyés par GET /api/frontend/navbar-types
const NAVBAR_LABELS: Record<string, string> = { side: 'Barre latérale (Side)', top: 'Barre supérieure (Top)' }
const navbarOptions = computed(() => s.value.navbarTypes.map(v => ({ label: NAVBAR_LABELS[v] ?? v, value: v })))

function toggleLanguage(code: string) {
  const list = s.value.layout.selectedInterfaceLanguages
  const i = list.indexOf(code)
  if (i >= 0) list.splice(i, 1)
  else list.push(code)
}

function onFile(kind: 'logo' | 'favicon', e: Event) {
  const input = e.target as HTMLInputElement
  setAsset(kind, input.files?.[0] ?? null)
  input.value = ''
}
</script>

<template>
  <div>
    <PgStepHeader title="Frontend" description="Choisissez le framework frontend, puis la langue, les couleurs et le logo de l'application." :illustration="illus" />

    <!-- 1. CONFIGURATION DE BASE (framework, port) -->
    <p v-if="isMvc" class="mb-6 rounded-lg bg-bg-light p-4 text-sm text-text-muted">
      {{ s.framework?.name }} est un framework MVC : les vues sont générées avec le backend, pas de frontend séparé.
      Vous pouvez quand même régler leur layout ci-dessous.
    </p>
    <p v-else-if="!s.framework?.useFrontendApp" class="rounded-lg bg-bg-light p-4 text-sm text-text-muted">
      {{ s.framework?.name }} ne propose pas de frontend.
    </p>
    <template v-else>
      <div class="mb-4 flex rounded-md bg-bg-light p-1 w-fit">
        <button type="button" class="rounded px-3 py-1 text-xs font-medium" :class="s.withFrontend ? 'bg-primary text-primary-text' : 'text-text-muted'" @click="s.withFrontend = true">Avec frontend</button>
        <button type="button" class="rounded px-3 py-1 text-xs font-medium" :class="!s.withFrontend ? 'bg-primary text-primary-text' : 'text-text-muted'" @click="s.withFrontend = false">Sans frontend</button>
      </div>
      <template v-if="s.withFrontend">
        <div class="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
          <PgCard v-for="f in s.frontends" :key="f.id" :title="f.name" :subtitle="`port ${f.defaultPort}`"
            :logo="logoOf(f.name)" :selected="s.frontend?.id === f.id"
            @select="s.frontend = f; s.frontPort = f.defaultPort || s.frontPort" />
        </div>
        <div class="mt-4 max-w-xs">
          <GenesisInput v-model="s.frontPort" label="Port d'exécution" type="number" placeholder="ex: 3000" fill-width />
        </div>
      </template>
    </template>

    <template v-if="hasLayout">
      <div class="mt-8 grid gap-8 lg:grid-cols-[minmax(0,1fr)_320px]">
        <div class="space-y-8">

          <!-- 2. LANGUES SUPPORTÉES -->
          <section class="space-y-4">
            <h3 class="border-b border-bg-neutral-genesis pb-2 text-lg font-semibold text-text">Langues supportées</h3>
            <p class="text-sm text-text-muted">Les langues dans lesquelles l'interface générée pourra s'afficher.</p>
            <div class="flex flex-wrap gap-2">
              <button v-for="l in s.interfaceLanguages" :key="l.code" type="button"
                class="flex items-center gap-2 rounded-md border px-3 py-2 text-sm font-medium transition-colors"
                :class="s.layout.selectedInterfaceLanguages.includes(l.code)
                  ? 'border-primary bg-primary text-primary-text'
                  : 'border-transparent bg-bg-light text-text hover:border-primary'"
                :aria-pressed="s.layout.selectedInterfaceLanguages.includes(l.code)"
                @click="toggleLanguage(l.code)">
                <span class="rounded bg-bg-dark px-1.5 py-0.5 text-[10px] font-bold uppercase text-text">{{ l.code }}</span>
                {{ l.name }}
              </button>
            </div>
          </section>

          <!-- 3. CHARTE GRAPHIQUE & LAYOUT -->
          <section class="space-y-4">
            <h3 class="border-b border-bg-neutral-genesis pb-2 text-lg font-semibold text-text">Charte graphique &amp; Layout</h3>

            <GenesisInput v-model="s.layout.navbarType" type="select" :options="navbarOptions"
              label="Type de navbar" is-mandatory size="lg" fill-width placeholder="Sélectionner…" />

            <div class="grid grid-cols-1 gap-4 md:grid-cols-2">
              <GenesisInput v-model="s.layout.primaryColor" type="color" label="Couleur principale"
                placeholder="#3B82F6" is-mandatory size="lg" fill-width />
              <GenesisInput v-model="s.layout.secondaryColor" type="color" label="Couleur secondaire"
                placeholder="#64748B" is-mandatory size="lg" fill-width />
            </div>

            <div class="grid grid-cols-1 gap-4 md:grid-cols-2">
              <div v-for="kind in (['logo', 'favicon'] as const)" :key="kind" class="flex flex-col gap-2">
                <span class="text-sm font-medium text-muted">{{ kind === 'logo' ? 'Fichier logo' : 'Fichier favicon' }}</span>
                <div class="flex h-24 items-center justify-center overflow-hidden rounded-lg border border-bg-neutral-genesis bg-bg-light">
                  <img v-if="kind === 'logo' ? s.logoPreview : s.faviconPreview" :src="kind === 'logo' ? s.logoPreview : s.faviconPreview"
                    :alt="`Aperçu ${kind}`" class="max-h-full max-w-full object-contain">
                  <span v-else class="text-xs text-text-muted">Aucun fichier sélectionné</span>
                </div>
                <div class="flex gap-2">
                  <label class="flex-1 cursor-pointer rounded-md bg-bg-secondary px-3 py-2 text-center text-sm font-medium text-secondary hover:brightness-110">
                    {{ (kind === 'logo' ? s.logoFile : s.faviconFile) ? 'Changer' : 'Choisir un fichier' }}
                    <input type="file" class="sr-only" :accept="kind === 'logo' ? '.png,.jpg,.jpeg,.ico' : '.png,.ico'" @change="onFile(kind, $event)">
                  </label>
                  <button v-if="kind === 'logo' ? s.logoFile : s.faviconFile" type="button"
                    class="rounded-md bg-bg-light px-3 py-2 text-sm text-text-muted hover:text-text" @click="setAsset(kind, null)">
                    Retirer
                  </button>
                </div>
                <small class="text-xs text-text-muted">{{ kind === 'logo' ? 'PNG, JPEG ou ICO · 2 Mo max · facultatif' : 'PNG ou ICO · 2 Mo max · facultatif' }}</small>
              </div>
            </div>
          </section>
        </div>

        <!-- APERÇU DE L'INTERFACE -->
        <aside class="self-start rounded-lg border border-bg-neutral-genesis bg-bg-light p-4 lg:sticky lg:top-0">
          <p class="mb-3 text-xs font-semibold uppercase tracking-wide text-text">Aperçu de l'interface</p>
          <div class="overflow-hidden rounded-md border border-bg-neutral-genesis bg-white text-[11px] text-slate-700">
            <div class="flex h-44" :class="s.layout.navbarType === 'top' ? 'flex-col' : ''">
              <div class="flex shrink-0 gap-2 p-2 text-white"
                :class="s.layout.navbarType === 'top' ? 'h-9 items-center' : 'w-20 flex-col'"
                :style="{ background: s.layout.primaryColor }">
                <img v-if="s.logoPreview" :src="s.logoPreview" alt="" class="h-5 w-5 rounded object-contain bg-white">
                <span v-else class="block h-5 w-5 rounded bg-white"></span>
                <span class="block h-1.5 w-10 rounded bg-white"></span>
                <span class="block h-1.5 w-8 rounded bg-slate-200"></span>
                <span class="block h-1.5 w-9 rounded bg-slate-200"></span>
              </div>
              <div class="flex-1 space-y-2 p-3">
                <span class="block h-2 w-24 rounded" :style="{ background: s.layout.secondaryColor }"></span>
                <span class="block h-1.5 w-full rounded bg-slate-200"></span>
                <span class="block h-1.5 w-5/6 rounded bg-slate-200"></span>
                <span class="mt-3 inline-block rounded px-2 py-1 font-semibold text-white" :style="{ background: s.layout.primaryColor }">Enregistrer</span>
                <span class="ml-1 inline-block rounded px-2 py-1 font-semibold text-white" :style="{ background: s.layout.secondaryColor }">Annuler</span>
              </div>
            </div>
          </div>
          <p class="mt-3 text-xs text-text-muted">
            Langues : {{ s.layout.selectedInterfaceLanguages.map(c => c.toUpperCase()).join(', ') || '—' }}
          </p>
        </aside>
      </div>
    </template>
  </div>
</template>
