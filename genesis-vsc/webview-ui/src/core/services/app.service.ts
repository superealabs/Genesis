import { VscodeService } from './vscode.service';
import { useAppStore } from '@/core/store/useApp.store';
import { useThemeStore } from '@/core/store/useTheme.store';

export class AppService extends VscodeService {
    private appStore = useAppStore();
    private themeStore = useThemeStore();
    private cleanups: (() => void)[] = [];
    
    // NOUVEAU : Pour calculer la durée minimale de 3 secondes
    private loadingStartTime: number = 0;

    init(): void {
        // 1. OUTPUT : Écouter les réponses de l'extension
        this.cleanups.push(
            this.onMessage('init', (payload: any) => {
                this.themeStore.applyTheme(payload.theme.theme, payload.theme.colorMode);
                
                // RÈGLE DES 3 SECONDES :
                const elapsed = Date.now() - this.loadingStartTime;
                const minLoadingTime = 3000; // 3 secondes
                const delay = Math.max(0, minLoadingTime - elapsed);

                setTimeout(() => {
                    // Si on veut être très précis, on pourrait passer payload.isOffline au store
                    // pour afficher un petit badge "Mode Hors Ligne", mais ce n'est pas obligatoire.
                    this.appStore.setApiReady();
                }, delay);
            })
        );

        this.cleanups.push(
            this.onMessage('themeChanged', (payload: any) => {
                this.themeStore.applyTheme(payload.theme, payload.colorMode);
            })
        );

        this.cleanups.push(
            this.onMessage('apiError', (payload: any) => {
                // En cas d'erreur critique, on annule le délai et on affiche l'erreur immédiatement
                this.appStore.setApiError(payload.message);
            })
        );

        // 2. INPUT : Signaler à l'extension que la webview est prête
        // On enregistre l'heure de départ AVANT d'envoyer le message
        this.loadingStartTime = Date.now();
        this.sendMessage('ready');
    }

    dispose(): void {
        this.cleanups.forEach(cleanup => cleanup());
        this.cleanups = [];
    }
}

export const appService = new AppService();