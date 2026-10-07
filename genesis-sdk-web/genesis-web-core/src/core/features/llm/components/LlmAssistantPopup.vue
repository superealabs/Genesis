<template>
    <!-- ═══ PANNEAU LATÉRAL (habillage autour du contenu partagé) ═══ -->
    <div
        class="flex flex-col bg-bg-dark flex-shrink-0 h-full overflow-hidden transition-all duration-300 ease-in-out border-l border-secondary"
        :class="isOpen ? 'w-full' : 'w-0 border-none'"
    >
        <div class="flex flex-col w-full h-full">

            <!-- En-tête fixe -->
            <div class="flex items-center justify-between px-4 py-3 border-b border-secondary flex-shrink-0 bg-bg-dark/50 backdrop-blur-sm">
                <div class="flex items-center gap-2">
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="text-accent">
                        <path d="m12 3-1.912 5.813a2 2 0 0 1-1.275 1.275L3 12l5.813 1.912a2 2 0 0 1 1.275 1.275L12 21l1.912-5.813a2 2 0 0 1 1.275-1.275L21 12l-5.813-1.912a2 2 0 0 1-1.275-1.275L12 3Z"/>
                    </svg>
                    <span class="text-sm font-semibold text-text">Assistant IA</span>
                </div>
                <GenesisButtonIcon variant="tertiary" size="sm" @click="emit('update:isOpen', false)">
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M18 6 6 18"/><path d="m6 6 12 12"/></svg>
                </GenesisButtonIcon>
            </div>

            <!-- Contenu : chat + saisie (même composant que dans les panneaux) -->
            <LlmAssistantPanel class="flex-1 min-h-0" @generate="emit('generate', $event)" />
        </div>
    </div>
</template>

<script setup lang="ts">
import GenesisButtonIcon from '@genesis-labs/web-core/core/components/ui/actions/GenesisButtonIcon.vue';
import LlmAssistantPanel from './LlmAssistantPanel.vue';
import { useLlmChat, type LlmGeneratePayload } from '@genesis-labs/web-core/core/features/llm/composables/UseLlmChat';

defineProps<{ isOpen: boolean }>();

const emit = defineEmits<{
    'update:isOpen': [value: boolean];
    generate: [payload: LlmGeneratePayload];
}>();

// Fonction exposée pour que le parent puisse injecter la vraie réponse s'il gère l'API
const { appendAssistantResponse } = useLlmChat();
defineExpose({ appendAssistantResponse });
</script>