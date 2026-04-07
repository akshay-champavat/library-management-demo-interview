package com.library.api.service;

import com.library.api.dto.BookRequest;
import com.library.api.dto.BookResponse;
import com.library.api.dto.BookUpdateRequest;
import com.library.api.dto.PagedResponse;
import com.library.api.entity.AvailabilityStatus;
import com.library.api.entity.Book;
import com.library.api.exception.DuplicateIsbnException;
import com.library.api.exception.ResourceNotFoundException;
import com.library.api.repository.BookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class BookService {

    private static final Logger log = LoggerFactory.getLogger(BookService.class);

    private final BookRepository bookRepository;
    private final NotificationService notificationService;

    public BookService(BookRepository bookRepository, NotificationService notificationService) {
        this.bookRepository = bookRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public BookResponse createBook(BookRequest request) {
        if (bookRepository.existsByIsbn(request.getIsbn())) {
            throw new DuplicateIsbnException("A book with ISBN '" + request.getIsbn() + "' already exists.");
        }
        Book book = Book.builder()
                .title(request.getTitle())
                .author(request.getAuthor())
                .isbn(request.getIsbn())
                .publishedYear(request.getPublishedYear())
                .availabilityStatus(request.getAvailabilityStatus())
                .build();

        Book saved = bookRepository.save(book);
        log.info("Created book: '{}' (id={})", saved.getTitle(), saved.getId());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<BookResponse> getBooks(String author, Integer publishedYear,
                                                 int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Book> bookPage = bookRepository.findWithFilters(author, publishedYear, pageable);
        return PagedResponse.from(bookPage.map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public BookResponse getBookById(Long id) {
        return toResponse(findActiveBookById(id));
    }

    @Transactional
    public BookResponse updateBook(Long id, BookUpdateRequest request) {
        Book book = findActiveBookById(id);

        boolean bookJustReturned =
                request.getAvailabilityStatus() == AvailabilityStatus.AVAILABLE
                && book.getAvailabilityStatus() == AvailabilityStatus.BORROWED;

        if (request.getTitle() != null) book.setTitle(request.getTitle());
        if (request.getAuthor() != null) book.setAuthor(request.getAuthor());
        if (request.getIsbn() != null && !request.getIsbn().equals(book.getIsbn())) {
            if (bookRepository.existsByIsbnAndIdNot(request.getIsbn(), id)) {
                throw new DuplicateIsbnException("A book with ISBN '" + request.getIsbn() + "' already exists.");
            }
            book.setIsbn(request.getIsbn());
        }
        if (request.getPublishedYear() != null) book.setPublishedYear(request.getPublishedYear());
        if (request.getAvailabilityStatus() != null) book.setAvailabilityStatus(request.getAvailabilityStatus());

        Book saved = bookRepository.save(book);
        log.info("Updated book: '{}' (id={})", saved.getTitle(), saved.getId());

        if (bookJustReturned) {
            log.info("Book '{}' returned — triggering async wishlist notifications.", saved.getTitle());
            notificationService.notifyWishlistedUsers(saved.getId(), saved.getTitle());
        }

        return toResponse(saved);
    }

    @Transactional
    public void deleteBook(Long id) {
        Book book = findActiveBookById(id);
        book.setDeleted(true);
        book.setDeletedAt(LocalDateTime.now());
        bookRepository.save(book);
        log.info("Soft-deleted book: '{}' (id={})", book.getTitle(), id);
    }

    @Transactional(readOnly = true)
    public PagedResponse<BookResponse> searchBooks(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("title").ascending());
        Page<Book> results = bookRepository.search(query.trim(), pageable);
        return PagedResponse.from(results.map(this::toResponse));
    }

    private Book findActiveBookById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
    }

    private BookResponse toResponse(Book book) {
        return BookResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .isbn(book.getIsbn())
                .publishedYear(book.getPublishedYear())
                .availabilityStatus(book.getAvailabilityStatus())
                .createdAt(book.getCreatedAt())
                .updatedAt(book.getUpdatedAt())
                .build();
    }
}
