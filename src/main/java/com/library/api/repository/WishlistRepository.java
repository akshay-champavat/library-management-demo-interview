package com.library.api.repository;

import com.library.api.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    /**
     * Fetches all wishlist entries for a given book, eagerly loading
     * the associated User to avoid N+1 queries in the notification loop.
     *
     * JOIN FETCH ensures we get all user data in a single SQL query
     * rather than one query per wishlist entry.
     */
    @Query("SELECT w FROM Wishlist w JOIN FETCH w.user WHERE w.book.id = :bookId")
    List<Wishlist> findByBookIdWithUser(@Param("bookId") Long bookId);
}
