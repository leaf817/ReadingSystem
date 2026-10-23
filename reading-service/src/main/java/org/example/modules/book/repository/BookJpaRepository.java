package org.example.modules.book.repository;

import org.example.modules.book.entity.BookEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookJpaRepository extends JpaRepository<BookEntity, Long> {

    List<BookEntity> findAllByOrderByIdAsc();

    List<BookEntity> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCaseOrCategoryContaining(
            String title, String author, String category);
}
