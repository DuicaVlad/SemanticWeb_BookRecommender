async function loadBooks() {
    try {
        const res = await fetch('/api/books');
        if (!res.ok) throw new Error("Failed to fetch books");
        
        const books = await res.json();
        const bookListContainer = document.getElementById('bookList');
        
        let html = '';
        books.forEach(b => {
            html += `
                <div class="book-item" onclick="window.location.href='book_details.html?id=${b.id}'">
                    <strong>${b.title}</strong> by ${b.author}
                </div>`;
        });
        
        bookListContainer.innerHTML = html || '<p>No books found.</p>';
    } catch(e) { 
        console.error("Error loading books:", e); 
    }
}

async function uploadAndVisualize() {
    const fileInput = document.getElementById('fileInput');
    if (fileInput.files.length === 0) { 
        alert("Please select an RDF file first."); 
        return; 
    }

    const formData = new FormData();
    formData.append('file', fileInput.files[0]);

    try {
        const response = await fetch('/api/upload', { 
            method: 'POST', 
            body: formData 
        });
        const data = await response.json();
        drawGraph(data);
    } catch (e) {
        console.error("Error uploading RDF:", e);
        alert("Error visualizing graph.");
    }
}

function drawGraph(data) {
    const container = document.getElementById('mynetwork');
    const visData = {
        nodes: new vis.DataSet(data.nodes),
        edges: new vis.DataSet(data.edges)
    };
    const options = {
        nodes: { 
            shape: 'dot', 
            font: { color: 'white' },
            size: 16
        },
        physics: {
            enabled: true,
            stabilization: { iterations: 150 }
        }
    };
    new vis.Network(container, visData, options);
}

async function addBook() {
    const bookData = {
        id: document.getElementById('bId').value,
        title: document.getElementById('bTitle').value,
        author: document.getElementById('bAuthor').value,
        theme: document.getElementById('bTheme').value,
        level: document.getElementById('bLevel').value
    };

    if (!bookData.id) { 
        alert("The Book ID is required!"); 
        return; 
    }

    try {
        const response = await fetch('/api/addBook', {
            method: 'POST', 
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(bookData)
        });

        if (response.ok) {
            alert("Book saved successfully!");
            document.querySelectorAll('input, select').forEach(el => el.value = '');
            loadBooks();
        } else {
            alert("Error saving book.");
        }
    } catch (e) {
        console.error("Submission error:", e);
    }
}

document.addEventListener('DOMContentLoaded', loadBooks);