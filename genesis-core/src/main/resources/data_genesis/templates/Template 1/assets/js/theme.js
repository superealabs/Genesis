document.addEventListener("DOMContentLoaded", () => {
    const btn = document.getElementById("settingsBtn");
    const menu = document.getElementById("settingsMenu");
    const themeSelect = document.getElementById("themeSelect");

    // Fonction pour appliquer le thème
    function applyTheme(theme) {
        document.documentElement.setAttribute('data-theme', theme);
    }

    // Charger le thème depuis le localStorage ou les préférences système
    function loadTheme() {
        const savedTheme = localStorage.getItem("theme");
        const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;

        // Priorité: localStorage > préférences système > light par défaut
        const theme = savedTheme || (prefersDark ? "dark" : "light");

        themeSelect.value = theme;
        applyTheme(theme);
    }

    // Événement de changement de thème
    themeSelect.addEventListener("change", () => {
        const theme = themeSelect.value;
        applyTheme(theme);
        localStorage.setItem("theme", theme);
    });

    // Observer les changements de préférences système
    window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', e => {
        // Seulement si aucun thème n'est sauvegardé dans localStorage
        if (!localStorage.getItem("theme")) {
            const newTheme = e.matches ? "dark" : "light";
            themeSelect.value = newTheme;
            applyTheme(newTheme);
        }
    });

    // Initialiser le thème au chargement
    loadTheme();

    // Charger le thème depuis le localStorage
    const savedTheme = localStorage.getItem("theme");
    if (savedTheme) {
        themeSelect.value = savedTheme;
        themeSelect.dispatchEvent(new Event("change"));
    }

    themeSelect.addEventListener("change", () => {
        const theme = themeSelect.value;
        localStorage.setItem("theme", theme);
        // appliquer les couleurs comme avant...
    });

    btn.addEventListener("click", () => {
        const isOpen = menu.style.display === "block";
        menu.style.display = isOpen ? "none" : "block";
        btn.classList.toggle("active", !isOpen);
    });

    document.addEventListener("click", (e) => {
        if (!btn.contains(e.target) && !menu.contains(e.target)) {
            menu.style.display = "none";
            btn.classList.remove("active");
        }
    });
});