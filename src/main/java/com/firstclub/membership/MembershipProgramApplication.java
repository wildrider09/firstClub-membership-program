package com.firstclub.membership;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

/**
 * Entry point for the FirstClub Membership Program backend.
 *
 * <p>The application exposes REST APIs to manage membership plans, tiers,
 * configurable benefits and the full subscription lifecycle. Tier promotion
 * is driven by a pluggable, data-configured rule engine (Strategy pattern).</p>
 */
@SpringBootApplication
@EnableRetry
public class MembershipProgramApplication {

    public static void main(String[] args) {
        SpringApplication.run(MembershipProgramApplication.class, args);
    }
}
