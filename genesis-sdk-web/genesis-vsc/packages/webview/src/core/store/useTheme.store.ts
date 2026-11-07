// webview-ui/src/store/useThemeStore.ts
import { ref, computed } from 'vue';

// ═══ Types ═══
export type ColorMode = 'light' | 'dark' | 'auto';
export type ResolvedColorMode = 'light' | 'dark' | 'high-contrast';

// ═══ État global partagé ═══
const currentTheme = ref('genesis');
const currentColorMode = ref<ColorMode>('dark');       //  élargi
const systemPrefersDark = ref<boolean>(false);          //  nouveau

// ═══ Détection du mode système (une fois au démarrage) ═══
if (typeof window !== 'undefined' && window.matchMedia) {
    const media = window.matchMedia('(prefers-color-scheme: dark)');
    systemPrefersDark.value = media.matches;
    // Sync en cas de changement OS
    media.addEventListener('change', (e) => {
        systemPrefersDark.value = e.matches;
    });
}

/**
 * Réapplique le thème quand VS Code change de thème (classes vscode-* sur <body>),
 * uniquement si la préférence est 'auto'.
 * Retourne une fonction de nettoyage.
 */
export function watchVsCodeTheme(): () => void {
    const store = useThemeStore();
    let lastVsCode = store.detectVsCodeMode();

    const observer = new MutationObserver(() => {
        const now = store.detectVsCodeMode();
        if (now === lastVsCode) return;   // ignore nos propres mutations (theme-*, genesis-*)
        lastVsCode = now;
        if (store.currentColorMode.value === 'auto') {
            store.applyTheme(store.currentTheme.value, 'auto');
        }
    });

    observer.observe(document.body, { attributes: true, attributeFilter: ['class'] });
    return () => observer.disconnect();
}

export function useThemeStore() {

    function detectVsCodeMode(): 'light' | 'dark' | 'high-contrast' | 'unknown' {
        const body = document.body;
        if (body.classList.contains('vscode-light')) return 'light';
        if (body.classList.contains('vscode-dark')) return 'dark';
        if (body.classList.contains('vscode-high-contrast') || body.classList.contains('vscode-high-contrast-light')) return 'high-contrast';
        return 'unknown';
    }

    /**
     * Résout 'auto' en 'light' | 'dark'.
     * On combine 2 sources :
     *  1. Le thème VS Code (classe `vscode-dark` / `vscode-light` sur <body>) — prioritaire
     *  2. Le mode OS (`prefers-color-scheme`) — fallback
     */
    function resolveColorMode(mode: ColorMode): ResolvedColorMode {
        if (mode !== 'auto') return mode;

        const vscodeMode = detectVsCodeMode();
        if (vscodeMode === 'light' || vscodeMode === 'dark') return vscodeMode;
        if (vscodeMode === 'high-contrast') return 'high-contrast';

        // Fallback : préférence OS
        return systemPrefersDark.value ? 'dark' : 'light';
    }

    function applyTheme(theme: string, colorMode: string) {
        const body = document.body;

        // Normaliser : tout ce qui n'est pas light/dark/auto devient 'dark'
        const safeMode: ColorMode =
            colorMode === 'light' || colorMode === 'dark' || colorMode === 'auto'
                ? colorMode
                : 'dark';

        // 1) Stocker la PRÉFÉRENCE (peut être 'auto')
        currentTheme.value = theme;
        currentColorMode.value = safeMode;

        // 2) Nettoyer les anciennes classes
        body.classList.remove(
            'theme-genesis',
            'theme-vscode',
            'theme-mono',
            'genesis-light',
            'genesis-dark'
        );

        // 3) Appliquer le thème (nom)
        body.classList.add(`theme-${theme}`);

        // 4) Résoudre 'auto' → light/dark/high-contrast
        const resolved = resolveColorMode(safeMode);

        // 5) Appliquer le mode de couleur EFFECTIF
        //    (high-contrast → on laisse VS Code gérer via sa classe native)
        if (resolved === 'light') {
            body.classList.add('genesis-light');
        } else {
            // 'dark' et 'high-contrast' : on utilise le rendu sombre
            body.classList.add('genesis-dark');
        }
        // Pour 'high-contrast' : ne rien ajouter → VS Code applique ses propres couleurs
    }

    // ═══ Getters exposés ═══
    /** Préférence utilisateur ('light' | 'dark' | 'auto') — pour le switcher */
    const colorMode = computed<ColorMode>(() => currentColorMode.value);

    /** Mode effectif ('light' | 'dark' | 'high-contrast') — pour le CSS/JS */
    const resolvedColorMode = computed<ResolvedColorMode>(() =>
        resolveColorMode(currentColorMode.value)
    );

    
    return {
        // État readonly
        currentTheme: currentTheme as Readonly<typeof currentTheme>,
        currentColorMode: currentColorMode as Readonly<typeof currentColorMode>,
        colorMode,                  //  nouveau getter
        resolvedColorMode,          //  nouveau getter

        // Actions
        detectVsCodeMode,
        resolveColorMode,
        applyTheme
    };
}