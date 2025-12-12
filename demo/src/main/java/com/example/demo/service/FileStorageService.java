package com.example.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {

    // nếu không khai báo app.upload-dir thì mặc định = "uploads"
    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    public String storeProductImage(Long productId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File rỗng");
        }
        if (productId == null) {
            throw new IllegalStateException("productId null khi lưu ảnh");
        }

        try {
            Path root = Paths.get(uploadDir);
            Path folder = root.resolve("products").resolve(productId.toString());

            log.info("[FileStorage] uploadDir = {}", root.toAbsolutePath());
            log.info("[FileStorage] Thư mục ảnh sản phẩm = {}", folder.toAbsolutePath());

            Files.createDirectories(folder);

            String original = file.getOriginalFilename();
            String ext = "";
            if (original != null && original.contains(".")) {
                ext = original.substring(original.lastIndexOf("."));
            }

            String filename = UUID.randomUUID() + ext;
            Path target = folder.resolve(filename);

            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }

            String url = "/products/" + productId + "/" + filename;
            log.info("[FileStorage] Đã lưu ảnh: {}", url);
            return url;

        } catch (IOException e) {
            log.error("[FileStorage] IOException khi lưu ảnh: {}", e.getMessage(), e);
            throw new RuntimeException("Không lưu được file ảnh trên server", e);
        } catch (Exception e) {
            log.error("[FileStorage] Lỗi bất ngờ khi lưu ảnh", e);
            throw new RuntimeException("Không lưu được file ảnh trên server", e);
        }
    }
}
