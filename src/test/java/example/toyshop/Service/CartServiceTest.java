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

/**
 * Unit-тесты для {@link CartService} с использованием Mockito.
 * 
 * <p>
 * Покрываются основные сценарии работы с корзиной:
 * получение активной корзины, добавление товара,
 * удаление товара и оформление заказа (checkout).
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CartService cartService;

    private final String sessionId = "session-1";

    /**
     * Тестирует получение активной корзины, если она уже существует.
     * 
     * <p>
     * Проверяется, что метод возвращает существующую корзину с статусом ACTIVE
     * для данного sessionId.
     * </p>
     */
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

    /**
     * Тестирует создание новой активной корзины, если ранее такой не было.
     * 
     * <p>
     * Проверяется, что при отсутствии активной корзины с данным sessionId,
     * создаётся новая корзина с правильным статусом и sessionId, и она сохраняется
     * в репозитории.
     * </p>
     */
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

    /**
     * Тестирует добавление продукта в корзину.
     * 
     * <p>
     * Проверяется корректное добавление товара в новую корзину, создание корзины,
     * сохранение корзины и обновление количества товара.
     * </p>
     */
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

    /**
     * Тестирует удаление товара из корзины.
     * 
     * <p>
     * Проверяется, что после удаления корзина не содержит данного товара,
     * а количество товара на складе увеличивается на количество удалённого из
     * корзины.
     * </p>
     */
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

    /**
     * Тестирует успешное оформление заказа (checkout).
     * 
     * <p>
     * Проверяется, что у активной корзины меняется статус на COMPLETED и происходит
     * сохранение.
     * </p>
     */
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

    /**
     * Тестирует ошибку при оформлении заказа, если нет активной корзины.
     * 
     * <p>
     * Проверяется, что при отсутствии активной корзины с данным sessionId
     * выбрасывается {@link IllegalStateException}.
     * </p>
     */
    @Test
    void testCheckout_noActiveCart_throws() {
        when(cartRepository.findBySessionIdAndStatus(sessionId, CartStatus.ACTIVE))
                .thenReturn(Collections.emptyList());

        assertThrows(IllegalStateException.class, () -> cartService.checkout(sessionId));
    }
}
