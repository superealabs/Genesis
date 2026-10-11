import { ref, computed } from 'vue';

// ============================================================================
// TYPES
// ============================================================================
export interface ChatMessage {
    id: string;
    role: 'user' | 'assistant';
    content: string;
    timestamp: Date;
    isLoading?: boolean;
}

export interface LlmGeneratePayload {
    model: string;
    prompt: string;
    includeDbSchema: boolean;
    token: string;
}

export const LLM_MODELS = [
    { label: 'GPT-4o', value: 'gpt-4o' },
    { label: 'GPT-4o mini', value: 'gpt-4o-mini' },
    { label: 'Claude 3.5 Sonnet', value: 'claude-sonnet-3-5' },
] as const;

// ============================================================================
// ÉTAT PARTAGÉ
// ============================================================================
// Défini au niveau du module (et non dans la fonction) : toutes les instances du panneau
// « Assistant IA » lisent et écrivent le MÊME état. Si l'une charge, les autres chargent aussi ;
// ce qui est tapé dans l'une apparaît dans l'autre. Seul le point de vue (défilement) reste propre
// à chaque panneau.
const messages = ref<ChatMessage[]>([]);
const isGenerating = ref(false);
const prompt = ref('');
const selectedModel = ref<string>(LLM_MODELS[0].value);
const includeDbSchema = ref(false);
const personalToken = ref('');

/**
 * TODO : désactiver quand le vrai service LLM sera branché.
 * Simule une réponse après 1,5 s, comme le faisait l'ancien LlmAssistantPopup.
 */
const DEMO_SIMULATION = true;

let counter = 0;
const nextId = () => `msg-${Date.now().toString(36)}-${(++counter).toString(36)}`;

// ============================================================================
// COMPOSABLE
// ============================================================================
export function useLlmChat() {
    const selectedModelLabel = computed(
        () => LLM_MODELS.find(m => m.value === selectedModel.value)?.label ?? 'Modèle'
    );

    /**
     * Envoie le prompt courant : ajoute le message utilisateur et un message assistant « en cours ».
     * @returns la charge utile à transmettre au service de génération, ou null si rien à envoyer
     */
    function submit(): LlmGeneratePayload | null {
        const text = prompt.value;
        if (!text.trim() || isGenerating.value) return null;

        prompt.value = '';

        messages.value.push({ id: nextId(), role: 'user', content: text, timestamp: new Date() });

        const assistantId = nextId();
        messages.value.push({
            id: assistantId,
            role: 'assistant',
            content: '',
            timestamp: new Date(),
            isLoading: true
        });
        isGenerating.value = true;

        const payload: LlmGeneratePayload = {
            model: selectedModel.value,
            prompt: text,
            includeDbSchema: includeDbSchema.value,
            token: personalToken.value
        };

        if (DEMO_SIMULATION) {
            setTimeout(() => {
                const msg = messages.value.find(m => m.id === assistantId);
                if (msg) {
                    msg.content =
                        "Ceci est une réponse simulée de l'IA. Dans l'implémentation finale, ce texte sera remplacé par la réponse réelle de votre API LLM basée sur votre prompt : \n\n" + text;
                    msg.isLoading = false;
                }
                isGenerating.value = false;
            }, 1500);
        }

        return payload;
    }

    /** Injecte la vraie réponse (à appeler par la vue qui gère l'API) dans le dernier message en attente */
    function appendAssistantResponse(responseText: string) {
        const last = messages.value[messages.value.length - 1];
        if (last && last.role === 'assistant' && last.isLoading) {
            last.content = responseText;
            last.isLoading = false;
            isGenerating.value = false;
        }
    }

    function clear() {
        messages.value = [];
        isGenerating.value = false;
        prompt.value = '';
    }

    return {
        messages,
        isGenerating,
        prompt,
        selectedModel,
        selectedModelLabel,
        includeDbSchema,
        personalToken,
        submit,
        appendAssistantResponse,
        clear
    };
}