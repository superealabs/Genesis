<template>
    <div class="min-h-screen bg-bg text-text p-2 transition-colors duration-300">
        <GenesisLoader
            v-if="apiStatus === 'loading'"
            title="Démarrage de Genesis"
            message="Initialisation de l'API en cours..."
            :isClosable="false"
            :fullPage="true"
        />

        <GenesisError
            v-if="apiStatus === 'error'"
            title="Erreur de démarrage"
            :message="apiError"
            @close="resetApi"
        />
        
        <div v-if="apiStatus === 'ready'">
            <RouterView />
        </div>
    </div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted } from 'vue';
import { useApp } from '@/core/composables/useApp';
import GenesisLoader from '@/core/components/ui/feedback/GenesisLoader.vue';
import GenesisError from '@/core/components/ui/feedback/GenesisError.vue';

// La View ne parle QU'AU composable
const { apiStatus, apiError, resetApi, initialize, dispose } = useApp();

onMounted(() => {
    initialize();
});

onUnmounted(() => {
    dispose();
});
</script>