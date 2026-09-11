import * as vscode from 'vscode';

/**
 * Service centralisé pour la gestion des logs de l'extension.
 * Gère le cycle de vie des OutputChannels pour éviter les fuites mémoire.
 */
class LoggerService {
    private channels: Map<string, vscode.OutputChannel> = new Map();
    private _disposables: vscode.Disposable[] = [];

    /**
     * Récupère (ou crée) un canal de sortie par son nom.
     * Le canal est automatiquement enregistré pour être détruit à la fermeture de l'extension.
     */
    public getChannel(name: string): vscode.OutputChannel {
        if (!this.channels.has(name)) {
            const channel = vscode.window.createOutputChannel(name);
            this.channels.set(name, channel);
            this._disposables.push(channel); // ✅ Enregistrement pour le cleanup automatique
        }
        return this.channels.get(name)!;
    }

    /**
     * Écrit une ligne dans un canal spécifique.
     */
    public log(channelName: string, message: string): void {
        const channel = this.getChannel(channelName);
        const timestamp = new Date().toISOString().substring(11, 23); // HH:mm:ss.SSS
        channel.appendLine(`[${timestamp}] ${message}`);
    }

    /**
     * Détruit tous les canaux de sortie (à appeler dans deactivate()).
     */
    public dispose(): void {
        this._disposables.forEach(d => d.dispose());
        this._disposables = [];
        this.channels.clear();
    }
}

// ✅ Export du singleton
export const logger = new LoggerService();