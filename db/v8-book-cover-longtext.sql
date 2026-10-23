-- 书籍封面改为 LONGTEXT，支持上传图片（data URL）
USE reading_system;

ALTER TABLE book
  MODIFY COLUMN cover LONGTEXT NULL COMMENT '封面（data URL 或 URL）';
