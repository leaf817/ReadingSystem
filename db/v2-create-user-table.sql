-- 阅界 · 用户表
-- 执行顺序：2 / 3

USE reading_system;

CREATE TABLE IF NOT EXISTS user_account (
  id         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  username   VARCHAR(64)  NOT NULL COMMENT '登录账号',
  password   VARCHAR(128) NOT NULL COMMENT '登录密码',
  nickname   VARCHAR(64)  NOT NULL COMMENT '昵称',
  role       VARCHAR(32)  NOT NULL COMMENT '角色：admin/author/reader',
  status     VARCHAR(16)  NOT NULL DEFAULT '正常' COMMENT '状态：正常/禁用/待审核',
  joined_at  VARCHAR(16)  NOT NULL COMMENT '注册日期 yyyy-MM-dd',
  avatar     LONGTEXT     NULL COMMENT '头像（data URL 或 URL）',
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_account_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户账号';
