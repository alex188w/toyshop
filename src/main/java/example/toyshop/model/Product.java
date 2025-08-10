package example.toyshop.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Сущность продукта (товара) в магазине.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    /**
     * Уникальный идентификатор продукта.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Название продукта.
     */
    private String name;

    /**
     * Описание продукта.
     */
    private String description;

    /**
     * Цена продукта.
     */
    private BigDecimal price;

    /**
     * URL изображения продукта.
     */
    private String imageUrl;

    /**
     * Количество продукта в наличии на складе.
     */
    private int quantity;
}
