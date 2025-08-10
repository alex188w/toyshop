package example.toyshop.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import example.toyshop.model.Product;
import example.toyshop.service.ImageService;
import example.toyshop.service.ProductService;
import jakarta.transaction.Transactional;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

@AutoConfigureMockMvc
@SpringBootTest
@Transactional
public class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // Внедряем сервис для сохранения продукта
    @Autowired
    private ProductService productService;

    @MockitoBean
    private ImageService imageService;

    @Test
    void testListProducts() throws Exception {
        // Добавим в базу несколько продуктов через сервис
        // Сервис и репозиторий используются из контекста Spring (реальная база)
        // Поэтому просто вызываем контроллер напрямую через MockMvc

        mockMvc.perform(MockMvcRequestBuilders.get("/products")
                .param("page", "0")
                .param("size", "10")
                .param("sort", "price_asc"))
                .andExpect(status().isOk())
                .andExpect(view().name("products"))
                .andExpect(model().attributeExists("products"))
                .andExpect(model().attributeExists("currentPage"))
                .andExpect(model().attributeExists("totalPages"));
    }

    @Test
    void testViewProduct() throws Exception {
        // Сохраним продукт в базу
        Product product = new Product();
        product.setName("Test Product");
        product.setPrice(BigDecimal.valueOf(123.45));

        // Сохраним через сервис (через репозиторий) - внедрить ProductService
        // Добавим поле в тесте
        productService.saveProduct(product);

        mockMvc.perform(MockMvcRequestBuilders.get("/products/" + product.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("product"))
                .andExpect(model().attributeExists("product"))
                .andExpect(model().attribute("product", product));
    }

    @Test
    void testShowAddProductForm() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/products/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("add-product"))
                .andExpect(model().attributeExists("product"));
    }

    @Test
    void testAddProduct() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/products/add")
                .param("name", "New Product")
                .param("price", "99.99"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/products"));
    }

    @Test
    void testUploadImage() throws Exception {
        when(imageService.uploadImage(any())).thenReturn("http://example.com/image.png");

        mockMvc.perform(MockMvcRequestBuilders.multipart("/products/uploadImage")
                .file("file", "dummy image content".getBytes()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.url").value("http://example.com/image.png"));
    }
}
