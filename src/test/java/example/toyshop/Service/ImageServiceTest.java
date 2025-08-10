package example.toyshop.Service;

import org.springframework.web.multipart.MultipartFile;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import example.toyshop.service.ImageService;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ImageServiceTest {

    private Path tempDir;
    private ImageService imageService;

    @BeforeEach
    void setup() throws Exception {
        // Создаём временную директорию для загрузок
        tempDir = Files.createTempDirectory("upload-test-");
        imageService = new ImageService(tempDir.toString());
    }

    @Test
    void testUploadImage_Success() throws Exception {
        // Создаём мок MultipartFile с тестовым содержимым
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test-image.png",
                "image/png",
                "dummy content".getBytes()
        );

        // Вызываем метод
        String resultPath = imageService.uploadImage(mockFile);

        // Проверяем, что возвращаемый путь содержит /uploads/
        assertTrue(resultPath.startsWith("/uploads/"));

        // Проверяем, что файл реально создался в директории
        String filename = resultPath.substring("/uploads/".length());
        Path savedFile = tempDir.resolve(filename);
        assertTrue(Files.exists(savedFile));

        // Сравниваем содержимое файла
        byte[] savedBytes = Files.readAllBytes(savedFile);
        assertArrayEquals("dummy content".getBytes(), savedBytes);

        // Удаляем временный файл после проверки (можно в @AfterEach)
        Files.deleteIfExists(savedFile);
    }

    @Test
    void testUploadImage_Failure() {
        MultipartFile failingFile = mock(MultipartFile.class);

        // Заставим метод transferTo выбрасывать исключение
        try {
            doThrow(new RuntimeException("Fail transfer")).when(failingFile).transferTo(any(Path.class));
        } catch (Exception e) {
            fail("Setup mock failed");
        }

        RuntimeException ex = assertThrows(RuntimeException.class, () -> imageService.uploadImage(failingFile));
        assertTrue(ex.getMessage().contains("Ошибка загрузки файла"));
        assertTrue(ex.getCause().getMessage().contains("Fail transfer"));
    }
}
