package example.toyshop.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import example.toyshop.model.Cart;
import example.toyshop.model.CartStatus;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findBySessionId(String sessionId);

    Optional<Cart> findBySessionIdAndStatus(String sessionId, CartStatus status);

    List<Cart> findAllBySessionIdAndStatus(String sessionId, CartStatus status);
}
