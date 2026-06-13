/**
 * DYNAMIC NAVBAR - CỨU TRUYỆN
 * Fetch menu từ API /api/menus/tree và render navbar động
 */
(function () {
    'use strict';

    // Xác định base path
    const isPagesDir = window.location.pathname.includes('/pages/');
    const basePath   = isPagesDir ? '../' : './';
    const pagesPath  = isPagesDir ? '' : 'pages/';
    const currentPath = window.location.pathname;

    // Lấy user info từ localStorage
    function getUserData() {
        try { return JSON.parse(localStorage.getItem('user')); }
        catch (e) { return null; }
    }

    function isActive(url) {
        if (!url || url === '#') return false;
        const clean = url.split('?')[0].split('#')[0];
        return currentPath.endsWith(clean) || currentPath.includes(clean.replace('/pages/', ''));
    }

    function resolveUrl(url) {
        if (!url || url === '#') return '#';
        if (url.startsWith('http') || url.startsWith('//')) return url;
        // Nếu là đường dẫn tuyệt đối từ gốc web
        if (url.startsWith('/')) return url;
        return pagesPath + url;
    }

    /** Render danh sách dropdown items */
    function renderDropdownItems(children) {
        return children.map(child => {
            const href = resolveUrl(child.menuUrl);
            const icon = child.icon ? `<i class="${child.icon}"></i>` : '';
            const activeClass = isActive(child.menuUrl) ? 'active' : '';
            const target = child.target === '_blank' ? 'target="_blank" rel="noopener"' : '';
            return `<li>
                <a href="${href}" class="dyn-dropdown-item ${activeClass}" ${target}>
                    ${icon}<span>${escapeHtml(child.menuName)}</span>
                </a>
            </li>`;
        }).join('');
    }

    /** Render từng nav item (có thể có dropdown) */
    function renderNavItem(item) {
        const hasChildren = item.children && item.children.length > 0;
        const href = resolveUrl(item.menuUrl);
        const icon = item.icon ? `<i class="${item.icon} nav-icon"></i>` : '';
        const activeClass = isActive(item.menuUrl) ? 'active' : '';
        const target = item.target === '_blank' ? 'target="_blank" rel="noopener"' : '';

        if (hasChildren) {
            // Dropdown grid nếu nhiều hơn 5 items
            const gridClass = item.children.length > 5 ? 'dyn-dropdown-grid' : '';
            return `<li class="dyn-nav-item">
                <a href="${href}" class="dyn-nav-link ${activeClass}">
                    ${icon}<span>${escapeHtml(item.menuName)}</span>
                    <i class="fa-solid fa-chevron-down caret"></i>
                </a>
                <ul class="dyn-dropdown ${gridClass}">
                    ${renderDropdownItems(item.children)}
                </ul>
            </li>`;
        } else {
            return `<li class="dyn-nav-item">
                <a href="${href}" class="dyn-nav-link ${activeClass}" ${target}>
                    ${icon}<span>${escapeHtml(item.menuName)}</span>
                </a>
            </li>`;
        }
    }

    /** Render phần user (đăng nhập / avatar) */
    function renderUserSection(userData) {
        const role  = userData ? userData.role.toLowerCase() : 'guest';
        const isAdmin = role === 'admin';

        if (!userData) {
            return `
                <button class="dyn-btn-login" id="dynBtnLogin">Đăng nhập</button>
                <button class="dyn-btn-register" id="dynBtnRegister">Đăng ký</button>
            `;
        }

        const avatarChar = (userData.username || 'U').charAt(0).toUpperCase();
        const roleBadge  = isAdmin
            ? `<span class="dyn-admin-badge"><i class="fa-solid fa-shield-halved"></i>Admin</span>`
            : '';

        // Menu dropdown user items
        let userMenuItems = `
            <li><a href="${pagesPath}truyen-theo-doi.html" class="dyn-user-dropdown-item">
                <i class="fa-solid fa-bookmark"></i>Truyện theo dõi
            </a></li>
            <li><a href="${pagesPath}truyen-da-doc.html" class="dyn-user-dropdown-item">
                <i class="fa-solid fa-book-open"></i>Truyện đã đọc
            </a></li>
        `;

        if (role === 'admin' || role === 'uploader' || role === 'translator' || role === 'user') {
            userMenuItems += `<li><a href="${pagesPath}dang-truyen.html" class="dyn-user-dropdown-item">
                <i class="fa-solid fa-upload"></i>Đăng truyện
            </a></li>`;
        }

        if (role === 'admin' || role === 'uploader') {
            userMenuItems += `<li><a href="${pagesPath}quan-ly-nhom.html" class="dyn-user-dropdown-item">
                <i class="fa-solid fa-users"></i>Quản lý nhóm
            </a></li>`;
        }

        if (isAdmin) {
            userMenuItems += `<li><a href="${pagesPath}quan-ly-he-thong.html" class="dyn-user-dropdown-item">
                <i class="fa-solid fa-gear"></i>Quản trị hệ thống
            </a></li>`;
        }

        userMenuItems += `
            <li><a href="${pagesPath}cai-dat.html" class="dyn-user-dropdown-item">
                <i class="fa-solid fa-sliders"></i>Cài đặt
            </a></li>
            <li><div class="dyn-user-dropdown-divider"></div></li>
            <li><button class="dyn-user-dropdown-item danger" id="dynBtnLogout">
                <i class="fa-solid fa-right-from-bracket"></i>Đăng xuất
            </button></li>
        `;

        return `
            <a href="${pagesPath}thong-bao.html" class="dyn-btn-icon" title="Thông báo">
                <i class="fa-solid fa-bell"></i>
            </a>
            <div class="dyn-user-dropdown-wrap" id="dynUserDropWrap">
                <div class="dyn-user-avatar" id="dynUserAvatar" title="${escapeHtml(userData.username)}">
                    ${avatarChar}
                </div>
                <ul class="dyn-user-dropdown">
                    <li>
                        <div class="dyn-user-info">
                            <div class="uname">${escapeHtml(userData.displayName || userData.username)} ${roleBadge}</div>
                            <div class="urole">${escapeHtml(role)}</div>
                        </div>
                    </li>
                    ${userMenuItems}
                </ul>
            </div>
        `;
    }

    /** Render toàn bộ navbar HTML */
    function renderNavbar(menuTree) {
        const userData = getUserData();
        const menuItemsHTML = menuTree.map(renderNavItem).join('');
        const userSectionHTML = renderUserSection(userData);

        return `
        <nav class="dyn-navbar" role="navigation" aria-label="Main navigation">
            <div class="dyn-navbar-inner">
                <a href="${basePath}index.html" class="dyn-navbar-logo">
                    <div class="logo-icon"><i class="fa-solid fa-book-open"></i></div>
                    <span class="logo-text">CỨU TRUYỆN</span>
                </a>

                <button class="dyn-hamburger" id="dynHamburger" aria-label="Menu" aria-expanded="false">
                    <span></span><span></span><span></span>
                </button>

                <ul class="dyn-nav-menu" id="dynNavMenu">
                    ${menuItemsHTML}
                </ul>

                <div class="dyn-navbar-right">
                    <button class="dyn-btn-icon" id="dynSearchToggle" title="Tìm kiếm">
                        <i class="fa-solid fa-magnifying-glass"></i>
                    </button>
                    ${userSectionHTML}
                </div>
            </div>
        </nav>`;
    }

    /** Gắn các sự kiện sau khi render */
    function attachNavbarEvents() {
        // Hamburger mobile
        const hamburger = document.getElementById('dynHamburger');
        const navMenu   = document.getElementById('dynNavMenu');
        if (hamburger && navMenu) {
            hamburger.addEventListener('click', () => {
                const isOpen = navMenu.classList.toggle('open');
                hamburger.setAttribute('aria-expanded', String(isOpen));
            });
        }

        // Search toggle (kết nối với overlay hiện có)
        const searchToggle = document.getElementById('dynSearchToggle');
        const searchOverlay = document.getElementById('searchOverlay');
        const liveSearchInput = document.getElementById('liveSearchInput');
        if (searchToggle && searchOverlay) {
            searchToggle.addEventListener('click', () => {
                searchOverlay.classList.remove('d-none');
                setTimeout(() => liveSearchInput && liveSearchInput.focus(), 100);
            });
        }

        // User dropdown toggle
        const userAvatar = document.getElementById('dynUserAvatar');
        const userDropWrap = document.getElementById('dynUserDropWrap');
        if (userAvatar && userDropWrap) {
            userAvatar.addEventListener('click', (e) => {
                e.stopPropagation();
                userDropWrap.classList.toggle('open');
            });
            document.addEventListener('click', (e) => {
                if (!userDropWrap.contains(e.target)) {
                    userDropWrap.classList.remove('open');
                }
            });
        }

        // Login / Register buttons
        const dynBtnLogin = document.getElementById('dynBtnLogin');
        const dynBtnRegister = document.getElementById('dynBtnRegister');
        if (dynBtnLogin) {
            dynBtnLogin.addEventListener('click', () => {
                const modal = document.getElementById('loginModal');
                if (modal) new bootstrap.Modal(modal).show();
            });
        }
        if (dynBtnRegister) {
            dynBtnRegister.addEventListener('click', () => {
                const modal = document.getElementById('registerModal');
                if (modal) new bootstrap.Modal(modal).show();
            });
        }

        // Logout
        const btnLogout = document.getElementById('dynBtnLogout');
        if (btnLogout) {
            btnLogout.addEventListener('click', () => {
                localStorage.removeItem('token');
                localStorage.removeItem('user');
                window.location.href = basePath + 'index.html';
            });
        }
    }

    /** Skeleton loading */
    function renderSkeleton() {
        return `
        <nav class="dyn-navbar">
            <div class="dyn-navbar-inner">
                <a href="${basePath}index.html" class="dyn-navbar-logo">
                    <div class="logo-icon"><i class="fa-solid fa-book-open"></i></div>
                    <span class="logo-text">CỨU TRUYỆN</span>
                </a>
                <div class="dyn-nav-skeleton">
                    <span style="width:80px"></span>
                    <span style="width:80px"></span>
                    <span style="width:100px"></span>
                    <span style="width:70px"></span>
                </div>
            </div>
        </nav>`;
    }

    function escapeHtml(str) {
        if (!str) return '';
        return String(str)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;');
    }

    /** Main init */
    async function initDynamicNavbar() {
        const container = document.getElementById('dyn-navbar-container');
        if (!container) return;

        // Hiện skeleton ngay lập tức
        container.innerHTML = renderSkeleton();

        let menuTree = [];
        try {
            // Gọi API lấy cây menu
            if (window.ApiService && ApiService.getMenuTree) {
                menuTree = await ApiService.getMenuTree() || [];
            }
        } catch (e) {
            console.warn('DynNavbar: Không thể tải menu từ API, dùng menu mặc định', e);
            // Fallback menu khi API lỗi
            menuTree = [
                { id: 1, menuName: 'Trang chủ', menuUrl: '/index.html', icon: 'fa-solid fa-house', children: [] },
                { id: 2, menuName: 'Truyện mới', menuUrl: '/pages/truyen-moi.html', icon: 'fa-solid fa-fire', children: [] },
                { id: 3, menuName: 'Xếp hạng', menuUrl: '/pages/bang-xep-hang.html', icon: 'fa-solid fa-ranking-star', children: [] },
            ];
        }

        container.innerHTML = renderNavbar(menuTree);
        attachNavbarEvents();
    }

    // Khởi động sau khi DOM ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initDynamicNavbar);
    } else {
        initDynamicNavbar();
    }

    // Expose để trang khác có thể gọi refresh
    window.DynNavbar = { refresh: initDynamicNavbar };
})();
