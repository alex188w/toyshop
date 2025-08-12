package example.toyshop.controller;

import example.toyshop.model.Product;
import example.toyshop.service.ImageService;
import example.toyshop.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;
import org.springframework.http.MediaType;

/**
 * Контроллер для управления товарами.
 */
@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ImageService imageService;

    /**
     * Отображает список товаров с возможностью поиска, сортировки и пагинации.
     *
     * @param keyword параметр поиска по названию товара (необязательный)
     * @param sort    способ сортировки (например, "price_asc", "name_desc")
     * @param page    номер страницы (начинается с 0)
     * @param size    количество товаров на странице
     * @param model   модель для передачи данных в представление
     * @return имя шаблона страницы со списком товаров
     */
    @GetMapping
    public String listProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "price_asc") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        Page<Product> products = productService.getProducts(keyword, sort, page, size);

        model.addAttribute("products", products.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", products.getTotalPages());
        model.addAttribute("keyword", keyword);
        model.addAttribute("sort", sort);
        model.addAttribute("size", size);

        return "products";
    }

    /**
     * Отображает страницу с деталями одного товара по его ID.
     *
     * @param id    идентификатор товара
     * @param model модель для передачи данных в представление
     * @return имя шаблона страницы с деталями товара
     */
    @GetMapping("/{id}")
    public String viewProduct(@PathVariable Long id, Model model) {
        model.addAttribute("product", productService.getProductById(id));
        return "product";
    }

    /**
     * Показывает форму для добавления нового товара.
     *
     * @param model модель для передачи нового объекта товара в представление
     * @return имя шаблона страницы с формой добавления товара
     */
    @GetMapping("/add")
    public String showAddProductForm(Model model) {
        model.addAttribute("product", new Product());
        return "add-product";
    }

    /**
     * Обрабатывает отправку формы для добавления нового товара.
     *
     * @param product объект товара, заполненный из формы
     * @return редирект на страницу списка товаров
     */
    @PostMapping("/add")
    public String addProduct(
            @Valid @ModelAttribute Product product,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {
            // Вернуть пользователя на форму с ошибками
            model.addAttribute("product", product);
            return "add-product";
        }

        productService.saveProduct(product);
        return "redirect:/products";
    }

    /**
     * Обрабатывает загрузку изображения товара через multipart запрос.
     *
     * @param file загружаемый файл изображения
     * @return JSON-ответ с URL загруженного изображения
     */
    @PostMapping(value = "/uploadImage", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, String> uploadImage(@RequestParam("file") MultipartFile file) {
        String fileUrl = imageService.uploadImage(file);
        return Map.of("url", fileUrl);
    }
}
