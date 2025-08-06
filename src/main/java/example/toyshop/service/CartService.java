package example.toyshop.service;

import java.util.Iterator;
import java.util.Optional;

import org.springframework.stereotype.Service;

import example.toyshop.model.Cart;
import example.toyshop.model.CartItem;
import example.toyshop.model.CartStatus;
import example.toyshop.model.Product;
import example.toyshop.repository.CartRepository;
import example.toyshop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    public Cart getCartBySessionId(String sessionId) {
        return cartRepository.findBySessionId(sessionId)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setSessionId(sessionId);
                    return cartRepository.save(newCart);
                });
    }

    public void addToCart(String sessionId, Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Товар не найден"));

        if (product.getQuantity() <= 0) {
            throw new RuntimeException("Товара нет в наличии");
        }

        Cart cart = cartRepository.findBySessionId(sessionId)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setSessionId(sessionId);
                    return cartRepository.save(newCart);
                });

        // Проверим, есть ли уже такой товар
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
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

        product.setQuantity(product.getQuantity() - 1);
        productRepository.save(product);
        cartRepository.save(cart);
    }

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

    public Cart getActiveCart(String sessionId) {
        return cartRepository.findBySessionIdAndStatus(sessionId, CartStatus.ACTIVE)
                .orElseGet(() -> {
                    Cart cart = new Cart();
                    cart.setSessionId(sessionId);
                    cart.setStatus(CartStatus.ACTIVE);
                    return cartRepository.save(cart);
                });
    }

    public void checkout(String sessionId) {
        Cart cart = cartRepository.findBySessionIdAndStatus(sessionId, CartStatus.ACTIVE)
                .orElseThrow(() -> new RuntimeException("Активная корзина не найдена"));

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Корзина пуста");
        }

        cart.setStatus(CartStatus.COMPLETED);
        cartRepository.save(cart);
    }

}
