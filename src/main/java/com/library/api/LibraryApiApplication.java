package com.library.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Entry point for the Library API application.
 *
 * @EnableAsync activates Spring's asynchronous method execution,
 * which powers the decoupled wishlist notification task.
 */
@SpringBootApplication
@EnableAsync
public class LibraryApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(LibraryApiApplication.class, args);
    }
}
