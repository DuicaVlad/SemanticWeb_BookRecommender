package com.example.semantic;

import org.apache.jena.rdf.model.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;
import java.util.*;

@RestController
@RequestMapping("/api")
public class RdfController {

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadRdf(@RequestParam("file") MultipartFile file) {
        try {
            Model model = ModelFactory.createDefaultModel();
            InputStream inputStream = file.getInputStream();
            model.read(inputStream, null); // Reads RDF/XML

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

            return ResponseEntity.ok(Map.of("nodes", nodes, "edges", edges));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    private String shortLabel(String uri) {
        if (uri.contains("#")) return uri.substring(uri.lastIndexOf("#") + 1);
        return uri;
    }
}