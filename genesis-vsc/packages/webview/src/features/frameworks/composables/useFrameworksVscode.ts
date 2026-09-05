// ✅ Import du composable pur du Core
import { useFrameworks } from '@genesis-labs/core/features/frameworks/composables/useFrameworks';

// ✅ Import du service concret spécifique à VSC (qui implémente IFrameworkService)
import { frameworkService } from '../services/framework.service';

/**
 * Composable spécifique à VS Code.
 * Il étend le composable du core en lui injectant le service de communication VSC.
 * Si tu as un jour besoin d'ajouter une logique VSC-only (ex: analytics VSC), 
 * tu peux l'ajouter ici avant de retourner le résultat.
 */
export function useFrameworksVscode() {
    // On passe simplement le service VSC au composable du core
    return useFrameworks(frameworkService);
}