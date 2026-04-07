package com.library.api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

/**
 * The actual logic behind @ValidPublishedYear.
 *
 * Validates that the given year is:
 *   - At least 1000 (first realistic printed books)
 *   - Not in the future (can't publish a book that hasn't been written yet)
 *
 * Null values are allowed here — use @NotNull separately if the field is required.
 */
public class PublishedYearValidator implements ConstraintValidator<ValidPublishedYear, Integer> {

    @Override
    public boolean isValid(Integer year, ConstraintValidatorContext context) {
        if (year == null) {
            return true; // @NotNull handles the null check; this validator only checks range
        }
        int currentYear = LocalDate.now().getYear();
        return year >= 1000 && year <= currentYear;
    }
}
