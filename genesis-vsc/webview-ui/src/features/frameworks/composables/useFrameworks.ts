import { ref } from 'vue';
import type { Framework } from '../types/framework.types';

const frameworks: Framework[] = [
    {
        id: 1,
        languageId: 1,
        name: 'Spring Boot REST',
        coreFramework: 'Spring',
        type: 'REST_API',
        isProd: true,
        useDB: true,
        useCloud: false,
        useEurekaServer: false,
        isGateway: false,
        useFrontendApp: false,
    },
    {
        id: 2,
        languageId: 1,
        name: 'Spring MVC',
        coreFramework: 'Spring',
        type: 'MVC',
        isProd: true,
        useDB: true,
        useCloud: false,
        useEurekaServer: false,
        isGateway: false,
        useFrontendApp: true,
    },
    {
        id: 3,
        languageId: 2,
        name: 'Django REST',
        coreFramework: 'Django',
        type: 'REST_API',
        isProd: true,
        useDB: true,
        useCloud: false,
        useEurekaServer: false,
        isGateway: false,
        useFrontendApp: false,
    },
    {
        id: 4,
        languageId: 3,
        name: 'Laravel MVC',
        coreFramework: 'Laravel',
        type: 'MVC',
        isProd: true,
        useDB: true,
        useCloud: false,
        useEurekaServer: false,
        isGateway: false,
        useFrontendApp: true,
    },
    {
        id: 5,
        languageId: 4,
        name: 'Express REST',
        coreFramework: 'Express',
        type: 'REST_API',
        isProd: false,
        useDB: false,
        useCloud: false,
        useEurekaServer: false,
        isGateway: false,
        useFrontendApp: false,
    },
];

export function useFrameworks() {
    const list = ref<Framework[]>(frameworks);
    const displayMode = ref<'grid' | 'list'>('grid');

    function toggleMode() {
        displayMode.value = displayMode.value === 'grid' ? 'list' : 'grid';
    }

    return { list, displayMode, toggleMode };
}