package example.toyshop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import example.toyshop.model.Cart;
import example.toyshop.service.CartService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

/**
 * Контроллер для управления корзиной пользователя.
 * Обрабатывает добавление, удаление и изменение количества товаров,
 * а также оформление заказа.
 */
@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    /**
     * Отображает текущую корзину пользователя.
     * Идентификатор сессии используется для поиска активной корзины.
     *
     * @param request HTTP-запрос для получения сессии
     * @param model   модель для передачи данных в представление
     * @return имя шаблона страницы корзины
     */
    @GetMapping
    public String viewCart(HttpServletRequest request, Model model) {
        String sessionId = request.getSession(true).getId();
        Cart cart = cartService.getActiveCartBySessionId(sessionId);
        model.addAttribute("cart", cart);
        return "cart";
    }

    /**
     * Добавляет товар в корзину пользователя.
     *
     * @param productId идентификатор добавляемого продукта
     * @param request   HTTP-запрос для получения сессии
     * @return редирект на страницу списка продуктов
     */
    @PostMapping("/add/{productId}")
    public String addToCart(@PathVariable Long productId,
                            HttpServletRequest request) {
        String sessionId = request.getSession(true).getId();
        cartService.addToCart(sessionId, productId);
        return "redirect:/products";
    }

    /**
     * Удаляет товар из корзины.
     *
     * @param productId идентификатор удаляемого продукта
     * @param request   HTTP-запрос для получения сессии
     * @return редирект на страницу корзины
     */
    @PostMapping("/remove/{productId}")
    public String removeFromCart(@PathVariable Long productId, HttpServletRequest request) {
        String sessionId = request.getSession(true).getId();
        cartService.removeFromCart(sessionId, productId);
        return "redirect:/cart";
    }

    /**
     * Увеличивает количество товара в корзине на 1.
     *
     * @param productId идентификатор товара
     * @param request   HTTP-запрос для получения сессии
     * @return редирект на страницу корзины
     */
    @PostMapping("/increase/{productId}")
    public String increaseItem(@PathVariable Long productId, HttpServletRequest request) {
        String sessionId = request.getSession(true).getId();
        cartService.increaseItem(sessionId, productId);
        return "redirect:/cart";
    }

    /**
     * Уменьшает количество товара в корзине на 1.
     *
     * @param productId идентификатор товара
     * @param request   HTTP-запрос для получения сессии
     * @return редирект на страницу корзины
     */
    @PostMapping("/decrease/{productId}")
    public String decreaseItem(@PathVariable Long productId, HttpServletRequest request) {
        String sessionId = request.getSession(true).getId();
        cartService.decreaseItem(sessionId, productId);
        return "redirect:/cart";
    }

    /**
     * Оформляет заказ — переводит корзину в статус оформленного заказа,
     * инвалидирует текущую сессию для создания новой корзины.
     *
     * @param request HTTP-запрос для получения сессии
     * @return редирект на страницу просмотра заказа
     */
    @PostMapping("/checkout")
    public String checkout(HttpServletRequest request) {
        String sessionId = request.getSession(true).getId();

        Cart completedOrder = cartService.checkout(sessionId);

        request.getSession().invalidate();

        return "redirect:/orders/" + completedOrder.getId();
    }
}