import * as net from 'net';
import * as path from 'path';
import * as vscode from 'vscode';
import { spawn, ChildProcess } from 'child_process';
import { createAxiosInstance } from './http/genesisAxiosInstance';
import * as fs from 'fs';

import { logger } from './LoggerService';

export class GenesisApiService {
    private process: ChildProcess | null = null;
    private port: number | null = null;
    private startupError: string | null = null;
    private ready: boolean = false;

    async start(context: vscode.ExtensionContext): Promise<void> {
        try {
            this.port = await this.findFreePort();
            const jarPath = path.join(context.extensionPath, 'bin', 'genesis-api-1.0.0.jar');

            // 👇 Redirige stdout + stderr vers un fichier
            const logDir = context.globalStorageUri.fsPath;
            fs.mkdirSync(logDir, { recursive: true });
            const logPath = path.join(logDir, 'genesis-api.log');
            const logFd = fs.openSync(logPath, 'a');

            this.process = spawn(this.resolveJavaCommand(), [
                '-jar', jarPath,
                `--server.port=${this.port}`,
                `--server.address=127.0.0.1`,
                `--logging.file.name=${logPath}`   // 👈 Spring Boot écrit dans le même fichier
            ], {
                stdio: ['ignore', logFd, logFd],   // 👈 stdin fermé, stdout/stderr → fichier
                windowsHide: true
            });

            logger.log('Genesis Generator Service', `Project set on port : ${this.port}`, true);

            const proc = this.process;

            proc.on('exit', (code) => {
                console.log(`[Genesis API] Processus terminé (code ${code})`);
                try { fs.closeSync(logFd); } catch { /* already closed */ }
                this.process = null;
                this.port = null;
                this.ready = false;
            });

            const spawnFailure = new Promise<never>((_, reject) => {
                proc.once('error', (e: NodeJS.ErrnoException) => reject(new Error(
                    e.code === 'ENOENT'
                        ? 'Java introuvable. Installez un JDK 17+ ou définissez le réglage genesis.javaHome.'
                        : `Échec du lancement de Java : ${e.message}`
                )));
                proc.once('exit', (code) => reject(new Error(
                    `L'API s'est arrêtée au démarrage (code ${code}). Voir ${logPath}`
                )));
            });
            spawnFailure.catch(() => {});

            await Promise.race([this.waitForApi(), spawnFailure]);

            createAxiosInstance(this.port!);
            this.ready = true;
        } catch (err) {
            this.startupError = err instanceof Error ? err.message : 'Erreur inconnue';
            throw err;
        }
    }

    stop(): void {
        if (this.process) {
            this.process.kill();
            this.process = null;
            this.port = null;
            this.ready = false;
        }
    }

    getStatus(): { ready: boolean; error: string | null } {
        return { ready: this.ready, error: this.startupError };
    }

    getPort(): number | null {
        return this.port;
    }

    isRunning(): boolean {
        return this.process !== null;
    }

    // ═══ Appels API (Conservé comme à l'origine) ═══
    async generateJavaFile(className: string, destinationPath: string): Promise<{ success: string; message: string }> {
        if (!this.port) {
            return { success: 'false', message: 'Genesis API non démarrée' };
        }

        const response = await fetch(
            `http://127.0.0.1:${this.port}/api/generator/java?className=${className}&destinationPath=${encodeURIComponent(destinationPath)}`,
            { method: 'POST' }
        );

        return response.json() as Promise<{ success: string; message: string }>;
    }

    // ═══ Utilitaires privés ═══
    private findFreePort(): Promise<number> {
        return new Promise((resolve) => {
            const server = net.createServer();
            server.listen(0, '127.0.0.1', () => {
                const port = (server.address() as net.AddressInfo).port;
                server.close(() => resolve(port));
            });
        });
    }

    private waitForApi(retries = 20): Promise<void> {
        return new Promise((resolve, reject) => {
            let attempts = 0;
            const check = () => {
                if (!this.port) return reject(new Error('Port non défini'));
                const socket = net.createConnection(this.port, '127.0.0.1');
                socket.on('connect', () => { socket.destroy(); resolve(); });
                socket.on('error', () => {
                    if (++attempts >= retries) reject(new Error('Genesis API non disponible'));
                    else setTimeout(check, 500);
                });
            };
            check();
        });
    }

    private resolveJavaCommand(): string {
        const javaHome = process.env.JAVA_HOME
            ?? vscode.workspace.getConfiguration('genesis').get<string>('javaHome');
        if (javaHome) {
            return path.join(javaHome, 'bin', process.platform === 'win32' ? 'java.exe' : 'java');
        }
        if (process.platform === 'darwin') {
            // Candidats usuels sur Mac quand le PATH du shell n'est pas hérité
            const candidates = [
                '/opt/homebrew/opt/openjdk/bin/java',
                '/usr/local/opt/openjdk/bin/java',
            ];
            const found = candidates.find(c => fs.existsSync(c));
            if (found) return found;
        }
        return 'java';
    }
}