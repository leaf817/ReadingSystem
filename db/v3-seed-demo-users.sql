-- 阅界 · 演示账号种子数据
-- 执行顺序：3 / 3
-- 密码均为 123456

USE reading_system;

INSERT INTO user_account (id, username, password, nickname, role, status, joined_at, avatar)
VALUES
  (1, 'admin',  '123456', '管理员',   'admin',  '正常', '2025-01-08', NULL),
  (2, 'author', '123456', '签约作者', 'author', '正常', '2025-03-12', NULL),
  (3, 'reader', '123456', '普通读者', 'reader', '正常', '2025-04-02', NULL)
ON DUPLICATE KEY UPDATE
  password = VALUES(password),
  nickname = VALUES(nickname),
  role = VALUES(role),
  status = VALUES(status),
  joined_at = VALUES(joined_at);

ALTER TABLE user_account AUTO_INCREMENT = 100;
