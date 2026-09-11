export class VscodeService {
    private static instance: VscodeService;
    private vscode: any;

    // ✅ Le constructeur est privé pour empêcher l'instanciation directe avec "new"
    private constructor() {
        // acquireVsCodeApi() ne peut être appelé qu'UNE SEULE FOIS
        this.vscode = (window as any).acquireVsCodeApi();
    }

    // ✅ Méthode statique pour obtenir l'instance unique
    public static getInstance(): VscodeService {
        if (!VscodeService.instance) {
            VscodeService.instance = new VscodeService();
        }
        return VscodeService.instance;
    }

    public sendMessage(type: string, payload?: any): void {
        this.vscode.postMessage({ type, payload });
    }

    public onMessage<T>(type: string, callback: (data: T) => void): () => void {
        const handler = (event: MessageEvent) => {
            if (event.data.type === type) {
                callback(event.data.payload);
            }
        };
        window.addEventListener('message', handler);
        
        // Retourne la fonction de nettoyage (cleanup)
        return () => {
            window.removeEventListener('message', handler);
        };
    }
}

// ✅ Export de l'instance unique prête à l'emploi
export const vscodeService = VscodeService.getInstance();