<script setup lang="ts">
// Étape « Script SQL » — panneaux comme le ScriptConfigView de genesis-vsc :
// éditeur de code + panneau latéral (Tables à générer | Assistant IA).
import GenesisIdeCm from '@genesis-labs/web-core/core/components/layouts/ide/GenesisIdeCm.vue'
import GenesisButton from '@genesis-labs/web-core/core/components/ui/actions/GenesisButton.vue'
import GenesisSegmentedControl from '@genesis-labs/web-core/core/components/ui/actions/GenesisSegmentedControl.vue'
import GenesisCheckboxSimple from '@genesis-labs/web-core/core/components/ui/inputs/GenesisCheckboxSimple.vue'
import GenesisSwitch from '@genesis-labs/web-core/core/components/ui/inputs/GenesisSwitch.vue'
import IconTable from '@genesis-labs/web-core/core/components/ui/icons/IconTable.vue'
import IconData from '@genesis-labs/web-core/core/components/ui/icons/IconData.vue'
import IconBot from '@genesis-labs/web-core/core/components/ui/icons/IconBot.vue'
import IconUpload from '@genesis-labs/web-core/core/components/ui/icons/IconUpload.vue'
import IconSearch from '@genesis-labs/web-core/core/components/ui/icons/IconSearch.vue'
import illus from '~/assets/illustrations/database-summary.png'

const { s, analyseScript, selectDatabase, suggestedDatabases, askAi, applyAiScript } = usePlayground()
const { theme } = useTheme()
const fileInput = ref<HTMLInputElement | null>(null)
const chat = ref<HTMLElement | null>(null)

const panel = ref<'tables' | 'ai'>('tables')
const PANELS = [
  { value: 'tables', label: 'Tables', icon: IconTable },
  { value: 'ai', label: 'Assistant IA', icon: IconBot }
]
const filter = ref<'all' | 'table' | 'view'>('all')
const FILTERS = [{ value: 'all', label: 'Tous' }, { value: 'table', label: 'Tables' }, { value: 'view', label: 'Vues' }]
const search = ref('')
const COMPONENTS = [
  { value: 'model', label: 'Modèles' }, { value: 'dao', label: 'Repositories (DAO)' },
  { value: 'service', label: 'Services' }, { value: 'controller', label: 'Contrôleurs' }
]
const SUGGESTIONS = [
  'Une boutique : clients, produits, commandes et lignes de commande',
  'Une école : étudiants, cours, inscriptions avec une note',
  'Ajoute une table fournisseur liée aux produits'
]

/** Tables + vues du script, filtrées comme la liste « Disponibles » de genesis-vsc */
const entities = computed(() => {
  const q = search.value.trim().toLowerCase()
  return [
    ...s.value.tables.map(name => ({ name, kind: 'table' as const })),
    ...s.value.views.map(name => ({ name, kind: 'view' as const }))
  ].filter(e => (filter.value === 'all' || e.kind === filter.value) && e.name.toLowerCase().includes(q))
})
const listOf = (kind: 'table' | 'view') => kind === 'table' ? s.value.selectedTables : s.value.selectedViews
const isChecked = (e: { name: string; kind: 'table' | 'view' }) => listOf(e.kind).includes(e.name)
function toggle(list: string[], v: string) {
  const i = list.indexOf(v); i < 0 ? list.push(v) : list.splice(i, 1)
}
const allChecked = computed(() => entities.value.length > 0 && entities.value.every(isChecked))
function toggleAll() {
  const check = !allChecked.value
  for (const e of entities.value) {
    const list = listOf(e.kind)
    if (check !== list.includes(e.name)) toggle(list, e.name)
  }
}

const modelOptions = computed(() => s.value.ai.models.map(m => ({ label: m.name, value: m.id })))
const mismatch = computed(() => s.value.error.includes('ne correspond pas'))

async function loadFile(e: Event) {
  const input = e.target as HTMLInputElement
  const f = input.files?.[0]
  if (f) s.value.script = await f.text()
  input.value = ''
}
async function useDatabase(db: (typeof s.value.databases)[number]) {
  selectDatabase(db)
  await analyseScript()
}
function send() {
  askAi()
}
function onKey(e: KeyboardEvent) {
  if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); send() }
}
async function apply(script: string, mode: 'replace' | 'append') {
  await applyAiScript(script, mode)
  panel.value = 'tables'
}

watch(() => s.value.script, () => { s.value.tables = []; s.value.views = [] })
watch(() => s.value.ai.messages.length, () => nextTick(() => {
  if (chat.value) chat.value.scrollTop = chat.value.scrollHeight
}))
</script>

