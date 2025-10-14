// ===== CODE UNIFIÉ POUR SIDEBAR ET TOPBAR =====
// Toggle sidebar/topbar pour mobile
document.getElementById('menuToggle')?.addEventListener('click', function () {
    const sidebar = document.getElementById('sidebar');
    const topbar = document.getElementById('topbar');

    if (sidebar) {
        sidebar.classList.toggle('open');
    }
    if (topbar) {
        topbar.classList.toggle('active');
    }
});

// Récupérer tous les éléments de navigation UNE SEULE FOIS
const navItems = document.querySelectorAll('.nav-item');

// Navigation active - CENTRAGE POUR SIDEBAR ET TOPBAR
navItems.forEach(item => {
    item.addEventListener('click', function () {
        navItems.forEach(i => i.classList.remove('active'));
        this.classList.add('active');

        // Centrer l'élément pour sidebar ET topbar
        setTimeout(scrollToActiveItemRobust, 50);
    });
});

// Simulation de pagination
const paginationBtns = document.querySelectorAll('.pagination-btn');
paginationBtns.forEach(btn => {
    btn.addEventListener('click', function () {
        if (!this.classList.contains('active') && !this.disabled) {
            document.querySelector('.pagination-btn.active').classList.remove('active');
            this.classList.add('active');
        }
    });
});

// ===== GESTION DU DÉFILEMENT (SIDEBAR ET TOPBAR) =====

// Variables pour gérer le comportement de défilement
let isUserScrolling = false;
let scrollTimeout = null;

// Fonction pour vérifier la visibilité des boutons (sidebar)
function updateScrollButtonsVertical() {
    const navContent = document.getElementById('navContent');
    const scrollUp = document.getElementById('scrollUp');
    const scrollDown = document.getElementById('scrollDown');

    if (!navContent || !scrollUp || !scrollDown) return;

    const scrollTop = navContent.scrollTop;
    const scrollHeight = navContent.scrollHeight;
    const clientHeight = navContent.clientHeight;

    scrollUp.classList.toggle('hidden', scrollTop === 0);
    const isAtBottom = Math.abs(scrollTop + clientHeight - scrollHeight) < 2;
    scrollDown.classList.toggle('hidden', isAtBottom);
}

// Fonction pour vérifier la visibilité des boutons (topbar) AVEC EFFETS DE FLou
function updateScrollButtonsHorizontal() {
    const navContent = document.getElementById('navContent');
    const scrollLeft = document.getElementById('scrollLeft');
    const scrollRight = document.getElementById('scrollRight');
    const navigation = document.querySelector('.topbar .navigation');

    if (!navContent || !scrollLeft || !scrollRight) return;

    const canScrollLeft = navContent.scrollLeft > 0;
    const canScrollRight = navContent.scrollLeft + navContent.clientWidth < navContent.scrollWidth - 1;

    scrollLeft.classList.toggle('hidden', !canScrollLeft);
    scrollRight.classList.toggle('hidden', !canScrollRight);

    // Gérer les effets de flou sur les bords
    if (navigation) {
        navigation.classList.toggle('can-scroll-left', canScrollLeft);
        navigation.classList.toggle('can-scroll-right', canScrollRight);
    }
}

