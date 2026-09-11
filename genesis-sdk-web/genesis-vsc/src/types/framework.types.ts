export interface Framework {
    id: number;
    languageId: number;
    name: string;
    coreFramework: string;
    type: string;
    isProd: boolean;
    useDB: boolean;
    useCloud: boolean;
    useEurekaServer: boolean;
    isGateway: boolean;
    useFrontendApp: boolean;
}