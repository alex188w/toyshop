package example.toyshop.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import example.toyshop.model.Product;
import example.toyshop.repository.ProductRepository;
import example.toyshop.service.ProductService;

import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void getProducts_withKeyword_callsFindByNameContainingIgnoreCase() {
        String keyword = "test";
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "price"));
        Page<Product> page = new PageImpl<>(List.of(new Product()));
        when(productRepository.findByNameContainingIgnoreCase(eq(keyword), any(Pageable.class))).thenReturn(page);

        Page<Product> result = productService.getProducts(keyword, "price_asc", 0, 10);

        assertEquals(page, result);
        verify(productRepository).findByNameContainingIgnoreCase(eq(keyword), any(Pageable.class));
    }

    @Test
    void getProducts_withoutKeyword_callsFindAll() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "price"));
        Page<Product> page = new PageImpl<>(List.of(new Product()));
        when(productRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<Product> result = productService.getProducts(null, "price_asc", 0, 10);

        assertEquals(page, result);
        verify(productRepository).findAll(any(Pageable.class));
    }

    @Test
    void getProductById_existingId_returnsProduct() {
        Product product = new Product();
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Product result = productService.getProductById(1L);

        assertEquals(product, result);
    }

    @Test
    void getProductById_notFound_throwsException() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> productService.getProductById(1L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void saveProduct_callsRepositorySave() {
        Product product = new Product();

        productService.saveProduct(product);

        verify(productRepository).save(product);
    }
}
