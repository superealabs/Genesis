// ✅ 1. Import via le manifeste (plus d'imports profonds)
import { useFrontend } from '@genesis-labs/core/features/frontend/manifest';

/**
 * Composable spécifique à VS Code.
 * Le service est déjà injecté en interne par useFrontend() via provide/inject.
 * Si tu as besoin d'ajouter une logique VSC-only (ex: analytics, logs spécifiques), 
 * tu peux l'ajouter ici avant de retourner le résultat.
 */
export function useFrontendVscode() {
    // ✅ 2. On appelle simplement le composable du core.
    // Il récupérera automatiquement le service via inject(FRONTEND_SERVICE_KEY)
    const base = useFrontend();

    // Ici, tu peux ajouter de la logique spécifique à VSC si nécessaire
    // Exemple :
    // const filteredFrameworks = computed(() => base.availableFrameworks.value.filter(f => f.isVscCompatible));

    return {
        ...base
        // filteredFrameworks (si tu en as ajouté)
    };
}