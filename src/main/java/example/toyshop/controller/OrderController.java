package example.toyshop.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import example.toyshop.model.Cart;
import example.toyshop.model.CartStatus;
import example.toyshop.repository.CartRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final CartRepository cartRepository;

    @GetMapping
    public String viewOrders(HttpServletRequest request, Model model) {
        String sessionId = request.getSession(true).getId();

        Optional<Cart> completedOrders = cartRepository.findBySessionIdAndStatus(sessionId, CartStatus.COMPLETED);

        model.addAttribute("orders", completedOrders);
        return "orders"; // orders.html
    }
}
