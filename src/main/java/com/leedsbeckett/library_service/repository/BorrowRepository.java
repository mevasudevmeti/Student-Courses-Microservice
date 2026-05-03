package com.leedsbeckett.library_service.repository;
import com.leedsbeckett.library_service.model.BorrowRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface BorrowRepository extends JpaRepository<BorrowRecord, Long> {
    List<BorrowRecord> findByStatus(String status);
    List<BorrowRecord> findByMemberId(Long memberId);
}
