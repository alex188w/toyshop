package example.toyshop.service;

import example.toyshop.model.Product;
import example.toyshop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Сервис для работы с товарами.
 */
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    /**
     * Возвращает страницу товаров с учетом фильтрации по ключевому слову и
     * сортировки.
     *
     * @param keyword ключевое слово для поиска по названию (может быть null или
     *                пустым)
     * @param sort    параметр сортировки (price_asc, price_desc, name_asc,
     *                name_desc)
     * @param page    номер страницы (0-based)
     * @param size    размер страницы
     * @return страница товаров с учетом фильтра и сортировки
     */
    public Page<Product> getProducts(String keyword, String sort, int page, int size) {
        Sort sorting = getSort(sort);
        Pageable pageable = PageRequest.of(page, size, sorting);

        if (keyword != null && !keyword.isEmpty()) {
            return productRepository.findByNameContainingIgnoreCase(keyword, pageable);
        } else {
            return productRepository.findAll(pageable);
        }
    }

    /**
     * Получает товар по его идентификатору.
     *
     * @param id идентификатор товара
     * @return найденный товар
     * @throws ResponseStatusException если товар не найден (HTTP 404)
     */
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Товар не найден"));
    }

    /**
     * Сохраняет товар (новый или обновлённый).
     *
     * @param product объект товара
     */
    public void saveProduct(Product product) {
        productRepository.save(product);
    }

    /**
     * Парсит строку сортировки в объект Sort.
     *
     * @param sort строка с параметром сортировки
     * @return объект Sort
     */
    private Sort getSort(String sort) {
        switch (sort) {
            case "price_desc":
                return Sort.by(Sort.Direction.DESC, "price");
            case "name_asc":
                return Sort.by(Sort.Direction.ASC, "name");
            case "name_desc":
                return Sort.by(Sort.Direction.DESC, "name");
            default:
                return Sort.by(Sort.Direction.ASC, "price");
        }
    }
}
