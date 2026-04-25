package com.lyrascore;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.lyrascore.**.mapper")
public class LyraScoreApplication {
    public static void main(String[] args) {
        SpringApplication.run(LyraScoreApplication.class, args);
    }
}
