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
// Aligné avec org.labs.genesis.frontend.generator.FrontendFramework
export interface FrontendFramework {
    id: number;
    languageId: number;          // Référence l'ID de FrontendProgrammingLanguage
    coreFramework: string;       // ex: 'React', 'Vue', 'Angular'
    name: string;                // ex: 'React avec TypeScript'
    template?: string;
    componentExtension: string;  // ex: '.tsx', '.vue'
    defaultPort: string;         // ex: '3000', '5173'
}