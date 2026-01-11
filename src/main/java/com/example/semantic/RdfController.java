package com.example.semantic;

import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.util.*;

@RestController
@RequestMapping("/api")
public class RdfController {

    private static final String RDF_FILE_PATH = "books_data.rdf";
    private static final String NS = "http://example.org/bookstore#"; 

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadRdf(@RequestParam("file") MultipartFile file) {
        try {
            Model model = ModelFactory.createDefaultModel();
            model.read(file.getInputStream(), null);
            return ResponseEntity.ok(modelToVisJs(model));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/addBook")
    public ResponseEntity<String> addBook(@RequestBody Map<String, String> payload) {
        try {
            Model model = loadModel();
            String bookId = payload.get("id");
            if (bookId == null || bookId.isEmpty()) return ResponseEntity.badRequest().body("ID required");

            Resource book = model.createResource(NS + bookId.replaceAll("\\s+", ""));
            Resource typeBook = model.createResource(NS + "Book");
            
            if (!model.contains(book, RDF.type, typeBook)) book.addProperty(RDF.type, typeBook);

            updateProperty(book, "hasTitle", payload.get("title"));
            updateProperty(book, "hasAuthor", payload.get("author"));           
            updateProperty(book, "hasTheme", payload.get("theme"));
            updateProperty(book, "suitableForLevel", payload.get("level"));

            saveModel(model);

            // Notify chatbot to reload data
            reloadChatbotData();

            return ResponseEntity.ok("Book saved!");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    private void reloadChatbotData() {
        try {
            java.net.URL url = new java.net.URL("http://localhost:5000/reload");
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                System.out.println("Chatbot data reloaded successfully");
            }
            conn.disconnect();
        } catch (Exception e) {
            System.err.println("Failed to reload chatbot data: " + e.getMessage());
        }
    }

    @GetMapping("/books")
    public ResponseEntity<List<Map<String, String>>> listBooks() {
        try {
            Model model = loadModel();
            List<Map<String, String>> books = new ArrayList<>();
            Resource typeBook = model.createResource(NS + "Book");

            ResIterator iter = model.listSubjectsWithProperty(RDF.type, typeBook);
            while (iter.hasNext()) {
                Resource book = iter.nextResource();
                books.add(Map.of(
                    "id", shortLabel(book.getURI()),
                    "title", getPropValue(book, "hasTitle"),
                    "author", getPropValue(book, "hasAuthor")
                ));
            }
            return ResponseEntity.ok(books);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/book/{id}")
    public ResponseEntity<Map<String, String>> getBookDetails(@PathVariable String id) {
        try {
            Model model = loadModel();
            Resource book = model.getResource(NS + id);
            
            if (!model.containsResource(book)) return ResponseEntity.notFound().build();

            Map<String, String> details = new HashMap<>();
            details.put("id", id);
            details.put("title", getPropValue(book, "hasTitle"));
            details.put("author", getPropValue(book, "hasAuthor"));
            details.put("level", getPropValue(book, "suitableForLevel"));

            List<String> themes = new ArrayList<>();
            StmtIterator it = book.listProperties(model.getProperty(NS + "hasTheme"));
            while(it.hasNext()) themes.add(it.nextStatement().getString());
            details.put("theme", String.join(", ", themes));

            return ResponseEntity.ok(details);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private Model loadModel() throws FileNotFoundException {
        Model model = ModelFactory.createDefaultModel();
        InputStream in = new FileInputStream(RDF_FILE_PATH);
        model.read(in, null);
        try { in.close(); } catch (IOException e) {} 
        return model;
    }

    private void saveModel(Model model) throws IOException {
        FileOutputStream out = new FileOutputStream(RDF_FILE_PATH);
        model.write(out, "RDF/XML");
        out.close();
    }

    private void updateProperty(Resource r, String propName, String value) {
        if (value != null && !value.isEmpty()) {
            Property p = r.getModel().createProperty(NS + propName);
            r.removeAll(p);
            r.addProperty(p, value);
        }
    }

    private String getPropValue(Resource r, String propName) {
        Property p = r.getModel().getProperty(NS + propName);
        if (r.hasProperty(p)) return r.getProperty(p).getString();
        return "Unknown";
    }

    private Map<String, Object> modelToVisJs(Model model) {
        List<Map<String, String>> nodes = new ArrayList<>();
        List<Map<String, String>> edges = new ArrayList<>();
        Set<String> addedNodes = new HashSet<>();
        StmtIterator iter = model.listStatements();
        while (iter.hasNext()) {
            Statement stmt = iter.nextStatement();
            String s = stmt.getSubject().toString();
            String p = stmt.getPredicate().getLocalName();
            String o = stmt.getObject().toString();
            if (addedNodes.add(s)) nodes.add(Map.of("id", s, "label", shortLabel(s), "color", "#97c2fc"));
            if (addedNodes.add(o)) nodes.add(Map.of("id", o, "label", shortLabel(o), "color", "#ffff00"));
            edges.add(Map.of("from", s, "to", o, "label", p, "arrows", "to"));
        }
        return Map.of("nodes", nodes, "edges", edges);
    }

    private String shortLabel(String uri) {
        if (uri.contains("#")) return uri.substring(uri.lastIndexOf("#") + 1);
        return uri;
    }
}