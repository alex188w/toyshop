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

    @Test
    void testAddToCartAndView() throws Exception {
        // Добавляем товар с productId в корзину с заданной сессией
        mockMvc.perform(post("/cart/add/{id}", productId).session(session))
                .andExpect(status().is3xxRedirection());

        // Получаем корзину по той же сессии
        mockMvc.perform(get("/cart").session(session))
                .andExpect(status().isOk())
                .andExpect(model().attribute("cart", hasProperty("items", hasSize(1))));
    }

    @Test
    void testViewEmptyCart() throws Exception {
        mockMvc.perform(get("/cart"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"))
                .andExpect(model().attributeExists("cart"));
    }

    @Test
    void testIncreaseAndDecreaseItem() throws Exception {
        MockHttpSession session = new MockHttpSession();

        // Добавляем товар с сессией
        mockMvc.perform(post("/cart/add/{id}", productId).session(session))
                .andExpect(status().is3xxRedirection());

        // Увеличиваем
        mockMvc.perform(post("/cart/increase/{id}", productId).session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));

        // Уменьшаем
        mockMvc.perform(post("/cart/decrease/{id}", productId).session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));
    }

    @Test
    void testRemoveFromCart() throws Exception {
        MockHttpSession session = new MockHttpSession();

        // Добавляем товар с сессией
        mockMvc.perform(post("/cart/add/{id}", productId).session(session))
                .andExpect(status().is3xxRedirection());

        // Удаляем товар с сессией
        mockMvc.perform(post("/cart/remove/{id}", productId).session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));

        // Проверяем, что корзина пуста с сессией
        mockMvc.perform(get("/cart").session(session))
                .andExpect(status().isOk())
                .andExpect(model().attribute("cart", hasProperty("items", hasSize(0))));
    }

    @Test
    void testCheckout() throws Exception {
        MockHttpSession session = new MockHttpSession();

        // Добавляем товар с сессией
        mockMvc.perform(post("/cart/add/{id}", productId).session(session))
                .andExpect(status().is3xxRedirection());

        // Оформляем заказ с сессией
        mockMvc.perform(post("/cart/checkout").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/orders/*"));
    }
}
