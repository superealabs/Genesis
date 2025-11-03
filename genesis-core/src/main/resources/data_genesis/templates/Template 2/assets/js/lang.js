const defaultLang = localStorage.getItem("lang") || "en";

async function loadLang(lang) {
    try {
        const response = await fetch(`/static/i18n/${lang}.json`);
        const translations = await response.json();

        document.querySelectorAll("[data-i18n]").forEach(el => {
            const key = el.getAttribute("data-i18n");
            if (translations[key]) {
                el.textContent = translations[key];
            }
        });

        document.documentElement.setAttribute("lang", lang);
        localStorage.setItem("lang", lang);

        const langSelect = document.getElementById("langSelect");
        if (langSelect) {
            langSelect.value = lang;
        }
    } catch (error) {
        console.error("Error loading language file:", error);
    }
}

document.addEventListener("DOMContentLoaded", () => {
    const langSelect = document.getElementById("langSelect");

    if (langSelect) {
        langSelect.addEventListener("change", (e) => {
            loadLang(e.target.value);
        });
    }

    loadLang(defaultLang);
});