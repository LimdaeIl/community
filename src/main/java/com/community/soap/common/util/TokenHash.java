package com.community.soap.common.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class TokenHash {

    private static final String PEPPER = resolvePepper();

    private static String resolvePepper() {
        // 1) System properties 우선 (java-dotenv가 여기에 넣어줌)
        String p = System.getProperty("TOKEN_PEPPER");
        if (p == null || p.isBlank()) {
            // 2) 환경변수 보조
            p = System.getenv("TOKEN_PEPPER");
        }
        if (p == null || p.isBlank()) {
            // 3) 없으면 바로 실패 (보안상 반드시 존재해야 함)
            throw new IllegalStateException("Missing TOKEN_PEPPER (system property or environment variable).");
        }
        return p;
    }

    public static String sha256(String token) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] b = md.digest((token + PEPPER).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte x : b) sb.append(String.format("%02x", x));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
