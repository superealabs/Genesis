<template>
  <div class="relative flex h-full w-full overflow-hidden">

    <!-- 1. PANNEAU GAUCHE : Assistant IA -->
    <div 
      class="flex flex-col bg-bg-dark flex-shrink-0 h-full overflow-hidden"
      :class="[isLlmOpen ? '' : 'w-0', !isResizingLlm ? 'transition-all duration-300 ease-in-out' : '']"
      :style="isLlmOpen ? { width: `${llmWidth}px` } : {}"
    >
      <LlmAssistantPopup 
        :is-open="isLlmOpen" 
        @update:is-open="isLlmOpen = $event"
        @generate="handleLlmGenerate" 
      />
    </div>

    <!-- 2. DIVISEUR GAUCHE (Redimensionnable) -->
    <div 
      v-if="isLlmOpen" 
      class="relative flex-shrink-0 w-1 cursor-col-resize group flex justify-center"
      @mousedown="startResizeLlm"
    >
      <div class="w-px h-full bg-secondary group-hover:bg-accent/50 group-hover:w-1 transition-all duration-200" />
    </div>

    <!-- 3. PANNEAU CENTRAL : Inputs Principaux -->
    <div class="flex flex-col flex-1 min-w-0 h-full relative">
      <div class="flex flex-col gap-6 p-6 overflow-y-auto h-full">
        
        <div class="flex items-center justify-between flex-shrink-0">
          <h3 class="text-lg font-semibold text-text">Import de Script</h3>
        </div>

        <GenesisInput
          v-model="scriptPath"
          label="Chemin du script"
          placeholder="/chemin/vers/script.sql"
          fill-width
          variant="secondary"
        >
          <template #right>
            <GenesisButtonIcon variant="tertiary" @click="handleSelectScriptPath">
              <IconFolder />
            </GenesisButtonIcon>
          </template>
        </GenesisInput>

        <div class="flex-1" />

        <GenesisButtonIcon
          variant="secondary"
          size="md"
          :title="(isLlmOpen && isPreviewOpen) ? 'Fermer les deux panneaux' : 'Ouvrir les deux panneaux (Split View)'"
          @click="toggleBothPanels"
        >
          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
            <line x1="9" y1="3" x2="9" y2="21"></line>
            <line x1="15" y1="3" x2="15" y2="21"></line>
          </svg>
        </GenesisButtonIcon>
      </div>
    </div>

    <!-- 4. DIVISEUR DROIT (Redimensionnable) -->
    <div 
      v-if="isPreviewOpen" 
      class="relative flex-shrink-0 w-1 cursor-col-resize group flex justify-center"
      @mousedown="startResizeCode"
    >
      <div class="w-px h-full bg-secondary group-hover:bg-accent/50 group-hover:w-1 transition-all duration-200" />
    </div>

    <!-- 5. PANNEAU DROIT : Éditeur de Code -->
    <div 
      class="flex flex-col h-full overflow-hidden flex-shrink-0"
      :class="[isPreviewOpen ? '' : 'w-0', !isResizingCode ? 'transition-all duration-300 ease-in-out' : '']"
      :style="isPreviewOpen ? { width: `${codeWidth}px` } : {}"
    >
      <GenesisIdeCm
        v-model="scriptContent"
        language="sql"
        :filename="scriptFileName"
        :show-line-numbers="true"
        :is-dark="isDark"
        class="flex-1 min-w-0 min-h-0"
      >
        <template #actions>
          <GenesisButtonIcon variant="tertiary" size="sm" @click="togglePreview">
            <IconX />
          </GenesisButtonIcon>
        </template>
      </GenesisIdeCm>
    </div>

  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import type { FileRequestPayload } from '@genesis-labs/shared-types';

// Composables
import { useGenerator } from '@genesis-labs/web-core/features/generator/composables/useGenerator';
import { usePanelResizer } from '@genesis-labs/web-core/core/composables/ux/usePanelResizer';

// UI Components
import GenesisIdeCm from '@genesis-labs/web-core/core/components/layouts/ide/GenesisIdeCm.vue';
import GenesisInput from '@genesis-labs/web-core/core/components/ui/inputs/GenesisInput.vue';
import GenesisButtonIcon from '@genesis-labs/web-core/core/components/ui/actions/GenesisButtonIcon.vue';
import LlmAssistantPopup from '@genesis-labs/web-core/core/components/ux/LlmAssistantPopup.vue';

// Icons
import IconFolder from '@genesis-labs/web-core/core/components/ui/icons/IconFolder.vue';
import IconX from '@genesis-labs/web-core/core/components/ui/icons/IconX.vue';

// ============================================================================
// 1. EMITS
// ============================================================================
const emit = defineEmits<{
  'request-file-path': [payload: FileRequestPayload];
}>();

// ============================================================================
// 2. COMPOSABLES & ÉTAT LOCAL
// ============================================================================
const { updateScript, stepperData } = useGenerator();

const isLlmOpen = ref(false);
const isPreviewOpen = ref(false);

/**
 * Gestionnaire de redimensionnement pour le panneau de gauche (LLM).
 * invertDirection = false : La largeur augmente quand on tire le diviseur vers la droite.
 */
const { 
  width: llmWidth, 
  isResizing: isResizingLlm, 
  startResize: startResizeLlm 
} = usePanelResizer(380, 250, 800, false);

/**
 * Gestionnaire de redimensionnement pour le panneau de droite (Code).
 * invertDirection = true : La largeur diminue quand on tire le diviseur vers la droite 
 * (car le panneau est ancré à droite).
 */
const { 
  width: codeWidth, 
  isResizing: isResizingCode, 
  startResize: startResizeCode 
} = usePanelResizer(600, 400, 2000, true);

// ============================================================================
// 3. COMPUTEDS (Liaison réactive avec le store)
// ============================================================================

/**
 * Liaison bidirectionnelle avec le store pour le chemin du script.
 * L'utilisation de get/set permet de mettre à jour le store directement via v-model 
 * dans le template, sans fonction intermédiaire.
 */
const scriptPath = computed({
  get: () => stepperData.value.script.path,
  set: (val) => updateScript('path', val),
});

/**
 * Liaison bidirectionnelle pour le contenu du script.
 */
const scriptContent = computed({
  get: () => stepperData.value.script.content,
  set: (val) => updateScript('content', val),
});

const scriptFileName = computed(() => {
  if (!scriptPath.value) return '';
  return scriptPath.value.split(/[\\/]/).pop() ?? scriptPath.value;
});

const isDark = computed(() =>
  document.body.classList.contains('vscode-dark') ||
  document.body.classList.contains('genesis-dark')
);

// ============================================================================
// 4. ACTIONS
// ============================================================================

function togglePreview() {
  isPreviewOpen.value = !isPreviewOpen.value;
}

function handleSelectScriptPath() {
  emit('request-file-path', { field: 'script', extensions: ['sql'] });
}

/**
 * Bascule l'état d'ouverture des deux panneaux latéraux simultanément.
 * Si les deux sont ouverts, ils sont fermés. Sinon, ils sont tous les deux ouverts.
 */
function toggleBothPanels() {
  const shouldOpen = !(isLlmOpen.value && isPreviewOpen.value);
  isLlmOpen.value = shouldOpen;
  isPreviewOpen.value = shouldOpen;
}

function handleLlmGenerate(payload: {
  model: string;
  prompt: string;
  includeDbSchema: boolean;
  token: string;
}) {
  console.log('[ScriptConfigView] LLM generate payload:', payload);
  // TODO: Implémenter l'appel au service de génération LLM ici
}
</script>