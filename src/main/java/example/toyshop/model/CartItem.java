package example.toyshop.model;

import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Элемент корзины — связывает товар с корзиной и количеством.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {

    /**
     * Уникальный идентификатор элемента корзины.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Корзина, к которой принадлежит этот элемент.
     */
    @ManyToOne
    @JoinColumn(name = "cart_id")
    private Cart cart;

    /**
     * Товар, добавленный в корзину.
     */
    @ManyToOne
    private Product product;

    /**
     * Количество единиц товара в корзине.
     */
    private int quantity;

    /**
     * Вычисляет общую цену этого элемента корзины,
     * умножая цену товара на количество.
     *
     * @return общая стоимость (цена * количество)
     */
    public BigDecimal getTotalPrice() {
        return product.getPrice().multiply(BigDecimal.valueOf(quantity));
    }
}