// Version robuste avec fallbacks et vérifications - CENTRAGE POUR TOPBAR AUSSI
function scrollToActiveItemRobust() {
    const navContent = document.getElementById('navContent');
    const activeItem = document.querySelector('.nav-item.active');

    if (!navContent || !activeItem) {
        return;
    }

    // Attendre que le layout soit stable
    setTimeout(() => {
        try {
            const sidebar = document.getElementById('sidebar');
            const topbar = document.getElementById('topbar');

            let targetScroll, maxScroll, clampedScroll;

            if (sidebar) {
                // SIDEBAR - Défilement vertical
                const navContentRect = navContent.getBoundingClientRect();
                const activeItemRect = activeItem.getBoundingClientRect();

                // Vérifier la visibilité
                if (navContentRect.height === 0 || activeItemRect.height === 0) {
                    setTimeout(scrollToActiveItemRobust, 100);
                    return;
                }

                const scrollTop = navContent.scrollTop;
                const itemOffset = activeItemRect.top - navContentRect.top + scrollTop;
                targetScroll = itemOffset - (navContentRect.height / 2) + (activeItemRect.height / 2);
                maxScroll = navContent.scrollHeight - navContent.clientHeight;
                clampedScroll = Math.max(0, Math.min(targetScroll, maxScroll));

                navContent.scrollTo({ top: clampedScroll, behavior: 'smooth' });

            } else if (topbar) {
                // TOPBAR - Défilement horizontal - CENTRAGE HORIZONTAL
                const navContentRect = navContent.getBoundingClientRect();
                const activeItemRect = activeItem.getBoundingClientRect();

                // Vérifier la visibilité
                if (navContentRect.width === 0 || activeItemRect.width === 0) {
                    setTimeout(scrollToActiveItemRobust, 100);
                    return;
                }

                const scrollLeft = navContent.scrollLeft;
                const itemOffset = activeItemRect.left - navContentRect.left + scrollLeft;
                targetScroll = itemOffset - (navContentRect.width / 2) + (activeItemRect.width / 2);
                maxScroll = navContent.scrollWidth - navContent.clientWidth;
                clampedScroll = Math.max(0, Math.min(targetScroll, maxScroll));

                navContent.scrollTo({ left: clampedScroll, behavior: 'smooth' });
            } else {
                return;
            }
        } catch (error) {
            console.error('Erreur lors du centrage:', error);
        }
    }, 10);
}

// Gestion du redimensionnement
let resizeTimeout;
function handleResize() {
    clearTimeout(resizeTimeout);
    resizeTimeout = setTimeout(() => {
        if (document.getElementById('sidebar') || document.getElementById('topbar')) {
            updateScrollButtonsHorizontal();
            updateScrollButtonsVertical();
            // Recentrer l'élément actif après redimensionnement
            setTimeout(scrollToActiveItemRobust, 100);
        }
    }, 250);
}

window.addEventListener('resize', handleResize);

// Fonction pour marquer le début du défilement utilisateur
function startUserScroll() {
    isUserScrolling = true;

    // Réinitialiser le timeout
    if (scrollTimeout) {
        clearTimeout(scrollTimeout);
    }

    // Marquer la fin du défilement utilisateur après un délai
    scrollTimeout = setTimeout(() => {
        isUserScrolling = false;
    }, 500);
}

// Initialisation du défilement selon le type de navigation
function initScrollBehavior() {
    const navContent = document.getElementById('navContent');
    const scrollUp = document.getElementById('scrollUp');
    const scrollDown = document.getElementById('scrollDown');
    const scrollLeft = document.getElementById('scrollLeft');
    const scrollRight = document.getElementById('scrollRight');

    // Défilement vertical (sidebar)
    if (scrollUp && scrollDown) {
        scrollDown.addEventListener('click', () => {
            isUserScrolling = true;
            navContent.scrollBy({ top: 100, behavior: 'smooth' });
            setTimeout(() => { isUserScrolling = false; }, 500);
        });

        scrollUp.addEventListener('click', () => {
            isUserScrolling = true;
            navContent.scrollBy({ top: -100, behavior: 'smooth' });
            setTimeout(() => { isUserScrolling = false; }, 500);
        });

        navContent.addEventListener('scroll', () => {
            updateScrollButtonsVertical();
            startUserScroll();
        });
    }

    // Défilement horizontal (topbar)
    if (scrollLeft && scrollRight) {
        scrollLeft.addEventListener('click', function () {
            isUserScrolling = true;
            navContent.scrollBy({ left: -200, behavior: 'smooth' });
            setTimeout(() => { isUserScrolling = false; }, 500);
        });

        scrollRight.addEventListener('click', function () {
            isUserScrolling = true;
            navContent.scrollBy({ left: 200, behavior: 'smooth' });
            setTimeout(() => { isUserScrolling = false; }, 500);
        });

        navContent.addEventListener('scroll', () => {
            updateScrollButtonsHorizontal();
            startUserScroll();
        });
    }
}

// ===== GESTION DES PARAMÈTRES (COMMUN) =====

