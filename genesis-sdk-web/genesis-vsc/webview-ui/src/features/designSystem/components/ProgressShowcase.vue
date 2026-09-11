<template>
    <ShowcaseLayout title="Progress">
        
        <!-- ═══ Section 1 : Test des tailles ═══ -->
        <div class="space-y-2">
            <h3 class="text-text-muted text-sm">Sizes</h3>
            <div class="flex flex-wrap gap-3">
                <GenesisButton
                    v-for="size in sizes"
                    :key="size"
                    variant="secondary"
                    @click="openProgress(size)"
                >
                    {{ size }}
                </GenesisButton>
            </div>
        </div>

        <!-- ═══ Section 2 : Test des variants ═══ -->
        <div class="space-y-2">
            <h3 class="text-text-muted text-sm">Variants</h3>
            <div class="flex flex-wrap gap-3">
                <GenesisButton
                    v-for="v in variants"
                    :key="v"
                    variant="secondary"
                    @click="openVariant(v)"
                >
                    {{ v }}
                </GenesisButton>
            </div>
        </div>

        <!-- ═══ Section 3 : Test des modes ═══ -->
        <div class="space-y-2">
            <h3 class="text-text-muted text-sm">Modes</h3>
            <div class="flex flex-wrap gap-3">
                <GenesisButton
                    v-for="m in modes"
                    :key="m"
                    variant="secondary"
                    @click="openMode(m)"
                >
                    {{ m }}
                </GenesisButton>
            </div>
        </div>

        <!-- ═══ Section 4 : Démo animée ═══ -->
        <div class="space-y-2">
            <h3 class="text-text-muted text-sm">Simulation animée</h3>
            <GenesisButton @click="startSimulation">
                Lancer une simulation
            </GenesisButton>
        </div>

        <!-- ═══ Popup active ═══ -->
        <ProgressPopup
            v-if="isPopupOpen"
            
            :message="popupConfig.message"
            :value="currentValue"
            :max="popupConfig.max"
            :mode="popupConfig.mode"
            :variant="popupConfig.variant"
            :progressLabel="popupConfig.progressLabel"
            :size="popupConfig.size"
            :isClosable="true"
            @close="closePopup"
        />
    </ShowcaseLayout>
</template>

<script setup lang="ts">
import { ref, reactive, onUnmounted } from 'vue';
import ProgressPopup from '@/core/components/layouts/Popup/ProgressPopup.vue';
import GenesisButton from '@/core/components/ui/actions/GenesisButton.vue';
import ShowcaseLayout from '@/features/designSystem/components/layouts/ShowcaseLayout.vue';

// ═══ Types et constantes ═══
const sizes = ['sm', 'md', 'lg', 'xl', '2xl', '3xl', 'full'] as const;
const variants = ['default', 'success', 'warning', 'danger'] as const;
const modes = ['percent', 'value', 'both'] as const;

type Size = typeof sizes[number];
type Variant = typeof variants[number];
type Mode = typeof modes[number];

// ═══ État réactif ═══
const isPopupOpen = ref(false);
const currentValue = ref(0);
let simulationInterval: ReturnType<typeof setInterval> | null = null;

const popupConfig = reactive({
    title: 'Progression',
    message: 'Veuillez patienter...',
    max: 100,
    mode: 'percent' as Mode,
    variant: 'default' as Variant,
    progressLabel: undefined as string | undefined,
    size: 'md' as Size
});

// ═══ Fonctions d'ouverture ═══
function openProgress(size: Size) {
    resetConfig();
    popupConfig.size = size;
    popupConfig.title = `Taille : ${size}`;
    currentValue.value = 65;
    isPopupOpen.value = true;
}

function openVariant(variant: Variant) {
    resetConfig();
    popupConfig.variant = variant;
    popupConfig.title = `Variant : ${variant}`;
    popupConfig.progressLabel = `État : ${variant}`;
    currentValue.value = getVariantValue(variant);
    isPopupOpen.value = true;
}

function openMode(mode: Mode) {
    resetConfig();
    popupConfig.mode = mode;
    popupConfig.title = `Mode : ${mode}`;
    popupConfig.max = 200;
    currentValue.value = 120;
    isPopupOpen.value = true;
}

// ═══ Simulation animée ═══
function startSimulation() {
    resetConfig();
    popupConfig.title = 'Installation en cours...';
    popupConfig.message = 'Téléchargement des dépendances';
    popupConfig.progressLabel = 'Installation';
    currentValue.value = 0;
    isPopupOpen.value = true;

    simulationInterval = setInterval(() => {
        currentValue.value += 5;
        
        // Mise à jour du message selon la progression
        if (currentValue.value >= 100) {
            popupConfig.variant = 'success';
            popupConfig.title = 'Installation terminée !';
            popupConfig.message = 'Tout est prêt.';
            stopSimulation();
        } else if (currentValue.value >= 70) {
            popupConfig.message = 'Finalisation...';
        } else if (currentValue.value >= 40) {
            popupConfig.message = 'Configuration en cours...';
        }
    }, 300);
}

function stopSimulation() {
    if (simulationInterval) {
        clearInterval(simulationInterval);
        simulationInterval = null;
    }
}

// ═══ Helpers ═══
function resetConfig() {
    stopSimulation();
    popupConfig.title = 'Progression';
    popupConfig.message = 'Veuillez patienter...';
    popupConfig.max = 100;
    popupConfig.mode = 'percent';
    popupConfig.variant = 'default';
    popupConfig.progressLabel = undefined;
    popupConfig.size = 'md';
    currentValue.value = 0;
}

function getVariantValue(variant: Variant): number {
    switch (variant) {
        case 'success': return 100;
        case 'warning': return 60;
        case 'danger': return 25;
        default: return 50;
    }
}

function closePopup() {
    stopSimulation();
    isPopupOpen.value = false;
}

// ═══ Cleanup au démontage ═══
onUnmounted(() => {
    stopSimulation();
});
</script>