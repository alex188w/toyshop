package example.toyshop.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import example.toyshop.model.Cart;
import example.toyshop.model.CartStatus;
import example.toyshop.repository.CartRepository;

import java.util.List;
import java.util.Optional;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CartRepository cartRepository;

    /**
     * Тестирует отображение страницы списка заказов:
     * - Мокаем метод findByStatus, чтобы вернуть два фейковых заказа.
     * - Проверяем статус 200 OK.
     * - Проверяем использование view "orders".
     * - Проверяем, что в модель передан атрибут "orders".
     */
    @Test
    void testViewOrders() throws Exception {
        List<Cart> fakeOrders = List.of(new Cart(), new Cart());
        Mockito.when(cartRepository.findByStatus(CartStatus.COMPLETED)).thenReturn(fakeOrders);

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(view().name("orders"))
                .andExpect(model().attributeExists("orders"));
    }

    /**
     * Тестирует отображение конкретного заказа по id, если заказ найден:
     * - Мокаем метод findById для возвращения заказа.
     * - Проверяем статус 200 OK.
     * - Проверяем использование view "order".
     * - Проверяем, что в модель передан атрибут "order".
     */
    @Test
    void testViewOrder_Found() throws Exception {
        Cart order = new Cart();
        Mockito.when(cartRepository.findById(1L)).thenReturn(Optional.of(order));

        mockMvc.perform(get("/orders/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("order"))
                .andExpect(model().attributeExists("order"));
    }

    /**
     * Тестирует ситуацию, когда заказ с заданным id не найден:
     * - Мокаем метод findById для возвращения пустого Optional.
     * - Проверяем, что сервер возвращает статус 404 Not Found.
     */
    @Test
    void testViewOrder_NotFound() throws Exception {
        Mockito.when(cartRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/orders/999"))
                .andExpect(status().isNotFound());
    }
}
