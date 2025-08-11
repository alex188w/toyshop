package example.toyshop.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Конфигурация веб-ресурсов приложения.
 * <p>
 * Регистрирует обработчик статических ресурсов для отдачи
 * загруженных файлов (например, изображений) из локальной директории,
 * путь к которой задаётся через параметр конфигурации {@code upload.dir}.
 * </p>
 * <p>
 * Позволяет обращаться к загруженным файлам по URL-пути {@code /uploads/**}.
 * </p>
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Путь к директории с загруженными изображениями.
     * Значение берётся из конфигурационного свойства {@code upload.dir}.
     */
    @Value("${upload.dir}")
    private String uploadDir;

    /**
     * Регистрирует обработчик ресурсов для отдачи файлов.
     * <p>
     * Сопоставляет URL-путь {@code /uploads/**} с файловой системой,
     * где расположена директория с загруженными файлами.
     * Путь к директории преобразуется в URL-формат с правильными
     * разделителями и завершающим слэшем.
     * </p>
     *
     * @param registry реестр обработчиков ресурсов Spring MVC
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String resourceLocation = "file:///" + uploadDir.replace("\\", "/");
        if (!resourceLocation.endsWith("/")) {
            resourceLocation += "/";
        }
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(resourceLocation);
    }
}
