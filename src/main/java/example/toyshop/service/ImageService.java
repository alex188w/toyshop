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

    private final String uploadDir;

    public ImageService(@Value("${upload.dir}") String uploadDir) {
        this.uploadDir = uploadDir;
    }

    public String uploadImage(MultipartFile file) {
        try {
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filepath = Paths.get(uploadDir, filename);

            Files.createDirectories(filepath.getParent());
            file.transferTo(filepath);

            return "/uploads/" + filename;
        } catch (Exception e) {
            throw new RuntimeException("Ошибка загрузки файла: " + e.getMessage(), e);
        }
    }
}
