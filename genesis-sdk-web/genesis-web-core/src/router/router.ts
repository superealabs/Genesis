import type { RouteRecordRaw } from 'vue-router';

import HomeView from '@genesis-labs/web-core/features/home/components/HomeView.vue';
import DesignSystemView from '@genesis-labs/web-core/features/designSystem/components/DesignSystemView.vue';
import ButtonShowcase from '@genesis-labs/web-core/features/designSystem/components/ButtonShowcase.vue';
import PopupShowcase from '@genesis-labs/web-core/features/designSystem/components/PopupShowcase.vue';
import LoaderShowcase from '@genesis-labs/web-core/features/designSystem/components/LoaderShowcase.vue';
import ColorShowcase from '@genesis-labs/web-core/features/designSystem/components/ColorShowcase.vue';
import ErrorShowcase from '@genesis-labs/web-core/features/designSystem/components/ErrorShowcase.vue';
import ProgressShowcase from '@genesis-labs/web-core/features/designSystem/components/ProgressShowcase.vue';
import FrameworksView from '@genesis-labs/web-core/features/frameworks/views/FrameworksView.vue';
import FrontEndSelectionView from '@genesis-labs/web-core/features/frontend/views/FrontEndSelectionView.vue';
import DatabaseSelection from '@genesis-labs/web-core/features/database/views/DatabaseSelection.vue';

// Routes communes à toutes les plateformes
export const commonRoutes: RouteRecordRaw[] = [
    {
        path: '/',
        name: 'home',
        component: HomeView
    },
    {
        path: '/frameworks',
        name: 'frameworks',
        component: FrameworksView
    },
    {
        path: '/frontend',
        name: 'frontend',
        component: FrontEndSelectionView
    },
    {
        path: '/databases',
        name: 'database',
        component: DatabaseSelection
    }
];

export const designSystemRoutes: RouteRecordRaw[] = [
    {
        path: '/design-system',
        name: 'design-system',
        component: DesignSystemView
    },
    {
        path: '/design-system/buttons',
        name: 'design-system-buttons',
        component: ButtonShowcase
    },
    {
        path: '/design-system/popups',
        name: 'design-system-popups',
        component: PopupShowcase
    },
    {
        path: '/design-system/loaders',
        name: 'design-system-loaders',
        component: LoaderShowcase
    },
    {
        path: '/design-system/colors',
        name: 'design-system-colors',
        component: ColorShowcase
    },
    {
        path: '/design-system/errors',
        name: 'design-system-errors',
        component: ErrorShowcase
    },
    {
        path: '/design-system/progress-bar',
        name: 'design-system-progressBar',
        component: ProgressShowcase
    }
];