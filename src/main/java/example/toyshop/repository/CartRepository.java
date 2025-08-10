package example.toyshop.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import example.toyshop.model.Cart;
import example.toyshop.model.CartStatus;

/**
 * Репозиторий для работы с сущностями {@link Cart}.
 * Расширяет JpaRepository, предоставляя стандартные CRUD операции.
 */
public interface CartRepository extends JpaRepository<Cart, Long> {

    /**
     * Находит корзину по идентификатору сессии.
     *
     * @param sessionId идентификатор сессии гостя
     * @return {@link Optional} с корзиной, если она найдена
     */
    Optional<Cart> findBySessionId(String sessionId);

    /**
     * Находит все корзины с заданным статусом.
     *
     * @param status статус корзины (например, ACTIVE, COMPLETED)
     * @return список корзин с указанным статусом
     */
    List<Cart> findByStatus(CartStatus status);

    /**
     * Находит все корзины по идентификатору сессии и статусу.
     *
     * @param sessionId идентификатор сессии гостя
     * @param status статус корзины
     * @return список корзин с указанными sessionId и статусом
     */
    List<Cart> findBySessionIdAndStatus(String sessionId, CartStatus status);
}
