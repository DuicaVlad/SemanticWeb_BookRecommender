from flask import Flask, request, jsonify
from flask_cors import CORS
from langchain_community.llms import Ollama
from langchain_community.embeddings import HuggingFaceEmbeddings
from langchain_community.vectorstores import Chroma
from langchain_core.documents import Document
import rdflib
from rdflib.namespace import RDF

app = Flask(__name__)
CORS(app)


g = rdflib.Graph()
g.parse("../books_data.rdf")


NS = rdflib.Namespace("http://example.org/bookstore#")


documents = []
book_metadata = {}

for s, p, o in g:
    subject = s.split('#')[-1] if '#' in str(s) else str(s).split('/')[-1]
    predicate = p.split('#')[-1] if '#' in str(p) else str(p).split('/')[-1]
    obj = o.split('#')[-1] if '#' in str(o) else str(o)

    text = f"{subject} {predicate} {obj}"
    documents.append(Document(page_content=text, metadata={"subject": subject, "predicate": predicate, "object": obj}))


    if subject not in book_metadata:
        book_metadata[subject] = {}
    book_metadata[subject][predicate] = obj

vector_db = Chroma.from_documents(
    documents=documents,
    embedding=HuggingFaceEmbeddings(model_name="all-MiniLM-L6-v2")
)

llm = Ollama(model="llama3")

@app.route('/chat', methods=['POST'])
def chat():
    user_query = request.json.get('message')
    context_page = request.json.get('context', 'index')
    book_id = request.json.get('bookId', None)


    docs = vector_db.similarity_search(user_query, k=5)
    context = "\n".join([d.page_content for d in docs])


    system_context = "You are a helpful book recommendation assistant. Answer questions based ONLY on the provided book database information."

    if book_id and book_id in book_metadata:
        book_info = book_metadata[book_id]
        system_context += f"\nCurrent book context: {book_id}"
        for key, value in book_info.items():
            system_context += f"\n- {key}: {value}"

    prompt = f"""{system_context}

Database information:
{context}

User question: {user_query}

Instructions:
- Answer based ONLY on the database information provided above
- Use the book titles, authors, themes, and reading levels from the database
- Be specific and mention book details when relevant
- Format your response in a clear, friendly way
- If the database doesn't have the information, say so clearly

Provide your answer:"""

    response = llm.invoke(prompt)

    return jsonify({"response": response})

@app.route('/conversation-starters', methods=['POST'])
def get_conversation_starters():
    context_page = request.json.get('context', 'index')
    book_id = request.json.get('bookId', None)

    starters = []

    if context_page == 'book_details' and book_id:

        if book_id in book_metadata:
            book_info = book_metadata[book_id]
            title = book_info.get('hasTitle', book_id)
            author = book_info.get('hasAuthor', 'Unknown')
            theme = book_info.get('hasTheme', 'Unknown')

            starters = [
                f"Tell me more about {title}",
                f"What other books did {author} write?",
                f"What other {theme} books do you have?"
            ]
        else:
            starters = [
                "Tell me more about this book",
                "Who is the author?",
                "What genre is this book?"
            ]
    else:

        starters = [
            "What is a book that I am most likely to enjoy from this list?",
            "Show me all Science Fiction books",
            "What books are suitable for beginners?"
        ]

    return jsonify({"starters": starters})

@app.route('/search-books', methods=['POST'])
def search_books():
    """Search books by theme and author"""
    theme = request.json.get('theme', '').strip()
    author = request.json.get('author', '').strip()

    results = []


    for book_id, metadata in book_metadata.items():
        match = True

        if theme and metadata.get('hasTheme', '').lower() != theme.lower():
            match = False

        if author and metadata.get('hasAuthor', '').lower() != author.lower():
            match = False

        if match and (theme or author):
            results.append({
                'id': book_id,
                'title': metadata.get('hasTitle', book_id),
                'author': metadata.get('hasAuthor', 'Unknown'),
                'theme': metadata.get('hasTheme', 'Unknown'),
                'level': metadata.get('suitableForLevel', 'Unknown')
            })

    return jsonify({"results": results})

@app.route('/reload', methods=['POST'])
def reload_data():
    """Reload RDF data and rebuild vector database"""
    global g, documents, book_metadata, vector_db

    try:
        g = rdflib.Graph()
        g.parse("../books_data.rdf")

        documents = []
        book_metadata = {}

        for s, p, o in g:
            subject = s.split('#')[-1] if '#' in str(s) else str(s).split('/')[-1]
            predicate = p.split('#')[-1] if '#' in str(p) else str(p).split('/')[-1]
            obj = o.split('#')[-1] if '#' in str(o) else str(o)

            text = f"{subject} {predicate} {obj}"
            documents.append(Document(page_content=text, metadata={"subject": subject, "predicate": predicate, "object": obj}))

            if subject not in book_metadata:
                book_metadata[subject] = {}
            book_metadata[subject][predicate] = obj

        vector_db = Chroma.from_documents(
            documents=documents,
            embedding=HuggingFaceEmbeddings(model_name="all-MiniLM-L6-v2")
        )

        return jsonify({"status": "success", "message": "Data reloaded successfully"})
    except Exception as e:
        return jsonify({"status": "error", "message": str(e)}), 500

if __name__ == '__main__':
    app.run(port=5000)