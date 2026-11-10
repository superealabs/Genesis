import axios, { AxiosInstance } from 'axios';

let instance: AxiosInstance | null = null;

export function createAxiosInstance(port: number): AxiosInstance {
    instance = axios.create({
        baseURL: `http://127.0.0.1:${port}/api`,
        timeout: 10000, // Augmenté un peu pour la génération de fichiers
        headers: { 'Content-Type': 'application/json' }
    });

    instance.interceptors.request.use((config) => {
        console.log(`[Genesis HTTP] ${config.method?.toUpperCase()} ${config.url}`);
        return config;
    });

    instance.interceptors.response.use(
        (response) => response,
        (error) => {
            const msg = error.response?.data?.message ?? error.message ?? 'Erreur réseau';
            return Promise.reject(new Error(msg));
        }
    );

    return instance;
}

export function getAxiosInstance(): AxiosInstance {
    if (!instance) throw new Error('[Genesis] axiosInstance non initialisée. Le JAR est-il démarré ?');
    return instance;
}

/**
 * ✅ NOUVEAU : remet l'instance à null.
 * À appeler quand le JAR s'arrête (stop()) ou avant un nouveau start(),
 * pour éviter de réutiliser une instance qui pointe vers un port mort.
 */
export function resetAxiosInstance(): void {
    instance = null;
}

/**
 * ✅ NOUVEAU : indique si une instance est actuellement disponible.
 * Utile pour les checks défensifs sans try/catch.
 */
export function hasAxiosInstance(): boolean {
    return instance !== null;
}