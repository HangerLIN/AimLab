-- 用户表
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'ATHLETE',
    status TINYINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '系统用户表';

-- 运动员信息表
CREATE TABLE athletes (
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
CREATE TABLE training_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    athlete_id BIGINT NOT NULL,
    session_name VARCHAR(100),
    start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    end_time TIMESTAMP,
    notes TEXT
) COMMENT '训练场次表';

-- 比赛信息表
CREATE TABLE competitions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT COMMENT '比赛描述',
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
CREATE TABLE competition_athletes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    competition_id INT NOT NULL,
    athlete_id BIGINT NOT NULL,
    status ENUM('ENROLLED', 'CONFIRMED', 'REJECTED', 'WITHDRAWN') DEFAULT 'ENROLLED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_competition_athlete (competition_id, athlete_id)
) COMMENT '比赛运动员关联表';

-- 比赛结果表
CREATE TABLE competition_results (
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

-- 射击记录表（这是分片表的模板，实际会按时间分片）
CREATE TABLE shooting_records (
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
