package example.toyshop.config;


import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

/**
 * Класс для управления тестовым контейнером PostgreSQL с использованием Testcontainers.
 * Контейнер запускается один раз на весь набор тестов и обеспечивает изолированную
 * среду базы данных для интеграционных тестов.
 */
public final class PostgresTestcontainer {

    /**
     * Тестовый контейнер PostgreSQL.
     * <p>
     * Используется образ postgres:15-alpine.
     * Конфигурируется с базой данных "testdb", пользователем "test" и паролем "test".
     * Аннотация {@code @ServiceConnection} позволяет Spring Boot автоматически
     * подключаться к этому контейнеру при тестировании.
     */
    @Container
    @ServiceConnection
    @SuppressWarnings("resource")
    static final PostgreSQLContainer<?> postgresContainer =
        new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");
}
