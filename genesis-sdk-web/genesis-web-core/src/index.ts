// ═══════════════════════════════════════════════════════════
// MANIFEST CENTRAL DE @genesis-labs/core
// ═══════════════════════════════════════════════════════════
// Ce fichier est le point d'entrée unique. 
// Il réexporte les manifests de chaque feature.

// ═══ FEATURE : FRAMEWORKS ═══
export * from '@genesis-labs/web-core/features/frameworks/manifest';

// ═══ FEATURE : FRONTEND ═══
export * from '@genesis-labs/web-core/features/frontend/manifest';

// ═══ FEATURE : GENERATOR ═══
export * from '@genesis-labs/web-core/features/generator/manifest';

export * from '@genesis-labs/web-core/features/database/manifest'

// Expose les routes pour les modules consommateurs (Webview, etc.)
export { commonRoutes, designSystemRoutes } from './router/router';