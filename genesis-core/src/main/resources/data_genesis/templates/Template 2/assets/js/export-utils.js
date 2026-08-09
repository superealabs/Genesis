// Gestion du pop-up d'export
class ExportManager {
    constructor() {
        this.exportModal = document.getElementById('exportModal');
        this.exportBtn = document.getElementById('exportBtn');
        this.exportCloseBtn = document.getElementById('exportCloseBtn');
        this.exportCancelBtn = document.getElementById('exportCancelBtn');
        this.exportConfirmBtn = document.getElementById('exportConfirmBtn');
        this.init();
    }

    init() {
        this.setupEventListeners();
        this.injectToastCSS();
    }

    injectToastCSS() {
        // Vérifier si le style existe déjà
        if (document.getElementById('export-toast-style')) {
            return;
        }

        const toastCSS = `
.export-toast {
    position: fixed;
    top: 20px;
    right: 20px;
    background: var(--success-color);
    color: white;
    padding: 12px 16px;
    border-radius: var(--border-radius);
    box-shadow: var(--shadow-lg);
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 0.9rem;
    font-weight: 500;
    z-index: 3000;
    transform: translateX(100%);
    transition: transform 0.3s ease;
}

.export-toast.show {
    transform: translateX(0);
}

.loading-spinner {
    width: 14px;
    height: 14px;
    border: 2px solid transparent;
    border-top: 2px solid currentColor;
    border-radius: 50%;
    animation: spin 1s linear infinite;
}

@keyframes spin {
    0% { transform: rotate(0deg); }
    100% { transform: rotate(360deg); }
}
`;

        const styleElement = document.createElement('style');
        styleElement.id = 'export-toast-style';
        styleElement.textContent = toastCSS;
        document.head.appendChild(styleElement);
    }

    setupEventListeners() {
        // Vérifier que les éléments existent avant d'ajouter les listeners
        if (this.exportBtn) {
            this.exportBtn.addEventListener('click', () => {
                this.openModal();
            });
        }

        if (this.exportCloseBtn) {
            this.exportCloseBtn.addEventListener('click', () => {
                this.closeModal();
            });
        }

        if (this.exportCancelBtn) {
            this.exportCancelBtn.addEventListener('click', () => {
                this.closeModal();
            });
        }

        if (this.exportModal) {
            this.exportModal.addEventListener('click', (e) => {
                if (e.target === this.exportModal) {
                    this.closeModal();
                }
            });
        }

        if (this.exportConfirmBtn) {
            this.exportConfirmBtn.addEventListener('click', () => {
                this.generatePDF();
            });
        }

        // Mettre à jour l'aperçu quand les options changent
        document.querySelectorAll('input[name="exportScope"]').forEach(radio => {
            radio.addEventListener('change', () => {
                this.updatePreview();
            });
        });

        const orientationSelect = document.getElementById('exportOrientation');
        const formatSelect = document.getElementById('exportFormat');

        if (orientationSelect) {
            orientationSelect.addEventListener('change', () => {
                this.updatePreview();
            });
        }

        if (formatSelect) {
            formatSelect.addEventListener('change', () => {
                this.updatePreview();
            });
        }
    }

    openModal() {
        if (this.exportModal) {
            this.exportModal.classList.add('active');
            this.updatePreview();
            document.body.style.overflow = 'hidden';
        }
    }

    closeModal() {
        if (this.exportModal) {
            this.exportModal.classList.remove('active');
            document.body.style.overflow = '';
        }
    }

    updatePreview() {
        const scopeRadio = document.querySelector('input[name="exportScope"]:checked');
        const orientationSelect = document.getElementById('exportOrientation');
        const formatSelect = document.getElementById('exportFormat');
        
        if (!scopeRadio || !orientationSelect || !formatSelect) return;

        const scope = scopeRadio.value;
        const orientation = orientationSelect.value;
        const format = formatSelect.value;
        
        // Simuler le calcul des pages et éléments
        const filters = window.filtersManager ? window.filtersManager.getActiveFilters() : {};
        const itemCount = scope === 'current' ? this.estimateItemCount(filters) : 1000;
        const pageCount = this.estimatePageCount(itemCount, orientation, format);
        
        const pageCountElement = document.getElementById('pageCount');
        const dataCountElement = document.getElementById('dataCount');
        
        if (pageCountElement) pageCountElement.textContent = `~${pageCount} pages`;
        if (dataCountElement) dataCountElement.textContent = `~${itemCount} éléments`;
    }

    estimateItemCount(filters) {
        // Simulation basée sur les filtres
        let baseCount = 50;
        if (filters && Object.keys(filters).length > 0) {
            baseCount = Math.max(10, baseCount - Object.keys(filters).length * 10);
        }
        return baseCount;
    }

    estimatePageCount(itemCount, orientation, format) {
        const itemsPerPage = orientation === 'portrait' ? 25 : 40;
        return Math.ceil(itemCount / itemsPerPage);
    }

    generatePDF() {
        const scopeRadio = document.querySelector('input[name="exportScope"]:checked');
        const orientationSelect = document.getElementById('exportOrientation');
        const formatSelect = document.getElementById('exportFormat');
        
        if (!scopeRadio || !orientationSelect || !formatSelect) return;

        const scope = scopeRadio.value;
        const orientation = orientationSelect.value;
        const format = formatSelect.value;
        const filters = window.filtersManager ? window.filtersManager.getActiveFilters() : {};

        console.log('Génération PDF:', {
            scope,
            orientation,
            format,
            filters
        });

        // Simulation de génération PDF
        this.showLoadingState();
        
        setTimeout(() => {
            this.hideLoadingState();
            
            // Ici vous intégrerez votre vraie logique PDF
            // Pour l'instant, simulation de téléchargement
            this.simulatePDFDownload();
            
            this.closeModal();
            
            // Notification de succès
            this.showSuccessMessage();
            
        }, 2000);
    }

    showLoadingState() {
        if (this.exportConfirmBtn) {
            this.exportConfirmBtn.innerHTML = `
                <div class="loading-spinner"></div>
                Génération en cours...
            `;
            this.exportConfirmBtn.disabled = true;
        }
    }

    hideLoadingState() {
        if (this.exportConfirmBtn) {
            this.exportConfirmBtn.innerHTML = `
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M7 10l5 5 5-5M12 15V3"/>
                </svg>
                Générer le PDF
            `;
            this.exportConfirmBtn.disabled = false;
        }
    }

    simulatePDFDownload() {
        // Simulation de téléchargement
        const link = document.createElement('a');
        link.href = '#'; // Remplacez par l'URL de votre PDF
        link.download = `export-${new Date().toISOString().split('T')[0]}.pdf`;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
    }

    showSuccessMessage() {
        // Créer une notification toast
        const toast = document.createElement('div');
        toast.className = 'export-toast';
        toast.innerHTML = `
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M20 6L9 17l-5-5"/>
            </svg>
            PDF généré avec succès !
        `;
        
        document.body.appendChild(toast);
        
        setTimeout(() => {
            toast.classList.add('show');
        }, 100);
        
        setTimeout(() => {
            toast.classList.remove('show');
            setTimeout(() => {
                if (document.body.contains(toast)) {
                    document.body.removeChild(toast);
                }
            }, 300);
        }, 3000);
    }
}

// Initialisation
let exportManager;

// Attendre que le DOM soit chargé
document.addEventListener('DOMContentLoaded', function() {
    exportManager = new ExportManager();
});

// Ou si vous utilisez des modules ES6, exportez proprement
if (typeof module !== 'undefined' && module.exports) {
    module.exports = ExportManager;
}