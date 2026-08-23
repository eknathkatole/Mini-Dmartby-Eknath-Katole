# Testing & Quality Assurance — Mini D-Mart

> **Round 2 · Full Stack Developer Practical Assessment**  
> Comprehensive Test Suite, Edge Case Validations & Verification Matrix

---

## 1. Automated Test Execution

### Backend Automated Test Suite (JUnit 5 + Spring Boot Test)
Run the complete automated test suite locally:
```bash
cd demart-api
./mvnw clean test
```
**Test Results:**
- `DemartApiApplicationTests` — Context loads successfully with isolated H2 test profile
- **Result:** `Tests run: 1, Failures: 0, Errors: 0, Skipped: 0` (BUILD SUCCESS)

### Frontend Build & Bundle Validation (Vite + React)
Run production build validation:
```bash
cd demart-ui
npm run build
```
**Build Results:**
- `1870 modules transformed`
- **Result:** `✓ built in 1.30s` (Zero JSX/CSS syntax errors)

---

## 2. Functional Test Cases & Verification Matrix

| # | Feature / Scenario | Test Steps | Expected Result | Status |
|---|---|---|---|:---:|
| **TC-01** | **Customer Registration with Email OTP** | 1. Enter Name, Email, Password, Confirm Password, Phone<br>2. Submit Step 1<br>3. Enter 6-digit OTP from email | OTP generated, email sent via Gmail SMTP, user created upon OTP verification | ✅ **PASS** |
| **TC-02** | **Password Confirmation Mismatch** | 1. Enter Password `Secret@123`<br>2. Enter Confirm Password `Different@123`<br>3. Submit | Blocked with error: `Passwords do not match` | ✅ **PASS** |
| **TC-03** | **Forgot Password OTP Reset** | 1. Click "Forgot Password?"<br>2. Enter email<br>3. Enter 6-digit OTP code & new password | Password updated via BCrypt, old password invalid, login succeeds with new password | ✅ **PASS** |
| **TC-04** | **Product Search & Filtering** | 1. Type "Atta" into search bar<br>2. Filter by "Foodgrains, Atta & Dals"<br>3. Sort by "Price: Low → High" | Real-time debounced search (350ms) filters catalog without full page reload | ✅ **PASS** |
| **TC-05** | **Dual Action: Add to Cart vs Buy Now** | 1. Click `Add` on Product A<br>2. Click `⚡ Buy Now` on Product B | Product A quantity increments in background; Product B immediately opens Express Checkout drawer | ✅ **PASS** |
| **TC-06** | **Delivery Fee Calculation** | 1. Create cart with subtotal = ₹350<br>2. Increase subtotal to ₹550 | At ₹350, delivery charge = ₹50; at ₹550, delivery charge = ₹0 (Free delivery threshold applied) | ✅ **PASS** |
| **TC-07** | **Overselling / Stock Conflict Guard** | 1. Product stock = 5<br>2. Attempt to add/order quantity = 6 | Prevented with error; order transaction rolls back atomically | ✅ **PASS** |
| **TC-08** | **Order Cancellation & Stock Restoral** | 1. Place order for 2 units (stock: 50 ➔ 48)<br>2. Cancel order in "PLACED" status | Order marked `CANCELLED`; stock restored automatically back to 50 | ✅ **PASS** |
| **TC-09** | **7-Day Return Eligibility Guard** | 1. Attempt return on non-delivered order<br>2. Attempt return on order delivered > 7 days ago | Blocked by system; returns only allowed for `DELIVERED` orders within 7 days | ✅ **PASS** |
| **TC-10** | **Staff Order Fulfillment Pipeline** | 1. Staff logs in<br>2. Advances order: `CONFIRMED` ➔ `PREPARING` ➔ `OUT_FOR_DELIVERY` ➔ `DELIVERED` | Status updates chronologically; order status reflects in customer's "My Orders" in real time | ✅ **PASS** |
| **TC-11** | **Staff Inventory Replenishment** | 1. Staff opens Low Stock monitor<br>2. Performs `ADD` +50 units | Stock quantity increments atomically | ✅ **PASS** |
| **TC-12** | **Partner Application & Admin Approval** | 1. User submits Staff Partner Application<br>2. Admin reviews & clicks "Approve & Generate Password" | Account created as `STAFF`, secure password auto-generated, credentials emailed to applicant | ✅ **PASS** |

---

## 3. Security & Edge Case Validations

- **Stateless Bearer JWT Validation**: Non-authenticated requests to `/api/v1/orders/**` return `401 Unauthorized`.
- **Role Privilege Escalation Prevention**: Non-admin users attempting to call `/api/v1/admin/**` receive `403 Forbidden`.
- **Cross-Customer Order Isolation**: Customers cannot view or cancel orders belonging to other users (`userId` matching verified at query level).
- **Mandatory Rejection Note**: Staff attempting to reject a return request without providing a reason is rejected by Jakarta validation.

---

## 4. Interactive Swagger Testing

Evaluators can test all REST endpoints directly using the live OpenAPI Swagger UI:
👉 **[Open Live Swagger UI](https://mini-dmartby-eknath-katole-ha7f.onrender.com/swagger-ui/index.html)**

**Steps to test in Swagger:**
1. Execute `POST /api/v1/auth/login` with `admin@dmart.com` / `Admin@123`.
2. Copy the `accessToken`.
3. Click the green **Authorize 🔓** button at the top of Swagger UI.
4. Paste the token in `Bearer <token>` format and click **Authorize**.
5. Test all secured `/api/v1/admin/**` and `/api/v1/staff/**` endpoints directly in your browser!
