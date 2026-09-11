import { inject } from 'vue';
import { storeToRefs } from 'pinia';
import { useFrontendStore } from '../store/useFrontend.store';
import { FRONTEND_SERVICE_KEY, type IFrontendService } from '../types/frontend.service.interface';
import type { FrontendFramework } from '../types/frontend.types';

export function useFrontend() {
    // 1. Récupération sécurisée du service via inject
    const service = inject(FRONTEND_SERVICE_KEY);
    if (!service) {
        throw new Error('[useFrontend] IFrontendService non fourni. Vérifiez app.provide() dans main.ts');
    }

    // 2. ✅ Astuce TypeScript : variable locale fortement typée pour les closures asynchrones
    const svc = service as IFrontendService;

    const store = useFrontendStore();
    
    // ✅ Exposition réactive de TOUT l'état nécessaire à la vue
    const { 
        availableFrameworks, 
        selectedFramework, 
        hasSelectedFramework,
        displayMode,
        searchQuery
    } = storeToRefs(store);

    /**
     * À appeler au montage du composant pour charger les données.
     * ✅ Le composable est le SEUL à muter le store avec les données du service.
     */
    async function initialize() {
        try {
            // ✅ On attend la Promise et on récupère les données brutes
            const data = await svc.fetchFrontendFrameworks();
            
            // ✅ Le composable met à jour le store (pas le service !)
            store.setAvailableFrameworks(data);
        } catch (error) {
            console.error('[useFrontend] Erreur lors du chargement des frameworks:', error);
        }
    }

    /**
     * Action déclenchée par la Vue lors du clic sur un framework
     */
    async function selectFramework(framework: FrontendFramework) {
        // 1. Mise à jour locale immédiate (Optimistic UI)
        store.selectFramework(framework);
        
        // 2. ✅ Notification au service et attente de la confirmation
        try {
            await svc.selectFrontendFramework(framework);
        } catch (error) {
            console.error('[useFrontend] Erreur lors de la sélection du framework:', error);
            // Optionnel : store.reset() ou revert de la sélection en cas d'échec
        }
    }

    /**
     * Réinitialisation de l'état (utile si on ferme/rouvre le stepper)
     */
    function reset() {
        store.reset();
    }

    return {
        // État (readonly via storeToRefs)
        availableFrameworks,
        selectedFramework,
        hasSelectedFramework,
        displayMode,
        searchQuery,
        
        // Actions
        initialize,
        selectFramework,
        reset,
        
        // ✅ Mappings vers les actions du store pour la vue
        // (Note : adapte 'setDisplayMode' en 'toggleDisplayMode' si ton store a déjà une méthode qui bascule)
        setSearch: store.setSearch,
        toggleDisplayMode: () => store.setDisplayMode(displayMode.value === 'grid' ? 'grid' : 'list')
    };
}