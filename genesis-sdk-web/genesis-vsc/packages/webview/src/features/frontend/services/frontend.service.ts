import { 
    FrontendFramework, 
    IFrontendService, 
    InterfaceLanguage, 
    FrontendProgrammingLanguage //  NOUVEAU IMPORT
} from '@genesis-labs/shared-types';
import { vscodeService } from '../../../core/services/vscode.service';

export class FrontendServiceVsc implements IFrontendService {
    constructor(private vscode = vscodeService) {}

    fetchFrontendFrameworks(): Promise<FrontendFramework[]> {
        return new Promise((resolve) => {
            this.vscode.sendMessage('GET_FRONTEND_FRAMEWORKS');
            const cleanup = this.vscode.onMessage<FrontendFramework[]>('FRONTEND_FRAMEWORKS_LOADED', (data) => {
                cleanup(); resolve(data);
            });
        });
    }

    //  NOUVEAU
    fetchFrontendProgrammingLanguages(): Promise<FrontendProgrammingLanguage[]> {
        return new Promise((resolve) => {
            this.vscode.sendMessage('GET_FRONTEND_PROGRAMMING_LANGUAGES');
            const cleanup = this.vscode.onMessage<FrontendProgrammingLanguage[]>('FRONTEND_PROGRAMMING_LANGUAGES_LOADED', (data) => {
                cleanup(); resolve(data);
            });
        });
    }

    //  RENOMMÉ : fetchAvailableLanguages -> fetchInterfaceLanguages
    fetchInterfaceLanguages(): Promise<InterfaceLanguage[]> {
        return new Promise((resolve) => {
            this.vscode.sendMessage('GET_INTERFACE_LANGUAGES');
            const cleanup = this.vscode.onMessage<InterfaceLanguage[]>('INTERFACE_LANGUAGES_LOADED', (data) => {
                cleanup(); resolve(data);
            });
        });
    }

    //  NOUVEAU
    fetchNavbarTypes(): Promise<string[]> {
        return new Promise((resolve) => {
            this.vscode.sendMessage('GET_NAVBAR_TYPES');
            const cleanup = this.vscode.onMessage<string[]>('NAVBAR_TYPES_LOADED', (data) => {
                cleanup(); resolve(data);
            });
        });
    }


}

export const frontendServiceVsc = new FrontendServiceVsc();