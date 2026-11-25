<script setup lang="ts">
// Playground : génération d'un projet SANS base de données (mode script de genesis-api).
// Interface : styles, composants et icônes de genesis-vsc (genesis-web-core).
import '~/assets/css/web-core.css'
import '~/assets/css/playground.css'
import StepperPopup from '@genesis-labs/web-core/core/components/layouts/Popup/StepperPopup.vue'
import IconPlus from '@genesis-labs/web-core/core/components/ui/icons/IconPlus.vue'
import { STEPS } from '~/composables/usePlayground'

useHead({ title: 'Playground · Genesis' })
const { theme } = useTheme()
// Variables du thème genesis-vsc : body.theme-genesis.genesis-dark / genesis-light
useHead({ bodyAttrs: { class: computed(() => `theme-genesis genesis-${theme.value}`) } })

const { s, next, previous, goTo, reset } = usePlayground()
const open = ref(false)
const visited = computed(() => new Set(s.value.visited))

async function start() {
  open.value = true
  await goTo(s.value.step)
}
function close() {
  open.value = false
  if (s.value.result) reset()
}
</script>

<template>
  <main id="contenu">
    <section class="duo-hero">
      <div class="duo-hero__inner">
        <div class="duo-hero__text">
          <span class="eyebrow">Playground</span>
          <h1>Générez un projet <span class="hl">sans base de données</span></h1>
          <p class="lead">Choisissez votre stack, collez un script SQL : Genesis lit vos tables et leurs relations, puis écrit le backend et le frontend.</p>
          <div class="duo-hero__cta">
            <button class="btn btn-primary btn-lg" type="button" @click="start">
              <IconPlus color="currentColor" /> Créer un nouveau projet
            </button>
          </div>
          <p class="muted" style="margin-top:12px;font-size:.85em">
            Nécessite genesis-api lancé en local (<code>java -jar genesis-api.jar</code>, port 8081).
          </p>
        </div>
      </div>
    </section>

    <ClientOnly>
      <StepperPopup v-if="open" title="Créer un nouveau projet" size="full" :draggable="false"
        content-class="overflow-y-auto p-1"
        :current-step="s.step" :total-steps="STEPS.length" :step-labels="[...STEPS]" :visited-steps="visited"
        :is-loading="s.loading" :hide-footer="!!s.result"
        @close="close" @previous="previous" @next="next" @go-to-step="goTo">
        <div v-if="s.error" class="mb-4 rounded-lg border border-red-500/60 bg-red-500/10 p-3 text-sm text-text" role="alert">
          {{ s.error }}
        </div>
        <PgStepFramework v-if="s.step === 1" />
        <PgStepProject v-else-if="s.step === 2" />
        <PgStepDatabase v-else-if="s.step === 3" />
        <PgStepScript v-else-if="s.step === 4" />
        <PgStepRelations v-else-if="s.step === 5" />
        <PgStepFrontend v-else-if="s.step === 6" />
        <PgStepForms v-else-if="s.step === 7" />
        <PgStepSummary v-else />
      </StepperPopup>
    </ClientOnly>
  </main>
</template>