// Gestion de la box flottante des paramètres
const settingsBtn = document.getElementById('settingsBtn');
const settingsFloating = document.getElementById('settingsFloating');

if (settingsBtn && settingsFloating) {
    // Ouvrir/fermer la box flottante
    settingsBtn.addEventListener('click', function(e) {
        e.stopPropagation();
        const isActive = settingsFloating.classList.contains('active');

        // Fermer toutes les boxes flottantes d'abord
        document.querySelectorAll('.settings-floating.active').forEach(box => {
            box.classList.remove('active');
        });
        document.querySelectorAll('.settings-btn.active').forEach(btn => {
            btn.classList.remove('active');
        });

        // Ouvrir/fermer cette box
        if (!isActive) {
            settingsFloating.classList.add('active');
            settingsBtn.classList.add('active');
        }
    });

    // Fermer la box en cliquant ailleurs
    document.addEventListener('click', function(e) {
        if (!settingsFloating.contains(e.target) && !settingsBtn.contains(e.target)) {
            settingsFloating.classList.remove('active');
            settingsBtn.classList.remove('active');
        }
    });

    // Empêcher la fermeture quand on clique dans la box
    settingsFloating.addEventListener('click', function(e) {
        e.stopPropagation();
    });
}

// ===== FONCTIONS DE DÉTECTION AUTOMATIQUE =====

// Détecter le type de navigation et initialiser les comportements appropriés
function detectNavigationType() {
    const sidebar = document.getElementById('sidebar');
    const topbar = document.getElementById('topbar');

    if (sidebar) {
        // Comportement sidebar
        initScrollBehavior();

        // Mettre à jour les boutons au chargement et centrer l'élément actif
        window.addEventListener('load', () => {
            updateScrollButtonsVertical();
            // Centrer au premier chargement pour sidebar
            setTimeout(scrollToActiveItemRobust, 100);
        });

        setTimeout(() => {
            updateScrollButtonsVertical();
        }, 100);
    }

    if (topbar) {
        // Comportement topbar
        initScrollBehavior();

        // Mettre à jour les boutons au chargement POUR TOPBAR AUSSI
        window.addEventListener('load', () => {
            updateScrollButtonsHorizontal();
            // Centrer au premier chargement pour topbar aussi
            setTimeout(scrollToActiveItemRobust, 100);
        });

        window.addEventListener('resize', () => {
            updateScrollButtonsHorizontal();
            // Recentrer après redimensionnement pour topbar
            setTimeout(scrollToActiveItemRobust, 50);
        });

        setTimeout(() => {
            updateScrollButtonsHorizontal();
            // Centrer après un court délai pour topbar
            setTimeout(scrollToActiveItemRobust, 50);
        }, 100);
    }
}

// ===== INITIALISATION AU CHARGEMENT =====

document.addEventListener('DOMContentLoaded', function() {
    // Détecter automatiquement le type de navigation
    detectNavigationType();

    // Charger les autres fonctionnalités communes
    loadSettings();
    loadTheme();
    optimizeExistingPagination();

    // Initialiser les modals
    document.querySelectorAll('.modal-overlay').forEach(modal => {
        const id = modal.id;
        const trigger = modal.dataset.trigger;
        const input = modal.dataset.input;
        const text = modal.dataset.text;

        if (id && trigger && input && text) {
            setupModal(id, trigger, input, text);
        }
    });

    // Initialiser le modal de suppression
    const deleteModal = document.getElementById('customModal');
    if (deleteModal) {
        const trigger = deleteModal.dataset.trigger;
        const input = deleteModal.dataset.input;
        const text = deleteModal.dataset.text;

        if (trigger && input && text) {
            setupModal('customModal', trigger, input, text);
        }
    }
});

// ===== FONCTIONS EXISTANTES (GARDÉES TELLES QUELLES) =====

