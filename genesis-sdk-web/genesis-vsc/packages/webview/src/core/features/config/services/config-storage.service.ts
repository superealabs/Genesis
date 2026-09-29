import type { 
    GenesisConfig, 
    IConfigService, 
    ConfigType 
} from '@genesis-labs/shared-types';

import { vscodeService } from '../../../services/vscode.service';

export class ConfigServiceVsc implements IConfigService {
    
    // ✅ 2. Utilise l'instance singleton par défaut
    constructor(private vscode = vscodeService) {}

    getAll(configType?: ConfigType): Promise<GenesisConfig[]> {
        return new Promise((resolve) => {
            this.vscode.sendMessage('CONFIG_GET_ALL', { configType });
            
            const cleanup = this.vscode.onMessage<GenesisConfig[]>('CONFIG_GET_ALL_SUCCESS', (data) => {
                cleanup();
                resolve(data);
            });
        });
    }

    save(config: GenesisConfig): Promise<void> {
        return new Promise((resolve) => {
            this.vscode.sendMessage('CONFIG_SAVE', config);
            
            const cleanup = this.vscode.onMessage<{ success: boolean }>('CONFIG_SAVE_SUCCESS', () => {
                cleanup();
                resolve();
            });
        });
    }

    delete(id: string): Promise<void> {
        return new Promise((resolve) => {
            this.vscode.sendMessage('CONFIG_DELETE', id);
            
            const cleanup = this.vscode.onMessage<{ success: boolean }>('CONFIG_DELETE_SUCCESS', () => {
                cleanup();
                resolve();
            });
        });
    }

    import(): Promise<GenesisConfig | GenesisConfig[] | null> {
        return new Promise((resolve) => {
            this.vscode.sendMessage('CONFIG_IMPORT');
            
            const cleanup = this.vscode.onMessage<GenesisConfig | GenesisConfig[] | null>('CONFIG_IMPORT_SUCCESS', (data) => {
                cleanup();
                resolve(data);
            });
        });
    }

    export(config: GenesisConfig): Promise<void> {
        return new Promise((resolve) => {
            this.vscode.sendMessage('CONFIG_EXPORT', config);
            
            const cleanup = this.vscode.onMessage<{ success: boolean }>('CONFIG_EXPORT_SUCCESS', () => {
                cleanup();
                resolve();
            });
        });
    }

    exportAll(configs: GenesisConfig[]): Promise<void> {
        return new Promise((resolve) => {
            this.vscode.sendMessage('CONFIG_EXPORT_ALL', configs);
            
            const cleanup = this.vscode.onMessage<{ success: boolean }>('CONFIG_EXPORT_ALL_SUCCESS', () => {
                cleanup();
                resolve();
            });
        });
    }
}

// ✅ 3. Instanciation sans argument : elle utilisera automatiquement le singleton
export const configServiceVsc = new ConfigServiceVsc();