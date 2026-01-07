# API Endpoint Reference — P2_SKUsProfitability

This document lists the HTTP endpoints implemented in the backend microservices for our project.


Microservices covered in this document
- Auth Service (backend/auth-service) — endpoints for user auth (login/register/health)
- Calculator Service (backend/calculator-service) — endpoints for fee calculations
- Product Service (backend/product-service) — endpoints for SKU management, seller lists, health

---

## Auth Service (backend/auth-service)
Base path: `/api/auth` (AuthController)
Additional public endpoint: `/hello` (MyController)

1) POST /api/auth/login
- Authenticate a user and return a JWT token.
- Method: POST
- Path: /api/auth/login
- Request headers: Content-Type: application/json
- Request body (JSON):
  - email (string) — user's email
  - password (string) — plain-text password to check against stored (bcrypt) hash
- Success response:
  - HTTP 200 OK
  - The token is created by `JwtUtil.generateToken(userId, email, userRole)`.
- Common error responses:
  - HTTP 404 Not Found — when user with given email is not found.
  - HTTP 401 Unauthorized — when password check fails.
  - HTTP 500 Internal Server Error — unexpected errors.
- Notes:
    - Passwords are verified with the injected `PasswordEncoder` (BCrypt).
    - The controller looks up user by email using `AppUserRepository.findByEmail`.

2) POST /api/auth/register
- Register a new customer / seller, create an AppUser and return a JWT token.
- Method: POST
- Path: /api/auth/register
- Request headers: Content-Type: application/json
- Request body (JSON) — corresponds to `RegisterCustomerDTO`:
  - email (string) — required, validated (pattern)
  - password (string) — required, validated (length >= 6 and <= 100)
  - firstName (string) — required
  - lastName (string) — required
- Success response:
  - HTTP 200 OK
  - The controller sets the newly created user's role to `"SELLER"` when generating the token.
- Error responses:
  - HTTP 400 Bad Request — validation failure (ValidationUtil throws IllegalArgumentException)
  - HTTP 500 Internal Server Error — unexpected errors during registration or database save.
- Notes:
    - Validation performed via `ValidationUtil` (validateEmail, validatePassword, validateName).
    - The service checks if an email already exists and throws `IllegalArgumentException("Email already in use.")`.

3) GET /api/auth/health
- Health check for the Auth service.
- Method: GET
- Path: /api/auth/health
- Response:
  - HTTP 200 OK

4) GET /hello
- Simple greeting endpoint for the Auth service (MyController).
- Method: GET
- Path: /hello
- Response:
  - HTTP 200 OK
  - Plain text: `Hello from Eureka Auth Service`

---

## Calculator Service (backend/calculator-service)
Base path: `/api/calculator`  
Controller: `FeeCalculatorController`  
Also: `/hello` (simple greeting)

1) POST /api/calculator/calculate
- Purpose: Calculate Amazon FBA related fees and profitability for provided product dimensions, price and other inputs; persists a Calculation record.
- Method: POST
- Request body: JSON matching `FeeCalculationRequest` (fields seen in DTO usage)
  - Common fields used:
    - length, width, height (BigDecimal)
    - weight (BigDecimal)
    - sellingPrice (BigDecimal)
    - category (String)
    - timeInStorage (BigDecimal) — optional
    - referralFeePercentage (BigDecimal) — optional (if provided used directly)
    - freightCost, freightCostUnit, otherCosts, otherCostsType, fbaFeeCategory
- Authentication: method reads request attribute or header `userId` (optional); if present it is recorded on the saved Calculation.
- Response: HTTP 200 with JSON map containing:
  - sizeTier, fbaFulfillmentFee, storageFeeJanSep, storageFeeOctDec, unitFreightCost, otherCosts,
    totalFeesJanSep, totalFeesOctDec, netProfitJanSep, netProfitOctDec, profitMarginJanSep, profitMarginOctDec, sellingPrice
