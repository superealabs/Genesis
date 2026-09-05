import { createRouter, createWebHashHistory } from 'vue-router';
import type { RouteRecordRaw } from 'vue-router';

// ✅ 1. Import des routes communes depuis le core (si tu en as)
import { commonRoutes, designSystemRoutes } from '@genesis-labs/core/router/router';

// ✅ 2. Import de TA vue wrapper locale VSC (et non celle du core)
import FrameworksViewVsc from '../features/frameworks/views/FrameworksView.vue';
import FrontendSelectionViewVsc from '../features/frontend/views/FrontendSelectionView.vue'; // Si tu en as une

// ✅ 3. Définition des routes spécifiques à VSC
const vscodeSpecificRoutes: RouteRecordRaw[] = [
    {
        path: '/frameworks',
        name: 'frameworks',
        component: FrameworksViewVsc // ← ✅ C'est ICI que la magie opère
    },
    {
        path: '/frontend',
        name: 'frontend',
        component: FrontendSelectionViewVsc
    }
];

// ✅ 4. Assemblage final
const routes: RouteRecordRaw[] = [
    ...commonRoutes.filter(r => r.path !== '/frameworks' && r.path !== '/frontend'), // On exclut celles du core pour éviter les doublons
    ...designSystemRoutes,
    ...vscodeSpecificRoutes
];

export const router = createRouter({
    history: createWebHashHistory(),
    routes
});