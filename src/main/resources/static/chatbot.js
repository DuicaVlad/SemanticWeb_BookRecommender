const CHATBOT_API_URL = 'http://localhost:5000';

let currentContext = 'index';
let currentBookId = null;

document.addEventListener('DOMContentLoaded', function() {
    initializeChatbot();
});

function initializeChatbot() {
    const pathname = window.location.pathname;
    if (pathname.includes('book_details.html')) {
        currentContext = 'book_details';
        const params = new URLSearchParams(window.location.search);
        currentBookId = params.get('id');
    } else {
        currentContext = 'index';
        currentBookId = null;
    }

    loadConversationStarters();

    document.getElementById('chatbot-toggle').addEventListener('click', toggleChatbot);
    document.getElementById('chat-close').addEventListener('click', closeChatbot);
    document.getElementById('chat-send').addEventListener('click', sendMessage);
    document.getElementById('chat-input').addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            sendMessage();
        }
    });
}

function toggleChatbot() {
    const chatWindow = document.getElementById('chatbot-window');
    const toggleBtn = document.getElementById('chatbot-toggle');

    if (chatWindow.classList.contains('hidden')) {
        chatWindow.classList.remove('hidden');
        toggleBtn.classList.add('active');
    } else {
        chatWindow.classList.add('hidden');
        toggleBtn.classList.remove('active');
    }
}

function closeChatbot() {
    const chatWindow = document.getElementById('chatbot-window');
    const toggleBtn = document.getElementById('chatbot-toggle');

    chatWindow.classList.add('hidden');
    toggleBtn.classList.remove('active');
}

async function loadConversationStarters() {
    try {
        const response = await fetch(`${CHATBOT_API_URL}/conversation-starters`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                context: currentContext,
                bookId: currentBookId
            })
        });

        if (!response.ok) {
            throw new Error('Failed to load conversation starters');
        }

        const data = await response.json();
        displayConversationStarters(data.starters);
    } catch (error) {
        console.error('Error loading conversation starters:', error);
        displayConversationStarters([
            'Tell me about the books',
            'What genres are available?',
            'Recommend a book for me'
        ]);
    }
}

function displayConversationStarters(starters) {
    const container = document.getElementById('conversation-starters');
    container.innerHTML = '';

    starters.forEach(starter => {
        const button = document.createElement('button');
        button.className = 'starter-btn';
        button.textContent = starter;
        button.addEventListener('click', function() {
            document.getElementById('chat-input').value = starter;
            sendMessage();
        });
        container.appendChild(button);
    });
}

async function sendMessage() {
    const input = document.getElementById('chat-input');
    const message = input.value.trim();

    if (!message) return;

    addMessageToChat('user', message);
    input.value = '';

    if (isBookSearchQuery(message)) {
        await handleBookSearch(message);
        return;
    }

    const typingIndicator = addTypingIndicator();

    try {
        const response = await fetch(`${CHATBOT_API_URL}/chat`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                message: message,
                context: currentContext,
                bookId: currentBookId
            })
        });

        if (!response.ok) {
            throw new Error('Failed to get response from chatbot');
        }

        const data = await response.json();

        typingIndicator.remove();

        addMessageToChat('bot', data.response);
    } catch (error) {
        console.error('Error sending message:', error);
        typingIndicator.remove();
        addMessageToChat('bot', 'Sorry, I encountered an error. Please make sure the chatbot service is running on port 5000.');
    }
}

function isBookSearchQuery(message) {
    const lowerMessage = message.toLowerCase();
    return (lowerMessage.includes('author') && lowerMessage.includes('theme')) ||
           (lowerMessage.includes('book') && lowerMessage.includes('author') && lowerMessage.includes('theme'));
}

async function handleBookSearch(message) {
    const authorMatch = message.match(/author[:\s]+([^,\s]+(?:\s+[^,\s]+)*)/i);
    const themeMatch = message.match(/theme[:\s]+([^,\s]+(?:\s+[^,\s]+)*)/i);

    const author = authorMatch ? authorMatch[1].trim() : '';
    const theme = themeMatch ? themeMatch[1].trim() : '';

    const typingIndicator = addTypingIndicator();

    try {
        const response = await fetch(`${CHATBOT_API_URL}/search-books`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                author: author,
                theme: theme
            })
        });

        if (!response.ok) {
            throw new Error('Failed to search books');
        }

        const data = await response.json();
        typingIndicator.remove();

        if (data.results.length === 0) {
            addMessageToChat('bot', `I couldn't find any books with author "${author}" and theme "${theme}".`);
        } else if (data.results.length === 1) {
            const book = data.results[0];
            addMessageToChat('bot', `I found "${book.title}" by ${book.author}. It's a ${book.theme} book suitable for ${book.level} readers.`);
        } else {
            let responseText = `I found ${data.results.length} books:\n\n`;
            data.results.forEach(book => {
                responseText += `• "${book.title}" by ${book.author} (${book.theme}, ${book.level})\n`;
            });
            addMessageToChat('bot', responseText);
        }
    } catch (error) {
        console.error('Error searching books:', error);
        typingIndicator.remove();
        addMessageToChat('bot', 'Sorry, I encountered an error while searching for books.');
    }
}

function addMessageToChat(sender, message) {
    const messagesContainer = document.getElementById('chat-messages');
    const messageDiv = document.createElement('div');
    messageDiv.className = `chat-message ${sender}-message`;

    const messageText = document.createElement('p');
    messageText.textContent = message;
    messageText.style.whiteSpace = 'pre-wrap';

    messageDiv.appendChild(messageText);
    messagesContainer.appendChild(messageDiv);

    messagesContainer.scrollTop = messagesContainer.scrollHeight;

    return messageDiv;
}

function addTypingIndicator() {
    const messagesContainer = document.getElementById('chat-messages');
    const typingDiv = document.createElement('div');
    typingDiv.className = 'chat-message bot-message typing-indicator';
    typingDiv.innerHTML = '<div class="typing-dots"><span></span><span></span><span></span></div>';

    messagesContainer.appendChild(typingDiv);
    messagesContainer.scrollTop = messagesContainer.scrollHeight;

    return typingDiv;
}
