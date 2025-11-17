package org.example.learningplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Main application class for the Learning Platform.
 * This educational platform demonstrates ORM concepts using Hibernate/JPA and PostgreSQL.
 */
@SpringBootApplication
@ComponentScan(basePackages = {
    "org.example.learningplatform",
    "service",
    "controller",
    "config",
    "exception.handler"
})
@EntityScan(basePackages = "entity")
@EnableJpaRepositories(basePackages = "repository")
public class LearningPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(LearningPlatformApplication.class, args);
    }
}
