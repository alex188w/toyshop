package example.toyshop.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.*;
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
    @NotBlank(message = "Название обязательно")
    @Size(min = 2, max = 100, message = "Название должно быть от 2 до 100 символов")
    private String name;

    /**
     * Описание продукта.
     */
    @NotBlank(message = "Описание обязательно")
    @Size(min = 10, max = 1000, message = "Описание должно быть от 10 до 1000 символов")
    private String description;

    /**
     * Цена продукта.
     */
    @NotNull(message = "Цена обязательна")
    @DecimalMin(value = "0.01", message = "Цена должна быть больше 0")
    private BigDecimal price;

    /**
     * URL изображения продукта.
     */
    @NotBlank(message = "URL изображения обязателен")
    @Pattern(regexp = "^(http|https)://.*$", message = "URL изображения должен быть валидным")
    private String imageUrl;

    /**
     * Количество продукта в наличии на складе.
     */
    @Min(value = 0, message = "Количество не может быть отрицательным")
    private int quantity;
}
