async function loadDetails() {
    const params = new URLSearchParams(window.location.search);
    const id = params.get('id');

    const elements = {
        title: document.getElementById('bTitle'),
        author: document.getElementById('bAuthor'),
        theme: document.getElementById('bTheme'),
        level: document.getElementById('bLevel'),
        id: document.getElementById('bId')
    };

    if (!id) {
        elements.title.innerText = "No Book ID Provided";
        return;
    }

    try {
        const res = await fetch(`/api/book/${id}`);
        if (!res.ok) throw new Error("Book not found");
        
        const data = await res.json();
        
        elements.title.innerText = data.title || "Untitled";
        elements.author.innerText = data.author || "Unknown";
        elements.theme.innerText = data.theme || "N/A";
        elements.level.innerText = data.level || "N/A";
        elements.id.innerText = data.id;
    } catch (e) {
        elements.title.innerText = "Error loading book";
        console.error("Fetch error:", e);
    }
}

// Run on load
document.addEventListener('DOMContentLoaded', loadDetails);