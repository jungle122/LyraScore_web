-- ============================================================
-- LyraScore 数据库表结构（DDL）
-- 来源：docs/E-R 图.md
-- 字符集：utf8mb4（适配中文与 emoji）
-- 引擎：InnoDB（支持外键、事务）
-- 命名：表名 t_ 前缀，列名 snake_case（与 MyBatis map-underscore-to-camel-case 配合）
--
-- 执行方式：
--   mysql --default-character-set=utf8mb4 -u root -p
--   CREATE DATABASE IF NOT EXISTS lyrascore DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
--   USE lyrascore;
--   SOURCE backend/src/main/resources/db/schema.sql;
--
-- ⚠️  Windows 中文系统必须加 --default-character-set=utf8mb4，否则 mysql 客户端
--     会按 GBK 解析本文件，导致中文注释在 information_schema 中被存成乱码。
-- ============================================================

-- 反向依赖顺序 DROP，保证脚本可重复执行
DROP TABLE IF EXISTS t_user_badge;
DROP TABLE IF EXISTS t_badge;
DROP TABLE IF EXISTS t_dictionary;
DROP TABLE IF EXISTS t_setlist_items;
DROP TABLE IF EXISTS t_setlist;
DROP TABLE IF EXISTS t_practice_log;
DROP TABLE IF EXISTS t_plan_items;
DROP TABLE IF EXISTS t_practice_plan;
DROP TABLE IF EXISTS t_song_request;
DROP TABLE IF EXISTS t_friendship;
DROP TABLE IF EXISTS t_score;
DROP TABLE IF EXISTS t_user;


