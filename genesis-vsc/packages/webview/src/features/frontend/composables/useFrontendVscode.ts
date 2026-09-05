// ✅ Import du composable pur du Core
import { useFrontend } from '@genesis-labs/core/features/frontend/composables/useFrontend';

// ✅ Import du service concret spécifique à VSC (qui implémente IFrontendService)
import { frontendService } from '../services/frontend.service';

/**
 * Composable spécifique à VS Code.
 * Il étend le composable du core en lui injectant le service de communication VSC.
 */
export function useFrontendVscode() {
    return useFrontend(frontendService);
}