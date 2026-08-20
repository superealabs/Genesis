// webview-ui/src/store/useAppStore.ts
import { ref } from 'vue';

// État global partagé (singleton)
const apiStatus = ref<'loading' | 'ready' | 'error' | 'idle'>('loading');
const apiError = ref('');

export function useAppStore() {
    function setApiReady() {
        apiStatus.value = 'ready';
        apiError.value = '';
    }

    function setApiError(message: string) {
        apiError.value = message;
        apiStatus.value = 'error';
    }

    function setApiLoading() {
        apiStatus.value = 'loading';
        apiError.value = '';
    }

    function resetApi() {
        apiStatus.value = 'idle';
        apiError.value = '';
    }

    return {
        // État (readonly depuis l'extérieur)
        apiStatus: apiStatus as Readonly<typeof apiStatus>,
        apiError: apiError as Readonly<typeof apiError>,
        
        // Actions
        setApiReady,
        setApiError,
        setApiLoading,
        resetApi
    };
}