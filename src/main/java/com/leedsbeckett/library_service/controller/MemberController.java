package com.leedsbeckett.library_service.controller;

import com.leedsbeckett.library_service.model.BorrowRecord;
import com.leedsbeckett.library_service.model.Member;
import com.leedsbeckett.library_service.repository.BorrowRepository;
import com.leedsbeckett.library_service.repository.MemberRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/members")
public class MemberController {
    private final MemberRepository repo;
    private final BorrowRepository borrowRepo;
    public MemberController(MemberRepository repo, BorrowRepository borrowRepo) { this.repo = repo; this.borrowRepo = borrowRepo; }

    @GetMapping
    public List<Member> getAll() { return repo.findAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<Member> getById(@PathVariable Long id) {
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/history")
    public List<BorrowRecord> getHistory(@PathVariable Long id) {
        return borrowRepo.findByMemberId(id);
    }

    @PostMapping
    public Member create(@RequestBody Member member) { return repo.save(member); }

    @PutMapping("/{id}")
    public ResponseEntity<Member> update(@PathVariable Long id, @RequestBody Member member) {
        return repo.findById(id).map(existing -> {
            existing.setMemberCode(member.getMemberCode());
            existing.setFullName(member.getFullName());
            existing.setEmail(member.getEmail());
            existing.setStatus(member.getStatus());
            return ResponseEntity.ok(repo.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
