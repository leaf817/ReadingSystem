-- 阅界 · 用户资料扩展（作者简介）
-- 执行顺序：4

USE reading_system;

ALTER TABLE user_account
  ADD COLUMN bio VARCHAR(500) NULL COMMENT '个人简介（作者等）' AFTER avatar;
