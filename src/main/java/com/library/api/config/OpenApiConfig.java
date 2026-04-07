package com.library.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures the Swagger UI / OpenAPI documentation.
 *
 * Access the interactive API docs at: http://localhost:8080/swagger-ui.html
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI libraryOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Library Book Inventory API")
                        .description(
                                "RESTful API for managing a library's book inventory. " +
                                "Supports CRUD operations, search, pagination, soft deletes, " +
                                "and asynchronous wishlist notifications when a book is returned."
                        )
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Library API Team")
                                .email("library-api@example.com")));
    }
}
