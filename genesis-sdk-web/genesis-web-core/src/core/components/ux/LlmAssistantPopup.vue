<template>
    <!-- ═══ PANNEAU LATÉRAL ═══ -->
    <div 
        class="flex flex-col bg-bg-dark flex-shrink-0 h-full overflow-hidden transition-all duration-300 ease-in-out border-l border-secondary"
        :class="isOpen ? 'w-full' : 'w-0 border-none'"
    >
        <div class="flex flex-col w-full h-full">
            
            <!-- 1. En-tête fixe -->
            <div class="flex items-center justify-between px-4 py-3 border-b border-secondary flex-shrink-0 bg-bg-dark/50 backdrop-blur-sm">
                <div class="flex items-center gap-2">
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="text-accent">
                        <path d="m12 3-1.912 5.813a2 2 0 0 1-1.275 1.275L3 12l5.813 1.912a2 2 0 0 1 1.275 1.275L12 21l1.912-5.813a2 2 0 0 1 1.275-1.275L21 12l-5.813-1.912a2 2 0 0 1-1.275-1.275L12 3Z"/>
                    </svg>
                    <span class="text-sm font-semibold text-text">Assistant IA</span>
                </div>
                <GenesisButtonIcon :variant="'tertiary'" size="sm" @click="emit('update:isOpen', false)">
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M18 6 6 18"/><path d="m6 6 12 12"/></svg>
                </GenesisButtonIcon>
            </div>

            <!-- 2. Zone de Chat (Scrollable) -->
            <div ref="chatContainerRef" class="flex-1 overflow-y-auto p-4 space-y-4 scroll-smooth">
                
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
                    </span if="msg.role === 'user'">
                </div>
            </div>

            <!-- 3. Zone de Saisie (Fixe en bas) -->
            <div class="flex-shrink-0 p-4 border-t border-secondary bg-bg-dark/50 backdrop-blur-sm space-y-3">
                
                <!-- Options compactes -->
                <div class="flex items-center justify-between gap-2">
                    <GenesisDropdown :match-trigger-width="true" trigger-variant="secondary" trigger-size="xs" class="w-32">
                        <template #trigger>
                            <span class="text-xs font-medium text-text-muted truncate">{{ selectedModelLabel }}</span>
                        </template>
                        <div class="py-1">
                            <button v-for="model in availableModels" :key="model.value" type="button" class="w-full text-left px-3 py-1.5 text-xs text-text hover:bg-[var(--color-hover-ghost)] transition-colors flex items-center justify-between" @click="selectedModel = model.value">
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
                        @click="handleGenerate"
                        class="mb-0.5"
                    >
                        <svg v-if="!isGenerating" xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m12 3-1.912 5.813a2 2 0 0 1-1.275 1.275L3 12l5.813 1.912a2 2 0 0 1 1.275 1.275L12 21l1.912-5.813a2 2 0 0 1 1.275-1.275L21 12l-5.813-1.912a2 2 0 0 1-1.275-1.275L12 3Z"/></svg>
                        <svg v-else class="animate-spin" xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg>
                    </GenesisButtonIcon>
                </div>
                <div class="text-[10px] text-text-muted/50 text-right">Entrée pour envoyer</div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick } from 'vue';
import GenesisButtonIcon from '@/core/components/ui/actions/GenesisButtonIcon.vue';
import GenesisDropdown from '@/core/components/ui/dropdown/GenesisDropdown.vue';

const props = defineProps<{ isOpen: boolean }>();
const emit = defineEmits<{ 
    'update:isOpen': [value: boolean];
    generate: [payload: { model: string; prompt: string; includeDbSchema: boolean; token: string }];
}>();

// ═══ État du Chat ═══
interface ChatMessage {
    id: string;
    role: 'user' | 'assistant';
    content: string;
    timestamp: Date;
    isLoading?: boolean;
}

const messages = ref<ChatMessage[]>([]);
const chatContainerRef = ref<HTMLElement | null>(null);
const isGenerating = ref(false);

const prompt = ref('');
const selectedModel = ref('gpt-4o');
const includeDbSchema = ref(false);
const personalToken = ref('');

const availableModels = [
    { label: 'GPT-4o', value: 'gpt-4o' },
    { label: 'GPT-4o mini', value: 'gpt-4o-mini' },
    { label: 'Claude 3.5 Sonnet', value: 'claude-sonnet-3-5' },
];

const selectedModelLabel = computed(() =>
    availableModels.find(m => m.value === selectedModel.value)?.label ?? 'Modèle'
);

// ═══ Auto-scroll vers le bas à chaque nouveau message ═══
watch(messages, () => {
    nextTick(() => {
        if (chatContainerRef.value) {
            chatContainerRef.value.scrollTop = chatContainerRef.value.scrollHeight;
        }
    });
}, { deep: true });

// ═══ Actions ═══
async function handleGenerate() {
    if (!prompt.value.trim() || isGenerating.value) return;

    const currentPrompt = prompt.value;
    prompt.value = ''; // Vider l'input immédiatement

    // 1. Ajouter le message utilisateur
    messages.value.push({
        id: Date.now().toString(),
        role: 'user',
        content: currentPrompt,
        timestamp: new Date()
    });

    // 2. Ajouter un message "assistant" en état de chargement
    const assistantMsgId = (Date.now() + 1).toString();
    messages.value.push({
        id: assistantMsgId,
        role: 'assistant',
        content: '',
        timestamp: new Date(),
        isLoading: true
    });

    isGenerating.value = true;

    // 3. Émettre l'événement au parent pour traiter la requête
    emit('generate', {
        model: selectedModel.value,
        prompt: currentPrompt,
        includeDbSchema: includeDbSchema.value,
        token: personalToken.value,
    });

    // NOTE : Dans une implémentation réelle, vous attendriez la réponse de l'API ici.
    // Pour la démo, nous simulons une réponse après 1.5 seconde.
    // Dans votre code final, remplacez ce setTimeout par l'appel réel à votre service LLM.
    setTimeout(() => {
        const msgIndex = messages.value.findIndex(m => m.id === assistantMsgId);
        if (msgIndex !== -1) {
            messages.value[msgIndex].content = "Ceci est une réponse simulée de l'IA. Dans l'implémentation finale, ce texte sera remplacé par la réponse réelle de votre API LLM basée sur votre prompt : \n\n" + currentPrompt;
            messages.value[msgIndex].isLoading = false;
        }
        isGenerating.value = false;
    }, 1500);
}

// Fonction exposée pour que le parent puisse injecter la vraie réponse s'il gère l'API
function appendAssistantResponse(responseText: string) {
    const lastMsg = messages.value[messages.value.length - 1];
    if (lastMsg && lastMsg.role === 'assistant' && lastMsg.isLoading) {
        lastMsg.content = responseText;
        lastMsg.isLoading = false;
        isGenerating.value = false;
    }
}

// Exposer la méthode au parent si besoin
defineExpose({ appendAssistantResponse });
</script>