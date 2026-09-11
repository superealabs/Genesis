<template>
    <div class="relative flex h-full w-full overflow-hidden">

        <!-- ═══ 1. PANNEAU GAUCHE : Assistant IA ═══ -->
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

        <!-- ═══ DIVISEUR 1 (Redimensionnable) ═══ -->
        <div 
            v-if="isLlmOpen" 
            class="relative flex-shrink-0 w-1 cursor-col-resize group flex justify-center"
            @mousedown="startResizeLlm"
        >
            <!-- Ligne visuelle de 1px au centre, qui s'épaissit au survol -->
            <div class="w-px h-full bg-secondary group-hover:bg-accent/50 group-hover:w-1 transition-all duration-200" />
        </div>

        <!-- ═══ 2. PANNEAU CENTRAL : Inputs Principaux ═══ -->
        <!-- flex-1 permet à ce panneau d'absorber tout l'espace restant entre les deux panneaux redimensionnables -->
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
                    :variant="'secondary'"
                >
                    <template #right>
                        <GenesisButtonIcon :variant="'tertiary'" @click="handleSelectScriptPath">
                            <IconFolder />
                        </GenesisButtonIcon>
                    </template>
                </GenesisInput>

                <div class="flex-1" />

                <GenesisButtonIcon
                    variant="secondary"
                    size="md"
                    @click="toggleBothPanels"
                    :title="(isLlmOpen && isPreviewOpen) ? 'Fermer les deux panneaux' : 'Ouvrir les deux panneaux (Split View)'"
                >
                    <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
                        <line x1="9" y1="3" x2="9" y2="21"></line>
                        <line x1="15" y1="3" x2="15" y2="21"></line>
                    </svg>
                </GenesisButtonIcon>
            </div>
        </div>

        <!-- ═══ DIVISEUR 2 (Redimensionnable) ═══ -->
        <div 
            v-if="isPreviewOpen" 
            class="relative flex-shrink-0 w-1 cursor-col-resize group flex justify-center"
            @mousedown="startResizeCode"
        >
            <div class="w-px h-full bg-secondary group-hover:bg-accent/50 group-hover:w-1 transition-all duration-200" />
        </div>

        <!-- ═══ 3. PANNEAU DROIT : CodeMirror ═══ -->
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
import GenesisIdeCm from '@/core/components/layouts/ide/GenesisIdeCm.vue';
import GenesisInput from '@/core/components/ui/inputs/GenesisInput.vue';
import GenesisButtonIcon from '@/core/components/ui/actions/GenesisButtonIcon.vue';
import IconFolder from '@/core/components/ui/icons/IconFolder.vue';
import IconX from '@/core/components/ui/icons/IconX.vue';
import LlmAssistantPopup from '@/core/components/ux/LlmAssistantPopup.vue';
import { useGenerator } from '../../composables/useGenerator';
import { FileRequestPayload } from '../../manifest';
// 1. Import du nouveau composable
import { usePanelResizer } from '@/core/composables/ux/usePanelResizer';

const { updateScript, stepperData } = useGenerator();

const emit = defineEmits<{
    'request-file-path': [payload: FileRequestPayload];
}>();

const isPreviewOpen = ref(false);
const isLlmOpen = ref(false);

// 2. Initialisation des resizeurs
// Panneau de gauche : grandit quand on tire vers la droite (invertDirection = false)
const { width: llmWidth, isResizing: isResizingLlm, startResize: startResizeLlm } = usePanelResizer(380, 250, 800, false);

// Panneau de droite : rétrécit quand on tire vers la droite (invertDirection = true)
const { width: codeWidth, isResizing: isResizingCode, startResize: startResizeCode } = usePanelResizer(600, 400, 2000, true);

const scriptPath = computed({
    get: () => stepperData.value.script.path,
    set: (val) => updateScript('path', val),
});

const scriptContent = computed({
    get: () => stepperData.value.script.content,
    set: (val) => updateScript('content', val),
});

const scriptFileName = computed(() => {
    if (!scriptPath.value) return '';
    return scriptPath.value.split(/[\\/]/).pop() ?? scriptPath.value;
});

function togglePreview() {
    isPreviewOpen.value = !isPreviewOpen.value;
}

function handleSelectScriptPath() {
    emit('request-file-path', { field: 'script', extensions: ['sql'] });
}

const isDark = computed(() =>
    document.body.classList.contains('vscode-dark') ||
    document.body.classList.contains('genesis-dark')
);

function toggleBothPanels() {
    if (isLlmOpen.value && isPreviewOpen.value) {
        // Si les deux sont déjà ouverts, on les ferme tous les deux
        isLlmOpen.value = false;
        isPreviewOpen.value = false;
    } else {
        // Si au moins l'un est fermé, on les ouvre tous les deux
        isLlmOpen.value = true;
        isPreviewOpen.value = true;
    }
}

function handleLlmGenerate(payload: {
    model: string;
    prompt: string;
    includeDbSchema: boolean;
    token: string;
}) {
    console.log('[ScriptConfigView] LLM generate payload:', payload);
}
</script>