<template>
    <div class="relative flex h-full overflow-hidden">

        <!-- ═══ PANNEAU GAUCHE : Inputs ═══ -->
        <div
            class="flex flex-col gap-6 p-6 overflow-y-auto transition-all duration-300"
            :class="isPreviewOpen ? 'w-1/2' : 'w-full'"
        >
            <h3 class="text-lg font-semibold text-text">Import de Script</h3>

            <!-- Input chemin du script -->
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

            <!-- Spacer -->
            <div class="flex-1" />

            <!-- Toggle prévisualisation -->
            <button
                type="button"
                class="flex items-center gap-2 text-xs text-text-muted hover:text-text transition-colors self-start"
                @click="togglePreview"
            >
                <IconChevronRight
                    class="transition-transform duration-300"
                    :class="{ 'rotate-180': isPreviewOpen }"
                />
                {{ isPreviewOpen ? 'Masquer le script' : 'Prévisualiser le script' }}
            </button>
        </div>

        <!-- ═══ DIVISEUR ═══ -->
        <div v-if="isPreviewOpen" class="w-px bg-secondary flex-shrink-0" />

        <!-- ═══ PANNEAU DROIT : CodeMirror ═══ -->
        <transition name="preview-slide">
            <div
                v-if="isPreviewOpen"
                class="flex flex-col flex-1 min-w-0 overflow-hidden"
            >
                <!-- Header panneau droit -->
                <div class="flex items-center justify-between px-4 py-3 border-b border-secondary flex-shrink-0">
                    <span class="text-xs font-medium text-text-muted">
                        Contenu du script
                        <span v-if="scriptPath" class="text-accent ml-1">{{ scriptFileName }}</span>
                    </span>
                    <GenesisButtonIcon variant="tertiary" size="sm" @click="togglePreview">
                        <IconX />
                    </GenesisButtonIcon>
                </div>

                <!-- Éditeur CodeMirror -->
                <div style="flex: 1; height: 0; overflow: hidden;">
                    <codemirror
                        v-model="scriptContent"
                        :extensions="cmExtensions"
                        :autofocus="false"
                        :indent-with-tab="true"
                        :tab-size="2"
                        style="height: 100%;"
                    />
                </div>
            </div>
        </transition>

        <!-- ═══ BOUTON FLOTTANT : Assistant IA ═══ -->
        <div class="absolute bottom-4 left-4 z-10">
            <LlmAssistantPopup @generate="handleLlmGenerate" />
        </div>

    </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { Codemirror } from 'vue-codemirror';
import { sql } from '@codemirror/lang-sql';
import { oneDark } from '@codemirror/theme-one-dark';
import { EditorView } from '@codemirror/view';

import GenesisInput from '@/core/components/ui/inputs/GenesisInput.vue';
import GenesisButtonIcon from '@/core/components/ui/actions/GenesisButtonIcon.vue';
import IconFolder from '@/core/components/ui/icons/IconFolder.vue';
import IconChevronRight from '@/core/components/ui/icons/IconChevronRight.vue';
import IconX from '@/core/components/ui/icons/IconX.vue';
import LlmAssistantPopup from '@/core/components/ux/LlmAssistantPopup.vue';
import { useGenerator } from '../composables/useGenerator';

const { selectScriptPath, updateScript, stepperData } = useGenerator();

// ═══ State local UI ═══
const isPreviewOpen = ref(false);

// ═══ State bindé au store ═══
const scriptPath = computed({
    get: () => stepperData.value.script.path,
    set: (val) => updateScript('path', val),
});

const scriptContent = computed({
    get: () => stepperData.value.script.content,
    set: (val) => updateScript('content', val),
});

// ═══ Computed ═══
const scriptFileName = computed(() => {
    if (!scriptPath.value) return '';
    return scriptPath.value.split(/[\\/]/).pop() ?? scriptPath.value;
});

// ═══ Actions ═══
function togglePreview() {
    isPreviewOpen.value = !isPreviewOpen.value;
}

function handleSelectScriptPath() {
    selectScriptPath();
}

// ═══ Extensions CodeMirror ═══
const cmExtensions = computed(() => [
    sql(),
    oneDark,
    EditorView.theme({
        '&': {
            height: '100%',
            backgroundColor: 'transparent',
            fontSize: '13px',
        },
        '.cm-scroller': {
            fontFamily: "'JetBrains Mono', 'Fira Code', monospace",
            overflow: 'auto',
        },
        '.cm-content': {
            caretColor: 'var(--color-accent)',
        },
    }),
    EditorView.lineWrapping,
]);

// ═══ Handler LLM ═══
function handleLlmGenerate(payload: {
    model: string;
    prompt: string;
    includeDbSchema: boolean;
    token: string;
}) {
    console.log('[ScriptConfigView] LLM generate payload:', payload);
}
</script>

<style scoped>
.preview-slide-enter-active,
.preview-slide-leave-active {
    transition: opacity 0.25s ease, width 0.3s ease;
}
.preview-slide-enter-from,
.preview-slide-leave-to {
    opacity: 0;
    width: 0;
}
</style>