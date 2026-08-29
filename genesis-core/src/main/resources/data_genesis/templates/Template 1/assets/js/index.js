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

const navItems = document.querySelectorAll('.nav-item');
const currentPath = window.location.pathname;

navItems.forEach(item => {
    const href = item.getAttribute('href');

    if (href && (currentPath === href || currentPath.startsWith(href + '/'))) {
        item.classList.add('active');
    }
});

navItems.forEach(item => {
    item.addEventListener('click', function () {
        navItems.forEach(i => i.classList.remove('active'));
        this.classList.add('active');

        setTimeout(scrollToActiveItemRobust, 50);
    });
});

const paginationBtns = document.querySelectorAll('.pagination-btn');
paginationBtns.forEach(btn => {
    btn.addEventListener('click', function () {
        if (!this.classList.contains('active') && !this.disabled) {
            document.querySelector('.pagination-btn.active').classList.remove('active');
            this.classList.add('active');
        }
    });
});

let isUserScrolling = false;
let scrollTimeout = null;

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

    if (navigation) {
        navigation.classList.toggle('can-scroll-left', canScrollLeft);
        navigation.classList.toggle('can-scroll-right', canScrollRight);
    }
}

function scrollToActiveItemRobust() {
    const navContent = document.getElementById('navContent');
    const activeItem = document.querySelector('.nav-item.active');

    if (!navContent || !activeItem) {
        return;
    }

    setTimeout(() => {
        try {
            const sidebar = document.getElementById('sidebar');
            const topbar = document.getElementById('topbar');

            let targetScroll, maxScroll, clampedScroll;

            if (sidebar) {
                const navContentRect = navContent.getBoundingClientRect();
                const activeItemRect = activeItem.getBoundingClientRect();

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
                const navContentRect = navContent.getBoundingClientRect();
                const activeItemRect = activeItem.getBoundingClientRect();

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

let resizeTimeout;
function handleResize() {
    clearTimeout(resizeTimeout);
    resizeTimeout = setTimeout(() => {
        if (document.getElementById('sidebar') || document.getElementById('topbar')) {
            updateScrollButtonsHorizontal();
            updateScrollButtonsVertical();
            setTimeout(scrollToActiveItemRobust, 100);
        }
    }, 250);
}

window.addEventListener('resize', handleResize);

function startUserScroll() {
    isUserScrolling = true;

    if (scrollTimeout) {
        clearTimeout(scrollTimeout);
    }

    scrollTimeout = setTimeout(() => {
        isUserScrolling = false;
    }, 500);
}

function initScrollBehavior() {
    const navContent = document.getElementById('navContent');
    const scrollUp = document.getElementById('scrollUp');
    const scrollDown = document.getElementById('scrollDown');
    const scrollLeft = document.getElementById('scrollLeft');
    const scrollRight = document.getElementById('scrollRight');

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

const settingsBtn = document.getElementById('settingsBtn');
const settingsFloating = document.getElementById('settingsFloating');

if (settingsBtn && settingsFloating) {
    settingsBtn.addEventListener('click', function(e) {
        e.stopPropagation();
        const isActive = settingsFloating.classList.contains('active');

        document.querySelectorAll('.settings-floating.active').forEach(box => {
            box.classList.remove('active');
        });
        document.querySelectorAll('.settings-btn.active').forEach(btn => {
            btn.classList.remove('active');
        });

        if (!isActive) {
            settingsFloating.classList.add('active');
            settingsBtn.classList.add('active');
        }
    });

    document.addEventListener('click', function(e) {
        if (!settingsFloating.contains(e.target) && !settingsBtn.contains(e.target)) {
            settingsFloating.classList.remove('active');
            settingsBtn.classList.remove('active');
        }
    });

    settingsFloating.addEventListener('click', function(e) {
        e.stopPropagation();
    });
}

function detectNavigationType() {
    const sidebar = document.getElementById('sidebar');
    const topbar = document.getElementById('topbar');

    if (sidebar) {
        initScrollBehavior();

        window.addEventListener('load', () => {
            updateScrollButtonsVertical();
            setTimeout(scrollToActiveItemRobust, 100);
        });

        setTimeout(() => {
            updateScrollButtonsVertical();
        }, 100);
    }

    if (topbar) {
        initScrollBehavior();

        window.addEventListener('load', () => {
            updateScrollButtonsHorizontal();
            setTimeout(scrollToActiveItemRobust, 100);
        });

        window.addEventListener('resize', () => {
            updateScrollButtonsHorizontal();
            setTimeout(scrollToActiveItemRobust, 50);
        });

        setTimeout(() => {
            updateScrollButtonsHorizontal();
            setTimeout(scrollToActiveItemRobust, 50);
        }, 100);
    }
}

document.addEventListener('DOMContentLoaded', function() {
    detectNavigationType();

    loadSettings();
    loadTheme();
    optimizeExistingPagination();

    document.querySelectorAll('.modal-overlay').forEach(modal => {
        const id = modal.id;
        const trigger = modal.dataset.trigger;
        const input = modal.dataset.input;
        const text = modal.dataset.text;

        if (id && trigger && input && text) {
            setupModal(id, trigger, input, text);
        }
    });

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

const settingsForm = document.getElementById('settingsForm');
if (settingsForm) {
    const settingsInputs = settingsForm.querySelectorAll('select');

    settingsInputs.forEach(input => {
        input.addEventListener('change', function() {
            const settings = {
                language: document.getElementById('language')?.value,
                theme: document.getElementById('theme')?.value,
                separator: document.getElementById('separator')?.value
            };
            localStorage.setItem('appSettings', JSON.stringify(settings));

            applySettings(settings);
        });
    });
}

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

function applySettings(settings) {
    if (settings.theme === 'dark') {
        document.body.classList.add('dark-theme');
    } else {
        document.body.classList.remove('dark-theme');
    }
}

const themeSelect = document.getElementById('theme');

function getSystemTheme() {
    if (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches) {
        return 'dark';
    }
    return 'light';
}

function loadTheme() {
    const savedTheme = localStorage.getItem('appTheme');
    const systemTheme = getSystemTheme();

    const theme = savedTheme || systemTheme;
    if (themeSelect) themeSelect.value = theme;
    applyTheme(theme);
}

function applyTheme(theme) {
    const actualTheme = theme === 'dark' ? 'dark' : 'light';
    document.body.setAttribute('data-theme', actualTheme);
    localStorage.setItem('appTheme', actualTheme);

    // updateThemeDependentElements(actualTheme);
}

// function updateThemeDependentElements(theme) {
//     console.log(`Thème appliqué: ${theme}`);
// }

if (themeSelect) {
    themeSelect.addEventListener('change', function() {
        applyTheme(this.value);
    });
}

if (window.matchMedia) {
    const colorSchemeQuery = window.matchMedia('(prefers-color-scheme: dark)');
    colorSchemeQuery.addEventListener('change', (e) => {
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

    const closeBtn = modal.querySelector('.export-close-btn');
    const cancelBtn = modal.querySelector('.btn-tertiary');
    const input = document.getElementById(inputId);
    const textSpan = document.getElementById(textId);
    const bodyTextSpan = document.getElementById('body'+textId);

    document.querySelectorAll(triggerSelector).forEach(btn => {
        btn.addEventListener('click', function () {
            const value = this.getAttribute('data-id');

            if (textSpan) {
                textSpan.textContent = value;
            }
            if (bodyTextSpan) {
                bodyTextSpan.textContent = value;
            }
            if (input) {
                input.value = value;
            }

            modal.classList.add('active');
            document.body.style.overflow = 'hidden';
        });
    });

    function closeModal() {
        modal.classList.remove('active');
        document.body.style.overflow = '';
    }

    if (closeBtn) {
        closeBtn.addEventListener('click', closeModal);
    }

    if (cancelBtn) {
        cancelBtn.addEventListener('click', closeModal);
    }

    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape' && modal.classList.contains('active')) {
            closeModal();
        }
    });

    modal.addEventListener('click', (e) => {
        if (e.target === modal) {
            closeModal();
        }
    });

    const modalContent = modal.querySelector('.modal-content');
    if (modalContent) {
        modalContent.addEventListener('click', (e) => {
            e.stopPropagation();
        });
    }
}

function generateSmartPages(currentPage, totalPages, maxVisible = 5) {
    const pages = [];

    if (totalPages <= maxVisible) {
        for (let i = 0; i <= totalPages; i++) {
            pages.push(i);
        }
        return pages;
    }

    const half = Math.floor(maxVisible / 2);
    let startPage = Math.max(0, currentPage - half);
    let endPage = Math.min(totalPages, currentPage + half);

    if (currentPage <= half) {
        endPage = Math.min(totalPages, maxVisible - 1);
    }

    if (currentPage >= totalPages - half) {
        startPage = Math.max(0, totalPages - maxVisible + 1);
    }

    if (startPage > 0) {
        pages.push(0);
        if (startPage > 1) {
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

function optimizeExistingPagination() {
    const paginationContainers = document.querySelectorAll('.pagination');

    paginationContainers.forEach(container => {
        const pageLinks = Array.from(container.querySelectorAll('a:not(.prev):not(.next)'));
        let currentPage = 0;
        const totalPages = pageLinks.length - 1;

        const pageUrls = {};
        pageLinks.forEach(link => {
            const pageNum = parseInt(link.textContent) || 0;
            pageUrls[pageNum] = link.getAttribute('href');

            if (link.classList.contains('active')) {
                currentPage = pageNum;
            }
        });

        const smartPages = generateSmartPages(currentPage, totalPages, 5);

        if (smartPages.length < pageLinks.length) {
            pageLinks.forEach(link => link.remove());

            const prevBtn = container.querySelector('.prev');
            const nextBtn = container.querySelector('.next');

            smartPages.forEach(page => {
                if (page === '...') {
                    const ellipsis = document.createElement('span');
                    ellipsis.className = 'ellipsis';
                    ellipsis.textContent = '...';
                    container.insertBefore(ellipsis, nextBtn);
                } else {
                    const pageLink = document.createElement('a');
                    pageLink.textContent = page;
                    pageLink.href = pageUrls[page] || '#';

                    if (page === currentPage) {
                        pageLink.classList.add('active');
                    }

                    container.insertBefore(pageLink, nextBtn);
                }
            });
        }
    });
}

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

const sidebar = document.getElementById('sidebar');
const sidebarToggleBtn = document.getElementById('sidebarToggleBtn');

const isMobile = () => window.innerWidth <= 992;

if (!isMobile() && localStorage.getItem('sidebarCollapsed') === 'true') {
    sidebar?.classList.add('collapsed');
}

if (isMobile()) {
    sidebar?.classList.add('open');
}

sidebarToggleBtn?.addEventListener('click', () => {
    sidebar.classList.toggle('collapsed');
    const isCollapsed = sidebar.classList.contains('collapsed');
    localStorage.setItem('sidebarCollapsed', isCollapsed);
});

window.addEventListener('resize', () => {
    if (isMobile()) {
        sidebar?.classList.remove('collapsed');
        sidebar?.classList.add('open');
    } else {
        if (localStorage.getItem('sidebarCollapsed') === 'true') {
            sidebar?.classList.add('collapsed');
        }
    }
});