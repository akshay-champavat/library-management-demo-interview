package com.library.api.controller;

import com.library.api.dto.*;
import com.library.api.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/books")
@Tag(name = "Books", description = "Library book inventory management")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @Operation(summary = "Add a new book", description = "Creates a new book. Returns 409 if ISBN already exists.")
    @PostMapping
    public ResponseEntity<ApiResponse<BookResponse>> createBook(@Valid @RequestBody BookRequest request) {
        BookResponse created = bookService.createBook(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Book created successfully.", created));
    }

    @Operation(summary = "Get all books (paginated)", description = "Returns a paginated list. Filter by author or publishedYear.")
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<BookResponse>>> getBooks(
            @Parameter(description = "Partial author name filter") @RequestParam(required = false) String author,
            @Parameter(description = "Exact year filter") @RequestParam(required = false) Integer publishedYear,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        PagedResponse<BookResponse> result = bookService.getBooks(author, publishedYear, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success("Books retrieved successfully.", result));
    }

    @Operation(summary = "Get a book by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookResponse>> getBookById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Book retrieved successfully.", bookService.getBookById(id)));
    }

    @Operation(summary = "Update a book", description = "Partial update. BORROWED→AVAILABLE triggers async wishlist notifications.")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BookResponse>> updateBook(
            @PathVariable Long id, @Valid @RequestBody BookUpdateRequest request) {
        BookResponse updated = bookService.updateBook(id, request);
        return ResponseEntity.ok(ApiResponse.success(
                "Book updated successfully. Wishlist notifications (if any) are processing in the background.", updated));
    }

    @Operation(summary = "Delete a book (soft delete)", description = "Marks book as deleted. Invisible to API but stays in DB for audit.")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.ok(ApiResponse.success("Book deleted successfully."));
    }

    @Operation(summary = "Search books by title or author", description = "Case-insensitive partial match on title and author.")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PagedResponse<BookResponse>>> searchBooks(
            @Parameter(description = "Search term", required = true) @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PagedResponse<BookResponse> results = bookService.searchBooks(q, page, size);
        return ResponseEntity.ok(ApiResponse.success("Search completed. " + results.getTotalElements() + " result(s) found.", results));
    }
}
