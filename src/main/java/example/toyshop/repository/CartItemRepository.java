package example.toyshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import example.toyshop.model.CartItem;

/**
 * Репозиторий для работы с сущностями {@link CartItem}.
 * Предоставляет стандартные CRUD операции благодаря расширению JpaRepository.
 */
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
}
