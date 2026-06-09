# FirstClub — Membership Program with Tiers

## 🧩 Problem statement

Design and build the **backend system for a Membership Program** for FirstClub.
The platform offers users **subscription-based memberships with tiered benefits**,
integrated smoothly with the shopping and checkout journey. Users subscribe to a
plan, receive tier-based perks, and progress through tiers based on their
shopping activity.

## 🎯 What this project delivers

A **running, demo-able Spring Boot backend** (Java 21 · Spring Boot 3.4 · H2 ·
Spring Data JPA) with functional REST APIs, an interactive demo console, and a
configurable, data-driven tier engine. Plans, tiers, benefits and promotion
rules are all stored as data, so the program can be reshaped at runtime without
code changes.

- **Run:** `./mvnw spring-boot:run` → interactive console at `http://localhost:8080/`
- **Docs:** `docs/ARCHITECTURE.html` (flow + concurrency), `docs/ENTITY_DESIGN.html` (ER diagram)
- **Tests:** `./mvnw test` → 11 tests (unit + integration + concurrency)

---

## ✅ Requirements → implementation

### 1. Membership Plans
> Monthly, Quarterly, Yearly plans, each with specific pricing.

- `MembershipPlan` entity with a `BillingCycle` enum (`MONTHLY`, `QUARTERLY`,
  `YEARLY`) that carries its duration in months (drives expiry calculation) and a
  `price`.
- Seeded plans: Monthly ₹199, Quarterly ₹499, Yearly ₹1499.
- API: `GET /api/plans`, `POST /api/plans`.

### 2. Membership Benefits *(configurable)*
> Free delivery, extra X% discount, exclusive deals / early access, optional
> priority support — each tier unlocks additional perks, **configurable**.

- `TierBenefit` rows (not hard-coded flags) keyed by a `BenefitType` enum:
  `FREE_DELIVERY`, `EXTRA_DISCOUNT_PERCENT` (carries a value), `EXCLUSIVE_DEALS`,
  `EARLY_ACCESS_TO_SALES`, `PRIORITY_SUPPORT`.
- Higher tiers grant strictly richer perks (e.g. 5% → 10% → 20% discount).
- Fully configurable at runtime: `POST /api/tiers/{id}/benefits`.

### 3. User Actions
> Get plans/tiers, subscribe (plan + tier), upgrade/downgrade/cancel, track
> current membership and expiry.

| Action | API |
|--------|-----|
| Get plans & tiers to choose from | `GET /api/plans`, `GET /api/tiers` |
| Subscribe to a plan (+ tier) | `POST /api/subscriptions` |
| Upgrade tier | `POST /api/subscriptions/{id}/upgrade` |
| Downgrade tier | `POST /api/subscriptions/{id}/downgrade` |
| Cancel subscription | `POST /api/subscriptions/{id}/cancel` |
| Track current membership & expiry | `GET /api/subscriptions/users/{userId}/current` |
| See highest tier a user qualifies for | `GET /api/subscriptions/users/{userId}/eligible-tier` |

Subscribing with no tier auto-assigns the **highest tier the user qualifies for**
(falling back to the base tier). Upgrades enforce eligibility.

### 4. Membership Tiers
> Silver / Gold / Platinum, with movement based on order count, monthly order
> value, or cohort.

- `MembershipTier` (ordered by `rank`) owns configurable `TierEligibilityRule`
  rows. A user qualifies for a tier when **all** its rules pass.
- Criteria map to an `EligibilityRuleType`: `MIN_ORDER_COUNT`,
  `MIN_MONTHLY_ORDER_VALUE`, `REQUIRED_COHORT`.
- Seeded: **SILVER** (base, open to all), **GOLD** (≥ 5 orders),
  **PLATINUM** (≥ 10 orders **AND** ≥ ₹5000 monthly spend).
- `Order` entity feeds the order-count and monthly-spend signals.

---

## 🏆 Evaluation criteria → how it's addressed

### Abstractions
- **Strategy pattern** for tier eligibility: a `TierEligibilityStrategy` interface
  with one implementation per criterion, aggregated by `TierEligibilityEvaluator`.
- `UserActivitySnapshot` value object isolates the signals used for evaluation.
- DTOs + `DtoMapper` separate the API contract from the persistence model.

### Entity design
- Normalised, data-driven model: `User`, `MembershipPlan`, `MembershipTier`,
  `TierBenefit`, `TierEligibilityRule`, `Subscription`, `Order`.
- Plan and tier are orthogonal (billing cadence vs. benefit level).
- See `docs/ENTITY_DESIGN.html` for the ER diagram and rationale.

### Extensibility & modularity
- **Open/Closed**: a new tier criterion = one enum constant + one Spring bean;
  no existing code changes.
- Benefits and rules are configurable as data via admin APIs.
- Clean layering: `controller → service → repository`, with the tier engine as an
  isolated module. See `docs/ARCHITECTURE.html`.

### Java best practices
- Constructor injection, `@Transactional` boundaries, Bean Validation on inputs,
  a global exception handler with consistent error responses, immutable DTOs
  (records), and an injectable `Clock` for testable time.

### Concurrency *(bonus)*
- **Optimistic locking** (`@Version`) on `Subscription` prevents lost updates.
- **Automatic retry** (`@Retryable` on `OptimisticLockingFailureException`)
  recovers from transient version clashes.
- **Idempotent subscribe** (unique key) prevents duplicate subscriptions.
- Proven by `SubscriptionConcurrencyTest` (concurrent interleaved tier changes).

---

## 📦 Tech stack
Java 21 · Spring Boot 3.4 (Web, Data JPA, Validation, AOP) · Spring Retry ·
H2 (in-memory) · Lombok · JUnit 5 / AssertJ · Maven (wrapper included).

See [`README.md`](README.md) for full run/demo instructions and the API walk-through.
