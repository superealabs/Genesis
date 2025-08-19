function setupModal(modalId, triggerSelector, inputId, textId) {
    const modal = document.getElementById(modalId);
    const cancelBtn = modal.querySelector('.btn-cancel');
    const input = document.getElementById(inputId);
    const textSpan = document.getElementById(textId);

    // Boutons qui déclenchent le modal
    document.querySelectorAll(triggerSelector).forEach(btn => {
        btn.addEventListener('click', function () {
            const value = this.getAttribute('data-id');
            if (textSpan) textSpan.textContent = value;
            if (input) input.value = value;
            modal.style.display = 'flex';
        });
    });

    // Bouton Annuler
    cancelBtn.addEventListener('click', () => {
        modal.style.display = 'none';
    });

    // Fermer en cliquant à l'extérieur
    window.addEventListener('click', (e) => {
        if (e.target === modal) {
            modal.style.display = 'none';
        }
    });
}

// 🔹 Auto-init pour tous les modals présents
document.addEventListener('DOMContentLoaded', () => {
    document.querySelectorAll('.modal-overlay').forEach(modal => {
        const id = modal.id;
        const trigger = modal.dataset.trigger;
        const input = modal.dataset.input;
        const text = modal.dataset.text;

        if (id && trigger && input && text) {
            setupModal(id, trigger, input, text);
        }
    });
});
