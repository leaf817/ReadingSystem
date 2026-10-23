package org.example.modules.book.repository;

import org.example.modules.book.entity.BookCoverEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookCoverRepository extends JpaRepository<BookCoverEntity, Long> {
}
