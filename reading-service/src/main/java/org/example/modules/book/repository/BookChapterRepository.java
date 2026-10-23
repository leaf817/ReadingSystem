package org.example.modules.book.repository;

import org.example.modules.book.entity.BookChapterEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookChapterRepository extends JpaRepository<BookChapterEntity, Long> {

    List<BookChapterEntity> findByBookIdOrderBySeqAsc(Long bookId);

    Optional<BookChapterEntity> findByBookIdAndSeq(Long bookId, Integer seq);

    void deleteByBookId(Long bookId);
}
