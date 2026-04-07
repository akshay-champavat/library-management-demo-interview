package com.library.api.dto;

import com.library.api.entity.AvailabilityStatus;
import com.library.api.validation.ValidPublishedYear;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Payload for creating a new book")
public class BookRequest {

    @NotBlank(message = "Title is required")
    @Schema(example = "The Great Gatsby")
    private String title;

    @NotBlank(message = "Author is required")
    @Schema(example = "F. Scott Fitzgerald")
    private String author;

    @NotBlank(message = "ISBN is required")
    @Schema(example = "978-0743273565")
    private String isbn;

    @NotNull(message = "Published year is required")
    @ValidPublishedYear
    @Schema(example = "1925")
    private Integer publishedYear;

    @NotNull(message = "Availability status is required")
    @Schema(example = "AVAILABLE")
    private AvailabilityStatus availabilityStatus;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public Integer getPublishedYear() { return publishedYear; }
    public void setPublishedYear(Integer publishedYear) { this.publishedYear = publishedYear; }

    public AvailabilityStatus getAvailabilityStatus() { return availabilityStatus; }
    public void setAvailabilityStatus(AvailabilityStatus availabilityStatus) { this.availabilityStatus = availabilityStatus; }
}