// Sauvegarder les paramètres lors du changement
const settingsForm = document.getElementById('settingsForm');
if (settingsForm) {
    const settingsInputs = settingsForm.querySelectorAll('select');

    settingsInputs.forEach(input => {
        input.addEventListener('change', function() {
            // Sauvegarder les paramètres (exemple avec localStorage)
            const settings = {
                language: document.getElementById('language')?.value,
                theme: document.getElementById('theme')?.value,
                separator: document.getElementById('separator')?.value
            };
            localStorage.setItem('appSettings', JSON.stringify(settings));

            // Appliquer les changements immédiatement
            applySettings(settings);
        });
    });
}

// Charger les paramètres sauvegardés au démarrage
function loadSettings() {
    const savedSettings = localStorage.getItem('appSettings');
    if (savedSettings) {
        const settings = JSON.parse(savedSettings);
        if (document.getElementById('language')) document.getElementById('language').value = settings.language;
        if (document.getElementById('theme')) document.getElementById('theme').value = settings.theme;
        if (document.getElementById('separator')) document.getElementById('separator').value = settings.separator;
        applySettings(settings);
    }
}

// Appliquer les paramètres
function applySettings(settings) {
    // Appliquer le thème
    if (settings.theme === 'dark') {
        document.body.classList.add('dark-theme');
    } else {
        document.body.classList.remove('dark-theme');
    }
}

// Gestion du changement de thème avancée
const themeSelect = document.getElementById('theme');

// Détecter la préférence système
function getSystemTheme() {
    if (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches) {
        return 'dark';
    }
    return 'light';
}

// Charger le thème au démarrage
function loadTheme() {
    // Priorité : localStorage > préférence système > light
    const savedTheme = localStorage.getItem('appTheme');
    const systemTheme = getSystemTheme();

    const theme = savedTheme || systemTheme;
    if (themeSelect) themeSelect.value = theme;
    applyTheme(theme);
}

// Appliquer le thème
function applyTheme(theme) {
    const actualTheme = theme === 'dark' ? 'dark' : 'light';
    document.body.setAttribute('data-theme', actualTheme);
    localStorage.setItem('appTheme', actualTheme);

    // Mettre à jour l'interface si nécessaire
    // updateThemeDependentElements(actualTheme);
}

// // Mettre à jour les éléments dépendants du thème
// function updateThemeDependentElements(theme) {
//     // Exemple : mettre à jour les couleurs des graphiques, etc.
//     console.log(`Thème appliqué: ${theme}`);
// }

// Écouter les changements du sélecteur
if (themeSelect) {
    themeSelect.addEventListener('change', function() {
        applyTheme(this.value);
    });
}

// Écouter les changements de préférence système
if (window.matchMedia) {
    const colorSchemeQuery = window.matchMedia('(prefers-color-scheme: dark)');
    colorSchemeQuery.addEventListener('change', (e) => {
        // Seulement si l'utilisateur n'a pas choisi de préférence manuelle
        if (!localStorage.getItem('appTheme')) {
            const newTheme = e.matches ? 'dark' : 'light';
            if (themeSelect) themeSelect.value = newTheme;
            applyTheme(newTheme);
        }
    });
}

function setupModal(modalId, triggerSelector, inputId, textId) {
    const modal = document.getElementById(modalId);
    if (!modal) {
        return;
    }

    // Trouver les boutons de fermeture (adapté au nouveau style)
    const closeBtn = modal.querySelector('.export-close-btn');
    const cancelBtn = modal.querySelector('.btn-tertiary');
    const input = document.getElementById(inputId);
    const textSpan = document.getElementById(textId);

    // Boutons qui déclenchent le modal
    document.querySelectorAll(triggerSelector).forEach(btn => {
        btn.addEventListener('click', function () {
            const value = this.getAttribute('data-id');

            if (textSpan) {
                textSpan.textContent = value;
            }
            if (input) {
                input.value = value;
            }

            modal.classList.add('active');
            document.body.style.overflow = 'hidden';
        });
    });

    // Fonction pour fermer le modal
    function closeModal() {
        modal.classList.remove('active');
        document.body.style.overflow = '';
    }

    // Bouton de fermeture (X)
    if (closeBtn) {
        closeBtn.addEventListener('click', closeModal);
    }

    // Bouton Annuler
    if (cancelBtn) {
        cancelBtn.addEventListener('click', closeModal);
    }

    // Fermer avec la touche Escape
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape' && modal.classList.contains('active')) {
            closeModal();
        }
    });

    // Fermer en cliquant à l'extérieur
    modal.addEventListener('click', (e) => {
        if (e.target === modal) {
            closeModal();
        }
    });

    // Empêcher la fermeture quand on clique dans le contenu
    const modalContent = modal.querySelector('.modal-content');
    if (modalContent) {
        modalContent.addEventListener('click', (e) => {
            e.stopPropagation();
        });
    }
}

