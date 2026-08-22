# Security Policy — D-Mart API

## Supported Versions

| Version | Supported |
|---|---|
| `0.0.1-SNAPSHOT` | ✅ Active development |

---

## Authentication & Authorization

### JWT-Based Authentication
- All protected endpoints require a valid `Authorization: Bearer <token>` header
- Tokens are signed using **HMAC-SHA256** with a 256-bit key (64-char hex)
- Token expiry: **24 hours** (configurable via `JWT_EXPIRATION_MS`)
- Tokens are **stateless** — no server-side session storage

### Role-Based Access Control (RBAC)

| Role | Scope |
|---|---|
| `CUSTOMER` | Own orders, own returns, public product/category browsing |
| `STAFF` | Order fulfillment, stock management, return processing |
| `ADMIN` | Full access — everything STAFF can do + category/product CRUD + user provisioning |

Role elevation is **not possible** through public APIs:
- `/api/v1/auth/register` always creates a `CUSTOMER` account
- Only an `ADMIN` can provision `STAFF` or `ADMIN` accounts via `/api/v1/admin/users`

---

## Secret Management

### Environment Variables (Required)
All sensitive values **must** be provided via environment variables. **Never hardcode credentials in code or commit them to version control.**

| Variable | Purpose | How to Generate |
|---|---|---|
| `DB_PASSWORD` | PostgreSQL database password | Set in your cloud DB dashboard |
| `JWT_SECRET` | HMAC-SHA256 signing key (64-char hex) | `openssl rand -hex 32` |

### `.gitignore` Coverage
The following files are gitignored and must never be committed:
- `.env` (actual environment file)
- `application-local.properties`
- Any file containing real credentials

The repository only contains `.env.example` with placeholder values.

---

## Input Validation

All incoming request bodies are validated using **Jakarta Bean Validation** (`@Valid`):
- Email format validation (`@Email`)
- Password minimum length (8 characters)
- Phone number: Indian mobile regex (`^[6-9]\\d{9}$`)
- Pincode: Indian 6-digit format (`^[1-9][0-9]{5}$`)
- Price fields: `@DecimalMin`, `@Digits` — prevents negative or malformed prices
- Quantity fields: `@Min(0)` — prevents negative quantities

Validation errors return structured `400 VALIDATION_ERROR` responses (never stack traces).

---

## API Security Headers

- **CSRF**: Disabled (stateless REST API — JWT provides CSRF protection equivalent)
- **Session**: `STATELESS` — no cookies, no session fixation risk
- **Password Storage**: BCrypt hashing with default strength (10 rounds)

---

## Business Logic Security

### Order Ownership
- Customers can only view/cancel their **own** orders
- Order detail endpoint (`GET /api/v1/orders/{id}`) enforces `userId` ownership check at the query level — even with a valid JWT, users cannot access other customers' orders

### Stock Conflict Prevention
- Stock decrements during order placement use **atomic `@Modifying` JPQL** with a `WHERE stockQuantity >= quantity` guard
- If stock is insufficient for any item, the entire order transaction rolls back (all-or-nothing)
- This prevents overselling under concurrent order scenarios

### Return/Exchange Eligibility Guards
The system enforces multiple checks before accepting a return/exchange:
1. Order must be in `DELIVERED` status
2. Request must be within the **7-day eligibility window** (based on `deliveredAt` timestamp)
3. The specific order item must not already be marked as `returned = true`
4. No existing `PENDING` or `APPROVED` request for the same order item

### Exchange Stock Safety
When approving an EXCHANGE:
- The target product's stock is decremented atomically
- If insufficient stock exists for the exchange, approval is rejected with a clear error message
- Original product stock is restored only after the target decrement succeeds

---

## Known Limitations

- **No refresh tokens** — expired tokens require re-login
- **No rate limiting** — brute-force protection not implemented (recommended: add Spring Rate Limiter or API Gateway)
- **No audit logging** — admin/staff actions are not currently written to an audit trail (recommended next step)
- **Payment** — not implemented (out of scope for this assessment)
- **Image uploads** — `imageUrl` fields accept externally hosted URLs only; no file upload endpoint exists

---

## Reporting a Vulnerability

This is an educational/assessment project. For vulnerability reports, contact the repository owner directly via GitHub Issues marked `[SECURITY]`.

---

## Dependency Security

Key dependencies and their versions:

| Dependency | Version | Notes |
|---|---|---|
| Spring Boot | 3.3.2 | LTS release |
| JJWT | 0.12.6 | Latest stable JWT library |
| PostgreSQL Driver | Latest (managed by Spring Boot) | |
| Lombok | Optional (compile-time only, excluded from JAR) | |

Run `./mvnw dependency:check` to audit for known CVEs.
