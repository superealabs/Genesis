const defaultLang = localStorage.getItem("lang") || "en";

async function loadLang(lang) {
    const response = await fetch(`/i18n/${lang}.json`);
    const translations = await response.json();

    document.querySelectorAll("[data-i18n]").forEach(el => {
        const key = el.getAttribute("data-i18n");
        if (translations[key]) {
            el.textContent = translations[key];
        }
    });

    document.documentElement.setAttribute("lang", lang);

    localStorage.setItem("lang", lang);
    document.getElementById("langSelect").value = lang;
}

document.addEventListener("DOMContentLoaded", () => {
    document.getElementById("langSelect").addEventListener("change", (e) => {
        loadLang(e.target.value);
    });

    loadLang(defaultLang);
});