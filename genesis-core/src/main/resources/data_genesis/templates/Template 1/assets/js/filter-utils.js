class SimpleFilterManager {
    constructor(containerId) {
        this.container = document.getElementById(containerId);
        this.activeFilterIds = new Set();
        this.init();
    }

    init() {
        this.setupDOM();
        this.setupEventListeners();
        this.checkDefaultValues();
    }

    setupDOM() {
        this.activeFilters = this.container.querySelector('.active-filters');
        this.addFilterBtn = this.container.querySelector('#addFilterBtn');
        this.applyFilterBtn = this.container.querySelector('#applyFiltersBtn');
        this.filtersDropdown = this.container.querySelector('.filters-dropdown');
        this.filterWrapper = this.container.querySelector('.filter-add-wrapper');
        this.filterInputsContainer = this.container.querySelector('.filter-inputs-container');
        this.form = this.container.querySelector('form');
        this.filterControls = this.container.querySelector('.filter-controls');
        this.setupHiddenInputs();
    }

    setupHiddenInputs() {
        const createHiddenInput = (name) => {
            let input = this.form.querySelector(`input[name="${name}"]`);
            if (!input) {
                input = document.createElement('input');
                input.type = 'hidden';
                input.name = name;
                this.form.appendChild(input);
            }
            return input;
        };

        this.pageInput = createHiddenInput('page');
        this.pageSizeInput = createHiddenInput('pageSize');
        this.sortOrderInput = createHiddenInput('sortOrder');

        const urlParams = new URL(window.location.href).searchParams;
        const fallbackPageSize = document.querySelector('form.ongoing-page input[name="pageSize"]')?.value || "10";
        const fallbackSortOrder = document.querySelector('form.ongoing-page input[name="sortOrder"]')?.value || "";
        const fallbackPage = document.querySelector('form.ongoing-page select[name="page"]')?.value || "0";

        this.pageInput.value = urlParams.get('page') || fallbackPage;
        this.pageSizeInput.value = urlParams.get('pageSize') || fallbackPageSize;
        this.sortOrderInput.value = urlParams.get('sortOrder') || fallbackSortOrder;
    }

    setupEventListeners() {
        this.form.addEventListener('submit', (e) => {
            if (e.submitter && e.submitter.id === 'applyFiltersBtn') {
                this.pageInput.value = "0";
            }
        });

        document.querySelectorAll('.sort-link').forEach(link => {
            link.addEventListener('click', (e) => {
                e.preventDefault();
                const url = new URL(link.href);
                const sortByParam = url.searchParams.get('sortBy');
                if (sortByParam) {
                    const field = sortByParam.split(',')[0];
                    this.handleSortClick(field);
                }
            });
        });

        document.querySelectorAll('.pagination a').forEach(link => {
            link.addEventListener('click', (e) => {
                e.preventDefault();
                const url = new URL(link.href);
                const page = url.searchParams.get('page');
                if (page !== null) this.pageInput.value = page;
                const pageSize = url.searchParams.get('pageSize');
                if (pageSize !== null) this.pageSizeInput.value = pageSize;
                this.form.submit();
            });
        });

        const showingSelect = document.getElementById('showingSelect');
        if (showingSelect) {
            showingSelect.removeAttribute('onchange');
            showingSelect.addEventListener('change', (e) => {
                this.pageInput.value = "0";
                this.pageSizeInput.value = e.target.value;
                this.form.submit();
            });
        }

        const pageSelect = document.querySelector('form.ongoing-page select[name="page"]');
        if (pageSelect) {
            pageSelect.addEventListener('change', (e) => {
                this.pageInput.value = e.target.value;
                this.form.submit();
            });
        }

        this.setupResetSortButton();

        // Ouvrir/fermer le menu des filtres
        this.addFilterBtn.addEventListener('click', (e) => {
            e.preventDefault();
            e.stopImmediatePropagation();
            this.filterWrapper.classList.toggle('active');
        });

        // Fermer le menu en cliquant ailleurs
        document.addEventListener('click', (e) => {
            if (!this.filterWrapper.contains(e.target) && e.target !== this.addFilterBtn) {
                this.filterWrapper.classList.remove('active');
            }
        });

        // Empêcher la fermeture quand on clique dans le menu
        this.filterWrapper.addEventListener('click', (e) => {
            e.stopPropagation();
        });

        // Ajouter un filtre au clic sur une option
        this.container.querySelectorAll('.filter-option').forEach(option => {
            option.addEventListener('click', (e) => {
                e.preventDefault();
                e.stopPropagation();
                const filterId = option.dataset.filter;
                this.addFilter(filterId);
                this.filterWrapper.classList.remove('active');
            });
        });

        // Le bouton d'application garde son comportement par défaut (soumission du formulaire)
        // On ne fait rien de spécial
    }

    // Vérifier les valeurs par défaut et afficher les filtres correspondants
    checkDefaultValues() {
        this.filterInputsContainer.querySelectorAll('.filter-input-group').forEach(group => {
            const filterId = group.dataset.filter;
            const hasValue = this.hasInputValue(group);

            if (hasValue) {
                this.addFilter(filterId);
            }
        });
    }

    // Vérifier si un groupe d'inputs a une valeur
    hasInputValue(inputGroup) {
        const inputs = inputGroup.querySelectorAll('input, select');

        for (let input of inputs) {
            if (input.value && input.value !== '') {
                return true;
            }
        }
        return false;
    }

    reorderElements() {
        const activeFilters = this.activeFilters;
        const filterControls = this.filterControls;

        // Déplacer les boutons à la fin du container active-filters
        activeFilters.appendChild(filterControls);

        // Forcer le style pour que les boutons soient à droite
        filterControls.style.order = '999';
    }

    addFilter(filterId) {
        if (this.activeFilterIds.has(filterId)) return;

        this.activeFilterIds.add(filterId);

        const originalGroup = this.filterInputsContainer.querySelector(`[data-filter="${filterId}"]`);
        if (!originalGroup) return;

        const clonedGroup = originalGroup.cloneNode(true);

        const filterItem = document.createElement('div');
        filterItem.className = 'filter-item';
        filterItem.dataset.filterId = filterId;

        filterItem.innerHTML = `
            <div class="filter-item-content">
                ${clonedGroup.innerHTML}
            </div>
            <button class="filter-remove" type="button">
                <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M18 6L6 18M6 6l12 12"/>
                </svg>
            </button>
        `;

        // Gestion de la suppression
        filterItem.querySelector('.filter-remove').addEventListener('click', (e) => {
            e.preventDefault();
            this.removeFilter(filterId, filterItem);
        });

        this.activeFilters.appendChild(filterItem);
        this.updateFiltersState();
        this.reorderElements();
    }

    removeFilter(filterId, filterItem) {
        this.activeFilterIds.delete(filterId);
        filterItem.remove();
        this.updateFiltersState();
    }

    // SUPPRIMÉ : Les méthodes applyFilters et getActiveFilters ne sont plus nécessaires

    updateFiltersState() {
        // Mettre à jour l'état des options
        this.container.querySelectorAll('.filter-option').forEach(option => {
            const filterId = option.dataset.filter;
            if (this.activeFilterIds.has(filterId)) {
                option.style.opacity = '0.5';
                option.style.pointerEvents = 'none';
            } else {
                option.style.opacity = '1';
                option.style.pointerEvents = 'auto';
            }
        });
    }

    // Méthodes utilitaires
    clearFilters() {
        this.activeFilterIds.clear();
        this.activeFilters.innerHTML = '';
        this.updateFiltersState();
    }

    setFilterValue(filterId, value) {
        const inputGroup = this.filterInputsContainer.querySelector(`[data-filter="${filterId}"]`);
        if (!inputGroup) return;

        const inputs = inputGroup.querySelectorAll('input, select');

        if (Array.isArray(value)) {
            // Pour les filtres multiples (prix, date)
            inputs.forEach((input, index) => {
                if (value[index]) input.value = value[index];
            });
        } else {
            // Pour les filtres simples
            inputs[0].value = value;
        }

        // Ajouter le filtre s'il n'est pas déjà actif
        if (!this.activeFilterIds.has(filterId)) {
            this.addFilter(filterId);
        }
    }

    handleSortClick(field) {
        let currentSort = this.sortOrderInput.value || "";
        let sortParts = currentSort.split(';').filter(s => s.trim() !== "");
        let fieldIndex = sortParts.findIndex(s => s.startsWith(field + ','));
        
        if (fieldIndex !== -1) {
            let part = sortParts[fieldIndex];
            if (part.endsWith(',asc')) {
                sortParts[fieldIndex] = field + ',desc';
            } else {
                sortParts.splice(fieldIndex, 1);
            }
        } else {
            sortParts.push(field + ',asc');
        }
        
        this.sortOrderInput.value = sortParts.join(';');
        this.pageInput.value = "0"; 
        this.form.submit();
    }

    setupResetSortButton() {
        const orderByContent = document.querySelector('.order-by-content');
        if (!orderByContent) return;

        const currentSortValue = this.sortOrderInput?.value;
        const hasActiveSort = currentSortValue && currentSortValue.trim() !== "";

        const resetBtn = document.createElement('button');
        resetBtn.type = 'button';
        resetBtn.className = 'reset-sort-btn';
        resetBtn.title = 'Réinitialiser le tri';
        resetBtn.disabled = !hasActiveSort;
        resetBtn.innerHTML = `
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="18" y1="6" x2="6" y2="18"></line>
                <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
        `;

        resetBtn.addEventListener('click', () => {
            if (!resetBtn.disabled) {
                this.sortOrderInput.value = "";
                this.pageInput.value = "0";
                this.form.submit();
            }
        });

        orderByContent.appendChild(resetBtn);
    }
}

// Initialisation
const filtersManager = new SimpleFilterManager('filters-container');