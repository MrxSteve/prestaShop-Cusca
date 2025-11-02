package com.cusca.shopmoney_pg.services.images;

import com.cloudinary.Cloudinary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public String uploadImage(MultipartFile file) {
        try {
            // Validar que sea una imagen
            if (!isValidImageFile(file)) {
                throw new RuntimeException("El archivo debe ser una imagen válida (JPG, JPEG, PNG, GIF, WEBP, BMP)");
            }

            Map<String, Object> uploadParams = new HashMap<>();
            uploadParams.put("folder", "productos"); // Carpeta en Cloudinary
            uploadParams.put("resource_type", "image");
            uploadParams.put("width", 800);
            uploadParams.put("height", 600);
            uploadParams.put("crop", "limit");
            uploadParams.put("quality", "auto");
            uploadParams.put("fetch_format", "auto"); // Cloudinary elige el mejor formato (WebP, AVIF, etc.)

            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);
            return (String) uploadResult.get("secure_url");

        } catch (Exception e) {
            log.error("Error uploading image to Cloudinary: ", e);
            throw new RuntimeException("Error al subir la imagen: " + e.getMessage());
        }
    }

    private boolean isValidImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        String contentType = file.getContentType();
        if (contentType == null) {
            return false;
        }

        // Formatos de imagen comunes aceptados
        return contentType.equals("image/jpeg") ||
               contentType.equals("image/jpg") ||
               contentType.equals("image/png") ||
               contentType.equals("image/gif") ||
               contentType.equals("image/webp") ||
               contentType.equals("image/bmp") ||
               contentType.equals("image/svg+xml");
    }

    public boolean deleteImage(String imageUrl) {
        try {
            if (imageUrl == null || imageUrl.isEmpty()) {
                return true;
            }

            String publicId = extractPublicIdFromUrl(imageUrl);
            Map<?, ?> result = cloudinary.uploader().destroy(publicId, Map.of());
            return "ok".equals(result.get("result"));

        } catch (Exception e) {
            log.error("Error deleting image from Cloudinary: ", e);
            return false;
        }
    }

    private String extractPublicIdFromUrl(String url) {
        // Extrae el public_id de la URL de Cloudinary
        // Ej: https://res.cloudinary.com/cloud/image/upload/v123/productos/abc123.jpg
        // Retorna: productos/abc123
        String[] parts = url.split("/");
        int uploadIndex = -1;
        for (int i = 0; i < parts.length; i++) {
            if ("upload".equals(parts[i])) {
                uploadIndex = i;
                break;
            }
        }

        if (uploadIndex != -1 && uploadIndex + 2 < parts.length) {
            String fileName = parts[parts.length - 1];
            String folderPath = String.join("/", Arrays.copyOfRange(parts, uploadIndex + 2, parts.length - 1));
            String fileNameWithoutExtension = fileName.substring(0, fileName.lastIndexOf('.'));
            return folderPath + "/" + fileNameWithoutExtension;
        }

        return "";
    }
}
