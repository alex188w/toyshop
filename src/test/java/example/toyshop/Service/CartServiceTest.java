package example.toyshop.Service;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import example.toyshop.model.Cart;
import example.toyshop.model.CartItem;
import example.toyshop.model.CartStatus;
import example.toyshop.model.Product;
import example.toyshop.repository.CartRepository;
import example.toyshop.repository.ProductRepository;
import example.toyshop.service.CartService;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CartService cartService;

    private final String sessionId = "session-1";

    @Test
    void testGetActiveCart_existing() {
        Cart cart = new Cart();
        cart.setSessionId(sessionId);
        cart.setStatus(CartStatus.ACTIVE);

        when(cartRepository.findBySessionIdAndStatus(sessionId, CartStatus.ACTIVE))
                .thenReturn(List.of(cart));

        Cart result = cartService.getActiveCart(sessionId);

        assertSame(cart, result);
    }

    @Test
    void testGetActiveCart_newCartCreated() {
        when(cartRepository.findBySessionIdAndStatus(sessionId, CartStatus.ACTIVE))
                .thenReturn(Collections.emptyList());

        Cart savedCart = new Cart();
        savedCart.setSessionId(sessionId);
        savedCart.setStatus(CartStatus.ACTIVE);

        when(cartRepository.save(any())).thenReturn(savedCart);

        Cart result = cartService.getActiveCart(sessionId);

        assertEquals(sessionId, result.getSessionId());
        assertEquals(CartStatus.ACTIVE, result.getStatus());
        verify(cartRepository).save(any());
    }

    @Test
    void testAddToCart_existingProductAndCart() {
        String sessionId = "session-1";

        Product product = new Product();
        product.setId(1L);
        product.setQuantity(10);

        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setQuantity(2);

        Cart cart = new Cart();
        cart.setSessionId(sessionId);
        cart.setItems(new ArrayList<>(List.of(cartItem)));

        when(cartRepository.findBySessionIdAndStatus(sessionId, CartStatus.ACTIVE))
                .thenReturn(Collections.emptyList()); // чтобы проверить создание нового cart

        when(cartRepository.save(any(Cart.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // вызов метода
        cartService.addToCart(sessionId, 1L);

        // проверки
        verify(cartRepository, times(2)).save(any(Cart.class));
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void testRemoveFromCart() {
        Product product = new Product();
        product.setId(1L);
        product.setQuantity(5);

        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setQuantity(3);

        Cart cart = new Cart();
        cart.setItems(new ArrayList<>(List.of(cartItem)));

        when(cartRepository.findBySessionId(sessionId)).thenReturn(Optional.of(cart));
        when(productRepository.save(product)).thenReturn(product);
        when(cartRepository.save(cart)).thenReturn(cart);

        cartService.removeFromCart(sessionId, 1L);

        assertTrue(cart.getItems().isEmpty());
        assertEquals(8, product.getQuantity());
        verify(cartRepository).save(cart);
        verify(productRepository).save(product);
    }

    @Test
    void testCheckout_success() {
        Cart cart = new Cart();
        cart.setStatus(CartStatus.ACTIVE);

        when(cartRepository.findBySessionIdAndStatus(sessionId, CartStatus.ACTIVE))
                .thenReturn(List.of(cart));
        when(cartRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Cart completed = cartService.checkout(sessionId);

        assertEquals(CartStatus.COMPLETED, completed.getStatus());
        verify(cartRepository).save(cart);
    }

    @Test
    void testCheckout_noActiveCart_throws() {
        when(cartRepository.findBySessionIdAndStatus(sessionId, CartStatus.ACTIVE))
                .thenReturn(Collections.emptyList());

        assertThrows(IllegalStateException.class, () -> cartService.checkout(sessionId));
    }
}
