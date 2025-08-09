package example.toyshop.service;

import example.toyshop.model.Product;
import example.toyshop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    // public Page<Product> getAll(int page, int size, String sortBy) {
    //     Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
    //     return productRepository.findAll(pageable);
    // }

    // // public List<Product> searchByName(String keyword) {
    // //     return productRepository.findByNameContainingIgnoreCase(keyword);
    // // }

    // public Page<Product> searchByName(String keyword, int page, int size, String sortBy) {
    //     Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
    //     return productRepository.findByNameContainingIgnoreCase(keyword, pageable);
    // }

    // public Page<Product> getProducts(String keyword, int page, int size, String sortDirection) {
    //     Sort sort = Sort.by("price");
    //     if ("desc".equalsIgnoreCase(sortDirection)) {
    //         sort = sort.descending();
    //     } else {
    //         sort = sort.ascending();
    //     }

    //     Pageable pageable = PageRequest.of(page, size, sort);

    //     if (keyword != null && !keyword.isEmpty()) {
    //         return productRepository.findByNameContainingIgnoreCase(keyword, pageable);
    //     } else {
    //         return productRepository.findAll(pageable);
    //     }
    // }
}
