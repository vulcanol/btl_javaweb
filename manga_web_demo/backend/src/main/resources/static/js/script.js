window.initSwipers = function () {
    const heroSwiperEl = document.querySelector('.heroSwiper');
    if (heroSwiperEl) {
        new Swiper('.heroSwiper', {
            loop: true,
            autoplay: { delay: 4000, disableOnInteraction: false },
            pagination: { el: '.swiper-pagination', clickable: true },
            grabCursor: true
        });
    }

    const updatedSwiperEl = document.querySelector('.updatedSwiper');
    if (updatedSwiperEl) {
        new Swiper('.updatedSwiper', {
            slidesPerView: 'auto',
            spaceBetween: 16,
            grabCursor: true,
            navigation: { nextEl: '.swiper-button-next' },
            slidesOffsetAfter: 20
        });
    }
};

const injectAuthComponents = () => {
    // Inject Modals
    if (!document.getElementById('loginModal')) {
        const modalHTML = `
            <!-- Login Modal -->
            <div class="modal fade auth-modal" id="loginModal" tabindex="-1" aria-hidden="true">
                <div class="modal-dialog modal-dialog-centered">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h4 class="modal-title fw-bold">Đăng nhập tài khoản</h4>
                            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                        </div>
                        <div class="modal-body">
                            <div class="mb-3">
                                <label class="form-label small fw-bold text-muted">Tên tài khoản</label>
                                <input type="text" id="loginUsername" class="form-control auth-input" placeholder="Nhập tên tài khoản">
                            </div>
                            <div class="mb-4">
                                <label class="form-label small fw-bold text-muted">Mật khẩu</label>
                                <input type="password" id="loginPassword" class="form-control auth-input" placeholder="••••••••">
                            </div>
                            <button id="btnSubmitLogin" class="btn-auth-submit">ĐĂNG NHẬP</button>
                            <div class="mt-4 text-center">
                                <p class="text-muted small">Chưa có tài khoản? <a href="#" class="text-dark fw-bold" data-bs-toggle="modal" data-bs-target="#registerModal">Đăng ký ngay</a></p>

                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Register Modal -->
            <div class="modal fade auth-modal" id="registerModal" tabindex="-1" aria-hidden="true">
                <div class="modal-dialog modal-dialog-centered">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h4 class="modal-title fw-bold">Đăng ký thành viên</h4>
                            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                        </div>
                        <div class="modal-body">
                            <div class="mb-3">
                                <label class="form-label small fw-bold text-muted">Tên tài khoản</label>
                                <input type="text" id="regUsername" class="form-control auth-input">
                            </div>
                            <div class="mb-3">
                                <label class="form-label small fw-bold text-muted">Email</label>
                                <input type="email" id="regEmail" class="form-control auth-input">
                            </div>
                            <div class="mb-3">
                                <label class="form-label small fw-bold text-muted">Tên hiển thị</label>
                                <input type="text" id="regDisplayName" class="form-control auth-input">
                            </div>
                            <div class="mb-4">
                                <label class="form-label small fw-bold text-muted">Mật khẩu</label>
                                <input type="password" id="regPassword" class="form-control auth-input">
                            </div>
                            <button id="btnSubmitRegister" class="btn-auth-submit">ĐĂNG KÝ</button>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Create Group Request Modal -->
            <div class="modal fade auth-modal" id="createGroupRequestModal" tabindex="-1" aria-hidden="true">
                <div class="modal-dialog modal-dialog-centered">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h4 class="modal-title fw-bold">Yêu cầu Tạo Nhóm Dịch</h4>
                            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                        </div>
                        <div class="modal-body">
                            <p class="text-secondary small mb-4">Để có thể đăng truyện, bạn cần phải có một nhóm dịch. Vui lòng gửi yêu cầu để Admin phê duyệt.</p>
                            <div class="mb-4">
                                <label class="form-label small fw-bold text-muted">Tên nhóm dịch muốn tạo</label>
                                <input type="text" id="reqGroupName" class="form-control auth-input" placeholder="Ví dụ: Cứu Truyện Team">
                            </div>
                            <button id="btnSubmitGroupRequest" class="btn-auth-submit">GỬI YÊU CẦU CHO ADMIN</button>
                        </div>
                    </div>
                </div>
            </div>
        `;
        document.body.insertAdjacentHTML('beforeend', modalHTML);
    }

    // User header injection removed as it's now in HTML for better design control
};

