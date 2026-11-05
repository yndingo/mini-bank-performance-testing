package com.example.minibank.auth;

/*
 * Вычисляет SHA-256-хеш токена доступа.
 *
 * Используется при выдаче токена и позже — при его проверке.
 * Результат: строка из 64 шестнадцатеричных символов.
 *
 * Не предназначен для хеширования пользовательских паролей:
 * для паролей в проекте используется BCrypt.
 */

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class AccessTokenHasher {

    private AccessTokenHasher() {
    }

    public static String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    token.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 is not available",
                    exception
            );
        }
    }
}