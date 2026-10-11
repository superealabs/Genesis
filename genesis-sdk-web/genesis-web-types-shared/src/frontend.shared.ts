// ═══ 1. LANGAGE DE PROGRAMMATION FRONTEND (JS, TS, etc.) ═══
export interface FrontendProgrammingLanguage {
    id: number;
    name: string;      // ex: 'JavaScript', 'TypeScript'
    extension: string; // ex: '.js', '.ts'
}

// ═══ 2. LANGUE D'INTERFACE / LOCALISATION (FR, EN, etc.) ═══
export interface InterfaceLanguage {
    id: number;
    code: string;      // ex: 'fr', 'en', 'es'
    name: string;      // ex: 'Français', 'English', 'Español'
}

// ═══ 3. FRAMEWORK FRONTEND ═══
export interface FrontendFramework {
    id: number;
    languageId: number;          
    coreFramework: string;       // ex: 'React', 'Vue', 'Angular', ou '.NET MVC'
    name: string;                // ex: 'React avec TypeScript'
    
    /**
     * MOTEUR DE TEMPLATE (Cas MVC)
     * Si le framework backend est de type MVC, cette propriété contient 
     * le nom du moteur de vue (ex: 'Razor', 'Thymeleaf', 'Blade').
     * Dans ce cas, le frontend doit masquer les sélecteurs de langages JS/TS 
     * et afficher cette valeur comme choix de template.
     */
    viewTemplateEngine?: string; 
    
    componentExtension: string;  // ex: '.tsx', '.vue', '.cshtml'
    defaultPort: string;         // ex: '3000', '5173'
}