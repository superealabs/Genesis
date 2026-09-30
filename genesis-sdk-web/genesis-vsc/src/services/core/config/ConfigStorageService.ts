// src/services/core/config/ConfigStorageService.ts
import * as vscode from 'vscode';
import type { GenesisConfig, ConfigType, IConfigService } from '@genesis-labs/shared-types';

const STORAGE_KEY = 'genesis_configs';

export class ConfigStorageService implements IConfigService {
  constructor(private context: vscode.ExtensionContext) {}

  /**
   * Récupère les configurations, optionnellement filtrées par type.
   */
  async getAll(configType?: ConfigType): Promise<GenesisConfig[]> {
    const allConfigs = this.context.globalState.get<GenesisConfig[]>(STORAGE_KEY) || [];
    
    if (configType) {
      return allConfigs.filter(c => c.configType === configType);
    }
    
    return allConfigs;
  }

  async save(config: GenesisConfig): Promise<void> {
    // Note à moi même, le save, n'arrive pas ici
    console.log("Utilisation de Save depuis Extensionhost");

    const configs = await this.getAll();
    const index = configs.findIndex(c => String(c.id) === String(config.id));
    
    const newConfigs = [...configs];
    config.updatedAt = new Date().toISOString();

    if (index >= 0) {
      newConfigs[index] = config;
    } else {
      config.createdAt = new Date().toISOString();
      newConfigs.push(config);
    }
    
    await this.context.globalState.update(STORAGE_KEY, newConfigs);
  }

  async delete(id: string | number): Promise<void> { // ✅ Corrigé : signature alignée avec l'interface
    const configs = await this.getAll();
    // La comparaison avec != gère à la fois string et number de manière sûre ici
    const filtered = configs.filter(c => c.id != id); 
    await this.context.globalState.update(STORAGE_KEY, filtered);
  }

  async export(config: GenesisConfig): Promise<void> {
    const uri = await vscode.window.showSaveDialog({
      filters: { 'JSON': ['json'] },
      defaultUri: vscode.Uri.file(`${config.name.replace(/\s+/g, '_')}.json`),
      title: 'Exporter la configuration'
    });

    if (uri) {
      const content = JSON.stringify(config, null, 2);
      await vscode.workspace.fs.writeFile(uri, Buffer.from(content, 'utf-8'));
      vscode.window.showInformationMessage('Configuration exportée avec succès.');
    }
  }

  async exportAll(configs: GenesisConfig[]): Promise<void> {
    const uri = await vscode.window.showSaveDialog({
      filters: { 'JSON': ['json'] },
      defaultUri: vscode.Uri.file('all_configurations.json'),
      title: 'Exporter toutes les configurations'
    });

    if (uri) {
      const content = JSON.stringify(configs, null, 2);
      await vscode.workspace.fs.writeFile(uri, Buffer.from(content, 'utf-8'));
      vscode.window.showInformationMessage('Toutes les configurations ont été exportées.');
    }
  }

  async import(): Promise<GenesisConfig | GenesisConfig[] | null> {
    const openUri = await vscode.window.showOpenDialog({
      filters: { 'JSON': ['json'] },
      canSelectMany: false,
      title: 'Importer une configuration'
    });

    if (openUri && openUri[0]) {
      try {
        const fileContent = await vscode.workspace.fs.readFile(openUri[0]);
        const content = Buffer.from(fileContent).toString('utf-8');
        const parsed = JSON.parse(content);

        // Validation basique
        const isValid = Array.isArray(parsed)
          ? parsed.every((c: any) => c.configType && c.payload)
          : parsed.configType && parsed.payload;

        if (!isValid) {
          throw new Error('Format de configuration invalide.');
        }

        return parsed as GenesisConfig | GenesisConfig[];
      } catch (error) {
        vscode.window.showErrorMessage(`Erreur d'import : ${(error as Error).message}`);
        return null;
      }
    }
    return null;
  }
}