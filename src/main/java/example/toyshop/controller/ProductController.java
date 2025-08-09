package example.toyshop.controller;

import example.toyshop.model.Product;
import example.toyshop.repository.ProductRepository;
import example.toyshop.service.ProductService;
import lombok.RequiredArgsConstructor;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductRepository productRepository;

    @GetMapping
    public String listProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "price_asc") String sort,
            @RequestParam(defaultValue = "0") int page,
            // задаём, сколько товаров будет по умолчанию без параметра в URL
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        Sort sorting;
        switch (sort) {
            case "price_desc":
                sorting = Sort.by(Sort.Direction.DESC, "price");
                break;
            case "name_asc":
                sorting = Sort.by(Sort.Direction.ASC, "name");
                break;
            case "name_desc":
                sorting = Sort.by(Sort.Direction.DESC, "name");
                break;
            default:
                sorting = Sort.by(Sort.Direction.ASC, "price");
        }

        Pageable pageable = PageRequest.of(page, size, sorting);

        Page<Product> products;
        if (keyword != null && !keyword.isEmpty()) {
            products = productRepository.findByNameContainingIgnoreCase(keyword, pageable);
        } else {
            products = productRepository.findAll(pageable);
        }

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
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Товар не найден"));

        model.addAttribute("product", product);
        return "product"; // шаблон product.html
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
        productRepository.save(product);
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
        try {
            String uploadDir = "C:/myapp/uploads/toyshop";
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filepath = Paths.get(uploadDir, filename);
            Files.createDirectories(filepath.getParent());
            file.transferTo(filepath);

            String fileUrl = "/uploads/" + filename;

            return Map.of("url", fileUrl);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Ошибка загрузки файла: " + e.getMessage(), e);
        }
    }
}
