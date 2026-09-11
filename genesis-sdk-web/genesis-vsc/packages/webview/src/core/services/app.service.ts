// ✅ 1. Import de l'INSTANCE singleton (et non de la classe)
import { useAppStore } from '../store/useApp.store';
import { useThemeStore } from '../store/useTheme.store';
import { vscodeService } from './vscode.service'; 

export class AppService {
    // ✅ 2. Utilise l'instance singleton par défaut. 
    // Cela garantit qu'aucune nouvelle instance de VscodeService n'est créée.
    constructor(private vscode = vscodeService) {}

    private appStore = useAppStore();
    private themeStore = useThemeStore();
    private cleanups: (() => void)[] = [];
    private loadingStartTime: number = 0;

    init(): void {
        // 1. OUTPUT : Écouter les réponses de l'extension
        this.cleanups.push(
            this.vscode.onMessage('init', (payload: any) => {
                this.themeStore.applyTheme(payload.theme.theme, payload.theme.colorMode);
                
                // RÈGLE DES 3 SECONDES :
                const elapsed = Date.now() - this.loadingStartTime;
                const minLoadingTime = 3000;
                const delay = Math.max(0, minLoadingTime - elapsed);

                setTimeout(() => {
                    this.appStore.setApiReady();
                }, delay);
            })
        );

        this.cleanups.push(
            this.vscode.onMessage('themeChanged', (payload: any) => {
                this.themeStore.applyTheme(payload.theme, payload.colorMode);
            })
        );

        this.cleanups.push(
            this.vscode.onMessage('apiError', (payload: any) => {
                this.appStore.setApiError(payload.message);
            })
        );

        // 2. INPUT : Signaler à l'extension que la webview est prête
        this.loadingStartTime = Date.now();
        this.vscode.sendMessage('ready');
    }

    dispose(): void {
        this.cleanups.forEach(cleanup => cleanup());
        this.cleanups = [];
    }
}

// ✅ 3. Instanciation sans argument : elle utilisera automatiquement le singleton
export const appService = new AppService();