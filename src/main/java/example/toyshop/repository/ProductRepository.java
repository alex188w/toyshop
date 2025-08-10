package example.toyshop.repository;

import example.toyshop.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Репозиторий для работы с сущностями {@link Product}.
 * Расширяет JpaRepository для стандартных CRUD операций.
 */
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Ищет продукты, название которых содержит заданную подстроку, без учёта регистра,
     * с постраничным выводом результатов.
     *
     * @param name подстрока для поиска в названии продукта
     * @param pageable параметры пагинации и сортировки
     * @return страница продуктов, удовлетворяющих условию поиска
     */
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
