document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("inputContainer");
    const addFilterBtn = document.getElementById("add-filter");
    const exportBtn = document.getElementById("export-btn");
    const dropdownMenu = document.getElementById("dropdown-menu");
    const exportOptions = document.getElementById("export-options");
    const templates = document.getElementById("filter-templates");
    const dropdownWrapper = addFilterBtn.closest(".dropdown");


    // Ouvrir/fermer le menu
    addFilterBtn.addEventListener("click", (e) => {
        e.stopPropagation();
        dropdownMenu.style.display = dropdownMenu.style.display === "block" ? "none" : "block";
    });

    // Fermer si clic en dehors
    document.addEventListener("click", () => {
        dropdownMenu.style.display = "none";
    });

    // Ouvrir/fermer le menu
    exportBtn.addEventListener("click", (e) => {
        e.stopPropagation();
        exportOptions.style.display = exportOptions.style.display === "block" ? "none" : "block";
    });

    // Fermer si clic en dehors
    document.addEventListener("click", () => {
        exportOptions.style.display = "none";
    });

    // Ajouter un champ depuis le menu
    dropdownMenu.addEventListener("click", (e) => {
        e.stopPropagation();

        const btn = e.target.closest('button[data-type]');
        if (!btn) return;

        const type = btn.getAttribute("data-type");

        // Récupérer et cloner le template
        const template = templates.querySelector(`.filter-content[data-type="${type}"]`);
        if (!template) return;

        const clone = template.cloneNode(true);
        clone.style.display = "";
        form.insertBefore(clone, dropdownWrapper);

        // Focus sur le premier champ
        const input = clone.querySelector("input, select, textarea");
        if (input) input.focus();

        // Retirer l'option choisie du dropdown
        btn.remove();

        // Si le menu est vide -> cacher Add
        if (!dropdownMenu.querySelector("button[data-type]")) {
            addFilterBtn.style.display = "none";
        }

        dropdownMenu.style.display = "none";
    });

    // Supprimer un filtre
    form.addEventListener("click", (e) => {
        const removeBtn = e.target.closest(".remove-item");
        if (removeBtn) {
            e.preventDefault();
            const block = removeBtn.closest(".filter-content");
            if (block) {
                const type = block.getAttribute("data-type");
                block.remove();

                // Réinsérer le bouton correspondant dans le dropdown
                const templateBtn = templates.querySelector(`.filter-content[data-type="${type}"]`);
                if (templateBtn) {
                    const newBtn = document.createElement("button");
                    newBtn.type = "button";
                    newBtn.setAttribute("data-type", type);
                    newBtn.textContent = type;
                    dropdownMenu.appendChild(newBtn);
                }

                // Réafficher Add si on a au moins 1 élément
                if (dropdownMenu.querySelector("button[data-type]")) {
                    addFilterBtn.style.display = "inline-block";
                }
            }
        }
    });
});
