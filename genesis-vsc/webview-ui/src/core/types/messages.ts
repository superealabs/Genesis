export type VsCodeMessage = {
    type: string;
    payload?: unknown;
}

// Exemple de messages typés pour Genesis
export type ExtensionToWebview =
    | { type: 'setEnvironments'; payload: string[] }
    | { type: 'setUser'; payload: { username: string } }
    | { type: 'error'; payload: { message: string } }

export type WebviewToExtension =
    | { type: 'ready' }
    | { type: 'selectEnvironment'; payload: { name: string } }
    | { type: 'formSubmitted'; payload: unknown }
