package example.toyshop.service;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import example.toyshop.model.Cart;
import example.toyshop.model.CartItem;
import example.toyshop.model.CartStatus;
import example.toyshop.model.Product;
import example.toyshop.repository.CartRepository;
import example.toyshop.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

/**
 * Сервис для работы с корзиной покупок.
 */
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    /**
     * Получает активную корзину по идентификатору сессии.
     * Если активная корзина отсутствует, создаёт новую.
     *
     * @param sessionId идентификатор сессии пользователя
     * @return активная корзина
     * @throws IllegalStateException если найдено более одной активной корзины для
     *                               sessionId
     */
    public Cart getActiveCartBySessionId(String sessionId) {
        List<Cart> carts = cartRepository.findBySessionIdAndStatus(sessionId, CartStatus.ACTIVE);
        if (carts.size() > 1) {
            throw new IllegalStateException("Обнаружено несколько активных корзин для sessionId: " + sessionId);
        }
        return carts.stream()
                .findFirst()
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setSessionId(sessionId);
                    newCart.setStatus(CartStatus.ACTIVE);
                    return cartRepository.save(newCart);
                });
    }

    /**
     * Добавляет товар в корзину пользователя.
     * Уменьшает количество товара на складе.
     *
     * @param sessionId идентификатор сессии пользователя
     * @param productId идентификатор добавляемого товара
     * @throws RuntimeException если товар не найден или отсутствует на складе
     */
    @Transactional
    public void addToCart(String sessionId, Long productId) {
        Product product = findAvailableProduct(productId);
        Cart cart = findOrCreateActiveCart(sessionId);
        addOrUpdateCartItem(cart, product);
        decreaseProductStock(product);
    }

    /**
     * Находит товар по ID и проверяет, что он есть в наличии.
     *
     * @param productId ID товара
     * @return найденный товар
     * @throws RuntimeException если товар не найден или его количество равно 0
     */
    private Product findAvailableProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Товар не найден"));
        if (product.getQuantity() <= 0) {
            throw new RuntimeException("Товара нет в наличии");
        }
        return product;
    }

    /**
     * Ищет активную корзину для указанной сессии. Если корзины нет — создаёт новую.
     *
     * @param sessionId идентификатор сессии
     * @return активная корзина
     */
    private Cart findOrCreateActiveCart(String sessionId) {
        return cartRepository.findBySessionIdAndStatus(sessionId, CartStatus.ACTIVE)
                .stream()
                .findFirst()
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setSessionId(sessionId);
                    newCart.setStatus(CartStatus.ACTIVE);
                    return cartRepository.save(newCart);
                });
    }

    /**
     * Добавляет товар в корзину или увеличивает его количество, если он уже есть.
     *
     * @param cart    корзина
     * @param product товар
     */
    private void addOrUpdateCartItem(Cart cart, Product product) {
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst();

        if (existingItem.isPresent()) {
            existingItem.get().setQuantity(existingItem.get().getQuantity() + 1);
        } else {
            CartItem newItem = new CartItem();
            newItem.setProduct(product);
            newItem.setQuantity(1);
            newItem.setCart(cart);
            cart.getItems().add(newItem);
        }

        cartRepository.save(cart);
    }

    /**
     * Уменьшает количество товара на складе на 1 и сохраняет изменения.
     *
     * @param product товар
     */
    private void decreaseProductStock(Product product) {
        product.setQuantity(product.getQuantity() - 1);
        productRepository.save(product);
    }

    /**
     * Удаляет товар из корзины пользователя и возвращает количество товара на
     * склад.
     *
     * @param sessionId идентификатор сессии пользователя
     * @param productId идентификатор удаляемого товара
     * @throws RuntimeException если корзина не найдена
     */
    public void removeFromCart(String sessionId, Long productId) {
        Cart cart = cartRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("Корзина не найдена"));

        Iterator<CartItem> iterator = cart.getItems().iterator();
        while (iterator.hasNext()) {
            CartItem item = iterator.next();
            if (item.getProduct().getId().equals(productId)) {
                Product product = item.getProduct();
                product.setQuantity(product.getQuantity() + item.getQuantity());
                productRepository.save(product);

                iterator.remove(); // удаляем из списка
                break;
            }
        }
        cartRepository.save(cart);
    }

    /**
     * Увеличивает количество единиц товара в корзине на 1, если товар есть на
     * складе.
     * Уменьшает количество товара на складе.
     *
     * @param sessionId идентификатор сессии пользователя
     * @param productId идентификатор товара
     * @throws RuntimeException если корзина не найдена
     */
    public void increaseItem(String sessionId, Long productId) {
        Cart cart = cartRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("Корзина не найдена"));

        cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .ifPresent(item -> {
                    Product product = item.getProduct();
                    if (product.getQuantity() > 0) {
                        item.setQuantity(item.getQuantity() + 1);
                        product.setQuantity(product.getQuantity() - 1);
                        productRepository.save(product);
                        cartRepository.save(cart);
                    }
                });
    }

    /**
     * Уменьшает количество единиц товара в корзине на 1.
     * Если количество становится 0, удаляет товар из корзины.
     * Возвращает товар на склад.
     *
     * @param sessionId идентификатор сессии пользователя
     * @param productId идентификатор товара
     * @throws RuntimeException если корзина не найдена
     */
    public void decreaseItem(String sessionId, Long productId) {
        Cart cart = cartRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("Корзина не найдена"));

        cart.getItems().removeIf(item -> {
            if (item.getProduct().getId().equals(productId)) {
                Product product = item.getProduct();
                product.setQuantity(product.getQuantity() + 1);
                productRepository.save(product);

                if (item.getQuantity() > 1) {
                    item.setQuantity(item.getQuantity() - 1);
                    return false; // не удалять из списка
                } else {
                    return true; // удалить товар из корзины
                }
            }
            return false;
        });
        cartRepository.save(cart);
    }

    /**
     * Получает активную корзину по идентификатору сессии.
     * Если корзина отсутствует, создаёт новую.
     *
     * @param sessionId идентификатор сессии пользователя
     * @return активная корзина
     */
    public Cart getActiveCart(String sessionId) {
        return cartRepository.findBySessionIdAndStatus(sessionId, CartStatus.ACTIVE).stream()
                .findFirst()
                .orElseGet(() -> {
                    Cart cart = new Cart();
                    cart.setSessionId(sessionId);
                    cart.setStatus(CartStatus.ACTIVE);
                    return cartRepository.save(cart);
                });
    }

    /**
     * Оформляет заказ, изменяя статус корзины на COMPLETED.
     *
     * @param sessionId идентификатор сессии пользователя
     * @return оформленная корзина (заказ)
     * @throws IllegalStateException если активная корзина не найдена
     */
    public Cart checkout(String sessionId) {
        Cart cart = cartRepository.findBySessionIdAndStatus(sessionId, CartStatus.ACTIVE)
                .stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("Активная корзина не найдена"));

        cart.setStatus(CartStatus.COMPLETED);
        return cartRepository.save(cart);
    }
}
