package example.toyshop.Service;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import example.toyshop.model.Product;
import example.toyshop.repository.CartItemRepository;
import example.toyshop.repository.CartRepository;
import example.toyshop.repository.ProductRepository;
import jakarta.transaction.Transactional;

import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Интеграционные тесты для
 * {@link example.toyshop.controller.ProductController}.
 * 
 * <p>
 * Тестируются основные сценарии работы с продуктами через веб-интерфейс:
 * просмотр списка продуктов, просмотр отдельного продукта, показ формы
 * добавления,
 * добавление нового продукта и загрузка изображения.
 * </p>
 * 
 * <p>
 * Используется {@link MockMvc} для имитации HTTP-запросов к контроллеру.
 * </p>
 * 
 * <p>
 * Тесты запускаются в контексте Spring с использованием
 * аннотаций {@link SpringBootTest}, {@link Transactional} и
 * {@link AutoConfigureMockMvc}.
 * </p>
 */
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class ProductControllerIntegrationTest {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    private MockMvc mockMvc;

    /**
     * Настройка тестового окружения перед каждым тестом:
     * инициализация {@link MockMvc}, очистка репозиториев и добавление тестовых
     * продуктов.
     */
    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        cartItemRepository.deleteAll();
        cartRepository.deleteAll();
        productRepository.deleteAll();

        Product p1 = new Product();
        p1.setName("Машинка");
        p1.setDescription("Игрушечная машинка");
        p1.setPrice(BigDecimal.valueOf(50));
        p1.setImageUrl(null);
        p1.setQuantity(5);
        productRepository.save(p1);

        Product p2 = new Product();
        p2.setName("Пазлы");
        p2.setDescription("Детские пазлы");
        p2.setPrice(BigDecimal.valueOf(30));
        p2.setImageUrl(null);
        p2.setQuantity(3);
        productRepository.save(p2);
    }

    /**
     * Тестирует получение страницы со списком продуктов.
     * 
     * <p>
     * Проверяется, что запрос возвращает HTTP 200, используется правильный шаблон
     * и в модель передаются два продукта, включая продукт с именем "Машинка".
     * </p>
     * 
     * @throws Exception при ошибках выполнения запроса
     */
    @Test
    void testListProducts() throws Exception {
        mockMvc.perform(get("/products")
                .param("page", "0")
                .param("size", "10")
                .param("sort", "price_asc"))
                .andExpect(status().isOk())
                .andExpect(view().name("products"))
                .andExpect(model().attributeExists("products"))
                .andExpect(model().attribute("products", hasSize(2)))
                .andExpect(model().attribute("products", hasItem(
                        hasProperty("name", is("Машинка")))));
    }

    /**
     * Тестирует просмотр страницы отдельного продукта по ID.
     * 
     * <p>
     * Проверяется HTTP 200, правильное имя представления и передача продукта
     * с ожидаемым именем в модель.
     * </p>
     * 
     * @throws Exception при ошибках выполнения запроса
     */
    @Test
    void testViewProduct() throws Exception {
        Product product = productRepository.findAll().get(0);

        mockMvc.perform(get("/products/" + product.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("product"))
                .andExpect(model().attributeExists("product"))
                .andExpect(model().attribute("product", hasProperty("name", is(product.getName()))));
    }

    /**
     * Тестирует отображение формы добавления нового продукта.
     * 
     * <p>
     * Проверяется, что возвращается HTTP 200, используется шаблон "add-product"
     * и в модель передаётся пустой объект продукта.
     * </p>
     * 
     * @throws Exception при ошибках выполнения запроса
     */
    @Test
    void testShowAddProductForm() throws Exception {
        mockMvc.perform(get("/products/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("add-product"))
                .andExpect(model().attributeExists("product"));
    }

    /**
     * Тестирует добавление нового продукта через POST-запрос.
     * 
     * <p>
     * Проверяется, что после успешного добавления происходит редирект
     * на страницу списка продуктов.
     * </p>
     * 
     * @throws Exception при ошибках выполнения запроса
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
     * 
     * <p>
     * Отправляется multipart-запрос с тестовым изображением,
     * проверяется успешный ответ с JSON-объектом, содержащим URL загруженного
     * файла.
     * </p>
     * 
     * @throws Exception при ошибках выполнения запроса
     */
    @Test
    void testUploadImage() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes());

        mockMvc.perform(multipart("/products/uploadImage").file(file))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.url").isNotEmpty());
    }
}
