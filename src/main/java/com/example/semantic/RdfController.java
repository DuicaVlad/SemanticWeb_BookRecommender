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
public class RdfController 
{

    private static final String RDF_FILE_PATH = "books_data.rdf";
    private static final String NS = "http://example.org/bookstore#"; 

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadRdf(@RequestParam("file") MultipartFile file) {
        try {
            Model model = ModelFactory.createDefaultModel();
            model.read(file.getInputStream(), null);
            return ResponseEntity.ok(modelToVisJs(model));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/addBook")
    public ResponseEntity<String> addBook(@RequestBody Map<String, String> payload) {
        try {
            // 1. Load the existing RDF file
            Model model = ModelFactory.createDefaultModel();
            InputStream in = new FileInputStream(RDF_FILE_PATH);
            model.read(in, null);
            in.close();

            String bookId = payload.get("id");
            String title = payload.get("title");
            String author = payload.get("author");
            String theme = payload.get("theme");
            String level = payload.get("level");

            if (bookId == null || bookId.isEmpty()) {
                return ResponseEntity.badRequest().body("Book ID is required.");
            }

            String validId = bookId.replaceAll("\\s+", "");
            Resource book = model.createResource(NS + validId);

            Property pTitle = model.createProperty(NS + "hasTitle");
            Property pAuthor = model.createProperty(NS + "hasAuthor");
            Property pTheme = model.createProperty(NS + "hasTheme");
            Property pLevel = model.createProperty(NS + "suitableForLevel");
            Resource typeBook = model.createResource(NS + "Book");

            if (!model.contains(book, RDF.type, typeBook)) {
                book.addProperty(RDF.type, typeBook);
            }

            if (title != null && !title.isEmpty()) {
                book.removeAll(pTitle); 
                book.addProperty(pTitle, title);
            }
            if (author != null && !author.isEmpty()) {
                book.removeAll(pAuthor);
                book.addProperty(pAuthor, author);
            }
            if (theme != null && !theme.isEmpty()) {
                book.addProperty(pTheme, theme);
            }
            if (level != null && !level.isEmpty()) {
                book.removeAll(pLevel);
                book.addProperty(pLevel, level);
            }

            FileOutputStream out = new FileOutputStream(RDF_FILE_PATH);
            model.write(out, "RDF/XML");
            out.close();

            return ResponseEntity.ok("Book '" + bookId + "' saved successfully!");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
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