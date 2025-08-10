package example.toyshop.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.mock;
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

    @BeforeEach
    void setup() {
        when(request.getSession(true)).thenReturn(session);
        when(session.getId()).thenReturn(sessionId);
    }

    @Test
    void testViewCart() {
        Cart cart = new Cart();
        when(cartService.getActiveCartBySessionId(sessionId)).thenReturn(cart);

        String view = cartController.viewCart(request, model);

        verify(cartService).getActiveCartBySessionId(sessionId);
        verify(model).addAttribute("cart", cart);
        assertEquals("cart", view);
    }

    @Test
    void testAddToCart() {
        Long productId = 42L;

        String view = cartController.addToCart(productId, request);

        verify(cartService).addToCart(sessionId, productId);
        assertEquals("redirect:/products", view);
    }

    @Test
    void testRemoveFromCart() {
        Long productId = 42L;

        String view = cartController.removeFromCart(productId, request);

        verify(cartService).removeFromCart(sessionId, productId);
        assertEquals("redirect:/cart", view);
    }

    @Test
    void testIncreaseItem() {
        Long productId = 42L;

        String view = cartController.increaseItem(productId, request);

        verify(cartService).increaseItem(sessionId, productId);
        assertEquals("redirect:/cart", view);
    }

    @Test
    void testDecreaseItem() {
        Long productId = 42L;

        String view = cartController.decreaseItem(productId, request);

        verify(cartService).decreaseItem(sessionId, productId);
        assertEquals("redirect:/cart", view);
    }

    // @Test
    // void testCheckout() {
    //     String sessionId = "test-session-id";

    //     HttpServletRequest request = mock(HttpServletRequest.class);
    //     HttpSession session = mock(HttpSession.class);

    //     // Мокаем оба варианта getSession
    //     when(request.getSession(true)).thenReturn(session);
    //     when(request.getSession()).thenReturn(session);

    //     when(session.getId()).thenReturn(sessionId);

    //     Cart completedCart = new Cart();
    //     completedCart.setId(1L);

    //     when(cartService.checkout(sessionId)).thenReturn(completedCart);

    //     CartController controller = new CartController(cartService);

    //     String result = controller.checkout(request);

    //     verify(cartService).checkout(sessionId);
    //     verify(session).invalidate();

    //     assertEquals("redirect:/orders/1", result);
    // }
}
