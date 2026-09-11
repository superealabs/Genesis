import { storeToRefs } from 'pinia';
import { useFrontendStore } from '../store/useFrontend.store';
import { frontendService } from '../services/frontend.service';
import type { FrontendFramework } from '../types/frontend.types';

export function useFrontend() {
    const store = useFrontendStore();
    
    // Exposition réactive de l'état
    const { availableFrameworks, selectedFramework, hasSelectedFramework } = storeToRefs(store);

    // Initialisation des écouteurs du service (fait une seule fois)
    frontendService.init();

    /**
     * À appeler au montage du composant pour charger les données
     */
    function initialize() {
        frontendService.fetchFrontendFrameworks();
    }

    /**
     * Action déclenchée par la Vue lors du clic sur un framework
     */
    function selectFramework(framework: FrontendFramework) {
        // 1. Mise à jour locale immédiate (Optimistic UI)
        store.selectFramework(framework);
        
        // 2. Notification au service (pour d'éventuels logs ou synchronisation future)
        frontendService.selectFrontendFramework(framework);
    }

    /**
     * Réinitialisation de l'état (utile si on ferme/rouvre le stepper)
     */
    function reset() {
        store.reset();
    }

    return {
        // État (readonly)
        availableFrameworks,
        selectedFramework,
        hasSelectedFramework,
        
        // Actions
        initialize,
        selectFramework,
        reset
    };
}