-- ============================================================
-- 1. t_user — 用户
-- ============================================================
CREATE TABLE t_user (
    id              BIGINT          NOT NULL AUTO_INCREMENT             COMMENT '用户ID',
    username        VARCHAR(50)     NOT NULL                            COMMENT '学号/用户名',
    password_hash   VARCHAR(255)    NOT NULL                            COMMENT '加密密码（BCrypt）',
    avatar_url      VARCHAR(500)    DEFAULT NULL                        COMMENT '头像相对路径，例如 /uploads/avatars/xx.png',
    role            VARCHAR(20)     NOT NULL DEFAULT 'user'             COMMENT '角色：user / admin',
    status          TINYINT         NOT NULL DEFAULT 0                  COMMENT '状态：0正常 / 1封禁',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '注册时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';


-- ============================================================
-- 2. t_score — 乐谱
-- ============================================================
CREATE TABLE t_score (
    id              BIGINT          NOT NULL AUTO_INCREMENT             COMMENT '乐谱ID',
    user_id         BIGINT          NOT NULL                            COMMENT '所属用户ID',
    title           VARCHAR(100)    NOT NULL                            COMMENT '歌曲名',
    artist          VARCHAR(100)    DEFAULT NULL                        COMMENT '原唱/歌手',
    image_url       VARCHAR(500)    NOT NULL                            COMMENT '乐谱图片相对路径，例如 /uploads/scores/xx.jpg',
    tuning          VARCHAR(50)     DEFAULT NULL                        COMMENT '调式（如：标准调弦 / Drop D）',
    capo            INT             DEFAULT NULL                        COMMENT '变调夹位置（0~12，NULL 表示无）',
    bpm             INT             DEFAULT NULL                        COMMENT '速度 BPM',
    is_public       TINYINT         NOT NULL DEFAULT 0                  COMMENT '是否公开：0私密 / 1公开',
    memo            TEXT            DEFAULT NULL                        COMMENT '备忘录/批注',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '录入时间',
    PRIMARY KEY (id),
    KEY idx_score_user_created (user_id, created_at),
    CONSTRAINT fk_score_user FOREIGN KEY (user_id) REFERENCES t_user (id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='乐谱表';


-- ============================================================
-- 3. t_friendship — 好友关系（自引用 N:M）
-- ============================================================
CREATE TABLE t_friendship (
    id              BIGINT          NOT NULL AUTO_INCREMENT             COMMENT '关系ID',
    user_id_1       BIGINT          NOT NULL                            COMMENT '发起方用户ID',
    user_id_2       BIGINT          NOT NULL                            COMMENT '接收方用户ID',
    status          TINYINT         NOT NULL DEFAULT 0                  COMMENT '状态：0待通过 / 1已成为好友',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '申请时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_friendship_pair (user_id_1, user_id_2),
    KEY idx_friendship_receiver (user_id_2, status),
    CONSTRAINT fk_friendship_user1 FOREIGN KEY (user_id_1) REFERENCES t_user (id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_friendship_user2 FOREIGN KEY (user_id_2) REFERENCES t_user (id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='好友关系表';


-- ============================================================
-- 4. t_song_request — AI 智能点歌
-- ============================================================
CREATE TABLE t_song_request (
    id              BIGINT          NOT NULL AUTO_INCREMENT             COMMENT '点歌ID',
    sender_id       BIGINT          NOT NULL                            COMMENT '点歌人ID',
    receiver_id     BIGINT          NOT NULL                            COMMENT '被点人ID',
    raw_input       VARCHAR(500)    NOT NULL                            COMMENT '用户原始输入（自由文本）',
    ai_song         VARCHAR(100)    DEFAULT NULL                        COMMENT 'AI 清洗后的歌曲名（失败时为 NULL）',
    ai_artist       VARCHAR(100)    DEFAULT NULL                        COMMENT 'AI 清洗后的歌手名（失败时为 NULL）',
    request_message VARCHAR(255)    DEFAULT NULL                        COMMENT '快捷短语留言',
    status          TINYINT         NOT NULL DEFAULT 0                  COMMENT '状态：0未弹 / 1已还愿 / 2 AI清洗失败待人工审核',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '点歌时间',
    PRIMARY KEY (id),
    KEY idx_song_request_receiver (receiver_id, status),
    KEY idx_song_request_sender (sender_id, created_at),
    CONSTRAINT fk_song_request_sender   FOREIGN KEY (sender_id)   REFERENCES t_user (id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_song_request_receiver FOREIGN KEY (receiver_id) REFERENCES t_user (id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI 智能点歌请求表';


-- ============================================================
-- 5. t_practice_plan — 练习计划
-- ============================================================
CREATE TABLE t_practice_plan (
    id              BIGINT          NOT NULL AUTO_INCREMENT             COMMENT '计划ID',
    user_id         BIGINT          NOT NULL                            COMMENT '所属用户ID',
    title           VARCHAR(100)    NOT NULL                            COMMENT '计划标题（如：本周指弹）',
    status          TINYINT         NOT NULL DEFAULT 0                  COMMENT '状态：0进行中 / 1已完成',
    start_date      DATE            NOT NULL                            COMMENT '开始日期',
    end_date        DATE            NOT NULL                            COMMENT '结束日期',
    PRIMARY KEY (id),
    KEY idx_plan_user (user_id, status),
    CONSTRAINT fk_plan_user FOREIGN KEY (user_id) REFERENCES t_user (id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='练习计划表';


-- ============================================================
-- 6. t_plan_items — 计划-乐谱（N:M 关联）
-- ============================================================
CREATE TABLE t_plan_items (
    id              BIGINT          NOT NULL AUTO_INCREMENT             COMMENT '关联主键',
    plan_id         BIGINT          NOT NULL                            COMMENT '计划ID',
    score_id        BIGINT          NOT NULL                            COMMENT '乐谱ID',
    sort_order      INT             NOT NULL DEFAULT 0                  COMMENT '排序权重（小者靠前）',
    PRIMARY KEY (id),
    UNIQUE KEY uk_plan_items_pair (plan_id, score_id),
    KEY idx_plan_items_score (score_id),
    CONSTRAINT fk_plan_items_plan  FOREIGN KEY (plan_id)  REFERENCES t_practice_plan (id) ON DELETE CASCADE  ON UPDATE CASCADE,
    CONSTRAINT fk_plan_items_score FOREIGN KEY (score_id) REFERENCES t_score (id)         ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='练习计划-乐谱关联表';


-- ============================================================
-- 7. t_practice_log — 练习日志
-- ============================================================
CREATE TABLE t_practice_log (
    id              BIGINT          NOT NULL AUTO_INCREMENT             COMMENT '日志ID',
    user_id         BIGINT          NOT NULL                            COMMENT '用户ID',
    score_id        BIGINT          NOT NULL                            COMMENT '练习的乐谱ID',
    duration_mins   INT             NOT NULL                            COMMENT '本次练习时长（分钟）',
    current_bpm     INT             DEFAULT NULL                        COMMENT '本次达到的 BPM',
    thoughts        VARCHAR(500)    DEFAULT NULL                        COMMENT '心得体会',
    log_time        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '记录时间',
    PRIMARY KEY (id),
    KEY idx_log_user_time (user_id, log_time),
    KEY idx_log_score (score_id),
    CONSTRAINT fk_log_user  FOREIGN KEY (user_id)  REFERENCES t_user (id)  ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_log_score FOREIGN KEY (score_id) REFERENCES t_score (id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='练习日志表';


-- ============================================================
-- 8. t_setlist — 歌单
-- ============================================================
CREATE TABLE t_setlist (
    id              BIGINT          NOT NULL AUTO_INCREMENT             COMMENT '歌单ID',
    user_id         BIGINT          NOT NULL                            COMMENT '创建者ID',
    name            VARCHAR(100)    NOT NULL                            COMMENT '歌单名称',
    description     VARCHAR(255)    DEFAULT NULL                        COMMENT '歌单描述',
    cover_url       VARCHAR(500)    DEFAULT NULL                        COMMENT '封面图相对路径',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_setlist_user (user_id, created_at),
    CONSTRAINT fk_setlist_user FOREIGN KEY (user_id) REFERENCES t_user (id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='歌单表';


-- ============================================================
-- 9. t_setlist_items — 歌单-乐谱（N:M 关联）
-- ============================================================
CREATE TABLE t_setlist_items (
    id              BIGINT          NOT NULL AUTO_INCREMENT             COMMENT '关联主键',
    setlist_id      BIGINT          NOT NULL                            COMMENT '歌单ID',
    score_id        BIGINT          NOT NULL                            COMMENT '乐谱ID',
    added_at        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '收藏时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_setlist_items_pair (setlist_id, score_id),
    KEY idx_setlist_items_score (score_id),
    CONSTRAINT fk_setlist_items_setlist FOREIGN KEY (setlist_id) REFERENCES t_setlist (id) ON DELETE CASCADE  ON UPDATE CASCADE,
    CONSTRAINT fk_setlist_items_score   FOREIGN KEY (score_id)   REFERENCES t_score (id)   ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='歌单-乐谱关联表';


-- ============================================================
-- 10. t_dictionary — 通用字典（快捷私信短语等）
-- ============================================================
CREATE TABLE t_dictionary (
    id              BIGINT          NOT NULL AUTO_INCREMENT             COMMENT '字典ID',
    dict_type       VARCHAR(50)     NOT NULL                            COMMENT '字典类型（如：quick_dm）',
    dict_key        VARCHAR(50)     NOT NULL                            COMMENT '字典键名',
    dict_value      VARCHAR(255)    NOT NULL                            COMMENT '展示值（如：大佬教教我）',
    sort_order      INT             NOT NULL DEFAULT 0                  COMMENT '排序权重',
    is_active       TINYINT         NOT NULL DEFAULT 1                  COMMENT '是否启用：0停用 / 1启用',
    PRIMARY KEY (id),
    UNIQUE KEY uk_dictionary_type_key (dict_type, dict_key),
    KEY idx_dictionary_type_active (dict_type, is_active, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='通用字典表';


-- ============================================================
-- 11. t_badge — 徽章定义
-- ============================================================
CREATE TABLE t_badge (
    id              BIGINT          NOT NULL AUTO_INCREMENT             COMMENT '徽章ID',
    name            VARCHAR(50)     NOT NULL                            COMMENT '徽章名称（如：肝帝）',
    icon_url        VARCHAR(500)    DEFAULT NULL                        COMMENT '徽章图标相对路径',
    condition_type  VARCHAR(50)     NOT NULL                            COMMENT '触发条件类型（如：total_minutes / score_count）',
    condition_value INT             NOT NULL                            COMMENT '触发阈值（如：600 表示 600 分钟）',
    description     VARCHAR(255)    DEFAULT NULL                        COMMENT '获取说明',
    PRIMARY KEY (id),
    UNIQUE KEY uk_badge_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='成就徽章定义表';


-- ============================================================
-- 12. t_user_badge — 用户已获徽章（N:M 关联）
-- ============================================================
CREATE TABLE t_user_badge (
    id              BIGINT          NOT NULL AUTO_INCREMENT             COMMENT '关联主键',
    user_id         BIGINT          NOT NULL                            COMMENT '用户ID',
    badge_id        BIGINT          NOT NULL                            COMMENT '徽章ID',
    unlocked_at     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '解锁时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_badge_pair (user_id, badge_id),
    KEY idx_user_badge_badge (badge_id),
    CONSTRAINT fk_user_badge_user  FOREIGN KEY (user_id)  REFERENCES t_user (id)  ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_user_badge_badge FOREIGN KEY (badge_id) REFERENCES t_badge (id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户徽章关联表';
