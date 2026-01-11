# Book Recommendation System - Semantic Web Project

A semantic web-based book recommendation system with RDF/OWL ontology, SPARQL queries, and an AI-powered chatbot.

https://github.com/DuicaVlad/SemanticWeb_BookRecommender

## Team Members and Contributions

**Duica Vlad** - Exercises 1-6
- Created RDF/XML data model for the book recommendation scenario
- Implemented RDF graph visualization feature with vis.js
- Designed and developed the OWL ontology for the book recommendation domain
- Wrote 5 SPARQL queries for ontology querying
- Generated ontology visualizations and query screenshots

**Dragos Rascanu** - Exercises 3-4 improvements and additional features
- Enhanced and rebuilt the book management functionality
- Designed and implemented the navigation bar and UI components
- Improved overall application structure and user experience
- Assisted with chatbot backend integration and testing
- Helped with RDF data synchronization between Java and Python services

**Puscasu Tudor** - Chatbot implementation (Exercise 7)
- Implemented the complete AI chatbot system with RAG architecture
- Built the floating chat window UI with context-aware conversation starters
- Integrated Ollama and Llama 3 for natural language processing
- Developed the vector database system using ChromaDB and HuggingFace embeddings
- Implemented automatic data reload functionality for real-time book updates
- Created the Flask backend API for chatbot services

## GitHub Repository

[Add your public GitHub repository link here]

## About This Project

This project was developed for the Semantic Web course. We built a book recommendation system that combines semantic web technologies like RDF, OWL, and SPARQL with modern AI capabilities including RAG (Retrieval-Augmented Generation), vector databases, and large language models.

The application allows users to manage a book catalog, visualize relationships between books and users using RDF graphs, and interact with an intelligent chatbot that provides personalized book recommendations based on reading preferences and skill levels. The chatbot uses a vector database built from our RDF data to ensure all responses are based on our actual book collection rather than general knowledge.

## Main Features

**RDF/XML Data Model**
- Represents books, users, themes, and reading levels using RDF triples
- Models relationships between entities in a semantic format

**RDF Graph Visualization**
- Upload RDF/XML files through the web interface
- Visualize RDF graphs using vis.js library for interactive network visualization
- Explore relationships between books, authors, themes, and users

**Book Management**
- Add new books with title, author, theme, and reading level
- Modify existing book information
- Uses Apache Jena API for all RDF operations
- Data persists in RDF/XML format in books_data.rdf

**Book Listing and Details**
- Browse all available books in the catalog
- Dedicated detail page for each book showing all properties
- Clean and intuitive user interface with navigation bar

**OWL Ontology**
- Complete ontology for the book recommendation domain
- Includes classes (Book, User), object properties (recommends), and datatype properties (hasTitle, hasAuthor, etc.)
- Restrictions and axioms for reasoning
- Visualized using Protégé and GraphDB

**SPARQL Queries**
- Five SPARQL queries demonstrating different types of queries
- Queries for books by author, theme, and reading level
- User preference matching queries
- All queries documented in sparql_owl.txt with screenshots of results

**AI Chatbot with RAG**
- Floating chat window accessible on all pages
- Context-aware conversation starters that change based on the current page
- On book details page: questions about that specific book, its author, or genre
- On books list page: general recommendations and search queries
- RAG (Retrieval-Augmented Generation) using a vector database built from RDF data
- All responses based on actual book database, not general LLM knowledge
- Book search by natural language: example "What book has the author Frank Herbert and the theme Science Fiction"
- Automatic reload when books are added or modified through the web interface

## Technology Stack

**Backend Technologies**
- Java Spring Boot for the main web application server
- Apache Jena for RDF/SPARQL processing and manipulation
- Flask (Python) for the chatbot API service

**Frontend Technologies**
- HTML/CSS/JavaScript for the user interface
- vis.js library for RDF graph visualization

**AI and Machine Learning**
- Ollama + Llama 3 for local large language model inference
- LangChain for LLM orchestration and workflow management
- ChromaDB for vector database storage
- HuggingFace Embeddings (all-MiniLM-L6-v2 model) for text embeddings
- RDFLib for Python-based RDF processing

## Setup Instructions

**Prerequisites**
- Java 17 or higher
- Maven for building the Java application
- Python 3.8 or higher for the chatbot service
- Ollama for running local LLMs

**Step 1: Install Ollama and Download Llama 3**

Download and install Ollama from https://ollama.ai

After installation, download the Llama 3 model by running:
```bash
ollama run llama3
```

This will download the model which is about 4-5 GB. The first time may take several minutes depending on your internet connection.

**Step 2: Set Up Python Environment**

Navigate to the python directory:
```bash
cd SemanticWeb_BookRecommender/python
```

Create a virtual environment (optional but recommended):
```bash
python -m venv venv
```

Activate the virtual environment:
- On Windows: `venv\Scripts\activate`
- On Linux/Mac: `source venv/bin/activate`

