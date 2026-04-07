package com.library.api.repository;

import com.library.api.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Book entities.
 *
 * Note: Because @SQLRestriction("deleted = false") is on the Book entity,
 * ALL queries here automatically exclude soft-deleted books.
 * No manual filtering is needed.
 */
@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    /**
     * Checks if any active (non-deleted) book exists with the given ISBN.
     * Used during book creation to prevent duplicates.
     */
    boolean existsByIsbn(String isbn);

    /**
     * Checks if any active book OTHER than the given ID has the same ISBN.
     * Used during book update to allow keeping the same ISBN while preventing conflicts.
     */
    boolean existsByIsbnAndIdNot(String isbn, Long id);

    /**
     * Paginated list with optional filters.
     *
     * Both filters are nullable — if null, that filter is skipped entirely.
     * This allows a single query method to handle all filter combinations:
     *   - No filters: returns all books
     *   - Author only: filters by author
     *   - Year only: filters by year
     *   - Both: filters by both
     *
     * LOWER() is used for case-insensitive author matching.
     */
    @Query("SELECT b FROM Book b WHERE " +
           "(:author IS NULL OR LOWER(b.author) LIKE LOWER(CONCAT('%', :author, '%'))) AND " +
           "(:publishedYear IS NULL OR b.publishedYear = :publishedYear)")
    Page<Book> findWithFilters(
            @Param("author") String author,
            @Param("publishedYear") Integer publishedYear,
            Pageable pageable
    );

    /**
     * Full-text partial search across both title and author.
     *
     * Uses LOWER() on both sides for case-insensitive matching.
     * In a production system with large data, consider adding a full-text index
     * or using a search engine like Elasticsearch.
     */
    @Query("SELECT b FROM Book b WHERE " +
           "LOWER(b.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(b.author) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Book> search(@Param("query") String query, Pageable pageable);

    /**
     * Find a specific book by ISBN (used for validation messages).
     */
    Optional<Book> findByIsbn(String isbn);
}
