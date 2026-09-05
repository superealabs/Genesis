// ✅ 1. Chemins relatifs corrigés (car ce fichier est dans le package VSC, pas le core)
import { useAppStore } from '../store/useApp.store';
import { useThemeStore } from '../store/useTheme.store';
import { VscodeService } from './vscode.service';

export class AppService {
    // ✅ 2. Composition : on injecte VscodeService, on n'hérite plus de lui
    constructor(private vscode: VscodeService) {}

    private appStore = useAppStore();
    private themeStore = useThemeStore();
    private cleanups: (() => void)[] = [];
    private loadingStartTime: number = 0;

    init(): void {
        // 1. OUTPUT : Écouter les réponses de l'extension
        // ✅ 3. On appelle this.vscode.onMessage, qui retourne maintenant une () => void
        this.cleanups.push(
            this.vscode.onMessage('init', (payload: any) => {
                this.themeStore.applyTheme(payload.theme.theme, payload.theme.colorMode);
                
                // RÈGLE DES 3 SECONDES :
                const elapsed = Date.now() - this.loadingStartTime;
                const minLoadingTime = 3000; // 3 secondes
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
                // En cas d'erreur critique, on annule le délai et on affiche l'erreur immédiatement
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

// ✅ 4. On instancie en passant la dépendance VscodeService
export const appService = new AppService(new VscodeService());