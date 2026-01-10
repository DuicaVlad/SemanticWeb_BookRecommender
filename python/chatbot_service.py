from flask import Flask, request, jsonify
from flask_cors import CORS
from langchain_community.llms import Ollama
from langchain_community.embeddings import HuggingFaceEmbeddings
from langchain_community.vectorstores import Chroma
from langchain_core.documents import Document
import rdflib

app = Flask(__name__)
CORS(app) 

g = rdflib.Graph()
g.parse("books_data.rdf")

documents = []
for s, p, o in g:
    text = f"Book: {s.split('#')[-1]} has {p.split('#')[-1]} value {o.split('#')[-1]}"
    documents.append(Document(page_content=text))

vector_db = Chroma.from_documents(
    documents=documents, 
    embedding=HuggingFaceEmbeddings(model_name="all-MiniLM-L6-v2")
)

llm = Ollama(model="llama3")

@app.route('/chat', methods=['POST'])
def chat():
    user_query = request.json.get('message')
    
    # RAG: Find the 3 most relevant facts from your RDF
    docs = vector_db.similarity_search(user_query, k=3)
    context = "\n".join([d.page_content for d in docs])
    
    prompt = f"Using this data:\n{context}\n\nUser asked: {user_query}"
    response = llm.invoke(prompt)
    
    return jsonify({"response": response})

if __name__ == '__main__':
    app.run(port=5000)