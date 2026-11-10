// src/services/GenesisApiService.ts
import * as net from 'net';
import * as path from 'path';
import * as vscode from 'vscode';
import { spawn, ChildProcess } from 'child_process';
import { createAxiosInstance, resetAxiosInstance } from './http/genesisAxiosInstance';
import * as fs from 'fs';

import { logger } from './LoggerService';

const LOG_CHANNEL = 'Genesis Generator Service';
/** Attente max du démarrage : STARTUP_RETRIES × STARTUP_RETRY_DELAY_MS (60 × 500 ms = 30 s). */
const STARTUP_RETRIES = 60;
const STARTUP_RETRY_DELAY_MS = 500;
/** Nombre de dernières lignes du log Spring recopiées dans le canal en cas d'échec. */
const LOG_TAIL_LINES = 30;
/** Délai avant d'envoyer SIGKILL si SIGTERM n'a pas tué le process. */
const KILL_GRACE_PERIOD_MS = 5000;

export class GenesisApiService {
    private process: ChildProcess | null = null;
    private port: number | null = null;
    private startupError: string | null = null;
    private ready: boolean = false;
    private startPromise: Promise<void> | null = null;

    /** true pendant qu'un démarrage est en cours (évite les double-start). */
    private starting: boolean = false;

    /** true pendant qu'un arrêt volontaire est en cours (distingue crash vs stop). */
    private stopping: boolean = false;

    /**
     * Démarre le JAR. Si un démarrage est déjà en cours, retourne la promesse existante.
     * Si le JAR est déjà prêt, retourne immédiatement.
     */
    start(context: vscode.ExtensionContext): Promise<void> {
        if (this.ready) return Promise.resolve();
        if (this.starting && this.startPromise) return this.startPromise;

        this.starting = true;
        this.startPromise = this.doStart(context).finally(() => {
            this.starting = false;
        });
        return this.startPromise;
    }

    /**
     * Résout quand l'API est prête. Rejette avec la VRAIE cause si le démarrage échoue.
     */
    whenReady(): Promise<void> {
        if (this.ready) return Promise.resolve();
        return this.startPromise
            ?? Promise.reject(new Error("Genesis API : start() n'a pas encore été appelé."));
    }

