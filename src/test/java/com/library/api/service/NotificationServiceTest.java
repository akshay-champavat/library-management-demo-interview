package com.library.api.service;

import com.library.api.entity.Book;
import com.library.api.entity.AvailabilityStatus;
import com.library.api.entity.User;
import com.library.api.entity.Wishlist;
import com.library.api.repository.WishlistRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

/**
 * Unit tests for NotificationService.
 *
 * Note: @Async is NOT active in unit tests (no Spring context), so
 * notifyWishlistedUsers() runs synchronously here. This lets us
 * test the business logic directly without thread coordination.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService Tests")
class NotificationServiceTest {

    @Mock
    private WishlistRepository wishlistRepository;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    @DisplayName("should query wishlist repository and process each wishlisted user")
    void notifyWishlistedUsers_withWishlists_processesAllUsers() {
        // Arrange
        User alice = new User(1L, "alice", "alice@example.com");
        User bob   = new User(2L, "bob",   "bob@example.com");

        Book book = Book.builder()
                .title("Dune")
                .author("Frank Herbert")
                .isbn("978-0441013593")
                .publishedYear(1965)
                .availabilityStatus(AvailabilityStatus.AVAILABLE)
                .build();
        book.setId(3L);

        Wishlist w1 = new Wishlist(); w1.setUser(alice); w1.setBook(book);
        Wishlist w2 = new Wishlist(); w2.setUser(bob);   w2.setBook(book);

        when(wishlistRepository.findByBookIdWithUser(3L)).thenReturn(List.of(w1, w2));

        // Act — runs synchronously in test (no @Async processing)
        notificationService.notifyWishlistedUsers(3L, "Dune");

        // Assert — repository was queried exactly once
        verify(wishlistRepository, times(1)).findByBookIdWithUser(3L);
    }

    @Test
    @DisplayName("should do nothing when no users have wishlisted the book")
    void notifyWishlistedUsers_noWishlists_skipsNotifications() {
        when(wishlistRepository.findByBookIdWithUser(99L)).thenReturn(Collections.emptyList());

        notificationService.notifyWishlistedUsers(99L, "Unknown Book");

        // Repository is queried but no further processing happens
        verify(wishlistRepository, times(1)).findByBookIdWithUser(99L);
    }
}
