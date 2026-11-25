<script setup lang="ts">
import GenesisButton from '@genesis-labs/web-core/core/components/ui/actions/GenesisButton.vue'
import IconEdit from '@genesis-labs/web-core/core/components/ui/icons/IconEdit.vue'
import illus from '~/assets/illustrations/generation-summary.png'
import okImg from '@genesis-labs/web-core/assets/LOGO/Genesis/genie-happy.svg'

const { s, hasFront, hasLayout, isMvc, goTo, reset, download, formConfigPayload } = usePlayground()
const formSummary = computed(() => {
  const conf = formConfigPayload()
  const parts = Object.entries(conf).flatMap(([t, cols]) => Object.entries(cols).map(([c, v]) => `${t}.${c} → ${v.component}`))
  return parts.length ? parts.join(', ') : 'rendu par défaut'
})
const rows = computed(() => [
  { label: 'Backend', value: s.value.framework?.name, step: 1 },
  { label: 'Projet', value: `${s.value.project.projectName} · port ${s.value.project.projectPort} · ${s.value.project.buildTool} · ${s.value.project.languageVersion}`, step: 2 },
  { label: 'Destination', value: s.value.output === 'zip' ? `Téléchargement de ${s.value.project.projectName}.zip` : s.value.project.projectLocation, step: 2 },
  { label: 'Base de données', value: `${s.value.database?.name} (script, sans connexion)`, step: 3 },
  { label: 'Tables', value: [...s.value.selectedTables, ...s.value.selectedViews].join(', '), step: 4 },
  { label: 'Composants', value: s.value.components.join(', '), step: 4 },
  { label: 'Relations', value: s.value.relations.length ? s.value.relations.map(r => `${r.parentTable} → ${r.childTable}${r.hasForm ? ' (formulaire)' : ''}`).join(', ') : 'aucune', step: 5 },
  { label: 'Frontend', value: isMvc.value ? 'Vues intégrées (MVC)' : hasFront.value ? `${s.value.frontend?.name} · port ${s.value.frontPort}` : 'Sans frontend', step: 6 },
  ...(hasLayout.value ? [{
    label: 'Layout',
    value: [
      s.value.layout.navbarType === 'top' ? 'Barre supérieure' : 'Barre latérale',
      `couleurs ${s.value.layout.primaryColor} / ${s.value.layout.secondaryColor}`,
      `langues ${s.value.layout.selectedInterfaceLanguages.map(c => c.toUpperCase()).join(', ')}`,
      s.value.logoFile ? `logo ${s.value.logoFile.name}` : '',
      s.value.faviconFile ? `favicon ${s.value.faviconFile.name}` : ''
    ].filter(Boolean).join(' · '),
    step: 6
  }, { label: 'Formulaires', value: formSummary.value, step: 7 }] : [])
])
</script>

<template>
  <div>
    <template v-if="s.result">
      <div class="flex flex-col items-center gap-4 rounded-lg bg-bg-light p-8 text-center">
        <img :src="okImg" alt="" class="h-24 w-24">
        <h2 class="font-display text-2xl text-text">Votre projet est prêt</h2>
        <p class="text-sm text-text-muted">{{ s.result.message }}</p>
        <code class="rounded bg-bg-dark px-3 py-2 text-sm text-primary">{{ s.result.destination }}</code>
        <p class="text-xs text-text-muted opacity-70">Tables : {{ [...s.result.tables, ...s.result.views].join(', ') }}</p>
        <div class="flex gap-2">
          <GenesisButton v-if="s.downloadUrl" variant="primary" @click="download">Télécharger à nouveau</GenesisButton>
          <GenesisButton variant="accent" @click="reset">Créer un autre projet</GenesisButton>
        </div>
      </div>
    </template>
    <template v-else>
      <PgStepHeader title="Résumé" description="Vérifiez vos choix avant de générer le projet." :illustration="illus" />
      <dl class="divide-y divide-bg-neutral-genesis overflow-hidden rounded-lg border border-bg-neutral-genesis">
        <div v-for="r in rows" :key="r.label" class="flex items-center gap-4 p-3">
          <dt class="w-40 shrink-0 text-xs font-semibold uppercase tracking-wide text-text-muted opacity-70">{{ r.label }}</dt>
          <dd class="min-w-0 flex-1 break-words text-sm">{{ r.value || '—' }}</dd>
          <button type="button" class="rounded p-1 hover:bg-bg-light" title="Modifier" @click="goTo(r.step)">
            <IconEdit color="var(--color-text-muted)" />
          </button>
        </div>
      </dl>
    </template>
  </div>
</template>
