package example.toyshop.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Сущность корзины покупок.
 * Хранит список товаров, связанные с конкретной сессией пользователя.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cart {

    /**
     * Уникальный идентификатор корзины.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Идентификатор сессии пользователя (гостя),
     * для которого создана корзина.
     */
    private String sessionId;

    /**
     * Статус корзины (например, ACTIVE - активна, ORDERED - оформлена и т.п.).
     */
    @Enumerated(EnumType.STRING)
    private CartStatus status = CartStatus.ACTIVE;

    /**
     * Список товаров (элементов корзины), входящих в эту корзину.
     * Связь "один ко многим" с сущностью CartItem.
     * 
     * Исключён из методов toString(), equals() и hashCode(), чтобы избежать циклических ссылок.
     */
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<CartItem> items = new ArrayList<>();

    /**
     * Дата и время создания корзины.
     * Инициализируется при создании объекта.
     */
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Вычисляет общую стоимость всех товаров в корзине,
     * суммируя стоимость каждого элемента.
     * 
     * @return общая сумма стоимости товаров в корзине
     */
    public BigDecimal getTotalAmount() {
        return items.stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
