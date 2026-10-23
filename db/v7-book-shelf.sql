-- 阅界 · 书籍上架状态字段
-- 执行顺序：在 v5 / v6 之后
-- 若列已存在可忽略报错

USE reading_system;

ALTER TABLE book
  ADD COLUMN shelf VARCHAR(16) NOT NULL DEFAULT '上架' COMMENT '上架状态：上架/下架' AFTER status;
