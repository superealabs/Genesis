<script setup lang="ts">
// Sélecteur de dossier (même rôle que la boîte « Choisir un dossier » de VS Code / IntelliJ)
import BaseFormPopup from '@genesis-labs/web-core/core/components/layouts/Popup/BaseFormPopup.vue'
import GenesisButton from '@genesis-labs/web-core/core/components/ui/actions/GenesisButton.vue'
import GenesisInput from '@genesis-labs/web-core/core/components/ui/inputs/GenesisInput.vue'
import IconFolder from '@genesis-labs/web-core/core/components/ui/icons/IconFolder.vue'
import IconFolderPlus from '@genesis-labs/web-core/core/components/ui/icons/IconFolderPlus.vue'
import IconChevronUp from '@genesis-labs/web-core/core/components/ui/icons/IconChevronUp.vue'
import IconHome from '@genesis-labs/web-core/core/components/ui/icons/IconHome.vue'
import { Z_INDEX } from '@genesis-labs/web-core/core/components/layouts/Popup/popup.types'
import { FolderService, type FolderList } from '~/services/Folder'

const props = defineProps<{ start?: string }>()
const emit = defineEmits<{ select: [path: string]; close: [] }>()

const list = ref<FolderList | null>(null)
const error = ref('')
const newName = ref('')
const creating = ref(false)

async function open(path?: string) {
  error.value = ''
  try { list.value = await FolderService.List(path) }
  catch (e: any) { error.value = e?.data?.statusMessage || e?.message || 'Erreur' }
}
async function create() {
  if (!list.value || !newName.value.trim()) return
  try {
    list.value = await FolderService.Create(list.value.path, newName.value)
    newName.value = ''; creating.value = false
  } catch (e: any) { error.value = e?.data?.statusMessage || e?.message || 'Erreur' }
}
onMounted(() => open(props.start).then(() => { if (error.value && props.start) open() }))
</script>

<template>
  <BaseFormPopup title="Choisir le dossier de destination" size="lg" :draggable="false" :z-index="Z_INDEX.modalStacked" @close="emit('close')">
    <div class="flex h-full min-h-0 flex-col gap-3">
      <!-- Chemin courant + raccourcis -->
      <div class="flex flex-wrap items-center gap-2">
        <GenesisButton variant="tertiary" size="sm" :disabled="!list?.parent" title="Dossier parent" @click="open(list!.parent!)">
          <template #leftIcon><IconChevronUp color="var(--color-text-muted)" /></template>
        </GenesisButton>
        <GenesisButton variant="tertiary" size="sm" title="Dossier personnel" @click="open(list?.home)">
          <template #leftIcon><IconHome color="var(--color-text-muted)" /></template>
        </GenesisButton>
        <GenesisButton v-for="r in list?.roots" :key="r.path" variant="neutral" size="sm" @click="open(r.path)">{{ r.name }}</GenesisButton>
        <code class="ml-auto min-w-0 truncate rounded bg-bg-light px-2 py-1 text-xs text-primary">{{ list?.path }}</code>
      </div>

      <p v-if="error" class="rounded border border-red-500/60 bg-red-500/10 p-2 text-xs text-text">{{ error }}</p>

      <!-- Sous-dossiers -->
      <div class="min-h-0 flex-1 overflow-y-auto rounded-lg border border-bg-neutral-genesis">
        <button v-for="f in list?.folders" :key="f.path" type="button"
          class="flex w-full items-center gap-2 px-3 py-2 text-left text-sm hover:bg-bg-light"
          @dblclick="open(f.path)" @click="open(f.path)">
          <IconFolder color="var(--color-accent)" /> <span class="truncate">{{ f.name }}</span>
        </button>
        <p v-if="list && !list.folders.length" class="p-3 text-xs text-text-muted opacity-70">Aucun sous-dossier.</p>
      </div>

      <!-- Nouveau dossier -->
      <div v-if="creating" class="flex items-center gap-2">
        <GenesisInput v-model="newName" placeholder="Nom du nouveau dossier" fill-width class="flex-1" @keyup.enter="create" />
        <GenesisButton variant="primary" @click="create">Créer</GenesisButton>
        <GenesisButton variant="tertiary" @click="creating = false">Annuler</GenesisButton>
      </div>

      <div class="flex items-center justify-between border-t border-bg-neutral-genesis pt-3">
        <GenesisButton v-if="!creating" variant="secondary" @click="creating = true">
          <template #leftIcon><IconFolderPlus color="var(--color-secondary)" /></template>
          Nouveau dossier
        </GenesisButton>
        <span v-else />
        <div class="flex gap-2">
          <GenesisButton variant="tertiary" @click="emit('close')">Annuler</GenesisButton>
          <GenesisButton variant="accent" :disabled="!list" @click="emit('select', list!.path)">Choisir ce dossier</GenesisButton>
        </div>
      </div>
    </div>
  </BaseFormPopup>
</template>
