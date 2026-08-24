type MessageHandler<T = any> = (payload: T) => void;

export abstract class VscodeService {
    // Une seule instance de l'API, partagée entre toutes les sous-classes
    private static _vscodeApi: any;

    protected get vscode(): any {
        if (!VscodeService._vscodeApi) {
            // @ts-ignore
            VscodeService._vscodeApi = acquireVsCodeApi();
        }
        return VscodeService._vscodeApi;
    }

    private handlers: Map<string, Set<MessageHandler>> = new Map();

    constructor() {
        this.setupListener();
    }

    protected sendMessage(command: string, payload?: any): void {
        this.vscode.postMessage({ type: command, payload });
    }

    protected onMessage<T = any>(command: string, callback: MessageHandler<T>): () => void {
        if (!this.handlers.has(command)) {
            this.handlers.set(command, new Set());
        }
        this.handlers.get(command)!.add(callback as MessageHandler);

        return () => {
            this.handlers.get(command)?.delete(callback as MessageHandler);
        };
    }

    // ✅ Méthode protégée pour simuler une réponse mock sans casser l'encapsulation
    protected simulateMessage<T = any>(command: string, payload: T): void {
        const handlers = this.handlers.get(command);
        if (handlers) {
            handlers.forEach(cb => cb(payload));
        }
    }

    private setupListener(): void {
        window.addEventListener('message', (event: MessageEvent) => {
            const message = event.data;
            const handlers = this.handlers.get(message.type);
            if (handlers) {
                handlers.forEach(cb => cb(message.payload));
            }
        });
    }
}