package com.securevault.main.util;

import java.io.IOException;
import java.util.Base64;

import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ImageUtils {

	public static String getImageAsDataUrl(String imagePath) throws IOException {
		ClassPathResource resource = new ClassPathResource(imagePath);
		if (!resource.exists()) {
			throw new IOException("Resource not found: " + imagePath);
		}

		byte[] imageBytes = StreamUtils.copyToByteArray(resource.getInputStream());
		String base64Image = Base64.getEncoder().encodeToString(imageBytes);
		String mimeType = getMimeType(imagePath);

		return String.format("data:%s;base64,%s", mimeType, base64Image);
	}

	private static String getMimeType(String imagePath) {
		String path = imagePath.toLowerCase();
		if (path.endsWith(".png")) {
			return "image/png";
		} else if (path.endsWith(".jpg") || path.endsWith(".jpeg")) {
			return "image/jpeg";
		} else if (path.endsWith(".svg")) {
			return "image/svg+xml";
		}
		return "image/png";
	}
}