    private async doStart(context: vscode.ExtensionContext): Promise<void> {
        const startedAt = Date.now();
        const elapsed = () => `${((Date.now() - startedAt) / 1000).toFixed(1)}s`;
        let logPath = '';

        // ✅ Reset défensif : évite toute instance fantôme d'un run précédent.
        resetAxiosInstance();
        this.ready = false;
        this.startupError = null;

        try {
            this.port = await this.findFreePort();
            const jarPath = path.join(context.extensionPath, 'bin', 'genesis-api-1.0.0.jar');

            const logDir = context.globalStorageUri.fsPath;
            fs.mkdirSync(logDir, { recursive: true });
            logPath = path.join(logDir, 'genesis-api.log');
            const logFd = fs.openSync(logPath, 'a');

            this.process = spawn(this.resolveJavaCommand(), [
                '-jar', jarPath,
                `--server.port=${this.port}`,
                `--server.address=127.0.0.1`,
                `--logging.file.name=${logPath}`
            ], {
                stdio: ['ignore', logFd, logFd],
                windowsHide: true
            });

            const java = this.resolveJavaCommand();
            logger.log(LOG_CHANNEL, `Lancement : ${java} -jar ${jarPath} (port ${this.port}) — log Spring : ${logPath}`, true);

            const proc = this.process;

            proc.on('exit', (code, signal) => {
                const reason = this.stopping ? 'arrêt volontaire' : 'arrêt inattendu';
                logger.log(LOG_CHANNEL, `Processus Java terminé (code ${code}, signal ${signal}, ${reason}) après ${elapsed()}`);
                console.log(`[Genesis API] Processus terminé (code ${code}, ${reason})`);

                try { fs.closeSync(logFd); } catch { /* already closed */ }

                // ✅ Nettoyage défensif
                this.process = null;
                this.port = null;
                this.ready = false;
                resetAxiosInstance();
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

            await Promise.race([this.waitForApi(elapsed), spawnFailure]);

            createAxiosInstance(this.port!);
            this.ready = true;
            logger.log(LOG_CHANNEL, `✅ API prête sur le port ${this.port} après ${elapsed()}`);
        } catch (err) {
            this.startupError = err instanceof Error ? err.message : 'Erreur inconnue';
            logger.log(LOG_CHANNEL, `❌ Démarrage échoué après ${elapsed()} : ${this.startupError}`, true);
            this.logTail(logPath);
            // On stoppe proprement (laisse le temps au process de mourir)
            await this.stop();
            throw err;
        }
    }

    /** Recopie les dernières lignes du log Spring dans le canal. */
    private logTail(logPath: string): void {
        if (!logPath) return;
        try {
            const lines = fs.readFileSync(logPath, 'utf-8').trimEnd().split(/\r?\n/).slice(-LOG_TAIL_LINES);
            logger.log(LOG_CHANNEL, `── Dernières lignes de ${logPath} ──\n${lines.join('\n')}`);
        } catch (e) {
            logger.log(LOG_CHANNEL, `Impossible de lire ${logPath} : ${(e as Error).message}`);
        }
    }

    /**
     * Arrête le JAR de façon synchrone-safe (async).
     * - Marque `stopping = true` pour distinguer un arrêt volontaire d'un crash.
     * - Envoie SIGTERM puis SIGKILL après un délai de grâce.
     * - Attend la mort effective du process.
     * - Reset l'instance axios.
     */
    async stop(): Promise<void> {
        const proc = this.process;
        if (!proc) {
            // Rien à arrêter, mais on s'assure que l'état est propre.
            this.process = null;
            this.port = null;
            this.ready = false;
            resetAxiosInstance();
            return;
        }

        this.stopping = true;
        this.ready = false;

        await new Promise<void>((resolve) => {
            let settled = false;
            const finish = () => {
                if (settled) return;
                settled = true;
                clearTimeout(graceTimer);
                clearTimeout(hardKillTimer);
                resolve();
            };

            // 1. Attendre l'exit naturel (SIGTERM a été envoyé ou le process s'arrête seul)
            proc.once('exit', finish);

            // 2. Envoyer SIGTERM
            try {
                proc.kill('SIGTERM');
            } catch {
                // Déjà mort : on nettoie
                finish();
                return;
            }

            // 3. Si pas mort après le grace period, SIGKILL
            const graceTimer = setTimeout(() => {
                logger.log(LOG_CHANNEL, `⚠️ SIGTERM ignoré, envoi de SIGKILL après ${KILL_GRACE_PERIOD_MS}ms`);
                try { proc.kill('SIGKILL'); } catch { /* already dead */ }
            }, KILL_GRACE_PERIOD_MS);

            // 4. Filet de sécurité ultime : si toujours pas mort, on force
            const hardKillTimer = setTimeout(() => {
                logger.log(LOG_CHANNEL, `⚠️ Processus toujours vivant après SIGKILL, abandon du suivi`);
                finish();
            }, KILL_GRACE_PERIOD_MS * 2);
        });

        // Nettoyage final (le on('exit') a déjà fait la plupart, mais on s'assure)
        this.process = null;
        this.port = null;
        this.ready = false;
        this.stopping = false;
        this.startPromise = null;
        resetAxiosInstance();

        logger.log(LOG_CHANNEL, `🛑 API arrêtée.`);
    }

    getStatus(): { ready: boolean; error: string | null } {
        return { ready: this.ready, error: this.startupError };
    }

    getPort(): number | null {
        return this.port;
    }

    /**
     * true UNIQUEMENT si le JAR est lancé ET prêt à recevoir des requêtes.
     * (Avant : `process !== null`, ce qui était vrai même pendant le démarrage.)
     */
    isRunning(): boolean {
        return this.process !== null && this.ready;
    }

    /** true pendant qu'un démarrage est en cours (utile pour l'UI). */
    isStarting(): boolean {
        return this.starting;
    }

    // ═══ Appels API (inchangé) ═══
    async generateJavaFile(className: string, destinationPath: string): Promise<{ success: string; message: string }> {
        if (!this.port) {
            return { success: 'false', message: 'Genesis API non démarrée' };
        }

        const response = await fetch(
            `http://127.0.0.1:${this.port}/api/generator/java?className=${encodeURIComponent(className)}&destinationPath=${encodeURIComponent(destinationPath)}`,
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

    private waitForApi(elapsed: () => string, retries = STARTUP_RETRIES): Promise<void> {
        return new Promise((resolve, reject) => {
            let attempts = 0;
            const check = () => {
                if (!this.port) return reject(new Error('Port non défini'));
                const socket = net.createConnection(this.port, '127.0.0.1');
                socket.on('connect', () => { socket.destroy(); resolve(); });
                socket.on('error', (e: NodeJS.ErrnoException) => {
                    attempts++;
                    if (attempts % 10 === 0) logger.log(LOG_CHANNEL, `En attente de l'API… tentative ${attempts}/${retries} (${e.code}) — ${elapsed()}`);
                    if (attempts >= retries) {
                        reject(new Error(`Genesis API non disponible après ${elapsed()} (${retries} tentatives, dernière erreur : ${e.code ?? e.message})`));
                    } else {
                        setTimeout(check, STARTUP_RETRY_DELAY_MS);
                    }
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