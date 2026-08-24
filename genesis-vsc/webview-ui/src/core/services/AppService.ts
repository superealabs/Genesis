// webview-ui/src/core/services/AppService.ts
import { VscodeService } from './VscodeService';
import { useAppStore } from '@/core/store/useApp.store';
import { useThemeStore } from '@/core/store/useTheme.store';

export class AppService extends VscodeService {
    private appStore = useAppStore();
    private themeStore = useThemeStore();
    private cleanups: (() => void)[] = [];

    /**
     * Initialise les écouteurs et envoie le signal de prêt
     */
    init(): void {
        // 1. OUTPUT : Écouter les réponses de l'extension
        this.cleanups.push(
            this.onMessage('init', (payload: any) => {
                this.themeStore.applyTheme(payload.theme.theme, payload.theme.colorMode);
                this.appStore.setApiReady();
            })
        );

        this.cleanups.push(
            this.onMessage('themeChanged', (payload: any) => {
                this.themeStore.applyTheme(payload.theme, payload.colorMode);
            })
        );

        this.cleanups.push(
            this.onMessage('apiError', (payload: any) => {
                this.appStore.setApiError(payload.message);
            })
        );

        // 2. INPUT : Signaler à l'extension que la webview est prête
        this.sendMessage('ready');
    }

    /**
     * Nettoyage des listeners (appelé auUnmounted de la vue)
     */
    dispose(): void {
        this.cleanups.forEach(cleanup => cleanup());
        this.cleanups = [];
    }
}

// Singleton
export const appService = new AppService();