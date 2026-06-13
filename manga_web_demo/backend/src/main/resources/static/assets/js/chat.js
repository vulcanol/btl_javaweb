document.addEventListener('DOMContentLoaded', () => {
    const isInsidePages = window.location.pathname.includes('/pages/') || window.location.href.includes('/pages/');
    const imgPath = isInsidePages ? '../assets/img/anime_maid_icon.png' : 'assets/img/anime_maid_icon.png';
    const chatWidget = `
        <div class="anime-maid-chat-widget">
            <div class="chat-window" id="maidChatWindow">
                <div class="chat-header">
                    <span><i class="fa-solid fa-heart me-2"></i>Maid AI</span>
                    <span class="close-btn" id="closeMaidChat"><i class="fa-solid fa-xmark"></i></span>
                </div>
                <div class="chat-messages" id="maidChatMessages">
                    <div class="chat-message bot">
                        <div class="message-bubble">Chào Chủ nhân! Ngài cần em giúp gì ạ? ♡</div>
                    </div>
                </div>
                <div class="typing-indicator" id="maidTypingIndicator">Đang gõ...</div>
                <div class="chat-input-area">
                    <input type="text" id="maidChatInput" placeholder="Gửi tin nhắn cho Maid...">
                    <button id="maidChatSend"><i class="fa-solid fa-paper-plane"></i></button>
                </div>
            </div>
            <button class="anime-maid-icon-btn" id="openMaidChat">
                <img src="${imgPath}" alt="Maid Chat">
            </button>
        </div>
    `;

    document.body.insertAdjacentHTML('beforeend', chatWidget);

    const openBtn = document.getElementById('openMaidChat');
    const closeBtn = document.getElementById('closeMaidChat');
    const chatWindow = document.getElementById('maidChatWindow');
    const sendBtn = document.getElementById('maidChatSend');
    const chatInput = document.getElementById('maidChatInput');
    const chatMessages = document.getElementById('maidChatMessages');
    const typingIndicator = document.getElementById('maidTypingIndicator');

    openBtn.addEventListener('click', () => {
        chatWindow.classList.toggle('open');
        if(chatWindow.classList.contains('open')) {
            chatInput.focus();
        }
    });

    closeBtn.addEventListener('click', () => {
        chatWindow.classList.remove('open');
    });

    const appendMessage = (text, sender) => {
        const msgDiv = document.createElement('div');
        msgDiv.className = `chat-message ${sender}`;
        const safeText = window.escapeHTML ? window.escapeHTML(text) : text.replace(/</g, "&lt;").replace(/>/g, "&gt;");
        msgDiv.innerHTML = `<div class="message-bubble">${safeText.replace(/&#10;/g, '<br>').replace(/\\n/g, '<br>')}</div>`;
        chatMessages.appendChild(msgDiv);
        chatMessages.scrollTop = chatMessages.scrollHeight;
    };

    const sendMessage = async () => {
        const text = chatInput.value.trim();
        if (!text) return;

        appendMessage(text, 'user');
        chatInput.value = '';
        typingIndicator.style.display = 'block';
        chatMessages.scrollTop = chatMessages.scrollHeight;

        try {
            const token = localStorage.getItem('token');
            const headers = {
                'Content-Type': 'application/json'
            };
            if (token) {
                headers['Authorization'] = 'Bearer ' + token;
            }

            const baseUrl = (typeof API_CONFIG !== 'undefined' && API_CONFIG.BASE_URL) ? API_CONFIG.BASE_URL : 'http://localhost:8080/api';
            const response = await fetch(`${baseUrl}/ai/chat`, {
                method: 'POST',
                headers: headers,
                body: JSON.stringify({ message: text })
            });

            if (response.ok) {
                const data = await response.json();
                appendMessage(data.response, 'bot');
            } else {
                appendMessage('Xin lỗi Chủ nhân, em đang gặp chút trục trặc với Ollama (Lỗi kết nối).', 'bot');
            }
        } catch (error) {
            console.error('Chat error:', error);
            appendMessage('Xin lỗi Chủ nhân, em không thể kết nối tới máy chủ backend.', 'bot');
        } finally {
            typingIndicator.style.display = 'none';
        }
    };

    sendBtn.addEventListener('click', sendMessage);
    chatInput.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            sendMessage();
        }
    });
});
