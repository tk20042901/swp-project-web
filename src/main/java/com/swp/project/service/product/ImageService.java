package com.swp.project.service.product;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;

import javax.imageio.ImageIO;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ImageService {
    public static final String IMAGES_FINAL_PATH = "images/products/";
    public static final String DISPLAY_FINAL_PATH = "/images/products/";

    public static String convertToBase64WithPrefix(MultipartFile file) throws IOException {
        byte[] bytes = file.getBytes();
        String base64 = Base64.getEncoder().encodeToString(bytes);

        String contentType = file.getContentType();
        if (contentType == null) {
            contentType = "image/jpeg"; // default
        }

        return String.format("data:%s;base64,%s", contentType, base64);
    }

    public String base64ToFileNIO(String base64String, String outputPath) throws IOException {
        String base64Data = base64String.contains(",")
                ? base64String.split(",")[1]
                : base64String;

        byte[] imageBytes = Base64.getDecoder().decode(base64Data);
        Path path = Paths.get(outputPath);
        Files.write(path, imageBytes);
        return outputPath;
    }

    @Async
    public CompletableFuture<String> convertFromDisplayPathToBase64(String stringPath) throws IOException {
        String result = stringPath.replace(DISPLAY_FINAL_PATH, IMAGES_FINAL_PATH);
        // Convert relative path to absolute path from project root
        File imageFile = new File(result);

        // Đọc ảnh
        BufferedImage originalImage = ImageIO.read(imageFile);
        Image scaledImage = originalImage.getScaledInstance(100, 100, Image.SCALE_SMOOTH);
        BufferedImage resizedImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);

        Graphics2D g2d = resizedImage.createGraphics();
        g2d.drawImage(scaledImage, 0, 0, null);
        g2d.dispose();

        // Ghi ảnh nén vào bộ nhớ tạm
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(resizedImage, "jpg", outputStream);

        // Chuyển sang Base64
        byte[] imageBytes = outputStream.toByteArray();
        String base64 = Base64.getEncoder().encodeToString(imageBytes);

        return CompletableFuture.completedFuture("data:image/jpeg;base64," + base64);
    }

}