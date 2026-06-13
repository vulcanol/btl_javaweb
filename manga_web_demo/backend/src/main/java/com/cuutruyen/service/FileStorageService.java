package com.cuutruyen.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {
    private final String uploadDir = "uploads";

    public FileStorageService() {
        try {
            Files.createDirectories(Paths.get(uploadDir));
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage", e);
        }
    }

    public String saveFile(MultipartFile file, String subDir) {
        try {
            Path targetDir = Paths.get(uploadDir, subDir);
            Files.createDirectories(targetDir);

            String originalName = file.getOriginalFilename();
            if (originalName == null || originalName.isEmpty()) {
                originalName = "unnamed_file";
            } else {
                // Extract only the file name in case browsers send full paths
                originalName = Paths.get(originalName).getFileName().toString();
            }

            String fileName = UUID.randomUUID().toString() + "_" + originalName;
            Path targetPath = targetDir.resolve(fileName);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            return "/uploads/" + subDir + "/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Could not save file", e);
        }
    }
}
