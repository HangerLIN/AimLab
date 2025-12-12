-- 创建数据库
CREATE DATABASE IF NOT EXISTS shooting_system_0 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS shooting_system_1 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE shooting_system_0;

-- 创建广播表（在每个数据库中都需要创建）
-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'ATHLETE',
    status TINYINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '系统用户表';

-- 运动员信息表
CREATE TABLE IF NOT EXISTS athletes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL,
    gender ENUM('MALE', 'FEMALE', 'UNKNOWN') DEFAULT 'UNKNOWN',
    birth_date DATE,
    level VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '运动员详细信息表';

-- 训练场次表
CREATE TABLE IF NOT EXISTS training_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    athlete_id BIGINT NOT NULL,
    session_name VARCHAR(100),
    start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    end_time TIMESTAMP,
    notes TEXT
) COMMENT '训练场次表';

-- 比赛信息表
CREATE TABLE IF NOT EXISTS competitions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    rounds_count INT DEFAULT 1 COMMENT '总轮数',
    shots_per_round INT DEFAULT 10 COMMENT '每轮射击次数',
    time_limit_per_shot INT COMMENT '每发时间限制(秒)',
    status ENUM('CREATED', 'RUNNING', 'PAUSED', 'COMPLETED', 'CANCELED') DEFAULT 'CREATED',
    created_by BIGINT COMMENT '创建者ID，关联users表',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    started_at TIMESTAMP,
    ended_at TIMESTAMP,
    completed_at TIMESTAMP COMMENT '完成时间',
    duration_seconds INT COMMENT '比赛持续时间(秒)'
) COMMENT '比赛信息表';

-- 比赛运动员关联表
CREATE TABLE IF NOT EXISTS competition_athletes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    competition_id INT NOT NULL,
    athlete_id BIGINT NOT NULL,
    status ENUM('ENROLLED', 'CONFIRMED', 'REJECTED', 'WITHDRAWN') DEFAULT 'ENROLLED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    registered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '报名时间',
    UNIQUE KEY uk_competition_athlete (competition_id, athlete_id)
) COMMENT '比赛运动员关联表';

-- 射击记录表（这是分片表的模板，实际会按时间分片）
CREATE TABLE IF NOT EXISTS shooting_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    record_type ENUM('TRAINING', 'COMPETITION') NOT NULL COMMENT '记录类型',
    athlete_id BIGINT NOT NULL,
    competition_id INT,
    training_session_id BIGINT,
    round_number INT COMMENT '第几轮',
    shot_number INT COMMENT '第几发',
    x DECIMAL(5,4) NOT NULL COMMENT 'X轴坐标 (0-1)',
    y DECIMAL(5,4) NOT NULL COMMENT 'Y轴坐标 (0-1)',
    score DECIMAL(4,1) NOT NULL COMMENT '环数',
    shot_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '射击时间(毫秒精度)',
    user_id BIGINT NOT NULL COMMENT '冗余用户ID，用于分片',
    INDEX idx_athlete_time (athlete_id, shot_at),
    INDEX idx_competition (competition_id),
    INDEX idx_training (training_session_id)
) COMMENT '射击记录总表';

-- 比赛结果表
CREATE TABLE IF NOT EXISTS competition_results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    competition_id INT NOT NULL,
    athlete_id BIGINT NOT NULL,
    athlete_name VARCHAR(100) NOT NULL,
    final_rank INT NOT NULL,
    final_score DECIMAL(10, 2) NOT NULL,
    total_shots INT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    INDEX idx_competition_id (competition_id),
    INDEX idx_athlete_id (athlete_id)
) COMMENT '比赛结果表';

