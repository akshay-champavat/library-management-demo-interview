package com.library.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.api.dto.BookRequest;
import com.library.api.dto.BookResponse;
import com.library.api.dto.BookUpdateRequest;
import com.library.api.dto.PagedResponse;
import com.library.api.entity.AvailabilityStatus;
import com.library.api.exception.DuplicateIsbnException;
import com.library.api.exception.GlobalExceptionHandler;
import com.library.api.exception.ResourceNotFoundException;
import com.library.api.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller layer tests using MockMvc.
 *
 * @WebMvcTest loads only the web layer (no full Spring context),
 * making these tests fast while still testing routing, serialisation,
 * validation, and error handling end-to-end.
 */
@WebMvcTest(BookController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("BookController Tests")
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookService bookService;

    private BookResponse sampleResponse;

    @BeforeEach
    void setUp() {
        sampleResponse = BookResponse.builder()
                .id(1L)
                .title("The Great Gatsby")
                .author("F. Scott Fitzgerald")
                .isbn("978-0743273565")
                .publishedYear(1925)
                .availabilityStatus(AvailabilityStatus.AVAILABLE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // ── POST /api/books ───────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/books: should return 201 when request is valid")
    void createBook_validRequest_returns201() throws Exception {
        BookRequest request = buildBookRequest("The Great Gatsby", "F. Scott Fitzgerald",
                "978-0743273565", 1925, AvailabilityStatus.AVAILABLE);

        when(bookService.createBook(any(BookRequest.class))).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("The Great Gatsby"))
                .andExpect(jsonPath("$.data.isbn").value("978-0743273565"));
    }

    @Test
    @DisplayName("POST /api/books: should return 400 when required fields are missing")
    void createBook_missingRequiredFields_returns400() throws Exception {
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.title").exists())
                .andExpect(jsonPath("$.data.isbn").exists());
    }

    @Test
    @DisplayName("POST /api/books: should return 409 when ISBN already exists")
    void createBook_duplicateIsbn_returns409() throws Exception {
        BookRequest request = buildBookRequest("Any Book", "Any Author",
                "978-0743273565", 2020, AvailabilityStatus.AVAILABLE);

        when(bookService.createBook(any())).thenThrow(
                new DuplicateIsbnException("A book with ISBN '978-0743273565' already exists."));

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("978-0743273565")));
    }

    // ── GET /api/books ────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/books: should return 200 with paginated book list")
    void getBooks_noFilters_returns200WithList() throws Exception {
        PagedResponse<BookResponse> paged = new PagedResponse<>(
                List.of(sampleResponse), 0, 10, 1, 1, true);

        when(bookService.getBooks(any(), any(), anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(paged);

        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].title").value("The Great Gatsby"));
    }

    // ── GET /api/books/{id} ───────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/books/{id}: should return 200 with book details when ID exists")
    void getBookById_existingId_returns200() throws Exception {
        when(bookService.getBookById(1L)).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("The Great Gatsby"));
    }

    @Test
    @DisplayName("GET /api/books/{id}: should return 404 when ID does not exist")
    void getBookById_nonExistentId_returns404() throws Exception {
        when(bookService.getBookById(99L))
                .thenThrow(new ResourceNotFoundException("Book not found with id: 99"));

        mockMvc.perform(get("/api/books/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("99")));
    }

    @Test
    @DisplayName("GET /api/books/{id}: should return 400 when ID is not a valid number")
    void getBookById_invalidIdFormat_returns400() throws Exception {
        mockMvc.perform(get("/api/books/abc"))
                .andExpect(status().isBadRequest());
    }

    // ── PUT /api/books/{id} ───────────────────────────────────────────────────

    @Test
    @DisplayName("PUT /api/books/{id}: should return 200 with updated book")
    void updateBook_validRequest_returns200() throws Exception {
        BookUpdateRequest request = new BookUpdateRequest();
        request.setAvailabilityStatus(AvailabilityStatus.BORROWED);

        BookResponse updatedResponse = BookResponse.builder()
                .id(1L).title("The Great Gatsby").author("F. Scott Fitzgerald")
                .isbn("978-0743273565").publishedYear(1925)
                .availabilityStatus(AvailabilityStatus.BORROWED)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();

        when(bookService.updateBook(eq(1L), any(BookUpdateRequest.class)))
                .thenReturn(updatedResponse);

        mockMvc.perform(put("/api/books/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.availabilityStatus").value("BORROWED"));
    }

    @Test
    @DisplayName("PUT /api/books/{id}: should return 404 when book does not exist")
    void updateBook_nonExistentId_returns404() throws Exception {
        when(bookService.updateBook(eq(99L), any()))
                .thenThrow(new ResourceNotFoundException("Book not found with id: 99"));

        mockMvc.perform(put("/api/books/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound());
    }

    // ── DELETE /api/books/{id} ────────────────────────────────────────────────

    @Test
    @DisplayName("DELETE /api/books/{id}: should return 200 on successful soft delete")
    void deleteBook_existingId_returns200() throws Exception {
        doNothing().when(bookService).deleteBook(1L);

        mockMvc.perform(delete("/api/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(containsString("deleted")));
    }

    @Test
    @DisplayName("DELETE /api/books/{id}: should return 404 when book does not exist")
    void deleteBook_nonExistentId_returns404() throws Exception {
        doThrow(new ResourceNotFoundException("Book not found with id: 99"))
                .when(bookService).deleteBook(99L);

        mockMvc.perform(delete("/api/books/99"))
                .andExpect(status().isNotFound());
    }

    // ── GET /api/books/search ─────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/books/search: should return 200 with matching results")
    void searchBooks_withQuery_returns200() throws Exception {
        PagedResponse<BookResponse> paged = new PagedResponse<>(
                List.of(sampleResponse), 0, 10, 1, 1, true);

        when(bookService.searchBooks(eq("gatsby"), anyInt(), anyInt())).thenReturn(paged);

        mockMvc.perform(get("/api/books/search").param("q", "gatsby"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(1)));
    }

    @Test
    @DisplayName("GET /api/books/search: should return 400 when query param q is missing")
    void searchBooks_missingQueryParam_returns400() throws Exception {
        mockMvc.perform(get("/api/books/search"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private BookRequest buildBookRequest(String title, String author, String isbn,
                                          int year, AvailabilityStatus status) {
        BookRequest req = new BookRequest();
        req.setTitle(title);
        req.setAuthor(author);
        req.setIsbn(isbn);
        req.setPublishedYear(year);
        req.setAvailabilityStatus(status);
        return req;
    }
}