// Fonction pour générer les pages intelligentes
function generateSmartPages(currentPage, totalPages, maxVisible = 5) {
    const pages = [];

    if (totalPages <= maxVisible) {
        for (let i = 1; i <= totalPages; i++) {
            pages.push(i);
        }
        return pages;
    }

    const half = Math.floor(maxVisible / 2);
    let startPage = Math.max(1, currentPage - half);
    let endPage = Math.min(totalPages, currentPage + half);

    if (currentPage <= half + 1) {
        endPage = maxVisible;
    }

    if (currentPage > totalPages - half) {
        startPage = totalPages - maxVisible + 1;
    }

    if (startPage > 1) {
        pages.push(1);
        if (startPage > 2) {
            pages.push('...');
        }
    }

    for (let i = startPage; i <= endPage; i++) {
        pages.push(i);
    }

    if (endPage < totalPages) {
        if (endPage < totalPages - 1) {
            pages.push('...');
        }
        pages.push(totalPages);
    }

    return pages;
}

// Fonction pour analyser et optimiser la pagination existante
function optimizeExistingPagination() {
    const paginationContainers = document.querySelectorAll('.pagination');

    paginationContainers.forEach(container => {
        // Trouver la page active et compter le total des pages
        const pageLinks = Array.from(container.querySelectorAll('a:not(.prev):not(.next)'));
        let currentPage = 1;
        const totalPages = pageLinks.length;

        // Trouver la page active
        pageLinks.forEach(link => {
            if (link.classList.contains('active')) {
                currentPage = parseInt(link.textContent) || 1;
            }
        });

        // Générer les pages intelligentes
        const smartPages = generateSmartPages(currentPage, totalPages, 5);

        // Remplacer les pages existantes par les pages optimisées
        if (smartPages.length < pageLinks.length) {
            // Supprimer toutes les pages existantes (sauf prev/next)
            pageLinks.forEach(link => link.remove());

            // Ajouter les nouvelles pages optimisées dans l'ordre
            const prevBtn = container.querySelector('.prev');
            const nextBtn = container.querySelector('.next');

            // Insérer après le bouton prev et avant le bouton next
            smartPages.forEach(page => {
                if (page === '...') {
                    const ellipsis = document.createElement('span');
                    ellipsis.className = 'ellipsis';
                    ellipsis.textContent = '...';
                    container.insertBefore(ellipsis, nextBtn);
                } else {
                    const pageLink = document.createElement('a');
                    pageLink.className = page === currentPage ? 'active' : '';
                    pageLink.textContent = page;
                    pageLink.href = '#'; // Garder le lien
                    container.insertBefore(pageLink, nextBtn);
                }
            });
        }

        // Mettre à jour l'état des boutons prev/next
        const prevBtn = container.querySelector('.prev');
        const nextBtn = container.querySelector('.next');

        if (currentPage === 1) {
            prevBtn.classList.add('disabled');
        } else {
            prevBtn.classList.remove('disabled');
        }

        if (currentPage === totalPages) {
            nextBtn.classList.add('disabled');
        } else {
            nextBtn.classList.remove('disabled');
        }
    });
}

// CSS pour l'ellipsis (injecté une seule fois)
if (!document.querySelector('style[data-pagination-css]')) {
    const paginationCSS = `
    .ellipsis {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        padding: 8px 12px;
        color: var(--text-muted);
        user-select: none;
        margin: 0 2px;
    }
    `;

    const style = document.createElement('style');
    style.setAttribute('data-pagination-css', 'true');
    style.textContent = paginationCSS;
    document.head.appendChild(style);
}