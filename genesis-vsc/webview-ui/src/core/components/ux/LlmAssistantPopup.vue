<template>
    <div class="relative" ref="triggerRef">

        <!-- Bouton flottant icône seule -->
        <GenesisButtonIcon
            variant="secondary"
            size="md"
            @click="togglePopup"
        >
            <IconHelpCircle />
        </GenesisButtonIcon>

        <!-- Popup ancrée au bouton, s'ouvre vers le haut -->
        <BasePopup
            :show="isOpen"
            :anchor="(triggerRef as HTMLElement)"
            position="top-left"
            :offset="8"
            :close-on-outside-click="true"
            :close-on-escape="true"
            @close="isOpen = false"
        >
            <div class="flex flex-col gap-3 p-4 w-80">

                <!-- En-tête -->
                <div class="flex items-center justify-between">
                    <span class="text-sm font-semibold text-text">Assistant IA</span>
                    <GenesisButtonIcon variant="tertiary" size="sm" @click="isOpen = false">
                        <IconX />
                    </GenesisButtonIcon>
                </div>

                <div class="w-full h-px bg-secondary" />

                <!-- Dropdown modèle -->
                <div class="flex flex-col gap-1">
                    <label class="text-xs font-medium text-text-muted">Modèle</label>
                    <GenesisDropdown
                        :match-trigger-width="true"
                        trigger-variant="secondary"
                        trigger-size="sm"
                        fill-width
                    >
                        <template #trigger>
                            <span class="text-sm">{{ selectedModelLabel }}</span>
                        </template>
                        <div class="py-1">
                            <button
                                v-for="model in availableModels"
                                :key="model.value"
                                type="button"
                                class="w-full text-left px-3 py-1.5 text-sm text-text hover:bg-[var(--color-hover-ghost)] transition-colors flex items-center justify-between"
                                @click="selectedModel = model.value"
                            >
                                <span>{{ model.label }}</span>
                                <span v-if="selectedModel === model.value" class="text-accent text-xs">✓</span>
                            </button>
                        </div>
                    </GenesisDropdown>
                </div>

                <!-- Toggle DB Schema -->
                <GenesisInput
                    v-model="includeDbSchema"
                    label="Inclure le schéma DB"
                    type="boolean"
                    one-line
                />

                <!-- Token personnel -->
                <GenesisInput
                    v-model="personalToken"
                    label="Token personnel"
                    type="password"
                    placeholder="sk-••••••••••••"
                    fill-width
                />

                <!-- Zone de prompt -->
                <div class="flex flex-col gap-1">
                    <label class="text-xs font-medium text-text-muted">Prompt</label>
                    <textarea
                        v-model="prompt"
                        placeholder="Décris ce que tu veux générer..."
                        rows="4"
                        class="w-full px-3 py-2 text-sm bg-transparent border border-secondary rounded text-text placeholder:text-muted resize-none outline-none focus:ring-1 focus:ring-accent focus:border-accent transition-all"
                    />
                </div>

                <!-- Bouton Generate -->
                <GenesisButton
                    variant="primary"
                    size="sm"
                    fill-width
                    :disabled="!prompt.trim()"
                    @click="handleGenerate"
                >
                    Générer
                </GenesisButton>
            </div>
        </BasePopup>
    </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import BasePopup from '@/core/components/layouts/Popup/BasePopup.vue';
import GenesisButton from '@/core/components/ui/actions/GenesisButton.vue';
import GenesisButtonIcon from '@/core/components/ui/actions/GenesisButtonIcon.vue';
import GenesisDropdown from '@/core/components/ui/dropdown/GenesisDropdown.vue';
import GenesisInput from '@/core/components/ui/inputs/GenesisInput.vue';
import IconHelpCircle from '@/core/components/ui/icons/IconHelpCircle.vue';
import IconX from '@/core/components/ui/icons/IconX.vue';

const emit = defineEmits<{
    generate: [payload: { model: string; prompt: string; includeDbSchema: boolean; token: string }];
}>();

// ═══ State ═══
const triggerRef = ref<HTMLElement | null>(null);
const isOpen = ref(false);

const selectedModel = ref('gpt-4o');
const includeDbSchema = ref(false);
const personalToken = ref('');
const prompt = ref('');

// ═══ Modèles disponibles ═══
const availableModels = [
    { label: 'GPT-4o', value: 'gpt-4o' },
    { label: 'GPT-4o mini', value: 'gpt-4o-mini' },
    { label: 'Claude Sonnet 4.5', value: 'claude-sonnet-4-5' },
    { label: 'Claude Haiku 4.5', value: 'claude-haiku-4-5' },
    { label: 'Gemini 1.5 Pro', value: 'gemini-1.5-pro' },
];

const selectedModelLabel = computed(() =>
    availableModels.find(m => m.value === selectedModel.value)?.label ?? 'Sélectionner...'
);

// ═══ Actions ═══
function togglePopup() {
    isOpen.value = !isOpen.value;
}

function handleGenerate() {
    if (!prompt.value.trim()) return;
    emit('generate', {
        model: selectedModel.value,
        prompt: prompt.value,
        includeDbSchema: includeDbSchema.value,
        token: personalToken.value,
    });
    isOpen.value = false;
}
</script>