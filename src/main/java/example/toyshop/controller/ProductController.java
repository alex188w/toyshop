package example.toyshop.controller;

import example.toyshop.model.Product;
import example.toyshop.service.ImageService;
import example.toyshop.service.ProductService;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;
import org.springframework.http.MediaType;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ImageService imageService;

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

    @GetMapping("/{id}")
    public String viewProduct(@PathVariable Long id, Model model) {
        model.addAttribute("product", productService.getProductById(id));
        return "product";
    }

    /**
     * Показывает форму добавления нового поста.
     *
     * @param model модель с новым объектом поста
     * @return шаблон add-post.html
     */
    @GetMapping("/add")
    public String showAddProductForm(Model model) {
        model.addAttribute("product", new Product());
        return "add-product";
    }

    @PostMapping("/add")
    public String addProduct(@ModelAttribute Product product) {
        productService.saveProduct(product);
        return "redirect:/products";
    }

    /**
     * Обрабатывает загрузку изображений через multipart.
     *
     * @param file файл изображения
     * @return JSON с URL загруженного изображения
     */
    @PostMapping(value = "/uploadImage", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, String> uploadImage(@RequestParam("file") MultipartFile file) {
        String fileUrl = imageService.uploadImage(file);
        return Map.of("url", fileUrl);
    }
}
