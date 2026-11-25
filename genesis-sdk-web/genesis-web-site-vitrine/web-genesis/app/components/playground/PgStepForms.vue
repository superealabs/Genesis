<script setup lang="ts">
// Étape « Form configuration » — même étape que FormConfigurationWizardStep du plugin IntelliJ :
// choisir comment chaque colonne est rendue dans les formulaires générés.
import GenesisInput from '@genesis-labs/web-core/core/components/ui/inputs/GenesisInput.vue'
import illus from '~/assets/illustrations/frontend.png'
import type { FormColumn } from '~/services/FormConfig'
import type { FormColumnState } from '~/composables/usePlayground'

const { s, hasLayout } = usePlayground()
const search = ref('')

// Libellés des composants (form-field-configuration.json)
const LABELS: Record<string, string> = {
  number: 'Nombre', range: 'Curseur (range)', quantity: 'Quantité', currency: 'Montant (devise)', map: 'Carte (latitude / longitude)',
  text: 'Texte', textarea: 'Zone de texte', ckeditor: 'Éditeur riche (CKEditor)', email: 'E-mail', phone: 'Téléphone',
  password: 'Mot de passe', color: 'Couleur', file: 'Fichier', 'json-editor': 'Éditeur JSON'
}
const label = (c: string) => LABELS[c] ?? c

const tables = computed(() => s.value.formTables.filter(t => t.table.toLowerCase().includes(search.value.trim().toLowerCase())))
const table = computed(() => s.value.formTables.find(t => t.table === s.value.formTable) ?? null)

function state(col: FormColumn): FormColumnState {
  const conf = (s.value.formConfig[s.value.formTable] ??= {})
  return (conf[col.column] ??= { component: '', values: {} })
}
function setComponent(col: FormColumn, component: string) {
  const st = state(col)
  st.component = component
  st.values = {}
  st.role = component === 'map' ? (/lon|lng/i.test(col.column) ? 'longitude' : 'latitude') : undefined
}
/** Paramètres à afficher (pour map, une seule coordonnée : l'autre est la colonne elle-même) */
function fields(st: FormColumnState) {
  const all = s.value.formCatalog?.components[st.component] ?? []
  if (st.component !== 'map') return all
  const other = st.role === 'latitude' ? 'longitude' : 'latitude'
  return all.filter(f => f.name === other || (f.name !== 'latitude' && f.name !== 'longitude'))
}
function toggle(st: FormColumnState, name: string, option: string) {
  const list = Array.isArray(st.values[name]) ? [...(st.values[name] as string[])] : []
  const i = list.indexOf(option)
  if (i >= 0) list.splice(i, 1)
  else list.push(option)
  st.values[name] = list
}
/** Colonne utilisée comme coordonnée par une carte d'une autre colonne (le plugin la masque) */
function usedBy(col: FormColumn) {
  const conf = s.value.formConfig[s.value.formTable] ?? {}
  for (const [other, st] of Object.entries(conf)) {
    if (other === col.column || st.component !== 'map') continue
    if (st.values.latitude === col.column || st.values.longitude === col.column) return other
  }
  return ''
}
const configuredCount = (t: string) => Object.values(s.value.formConfig[t] ?? {}).filter(c => c.component).length
</script>

