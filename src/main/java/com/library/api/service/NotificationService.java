package com.library.api.service;

import com.library.api.entity.Wishlist;
import com.library.api.repository.WishlistRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final WishlistRepository wishlistRepository;

    public NotificationService(WishlistRepository wishlistRepository) {
        this.wishlistRepository = wishlistRepository;
    }

    @Async("notificationExecutor")
    public void notifyWishlistedUsers(Long bookId, String bookTitle) {
        log.info("Starting wishlist notification task for book: '{}' (id={})", bookTitle, bookId);

        List<Wishlist> wishlists = wishlistRepository.findByBookIdWithUser(bookId);

        if (wishlists.isEmpty()) {
            log.info("No users have wishlisted '{}'. No notifications to send.", bookTitle);
            return;
        }

        log.info("Found {} user(s) who wishlisted '{}'. Sending notifications...", wishlists.size(), bookTitle);

        for (Wishlist wishlist : wishlists) {
            log.info("Notification prepared for user_id {}: Book '{}' is now available.",
                    wishlist.getUser().getId(), bookTitle);
        }

        log.info("Completed wishlist notifications for '{}' ({} sent).", bookTitle, wishlists.size());
    }
}
