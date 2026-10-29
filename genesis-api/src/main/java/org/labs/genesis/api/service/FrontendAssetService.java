package org.labs.genesis.api.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FrontendAssetService {

    private static final long MAX_SIZE = 2 * 1024 * 1024;

    private final Path storage;
    private final Map<String, Path> assets = new ConcurrentHashMap<>();

    public FrontendAssetService() throws IOException {
        storage = Files.createTempDirectory("genesis-frontend-assets-").toRealPath();
    }

    public String upload(String kind, MultipartFile file) throws IOException {

        if (!kind.equals("logo") && !kind.equals("favicon")) {
            throw new IllegalArgumentException("Type de ressource invalide");
        }

        if (file.isEmpty() || file.getSize() > MAX_SIZE) {
            throw new IllegalArgumentException("Image vide ou supérieure à 2 Mo");
        }

        byte[] bytes = file.getBytes();

        String extension;
        if (bytes.length >= 8
                && (bytes[0] & 0xff) == 0x89
                && bytes[1] == 0x50
                && bytes[2] == 0x4E
                && bytes[3] == 0x47) {
            extension = ".png";
        } else if (bytes.length >= 3
                && (bytes[0] & 0xff) == 0xFF
                && (bytes[1] & 0xff) == 0xD8
                && (bytes[2] & 0xff) == 0xFF) {
            extension = ".jpg";
        } else if (bytes.length >= 4
                && bytes[0] == 0
                && bytes[1] == 0
                && (bytes[2] & 0xff) == 1
                && bytes[3] == 0) {
            extension = ".ico";
        } else {
            throw new IllegalArgumentException("Format non accepté : PNG, JPEG ou ICO requis");
        }

        if (kind.equals("favicon") && extension.equals(".jpg")) {
            throw new IllegalArgumentException("Favicon JPEG non accepté");
        }

        String token = UUID.randomUUID().toString();
        Path destination = storage.resolve(token + extension);

        Files.write(destination, bytes, StandardOpenOption.CREATE_NEW);

        assets.put(token, destination);

        return token;
    }

    public Path resolve(String token) {
        Path path = assets.get(token);

        if (path == null || !Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)) {
            throw new IllegalArgumentException("Ressource frontend inconnue");
        }

        return path;
    }

    @PreDestroy
    public void cleanup() throws IOException {
        for (Path path : assets.values()) {
            Files.deleteIfExists(path);
        }
        Files.deleteIfExists(storage);
    }
}