- Error responses: HTTP 400 with `{ "error": "<message>" }` for validation or processing exceptions.
- Validation: controller calls `ValidationUtil.validateDimension/validateWeight/validatePrice/validateTimeInStorage`.

2) POST /api/calculator/calculate/sku
- Purpose: Same calculation functionality but loads base product values from DB by SKU (fallback to request-provided values when present); computes ROI and saves Calculation.
- Method: POST
- Query param: `sku` (string) — required
- Request body: `FeeCalculationRequest` (same fields as above, used to override product fields where present)
- Response: HTTP 200 with JSON map containing:
  - sku, userId, sizeTier, sellingPrice, totalFeesJanSep, netProfitJanSep, profitMarginJanSep (as percent string), roiJanSep (percent), etc.
- Error: HTTP 400 with `{ "error": "<message>" }` (e.g. SKU not found).

3) GET /api/calculator/health
- Purpose: Health check
- Method: GET
- Response: HTTP 200 `{ "status": "UP", "service": "CalculatorService" }`

4) GET /hello
- Purpose: simple endpoint returning a greeting string
- Method: GET
- Response: plain text `Hello from Eureka Calculator Service`

Notes:
- Calculations are persisted via `CalculationRepository`.

---

## Product Service (backend/product-service)
Base paths:
- SKU management: `/api/skus` (controller `SKUController`)
- Seller lists: `/api/lists` (controller `ListController`)
- Health: `/api/product/health`

1) SKUController — /api/skus
- GET /api/skus
  - Returns list of SKUs (mapped to `SKUDTO`)
  - Requires `userId` attribute (401 if missing)
  - Response: 200 list of SKUDTO
- GET /api/skus/{skuId}
  - Returns single SKU by id (SKUDTO) or 404
  - Requires `userId`
- GET /api/skus/search?q={query}
  - Search by SKU string
  - Requires `userId`
- DELETE /api/skus/{skuId}
  - Delete SKU by id
  - Requires `userRole` to be ADMIN (403 otherwise)
  - Response: 204 No Content on success, 404 if not found
- POST /api/skus/test-kafka?sku={sku}
  - Admin-only endpoint that triggers a Kafka producer to send a message to topic `sku-updates`
  - Response: 200 OK with confirmation string
- Notes:
    - SKU model fields: skuId, sku, productName, description, length, width, height, weight, category, sellingPrice, etc.
    - There are BrightData related DTO imports in controller (BrightDataCollectByUrlRequest, BrightDataDiscoverByBestSellerRequest).

2) ListController — /api/lists (Seller lists)
- GET /api/lists
  - Returns all lists for authenticated user (userId required)
  - Response: 200 list of SellerListDTO
- GET /api/lists/{listId}
  - Return list by id for the authenticated user (or 404)
- POST /api/lists
  - Create a seller list from `SellerListCreateDTO`
  - Requires seller role (`userRole` SELLER or ADMIN)
  - Response: 201 Created with created SellerListDTO
- PUT /api/lists/{listId}
  - Update list (seller role required)
  - Returns 200 updated DTO or 404
- DELETE /api/lists/{listId}
  - Delete list (seller role required)
  - Returns 204 or 404
- POST /api/lists/{listId}/skus/{skuId}
  - Add SKU to list (seller role required)
  - Returns updated SellerListDTO or error
- Notes:
    - The controller obtains the current userId and role from request attributes and enforces authorization locally.
    - Service layer: `SellerListService` handles the business logic.

3) Health endpoint
- GET /api/product/health
  - Response: 200 `{ "status": "UP", "service": "ProductService" }`
Error handling
- `GlobalExceptionHandler` maps:
  - `BrightDataException` -> 500 with ErrorResponse
  - `ResourceNotFoundException` -> 404 with ErrorResponse (includes resourceType/resourceId when provided)
  - `IllegalArgumentException` -> 400
  - Generic Exception -> 500

---
