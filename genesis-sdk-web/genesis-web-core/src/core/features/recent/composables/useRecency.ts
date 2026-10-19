// genesis-sdk-web/genesis-web-core/src/core/features/recent/composables/useRecency.ts
import { inject, computed } from 'vue'; // ✅ Ajout de computed
import { storeToRefs } from 'pinia';
import { RECENT_SERVICE_KEY, type IRecentService, type RecencyCategory, type RecentItem } from '../types/recent.service.interface';
import { useRecentStore } from '../store/useRecent.store';

export function useRecency() {
    const recentService = inject(RECENT_SERVICE_KEY);
    if (!recentService) {
        throw new Error('[useRecency] IRecentService non fourni. Vérifiez le provide() dans l\'application.');
    }

    const svc = recentService as IRecentService;
    const store = useRecentStore();
    const { recencyData, isLoading, error } = storeToRefs(store);

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

    async function addRecentItem(category: RecencyCategory, item: RecentItem) {
        store.clearError();
        try {
            store.addOrUpdateRecentItem(category, item);
            await svc.addRecentItem(category, item);
        } catch (err) {
            console.error('[useRecency] Erreur lors de l\'ajout de l\'élément récent:', err);
            store.setError((err as Error).message || 'Erreur lors de la sauvegarde de la récence.');
        }
    }

    async function clearRecentItems(category?: RecencyCategory) {
        store.setLoading(true);
        store.clearError();
        try {
            store.clearRecentItems(category);
            await svc.clearRecentItems(category);
        } catch (err) {
            store.setError((err as Error).message || 'Erreur lors de l\'effacement des récents.');
        } finally {
            store.setLoading(false);
        }
    }

    const recentFrameworks = computed(() => recencyData.value.frameworks);
    const recentDatabases = computed(() => recencyData.value.databases);
    const recentFrontends = computed(() => recencyData.value.frontends);

    return {
        // État réactif
        recencyData,
        recentFrameworks,
        recentDatabases,
        recentFrontends,
        isLoading,
        error,
        
        // Actions
        loadRecencyData,
        addRecentItem,
        clearRecentItems,
        clearError: store.clearError
    };
}