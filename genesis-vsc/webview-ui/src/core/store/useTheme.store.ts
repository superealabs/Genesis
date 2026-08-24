// webview-ui/src/store/useThemeStore.ts
import { ref } from 'vue';

// État global partagé
const currentTheme = ref('genesis');
const currentColorMode = ref<'light' | 'dark'>('dark');

export function useThemeStore() {
    function detectVsCodeMode(): 'light' | 'dark' | 'high-contrast' | 'unknown' {
        const body = document.body;
        if (body.classList.contains('vscode-light')) return 'light';
        if (body.classList.contains('vscode-dark')) return 'dark';
        if (body.classList.contains('vscode-high-contrast') || body.classList.contains('vscode-high-contrast-light')) return 'high-contrast';
        return 'unknown';
    }

    function applyTheme(theme: string, colorMode: string) {
        const body = document.body;
        
        // Nettoyer les anciennes classes
        body.classList.remove(
            'theme-genesis', 
            'theme-vscode', 
            'theme-mono', 
            'genesis-light', 
            'genesis-dark'
        );
        
        // Appliquer le thème
        body.classList.add(`theme-${theme}`);
        
        // Appliquer le mode de couleur
        if (colorMode === 'light') {
            body.classList.add('genesis-light');
        } else if (colorMode === 'dark') {
            body.classList.add('genesis-dark');
        }
        
        // Mettre à jour l'état
        currentTheme.value = theme;
        currentColorMode.value = colorMode as 'light' | 'dark';
    }

    return {
        // État (readonly depuis l'extérieur)
        currentTheme: currentTheme as Readonly<typeof currentTheme>,
        currentColorMode: currentColorMode as Readonly<typeof currentColorMode>,
        
        // Actions
        detectVsCodeMode,
        applyTheme
    };
}