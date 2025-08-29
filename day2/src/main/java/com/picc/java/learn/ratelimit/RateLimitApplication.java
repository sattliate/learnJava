package com.picc.java.learn.ratelimit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 限流器应用主类
 * 
 * @author learn
 * @since 1.0
 */
@SpringBootApplication
public class RateLimitApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(RateLimitApplication.class, args);
    }
}
