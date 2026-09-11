// On ne garde que les attributs nécessaires à l'UX et à l'information de l'utilisateur
export interface FrontendFramework {
    id: number;
    languageId: number;
    name: string;
    coreFramework: string;
    componentExtension: string;
    defaultPort: string;
}