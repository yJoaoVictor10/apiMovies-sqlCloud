package br.com.fiap.movies.controllers;

import br.com.fiap.movies.models.Category;
import br.com.fiap.movies.serices.CategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private CategoryService service;

    @GetMapping
    public List<Category> getCategories() {
        log.info("Listando todas as categorias");
        return service.getCategories();
    }

    @PostMapping
    public ResponseEntity<Category> addCategory(@RequestBody Category category) {
        log.info("Cadastrando categoria...");

        var savedCategory = service.addCategory(category);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedCategory);
    }

    @GetMapping("{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {

        return service.getCategoryById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {

        service.deleteCategory(id);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("{id}")
    public ResponseEntity<Category> updateCategory(
            @PathVariable Long id,
            @RequestBody Category newCategory) {

        Category category = service.updateCategory(id, newCategory);

        return ResponseEntity.ok(category);
    }
}