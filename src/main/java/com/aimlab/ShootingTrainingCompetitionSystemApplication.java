package com.aimlab;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 射击训练比赛系统启动类
 */
@SpringBootApplication
@MapperScan("com.aimlab.mapper")
public class ShootingTrainingCompetitionSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShootingTrainingCompetitionSystemApplication.class, args);
    }
} 