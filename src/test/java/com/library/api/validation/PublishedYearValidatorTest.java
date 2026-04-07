package com.library.api.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for PublishedYearValidator.
 * Tests the @ValidPublishedYear custom constraint logic directly.
 */
@DisplayName("PublishedYearValidator Tests")
class PublishedYearValidatorTest {

    private PublishedYearValidator validator;

    @BeforeEach
    void setUp() {
        validator = new PublishedYearValidator();
    }

    @Test
    @DisplayName("should return true for null (null is handled by @NotNull separately)")
    void isValid_nullValue_returnsTrue() {
        assertThat(validator.isValid(null, null)).isTrue();
    }

    @Test
    @DisplayName("should return true for current year")
    void isValid_currentYear_returnsTrue() {
        int currentYear = LocalDate.now().getYear();
        assertThat(validator.isValid(currentYear, null)).isTrue();
    }

    @ParameterizedTest(name = "year={0} should be valid")
    @ValueSource(ints = {1000, 1450, 1800, 1925, 1984, 2000, 2020})
    @DisplayName("should return true for valid historical and recent years")
    void isValid_validYears_returnsTrue(int year) {
        assertThat(validator.isValid(year, null)).isTrue();
    }

    @ParameterizedTest(name = "year={0} should be invalid (below 1000)")
    @ValueSource(ints = {0, 100, 500, 999})
    @DisplayName("should return false for years before 1000")
    void isValid_yearBefore1000_returnsFalse(int year) {
        assertThat(validator.isValid(year, null)).isFalse();
    }

    @Test
    @DisplayName("should return false for a year in the future")
    void isValid_futureYear_returnsFalse() {
        int futureYear = LocalDate.now().getYear() + 1;
        assertThat(validator.isValid(futureYear, null)).isFalse();
    }

    @Test
    @DisplayName("boundary: year 1000 should be valid (minimum allowed)")
    void isValid_year1000_returnsTrue() {
        assertThat(validator.isValid(1000, null)).isTrue();
    }

    @Test
    @DisplayName("boundary: year 999 should be invalid (just below minimum)")
    void isValid_year999_returnsFalse() {
        assertThat(validator.isValid(999, null)).isFalse();
    }
}