const setupAuthHandlers = () => {
    const btnSubmitLogin = document.getElementById('btnSubmitLogin');
    if (btnSubmitLogin) {
        btnSubmitLogin.addEventListener('click', async () => {
            const username = document.getElementById('loginUsername').value;
            const password = document.getElementById('loginPassword').value;


            try {
                btnSubmitLogin.disabled = true;
                const response = await ApiService.login(username, password);
                localStorage.setItem('token', response.token);
                localStorage.setItem('user', JSON.stringify(response));

                // Hide modal
                bootstrap.Modal.getInstance(document.getElementById('loginModal')).hide();
                window.location.reload();
            } catch (err) {
                alert("Lỗi: " + err.message);
            } finally {
                btnSubmitLogin.disabled = false;
            }
        });
    }

    const btnSubmitRegister = document.getElementById('btnSubmitRegister');
    if (btnSubmitRegister) {
        btnSubmitRegister.addEventListener('click', async () => {
            const username = document.getElementById('regUsername').value;
            const email = document.getElementById('regEmail').value;
            const displayName = document.getElementById('regDisplayName').value;
            const password = document.getElementById('regPassword').value;

            try {
                btnSubmitRegister.disabled = true;
                const data = { username, email, displayName, password };
                await ApiService.register(data);
                alert("Đăng ký thành công! Hãy đăng nhập.");
                
                // Switch to login modal
                bootstrap.Modal.getInstance(document.getElementById('registerModal')).hide();
                new bootstrap.Modal(document.getElementById('loginModal')).show();
            } catch (err) {
                alert("Lỗi: " + err.message);
            } finally {
                btnSubmitRegister.disabled = false;
            }
        });
    }


    const btnSubmitGroupRequest = document.getElementById('btnSubmitGroupRequest');
    if (btnSubmitGroupRequest) {
        btnSubmitGroupRequest.addEventListener('click', async () => {
            const groupName = document.getElementById('reqGroupName').value;
            if(!groupName) {
                alert("Vui lòng nhập tên nhóm dịch!");
                return;
            }
            
            try {
                btnSubmitGroupRequest.disabled = true;
                await ApiService.requestGroup(groupName);
                
                alert("Đã gửi yêu cầu tạo nhóm đến Admin! Vui lòng chờ phê duyệt.");
                bootstrap.Modal.getInstance(document.getElementById('createGroupRequestModal')).hide();
                document.getElementById('reqGroupName').value = '';
            } catch(err) {
                alert("Lỗi: " + err.message);
            } finally {
                btnSubmitGroupRequest.disabled = false;
            }
        });
    }


    const btnLogout = document.getElementById('btnLogout');
    if (btnLogout) {
        btnLogout.addEventListener('click', (e) => {
            e.preventDefault();
            localStorage.removeItem('token');
            localStorage.removeItem('user');
            window.location.href = window.location.pathname.includes('/pages/') ? '../index.html' : 'index.html';
        });
    }
};

