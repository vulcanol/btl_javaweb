/**
 * API Service for CuuTruyen Platform
 * Handles all network requests to the Backend API
 */
const protocol = window.location.protocol === 'file:' ? 'http:' : window.location.protocol;
const host = (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1' || window.location.protocol === 'file:') ? 'localhost:8080' : window.location.host;
const API_CONFIG = {
    BASE_URL: `${protocol}//${host}/api`,
    TIMEOUT: 5000
};

// Global utility for XSS prevention
window.escapeHTML = function(str) {
    if (str === null || str === undefined) return '';
    const div = document.createElement('div');
    div.textContent = String(str);
    return div.innerHTML;
};
const escapeHTML = window.escapeHTML;

// Helper function to construct image URLs
window.getImageUrl = (coverUrl) => {
    if (!coverUrl) return null;

    // Explicitly block placeholder/mock URLs
    if (coverUrl.includes('placehold.co') || coverUrl.includes('placeholder')) {
        return null;
    }

    // If it's already a full URL, return as-is
    if (coverUrl.startsWith('http')) return coverUrl;

    const serverUrl = API_CONFIG.BASE_URL.replace('/api', '');
    // If it's a relative path starting with /, prepend server URL
    if (coverUrl.startsWith('/')) {
        return serverUrl + coverUrl;
    }
    // Otherwise it might be "uploads/...", prepend server URL with slash
    return serverUrl + '/' + coverUrl;
};

const getImageUrl = window.getImageUrl;

const ApiService = {
    async fetchWithTimeout(resource, options = {}) {
        const { timeout = API_CONFIG.TIMEOUT } = options;
        const controller = new AbortController();
        const id = setTimeout(() => controller.abort(), timeout);

        try {
            const token = localStorage.getItem('token');
            const headers = { 
                'Accept': 'application/json',
                ...options.headers 
            };
            if (token) {
                headers['Authorization'] = `Bearer ${token}`;
            }

            const response = await fetch(API_CONFIG.BASE_URL + resource, {
                ...options,
                headers,
                signal: controller.signal
            });
            clearTimeout(id);
            
            if (!response.ok) {
                const text = await response.text();
                let errorMsg = `HTTP error! status: ${response.status}`;
                try {
                    const json = JSON.parse(text);
                    if (json.message) errorMsg = json.message;
                } catch (e) {
                    if (text) errorMsg = text;
                }
                throw new Error(errorMsg);
            }
            
            const text = await response.text();
            return text ? JSON.parse(text) : null;
        } catch (error) {
            clearTimeout(id);
            console.error("API Call Failed:", error);
            throw error;
        }
    },

    // MANGA APIs
    async getMangas() {
        const response = await this.fetchWithTimeout("/manga");
        return response.content !== undefined ? response.content : response;
    },

    async getTopViewsMangas(limit = 10) {
        const response = await this.fetchWithTimeout(`/manga/top-views?limit=${limit}`);
        return response.content !== undefined ? response.content : response;
    },

    async getGenres() {
        return this.fetchWithTimeout("/manga/genres");
    },

    async searchMangas(query, page = 0, size = 20) {
        return this.fetchWithTimeout(`/manga/search?q=${encodeURIComponent(query)}&page=${page}&size=${size}`);
    },

    async getAllMangas(page = 0, size = 20) {
        return this.fetchWithTimeout(`/manga?page=${page}&size=${size}`);
    },

    async getMangasForAdmin(page = 0, size = 50) {
        return this.fetchWithTimeout(`/manga/admin?page=${page}&size=${size}`);
    },

    async getMangasByGroup(groupId) {
        return this.fetchWithTimeout(`/manga/group/${groupId}`);
    },

    async deleteManga(seriesId) {
        return this.fetchWithTimeout(`/manga/${seriesId}`, { method: "DELETE" });
    },

    async getMangaDetail(seriesId) {
        return this.fetchWithTimeout(`/manga/${seriesId}`);
    },

    async getChaptersBySeries(seriesId) {
        return this.fetchWithTimeout(`/chapter/series/${seriesId}`);
    },

    async toggleFavorite(seriesId, isFollowing) {
        return this.fetchWithTimeout(`/manga/${seriesId}/favorite?isFollowing=${isFollowing}`, {
            method: "POST"
        });
    },

    async rateManga(seriesId, rating) {
        return this.fetchWithTimeout(`/manga/${seriesId}/rate?rating=${rating}`, {
            method: "POST"
        });
    },

    async login(username, password) {
        return this.fetchWithTimeout("/auth/login", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ username, password })
        });
    },

    async register(userData) {
        return this.fetchWithTimeout("/auth/register", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(userData)
        });
    },

    async createManga(mangaData, coverFile, bannerFile) {
        const formData = new FormData();
        formData.append("title", mangaData.title);
        formData.append("alternativeTitle", mangaData.alternativeTitle);
        formData.append("description", mangaData.description);
        formData.append("seriesType", mangaData.seriesType);
        if (mangaData.genres && mangaData.genres.length > 0) {
            mangaData.genres.forEach(genre => formData.append("genres", genre));
        }
        if (coverFile) {
            formData.append("cover", coverFile);
        }
        if (bannerFile) {
            formData.append("banner", bannerFile);
        }

        const token = localStorage.getItem('token');
        const response = await fetch(API_CONFIG.BASE_URL + "/manga", {
            method: "POST",
            headers: {
                'Authorization': `Bearer ${token}`
            },
            body: formData
        });

        if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
        return await response.json();
    },

    async updateManga(seriesId, mangaData, coverFile, bannerFile) {
        const formData = new FormData();
        formData.append("title", mangaData.title);
        formData.append("alternativeTitle", mangaData.alternativeTitle);
        formData.append("description", mangaData.description);
        formData.append("seriesType", mangaData.seriesType);
        formData.append("status", mangaData.status);
        if (mangaData.genres && mangaData.genres.length > 0) {
            mangaData.genres.forEach(genre => formData.append("genres", genre));
        }
        if (coverFile) {
            formData.append("cover", coverFile);
        }
        if (bannerFile) {
            formData.append("banner", bannerFile);
        }

        const token = localStorage.getItem('token');
        const response = await fetch(API_CONFIG.BASE_URL + "/manga/" + seriesId, {
            method: "PUT",
            headers: {
                'Authorization': `Bearer ${token}`
            },
            body: formData
        });

        if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
        return await response.json();
    },



    async getChapter(chapterId) {
        return this.fetchWithTimeout(`/chapter/${chapterId}`);
    },

    async createChapter(chapterData) {
        return this.fetchWithTimeout("/chapter", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(chapterData)
        });
    },

    async uploadChapterPages(chapterId, files) {
        const formData = new FormData();
        Array.from(files).forEach(file => {
            formData.append("files", file);
        });

        // Use standard fetch here because fetchWithTimeout handles content-type automatically for JSON
        // but for FormData we want the browser to set it with the boundary
        const token = localStorage.getItem('token');
        const response = await fetch(API_CONFIG.BASE_URL + `/chapter/${chapterId}/upload`, {
            method: "POST",
            headers: {
                'Authorization': `Bearer ${token}`
            },
            body: formData
        });

        if (!response.ok) throw new Error(`Upload failed! status: ${response.status}`);
        return true;
    },

    async getChapterPages(chapterId) {
        return this.fetchWithTimeout(`/chapter/${chapterId}/pages`);
    },

    async getComments(chapterId) {
        return this.fetchWithTimeout(`/comment/chapter/${chapterId}`);
    },

    async addComment(commentData) {
        return this.fetchWithTimeout("/comment", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(commentData)
        });
    },

    // GROUP APIs
    async getAllGroups() {
        return this.fetchWithTimeout("/groups");
    },

    async getPendingGroups() {
        return this.fetchWithTimeout("/groups/pending");
    },

    async requestGroup(name) {
        return this.fetchWithTimeout("/groups/request", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ name })
        });
    },

    async acceptGroup(groupId) {
        return this.fetchWithTimeout(`/groups/${groupId}/accept`, {
            method: "PUT"
        });
    },

    async rejectGroup(groupId) {
        return this.fetchWithTimeout(`/groups/${groupId}/reject`, {
            method: "PUT"
        });
    },

    async deleteGroup(groupId) {
        return this.fetchWithTimeout(`/groups/${groupId}`, {
            method: "DELETE"
        });
    },



    async getProfile() {
        return this.fetchWithTimeout("/users/me");
    },

    async addMemberToGroup(groupId, userId, role) {
        return this.fetchWithTimeout(`/groups/${groupId}/members/${userId}`, {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ role: role })
        });
    },

    async updateEmail(email) {
        return this.fetchWithTimeout(`/users/me/email`, {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email })
        });
    },

    async updatePassword(currentPassword, newPassword) {
        return this.fetchWithTimeout(`/users/me/password`, {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ currentPassword, newPassword })
        });
    },

    // ME APIs (Personal Profile)
    async getMeFavorites() {
        return this.fetchWithTimeout("/me/favorites");
    },

    async getMeHistory() {
        return this.fetchWithTimeout("/me/history");
    },

    // GROUP APIs (cont.)
    async getGroupInfo(groupId) {
        return this.fetchWithTimeout(`/groups/${groupId}`);
    },

    async getGroupMembers(groupId) {
        return this.fetchWithTimeout(`/groups/${groupId}/members`);
    },

    async deleteGroupMember(groupId, userId) {
        return this.fetchWithTimeout(`/groups/${groupId}/members/${userId}`, {
            method: 'DELETE'
        });
    },

    async getMyGroup() {
        return this.fetchWithTimeout("/groups/my-group");
    },

    // MANGA APPROVAL APIs
    async getPendingManga() {
        return this.fetchWithTimeout("/manga/pending");
    },

    async getPendingMangasByGroup(groupId) {
        return this.fetchWithTimeout(`/manga/group/${groupId}/pending`);
    },

    async approveManga(seriesId) {
        return this.fetchWithTimeout(`/manga/${seriesId}/approve`, { method: "PUT" });
    },

    async rejectManga(seriesId) {
        return this.fetchWithTimeout(`/manga/${seriesId}/reject-manga`, { method: "PUT" });
    },

    // ADMIN APIs
    async getStats() {
        return this.fetchWithTimeout("/admin/stats");
    },

    async getAllComments() {
        return this.fetchWithTimeout("/comment/all");
    },

    async deleteCommentAdmin(commentId) {
        return this.fetchWithTimeout(`/comment/${commentId}/admin`, { method: "DELETE" });
    },

    async createGenre(genreName) {
        return this.fetchWithTimeout("/manga/genres", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ genreName })
        });
    },

    async updateGenre(genreId, genreName) {
        return this.fetchWithTimeout(`/manga/genres/${genreId}`, {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ genreName })
        });
    },

    async deleteGenre(genreId) {
        return this.fetchWithTimeout(`/manga/genres/${genreId}`, { method: "DELETE" });
    },

    async createReport(entityType, entityId, reason) {
        return this.fetchWithTimeout("/reports", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ entityType, entityId, reason })
        });
    },

    async getReports(status = "") {
        const query = status ? `?status=${status}` : "";
        return this.fetchWithTimeout(`/reports${query}`);
    },

    async resolveReport(reportId, status) {
        return this.fetchWithTimeout(`/reports/${reportId}/resolve`, {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ status })
        });
    },

    // MENU APIs
    async getAllMenus() {
        return this.fetchWithTimeout("/menus");
    },

    async getMenuTree() {
        return this.fetchWithTimeout("/menus/tree");
    },

    async createMenu(menuData) {
        return this.fetchWithTimeout("/menus", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(menuData)
        });
    },

    async updateMenu(id, menuData) {
        return this.fetchWithTimeout(`/menus/${id}`, {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(menuData)
        });
    },

    async deleteMenu(id) {
        return this.fetchWithTimeout(`/menus/${id}`, { method: "DELETE" });
    }
};

window.ApiService = ApiService;
