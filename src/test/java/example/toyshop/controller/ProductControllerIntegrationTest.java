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

/**
 * Интеграционные тесты для контроллера продуктов.
 * 
 * Тесты выполняются с использованием Spring Boot Test, MockMvc и реальной базы
 * данных (в тестовом режиме).
 * Транзакции откатываются после каждого теста (@Transactional).
 */
@AutoConfigureMockMvc
@SpringBootTest
@Transactional
public class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * Сервис для сохранения продуктов в базу данных.
     */
    @Autowired
    private ProductService productService;

    /**
     * Мок-сервис для загрузки изображений, внедряется в контекст.
     */
    @MockitoBean
    private ImageService imageService;

    /**
     * Тестирует отображение страницы списка продуктов с параметрами пагинации и
     * сортировки.
     * Проверяет:
     * - HTTP статус 200 OK,
     * - отображение view с именем "products",
     * - наличие в модели атрибутов: products, currentPage, totalPages.
     */
    @Test
    void testListProducts() throws Exception {
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

    /**
     * Тестирует отображение страницы продукта по ID.
     * Сохраняет продукт через сервис и проверяет:
     * - HTTP статус 200 OK,
     * - отображение view с именем "product",
     * - наличие в модели атрибута "product" с ожидаемым объектом.
     */
    @Test
    void testViewProduct() throws Exception {
        Product product = new Product();
        product.setName("Test Product");
        product.setPrice(BigDecimal.valueOf(123.45));
        productService.saveProduct(product);

        mockMvc.perform(MockMvcRequestBuilders.get("/products/" + product.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("product"))
                .andExpect(model().attributeExists("product"))
                .andExpect(model().attribute("product", product));
    }

    /**
     * Тестирует отображение формы добавления нового продукта.
     * Проверяет:
     * - HTTP статус 200 OK,
     * - отображение view с именем "add-product",
     * - наличие в модели атрибута "product".
     */
    @Test
    void testShowAddProductForm() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/products/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("add-product"))
                .andExpect(model().attributeExists("product"));
    }

    /**
     * Тестирует добавление нового продукта через POST-запрос.
     * Отправляет параметры формы name и price.
     * Проверяет редирект на страницу списка продуктов.
     */
    @Test
    void testAddProduct() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/products/add")
                .param("name", "New Product")
                .param("price", "99.99"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/products"));
    }

    /**
     * Тестирует загрузку изображения для продукта.
     * Используется мок-объект imageService для возврата фиксированного URL.
     * Проверяет:
     * - HTTP статус 200 OK,
     * - тип содержимого application/json,
     * - в теле JSON есть поле "url" с ожидаемым значением.
     */
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
