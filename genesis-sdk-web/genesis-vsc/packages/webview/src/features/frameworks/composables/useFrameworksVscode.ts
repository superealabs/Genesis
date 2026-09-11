// ✅ Import via le manifeste
import { useFrameworks } from '@genesis-labs/core/features/frameworks/manifest';

/**
 * Composable spécifique à VS Code.
 * Le service est déjà injecté en interne par useFrameworks() via provide/inject.
 */
export function useFrameworksVscode() {
    // ✅ Appel simple, sans passer le service en paramètre
    const base = useFrameworks();

    // Ici, tu peux ajouter de la logique VSC-specific si besoin
    // const filteredForVsc = computed(() => base.frameworks.value.filter(...));

    return {
        ...base
        // filteredForVsc
    };
}