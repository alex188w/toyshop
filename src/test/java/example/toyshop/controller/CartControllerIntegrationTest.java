package example.toyshop.controller;

import example.toyshop.model.Product;
import example.toyshop.repository.CartItemRepository;
import example.toyshop.repository.CartRepository;
import example.toyshop.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class CartControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    private Long productId;    

    private MockHttpSession session;

    /**
     * Подготавливает тестовые данные перед каждым тестом:
     * - Очищает таблицы корзин, элементов корзины и товаров.
     * - Создаёт новый товар с запасом 10 штук.
     * - Инициализирует объект сессии.
     */
    @BeforeEach
    void setUp() {
        cartItemRepository.deleteAll();
        cartRepository.deleteAll();
        productRepository.deleteAll();

        Product product = new Product();
        product.setName("Toy Car");
        product.setDescription("Red toy car");
        product.setPrice(new BigDecimal("99.99"));
        product.setQuantity(10);
        productRepository.save(product);

        productId = product.getId();
        session = new MockHttpSession();
    }

    /**
     * Проверяет добавление товара в корзину и последующий просмотр корзины.
     * Убеждается, что после добавления товара в корзину по сессии
     * в модели появляется корзина с одним товаром.
     * 
     * @throws Exception при ошибках MockMvc
     */
    @Test
    void testAddToCartAndView() throws Exception {
        // Добавляем товар с productId в корзину с заданной сессией
        mockMvc.perform(post("/cart/add/{id}", productId).session(session))
                .andExpect(status().is3xxRedirection());

        // Получаем корзину по той же сессии и проверяем, что в корзине 1 элемент
        mockMvc.perform(get("/cart").session(session))
                .andExpect(status().isOk())
                .andExpect(model().attribute("cart", hasProperty("items", hasSize(1))));
    }

    /**
     * Проверяет отображение пустой корзины при отсутствии товаров.
     * Убеждается, что в модели есть атрибут "cart" и возвращается правильный вид.
     * 
     * @throws Exception при ошибках MockMvc
     */
    @Test
    void testViewEmptyCart() throws Exception {
        mockMvc.perform(get("/cart"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"))
                .andExpect(model().attributeExists("cart"));
    }

    /**
     * Проверяет корректность увеличения и уменьшения количества товара в корзине.
     * Сценарий:
     * - Добавляем товар.
     * - Увеличиваем количество товара в корзине.
     * - Уменьшаем количество товара.
     * Убеждается, что редиректы происходят на страницу корзины.
     * 
     * @throws Exception при ошибках MockMvc
     */
    @Test
    void testIncreaseAndDecreaseItem() throws Exception {
        MockHttpSession session = new MockHttpSession();

        // Добавляем товар с сессией
        mockMvc.perform(post("/cart/add/{id}", productId).session(session))
                .andExpect(status().is3xxRedirection());

        // Увеличиваем количество товара
        mockMvc.perform(post("/cart/increase/{id}", productId).session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));

        // Уменьшаем количество товара
        mockMvc.perform(post("/cart/decrease/{id}", productId).session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));
    }

    /**
     * Проверяет удаление товара из корзины.
     * Сценарий:
     * - Добавляем товар.
     * - Удаляем товар.
     * - Проверяем, что корзина пустая.
     * 
     * @throws Exception при ошибках MockMvc
     */
    @Test
    void testRemoveFromCart() throws Exception {
        MockHttpSession session = new MockHttpSession();

        // Добавляем товар
        mockMvc.perform(post("/cart/add/{id}", productId).session(session))
                .andExpect(status().is3xxRedirection());

        // Удаляем товар из корзины
        mockMvc.perform(post("/cart/remove/{id}", productId).session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));

        // Проверяем, что корзина пуста
        mockMvc.perform(get("/cart").session(session))
                .andExpect(status().isOk())
                .andExpect(model().attribute("cart", hasProperty("items", hasSize(0))));
    }

    /**
     * Проверяет процесс оформления заказа.
     * Сценарий:
     * - Добавляем товар в корзину.
     * - Выполняем оформление заказа.
     * - Проверяем редирект на страницу заказов с динамическим ID.
     * 
     * @throws Exception при ошибках MockMvc
     */
    @Test
    void testCheckout() throws Exception {
        MockHttpSession session = new MockHttpSession();

        // Добавляем товар
        mockMvc.perform(post("/cart/add/{id}", productId).session(session))
                .andExpect(status().is3xxRedirection());

        // Выполняем checkout
        mockMvc.perform(post("/cart/checkout").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/orders/*"));
    }
}
