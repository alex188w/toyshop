package example.toyshop.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

/**
 * Обрабатывает загрузку изображений через multipart.
 *
 * @param file файл изображения
 * @return JSON с URL загруженного изображения
 */
@Service
public class ImageService {

    /**
     * Директория для сохранения загруженных файлов.
     */
    private final String uploadDir;

    /**
     * Конструктор сервиса, в который внедряется путь к директории загрузок из настроек.
     *
     * @param uploadDir путь к директории для хранения загруженных изображений
     */
    public ImageService(@Value("${upload.dir}") String uploadDir) {
        this.uploadDir = uploadDir;
    }

    /**
     * Загружает изображение на сервер.
     * Файл сохраняется в директорию uploadDir с уникальным именем.
     *
     * @param file multipart файл изображения
     * @return URL для доступа к загруженному файлу (относительно корня сервера)
     * @throws RuntimeException в случае ошибки при сохранении файла
     */
    public String uploadImage(MultipartFile file) {
        try {
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filepath = Paths.get(uploadDir, filename);

            // Создаём директорию, если её нет
            Files.createDirectories(filepath.getParent());

            // Сохраняем файл
            file.transferTo(filepath);

            // Возвращаем относительный URL для доступа к файлу (например, /uploads/filename.jpg)
            return "/uploads/" + filename;
        } catch (Exception e) {
            throw new RuntimeException("Ошибка загрузки файла: " + e.getMessage(), e);
        }
    }
}
