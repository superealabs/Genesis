<template>
    <div class="flex flex-col w-full h-full bg-bg-dark">

        <!-- ═══ Zone de Chat (Scrollable) ═══ -->
        <div
            ref="chatContainerRef"
            class="flex-1 min-h-0 overflow-y-auto p-4 space-y-4 scroll-smooth"
            @scroll.passive="onChatScroll"
        >
            <!-- État vide -->
            <div v-if="messages.length === 0" class="flex flex-col items-center justify-center h-full text-text-muted/50 space-y-2">
                <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                    <path d="m12 3-1.912 5.813a2 2 0 0 1-1.275 1.275L3 12l5.813 1.912a2 2 0 0 1 1.275 1.275L12 21l1.912-5.813a2 2 0 0 1 1.275-1.275L21 12l-5.813-1.912a2 2 0 0 1-1.275-1.275L12 3Z"/>
                </svg>
                <span class="text-sm">Posez votre première question...</span>
            </div>

            <!-- Boucle des messages -->
            <div
                v-for="msg in messages"
                :key="msg.id"
                class="flex flex-col"
                :class="msg.role === 'user' ? 'items-end' : 'items-start'"
            >
                <div
                    class="max-w-[90%] px-4 py-3 rounded-2xl text-sm leading-relaxed"
                    :class="msg.role === 'user'
                        ? 'bg-accent/10 text-accent border border-accent/20 rounded-tr-sm'
                        : 'bg-bg/50 text-text border border-secondary rounded-tl-sm'"
                >
                    <!-- Indicateur de chargement -->
                    <div v-if="msg.isLoading" class="flex items-center gap-2 text-text-muted italic">
                        <span class="relative flex h-2 w-2">
                            <span class="animate-ping absolute inline-flex h-full w-full rounded-full bg-accent opacity-75"></span>
                            <span class="relative inline-flex rounded-full h-2 w-2 bg-accent"></span>
                        </span>
                        Génération en cours...
                    </div>

                    <!-- Contenu du message (support basique des sauts de ligne) -->
                    <div v-else class="whitespace-pre-wrap">{{ msg.content }}</div>
                </div>
                <span class="text-[10px] text-text-muted/50 mt-1 px-1">
                    {{ msg.timestamp.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) }}
                </span>
            </div>
        </div>

        <!-- ═══ Zone de Saisie (fixe en bas) ═══ -->
        <div class="flex-shrink-0 p-4 border-t border-secondary bg-bg-dark/50 backdrop-blur-sm space-y-3">

            <!-- Options compactes -->
            <div class="flex items-center justify-between gap-2">
                <GenesisDropdown :match-trigger-width="true" trigger-variant="secondary" trigger-size="xs" class="w-32">
                    <template #trigger>
                        <span class="text-xs font-medium text-text-muted truncate">{{ selectedModelLabel }}</span>
                    </template>
                    <div class="py-1">
                        <button
                            v-for="model in LLM_MODELS"
                            :key="model.value"
                            type="button"
                            class="w-full text-left px-3 py-1.5 text-xs text-text hover:bg-[var(--color-hover-ghost)] transition-colors flex items-center justify-between"
                            @click="selectedModel = model.value"
                        >
                            <span>{{ model.label }}</span>
                            <svg v-if="selectedModel === model.value" xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" class="text-accent"><polyline points="20 6 9 17 4 12"></polyline></svg>
                        </button>
                    </div>
                </GenesisDropdown>

                <label class="flex items-center gap-1.5 cursor-pointer group">
                    <input type="checkbox" v-model="includeDbSchema" class="sr-only peer">
                    <div class="w-8 h-4 bg-secondary rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:rounded-full after:h-3 after:w-3 after:transition-all peer-checked:bg-accent relative"></div>
                    <span class="text-[10px] font-medium text-text-muted group-hover:text-text transition-colors">Schéma DB</span>
                </label>
            </div>

            <!-- Input + Bouton -->
            <div class="relative flex items-end gap-2">
                <textarea
                    v-model="prompt"
                    placeholder="Décrivez ce que vous voulez générer..."
                    rows="3"
                    class="flex-1 px-3 py-2.5 text-sm bg-bg-dark border border-secondary rounded-lg text-text placeholder:text-text-muted/50 resize-none outline-none focus:ring-1 focus:ring-accent/50 focus:border-accent transition-all"
                    @keydown.enter.exact.prevent="handleGenerate"
                />
                <GenesisButtonIcon
                    variant="primary"
                    size="md"
                    :disabled="!prompt.trim() || isGenerating"
                    class="mb-0.5"
                    @click="handleGenerate"
                >
                    <svg v-if="!isGenerating" xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m12 3-1.912 5.813a2 2 0 0 1-1.275 1.275L3 12l5.813 1.912a2 2 0 0 1 1.275 1.275L12 21l1.912-5.813a2 2 0 0 1 1.275-1.275L21 12l-5.813-1.912a2 2 0 0 1-1.275-1.275L12 3Z"/></svg>
                    <svg v-else class="animate-spin" xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg>
                </GenesisButtonIcon>
            </div>
            <div class="text-[10px] text-text-muted/50 text-right">Entrée pour envoyer</div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, watch, nextTick, onMounted, onBeforeUnmount } from 'vue';
import GenesisButtonIcon from '@genesis-labs/web-core/core/components/ui/actions/GenesisButtonIcon.vue';
import GenesisDropdown from '@genesis-labs/web-core/core/components/ui/dropdown/GenesisDropdown.vue';
import { useLlmChat, LLM_MODELS, type LlmGeneratePayload } from '@genesis-labs/web-core/core/features/llm/composables/UseLlmChat';
import { usePanelView } from '@genesis-labs/web-core/core/composables/ux/UseGenesisPanel';

const emit = defineEmits<{
    generate: [payload: LlmGeneratePayload];
}>();

// ═══ État du chat : PARTAGÉ entre tous les panneaux « Assistant IA » ═══
const { messages, isGenerating, prompt, selectedModel, selectedModelLabel, includeDbSchema, submit } = useLlmChat();

function handleGenerate() {
    const payload = submit();
    if (payload) emit('generate', payload);
}

// ═══ Point de vue PROPRE à ce panneau : position de défilement du chat ═══
const chatContainerRef = ref<HTMLElement | null>(null);
const { view, update } = usePanelView();
let frame = 0;

function onChatScroll() {
    cancelAnimationFrame(frame);
    frame = requestAnimationFrame(() => {
        const el = chatContainerRef.value;
        if (el) update({ extra: { chatScrollTop: el.scrollTop } });
    });
}

function restoreChatScroll() {
    const el = chatContainerRef.value;
    const saved = view.value.extra.chatScrollTop;
    // 'instant' : le conteneur est en scroll-smooth, on ne veut pas d'animation à la restauration
    if (el && typeof saved === 'number') el.scrollTo({ top: saved, behavior: 'instant' as ScrollBehavior });
}

onMounted(() => {
    restoreChatScroll();
    requestAnimationFrame(restoreChatScroll);
});

onBeforeUnmount(() => cancelAnimationFrame(frame));

// ═══ Auto-scroll vers le bas à chaque nouveau message ═══
watch(messages, () => {
    nextTick(() => {
        if (chatContainerRef.value) {
            chatContainerRef.value.scrollTop = chatContainerRef.value.scrollHeight;
        }
    });
}, { deep: true });
</script>