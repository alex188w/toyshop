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

/**
 * Unit-тесты для {@link ImageService}.
 * 
 * <p>
 * Покрываются сценарии успешной загрузки изображения
 * и обработки ошибок при загрузке.
 * </p>
 */
class ImageServiceTest {

    private Path tempDir;
    private ImageService imageService;

    /**
     * Создаёт временную директорию и инициализирует {@link ImageService}
     * перед каждым тестом.
     * 
     * @throws Exception при ошибках создания временной директории
     */
    @BeforeEach
    void setup() throws Exception {
        tempDir = Files.createTempDirectory("upload-test-");
        imageService = new ImageService(tempDir.toString());
    }

    /**
     * Проверяет успешную загрузку изображения.
     * 
     * <p>
     * Создаётся мок объекта {@link org.springframework.web.multipart.MultipartFile}
     * с тестовым содержимым,
     * вызывается метод {@link ImageService#uploadImage(MultipartFile)},
     * проверяется корректность возвращаемого пути,
     * а также реальное сохранение файла с ожидаемым содержимым в файловой системе.
     * </p>
     * 
     * @throws Exception при ошибках работы с файлами
     */
    @Test
    void testUploadImage_Success() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test-image.png",
                "image/png",
                "dummy content".getBytes());

        String resultPath = imageService.uploadImage(mockFile);

        assertTrue(resultPath.startsWith("/uploads/"));

        String filename = resultPath.substring("/uploads/".length());
        Path savedFile = tempDir.resolve(filename);
        assertTrue(Files.exists(savedFile));

        byte[] savedBytes = Files.readAllBytes(savedFile);
        assertArrayEquals("dummy content".getBytes(), savedBytes);

        Files.deleteIfExists(savedFile);
    }

    /**
     * Проверяет корректную обработку исключений при ошибке загрузки файла.
     * 
     * <p>
     * Создаётся мок MultipartFile, метод {@code transferTo} которого настроен
     * на выброс исключения. Проверяется, что метод
     * {@link ImageService#uploadImage(MultipartFile)}
     * выбрасывает {@link RuntimeException} с ожидаемым сообщением и вложенной
     * причиной.
     * </p>
     */
    @Test
    void testUploadImage_Failure() {
        MultipartFile failingFile = mock(MultipartFile.class);

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
