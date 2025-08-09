package example.toyshop.service;

import example.toyshop.model.Product;
import example.toyshop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public Page<Product> getProducts(String keyword, String sort, int page, int size) {
        Sort sorting = getSort(sort);
        Pageable pageable = PageRequest.of(page, size, sorting);

        if (keyword != null && !keyword.isEmpty()) {
            return productRepository.findByNameContainingIgnoreCase(keyword, pageable);
        } else {
            return productRepository.findAll(pageable);
        }
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Товар не найден"));
    }

    public void saveProduct(Product product) {
        productRepository.save(product);
    }

    private Sort getSort(String sort) {
        switch (sort) {
            case "price_desc":
                return Sort.by(Sort.Direction.DESC, "price");
            case "name_asc":
                return Sort.by(Sort.Direction.ASC, "name");
            case "name_desc":
                return Sort.by(Sort.Direction.DESC, "name");
            default:
                return Sort.by(Sort.Direction.ASC, "price");
        }
    }
}
