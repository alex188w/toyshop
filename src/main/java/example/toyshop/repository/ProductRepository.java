package example.toyshop.repository;

import example.toyshop.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    // List<Product> findByNameContainingIgnoreCase(String keyword);
    
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