const injectHeaderComponent = () => {
    const headerContainer = document.getElementById('app-header');
    if (!headerContainer) return;

    // Determine base path
    const isPagesDir = window.location.pathname.includes('/pages/');
    const basePath = isPagesDir ? '../' : './';
    const pagesPath = isPagesDir ? '' : 'pages/';
    const currentPath = window.location.pathname;

    let userData = null;
    try {
        userData = JSON.parse(localStorage.getItem('user'));
    } catch (e) {}

    // Tự động thăng cấp role nếu được Admin duyệt (Lưu ở localStorage)
    if (userData) {
        const approvedUploaders = JSON.parse(localStorage.getItem('approved_uploaders') || '[]');
        if (approvedUploaders.includes(userData.username) && userData.role.toLowerCase() === 'user') {
            userData.role = 'uploader';
            localStorage.setItem('user', JSON.stringify(userData));
        }
    }

    const role = userData ? userData.role.toLowerCase() : 'guest';
    const isSystemAdmin = role === 'admin';
    const canUpload = role === 'admin' || role === 'uploader' || role === 'translator';
    const canManageGroup = role === 'admin' || role === 'uploader';

    // Xây dựng menu bên trái
    let leftMenuHTML = `<a href="#" class="text-decoration-none fw-bold text-uppercase ${isSystemAdmin ? 'd-none' : ''}">Ủng hộ</a>
                        <a href="https://discord.gg/3vkD7hhdZ" target="_blank" class="text-decoration-none fw-bold text-uppercase">Discord</a>`;
    
    if (canUpload) {
        const uploadClass = currentPath.includes('dang-truyen.html') ? 'text-warning' : '';
        leftMenuHTML += `\n                        <a href="${pagesPath}dang-truyen.html" id="navUploadManga" class="text-decoration-none fw-bold text-uppercase ${uploadClass}">Đăng truyện</a>`;
    } else if (role !== 'guest') {
        leftMenuHTML += `\n                        <a href="#" id="navUploadManga" class="text-decoration-none fw-bold text-uppercase">Đăng truyện</a>`;
    }

    if (canManageGroup) {
        const groupClass = currentPath.includes('quan-ly-nhom.html') ? 'text-warning' : '';
        leftMenuHTML += `\n                        <a href="${pagesPath}quan-ly-nhom.html" id="navManageGroup" class="text-decoration-none fw-bold text-uppercase ${groupClass}">Quản lý nhóm</a>`;
    }

    if (isSystemAdmin) {
        const adminClass = currentPath.includes('quan-ly-he-thong.html') ? 'text-warning' : '';
        leftMenuHTML += `\n                        <a href="${pagesPath}quan-ly-he-thong.html" id="navSystemAdmin" class="text-decoration-none fw-bold text-uppercase ${adminClass}">Quản trị hệ thống</a>`;
    }

    const renderHeader = async () => {
        // Inject custom dynamic menus from Admin
        let customMenus = [];
        try {
            customMenus = await ApiService.getAllMenus() || [];
        } catch (e) {
            console.error("Failed to fetch menus from API");
        }

        customMenus.forEach(menu => {
            const roleArray = menu.roles ? menu.roles.split(',') : [];
            if (!menu.isHidden && (roleArray.includes('all') || roleArray.includes(role))) {
                let href = menu.url;
                if (!href.startsWith('http')) {
                    href = pagesPath + href;
                }
                const activeClass = currentPath.includes(menu.url.split('/').pop()) ? 'text-warning' : '';
                const safeTitle = window.escapeHTML ? window.escapeHTML(menu.title) : menu.title.replace(/</g, "&lt;").replace(/>/g, "&gt;");
                leftMenuHTML += `\n                        <a href="${href}" class="text-decoration-none fw-bold text-uppercase ${activeClass}">${safeTitle}</a>`;
            }
        });

    // Xây dựng block User/Guest
    let rightAuthHTML = '';
    if (role === 'guest') {
        rightAuthHTML = `
                        <div id="headerGuestView" class="d-flex align-items-center gap-2">
                            <button class="btn btn-outline-secondary fw-bold rounded-pill px-3" data-bs-toggle="modal" data-bs-target="#loginModal">Đăng nhập</button>
                            <button class="btn btn-dark fw-bold rounded-pill px-3" data-bs-toggle="modal" data-bs-target="#registerModal">Đăng ký</button>
                        </div>
        `;
    } else {
        const avatarChar = userData.username ? userData.username.charAt(0).toUpperCase() : 'U';
        rightAuthHTML = `
                        <div id="headerUserView" class="d-flex align-items-center gap-3">
                            <button class="btn-circular btn-icon"><i class="fa-solid fa-bell"></i></button>
                            <div class="dropdown">
                                <button class="btn-circular btn-user fw-bold border-0" type="button" data-bs-toggle="dropdown" aria-expanded="false">
                                    <span id="headerAvatarText">${avatarChar}</span>
                                </button>
                                <ul class="dropdown-menu dropdown-menu-end shadow border-0 mt-2 rounded-3">
                                    <li><a class="dropdown-item py-2 fw-medium ${currentPath.includes('truyen-theo-doi.html')?'active':''}" href="${pagesPath}truyen-theo-doi.html">Truyện theo dõi</a></li>
                                    <li><a class="dropdown-item py-2 fw-medium ${currentPath.includes('truyen-da-doc.html')?'active':''}" href="${pagesPath}truyen-da-doc.html">Truyện đã đọc</a></li>
                                    ${canUpload ? `<li><a class="dropdown-item py-2 fw-medium ${currentPath.includes('dang-truyen.html')?'active':''}" id="menuUploadManga" href="${pagesPath}dang-truyen.html">Đăng truyện</a></li>` : ''}
                                    <li><a class="dropdown-item py-2 fw-medium ${currentPath.includes('cai-dat.html')?'active':''}" href="${pagesPath}cai-dat.html">Cài đặt</a></li>
                                    <li><hr class="dropdown-divider"></li>
                                    <li><a class="dropdown-item py-2 text-danger fw-medium" id="btnLogout" href="#">Đăng xuất</a></li>
                                </ul>
                            </div>
                        </div>
        `;
    }

    const headerHTML = `
        <header class="cuutruyen-header sticky-top pt-3 pb-3 shadow-sm">
            <div class="container-fluid px-5">
                <div class="row align-items-center">
                    <div class="col-5 d-flex align-items-center gap-4 header-links">
                        ${leftMenuHTML}
                    </div>
                    <div class="col-2 text-center">
                        <a href="${basePath}index.html" class="text-decoration-none text-dark fs-4 fw-bold cuutruyen-logo">
                            CỨU TRUYỆN
                        </a>
                    </div>
                    <div class="col-5 d-flex justify-content-end align-items-center gap-3">
                        <button id="btnSearchToggle" class="btn-circular btn-icon"><i class="fa-solid fa-magnifying-glass"></i></button>
                        ${rightAuthHTML}
                    </div>
                </div>
            </div>
        </header>
    `;

    headerContainer.innerHTML = headerHTML;

    // Attach local events specific to header
    const uploadLinks = document.querySelectorAll('#navUploadManga');
    if (role !== 'guest' && !canUpload) {
        uploadLinks.forEach(link => {
            link.addEventListener('click', (e) => {
                e.preventDefault();
                new bootstrap.Modal(document.getElementById('createGroupRequestModal')).show();
            });
        });
    }

    document.querySelectorAll('#btnLogout').forEach(btn => {
        btn.addEventListener('click', (e) => {
            e.preventDefault();
            localStorage.removeItem('token');
            localStorage.removeItem('user');
            window.location.href = basePath + 'index.html';
        });
    });

    const bellIcon = document.querySelector('.fa-bell');
    if (bellIcon) {
        const bellBtn = bellIcon.closest('button') || bellIcon.closest('a');
        if (bellBtn) {
            bellBtn.addEventListener('click', (e) => {
                e.preventDefault();
                window.location.href = `${pagesPath}thong-bao.html`;
            });
        }
    }
    }; // end of renderHeader
    
    renderHeader();
};

