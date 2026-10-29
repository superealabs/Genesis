<template>
    <div class="min-h-screen bg-bg-dark text-text transition-colors duration-300">
        <!-- Reste monté jusqu'à la fin de la vidéo + du fondu, au-dessus de l'app -->
        <GenesisLoader
            v-if="!loaderDone && apiStatus !== 'error'"
            :loading="apiStatus === 'loading'"
            overlay
            title="Démarrage de Genesis"
            message="Initialisation de l'API en cours..."
            @finished="loaderDone = true"
        />

        <GenesisError
            v-if="apiStatus === 'error'"
            title="Erreur de démarrage"
            :message="apiError"
            @close="resetApi"
        />

        <!-- L'app est montée dès que l'API est prête : le fondu du loader la révèle -->
        <div v-if="apiStatus === 'ready'">
            <RouterView />
        </div>
    </div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref, watch } from 'vue';
import { useApp } from '@/core/composables/useApp';
import GenesisLoader from '@genesis-labs/core/core/components/ui/feedback/GenesisLoader.vue';
import GenesisError from '@genesis-labs/core/core/components/ui/feedback/GenesisError.vue';

// La View ne parle QU'AU composable
const { apiStatus, apiError, resetApi, initialize, dispose } = useApp();

// false tant que le loader (vidéo + fondu) n'a pas terminé
const loaderDone = ref(false);

// Nouveau démarrage de l'API (ex. après une erreur) : le loader doit rejouer
watch(apiStatus, (status) => {
    if (status === 'loading') loaderDone.value = false;
});

onMounted(() => { initialize(); });
onUnmounted(() => { dispose(); });
</script>