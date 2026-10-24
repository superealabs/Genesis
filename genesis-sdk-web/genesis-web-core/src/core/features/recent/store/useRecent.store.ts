// genesis-sdk-web/genesis-web-core/src/core/features/recent/store/useRecent.store.ts
import { defineStore } from 'pinia';
import { ref } from 'vue';
import type { RecencyData, RecentItem, RecencyCategory } from '@genesis-labs/shared-types';
import { MAX_RECENT_ITEMS } from '../types/recent.service.interface';

export const useRecentStore = defineStore('recent', () => {
    // ═══ 1. État (State) ═══
    const recencyData = ref<RecencyData>({
        frameworks: [],
        databases: [],
        frontends: [],
        repositories: []
    });
    const isLoading = ref(false);
    const error = ref<string | null>(null);

    // ═══ 2. Actions (Mutations) ═══
    function setRecencyData(data: RecencyData) {
        recencyData.value = data;
    }

    /**
     * Met à jour le store de manière réactive et immédiate.
     * Applique la logique LRU (sans doublon, en tête de liste, tronqué à MAX_RECENT_ITEMS).
     * Cela garantit une UI fluide sans attendre la réponse du service de stockage.
     */
    function addOrUpdateRecentItem(category: RecencyCategory, item: RecentItem) {
        const currentList = recencyData.value[category];
        
        // 1. Retirer l'élément s'il existe déjà (évite les doublons)
        const filteredList = currentList.filter(i => i.id !== item.id);
        
        // 2. Ajouter au début et tronquer à la limite maximale
        const updatedList = [item, ...filteredList].slice(0, MAX_RECENT_ITEMS);
        
        // 3. Mettre à jour le state
        recencyData.value = {
            ...recencyData.value,
            [category]: updatedList
        };
    }

    function clearRecentItems(category?: RecencyCategory) {
        if (category) {
            recencyData.value[category] = [];
        } else {
            recencyData.value = { frameworks: [], databases: [], frontends: [], repositories: [] };
        }
    }

    function setLoading(state: boolean) {
        isLoading.value = state;
    }

    function setError(message: string | null) {
        error.value = message;
    }

    function clearError() {
        error.value = null;
    }

    function reset() {
        recencyData.value = { frameworks: [], databases: [], frontends: [], repositories: [] };
        isLoading.value = false;
        error.value = null;
    }

    // ═══ 3. Retour ═══
    return {
        // État
        recencyData,
        isLoading,
        error,
        
        // Actions
        setRecencyData,
        addOrUpdateRecentItem,
        clearRecentItems,
        setLoading,
        setError,
        clearError,
        reset
    };
});