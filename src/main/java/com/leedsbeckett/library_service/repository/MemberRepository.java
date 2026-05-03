package com.leedsbeckett.library_service.repository;
import com.leedsbeckett.library_service.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {}
