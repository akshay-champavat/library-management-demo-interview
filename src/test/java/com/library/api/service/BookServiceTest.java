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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BookService.
 *
 * Uses Mockito to mock all dependencies so each test is isolated
 * and fast — no Spring context, no database.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BookService Tests")
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private BookService bookService;

    private Book availableBook;
    private Book borrowedBook;

    @BeforeEach
    void setUp() {
        availableBook = Book.builder()
                .title("The Great Gatsby")
                .author("F. Scott Fitzgerald")
                .isbn("978-0743273565")
                .publishedYear(1925)
                .availabilityStatus(AvailabilityStatus.AVAILABLE)
                .build();
        availableBook.setId(1L);

        borrowedBook = Book.builder()
                .title("Dune")
                .author("Frank Herbert")
                .isbn("978-0441013593")
                .publishedYear(1965)
                .availabilityStatus(AvailabilityStatus.BORROWED)
                .build();
        borrowedBook.setId(2L);
    }

    // ── CREATE ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("createBook: should create and return book when ISBN is unique")
    void createBook_uniqueIsbn_returnsBookResponse() {
        BookRequest request = buildBookRequest("Clean Code", "Robert Martin", "978-0132350884", 2008);
        when(bookRepository.existsByIsbn("978-0132350884")).thenReturn(false);
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> {
            Book b = inv.getArgument(0);
            b.setId(10L);
            return b;
        });

        BookResponse response = bookService.createBook(request);

        assertThat(response.getTitle()).isEqualTo("Clean Code");
        assertThat(response.getAuthor()).isEqualTo("Robert Martin");
        assertThat(response.getIsbn()).isEqualTo("978-0132350884");
        assertThat(response.getAvailabilityStatus()).isEqualTo(AvailabilityStatus.AVAILABLE);
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    @DisplayName("createBook: should throw DuplicateIsbnException when ISBN already exists")
    void createBook_duplicateIsbn_throwsException() {
        BookRequest request = buildBookRequest("Any Book", "Any Author", "978-0743273565", 2020);
        when(bookRepository.existsByIsbn("978-0743273565")).thenReturn(true);

        assertThatThrownBy(() -> bookService.createBook(request))
                .isInstanceOf(DuplicateIsbnException.class)
                .hasMessageContaining("978-0743273565");

        verify(bookRepository, never()).save(any());
    }

    // ── READ ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getBookById: should return book when ID exists")
    void getBookById_existingId_returnsBookResponse() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(availableBook));

        BookResponse response = bookService.getBookById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("The Great Gatsby");
    }

    @Test
    @DisplayName("getBookById: should throw ResourceNotFoundException when ID not found")
    void getBookById_nonExistentId_throwsResourceNotFoundException() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.getBookById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("getBooks: should return paginated books with no filters applied")
    void getBooks_noFilters_returnsPaginatedResults() {
        var pageImpl = new PageImpl<>(List.of(availableBook, borrowedBook));
        when(bookRepository.findWithFilters(isNull(), isNull(), any(Pageable.class)))
                .thenReturn(pageImpl);

        PagedResponse<BookResponse> result = bookService.getBooks(null, null, 0, 10, "createdAt", "desc");

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("getBooks: should pass author filter to repository")
    void getBooks_withAuthorFilter_passesFilterToRepository() {
        var pageImpl = new PageImpl<>(List.of(availableBook));
        when(bookRepository.findWithFilters(eq("Fitzgerald"), isNull(), any(Pageable.class)))
                .thenReturn(pageImpl);

        PagedResponse<BookResponse> result = bookService.getBooks("Fitzgerald", null, 0, 10, "title", "asc");

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getAuthor()).isEqualTo("F. Scott Fitzgerald");
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("updateBook: should update and return book with changed fields only")
    void updateBook_validRequest_updatesOnlyProvidedFields() {
        BookUpdateRequest request = new BookUpdateRequest();
        request.setTitle("The Great Gatsby (Updated)");

        when(bookRepository.findById(1L)).thenReturn(Optional.of(availableBook));
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        BookResponse response = bookService.updateBook(1L, request);

        assertThat(response.getTitle()).isEqualTo("The Great Gatsby (Updated)");
        assertThat(response.getAuthor()).isEqualTo("F. Scott Fitzgerald"); // unchanged
    }

    @Test
    @DisplayName("updateBook: should trigger async notification when status changes BORROWED → AVAILABLE")
    void updateBook_borrowedToAvailable_triggersWishlistNotification() {
        BookUpdateRequest request = new BookUpdateRequest();
        request.setAvailabilityStatus(AvailabilityStatus.AVAILABLE);

        when(bookRepository.findById(2L)).thenReturn(Optional.of(borrowedBook));
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        bookService.updateBook(2L, request);

        verify(notificationService).notifyWishlistedUsers(eq(2L), eq("Dune"));
    }

    @Test
    @DisplayName("updateBook: should NOT trigger notification when status stays AVAILABLE")
    void updateBook_availableToAvailable_doesNotTriggerNotification() {
        BookUpdateRequest request = new BookUpdateRequest();
        request.setAvailabilityStatus(AvailabilityStatus.AVAILABLE);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(availableBook));
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        bookService.updateBook(1L, request);

        verify(notificationService, never()).notifyWishlistedUsers(any(), any());
    }

    @Test
    @DisplayName("updateBook: should NOT trigger notification when status changes AVAILABLE → BORROWED")
    void updateBook_availableToBorrowed_doesNotTriggerNotification() {
        BookUpdateRequest request = new BookUpdateRequest();
        request.setAvailabilityStatus(AvailabilityStatus.BORROWED);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(availableBook));
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        bookService.updateBook(1L, request);

        verify(notificationService, never()).notifyWishlistedUsers(any(), any());
    }

    @Test
    @DisplayName("updateBook: should throw DuplicateIsbnException when updated ISBN belongs to another book")
    void updateBook_isbnConflict_throwsDuplicateIsbnException() {
        BookUpdateRequest request = new BookUpdateRequest();
        request.setIsbn("978-0441013593"); // belongs to borrowedBook (id=2)

        when(bookRepository.findById(1L)).thenReturn(Optional.of(availableBook));
        when(bookRepository.existsByIsbnAndIdNot("978-0441013593", 1L)).thenReturn(true);

        assertThatThrownBy(() -> bookService.updateBook(1L, request))
                .isInstanceOf(DuplicateIsbnException.class)
                .hasMessageContaining("978-0441013593");
    }

    @Test
    @DisplayName("updateBook: should throw ResourceNotFoundException when book ID does not exist")
    void updateBook_invalidId_throwsResourceNotFoundException() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.updateBook(99L, new BookUpdateRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── DELETE ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteBook: should soft-delete book by setting deleted=true and recording deletedAt")
    void deleteBook_validId_softDeletesBook() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(availableBook));
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        bookService.deleteBook(1L);

        ArgumentCaptor<Book> captor = ArgumentCaptor.forClass(Book.class);
        verify(bookRepository).save(captor.capture());

        Book saved = captor.getValue();
        assertThat(saved.isDeleted()).isTrue();
        assertThat(saved.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("deleteBook: should throw ResourceNotFoundException when book ID does not exist")
    void deleteBook_invalidId_throwsResourceNotFoundException() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.deleteBook(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(bookRepository, never()).save(any());
    }

    // ── SEARCH ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("searchBooks: should return paginated results matching query")
    void searchBooks_withQuery_returnsPaginatedResults() {
        var pageImpl = new PageImpl<>(List.of(availableBook));
        when(bookRepository.search(eq("gatsby"), any(Pageable.class))).thenReturn(pageImpl);

        PagedResponse<BookResponse> result = bookService.searchBooks("gatsby", 0, 10);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("The Great Gatsby");
    }

    @Test
    @DisplayName("searchBooks: should trim whitespace from search query")
    void searchBooks_queryWithWhitespace_trimsBeforeSearch() {
        var pageImpl = new PageImpl<>(List.of(availableBook));
        when(bookRepository.search(eq("gatsby"), any(Pageable.class))).thenReturn(pageImpl);

        bookService.searchBooks("  gatsby  ", 0, 10);

        verify(bookRepository).search(eq("gatsby"), any(Pageable.class));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private BookRequest buildBookRequest(String title, String author, String isbn, int year) {
        BookRequest req = new BookRequest();
        req.setTitle(title);
        req.setAuthor(author);
        req.setIsbn(isbn);
        req.setPublishedYear(year);
        req.setAvailabilityStatus(AvailabilityStatus.AVAILABLE);
        return req;
    }
}
