export type VsCodeMessage = {
    type: string;
    payload?: unknown;
}

// Exemple de messages typés pour Genesis
export type WebviewToExtension =
    | { type: 'ready' }
    | { type: 'selectEnvironment'; payload: { name: string } }
    | { type: 'formSubmitted'; payload: unknown }
    | { type: 'REQUEST_FILE_PATH'; payload: { extensions?: string[] } }

export type ExtensionToWebview =
    | { type: 'setEnvironments'; payload: string[] }
    | { type: 'setUser'; payload: { username: string } }
    | { type: 'error'; payload: { message: string } }
    | { type: 'FILE_PATH_SELECTED'; payload: { path: string; content: string } }
