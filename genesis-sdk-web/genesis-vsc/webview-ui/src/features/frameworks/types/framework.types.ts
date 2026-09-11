// webview-ui/src/features/frameworks/types/framework.types.ts

export type FrameworkType = 'MVC' | 'REST API';

export interface Framework {
    id: number;
    languageId: number;
    name: string;
    coreFramework: string;
    type: FrameworkType; // <-- Utilise maintenant 'MVC' | 'REST API'
    isProd: boolean;
    useDB: boolean;
    useCloud: boolean;
    useEurekaServer: boolean;
    isGateway: boolean;
    useFrontendApp: boolean;
}

export interface FrameworkFilters {
    language?: string;
    type?: FrameworkType; // <-- Cohérent avec le type ci-dessus
    coreFramework?: string;
    isProd?: boolean;
    useDB?: boolean;
    useCloud?: boolean;
    useEurekaServer?: boolean;
    isGateway?: boolean;
    useFrontendApp?: boolean;
    viewTemplateEngine?: string;
    viewExtension?: string;
}