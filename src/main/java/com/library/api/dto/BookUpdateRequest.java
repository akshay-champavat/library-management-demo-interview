package com.library.api.dto;

import com.library.api.entity.AvailabilityStatus;
import com.library.api.validation.ValidPublishedYear;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Payload for updating an existing book. Only provided fields are updated.")
public class BookUpdateRequest {

    @Schema(example = "The Great Gatsby")
    private String title;

    @Schema(example = "F. Scott Fitzgerald")
    private String author;

    @Schema(example = "978-0743273565")
    private String isbn;

    @ValidPublishedYear
    @Schema(example = "1925")
    private Integer publishedYear;

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
