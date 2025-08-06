package example.toyshop.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import example.toyshop.model.Cart;
import example.toyshop.model.CartStatus;
import example.toyshop.repository.CartRepository;
import example.toyshop.service.CartService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final CartRepository cartRepository;

    @GetMapping
    public String viewCart(HttpServletRequest request, Model model) {
        String sessionId = request.getSession(true).getId();
        Cart cart = cartService.getCartBySessionId(sessionId);
        model.addAttribute("cart", cart);
        return "cart";
    }

    @PostMapping("/add/{productId}")
    public String addToCart(@PathVariable Long productId,
            HttpServletRequest request) {
        String sessionId = request.getSession(true).getId();
        cartService.addToCart(sessionId, productId);
        return "redirect:/products";
    }

    @PostMapping("/remove/{productId}")
    public String removeFromCart(@PathVariable Long productId, HttpServletRequest request) {
        String sessionId = request.getSession(true).getId();
        cartService.removeFromCart(sessionId, productId);
        return "redirect:/cart";
    }

    @PostMapping("/increase/{productId}")
    public String increaseItem(@PathVariable Long productId, HttpServletRequest request) {
        String sessionId = request.getSession(true).getId();
        cartService.increaseItem(sessionId, productId);
        return "redirect:/cart";
    }

    @PostMapping("/decrease/{productId}")
    public String decreaseItem(@PathVariable Long productId, HttpServletRequest request) {
        String sessionId = request.getSession(true).getId();
        cartService.decreaseItem(sessionId, productId);
        return "redirect:/cart";
    }

    @PostMapping("/checkout")
    public String checkout(HttpServletRequest request) {
        String sessionId = request.getSession(true).getId();
        cartService.checkout(sessionId);
        return "redirect:/orders";
    }

    // @GetMapping("/orders")
    // public String orderHistory(HttpServletRequest request, Model model) {
    //     String sessionId = request.getSession(true).getId();
    //     List<Cart> orders = cartRepository.findAllBySessionIdAndStatus(sessionId, CartStatus.COMPLETED);
    //     model.addAttribute("orders", orders);
    //     return "orders"; // создадим шаблон orders.html
    // }
}