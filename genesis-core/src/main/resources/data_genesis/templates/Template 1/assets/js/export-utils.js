class ExportManager {
    constructor() {
        this.exportModal = document.getElementById('exportModal');
        this.exportBtn = document.getElementById('exportBtn');
        this.exportCloseBtn = document.getElementById('exportCloseBtn');
        this.exportCancelBtn = document.getElementById('exportCancelBtn');
        this.exportConfirmBtn = document.getElementById('exportConfirmBtn');
        this.form = this.exportModal?.querySelector('form');
        this.init();
    }

    init() {
        this.setupEventListeners();
    }

    setupEventListeners() {
        if (this.exportBtn)
            this.exportBtn.addEventListener('click', () => this.openModal());

        if (this.exportCloseBtn)
            this.exportCloseBtn.addEventListener('click', () => this.closeModal());

        if (this.exportCancelBtn)
            this.exportCancelBtn.addEventListener('click', () => this.closeModal());

        if (this.exportModal)
            this.exportModal.addEventListener('click', e => {
                if (e.target === this.exportModal) this.closeModal();
            });

        if (this.form) {
            this.form.addEventListener('submit', () => this.showLoadingState());
        }
    }

    openModal() {
        this.exportModal?.classList.add('active');
        document.body.style.overflow = 'hidden';
    }

    closeModal() {
        this.exportModal?.classList.remove('active');
        document.body.style.overflow = '';
        this.resetButton();
    }

    showLoadingState() {
        if (this.exportConfirmBtn) {
            this.exportConfirmBtn.disabled = true;
            this.exportConfirmBtn.innerHTML = `
                <div class="loading-spinner"></div> Génération...
            `;
        }
        setTimeout(() => this.resetButton(), 5000);
    }

    resetButton() {
        if (this.exportConfirmBtn) {
            this.exportConfirmBtn.disabled = false;
            this.exportConfirmBtn.innerHTML = `
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M7 10l5 5 5-5M12 15V3"/>
                </svg>
                Générer
            `;
        }
    }
}

document.addEventListener('DOMContentLoaded', () => new ExportManager());
