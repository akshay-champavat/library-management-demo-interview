package com.library.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Custom validation annotation that ensures a publishedYear value
 * is between 1000 and the current calendar year (inclusive).
 *
 * Usage: @ValidPublishedYear on any Integer field in a DTO.
 */
@Documented
@Constraint(validatedBy = PublishedYearValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPublishedYear {

    String message() default "Published year must be between 1000 and the current year";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