document.addEventListener('DOMContentLoaded', () => {
    injectHeaderComponent();
    injectAuthComponents();
    setupAuthHandlers();

    // Initial check for non-dynamic pages
    if (!document.querySelector('.heroSwiper .swiper-wrapper')) {
        window.initSwipers();
    }

    // LIVE SEARCH
    const btnSearchToggle = document.getElementById('btnSearchToggle');
    const searchOverlay = document.getElementById('searchOverlay');
    const closeSearchBtn = document.getElementById('closeSearchBtn');
    const liveSearchInput = document.getElementById('liveSearchInput');
    const searchResults = document.getElementById('searchResults');
    const searchResultList = document.getElementById('searchResultList');

    if (btnSearchToggle && searchOverlay) {
        btnSearchToggle.addEventListener('click', () => {
            searchOverlay.classList.remove('d-none');
            setTimeout(() => liveSearchInput && liveSearchInput.focus(), 100);
        });

        closeSearchBtn.addEventListener('click', () => {
            searchOverlay.classList.add('d-none');
        });

        let typingTimer;
        if (liveSearchInput) {
            liveSearchInput.addEventListener('keydown', (e) => {
                if (e.key === 'Enter') {
                    const query = liveSearchInput.value.trim();
                    if (query) {
                        window.location.href = (window.location.pathname.includes('/pages/') ? '' : 'pages/') + `search.html?q=${encodeURIComponent(query)}`;
                    }
                }
            });

            const btnFullSearch = document.getElementById('btnFullSearch');
            if (btnFullSearch) {
                btnFullSearch.addEventListener('click', () => {
                    const query = liveSearchInput.value.trim();
                    if (query) {
                        window.location.href = (window.location.pathname.includes('/pages/') ? '' : 'pages/') + `search.html?q=${encodeURIComponent(query)}`;
                    }
                });
            }

            liveSearchInput.addEventListener('input', (e) => {
                const val = e.target.value.trim();
                clearTimeout(typingTimer);
                
                if (val.length >= 1) {
                    typingTimer = setTimeout(async () => {
                        try {
                            const response = await ApiService.searchMangas(val, 0, 10);
                            const matches = response.content || [];

                            if (searchResultList) {
                                if (matches.length > 0) {
                                    searchResultList.innerHTML = matches.map(m => {
                                        const safeTitle = window.escapeHTML ? window.escapeHTML(m.title) : String(m.title).replace(/</g, "&lt;").replace(/>/g, "&gt;");
                                        const safeType = window.escapeHTML ? window.escapeHTML(m.seriesType || 'Manga') : String(m.seriesType || 'Manga').replace(/</g, "&lt;").replace(/>/g, "&gt;");
                                        return `
                                        <a href="${window.location.pathname.includes('/pages/') ? '' : 'pages/'}chi-tiet-truyen.html?id=${m.seriesId}" class="list-group-item list-group-item-action d-flex align-items-center gap-3 p-3">
                                             <img src="${getImageUrl(m.coverUrl)}" alt="Cover" style="width: 50px; height: 70px; object-fit: cover; border-radius: 4px;">
                                             <div>
                                                 <h6 class="mb-1 fw-bold text-dark">${safeTitle}</h6>
                                                 <small class="text-muted">${safeType}</small>
                                             </div>
                                         </a>
                                    `;}).join('');
                                } else {
                                    searchResultList.innerHTML = `<div class="p-3 text-center text-muted">Không tìm thấy kết quả nào cho "${val}"</div>`;
                                }
                                searchResults.classList.remove('d-none');
                            }
                        } catch (err) { console.error(err); }
                    }, 300);
                } else {
                    if (searchResults) searchResults.classList.add('d-none');
                }
            });
        }
    }
});
