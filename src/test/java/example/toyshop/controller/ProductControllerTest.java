package example.toyshop.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import example.toyshop.model.Product;
import example.toyshop.service.ImageService;
import example.toyshop.service.ProductService;

/**
 * Unit-тесты для {@link ProductController} с использованием @WebMvcTest.
 * 
 * Использует MockMvc для тестирования HTTP-запросов к контроллеру,
 * а также моки сервисов {@link ProductService} и {@link ImageService}.
 */
@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private ImageService imageService;

    /**
     * Тестирует получение страницы списка продуктов с параметрами фильтрации и
     * пагинации.
     * Проверяет:
     * - HTTP статус 200 OK,
     * - отображение view с именем "products",
     * - наличие в модели атрибутов: products, currentPage, totalPages, keyword,
     * sort, size.
     */
    @Test
    void testListProducts() throws Exception {
        Product p = new Product(1L, "Toy", "Nice toy", new BigDecimal("10.0"), null, 5);
        Page<Product> page = new PageImpl<>(List.of(p), PageRequest.of(0, 10), 1);

        when(productService.getProducts(anyString(), anyString(), anyInt(), anyInt())).thenReturn(page);

        mockMvc.perform(get("/products")
                .param("keyword", "toy")
                .param("sort", "price_asc")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(view().name("products"))
                .andExpect(model().attributeExists("products"))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("totalPages", 1))
                .andExpect(model().attribute("keyword", "toy"))
                .andExpect(model().attribute("sort", "price_asc"))
                .andExpect(model().attribute("size", 10));
    }

    /**
     * Тестирует отображение страницы конкретного продукта по ID.
     * Проверяет:
     * - HTTP статус 200 OK,
     * - отображение view с именем "product",
     * - наличие в модели атрибута "product".
     */
    @Test
    void testViewProduct() throws Exception {
        Product p = new Product(1L, "Toy", "Nice toy", new BigDecimal("10.0"), null, 5);

        when(productService.getProductById(1L)).thenReturn(p);

        mockMvc.perform(get("/products/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("product"))
                .andExpect(model().attributeExists("product"));
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
        mockMvc.perform(get("/products/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("add-product"))
                .andExpect(model().attributeExists("product"));
    }

    /**
     * Тестирует добавление нового продукта через POST-запрос с параметрами формы.
     * Проверяет:
     * - редирект на страницу списка продуктов,
     * - вызов метода сохранения продукта в сервисе.
     */
    @Test
    void testAddProduct() throws Exception {
        mockMvc.perform(post("/products/add")
                .param("name", "New Toy")
                .param("description", "Fun toy")
                .param("price", "15.99")
                .param("quantity", "10"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/products"));

        Mockito.verify(productService).saveProduct(any(Product.class));
    }

    /**
     * Тестирует загрузку изображения через multipart-запрос.
     * Использует мок для возврата URL загруженного изображения.
     * Проверяет:
     * - HTTP статус 200 OK,
     * - JSON с полем "url" с корректным значением,
     * - content-type application/json.
     */
    @Test
    void testUploadImage() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-image.png",
                MediaType.IMAGE_PNG_VALUE,
                "dummy image content".getBytes());

        when(imageService.uploadImage(any())).thenReturn("/uploads/test-image.png");

        mockMvc.perform(multipart("/products/uploadImage")
                .file(file)
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.url").value("/uploads/test-image.png"));
    }
}
