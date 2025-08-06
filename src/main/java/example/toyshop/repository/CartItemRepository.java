package example.toyshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import example.toyshop.model.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
}
