import { createRouter, createWebHashHistory } from 'vue-router';
import HomeView from '@/features/home/components/HomeView.vue';
import DesignSystemView from '@/features/designSystem/components/DesignSystemView.vue';
import ButtonShowcase from '@/features/designSystem/components/ButtonShowcase.vue';
import PopupShowcase from '@/features/designSystem/components/PopupShowcase.vue';
import LoaderShowcase from '@/features/designSystem/components/LoaderShowcase.vue';
import ColorShowcase from '@/features/designSystem/components/ColorShowcase.vue';
import ErrorShowcase from '@/features/designSystem/components/ErrorShowcase.vue';
import ProgressShowcase from '@/features/designSystem/components/ProgressShowcase.vue';
import FrameworksView from '@/features/frameworks/views/FrameworksView.vue';
import FrontEndSelectionView from '@/features/frontend/views/FrontEndSelectionView.vue';

export const router = createRouter({
    history: createWebHashHistory(),
    routes: [
        {
            path: '/',
            name: 'home',
            component: HomeView
        },
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
    ]
});