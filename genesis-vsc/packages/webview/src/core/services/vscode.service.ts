export class VscodeService {
    protected vscode = acquireVsCodeApi();

    sendMessage(type: string, payload?: any): void {
        this.vscode.postMessage({ type, payload });
    }

    // MODIFICATION : Retourne une fonction de cleanup pour pouvoir retirer l'écouteur
    onMessage<T>(type: string, callback: (data: T) => void): () => void {
        const handler = (event: MessageEvent) => {
            if (event.data.type === type) {
                callback(event.data.payload);
            }
        };
        
        window.addEventListener('message', handler);
        
        // Retourne la fonction qui supprime l'écouteur
        return () => {
            window.removeEventListener('message', handler);
        };
    }
}