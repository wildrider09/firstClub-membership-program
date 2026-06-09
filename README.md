# FirstClub Membership Program

A backend for a subscription-based **Membership Program with tiered benefits**,
built with **Java 21 + Spring Boot 3**. Users subscribe to a plan (Monthly /
Quarterly / Yearly), are placed on a tier (Silver / Gold / Platinum), and move
between tiers based on **configurable** criteria (order count, monthly spend,
cohort). Plans, tiers, benefits and promotion rules are all **data-driven**, so
the program can be reshaped at runtime without code changes.

---

## Quick start

No local Maven needed — the project ships with the Maven Wrapper.

> **Requires JDK 21+.** If your shell defaults to an older JDK (e.g. Java 8),
> point Maven at a 21+ JDK first, otherwise the Spring Boot plugin fails to load:
> ```bash
> export JAVA_HOME=$(/usr/libexec/java_home -v 21)   # macOS
> ```

```bash
# from the project root
./mvnw spring-boot:run         # starts on http://localhost:8080
```

Other useful commands:

```bash
./mvnw test                    # run the test suite (unit + integration)
./mvnw clean package           # build the runnable jar
java -jar target/membership-program-1.0.0.jar
```

On startup the app seeds demo data and an in-memory **H2** database
(console at `http://localhost:8080/h2-console`, JDBC URL
`jdbc:h2:mem:membershipdb`, user `sa`, empty password).

**Requirements:** JDK 21+. The wrapper downloads Maven automatically on first run.

---

## 🎬 Demo &amp; docs

| What | Where | Notes |
|------|-------|-------|
| **Interactive demo console** | `http://localhost:8080/` | Click-through UI (served by the app) that calls every API live. Includes a one-click **“Run full scenario”** that drives subscribe → orders → upgrade → downgrade → cancel. |
| **API index (JSON)** | `http://localhost:8080/api` | Self-describing list of all routes. |
| **Architecture &amp; flow diagrams** | [`docs/ARCHITECTURE.html`](docs/ARCHITECTURE.html) | Component architecture, request flow, subscribe/tier-promotion sequence, tier-engine strategy, and the concurrency sequence. Open in a browser (Mermaid diagrams). |
| **Entity design diagram** | [`docs/ENTITY_DESIGN.html`](docs/ENTITY_DESIGN.html) | ER diagram of all entities + design rationale. |

> The two `docs/*.html` files render Mermaid diagrams via CDN — open them directly
> in a browser (internet access required for the diagram script).

---

## What gets seeded

| Plans | Cycle | Price |
|------|-------|-------|
| FirstClub Monthly | MONTHLY | 199 |
| FirstClub Quarterly | QUARTERLY | 499 |
| FirstClub Yearly | YEARLY | 1499 |

| Tier | Rank | Benefits | Eligibility (ALL must hold) |
|------|------|----------|------------------------------|
| SILVER (base) | 1 | Free delivery, 5% discount | none — everyone qualifies |
| GOLD | 2 | Free delivery, 10% discount, exclusive deals | ≥ 5 orders |
| PLATINUM | 3 | Free delivery, 20% discount, exclusive deals, early access, priority support | ≥ 10 orders **AND** ≥ 5000 monthly spend |

Demo users: `1` = Alice (no cohort), `2` = Bob (cohort `VIP`).

---

## API reference

### Catalogue
| Method | Path | Description |
|--------|------|-------------|
| GET | `/` | Service index — lists all available routes |
| GET | `/api/plans` | List active plans |
| GET | `/api/tiers` | List tiers with benefits & rules |
| GET | `/api/tiers/{id}` | Get one tier |
| POST | `/api/plans` | (admin) Create a plan |
| POST | `/api/tiers` | (admin) Create a tier |
| POST | `/api/tiers/{id}/benefits` | (admin) Add a configurable benefit |
| POST | `/api/tiers/{id}/eligibility-rules` | (admin) Add a configurable promotion rule |

### Users & orders
| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/users` | Create a user |
| GET | `/api/users` / `/api/users/{id}` | List / get users |
| POST | `/api/orders` | Place an order (feeds tier eligibility) |

### Subscriptions (the user actions from the brief)
| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/subscriptions` | Subscribe to a plan (+ optional tier) |
| POST | `/api/subscriptions/{id}/upgrade` | Upgrade tier (eligibility enforced) |
| POST | `/api/subscriptions/{id}/downgrade` | Downgrade tier |
| POST | `/api/subscriptions/{id}/cancel` | Cancel subscription |
| GET | `/api/subscriptions/users/{userId}/current` | Track current membership & expiry |
| GET | `/api/subscriptions/users/{userId}/eligible-tier` | Highest tier the user currently qualifies for |

---

## Demo walk-through (copy/paste)

