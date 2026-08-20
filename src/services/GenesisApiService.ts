import * as net from 'net';
import * as path from 'path';
import * as vscode from 'vscode';
import { spawn, ChildProcess } from 'child_process';

export class GenesisApiService {

    private process: ChildProcess | null = null;
    private port: number | null = null;
    private startupError: string | null = null;
    private ready: boolean = false;

    // ═══ Démarrage ═══
    async start(context: vscode.ExtensionContext): Promise<void> {
        try {
            this.port = await this.findFreePort();
            const jarPath = path.join(context.extensionPath, 'bin', 'genesis-api-1.0.0.jar');

            this.process = spawn('java', [
                '-jar', jarPath,
                `--server.port=${this.port}`,
                '--server.address=127.0.0.1'
            ]);

            this.process.stderr?.on('data', (data) => {
                console.error('[Genesis API]', data.toString());
            });

            this.process.on('exit', (code) => {
                console.log(`[Genesis API] Processus terminé (code ${code})`);
                this.process = null;
                this.port = null;
            });

            await this.waitForApi();
            this.ready = true;
        } catch (err) {
            this.startupError = err instanceof Error ? err.message : 'Erreur inconnue';
            throw err;
        }
    }

    getStatus(): { ready: boolean; error: string | null } {
        return {
            ready: this.ready,
            error: this.startupError
        };
    }

    // ═══ Arrêt ═══
    stop(): void {
        if (this.process) {
            this.process.kill();
            this.process = null;
            this.port = null;
        }
    }

    // ═══ Getters ═══
    getPort(): number | null {
        return this.port;
    }

    isRunning(): boolean {
        return this.process !== null;
    }

    // ═══ Appels API ═══
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
                const socket = net.createConnection(this.port!, '127.0.0.1');
                socket.on('connect', () => {
                    socket.destroy();
                    resolve();
                });
                socket.on('error', () => {
                    attempts++;
                    if (attempts >= retries) {
                        reject(new Error('Genesis API non disponible'));
                    } else {
                        setTimeout(check, 500);
                    }
                });
            };

            check();
        });
    }
}