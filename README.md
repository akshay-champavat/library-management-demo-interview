# Library Book Inventory API

Spring Boot REST API for managing a library's book inventory with CRUD, search, pagination, soft deletes, and async wishlist notifications.

---

## Requirements

| Tool | Version |
|------|---------|
| Java (JDK) | **25** (developed and tested on this version) |
| Apache Maven | 3.6+ |

> Spring Boot 3.2.0 requires Java 17 minimum, but this project was only tested on **Java 25**. Use Java 25 to avoid surprises.

No database installation needed — H2 in-memory database is embedded and starts automatically.

---

## How to Run

```bash
# Clone the repo
git clone https://github.com/akshay-champavat/library-management-demo-interview.git

# Navigate into the project
cd library-management-demo-interview

# Start the application
mvn spring-boot:run
```

App is ready when you see: `Started LibraryApiApplication in X seconds`

| URL | What it is |
|-----|-----------|
| `http://localhost:8080` | API base |
| `http://localhost:8080/swagger-ui.html` | Interactive API docs (try endpoints from browser) |
| `http://localhost:8080/h2-console` | Database console — **DB Name:** `librarydb` · **JDBC URL:** `jdbc:h2:mem:librarydb` · **Username:** `sa` · **Password:** *(blank)* |

```bash
# Run all tests
mvn test

# Run by class
mvn test -Dtest=BookServiceTest
mvn test -Dtest=BookControllerTest
mvn test -Dtest=NotificationServiceTest
mvn test -Dtest=PublishedYearValidatorTest
```

---

## API Endpoints

All responses follow this structure:
```json
{ "success": true, "message": "...", "data": { ... } }
```

---

### POST `/api/books` — Create a book

```bash
curl -X POST http://localhost:8080/api/books \
  -H "Content-Type: application/json" \
  -d '{"title":"Clean Code","author":"Robert Martin","isbn":"978-0132350884","publishedYear":2008,"availabilityStatus":"AVAILABLE"}'
```

| Field | Required | Rules |
|-------|----------|-------|
| `title` | Yes | Not blank |
| `author` | Yes | Not blank |
| `isbn` | Yes | Not blank, must be unique |
| `publishedYear` | Yes | 1000 to current year |
| `availabilityStatus` | Yes | `AVAILABLE` or `BORROWED` |

Returns `201 Created` · `400` validation failed · `409` duplicate ISBN

---

### GET `/api/books` — List all books (paginated)

```bash
curl "http://localhost:8080/api/books"
curl "http://localhost:8080/api/books?author=Orwell&page=0&size=5&sortBy=title&sortDir=asc"
```

| Param | Default | Description |
|-------|---------|-------------|
| `author` | — | Partial match, case-insensitive |
| `publishedYear` | — | Exact year filter |
| `page` | `0` | Page number (0-indexed) |
| `size` | `10` | Items per page |
| `sortBy` | `createdAt` | `title`, `author`, `publishedYear`, `createdAt` |
| `sortDir` | `desc` | `asc` or `desc` |

Returns `200 OK` with paginated list. Soft-deleted books are automatically excluded.

---

### GET `/api/books/{id}` — Get a single book

```bash
curl http://localhost:8080/api/books/1
```

Returns `200 OK` · `404` not found

---

### PUT `/api/books/{id}` — Update a book (partial)

Only fields included in the body are updated. All fields are optional.

```bash
# Mark a book as returned — triggers async wishlist notifications
curl -X PUT http://localhost:8080/api/books/3 \
  -H "Content-Type: application/json" \
  -d '{"availabilityStatus":"AVAILABLE"}'
```

Returns `200 OK` · `404` not found · `409` ISBN conflict

> **Async Notification (simulated):** When status changes `BORROWED → AVAILABLE`, the system notifies wishlisted users in a background thread. In this project, notifications are **simulated via console logs only** — no actual email or push notification is sent. This is intentional; the purpose is to demonstrate the `@Async` pattern (background thread execution, fire-and-forget). Watch the app console after this call and you will see log lines like:
> ```
> [notification-1] INFO - Notification prepared for user_id 1: Book 'Dune' is now available.
> ```
> In a real system, these log lines would be replaced with actual email/SMS/push calls.

---

### DELETE `/api/books/{id}` — Soft delete a book

```bash
curl -X DELETE http://localhost:8080/api/books/1
```

The book is **not removed from the database** — it is marked `deleted=true` with a timestamp. It becomes invisible to all API queries but remains for audit purposes.

Returns `200 OK` · `404` not found

---

### GET `/api/books/search` — Search by title or author

```bash
curl "http://localhost:8080/api/books/search?q=gatsby"
curl "http://localhost:8080/api/books/search?q=frank&page=0&size=5"
```

Case-insensitive partial match across both title and author simultaneously. `q` is required — returns `400` if missing.

---

## Seed Data

The database is pre-populated on every startup with 3 users, 7 visible books (1 soft-deleted), and 4 wishlist entries.

To demo async notifications: call `PUT /api/books/3` with `{"availabilityStatus":"AVAILABLE"}` — Dune is seeded as `BORROWED` with 2 users (alice, bob) wishlisting it. Watch the app console for background thread logs. Note: notifications are **simulated as console logs only**, no real emails are sent.
