package example.toyshop.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.springframework.ui.Model;

import example.toyshop.model.Cart;
import example.toyshop.service.CartService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@ExtendWith(MockitoExtension.class)
class CartControllerTest {

    @Mock
    private CartService cartService;

    @InjectMocks
    private CartController cartController;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpSession session;

    @Mock
    private Model model;

    private final String sessionId = "session-123";

    /**
     * Настраивает поведение моков для HTTP-запроса и сессии,
     * чтобы при вызове request.getSession(true) возвращалась сессия с заданным sessionId.
     */
    @BeforeEach
    void setup() {
        when(request.getSession(true)).thenReturn(session);
        when(session.getId()).thenReturn(sessionId);
    }

    /**
     * Тестирует метод отображения корзины:
     * - Проверяет вызов сервиса получения активной корзины по sessionId.
     * - Проверяет добавление корзины в модель.
     * - Проверяет, что возвращается имя view "cart".
     */
    @Test
    void testViewCart() {
        Cart cart = new Cart();
        when(cartService.getActiveCartBySessionId(sessionId)).thenReturn(cart);

        String view = cartController.viewCart(request, model);

        verify(cartService).getActiveCartBySessionId(sessionId);
        verify(model).addAttribute("cart", cart);
        assertEquals("cart", view);
    }

    /**
     * Тестирует добавление товара в корзину:
     * - Проверяет вызов сервиса добавления товара с sessionId и productId.
     * - Проверяет редирект на страницу товаров "/products".
     */
    @Test
    void testAddToCart() {
        Long productId = 42L;

        String view = cartController.addToCart(productId, request);

        verify(cartService).addToCart(sessionId, productId);
        assertEquals("redirect:/products", view);
    }

    /**
     * Тестирует удаление товара из корзины:
     * - Проверяет вызов сервиса удаления товара по sessionId и productId.
     * - Проверяет редирект на страницу корзины "/cart".
     */
    @Test
    void testRemoveFromCart() {
        Long productId = 42L;

        String view = cartController.removeFromCart(productId, request);

        verify(cartService).removeFromCart(sessionId, productId);
        assertEquals("redirect:/cart", view);
    }

    /**
     * Тестирует увеличение количества товара в корзине:
     * - Проверяет вызов сервиса увеличения количества по sessionId и productId.
     * - Проверяет редирект на страницу корзины "/cart".
     */
    @Test
    void testIncreaseItem() {
        Long productId = 42L;

        String view = cartController.increaseItem(productId, request);

        verify(cartService).increaseItem(sessionId, productId);
        assertEquals("redirect:/cart", view);
    }

    /**
     * Тестирует уменьшение количества товара в корзине:
     * - Проверяет вызов сервиса уменьшения количества по sessionId и productId.
     * - Проверяет редирект на страницу корзины "/cart".
     */
    @Test
    void testDecreaseItem() {
        Long productId = 42L;

        String view = cartController.decreaseItem(productId, request);

        verify(cartService).decreaseItem(sessionId, productId);
        assertEquals("redirect:/cart", view);
    }
}
