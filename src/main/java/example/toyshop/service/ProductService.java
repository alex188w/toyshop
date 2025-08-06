package example.toyshop.service;

import example.toyshop.model.Product;
import example.toyshop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    public Page<Product> getAll(int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        return productRepository.findAll(pageable);
    }

    public List<Product> searchByName(String keyword) {
        return productRepository.findByNameContainingIgnoreCase(keyword);
    }
}
