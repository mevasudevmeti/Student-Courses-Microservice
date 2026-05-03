package com.leedsbeckett.library_service.controller;

import com.leedsbeckett.library_service.model.Book;
import com.leedsbeckett.library_service.repository.BookRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {
    private final BookRepository repo;
    public BookController(BookRepository repo) { this.repo = repo; }

    @GetMapping
    public List<Book> getAll() { return repo.findAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<Book> getById(@PathVariable Long id) {
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Book create(@RequestBody Book book) {
        book.setAvailableCopies(book.getTotalCopies());
        return repo.save(book);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Book> update(@PathVariable Long id, @RequestBody Book book) {
        return repo.findById(id).map(existing -> {
            int diff = book.getTotalCopies() - existing.getTotalCopies();
            existing.setIsbn(book.getIsbn());
            existing.setTitle(book.getTitle());
            existing.setAuthor(book.getAuthor());
            existing.setCategory(book.getCategory());
            existing.setPublishedYear(book.getPublishedYear());
            existing.setTotalCopies(book.getTotalCopies());
            existing.setAvailableCopies(Math.max(0, existing.getAvailableCopies() + diff));
            return ResponseEntity.ok(repo.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