```bash
# 1. Browse the catalogue
curl -s localhost:8080/api/plans
curl -s localhost:8080/api/tiers

# 2. Subscribe Alice (user 1) to the Monthly plan.
#    No tier specified -> she lands on the base tier (SILVER).
curl -s -X POST localhost:8080/api/subscriptions \
  -H 'Content-Type: application/json' \
  -d '{"userId":1,"planId":1}'

# 3. Track her current membership + expiry
curl -s localhost:8080/api/subscriptions/users/1/current

# 4. Try to jump to GOLD now -> 409, she isn't eligible yet
curl -i -s -X POST localhost:8080/api/subscriptions/1/upgrade \
  -H 'Content-Type: application/json' -d '{"targetTierId":2}'

# 5. Place 6 orders so she meets GOLD's "5+ orders" rule
for i in $(seq 1 6); do
  curl -s -X POST localhost:8080/api/orders \
    -H 'Content-Type: application/json' \
    -d '{"userId":1,"amount":100}' > /dev/null
done

# 6. Check what she now qualifies for, then upgrade
curl -s localhost:8080/api/subscriptions/users/1/eligible-tier
curl -s -X POST localhost:8080/api/subscriptions/1/upgrade \
  -H 'Content-Type: application/json' -d '{"targetTierId":2}'

# 7. Downgrade and cancel
curl -s -X POST localhost:8080/api/subscriptions/1/downgrade \
  -H 'Content-Type: application/json' -d '{"targetTierId":1}'
curl -s -X POST localhost:8080/api/subscriptions/1/cancel
```

Idempotent subscribe — repeating the same `idempotencyKey` returns the original
subscription instead of creating a duplicate:

```bash
curl -s -X POST localhost:8080/api/subscriptions \
  -H 'Content-Type: application/json' \
  -d '{"userId":2,"planId":3,"idempotencyKey":"abc-123"}'
```

Add a brand-new benefit or rule at runtime (configurability):

```bash
# Give GOLD (tier 2) priority support too
curl -s -X POST localhost:8080/api/tiers/2/benefits \
  -H 'Content-Type: application/json' \
  -d '{"type":"PRIORITY_SUPPORT"}'
```

---

## Design notes (what to look at)

The brief is graded on *abstractions, entity design, extensibility, modularity,
best practices and concurrency*. Here's where each shows up:

### Layered, modular structure
```
controller/   REST endpoints (thin, DTO in/out)
service/      business logic & transaction boundaries
tier/         tier-eligibility engine (Strategy pattern)
domain/       JPA entities + enums
repository/   Spring Data JPA repositories
dto/          request/response records + mapper
exception/    domain exceptions + global handler
config/       Clock bean + demo data seeder
```

### Extensible tier engine (Strategy + Open/Closed)
Tier promotion is **not** a hard-coded `if/else`. Each criterion is a
`TierEligibilityStrategy` bean keyed by an `EligibilityRuleType`:

- `MinOrderCountStrategy` — "≥ X orders"
- `MinMonthlyOrderValueStrategy` — "≥ X spend in the trailing month"
- `RequiredCohortStrategy` — "belongs to cohort Y"

`TierEligibilityEvaluator` injects all strategies into a map and evaluates a
tier's rules with **AND** semantics. **Adding a new criterion** (e.g. "account
age") is just a new enum constant + a new bean — no existing code changes.

### Configurable, data-driven catalogue
Benefits (`TierBenefit`) and rules (`TierEligibilityRule`) are stored as rows,
not flags/columns, and exposed through admin APIs. Operators can add perks,
change discount values, or retune promotion thresholds at runtime.

### Concurrency (bonus)
- **Optimistic locking**: `Subscription` carries a JPA `@Version`. Concurrent
  mutations (e.g. simultaneous upgrade + cancel) can't silently clobber each
  other — the loser fails and is **retried** automatically
  (`@Retryable` on `OptimisticLockingFailureException`, see `SubscriptionService`).
- **Idempotency**: `subscribe` keys on a unique client token, so retries
  (network blips, double-clicks) don't create duplicate subscriptions.
- **Single active subscription** per user is enforced in the service.
- **Testable time**: a `Clock` bean is injected wherever "now" matters, and
  subscriptions lazily expire once their window elapses.

### Tests
- `TierEligibilityEvaluatorTest` — unit tests for the rule engine (AND
  semantics, cohort matching, highest-eligible-tier selection).
- `SubscriptionServiceIntegrationTest` — `@SpringBootTest` covering subscribe
  defaults, idempotency, ineligible-tier rejection, and the
  upgrade/downgrade/cancel lifecycle.
- `SubscriptionConcurrencyTest` — two threads issue interleaved upgrade/downgrade
  calls against the same subscription; asserts no optimistic-lock failure escapes
  (retries recover), the `@Version` advances per change, and the final state is
  consistent (no lost updates).

```bash
./mvnw test     # 11 tests: 4 unit + 6 lifecycle + 1 concurrency
```

---

## Tech stack
Java 21 · Spring Boot 3.4 (Web, Data JPA, Validation, AOP) · Spring Retry ·
H2 (in-memory) · Lombok · JUnit 5 / AssertJ.
