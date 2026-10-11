<template>
  <GenesisPanelContainer
    class="h-full w-full"
    :panel="panel"
    :editors="editors"
  />
</template>

<script setup lang="ts">
import type { FileRequestPayload } from '@genesis-labs/shared-types';

// Panneaux
import GenesisPanelContainer from '@genesis-labs/web-core/core/components/ux/GenesisPanelContainer.vue';
import {
  useGenesisPanel,
  panelLeaf,
  panelSplit,
  type PanelEditorDefinition
} from '@genesis-labs/web-core/core/composables/ux/UseGenesisPanel.ts';

// Éditeurs disponibles dans les panneaux
import ScriptImportPanel from '../script/ScriptImportPanel.vue';
import ScriptCodePanel from '../script/ScriptCodePanel.vue';
import ScriptGuidePanel from '../script/ScriptGuidePanel.vue';
import LlmAssistantPanel from '@genesis-labs/web-core/core/features/llm/components/LlmAssistantPanel.vue';

// Icônes
import IconFolder from '@genesis-labs/web-core/core/components/ui/icons/IconFolder.vue';
import IconHelpCircle from '@genesis-labs/web-core/core/components/ui/icons/IconHelpCircle.vue';
import IconCode from '@genesis-labs/web-core/core/components/ui/icons/IconCode.vue';
import IconBot from '@genesis-labs/web-core/core/components/ui/icons/IconBot.vue';

// ============================================================================
// 1. EMITS
// ============================================================================
const emit = defineEmits<{
  'request-file-path': [payload: FileRequestPayload];
}>();

// ============================================================================
// 2. ACTIONS
// ============================================================================
function handleLlmGenerate(payload: {
  model: string;
  prompt: string;
  includeDbSchema: boolean;
  token: string;
}) {
  console.log('[ScriptConfigView] LLM generate payload:', payload);
  // TODO : appeler le service de génération LLM, puis injecter la réponse avec
  //        useLlmChat().appendAssistantResponse(texte) (et passer DEMO_SIMULATION à false).
}

// ============================================================================
// 3. REGISTRE DES ÉDITEURS
// ============================================================================
// Chaque entrée apparaît dans le menu déroulant de chaque panneau. L'état métier de chaque éditeur
// vit hors du composant (store du générateur, useLlmChat) : tous les panneaux d'un même type
// partagent donc exactement le même état, seul le point de vue (défilement, curseur) est propre à chacun.
const editors: PanelEditorDefinition[] = [
  {
    id: 'import',
    label: 'Import de script',
    icon: IconFolder,
    component: ScriptImportPanel,
    props: { onRequestFilePath: (payload: FileRequestPayload) => emit('request-file-path', payload) }
  },
  {
    id: 'code',
    label: 'Éditeur de code',
    icon: IconCode,
    component: ScriptCodePanel
  },
  {
    id: 'assistant',
    label: 'Assistant IA',
    icon: IconBot,
    component: LlmAssistantPanel,
    props: { onGenerate: handleLlmGenerate }
  },
  {
    id: 'guide',
    label: 'Guide',
    icon: IconHelpCircle,
    component: ScriptGuidePanel
  }
];

// ============================================================================
// 4. DISPOSITION
// ============================================================================
// Par défaut : à gauche l'import au-dessus du guide, à droite l'éditeur de code.
// L'assistant IA est un clic plus loin (menu déroulant d'un panneau, ou Alt + glisser depuis un bord).
const STORAGE_KEY = 'genesis:script-config-panels:v1';

const panel = useGenesisPanel({
  defaultEditor: 'code',
  initialLayout: panelSplit(
    'column', 0.4,
    panelSplit('row', 0.3, panelLeaf('import'), panelLeaf('guide')),
    panelLeaf('code')
  ),
  // La disposition choisie par l'utilisateur est mémorisée. Changez la clé (v2…) pour
  // forcer une nouvelle disposition par défaut après une modification de celle-ci.
  persist: {
    load: () => {
      try {
        const raw = localStorage.getItem(STORAGE_KEY);
        return raw ? JSON.parse(raw) : null;
      } catch {
        return null;
      }
    },
    save: (layout) => {
      try {
        localStorage.setItem(STORAGE_KEY, JSON.stringify(layout));
      } catch {
        /* stockage indisponible : on ignore */
      }
    }
  }
});
</script>