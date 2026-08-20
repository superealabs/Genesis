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
import { useVsCode } from '@/core/composables/useVsCode';
import GenesisLoader from '@/core/components/ui/feedback/GenesisLoader.vue';
import GenesisError from '@/core/components/ui/feedback/GenesisError.vue';
import { useAppStore } from '@/store/useApp.store';
import { useThemeStore } from '@/store/useTheme.store';

// Stores
const { apiStatus, apiError, setApiReady, setApiError, resetApi } = useAppStore();
const { detectVsCodeMode, applyTheme } = useThemeStore();

// Communication VS Code
const { send, onMessage } = useVsCode();

let cleanup: () => void;

onMounted(() => {
    // Détecter le mode initial
    detectVsCodeMode();

    // Écouter les messages de l'extension
    cleanup = onMessage((message) => {
        switch (message.type) {
            case 'init': {
                const payload = message.payload as { 
                    port: number; 
                    theme: { theme: string; colorMode: string } 
                };
                applyTheme(payload.theme.theme, payload.theme.colorMode);
                setApiReady();
                break;
            }
            
            case 'themeChanged': {
                const payload = message.payload as { 
                    theme: string; 
                    colorMode: string 
                };
                applyTheme(payload.theme, payload.colorMode);
                break;
            }
            
            case 'apiError': {
                const payload = message.payload as { message: string };
                setApiError(payload.message);
                break;
            }
        }
    });

    // Signaler qu'on est prêt
    send('ready');
});

onUnmounted(() => {
    cleanup?.();
});
</script>