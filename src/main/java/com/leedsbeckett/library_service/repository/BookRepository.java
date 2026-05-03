package com.leedsbeckett.library_service.repository;
import com.leedsbeckett.library_service.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface BookRepository extends JpaRepository<Book, Long> {}
