package org.example.modules.book.service;

import org.example.common.exception.BusinessException;
import org.example.common.response.ResponseCode;
import org.example.config.UploadProperties;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class CoverStorageService {

    private static final String PUBLIC_PREFIX = "/uploads/covers/";
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif"
    );
    private static final Map<String, String> EXT_BY_TYPE = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp",
            "image/gif", ".gif"
    );

    private final Path coverDir;

    public CoverStorageService(UploadProperties properties) {
        this.coverDir = Paths.get(properties.getUploadDir()).toAbsolutePath().normalize().resolve("covers");
        try {
            Files.createDirectories(this.coverDir);
        } catch (IOException e) {
            throw new IllegalStateException("无法创建封面目录: " + this.coverDir, e);
        }
    }

    public Path uploadRoot() {
        return coverDir.getParent();
    }

    public String save(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResponseCode.BAD_REQUEST, "请选择封面图片");
        }
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        if (!ALLOWED_TYPES.contains(contentType)) {
            throw new BusinessException(ResponseCode.BAD_REQUEST, "仅支持 jpg / png / webp / gif");
        }
        if (file.getSize() > 2 * 1024 * 1024) {
            throw new BusinessException(ResponseCode.BAD_REQUEST, "封面大小不能超过 2MB");
        }
        String ext = EXT_BY_TYPE.get(contentType);
        String filename = UUID.randomUUID().toString().replace("-", "") + ext;
        Path target = coverDir.resolve(filename);
        try {
            file.transferTo(target.toFile());
        } catch (IOException e) {
            throw new BusinessException(ResponseCode.BAD_REQUEST, "封面保存失败");
        }
        return PUBLIC_PREFIX + filename;
    }

    public void deleteIfStored(String cover) {
        if (!StringUtils.hasText(cover) || !cover.startsWith(PUBLIC_PREFIX)) {
            return;
        }
        String name = cover.substring(PUBLIC_PREFIX.length());
        if (name.contains("..") || name.contains("/") || name.contains("\\")) {
            return;
        }
        try {
            Files.deleteIfExists(coverDir.resolve(name));
        } catch (IOException ignored) {
            // keep going
        }
    }
}
