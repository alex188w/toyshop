package example.toyshop.controller;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import example.toyshop.model.Cart;
import example.toyshop.model.CartStatus;
import example.toyshop.repository.CartRepository;
import org.springframework.transaction.annotation.Transactional;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CartRepository cartRepository;

    private Cart completedOrder;

    /**
     * Подготавливает тестовые данные:
     * - Очищает репозиторий корзин.
     * - Создаёт и сохраняет заказ со статусом COMPLETED и фиксированным sessionId.
     */
    @BeforeEach
    @Transactional
    void setUp() {
        cartRepository.deleteAll();

        completedOrder = new Cart();
        completedOrder.setStatus(CartStatus.COMPLETED);
        completedOrder.setSessionId("session-123");
        completedOrder.setItems(List.of()); // при необходимости можно добавить товары
        cartRepository.save(completedOrder);
    }

    /**
     * Тестирует отображение списка заказов:
     * - Проверяет успешный ответ (200 OK).
     * - Проверяет, что используется view "orders".
     * - Проверяет, что модель содержит список заказов, включая созданный заказ.
     */
    @Test
    void testViewOrders() throws Exception {
        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(view().name("orders"))
                .andExpect(model().attribute("orders", hasItem(
                        hasProperty("id", equalTo(completedOrder.getId())))));
    }

    /**
     * Тестирует отображение конкретного заказа по id:
     * - Проверяет успешный ответ (200 OK).
     * - Проверяет, что используется view "order".
     * - Проверяет, что в модели содержится заказ с ожидаемым id.
     */
    @Test
    void testViewOrderById() throws Exception {
        mockMvc.perform(get("/orders/{id}", completedOrder.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("order"))
                .andExpect(model().attribute("order",
                        hasProperty("id", equalTo(completedOrder.getId()))));
    }

    /**
     * Тестирует запрос несуществующего заказа:
     * - Проверяет, что сервер возвращает статус 404 Not Found.
     */
    @Test
    void testViewOrderNotFound() throws Exception {
        Long nonExistentId = 999999L;

        mockMvc.perform(get("/orders/{id}", nonExistentId))
                .andExpect(status().isNotFound());
    }
}
