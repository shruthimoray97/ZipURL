/**
 * ZipURL Frontend Application Logic
 * Communicates with Spring Boot REST API (/api/urls)
 */

document.addEventListener('DOMContentLoaded', () => {
    // DOM Elements
    const urlForm = document.getElementById('urlForm');
    const originalUrlInput = document.getElementById('originalUrlInput');
    const shortenBtn = document.getElementById('shortenBtn');
    const spinner = shortenBtn.querySelector('.spinner');
    const btnText = shortenBtn.querySelector('.btn-text');
    const formErrorMessage = document.getElementById('formErrorMessage');

    const toggleCustomBtn = document.getElementById('toggleCustomBtn');
    const customCodeDrawer = document.getElementById('customCodeDrawer');
    const customCodeInput = document.getElementById('customCodeInput');

    const resultCard = document.getElementById('resultCard');
    const resultShortUrl = document.getElementById('resultShortUrl');
    const resultOriginalUrl = document.getElementById('resultOriginalUrl');
    const resultTimestamp = document.getElementById('resultTimestamp');
    const copyResultBtn = document.getElementById('copyResultBtn');
    const testRedirectBtn = document.getElementById('testRedirectBtn');
    const showQrBtn = document.getElementById('showQrBtn');

    const statTotalUrls = document.getElementById('statTotalUrls');
    const statTotalClicks = document.getElementById('statTotalClicks');
    const statRecentUrls = document.getElementById('statRecentUrls');

    const urlTableBody = document.getElementById('urlTableBody');
    const emptyState = document.getElementById('emptyState');
    const searchInput = document.getElementById('searchInput');
    const refreshBtn = document.getElementById('refreshBtn');

    const qrModal = document.getElementById('qrModal');
    const closeQrModal = document.getElementById('closeQrModal');
    const qrCodeContainer = document.getElementById('qrCodeContainer');
    const qrUrlLabel = document.getElementById('qrUrlLabel');
    const downloadQrBtn = document.getElementById('downloadQrBtn');

    const notFoundModal = document.getElementById('notFoundModal');
    const closeNotFoundBtn = document.getElementById('closeNotFoundBtn');
    const toastContainer = document.getElementById('toastContainer');

    let allUrlsData = [];
    let currentQrUrl = '';

    // Initialize Application
    init();

    function init() {
        check404Hash();
        fetchStats();
        fetchUrls();
        setupEventListeners();
    }

    function setupEventListeners() {
        // Form Submission
        urlForm.addEventListener('submit', handleUrlShorten);

        // Custom Code Drawer Toggle
        toggleCustomBtn.addEventListener('click', () => {
            customCodeDrawer.classList.toggle('hidden');
            if (!customCodeDrawer.classList.contains('hidden')) {
                customCodeInput.focus();
            }
        });

        // Copy Generated Result URL
        copyResultBtn.addEventListener('click', () => {
            const textToCopy = resultShortUrl.textContent;
            copyToClipboard(textToCopy, copyResultBtn);
        });

        // Table Actions Delegation
        urlTableBody.addEventListener('click', (e) => {
            const target = e.target.closest('button');
            if (!target) return;

            const urlId = target.getAttribute('data-id');
            const shortUrl = target.getAttribute('data-url');

            if (target.classList.contains('copy-btn')) {
                copyToClipboard(shortUrl, target);
            } else if (target.classList.contains('delete-btn')) {
                deleteUrl(urlId);
            }
        });

        // Search Filter
        searchInput.addEventListener('input', (e) => {
            const query = e.target.value.toLowerCase().trim();
            filterAndRenderTable(query);
        });

        // Refresh Button
        refreshBtn.addEventListener('click', () => {
            fetchStats();
            fetchUrls();
            showToast('Refreshed link list', 'success');
        });

        // QR Code Modal
        showQrBtn.addEventListener('click', () => {
            openQrModal(resultShortUrl.textContent);
        });

        closeQrModal.addEventListener('click', () => {
            qrModal.classList.add('hidden');
        });

        downloadQrBtn.addEventListener('click', downloadQrImage);

        // 404 Modal Close
        closeNotFoundBtn.addEventListener('click', () => {
            notFoundModal.classList.add('hidden');
            history.replaceState(null, null, ' ');
        });

        // Close Modals on Overlay Click
        window.addEventListener('click', (e) => {
            if (e.target === qrModal) qrModal.classList.add('hidden');
            if (e.target === notFoundModal) {
                notFoundModal.classList.add('hidden');
                history.replaceState(null, null, ' ');
            }
        });
    }

    // Check for 404 redirect hash from backend
    function check404Hash() {
        if (window.location.hash.startsWith('#404')) {
            notFoundModal.classList.remove('hidden');
        }
    }

    // Handle Form Submit
    async function handleUrlShorten(e) {
        e.preventDefault();
        hideError();

        let rawUrl = originalUrlInput.value.trim();
        const customCode = customCodeInput.value.trim();

        if (!rawUrl) {
            showError('Please enter a valid URL.');
            return;
        }

        // Auto-prepend https:// if missing scheme
        if (!/^https?:\/\//i.test(rawUrl)) {
            rawUrl = 'https://' + rawUrl;
            originalUrlInput.value = rawUrl;
        }

        setLoading(true);

        const payload = {
            originalUrl: rawUrl,
            customCode: customCode || null
        };

        try {
            const response = await fetch('/api/urls', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(payload)
            });

            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.message || 'Failed to shorten URL.');
            }

            // Display Result
            displayResult(data);
            showToast('URL shortened successfully!', 'success');

            // Refresh Table & Stats
            fetchStats();
            fetchUrls();

        } catch (err) {
            showError(err.message);
            showToast(err.message, 'error');
        } finally {
            setLoading(false);
        }
    }

    // Display Result Card
    function displayResult(urlData) {
        resultShortUrl.textContent = urlData.shortUrl;
        resultShortUrl.href = urlData.shortUrl;
        resultOriginalUrl.textContent = urlData.originalUrl;
        resultTimestamp.textContent = formatDate(urlData.createdAt);

        testRedirectBtn.href = urlData.shortUrl;

        resultCard.classList.remove('hidden');
        resultCard.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    }

    // Fetch Stats
    async function fetchStats() {
        try {
            const response = await fetch('/api/urls/stats');
            if (response.ok) {
                const stats = await response.json();
                statTotalUrls.textContent = stats.totalUrls.toLocaleString();
                statTotalClicks.textContent = stats.totalClicks.toLocaleString();
                statRecentUrls.textContent = stats.recentUrls.toLocaleString();
            }
        } catch (err) {
            console.error('Error fetching stats:', err);
        }
    }

    // Fetch URLs List
    async function fetchUrls() {
        try {
            const response = await fetch('/api/urls');
            if (response.ok) {
                allUrlsData = await response.json();
                filterAndRenderTable(searchInput.value.toLowerCase().trim());
            }
        } catch (err) {
            console.error('Error fetching URLs:', err);
        }
    }

    // Filter & Render Table
    function filterAndRenderTable(query = '') {
        const filtered = allUrlsData.filter(url => 
            url.shortUrl.toLowerCase().includes(query) ||
            url.shortCode.toLowerCase().includes(query) ||
            url.originalUrl.toLowerCase().includes(query)
        );

        urlTableBody.innerHTML = '';

        if (filtered.length === 0) {
            emptyState.classList.remove('hidden');
            return;
        }

        emptyState.classList.add('hidden');

        filtered.forEach(url => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>
                    <a href="${escapeHtml(url.shortUrl)}" target="_blank" rel="noopener" class="table-short-link">${escapeHtml(url.shortUrl)}</a>
                </td>
                <td>
                    <span class="table-original-link" title="${escapeHtml(url.originalUrl)}">${escapeHtml(url.originalUrl)}</span>
                </td>
                <td style="color: var(--text-muted); font-size: 0.85rem;">
                    ${formatDate(url.createdAt)}
                </td>
                <td>
                    <span class="click-badge">
                        <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
                        ${url.clickCount}
                    </span>
                </td>
                <td class="text-right">
                    <div class="table-actions">
                        <button class="btn-table-action copy-btn" data-url="${escapeHtml(url.shortUrl)}" title="Copy Link">
                            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="9" y="9" width="13" height="13" rx="2" ry="2"/><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/></svg>
                            Copy
                        </button>
                        <button class="btn-table-action delete-btn" data-id="${url.id}" title="Delete Link">
                            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
                            Delete
                        </button>
                    </div>
                </td>
            `;
            urlTableBody.appendChild(tr);
        });
    }

    // Delete URL
    async function deleteUrl(id) {
        if (!confirm('Are you sure you want to delete this shortened URL?')) return;

        try {
            const response = await fetch(`/api/urls/${id}`, {
                method: 'DELETE'
            });

            if (response.ok || response.status === 204) {
                showToast('URL deleted successfully', 'success');
                fetchStats();
                fetchUrls();
            } else {
                throw new Error('Failed to delete URL.');
            }
        } catch (err) {
            showToast(err.message, 'error');
        }
    }

    // Clipboard Copy Helper
    function copyToClipboard(text, btnElement) {
        navigator.clipboard.writeText(text).then(() => {
            showToast('Copied to clipboard!', 'success');

            if (btnElement) {
                const originalContent = btnElement.innerHTML;
                btnElement.classList.add('copied');
                btnElement.innerHTML = `
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>
                    Copied!
                `;

                setTimeout(() => {
                    btnElement.classList.remove('copied');
                    btnElement.innerHTML = originalContent;
                }, 2000);
            }
        }).catch(err => {
            console.error('Clipboard copy failed:', err);
            showToast('Failed to copy link', 'error');
        });
    }

    // Optional Clean QR Code Modal Generator
    function openQrModal(url) {
        currentQrUrl = url;
        qrUrlLabel.textContent = url;
        const encodedUrl = encodeURIComponent(url);
        qrCodeContainer.innerHTML = `<img src="https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=${encodedUrl}&color=080B14&bgcolor=FFFFFF" alt="QR Code" width="200" height="200">`;
        qrModal.classList.remove('hidden');
    }

    function downloadQrImage() {
        if (!currentQrUrl) return;
        const encodedUrl = encodeURIComponent(currentQrUrl);
        const qrApiUrl = `https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=${encodedUrl}`;
        window.open(qrApiUrl, '_blank');
    }

    // UI Helpers
    function setLoading(isLoading) {
        if (isLoading) {
            shortenBtn.disabled = true;
            spinner.classList.remove('hidden');
            btnText.style.opacity = '0.5';
        } else {
            shortenBtn.disabled = false;
            spinner.classList.add('hidden');
            btnText.style.opacity = '1';
        }
    }

    function showError(msg) {
        formErrorMessage.textContent = msg;
        formErrorMessage.classList.remove('hidden');
    }

    function hideError() {
        formErrorMessage.textContent = '';
        formErrorMessage.classList.add('hidden');
    }

    function showToast(message, type = 'success') {
        const toast = document.createElement('div');
        toast.className = `toast ${type}`;
        toast.innerHTML = `
            <span>${type === 'success' ? '✓' : '✕'}</span>
            <span>${escapeHtml(message)}</span>
        `;
        toastContainer.appendChild(toast);

        setTimeout(() => {
            toast.style.opacity = '0';
            toast.style.transform = 'translateX(20px)';
            setTimeout(() => toast.remove(), 300);
        }, 3000);
    }

    function formatDate(dateStr) {
        if (!dateStr) return 'N/A';
        const d = new Date(dateStr);
        return d.toLocaleDateString(undefined, {
            month: 'short',
            day: 'numeric',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    }

    function escapeHtml(str) {
        if (!str) return '';
        return str.replace(/[&<>"']/g, match => {
            const map = { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#039;' };
            return map[match];
        });
    }
});
