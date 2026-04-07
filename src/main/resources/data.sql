-- ─────────────────────────────────────────────────────────────────────────────
-- Seed Data for Library API
--
-- This file is loaded automatically by Spring Boot on every startup.
-- It pre-populates the database so you can immediately test all features
-- without having to create data manually via the API first.
--
-- Loaded AFTER Hibernate creates the schema (due to defer-datasource-initialization=true)
-- ─────────────────────────────────────────────────────────────────────────────


-- ─── Users ───────────────────────────────────────────────────────────────────
-- These are the library members who can wishlist books.
INSERT INTO users (id, username, email) VALUES
(1, 'alice_wonder', 'alice@example.com'),
(2, 'bob_builder',  'bob@example.com'),
(3, 'carol_reads',  'carol@example.com');


-- ─── Books ───────────────────────────────────────────────────────────────────
-- Mix of AVAILABLE and BORROWED books across different authors and years.
-- book id=6 is soft-deleted to demonstrate the audit trail feature.
INSERT INTO books (id, title, author, isbn, published_year, availability_status, deleted, deleted_at, created_at, updated_at) VALUES
(1, 'The Great Gatsby',          'F. Scott Fitzgerald', '978-0743273565', 1925, 'AVAILABLE', false, NULL, NOW(), NOW()),
(2, 'To Kill a Mockingbird',     'Harper Lee',           '978-0061935466', 1960, 'AVAILABLE', false, NULL, NOW(), NOW()),
(3, 'Dune',                      'Frank Herbert',        '978-0441013593', 1965, 'BORROWED',  false, NULL, NOW(), NOW()),
(4, '1984',                      'George Orwell',        '978-0451524935', 1949, 'BORROWED',  false, NULL, NOW(), NOW()),
(5, 'The Hitchhiker''s Guide',   'Douglas Adams',        '978-0345391803', 1979, 'AVAILABLE', false, NULL, NOW(), NOW()),
(6, 'Old Man and the Sea',       'Ernest Hemingway',     '978-0684801469', 1952, 'AVAILABLE', true,  NOW(), NOW(), NOW()),
(7, 'Brave New World',           'Aldous Huxley',        '978-0060850524', 1932, 'AVAILABLE', false, NULL, NOW(), NOW()),
(8, 'The Catcher in the Rye',    'J.D. Salinger',        '978-0316769174', 1951, 'BORROWED',  false, NULL, NOW(), NOW());


-- ─── Wishlists ────────────────────────────────────────────────────────────────
-- These entries represent users waiting to be notified when a BORROWED book is returned.
--
-- Try this flow to test async notifications:
--   1. Open Swagger UI at http://localhost:8080/swagger-ui.html
--   2. Call PUT /api/books/3 with body: { "availabilityStatus": "AVAILABLE" }
--      (Dune is BORROWED and has 2 users wishlisting it)
--   3. Watch the console — you'll see notification logs appear on a background thread!
--
INSERT INTO wishlists (id, user_id, book_id, wished_at) VALUES
(1, 1, 3, NOW()),   -- alice is waiting for Dune
(2, 2, 3, NOW()),   -- bob is also waiting for Dune
(3, 3, 4, NOW()),   -- carol is waiting for 1984
(4, 1, 8, NOW());   -- alice is also waiting for Catcher in the Rye
