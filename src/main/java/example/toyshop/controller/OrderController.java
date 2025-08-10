package example.toyshop.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import example.toyshop.model.Cart;
import example.toyshop.model.CartStatus;
import example.toyshop.repository.CartRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

/**
 * Контроллер для управления заказами (завершёнными корзинами).
 */
@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final CartRepository cartRepository;

    /**
     * Отображает список всех завершённых заказов.
     * 
     * @param request HTTP-запрос (может быть использован для идентификации пользователя, если потребуется)
     * @param model   модель для передачи данных в представление
     * @return имя шаблона страницы со списком заказов
     */
    @GetMapping
    public String viewOrders(HttpServletRequest request, Model model) {
        List<Cart> completedOrders = cartRepository.findByStatus(CartStatus.COMPLETED);
        model.addAttribute("orders", completedOrders);
        return "orders";
    }

    /**
     * Отображает детали одного конкретного заказа по его идентификатору.
     * 
     * @param id    идентификатор заказа
     * @param model модель для передачи данных в представление
     * @return имя шаблона страницы с деталями заказа
     * @throws ResponseStatusException с кодом 404, если заказ не найден
     */
    @GetMapping("/{id}")
    public String viewOrder(@PathVariable Long id, Model model) {
        Cart order = cartRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Заказ не найден"));

        model.addAttribute("order", order);
        return "order"; // order.html
    }
}
