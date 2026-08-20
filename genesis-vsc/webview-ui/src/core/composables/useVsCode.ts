import type { VsCodeMessage } from '../types/messages';

// Déclaration locale de l'API injectée par VS Code
declare function acquireVsCodeApi(): {
    postMessage(message: unknown): void;
    getState(): unknown;
    setState(state: unknown): void;
};

const vscode = acquireVsCodeApi();

export function useVsCode() {
    function send(type: string, payload?: unknown): void {
        vscode.postMessage({ type, payload });
    }

    function onMessage(handler: (message: VsCodeMessage) => void): () => void {
        const listener = (event: MessageEvent) => handler(event.data);
        window.addEventListener('message', listener);
        return () => window.removeEventListener('message', listener);
    }

    return { send, onMessage };
}