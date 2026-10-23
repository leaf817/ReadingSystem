-- 阅界 · 章节正文
USE reading_system;

CREATE TABLE IF NOT EXISTS book_chapter (
  id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  book_id     BIGINT       NOT NULL COMMENT '书籍 id',
  seq         INT          NOT NULL COMMENT '阅读顺序，从 1 开始',
  title       VARCHAR(128) NOT NULL COMMENT '章节标题',
  content     LONGTEXT     NOT NULL COMMENT '正文',
  word_count  INT          NOT NULL DEFAULT 0 COMMENT '字数',
  created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_book_chapter_seq (book_id, seq),
  CONSTRAINT fk_book_chapter_book FOREIGN KEY (book_id) REFERENCES book (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='书籍章节';
