export type FrameworkType = 'MVC' | 'REST_API';

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