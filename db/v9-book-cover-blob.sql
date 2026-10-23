-- 封面存进数据库。可重复执行。
USE reading_system;

SET @db := DATABASE();

SET @sql := (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE book ADD COLUMN cover_mime VARCHAR(64) NULL COMMENT ''封面 MIME'' AFTER cover',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'book' AND COLUMN_NAME = 'cover_mime'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE book ADD COLUMN cover_ver INT NOT NULL DEFAULT 0 COMMENT ''封面版本'' AFTER cover_mime',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'book' AND COLUMN_NAME = 'cover_ver'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS book_cover (
  book_id BIGINT       NOT NULL COMMENT '书籍 ID',
  mime    VARCHAR(64)  NOT NULL COMMENT 'MIME',
  data    LONGBLOB     NOT NULL COMMENT '图片二进制',
  ver     INT          NOT NULL DEFAULT 1 COMMENT '版本',
  PRIMARY KEY (book_id),
  CONSTRAINT fk_book_cover_book FOREIGN KEY (book_id) REFERENCES book (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='书籍封面';
