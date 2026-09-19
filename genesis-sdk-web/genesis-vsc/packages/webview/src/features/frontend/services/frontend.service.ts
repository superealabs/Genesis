import { FrontendFramework, LanguageDto, IFrontendService } from '@genesis-labs/shared-types';
import { vscodeService } from '../../../core/services/vscode.service';

export class FrontendServiceVsc implements IFrontendService {
    constructor(private vscode = vscodeService) {}

    fetchFrontendFrameworks(): Promise<FrontendFramework[]> {
        return new Promise((resolve) => {
            this.vscode.sendMessage('GET_FRONTEND_FRAMEWORKS');
            const cleanup = this.vscode.onMessage<FrontendFramework[]>('FRONTEND_FRAMEWORKS_LOADED', (data) => {
                cleanup();
                resolve(data);
            });
        });
    }

    fetchAvailableLanguages(): Promise<LanguageDto[]> {
        return new Promise((resolve) => {
            this.vscode.sendMessage('GET_AVAILABLE_LANGUAGES');
            const cleanup = this.vscode.onMessage<LanguageDto[]>('AVAILABLE_LANGUAGES_LOADED', (data) => {
                cleanup();
                resolve(data);
            });
        });
    }

    selectFrontendFramework(framework: FrontendFramework): Promise<void> {
        return new Promise((resolve, reject) => {
            this.vscode.sendMessage('SELECT_FRONTEND', { framework });
            
            const cleanup = this.vscode.onMessage<any>('FRONTEND_FRAMEWORK_SELECTED', (data) => {
                cleanup();
                if (data.success) resolve();
                else reject(new Error('Échec de la sélection'));
            });

            const errorCleanup = this.vscode.onMessage<any>('API_ERROR', (data) => {
                if (data.command === 'SELECT_FRONTEND') {
                    cleanup(); errorCleanup();
                    reject(new Error(data.message));
                }
            });
        });
    }
}

export const frontendServiceVsc = new FrontendServiceVsc();