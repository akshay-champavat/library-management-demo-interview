package com.library.api.dto;

import com.library.api.entity.AvailabilityStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Book details returned from the API")
public class BookResponse {

    @Schema(example = "1")
    private Long id;
    @Schema(example = "The Great Gatsby")
    private String title;
    @Schema(example = "F. Scott Fitzgerald")
    private String author;
    @Schema(example = "978-0743273565")
    private String isbn;
    @Schema(example = "1925")
    private Integer publishedYear;
    @Schema(example = "AVAILABLE")
    private AvailabilityStatus availabilityStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public BookResponse() {}

    public BookResponse(Long id, String title, String author, String isbn,
                        Integer publishedYear, AvailabilityStatus availabilityStatus,
                        LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.publishedYear = publishedYear;
        this.availabilityStatus = availabilityStatus;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getIsbn() { return isbn; }
    public Integer getPublishedYear() { return publishedYear; }
    public AvailabilityStatus getAvailabilityStatus() { return availabilityStatus; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private String title;
        private String author;
        private String isbn;
        private Integer publishedYear;
        private AvailabilityStatus availabilityStatus;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(Long v) { this.id = v; return this; }
        public Builder title(String v) { this.title = v; return this; }
        public Builder author(String v) { this.author = v; return this; }
        public Builder isbn(String v) { this.isbn = v; return this; }
        public Builder publishedYear(Integer v) { this.publishedYear = v; return this; }
        public Builder availabilityStatus(AvailabilityStatus v) { this.availabilityStatus = v; return this; }
        public Builder createdAt(LocalDateTime v) { this.createdAt = v; return this; }
        public Builder updatedAt(LocalDateTime v) { this.updatedAt = v; return this; }

        public BookResponse build() {
            return new BookResponse(id, title, author, isbn, publishedYear,
                    availabilityStatus, createdAt, updatedAt);
        }
    }
}
