package com.firstclub.membership.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * API index endpoint. {@code GET /api} returns a JSON listing of the available
 * routes so the service is self-describing. The interactive demo console is
 * served separately as a static page at {@code /}.
 */
@RestController
public class RootController {

    @GetMapping("/api")
    public Map<String, Object> index() {
        return Map.of(
                "service", "FirstClub Membership Program",
                "status", "UP",
                "endpoints", List.of(
                        "GET    /api/plans",
                        "GET    /api/tiers",
                        "GET    /api/tiers/{id}",
                        "POST   /api/plans",
                        "POST   /api/tiers",
                        "POST   /api/tiers/{id}/benefits",
                        "POST   /api/tiers/{id}/eligibility-rules",
                        "POST   /api/users",
                        "GET    /api/users",
                        "GET    /api/users/{id}",
                        "POST   /api/orders",
                        "POST   /api/subscriptions",
                        "POST   /api/subscriptions/{id}/upgrade",
                        "POST   /api/subscriptions/{id}/downgrade",
                        "POST   /api/subscriptions/{id}/cancel",
                        "GET    /api/subscriptions/users/{userId}/current",
                        "GET    /api/subscriptions/users/{userId}/eligible-tier"),
                "h2Console", "/h2-console");
    }
}
