// webview-ui/src/core/composables/useApp.ts
import { useAppStore } from '../store/useApp.store';
import { useThemeStore } from '../store/useTheme.store';
import { appService } from '../services/app.service';

export function useApp() {
    const appStore = useAppStore();
    const themeStore = useThemeStore();

    function initialize() {
        // 1. Logique UI/DOM immédiate
        themeStore.detectVsCodeMode();
        
        // 2. Délégation au service pour la communication
        appService.init();
    }

    function dispose() {
        appService.dispose();
    }

    return {
        // Lecture du store (readonly)
        apiStatus: appStore.apiStatus,
        apiError: appStore.apiError,
        
        // Actions exposées à la vue
        resetApi: appStore.resetApi,
        initialize,
        dispose
    };
}