<template>
  <div>
    <PgStepHeader title="Script SQL" :description="`Collez, chargez ou générez le script SQL de votre base ${s.database?.name ?? ''}.`" :illustration="illus">
      <input ref="fileInput" type="file" accept=".sql,.txt" class="hidden" @change="loadFile">
      <GenesisButton variant="secondary" @click="fileInput?.click()">
        <template #leftIcon><IconUpload color="var(--color-secondary)" /></template>
        Charger un .sql
      </GenesisButton>
    </PgStepHeader>

    <!-- Le script ne correspond pas à la base choisie : proposer la bonne base -->
    <div v-if="mismatch && suggestedDatabases.length" class="mb-4 flex flex-wrap items-center gap-3 rounded-lg border border-bg-neutral-genesis bg-bg-light p-3 text-sm">
      <span class="text-text">Ce script est écrit pour une autre base que <strong>{{ s.database?.name }}</strong>.</span>
      <GenesisButton v-for="d in suggestedDatabases" :key="d.id" variant="primary" @click="useDatabase(d)">
        Utiliser {{ d.name }}
      </GenesisButton>
    </div>

    <div class="grid gap-4 lg:grid-cols-[minmax(0,1fr)_360px]">
      <!-- ÉDITEUR (le pied de l'éditeur affiche déjà lignes / caractères) -->
      <section class="h-[320px] overflow-hidden rounded-lg border border-bg-neutral-genesis lg:h-[520px]">
        <ClientOnly>
          <GenesisIdeCm v-model="s.script" language="sql" filename="schema.sql" :is-dark="theme === 'dark'" />
        </ClientOnly>
      </section>

      <!-- PANNEAU LATÉRAL -->
      <aside class="flex h-[520px] flex-col overflow-hidden rounded-lg border border-bg-neutral-genesis">
        <div class="flex items-center justify-between gap-2 border-b border-bg-neutral-genesis p-3">
          <GenesisSegmentedControl v-model="panel" :options="PANELS" size="md" />
          <span class="rounded bg-bg-light px-2 py-0.5 text-xs font-semibold text-text">{{ s.database?.name }}</span>
        </div>

        <!-- TABLES -->
        <div v-if="panel === 'tables'" class="flex min-h-0 flex-1 flex-col">
          <div class="space-y-3 p-3">
            <GenesisButton variant="primary" fill-width content-align="center" @click="analyseScript">
              <template #leftIcon><IconTable color="var(--color-primary-text)" /></template>
              Analyser le script
            </GenesisButton>
            <div class="flex items-center justify-between">
              <span class="text-sm font-semibold text-text">Disponibles <span class="text-primary">*</span></span>
              <button v-if="entities.length" type="button" class="text-xs font-semibold text-primary hover:underline" @click="toggleAll">
                {{ allChecked ? 'Tout désélectionner' : 'Tout sélectionner' }}
              </button>
            </div>
            <div class="flex gap-2">
              <label class="flex min-w-0 flex-1 items-center gap-2 rounded-md border border-bg-neutral-genesis bg-bg-dark px-2">
                <IconSearch :size="14" color="var(--color-text-muted)" />
                <input v-model="search" type="search" placeholder="Rechercher…" class="min-w-0 flex-1 bg-transparent py-1.5 text-sm text-text outline-none">
              </label>
              <GenesisSegmentedControl v-model="filter" :options="FILTERS" size="sm" />
            </div>
          </div>

          <ul class="min-h-0 flex-1 overflow-y-auto border-y border-bg-neutral-genesis">
            <li v-if="!s.tables.length && !s.views.length" class="p-4 text-xs text-text-muted">
              Cliquez sur « Analyser le script » pour lire les CREATE TABLE.
            </li>
            <li v-else-if="!entities.length" class="p-4 text-xs text-text-muted">Aucun résultat.</li>
            <li v-for="e in entities" :key="e.kind + e.name">
              <label class="flex cursor-pointer items-center gap-3 border-b border-bg-neutral-genesis px-3 py-2 last:border-b-0 hover:bg-bg-light">
                <GenesisCheckboxSimple :model-value="isChecked(e)" @update:model-value="toggle(listOf(e.kind), e.name)" />
                <component :is="e.kind === 'table' ? IconTable : IconData" :size="16" color="var(--color-text-muted)" />
                <span class="min-w-0">
                  <span class="block truncate text-sm text-text">{{ e.name }}</span>
                  <span class="block text-xs text-text-muted">{{ e.kind === 'table' ? 'Table' : 'Vue' }}</span>
                </span>
              </label>
            </li>
          </ul>

          <div class="p-3">
            <p class="mb-2 text-sm font-semibold text-text">Composants <span class="text-primary">*</span></p>
            <div class="flex flex-wrap gap-1.5">
              <button v-for="c in COMPONENTS" :key="c.value" type="button"
                class="rounded-full border px-3 py-1 text-xs font-semibold transition-colors"
                :class="s.components.includes(c.value) ? 'border-primary bg-primary text-primary-text' : 'border-bg-neutral-genesis text-text-muted hover:text-text'"
                @click="toggle(s.components, c.value)">
                {{ c.label }}
              </button>
            </div>
          </div>
        </div>

        <!-- ASSISTANT IA -->
        <div v-else class="flex min-h-0 flex-1 flex-col">
          <div ref="chat" class="min-h-0 flex-1 space-y-3 overflow-y-auto p-3">
            <div v-if="!s.ai.messages.length" class="space-y-3 pt-2">
              <p class="text-center text-sm text-text-muted">Décrivez votre base : l'IA écrit le script pour <strong class="text-text">{{ s.database?.name }}</strong>.</p>
              <button v-for="sug in SUGGESTIONS" :key="sug" type="button"
                class="block w-full rounded-lg border border-bg-neutral-genesis px-3 py-2 text-left text-xs text-text hover:bg-bg-light"
                @click="s.ai.prompt = sug">
                {{ sug }}
              </button>
            </div>

            <div v-for="m in s.ai.messages" :key="m.id" class="flex flex-col" :class="m.role === 'user' ? 'items-end' : 'items-start'">
              <div class="max-w-[92%] rounded-2xl px-3 py-2 text-sm"
                :class="m.role === 'user' ? 'rounded-tr-sm bg-bg-secondary text-text' : m.error ? 'rounded-tl-sm border border-red-500/60 bg-red-500/10 text-text' : 'rounded-tl-sm bg-bg-light text-text'">
                <span v-if="m.loading" class="flex items-center gap-2 italic text-text-muted">
                  <span class="h-2 w-2 animate-ping rounded-full bg-primary" /> Génération en cours…
                </span>
                <template v-else>
                  <p class="whitespace-pre-wrap break-words">{{ m.content }}</p>
                  <template v-if="m.script">
                    <pre class="mt-2 max-h-40 overflow-auto rounded bg-bg-dark p-2 text-[11px] leading-snug text-text">{{ m.script }}</pre>
                    <p v-if="m.warning" class="mt-2 text-xs text-text-muted">⚠ {{ m.warning }}</p>
                    <div class="mt-2 flex flex-wrap gap-2">
                      <GenesisButton variant="primary" size="sm" @click="apply(m.script!, 'replace')">Remplacer le script</GenesisButton>
                      <GenesisButton variant="secondary" size="sm" @click="apply(m.script!, 'append')">Ajouter à la suite</GenesisButton>
                    </div>
                  </template>
                </template>
              </div>
              <span class="mt-1 px-1 text-[10px] text-text-muted opacity-60">{{ new Date(m.at).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) }}</span>
            </div>
          </div>

          <div class="space-y-2 border-t border-bg-neutral-genesis p-3">
            <p v-if="!s.ai.models.length" class="text-xs text-text-muted">Assistant indisponible : genesis-api ne répond pas sur /generator/llm-models.</p>
            <div class="flex items-center gap-2">
              <select v-model="s.ai.modelId" class="min-w-0 flex-1 rounded-md border border-bg-neutral-genesis bg-bg-dark px-2 py-1.5 text-xs text-text outline-none focus:border-primary">
                <option v-for="o in modelOptions" :key="o.value" :value="o.value">{{ o.label }}</option>
              </select>
              <GenesisSwitch v-model="s.ai.includeCurrentScript" size="sm" label="Script actuel" />
            </div>
            <div class="flex items-center gap-2">
              <GenesisSwitch v-model="s.ai.useToken" size="sm" label="Token personnel" />
              <input v-if="s.ai.useToken" v-model="s.ai.token" type="password" autocomplete="off" placeholder="Clé API"
                class="min-w-0 flex-1 rounded-md border border-bg-neutral-genesis bg-bg-dark px-2 py-1 text-xs text-text outline-none focus:border-primary">
            </div>
            <div class="flex items-end gap-2">
              <textarea v-model="s.ai.prompt" rows="2" placeholder="Décrivez les tables à générer…"
                class="min-w-0 flex-1 resize-none rounded-lg border border-bg-neutral-genesis bg-bg-dark px-3 py-2 text-sm text-text outline-none focus:border-primary"
                @keydown="onKey" />
              <GenesisButton variant="primary" :disabled="!s.ai.prompt.trim() || s.ai.busy || !s.ai.models.length" @click="send">
                <template #leftIcon><IconBot color="var(--color-primary-text)" /></template>
              </GenesisButton>
            </div>
            <p class="text-right text-[10px] text-text-muted opacity-60">Entrée pour envoyer · Maj+Entrée : nouvelle ligne</p>
          </div>
        </div>
      </aside>
    </div>
  </div>
</template>
