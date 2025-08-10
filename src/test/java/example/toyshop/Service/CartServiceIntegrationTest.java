package example.toyshop.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import example.toyshop.model.Cart;
import example.toyshop.model.CartStatus;
import example.toyshop.model.Product;
import example.toyshop.repository.CartRepository;
import example.toyshop.repository.ProductRepository;
import example.toyshop.service.CartService;
import jakarta.transaction.Transactional;

/**
 * Интеграционные тесты для {@link CartService}.
 * 
 * Тестируют реальные взаимодействия с базой данных через репозитории,
 * а также логику работы корзины (добавление товара и оформление заказа).
 * 
 * @Transactional — для отката изменений в базе после каждого теста.
 */
@SpringBootTest
@Transactional
class CartServiceIntegrationTest {

    @Autowired
    private CartService cartService;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    private String sessionId = "session-1";

    /**
     * Подготавливает тестовые данные перед каждым тестом:
     * очищает таблицы корзин и продуктов,
     * создаёт один продукт с количеством 10 штук.
     */
    @BeforeEach
    void setup() {
        cartRepository.deleteAll();
        productRepository.deleteAll();

        Product product = new Product();
        product.setName("Toy");
        product.setQuantity(10);
        productRepository.save(product);
    }

    /**
     * Тестирует добавление товара в корзину и оформление заказа.
     * 
     * Шаги теста:
     * - Добавляет товар в корзину с помощью cartService.addToCart
     * - Проверяет, что корзина не пуста
     * - Оформляет заказ через cartService.checkout
     * - Проверяет, что статус корзины изменился на COMPLETED
     */
    @Test
    void testAddToCartAndCheckout() {
        Product product = productRepository.findAll().get(0);
        cartService.addToCart(sessionId, product.getId());

        Cart cart = cartService.getActiveCart(sessionId);
        assertFalse(cart.getItems().isEmpty());

        Cart checkedOut = cartService.checkout(sessionId);
        assertEquals(CartStatus.COMPLETED, checkedOut.getStatus());
    }
}
