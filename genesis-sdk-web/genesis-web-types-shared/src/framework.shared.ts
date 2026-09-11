// ═══ TYPES PARTAGÉS (Single Source of Truth) ═══
// RÈGLE STRICTE : Aucun import avec l'alias '@/'. 
// Ce fichier doit être lisible par tsc (Node.js) et Vite (Browser).

export type FrameworkType = 'MVC' | 'REST API';

export interface Framework {
    id: number;
    languageId: number;
    name: string;
    coreFramework: string;
    type: FrameworkType;
    isProd: boolean;
    useDB: boolean;
    useCloud: boolean;
    useEurekaServer: boolean;
    isGateway: boolean;
    useFrontendApp: boolean;
}

export interface FrameworkFilters {
    language?: string;
    type?: FrameworkType;
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