<template>
  <div>
    <PgStepHeader title="Formulaires" description="Choisissez comment chaque colonne s'affiche dans les formulaires générés." :illustration="illus" />

    <p v-if="!hasLayout" class="rounded-lg bg-bg-light p-4 text-sm text-text-muted">
      Pas de frontend ni de vues MVC : il n'y a pas de formulaire à configurer.
    </p>

    <div v-else class="grid min-h-[420px] gap-4 md:grid-cols-[240px_minmax(0,1fr)]">
      <!-- TABLES -->
      <aside class="flex flex-col overflow-hidden rounded-lg border border-bg-neutral-genesis">
        <div class="flex items-center justify-between px-3 pt-3">
          <span class="text-sm font-semibold text-text">Tables</span>
          <span class="text-xs text-text-muted">{{ s.formTables.length }}</span>
        </div>
        <div class="p-3">
          <input v-model="search" type="search" placeholder="Rechercher une table…"
            class="w-full rounded-md border border-bg-neutral-genesis bg-bg-dark px-3 py-1.5 text-sm text-text outline-none focus:border-primary">
        </div>
        <ul class="flex-1 overflow-y-auto border-t border-bg-neutral-genesis py-1">
          <li v-for="t in tables" :key="t.table">
            <button type="button" class="flex w-full items-center gap-2 px-3 py-2 text-left text-sm"
              :class="t.table === s.formTable ? 'bg-primary text-primary-text' : 'text-text hover:bg-bg-light'"
              @click="s.formTable = t.table">
              <span class="min-w-0 flex-1 truncate">{{ t.table }}</span>
              <span v-if="configuredCount(t.table)" class="rounded px-1.5 text-[10px] font-bold"
                :class="t.table === s.formTable ? 'bg-primary-text text-primary' : 'bg-primary text-primary-text'">{{ configuredCount(t.table) }}</span>
            </button>
          </li>
          <li v-if="!tables.length" class="px-3 py-2 text-xs text-text-muted">Aucune table</li>
        </ul>
      </aside>

      <!-- COLONNES -->
      <section class="overflow-y-auto rounded-lg border border-bg-neutral-genesis p-4">
        <p v-if="!table" class="grid h-full place-items-center text-sm text-text-muted">Sélectionnez une table pour configurer ses colonnes</p>
        <template v-else>
          <p v-if="!table.columns.length" class="text-sm text-text-muted">Aucune colonne configurable dans cette table.</p>
          <p v-if="table.hiddenColumns" class="mb-3 text-xs text-text-muted">
            {{ table.hiddenColumns }} colonne(s) masquée(s) : clés étrangères ou types non configurables.
          </p>

          <div class="space-y-3">
            <article v-for="col in table.columns" :key="col.column" class="rounded-lg bg-bg-light p-4">
              <div class="mb-3 flex flex-wrap items-center gap-2">
                <span class="font-semibold text-text">{{ col.column }}</span>
                <span class="rounded bg-bg-dark px-1.5 py-0.5 text-[10px] font-bold uppercase text-text">{{ col.sqlType }}</span>
                <span class="text-xs text-text-muted">{{ col.category }}</span>
              </div>

              <p v-if="usedBy(col)" class="text-xs text-text-muted">Utilisée comme coordonnée par la carte de « {{ usedBy(col) }} ».</p>
              <template v-else>
                <GenesisInput :model-value="state(col).component" type="select" label="Rendu" size="lg" fill-width
                  :options="[{ label: 'Par défaut', value: '' }, ...col.components.map(c => ({ label: label(c), value: c }))]"
                  @update:model-value="setComponent(col, $event as string)" />

                <div v-if="state(col).component" class="mt-3 grid gap-3 md:grid-cols-2">
                  <!-- map : rôle de cette colonne -->
                  <div v-if="state(col).component === 'map'" class="md:col-span-2 flex items-center gap-2 text-sm">
                    <span class="text-text-muted">Cette colonne est la</span>
                    <div class="flex rounded-md bg-bg-dark p-1">
                      <button v-for="r in (['latitude', 'longitude'] as const)" :key="r" type="button"
                        class="rounded px-3 py-1 text-xs font-medium"
                        :class="state(col).role === r ? 'bg-primary text-primary-text' : 'text-text-muted'"
                        @click="state(col).role = r; state(col).values = {}">{{ r }}</button>
                    </div>
                  </div>

                  <template v-for="f in fields(state(col))" :key="f.name">
                    <GenesisInput v-if="f.type === 'number'" v-model="state(col).values[f.name] as string"
                      type="number" :label="f.name" size="lg" fill-width placeholder="—" />
                    <GenesisInput v-else-if="f.type === 'select'" v-model="state(col).values[f.name] as string"
                      type="select" :label="f.name" size="lg" fill-width placeholder="Sélectionner…"
                      :options="f.options.map(o => ({ label: o, value: o }))" />
                    <GenesisInput v-else-if="f.type === 'column'" v-model="state(col).values[f.name] as string"
                      type="select" :label="`Colonne ${f.name}`" size="lg" fill-width placeholder="Sélectionner…" is-mandatory
                      :options="col.coordinateColumns.map(o => ({ label: o, value: o }))" />
                    <div v-else-if="f.type === 'multi-select'" class="md:col-span-2">
                      <span class="mb-1 block text-sm font-medium text-muted">{{ f.name }}</span>
                      <div class="flex flex-wrap gap-1.5">
                        <button v-for="o in f.options" :key="o" type="button"
                          class="rounded border px-2 py-1 text-xs font-medium"
                          :class="(state(col).values[f.name] as string[] | undefined)?.includes(o)
                            ? 'border-primary bg-primary text-primary-text' : 'border-bg-neutral-genesis bg-bg-dark text-text hover:border-primary'"
                          @click="toggle(state(col), f.name, o)">{{ o }}</button>
                      </div>
                    </div>
                  </template>
                </div>
              </template>
            </article>
          </div>
        </template>
      </section>
    </div>
  </div>
</template>
