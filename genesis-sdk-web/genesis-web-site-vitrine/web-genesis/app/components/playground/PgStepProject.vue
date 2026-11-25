<script setup lang="ts">
import GenesisInput from '@genesis-labs/web-core/core/components/ui/inputs/GenesisInput.vue'
import illus from '~/assets/illustrations/project-summary.png'

const { s } = usePlayground()
const picker = ref(false)
// En ligne, seul le téléchargement .zip est proposé (NUXT_PUBLIC_ALLOW_LOCAL_FOLDERS=false)
const allowFolders = useRuntimeConfig().public.allowLocalFolders !== false
if (!allowFolders) s.value.output = 'zip'
const opt = (l: string[]) => l.map(v => ({ label: v, value: v }))
const security = computed(() => [{ label: 'Aucune', value: '' }, ...opt(s.value.options.securityTypes)])
</script>

<template>
  <div>
    <PgStepHeader title="Configuration du projet" description="Nom, emplacement et options de votre projet." :illustration="illus" />
    <div class="grid gap-4 md:grid-cols-2">
      <GenesisInput v-model="s.project.projectName" label="Nom du projet" is-mandatory fill-width />
      <GenesisInput v-if="s.framework?.withGroupId !== false" v-model="s.project.groupId" label="Group ID / Namespace" fill-width />
      <div v-if="allowFolders" class="md:col-span-2">
        <span class="mb-1 block text-sm font-medium text-text-muted">Récupérer le projet</span>
        <div class="flex w-fit rounded-md bg-bg-light p-1">
          <button type="button" class="rounded px-3 py-1 text-xs font-medium" :class="s.output === 'zip' ? 'bg-primary text-primary-text' : 'text-text-muted'" @click="s.output = 'zip'">Télécharger un .zip</button>
          <button type="button" class="rounded px-3 py-1 text-xs font-medium" :class="s.output === 'folder' ? 'bg-primary text-primary-text' : 'text-text-muted'" @click="s.output = 'folder'">Écrire dans un dossier</button>
        </div>
      </div>
      <GenesisInput v-if="s.output === 'folder'" v-model="s.project.projectLocation" type="path" label="Dossier de destination" placeholder="Cliquez sur l'icône dossier pour choisir" is-mandatory fill-width class="md:col-span-2"
        @request-folder-path="picker = true" />
      <GenesisInput v-model="s.project.projectPort" label="Port backend" type="number" fill-width />
      <GenesisInput v-model="s.project.languageVersion" type="select" label="Version du langage" :options="opt(s.options.languageVersions)" fill-width />
      <GenesisInput v-model="s.project.frameworkVersion" type="select" label="Version du framework" :options="opt(s.options.frameworkVersions)" fill-width />
      <GenesisInput v-model="s.project.buildTool" type="select" label="Outil de build" :options="opt(s.options.buildTools)" fill-width />
      <GenesisInput v-model="s.project.securityType" type="select" label="Sécurité" :options="security" fill-width />
    </div>
    <Teleport to="body">
      <PgFolderPicker v-if="picker" :start="s.project.projectLocation" @close="picker = false"
        @select="(p) => { s.project.projectLocation = p; picker = false }" />
    </Teleport>
  </div>
</template>