-- 创建分片表（按月份分片，这里创建2023年和2024年的所有月份表）
-- 2023年
CREATE TABLE IF NOT EXISTS shooting_records_2023_01 LIKE shooting_records;
CREATE TABLE IF NOT EXISTS shooting_records_2023_02 LIKE shooting_records;
CREATE TABLE IF NOT EXISTS shooting_records_2023_03 LIKE shooting_records;
CREATE TABLE IF NOT EXISTS shooting_records_2023_04 LIKE shooting_records;
CREATE TABLE IF NOT EXISTS shooting_records_2023_05 LIKE shooting_records;
CREATE TABLE IF NOT EXISTS shooting_records_2023_06 LIKE shooting_records;
CREATE TABLE IF NOT EXISTS shooting_records_2023_07 LIKE shooting_records;
CREATE TABLE IF NOT EXISTS shooting_records_2023_08 LIKE shooting_records;
CREATE TABLE IF NOT EXISTS shooting_records_2023_09 LIKE shooting_records;
CREATE TABLE IF NOT EXISTS shooting_records_2023_10 LIKE shooting_records;
CREATE TABLE IF NOT EXISTS shooting_records_2023_11 LIKE shooting_records;
CREATE TABLE IF NOT EXISTS shooting_records_2023_12 LIKE shooting_records;

-- 2024年
CREATE TABLE IF NOT EXISTS shooting_records_2024_01 LIKE shooting_records;
CREATE TABLE IF NOT EXISTS shooting_records_2024_02 LIKE shooting_records;
CREATE TABLE IF NOT EXISTS shooting_records_2024_03 LIKE shooting_records;
CREATE TABLE IF NOT EXISTS shooting_records_2024_04 LIKE shooting_records;
CREATE TABLE IF NOT EXISTS shooting_records_2024_05 LIKE shooting_records;
CREATE TABLE IF NOT EXISTS shooting_records_2024_06 LIKE shooting_records;
CREATE TABLE IF NOT EXISTS shooting_records_2024_07 LIKE shooting_records;
CREATE TABLE IF NOT EXISTS shooting_records_2024_08 LIKE shooting_records;
CREATE TABLE IF NOT EXISTS shooting_records_2024_09 LIKE shooting_records;
CREATE TABLE IF NOT EXISTS shooting_records_2024_10 LIKE shooting_records;
CREATE TABLE IF NOT EXISTS shooting_records_2024_11 LIKE shooting_records;
CREATE TABLE IF NOT EXISTS shooting_records_2024_12 LIKE shooting_records;

-- 使用数据库
USE shooting_system_1;

-- 创建广播表（在每个数据库中都需要创建）
-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'ATHLETE',
    status TINYINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '系统用户表';

-- 运动员信息表
CREATE TABLE IF NOT EXISTS athletes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL,
    gender ENUM('MALE', 'FEMALE', 'UNKNOWN') DEFAULT 'UNKNOWN',
    birth_date DATE,
    level VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '运动员详细信息表';

-- 训练场次表
CREATE TABLE IF NOT EXISTS training_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    athlete_id BIGINT NOT NULL,
    session_name VARCHAR(100),
    start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    end_time TIMESTAMP,
    notes TEXT
) COMMENT '训练场次表';

-- 比赛信息表
CREATE TABLE IF NOT EXISTS competitions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    rounds_count INT DEFAULT 1 COMMENT '总轮数',
    shots_per_round INT DEFAULT 10 COMMENT '每轮射击次数',
    time_limit_per_shot INT COMMENT '每发时间限制(秒)',
    status ENUM('CREATED', 'RUNNING', 'PAUSED', 'COMPLETED', 'CANCELED') DEFAULT 'CREATED',
    created_by BIGINT COMMENT '创建者ID，关联users表',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    started_at TIMESTAMP,
    ended_at TIMESTAMP,
    completed_at TIMESTAMP COMMENT '完成时间',
    duration_seconds INT COMMENT '比赛持续时间(秒)'
) COMMENT '比赛信息表';

-- 比赛运动员关联表
CREATE TABLE IF NOT EXISTS competition_athletes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    competition_id INT NOT NULL,
    athlete_id BIGINT NOT NULL,
    status ENUM('ENROLLED', 'CONFIRMED', 'REJECTED', 'WITHDRAWN') DEFAULT 'ENROLLED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    registered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '报名时间',
    UNIQUE KEY uk_competition_athlete (competition_id, athlete_id)
) COMMENT '比赛运动员关联表';

