// genesis-sdk-web/genesis-web-core/src/core/features/recent/composables/useRecency.ts
import { inject } from 'vue';
import { storeToRefs } from 'pinia';
import { RECENT_SERVICE_KEY, type IRecentService, type RecencyCategory, type RecentItem } from '../types/recent.service.interface';
import { useRecentStore } from '../store/useRecent.store';

/**
 * Composable pour la gestion de la récence des éléments utilisés.
 * 
 * Ce composable est sans état : il délègue la persistance au service injecté 
 * et la réactivité au store Pinia.
 */
export function useRecency() {
    // 1. Injection du service (doit être fourni par le composant racine ou main.ts)
    const recentService = inject(RECENT_SERVICE_KEY);
    if (!recentService) {
        throw new Error('[useRecency] IRecentService non fourni. Vérifiez le provide() dans l\'application.');
    }

    const svc = recentService as IRecentService;

    // 2. Accès au store
    const store = useRecentStore();
    const { recencyData, isLoading, error } = storeToRefs(store);

    // 3. Actions orchestrées (Service + Store)
    
    async function loadRecencyData() {
        store.setLoading(true);
        store.clearError();
        try {
            const data = await svc.getRecencyData();
            store.setRecencyData(data);
        } catch (err) {
            store.setError((err as Error).message || 'Erreur lors du chargement des données de récence.');
        } finally {
            store.setLoading(false);
        }
    }

    /**
     * Ajoute un élément aux récents.
     * Met à jour le store immédiatement pour une UI réactive, puis synchronise avec le service de stockage.
     */
    async function addRecentItem(category: RecencyCategory, item: RecentItem) {
        store.clearError();
        try {
            // 1. Mise à jour optimiste du store (UI instantanée)
            store.addOrUpdateRecentItem(category, item);
            
            // 2. Synchronisation avec le stockage persistant (GlobalState / LocalStorage)
            await svc.addRecentItem(category, item);
        } catch (err) {
            console.error('[useRecency] Erreur lors de l\'ajout de l\'élément récent:', err);
            store.setError((err as Error).message || 'Erreur lors de la sauvegarde de la récence.');
            // Optionnel : Recharger les données pour annuler la mise à jour optimiste en cas d'échec
            // await loadRecencyData(); 
        }
    }

    async function clearRecentItems(category?: RecencyCategory) {
        store.setLoading(true);
        store.clearError();
        try {
            store.clearRecentItems(category); // Mise à jour optimiste
            await svc.clearRecentItems(category);
        } catch (err) {
            store.setError((err as Error).message || 'Erreur lors de l\'effacement des récents.');
        } finally {
            store.setLoading(false);
        }
    }

    // 4. Retour
    return {
        // État réactif (depuis le store)
        recencyData,
        isLoading,
        error,
        
        // Actions
        loadRecencyData,
        addRecentItem,
        clearRecentItems,
        clearError: store.clearError
    };
}