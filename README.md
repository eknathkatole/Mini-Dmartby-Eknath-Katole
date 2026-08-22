<<<<<<< HEAD
# D-Mart API — Grocery Store Backend

> **Round 2 · Full Stack Developer Practical Assessment**
> Mini D-Mart Grocery Store Application — REST API Backend

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.3.2 |
| Security | Spring Security + JWT (JJWT 0.12.6) |
| Database | PostgreSQL (Neon Tech cloud) |
| ORM | Spring Data JPA / Hibernate |
| Build | Maven |
| Validation | Jakarta Bean Validation |

---

## Getting Started

### Prerequisites
- Java 21+
- Maven 3.8+
- PostgreSQL database (or Neon Tech cloud DB)

### 1. Clone the repository
```bash
git clone <repo-url>
cd demart-api
```

### 2. Configure environment variables

Copy `.env.example` and fill in the values:
```bash
cp .env.example .env
```

Set the following environment variables before starting the application:

| Variable | Description | Example |
|---|---|---|
| `DB_URL` | PostgreSQL JDBC URL | `jdbc:postgresql://localhost:5432/dmart` |
| `DB_USERNAME` | Database username | `postgres` |
| `DB_PASSWORD` | Database password | `secret` |
| `JWT_SECRET` | 64-char hex key for HMAC-SHA256 signing | See below |
| `JWT_EXPIRATION_MS` | Token validity in milliseconds | `86400000` (24h) |
| `CORS_ORIGINS` | Allowed frontend origins | `http://localhost:5173` |
| `RETURN_WINDOW_DAYS` | Return/exchange eligibility window | `7` |

**Generate a JWT secret:**
```bash
openssl rand -hex 32
```

### 3. Run the application
```bash
./mvnw spring-boot:run
```

The server starts on port `8081` (override with `PORT` env var).

---

## API Overview

### Authentication (`/api/v1/auth/**` — public)

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/v1/auth/register` | Customer self-registration |
| `POST` | `/api/v1/auth/login` | Login — returns JWT token |
| `POST` | `/api/v1/admin/users?role=STAFF` | Admin: create STAFF/ADMIN user |

### Categories (`/api/v1/categories/**` — public read)

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/v1/categories` | List all active categories |
| `GET` | `/api/v1/categories/{id}` | Category details |
| `POST` | `/api/v1/admin/categories` | Admin: create category |
| `PUT` | `/api/v1/admin/categories/{id}` | Admin: update category |
| `DELETE` | `/api/v1/admin/categories/{id}` | Admin: soft-delete category |

### Products (`/api/v1/products/**` — public read)

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/v1/products?search=&categoryId=&inStockOnly=` | Browse/search products |
| `GET` | `/api/v1/products/{id}` | Product details |
| `POST` | `/api/v1/admin/products` | Admin: create product |
| `PUT` | `/api/v1/admin/products/{id}` | Admin: update product |
| `PATCH` | `/api/v1/staff/products/{id}/stock` | Staff: update stock (ADD/SUBTRACT/SET) |
| `GET` | `/api/v1/staff/products/low-stock` | Staff: low-stock alert |
| `GET` | `/api/v1/staff/products/out-of-stock` | Staff: out-of-stock list |

### Orders (`/api/v1/orders/**` — authenticated customers)

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/v1/orders` | Place order (STORE_PICKUP or HOME_DELIVERY) |
| `GET` | `/api/v1/orders` | My order history |
| `GET` | `/api/v1/orders/{id}` | Order details |
| `PATCH` | `/api/v1/orders/{id}/cancel` | Cancel order (PLACED/CONFIRMED only) |
| `POST` | `/api/v1/orders/{id}/returns` | Submit return/exchange request |
| `GET` | `/api/v1/orders/my-returns` | My return/exchange requests |

### Staff Operations (`/api/v1/staff/**` — STAFF + ADMIN)

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/v1/staff/orders?status=&fulfillmentType=` | All orders with filters |
| `GET` | `/api/v1/staff/orders/{id}` | Order details |
| `PATCH` | `/api/v1/staff/orders/{id}/status` | Change order status |
| `GET` | `/api/v1/staff/returns?status=PENDING` | All return/exchange requests |
| `PATCH` | `/api/v1/staff/returns/{id}/process` | Approve or reject a request |

---

## RBAC — Role-Based Access Control

| Role | Access |
|---|---|
| `CUSTOMER` | Place/view/cancel own orders, submit returns |
| `STAFF` | All staff endpoints — order fulfillment, stock updates, return processing |
| `ADMIN` | All staff endpoints + category/product management + user provisioning |

**Using the API with a JWT token:**
```http
Authorization: Bearer <your-jwt-token>
```

---

## Order Status Flow

```
PLACED → CONFIRMED → PREPARING → READY_FOR_PICKUP → DELIVERED  (STORE_PICKUP)
PLACED → CONFIRMED → PREPARING → OUT_FOR_DELIVERY → DELIVERED   (HOME_DELIVERY)

Any status before OUT_FOR_DELIVERY / DELIVERED → CANCELLED (stock restored)
```

---

## Return / Exchange Policy

- Only DELIVERED orders are eligible
- Request must be submitted within **7 days** of delivery date
- Each order item can only be returned/exchanged once
- EXCHANGE requires the target product to have available stock
- Staff note is mandatory when **rejecting** a request

---

## Test Credentials

After running the app, create test users via the API:

```json
POST /api/v1/auth/register
{ "name": "Test Customer", "email": "customer@dmart.com", "password": "Pass@1234", "phone": "9876543210" }

POST /api/v1/admin/users?role=STAFF   (requires admin token)
{ "name": "Store Staff", "email": "staff@dmart.com", "password": "Pass@1234", "phone": "9876543211" }

POST /api/v1/admin/users?role=ADMIN   (requires admin token)
{ "name": "Admin User", "email": "admin@dmart.com", "password": "Pass@1234", "phone": "9876543212" }
```

> **Note:** Bootstrap the first ADMIN account directly in the database via SQL, then use that token to provision further staff/admin users through the API.

---

## Project Structure

```
src/main/java/edu/demart_api/
├── config/         JPA auditing config
├── controller/     REST controllers (Auth, Category, Product, Order, Staff)
├── dto/            Request and Response DTOs
├── entity/         JPA entities + enums
├── exception/      Custom exceptions + GlobalExceptionHandler
├── repository/     Spring Data JPA repositories
├── security/       JwtService, JwtAuthenticationFilter, SecurityConfig, SecurityUtils
└── service/        Service interfaces + implementations
```

---

## AI Usage

This project was developed with assistance from **Google Antigravity (AGY)** AI coding assistant.
The AI was used to:
- Generate boilerplate entity/DTO/repository/service/controller structure
- Design JPQL queries for filtering and JOIN FETCH to avoid N+1
- Implement JWT filter and Spring Security configuration
- Design the return/exchange eligibility and inventory restock logic

All business logic, architectural decisions, and final code review were performed by the developer.
=======
# Mini-Dmart-prototypeByEknathKatole
>>>>>>> 9f4e5449126f6d6dbb4d6e2dead830566c83fbff
