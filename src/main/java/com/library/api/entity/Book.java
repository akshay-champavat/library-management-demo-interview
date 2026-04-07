package com.library.api.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "books")
@SQLRestriction("deleted = false")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String author;

    @Column(unique = true, nullable = false)
    private String isbn;

    @Column(nullable = false)
    private Integer publishedYear;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AvailabilityStatus availabilityStatus;

    @Column(nullable = false)
    private boolean deleted = false;

    private LocalDateTime deletedAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public Book() {}

    // Getters
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getIsbn() { return isbn; }
    public Integer getPublishedYear() { return publishedYear; }
    public AvailabilityStatus getAvailabilityStatus() { return availabilityStatus; }
    public boolean isDeleted() { return deleted; }
    public LocalDateTime getDeletedAt() { return deletedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setAuthor(String author) { this.author = author; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public void setPublishedYear(Integer publishedYear) { this.publishedYear = publishedYear; }
    public void setAvailabilityStatus(AvailabilityStatus s) { this.availabilityStatus = s; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }

    // Builder
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String title;
        private String author;
        private String isbn;
        private Integer publishedYear;
        private AvailabilityStatus availabilityStatus;

        public Builder title(String v) { this.title = v; return this; }
        public Builder author(String v) { this.author = v; return this; }
        public Builder isbn(String v) { this.isbn = v; return this; }
        public Builder publishedYear(Integer v) { this.publishedYear = v; return this; }
        public Builder availabilityStatus(AvailabilityStatus v) { this.availabilityStatus = v; return this; }

        public Book build() {
            Book b = new Book();
            b.title = this.title;
            b.author = this.author;
            b.isbn = this.isbn;
            b.publishedYear = this.publishedYear;
            b.availabilityStatus = this.availabilityStatus;
            b.deleted = false;
            return b;
        }
    }
}
