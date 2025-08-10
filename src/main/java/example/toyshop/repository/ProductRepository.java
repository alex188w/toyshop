package example.toyshop.repository;

import example.toyshop.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductRepository extends JpaRepository<Product, Long> {    
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
