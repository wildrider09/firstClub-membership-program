package com.firstclub.membership.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class AppConfig {

    /** Injectable clock so time-dependent logic is deterministic in tests. */
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