-- 射击记录表（这是分片表的模板，实际会按时间分片）
CREATE TABLE IF NOT EXISTS shooting_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    record_type ENUM('TRAINING', 'COMPETITION') NOT NULL COMMENT '记录类型',
    athlete_id BIGINT NOT NULL,
    competition_id INT,
    training_session_id BIGINT,
    round_number INT COMMENT '第几轮',
    shot_number INT COMMENT '第几发',
    x DECIMAL(5,4) NOT NULL COMMENT 'X轴坐标 (0-1)',
    y DECIMAL(5,4) NOT NULL COMMENT 'Y轴坐标 (0-1)',
    score DECIMAL(4,1) NOT NULL COMMENT '环数',
    shot_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '射击时间(毫秒精度)',
    user_id BIGINT NOT NULL COMMENT '冗余用户ID，用于分片',
    INDEX idx_athlete_time (athlete_id, shot_at),
    INDEX idx_competition (competition_id),
    INDEX idx_training (training_session_id)
) COMMENT '射击记录总表';

-- 比赛结果表
CREATE TABLE IF NOT EXISTS competition_results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    competition_id INT NOT NULL,
    athlete_id BIGINT NOT NULL,
    athlete_name VARCHAR(100) NOT NULL,
    final_rank INT NOT NULL,
    final_score DECIMAL(10, 2) NOT NULL,
    total_shots INT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    INDEX idx_competition_id (competition_id),
    INDEX idx_athlete_id (athlete_id)
) COMMENT '比赛结果表';

-- 创建分片表（按月份分片，这里创建2023年和2024年的所有月份表）
-- 2023年
CREATE TABLE IF NOT EXISTS shooting_records_2023_01 LIKE shooting_records;
CREATE TABLE IF NOT EXISTS shooting_records_2023_02 LIKE shooting_records;
CREATE TABLE IF NOT EXISTS shooting_records_2023_03 LIKE shooting_records;
CREATE TABLE IF NOT EXISTS shooting_records_2023_04 LIKE shooting_records;
CREATE TABLE IF NOT EXISTS shooting_records_2023_05 LIKE shooting_records;
CREATE TABLE IF NOT EXISTS shooting_records_2023_06 LIKE shooting_records;
CREATE TABLE IF NOT EXISTS shooting_records_2023_07 LIKE shooting_records;
CREATE TABLE IF NOT EXISTS shooting_records_2023_08 LIKE shooting_records;
CREATE TABLE IF NOT EXISTS shooting_records_2023_09 LIKE shooting_records;
CREATE TABLE IF NOT EXISTS shooting_records_2023_10 LIKE shooting_records;
CREATE TABLE IF NOT EXISTS shooting_records_2023_11 LIKE shooting_records;
CREATE TABLE IF NOT EXISTS shooting_records_2023_12 LIKE shooting_records;

-- 2024年
CREATE TABLE IF NOT EXISTS shooting_records_2024_01 LIKE shooting_records;
CREATE TABLE IF NOT EXISTS shooting_records_2024_02 LIKE shooting_records;
CREATE TABLE IF NOT EXISTS shooting_records_2024_03 LIKE shooting_records;
CREATE TABLE IF NOT EXISTS shooting_records_2024_04 LIKE shooting_records;
CREATE TABLE IF NOT EXISTS shooting_records_2024_05 LIKE shooting_records;
CREATE TABLE IF NOT EXISTS shooting_records_2024_06 LIKE shooting_records;
CREATE TABLE IF NOT EXISTS shooting_records_2024_07 LIKE shooting_records;
CREATE TABLE IF NOT EXISTS shooting_records_2024_08 LIKE shooting_records;
CREATE TABLE IF NOT EXISTS shooting_records_2024_09 LIKE shooting_records;
CREATE TABLE IF NOT EXISTS shooting_records_2024_10 LIKE shooting_records;
CREATE TABLE IF NOT EXISTS shooting_records_2024_11 LIKE shooting_records;
CREATE TABLE IF NOT EXISTS shooting_records_2024_12 LIKE shooting_records; 