Install Python dependencies:
```bash
pip install -r requirements.txt
```

**Step 3: Build and Run the Java Application**

Navigate to the project root:
```bash
cd SemanticWeb_BookRecommender
```

Build the project with Maven:
```bash
mvn clean install
```

Run the Spring Boot application:
```bash
mvn spring-boot:run
```

The web application will be available at http://localhost:8080

**Step 4: Start the Chatbot Service**

Open a new terminal window and navigate to the python directory:
```bash
cd SemanticWeb_BookRecommender/python
```

Activate the virtual environment if you created one.

Start the Flask chatbot service:
```bash
python chatbot_service.py
```

The chatbot API will be available at http://localhost:5000

Note: The chatbot automatically reads from books_data.rdf in the project root directory, which is where the Java application saves all book data.

## How to Use the Application

**Adding or Modifying Books**

1. Open the web application at http://localhost:8080
2. Navigate to the "Add or Modify" section
3. Enter the book details (ID is required, others are optional)
4. Click "Save Book"
5. The book is saved in RDF format to books_data.rdf
6. The chatbot automatically reloads the data and can immediately answer questions about the new book

**Visualizing RDF Graphs**

1. Navigate to the "Visualize RDF Graph" section
2. Click "Choose File" and select an RDF/XML file (you can use books_data.rdf)
3. Click "Upload & Visualize"
4. An interactive graph will be displayed showing all entities and their relationships
5. You can drag nodes and explore the graph structure

**Using the Chatbot**

1. Click the chat icon in the bottom-right corner of any page
2. The chat window will open with context-aware conversation starters
3. Click a conversation starter or type your own question
4. The chatbot will respond based on the data in your RDF database

Example queries you can try:
- "What books are suitable for beginners?"
- "Show me all Science Fiction books"
- "What book has the author Frank Herbert and the theme Science Fiction?"
- On a book detail page: "Tell me more about this book"

**Running SPARQL Queries**

All SPARQL queries are documented in the sparql_owl.txt file. To execute them:
- Open the ontology.owl file in Protégé
- Go to the SPARQL Query tab
- Copy and paste queries from sparql_owl.txt
- Execute and view results

Alternatively, you can use GraphDB for a web-based SPARQL interface.

## Project Structure

```
SemanticWeb_BookRecommender/
├── src/
│   └── main/
│       ├── java/com/example/semantic/
│       │   ├── SemanticApp.java          # Main Spring Boot application
│       │   └── RdfController.java        # REST API endpoints for RDF operations
│       └── resources/static/
│           ├── index.html                # Main page with navigation
│           ├── book_details.html         # Book detail page
│           ├── script.js                 # Main JavaScript file
│           ├── book_details.js           # Book details JavaScript
│           ├── chatbot.js                # Chatbot functionality
│           └── style.css                 # All styles including chatbot
├── python/
│   ├── chatbot_service.py                # Flask chatbot API
│   └── requirements.txt                  # Python dependencies
├── ontology.owl                          # OWL ontology file
├── books_data.rdf                        # RDF data store (created by Java app)
├── sparql_owl.txt                        # SPARQL queries with descriptions
├── pom.xml                               # Maven configuration
└── README.md                             # This file
```

## Assignment Completion Status

All requirements from the assignment have been completed:

- RDF/XML for book recommendation scenario (1 pt) - Done
- RDF graph visualization feature (1 pt) - Done
- Add/modify book feature using Jena API (0.5 pt) - Done
- List all books (0.5 pt) - Done
- Book detail pages (0.5 pt) - Done
- OWL ontology (1.5 pt) - Done
- 5 SPARQL queries (1 pt) - Done
- Floating chatbot window (0.5 pt) - Done
- Context-aware conversation starters (1 pt) - Done
- RAG-based responses from vector database (1.25 pt) - Done
- Book search by theme and author (1.25 pt) - Done

Total: 9/10 points

## Important Notes

- Both the Spring Boot application (port 8080) and Flask chatbot service (port 5000) must be running for full functionality
- First-time setup takes longer due to downloading the embeddings model and Llama 3
- The vector database is built in-memory on startup from the RDF file
- When you add or modify books through the web interface, the chatbot automatically reloads the data

## Troubleshooting

**Chatbot not responding**
- Check that the Flask service is running on port 5000
- Look for CORS errors in the browser console
- Verify Ollama is running by typing `ollama list` in the terminal

**RDF operations failing**
- Make sure books_data.rdf exists and has proper XML syntax
- Check file permissions if running on Linux/Mac
- Verify the RDF namespace is correct in the file

**Out of memory errors**
- Llama 3 requires significant RAM (at least 8GB recommended)
- If your computer struggles, you can modify chatbot_service.py to use cloud-based LLMs instead
- Options include Google AI Studio (Gemini API), OpenAI API, or OpenRouter.ai

## License

This is an academic project developed for the Semantic Web course.
