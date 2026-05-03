package com.leedsbeckett.library_service.controller;

import com.leedsbeckett.library_service.model.BorrowRecord;
import com.leedsbeckett.library_service.model.Book;
import com.leedsbeckett.library_service.model.Member;
import com.leedsbeckett.library_service.repository.BorrowRepository;
import com.leedsbeckett.library_service.repository.BookRepository;
import com.leedsbeckett.library_service.repository.MemberRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
public class BorrowController {
    private final BorrowRepository borrowRepo;
    private final BookRepository bookRepo;
    private final MemberRepository memberRepo;

    public BorrowController(BorrowRepository borrowRepo, BookRepository bookRepo, MemberRepository memberRepo) {
        this.borrowRepo = borrowRepo; this.bookRepo = bookRepo; this.memberRepo = memberRepo;
    }

    @GetMapping("/api/borrow/active")
    public List<BorrowRecord> getActive() { return borrowRepo.findByStatus("BORROWED"); }

    @GetMapping("/api/borrow/all")
    public List<BorrowRecord> getAll() { return borrowRepo.findAll(); }

    @PostMapping("/api/borrow")
    public ResponseEntity<?> borrow(@RequestBody Map<String, Long> body) {
        Long bookId = body.get("bookId");
        Long memberId = body.get("memberId");
        Book book = bookRepo.findById(bookId).orElse(null);
        Member member = memberRepo.findById(memberId).orElse(null);
        if (book == null || member == null) return ResponseEntity.badRequest().body(Map.of("message","Book or member not found"));
        if (book.getAvailableCopies() < 1) return ResponseEntity.badRequest().body(Map.of("message","No copies available"));
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepo.save(book);
        BorrowRecord record = new BorrowRecord();
        record.setBookId(bookId);
        record.setMemberId(memberId);
        record.setBookTitle(book.getTitle());
        record.setMemberName(member.getFullName());
        record.setBorrowedAt(LocalDateTime.now());
        record.setDueAt(LocalDateTime.now().plusDays(14));
        record.setStatus("BORROWED");
        return ResponseEntity.ok(borrowRepo.save(record));
    }

    @PostMapping("/api/return")
    public ResponseEntity<?> returnBook(@RequestBody Map<String, Long> body) {
        Long recordId = body.get("borrowRecordId");
        BorrowRecord record = borrowRepo.findById(recordId).orElse(null);
        if (record == null) return ResponseEntity.badRequest().body(Map.of("message","Record not found"));
        record.setReturnedAt(LocalDateTime.now());
        record.setStatus("RETURNED");
        borrowRepo.save(record);
        bookRepo.findById(record.getBookId()).ifPresent(book -> {
            book.setAvailableCopies(book.getAvailableCopies() + 1);
            bookRepo.save(book);
        });
        return ResponseEntity.ok(record);
    }

    @PutMapping("/api/borrow/{id}/extend")
    public ResponseEntity<?> extend(@PathVariable Long id) {
        BorrowRecord record = borrowRepo.findById(id).orElse(null);
        if (record == null) return ResponseEntity.badRequest().body(Map.of("message","Record not found"));
        record.setDueAt(record.getDueAt().plusDays(7));
        return ResponseEntity.ok(borrowRepo.save(record));
    }

    @GetMapping("/api/stats")
    public Map<String, Object> stats() {
        long totalBooks = bookRepo.count();
        int availableCopies = bookRepo.findAll().stream().mapToInt(Book::getAvailableCopies).sum();
        long totalMembers = memberRepo.count();
        long activeBorrowings = borrowRepo.findByStatus("BORROWED").size();
        long overdueCount = borrowRepo.findByStatus("BORROWED").stream()
            .filter(r -> r.getDueAt().isBefore(LocalDateTime.now())).count();
        return Map.of("totalBooks", totalBooks, "availableCopies", availableCopies,
            "totalMembers", totalMembers, "activeBorrowings", activeBorrowings, "overdueCount", overdueCount);
    }
}
