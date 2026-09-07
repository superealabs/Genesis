import { createRouter, createWebHashHistory } from 'vue-router';
import type { RouteRecordRaw } from 'vue-router';

// ✅ 1. Import des routes communes depuis le core (si tu en as)
import { commonRoutes, designSystemRoutes } from '@genesis-labs/core/router/router';

// ✅ 2. Import de TA vue wrapper locale VSC (et non celle du core)
import FrameworksViewVsc from '../features/frameworks/views/FrameworksView.vue';
import FrontendSelectionViewVsc from '../features/frontend/views/FrontendSelectionView.vue'; // Si tu en as une

// genesis-vsc/packages/webview/src/router/index.ts
import HomeViewVsc from '../features/home/views/HomeViewVsc.vue';

const vscodeSpecificRoutes: RouteRecordRaw[] = [
    {
        path: '/',
        name: 'home',
        component: HomeViewVsc  // ← wrapper VSC, pas le core
    },
    {
        path: '/frameworks',
        name: 'frameworks',
        component: FrameworksViewVsc
    },
    {
        path: '/frontend',
        name: 'frontend',
        component: FrontendSelectionViewVsc
    }
];

// Exclure aussi la route '/' des commonRoutes
const routes: RouteRecordRaw[] = [
    ...commonRoutes.filter(r =>
        r.path !== '/frameworks' &&
        r.path !== '/frontend' &&
        r.path !== '/'          // ← ajouter
    ),
    ...designSystemRoutes,
    ...vscodeSpecificRoutes
];

export const router = createRouter({
    history: createWebHashHistory(),
    routes
});