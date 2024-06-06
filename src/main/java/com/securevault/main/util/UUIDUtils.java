package com.securevault.main.util;

import java.util.UUID;

public class UUIDUtils {

	public static UUID fromHexString(String hexString) {
		if (hexString == null || hexString.isEmpty()) {
			throw new IllegalArgumentException("Invalid hexadecimal UUID string: " + hexString);
		}

		String uuidString = hexString.substring(0, 8) + "-" + hexString.substring(8, 12) + "-" + hexString.substring(12, 16)
				+ "-" + hexString.substring(16, 20) + "-" + hexString.substring(20);

		return UUID.fromString(uuidString);

	}
}