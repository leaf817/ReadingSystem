-- 阅界 · 书籍表
-- 执行顺序：在用户相关脚本之后
-- 数据来源：小说清单.xlsx

USE reading_system;

CREATE TABLE IF NOT EXISTS book (
  id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  title        VARCHAR(128) NOT NULL COMMENT '书名',
  author       VARCHAR(64)  NOT NULL COMMENT '作者',
  cover        LONGTEXT     NULL COMMENT '封面（data URL 或 URL）',
  category     VARCHAR(32)  NOT NULL COMMENT '分类',
  tags         VARCHAR(255) NOT NULL DEFAULT '' COMMENT '标签，逗号分隔',
  status       VARCHAR(16)  NOT NULL DEFAULT '连载' COMMENT '状态：连载/完结',
  shelf        VARCHAR(16)  NOT NULL DEFAULT '上架' COMMENT '上架状态：上架/下架',
  heat         VARCHAR(32)  NOT NULL DEFAULT '0' COMMENT '热度展示文案',
  description  VARCHAR(512) NOT NULL DEFAULT '' COMMENT '简介',
  chapters     INT          NOT NULL DEFAULT 0 COMMENT '章节数',
  created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_book_category (category),
  KEY idx_book_status (status),
  KEY idx_book_author (author)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='书籍';
